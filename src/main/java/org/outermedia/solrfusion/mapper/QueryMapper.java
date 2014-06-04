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
    private Query query;

    public void mapQuery(SearchServerConfig serverConfig, Query query, ScriptEnv env)
    {
        this.serverConfig = serverConfig;
        this.query = query;
        query.accept(this, env);
    }

    // ---- Visitor methods --------------------------------------------------------------------------------------------

    @Override
    public void visitQuery(TermQuery t, ScriptEnv env)
    {
        List<FieldMapping> mappings = serverConfig.findAllMappingsForFusionField(t.getFusionFieldName());
        for (FieldMapping m : mappings)
        {
            m.applyQueryMappings(t.getTerm(), env);
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

}
