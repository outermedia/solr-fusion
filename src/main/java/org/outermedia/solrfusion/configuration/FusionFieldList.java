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

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * Wrapper of all SolrFusion fields.
 *
 * Created by ballmann on 6/17/14.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "fusionSchemaFields", namespace = "http://solrfusion.outermedia.org/configuration/", propOrder =
        {
                "fusionFields"
        })
@Getter
@Setter
@ToString
public class FusionFieldList
{
    @XmlAttribute(name = "default-type", required = true)
    private String defaultType;

    @XmlElement(name = "field", namespace = "http://solrfusion.outermedia.org/configuration/", required = true)
    private List<FusionField> fusionFields;
}
