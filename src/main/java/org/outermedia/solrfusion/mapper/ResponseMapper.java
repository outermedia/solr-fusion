package org.outermedia.solrfusion.mapper;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.IdGeneratorIfc;
import org.outermedia.solrfusion.ScoreCorrectorIfc;
import org.outermedia.solrfusion.configuration.*;
import org.outermedia.solrfusion.response.parser.Document;
import org.outermedia.solrfusion.response.parser.SolrMultiValuedField;
import org.outermedia.solrfusion.response.parser.SolrSingleValuedField;
import org.outermedia.solrfusion.types.ScriptEnv;

import java.util.List;

/**
 * Created by ballmann on 04.06.14.
 */
@Slf4j
@ToString(exclude = {"serverConfig", "doc"})
public class ResponseMapper implements ResponseMapperIfc
{
    protected static final boolean MISSING_MAPPING_POLICY_IGNORE = true;
    protected static final boolean MISSING_MAPPING_POLICY_THROW_EXCEPTION = false;

    protected static final String DOC_FIELD_NAME_SCORE = "score";
    private SearchServerConfig serverConfig;
    private Document doc;
    private boolean missingMappingPolicy;
    private Configuration config;

    /**
     * Factory creates instances only.
     */
    private ResponseMapper()
    {
        missingMappingPolicy = MISSING_MAPPING_POLICY_THROW_EXCEPTION;
    }

    public static class Factory
    {
        public static ResponseMapper getInstance()
        {
            return new ResponseMapper();
        }
    }

    /**
     * Map a response of a certain search server (serverConfig) to the fusion fields.
     *
     * @param config       the whole configuration
     * @param serverConfig the currently used server's configuration
     * @param doc          one response document to process
     * @param env          the environment needed by the scripts which transform values
     */
    public void mapResponse(Configuration config, SearchServerConfig serverConfig, Document doc, ScriptEnv env)
    {
        this.serverConfig = serverConfig;
        this.doc = doc;
        this.config = config;
        env.setConfiguration(config);
        doc.accept(this, env);
        setFusionDocId(config, doc);
        correctScore(doc);
    }

    protected void correctScore(Document doc)
    {
        Term scoreTerm = doc.getFieldTermByName(DOC_FIELD_NAME_SCORE);
        if (scoreTerm != null)
        {
            try
            {
                ScoreCorrectorIfc scoreCorrector = serverConfig.getScoreCorrector();
                // if mapped use this value instead of search server's value
                String searchServerScoreStr = scoreTerm.getFusionFieldValue();
                if (searchServerScoreStr == null)
                {
                    searchServerScoreStr = scoreTerm.getSearchServerFieldValue();
                }
                try
                {
                    double searchServerScore = Double.parseDouble(searchServerScoreStr);
                    double newScore = scoreCorrector.applyCorrection(searchServerScore);
                    scoreTerm.setFusionFieldName(DOC_FIELD_NAME_SCORE);
                    scoreTerm.setFusionFieldValue(String.valueOf(newScore));
                    scoreTerm.setWasMapped(true);
                    scoreTerm.setFusionField(config.findFieldByName(DOC_FIELD_NAME_SCORE));
                }
                catch (Exception e)
                {
                    log.warn("Can't parse double value '{}'. score is not corrected and not set.", searchServerScoreStr, e);
                }
            }
            catch (Exception e)
            {
                log.error("Caught exception while correcting score", e);
            }
        }
        else
        {
            log.warn("Can't correct score in documents, because document contains no value (any more).");
        }
    }

    protected List<FieldMapping> getFieldMappings(String searchServerFieldName)
    {
        List<FieldMapping> mappings = serverConfig.findAllMappingsForSearchServerField(searchServerFieldName);
        if (mappings.isEmpty())
        {
            if (missingMappingPolicy == MISSING_MAPPING_POLICY_THROW_EXCEPTION)
            {
                String message = "Found no mapping for fusion field '"
                        + searchServerFieldName + "'";
                throw new MissingSearchServerFieldMapping(message);
            }
            else
            {
                log.warn("Found no mapping for field '{}'", searchServerFieldName);
            }
        }
        return mappings;
    }

    protected FusionField getFusionField(ScriptEnv env, FieldMapping m)
    {
        FusionField fusionField = env.getConfiguration().findFieldByName(m.getFusionName());
        if (fusionField == null)
        {
            throw new UndeclaredFusionField("Didn't find field '" + m.getFusionName()
                    + "' in fusion schema. Please define it their.");
        }
        return fusionField;
    }

    protected void setFusionDocId(Configuration config, Document doc)
    {
        try
        {
            IdGeneratorIfc idGenerator = config.getIdGenerator();
            Term idTerm = doc.getFieldTermByName(serverConfig.getIdFieldName());
            if (idTerm == null || idTerm.getSearchServerFieldValue() == null)
            {
                throw new RuntimeException("Found no id (" + serverConfig.getIdFieldName() + ") in response of server '"
                        + serverConfig.getSearchServerName() + "'");
            }
            String id = idGenerator.computeId(serverConfig.getSearchServerName(), idTerm.getSearchServerFieldValue());
            idTerm.setFusionFieldName(idGenerator.getFusionIdField());
            idTerm.setFusionFieldValue(id);
        }
        catch (Exception e)
        {
            log.error("Caught exception while setting fusion doc id", e);
        }
    }

    @Override
    public void init(ResponseMapperFactory config)
    {
        Boolean ignoreMissingMappings = config.getIgnoreMissingMappings();
        missingMappingPolicy =
                (ignoreMissingMappings != null && ignoreMissingMappings.booleanValue()) ? MISSING_MAPPING_POLICY_IGNORE
                        : MISSING_MAPPING_POLICY_THROW_EXCEPTION;
    }

    public void ignoreMissingMappings()
    {
        missingMappingPolicy = MISSING_MAPPING_POLICY_IGNORE;
    }

    // ---- Visitor methods --------------------------------------------------------------------------------------------

    @Override
    public boolean visitField(SolrSingleValuedField sf, ScriptEnv env)
    {
        List<FieldMapping> mappings = getFieldMappings(sf.getFieldName());
        Term t = sf.getTerm();
        for (FieldMapping m : mappings)
        {
            m.applyResponseMappings(t, env, getFusionField(env, m));
        }
        // always continue visiting
        return true;
    }

    @Override
    public boolean visitField(SolrMultiValuedField sf, ScriptEnv env)
    {
        List<FieldMapping> mappings = getFieldMappings(sf.getFieldName());
        List<Term> terms = sf.getTerms();
        for (FieldMapping m : mappings)
        {
            m.applyResponseMappings(terms, env, getFusionField(env, m));
        }
        // always continue visiting
        return true;
    }

}
