<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/list-macros.ftl" as rlist>
    <#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#import "/WEB-INF/macros/search/search-macros.ftl" as search>
    <#import "/WEB-INF/macros/resource/common.ftl" as common>
    <#import "/${themeDir}/settings.ftl" as settings>

<head>
    <title>${authenticatedUser.properName}'s Dashboard</title>
    <meta name="lastModifiedDate" content="$Date$"/>


    <@edit.resourceDataTableJavascript />

</head>

<div id="titlebar" parse="true">
    <h1>${authenticatedUser.properName}'s Dashboard</h1>

        <@headerNotifications />
</div>

<div id="sidebar-right" parse="true">
    <div>
        <#if contributor>
            <#if (activeResourceCount != 0)>
                <@resourcePieChart />
                <hr/>
            </#if>
        <#else>
            <div id="myCarousel" class="carousel slide" data-interval="5000">
                <#assign showBuy = (!accounts?has_content) />
              <!-- Carousel items -->
              <div class="carousel-inner">
                <div class="active item">
                    <img class="" src="#" width=120 height=150 alt="Read the Manual"/>
                        Read the Manual
                </div>
                    <#if (showBuy)>
                    <div class="item">
                        <img class="" src="#" width=120 height=150 alt="Purchase Space"/>
                            Buy tDAR now
                    </div>
                    </#if>
                <div class="item">
                        <img class="" src="#" width=120 height=150 alt="Explore"/>
                            Explore Content now
                </div>
              </div>
              <div class="clearfix centered">
              <ol class="carousel-indicators" >
                <li data-target="#myCarousel" data-slide-to="0" class="active"></li>
                <#if showBuy><li data-target="#myCarousel" data-slide-to="1"></li></#if>
                <li data-target="#myCarousel" data-slide-to="2"></li>
              </ol>
              </div>

        </div>
        </#if>
        <@collectionsSection />
    </div>
</div>

<div class="row">
    <div class="span9">
        Welcome <#if authenticated.penultimateLogin?has_content>back,</#if> ${authenticatedUser.firstName}!
        <#if contributor>
            The resources you can access are listed below. To create a <a href="<@s.url value="/resource/add"/>">new resource</a> or
            <a href="<@s.url value="/project/add"/>">project</a>, or <a href="<@s.url value="/collection/add"/>">collection</a>, click on the "upload" button
            above.
        <p><strong>Jump To:</strong><a href="#project-list">Browse Resources</a> | <a href="#collection-section">Collections</a> | <a href="#divAccountInfo">Profile</a>
            <#if payPerIngestEnabled>| <a href="#billing">Billing Accounts</a></#if>
            | <a href="#boomkarks">Bookmarks</a>
        </p>
        <hr/>
        </#if>
    </div>
</div>


    <#if contributor>
        <#if (activeResourceCount == 0)>
            <@gettingStarted />
        <hr/>
        <#else>
            <@recentlyUpdatedSection />
        </#if>

        <@emptyProjectsSection />
        <@browseResourceSection />
    <#else>
    <@searchSection />
    <#if featuredResources?has_content  >
    <hr/>
    <div class="row">
        <@view.featured span="span9" header="Featured and Recent Content"/>
    </div>
    </#if>
    </#if>
<hr/>
    <@accountSection />
<hr/>

    <@bookmarksSection />


<#macro searchSection>
    <div class="row">
        <div class="span9">
            <form name="searchheader" action="<@s.url value="/search/results"/>">
                <input type="text" name="query" class="searchbox" placeholder="Search ${siteAcronym} &hellip; ">
                <@s.checkboxlist id="includedResourceTypes" numColumns=4 spanClass="span2" name='resourceTypes' list='resourceTypes'  listValue='label' label="Resource Type"/>
                <@s.submit value="Search" cssClass="btn btn-primary" />
            </form>
        </div>    
    </div>

</#macro>




    <#macro gettingStarted>
    <div class="row">
        <div class="span9">
            <h2>Getting Started</h2>
            <ol style='list-style-position:inside'>
                <li><a href="<@s.url value="/project/add"/>">Start a new Project</a></li>
                <li><a href="<@s.url value="/resource/add"/>">Add a new Resource</a></li>
            </ol>
        </div>
    </div>
    </#macro>

    <#macro resourcePieChart>
    <div class="row">
        <div class="span3">
            <h2>At a glance</h2>

            <div class="piechart row">
                <@common.generatePieJson statusCountForUser "statusForUser" />
	            <@common.barGraph  data="statusForUser" searchKey="includedStatuses" graphHeight=150 context=true graphLabel="Resources By Status"/>
            </div>
            <div class="piechart row">
                <@common.generatePieJson resourceCountForUser "resourceCountForUser" />
                <script>
                    var pcconfig = {
                        legend: { show: true, location: 's', rendererOptions: {numberColumns: 3} }
                    };
                </script>
                <@common.pieChart  data="resourceCountForUser" searchKey="resourceTypes" graphHeight=280 context=true config="pcconfig" graphLabel="Resources By Type"/>
            </div>
        </div>
    </div>

    </#macro>

    <#macro recentlyUpdatedSection>

    <div class="row">
        <div class="span9">
            <h2><@s.text name="dashboard.recently_updated"/></h2>
            <ol id='recentlyEditedResources'>

                <#list recentlyEditedResources as res>
                    <li id="li-recent-resource-${res.id?c}">
	               <span class="fixed">
                       <@common.cartouche res true>
                           <span class="recent-nav">
	                    <a href="<@s.url value='/${res.urlNamespace}/edit'><@s.param name="id" value="${res.id?c}"/></@s.url>"><@s.text name="menu.edit" /></a> |
	                    <a href="<@s.url value='/${res.urlNamespace}/delete'><@s.param name="id" value="${res.id?c}"/></@s.url>"><@s.text name="menu.delete" /></a>
	                </span>
	                        <a href="<@s.url value='/${res.urlNamespace}/view'
                            ><@s.param name="id" value="${res.id?c}"/></@s.url>"><@common.truncate res.title 60 /></a>
                            <small>(ID: ${res.id?c})</small>
                       </@common.cartouche>
                   </span>
                    </li>
                </#list>
            </ol>
        </div>
    </div>


    </#macro>

    <#macro emptyProjectsSection>
        <#if (emptyProjects?? && !emptyProjects.empty )>
        <div class="row">
            <div class="span9" id="divEmptyProjects">
                <h2>Empty Projects</h2>
                <ol id="emptyProjects">
                    <#list emptyProjects as res>
                        <li id="li-recent-resource-${res.id?c}">
                            <a href="<@s.url value='/${res.urlNamespace}/view'><@s.param name="id" value="${res.id?c}"/></@s.url>">
                                <@common.truncate res.title 60 />
                            </a>
                            <small>(ID: ${res.id?c})</small>

                            <div class="recent-nav pull-right">
                                <a href="<@s.url value='/resource/add?projectId=${res.id?c}'><@s.param name="id" value="${res.id?c}"/></@s.url>"
                                   title="add a resource to this project">add resource</a> |
                                <a href="<@s.url value='/${res.urlNamespace}/edit'><@s.param name="id" value="${res.id?c}"/></@s.url>"><@s.text name="menu.edit" /></a>
                                |
                                <a href="<@s.url value='/${res.urlNamespace}/delete'><@s.param name="id" value="${res.id?c}"/></@s.url>"><@s.text name="menu.delete" /></a>
                            </div>
                        </li>
                    </#list>
                </ol>
            </div>
        </div>
        <hr/>
        </#if>
    </#macro>


    <#macro browseResourceSection>
        <@common.reindexingNote />
    <div class="" id="project-list">
        <h2>Browse Resources</h2>

        <div>
            <@edit.resourceDataTable />
        </div>
    </div>
    </#macro>

    <#macro repeat num val>
        <#if (num > 0)>
            <@repeat (num-1) val /><#noescape>${val}</#noescape>
        </#if>
    </#macro>

    <#macro collectionsSection>

    <div class="" id="collection-section">
        <h2>Collections</h2>
        <@common.listCollections collections=allResourceCollections>
            <li><a href="<@s.url value="/collection/add"/>">create one</a></li>
        </@common.listCollections>
    </div>
    <br/>
        <#if sharedResourceCollections?? && !sharedResourceCollections.empty >
        <div class="">
            <h2>Collections Shared With You</h2>
            <@common.listCollections collections=sharedResourceCollections />
        </div>
        </#if>

    </#macro>

    <#macro accountSection>
    <div id="accountSection" class="row">
        <div id="divAccountInfo" class="<#if payPerIngestEnabled>span4<#else>span9</#if>">
            <h2>About ${authenticatedUser.firstName}</h2>
            <strong>Full Name: </strong>${authenticatedUser.properName}<#if authenticatedUser.institution??>, ${authenticatedUser.institution.name}</#if><br/>
            <#if authenticatedUser.penultimateLogin??>
                <strong>Last Login: </strong>${authenticatedUser.penultimateLogin?datetime}<br/>
            </#if>
            <a href="<@s.url value='/entity/person/edit?id=${sessionData.tdarUser.id?c}'/>">edit your profile</a>
        </div>

        <div class="span5" id="billing">
            <@common.billingAccountList accounts />
        </div>
    </div>
    </#macro>


    <#macro bookmarksSection>
    <div class="row">
        <div class="span9" id="bookmarks">
            <#if ( bookmarkedResources?size > 0)>
            <h2 >Bookmarks</h2>
                <@rlist.listResources resourcelist=bookmarkedResources sortfield='RESOURCE_TYPE' listTag='ol' headerTag="h3" />
            <#else>
            <h3>Bookmarked resources appear in this section</h3>
            Bookmarks are a quick and useful way to access resources from your dashboard. To bookmark a resource, click on the star <i class="icon-star"></i> icon next to any resource's title.
            </#if>
        </div>
    </div>
    </#macro>

<script>
    $(document).ready(function () {
        TDAR.notifications.init();
        TDAR.common.collectionTreeview();
    });
</script>



<#macro headerNotifications>
    <#list currentNotifications as notification>
        <div class="${notification.messageType} alert" id="note_${notification.id?c}">
        <button type="button" id="close_note_${notification.id?c}" class="close" data-dismiss="alert" data-dismiss-id="${notification.id?c}" >&times;</button>
        <#if notification.messageDisplayType.normal>
        <@s.text name="${notification.messageKey}"/> [${notification.dateCreated?date?string.short}]
        <#else>
            <#local file = "../notifications/${notification.messageKey}.ftl" />
            <#if !notification.messageKey?string?contains("..") >
                <#attempt>
                    <#include file />
                <#recover>
                    Could not load notification.
                </#attempt>
            </#if>
        </#if>
        </div>
    </#list>

    <#if resourcesWithErrors?has_content>
    <div class="alert-error alert">
        <h3><@s.text name="dashboard.archiving_heading"/></h3>

        <p><@common.localText "dashboard.archiving_errors", serviceProvider, serviceProvider /> </p>
        <ul>
            <#list resourcesWithErrors as resource>
                <li>
                    <a href="<@s.url value="/${resource.resourceType.urlNamespace}/${resource.id?c}" />">${resource.title}:
                        <#list resource.filesWithProcessingErrors as file><#if file_index !=0>,</#if>${file.filename!"unknown"}</#list>
                    </a>
                </li>
            </#list>
        </ul>
    </div>
    </#if>


    <#if overdrawnAccounts?has_content>
    <div class="alert-error alert">
        <h3><@s.text name="dashboard.overdrawn_title"/></h3>

        <p><@s.text name="dashboard.overdrawn_description" />
            <a href="<@s.url value="/cart/add"/>"><@s.text name="dashboard.overdrawn_purchase_link_text" /></a>
        </p>
        <ul>
            <#list overdrawnAccounts as account>
                <li>
                    <a href="<@s.url value="/billing/${account.id?c}" />">${account.name!"unamed"}</a>
                </li>
            </#list>
        </ul>
    </div>
    </#if>

<div id="messages" style="margin:2px" class="hidden lt-ie8">
    <div id="message-ie-obsolete" class="message-error">
        <@common.localText "dashboard.ie_warning", siteAcronym />
        <a href="http://www.microsoft.com/ie" target="_blank">
            <@common.localText "dashboard.ie_warning_link_text" />
        </a>.
    </div>
</div>
</#macro>

</#escape>
