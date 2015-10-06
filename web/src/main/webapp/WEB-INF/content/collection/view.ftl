<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#import "/WEB-INF/macros/resource/common.ftl" as common>
    <#import "/WEB-INF/macros/resource/list-macros.ftl" as list>
    <#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
    <#import "/WEB-INF/macros/search/search-macros.ftl" as search>

<head>
    <@search.headerLinks includeRss=false />
    <title>${resourceCollection.name!"untitled collection"}</title>
    <@view.canonical resourceCollection />
    <#assign rssUrl = "/search/rss?groups[0].fieldTypes[0]=COLLECTION&groups[0].collections[0].id=${resourceCollection.id?c}&groups[0].collections[0].name=${(resourceCollection.name!'untitled')?url}">
    <@search.rssUrlTag url=rssUrl />
</head>
<body>

<div id="divSearchContext" parse="true">
    <input id="cbctxid" type="checkbox" name="collectionId" value="${id?c}">
    <label for="cbctxid">Search within this collection</label>
</div>

    <#if editable>
        <@nav.collectionToolbar "collection" "view">
            <@nav.makeLink
            namespace="collection"
            action="add?parentId=${id?c}"
            label="create child collection"
            name="columns"
            current=current
            includeResourceId=false
            disabled=disabled
            extraClass="hidden-tablet hidden-phone"/>
        </@nav.collectionToolbar>
    </#if>

    <@view.pageStatusCallout />
<h1>
    <#if logoAvailable>
        <img class="pull-right collection-logo" src="/files/collection/sm/${id?c}/logo"
        alt="logo" title="logo" /> 
    </#if>

${resourceCollection.name!"untitled collection"}</h1>

    <#if !visible>
    This collection is not accessible
    <#else>

        <#if !collections.empty>
        <!-- Don't show header if header doesn't exist -->
        <div id="sidebar-right" parse="true">
            <h3 class="sidebar-spacer">Child Collections</h3>
            <@common.listCollections collections=collections showOnlyVisible=true />
        </div>
        </#if>

        <#if resourceCollection.parent?? || resourceCollection.description??  || resourceCollection.formattedDescription?? || collections??>
        <div class="glide">
            <#if resourceCollection.parent??><p><b>Part of:</b>
            	<#if resourceCollection.parent.hidden && !authenticated >
					${resourceCollection.parent.name!"(n/a)"}
				<#else>
				 <a
                    href="${resourceCollection.parent.detailUrl}">${resourceCollection.parent.name!"(n/a)"}</a>
               	</#if>
			</p></#if>
            <@common.description resourceCollection.description />

            <#if resourceCollection.formattedDescription??>
                <p>
                    <#noescape>
			    ${resourceCollection.formattedDescription}
			  </#noescape>
                </p>
            </#if>

        </div>
        </#if>
        <#if  results?has_content && results?size !=0 >
        <br/>

        <div id="divResultsSortControl">
            <div class="row">
                <div class="span4">
                    <@search.totalRecordsSection tag="h2" helper=paginationHelper itemType="Record"/>
                </div>
                <div class="span5"></div>
            </div>
        </div>


        <div class="collection-facets">
            <#assign mapSize="450" />
            <#if (totalRecords > 10)>
                <#assign mapSize="700" />
            </#if>
            <#if (totalRecords > 18)>
                <#assign mapSize="1000" />
            </#if>
            <#if selectedResourceTypes.empty>
                <@search.facetBy facetlist=resourceTypeFacets currentValues=selectedResourceTypes label="" facetParam="selectedResourceTypes" />
            <#else>
            <h4>
               There <#if paginationHelper.totalNumberOfItems == 1>is<#else>are</#if> ${paginationHelper.totalNumberOfItems?c}

            <#if selectedResourceTypes?has_content>
                <#if paginationHelper.totalNumberOfItems == 1>
                    <@s.text name="${resourceTypeFacets[0].key}" />
                <#else>
                    <@s.text name="${resourceTypeFacets[0].pluralKey}" />
                </#if>
            <#else>
                <#if paginationHelper.totalNumberOfItems == 1>Resource<#else>Resources</#if>
            </#if> within this Collection <#if selectedResourceTypes?has_content>                <sup><a style="text-decoration: "
                                                                                                      href="<@s.url includeParams="all">
			            <@s.param name="selectedResourceTypes"value="" />
			            <@s.param name="startRecord" value=""/>
			</@s.url>">[remove this filter]</a></sup>
            </#if>
            </h4>
            </#if>
        </div>

        <div class="tdarresults">
            <#assign itemsPerRow = 4/>
		<#assign mapPosition="top"/>
		<#if collections.empty>
            <#assign itemsPerRow = 5 />
            <#assign mapPosition="left"/>
        </#if>
		    <@list.listResources resourcelist=results sortfield=sortField titleTag="h5" listTag="ul" itemTag="li" itemsPerRow=itemsPerRow
        orientation=resourceCollection.orientation    mapPosition=mapPosition mapHeight=mapSize />
        </div>

            <@search.basicPagination "Records" />
        <#else>
        <hr/>
        This collection is either empty or you do not currently have permissions to view the contents.
        </#if>
        <#if editable>
        <h3>Administrative Information</h3>

            <@common.resourceUsageInfo />
        <div class="row">
            <div class="span4">
                <@view.kvp key="Collection Type" val=resourceCollection.type.label />
            </div>
            <div class="span4">
                <@view.kvp key="Hidden" val=resourceCollection.hidden?string />
            </div>
        </div>
        <div class="row">
            <div class="span4">
                <@view.kvp key="Sort By" val=resourceCollection.sortBy.label />
            </div>
            <div class="span4">
                <#assign viewed>${viewCount} times</#assign>
                <@view.kvp key="Viewed" val=viewed />
            </div>
        </div>
        <div class="row">
            <div class="span4">
                <@view.kvp key="Created By" nested=true><a
                        href="<@s.url value="${resourceCollection.owner.detailUrl}"/>">${resourceCollection.owner.properName}</a>
                    on ${resourceCollection.dateCreated?datetime}</@view.kvp>
            </div>
            <div class="span4">
                <@view.kvp key="Updated By" nested=true><a
                        href="<@s.url value="${resourceCollection.updater.detailUrl}"/>">${resourceCollection.updater.properName}</a>
                    on ${resourceCollection.dateUpdated?datetime}</@view.kvp>
            </div>
        </div>

            <@_authorizedUsers resourceCollection />
        </#if>
    </#if>

<script type='text/javascript'>
    $(document).ready(function () {
        TDAR.common.initializeView();
        TDAR.common.collectionTreeview();
    });
</script>


</body>

    <#macro _authorizedUsers collection >
        <@common.resourceCollectionsRights collections=collection.hierarchicalResourceCollections />
    </#macro>

</#escape>