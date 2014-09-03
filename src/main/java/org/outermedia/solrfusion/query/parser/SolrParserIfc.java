package org.outermedia.solrfusion.query.parser;

import java.util.Locale;

/**
 * Created by ballmann on 9/3/14.
 */
public interface SolrParserIfc
{
    public Query parse(String query) throws ParseException;
    public void setLocale(Locale l);
}
