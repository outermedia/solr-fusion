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

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract super class of SolrFusion's servlets.
 *
 * Created by ballmann on 8/27/14.
 */
public class AbstractServlet extends HttpServlet
{
    protected String rebuildRequestUrl(HttpServletRequest request, Map<String, String[]> parameterMap)
        throws UnsupportedEncodingException
    {
        StringBuffer url = request.getRequestURL();
        if (url != null)
        {
            if (!parameterMap.isEmpty())
            {
                char sep = '?';
                for (String paramName : parameterMap.keySet())
                {
                    String[] paramValues = parameterMap.get(paramName);
                    if (paramValues != null)
                    {
                        for (String paramValue : paramValues)
                        {
                            url.append(sep);
                            url.append(paramName);
                            url.append('=');
                            url.append(URLEncoder.encode(paramValue, "UTF-8"));
                            sep = '&';
                        }
                    }
                    else
                    {
                        url.append(sep);
                        url.append(paramName);
                        url.append('=');
                        sep = '&';
                    }
                }
            }
            return url.toString();
        }
        else
        {
            return null;
        }
    }

    protected String buildPrintableParamMap(Map<String, ?> params)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        if (params != null)
        {
            for (String paramName : params.keySet())
            {
                Object paramValue = params.get(paramName);
                String s;
                if (paramValue.getClass().isArray())
                {
                    s = Arrays.toString((Object[]) paramValue);
                }
                else
                {
                    s = String.valueOf(paramValue);
                }
                sb.append("\t");
                sb.append(paramName);
                sb.append("=");
                sb.append(s);
                sb.append("\n");
            }
        }
        sb.append("}");
        return sb.toString();
    }

    protected Map<String, Object> collectHeader(HttpServletRequest request)
    {
        Map<String, Object> headerValues = new HashMap<>();
        Enumeration<String> headerNameEnum = request.getHeaderNames();
        if (headerNameEnum != null)
        {
            while (headerNameEnum.hasMoreElements())
            {
                String headerName = headerNameEnum.nextElement();
                headerValues.put(headerName, request.getHeader(headerName));
            }
        }
        headerValues.put(SolrFusionServlet.HEADER_LOCALE, request.getLocale());
        return headerValues;
    }

    protected long getCurrentTimeInMillis()
    {
        return System.currentTimeMillis();
    }
}
