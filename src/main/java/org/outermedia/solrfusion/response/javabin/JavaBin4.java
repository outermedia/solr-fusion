package org.outermedia.solrfusion.response.javabin;

import lombok.extern.slf4j.Slf4j;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.JavaBinCodec;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.outermedia.solrfusion.FusionRequest;
import org.outermedia.solrfusion.FusionResponse;
import org.outermedia.solrfusion.adapter.SearchServerResponseInfo;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.FusionField;
import org.outermedia.solrfusion.configuration.ResponseRendererFactory;
import org.outermedia.solrfusion.mapper.ResponseMapperIfc;
import org.outermedia.solrfusion.response.ClosableIterator;
import org.outermedia.solrfusion.response.ResponseRendererIfc;
import org.outermedia.solrfusion.response.freemarker.*;
import org.outermedia.solrfusion.response.parser.DocCount;
import org.outermedia.solrfusion.response.parser.Document;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by ballmann on 3/24/15.
 */

@Slf4j
public class JavaBin4 implements ResponseRendererIfc
{
    @Override
    public void writeResponse(Configuration configuration,
        ClosableIterator<Document, SearchServerResponseInfo> docStream, FusionRequest request,
        FusionResponse fusionResponse)
    {
        // all values are converted to known types, so no resolver is needed
        JavaBinCodec.ObjectResolver resolver = null;
        JavaBinCodec codec = new JavaBinCodec(resolver);
        NamedList<Object> response = new SimpleOrderedMap<>();
        convertToNamedList(configuration, request, fusionResponse, docStream, response);
        try
        {
            fusionResponse.wroteSomeData();
            BufferedOutputStream os = fusionResponse.binWriter();
            codec.marshal(response, os);
            os.flush();

        }
        catch (IOException e)
        {
            log.error("Caught exception while writing bin response", e);
        }
    }

    protected void convertToNamedList(Configuration configuration, FusionRequest request, FusionResponse fusionResponse,
        ClosableIterator<Document, SearchServerResponseInfo> docStream, NamedList<Object> responseEntries)
    {
        FreemarkerResponse freemarkerResponse = new FreemarkerResponse(configuration, docStream);
        FreemarkerErrorHeader freemarkerErrorHeader = new FreemarkerErrorHeader(fusionResponse);
        Map<String, Document> highlighting = null;
        Map<String, List<DocCount>> facets = null;
        if (docStream != null)
        {
            highlighting = docStream.getExtraInfo().getHighlighting();
            facets = docStream.getExtraInfo().getFacetFields();
        }
        FreemarkerResponseHighlighting freemarkerHighlighting = new FreemarkerResponseHighlighting(configuration,
            highlighting);
        FreemarkerFacets freemarkerFacets = new FreemarkerFacets(configuration, facets);

        // sort_values ::= { <sort field name> : [<sort value of doc1>, <sort value of doc2>, ...], ... }
        // doc1, doc2, ... are the returned documents
        NamedList<ArrayList> sorValuesNl = new NamedList<>();
        responseEntries.add("sort_values", sorValuesNl);

        boolean omitHeaders = request.getOmitHeader().getValueAsBool(false);
        if (!omitHeaders)
        {
            FreemarkerResponseHeader freemarkerResponseHeader = new FreemarkerResponseHeader(docStream, request,
                fusionResponse);
            SimpleOrderedMap<Object> responseHeader = new SimpleOrderedMap<>();
            responseEntries.add("responseHeader", responseHeader);
            responseHeader.add("status", 0);
            responseHeader.add("QTime", freemarkerResponseHeader.getQueryTime());
            SimpleOrderedMap<String> params = new SimpleOrderedMap<>();
            responseHeader.add("params", params);
            params.add("indent", "off");
            params.add("wt", "javabin");
            params.add("version", "4.8.1");
            Map<String, String> singleValueQueryParams = freemarkerResponseHeader.getQueryParams();
            params.addAll(singleValueQueryParams);
            Map<String, List<String>> multiValueQueryParams = freemarkerResponseHeader.getMultiValueQueryParams();
            params.addAll((Map) multiValueQueryParams);
        }

        // handle error case
        if (freemarkerErrorHeader.isError())
        {
            SimpleOrderedMap<Object> responseError = new SimpleOrderedMap<>();
            responseEntries.add("error", responseError);
            responseError.add("msg", freemarkerErrorHeader.getMsg());
            responseError.add("code", freemarkerErrorHeader.getCode());
        }

        // handle matched docs
        List<FreemarkerDocument> matchDocs = freemarkerResponse.getMatchDocuments();
        if (matchDocs.size() > 0)
        {
            SolrDocumentList solrDocs = new SolrDocumentList();
            responseEntries.add("match", solrDocs);
            solrDocs.setNumFound(freemarkerResponse.getTotalMatchHitNumber());
            solrDocs.setStart(0);
            for (FreemarkerDocument doc : matchDocs)
            {
                solrDocs.add(convertToSolrDocument(doc, configuration));
            }
        }

        // handle found docs
        List<FreemarkerDocument> foundDocs = freemarkerResponse.getDocuments();
        // always return the response part, even if empty
        // if (foundDocs.size() > 0)
        {
            SolrDocumentList solrDocs = new SolrDocumentList();
            responseEntries.add("response", solrDocs);
            solrDocs.setNumFound(freemarkerResponse.getTotalHitNumber());
            solrDocs.setStart(0);
            String fusionSortField = request.getFusionSortField();
            ArrayList sortValuesOfReturnedDocs = new ArrayList();
            // don't set sort_values in response if score is the sort field (explicitly or implicitly specified)
            if(!ResponseMapperIfc.FUSION_FIELD_NAME_SCORE.equalsIgnoreCase(fusionSortField))
            {
                sorValuesNl.add(fusionSortField, sortValuesOfReturnedDocs);
            }
            for (FreemarkerDocument doc : foundDocs)
            {
                SolrDocument solrDoc = convertToSolrDocument(doc, configuration);
                solrDocs.add(solrDoc);
                sortValuesOfReturnedDocs.add(solrDoc.getFieldValue(fusionSortField));
            }
        }

        // handle facets
        if (freemarkerFacets.isHasFacets())
        {
            // see /mnt/mvnrepo-om/org/apache/solr/solr-core/4.8.1/solr-core-4.8.1.jar!/org/apache/solr/handler/component/FacetComponent.class
            SimpleOrderedMap<Object> facetNamedList = new SimpleOrderedMap<>();
            responseEntries.add("facet_counts", facetNamedList);
            facetNamedList.add("facet_queries", new SimpleOrderedMap<>());

            SimpleOrderedMap<Object> facetFieldsNamedList = new SimpleOrderedMap<>();
            Map<String, List<DocCount>> facetFieldsAndValues = freemarkerFacets.getFacets();
            for (String key : facetFieldsAndValues.keySet())
            {
                List<DocCount> count = facetFieldsAndValues.get(key);
                NamedList<Integer> namedListCount = new SimpleOrderedMap<>();
                for (DocCount dc : count)
                {
                    namedListCount.add(dc.getWord(), dc.getCount());
                }
                facetFieldsNamedList.add(key, namedListCount);
            }

            facetNamedList.add("facet_fields", facetFieldsNamedList);
            facetNamedList.add("facet_dates", new SimpleOrderedMap<>());
            facetNamedList.add("facet_ranges", new SimpleOrderedMap<>());
        }

        // handle highlights
        if (freemarkerHighlighting.isHasHighlights())
        {
            // see /mnt/mvnrepo-om/org/apache/solr/solr-core/4.8.1/solr-core-4.8.1.jar!/org/apache/solr/handler/component/HighlightComponent.class
            SimpleOrderedMap<Object> hlNamedList = new SimpleOrderedMap<>();
            responseEntries.add("highlighting", hlNamedList);
            List<FreemarkerDocument> hlDocs = freemarkerHighlighting.getHighlighting();
            for (FreemarkerDocument hlDoc : hlDocs)
            {
                SimpleOrderedMap<Object> namedListHlDoc = new SimpleOrderedMap<>();
                hlNamedList.add(hlDoc.getId(), namedListHlDoc);
                List<FreemarkerMultiValuedField> fieldSnippets = hlDoc.getMultiValuedFields();
                for (FreemarkerMultiValuedField mv : fieldSnippets)
                {
                    namedListHlDoc.add(mv.getName(), mv.getValues().toArray());
                }
            }
        }

    }

    protected SolrDocument convertToSolrDocument(FreemarkerDocument fmDoc, Configuration config)
    {
        SolrDocument sDoc = new SolrDocument();
        List<FreemarkerSingleValuedField> singleFields = fmDoc.getSingleValuedFields();
        for (FreemarkerSingleValuedField f : singleFields)
        {
            String fusionField = f.getName();
            sDoc.addField(fusionField, objectFromString(f.getValue(), config.findFieldByName(fusionField)));
        }
        List<FreemarkerMultiValuedField> multiFields = fmDoc.getMultiValuedFields();
        for (FreemarkerMultiValuedField f : multiFields)
        {
            for (String v : f.getValues())
            {
                String fusionField = f.getName();
                sDoc.addField(fusionField, objectFromString(v, config.findFieldByName(fusionField)));
            }
        }
        return sDoc;
    }

    protected Object objectFromString(String docFieldValue, FusionField fusionField)
    {
        if (fusionField == null)
        {
            return docFieldValue;
        }

        switch (fusionField.getFieldType())
        {
            case BOOLEAN:
                return Boolean.valueOf("1".equals(docFieldValue) || "true".equalsIgnoreCase(docFieldValue) ||
                    "yes".equalsIgnoreCase(docFieldValue));
            case TEXT:
                return docFieldValue;
            case INT:
                return Integer.valueOf(docFieldValue);
            case LONG:
                return Long.valueOf(docFieldValue);
            case FLOAT:
                return Float.valueOf(docFieldValue);
            case DOUBLE:
                return Double.valueOf(docFieldValue);
            case DATE:
                SimpleDateFormat sdf = new SimpleDateFormat(fusionField.getFormat());
                try
                {
                    return sdf.parse(docFieldValue);
                }
                catch (Exception e)
                {
                    log.warn(
                        "Couldn't parse date from '" + docFieldValue + "' with pattern '" + fusionField.getFormat() +
                            "'. Return string object.", e);
                    return docFieldValue;
                }
        }
        log.warn(
            "Couldn't convert value '" + docFieldValue + "' of type '" + fusionField.getFormat() + "'. Using string.");
        return docFieldValue;
    }

    @Override
    public void init(ResponseRendererFactory config) throws InvocationTargetException, IllegalAccessException
    {
        // NOP
    }

    public static class Factory
    {
        public static JavaBin4 getInstance()
        {
            return new JavaBin4();
        }
    }
}
