package org.outermedia.solrfusion.response;

import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.FusionRequest;
import org.outermedia.solrfusion.IdGeneratorIfc;
import org.outermedia.solrfusion.MergeStrategyIfc;
import org.outermedia.solrfusion.SolrFusionRequestParam;
import org.outermedia.solrfusion.adapter.ClosableListIterator;
import org.outermedia.solrfusion.adapter.SearchServerResponseInfo;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.ResponseTarget;
import org.outermedia.solrfusion.configuration.SearchServerConfig;
import org.outermedia.solrfusion.response.parser.DocCount;
import org.outermedia.solrfusion.response.parser.Document;
import org.outermedia.solrfusion.response.parser.Highlighting;
import org.outermedia.solrfusion.types.ScriptEnv;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Created by ballmann on 4/14/15.
 */

@Slf4j
public class CollectingResponseConsolidator extends PagingResponseConsolidator implements ResponseConsolidatorIfc
{
    // fusionDocId x DocAndInfo
    protected Map<String, Document> allDocsByFusionId;

    @Override
    public void initConsolidator(Configuration config) throws InvocationTargetException, IllegalAccessException
    {
        super.initConsolidator(config);
        allDocsByFusionId = new HashMap<>();
    }

    @Override
    public void addResultStream(SearchServerConfig searchServerConfig,
        ClosableIterator<Document, SearchServerResponseInfo> docIterator, FusionRequest request,
        List<Highlighting> highlighting, Document facetFields)
    {
        String searchServerName = searchServerConfig.getSearchServerName();
        try
        {
            IdGeneratorIfc idGen = config.getIdGenerator();
            while (docIterator.hasNext())
            {
                Document doc = docIterator.next();
                SearchServerResponseInfo info = docIterator.getExtraInfo();
                rememberTotalDocsFound(searchServerName, info.getTotalNumberOfHits());
                allDocsByFusionId.put(
                    idGen.computeId(searchServerName, doc.getSearchServerDocId(searchServerConfig.getIdFieldName())),
                    doc);
            }
        }
        catch (Exception e)
        {
            log.error("Couldn't compute fusion doc id", e);
        }

        streamCounter++;

        processHighlighting(config, searchServerConfig, highlighting);

        processFacetFields(config, searchServerConfig, facetFields);
    }

    @Override
    public void clear()
    {
        super.clear();
        allDocsByFusionId.clear();
    }

    @Override
    public ClosableIterator<Document, SearchServerResponseInfo> getResponseIterator(FusionRequest fusionRequest)
        throws InvocationTargetException, IllegalAccessException
    {
        List<Document> docList = new ArrayList<>();

        IdGeneratorIfc idGen = config.getIdGenerator();

        Map<String, Document> highlighting = new HashMap<>();

        // build final doc list on basis of given ids
        SolrFusionRequestParam idsQuery = fusionRequest.getIds();
        StringTokenizer st = new StringTokenizer(idsQuery.getValue(), ", ");
        while(st.hasMoreTokens())
        {
            String mergedDocIds = st.nextToken();
            MergeStrategyIfc merger = config.getMerger();
            List<String> singleDocIds;
            if (merger != null)
            {
                singleDocIds = idGen.splitMergedId(mergedDocIds);
            }
            else
            {
                singleDocIds = Arrays.asList(mergedDocIds);
            }
            List<Document> sameDocs = new ArrayList<>();
            for (String fusionDocId : singleDocIds)
            {
                Document doc = allDocsByFusionId.get(fusionDocId);
                String searchServerName = idGen.getSearchServerIdFromFusionId(fusionDocId);
                if (doc != null)
                {
                    // map id only
                    SearchServerConfig searchServerConfig = config.getSearchServerConfigByName(searchServerName);
                    String idField = searchServerConfig.getIdFieldName();
                    Set<String> searchServerFieldsToMap = getSingleFieldMapping(idField);
                    try
                    {
                        config.getResponseMapper().mapResponse(config, searchServerConfig, doc,
                            getNewScriptEnv(searchServerConfig), searchServerFieldsToMap, ResponseTarget.DOCUMENT,
                            false);
                    }
                    catch (Exception e)
                    {
                        log.error("Couldn't create/get response mapper instance", e);
                    }
                    sameDocs.add(doc);
                }
                else
                {
                    log.error("Server " + searchServerName + " didn't return document with id: " +
                        idGen.getSearchServerDocIdFromFusionId(fusionDocId, config.allSearchServerNames()));
                }
            }
            if (merger != null && sameDocs.size() > 1)
            {
                docList.addAll(completelyMapMergedDoc(sameDocs, highlighting));
            }
            else if (sameDocs.size() > 0)
            {
                Document d = sameDocs.get(0);
                String fusionDocId = d.getFusionDocId(config.getFusionIdFieldName());
                completelyMapDoc(config, d, fusionDocId, null, ResponseTarget.DOCUMENT);
                Document hl = allHighlighting.get(fusionDocId);
                if (hl != null)
                {
                    completelyMapDoc(config, hl, fusionDocId, ScriptEnv.ENV_IN_MAP_HIGHLIGHT, ResponseTarget.HIGHLIGHT);
                    highlighting.put(fusionDocId, hl);
                }
                docList.add(d);
            }
        }

        Map<String, List<DocCount>> sortedFusionFacetFields = mapFacetDocCounts(idGen, fusionIdField, fusionRequest);
        log.debug("Total number of merged/sorted/filtered facets: {}", sortedFusionFacetFields.size());

        SearchServerResponseInfo info = new SearchServerResponseInfo(maxDocNr, highlighting, sortedFusionFacetFields,
            null);
        return new ClosableListIterator<>(docList, info);
    }

    public static class Factory
    {
        public static ResponseConsolidatorIfc getInstance()
        {
            return new CollectingResponseConsolidator();
        }
    }

}
