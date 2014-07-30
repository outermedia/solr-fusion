package org.outermedia.solrfusion.mapper;

import com.google.common.base.Joiner;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.FusionField;
import org.outermedia.solrfusion.query.QueryParserIfc;
import org.outermedia.solrfusion.query.parser.Query;

import java.util.*;

/**
 * Data holder which stores a fusion field name and its value.
 *
 * @author sballmann
 */

@ToString
@Getter
@Setter
@Slf4j
public class Term
{
    private String fusionFieldName;
    private List<String> fusionFieldValue;
    private FusionField fusionField;
    private String searchServerFieldName;
    private List<String> searchServerFieldValue;

    // set by an remove operation
    private boolean removed;

    // set by a change operation
    private boolean wasMapped;

    private boolean processed;

    // added by an add operation
    private List<Query> newQueryTerms;


    private Term()
    {
        removed = false;
        wasMapped = false;
        processed = false;
    }

    public static Term newFusionTerm(String field, String... termStr)
    {
        return newFusionTerm(field, Arrays.asList(termStr));
    }

    public static Term newFusionTerm(String field, List<String> termStr)
    {
        Term result = new Term();
        result.fusionFieldName = field;
        result.fusionFieldValue = new ArrayList<>();
        result.fusionFieldValue.addAll(termStr);
        return result;
    }

    public static Term newSearchServerTerm(String field, String... termStr)
    {
        return newSearchServerTerm(field, Arrays.asList(termStr));
    }

    public static Term newSearchServerTerm(String field, List<String> termStr)
    {
        Term result = new Term();
        result.searchServerFieldName = field;
        result.searchServerFieldValue = new ArrayList<>();
        result.searchServerFieldValue.addAll(termStr);
        return result;
    }

    public String field()
    {
        return fusionFieldName;
    }

    public void resetQuery()
    {
        searchServerFieldName = null;
        searchServerFieldValue = null;
        removed = false;
        wasMapped = false;
        processed = false;
        newQueryTerms = null;
    }

    public void resetSearchServerField()
    {
        fusionFieldName = null;
        fusionFieldValue = null;
        fusionField = null;
        removed = false;
        wasMapped = false;
        processed = false;
    }

    public int compareFusionValue(Term t)
    {
        // sort the uncomparable to one end
        int unknownReturnValue = -1;
        if (t == null)
        {
            return unknownReturnValue;
        }
        List<String> thisFusionValues = getFusionFieldValue();
        List<String> otherFusionValues = t.getFusionFieldValue();
        if (thisFusionValues == null && otherFusionValues == null)
        {
            return 0;
        }
        if (thisFusionValues == null)
        {
            return -unknownReturnValue;
        }
        if (otherFusionValues == null)
        {
            return unknownReturnValue;
        }
        int thisSize = thisFusionValues.size();
        int otherSize = otherFusionValues.size();
        if (thisSize >= 1)
        {
            if (otherSize == 0)
            {
                return unknownReturnValue;
            }
            else
            {
                // for multi values only the first field is used!
                String otherValue = otherFusionValues.get(0);
                String thisValue = thisFusionValues.get(0);
                return thisValue.compareTo(otherValue);
            }
        }
        else
        {
            if (otherSize == 0)
            {
                return 0;
            }
            return -unknownReturnValue;
        }
    }

    /**
     * Add a new query part to a search server's query.
     *
     * @param inside                 if true or outside (false)
     * @param searchServerFieldValue one complete search server query
     */
    public void addNewSearchServerQuery(boolean inside, List<String> searchServerFieldValue,
        Configuration configuration, Locale locale)
    {
        if (newQueryTerms == null)
        {
            newQueryTerms = new ArrayList<>();
        }
        // TODO throw exception if searchServerFieldValue.size() == 0
        for (String qs : searchServerFieldValue)
        {
            // qs contains whole query, otherwise it would be difficult to mix-in the field name, because complex
            // queries are possible here
            Query q = null;
            Map<String, Float> boosts = null;
            try
            {
                QueryParserIfc queryParser = configuration.getQueryParser();
                q = queryParser.parse(configuration, boosts, qs, locale, Boolean.TRUE);
                q.setAddInside(inside);
                newQueryTerms.add(q);
            }
            catch (Exception e)
            {
                String msg = "Parsing of query " + qs + " failed.";
                log.error(msg, e);
            }
        }
        setWasMapped(true);
    }

    public String mergeFusionValues()
    {
        String result = null;
        if (fusionFieldValue != null)
        {
            result = Joiner.on(",").join(fusionFieldValue);
        }
        return result;
    }

    public String mergeSearchServerValues()
    {
        String result = null;
        if (searchServerFieldValue != null)
        {
            result = Joiner.on(",").join(searchServerFieldValue);
        }
        return result;
    }

}
