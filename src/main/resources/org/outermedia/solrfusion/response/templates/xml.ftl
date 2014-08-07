<?xml version="1.0" encoding="UTF-8"?>
<response>
<lst name="responseHeader">
  <int name="status">0</int>
  <int name="QTime">0</int>
  <lst name="params">
    <str name="indent">on</str>
    <str name="start">0</str>
    <str name="q"><![CDATA[${responseHeader.query}]]></str>
    <#if responseHeader.filterQuery??>
    <str name="fq"><![CDATA[${responseHeader.filterQuery}]]></str>
    </#if>
    <#if responseHeader.sort??>
    <str name="sort"><![CDATA[${responseHeader.sort}]]></str>
    </#if>
    <#if responseHeader.fields??>
    <str name="fl"><![CDATA[${responseHeader.fields}]]></str>
    </#if>
    <#if responseHeader.highlight??>
    <str name="hl"><![CDATA[${responseHeader.highlight}]]></str>
    </#if>
    <#if responseHeader.highlightPre??>
    <str name="hl.simple.pre"><![CDATA[${responseHeader.highlightPre}]]></str>
    </#if>
    <#if responseHeader.highlightPost??>
    <str name="hl.simple.post"><![CDATA[${responseHeader.highlightPost}]]></str>
    </#if>
    <#if responseHeader.highlighFields??>
    <str name="hl.fl"><![CDATA[${responseHeader.highlighFields}]]></str>
    </#if>
    <#if responseHeader.highlightQuery??>
    <str name="hl.q"><![CDATA[${responseHeader.highlightQuery}]]></str>
    </#if>
    <str name="wt">wt</str>
    <str name="version">2.2</str>
    <str name="rows">${responseHeader.rows}</str>
  </lst>
</lst>
<#if responseError.error>
<lst name="error">
    <str name="msg"><![CDATA[${responseError.msg}]]></str>
    <int name="code">${responseError.code}</int>
</lst>
</#if>
<result name="response" numFound="${response.totalHitNumber}" start="0">
    <#list response.documents as document>
    <doc>
    <@outputSingleValueFields fields=document.singleValuedFields />
    <@outputMultiValueFields fields=document.multiValuedFields />
    </doc>
    </#list>
</result>
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