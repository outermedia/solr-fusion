package org.outermedia.solrfusion.mapper;

import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.Initiable;
import org.outermedia.solrfusion.configuration.ResponseMapperFactory;
import org.outermedia.solrfusion.configuration.SearchServerConfig;
import org.outermedia.solrfusion.response.parser.Document;
import org.outermedia.solrfusion.response.parser.FieldVisitor;
import org.outermedia.solrfusion.types.ScriptEnv;

import java.util.Collection;

/**
 * Created by ballmann on 6/11/14.
 */
public interface ResponseMapperIfc extends FieldVisitor, Initiable<ResponseMapperFactory>
{
    public String DOC_FIELD_NAME_SCORE = "score";
    public String FUSION_FIELD_NAME_SCORE = "score";

    public int mapResponse(Configuration config, SearchServerConfig serverConfig, Document doc, ScriptEnv env,
        Collection<String> searchServerFieldNamesToMap);

    public void ignoreMissingMappings();
}
