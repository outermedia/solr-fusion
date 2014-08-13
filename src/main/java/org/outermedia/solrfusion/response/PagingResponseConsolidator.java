package org.outermedia.solrfusion.response;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.FusionRequest;
import org.outermedia.solrfusion.IdGeneratorIfc;
import org.outermedia.solrfusion.MergeStrategyIfc;
import org.outermedia.solrfusion.MultiKeyAndValueMap;
import org.outermedia.solrfusion.adapter.ClosableListIterator;
import org.outermedia.solrfusion.adapter.SearchServerResponseInfo;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.ResponseConsolidatorFactory;
import org.outermedia.solrfusion.configuration.SearchServerConfig;
import org.outermedia.solrfusion.response.parser.*;
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
    protected int maxDocNr;
    protected HighlightingMap allHighlighting;
    protected Configuration config;
    protected String fusionIdField;
    protected String fusionMergeField;
    protected List<FacetHit> facetFields;

    /**
     * Factory creates instances only.
     */
    protected PagingResponseConsolidator()
    {
        super();
        allDocs = new ArrayList<>();
        streamCounter = 0;
        maxDocNr = 0;
        allHighlighting = new HighlightingMap();
    }

    @Override
    public void initConsolidator(Configuration config) throws InvocationTargetException, IllegalAccessException
    {
        this.config = config;
        fusionIdField = config.getFusionIdFieldName();
        fusionMergeField = null;
        MergeStrategyIfc merger = config.getMerger();
        if (merger != null)
        {
            fusionMergeField = merger.getFusionField();
        }
        allHighlighting.init(config.getIdGenerator());
    }

    @Override public void addResultStream(SearchServerConfig searchServerConfig,
        ClosableIterator<Document, SearchServerResponseInfo> docIterator, FusionRequest request,
        List<Highlighting> highlighting, List<FacetHit> facetFields)
    {
        streamCounter++;
        Set<String> searchServerFieldsToMap = getSingleFieldMapping(request.getSearchServerSortField());
        mapMergeField(config, searchServerConfig, request, searchServerFieldsToMap, fusionMergeField);

        processDocuments(config, searchServerConfig, docIterator, searchServerFieldsToMap);

        processHighlighting(config, searchServerConfig, highlighting);

        processFacetFields(config, searchServerConfig, facetFields);
    }

    protected void processFacetFields(Configuration config, SearchServerConfig searchServerConfig,
        List<FacetHit> facetFields)
    {
        String searchServerIdField = searchServerConfig.getIdFieldName();
        if (facetFields != null)
        {
            // generate dummy search server ids
            String idField = searchServerConfig.getIdFieldName();
            Set<String> searchServerFieldsToMap = getSingleFieldMapping(idField);
            for (int i = 0; i < facetFields.size(); i++)
            {
                Document d = facetFields.get(i).getDocument(searchServerIdField, i + 1);
                try
                {
                    config.getResponseMapper().mapResponse(config, searchServerConfig, d, getNewScriptEnv(),
                        searchServerFieldsToMap);
                }
                catch (Exception e)
                {
                    log.error("Couldn't create/get response mapper instance", e);
                }
            }
            if (this.facetFields == null)
            {
                this.facetFields = new ArrayList<>();
            }
            this.facetFields.addAll(facetFields);
        }
    }

    protected void processHighlighting(Configuration config, SearchServerConfig searchServerConfig,
        List<Highlighting> highlighting)
    {
        if (highlighting != null && highlighting.size() > 0)
        {
            String idField = searchServerConfig.getIdFieldName();
            List<Document> highlightingDocs = new ArrayList<>();
            for (Highlighting hl : highlighting)
            {
                highlightingDocs.add(hl.getDocument(idField));
            }
            ClosableListIterator<Document, SearchServerResponseInfo> highlightingIt = new ClosableListIterator<>(
                highlightingDocs, null);
            try
            {
                Set<String> searchServerFieldsToMap = getSingleFieldMapping(idField);
                MappingClosableIterator mapper = getNewMappingClosableIterator(config, searchServerConfig,
                    highlightingIt, searchServerFieldsToMap);
                while (mapper.hasNext())
                {
                    allHighlighting.put(mapper.next());
                }
            }
            catch (Exception e)
            {
                log.error("Caught exception while mapping highlighting of server {}",
                    searchServerConfig.getSearchServerName(), e);
            }
        }
    }

    private Set<String> getSingleFieldMapping(String searchServerField)
    {
        // map only id field
        Set<String> searchServerFieldsToMap = new HashSet<>();
        searchServerFieldsToMap.add(searchServerField);
        return searchServerFieldsToMap;
    }

    protected void processDocuments(Configuration config, SearchServerConfig searchServerConfig,
        ClosableIterator<Document, SearchServerResponseInfo> docIterator, Set<String> searchServerFieldsToMap)
    {
        maxDocNr += Math.min(searchServerConfig.getMaxDocs(), docIterator.getExtraInfo().getTotalNumberOfHits());

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
        Set<String> searchServerFieldsToMap, String fusionFieldToMap)
    {
        if (fusionFieldToMap != null)
        {
            try
            {
                Set<String> candidates = request.mapFusionFieldToSearchServerField(fusionFieldToMap, config,
                    searchServerConfig);
                if (candidates.isEmpty())
                {
                    log.error("Found not mapping for merge field '{}'", fusionFieldToMap);
                }
                searchServerFieldsToMap.addAll(candidates);
            }
            catch (Exception e)
            {
                log.error("Can't map merge field {}", fusionFieldToMap, e);
            }
        }
    }

    @Override public int numberOfResponseStreams()
    {
        return streamCounter;
    }

    @Override public void clear()
    {
        allDocs.clear();
    }

    @Override public ClosableIterator<Document, SearchServerResponseInfo> getResponseIterator(
        FusionRequest fusionRequest) throws InvocationTargetException, IllegalAccessException
    {
        String fusionSortField = fusionRequest.getSolrFusionSortField();
        MultiKeyAndValueMap<String, Document> docLookup = null;

        // merge all docs (at least id is merged)
        if (fusionMergeField != null)
        {
            docLookup = mergeDocuments(config, allDocs);
        }

        // sort all docs
        boolean sortAsc = fusionRequest.isSortAsc();
        Collections.sort(allDocs, new FusionValueDocumentComparator(fusionSortField, sortAsc));

        // get docs of page
        List<Document> docsOfPage = new ArrayList<>();
        int start = fusionRequest.getStart();
        final IdGeneratorIfc idGenerator = config.getIdGenerator();
        final String fusionIdField = idGenerator.getFusionIdField();
        Map<String, Document> highlighting = new HashMap<>();
        for (int i = 0; i < fusionRequest.getPageSize() && (i + start) < allDocs.size(); i++)
        {
            Document d = allDocs.get(start + i);
            // id was mapped too when sort field was mapped
            String fusionDocId = d.getFusionDocId(fusionIdField);
            if (idGenerator.isMergedDocument(fusionDocId))
            {
                String mergeFieldValue = d.getFusionValuesOf(fusionMergeField).get(0);
                // all values of fusionMergeField point to the same container which holds the same documents
                Set<Document> sameDocuments = docLookup.get(mergeFieldValue);
                completelyMapMergedDoc(sameDocuments, highlighting);
            }
            else
            {
                completelyMapDoc(config, d, fusionDocId);
                Document hl = allHighlighting.get(fusionDocId);
                if (hl != null)
                {
                    completelyMapDoc(config, hl, fusionDocId);
                    highlighting.put(fusionDocId, hl);
                }
            }
            docsOfPage.add(d);
        }

        final Map<String, Map<String, Integer>> fusionFacetFields = new HashMap<>();
        mapFacetWordCounts(idGenerator, fusionIdField, fusionFacetFields);

        SearchServerResponseInfo info = new SearchServerResponseInfo(maxDocNr, highlighting, fusionFacetFields);
        return new ClosableListIterator<>(docsOfPage, info);
    }

    protected void mapFacetWordCounts(final IdGeneratorIfc idGenerator, final String fusionIdField,
        final Map<String, Map<String, Integer>> fusionFacetFields)
        throws InvocationTargetException, IllegalAccessException
    {
        if (facetFields != null)
        {
            for (FacetHit fh : facetFields)
            {
                final Document doc = fh.getDocument();
                completelyMapDoc(config, doc, doc.getFusionDocId(fusionIdField));
                doc.accept(new FacetWordCountBuilder(fusionIdField, idGenerator, doc, fusionFacetFields), null);
            }
        }
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
     * @param sameDocuments
     * @param highlighting
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    @Override
    public Document completelyMapMergedDoc(Collection<Document> sameDocuments, Map<String, Document> highlighting)
        throws InvocationTargetException, IllegalAccessException
    {
        Document result;
        MergeStrategyIfc merger = config.getMerger();
        // map documents to merge (d is one entry of sameDocuments)
        for (Document toMerge : sameDocuments)
        {
            String fusionDocId = toMerge.getFusionDocId(fusionIdField);
            completelyMapDoc(config, toMerge, fusionDocId);
            Document hl = allHighlighting.get(fusionDocId);
            if (hl != null)
            {
                completelyMapDoc(config, hl, fusionDocId);
            }
        }
        if (merger != null)
        {
            result = merger.mergeDocuments(config, sameDocuments, allHighlighting, highlighting);
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

    protected MultiKeyAndValueMap<String, Document> mergeDocuments(Configuration config, List<Document> docs)
        throws InvocationTargetException, IllegalAccessException
    {
        MergeStrategyIfc merger = config.getMerger();
        String mergeFusionField = merger.getFusionField();
        List<Document> newAllDocs = new ArrayList<>();
        MultiKeyAndValueMap<String, Document> lookup = new MultiKeyAndValueMap();
        for (Document doc : docs)
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
        HighlightingMap emptyHighlights = new HighlightingMap();
        emptyHighlights.init(config.getIdGenerator());
        for (Set<Document> sameDocuments : lookup.values())
        {
            // highlights will be merged when docs of page are known, so we pass on empty highlights here
            newAllDocs.add(merger.mergeDocuments(config, sameDocuments, emptyHighlights, null));
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
