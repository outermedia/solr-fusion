package org.outermedia.solrfusion.query.parser;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BooleanClause
{
    public enum Occur
    {
        OCCUR_MAY, OCCUR_MUST, OCCUR_SHOULD, OCCUR_MUST_NOT
    }

    private Occur occur = Occur.OCCUR_MAY; // TODO correct initialization?
    private Query q;


    public BooleanClause(Query q, Occur occur)
    {
        this.q = q;
        this.occur = occur;
    }

    public boolean isProhibited()
    {
        // TODO Auto-generated method stub
        return false;
    }

}
