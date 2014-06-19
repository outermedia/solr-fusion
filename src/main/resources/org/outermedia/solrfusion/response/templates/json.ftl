{
  "responseHeader":{
    "status":0,
    "QTime":0,
    "params":{
      "indent":"on",
      "start":"0",
      "q":"${responseHeader.query}",
      "wt":"json",
      "version":"2.2",
      "rows":"${responseHeader.rows}"}},
  "response":{"numFound":${response.totalHitNumber},"start":0,"docs":[
  <#list response.documents as document>
  {
    <#list document.fields as field>
    "${field.name}":<#rt>
        <#if field.multiValued >
            [<#lt>
                <#list field.subfields as subfield>
                    <@outputField field=subfield /><#if subfield_has_next>,</#if>
                </#list>
            ]<#rt>
        <#else>
            <@outputField field=field /><#t>
        </#if>
        <#if field_has_next>,</#if><#lt>
    </#list>
  }<#if document_has_next>,</#if>
  </#list>
  ]}
}
<#macro outputField field>
    <#switch field.type>
        <#case "float">
        <#case "double">
        <#case "int">
        <#case "long">
            ${field.value}<#t>
        <#case "boolean">
            ${field.value}<#t>
        <#case "date">
        <#case "text">
            "${field.value}"<#t>
    </#switch>
</#macro>