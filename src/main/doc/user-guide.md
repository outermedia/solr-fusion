# User Guide

For SolrFusion 1.0.
Date: 2014-09-03

Outermedia GmbH 

# Overview

    TODO
    
Tested with Solr 1.4, 3.5, 3.6 and 4.3 servers.
 
    TODO

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

A general principle of the SolrFusion schema configuration is, that the implementing Java classes are not hard coded,
but are mostly configurable too. 
             
## SolrFusion Schema Fields
The mandatory XML element `<om:fusion-schema-fields>` is used to describe all available SolrFusion fields. This information
is e.g. used by the SolrFusion query parser and the validation of mapping rules. SolrFusion's response renderer uses 
especially the multi-value declarations to create proper document field values.

Declaration (mandatory, fields are optional):  

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

Only ScriptTypes used in mappings have to be declared.

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
                <file>test-js-file.js</file>
            </om:response>
        </om:change>
    </om:field>
    
The file test-js-file.js contains the code of the CDATA above.   
Note: The file is not classpath based loaded. This means, when relative paths are used, it depends on your applications 
server (and installation) what the current working directory is.

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
                <file>test-bsh-file.bsh</file>
            </om:response>
        </om:change>
    </om:field>

The file test-bsh-file.bsh contains the code of the CDATA above.    
Note: The file is not classpath based loaded. This means, when relative paths are used, it depends on your applications 
server (and installation) what the current working directory is.

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
                <file>test-table-file.xml</file>
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

Note: The file is not classpath based loaded. This means, when relative paths are used, it depends on your applications 
server (and installation) what the current working directory is.

### Simple Values
Set (overwrite) values. It is possible to return one ore more values.
Example declaration:  

    <om:script-type name="static-value" class="org.outermedia.solrfusion.types.Value"/>

Example mapping:

    <om:field fusion-name="text13">
        <om:add>
            <om:response type="static-value">
                <value>1</value>
                <value>2</value>
                <value>3</value>
            </om:response>
        </om:add>
    </om:field>


### Multi Value Merger
Flatten multiple values of one field to one value which is necessary when the destination field is a single value.
Example declaration:

    <om:script-type name="merge-multi-value" class="org.outermedia.solrfusion.types.MultiValueMerger"/>

Example mapping:

            <om:field name="s7" fusion-name="text7">
                <om:change>
                    <om:response type="merge-multi-value">
                        <range>2</range>
                        <separator>,</separator>
                    </om:response>
                </om:change>
            </om:field>
            
This ScriptType supports two options:  

* `<range>` - possible values are __all__ or a number. The number limits the number of entries to merge from the first to the 
    specified number (inclusive). Starting with 0.
* `<separator>` - This string is inserted between two values.

            <om:field name="s8" fusion-name="text8">
                <om:change>
                    <om:response type="merge-multi-value">
                        <range>all</range>
                        <separator><![CDATA[ || ]]></separator>
                    </om:response>
                </om:change>
            </om:field>

### Id Filter
SolrFusion creates own ids in order to be able to send queries based on a document's id to the right server (without
trying all configured Solr servers).
Example declaration:

    <om:script-type name="filter-id" class="org.outermedia.solrfusion.types.IdFilter"/>

Example mapping:

    <om:field name="id" fusion-name="id">
        <om:change>
            <om:query type="filter-id" />
        </om:change>
    </om:field>
    
Please note that SolrFusion modifies the ids of received Solr documents automatically and only for queries (targeting a 
specific Solr server) it is necessary to add a mapping like above.

### Field Merger
Merge several Solr fields and their values into one fusion field. Depending on the fusion field (single vs. multi value) 
either one or several values are created. So this ScriptType is only applicable to Solr responses.
Example declaration:

    <om:script-type name="field-merger" class="org.outermedia.solrfusion.types.FieldMerger"/>

Example mapping:

    <om:field fusion-name="author_facet">
        <om:add>
            <om:response type="field-merger">
                <separator>;</separator>
                <field>author</field>
                <field>author2</field>
            </om:response>
        </om:add>
        <om:drop><om:query /></om:drop>
    </om:field>
    
In a first step the values of the Solr fields (`<field>`) "author" and "author2" are concatenated. If the SolrFusion field ("author_facet"
in the example above) is a single value field, the list is flattened and two values are separated by the string 
specified by `<separator>`.

### String Normalizer
Especially for SolrFusion's sorting it is necessary to use comparable strings. Therefor this ScriptType allows to build such strings.
Example declaration:

    <om:script-type name="normalizer" class="org.outermedia.solrfusion.types.Normalizer"/>
        
Example mapping:
        
    <om:field name="title_sort" fusion-name="title_sort">
        <om:change>
            <om:response type="normalizer">
                <trim>true</trim>
                <to-lower-case>true</to-lower-case>
                <start-chars-to-del>&#32;&#160;."-</start-chars-to-del>
            </om:response>
        </om:change>
        <om:change><om:query /></om:change>
    </om:field>        
    
The "normalizer" supports three actions:
    
* `<trim>` - remove leading and trailing white spaces
* `<to-lower-case>` - true or false in order to enable or disable conversion to lower case
* `<start-chars-to-del>` - remove leading characters which are contained in the string of this attribute.

## Default Search Values      
Basic search settings are the default search field, the default sort field and the default (edismax) operator. Because 
of these settings it is not necessary to adjust them in the used Solr servers.

Declaration (mandatory):

        <om:default-search-field>allfields</om:default-search-field>    
        <om:default-sort-field>score desc</om:default-sort-field>    
        <om:default-operator>AND</om:default-operator>
        
The values above are only used when no specific value is present.         
            
## SolrFusion Id Generator     
This class offers all necessary methods to convert Solr document ids to SolrFusion ids and vice versa.    

Declaration (mandatory):  

    <om:id-generator fusion-name="id"
        class="org.outermedia.solrfusion.DefaultIdGenerator$Factory" />      

Examples of created SolrFusion document ids:  

* UBL_0002240553 - a document received from the Solr server named "UBL" with id "0002240553" (see [Solr Server Core Settings](#solr-server-core-settings)).
* UBL_0000220639-UBL4_0000230639 - a merged document received from the servers "UBL" and "UBL4" (see [Document Merging](#document-merging)).

## Response Consolidator
The Solr responses of the Solr servers to use are combined respecting document merging, facets and highlights. Also
the documents are sorted and the documents to return are selected.

Declaration (mandatory):

    <om:response-consolidator class="org.outermedia.solrfusion.response.PagingResponseConsolidator$Factory"/>
    
## Mapper
A "mapper" applies mapping rules (see [Field Mappings](#field-mappings)) to fields contained in a query or a Solr document.

Declaration (mandatory):

        <om:response-mapper class="org.outermedia.solrfusion.mapper.ResponseMapper$Factory" />
        <om:query-mapper class="org.outermedia.solrfusion.mapper.QueryMapper$Factory" />
    
The "response-mapper" is suitable to convert Solr documents and the "query-mapper" to convert SolrFusion queries.    
    
## Controller
The whole processing - handling a SolrFusion query until sending back a Solr response - is controlled by this class.

Declaration (mandatory):

    <om:controller class="org.outermedia.solrfusion.FusionController$Factory"/>

## Solr Connection Values
Global connection values are the  

* `<om:timeout>` - The socket/request/response timeout in milliseconds.
* `<om:disaster-limit>` - The minimum number of Solr servers which have to return a valid Solr response (0 is possible too)
    and not an HTTP error (HTTP code is not 200).
    If too few servers respond, the error message specified by `<om:error>` is used to return an error.
* `<om:error>` - The error message used in when the disaster limit is violated. The key attribute is currently not used.
* `<om:page-size>` - If the current SolrFusion request contains no page size parameter, the value of `<om:page-size>`
    is used.

Declaration (mandatory):

    <om:solr-servers>
        <om:timeout>4000</om:timeout>
        <om:disaster-limit>0</om:disaster-limit>
        <om:error key="disaster-limit">Please try later again.</om:error>
        <om:page-size>10</om:page-size>
        ...

## Query Response Parser
SolrFusion uses different parsers for dismax and edismax queries. For Solr XML responses a separate parser exists.

Declaration (mandatory, child of `<om:solr-servers>`):

    <om:query-parser
            class="org.outermedia.solrfusion.query.EdisMaxQueryParser$Factory"/>
    <om:dismax-query-parser
        class="org.outermedia.solrfusion.query.DisMaxQueryParser$Factory"/>
    <om:response-parser
            class="org.outermedia.solrfusion.response.DefaultResponseParser$Factory"/>

SolrFusions evaluates the "qt" HTTP request parameter and uses the dismax query parser when qt contains "dismax". The
 dismax query parser is also used when a Solr subquery contains "{!dismax ..,}".  
SolrFusion always requests XML responses from the configured Solr servers, but is capable to render the requested 
response format of the combined Solr documents.

The line above declares the global response renderer, which can be overwritten for certain Solr servers.

## Response Renderer
SolrFusion supports rendering of Solr documents in xml and json format. The response is always indented.

Declaration (mandatory, child of `<om:solr-servers>`):

    <om:response-renderer type="xml"
        class="org.outermedia.solrfusion.response.DefaultXmlResponseRenderer$Factory" />
    <om:response-renderer type="json"
        class="org.outermedia.solrfusion.response.DefaultJsonResponseRenderer$Factory" />

The json response renderer always renders facets in json.nl=arrarr format. But the format of json and xml is described
by a freemarker template which can be easily adjusted (see json.ftl and xml.ftl in chapter [Configuration Files](#configuration-files)).

Note: The php renderer is not implemented in version 1.0.    
    
## Query Builder    
Corresponding to the query parsers SolrFusion is capable to render dismax or edismax queries from mapped SolrFusion queries.

Declaration (mandatory, child of `<om:solr-servers>`):

    <om:query-builder class="org.outermedia.solrfusion.mapper.QueryBuilder$Factory"/>
    <om:dismax-query-builder class="org.outermedia.solrfusion.mapper.DisMaxQueryBuilder$Factory"/>
    
The lines above declare the global query builders, which can be overwritten for certain Solr servers.    
    
## Document Merging  
In order to avoid duplicate Solr documents - same document from different(!) Solr servers - SolrFusion can merge documents
based on SolrFusion field ("ISBN" in the example below).  
Note: Document merging is done after mapping.

Declaration (optional, child of `<om:solr-servers>`):

        <om:merge fusion-name="ISBN" class="org.outermedia.solrfusion.DefaultMergeStrategy$Factory">
            <om:target prio="1" target-name="Bibliothek A"/>
            <om:target prio="2" target-name="Bibliothek B"/>
        </om:merge>   
  
The `prio` attribute describes the relevance of the Solr server referenced by `target-name`. Valid values of
`target-name` are the "name" attribute values of the configured Solr servers.

According to the priority the document of the first server is used as the main document. The document fields of lower
prioritized servers are used to fill unset fields.

## Solr Server Settings
This section contains the description of one Solr server to use.

Declaration (optional, but at least one instance; child of `<om:solr-servers>`):

    <om:solr-server name="DBoD" version="3.5" query-param-name="q.alt" enabled="true"
                            class="org.outermedia.solrfusion.adapter.solr.DefaultSolrAdapter$Factory">

* __name__ - The value of the required __name__ attribute needs to be unique within all Solr server declarations and must not contain 
underscore or minus. 
* __version__ - The value of the required __version__ attribute is evaluated by the Solr1 adapter in order to generate
valid facet sort values.
* __query-param-name__ - this optional attribute allows to overwrite the HTTP request parameter which contains the Solr query (default: q). If the
value is set to a different value than "q", q will be set to a "dismax" version of the original edismax query. Note: 
Especially for a Solr 1.X server we used for testing, it was necessary to pass edismax queries in the q.alt 
parameter and the "same" query in dismax format in order to receive highlights.
* __enabled__ - To simplify testing with a subset of configured Solr servers the optional __enabled__ attribute can be set to false 
    (default: true). Note: Nesting of XML comments is not possible.

It is also possible to use an "XML include" statement to keep the complete Solr server settings in a separate file. Example:

    <om:solr-servers>
        ...
        <xi:include href="test-server-a-mapping.xml" xmlns:xi="http://www.w3.org/2001/XInclude" />

The file test-server-a-mapping.xml starts with (example):

    <?xml version="1.0" encoding="UTF-8"?>
    <om:solr-server name="UBL" version="3.5"
                    class="org.outermedia.solrfusion.adapter.solr.DefaultSolrAdapter$Factory"
                    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                    xmlns:om="http://solrfusion.outermedia.org/configuration/"
                    xmlns="http://solrfusion.outermedia.org/configuration/type/">
        ...

### Solr Server URL
SolrFusion needs the whole URL up to request parameters.

Declaration (mandatory, child of `<om:solr-servers>`):

    <om:url>http://host:port/solr/xyz</om:url>

### Score Corrector
When documents of several Solrs are combined into one result their document scores are in general different. So that
sorting by score results in "grouped" documents (first block from one server, second from another etc.). SolrFusion
offers no solution for this problem, but offers a simple workaround which hopefully helps sometimes: The default 
implementation simply multiplies all document's scores with the specified __factor__ attribute. 

Declaration (mandatory, child of `<om:solr-servers>`):

    <om:score factor="2.9744758135"
                          class="org.outermedia.solrfusion.DefaultScore$Factory"/>

### Solr Server Specific Response Parser
In order to use a Solr server specific response parser it is possible to hide the default parser with a special
implementation.

Declaration (optional, child of `<om:solr-servers>`):

    <om:response-parser
                    class="org.outermedia.solrfusion.SpecialResponseParser$Factory" />
                    
The global response parser is described in chapter [Query Response Parser](#query-response-parser).                    

### Solr Server Specific Edismax Query Builder
Declare a special edismax query builder, in the case that a certain Solr server needs a special query builder. If 
absent, the global edismax query builder will be used (see chapter [Query Builder](#query-builder)).

Declaration (optional, child of `<om:solr-servers>`):

    <om:query-builder class="org.outermedia.solrfusion.mapper.QueryBuilder$Factory" />

### Solr Server Document Id Field Name
The id generator uses this declaration to identify id fields in Solr documents.

Declaration (mandatory, child of `<om:solr-servers>`):

    <om:unique-key>id</om:unique-key>

### Maximum Number Of Document To Return
Because sorting and paging is necessarily implemented in SolrFusion, it is necessary to limit the maximum number of
documents to receive from one server for one query.

Declaration (mandatory, child of `<om:solr-servers>`):

    <om:max-docs>100</om:max-docs>

## Field Mappings
Finally a configuration of a Solr server contains the mapping rules which convert fields of queries and Solr documents.
The following chapters show use-cases of typical conversions.

The general format of mapping rules is:

    <om:field name="<Solr document field name>" fusion-name="<SolrFusion field name>" />

The matching of __name__ and __fusion-name__ supports

* literals e.g. "city"
* wildcards e.g name="*author*" and fusion-name="*Person*". Please note, that the number of wildcards ("*") has to be
equal in name and fusion-name. With e.g. name="first_author1" fusion-name="first_Person1" is produced and vice versa.
Note: The current implementation rewrites wildcard field names to regular expressions.
* regular expressions, e.g.: 


    <om:field
        name-pattern="val([0-9]+)Start" fusion-name-replacement="valueFrom$1"
        name-replacement="val$1Start" fusion-name-pattern="valueFrom([0-9]+)"
    />

To apply a mapping to convert fields of Solr documents to SolrFusion fields a pair of __name-pattern__ and
__fusion-name-replacement__ is necessary. For the opposite direction a pair of __fusion-name-pattern__ and
__name-replacement__ is needed.

It is allowed to write several mapping rules which address the same fields. And it is also possible to write rules which 
either contain __name__ or __fusion-name__.

Field mapping rules can execute a combination of three basic operations - [Change](#Change), [Add](#Add) and [Drop](#Drop)
which are applicable to SolrFusion queries and Solr documents. Mapping rule examples can be found in two provided
SolrFusion schema files: The working (real) example __WEB-INF/classes/fusion-schema-uni-leipzig.xml__ and the 
file __WEB-INF/classes/fusion-schema.xml__ (for documentation purpose).

### Change

The most basic mapping is copying:

    <om:field name="town" fusion-name="city" />

This rule copies values and is used when

* a SolrFusion query is mapped to a Solr query and the SolrFusion query contains "city" fields (__fusion-name__). Then
    the "city" field is replaced with a "town" field and the values are copied.
* a Solr document is mapped to a SolrFusion document and the document contains "town" fields (__name__). Then the "town"
    field is replaced with a "city" field and the values are copied.

The following rules are equivalent to the rule above:

    <!-- Example 1: Not recommended to use, because barely tested. -->
    <om:field name="town" fusion-name="city">
        <om:change><om:query-response /></om:change>
    </om:field>
    
    <!-- Example 2 -->
    <om:field name="town" fusion-name="city">
        <om:change><om:response /></om:change>
        <om:change><om:query /></om:change>
    </om:field>
    
    <!-- Example 3: Separate rules for query and response -->
    <om:field name="town" fusion-name="city">
        <om:change><om:response /></om:change>
    </om:field>
    <om:field name="town" fusion-name="city">
        <om:change><om:query /></om:change>
    </om:field>

As you might guess, it is possible to perform different change conversions for fields of queries (`<om:queries>`) and fields of 
Solr documents (`<om:response>`).

    <om:field name="title_sort" fusion-name="title_sort">
        <om:change>
            <om:response type="normalizer">
                <trim>true</trim>
                <to-lower-case>true</to-lower-case>
                <start-chars-to-del>&#32;&#160;."-</start-chars-to-del>
            </om:response>
        </om:change>
        <om:change><om:query /></om:change>
    </om:field>

Also more complex conversions than copying are supported by the use of ScriptTypes. Their name can be specified in the
__type__ attribute.
In the example rule above the ScriptType [normalizer](#string-normalizer) is applied to Solr documents and copies the value
unchanged in queries. As long as no __type__ is present, values are copied. Otherwise a special conversion is
processed. All pre-defined ScriptTypes are documented in chapter [Script Types](#script-types).

It is also possible to specify several `<om:change>`, `<om:response>` and `<om:query>` in order to execute several conversions
in sequence. A following conversion works on the values of the previous conversion (initially on a copy of the 
original values). Note: Same applies to `<om:drop>` and `<om:add>` which are described below.
   
    <om:field name="n1" fusion-name="n1">
        <om:change>
            <om:response type="add1" />
            <om:response type="add3" />
        </om:change>
    </om:field>
    
For the example above 1 would be converted to 1+1+3 = 5. 

### Drop

### Add
    
TODO