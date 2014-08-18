<?xml version="1.0" encoding="UTF-8"?>
<response>
<lst name="responseHeader">
  <int name="status">0</int>
  <int name="QTime">0</int>
  <lst name="params">
    <str name="indent">on</str>
    <str name="start">0</str><#list responseHeader.queryParams?keys as key>
    <str name="${key}"><![CDATA[${responseHeader.queryParams[key]}]]></str></#list><#list responseHeader.multiValueQueryParams?keys as key>
    <arr name="${key}"><#list responseHeader.multiValueQueryParams[key] as v>
        <str>${v?xml}</str></#list>
    </arr></#list>
    <str name="wt">wt</str>
    <str name="version">2.2</str>
  </lst>
</lst>
<#if responseError.error>
<lst name="error">
    <str name="msg"><![CDATA[${responseError.msg}]]></str>
    <int name="code">${responseError.code}</int>
</lst>
</#if>
<#if response.matchDocuments?has_content>
<result name="match" numFound="${response.totalMatchHitNumber?c}" start="0">
    <#list response.matchDocuments as document>
    <doc>
    <@outputSingleValueFields fields=document.singleValuedFields />
    <@outputMultiValueFields fields=document.multiValuedFields />
    </doc>
    </#list>
</result>
</#if>
<result name="response" numFound="${response.totalHitNumber?c}" start="0">
    <#list response.documents as document>
    <doc>
    <@outputSingleValueFields fields=document.singleValuedFields />
    <@outputMultiValueFields fields=document.multiValuedFields />
    </doc>
    </#list>
</result>
<#if facets.hasFacets>
<lst name="facet_counts">
    <lst name="facet_queries" />
    <lst name="facet_fields">
<#list facets.facets?keys as field>
        <lst name="${field}">
<#list facets.facets[field] as wc>
            <int name="${wc.word?xml}">${wc.count?c}</int>
</#list>
        </lst>
</#list>
    </lst>
    <lst name="facet_dates" />
    <lst name="facet_ranges" />
</lst>
</#if>
<#if highlighting.hasHighlights>
<lst name="highlighting"><#list highlighting.highlighting as doc>
    <lst name="${doc.id}">
<@outputMultiValueFields fields=doc.multiValuedFields />
    </lst></#list>
</lst>
</#if>
</response>
<#macro outputSingleValueFields fields>
    <#if fields??>
            <#list fields as field>
                <@outputField name=field.name type=field.type value=field.value />
            </#list>
    </#if>
</#macro>
<#macro outputMultiValueFields fields>
    <#if fields??>
            <#list fields as field>
                <arr name="${field.name}">
                    <#list field.values as value>
                        <@outputField type=field.type value=value />
                    </#list>
                </arr>
            </#list>
    </#if>
</#macro>
<#macro outputField type value name="" >
    <#switch type>
        <#case "float">
        <#case "double">
            <#assign responseKey="float">
            <#break>
        <#case "int">
        <#case "long">
            <#assign responseKey="int">
            <#break>
        <#case "boolean">
            <#assign responseKey="bool">
            <#break>
        <#case "date">
            <#assign responseKey="date">
            <#break>
        <#default>
            <#assign responseKey="str">
            <#break>
    </#switch>
    <#if name?has_content>
        <#assign printName=" name=\"${name}\"">
    <#else>
        <#assign printName="">
    </#if>
        <${responseKey}${printName}><#escape value as value?xml>${value}</#escape></${responseKey}>
</#macro>