package org.outermedia.solrfusion.mapper;

import org.outermedia.solrfusion.configuration.FieldMapping;
import org.outermedia.solrfusion.configuration.SearchServerConfig;
import org.outermedia.solrfusion.query.QueryVisitor;
import org.outermedia.solrfusion.query.parser.*;
import org.outermedia.solrfusion.types.ScriptEnv;

import java.util.List;

/**
 * Map a fusion query to a solr request.
 * <p/>
 * Created by ballmann on 03.06.14.
 */
public class QueryMapper implements QueryVisitor
{
    private SearchServerConfig serverConfig;

    /**
     * Map a query to a certain search server (serverConfig).
     *
     * @param serverConfig the currently used server's configuration
     * @param query        the query to map to process
     * @param env          the environment needed by the scripts which transform values
     */
    public void mapQuery(SearchServerConfig serverConfig, Query query, ScriptEnv env)
    {
        this.serverConfig = serverConfig;
        query.accept(this, env);
    }

    // ---- Visitor methods --------------------------------------------------------------------------------------------

    @Override
    public void visitQuery(TermQuery t, ScriptEnv env)
    {
        t.visitTerm(this, env);
    }

    @Override
    public void visitQuery(Term t, ScriptEnv env)
    {
        String fusionFieldName = t.getFusionFieldName();
        List<FieldMapping> mappings = serverConfig.findAllMappingsForFusionField(fusionFieldName);
        if (mappings.isEmpty())
        {
            throw new MissingFusionFieldMapping("Found no mapping for fusion field '" + fusionFieldName + "'");
        }
        for (FieldMapping m : mappings)
        {
            m.applyQueryMappings(t, env);
        }
    }

    @Override
    public void visitQuery(BooleanQuery t, ScriptEnv env)
    {
        t.visitQueryClauses(this, env);
    }

    @Override
    public void visitQuery(FuzzyQuery t, ScriptEnv env)
    {
        visitQuery((TermQuery) t, env);
    }

    @Override
    public void visitQuery(MatchAllDocsQuery t, ScriptEnv env)
    {
        // TODO expand * to all fields in order to apply add/remove operations?!
    }

    @Override
    public void visitQuery(MultiPhraseQuery t, ScriptEnv env)
    {
        // TODO
    }

    @Override
    public void visitQuery(NumericRangeQuery t, ScriptEnv env)
    {
        // TODO

    }

    @Override
    public void visitQuery(PhraseQuery t, ScriptEnv env)
    {
        // TODO

    }

    @Override
    public void visitQuery(PrefixQuery t, ScriptEnv env)
    {
        // TODO

    }

    @Override
    public void visitQuery(WildcardQuery t, ScriptEnv env)
    {
        // TODO

    }

    @Override
    public void visitQuery(BooleanClause booleanClause, ScriptEnv env)
    {
        booleanClause.accept(this, env);
    }

}