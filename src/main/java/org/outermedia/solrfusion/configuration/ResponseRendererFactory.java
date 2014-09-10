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
import org.outermedia.solrfusion.response.ResponseRendererIfc;
import org.w3c.dom.Element;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * Data holder class to store the response renderer factory's class
 * configuration.
 * 
 * @author ballmann
 * 
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "responseRenderer", namespace = "http://solrfusion.outermedia.org/configuration/", propOrder = {"factoryConfig"})
@Getter
@Setter
@ToString(callSuper = true)
public class ResponseRendererFactory extends
	ConfiguredFactory<ResponseRendererIfc, ResponseRendererFactory>
{
	@XmlAttribute(name = "type", required = true)
	private ResponseRendererType type;

    @XmlAnyElement
    private List<Element> factoryConfig;
}
