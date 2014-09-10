package org.outermedia.solrfusion.mapper;

import org.outermedia.solrfusion.configuration.*;
import org.outermedia.solrfusion.response.parser.Document;
import org.outermedia.solrfusion.response.parser.FieldVisitor;
import org.outermedia.solrfusion.types.ScriptEnv;

import java.util.Collection;

/**
 * Apply the configured mappings to a Solr response.
 *
 * Created by ballmann on 6/11/14.
 */
public interface ResponseMapperIfc extends FieldVisitor, Initiable<ResponseMapperFactory>
{
    public String DOC_FIELD_NAME_SCORE = "score";
    public String FUSION_FIELD_NAME_SCORE = "score";

    /**
     * Map one Solr document, so that the document becomes compliant with the SolrFusion schema.
     * @param config    the SolrFusion schema
     * @param serverConfig  the current destination Solr server configuration
     * @param doc   the Solr document to map
     * @param env   the context information
     * @param searchServerFieldNamesToMap if null map all fields otherwise only the fields contained in this list
     * @param target    for which response part this response builder is called
     * @return the number of mapped fields (added ones included)
     */
    public int mapResponse(Configuration config, SearchServerConfig serverConfig, Document doc, ScriptEnv env,
        Collection<String> searchServerFieldNamesToMap, ResponseTarget target);

    /**
     * Per default an exception is thrown if no mapping for a field is found. A call of this methods switches this
     * policy to log a warn message only.
     */
    public void ignoreMissingMappings();
}
