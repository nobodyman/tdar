<#import "email-macro.ftl" as mail /> 


<@mail.content>

The following ${totalEmails} user-generated emails need to be reviewed before they are sent.  Review online ( ${siteUrl}/admin/email ) here to review and change status in ${siteAcronym}.<br />
<br />
<ul>
    <#list emails as email>
     <li> ${email.date?string.short} ${email.subject} from:${email.from} to: ${email.to}</li>
    </#list>
</ul>
</@mail.content>
