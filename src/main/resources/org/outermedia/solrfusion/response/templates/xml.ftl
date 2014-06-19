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
            <#list document.fields as field>
                <#if field.multiValued >
                    <arr name="${field.name}">
                        <#list field.subfields as subfield>
                            <@outputField field=subfield />
                        </#list>
                    </arr>
                <#else>
                    <@outputField field=field />
                </#if>
            </#list>
        </doc>
    </#list>
</result>
</response>
<#macro outputField field>
    <#switch field.type>
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
    <#if field.name??>
        <#assign printName="name=\"${field.name}\"">
    <#else>
        <#assign printName="">
    </#if>
            <${responseKey} ${printName}>${field.value}</${responseKey}>
</#macro>