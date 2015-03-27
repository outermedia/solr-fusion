package org.outermedia.solrfusion.response;

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

import freemarker.template.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.outermedia.solrfusion.FusionRequest;
import org.outermedia.solrfusion.FusionResponse;
import org.outermedia.solrfusion.adapter.SearchServerResponseInfo;
import org.outermedia.solrfusion.configuration.ResponseRendererFactory;
import org.outermedia.solrfusion.response.freemarker.*;
import org.outermedia.solrfusion.response.parser.DocCount;
import org.outermedia.solrfusion.response.parser.Document;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Transforms Solr search results into an output format using freemarker templates. This is the base class of
 * specific renderers (json and xml).
 *
 * @author stephan
 */
@Slf4j
public class FreemarkerResponseRenderer implements TextResponseRendererIfc
{
    public final String XMLTEMPLATEFILE = "xml.ftl";
    public final String JSONTEMPLATEFILE = "json.ftl";
    public final String PHPTEMPLATEFILE = "php.ftl";

    private final String defaultEncoding = "UTF-8";
    private final String defaultLocale = "de-DE";

    private Configuration freemarkerConfig;

    @Getter @Setter
    private String templateFile;

    @Getter
    private String encoding;

    public void setEncoding(String encoding)
    {
        if (freemarkerConfig == null)
        {
            return;
        }
        freemarkerConfig.setDefaultEncoding(encoding);
        this.encoding = encoding;
    }

    @Getter
    private String locale;

    public void setLocale(String locale)
    {
        if (freemarkerConfig == null)
        {
            return;
        }
        freemarkerConfig.setLocale(Locale.forLanguageTag(locale));
        this.locale = locale;
    }

    public FreemarkerResponseRenderer()
    {
        templateFile = XMLTEMPLATEFILE;
        encoding = defaultEncoding;
        locale = defaultLocale;
    }

    @Override
    public void writeResponse(org.outermedia.solrfusion.configuration.Configuration configuration,
        ClosableIterator<Document, SearchServerResponseInfo> docStream, FusionRequest request,
        FusionResponse fusionResponse)
    {
        // prepare the template input:
        Map<String, Object> input = new HashMap<String, Object>();

        FreemarkerResponse freemarkerResponse = new FreemarkerResponse(configuration, docStream);
        FreemarkerResponseHeader freemarkerResponseHeader = new FreemarkerResponseHeader(docStream, request, fusionResponse);
        FreemarkerErrorHeader freemarkerErrorHeader = new FreemarkerErrorHeader(fusionResponse);
        Map<String, Document> highlighting = null;
        Map<String, List<DocCount>> facets = null;
        if (docStream != null)
        {
            highlighting = docStream.getExtraInfo().getHighlighting();
            facets = docStream.getExtraInfo().getFacetFields();
        }
        FreemarkerResponseHighlighting freemarkerHighlighting = new FreemarkerResponseHighlighting(configuration,
            highlighting);
        FreemarkerFacets freemarkerFacets = new FreemarkerFacets(configuration, facets);

        input.put("responseHeader", freemarkerResponseHeader);
        input.put("responseError", freemarkerErrorHeader);
        input.put("response", freemarkerResponse);
        input.put("highlighting", freemarkerHighlighting);
        input.put("facets", freemarkerFacets);

        // Get the template
        Template template = null;
        try
        {
            template = freemarkerConfig.getTemplate(templateFile);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        // Write output to the string
        StringWriter stringWriter = new StringWriter();
        try
        {
            template.process(input, stringWriter);
        }
        catch (Exception e)
        {
            log.error("Caught exception while applying template " + templateFile, e);
        }

        PrintWriter pw = fusionResponse.textWriter();
        // have to set the flag here, because perhaps the data is written partially
        fusionResponse.wroteSomeData();
        pw.println(stringWriter.getBuffer().toString());
        pw.flush();
    }

    @Override
    public void init(ResponseRendererFactory config)
    {
        initFreemarkerConfiguration();
    }

    private void initFreemarkerConfiguration()
    {
        // 1. Configure FreeMarker
        freemarkerConfig = new Configuration();
        freemarkerConfig.setWhitespaceStripping(true);

        // Where do we load the templates from:
        freemarkerConfig.setClassForTemplateLoading(FreemarkerResponseRenderer.class, "templates");

        // Some other recommended settings:
        freemarkerConfig.setIncompatibleImprovements(new Version(2, 3, 20));
        freemarkerConfig.setDefaultEncoding(defaultEncoding);
        freemarkerConfig.setLocale(Locale.forLanguageTag(defaultLocale));
        freemarkerConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

    }

    public static FreemarkerResponseRenderer getInstance()
    {
        return new FreemarkerResponseRenderer();
    }
}

