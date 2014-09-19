# User Guide

For SolrFusion 1.0.
Date: 2014-09-03

Outermedia GmbH 

# Table Of Contents

* [Overview](#overview)
* [Licence](#licence)
* [Software Requirements](#software-requirements)
* [Installation](#installation)
    * [Error Handling](#error-handling)
* [Supported Solr Features](#supported-solr-features)
    * [Highlights](#highlights)
    * [Facets](#facets)
    * [More Like This](#more-like-this)
* [Configuration Files](#configuration-files)
* [SolrFusion Servlet Configuration](#solrfusion-servlet-configuration)
* [Logging](#logging)
* [SolrFusion Schema Configuration](#solrfusion-schema-configuration)
    * [SolrFusion Schema Fields](#solrfusion-schema-fields)
    * [Script Types](#script-types)
        * [Javascript](#javascript)
        * [Bean Shell](#bean-shell)
        * [Regular Expressions](#regular-expressions)
        * [Tables](#tables)
        * [Simple Values](#simple-values)
        * [Multi Value Merger](#multi-value-merger)
        * [Id Filter](#id-filter)
        * [Field Merger](#field-merger)
        * [String Normalizer](#string-normalizer)
    * [Default Search Values](#default-search-values)
    * [SolrFusion Id Generator](#solrfusion-id-generator)
    * [Response Consolidator](#response-consolidator)
        * [Paging And Sorting](#paging-and-sorting)
    * [Mapper](#mapper)
    * [Controller](#controller)
    * [Solr Connection Values](#solr-connection-values)
    * [Query Response Parser](#query-response-parser)
    * [Response Renderer](#response-renderer)
    * [Query Builder](#query-builder)
    * [Document Merging](#document-merging)
    * [Solr Server Settings](#solr-server-settings)
        * [Solr Server URL](#solr-server-url)
        * [Score Corrector](#score-corrector)
        * [Solr Server Specific Response Parser](#solr-server-specific-response-parser)
        * [Solr Server Specific Edismax Query Builder](#solr-server-specific-edismax-query-builder)
        * [Solr Server Document Id Field Name](#solr-server-document-id-field-name)
        * [Maximum Number Of Documents To Return](#maximum-number-of-documents-to-return)
    * [Field Mappings](#field-mappings)
        * [Change](#change)
        * [Drop](#drop)
        * [Add](#add)
        * [Split Merge Use Case](#split-merge-use-case)
* [Known Issues](#known-issues)      
    * [Sort A Split Field](#sort-a-split-field)
    * [Map Values With Wildcards](#map-values-with-wildcards)
    * [Fuzzy Slop](#fuzzy-slop)
    * [Removed Fields In Filter Queries](#removed-fields-in-filter-queries)
    * [Morelikethis](#morelikethis)
    * [Merge Of Two SolrFusion Fields Into One Solr Field In Queries](#merge-of-two-solrfusion-fields-into-one-solr-field-in-queries)
    * [Boosting When Two SolrFusion Fields Are Mapped To One Solr Field](#boosting-when-two-solrfusion-fields-are-mapped-to-one-solr-field)
    * [New Static Query Parts](#new-static-query-parts)
    * [One SolrFusion Field Is Mapped To Several Solr Fields In Dismax Queries](#one-solrfusion-field-is-mapped-to-several-solr-fields-in-dismax-queries)
    * [Total Document Count Correction And Document Merging](#total-document-count-correction-and-document-merging)
  

# Overview

SolrFusion is intended to be used in the case that several Solr servers have to be combined into one logical Solr
server. In order to be able to create a unified logical Solr schema several basic operations are provided to map and 
normalize data of the used Solr servers.

The first release (version 1.0) is limited to a subset of supported Solr HTTP request parameters and features. The primary focus
of this version is to work with the customized vufind 1.3 used by the [Universitätsbibliothek Leipzig](http://www.ub.uni-leipzig.de/ "Universitätsbibliothek Leipzig").

But the concept and implementation allows a high level of customization, so it is conceivably to use SolrFusion to
combine e.g. data bases too.
    
Version 1.0 was tested with vufind 1.3 and  Solr 1.4, 3.5, 3.6, 4.3 servers.

A basic test with vufind 2.3 succeeded too.


# Licence

This software is licensed under the terms of the GPL V3.

# Software Requirements
* Java >= 1.7
* One JEE compliant application server >= 2.5. Tested with Tomcat 6.0.41.

# Installation
For tomcat simply copy the solrfusion-X.Y.war to `<tomcat install dir>/webapps/solrfusion.war` and start the tomcat with
./start.sh. so that the war is unpacked. Then Ctrl-C start.sh and stop tomcat with ./stop.sh in order to configure 
SolrFusion to you needs. If the servlet path isn't touched, point your browser finally to 
`http://<host>:8080/solrfusion/biblio/select/?q=*:*&wt=xml`. The request should return a proper Solr XML response when 
the configured Solr server(s) support edismax queries.

## Error Handling
The HTTP status is always set to value different to 200 (OK). If SolrFusion was able to create a response, then the
response is returned in the wanted format (Json or XML) too. An error message is added too. Example:

    <response>
        <lst name="responseHeader">
            <int name="status">400</int>
            <int name="QTime">2</int>
            <lst name="params">
                <str name="q">flubb:*</str>
                <str name="fq">(collection:GVK OR collection:DOAJ OR (collection_details:ZDB-1-PIO))</str>
            </lst>
        </lst>
        <lst name="error">
            <str name="msg">undefined field flubb</str>
            <int name="code">400</int>
        </lst>
    </response>

The error message's origin is either SolrFusion or an error message received from a Solr server. In the "disaster" case
 (too few Solr servers responded) the configured error message is used and augmented with the error messages of the
 Solr servers.

# Supported Solr Features
SolrFusion supports the following (e)dismax query types:

* Boolean Query - also "+", "-" and "!"
* Fuzzy Query
* Match All Docs Query (*:*)
* Numeric Range Query (long, int, float, double, date) - with open ("*") start or end
* Phrase Query
* Prefix Query
* Wildcard Query
* SubQuery - in edismax _query_:"..."
* {!dismax ...} - to pass dismax queries
* {!... qf="..." ...} - to pass boosts values; all other parameters are not mapped and are handed over to a Solr server unmodified.

The supported Solr HTTP request parameters are:

* q - query; in dismax or edismax format (depends on qt and {!...})
* wt - response type (only json or xml)
* fq - (multiple) filter query
* rows - paging number of documents
* start - paging offset
* sort - only `<field> asc|desc`
* fl - fields to return (list - separated by SPACE or COMMA, default: *)
* hl - enable/disable highlighting
* hl.simple.pre - highlighting prefix
* hl.simple.post - highlighting postfix
* hl.fl - fields to highlight (list - separated by SPACE or COMMA)
* hl.q - highlight query
* facet - enable/disable facets
* facet.mincount - the minimum expected occurrences of returned facets
* facet.limit - maximum number of facet values
* f.([^.]+).facet.sort - facet field based sorting; either "index" or "count"
* facet.sort - global facet sorting; either "index" or "count"
* facet.prefix - the facet prefix
* facet.field - (multiple) facet fields; meta information e.g. {!ex=format_filter} is preserved
* qt - query type; supported values are "dismax" and "morelikethis"
* qf - boost values
* mm - minimum match

If not mentioned all fields occur at most once in a HTTP request.

Facet and highlight values in Solr responses are processed too.

## Highlights
Because highlights are document related only the highlights of the finally returned documents are processed. In order to 
be able to apply the mapping rules, SolrFusion creates Solr documents from the highlights. 

## Facets
Because mapping rules work only on Solr documents, facets are transformed internally into Solr documents where the fields
are annotated with the word counts. But only the "facet_fields" of a response are processed, but neither "facet_queries"
nor "facet_dates" nor "facet_ranges".

Sorting of the facet values is done by SolrFusion, because facets of different Solr servers are combined. The order of
the fields is determined by the textual order of the server declarations. New fields are append at the end of the facet
list. "index" and "count" sorting is supported (description from Solr Wiki):

* __count__ - Sort the constraints by count (highest count first)
* __index__ - For terms in the ascii range, this will be alphabetically sorted.

The default is __count__ if __facet.limit__ is greater than 0, __index__ otherwise. 

The limit __facet.limit__ is evaluated and applied to the sorted list. The implementation follows the description in the
 Solr Wiki: This param indicates the maximum number of constraint counts that should be returned for the facet fields. 
 A negative value means unlimited. The default value is 100.

## More Like This
If enabled by qt=morelikethis the data in a Solr response is recognized and handled similar to the search results.
E.g. document merging is supported too. But only one document is expected in the response XML element "match".

In queries no extra logic was implemented.

# Configuration Files

All SolrFusion specific configuration files are located in the folder `<tomcat install dir>/webapps/solrfusion/WEB-INF`.

The following configuration files exist:

* __WEB-INF/classes/fusion-schema-uni-leipzig.xml__  
    This file contains a working solrfusion schema description which is used by the [Universitätsbibliothek Leipzig](http://www.ub.uni-leipzig.de/ "Universitätsbibliothek Leipzig").  
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
    
The configured SolrFusion schema is loaded earliest every five minutes after the last modification. But it is
possible to add the HTTP request parameter `forceSchemaReload=<any value>` to force an immediate reload of the
schema.

log4j.properties is reloaded every five minutes after modifications.

The Freemarker templates are reloaded every time before they are applied.
    
    
# SolrFusion Servlet Configuration
The servlets are configured in the file `<tomcat install dir>/webapps/solrfusion/WEB-INF/web.xml`.

The main servlet is named __SolrFusionServlet__ and offers three options:  

* __fusion-schema__ - The file name of the SolrFusion Schema XML File to use. E.g. fusion-schema-uni-leipzig.xml.
* __fusion-schema.xsd__ - The XML Schema file to validate __fusion-schema__.
* __applyLatin1Fix__ - With true or false it is possible to enable or disable this fix. SolrFusion was tested with
vufind 1.3 (slightly modified by [Universitätsbibliothek Leipzig](http://www.ub.uni-leipzig.de/ "Universitätsbibliothek Leipzig")) 
where it is necessary to fix the wrong encoding of diacritical chars e.g German Umlauts (ä, ü, ö etc). Note: We had to
disable the fix during a test with vufind2.3.

It is perhaps necessary to set the URI encoding for tomcat in 
`<tomcat install dir>/conf/server.xml`:  

`    <Server ...>  
        <Service ...>  
            <Connector ... URIEncoding="UTF-8"/>`

The second servlet is the ping servlet named __VuFindServlet__ which always returns HTTP Code 200 (OK) and a proper
Solr XML response. The only supported HTTP request parameter is `echoParams=all`. If provided all HTTP request 
parameters are returned in the XML response.

Both servlets are pre-configured to be used by Uni Leipzig's vufind.

# Logging
The file `<tomcat install dir>/webapps/solrfusion/WEB-INF/classes/log4j.properties` logs to `<tomcat install dir>/logs/log4j.log`.
Please note the `${tomcat.home}/logs/log4j.log` in __log4j.properties__ which expects that __tomcat.home__ is set
as a Java property. This is the reason why it is recommended to use __./start.sh__ and __./stop.sh__ instead of the standard
tomcat commands.

The messages of the __debug level__ print the received request, the solr requests and how many documents were
received from a solr server.

The __trace level__ allows to understand the whole processing, because the received Solr responses and the applied
mappings are logged too. Finally the whole SolrFusion response is logged.

# Architecture
The following sketch contains a roughly description of the internal architecture.

![SolrFusion Architecture](https://raw.githubusercontent.com/outermedia/solr-fusion/master/src/main/doc/architecture.jpg "SolrFusion Architecture")

In the following chapters you will find specific Java classes of the abstract interfaces mentioned in the sketch.

# SolrFusion Schema Configuration
Please note that the XML Schema file __solrfusion/WEB-INF/classes/configuration.xsd__ is referenced in the example
 schema __fusion-schema-uni-leipzig.xml file__.
So that XML Schema enabled XML editors can assist you to modify the SolrFusion schema file which is highly recommended.

A general principle of the SolrFusion schema configuration is, that the implementing Java classes are not hard coded,
but are mostly configurable too. 

The following chapters follow the order of the XML elements in the SolrFusion schema file.
             
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
The [mapping rules](#field-mappings), which are used to convert values and fields from SolrFusion to a Solr server and vice versa, can use 
one of the following predefined "script types" to convert data if more than the default copying action
is needed. 

Script Types are used to convert data contained in a SolrFusion query to data needed by a Solr server. Also they are applied
 to Solr document fields to create SolrFusion values. In general Script Types work on a java.util.List<java.lang.String>
and return either a java.lang.String or a java.util.List<java.lang.String>. The incoming list is maybe empty or can
contain null entries.

Additional context information is provided in a ScriptEnv (Java) object, which contains the following entries.
 
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
* __queryTarget__ - an enumeration value of: "all", "filter-query", "query" or "highlight-query". If unset the current
    ScriptType's invocation is not working on a query part.
* __responseTarget__ - an enumeration value of: "all", "facet", "document" or "highlight". If unset the current
    ScriptType's invocation is not working on a response part.

Outgoing: To be set by scripting languages only. Java implementations return an object of org.outermedia.solrfusion.types.TypeResult.

* __returnValues__ - a single java.lang.Object or a java.util.List<java.lang.String> to return the converted values; 
    the single Object value is automatically converted into a List of String (the toString() method is called).
* __returnWordCounts__ -  if __mapFacetValue__ is true and the order or number of values is modified then the facet
    word counts need to be adjusted too; the default value is the original word count list
 
These entries are directly accessible via simple variable names in the scripting languages supported by Java (e.g. [Javascript](#javascript) and
[Bean Shell](#bean-shell)). In Java implementations the ScriptEnv methods have to be used to access these context variables.

The following chapters describe all predefined Script Types in detail:

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
Note: The file is not classpath based loaded. This means, when relative paths are used, it depends on your application 
server (and installation) what the current working directory is.

Hint: When the Javascript code simply returns an integer number, this would be rendered as e.g. "2.0" which is not desired (in Javascript numbers are always floats).
Simply use x.toFixed() to get rid of the fractional digits.


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

In the case that a value is set for a facet, the word count is automatically set to "1".

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
The Solr responses of the configured Solr servers are combined respecting document merging, facets and highlights. Also
the documents are sorted and the documents to return are selected.

Declaration (mandatory):

    <om:response-consolidator class="org.outermedia.solrfusion.response.PagingResponseConsolidator$Factory"/>
    
### Paging and Sorting
Because of the supported paging it is not possible to base on the Solr server's sorting. Therefor SolrFusion sorts Solr
documents on its own and calculates the documents of a page too. To be able to implement this, the request parameters
"start" and "rows" are manipulated. For a given start=<START>, rows=<ROWS> and maximum number of documents to return
maxDocs(SolrServer1)=<MAX> SolrFusion sends start=0 and rows=min(MAX,START+ROWS) to the SolrServer1. The total number
of documents is the sum of min(MAX,totalHits) per requested Solr server. The limitation is necessary, because otherwise
really many Solr documents need to be transferred and processed for high page numbers. With a configurable limit Java's heap space is
computable too.

When all received Solr documents were converted to Java objects and mapped to SolrFusion's schema, they are sorted by the wanted field and the documents
of the requested page are returned. The implementation is optimized for speed: For sorting only the fields id, score and the sort
field are mapped at first. The remaining fields are only mapped for the documents being really returned.
    
In contrast to Solr SolrFusion is able to sort multi value by the first value.    
    
## Mapper
A "mapper" applies mapping rules (see [Field Mappings](#field-mappings)) to fields contained in a query or a Solr document.

Declaration (mandatory):

        <om:response-mapper class="org.outermedia.solrfusion.mapper.ResponseMapper$Factory" ignore-missing-mappings="true"/>
        <om:query-mapper class="org.outermedia.solrfusion.mapper.QueryMapper$Factory" />
    
The "response-mapper" is suitable to convert Solr documents and the "query-mapper" to convert SolrFusion queries.
    
The response mapper offers a switch whether to throw an error or to continue with a warning when unmapped fields are
found. The attribute __ignore-missing-mappings__ controls this behaviour (default: false).

The query mapper always continues, but logs warnings for unmapped fields.

    
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
based on SolrFusion field ("ISBN" in the example below). Single and multi values are supported. Multi values fields
are equal if their intersection is not empty.
Note: Document merging is done after mapping.

Declaration (optional, child of `<om:solr-servers>`):

        <om:merge fusion-name="ISBN" class="org.outermedia.solrfusion.DefaultMergeStrategy$Factory">
            <om:target prio="1" target-name="LibraryA"/>
            <om:target prio="2" target-name="LibraryB"/>
        </om:merge>   
  
The `prio` attribute describes the relevance of the Solr server referenced by `target-name`. Valid values of
`target-name` are the "name" attribute values of the configured Solr servers. Because the `<om:target>` are sorted
by their prio attribute at first, it is neither required to sort the fields manually or to start with 1 or to increment
each priority by 1. Higher prio numbers mean lower priority. Please note that the document merging works on already
converted SolrFusion values (the field is specified in the `fusion-name` attribute).

According to the priority the document of the first server is used as the main document. The document fields of lower
prioritized servers are used to fill unset fields. If the merge field is not unique one Solr server may return
several documents to merge. In this case - several documents with same priority - the order is undefined, but all 
documents are checked if they contain a value for an unset field.

The implementation is optimized for speed: At first only the map field of all received documents is converted to a 
SolrFusion field. Then all documents are grouped by equal map field values and are finally merged into one document. 
After sorting the merged documents, the remaining fields of the documents to return are completely mapped to SolrFusion
fields.

If document merging is enabled, a solr query may return merged documents with a limited set of document fields. A 
following query may request full details of a merged document so that it is necessary to request the document from
all servers which built the merged document. Therefor the id of a merged document contains the Solr server name and
Solr document id of all involved servers/documents (see [SolrFusion Id Generator](#solrfusion-id-generator)). For
such id queries the received documents are finally merged again.

Because document merging affects the total number of found documents, this counter is adjusted after merging. But it
is possible that this number varies depending on the page, because equal documents are maybe returned on later pages.

Because facet values are calculated by the configured Solr servers and their value is independent from the returned
documents, it is not possible to correct the number of word occurrences.


## Solr Server Settings
This section contains the description of one Solr server to use.

Declaration (optional, but at least one instance; child of `<om:solr-servers>`):

    <om:solr-server name="DBoD" version="3.5" query-param-name="q.alt" enabled="true"
        class="org.outermedia.solrfusion.adapter.solr.DefaultSolrAdapter$Factory">
        <om:config>
            <!-- adapter specific XML elements -->
        </om:config>                            

* __name__ - The value of the required __name__ attribute needs to be unique within all Solr server declarations and must not contain 
underscore, SPACE or minus. 
* __version__ - The value of the required __version__ attribute is evaluated by the Solr1 adapter in order to generate
valid facet sort values.
* __query-param-name__ - this optional attribute allows to overwrite the HTTP request parameter which contains the Solr query (default: q). If the
value is set to a different value than "q", q will be set to a "dismax" version of the original edismax query. Note: 
Especially for a Solr 1.X server we used for testing, it was necessary to pass edismax queries in the q.alt 
parameter and the "same" query in dismax format in order to receive highlights.
* __enabled__ - To simplify testing with a subset of configured Solr servers the optional __enabled__ attribute can be set to false 
    (default: true). Note: Nesting of XML comments is not possible.
* __`<om:config>`__ - additional adapter specific configuration can be optionally specified here. Implementations access the
    parsed XML with SearchServerConfig.getAdapterConfig()``.

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

### Maximum Number Of Documents To Return
Because sorting and paging is necessarily implemented in SolrFusion, it is necessary to limit the maximum number of
documents to receive from one server for one query.

Declaration (mandatory, child of `<om:solr-servers>`):

    <om:max-docs>100</om:max-docs>

## Field Mappings
Finally a configuration of a Solr server contains the mapping rules which convert fields of queries and Solr documents.
The following chapters show use-cases and examples of typical conversions.

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

Be aware that the mapping rules are per default applied to facets and highlights too, because they are transformed into Solr
documents. But it is possible to limit the inner `<om:query>` and `<om:response>` XML children to specific parts of a
query resp. response. Examples:

    <!-- Example 1: Add facet in response -->
    <om:field fusion-name="solr-server">
        <om:add>
            <om:response target="facet" type="static-value">
                <value>UBL4</value>
            </om:response>
        </om:add>
    </om:field>
    
    <!-- Example 2: Add new filter query -->
    <om:field name="extra">
        <om:add level="outside">
            <om:query target="filter-query" type="static-value">
                <value>title:newFQ</value>
            </om:query>
        </om:add>
    </om:field>

Valid target values in a `<om:response>` are: __all__ (default if absent), __facet__, __document__ and __highlight__.

In a `<om:query>` valid target values are: __all__ (default if absent), __filter-query__, __query__ and __highlight-query__.

If a multi value field is mapped to a single value field the behaviour is as follows:

* An error is logged if the multi value contains more than one value and the field is ignored. Use the ScriptType
    [MultiValueMerger](#multi-value-merger) to flatten multi values and to avoid this error.
* Contains the value exactly one value, this value is used without warning or error messages.

Especially for facets: When the mapping rules map two Solr fields to one SolrFusion field, then their mapped values and word counts
are combined automatically. Because it is necessary to eliminate duplicate words, their counters are added.

### Change
The most basic change is to create a copy:

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
This operation is needed to remove fields from queries when the destination Solr has no equivalent Solr field.

Examples:

    <!-- ignore field in response -->
    <om:field name="f52">
        <om:drop>
            <om:response />
        </om:drop>
    </om:field>
    
    <!-- ignore field in query -->
    <om:field fusion-name="g52">
        <om:drop>
            <om:query />
        </om:drop>
    </om:field>
    
    <!-- ignore field in query and response -->
    <om:field name="f52" fusion-name="g52">
        <om:drop>
            <om:response />
            <om:query />
        </om:drop>
    </om:field>

The drop implementation works non-destructive (only a flag is set), so that it is e.g. possible to drop a field and change it in a following
operation or mapping rule. The query builder and document response renderer respect the drop flag and ignore fields marked dropped.

Only the following combinations of __name__ / __fusion-name__ and `<om:query>`/`<om:response>` are valid for `<om:drop>`:

* __name__ and `<om:response>` - remove the Solr field from the received document
* __fusion-name__ and `<om:query>` - remove the SolrFusion field from the SolrFusion query

This avoids ambiguities and allows to use drop statements even if also other attributes are specified.

The removal is easily possible, because the query parser transforms AND/OR expressions into a list of single
terms (AND means prefix with "+") which can be deleted one by one. The query builder rebuilds the AND/OR expressions again.

### Add
You can use `<om:add>` in order to add new query parts or document fields. But the behaviour is different. When applied
to queries `<om:add>` always create new query parts, but applied to a Solr document it depends to the field's name. As
long as the field is unset it is added, but following rules - using the same field name - will overwrite the field, because
in a document a field has to be unique.

Another difference is, that it is possible to define where to add a query part. As a global filter at the end (`<om:add level="outside">`) 
or as a sibling of an existing field (`<om:add level="inside">`).

Please find below examples for queries and documents.

Query examples:

    <!-- Example 1 -->
    <!-- the provided condition is added once at the end of the original query -->
    <!-- the leading "+" results in an AND and nothing in an OR -->
    <om:field name="t11">
        <om:add level="outside">
            <om:query type="static-value">
                <value>+t11:"searched text"~2^75</value>
            </om:query>
        </om:add>
    </om:field>

    <!-- Example 2 -->
    <!-- the provided condition is added as a sibling to all text14 occurrences -->
    <!-- leading "+" would result in an AND and nothing in an OR -->
    <!-- the SolrFusion field text14 is mapped to t14a and remains in the query -->
    <om:field name="t14a" fusion-name="text14">
        <om:add level="inside">
            <om:query type="static-value">
                <value>t14a:helloA</value>
            </om:query>
        </om:add>
    </om:field>
    
    <!-- Example 3 -->    
    <!-- fields s17 and s18 are copied as siblings for all text17 occurrences -->
    <!-- finally the doubly added field text17 is removed -->
    <om:field name="s17" fusion-name="text17">
        <om:add level="inside">
            <om:query/>
        </om:add>
    </om:field>
    <om:field name="s18" fusion-name="text17">
        <om:add level="inside">
            <om:query/>
        </om:add>
        <om:drop>
            <om:query/>
        </om:drop>
    </om:field>

Response examples:

    <!-- Example 1 -->
    <!-- add field with multiple values to every document -->
    <om:field fusion-name="Tags">
        <om:add>
            <om:response type="static-value">
                <value>article</value>
                <value>news</value>
                <value>daily</value>
            </om:response>
        </om:add>
    </om:field>
                
    <!-- Example 2 -->                
    <!-- second rule modifies value of first rule, because it is the same field -->
    <om:field name="t12" fusion-name="text12">
        <om:add>
            <om:response type="static-value">
                <value>42</value>
            </om:response>
        </om:add>
    </om:field>
    <om:field fusion-name="text12">
        <om:add>
            <om:response type="javascript">
                <script>
                    println("Mapping for text12: fusionValue="+fusionValue);
                    // in JS all numbers are float!
                    returnValues = (fusionValue.get(0)-41).toFixed()
                </script>
            </om:response>
        </om:add>
    </om:field>
                    
    <!-- Example 3 -->                    
    <!-- copy field in response -->                    
    <om:field name="s19" fusion-name="text18a">
        <om:add>
            <om:response />
        </om:add>
    </om:field>                    
    
Only the following combinations of __name__ / __fusion-name__ and `<om:query>`/`<om:response>` are valid for `<om:add>`:    
    
* __name__ and `<om:query>` - directly adds a new Solr query part; the value is used unmodified and not mapped, so
    the value is expected to be a valid raw Solr query (perhaps complex)
* __fusion-name__ and `<om:response>` - directly add a fusion field to a Solr document; the value is used unmodified and not mapped,
    so the value is expected to be a raw SolrFusion value

This avoids ambiguities and allows to use add statements even if also other attributes are specified.
    
It is worth to mention that all add "outside" query rules are applied when all change and delete rules have been
applied and the query builder has created the query. The new query parts are appended, separated by "AND" (edismax) or
a SPACE (dismax).

When a field is copied ("Example 3" above) word counts of facets are copied too (when a facet is mapped).
    
### Split Merge Use Case
This is a common mapping use case where one SolrFusion field is mapped to several Solr fields or vice versa. 

The following two examples show a possible solution for both cases.

Example 1: Two Solr fields are mapped to one SolrFusion field

    <om:field name="author" fusion-name="author_facet">
        <om:add level="inside"><om:query /></om:add>
        <om:drop><om:response /></om:drop>
    </om:field>
    <om:field name="author2" fusion-name="author_facet">
        <om:add level="inside"><om:query /></om:add>
        <om:drop><om:response /></om:drop>
    </om:field>
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
    
Explanation: In queries the field __author_facet__ is removed and two copies - __author__ and __author2__ - are added as siblings.    
In responses the fields __author__ and __author2__ are removed and a new field __author_facet__ is added which merges
the fields __author__ and __author2__.
    
Example 2: Map two SolrFusion fields to one Solr field
    
    <om:field name="edition" fusion-name="publishDateSort" >
    </om:field>
    <om:field name="edition" fusion-name="publishDate" >
    </om:field>
    
Explanation: The mapping depends on your requirements, because it is in general not possible to implement the query
mapping part: Therefor the "field-merger" would have to search all occurrences of __publishDateSort__ and
__publishDate__ and to join them. This might work if they occur only once in simple queries. But how to handle
several occurrences in complex boolean queries? 

The example above doesn't implement a split-merge, but uses domain specific knowledge to solve this issue.  
If no __publishDate__ field is present in a query the value of __publishDateSort__
is used for the Solr field __edition__. Otherwise __publishDate__ sets the value. Because __publishDateSort__ is used
for sorting only, their value is ignored in responses, if __publishDate__ is present.

For responses it is easily possible to copy the same Solr value to both SolrFusion fields:

    <om:field name="edition" fusion-name="publishDateSort" >
        <om:add><om:response /></om:add>
    </om:field>
    <om:field name="edition" fusion-name="publishDate" >
        <om:add><om:response /></om:add>
    </om:field>
    <om:field name="edition">
        <om:drop><om:response /></om:drop>
    </om:field>
     
# Known Issues     
The following chapters describe mostly - in general - unsolvable issues which were found during the implementation and
testing with vufind. 

## Sort A Split Field
If a SolrFusion field is split into several Solr fields and the SolrFusion field is used for sorting then it is not
possible to map the semantics to a Solr field. Because the Solr sort field supports only tiered sort fields only one
field can be specified. All other sort fields have a lower precedence and it is not possible to decide which of the split fields
is the most important field.

In version 1.0 SolrFusion uses only the textually first mapping for sorting.
     
## Map Values With Wildcards
In general it is not possible to map values which e.g. contain a "?" or "*", because it is not possible to map all
parts except the wildcards in a useful way. But it depends on the value range and is perhaps in some cases possible.
   
SolrFusion contains neither a recognition nor a special handling of values with wildcards. They are treated as values
without wildcards. If a special handling is needed a new ScriptType Java class has to be implemented and used in the
mapping of the affected field.
     
## Fuzzy Slop
Because the value range and semantics of the fuzzy slop changed between Solr versions, SolrFusion can't automatically
adapt the fuzzy values. In Solr 3.X a value range between 0 and 1 is used and in Solr4 only 0, 1 or 2 are allowed.

SolrFusion's query builder simply print out only the "~" without any value.
     
## Removed Fields In Filter Queries
Depending on the application which uses SolrFusion and the involved Solr schemas it might happen that query expressions 
of filter queries become "empty", because their contained field is removed. If then the remaining request is still sent
 to a Solr server the server returns to many documents which is not wanted.
     
Currently SolrFusion 1.0 has no solution for this issue. But it makes sense to enhance the implementation not to sent a
a query at all, because in general too many documents would be returned.

## Morelikethis
Especially vufind uses a simple id query with qt=morelikethis in order to get similar documents. If the affected document is
not merged, then the whole document is stored in one Solr server. Because of the special id handling (see [Id Filter](#id-filter))
actually only one server is requested and it is not possible to return similar documents from the other configured Solr servers.
    
If the document of the id query was merged from several Solr servers then several Solr servers are requested and their
result will be merged again.
    
SolrFusion offers no solution for this issue, but at least as much similar documents as without SolrFusion are returned,
but not more.
     
## Merge Of Two SolrFusion Fields Into One Solr Field In Queries
If the SolrFusion fields f1 and f2 should be mapped to one single Solr field s1, it is not decidable to join the correct
instances in the case that several f1 and f2 occur in a query. Even if only one f1 and one f2 is present it is 
uncertain whether both belong together. 

SolrFusion can't implement a solution, so that only a domain specific or application specific solution is possible.

## Boosting When Two SolrFusion Fields Are Mapped To One Solr Field
Example: `qf=author^500 author2^250` is mapped to `qf=name^500 name^250`. Which boost value is preferrable? Maximum
 value, minimum value or average?
     
SolrFusion offers no general solution and sends the mapped qf with "duplicate" boost values to a Solr server.   
     
## New Static Query Parts
By the use of `<om:add level="outside">` it is possible to add new parts to a query. But the query syntax is currently
ignored, so that this only works when always either dismax or edismax queries are sent from the application using
SolrFusion. The same applies to static query parts when added "inside".
      
The `<om:add>` is executed for every query, but perhaps is the addition not always desired?
       
Several solutions are conceivable, but for all SolrFusions needs to be improved.
     
## One SolrFusion Field Is Mapped To Several Solr Fields In Dismax Queries
If the field title is the default search field and e.g. the query `title:abc` is mapped to `title1:abc OR title2:abc`, 
then the equivalent dismax query would be `abc abc`, because title1 and title2 are the Solr default search fields.

Perhaps domain/application specific solutions are possible, i.e. the query mapping of "title" simply targets one
Solr field (instead of several). A SolrFusion improvement, which automatically removes duplicate search word,  
is conceivable too.

In version 1.0 SolrFusion no special handling exists for this case and the query is directly sent to a Solr server.
     
## Total Document Count Correction And Document Merging
Because SolrFusion (almost) never works on all documents found by a search, it is impossible to return the right
document count when document merging is enabled. So it is possible that the total document count varies depending
on the current "page", because SolrFusion is perhaps able to merge documents (depends on sort fields).
                    
          
END