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
import lombok.extern.slf4j.Slf4j;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * Created by ballmann on 8/11/14.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "responseFacetField")
@ToString
@Slf4j
@Getter
@Setter
public class FacetHit
{
    @XmlAttribute(name = "name", required = true)
    private String searchServerFieldName;

    @XmlElement(name = "int", required = true)
    private List<DocCount> fieldCounts;

}
