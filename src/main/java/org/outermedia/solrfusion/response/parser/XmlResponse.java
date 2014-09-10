package org.outermedia.solrfusion.response.parser;

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

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * Parses a solr server's xml response into an internal representation.
 *
 * @author ballmann
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"header", "results"})
@XmlRootElement(name = "response")
@ToString
public class XmlResponse
{
    // this field contains the facet and highlighting info too, because jaxb handles same elements ("lst") this ways
    @XmlElement(name = "lst", required = true)
    private List<ResponseSection> header;

    @XmlElement(name = "result", required = true)
    private List<Result> results;

    @XmlTransient @Getter @Setter
    private Exception errorReason;

    public ResponseSection getResponseHeader()
    {
        return findSectionByName("responseHeader");
    }

    public ResponseSection getResponseErrors()
    {
        return findSectionByName("error");
    }

    public List<Highlighting> getHighlighting()
    {
        List<Highlighting> result = null;
        ResponseSection section = findSectionByName("highlighting");
        if (section != null)
        {
            result = section.getHighlighting();
        }
        return result;
    }

    public Document getFacetFields(String searchServerIdField, int docId)
    {
        Document result = null;
        ResponseSection section = findSectionByName("facet_counts");
        if (section != null)
        {
            result = section.getFacetDocument(searchServerIdField, docId);
        }
        return result;
    }

    protected ResponseSection findSectionByName(String n)
    {
        ResponseSection result = null;
        if (header != null)
        {
            for (ResponseSection re : header)
            {
                if (n.equals(re.getName()))
                {
                    result = re;
                    break;
                }
            }
        }
        return result;
    }

    protected Result findResultByName(String name)
    {
        Result aResult = null;
        if(results != null)
        {
            for(Result result : results)
            {
                if(name.equals(result.getResultName()))
                {
                    aResult = result;
                    break;
                }
            }
        }
        return aResult;
    }

    protected List<Document> getDocumentsImpl(String resultName)
    {
        List<Document> docs = null;
        Result result = findResultByName(resultName);
        if (result != null)
        {
            docs = result.getDocuments();
        }
        return docs;
    }

    /**
     * Get the documents of the result list named "response".
     *
     * @return
     */
    public List<Document> getDocuments()
    {
        return getDocumentsImpl("response");
    }

    /**
     * Get the documents of the result list named "match".
     *
     * @return
     */
    public List<Document> getMatchDocuments()
    {
        return getDocumentsImpl("match");
    }

    protected int getNumFoundImpl(String resultName)
    {
        int nr = 0;
        Result result = findResultByName(resultName);
        if (result != null)
        {
            nr = result.getNumFound();
        }
        return nr;
    }

    /**
     * Get the numFound value of the result list named "response".
     *
     * @return
     */
    public int getNumFound()
    {
        return getNumFoundImpl("response");
    }

    /**
     * Get the numFound value of the result list named "match".
     *
     * @return
     */
    public int getMatchNumFound()
    {
        return getNumFoundImpl("match");
    }
}
