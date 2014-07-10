package org.outermedia.solrfusion.response;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.FusionRequest;
import org.outermedia.solrfusion.adapter.ClosableListIterator;
import org.outermedia.solrfusion.adapter.SearchServerResponseInfo;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.ResponseConsolidatorFactory;
import org.outermedia.solrfusion.configuration.SearchServerConfig;
import org.outermedia.solrfusion.mapper.Term;
import org.outermedia.solrfusion.response.parser.Document;
import org.outermedia.solrfusion.types.ScriptEnv;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by ballmann on 04.06.14.
 */

/**
 * Supports sorting and paging and a limit of documents to retrieve (per search server).
 */
@ToString
@Slf4j
public class PagingResponseConsolidator extends AbstractResponseConsolidator
{
    private List<Document> allDocs;
    private int streamCounter;

    /**
     * Factory creates instances only.
     */
    private PagingResponseConsolidator()
    {
        super();
        allDocs = new ArrayList<>();
        streamCounter = 0;
    }

    @Override public void addResultStream(Configuration config, SearchServerConfig searchServerConfig,
        ClosableIterator<Document, SearchServerResponseInfo> docIterator, FusionRequest request)
    {
        streamCounter++;
        List<String> searchServerFieldsToMap = new ArrayList<>();
        searchServerFieldsToMap.add(request.getSearchServerSortField());
        try
        {
            // map sort field and id only
            MappingClosableIterator mapper = new MappingClosableIterator(docIterator, config, searchServerConfig,
                searchServerFieldsToMap);
            int docCount = 0;
            while (mapper.hasNext())
            {
                allDocs.add(mapper.next());
                docCount++;
            }
            log.debug("Added {} docs from server {}", searchServerConfig.getSearchServerName(), docCount);
        }
        catch (Exception e)
        {
            log.error("Caught exception while mapping documents of server {}", searchServerConfig.getSearchServerName(),
                e);
        }
    }

    @Override public int numberOfResponseStreams()
    {
        return streamCounter;
    }

    @Override public void clear()
    {
        allDocs = null;
    }

    @Override public ClosableIterator<Document, SearchServerResponseInfo> getResponseIterator(Configuration config,
        FusionRequest fusionRequest) throws InvocationTargetException, IllegalAccessException
    {
        String fusionSortField = fusionRequest.getSolrFusionSortField();
        // sort all docs
        boolean sortAsc = fusionRequest.isSortAsc();
        Collections.sort(allDocs, new FusionValueDocumentComparator(fusionSortField, sortAsc));
        // get docs of page
        List<Document> docsOfPage = new ArrayList<>();
        int start = fusionRequest.getStart();
        String fusionIdField = config.getIdGenerator().getFusionIdField();
        for (int i = 0; i < fusionRequest.getPageSize() && (i + start) < allDocs.size(); i++)
        {
            Document d = allDocs.get(i);
            // id was mapped too when sort field was mapped
            Term idTerm = d.getFieldTermByFusionName(fusionIdField);
            // id is always a single value
            SearchServerConfig searchServerConfig = config.getSearchServerConfigByFusionDocId(idTerm.getFusionFieldValue().get(0));
            // map whole document
            config.getResponseMapper().mapResponse(config, searchServerConfig, d, new ScriptEnv(), null);
            docsOfPage.add(d);
        }
        SearchServerResponseInfo info = new SearchServerResponseInfo(allDocs.size());
        return new ClosableListIterator<>(docsOfPage, info);
    }

    public static class Factory
    {
        public static ResponseConsolidatorIfc getInstance()
        {
            return new PagingResponseConsolidator();
        }
    }

    @Override
    public void init(ResponseConsolidatorFactory config)
    {
        // NOP
    }

}
