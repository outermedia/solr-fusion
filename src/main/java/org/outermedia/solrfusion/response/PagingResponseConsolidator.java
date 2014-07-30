package org.outermedia.solrfusion.response;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.FusionRequest;
import org.outermedia.solrfusion.IdGeneratorIfc;
import org.outermedia.solrfusion.MultiKeyAndValueMap;
import org.outermedia.solrfusion.MergeStrategyIfc;
import org.outermedia.solrfusion.adapter.ClosableListIterator;
import org.outermedia.solrfusion.adapter.SearchServerResponseInfo;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.ResponseConsolidatorFactory;
import org.outermedia.solrfusion.configuration.SearchServerConfig;
import org.outermedia.solrfusion.response.parser.Document;
import org.outermedia.solrfusion.types.ScriptEnv;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Created by ballmann on 04.06.14.
 */

/**
 * Supports sorting, merging of documents and paging and a limit of documents to retrieve (per search server).
 */
@ToString
@Slf4j
public class PagingResponseConsolidator extends AbstractResponseConsolidator
{
    protected List<Document> allDocs;
    protected int streamCounter;
    private int maxDocNr;

    /**
     * Factory creates instances only.
     */
    private PagingResponseConsolidator()
    {
        super();
        allDocs = new ArrayList<>();
        streamCounter = 0;
        maxDocNr = 0;
    }

    @Override public void addResultStream(Configuration config, SearchServerConfig searchServerConfig,
        ClosableIterator<Document, SearchServerResponseInfo> docIterator, FusionRequest request)
    {
        streamCounter++;
        maxDocNr += Math.min(searchServerConfig.getMaxDocs(), docIterator.getExtraInfo().getTotalNumberOfHits());
        Set<String> searchServerFieldsToMap = new HashSet<>();
        searchServerFieldsToMap.add(request.getSearchServerSortField());
        mapMergeField(config, searchServerConfig, request, searchServerFieldsToMap);
        try
        {
            // map sort field and id/score only
            // map merge document field too if needed
            MappingClosableIterator mapper = getNewMappingClosableIterator(config, searchServerConfig, docIterator,
                searchServerFieldsToMap);
            int docCount = 0;
            while (mapper.hasNext())
            {
                allDocs.add(mapper.next());
                docCount++;
            }
            log.debug("Added {} docs from server {}", docCount, searchServerConfig.getSearchServerName());
        }
        catch (Exception e)
        {
            log.error("Caught exception while mapping documents of server {}", searchServerConfig.getSearchServerName(),
                e);
        }
        docIterator.close();
    }

    protected MappingClosableIterator getNewMappingClosableIterator(Configuration config,
        SearchServerConfig searchServerConfig, ClosableIterator<Document, SearchServerResponseInfo> docIterator,
        Set<String> searchServerFieldsToMap) throws InvocationTargetException, IllegalAccessException
    {
        return new MappingClosableIterator(docIterator, config, searchServerConfig, searchServerFieldsToMap);
    }

    protected void mapMergeField(Configuration config, SearchServerConfig searchServerConfig, FusionRequest request,
        Set<String> searchServerFieldsToMap)
    {
        String mergeField = documentMergingWanted(config);
        if (mergeField != null)
        {
            try
            {
                Set<String> candidates = request.mapFusionFieldToSearchServerField(mergeField, config,
                    searchServerConfig);
                if (candidates.isEmpty())
                {
                    log.error("Found not mapping for merge field '{}'", mergeField);
                }
                searchServerFieldsToMap.addAll(candidates);
            }
            catch (Exception e)
            {
                log.error("Can't map merge field {}", mergeField, e);
            }
        }
    }

    /**
     * Get the fusion field name which is used to detect same documents.
     *
     * @param configuration
     * @return null if merging is not configured or the fusion field.
     */
    protected String documentMergingWanted(Configuration configuration)
    {
        String result = null;
        try
        {
            MergeStrategyIfc merger = configuration.getMerger();
            if (merger != null)
            {
                result = merger.getFusionField();
            }
        }
        catch (Exception e)
        {
            log.error("Couldn't get document merger instance.", e);
        }
        return result;
    }

    @Override public int numberOfResponseStreams()
    {
        return streamCounter;
    }

    @Override public void clear()
    {
        allDocs.clear();
    }

    @Override public ClosableIterator<Document, SearchServerResponseInfo> getResponseIterator(Configuration config,
        FusionRequest fusionRequest) throws InvocationTargetException, IllegalAccessException
    {
        String fusionSortField = fusionRequest.getSolrFusionSortField();
        MultiKeyAndValueMap<String, Document> lookup = null;

        // merge all docs (at least id is merged)
        String fusionMergeField = documentMergingWanted(config);
        if (fusionMergeField != null)
        {
            lookup = mergeDocuments(config);
        }

        // sort all docs
        boolean sortAsc = fusionRequest.isSortAsc();
        Collections.sort(allDocs, new FusionValueDocumentComparator(fusionSortField, sortAsc));

        // get docs of page
        List<Document> docsOfPage = new ArrayList<>();
        int start = fusionRequest.getStart();
        IdGeneratorIfc idGenerator = config.getIdGenerator();
        String fusionIdField = idGenerator.getFusionIdField();
        for (int i = 0; i < fusionRequest.getPageSize() && (i + start) < allDocs.size(); i++)
        {
            Document d = allDocs.get(start + i);
            // id was mapped too when sort field was mapped
            String fusionDocId = d.getFusionDocId(fusionIdField);
            if (idGenerator.isMergedDocument(fusionDocId))
            {
                String mergeFieldValue = d.getFusionValuesOf(fusionMergeField).get(0);
                // all values of fusionMergeField point to the same container which holds the same documents
                Set<Document> sameDocuments = lookup.get(mergeFieldValue);
                completelyMapMergedDoc(config, fusionIdField, sameDocuments);
            }
            else
            {
                completelyMapDoc(config, d, fusionDocId);
            }
            docsOfPage.add(d);
        }
        SearchServerResponseInfo info = new SearchServerResponseInfo(maxDocNr);
        return new ClosableListIterator<>(docsOfPage, info);
    }

    protected void completelyMapDoc(Configuration config, Document d, String fusionDocId)
        throws InvocationTargetException, IllegalAccessException
    {
        SearchServerConfig searchServerConfig = config.getSearchServerConfigByFusionDocId(fusionDocId);
        config.getResponseMapper().mapResponse(config, searchServerConfig, d, getNewScriptEnv(), null);
    }

    /**
     * Apply all mappings to the given list of documents. This method expects, that the "id" field of the documents has
     * already been mapped.
     *
     * @param config
     * @param fusionIdField
     * @param sameDocuments
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    @Override
    public Document completelyMapMergedDoc(Configuration config, String fusionIdField,
        Collection<Document> sameDocuments) throws InvocationTargetException, IllegalAccessException
    {
        Document result;
        MergeStrategyIfc merger = config.getMerger();
        // map documents to merge (d is one entry of sameDocuments)
        for (Document toMerge : sameDocuments)
        {
            completelyMapDoc(config, toMerge, toMerge.getFusionDocId(fusionIdField));
        }
        if (merger != null)
        {
            result = merger.mergeDocuments(config, sameDocuments);
        }
        else
        {
            // no merger configured, sameDocuments contains exactly one document
            result = sameDocuments.iterator().next();
        }
        return result;
    }

    protected ScriptEnv getNewScriptEnv()
    {
        return new ScriptEnv();
    }

    protected MultiKeyAndValueMap<String, Document> mergeDocuments(Configuration config)
        throws InvocationTargetException, IllegalAccessException
    {
        MergeStrategyIfc merger = config.getMerger();
        String mergeFusionField = merger.getFusionField();
        List<Document> newAllDocs = new ArrayList<>();
        MultiKeyAndValueMap<String, Document> lookup = new MultiKeyAndValueMap();
        for (Document doc : allDocs)
        {
            List<String> mergeFieldValues = doc.getFusionValuesOf(mergeFusionField);
            if (mergeFieldValues != null && mergeFieldValues.size() > 0)
            {
                lookup.put(mergeFieldValues, doc);
            }
            else
            {
                // doc doesn't contain the merge field, so keep it
                newAllDocs.add(doc);
            }
        }
        for (Set<Document> sameDocuments : lookup.values())
        {
            newAllDocs.add(merger.mergeDocuments(config, sameDocuments));
        }
        allDocs = newAllDocs;
        return lookup;
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
