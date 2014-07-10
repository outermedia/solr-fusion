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
        public static DefaultIdGenerator getInstance()
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
