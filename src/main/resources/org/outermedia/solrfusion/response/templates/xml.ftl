<?xml version="1.0" encoding="UTF-8"?>
<response>
<lst name="responseHeader">
  <int name="status">0</int>
  <int name="QTime">0</int>
  <lst name="params">
    <str name="indent">on</str>
    <str name="start">0</str>
    <str name="q"><![CDATA[${responseHeader.query}]]></str>
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
        <#case "int">
        <#case "long">
            <#assign responseKey="int">
        <#case "boolean">
            <#assign responseKey="bool">
        <#case "date">
            <#assign responseKey="date">
        <#default>
            <#assign responseKey="str">
    </#switch>
    <#if name??>
        <#assign printName="name=\"${name}\"">
    <#else>
        <#assign printName="">
    </#if>
            <${responseKey} ${printName}>${value}</${responseKey}>
</#macro>