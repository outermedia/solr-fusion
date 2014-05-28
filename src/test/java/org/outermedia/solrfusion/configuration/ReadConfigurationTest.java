package org.outermedia.solrfusion.configuration;

import java.io.FileNotFoundException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

public class ReadConfigurationTest
{
	protected Util xmlUtil;

	@Before
	public void setup()
	{
		xmlUtil = new Util();
	}

	@Test
	public void readFusionSchema() throws JAXBException, SAXException,
		FileNotFoundException, ParserConfigurationException
	{
		// one xml file which contains servers too
		String config1 = "test-fusion-schema.xml";

		// with validation
		String schemaPath = "configuration.xsd";

		Configuration cfg1 = xmlUtil.unmarshal(Configuration.class, config1,
			schemaPath);
		Assert.assertNotNull(
			"Expected configuration object, but could't read in the xml file "
				+ config1, cfg1);

		String config1Out = cfg1.toString();
		// System.out.println("CONFIG1 " + config1Out);

		// this configuration uses <xi:include> to include server declarations
		String config2 = "test-global-fusion-schema.xml";
		Configuration cfg2 = xmlUtil.unmarshal(Configuration.class, config2,
			schemaPath);
		Assert.assertNotNull(
			"Expected configuration object, but could't read in the xml file "
				+ config2, cfg2);

		String config2Out = cfg2.toString();
		// System.out.println("CONFIG2 " + config2Out);

		Assert.assertEquals(
			"<xi:include> should work transparently, but differences occurred",
			config1Out, config2Out);

		String expected = "Configuration(fusionFields=[FusionField(fieldName=id, type=string, format=null), FusionField(fieldName=city, type=string, format=null), FusionField(fieldName=title, type=string, format=null), FusionField(fieldName=numberExample, type=int, format=null), FusionField(fieldName=mappingExample, type=string, format=null), FusionField(fieldName=computingExample, type=string, format=null), FusionField(fieldName=publicationDate, type=date, format=dd.MM.yyyy)], scriptTypes=[ScriptType(super=ConfiguredFactory(classFactory=org.outermedia.solrfusion.types.JsFile, implementation=JsFile(super=AbstractType())), name=javascript-file), ScriptType(super=ConfiguredFactory(classFactory=org.outermedia.solrfusion.types.BshFile, implementation=BshFile(super=AbstractType())), name=beanshell-file), ScriptType(super=ConfiguredFactory(classFactory=org.outermedia.solrfusion.types.Js, implementation=Js(super=AbstractType())), name=javascript), ScriptType(super=ConfiguredFactory(classFactory=org.outermedia.solrfusion.types.Bsh, implementation=Bsh(super=AbstractType())), name=beanshell), ScriptType(super=ConfiguredFactory(classFactory=org.outermedia.solrfusion.types.Java, implementation=Java(super=AbstractType())), name=java-class), ScriptType(super=ConfiguredFactory(classFactory=org.outermedia.solrfusion.types.RegularExpression, implementation=RegularExpression(super=AbstractType())), name=regexp), ScriptType(super=ConfiguredFactory(classFactory=org.outermedia.solrfusion.types.TableFile, implementation=TableFile(super=AbstractType())), name=static-table-file), ScriptType(super=ConfiguredFactory(classFactory=org.outermedia.solrfusion.types.Table, implementation=Table(super=AbstractType())), name=static-table), ScriptType(super=ConfiguredFactory(classFactory=org.outermedia.solrfusion.types.DummyType, implementation=DummyType()), name=extract-width), ScriptType(super=ConfiguredFactory(classFactory=org.outermedia.solrfusion.types.DummyType, implementation=DummyType()), name=extract-height), ScriptType(super=ConfiguredFactory(classFactory=org.outermedia.solrfusion.types.DummyType, implementation=DummyType()), name=extract-depth), ScriptType(super=ConfiguredFactory(classFactory=org.outermedia.solrfusion.types.DummyType, implementation=DummyType()), name=merge-measure-units)], defaultSearchField=title, defaultOperator=AND, idGeneratorFactory=IdGeneratorFactory(super=ConfiguredFactory(classFactory=org.outermedia.solrfusion.DefaultIdGenerator$Factory, implementation=DefaultIdGenerator()), fusionFieldName=id), searchServerConfigs=GlobalSearchServerConfig(timeout=4000, disasterLimit=3, disasterMessage=Message(key=disaster-limit, text=Ihre Anfrage konnte nicht von ausreichend\n"
			+ "            vielen Systemen beantwortet werden.\n"
			+ "        ), queryParserFactory=QueryParserFactory(super=ConfiguredFactory(classFactory=org.outermedia.solrfusion.query.EdisMaxQueryParser$Factory, implementation=EdisMaxQueryParser())), defaultResponseParserFactory=ResponseParserFactory(super=ConfiguredFactory(classFactory=org.outermedia.solrfusion.response.DefaultResponseParser$Factory, implementation=DefaultResponseParser())), responseRendererFactories=[ResponseRendererFactory(super=ConfiguredFactory(classFactory=org.outermedia.solrfusion.response.DefaultXmlResponseRenderer$Factory, implementation=DefaultXmlResponseRenderer()), type=XML), ResponseRendererFactory(super=ConfiguredFactory(classFactory=org.outermedia.solrfusion.response.DefaultJsonResponseRenderer$Factory, implementation=DefaultJsonResponseRenderer()), type=JSON), ResponseRendererFactory(super=ConfiguredFactory(classFactory=org.outermedia.solrfusion.response.DefaultPhpResponseRenderer$Factory, implementation=DefaultPhpResponseRenderer()), type=PHP)], merge=Merge(super=ConfiguredFactory(classFactory=org.outermedia.solrfusion.DefaultMergeStrategy$Factory, implementation=DefaultMergeStrategy()), fusionName=ISBN, targets=[MergeTarget(prio=1, targetName=Bibliothek A), MergeTarget(prio=2, targetName=Bibliothek B)]), searchServerConfigs=[SearchServerConfig(super=ConfiguredFactory(classFactory=org.outermedia.solrfusion.adapter.solr.DefaultSolrAdapter$Factory, implementation=DefaultSolrAdapter()), searchServerName=Bibliothek A, searchServerVersion=3.6, url=http://host:port/solr/xyz, scoreFactory=ScoreFactory(super=ConfiguredFactory(classFactory=org.outermedia.solrfusion.DefaultScore$Factory, implementation=DefaultScore()), factor=1.2), responseParserFactory=ResponseParserFactory(super=ConfiguredFactory(classFactory=org.outermedia.solrfusion.SpecialResponseParser$Factory, implementation=SpecialResponseParser())), idFieldName=id, fieldMappings=[FieldMapping(searchServersName=start, fusionName=city, operations=null), FieldMapping(searchServersName=*_text, fusionName=*_t, operations=null), FieldMapping(searchServersName=int_*, fusionName=i_*, operations=null), FieldMapping(searchServersName=u, fusionName=user, operations=[ChangeOperation(super=Operation(targets=[QueryResponse(super=Target(type=ScriptType(super=ConfiguredFactory(classFactory=org.outermedia.solrfusion.types.TableFile, implementation=TableFile(super=AbstractType())), name=static-table-file), name=null, fusionName=null, typeConfig=[[file: null]], typeImpl=TableFile(super=AbstractType())))]))]), FieldMapping(searchServersName=u, fusionName=user, operations=[ChangeOperation(super=Operation(targets=[QueryResponse(super=Target(type=ScriptType(super=ConfiguredFactory(classFactory=org.outermedia.solrfusion.types.Table, implementation=Table(super=AbstractType())), name=static-table), name=null, fusionName=null, typeConfig=[[entry: null], [entry: null]], typeImpl=Table(super=AbstractType())))]))]), FieldMapping(searchServersName=start, fusionName=city, operations=[ChangeOperation(super=Operation(targets=[Response(super=Target(type=ScriptType(super=ConfiguredFactory(classFactory=org.outermedia.solrfusion.types.JsFile, implementation=JsFile(super=AbstractType())), name=javascript-file), name=null, fusionName=null, typeConfig=[[file: null]], typeImpl=JsFile(super=AbstractType()))), Response(super=Target(type=ScriptType(super=ConfiguredFactory(classFactory=org.outermedia.solrfusion.types.Java, implementation=Java(super=AbstractType())), name=java-class), name=null, fusionName=null, typeConfig=[[class: null]], typeImpl=Java(super=AbstractType()))), Query(super=Target(type=ScriptType(super=ConfiguredFactory(classFactory=org.outermedia.solrfusion.types.BshFile, implementation=BshFile(super=AbstractType())), name=beanshell-file), name=null, fusionName=null, typeConfig=[[file: null]], typeImpl=BshFile(super=AbstractType()))), Query(super=Target(type=ScriptType(super=ConfiguredFactory(classFactory=org.outermedia.solrfusion.types.Bsh, implementation=Bsh(super=AbstractType())), name=beanshell), name=null, fusionName=null, typeConfig=[[script: null]], typeImpl=Bsh(super=AbstractType()))), Query(super=Target(type=ScriptType(super=ConfiguredFactory(classFactory=org.outermedia.solrfusion.types.RegularExpression, implementation=RegularExpression(super=AbstractType())), name=regexp), name=null, fusionName=null, typeConfig=[[pattern: null], [replacement: null]], typeImpl=RegularExpression(super=AbstractType())))]))]), FieldMapping(searchServersName=ende52, fusionName=null, operations=[DropOperation(super=Operation(targets=[Response(super=Target(type=null, name=null, fusionName=null, typeConfig=null, typeImpl=null))]))]), FieldMapping(searchServersName=null, fusionName=ende52, operations=[DropOperation(super=Operation(targets=[Query(super=Target(type=null, name=null, fusionName=null, typeConfig=null, typeImpl=null))]))]), FieldMapping(searchServersName=ende52, fusionName=ende, operations=[DropOperation(super=Operation(targets=[Response(super=Target(type=null, name=null, fusionName=null, typeConfig=null, typeImpl=null)), Query(super=Target(type=null, name=null, fusionName=null, typeConfig=null, typeImpl=null))]))]), FieldMapping(searchServersName=ende51, fusionName=ende, operations=[DropOperation(super=Operation(targets=[Query(super=Target(type=null, name=null, fusionName=null, typeConfig=null, typeImpl=null))])), AddOperation(super=Operation(targets=[Response(super=Target(type=ScriptType(super=ConfiguredFactory(classFactory=org.outermedia.solrfusion.types.BshFile, implementation=BshFile(super=AbstractType())), name=beanshell-file), name=null, fusionName=null, typeConfig=[[file: null]], typeImpl=BshFile(super=AbstractType())))]))]), FieldMapping(searchServersName=w,h,d, fusionName=measure, operations=[ChangeOperation(super=Operation(targets=[Query(super=Target(type=ScriptType(super=ConfiguredFactory(classFactory=org.outermedia.solrfusion.types.DummyType, implementation=DummyType()), name=extract-width), name=w, fusionName=null, typeConfig=null, typeImpl=null)), Query(super=Target(type=ScriptType(super=ConfiguredFactory(classFactory=org.outermedia.solrfusion.types.DummyType, implementation=DummyType()), name=extract-height), name=h, fusionName=null, typeConfig=null, typeImpl=null)), Query(super=Target(type=ScriptType(super=ConfiguredFactory(classFactory=org.outermedia.solrfusion.types.DummyType, implementation=DummyType()), name=extract-depth), name=d, fusionName=null, typeConfig=null, typeImpl=null)), Response(super=Target(type=ScriptType(super=ConfiguredFactory(classFactory=org.outermedia.solrfusion.types.DummyType, implementation=DummyType()), name=merge-measure-units), name=null, fusionName=null, typeConfig=[[width-field: null], [height-field: null], [depth-field: null]], typeImpl=DummyType()))]))]), FieldMapping(searchServersName=measure, fusionName=w,h,d, operations=[ChangeOperation(super=Operation(targets=[Response(super=Target(type=ScriptType(super=ConfiguredFactory(classFactory=org.outermedia.solrfusion.types.DummyType, implementation=DummyType()), name=extract-width), name=null, fusionName=w, typeConfig=null, typeImpl=null)), Response(super=Target(type=ScriptType(super=ConfiguredFactory(classFactory=org.outermedia.solrfusion.types.DummyType, implementation=DummyType()), name=extract-height), name=null, fusionName=h, typeConfig=null, typeImpl=null)), Response(super=Target(type=ScriptType(super=ConfiguredFactory(classFactory=org.outermedia.solrfusion.types.DummyType, implementation=DummyType()), name=extract-depth), name=null, fusionName=d, typeConfig=null, typeImpl=null)), Query(super=Target(type=ScriptType(super=ConfiguredFactory(classFactory=org.outermedia.solrfusion.types.DummyType, implementation=DummyType()), name=merge-measure-units), name=null, fusionName=null, typeConfig=[[width-field: null], [height-field: null], [depth-field: null]], typeImpl=DummyType()))]))])])]))";

		//		System.out.println("E " + addNewlines(expected));
		//		System.out.println("F " + addNewlines(config2Out));

		Assert.assertEquals("Found different configuration",
			addNewlines(expected), addNewlines(config2Out));
	}

	protected String addNewlines(String s)
	{
		return s.replace("[", "{\n\t");
	}

	@Test
	public void checkXpath() throws FileNotFoundException, JAXBException,
		SAXException, ParserConfigurationException, XPathExpressionException
	{
		// one xml file which contains servers too
		String config = "test-fusion-schema.xml";

		// with validation
		String schemaPath = "configuration.xsd";

		Configuration cfg = xmlUtil.unmarshal(Configuration.class, config,
			schemaPath);
		Assert.assertNotNull(
			"Expected configuration object, but could't read in the xml file "
				+ config, cfg);

		/*
		    <om:query type="beanshell">
		        <script><![CDATA[
		        	currentQuery = 
		        		currentQuery.replace("XXX",System.currentTimeMillis());
		        ]]></script>
		    </om:query>
		 */
		Target beanShellQuery = cfg.getSearchServerConfigs()
			.getSearchServerConfigs().get(0).getFieldMappings().get(5)
			.getOperations().get(0).getTargets().get(3);
		System.out.println("Config "
			+ beanShellQuery.getTypeConfig().toString());
		String xpathStr = "//:script";
		String r = xmlUtil.getValueOfXpath(xpathStr,
			beanShellQuery.getTypeConfig());
		System.out.println("R=" + r);
		// TODO check r and implement Bsh.passArguments()
	}
}
