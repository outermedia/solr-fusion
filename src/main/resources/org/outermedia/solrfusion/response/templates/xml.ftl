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
    <str name="version">2.2</str>
    <str name="rows">${responseHeader.rows}</str>
  </lst>
</lst>
<result name="response" numFound="${response.totalHitNumber}" start="0">
    <#list response.documents as document>
        <doc>
            <#list document.singleValuedFields as field>
                <@outputField name=field.name type=field.type value=field.value />
            </#list>
            <#list document.multiValuedFields as field>
                <arr name="${field.name}">
                    <#list field.values as value>
                        <@outputField type=field.type value=value />
                    </#list>
                </arr>
            </#list>
        </doc>
    </#list>
</result>
</response>
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
    <#if name??>
        <#assign printName="name=\"${name}\"">
    <#else>
        <#assign printName="">
    </#if>
            <${responseKey} ${printName}>${value}</${responseKey}>
</#macro>