{
  "responseHeader":{
    "status":0,
    "QTime":0,
    "params":{
      "indent":"on",
      "start":"0",
      "q":"${responseHeader.query}",
    <#if responseHeader.filterQuery??>
      "fq":${responseHeader.filterQuery},
    </#if>
      "wt":"json",
      "version":"2.2",
      "rows":"${responseHeader.rows}"}},
  "response":{"numFound":${response.totalHitNumber?string("0")},"start":0,"docs":[
  <#list response.documents as document>
  {
    <#list document.multiValuedFields as field>
    "${field.name}":<#rt>
            [<#lt>
                <#list field.values as value>
                    <@outputField type=field.type value=value /><#if value_has_next>,</#if>
                </#list>
            ]<#rt>
        <#if field_has_next>,</#if><#lt>
    </#list>
    <#if document.hasMultiValuedFields && document.hasSingleValuedFields >,
    </#if>
    <#list document.singleValuedFields as field>
    "${field.name}":<#rt>
            <@outputField type=field.type value=field.value /><#t>
        <#if field_has_next>,</#if><#lt>
    </#list>
  }<#if document_has_next>,</#if>
  </#list>
  ]}
}
<#macro outputField type value>
    <#switch type>
        <#case "float">
        <#case "double">
        <#case "int">
        <#case "long">
            ${value?string("0")}<#t>
        <#case "boolean">
            ${value}<#t>
        <#case "date">
        <#case "text">
            "${value}"<#t>
    </#switch>
</#macro>