<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/list-macros.ftl" as rlist>
    <#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#import "/WEB-INF/macros/search/search-macros.ftl" as search>
    <#import "/WEB-INF/macros/resource/common.ftl" as common>
    <#import "dashboard-common.ftl" as dash />
    <#import "/${themeDir}/settings.ftl" as settings>

<head>
    <title>${authenticatedUser.properName}'s Dashboard</title>
    <meta name="lastModifiedDate" content="$Date$"/>
    <@edit.resourceDataTableJavascript />
</head>

<div id="titlebar" parse="true">
    <h1>Dashboard &raquo; <span class="red">Manage Rights &amp; Permissions</span></h1>
</div>
<div class="row">
    <div class="span2">
    <@dash.sidebar current="manage" />
    </div>
    <div class="span10">
        <@shareSection />
    </div>

</div>

    <#macro repeat num val>
        <#if (num > 0)>
            <@repeat (num-1) val /><#noescape>${val}</#noescape>
        </#if>
    </#macro>



<#macro shareSection>
    <form class="form-horizontal" method="POST" action="/share/adhoc">
    <div class="well">
    <div class="row">
        <div class="span8">
            <h4>Share:</h4>
        
            <div class="control-group">
                <label class="control-label">What to Share</label>
                <div class="controls" id="divWhat">
                    <select name="type" id="what" >
                        <option value="RESOURCE" data-target="#divResourceLookup" data-toggle="tab">A resource</option>
                        <option value="SHARE" data-target="#divCollectionLookup" data-toggle="tab">Resources in a list </option>
                        <option value="ACCOUNT" data-target="#divAccountLookup" data-toggle="tab">Resources in an account </option>
                    </select>
                </div>
            </div>
            <div class="control-group">
                        <div class="controls" id="toggleControls">

                            <div id="divResourceLookup">

                                <input type="text" id="txtShareResourceName" placeholder="Resource name" class="input-xxlarge resourceAutoComplete"
                                       autocompleteIdElement="#hdnShareResourceId"
                                       autocompleteName="title"
                                       autocompleteParentElement="#divResourceLookup">
                                <input type="hidden" id="hdnShareResourceId" name="share.resourceIds">
                            </div>

                            <div id="divCollectionLookup">
                                <@s.textfield theme="simple" name="shareCollectionName" cssClass="input-xxlarge collectionAutoComplete"  autocomplete="off"
                                autocompleteIdElement="#hdnSourceCollectionId" maxlength=255 autocompleteParentElement="#divCollectionLookup" autocompleteName="name"
                                placeholder="List name" id="txtShareCollectionName"

                                />
                                <input type="hidden" id="hdnSourceCollectionId" name="share.collectionId">
                            </div>

                            <div id="divAccountLookup">

                                <@s.select name="share.accountId" list="billingAccounts" listKey="id" listValue="name"  emptyOption="true" theme="tdar" />

                            </div>


                        </div>
            </div>
        </div>
        </div>
        <div class="row">
            <div class="span4">
                    <div class="control-group">
                         <label class="control-label" for="inputEmail">With:</label>
                         <div  class="controls" id="divUserLookup">
                             <input type="text" name="share.email" id="txtShareEmail" placeholder="Email"
                                    autocompleteIdElement="#hdnShareUserId"
                                    autocompleteParentElement="#divUserLookup"
                                    autocompleteName="email">
                             <input type="hidden" name="shareUserId" id="hdnShareUserId">
                         </div>
                </div>
            </div>
            
              <div class="span4">
                  <div class="control-group">
                        <label class="control-label" for="inputPassword">Permission:</label>
                        <div class="controls">
                            <select name="authorizedUsers[0].generalPermission" id="metadataForm_authorizedUsers_0__generalPermission" class="creator-rights-select span3">
                                <option value="VIEW_ALL" selected="selected">View and Download</option>
                                <option value="MODIFY_METADATA">Modify Metadata</option>
                                <option value="MODIFY_RECORD">Modify Files &amp; Metadata</option>
                                <option value="ADMINISTER_GROUP">Add/Remove Items from Collection</option>
                            </select>
                        </div>
                  </div>
              </div>
         </div>
         <div class="row">
            <div class="span5">
                  <div class="control-group">
                    <label class="control-label" for="inputPassword">Until:</label>
                    <div class="controls">
                        <div class="input-append">
                          <input class="span2 datepicker" size="16" type="text" value="12-02-2016" id="dp3" data-date-format="mm-dd-yyyy" >
                          <span class="add-on"><i class="icon-th"></i></span>
                        </div>
                    </div>
                 </div>
           </div>
           <div class="span3">
              <input type="submit" class="btn tdar-button btn-primary" value="Submit">
           </div>
    
        </div>
    </div>
</form>

</#macro>

<@edit.personAutocompleteTemplate />

<div id="customIncludes" parse="true">
    <script src="/js/tdar.manage.js"></script>
<script>
$(function() {
    TDAR.manage.init();
})
</script>
</div>


</#escape>
