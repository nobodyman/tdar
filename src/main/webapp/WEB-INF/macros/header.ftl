<#import "resource/common.ftl" as common>

<#macro scripts combine=false>
<!--[if lte IE 8]><script language="javascript" type="text/javascript" src="<@s.url value="/includes/jqplot-1.08/excanvas.js"/>"></script><![endif]--> 

    <#if !production>
        <script type="text/javascript" src="/js/tdar.test.js"></script>
    </#if>

<#if combine>
    <script type="text/javascript" src="/wro/default.js"></script>
<#else>
    <#list javascriptFiles as src>
      <script type="text/javascript" src="${src}"></script>
    </#list>
</#if>

</#macro>


<#macro css combine=true>
<#if combine>
<!-- call to http://code.google.com/p/webutilities/wiki/JSCSSMergeServlet#URLs_in_CSS -->
    <link rel="stylesheet" type="text/css" href="/wro/default.css"/>
<#else>
<#list cssFiles as src>
  <link rel="stylesheet" type="text/css" href="${src}" data-version="${common.tdarBuildId}">
</#list>

</#if>


</#macro>