{
  "responseHeader":{
    "status":0,
    "QTime":0,
    "params":{
      "indent":"on",
      "start":"0",
      "q":"${responseHeader.query?json_string}",
    <#if responseHeader.filterQuery??>
      "fq":"${responseHeader.filterQuery?json_string}",
    </#if>
    <#if responseHeader.sort??>
      "sort":"${responseHeader.sort?json_string}",
    </#if>
    <#if responseHeader.fields??>
      "fl":"${responseHeader.fields?json_string}",
    </#if>
    <#if responseHeader.highlight??>
      "hl":"${responseHeader.highlight?json_string}",
    </#if>
    <#if responseHeader.highlightPre??>
      "hl.simple.pre":"${responseHeader.highlightPre?json_string}",
    </#if>
    <#if responseHeader.highlightPost??>
      "hl.simple.post":"${responseHeader.highlightPost?json_string}",
    </#if>
    <#if responseHeader.highlightFields??>
      "hl.fl":"${responseHeader.highlightFields?json_string}",
    </#if>
    <#if responseHeader.highlightQuery??>
      "hl.q":"${responseHeader.highlightQuery?json_string}",
    </#if>
      "wt":"json",
      "version":"2.2",
      "rows":"${responseHeader.rows}"}},
  <#if responseError.error>
  "error":{
      "msg":"${responseError.msg?json_string}",
      "code":${responseError.code}},
  </#if>
  "response":{"numFound":${response.totalHitNumber?string("0")},"start":0,"docs":[
  <#list response.documents as document>
  {
    <@outputMultiValueFields fields=document.multiValuedFields />
    <#if document.hasMultiValuedFields && document.hasSingleValuedFields >,
    </#if>
    <@outputSingleValueFields fields=document.singleValuedFields />
  }<#if document_has_next>,</#if>
  </#list>
  ]}
  <#if highlighting.hasHighlights>
  , "highlighting":{<#list highlighting.highlighting as doc>
    "${doc.id}": {
    <@outputMultiValueFields fields=doc.multiValuedFields />
    }<#if doc_has_next>,</#if></#list>
  }
  </#if>
}
<#macro outputMultiValueFields fields>
    <#if fields??>
    <#list fields as field>
    "${field.name}":<#rt>
            [<#lt>
                <#list field.values as value>
                    <@outputField type=field.type value=value /><#if value_has_next>,</#if>
                </#list>
            ]<#rt>
        <#if field_has_next>,</#if><#lt>
    </#list>
    </#if>
</#macro>
<#macro outputSingleValueFields fields>
    <#if fields??>
    <#list fields as field>
    "${field.name}":<#rt>
            <@outputField type=field.type value=field.value /><#t>
        <#if field_has_next>,</#if><#lt>
    </#list>
    </#if>
</#macro>
<#macro outputField type value>
    <#switch type>
        <#case "float">
        <#case "double">
        <#case "int">
        <#case "long">
            ${value}<#t>
            <#break>
        <#case "boolean">
            ${value}<#t>
            <#break>
        <#case "date">
        <#case "text">
            "${value?json_string}"<#t>
            <#break>
    </#switch>
</#macro>