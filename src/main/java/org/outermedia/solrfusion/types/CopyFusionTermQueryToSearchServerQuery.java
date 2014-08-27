package org.outermedia.solrfusion.types;

import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.FusionRequest;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.SearchServerConfig;
import org.outermedia.solrfusion.configuration.Util;
import org.outermedia.solrfusion.mapper.QueryBuilderIfc;
import org.outermedia.solrfusion.query.parser.TermQuery;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by ballmann on 8/1/14.
 */
@Slf4j
public class CopyFusionTermQueryToSearchServerQuery extends AbstractType
{
    @Override public void passArguments(List<Element> typeConfig, Util util)
    {
        // NOP
    }

    @Override public TypeResult apply(List<String> values, List<Integer> facetWordCounts, ScriptEnv env,
        ConversionDirection dir)
    {
        // values come from tq, but here we need the whole term query
        TermQuery tq = (TermQuery) env.getBinding(ScriptEnv.ENV_IN_TERM_QUERY_PART);
        String searchServerFieldName = env.getStringBinding(ScriptEnv.ENV_IN_SEARCH_SERVER_FIELD);
        TermQuery searchServerTermQuery = tq.shallowClone();
        searchServerTermQuery.setSearchServerFieldName(searchServerFieldName);
        Configuration configuration = env.getConfiguration();
        SearchServerConfig searchServerConfig = env.getSearchServerConfig();
        List<String> newValues = null;
        TypeResult result = null;
        try
        {
            FusionRequest fusionRequest = (FusionRequest) env.getBinding(ScriptEnv.ENV_IN_FUSION_REQUEST);
            QueryBuilderIfc qb = searchServerConfig.getQueryBuilder(configuration.getDefaultQueryBuilder());
            Set<String> defaultSearchServerSearchFields = fusionRequest.mapFusionFieldToSearchServerField(configuration.getDefaultSearchField(), configuration, searchServerConfig, null);
            String qs = qb.buildQueryStringWithoutNew(searchServerTermQuery, configuration, searchServerConfig,
                env.getLocale(),defaultSearchServerSearchFields);
            newValues = new ArrayList<>();
            newValues.add(qs);
            result = new TypeResult(newValues, facetWordCounts);
        }
        catch (Exception e)
        {
            log.error("Caught exception while creating new query to add: {}", tq, e);
        }
        return result;
    }

    public static CopyFusionTermQueryToSearchServerQuery getInstance()
    {
        return new CopyFusionTermQueryToSearchServerQuery();
    }
}
