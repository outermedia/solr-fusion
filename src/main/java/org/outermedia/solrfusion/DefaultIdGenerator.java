package org.outermedia.solrfusion;

import lombok.Getter;
import lombok.ToString;

import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.configuration.IdGeneratorFactory;

/**
 * The unified documents need an id, which are created by this generator.
 *
 * @author ballmann
 */

/**
 * This class creates for a given server name and a solr document id a unique fusion document id. Don't use "#" in
 * server names.
 */
@ToString
@Getter
@Slf4j
public class DefaultIdGenerator implements IdGeneratorIfc
{
    // "-" and "_" are valid chars in html ids and css class names
    public final static String SEPARATOR = "-";
    public final static String SPACE_REPLACEMENT = "_";
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
        if (serverName.contains(SPACE_REPLACEMENT))
        {
            throw new RuntimeException(
                "Can't handle server names containing '" + SPACE_REPLACEMENT + "': '" + serverName);
        }
        return serverName.replace(" ", SPACE_REPLACEMENT) + SEPARATOR + searchServerDocId;
    }

    @Override public String getSearchServerIdFromFusionId(String fusionDocId)
    {
        int hashPos = fusionDocId.indexOf(SEPARATOR);
        if (hashPos < 0)
        {
            log.warn("Wrong id field value: '{}'", fusionDocId);
        }
        return fusionDocId.substring(0, hashPos).replace(SPACE_REPLACEMENT, " ");
    }

    @Override public String getSearchServerDocIdFromFusionId(String fusionDocId)
    {
        int hashPos = fusionDocId.indexOf(SEPARATOR);
        if (hashPos < 0)
        {
            log.warn("Wrong id field value: '{}'", fusionDocId);
        }
        return fusionDocId.substring(hashPos + 1);
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
