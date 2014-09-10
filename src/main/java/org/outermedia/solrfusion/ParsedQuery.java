package org.outermedia.solrfusion;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.query.parser.Query;

/**
 * Helper class to associate the original query string and the parsed object.
 *
 * Created by ballmann on 8/29/14.
 */
@Getter
@Setter
@Slf4j
@AllArgsConstructor
public class ParsedQuery
{
    private String queryStr;
    private Query query;
}
