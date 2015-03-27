package org.outermedia.solrfusion;

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

/**
 * Solr uses HTTP request parameters with a variable part, so that it is necessary to store two values per parameter.
 * <p/>
 * Created by ballmann on 8/8/14.
 */
@Getter
@Setter
@ToString
@Slf4j
public class SolrFusionRequestParam
{
    private String value;
    private String paramNameVariablePart;
    private boolean containedInRequest;

    public SolrFusionRequestParam()
    {
        this(null, null);
    }

    public SolrFusionRequestParam(String value)
    {
        this(value, null);
    }

    public SolrFusionRequestParam(String value, String defaultValue)
    {
        this(value, null, defaultValue);
    }

    public SolrFusionRequestParam(String value, String paramNameVariablePart, String defaultValue)
    {
        this.containedInRequest = false;
        if (value != null)
        {
            value = value.trim();
            // sometimes tomcat returns "" even if a request parameter is not present
            this.containedInRequest = value.length() > 0;
        }
        else
        {
            if (defaultValue != null)
            {
                value = defaultValue;
            }
        }
        this.value = value;
        this.paramNameVariablePart = paramNameVariablePart;
    }

    public int getValueAsInt(int defaultValue)
    {
        int result = defaultValue;
        if (value != null && containedInRequest)
        {
            try
            {
                result = Integer.parseInt(value);
            }
            catch (Exception e)
            {
                log.error("Invalid int number. Can't parse int from '{}'.", value, e);
            }
        }
        return result;
    }

    public boolean getValueAsBool(boolean defaultValue)
    {
        boolean result = defaultValue;
        if (value != null && containedInRequest)
        {
            if ("true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value))
            {
                result = true;
            }
            else if ("false".equalsIgnoreCase(value) || "no".equalsIgnoreCase(value))
            {
                result = false;
            }
            else
            {
                log.warn("Unsupported bool value '" + value + "'. Known are 'yes', 'no', 'true' and 'false'.");
            }
        }
        return result;
    }
}
