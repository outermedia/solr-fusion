package org.outermedia.solrfusion.types;

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

    @Override public TypeResult apply(List<String> values, List<Integer> facetDocCounts, ScriptEnv env,
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
     * The multi values are joined with {@link #valueSeparator}. If facet doc counts are set, their average is
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
            List<Integer> docCounts = result.getDocCounts();
            int docCountSum = 0;
            int docCountNr = 0;
            for (int i = 0; i < fieldValues.size(); i++)
            {
                if (i > 0)
                {
                    sb.append(valueSeparator);
                }
                sb.append(fieldValues.get(i));
                if (docCounts != null)
                {
                    for (Integer count : docCounts)
                    {
                        docCountSum += count;
                    }
                    docCountNr += docCounts.size();
                }
            }
            result.setValues(Arrays.asList(sb.toString()));
            List<Integer> newDocCounts = null;
            if (docCountNr > 0)
            {
                newDocCounts = Arrays.asList(docCountSum / docCountNr);
            }
            result.setDocCounts(newDocCounts);
        }
    }

    protected TypeResult joinFieldsToMultiValue(Document currentDoc)
    {
        TypeResult result;
        List<String> allValues = new ArrayList<>();
        List<Integer> allDocCounts = new ArrayList<>();

        // step 1: collect fields and their doc counts
        List<List<String>> fields = new ArrayList<>();
        List<List<Integer>> counts = new ArrayList<>();
        boolean atLeastOneDocCountListExists = false;
        boolean atLeastOneFieldWithoutDocCountList = false;
        for (String searchServerField : searchServerFieldsToMerge)
        {
            SolrField field = currentDoc.findFieldByName(searchServerField);
            if (field != null)
            {
                List<String> fieldValues = field.getAllSearchServerFieldValue();
                if (fieldValues != null)
                {
                    fields.add(fieldValues);
                    List<Integer> searchServerfacetDocCounts = field.getSearchServerFacetDocCounts();
                    counts.add(searchServerfacetDocCounts);
                    if (searchServerfacetDocCounts != null)
                    {
                        atLeastOneDocCountListExists = true;
                    }
                    else
                    {
                        atLeastOneFieldWithoutDocCountList = true;
                    }
                }
            }
        }

        // step 2: are doc counts missing?
        // at least one field has no doc counts, but at least one other fields has doc counts
        // then generate all missing doc counts
        if (atLeastOneDocCountListExists && atLeastOneFieldWithoutDocCountList)
        {
            for (int i = 0; i < fields.size(); i++)
            {
                if (counts.get(i) == null)
                {
                    List<Integer> newDocCounts = Collections.nCopies(fields.get(i).size(), 1);
                    counts.set(i, newDocCounts);
                }
            }
        }

        // step 3: join all fields and their doc counts
        for (int i = 0; i < fields.size(); i++)
        {
            allValues.addAll(fields.get(i));
            List<Integer> cs = counts.get(i);
            if (cs != null)
            {
                allDocCounts.addAll(cs);
            }
        }

        // step 4: normalize joined lists
        if (allValues.isEmpty())
        {
            allValues = null;
            allDocCounts = null;
        }
        if (allDocCounts != null && allDocCounts.isEmpty())
        {
            allDocCounts = null;
        }

        return new TypeResult(allValues, allDocCounts);
    }

    public static FieldMerger getInstance()
    {
        return new FieldMerger();
    }
}
