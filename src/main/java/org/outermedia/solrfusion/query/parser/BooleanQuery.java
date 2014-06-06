package org.outermedia.solrfusion.query.parser;

import lombok.ToString;
import org.outermedia.solrfusion.query.QueryVisitor;
import org.outermedia.solrfusion.types.ScriptEnv;

import java.util.ArrayList;
import java.util.List;

@ToString(callSuper = true)
public class BooleanQuery extends Query
{
    private List<BooleanClause> clauses;

    public BooleanQuery(boolean disableCoord)
    {
        clauses = new ArrayList<>();
        // TODO Auto-generated constructor stub
    }

    public void add(BooleanClause clause)
    {
        clauses.add(clause);
    }

    @Override
    public void accept(QueryVisitor visitor, ScriptEnv env)
    {
        visitor.visitQuery(this, env);
    }

    public void visitQueryClauses(QueryVisitor queryVisitor, ScriptEnv env)
    {
        for (BooleanClause c : clauses)
        {
            queryVisitor.visitQuery(c, env);
        }
    }
}
