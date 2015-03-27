package org.outermedia.solrfusion.adapter.solr;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by ballmann on 3/27/15.
 */

@ToString
@EqualsAndHashCode
public class Version
{
    protected List<Integer> numericVersion;

    public Version(String dottedString)
    {
        numericVersion = new ArrayList<>();
        StringTokenizer st = new StringTokenizer(dottedString.trim(),".");
        while (st.hasMoreTokens())
        {
            numericVersion.add(Integer.parseInt(st.nextToken()));
        }
    }

    public boolean lessThan(Version other)
    {
        int pos;
        int thisSize = numericVersion.size();
        int otherSize = other.numericVersion.size();
        for (pos = 0; pos < thisSize && pos < otherSize; pos++)
        {
            if (numericVersion.get(pos) < other.numericVersion.get(pos))
            {
                return true;
            }
            if (numericVersion.get(pos) > other.numericVersion.get(pos))
            {
                return false;
            }
            // neither less nor greater -> equal, continue
        }

        // equal, but sizes are perhaps different

        // equal if sizes are equal
        if (pos == thisSize && pos == otherSize)
        {
            return false;
        }

        // if size is shorter, than it is "lessThan"
        return thisSize < otherSize;
    }
}
