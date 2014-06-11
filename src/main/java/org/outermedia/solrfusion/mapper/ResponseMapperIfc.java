package org.outermedia.solrfusion.mapper;

import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.Initiable;
import org.outermedia.solrfusion.configuration.ResponseMapperFactory;
import org.outermedia.solrfusion.configuration.SearchServerConfig;
import org.outermedia.solrfusion.response.parser.Document;
import org.outermedia.solrfusion.types.ScriptEnv;

/**
 * Created by ballmann on 6/11/14.
 */
public interface ResponseMapperIfc extends Initiable<ResponseMapperFactory>
{
    public void mapResponse(Configuration config, SearchServerConfig serverConfig, Document doc, ScriptEnv env);
}
