package org.outermedia.solrfusion;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.configuration.IdGeneratorFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * The unified documents need an id, which are created by this generator.
 *
 * @author ballmann
 */

/**
 * This class creates for a given server name and a solr document id a unique fusion document id. Don't use "#" or "_"
 * in server names.
 */
@ToString
@Getter
@Slf4j
public class DefaultIdGenerator implements IdGeneratorIfc
{
    // "-" and "_" are valid chars in html ids and css class names. Can't use &#92;XXXX, because vufind would
    // not escape the backslash in queries.
    public final static String SEPARATOR = "_";
    public final static String ID_SEPARATOR = "-";

    private String fusionIdField;

    /**
     * Factory creates instances only.
     */
    private DefaultIdGenerator()
    {
    }

    @Override
    public String computeId(String serverName, String searchServerDocId)
    {
        if (serverName.contains(SEPARATOR))
        {
            throw new RuntimeException("Can't handle server names containing '" + SEPARATOR + "': '" + serverName);
        }
        if (serverName.contains(ID_SEPARATOR))
        {
            throw new RuntimeException("Can't handle server names containing '" + SEPARATOR + "': '" + serverName);
        }
        if (serverName.contains(" "))
        {
            throw new RuntimeException("Can't handle server names containing SPACE: '" + serverName);
        }
        return serverName + SEPARATOR + searchServerDocId;
    }

    /**
     * Get the search server name (used in fusion schema) from the given fusion doc id. In the case of a merged fusion
     * doc id, the first fusion doc id is used.
     *
     * @param fusionDocId
     * @return
     */
    @Override public String getSearchServerIdFromFusionId(String fusionDocId)
    {
        int hashPos = fusionDocId.indexOf(SEPARATOR);
        if (hashPos < 0)
        {
            log.warn("Wrong id field value: '{}'", fusionDocId);
        }
        return fusionDocId.substring(0, hashPos);
    }

    /**
     * Get the search server's doc id from the given fusion doc id. In the case of a merged fusion doc id, the first
     * fusion doc id is used.
     *
     * @param fusionDocId
     * @return
     */
    @Override public String getSearchServerDocIdFromFusionId(String fusionDocId)
    {
        if (fusionDocId.contains(ID_SEPARATOR))
        {
            fusionDocId = fusionDocId.substring(0, fusionDocId.indexOf(ID_SEPARATOR));
        }
        int hashPos = fusionDocId.indexOf(SEPARATOR);
        if (hashPos < 0)
        {
            log.warn("Wrong id field value: '{}'", fusionDocId);
        }
        return fusionDocId.substring(hashPos + SEPARATOR.length());
    }

    /**
     * Returns a merged id if otherId is not contained in thisId. Otherwise thisId is returned.
     *
     * @param thisId  not null
     * @param otherId not null
     * @return either thisId or a new merged id
     */
    @Override public String mergeIds(String thisId, String otherId)
    {
        String result = thisId;
        if (!thisId.contains(otherId))
        {
            result = thisId + ID_SEPARATOR + otherId;
        }
        return result;
    }

    @Override public List<String> splitMergedId(String mergedIds)
    {
        List<String> result = new ArrayList<>();
        String split[] = mergedIds.split(ID_SEPARATOR.replace("\\", "\\\\"));
        if (split != null)
        {
            for (String s : split)
            {
                s = s.trim();
                if (s.length() > 0)
                {
                    result.add(s);
                }
            }
        }
        return result;
    }

    @Override public boolean isMergedDocument(String fusionDocId)
    {
        return fusionDocId.contains(ID_SEPARATOR);
    }

    public static class Factory
    {
        public static IdGeneratorIfc getInstance()
        {
            return new DefaultIdGenerator();
        }
    }

    @Override
    public void init(IdGeneratorFactory config)
    {
        fusionIdField = config.getFusionFieldName();
    }

}
