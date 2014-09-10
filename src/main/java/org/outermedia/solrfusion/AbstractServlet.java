package org.outermedia.solrfusion;

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
