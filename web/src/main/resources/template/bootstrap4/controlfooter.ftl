<#if parameters.labelposition?exists && parameters.labelposition.equalsIgnoreCase('right') >
    <#include "/${parameters.templateDir}/${parameters.theme}/controllabel.ftl" />
</#if>
<#if (parameters.includeGroup)!true == false><#elseif (parameters.cssClass!'')?contains("col-")><#else>
</div>
</#if>