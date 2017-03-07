Getting Started
===============
For SolrFusion 1.1. Date: 2017-03-07

Outermedia GmbH 

This guide gives a short introduction how to install and set up a 
working SolrFusion instance in a linux environment.

The examples were tested/evaluated against release 1.1.

Installation
------------
For tests/development no root user access is required.

The steps below assume that Java 7.X is already installed and that
the binary "java" is in the PATH (environment variable). Ensure that
the environment variable JAVA_HOME points to your Java installation.

If no java is installed, get it from <https://java.com/de/download/>.

Note: The Java 8.X incompatibility is caused by the used Solr Java
API and not SolrFusion itself.

The following chapters use "$>" to indicate shell commands and ">" 
for console output.

### 1. Create install dir

	$> cd ${BASE_DIR}
	$> mkdir solrfusion-install
	
### 2. Download latest releases

	$> wget https://github.com/outermedia/solr-fusion/releases/download/solrfusion-1.1/solrfusion-1.1.war
	
### 3. Download apache tomcat

	# https://tomcat.apache.org/download-80.cgi
	$> wget http://ftp.halifax.rwth-aachen.de/apache/tomcat/tomcat-8/v8.5.11/bin/apache-tomcat-8.5.11.tar.gz
	$> wget https://www.apache.org/dist/tomcat/tomcat-8/KEYS
	$> wget https://www.apache.org/dist/tomcat/tomcat-8/v8.5.11/bin/apache-tomcat-8.5.11.tar.gz.asc

	# check file integrity ...
	$> gpg --import KEYS
	$> gpg --verify apache-tomcat-8.5.11.tar.gz.asc
	
	>	gpg: assuming signed data in 'apache-tomcat-8.5.11.tar.gz'
	>	gpg: Signature made Tue Jan 10 22:05:37 2017 CET using RSA key ID 2F6059E7
	>	gpg: Good signature from "Mark E D Thomas <markt@apache.org>"
	>	gpg: WARNING: This key is not certified with a trusted signature!
	>	gpg:          There is no indication that the signature belongs to the owner.
	
### 4. Install war

	$> tar zxf apache-tomcat-8.5.11.tar.gz

Apache tomcat uses the name of war file as the web application's name (here "sf").

	$> mv solrfusion-1.1.war apache-tomcat-8.5.11/webapps/sf.war

### 5. Start/stop tomcat
This step  will unpack the war file, so that it is possible to customize
logging and to create an initial schema mapping file.

Ensure that no other program is blocking tomcat's default port 8080: Simply
point your browser to <http://localhost:8080/>. If no response is generated
the port 8080 is free.

We will use tomcat's manager ui, which expects to add a manager user for login.
The commands below configure a user "man1" with password "man1".

	$> sed -i -e '$i<role rolename="manager-gui"/>\n<user username="man1" password="man1" roles="manager-gui"/>' \
	$> apache-tomcat-8.5.11/conf/tomcat-users.xml
	
Ensure that the current working directory is still ${BASE_DIR}/solrfusion-install
before executing the next command.
	
	$> CATALINA_OPTS=-Dtomcat.home=`pwd`/apache-tomcat-8.5.11 && export CATALINA_OPTS 

	$> apache-tomcat-8.5.11/bin/startup.sh 
	
	>	Using CATALINA_BASE:   <BASE_DIR>/solrfusion-install/apache-tomcat-8.5.11
	>	Using CATALINA_HOME:   <BASE_DIR>/solrfusion-install/apache-tomcat-8.5.11
	>	Using CATALINA_TMPDIR: <BASE_DIR>/solrfusion-install/apache-tomcat-8.5.11/temp
	>	Using JRE_HOME:        <JAVA_HOME>/jdk1.8.0_40
	>	Using CLASSPATH:       <BASE_DIR>/solrfusion-install/apache-tomcat-8.5.11/bin/bootstrap.jar\
	>	:<BASE_DIR>/solrfusion-install/apache-tomcat-8.5.11/bin/tomcat-juli.jar
	>	Tomcat started.
	
	$> tail apache-tomcat-8.5.11/logs/catalina.out
	
	>	...
	>	07-Mar-2017 09:59:55.019 INFO [main] org.apache.coyote.AbstractProtocol.start Starting ProtocolHandler [http-nio-8080]
	>	07-Mar-2017 09:59:55.024 INFO [main] org.apache.coyote.AbstractProtocol.start Starting ProtocolHandler [ajp-nio-8009]
	>	07-Mar-2017 09:59:55.025 INFO [main] org.apache.catalina.startup.Catalina.start Server startup in 987 ms

Call in your browser http://localhost:8080/manager/status to check your apache's status.
At first a popup should be displayed to login. Use "man1" as username and "man1" as password.

After login a server status page is displayed. In the left upper corner the link "List Applications" 
leads to a new page where all installed applications are listed. At the bottom the
SolrFusion application "sf" should be present. Check that the column "Running" contains "true".

If the above steps succeeded, the SolrFusion installation succeeded in principle, 
but the configuration is still missing. Therefor shutdown tomcat:

	$> apache-tomcat-8.5.11/bin/shutdown.sh

Tomcat's shutdown may take a while. Sometimes the shutdown is blocked
by resources occupied by the web application. To be sure that tomcat really exited, try:

	$> ps auxw|grep -v grep |grep tomcat

If this command prints nothing, no tomcat unix process exists anymore and
tomcat exited. To check tomcat's manager (<http://localhost:8080/manager/status>)
isn't sufficient.

### 6. Customize logging
The initial log level is set to debug which should give enough information
to check the processing of SolrFusion.

The log level and other log settings are located in the log4j.properties file:

	apache-tomcat-8.5.11/webapps/sf/WEB-INF/classes/log4j.properties
	
By default the log output is written to apache-tomcat-8.5.11/logs/log4j.log.

Note: Changes require a tomcat restart.

### 7. Initial mapping file
The following configuration assumes that a solr is running at

	http://localhost:8180/solr/p1
	
and a core named "p1" exists.

The mapping file is configured in apache-tomcat-8.5.11/webapps/sf/WEB-INF/web.xml.
Initially fusion-schema-uni-leipzig.xml is used:

    <servlet>
        <servlet-name>SolrFusionServlet</servlet-name>
        <servlet-class>org.outermedia.solrfusion.SolrFusionServlet</servlet-class>
        <init-param>
            <param-name>fusion-schema</param-name>
            <param-value>fusion-schema-uni-leipzig.xml</param-value>
        </init-param>
        <init-param>
            <param-name>fusion-schema-xsd</param-name>
            <param-value>configuration.xsd</param-value>
        </init-param>
        <init-param>
            <param-name>applyLatin1Fix</param-name>
            <param-value>false</param-value>
        </init-param>
        <load-on-startup>1</load-on-startup>
    </servlet>
    
Now replace fusion-schema-uni-leipzig.xml with fusion-schema-p1.xml in the
web.xml file and save it.

Create fusion-schema-p1.xml in apache-tomcat-8.5.11/webapps/sf/WEB-INF/classes/
and paste-in the following xml:

	<?xml version="1.0" encoding="UTF-8"?>
	<om:core
		xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xmlns:om="http://solrfusion.outermedia.org/configuration/" 
		xmlns="http://solrfusion.outermedia.org/configuration/type/"
		xsi:schemaLocation="http://solrfusion.outermedia.org/configuration/ configuration.xsd"
	>
		<om:fusion-schema-fields default-type="text">
			<om:field name="score" type="float"/>
			<om:field name="id" />
		</om:fusion-schema-fields>

		<om:script-type name="regexp" class="org.outermedia.solrfusion.types.RegularExpression" />
		<om:script-type name="static-table" class="org.outermedia.solrfusion.types.Table"/>
		<om:script-type name="static-value" class="org.outermedia.solrfusion.types.Value"/>
		<om:script-type name="filter-id" class="org.outermedia.solrfusion.types.IdFilter"/>

		<om:default-search-field>title</om:default-search-field>
		<om:default-sort-field>score desc</om:default-sort-field>
		<om:default-operator>AND</om:default-operator>

		<om:id-generator fusion-name="id" class="org.outermedia.solrfusion.DefaultIdGenerator$Factory" />
		<om:response-consolidator class="org.outermedia.solrfusion.response.PagingResponseConsolidator$Factory"/>
		<om:response-mapper class="org.outermedia.solrfusion.mapper.ResponseMapper$Factory" />
		<om:query-mapper class="org.outermedia.solrfusion.mapper.QueryMapper$Factory" />
		<om:controller class="org.outermedia.solrfusion.FusionController$Factory" />

		<om:solr-servers>
			<om:timeout>4000</om:timeout>
			<om:disaster-limit>0</om:disaster-limit>
			<om:error key="disaster-limit">Too few search servers responded to the request.</om:error>
			<om:page-size>10</om:page-size>
			
			<om:query-parser class="org.outermedia.solrfusion.query.EdisMaxQueryParser$Factory"/>
			<om:dismax-query-parser class="org.outermedia.solrfusion.query.DisMaxQueryParser$Factory"/>
			<om:response-parser class="org.outermedia.solrfusion.response.DefaultResponseParser$Factory" />
			<om:response-renderer type="json" class="org.outermedia.solrfusion.response.DefaultJsonResponseRenderer$Factory" />
			<om:query-builder class="org.outermedia.solrfusion.mapper.QueryBuilder$Factory" />
			<om:dismax-query-builder class="org.outermedia.solrfusion.mapper.DisMaxQueryBuilder$Factory"/>

			<om:solr-server name="LibP1" version="3.6" class="org.outermedia.solrfusion.adapter.solr.DefaultSolrAdapter$Factory">
				<om:url>http://localhost:8180/solr/p1/select</om:url>
				<om:score factor="1.0" class="org.outermedia.solrfusion.DefaultScore$Factory" />
				<om:unique-key>id</om:unique-key>
				<om:max-docs>100</om:max-docs>

				<!-- simply copy all fields -->
				<om:field name="*" fusion-name="*"/>

				<!-- but with the following exceptions -->
				<om:field name="id" fusion-name="id">
					<om:change>
						<om:query type="filter-id"/>
					</om:change>
				</om:field>
			</om:solr-server>
		</om:solr-servers>
	</om:core>

Then change the settings of the solr server to your needs in: \<om:core\>/\<om:solr-servers\>/\<om:solr-server\>.
Change version="3.6" to the version of your solr and edit \<om:url\>. SolrFusion 
needs the whole path which is used to "select" solr queries

Note: The mapping above assumes that the referenced solr contains documents
which have an id field.

Restart tomcat (see chapter 5). A successful start of SolrFusion prints 
no errors to apache-tomcat-8.5.11/logs/log4j.log, but only:

	07.03 12:28:35.832 [localhost-startStop-1] INFO - Will use latin1 fix: false
	07.03 12:28:36.064 [localhost-startStop-1] INFO - org.outermedia.solrfusion.configuration.Configuration Reading conf file: 'fusion-schema-p1.xml' (schema: 'configuration.xsd' -> file:/home/ballmann/muell/solrfusion-install/apache-tomcat-8.5.11/webapps/sf/WEB-INF/classes/configuration.xsd)

Because the mapping file validated, you might see errors like this if
the file contains errors:

	unexpected element (uri:"http://solrfusion.outermedia.org/configuration/", local:"ids-controller"). 
	Expected elements are 
	<{http://solrfusion.outermedia.org/configuration/}default-sort-field>,
	...
	<{http://solrfusion.outermedia.org/configuration/}default-operator> 
	at line=26, column=88
	
Read it like this: At line 26 (column 88) is an XML element (\<om:ids-controller\>) 
which is not expected. The list of expected elements is \<om:default-sort-field\>, ..., \<om:default-operator\>
(the namespace "om" maps to {http://solrfusion.outermedia.org/configuration/}).

Please note, that the order of XML elements is defined and can't be choosen
arbitrary. <https://github.com/outermedia/solr-fusion/blob/master/src/main/resources/fusion-schema-uni-leipzig.xml>
is an almost complete example.

### 8. First SolrFusion query
Open your browser and open the link:

<http://localhost:8080/sf/biblio/select/?q=*:*&wt=json&forceSchemaReload=1>

Because the xml element \<om:fusion-schema-fields\> in fusion-schema-p1.xml contains 
only "id" and "score", only these fields are returned in the response:

	{
	  "responseHeader":{
		"status":0,
		"QTime":93,
		"params":{
		  "indent":"on",
		  "rows":"10",
		  "q":"*:*",
		  "wt":"json",
		  "version":"2.2"}}
	  , "response":{"numFound":1457,"start":0,"docs": [
	  {
		"score":1.0,
		"id":"LibP1_633"
	  },
	  ...
	  ]}
	}
	
If e.g. \<om:field name="title" /\> is added and the URL above is opened
again, a title field will occur (if your solr contains such a field).

	...
	  {
		"score":1.0,
		"id":"LibP1_633",
		"title":"Math symbols"
	  },
	...
	
Note: Fields are explained in detail in 
<https://github.com/outermedia/solr-fusion/blob/master/src/main/doc/user-guide.md#solrfusion-schema-fields>

Plese note, that the SolrFusion special request parameter forceSchemaReload=1
forces a reload of the changed mapping file. Otherwise a tomcat restart
is necessary.

If you want to add more solr servers, simply copy the <om:solr-server>
element, paste it below \</om:solr-server\> and apply your changes. You find
a real world example in:

<https://github.com/outermedia/solr-fusion/blob/master/src/main/resources/fusion-schema-uni-leipzig.xml>


### 9. Mapping

The example mapping contains only two rules:

	<om:field name="*" fusion-name="*"/>
	
	<om:field name="id" fusion-name="id">
		<om:change>
			<om:query type="filter-id"/>
		</om:change>
	</om:field>
	
The first rule simply copies all fields from a solr response (name="\*") to 
a SolrFusion field with the same name (fusion-name="\*") if the SolrFusion
field is defined in the \<om:fusion-schema-fields\> element.

The second rule is part of a common problem, when multiple solr servers are requested:
In order to create a unique SolrDocument id, SolrFusion automatically prepends the original
id with the solr server's name specified in \<om:solr-server name="..." ...\> (in the
example "LibP1") when evaluating solr responses. Note: This implies that 
the type of SolrFusions' "id" field is always "text". 

Slightly inconsistent, SolrFusion doesn't automatically remove the prefix when a query is sent
to a solr server. Therefor the second rule declares to \<om:change\> the SolrFusion 
"id" field before a \<om:query\> of the underlying solr server is executed.

The implementation of "filter-id" can be found in the Java class org.outermedia.solrfusion.types.IdFilter
as you can see in:

	<om:script-type name="filter-id" class="org.outermedia.solrfusion.types.IdFilter"/>

If you now look at the example mapping again, you may notice, that mostly 
all SolrFusion functionality is configurable and so adjustable (if Java know-how
is present).

In order to complete this mapping example, we will modify a Solr response field too.

1. Add \<om:field name="publicSince" type="date" /\> to \<om:fusion-schema-fields\>.
2. Append \<om:script-type name="javascript" class="org.outermedia.solrfusion.types.Js"/\> at the end of
the \<om:script-type ...\> block.
3. Add a new last mapping rule:

			<om:field name="Öffentlich_frei_ab_d" fusion-name="publicSince">
				<om:change>
					<om:response type="javascript"><script><![CDATA[
						importClass(java.util.Arrays);
						importClass(java.lang.Long);
						importClass(java.util.GregorianCalendar);
						importClass(java.text.SimpleDateFormat);
						var fmt = new SimpleDateFormat("yyyy-MM-dd");
						var dateMilliStr = searchServerValue.get(0);
						var date = new GregorianCalendar();
						date.setTimeInMillis(Long.parseLong(dateMilliStr));
						returnValues = Arrays.asList(fmt.format(date.getTime().getTime()));
						println("DATE CONVERT " + dateMilliStr + "->" + returnValues);
					]]></script></om:response>
				</om:change>
			</om:field>

The rule converts millis since 1970 to a string with format year-month-day. Be aware that a real implementation would have
to implement the opposite conversion too (for \<om:query\>).
The syntax is Javascript, but uses Java libraries. Such scripts can access 
a bunch of predefined variabeles (e.g. "searchServerValue"). All
are briefly described in <https://github.com/outermedia/solr-fusion/blob/master/src/main/doc/user-guide.md#script-types>.
Rules are explained in detail in: <https://github.com/outermedia/solr-fusion/blob/master/src/main/doc/user-guide.md#field-mappings>

**IMPORTANT:** Although <https://github.com/outermedia/solr-fusion/blob/master/src/main/doc/user-guide.md#javascript>
states that the right-hand-side value for "returnValues" is automatically
converted to a list, it is not correct (fails even in SolrFusion's unreleased master). 
It is necessary to manually build a list as shown in the example above (Arrays.asList(v1, v2, ...)).
I encourage you to build your own version with "mvn clean package" and to use the current stable "master"
to get javabin support too. Then it is necessary to update existing schema files and to add before line 31: \<om:ids-controller class="org.outermedia.solrfusion.IdsFusionController$Factory" /\>.

Java exceptions will be written to log4j.log and catalina.out. "println" output
(see last line in code above) goes to catalina.out only. But initially
when almost no Solr fields are mapped, you will find a lot of messages 
like this in the log files:

	org.outermedia.solrfusion.mapper.UndeclaredFusionField: Didn't find field 'XXXX' in fusion schema. Please define it there.
			at org.outermedia.solrfusion.configuration.ChangeOperation.applyAllResponseOperations(ChangeOperation.java:67)
			at org.outermedia.solrfusion.configuration.FieldMapping.applyResponseOperations(FieldMapping.java:236)
			at org.outermedia.solrfusion.mapper.ResponseMapper.mapField(ResponseMapper.java:317)
			at org.outermedia.solrfusion.mapper.ResponseMapper.visitField(ResponseMapper.java:421)
			at org.outermedia.solrfusion.response.parser.Document.accept(Document.java:127)
	...

This helps you not to miss a field's mapping.

In the case it is necessary to check SolrFusion's processing step-by-step,
the log level needs to be set to TRACE (log4j.properties):

	log4j.rootCategory=TRACE, Default, File
	...
	log4j.appender.File.Threshold=TRACE

Because then SolrFusion logs really a lot, I recommend to search in log4j.log from the bottom 
upwards for "Received request: http" which marks the start of the last processed request.


END
