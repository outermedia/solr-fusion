package org.outermedia.solrfusion.types;

import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.SearchServerConfig;
import org.outermedia.solrfusion.configuration.Util;
import org.outermedia.solrfusion.mapper.QueryBuilderIfc;
import org.outermedia.solrfusion.query.parser.TermQuery;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

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

    @Override public List<String> apply(List<String> values, ScriptEnv env, ConversionDirection dir)
    {
        // values come from tq, but here we need the whole term query
        TermQuery tq = (TermQuery) env.getBinding(ScriptEnv.ENV_TERM_QUERY_PART);
        String searchServerFieldName = env.getStringBinding(ScriptEnv.ENV_SEARCH_SERVER_FIELD);
        TermQuery searchServerTermQuery = tq.shallowClone();
        searchServerTermQuery.setSearchServerFieldName(searchServerFieldName);
        Configuration configuration = env.getConfiguration();
        SearchServerConfig searchServerConfig = env.getSearchServerConfig();
        List<String> result = null;
        try
        {
            QueryBuilderIfc qb = searchServerConfig.getQueryBuilder(configuration.getDefaultQueryBuilder());
            String qs = qb.buildQueryStringWithoutNew(searchServerTermQuery, configuration, searchServerConfig,
                env.getLocale());
            result = new ArrayList<>();
            result.add(qs);
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
