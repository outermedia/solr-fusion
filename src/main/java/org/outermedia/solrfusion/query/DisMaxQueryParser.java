package org.outermedia.solrfusion.query;

/*
 * #%L
 * SolrFusion
 * %%
 * Copyright (C) 2014 outermedia GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.QueryParserFactory;
import org.outermedia.solrfusion.query.dismaxparser.DismaxQueryParser;
import org.outermedia.solrfusion.query.parser.ParseException;
import org.outermedia.solrfusion.query.parser.Query;
import org.outermedia.solrfusion.query.parser.Operator;

import java.util.Locale;
import java.util.Map;

/**
 * A common solr dismax query parser.
 *
 * @author ballmann
 */

@ToString
@Slf4j
@Getter
@Setter
public class DisMaxQueryParser implements QueryParserIfc
{

    /**
     * Factory creates instances only.
     */
    protected DisMaxQueryParser()
    {
    }

    public static class Factory
    {
        public static DisMaxQueryParser getInstance()
        {
            return new DisMaxQueryParser();
        }
    }

    @Override
    public void init(QueryParserFactory config)
    {
        // NOP

    }

    @Override
    public Query parse(Configuration config, Map<String, Float> boosts, String queryString, Locale locale,
        Boolean allTermsAreProcessed) throws ParseException
    {
        Query result = null;
        if (queryString != null && queryString.trim().length() > 0)
        {
            String defaultOpStr = config.getDefaultOperator();
            Operator defaultOp = Operator.AND;
            try
            {
                defaultOp = Operator.valueOf(defaultOpStr);
            }
            catch (Exception e)
            {
                log.error("Found illegal default operator '{}'. Expected either 'or' or 'and'. Using {}.", defaultOpStr,
                    defaultOp, e);
            }
            DismaxQueryParser parser = new DismaxQueryParser(config.getDefaultSearchField(), config, boosts, defaultOp,
                allTermsAreProcessed);
            parser.setLocale(locale);
            result = parser.parse(queryString);
        }
        return result;
    }
}
