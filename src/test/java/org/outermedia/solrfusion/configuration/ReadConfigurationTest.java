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

import com.google.common.io.Files;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.outermedia.solrfusion.TestHelper;
import org.outermedia.solrfusion.types.Bsh;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

public class ReadConfigurationTest
{
    protected TestHelper helper;

    @Before
    public void setup()
    {
        helper = new TestHelper();
    }

    @Test
    public void readFusionSchema() throws JAXBException, SAXException, ParserConfigurationException, IOException
    {
        // one xml file which contains servers too
        String config1Out = addNewlines(
            helper.readFusionSchemaWithValidation("test-fusion-schema.xml", "configuration.xsd").toString());

        // this configuration uses <xi:include> to include server declarations
        String config2Out = addNewlines(
            helper.readFusionSchemaWithValidation("test-global-fusion-schema.xml", "configuration.xsd").toString());

        Assert.assertEquals("<xi:include> should work transparently, but differences occurred", addNewlines(config1Out),
            (config2Out));

        String expected = Files.toString(new File("src/test/resources/schema-toString.txt"), Charset.forName("UTF-8"));

        expected = addNewlines(expected);
        // System.out.println(config2Out);

        Assert.assertEquals("Found different configuration", expected, config2Out);
    }

    protected String addNewlines(String s)
    {
        return s.replace("[", "[\n\t").replace("),", "),\n\t").replaceAll("\\n\\s+", "\n ");
    }

    @Test
    public void checkXpath() throws FileNotFoundException, JAXBException, SAXException, ParserConfigurationException,
        XPathExpressionException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-fusion-schema.xml");

		/*
            <om:query type="beanshell">
		        <script><![CDATA[
		        	currentQuery = 
		        		currentQuery.replace("XXX",System.currentTimeMillis());
		        ]]></script>
		    </om:query>
		 */
        Target beanShellQuery = cfg.getSearchServerConfigs().getSearchServerConfigs().get(0).getFieldMappings().get(
            5).getOperations().get(0).getTargets().get(3);
        Bsh bsh = Bsh.getInstance();
        bsh.passArguments(beanShellQuery.getTypeConfig(), helper.getXmlUtil());
        String r = bsh.getCode().replace(" ", "").replace("\n", "");
        Assert.assertEquals("Xpath returned wrong value",
            "currentQuery=currentQuery.replace(\"XXX\",System.currentTimeMillis());", r);
    }

    @Test
    public void testMultiValue() throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-fusion-schema.xml");
        List<FusionField> fl = cfg.getFusionFields().getFusionFields();
        Assert.assertFalse("city shouldn't be a multi value field", findByName("city", fl).isMultiValue());
        Assert.assertTrue("city shouldn't be a multi value field", findByName("city", fl).isSingleValue());
        Assert.assertFalse("city shouldn't be a multi value field", findByName("city", fl).isMultiValue());
        Assert.assertTrue("multiValue1 shouldn't be a multi value field", findByName("multiValue1", fl).isMultiValue());
        Assert.assertFalse("multiValue1 shouldn't be a multi value field",
            findByName("multiValue1", fl).isSingleValue());
        Assert.assertTrue("multiValue1 shouldn't be a multi value field", findByName("multiValue1", fl).isMultiValue());
    }

    protected FusionField findByName(String name, List<FusionField> fl)
    {
        for (FusionField ff : fl)
        {
            if (ff.getFieldName().equals(name))
            {
                return ff;
            }
        }
        return null;
    }
}
