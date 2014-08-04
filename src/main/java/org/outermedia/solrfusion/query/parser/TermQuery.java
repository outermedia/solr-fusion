package org.outermedia.solrfusion.query.parser;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.outermedia.solrfusion.mapper.Term;
import org.outermedia.solrfusion.query.QueryVisitor;
import org.outermedia.solrfusion.types.ScriptEnv;

import java.util.List;

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

    protected TermQuery() {}

    @Override
    public void accept(QueryVisitor visitor, ScriptEnv env)
    {
        visitor.visitQuery(this, env);
    }

    public String getFusionFieldName()
    {
        return term.getFusionFieldName();
    }

    public List<String> getFusionFieldValue()
    {
        return term.getFusionFieldValue();
    }

    public String getSearchServerFieldName()
    {
        return term.getSearchServerFieldName();
    }

    public void setSearchServerFieldName(String s)
    {
        term.setSearchServerFieldName(s);
    }

    public List<String> getSearchServerFieldValue()
    {
        return term.getSearchServerFieldValue();
    }

    protected <T extends TermQuery> T shallowCloneImpl(T result)
    {
        result.setTerm(getTerm().shallowClone());
        result.setBoostValue(getBoostValue());
        return result;
    }

    public TermQuery shallowClone()
    {
        return shallowCloneImpl(new TermQuery());
    }
}
