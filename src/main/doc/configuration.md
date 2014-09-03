# User Guide

For SolrFusion 1.0.
Date: 2014-09-03

Outermedia GmbH 


# Software Requirements
* Java >= 1.7
* One JEE compliant application server >= 2.5. Tested with Tomcat 6.0.41.

# Installation
For tomcat simply copy the solrfusion-X.Y.war to `<tomcat install dir>/webapps/solrfusion.war` and start the tomcat with
./start.sh. so that the war is unpacked. Then Ctrl-C start.sh and stop tomcat with ./stop.sh in order to configure 
SolrFusion to you needs. If the servlet path isn't touched, point your browser finally to 
`http://<host>:8080/solrfusion/biblio/select/?q=*:*&wt=xml`. The request should return a proper Solr XML response when 
the configured Solr server(s) support edismax queries.

# Configuration Files

All SolrFusion specific configuration files are located in the folder `<tomcat install dir>/webapps/solrfusion/WEB-INF`.

The following configuration files exist:

* __WEB-INF/classes/fusion-schema-uni-leipzig.xml__  
    This file contains a working solrfusion schema description which is used by the [Universität Leipzig](http://TODO "TODO").  
    The syntax is described in the chapter [SolrFusion Schema Configuration](#solrfusion-schema-configuration)
* __WEB-INF/classes/log4j.properties__  
    The logging is controlled by log4j 1.2.16 and these settings. [Please see](http://logging.apache.org/log4j/1.2/ "http://logging.apache.org/log4j/1.2/").
      Details are described in chapter [Logging](#logging).
* __WEB-INF/classes/org/outermedia/solrfusion/response/templates/json.ftl__ and  
  __WEB-INF/classes/org/outermedia/solrfusion/response/templates/xml.ftl__  
    Both [freemarker 2.3.20](http://freemarker.org/docs/index.html "http://freemarker.org/docs/index.html") files control the response format - for json and xml.  
* __WEB-INF/web.xml__  
    As a servlet init parameter the solrfusion schema can be configured here. Please replace fusion-schema-uni-leipzig.xml 
    with your own version which should be located in WEB-INF/classes/. Details are described in chapter
    [SolrFusion Servlet Configuration](#solrfusion-servlet-configuration)
    
# SolrFusion Servlet Configuration
The servlets are configured in the file `<tomcat install dir>/webapps/solrfusion/WEB-INF/web.xml`.

The main servlet is named __SolrFusionServlet__ and offers three options:  

* __fusion-schema__ - The file name of the SolrFusion Schema XML File to use. E.g. fusion-schema-uni-leipzig.xml.
* __fusion-schema-xsd__ - The XML Schema file to validate __fusion-schema__.
* __applyLatin1Fix__ - With true or false it is possible to enable or disable this fix. SolrFusion was tested with
vufind 1.3 (slightly modified by Universität Leipzig) where it is necessary to fix the wrong encoding of diacritical
chars e.g German Umlauts (ä, ü, ö etc). It is perhaps necessary to set the URI encoding for tomcat in 
<tomcat install dir>/conf/server.xml:  


    <Server ...>  
        <Service ...>  
            <Connector ... URIEncoding="UTF-8"/>  
            ...

The second servlet is the ping servlet named __VuFindServlet__ which always returns HTTP Code 200 (OK) and a proper
Solr XML response. The only supported HTTP request parameter is `echoParams=all`. If provided all HTTP request 
parameters are returned in the XML response.

Both servlets are pre-configured to be used by Uni Leipzig's vufind.

# Logging
The file `<tomcat install dir>/webapps/solrfusion/WEB-INF/classes/log4j.properties` logs to `<tomcat install dir>/logs/log4j.log`.
Please note the `${tomcat.home}/logs/log4j.log` in __log4j.properties__ which expects that __tomcat.home__ is set
as a Java property. This is the reason why it is recommended to use ./start.sh and ./stop.sh instead of the standard
tomcat commands.

The messages of the __debug level__ print the received request, the solr requests and how many documents were
received from a solr server.

The __trace level__ allows to understand the whole processing, because the received Solr responses and the applied
mappings are logged too. Finally the whole SolrFusion response is logged.


# SolrFusion Schema Configuration

Please note that the XML Schema file __solrfusion/WEB-INF/classes/configuration.xsd__ is referenced in the example
 schema __fusion-schema-uni-leipzig.xml file__.
So that XML Schema enabled XML editors can assist you to modify the SolrFusion schema file which is highly recommended.
             
## SolrFusion Schema Fields
The XML element `<om:fusion-schema-fields>` is used to describe all available SolrFusion fields. This information
is e.g. used by the SolrFusion query parser and the validation of mapping rules. SolrFusion's response renderer uses 
especially the multi-value declarations to create proper document field values.
Example:  

    <om:fusion-schema-fields default-type="text">
        <om:field name="spelling"/>
        <om:field name="spellingShingle" multi-value="true"/>
        <om:field name="publicationDate" type="date" format="dd.MM.yyyy" />
    </om:fusion-schema-fields>
    
For `type` the following values are allowed: text, int, long, float, double, boolean, date. If the type attribute 
is omitted the value of `default-type` is used. The default value of the `multi-value` attribute is false. The
specification of the `format` date pattern attribute is only valid for the type "date". The date pattern syntax 
is described [here](http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html "JavaDoc: SimpleDateFormat.java").

## Script Types    
The mapping rules can use one of the following predefined "script types" to convert data if more than the default copying action
is needed. 

Script Types are used to convert data contained in a SolrFusion query to data needed by a Solr server. Also they are applied
 to Solr document fields to create SolrFusion values. Context information is provided in a ScriptEnv (Java) object, which 
 contains the following entries.
 
Incoming:  

* __fusionField__ - the SolrFusion field name (java.lang.String) being mapped
* __fusionValue__ - the value of __fusionField__ (java.util.List<java.lang.String>)
* __searchServerField__ - the Solr field name being mapped (java.lang.String)
* __searchServerValue__ - the value of __searchServerField__ (java.util.List<java.lang.String>)
* __fusionFieldDeclaration__ - the field declaration of __fusionField__ (org.outermedia.solrfusion.configuration.FusionField)
* __fusionSchema__ - the SolrFusion schema (org.outermedia.solrfusion.configuration.Configuration)
* __values__ - the values to convert (java.util.List<java.lang.String>)
* __conversion__ - the conversion direction (org.outermedia.solrfusion.types.ConversionDirection); either FUSION_TO_SEARCH
    (SolrFusion to Solr Server) or SEARCH_TO_FUSION (Solr Server to SolrFusion)
* __locale__ - a java.util.Locale necessary when e.g. dates are formatted
* __responseDocument__ - a org.outermedia.solrfusion.response.parser.Document which contains the value which shall be converted
* __termQueryPart__ - a org.outermedia.solrfusion.query.parser.TermQuery (part of a SolrFusion query) which contains a
    value to be converted
* __searchServerConfig__ - the Solr server specific configuration of the SolrFusion schema (org.outermedia.solrfusion.configuration.SearchServerConfig)
    for which a query is mapped or a response is mapped
* __facetWordCount__ - is a java.util.List<java.lang.Integer> which represent the number of word occurrences in facets;
    Note: Facets of a Solr response are internally combined into one SolrFusion Document in order to be able to apply
    the mapping rules.
* __docFieldTerm__ - a org.outermedia.solrfusion.mapper.Term which contains the values being mapped    
* __fusionRequest__ - the data of the current SolrFusion request (org.outermedia.solrfusion.FusionRequest) 
* __mapFacetValue__ - whether the values being mapped belong to a facet (java.lang.Boolean); if true a ScriptType
    has to adjust the __facetWordCount__ when either the order or number of the values beeing mapped is changed.
* __mapHighlightValue__ - signals whether a highlight value is mapped    

Outgoing:

* __returnValues__ - a single java.lang.String or a java.util.List<java.lang.String> to return the converted values; 
    the single String value is automatically converted into a List.
* __returnWordCounts__ -  if __mapFacetValue__ is true and the order or number of values is modified then the facet
    word counts need to be adjusted too; the default value is the original word count list
 
These entries are directly accessible via simple variable names in the scripting languages supported by Java (e.g. [Javascript](#javascript) and
[Bean Shell](#bean-shell)). In Java implementations the ScriptEnv methods have to be used to access these context variables.

The following chapters describe them in detail:

* [Javascript](#javascript)
* [Bean Shell](#bean-shell)
* [Regular Expressions](#regular-expressions)
* [Tables](#tables)
* [Simple Values](#simple-values)
* [Multi Value Merger](#multi-value-merger)
* [Id Filter](#id-filter)
* [Field Merger](#field-merger)
* [String Normalizer](#string-normalizer)

### Javascript
Convert values by the use of Javascript code.
Example declaration:  

    <om:script-type name="javascript-file" class="org.outermedia.solrfusion.types.JsFile" />
    <om:script-type name="javascript" class="org.outermedia.solrfusion.types.Js" />

It is possible to embed the Javascript code into the XML (*.Js) or an external file (*.JsFile). Example mapping:

    <om:field name="f3" fusion-name="today3">
        <om:change>
            <om:response type="javascript">
                <script><![CDATA[
                    importClass(java.text.SimpleDateFormat);
                    importClass(java.util.GregorianCalendar);
                    var now = new GregorianCalendar(2014, 6, 19);
                    var fmt = new SimpleDateFormat("yyyy-MM-dd");
                    // predefined variables: see ScriptEnv.ENV_*
                    print("Conversion         : "+conversion+"\n");
                    print("Values             : "+values+"\n");
                    print("Search Server Field: "+searchServerField+"\n");
                    print("Search Server Value: "+searchServerValue+"\n");
                    print("Fusion Field       : "+fusionField+"\n");
                    print("Fusion Value       : "+fusionValue+"\n");
                    print("Fusion Field       : "+fusionFieldDeclaration+"\n");
                    print("Fusion Schema      : "+fusionSchema+"\n");
                    print("Word Count         : "+facetWordCount+"\n");
                    returnValues = searchServerValue.get(0)+" at "+fmt.format(now.getTime().getTime());
                    returnWordCounts = ["4", "1", "3", "2"];
                ]]></script>
            </om:response>
         </om:change>
    </om:field>

External file example:

    <om:field name="f4" fusion-name="today4">
        <om:change>
            <om:response type="javascript-file">
                <file>target/test-classes/test-js-file.js</file>
            </om:response>
        </om:change>
    </om:field>
    
The file test-js-file.js contains the code of the CDATA above.    

### Bean Shell
Convert values by the use of Bean Shell code.
Example declaration:  

    <om:script-type name="beanshell-file" class="org.outermedia.solrfusion.types.BshFile" />
    <om:script-type name="beanshell" class="org.outermedia.solrfusion.types.Bsh" />
    
It is possible to embed the Beanshell code into the XML (*.Bsh) or an external file (*.BshFile). Example mapping:

    <om:field name="f1" fusion-name="today">
        <om:change>
            <om:response type="beanshell">
                <script><![CDATA[
                    import java.text.SimpleDateFormat;
                    import java.util.*;
                    GregorianCalendar now = new GregorianCalendar(2014, 6, 19);
                    SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
                    // predefined variables: see ScriptEnv.ENV_*
                    print("Conversion         : "+conversion);
                    print("Values             : "+values);
                    print("Search Server Field: "+searchServerField);
                    print("Search Server Value: "+searchServerValue);
                    print("Fusion Field       : "+fusionField);
                    print("Fusion Value       : "+fusionValue);
                    print("Fusion Field       : "+fusionFieldDeclaration);
                    print("Fusion Schema      : "+fusionSchema);
                    print("Word Count         : "+facetWordCount);
                    print("Request            : "+fusionRequest);
                    print("Map facet doc?     : "+mapFacetValue);
                    print("Map highlight doc? : "+mapHighlightValue);
                    returnValues = searchServerValue.get(0)+" at "+fmt.format(now.getTime().getTime());
                    returnWordCounts = new ArrayList();
                    returnWordCounts.add("4");
                    returnWordCounts.add("1");
                ]]></script>
            </om:response>
         </om:change>
    </om:field>

External file example:

    <om:field name="f2" fusion-name="today2">
        <om:change>
            <om:response type="beanshell-file">
                <file>target/test-classes/test-bsh-file.bsh</file>
            </om:response>
        </om:change>
    </om:field>

The file test-bsh-file.bsh contains the code of the CDATA above.    

### Regular Expressions
Convert values by the use of regular expressions. The supported format is described [here](http://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html "Java Regular Expressions").
Example declaration:  

    <om:script-type name="regexp" class="org.outermedia.solrfusion.types.RegularExpression"/>

Example usage: Swap first name and last name

    <om:field name="f5" fusion-name="text1">
        <om:change>
            <om:response type="regexp">
                <pattern>([^,]+),\s*(.+)</pattern>
                <replacement>$2 $1</replacement>
            </om:response>
        </om:change>
    </om:field>

### Tables
Map values by the use of a one-to-one relation.
Example declaration:  

    <om:script-type name="static-table-file" class="org.outermedia.solrfusion.types.TableFile"/>
    <om:script-type name="static-table" class="org.outermedia.solrfusion.types.Table"/>

It is possible to embed the table data into the XML (*.Table) or an external file (*.TableFile). Example mapping:

    <om:field name="f6" fusion-name="text2">
        <om:change>
            <om:query-response type="static-table">
                <entry>
                    <value>u1</value>
                    <fusion-value>user1</fusion-value>
                </entry>
                <entry>
                    <value>u2</value>
                    <fusion-value>user2</fusion-value>
                </entry>
            </om:query-response>
        </om:change>
    </om:field>
    
The declaration above connects "u1" to "user1" and "u2" to "user2". The definition needs to be bijective. 
External file example:

    <om:field name="f7" fusion-name="text3">
        <om:change>
            <om:query-response type="static-table-file">
                <file>target/test-classes/test-table-file.xml</file>
            </om:query-response>
        </om:change>
    </om:field>
    
The contents of test-table-file.xml is:

    <?xml version="1.0" encoding="UTF-8"?>
    <mapping>
        <entry>
            <value>u1</value>
            <fusion-value>user1</fusion-value>
        </entry>
        <entry>
            <value>u2</value>
            <fusion-value>user2</fusion-value>
        </entry>
    </mapping>

### Simple Values

### Multi Value Merger

### Id Filter

### Field Merger

### String Normalizer
        
## Default Search Values        
    TODO
            
## SolrFusion Id Generator                
    TODO
    
## Response Consolidator
    TODO
    
## Query Mapper
    TODO
    
## Controller
    TODO
    
## Solr Connection Values
    TODO

## Default Response Settings
    TODO
    
## Default Query Settings    
    TODO
    
## Document Merging  
    TODO
  
## Solr Server Core Settings
    TODO

## Mappings
    TODO
    
TODO