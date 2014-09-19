package org.outermedia.solrfusion;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.adapter.*;
import org.outermedia.solrfusion.adapter.solr.Solr1Adapter;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.SearchServerConfig;
import org.outermedia.solrfusion.response.ClosableIterator;
import org.outermedia.solrfusion.response.ResponseConsolidatorIfc;
import org.outermedia.solrfusion.response.ResponseParserIfc;
import org.outermedia.solrfusion.response.parser.Document;
import org.outermedia.solrfusion.response.parser.XmlResponse;

import java.io.InputStream;
import java.util.concurrent.Callable;

/**
 * Created by ballmann on 9/18/14.
 */
@Getter
@Setter
@Slf4j
public class SolrRequestCallable implements Callable<Object>
{
    private Configuration configuration;
    private SolrFusionUriBuilderIfc ub;
    private SearchServerConfig searchServerConfig;
    private ResponseConsolidatorIfc consolidator;
    private FusionRequest fusionRequest;

    public SolrRequestCallable(SolrFusionUriBuilderIfc ub, Configuration configuration,
        SearchServerConfig searchServerConfig, ResponseConsolidatorIfc consolidator, FusionRequest fusionRequest)
    {
        this.configuration = configuration;
        this.ub = ub;
        this.searchServerConfig = searchServerConfig;
        this.consolidator = consolidator;
        this.fusionRequest = fusionRequest;
    }

    @Override public Object call()
    {
        XmlResponse result = sendAndReceive(ub, searchServerConfig);
        Exception se = result.getErrorReason();
        if (se == null)
        {
            SearchServerResponseInfo info = new SearchServerResponseInfo(result.getNumFound(), null, null, null);
            ClosableIterator<Document, SearchServerResponseInfo> docIterator = new ClosableListIterator<>(
                result.getDocuments(), info);
            consolidator.addResultStream(searchServerConfig, docIterator, fusionRequest, result.getHighlighting(),
                result.getFacetFields(searchServerConfig.getIdFieldName(), 1));
        }
        else
        {
            consolidator.addErrorResponse(se);
        }
        return null;
    }

    protected XmlResponse sendAndReceive(SolrFusionUriBuilderIfc ub, SearchServerConfig searchServerConfig)
    {
        try
        {
            XmlResponse result;
            int timeout = configuration.getSearchServerConfigs().getTimeout();
            SearchServerAdapterIfc adapter = searchServerConfig.getInstance();
            if (ub.isBuiltForDismax())
            {
                adapter = newSolr1Adapter(adapter.getUrl());
            }
            InputStream is = adapter.sendQuery(ub, timeout);
            ResponseParserIfc responseParser = searchServerConfig.getResponseParser(
                configuration.getDefaultResponseParser());
            result = responseParser.parse(is);
            if (result == null)
            {
                result = new XmlResponse();
                result.setErrorReason(new RuntimeException("Solr response parsing failed."));
            }
            if (log.isDebugEnabled())
            {
                int docNr = -1;
                int maxDocNr = -1;
                if (result != null)
                {
                    if (result.getDocuments() != null)
                    {
                        docNr = result.getDocuments().size();
                    }
                    maxDocNr = result.getNumFound();
                }
                log.debug("Received from {}: {} of max {}", searchServerConfig.getSearchServerName(), docNr, maxDocNr);
            }
            if (log.isTraceEnabled())
            {
                log.trace("Received from {}: {}", searchServerConfig.getSearchServerName(), result.toString());
            }

            return result;
        }
        catch (SearchServerResponseException se)
        {
            return handleSearchServerResponseException(searchServerConfig, se);
        }
        catch (Exception e)
        {
            return handleGeneralResponseException(searchServerConfig, e);
        }
    }

    protected SearchServerAdapterIfc<?> newSolr1Adapter(String url)
    {
        SearchServerAdapterIfc<?> result = Solr1Adapter.Factory.getInstance();
        result.setUrl(url);
        return result;
    }

    protected XmlResponse handleGeneralResponseException(SearchServerConfig searchServerConfig, Exception e)
    {
        log.error("Caught exception while communicating with server " + searchServerConfig.getSearchServerName(), e);
        XmlResponse responseError = new XmlResponse();
        responseError.setErrorReason(e);
        return responseError;
    }

    protected XmlResponse handleSearchServerResponseException(SearchServerConfig searchServerConfig,
        SearchServerResponseException se)
    {
        log.error("Caught exception while communicating with server " + searchServerConfig.getSearchServerName(), se);

        // try to parse error response if present
        try
        {
            ResponseParserIfc responseParser = searchServerConfig.getResponseParser(
                configuration.getDefaultResponseParser());
            XmlResponse responseError = responseParser.parse(se.getHttpContent());
            if (responseError != null)
            {
                se.setResponseError(responseError.getResponseErrors());
                responseError.setErrorReason(se);
                return responseError;
            }
        }
        catch (Exception e)
        {
            // depending on solr's version, a well formed error message is provided or not
            log.warn("Couldn't parse error response", e);
        }
        XmlResponse responseError = new XmlResponse();
        responseError.setErrorReason(se);
        return responseError;
    }
}
