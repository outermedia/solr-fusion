package org.outermedia.solrfusion.query;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.QueryParserFactory;
import org.outermedia.solrfusion.query.parser.ParseException;
import org.outermedia.solrfusion.query.parser.Query;
import org.outermedia.solrfusion.query.parser.QueryParser;
import org.outermedia.solrfusion.query.parser.QueryParser.Operator;

import java.util.Locale;
import java.util.Map;

/**
 * A common solr query parser.
 * 
 * @author ballmann
 * 
 */

@ToString
@Slf4j
@Getter
@Setter
public class EdisMaxQueryParser implements QueryParserIfc
{

	/**
	 * Factory creates instances only.
	 */
	private EdisMaxQueryParser()
	{}

	public static class Factory
	{
		public static EdisMaxQueryParser getInstance()
		{
			return new EdisMaxQueryParser();
		}
	}

	@Override
	public void init(QueryParserFactory config)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Query parse(Configuration config, Map<String, Float> boosts, String queryString, Locale locale)
	{
		Query result = null;
        if(queryString != null && queryString.trim().length() > 0)
        {
            String defaultOpStr = config.getDefaultOperator();
            Operator defaultOp = QueryParser.Operator.AND;
            try
            {
                defaultOp = QueryParser.Operator.valueOf(defaultOpStr);
            }
            catch (Exception e)
            {
                log.error("Found illegal default operator '{}'. Expected either 'or' or 'and'. Using {}.", defaultOpStr,
                    defaultOp, e);
            }
            QueryParser parser = new QueryParser(config.getDefaultSearchField(), config, boosts, defaultOp);
            parser.setLocale(locale);
            try
            {
                result = parser.parse(queryString);
            }
            catch (ParseException e)
            {
                log.error("Couldn't parse query: {}", queryString, e);
            }
        }
		return result;
	}
}
