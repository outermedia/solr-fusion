package org.outermedia.solrfusion;

import lombok.Getter;
import lombok.ToString;

import org.outermedia.solrfusion.configuration.IdGeneratorFactory;

/**
 * The unified documents need an id, which are created by this generator.
 *
 * @author ballmann
 */

@ToString
@Getter
public class DefaultIdGenerator implements IdGeneratorIfc
{
    private final static String SEPARATOR = "#";
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
            throw new RuntimeException(
                "Can't handle server names containing '" + SEPARATOR + "': '" + serverName);
        }
        return serverName + SEPARATOR + searchServerDocId;
    }

    @Override public String getSearchServerIdFromFusionId(String fusionDocId)
    {
        int hashPos = fusionDocId.indexOf(SEPARATOR);
        return fusionDocId.substring(0, hashPos);
    }

    @Override public String getSearchServerDocIdFromFusionId(String fusionDocId)
    {
        int hashPos = fusionDocId.indexOf(SEPARATOR);
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
