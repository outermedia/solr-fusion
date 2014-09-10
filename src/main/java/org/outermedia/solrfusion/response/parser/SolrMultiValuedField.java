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
import org.outermedia.solrfusion.mapper.Term;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import java.util.List;

/**
 * Data holder class keeping the field information of a solr response document.
 *
 * @author stephan
 */

@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
@ToString(callSuper = true)
public class SolrMultiValuedField extends SolrField
{

    @XmlElements(value =
            {
                    @XmlElement(name = "str"),
                    @XmlElement(name = "date"),
                    @XmlElement(name = "float"),
                    @XmlElement(name = "long"),
                    @XmlElement(name = "int")
            })
    private List<String> values;

    /**
     * Hook up unmarshalling in order to create an instance of
     * {@link org.outermedia.solrfusion.mapper.Term}.
     *
     * @param u      is the unmarshaller
     * @param parent the parent object
     */
    protected void afterUnmarshal(Unmarshaller u, Object parent)
    {
        Term t = Term.newSearchServerTerm(fieldName, values);
        setTerm(t);
    }

}
