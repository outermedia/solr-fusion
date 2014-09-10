package org.outermedia.solrfusion.response;

/*
 * #%L
 * SolrFusion
 * %%
 * Copyright (C) 2014 outermedia GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.FusionRequest;
import org.outermedia.solrfusion.IdGeneratorIfc;
import org.outermedia.solrfusion.MergeStrategyIfc;
import org.outermedia.solrfusion.MultiKeyAndValueMap;
import org.outermedia.solrfusion.adapter.ClosableListIterator;
import org.outermedia.solrfusion.adapter.SearchServerResponseInfo;
import org.outermedia.solrfusion.configuration.*;
import org.outermedia.solrfusion.response.parser.*;
import org.outermedia.solrfusion.types.ScriptEnv;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Created by ballmann on 04.06.14.
 */

/**
 * Supports sorting, merging of documents and paging and a limit of documents to retrieve (per search server).
 * Facets and highlights are handled too.
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
    protected List<Document> facetFields;

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
        List<Highlighting> highlighting, Document facetFields)
    {
        streamCounter++;
        Set<String> searchServerFieldsToMap = getSingleFieldMapping(request.getSearchServerSortField());
        mapMergeField(config, searchServerConfig, request, searchServerFieldsToMap, fusionMergeField);

        maxDocNr = processDocuments(allDocs, maxDocNr, config, searchServerConfig, docIterator,
            searchServerFieldsToMap);

        processHighlighting(config, searchServerConfig, highlighting);

        processFacetFields(config, searchServerConfig, facetFields);
    }

    protected void processFacetFields(Configuration config, SearchServerConfig searchServerConfig, Document facetFields)
    {
        if (facetFields != null)
        {
            log.debug("Received {} facet fields from search server {}", facetFields.size(),
                searchServerConfig.getSearchServerName());
            String idField = searchServerConfig.getIdFieldName();
            Set<String> searchServerFieldsToMap = getSingleFieldMapping(idField);
            try
            {
                config.getResponseMapper().mapResponse(config, searchServerConfig, facetFields, getNewScriptEnv(),
                    searchServerFieldsToMap, ResponseTarget.FACET);
            }
            catch (Exception e)
            {
                log.error("Couldn't create/get response mapper instance", e);
            }
            if (this.facetFields == null)
            {
                this.facetFields = new ArrayList<>();
            }
            this.facetFields.add(facetFields);
        }
    }

    protected void processHighlighting(Configuration config, SearchServerConfig searchServerConfig,
        List<Highlighting> highlighting)
    {
        if (highlighting != null && highlighting.size() > 0)
        {
            log.debug("Received {} highlights from search server {}", highlighting.size(),
                searchServerConfig.getSearchServerName());

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
                    highlightingIt, searchServerFieldsToMap, ResponseTarget.HIGHLIGHT);
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

    protected int processDocuments(List<Document> docs, int totalDocNr, Configuration config,
        SearchServerConfig searchServerConfig, ClosableIterator<Document, SearchServerResponseInfo> docIterator,
        Set<String> searchServerFieldsToMap)
    {
        if (docIterator != null)
        {
            totalDocNr += Math.min(searchServerConfig.getMaxDocs(), docIterator.getExtraInfo().getTotalNumberOfHits());

            try
            {
                // map sort field and id/score only
                // map merge document field too if needed
                MappingClosableIterator mapper = getNewMappingClosableIterator(config, searchServerConfig, docIterator,
                    searchServerFieldsToMap, ResponseTarget.DOCUMENT);
                int docCount = 0;
                while (mapper.hasNext())
                {
                    docs.add(mapper.next());
                    docCount++;
                }
                log.debug("Added {} docs from server {}", docCount, searchServerConfig.getSearchServerName());
            }
            catch (Exception e)
            {
                log.error("Caught exception while mapping documents of server {}",
                    searchServerConfig.getSearchServerName(), e);
            }
            docIterator.close();
        }
        return totalDocNr;
    }

    protected MappingClosableIterator getNewMappingClosableIterator(Configuration config,
        SearchServerConfig searchServerConfig, ClosableIterator<Document, SearchServerResponseInfo> docIterator,
        Set<String> searchServerFieldsToMap, ResponseTarget target) throws InvocationTargetException, IllegalAccessException
    {
        return new MappingClosableIterator(docIterator, config, searchServerConfig, searchServerFieldsToMap, target);
    }

    protected void mapMergeField(Configuration config, SearchServerConfig searchServerConfig, FusionRequest request,
        Set<String> searchServerFieldsToMap, String fusionFieldToMap)
    {
        if (fusionFieldToMap != null)
        {
            try
            {
                Set<String> candidates = request.mapFusionFieldToSearchServerField(fusionFieldToMap, config,
                    searchServerConfig, null, QueryTarget.QUERY);
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
        String fusionSortField = fusionRequest.getFusionSortField();
        MultiKeyAndValueMap<String, Document> docLookup = null;

        // merge all docs (at least id is merged)
        if (fusionMergeField != null)
        {
            int allDocNrBeforeMerging = allDocs.size();
            docLookup = mergeDocuments(config, allDocs);
            log.debug("Merging resulted in {} documents (before merging: {}).", allDocs.size(), allDocNrBeforeMerging);
            // correct max docs available
            maxDocNr = maxDocNr-(allDocNrBeforeMerging-allDocs.size());
        }

        // sort all docs
        boolean sortAsc = fusionRequest.isSortAsc();
        log.debug("Sort docs by '{}' {}", fusionSortField, sortAsc ? "asc" : "desc");
        Collections.sort(allDocs, new FusionValueDocumentComparator(fusionSortField, sortAsc));

        if (log.isDebugEnabled())
        {
            log.debug("Sorted documents by {} asc={}:", fusionSortField, sortAsc);
            for (int i = 0; i < allDocs.size(); i++)
            {
                Document document = allDocs.get(i);
                List<String> sortValue = document.getFusionValuesOf(fusionSortField);
                log.debug("{}. {}={}", i, document.getFusionDocId(fusionIdField), sortValue);
            }
            log.debug("---");
        }

        // get docs of page
        List<Document> docsOfPage = new ArrayList<>();
        int start = fusionRequest.getStart().getValueAsInt(0);
        final IdGeneratorIfc idGenerator = config.getIdGenerator();
        final String fusionIdField = idGenerator.getFusionIdField();
        Map<String, Document> highlighting = new HashMap<>();
        int rows = fusionRequest.getPageSize().getValueAsInt(allDocs.size());
        for (int i = 0; i < rows && (i + start) < allDocs.size(); i++)
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
                completelyMapDoc(config, d, fusionDocId, null, ResponseTarget.DOCUMENT);
                Document hl = allHighlighting.get(fusionDocId);
                if (hl != null)
                {
                    completelyMapDoc(config, hl, fusionDocId, ScriptEnv.ENV_IN_MAP_HIGHLIGHT, ResponseTarget.HIGHLIGHT);
                    highlighting.put(fusionDocId, hl);
                }
            }
            docsOfPage.add(d);
        }
        log.debug("Returning {} documents for page size {}", docsOfPage.size(), fusionRequest.getPageSize());

        if (log.isDebugEnabled())
        {
            log.debug("Page: Sorted documents by {} asc={}: start={}, rows={}", fusionSortField, sortAsc, start, rows);
            for (int i = 0; i < docsOfPage.size(); i++)
            {
                Document document = docsOfPage.get(i);
                List<String> sortValue = document.getFusionValuesOf(fusionSortField);
                List<String> titleShort = document.getFusionValuesOf("title_short");
                log.debug("{}.\n  {}\n  {}", i, sortValue, titleShort);
            }
            log.debug("---");
        }

        Map<String, List<WordCount>> sortedFusionFacetFields = mapFacetWordCounts(idGenerator, fusionIdField,
            fusionRequest);
        log.debug("Total number of merged/sorted/filtered facets: {}", sortedFusionFacetFields.size());

        SearchServerResponseInfo info = new SearchServerResponseInfo(maxDocNr, highlighting, sortedFusionFacetFields,
            null);
        return new ClosableListIterator<>(docsOfPage, info);
    }

    protected Map<String, List<WordCount>> mapFacetWordCounts(IdGeneratorIfc idGenerator, String fusionIdField,
        FusionRequest fusionRequest) throws InvocationTargetException, IllegalAccessException
    {
        Map<String, Map<String, Integer>> fusionFacetFields = new LinkedHashMap<>();
        if (facetFields != null)
        {
            for (Document doc : facetFields)
            {
                completelyMapDoc(config, doc, doc.getFusionDocId(fusionIdField), ScriptEnv.ENV_IN_MAP_FACET, ResponseTarget.FACET);
                doc.accept(getFacetBuilder(idGenerator, fusionIdField, fusionFacetFields, doc), null);
            }
        }
        return getNewFacetWordCountSorter().sort(fusionFacetFields, fusionRequest);
    }

    protected FacetWordCountSorter getNewFacetWordCountSorter()
    {
        return new FacetWordCountSorter();
    }

    protected FacetWordCountBuilder getFacetBuilder(IdGeneratorIfc idGenerator, String fusionIdField,
        Map<String, Map<String, Integer>> fusionFacetFields, Document doc)
    {
        return new FacetWordCountBuilder(fusionIdField, idGenerator, doc, fusionFacetFields);
    }

    protected void completelyMapDoc(Configuration config, Document d, String fusionDocId, String mapType, ResponseTarget target)
        throws InvocationTargetException, IllegalAccessException
    {
        SearchServerConfig searchServerConfig = config.getSearchServerConfigByFusionDocId(fusionDocId);
        ScriptEnv newScriptEnv = getNewScriptEnv();
        if (mapType != null)
        {
            newScriptEnv.setBinding(mapType, Boolean.TRUE);
        }
        config.getResponseMapper().mapResponse(config, searchServerConfig, d, newScriptEnv, null, target);
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
    public List<Document> completelyMapMergedDoc(Collection<Document> sameDocuments, Map<String, Document> highlighting)
        throws InvocationTargetException, IllegalAccessException
    {
        List<Document> result;
        MergeStrategyIfc merger = config.getMerger();
        // map documents to merge (d is one entry of sameDocuments)
        for (Document toMerge : sameDocuments)
        {
            String fusionDocId = toMerge.getFusionDocId(fusionIdField);
            completelyMapDoc(config, toMerge, fusionDocId, null, ResponseTarget.DOCUMENT);
            Document hl = allHighlighting.get(fusionDocId);
            if (hl != null)
            {
                completelyMapDoc(config, hl, fusionDocId, ScriptEnv.ENV_IN_MAP_HIGHLIGHT, ResponseTarget.HIGHLIGHT);
            }
        }
        if (merger != null)
        {
            result = merger.mergeDocuments(merger.getFusionField(), config, sameDocuments, allHighlighting,
                highlighting);
        }
        else
        {
            // no merger configured, sameDocuments contains exactly one document
            result = Arrays.asList(sameDocuments.iterator().next());
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
            newAllDocs.addAll(merger.mergeDocuments(mergeFusionField, config, sameDocuments, emptyHighlights, null));
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
