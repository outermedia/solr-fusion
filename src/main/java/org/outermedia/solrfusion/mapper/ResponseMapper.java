package org.outermedia.solrfusion.mapper;

/*
 * #%L
 * SolrFusion
 * %%
 * Copyright (C) 2014 outermedia GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.IdGeneratorIfc;
import org.outermedia.solrfusion.ScoreCorrectorIfc;
import org.outermedia.solrfusion.configuration.*;
import org.outermedia.solrfusion.response.parser.Document;
import org.outermedia.solrfusion.response.parser.SolrMultiValuedField;
import org.outermedia.solrfusion.response.parser.SolrSingleValuedField;
import org.outermedia.solrfusion.types.ScriptEnv;

import java.util.*;

/**
 * Apply the configured mappings to a Solr response.
 * <p/>
 * Created by ballmann on 04.06.14.
 */
@Slf4j
@ToString(exclude = {"serverConfig", "doc", "config", "unmappedFields"})
public class ResponseMapper implements ResponseMapperIfc
{
    protected static final boolean MISSING_MAPPING_POLICY_IGNORE = true;
    protected static final boolean MISSING_MAPPING_POLICY_THROW_EXCEPTION = false;

    protected SearchServerConfig serverConfig;
    protected Document doc;
    protected boolean missingMappingPolicy;
    protected Configuration config;
    protected Collection<String> searchServerFieldNamesToMap;
    protected int numberOfMappedFields;
    protected Set<String> unmappedFields;
    protected ResponseTarget target;

    /**
     * Factory creates instances only.
     */
    protected ResponseMapper()
    {
        missingMappingPolicy = MISSING_MAPPING_POLICY_THROW_EXCEPTION;
    }

    public static class Factory
    {
        public static ResponseMapperIfc getInstance()
        {
            return new ResponseMapper();
        }
    }

    /**
     * Map a response of a certain search server (serverConfig) to the fusion fields. Already processed fields are
     * ignored.
     *
     * @param config                      the whole configuration
     * @param serverConfig                the currently used server's configuration
     * @param doc                         one response document to process
     * @param env                         the environment needed by the scripts which transform values
     * @param searchServerFieldNamesToMap either null (for all) or a set of searchServerFieldName fields to map
     * @param target
     * @return number of mapped fields
     */
    public int mapResponse(Configuration config, SearchServerConfig serverConfig, Document doc, ScriptEnv env,
        Collection<String> searchServerFieldNamesToMap, ResponseTarget target)
    {
        this.serverConfig = serverConfig;
        this.doc = doc;
        this.config = config;
        this.searchServerFieldNamesToMap = searchServerFieldNamesToMap;
        this.target = target;
        env.setConfiguration(config);
        env.setDocument(doc);
        unmappedFields = new HashSet<>();
        numberOfMappedFields = 0;

        setFusionDocId(config, doc);

        correctScore(doc);

        doc.accept(this, env);
        if (!unmappedFields.isEmpty())
        {
            log.warn("Please fix the fusion schema of server '{}'. Found no mapping for fields: {}",
                serverConfig.getSearchServerName(), unmappedFields);
        }

        int addedFieldCounter = addResponseFields(serverConfig, doc);

        return numberOfMappedFields + addedFieldCounter;
    }

    protected int addResponseFields(SearchServerConfig serverConfig, Document doc)
    {
        int addedFieldCounter = 0;
        AddOperation addOp = new AddOperation();
        Map<String, TargetsOfMapping> allAddResponseTargets = serverConfig.findAllAddResponseMappings(target);
        for (Map.Entry<String, TargetsOfMapping> entry : allAddResponseTargets.entrySet())
        {
            String fusionFieldName = entry.getKey();
            FusionField fusionField = config.findFieldByName(fusionFieldName);
            if (fusionField != null)
            {
                TargetsOfMapping targets = entry.getValue();
                String searchServerFieldName = targets.getMappingsSearchServerFieldName();
                boolean added = false;
                for (Target t : targets)
                {
                    if (addOp.addToResponse(doc, fusionFieldName, fusionField, searchServerFieldName, t))
                    {
                        added = true;
                    }
                }
                if (added)
                {
                    addedFieldCounter++;
                }
                Term term = doc.getFieldTermByFusionName(fusionFieldName);
                if (term != null)
                {
                    term.setWasMapped(true);
                    term.setProcessed(true);
                }
            }
            else
            {
                log.error("Can't add value for unknown fusion field '{}'.", fusionFieldName);
            }
        }
        return addedFieldCounter;
    }

    protected void correctScore(Document doc)
    {
        Term scoreTerm = doc.getFieldTermByName(DOC_FIELD_NAME_SCORE);
        if (scoreTerm != null && !scoreTerm.isProcessed())
        {
            try
            {
                ScoreCorrectorIfc scoreCorrector = serverConfig.getScoreCorrector();
                // if mapped use this value instead of search server's value
                List<String> searchServerScoreStr = scoreTerm.getFusionFieldValue();
                if (searchServerScoreStr == null || searchServerScoreStr.isEmpty())
                {
                    searchServerScoreStr = scoreTerm.getSearchServerFieldValue();
                }
                try
                {
                    double searchServerScore = Double.parseDouble(searchServerScoreStr.get(0));
                    double newScore = scoreCorrector.applyCorrection(searchServerScore);
                    scoreTerm.setFusionFieldName(DOC_FIELD_NAME_SCORE);
                    List<String> newScoreVal = new ArrayList<>();
                    newScoreVal.add(String.valueOf(newScore));
                    scoreTerm.setFusionFieldValue(newScoreVal);
                    scoreTerm.setWasMapped(true);
                    scoreTerm.setFusionField(config.findFieldByName(DOC_FIELD_NAME_SCORE));
                    scoreTerm.setProcessed(true);
                    numberOfMappedFields++;
                }
                catch (Exception e)
                {
                    log.warn("Can't parse double value '{}'. score is not corrected and not set.", searchServerScoreStr,
                        e);
                }
            }
            catch (Exception e)
            {
                log.error("Caught exception while correcting score", e);
            }
        }
        else
        {
            if (scoreTerm == null || !scoreTerm.isProcessed())
            {
                log.trace("Can't correct score, because document contains no value.");
            }
        }
    }

    protected List<ApplicableResult> getFieldMappings(String searchServerFieldName)
    {
        List<ApplicableResult> mappings = filterForResponse(
            serverConfig.findAllMappingsForSearchServerField(searchServerFieldName), target);
        if (mappings.isEmpty())
        {
            if (missingMappingPolicy == MISSING_MAPPING_POLICY_THROW_EXCEPTION)
            {
                String message =
                    "Found no mapping for search server field '" + searchServerFieldName + "' in configuration of '" +
                        serverConfig.getSearchServerName() + "'";
                throw new MissingSearchServerFieldMapping(message);
            }
            else
            {
                // collect unmapped fields; log message will be written later
                unmappedFields.add(searchServerFieldName);
            }
        }
        return mappings;
    }

    protected FusionField getFusionField(ScriptEnv env, ApplicableResult ar)
    {
        FusionField fusionField = env.getConfiguration().findFieldByName(ar.getDestinationFieldName());
        return fusionField;
    }

    protected void setFusionDocId(Configuration config, Document doc)
    {
        try
        {
            IdGeneratorIfc idGenerator = config.getIdGenerator();
            Term idTerm = doc.getFieldTermByName(serverConfig.getIdFieldName());
            if (idTerm != null && idTerm.getSearchServerFieldValue() != null)
            {
                if (!idTerm.isProcessed())
                {
                    String searchServerDocId = idTerm.getSearchServerFieldValue().get(0);
                    String id = idGenerator.computeId(serverConfig.getSearchServerName(), searchServerDocId);
                    idTerm.setFusionFieldName(idGenerator.getFusionIdField());
                    List<String> newId = new ArrayList<>();
                    newId.add(id);
                    idTerm.setFusionFieldValue(newId);
                    idTerm.setProcessed(true);
                    idTerm.setWasMapped(true);
                    idTerm.setFusionField(config.findFieldByName(config.getIdGenerator().getFusionIdField()));
                    numberOfMappedFields++;
                }
            }
            else
            {
                // TODO only if id was requested (fl)
                log.warn("Found no id (" + serverConfig.getIdFieldName() + ") in response of server '" +
                    serverConfig.getSearchServerName() + "'");
            }
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

    protected boolean processField(String searchServerFieldName, Term t)
    {
        boolean ok = !t.isProcessed();
        if (ok && searchServerFieldNamesToMap != null)
        {
            ok = searchServerFieldNamesToMap.contains(searchServerFieldName);
        }
        return ok;
    }

    protected void mapField(String fieldName, Term t, ScriptEnv env)
    {
        if (fieldName != null && processField(fieldName, t))
        {
            List<ApplicableResult> mappings = getFieldMappings(fieldName);
            if (mappings.size() > 0)
            {
                for (ApplicableResult ar : mappings)
                {
                    FieldMapping m = ar.getMapping();
                    boolean traceEnabled = log.isTraceEnabled();
                    if (traceEnabled)
                    {
                        log.trace("APPLY field={} mapping[line={}]={}", fieldName, m.getLocator().getLineNumber(), m);
                    }
                    m.applyResponseOperations(t, env, getFusionField(env, ar), ar, target);
                    if (traceEnabled)
                    {
                        log.trace("AFTER APPLY {}", doc.buildFusionDocStr());
                    }
                }
                t.setProcessed(true);
                numberOfMappedFields++;
            }
        }
    }

    /**
     * Compare with {@link org.outermedia.solrfusion.FusionRequest#filterForQuery(java.util.List,
     * org.outermedia.solrfusion.configuration.QueryTarget)}
     *
     * @param allMappingsForSearchServerField
     * @param target
     * @return
     */
    protected List<ApplicableResult> filterForResponse(List<ApplicableResult> allMappingsForSearchServerField,
        ResponseTarget target)
    {
        boolean foundOkChangeMapping = false;
        List<ApplicableResult> result = new ArrayList<>();
        for (int i = allMappingsForSearchServerField.size() - 1; i >= 0; i--)
        {
            boolean forResponseOk = false;
            ApplicableResult ar = allMappingsForSearchServerField.get(i);
            FieldMapping m = ar.getMapping();
            FieldMapping filteredMapping = new FieldMapping();
            List<Operation> filteredOps = new ArrayList<>();
            filteredMapping.setOperations(filteredOps);
            filteredMapping.setLocator(m.getLocator());
            List<Operation> ops = m.getOperations();
            // no operations = <om:change>
            if (ops == null || ops.size() == 0)
            {
                if (!foundOkChangeMapping)
                {
                    forResponseOk = true;
                    foundOkChangeMapping = true;
                    filteredOps.add(new ChangeOperation());
                    ar.setChanged(true);
                }
            }
            else
            {
                List<Target> allAddResponseTargets = m.getAllAddResponseTargets(target);
                if (allAddResponseTargets.size() > 0)
                {
                    forResponseOk = true;
                    AddOperation addOp = new AddOperation();
                    addOp.setTargets(allAddResponseTargets);
                    filteredOps.add(addOp);
                    ar.setAdded(true);
                }
                if (!foundOkChangeMapping)
                {
                    List<Target> allChangeResponseTargets = m.getAllChangeResponseTargets(target);
                    if (allChangeResponseTargets.size() > 0)
                    {
                        forResponseOk = true;
                        foundOkChangeMapping = true;
                        ChangeOperation changeOp = new ChangeOperation();
                        changeOp.setTargets(allChangeResponseTargets);
                        filteredOps.add(changeOp);
                        ar.setChanged(true);
                    }
                }
                List<Target> allDropResponseTargets = m.getAllDropResponseTargets(target);
                if (allDropResponseTargets.size() > 0)
                {
                    DropOperation dropOp = new DropOperation();
                    dropOp.setTargets(allDropResponseTargets);
                    filteredOps.add(dropOp);
                    forResponseOk = true;
                    ar.setDropped(true);
                }
            }
            if (forResponseOk)
            {
                result.add(
                    new ApplicableResult(ar.getDestinationFieldName(), filteredMapping, ar.isDropped(), ar.isChanged(),
                        ar.isAdded()));
            }
        }
        return result;
    }

    // ---- Visitor methods --------------------------------------------------------------------------------------------

    @Override
    public boolean visitField(SolrSingleValuedField sf, ScriptEnv env)
    {
        mapField(sf.getFieldName(), sf.getTerm(), env);
        // always continue visiting
        return true;
    }

    @Override
    public boolean visitField(SolrMultiValuedField sf, ScriptEnv env)
    {
        mapField(sf.getFieldName(), sf.getTerm(), env);
        // always continue visiting
        return true;
    }

}
