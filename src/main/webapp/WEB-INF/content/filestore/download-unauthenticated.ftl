<#escape _untrusted as _untrusted?html>
<#assign download ="/filestore/${downloadRegistration.version.id?c}" />
<#import "/WEB-INF/macros/common-auth.ftl" as auth>

<html>
<head>
    <title>Download: ${downloadRegistration.version.fileName!"undefined"?html}</title>
</head>
<body>
<div class="hero-unit">
    <h1>Welcome Back</h1>

    <p>The download you requested will begin momentarily</p>
    <dl class="dl-horizontal">
        <dt>Requested File</dt>
        <dd><a href="${download!""}" class="manual-download">${downloadRegistration.version.filename!"undefined"?html}</a></dd>
    </dl>
    <p>
        You've reached this page because you requested a file download when you were not logged into ${siteAcronym}. If your download does not begin
        automatically,
        or if you would like to download the file again, please click on the link above.
    </p>
</div>

    <div class="row">
        <div class="span9" id="divRegistrationSection">
            <@s.form name='registrationForm' id='registrationForm' method="post" cssClass="disableFormNavigate"
                    enctype='multipart/form-data' action="/account/process-download-registration">
                <@s.token name='struts.csrf.token' />
                <@commonFields />
                <fieldset>
                    <legend>Register</legend>
                    <@auth.registrationFormFields detail="minimal" cols=9 beanPrefix="downloadRegistration" />
                </fieldset>
            </@s.form>

        </div>

        <div class="span3" id="divLoginSection">
            <@s.form name='loginForm' id='loginForm'  method="post" cssClass="disableFormNavigate"
                    enctype='multipart/form-data' action="/login/process-download-login">
                    <@commonFields />
                <@auth.login showLegend=true>
                    <div class="form-actions">
                        <input type="submit" name="submit" class="btn btn-large" value="Login and Continue">
                    </div>
                </@auth.login>
            </@s.form>

        </div>
    </div>
    <#macro commonFields>
        <@s.hidden name="downloadRegistration.version.id" />
        <@s.hidden name="downloadRegistration.version.filename"/>
        <@s.hidden name="downloadRegistration.resource.id"/>
        <@s.hidden name="downloadRegistration.resource.title"/>
        <@s.hidden name="downloadRegistration.resource.description"/>
        <@s.hidden name="downloadRegistration.resource.resourceType"/>
    </#macro>
</body>
</#escape>