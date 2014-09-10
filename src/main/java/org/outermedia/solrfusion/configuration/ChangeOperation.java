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
import org.outermedia.solrfusion.mapper.Term;
import org.outermedia.solrfusion.mapper.UndeclaredFusionField;
import org.outermedia.solrfusion.types.ScriptEnv;

import javax.xml.bind.UnmarshalException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * Data holder to store change operation configurations.
 *
 * @author ballmann
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "changeOperation", namespace = "http://solrfusion.outermedia.org/configuration/", propOrder =
        {
                "targets"
        })
@Getter
@Setter
@ToString(callSuper = true)
@Slf4j
public class ChangeOperation extends Operation
{
    @Override
    public void applyAllResponseOperations(Term term, ScriptEnv env, ResponseTarget target)
    {
        if (term.getFusionField() == null)
        {
            throw new UndeclaredFusionField("Didn't find field '" + env.getStringBinding(ScriptEnv.ENV_IN_FUSION_FIELD)
                    + "' in fusion schema. Please define it there.");
        }
        String specificFusionName = env.getStringBinding(ScriptEnv.ENV_IN_FUSION_FIELD);
        term.setFusionFieldName(specificFusionName);
        term.setWasMapped(true);
        super.applyAllResponseOperations(term, env, target);
    }

    @Override
    protected void check(FieldMapping fieldMapping) throws UnmarshalException
    {
        String msg = null;

        if(fieldMapping.getFusionName() == null)
        {
            msg = "A change operation requires a field name in attribute 'name' and 'fusion-name'.";
        }

        if(fieldMapping.getSearchServersName() == null)
        {
            msg = "A change operation requires a field name in attribute 'name' and 'fusion-name'.";
        }

        if (msg != null)
        {
            msg = "In fusion schema at line " + fieldMapping.geStartLineNumberInSchema() + ": " + msg;
            log.error(msg);
            throw new UnmarshalException(msg);
        }
    }

    @Override
    public void applyAllQueryOperations(Term term, ScriptEnv env, QueryTarget target)
    {
        if (term.getFusionField() == null)
        {
            throw new UndeclaredFusionField("Didn't find field '" + term.getFusionFieldName()
                    + "' in fusion schema. Please define it there.");
        }
        super.applyAllQueryOperations(term, env, target);
    }
}
