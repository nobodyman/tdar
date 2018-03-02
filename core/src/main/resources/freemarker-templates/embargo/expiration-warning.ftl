<#import "../email-macro.ftl" as mail /> 
<@mail.content>
Dear ${submitter.properName},

The following files are marked as "embargoed" in ${siteAcronym}. That embargo will expire soon.
At that time, the embargo will be automatically removed.
  
<#list files as file>
	- ${file.filename}:  ${file.informationResource.title} (${file.informationResource.id?c}) - ${baseUrl}${file.informationResource.detailUrl}
</#list>
</@mail.content>