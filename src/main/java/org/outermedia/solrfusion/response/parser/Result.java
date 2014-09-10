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
 * Data holder class keeping the a solr result object.
 *
 * @author stephan
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "resultType")
@Getter
@Setter
@ToString
public class Result {

    @XmlAttribute(name = "name", required = true)
    private String resultName;

    @XmlAttribute(name = "numFound", required = true)
    private int numFound;

    @XmlAttribute(name = "start", required = true)
    private int start;

    @XmlAttribute(name = "maxScore", required = true)
    private float maxScore;

    @XmlElement(name = "doc", required = true)
    @Getter
    @Setter
    private List<Document> documents;

}
