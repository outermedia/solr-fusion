package org.outermedia.solrfusion.configuration;

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

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.mapper.MapOperation;
import org.outermedia.solrfusion.mapper.Term;
import org.outermedia.solrfusion.types.ScriptEnv;

import javax.xml.bind.UnmarshalException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

/**
 * Data holder to store drop operation configurations.
 *
 * @author ballmann
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "dropOperation", namespace = "http://solrfusion.outermedia.org/configuration/", propOrder = {"targets"})
@Getter
@Setter
@ToString(callSuper = true)
@Slf4j
public class DropOperation extends Operation
{
    public DropOperation()
    {
        setOp(MapOperation.DROP);
    }

    @Override
    public void applyAllQueryOperations(Term term, ScriptEnv env, QueryTarget target, int lineNumber)
    {
        List<Target> queryTargets = getQueryTargets(target);
        if (!queryTargets.isEmpty())
        {
            term.setRemoved(true);
            term.setSearchServerFieldValue(null);
        }
    }

    @Override
    public void applyAllResponseOperations(Term term, ScriptEnv env, ResponseTarget target, int lineNumber)
    {
        List<Target> responseTargets = getResponseTargets(target);
        if (!responseTargets.isEmpty())
        {
            term.setRemoved(true);
            term.setFusionFieldValue(null);
            term.setWasMapped(true);
        }
    }

    @Override
    public void check(FieldMapping fieldMapping) throws UnmarshalException
    {
        String msg = null;

        // checks for public void applyAllQueryOperations(Term term, ScriptEnv env)
        if (fieldMapping.isFusionFieldOnlyMapping())
        {
            List<Response> responseTargets = getResponseOnlyTargets(ResponseTarget.ALL);
            if (responseTargets.size() > 0)
            {
                msg = "Invalid configuration: It is impossible to remove a fusion field from a search server's " +
                    "response (<om:field fusion-name=\"...\"><om:drop><om:response/>). Use <om:query/> instead or " +
                    "change the fusion-name attribute to name. Please fix the fusion schema.";
            }
            List<Target> queryTargets = getQueryTargets(QueryTarget.ALL);
            if (queryTargets.isEmpty())
            {
                msg = "Invalid configuration: Found <om:drop> without <om:query> or <om:query-response> target.";
            }
        }

        // checks for public void applyAllResponseOperations(Term term, ScriptEnv env)
        if (fieldMapping.isSearchServerFieldOnlyMapping())
        {
            List<Query> queries = getQueryOnlyTargets(QueryTarget.ALL);
            if (queries.size() > 0)
            {
                msg = "Invalid configuration: It is impossible to remove a search server field " +
                    "from a fusion query (<om:field name=\"...\"><om:drop><om:query/>). Use <om:response/> instead " +
                    "or change the name attribute to fusion-name. Please fix the fusion schema.";
            }
            List<Target> targets = getResponseTargets(ResponseTarget.ALL);
            if (targets.isEmpty())
            {
                msg = "Invalid configuration: Found <om:drop> without <om:response> or <om:query-response> target.";
            }
        }
        if (msg != null)
        {
            msg = "In fusion schema at line " + fieldMapping.getStartLineNumberInSchema() + ": " + msg;
            log.error(msg);
            throw new UnmarshalException(msg);
        }
    }
}
