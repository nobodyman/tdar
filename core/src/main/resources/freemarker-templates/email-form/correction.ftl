<#import "../email-macro.ftl" as mail /> 

<@mail.content>
Dear ${to.properName},

<p>
    ${from.properName} wants to suggest an edit or correction to the resource:
     <a href="${baseUrl}${resource.detailUrl}">${resource.title}</a> (${resource.id?c}) that you have administrative rights 
     to in tDAR.
</p>
<blockquote>
${message}
</blockquote>

<p>
You may correspond with ${from.properName} via ${from.email}.  To make 
edits to your ${siteAcronym} resource, log in to ${siteAcronym} and visit 
<a href="${baseUrl}${resource.detailUrl}">
${baseUrl}${resource.detailUrl}</a>.  
Select the edit tab at the top of the page, make any changes, and press save. 
</p>
<p>
Kind regards,<br />
<br />
Staff at ${serviceProvider}
</p>
<p>
<hr>
Note: please do not reply to this automated email
</p>
</@mail.content>