{
  "responseHeader":{
    "status":0,
    "QTime":${responseHeader.queryTime?c},
    "params":{
      "indent":"on",<#list responseHeader.queryParams?keys as key>
      "${key}":"${responseHeader.queryParams[key]?json_string}",</#list><#list responseHeader.multiValueQueryParams?keys as key>
      "${key}":[
        <#list responseHeader.multiValueQueryParams[key] as v>"${v?json_string}"<#if v_has_next>,</#if></#list>
      ],</#list>
      "wt":"json",
      "version":"2.2"}}
  <#if responseError.error>
  , "error":{
      "msg":"${responseError.msg?json_string}",
      "code":${responseError.code}},
  </#if>
  <#if response.matchDocuments?has_content>
  , "match":{"numFound":${response.totalMatchHitNumber?c},"start":0,"docs":[
    <#list response.matchDocuments as document>
    {
      <@outputMultiValueFields fields=document.multiValuedFields />
      <#if document.hasMultiValuedFields && document.hasSingleValuedFields >,
      </#if>
      <@outputSingleValueFields fields=document.singleValuedFields />
    }<#if document_has_next>,</#if>
    </#list>
    ]}
  </#if>
  , "response":{"numFound":${response.totalHitNumber?c},"start":0,"docs":[
  <#list response.documents as document>
  {
    <@outputMultiValueFields fields=document.multiValuedFields />
    <#if document.hasMultiValuedFields && document.hasSingleValuedFields >,
    </#if>
    <@outputSingleValueFields fields=document.singleValuedFields />
  }<#if document_has_next>,</#if>
  </#list>
  ]}
  <#if facets.hasFacets>
  ,"facet_counts":{
    "facet_queries":{},
    "facet_fields":{
  <#list facets.facets?keys as field>
      "${field}": [
    <#list facets.facets[field] as wc>
        ["${wc.word?json_string}", ${wc.count?c}]<#if wc_has_next>,</#if>
    </#list>
      ]<#if field_has_next>,</#if>
  </#list>
  },
  "facet_dates":{},
  "facet_ranges":{}}
  </#if>
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