package org.outermedia.solrfusion.mapper;

import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.FusionRequest;
import org.outermedia.solrfusion.configuration.*;
import org.outermedia.solrfusion.query.QueryVisitor;
import org.outermedia.solrfusion.query.parser.*;
import org.outermedia.solrfusion.query.parser.Query;
import org.outermedia.solrfusion.types.ScriptEnv;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Map a SolrFusion query to a solr query.
 * <p/>
 * Created by ballmann on 03.06.14.
 */
@Slf4j
public class QueryMapper implements QueryVisitor, QueryMapperIfc, MetaParamsVisitor<MetaParams>
{
    private final static String NO_MAPPING_THROW_EXCEPTION = "error";
    private final static String NO_MAPPING_DELETE = "delete";

    private FusionRequest fusionRequest;
    private SearchServerConfig serverConfig;
    private Configuration configuration;
    private QueryTarget target;

    private String noMappingPolicy = NO_MAPPING_DELETE;

    /**
     * Only factory creates instances.
     */
    protected QueryMapper()
    {
    }

    /**
     * Map a query to a certain search server (serverConfig).
     *
     * @param serverConfig  the currently used server's configuration
     * @param query         the query to map to process
     * @param env           the environment needed by the scripts which transform values
     * @param fusionRequest
     * @param target
     */
    public void mapQuery(Configuration config, SearchServerConfig serverConfig, Query query, ScriptEnv env,
        FusionRequest fusionRequest, QueryTarget target)
    {
        this.configuration = config;
        this.serverConfig = serverConfig;
        this.fusionRequest = fusionRequest;
        env.setConfiguration(config);
        ScriptEnv newEnv = new ScriptEnv(env);
        newEnv.setSearchServerConfig(serverConfig);
        this.target = target;
        query.accept(this, newEnv);
    }

    @Override
    public void init(QueryMapperFactory config) throws InvocationTargetException, IllegalAccessException
    {
        // NOP
    }

    public static class Factory
    {
        public static QueryMapperIfc getInstance()
        {
            return new QueryMapper();
        }
    }

    // ---- Query visitor methods --------------------------------------------------------------------------------------

    @Override
    public void visitQuery(TermQuery t, ScriptEnv env)
    {
        visitTermQuery(env, t.getBoostValue(), t);
    }

    protected boolean visitTermQuery(ScriptEnv env, Float boost, TermQuery tq)
    {
        Term t = tq.getTerm();
        String fusionFieldName = t.getFusionFieldName();
        List<ApplicableResult> mappings = serverConfig.findAllMappingsForFusionField(fusionFieldName);
        if (mappings.isEmpty())
        {
            if (noMappingPolicy.equals(NO_MAPPING_THROW_EXCEPTION))
            {
                throw new MissingFusionFieldMapping("Found no mapping for fusion field '" + fusionFieldName + "'");
            }
            else if (noMappingPolicy.equals(NO_MAPPING_DELETE))
            {
                t.setRemoved(true);
                log.warn("Found no mapping of fusion field '{}' for server {}. Deleting query part.", fusionFieldName,
                    serverConfig.getSearchServerName());
            }
        }
        ScriptEnv newEnv = new ScriptEnv(env);
        newEnv.setBinding(ScriptEnv.ENV_IN_TERM_QUERY_PART, tq);
        for (ApplicableResult ar : mappings)
        {
            FieldMapping m = ar.getMapping();
            boolean traceEnabled = log.isTraceEnabled();
            if (traceEnabled)
            {
                log.trace("APPLY field={} -> {} mapping[line={}]={}", m.getFusionName(), ar.getDestinationFieldName(),
                    m.getLocator().getLineNumber(), m);
            }
            m.applyQueryOperations(t, newEnv, ar, target);
            if (traceEnabled)
            {
                log.trace("AFTER APPLY {}", t);
            }
        }
        t.setProcessed(true);
        MetaInfo metaInfo = tq.getMetaInfo();
        if (metaInfo != null)
        {
            metaInfo.accept(this);
        }
        return true;
    }

    @Override
    public void visitQuery(BooleanQuery t, ScriptEnv env)
    {
        t.visitQueryClauses(this, env);
    }

    @Override
    public void visitQuery(FuzzyQuery t, ScriptEnv env)
    {
        visitTermQuery(env, null, t);
    }

    @Override
    public void visitQuery(MatchAllDocsQuery t, ScriptEnv env)
    {
        // NOP
    }

    @Override
    public void visitQuery(PhraseQuery t, ScriptEnv env)
    {
        visitTermQuery(env, null, t);
    }

    @Override
    public void visitQuery(PrefixQuery t, ScriptEnv env)
    {
        visitTermQuery(env, null, t);
    }

    @Override
    public void visitQuery(WildcardQuery t, ScriptEnv env)
    {
        visitTermQuery(env, null, t);
    }

    @Override
    public void visitQuery(BooleanClause booleanClause, ScriptEnv env)
    {
        booleanClause.accept(this, env);
    }

    @Override
    public void visitQuery(SubQuery t, ScriptEnv env)
    {
        t.getQuery().accept(this, env);
    }

    @Override
    public void visitQuery(IntRangeQuery t, ScriptEnv env)
    {
        visitTermQuery(env, null, t);
    }

    @Override
    public void visitQuery(LongRangeQuery t, ScriptEnv env)
    {
        visitTermQuery(env, null, t);
    }

    @Override
    public void visitQuery(FloatRangeQuery t, ScriptEnv env)
    {
        visitTermQuery(env, null, t);
    }

    @Override
    public void visitQuery(DoubleRangeQuery t, ScriptEnv env)
    {
        visitTermQuery(env, null, t);
    }

    @Override
    public void visitQuery(DateRangeQuery t, ScriptEnv env)
    {
        visitTermQuery(env, null, t);
    }


    // ---- MetaInfo params visitor ------------------------------------------------------------------------------------

    @Override public void visitEntry(String key, String value, MetaParams mappedMetaParams)
    {
        if ("qf".equals(key))
        {
            String mappedValue = fusionRequest.mapFusionBoostFieldListToSearchServerField(value, configuration,
                serverConfig);
            if (mappedValue != null && mappedValue.length() > 0)
            {
                mappedMetaParams.addEntry(key, mappedValue);
            }
        }
        else
        {
            mappedMetaParams.addEntry(key, value);
        }
    }

}
