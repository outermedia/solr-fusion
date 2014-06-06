package org.outermedia.solrfusion.query.parser;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.outermedia.solrfusion.mapper.Term;
import org.outermedia.solrfusion.query.QueryVisitor;
import org.outermedia.solrfusion.types.ScriptEnv;

/**
 * This class represents a simple term query aka "&lt;field&gt;:&lt;value&gt;".
 *
 * @author ballmann
 */

@ToString(callSuper = true)
@Getter
@Setter
public class TermQuery extends Query
{
    private Term term;

    public TermQuery(Term term)
    {
        this.term = term;
    }

    @Override
    public void accept(QueryVisitor visitor, ScriptEnv env)
    {
        visitor.visitQuery(this, env);
    }

    public void visitTerm(QueryVisitor visitor, ScriptEnv env)
    {
        visitor.visitQuery(term, env);
    }

    public String getFusionFieldName()
    {
        return term.getFusionFieldName();
    }

    public String getFusionFieldValue()
    {
        return term.getFusionFieldValue();
    }

    public String getSearchServerFieldName()
    {
        return term.getSearchServerFieldName();
    }

    public String getSearchServerFieldValue()
    {
        return term.getSearchServerFieldValue();
    }

}
