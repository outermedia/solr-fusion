package org.outermedia.solrfusion.query.parser;

/**
 * Created by ballmann on 8/22/14.
 */
public interface MetaParamsVisitor<C>
{
    public void visitEntry(String key, String value, C context);
}
