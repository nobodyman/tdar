<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<#import "/WEB-INF/macros/resource/list-macros.ftl" as list>
<#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
<#import "/WEB-INF/macros/search/search-macros.ftl" as search>
<@search.initResultPagination/>
 <@search.headerLinks includeRss=false />


<@nav.creatorToolbar "view" />

<title><#if creator?? && creator.properName??>${creator.properName}<#else>No title</#if></title>
<#if creator??>
    <#if creator.institution??>
    <a href="<@s.url value="${creator.institution.id?c}"/>">${creator.institution}</a>
    <br/>
    </#if>
    
    <p>${creator.description!''}</p>
    <br/>
        <#if creator.creatorType == 'PERSON'>
           <#if authenticated && (editor ||  id == authenticatedUser.id ) >
                <table class='tableFormat'>
                <tr>
                    <td>
                        <#if RPAEnabled && creator.rpaNumber??>
                            <B>Registered Public Archaeologist</B>:${creator.rpaNumber}
                        </#if>
                    </td><td>
                    <#if creator.registered && (editor || id == authenticatedUser.id)>
                            <#if creator.lastLogin??>
                                        <@view.datefield "Last Login"  creator.lastLogin />
                            <#else>
                                        <@view.textfield "Last Login"  "No record" />
                            </#if>                    
                    <#else>
                        <@view.boolean "Registered User" creator.registered />
                        </#if>
                    </td>
                </tr>
                <tr>
                    <#if creator.emailPublic || (editor || id == authenticatedUser.id) >
                        <td>
                            <@view.textfield "Email" creator.email />
                        </td>
                    <#else>
                        <td>
                            <@view.textfield "Email" "Not Shown" />
                        </td>
                    </#if>
                    <#if creator.phonePublic || (editor || id == authenticatedUser.id)>
                        <td>
                            <@view.textfield "Phone" creator.phone true />
                        </td>
                    <#else>
                        <td>
                            <@view.textfield "Phone" "Not Shown" />
                        </td>
                    </#if>
                </tr>
                <tr>
                    <td colspan=2>
                    <#escape x as x?html>
                    <@view.textfield "Contributor Reason" creator.contributorReason true />
                    </#escape>
                </td>
                </tr>
                </table>
            </#if>
        
        </#if>
<br/>    
</#if>

<@search.basicPagination "Records"/>

<div class="glide">
<#if results??>
<@list.listResources results "RESOURCE_TYPE" />
</#if>
</div>
</#escape>