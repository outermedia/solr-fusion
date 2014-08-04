package org.outermedia.solrfusion.query.parser;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.outermedia.solrfusion.mapper.Term;
import org.outermedia.solrfusion.query.QueryVisitor;
import org.outermedia.solrfusion.types.ScriptEnv;

@ToString(callSuper = true)
@Getter
@Setter
public class PhraseQuery extends TermQuery
{
    private Integer maxEdits;

    public PhraseQuery(Term term)
    {
        super(term);
    }

    protected PhraseQuery() {}

    @Override
    public void accept(QueryVisitor visitor, ScriptEnv env)
    {
        visitor.visitQuery(this, env);
    }

    @Override
    public PhraseQuery shallowClone()
    {
        return shallowCloneImpl(new PhraseQuery());
    }

}
