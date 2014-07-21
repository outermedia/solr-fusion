package org.outermedia.solrfusion.mapper;

import org.junit.Assert;
import org.junit.Test;
import org.outermedia.solrfusion.configuration.*;
import org.outermedia.solrfusion.types.AbstractTypeTest;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileNotFoundException;

/**
 * Created by ballmann on 7/18/14.
 */
public class AddTest extends AbstractTypeTest
{
    @Test
    public void testBadAddMapping()
        throws FileNotFoundException, ParserConfigurationException, SAXException, JAXBException
    {
        Configuration cfg = helper.readFusionSchemaWithoutValidation("test-script-types-fusion-schema.xml");
        SearchServerConfig searchServerConfig = cfg.getSearchServerConfigs().getSearchServerConfigs().get(0);

        // make bad add query
        FieldMapping text11Mapping = searchServerConfig.findAllMappingsForFusionField("text11").get(0);
        text11Mapping.setSearchServersName(null);
        text11Mapping.setMappingType(MappingType.EXACT_FUSION_NAME_ONLY);
        // System.out.println("BAD ADD " + text9Mapping);
        AddOperation add = (AddOperation) text11Mapping.getOperations().get(0);
        try
        {
            add.check(text11Mapping);
            Assert.fail("Expected exception for bad drop query.");
        }
        catch (Exception e)
        {
            Assert.assertEquals("Got other error message than expected",
                "In fusion schema at line 288: Please specify a field for attribute 'name' in order to add something to a query.",
                e.getMessage());
        }

        // make bad add response
        FieldMapping text12Mapping = searchServerConfig.findAllMappingsForFusionField("text12").get(0);
        text12Mapping.setFusionName(null);
        text12Mapping.setMappingType(MappingType.EXACT_NAME_ONLY);
        // System.out.println("BAD ADD " + text10Mapping);
        add = (AddOperation) text12Mapping.getOperations().get(0);
        try
        {
            add.check(text12Mapping);
            Assert.fail("Expected exception for bad drop query.");
        }
        catch (Exception e)
        {
            Assert.assertEquals("Got other error message than expected",
                "In fusion schema at line 295: Please specify a field for attribute 'fusion-name' in order to add something to a response.",
                e.getMessage());
        }

        // TODO check for missing type attribute -> Error
    }
}
