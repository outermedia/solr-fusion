package org.outermedia.solrfusion.response;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.FusionRequest;
import org.outermedia.solrfusion.adapter.SearchServerResponseInfo;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.ResponseConsolidatorFactory;
import org.outermedia.solrfusion.configuration.SearchServerConfig;
import org.outermedia.solrfusion.response.parser.Document;
import org.outermedia.solrfusion.response.parser.FacetHit;
import org.outermedia.solrfusion.response.parser.Highlighting;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by ballmann on 04.06.14.
 * <p/>
 * A simple consolidator which neither supports paging or sorting. All hits of all search servers are returned in a
 * round-robin manner. highlights are not supported.
 */
@ToString
@Slf4j
public class ResponseConsolidator extends AbstractResponseConsolidator
{
    private List<ClosableIterator<Document, SearchServerResponseInfo>> responseStreams;
    private Configuration configuration;

    public void initConsolidator(Configuration config)
    {
        this.configuration = config;
    }

    /**
     * Factory creates instances only.
     */
    protected ResponseConsolidator()
    {
        super();
        responseStreams = new ArrayList<>();
    }

    @Override
    public void addResultStream(SearchServerConfig searchServerConfig,
        ClosableIterator<Document, SearchServerResponseInfo> docIterator, FusionRequest request,
        List<Highlighting> highlighting, List<FacetHit> facetFields)
    {
        try
        {
            responseStreams.add(getNewMappingClosableIterator(searchServerConfig, docIterator));
        }
        catch (Exception e)
        {
            log.error("Caught exception while adding document responses of server {}",
                searchServerConfig.getSearchServerName(), e);
        }
    }

    protected MappingClosableIterator getNewMappingClosableIterator(SearchServerConfig searchServerConfig,
        ClosableIterator<Document, SearchServerResponseInfo> docIterator)
        throws InvocationTargetException, IllegalAccessException
    {
        return new MappingClosableIterator(docIterator, configuration, searchServerConfig, null);
    }

    public int numberOfResponseStreams()
    {
        return responseStreams.size();
    }

    public void clear()
    {
        for (ClosableIterator<Document, SearchServerResponseInfo> docIterator : responseStreams)
        {
            docIterator.close();
        }
        responseStreams.clear();
        errorResponses.clear();
    }

    @Override
    public ClosableIterator<Document, SearchServerResponseInfo> getResponseIterator(FusionRequest fusionRequest)
    {
        return new DefaultClosableIterator(responseStreams);
    }

    @Override public Document completelyMapMergedDoc(Collection<Document> sameDocuments,
        Map<String, Document> highlighting) throws InvocationTargetException, IllegalAccessException
    {
        // not supported
        return null;
    }

    public static class Factory
    {
        public static ResponseConsolidatorIfc getInstance()
        {
            return new ResponseConsolidator();
        }
    }

    @Override
    public void init(ResponseConsolidatorFactory config)
    {
        // NOP
    }


}
