package org.outermedia.solrfusion.types;

import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.configuration.FusionField;
import org.outermedia.solrfusion.configuration.Util;
import org.outermedia.solrfusion.response.parser.Document;
import org.outermedia.solrfusion.response.parser.SolrField;
import org.w3c.dom.Element;

import javax.xml.xpath.XPathExpressionException;
import java.util.*;

/**
 * Merge several Solr fields and their values into one fusion field. Depending on the fusion field (single vs. multi
 * value) either one or several values are created. So this ScriptType is only applicable to Solr responses.
 * <p/>
 * Created by ballmann on 8/19/14.
 */
@Slf4j
public class FieldMerger extends AbstractType
{
    private List<String> searchServerFieldsToMerge;

    protected String valueSeparator = ";";

    @Override public void passArguments(List<Element> typeConfig, Util util)
    {
        searchServerFieldsToMerge = new ArrayList<>();

        /* unfortunately the ":" is necessary for the empty xml namespace!
         * please see Util.getValueOfXpath() */
        searchServerFieldsToMerge = parseFields(typeConfig, util, "//:field");

        try
        {
            String separator = util.getValueOfXpath("//:separator", typeConfig, false);
            if (separator != null)
            {
                valueSeparator = separator;
            }
        }
        catch (XPathExpressionException e)
        {
            log.error("Caught exception while parsing configuration: " + elementListToString(typeConfig), e);
        }
    }

    @Override public TypeResult apply(List<String> values, List<Integer> facetWordCounts, ScriptEnv env,
        ConversionDirection dir)
    {
        TypeResult result = null;
        FusionField fusionField = (FusionField) env.getBinding(ScriptEnv.ENV_IN_FUSION_FIELD_DECLARATION);
        if (dir == ConversionDirection.SEARCH_TO_FUSION)
        {
            Document currentDoc = (Document) env.getBinding(ScriptEnv.ENV_IN_DOCUMENT);
            result = joinFieldsToMultiValue(currentDoc);
            boolean isHighlightDoc = env.getBoolBinding(ScriptEnv.ENV_IN_MAP_HIGHLIGHT);
            boolean isFacetDoc = env.getBoolBinding(ScriptEnv.ENV_IN_MAP_FACET);
            if (!isHighlightDoc && !isFacetDoc && fusionField.isSingleValue())
            {
                reduceMultiValuesToSingleValue(result);
            }
        }
        return result;
    }

    /**
     * The multi values are joined with {@link #valueSeparator}. If facet word counts are set, their average is
     * calculated.
     *
     * @param result a valid TypeResult object to flatten
     */
    protected void reduceMultiValuesToSingleValue(TypeResult result)
    {
        List<String> fieldValues = result.getValues();
        if (fieldValues != null)
        {
            StringBuilder sb = new StringBuilder();
            List<Integer> wordCounts = result.getWordCounts();
            int wordCountSum = 0;
            int wordCountNr = 0;
            for (int i = 0; i < fieldValues.size(); i++)
            {
                if (i > 0)
                {
                    sb.append(valueSeparator);
                }
                sb.append(fieldValues.get(i));
                if (wordCounts != null)
                {
                    for (Integer count : wordCounts)
                    {
                        wordCountSum += count;
                    }
                    wordCountNr += wordCounts.size();
                }
            }
            result.setValues(Arrays.asList(sb.toString()));
            List<Integer> newWordCounts = null;
            if (wordCountNr > 0)
            {
                newWordCounts = Arrays.asList(wordCountSum / wordCountNr);
            }
            result.setWordCounts(newWordCounts);
        }
    }

    protected TypeResult joinFieldsToMultiValue(Document currentDoc)
    {
        TypeResult result;
        List<String> allValues = new ArrayList<>();
        List<Integer> allWordCounts = new ArrayList<>();

        // step 1: collect fields and their word counts
        List<List<String>> fields = new ArrayList<>();
        List<List<Integer>> counts = new ArrayList<>();
        boolean atLeastOneWordCountListExists = false;
        boolean atLeastOneFieldWithoutWordCountList = false;
        for (String searchServerField : searchServerFieldsToMerge)
        {
            SolrField field = currentDoc.findFieldByName(searchServerField);
            if (field != null)
            {
                List<String> fieldValues = field.getAllSearchServerFieldValue();
                if (fieldValues != null)
                {
                    fields.add(fieldValues);
                    List<Integer> searchServerfacetWordCounts = field.getSearchServerFacetWordCounts();
                    counts.add(searchServerfacetWordCounts);
                    if (searchServerfacetWordCounts != null)
                    {
                        atLeastOneWordCountListExists = true;
                    }
                    else
                    {
                        atLeastOneFieldWithoutWordCountList = true;
                    }
                }
            }
        }

        // step 2: are word counts missing?
        // at least one field has no word counts, but at least one other fields has word counts
        // then generate all missing word counts
        if (atLeastOneWordCountListExists && atLeastOneFieldWithoutWordCountList)
        {
            for (int i = 0; i < fields.size(); i++)
            {
                if (counts.get(i) == null)
                {
                    List<Integer> newWordCounts = Collections.nCopies(fields.get(i).size(), 1);
                    counts.set(i, newWordCounts);
                }
            }
        }

        // step 3: join all fields and their word counts
        for (int i = 0; i < fields.size(); i++)
        {
            allValues.addAll(fields.get(i));
            List<Integer> cs = counts.get(i);
            if (cs != null)
            {
                allWordCounts.addAll(cs);
            }
        }

        // step 4: normalize joined lists
        if (allValues.isEmpty())
        {
            allValues = null;
            allWordCounts = null;
        }
        if (allWordCounts != null && allWordCounts.isEmpty())
        {
            allWordCounts = null;
        }

        return new TypeResult(allValues, allWordCounts);
    }

    public static FieldMerger getInstance()
    {
        return new FieldMerger();
    }
}
