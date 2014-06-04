package org.outermedia.solrfusion.mapper;

import org.outermedia.solrfusion.configuration.Configuration;
import org.outermedia.solrfusion.configuration.FieldMapping;
import org.outermedia.solrfusion.configuration.FusionField;
import org.outermedia.solrfusion.configuration.SearchServerConfig;
import org.outermedia.solrfusion.response.parser.Document;
import org.outermedia.solrfusion.response.parser.FieldVisitor;
import org.outermedia.solrfusion.response.parser.SolrField;
import org.outermedia.solrfusion.types.ScriptEnv;

import java.util.List;

/**
 * Created by ballmann on 04.06.14.
 */
public class ResponseMapper implements FieldVisitor
{
    private SearchServerConfig serverConfig;
    private Document doc;

    /**
     * Map a response of a certain search server (serverConfig) to the fusion fields.
     *
     * @param config the whole configuration
     * @param serverConfig the currently used server's configuration
     * @param doc one response document to process
     * @param env the environment needed by the scripts which transform values
     */
    public void mapResponse(Configuration config, SearchServerConfig serverConfig, Document doc, ScriptEnv env)
    {
        this.serverConfig = serverConfig;
        this.doc = doc;
        env.setConfiguration(config);
        doc.accept(this, env);
    }

    // ---- Visitor methods --------------------------------------------------------------------------------------------

    @Override
    public boolean visitField(SolrField sf, ScriptEnv env)
    {
        Term t = sf.getTerm();
        List<FieldMapping> mappings = serverConfig.findAllMappingsForSearchServerField(sf.getFieldName());
        for (FieldMapping m : mappings)
        {
            FusionField fusionField = env.getConfiguration().findFieldByName(m.getFusionName());
            if(fusionField == null)
            {
                throw new UndeclaredFusionField("Didn't find field '"+m.getFusionName()+"' in fusion schema. Please define it their.");
            }
            t.setFusionField(fusionField);
            m.applyResponseMappings(t, env);
        }

        // always continue visiting
        return true;
    }
}
