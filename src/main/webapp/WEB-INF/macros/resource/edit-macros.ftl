<#-- 
$Id$ 
Edit freemarker macros.  Getting large, should consider splitting this file up.
-->
<#-- include navigation menu in edit and view macros -->
<#escape _untrusted as _untrusted?html>
<#include "common.ftl">
<#import "/${themeDir}/settings.ftl" as settings>
<#include "navigation-macros.ftl">

<#macro basicInformation itemTypeLabel="file" itemPrefix="resource" isBulk=false>
<div class="well" id="basicInformationSection">
    <legend>Basic Information</legend>
  <#if resource.id?? &&  resource.id != -1>
      <@s.hidden name="id"  value="${resource.id?c}" />
  </#if>
  
  <@s.hidden name="startTime" value="${currentTime?c}" />

        <div id="spanStatus" tooltipcontent="#spanStatusToolTip"></div>   
        <@s.select label="Status" value="resource.status" name='status'  emptyOption='false' listValue='label' list='%{statuses}'/>
        <#if resource.resourceType.project><span class="help-block">Note: project status does not affect status of child resources.</span></#if>
    
        <#-- TODO: use bootstrap tooltips (need to decide how to toggle. click? hover?) -->
        <div id="spanStatusToolTip" class="hidden">
            <h2>Status</h2>
            <div>
                Indicates the stage of a resource's lifecycle and how ${siteAcronym} treats its content.
                <dl>
                    <dt>Draft</dt><dd>The resource is under construction and/or incomplete</dd>
                    <dt>Active</dt><dd>The resource is considered to be complete.</dd>
                    <dt>Flagged</dt><dd>This resource has been flagged for deletion or requires attention</dd>
                    <dt>Deleted</dt><dd>The item has been 'deleted' from ${siteAcronym} workspaces and search results, and is considered deprecated.</dd>  
                </dl>
                
            </div>
        </div>
<#if isBulk>

    <@s.hidden labelposition='left' id='resourceTitle' label='Title' name='image.title' cssClass="" value="BULK_TEMPLATE_TITLE"/>
    <@s.hidden labelposition='left' id='dateCreated' label='Year Created' name='image.date' cssClass="" value="-100"/>
    <@s.hidden id='ImageDescription' name='image.description' value="placeholder description"/>

<#else>
    <span
    tiplabel="Title"
    tooltipcontent="Enter the entire title, including sub-title, if appropriate."></span>
   
    <@s.textfield label="Title" id="resourceRegistrationTitle"  
        title="A title is required for all ${itemTypeLabel}s" name='${itemPrefix}.title' cssClass="required descriptiveTitle input-xxlarge" required=true maxlength="512"/>

    <#if resource.resourceType != 'PROJECT'>
    <span tiplabel="Year" tooltipcontent="Four digit year, e.g. 1966 or 2005."></span>
    <#local dateVal = ""/>
    <#if resource.date?? && resource.date != -1>
    <#local dateVal = resource.date?c />
    </#if>
    <@s.textfield label="Year" id='dateCreated' name='${itemPrefix}.date' value="${dateVal}" cssClass="reasonableDate required input-mini" required=true
      title="Please enter the year this ${itemTypeLabel} was created" />
    </#if>
</#if>
    <#nested>
</div>

</#macro>

<#macro abstractSection itemPrefix="resource">
<div class="well">
    <legend>Abstract / Description</legend>
    <span id="t-abstract" class="clear"
        tiplabel="Abstract / Description"
        tooltipcontent="Short description of the ${resource.resourceType.label}."></span>
    <@s.textarea id='resourceDescription'  name='${itemPrefix}.description' cssClass='required resizable span7' required=true title="A description is required" />
    
</div>
</#macro>

<#macro organizeResourceSection>
    <div class="well" id="organizeSection">
        <legend>${siteAcronym} Collections &amp; Project</legend>
        <h4>Add to a Collection</h4>
         <@edit.resourceCollectionSection />
        
        <div id="projectTipText" style="display:none;">
        ${settings.helptext['projectTipText']!"Select a project with which your ${resource.resourceType.label} will be associated. This is an important choice because it  will allow metadata to be inherited from the project further down this form"}
        </div>

        <#if !resource.resourceType.project>
        <h4>Choose a Project</h4>
        <div id="t-project" tooltipcontent="#projectTipText" tiplabel="Project">
            <#if resource.id != -1>
                <@s.select labelposition='left' label='Project' emptyOption='true' id='projectId' name='projectId' listKey='id' listValue='title' list='%{potentialParents}'
                truncate="70" value='project.id' required="true" title="Please select a project" cssClass="required input-xxlarge" />
            <#else>
                <@s.select labelposition='left' label='Project' title="Please select a project" emptyOption='true' id='projectId' name='projectId' listKey='id' listValue='title' list='%{potentialParents}'
                truncate="70" value="${request.getParameter('projectId')!''}"required="true" cssClass="required input-xxlarge" />
            </#if>
        </div>

        <div id="divSelectAllInheritanceTooltipContent" style="display:none"> 
        Projects in ${siteAcronym} can contain a variety of different information resources and used to organize a set of related information resources such as documents, datasets, coding sheets, and images. A project's child resources can either inherit or override the metadata entered at this project level. For instance, if you enter the keywords "southwest" and "pueblo" on a project, resources associated with this project that choose to inherit those keywords will also be discovered by searches for the keywords "southwest" and "pueblo". Child resources that override those keywords would not be associated with those keywords (only as long as the overriden keywords are different of course). 
        </div>

        <div class="indentFull" tiplabel="Inherit Metadata from Selected Project" tooltipcontent="#divSelectAllInheritanceTooltipContent" id="divInheritFromProject"></div>
        <div class="control-group">
            <div class="controls">
                <label class="checkbox" for="cbSelectAllInheritance">
                    <input type="checkbox" value="true" id="cbSelectAllInheritance" class="">
                    <span id="spanCurrentlySelectedProjectText">Inherit from project.</span>
                </label>
            </div>
        </div>
        </#if>   
    
</div>
</#macro>

<#macro resourceCollectionSection>
    <#local _resourceCollections = [blankResourceCollection] />
    <#if (resourceCollections?? && !resourceCollections.empty)>
    <#local _resourceCollections = resourceCollections />
    </#if>
    <div style="display:none" id="divResourceCollectionListTips">
        <p>
            Specify the names of the collections that ${siteAcronym} should add this resource to.  Alternately you can start a new, <em>public</em>  collection 
            by typing the desired name and selecting the last option in the list of pop-up results.  The newly-created collection will contain only this 
            resource, but can be modified at any time. 
        </p>
    </div>

    <p tiplabel="${siteAcronym} Collections" tooltipcontent="#divResourceCollectionListTips"></p>
    <p class="help-block">Collections enable you to organize and share resources within ${siteAcronym}</p>
    <table id="resourceCollectionTable" class="table repeatLastRow" addAnother="add another collection">
        <thead>
            <th colspan=2>Collection Name</th>
        </thead>
        <tbody>
            <#list _resourceCollections as resourceCollection>
            <@resourceCollectionRow resourceCollection resourceCollection_index/>
            </#list>
        </tbody>
    </table>

</#macro>

<#macro keywordRows label keywordList keywordField showDelete=true addAnother="add another keyword">
    <div class="control-group repeatLastRow" data-add-another="${addAnother}">
        <label class="control-label">${label}</label>
        <#if keywordList.empty >
          <@keywordRow keywordField />
        <#else>
        <#list keywordList as keyword>
          <@keywordRow keywordField keyword_index showDelete />
        </#list>
        </#if>
    </div>
</#macro>

<#macro keywordRow keywordField keyword_index=0 showDelete=true>
    <div class="controls controls-row " id='${keywordField}Row_${keyword_index}_'>
        <@s.textfield theme="tdar" name='${keywordField}[${keyword_index}]' cssClass='input-xxlarge keywordAutocomplete' placeholder="enter keyword"/>
        <#if showDelete>
        <@clearDeleteButton id="${keywordField}Row" />
        </#if>
    </div>
</#macro>


<#macro spatialContext showInherited=true>
<div class="well" id="spatialSection">
    <legend>Spatial Terms</legend>
    <@inheritsection checkboxId="cbInheritingSpatialInformation" name='resource.inheritingSpatialInformation' showInherited=showInherited />
    <div id="divSpatialInformation">
        <div tiplabel="Spatial Terms: Geographic" tooltipcontent="Keyword list: Geographic terms relevant to the document, e.g. &quot;Death Valley&quot; or &quot;Kauai&quot;." ></div>
        <@keywordRows "Geographic Terms" geographicKeywords 'geographicKeywords' />
        
        <h4>Geographic Region</h4>
        <div id='large-google-map' style='height:450px;'
            tiplabel="Geographic Coordinates"
            tooltipcontent="Identify the approximate region of this resource by clicking on &quot;Select Region&quot; and drawing a bounding box on the map.
                <br/>Note: to protect site security, ${siteAcronym} obfuscates all bounding boxes, bounding boxes smaller than 1 mile, especially.  This 'edit' view 
                will always show the exact coordinates."
            ></div>
        <br />
        <div id="divManualCoordinateEntry" tooltipcontent="#divManualCoordinateEntryTip">
            
            <@s.checkbox id="viewCoordinatesCheckbox" name="viewCoordinatesCheckbox" onclick="$('#explicitCoordinatesDiv').toggle(this.checked);" label='Enter / View Coordinates' labelposition='right'  />
            
            <script type="text/javascript">
                $(document).ready(function(){
                    $('#explicitCoordinatesDiv').toggle($('#viewCoordinatesCheckbox')[0].checked);
                    
                    $(".latLong").each(function(index, value){
                        $(this).hide();
                        //copy value of hidden original to the visible text input
                        var id = $(this).attr('id'); 
                        $('#d_' + id).val($('#' + id).val());
                    });
                });
                
            </script>
            <div id='explicitCoordinatesDiv' style='text-align:center;'>
            
                <table cellpadding="0" cellspacing="0" style="margin-left:auto;margin-right:auto;text-align:left;" >
                <tr>                                    
                <td></td>
                <td>
                <@s.textfield  name='latitudeLongitudeBoxes[0].maximumLatitude' id='maxy' size="14" cssClass="float latLong" title="Please enter a valid Maximum Latitude" />
                <input type="text"  id='d_maxy'  watermark="Latitude (max)" onChange='processLatLong(this)' onBlur='processLatLong(this)' />
                </td>
                <td></td>
                </tr>
                <tr>
                <td style="width:33%;text-align:center">
                    <@s.textfield  name="latitudeLongitudeBoxes[0].minimumLongitude" id='minx' size="14" cssClass="float latLong" title="Please enter a valid Minimum Longitude" />
                    <input type="text"  id='d_minx'  watermark="Longitude (min)"  onChange='processLatLong(this)' onBlur='processLatLong(this)' />
                </td>
                <td style="width:33%;text-align:center">
                    <input type="button" id="locate" value="Locate" onclick="locateCoords();" style="padding:5px; margin:0;width:10em" />
                </td>
                <td style="width:33%;text-align:center">
                    <@s.textfield  name="latitudeLongitudeBoxes[0].maximumLongitude" id='maxx' size="14" cssClass="float latLong" title="Please enter a valid Maximum Longitude" />
                    <input type="text"  id='d_maxx'   watermark="Longitude (max)" onChange='processLatLong(this)' onBlur='processLatLong(this)' />
                </td>
                </tr>
                <tr>
                <td></td>
                <td>
                    <@s.textfield  name="latitudeLongitudeBoxes[0].minimumLatitude" id="miny" size="14" cssClass="float latLong " title="Please enter a valid Minimum Latitude" /> 
                    <input type="text" id="d_miny"  watermark="Latitude (min)" onChange='processLatLong(this)' onBlur='processLatLong(this)' /> 
                </td>
                <td></td>
                </tr>           
                </table>
            </div>
            <div id="divManualCoordinateEntryTip" class="hidden">
                <h2>Manually Enter Coordinates</h2>
                <div>
                    Click the Locate button after entering the longitude-latitude pairs in the respective input fields to draw a box on the map and zoom to it.
                    <br />Examples:
                    <ul>
                        <li>40&deg;44'55"N</li>
                        <li>53 08 50N</li>
                        <li>-73.9864</li>
                    </ul>
                    <p><aside><strong>Note:</strong> to protect site security, ${siteAcronym} obfuscates all bounding boxes, bounding boxes smaller than 1 mile.  This 'edit' view will 
                    always show the exact coordinates.</aside></p>
                                   
                 </div>
            </div>
        </div>
    </div>
</div>
</#macro>


<#macro resourceProvider showInherited=true>
<div class="well" id="divResourceProvider" tiplabel="Resource Provider" tooltipcontent="The institution authorizing ${siteAcronym} to ingest the resource for the purpose of preservation and access.">
    <legend>Institution Authorizing Upload of this ${resource.resourceType.label}</legend>
    <@s.textfield label='Institution' name='resourceProviderInstitutionName' id='txtResourceProviderInstitution' cssClass="institution input-xxlarge" size='40'/>
    <br/>
</div>
</#macro>


<#macro temporalContext showInherited=true>
<div class="well" id="temporalSection">
    <legend>Temporal Coverage</legend>
    <@inheritsection checkboxId="cbInheritingTemporalInformation" name='resource.inheritingTemporalInformation' showInherited=showInherited  />
    <div  id="divTemporalInformation">
        <div tiplabel="Temporal Term" tooltipcontent="Keyword list: Temporal terms relevant to the document, e.g. &quot;Pueblo IV&quot; or &quot;Late Archaic&quot;."></div>
        <@keywordRows "Temporal Terms" temporalKeywords 'temporalKeywords' true "add another temporal keyword" />
        <@coverageDatesSection />
    </div>
</div>
</#macro>

<#macro combineValues list=[]>
	<#compress>
		<#list list as item>
			<#if item_index !=0>,</#if>"${item?html}"
		</#list>
	</#compress>
</#macro>
<#macro combineValues2 list=[]>
	<#compress>
		<#list list as item>
			<#if item_index !=0>,</#if>${item?html}
		</#list>
	</#compress>
</#macro>


<#macro generalKeywords showInherited=true>

<div class="well" 
    tiplabel="General Keywords"
    tooltipcontent="Keyword list: Select the artifact types discussed in the document.">   
    <legend>General Keywords</legend>
    <@inheritsection checkboxId="cbInheritingOtherInformation" name='resource.inheritingOtherInformation'  showInherited=showInherited />
    <div id="divOtherInformation">
        <@keywordRows "Keyword" otherKeywords 'otherKeywords' />
        
        <p>fixme: replace above with below</p>
    	<input type=text" name="test" id="otherKeywords" style="width:500px" value="<@combineValues2 otherKeywords/>"/>
    	<script>
    	        $(document).ready(function() {
        $("#otherKeywords").select2({
            tags:[<@combineValues otherKeywords />],
            tokenSeparators: [";"]});
    	});
    	</script>

    </div>
</div>
</#macro>


<#macro sharedUploadFile divTitle="Upload">
<div class="well" id="uploadSection">
    <legend>${divTitle}</legend>
        <div class='fileupload-content'>
            <#nested />
            <#-- XXX: verify logic for rendering this -->
            <#if multipleFileUploadEnabled || resource.hasFiles()>
            <h4>Current ${multipleFileUploadEnabled?string("and Pending Files", "File")}</h4>
            <table id="uploadFiles" class="files tableFormat">
            </table>
            <table id="files" class='files sortable tableFormat'>
            <thead>
                <tr class="reorder <#if (fileProxies?size < 2 )>hidden</#if>">
                    <th colspan=2>Reorder: <span class="link alphasort">Alphabetic</span> | <span class="link" onclick="customSort(this)">Custom</span>  </th>
                </tr>
            </thead>
            <tbody>
            <#list fileProxies as fileProxy>
                <#if fileProxy??>
                <@fileProxyRow rowId=fileProxy_index filename=fileProxy.filename filesize=fileProxy.size fileid=fileProxy.fileId action=fileProxy.action versionId=fileProxy.originalFileVersionId/>
                </#if>
            </#list>
            <#if fileProxies.empty>
            <tr class="noFiles width99percent newRow">
            <td><em>no files uploaded</em></td>
            </tr>
            </#if>
            </tbody>
            </table>
            </#if>
        </div>
      <div id="divConfidentialAccessReminder" class="hidden">
          <em>Embargoed records will become public in ${embargoPeriodInYears} years. Confidential records will not be made public. Use the &quot;Access Rights&quot; section to assign access to this file for specific users.</em>
      </div>
</div>
</#macro>

<#macro siteKeywords showInherited=true divTitle="Site Information">
<div class="well" id="siteSection">
    <legend>${divTitle}</legend>
    <@inheritsection checkboxId='cbInheritingSiteInformation' name='resource.inheritingSiteInformation'  showInherited=showInherited />
    <div id="divSiteInformation">
        <div class="hidden" id="siteinfohelp">
            Keyword list: Enter site name(s) and select feature types (<a href="${siteTypesHelpUrl}">view complete list</a>) 
            discussed in the document. Use the Other field if needed.
        </div>
        <@keywordRows "Site Name" siteNameKeywords 'siteNameKeywords' />
        
        <div class="control-group">
            <label class="control-label">Site Type</label>
            <div class="controls">
                <@s.checkboxlist theme="hier" name="approvedSiteTypeKeywordIds" keywordList="approvedSiteTypeKeywords" />
            </div>
        </div>
        
        <@keywordRows "Other" uncontrolledSiteTypeKeywords 'uncontrolledSiteTypeKeywords' />
    </div>
</div>
</#macro>


<#macro materialTypes showInherited=true>
<div class="well">
    <div class="hidden" id="materialtypehelp">
        Keyword list: Select the artifact types discussed in the document.<a href="${materialTypesHelpUrl}">view all material types</a>
    </div>
    <legend>Material Types</legend>
    <@inheritsection checkboxId='cbInheritingMaterialInformation' name='resource.inheritingMaterialInformation'  showInherited=showInherited />
    <div id="divMaterialInformation">
        <@s.checkboxlist name='materialKeywordIds' list='allMaterialKeywords' listKey='id' listValue='label' listTitle="definition"  label="Select Type(s)"
            spanSize="3" numColumns="3" cssClass="smallIndent" />
    </div>      
</div>

</#macro>

<#macro culturalTerms showInherited=true inline=false>
<div  <#if !inline> class="well" </#if>>
    <div id="culturehelp" class="hidden">
        Keyword list: Select the archaeological &quot;cultures&quot; discussed in the document. Use the Other field if needed. 
        <a href="${culturalTermsHelpUrl}">view all controlled terms</a>
    </div>
    <legend>Cultural Terms<legend>
    <@inheritsection checkboxId="cbInheritingCulturalInformation" name='resource.inheritingCulturalInformation'  showInherited=showInherited />
    <div id="divCulturalInformation">
        <div class="control-group">
            <label class="control-label">Culture</label>
            <div class="controls">
                <@s.checkboxlist theme="hier" name="approvedCultureKeywordIds" keywordList="approvedCultureKeywords" />
            </div>
        </div>
        
        <!--"add another cultural term" -->
        <@keywordRows "Other" uncontrolledCultureKeywords 'uncontrolledCultureKeywords' />
    </div>
</div>
</#macro>

<#macro uncontrolledCultureKeywordRow uncontrolledCultureKeyword_index=0>
            <tr id='uncontrolledCultureKeywordRow_${uncontrolledCultureKeyword_index}_'>
            <td>
                <@s.textfield name='uncontrolledCultureKeywords[${uncontrolledCultureKeyword_index}]' cssClass='longfield cultureKeywordAutocomplete' autocomplete="off" />
                </td><td><@clearDeleteButton id="uncontrolledCultureKeywordRow" />
            </td>
            </tr>
</#macro>

<#macro investigationTypes showInherited=true >
<div class="well" tiplabel="Investigation Types" tooltipcontent="#investigationtypehelp" id="investigationSection">
    <legend>Investigation Types</legend>
    <@inheritsection checkboxId='cbInheritingInvestigationInformation' name='resource.inheritingInvestigationInformation'  showInherited=showInherited />
    <div id="divInvestigationInformation">
        <@s.checkboxlist name='investigationTypeIds' list='allInvestigationTypes' listKey='id' listValue='label' numColumns="2" cssClass="smallIndent" 
            label="Select Type(s)" listTitle="definition" />
    </div>
</div>

<div class="hidden" id="investigationtypehelp">Keyword list: Select the investigation types relevant to the document.<a href="${investigationTypesHelpUrl}">
view all investigation types</a></div>
</#macro>


<#-- provides a fieldset just for full user access -->
<#macro fullAccessRights tipsSelector="#divAccessRightsTips">
<#local _authorizedUsers=authorizedUsers />
<#if _authorizedUsers.empty><#local _authorizedUsers=[blankAuthorizedUser]></#if>
<div id="divAccessRightsTips" style="display:none">
<p>Determines who can edit a document or related metadata. Enter the first few letters of the person's last name. 
The form will check for matches in the ${siteAcronym} database and populate the related fields.</p>
<em>Types of Permissions</em>
<dl>
    <dt>View All</dt>
    <dd>User can view/download all file attachments.</dd>
    <dt>Modify Record<dt>
    <dd>User can edit this resource.<dd>
</dl>
</div>

<div id="divAccessRights" class="well" tooltipcontent="${tipsSelector}">
<legend><a name="accessRights"></a>Access Rights</legend>
<h4>Users who can view or modify this resource</h4>

<div id="accessRightsRecords" class="repeatLastRow" data-addAnother="add another user">
    <div class="control-group">
        <label class="control-label">Users</label>
        <#list _authorizedUsers as authorizedUser>
            <#if authorizedUser??>
                <@authorizedUserRow authorizedUser authorizedUser_index />
            </#if>
        </#list>
    </div>
</div>

<#nested>

 <#if persistable.resourceType??>
  <@resourceCollectionsRights effectiveResourceCollections >
  Note: this does not reflect changes to resource collection you have made until you save.
  </@resourceCollectionsRights>
 </#if>

</div>
</#macro>

<#macro authorizedUserRow authorizedUser authorizedUser_index=0>
<#local bDisabled = (authorizedUser.user.id == authenticatedUser.id) />
<#local disabled =  bDisabled?string />

    <div id='authorizedUserRow_${authorizedUser_index}_' class="repeat-row">
        <@s.hidden name='authorizedUsers[${authorizedUser_index}].user.id' value='${(authorizedUser.user.id!-1)?c}' id="authorizedUserId__id_${authorizedUser_index}_"  cssClass="validIdRequired" onchange="this.valid()"  autocompleteParentElement="#authorizedUserRow_${authorizedUser_index}_"  />
        <!-- FIXME -- is this needed -->
        <@s.hidden name="authorizedUsers[${authorizedUser_index}].generalPermission" value="${authorizedUser.generalPermission!'VIEW_ALL'}"/>
        
        <div class="controls controls-row">
            <@s.textfield theme="tdar" cssClass="span2 userAutoComplete" placeholder="Last Name"  readonly="${disabled}" autocompleteParentElement="#authorizedUserRow_${authorizedUser_index}_"
                autocompleteIdElement="#authorizedUserId__id_${authorizedUser_index}_" autocompleteName="lastName" autocomplete="off"
                name="authorizedUsers[${authorizedUser_index}].user.lastName" maxlength="255" /> 
            <@s.textfield theme="tdar" cssClass="span2 userAutoComplete" placeholder="First Name"  readonly="${disabled}" autocomplete="off"
                name="authorizedUsers[${authorizedUser_index}].user.firstName" maxlength="255" autocompleteName="firstName"
                autocompleteIdElement="#authorizedUserId__id_${authorizedUser_index}_" 
                autocompleteParentElement="#authorizedUserRow_${authorizedUser_index}_"  />
            <@s.textfield theme="tdar" cssClass="span3 userAutoComplete" placeholder="Email (optional)" readonly="${disabled}" autocomplete="off"
                autocompleteIdElement="#authorizedUserId__id_${authorizedUser_index}_" autocompleteName="email" autocompleteParentElement="#authorizedUserRow_${authorizedUser_index}_"
                name="authorizedUsers[${authorizedUser_index}].user.email" maxlength="255"/>
          
          <@clearDeleteButton id="authorizedUserRow" disabled="${disabled}" />
        </div>
  
   
        <div class="controls controls-row">
        <@s.textfield theme="tdar" cssClass="span4 userAutoComplete" placeholder="Institution Name (Optional)" readonly="${disabled}" autocomplete="off"
            autocompleteIdElement="#authorizedUserId__id_${authorizedUser_index}_" 
            autocompleteName="institution" 
            autocompleteParentElement="#authorizedUserRow_${authorizedUser_index}_"
            name="authorizedUsers[${authorizedUser_index}].user.institution.name" maxlength="255" />
        <#if bDisabled>
        <@s.select theme="tdar" cssClass="span3" name="authorizedUsers[${authorizedUser_index}].generalPermission" 
            emptyOption='false' listValue='label' list='%{availablePermissions}' disabled=true
        />
        <#else>
        <@s.select theme="tdar" cssClass="span3" name="authorizedUsers[${authorizedUser_index}].generalPermission" 
            emptyOption='false' listValue='label' list='%{availablePermissions}'
        />
        </#if>
        </div>
  </div>

</#macro>

<#macro categoryVariable>
<div id='categoryDivId'>
<@s.select labelposition='left' label='Category' id='categoryId' name='categoryId' 
    onchange='changeSubcategory("#categoryId","#subcategoryId")'
                autocompleteName="sortCategoryId"
    listKey='id' listValue='name' emptyOption='true' list='%{allDomainCategories}' />
</div>
<div id='subcategoryDivId'>
<@s.select labelposition='left' label='Subcategory' id='subcategoryId' name='subcategoryId' 
    autocompleteName="subCategoryId" headerKey="-1" listKey='id' headerValue="N/A" list='%{subcategories}'/>
</div>
</#macro>


<#macro singleFileUpload typeLabel="${resource.resourceType.label}">
    <div tiplabel="Upload your ${typeLabel}<#if multipleFileUploadEnabled>(s)</#if>" 
    tooltipcontent="The metadata entered on this form will be associated with this file. We accept ${typeLabel}s in the following formats: <@join sequence=validFileExtensions delimiter=", "/>"
    >
    <@s.file name='uploadedFiles' label='${typeLabel}' cssClass="validateFileType" id="fileUploadField" labelposition='left' size='40' />
    <div class="field indentFull">
    <i>Valid file types include: <@join sequence=validFileExtensions delimiter=", "/></i>
    </div>
    <#nested>
    </div>
</#macro>

<#macro manualTextInput typeLabel="" type="">
<div class="glide">
    <h3>${(resource.id == -1)?string("Submit", "Replace")} ${typeLabel}</h3>
    <div>
    <label class='label' for='inputMethodId'>Submit as:</label>
    <select id='inputMethodId' name='fileInputMethod' onchange='refreshInputDisplay()' cssClass="field">
        <#-- show manual option by default -->
        <#assign usetext=(resource.getLatestVersions().isEmpty() || (fileTextInput!"") != "")>
        <#if type=="coding">
            <option value='file' <#if !usetext>selected="selected"</#if>>Upload an Excel or CSV coding sheet file</option>
            <option value='text' <#if usetext>selected="selected"</#if>>Manually enter coding rules into a textarea</option>
        <#else>
            <option value='file' <#if !usetext>selected="selected"</#if>>Upload an OWL file</option>
            <option value='text' <#if usetext>selected="selected"</#if>>Manually enter your ontology into a textarea</option>
        </#if>
    </select>
    </div>
    <br/>

    <div id='uploadFileDiv' style='display:none;'>
    <div id='uploadFileExampleDiv' class='info'  >
    <#if type=="coding">
        <p>
        To be parsed properly your coding sheet should have <b>Code, Term, Description (optional)</b> columns, in order.  For example,
        </p>
        <table class="zebracolors">
        <thead>
        <tr><th>Code</th><th>Term</th><th>Description (optional)</th></tr>
        </thead>
        <tbody>
        <tr>
        <td>18</td><td>Eutamias spp.</td><td>Tamias spp. is modern term</td>
        </tr>
        <tr>
        <td>19</td><td>Ammospermophilus spp.</td><td></td>
        </tr>
        <tr>
        <td>20</td><td>Spermophilus spp.</td><td></td>
        </tr>
        </tbody>
        </table>
        <br/>
    <#else>
        <p>
        We currently support uploads of <a class='external' href='http://www.w3.org/TR/owl2-overview/'>OWL XML/RDF files</a>.  
        You can create OWL files by hand (difficult) or with a tool like <a
        class='external' href='http://protege.stanford.edu/'>the
        Prot&eacute;g&eacute; ontology editor</a>.  Alternatively, choose the <b>Submit
        as: Manually enter your ontology</b> option above and enter your ontology
        into a text area.  
        </p>
    </#if>
    </div>
    <@singleFileUpload />
    </div>
    
    <div id='textInputDiv'>
    <div id='textInputExampleDiv' class='info'>
    <#if type="coding">
        <p>Enter your coding rules in the text area below.  Each line can have a maximum of three elements, separated by commas, 
        and should be in the form <code>code, term, optional description</code>.  Codes can be numbers or arbitrary strings.  
        For example, 
        </p>
        <p>
        <code>1, Small Mammal, Small mammals are smaller than a breadbox</code><br/>
        <code>2, Medium Mammal, Medium Mammals are coyote or dog-sized</code>
        </p>
        
        <div class='note'>If a code, a term, or a description has an embedded comma, 
            the whole value must be enclosed in double quotes, e.g. <br/>
            <code>3, Large Mammal, &quot;Large mammals include deer, antelope, and bears&quot;</code>
        </div>
        <br/>
    <#else>
        <p>
        You can enter your ontology in the text area below.  Separate each concept in
        your ontology with newlines (hit enter), and indicate parent-child relationships
        with tabs (make sure you use the tab key on your keyboard - spaces do not work).
        To specify synonyms for a given term use comma-separated parentheses, e.g.,
        <br/>
        <code>Flake (Debris, Debitage)</code>. 
        <br/> 
        For lithic form, the following would be a simple ontology:
        </p>
        <pre>
            Tool
                Projectile Point
                Scraper (Grattoir)
                    End Scraper
                    Side Scraper
                Other Tool
            Flake (Debris, Debitage)
                Utilized
                Unutilized
            Core
        </pre>
    </#if>
    </div>
    <@s.textarea label='${typeLabel}' labelposition='top' id='fileInputTextArea' name='fileTextInput' rows="5" cssClass='resizable' />
    </div>
</div>

</#macro>

<#macro submit label="Save" fileReminder=true buttonid="submitButton">
<div class="errorsection"> 
    <#if fileReminder>
    <div id="reminder" class="row">
        <p><span class="label label-info">Reminder</span> No files are attached to this record. </p>
    </div>
    <div id="error" class="row"><ul></ul></div>
    </#if>     
    <#nested>
    <div class="form-actions">
    <@submitButton label=label id=buttonid />
       <img src="<@s.url value="/images/indicator.gif"/>" class="waitingSpinner" style="visibility:hidden"/>
    </div> 

<div class="modal hide fade" id="validationErrorModal" tabindex="-1" role="dialog" aria-labelledby="validationErrorModalLabel" aria-hidden="true">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3 id="validationErrorModalLabel">Validation Errors</h3>
    </div>
    <div class="modal-body">
        <h4>Please correct the following errors</h4>
        <p></p>
    </div>
    <div class="modal-footer">
        <button type="button" class="btn" data-dismiss="modal" aria-hidden="true">Close</button>
    </div>
</div> 

</#macro>

<#macro submitButton label="submit" id="">
    <input type="submit" class='btn btn-primary submitButton' name="submitAction" value="${label}"  <#if id?has_content>id="${id}"</#if>>
</#macro>

<#macro resourceJavascript formSelector="#resourceMetadataForm" selPrefix="#resource" includeAsync=false includeInheritance=false>

<script type='text/javascript'>
$(function(){
    'use strict';
    var form = $("${formSelector}")[0];
    
    <#if includeAsync>
    //init fileupload
    var id = $('input[name=id]').val();
    var acceptFileTypes  = <@edit.acceptedFileTypesRegex />;
    TDAR.fileupload.registerUpload({informationResourceId: id, acceptFileTypes: acceptFileTypes});
    </#if>

    TDAR.common.initEditPage(form);
    
    
});
<#nested>
</script>
  
</#macro>



<#macro parentContextHelp element="div" resourceType="resource" valueType="values">
<${element} tiplabel="Inherited Values" tooltipcontent="The parent project for this ${resourceType} defines ${valueType} for this section.  You may also define your own, but note that they will not override the values defined by the parent.">
<#nested>
</${element}>
</#macro>

<#macro relatedCollections showInherited=true>
<#local _sourceCollections = sourceCollections />
<#local _relatedComparativeCollections = relatedComparativeCollections />
<#if _sourceCollections.empty><#local _sourceCollections = [blankSourceCollection] /></#if>
<#if _relatedComparativeCollections.empty><#local _relatedComparativeCollections = [blankRelatedComparativeCollection] /></#if>
<div class="well" id="relatedCollectionsSectionGlide">
    <less>Museum or Archive Collections</less>
    <@inheritsection checkboxId="cbInheritingCollectionInformation" name='resource.inheritingCollectionInformation' showInherited=showInherited />
    <div id="relatedCollectionsSection" >
        <div id="divSourceCollectionControl" class="control-group">
            <label class="control-label">Source Collection</label>
            <#list _sourceCollections as sourceCollection>
            <@sourceCollectionRow sourceCollection "sourceCollection" sourceCollection_index/>
            </#list>
        </div>
    
        <div id="divRelatedComparativeCitationControl" class="control-group">
            <label class="control-label">Related or Comparative Collection</label></label>
            <#list _relatedComparativeCollections as relatedComparativeCollection>
            <@sourceCollectionRow relatedComparativeCollection "relatedComparativeCollection" relatedComparativeCollection_index/>
            </#list>
        </div> 
    
        <div style="display:none" id="divSourceCollectionHelpText">
            <p>
              The museum or archival accession that contains the
              artifacts, original photographs, or original notes that are described
              in this ${siteAcronym} record.
            </p>
        </div>
        <div style="display:none" id="divComparativeCollectionHelpText">
            <p>
            Museum or archival collections (e.g.,
            artifacts, photographs, notes, etc.) which are associated with (or
            complement) a source collection. For example, a researcher may have
            used a comparative collection in an analysis of the materials
            documented in this ${siteAcronym} record.
            </p>
        </div>
    </div>
    </div>
</div>
</#macro>

<#macro sourceCollectionRow sourceCollection prefix index=0>
<#local plural = "${prefix}s" />
    <div class="controls control-row">
    <@s.hidden name="${plural}[${index}].id" />
    <@s.textfield theme="tdar" name='${plural}[${index}].text' cssClass="input-xxlarge" /></td>
    <@edit.clearDeleteButton id="${prefix}Row" />
    </div>
</#macro>

<#macro inheritsection checkboxId name showInherited=true  label="Inherit this section" >
    <div class='divInheritSection'>
    <#if showInherited>
        <@s.checkbox label="${label}" id="${checkboxId}" name="${name}" cssClass="alwaysEnabled" />
    <#elseif resource??>
         <@inheritTips id="${checkboxId}" />
    </#if>
    </div>    
</#macro>

<#macro resourceCollectionRow resourceCollection collection_index = 0 type="internal">
      <tr id="resourceCollectionRow_${collection_index}_" class="repeat-row">
          <td> 
              <@s.hidden name="resourceCollections[${collection_index}].id"  id="resourceCollectionRow_${collection_index}_id" />
              <@s.textfield id="resourceCollectionRow_${collection_index}_id" name="resourceCollections[${collection_index}].name" cssClass="input-xxlarge collectionAutoComplete "  autocomplete="off"
              autocompleteIdElement="#resourceCollectionRow_${collection_index}_id" label="${siteAcronym} Collection"
              autocompleteParentElement="#resourceCollectionRow_${collection_index}_" />
          </td>
          <td><@clearDeleteButton id="resourceCollectionRow" /> </td>
      </tr>
</#macro>



<#macro resourceNoteSection showInherited=true>
<div class="well" id="resourceNoteSectionGlide">
    <#local _resourceNotes = resourceNotes />
    <#if _resourceNotes.empty >
    <#local _resourceNotes = [blankResourceNote] />
    </#if>
    <div class="hidden" tiplabel="Notes"  tooltipcontent="Use this section to append any notes that may help clarify certain aspects of the resource.  For example, 
    a &quot;Redaction Note&quot; may be added to describe the rationale for certain redactions in a document."></div>
    <legend>Notes</legend>
    <@inheritsection checkboxId="cbInheritingNoteInformation" name='resource.inheritingNoteInformation' showInherited=showInherited />
    <div id="resourceNoteSection" class="control-group repeatLastRow">
        <label class="control-label">Type / Contents</label>
        <#list _resourceNotes as resourceNote>
        <#if resourceNote??><@noteRow resourceNote resourceNote_index/></#if>
        </#list>
    </div>
</div>
</#macro>

<#macro noteRow proxy note_index=0>
      <div id="resourceNoteRow_${note_index}_" class="repeat-row">
          <div class="controls controls-row">
              <@s.hidden name="resourceNotes[${note_index}].id" />
              <@s.select theme="tdar" emptyOption='false' name='resourceNotes[${note_index}].type' list='%{noteTypes}' listValue="label" /> 
          <@clearDeleteButton id="resourceNoteRow" />
          </div>
          <div class="controls">
              <@s.textarea theme="tdar" name='resourceNotes[${note_index}].note' placeholder="enter note contents" cssClass='resizable input-xxlarge'  rows='3' maxlength='5000' />
          </div>
      </div>
</#macro>




<#macro coverageDatesSection>
<#local _coverageDates=coverageDates />
<#if _coverageDates.empty><#local _coverageDates = [blankCoverageDate] /></#if>
<div class="hidden" id="coverageDatesTip">
    Select the approriate type of date (Gregorian calendar date or radiocarbon date). To enter a date range, enter the <em>earliest date</em> in the <em>Start Year field<em> 
    and the latest date in the End Year Field. <em>Dates containing "AD" or "BC" are not valid</em>. Use positive numbers for AD dates (500, 1200), and use negative numbers for BC dates (-500, -1200). Examples: 
    <ul>
        <li>Calendar dates: 300 start, 500 end (number only, smaller value first)</li>
        <li>Radiocarbon dates: 500 start, 300 end (number only, larger value first)</li>     
    </ul>
</div>
<div class="control-group repeatLastRow" data-add-another="add another coverage date">
    <label class="control-label">Coverage Dates</label>
    
    <#list _coverageDates as coverageDate>
    <#if coverageDate??>
    <@dateRow coverageDate coverageDate_index/>
    </#if>
    </#list>
</div>

</#macro>



<#macro dateRow proxy=proxy proxy_index=0>
<div class="controls controls-row" id="DateRow_${proxy_index}_">
        <@s.hidden name="coverageDates[${proxy_index}].id" />
        <@s.select theme="tdar"name="coverageDates[${proxy_index}].dateType" cssClass="coverageTypeSelect input-medium"
            listValue='label'  headerValue="Date Type" headerKey="NONE"
            list=allCoverageTypes />
        <@s.textfield theme="tdar" placeholder="Start Year" cssClass="coverageStartYear input-small" name="coverageDates[${proxy_index}].startDate" maxlength="10" /> 
        <@s.textfield theme="tdar" placeholder="End Year" cssClass="coverageEndYear input-small" name="coverageDates[${proxy_index}].endDate" maxlength="10" />
        <@s.textfield theme="tdar" placeholder="Description"  cssClass="coverageDescription input-xlarge" name="coverageDates[${proxy_index}].description" />
       <@edit.clearDeleteButton id="{proxy_index}DateRow"/>
</div>
</#macro>


<#macro allCreators sectionTitle proxies prefix inline=false showInherited=false>
    <@resourceCreators sectionTitle proxies prefix inline showInherited />
    <style>
    </style>
</#macro>

<#macro resourceCreators sectionTitle proxies prefix inline=false showInherited=false>
<#if !inline>
<div class="well" tiplabel="${sectionTitle}" 
	id="${prefix}Section"
	tooltipcontent="Use these fields to properly credit individuals and institutions for their contribution to the resource. Use the '+' sign to add fields for either persons or institutions, and use the drop-down menu to select roles">
    <legend>${sectionTitle}</legend>
<#else>
<label class="toplabel">${sectionTitle}</label> <br />
</#if>

    <table 
        id="${prefix}Table"
        class="table repeatLastRow creatorProxyTable">
        <tbody>
            <#if proxies?has_content >
              <#list proxies as proxy>
                <@creatorProxyRow proxy  prefix proxy_index/>
              </#list>
            <#else>
              <@creatorProxyRow blankCreatorProxy prefix 0 />
            </#if>
        </tbody>
    </table>
<#if !inline>
</div>
</#if>

</#macro>


<#macro creatorProxyRow proxy=proxy prefix=prefix proxy_index=proxy_index type_override="NONE">
    <#assign relevantPersonRoles=personAuthorshipRoles />
    <#assign relevantInstitutionRoles=institutionAuthorshipRoles />
    <#if prefix=='credit'>
        <#assign relevantPersonRoles=personCreditRoles />
        <#assign relevantInstitutionRoles=institutionCreditRoles />
    </#if>

    <#if proxy??>
    <tr id="${prefix}Row_${proxy_index}_" class="repeat-row">
          <#assign creatorType = proxy.actualCreatorType!"PERSON" />
         <td><div class="btn-group creator-toggle-button" data-toggle="buttons-radio">
	         <button type="button" class="btn btn-small personButton <#if type_override == "PERSON" || (creatorType=='PERSON' && type_override=='NONE') >btn-active active</#if>" data-toggle="button">Person</button>
	         <button type="button" class="btn btn-small institutionButton <#if creatorType =='INSTITUTION' || type_override == "INSTITUTION">btn-active active</#if>" data-toggle="button">Institution</button>
		</div>
</td>
        <td>
        <span class="creatorPerson <#if creatorType =='INSTITUTION' || type_override == "INSTITUTION">hidden</#if>"  id="${prefix}Row_${proxy_index}_p">
            <@s.hidden name="${prefix}Proxies[${proxy_index}].person.id" id="${prefix}person_id${proxy_index}" onchange="this.valid()"  autocompleteParentElement="#${prefix}Row_${proxy_index}_p"  />
            <div class="control-group">
                <div class="controls controls-row">
                    <@s.textfield theme="tdar" cssClass="nameAutoComplete span2" watermark="Last Name" placeholder="Last Name" autocomplete="off"
                        autocompleteName="lastName" autocompleteIdElement="#${prefix}person_id${proxy_index}" autocompleteParentElement="#${prefix}Row_${proxy_index}_p"
                        name="${prefix}Proxies[${proxy_index}].person.lastName" maxlength="255" /> 
                    <@s.textfield theme="tdar" cssClass="nameAutoComplete span2" watermark="First Name" placeholder="First Name" autocomplete="off"
                        autocompleteName="firstName" autocompleteIdElement="#${prefix}person_id${proxy_index}" autocompleteParentElement="#${prefix}Row_${proxy_index}_p"
                        name="${prefix}Proxies[${proxy_index}].person.firstName" maxlength="255" />
	                <@s.select theme="tdar" name="${prefix}Proxies[${proxy_index}].role"  autocomplete="off"
	                    listValue='label'
	                    list=relevantPersonRoles  
	                    cssClass="creator-role-select span3"
	                    />
                </div>
                <div class="controls controls-row">
                <@s.textfield theme="tdar" cssClass="nameAutoComplete span4" watermark="Institution Name (Optional)" placeholder="Institution Name (Optional)" autocomplete="off"
                     autocompleteName="institution" autocompleteIdElement="#${prefix}person_id${proxy_index}" autocompleteParentElement="#${prefix}Row_${proxy_index}_p"
                    name="${prefix}Proxies[${proxy_index}].person.institution.name" maxlength="255" />
                    <@s.textfield theme="tdar" cssClass="nameAutoComplete span3" watermark="Email (Optional)" placeholder="Email (Optional)" autocomplete="off"
                         autocompleteName="email" autocompleteIdElement="#${prefix}person_id${proxy_index}" autocompleteParentElement="#${prefix}Row_${proxy_index}_p"
                        name="${prefix}Proxies[${proxy_index}].person.email" maxlength="255"/>
                </div>
            </div>
        </span>
        
            <span class="creatorInstitution <#if type_override == "PERSON" || (creatorType=='PERSON' && type_override=='NONE') >hidden</#if>" id="${prefix}Row_${proxy_index}_i">
            <@s.hidden name="${prefix}Proxies[${proxy_index}].institution.id" id="${prefix}institution_id${proxy_index}"/>
            <div class="control-group">
                <div class="controls controls-row">
                    <@s.textfield theme="tdar" cssClass="institutionAutoComplete institution span4" watermark="Institution Name" placeholder="Institution Name" autocomplete="off"
                        autocompleteName="name" autocompleteIdElement="#${prefix}institution_id${proxy_index}" autocompleteParentElement="#${prefix}Row_${proxy_index}_i"
                        name="${prefix}Proxies[${proxy_index}].institution.name" maxlength="255" />
                    <@s.select theme="tdar" name="${prefix}Proxies[${proxy_index}].role" 
                        listValue='label'
                        list=relevantInstitutionRoles
                        cssClass="creator-role-select span3"
                         />
                </div>
            </div>
            </span>
        </td>
        <td>
            <button class="btn  btn-mini repeat-row-delete " type="button" tabindex="-1" onclick="deleteParentRow(this)"><i class="icon-trash"></i></button>
        </td>
    </tr>
    </#if>
</#macro>


<#macro identifiers showInherited=true>
    <#local _resourceAnnotations = resourceAnnotations />
    <#if _resourceAnnotations.empty>
    <#local _resourceAnnotations = [blankResourceAnnotation] />
    </#if>
    <div class="well" id="divIdentifiersGlide" tiplabel="${resource.resourceType.label} Specific or Agency Identifiers" tooltipcontent="#divIdentifiersTip">
        <div id="divIdentifiersTip" class="hidden">
            <div>
                <dl>
                    <dt>Name</<dt>
                    <dd>Description of the following agency or ${resource.resourceType.label} identifier (e.g. <code>ASU Accession Number</code> or <code>TNF Project Code</code>).</dd>
                    <dt>Value</<dt>
                    <dd>Number, code, or other identifier (e.g. <code>2011.045.335</code> or <code>AZ-123-45-10</code>).</dd>
                </dl> 
            </div>
        </div>
        <legend>${resource.resourceType.label} Specific or Agency Identifiers</legend>
        <@inheritsection checkboxId="cbInheritingIdentifierInformation" name='resource.inheritingIdentifierInformation' showInherited=showInherited />
        <div id="divIdentifiers">
        <table id="resourceAnnotationsTable" class="table repeatLastRow" addAnother="add another identifier" >
            <tbody>
                <#list _resourceAnnotations as annotation>
                    <@displayAnnotation annotation annotation_index/>
                </#list>
            </tbody>
        </table>
        </div>
    </div>

</#macro>

<#macro displayAnnotation annotation annotation_index=0>
    <tr id="resourceAnnotationRow_${annotation_index}_" class="repeat-row">
        <td >
            <div class="control-group">
            <label class="control-label">Name / Value</label>
                <div class="controls controls-row ">
                    <@s.textfield theme="tdar" placeholder="Name" cssClass="annotationAutoComplete span3" name='resourceAnnotations[${annotation_index}].resourceAnnotationKey.key' value='${annotation.resourceAnnotationKey.key!""}'  autocomplete="off" />
                    <@s.textfield theme="tdar" placeholder="Value" cssClass="span4" name='resourceAnnotations[${annotation_index}].value'  value='${annotation.value!""}' />
                </div>
            </div>            
        </td>
        <td><@clearDeleteButton id="resourceAnnotationRow" /></td>                        
    </tr>

</#macro>
<#macro join sequence delimiter=",">
  <#if sequence??>
    <#list sequence as item>
        ${item}<#if item_has_next><#noescape>${delimiter}</#noescape></#if><#t>
    </#list>
  </#if>
</#macro>

<#-- 
FIXME: this appears to only be used for Datasets.  Most of it has been extracted out
to singleFileUpload, continue lifting useful logic here into singleFileUpload (e.g.,
jquery validation hooks?)
-->
<#macro upload uploadLabel="File" showMultiple=false divTitle="Upload File" showAccess=true>
    <@sharedUploadFile>
      <@singleFileUpload>
          <div class="field indentFull">
          <@s.select name="fileProxies[0].restriction" id="cbConfidential" labelposition="right" label="This item has access restrictions" listValue="label" list=fileAccessRestrictions  />
          <div><b>NOTE:</b> by changing this from 'public', only the metadata will be visible to users, they will not be able to view this item.  
          You may explicity grant read access to users below.</div>
          <br />     
          </div>
      </@singleFileUpload>
    </@sharedUploadFile>
</#macro>


<#macro asyncFileUpload uploadLabel="Attach Files" showMultiple=false divTitle="Upload" divId="divFileUpload" >
<div id="${divId}" class="well">
    <@s.hidden name="ticketId" id="ticketId" />
    <legend>${uploadLabel}</legend>
    <div class="row fileupload-buttonbar">
        <div class="span2">
            <!-- The fileinput-button span is used to style the file input field as button -->
            <span class="btn btn-success fileinput-button">
                <i class="icon-plus icon-white"></i>
                <span>Add files...</span>
            <input type="file" name="uploadFile" multiple="multiple" 
                data-form-data='{"ticketId":$("#ticketId").val()}'>
            </span>
            <#-- we don't want the 'bulk operations' for now,  might be handy later -->
            <#--
            <button type="submit" class="btn btn-primary start">
                <i class="icon-upload icon-white"></i>
                <span>Start upload</span>
            </button>
            <button type="reset" class="btn btn-warning cancel">
                <i class="icon-ban-circle icon-white"></i>
                <span>Cancel upload</span>
            </button>
            <button type="button" class="btn btn-danger delete">
                <i class="icon-trash icon-white"></i>
                <span>Delete</span>
            </button>
            <input type="checkbox" class="toggle">
           -->
        </div>
    <#if validFileExtensions??>
    <span class="help-block">
        Accepted file types: .<@join validFileExtensions ", ." />
    </span>
    </#if>
        <!-- The global progress information -->
        <div class="span5 fileupload-progress fade">
            <!-- The global progress bar -->
            <div class="progress progress-success progress-striped active" role="progressbar" aria-valuemin="0" aria-valuemax="100">
                <div class="bar" style="width:0%;"></div>
            </div>
            <!-- The extended global progress information -->
            <div class="progress-extended">&nbsp;</div>
        </div>
    </div>
    <!-- The loading indicator is shown during file processing -->
    <div class="fileupload-loading"></div>
    <br />
        <!-- The table listing the files available for upload/download -->
        <table role="presentation" class="table table-striped">
            <thead>
               <th><!--preview-->&nbsp;</th>
               <th>Name</th>
               <th>Size</th>
               <th colspan="2">Access Restrictions</th>
               <th colspan="2">Action</th>
            </thead>
            <tbody class="files"></tbody>
        </table>
    
</div>
</#macro>

<#macro fileProxyRow rowId="{ID}" filename="{FILENAME}" filesize="{FILESIZE}" action="ADD" fileid=-1 versionId=-1>
<tr id="fileProxy_${rowId}" class="${(fileid == -1)?string('newrow', '')} sortable">
<td class="fileinfo">
    <div class="width99percent">
            <#if fileid == -1>
                <b class="filename replacefilename" title="{FILENAME}">{FILENAME}</b> 
            <#else>
                <b>Existing file:</b> <a class='filename' href="<@s.url value='/filestore/${versionId?c}/get'/>" title="${filename?html}"><@truncate filename 45 /></a>
            </#if>
    
        <span style='font-size: 0.9em;'>(${filesize} bytes)</span>

        <input type="hidden" class="fileAction" name="fileProxies[${rowId}].action" value="${action}"/>
        <input type="hidden" class="fileId" name="fileProxies[${rowId}].fileId" value="${fileid?c}"/>
        <input type="hidden" class="fileReplaceName" name="fileProxies[${rowId}].filename" value="${filename}"/>
        <input type="hidden" class="fileSequenceNumber" name="fileProxies[${rowId}].sequenceNumber" value=${rowId} />

    </div>
    <#if multipleFileUploadEnabled>
    <div class="width99percent field proxyConfidentialDiv">
        <label for="proxy${rowId}_conf">Access Restrictions</label>
        <@s.select id="proxy${rowId}_conf"  name="fileProxies[${rowId}].restriction" labelposition="right" 
        style="padding-left: 20px;" list=fileAccessRestrictions listValue="label"  
        onclick="updateFileAction('#fileProxy_${rowId}', 'MODIFY_METADATA');showAccessRightsLinkIfNeeded();" cssClass="fileProxyConfidential"/>
    </div>
    </#if>
    <#nested />
</td>
<td>
    <button id='deleteFile_${rowId}' onclick="deleteFile('#fileProxy_${rowId}', ${(fileid == -1)?string}, this);return false;"  type="button"
    class="deleteButton file-button cancel ui-button ui-widget ui-state-default ui-corner-all ui-button-text-icon-primary" role="button">
    <span class="ui-button-icon-primary ui-icon ui-icon-cancel"></span><span class="ui-button-text">delete</span></button><br/>
    <#if fileid != -1>
    <button onclick="replaceDialog('#fileProxy_${rowId}','${filename}');return false;"  type="button"
    class="replaceButton file-button cancel ui-button ui-widget ui-state-disabled ui-corner-all ui-button-text-icon-primary" role="button" disabled=disabled>
    <#-- replace with ui-icon-transferthick-e-w ? -->
    <span class="ui-button-icon-primary ui-icon"></span><span class="ui-button-text">replace</span></button>
    </#if>
</td>
</tr>
</#macro>

<#macro citationInfo prefix="resource" includeAbstract=true >
     <#if !resource.resourceType.codingSheet && !resource.resourceType.ontology>
<div id="citationInformation" class="well"> 
    <legend>Additional Citation Information</legend>

        <span id="publisher-hints" 
            book="Publisher" 
            book_section="Publisher"
            journal_article="Publisher" 
            conference="Conference"
            thesis="Institution"
            other="Publisher"></span>
        <span id="publisherLocation-hints" style="display:none"
            book="Publisher Loc." 
            book_section="Publisher Loc." 
            journal_article="Publisher Loc."
            conference="Location" 
            thesis="Department"
            other="Publisher Loc."></span>

    <p tiplabel="Department / Publisher Location" tooltipcontent="Department name, or City,State (and Country, if relevant)"></p>
    <@s.textfield id='publisher' label="Publisher" name='publisherName' cssClass="institution"  />

    <@s.textfield id='publisherLocation' label="Publisher Loc." name='${prefix}.publisherLocation' cssClass='longfield' />

    <#nested />

    <div id="divUrl" tiplabel="URL" tooltipcontent="Website address for this resource, if applicable"></div>
    <@s.textfield name="${prefix}.url" id="txtUrl" label="URL" labelposition="left" cssClass="longfield url" />
    
</div>
    </#if>
    <#if includeAbstract>
        <@abstractSection "${prefix}" />
    </#if>

    <#if resource.resourceType.label?lower_case != 'project'>
        <@copyrightHolders 'Primary Copyright Holder *' copyrightHolderProxy />
    </#if>
</#macro>

<#macro sharedFormComponents showInherited=true fileReminder=true prefix="${resource.resourceType.label?lower_case}">
    <@organizeResourceSection />
    <#if !resource.resourceType.project>
      <@resourceProvider showInherited />
      <#if licensesEnabled?? && licensesEnabled >
          <@edit.license />
      </#if>
      <#if copyrightEnabled??>
          <@edit.copyrightHolders />
      </#if>
    </#if>
    <@resourceCreators 'Individual and Institutional Roles' creditProxies 'credit' false showInherited />

    <@identifiers showInherited />

    <@spatialContext showInherited />
    <@temporalContext showInherited />

    <@investigationTypes showInherited />
    
    <@materialTypes showInherited />

    <@culturalTerms showInherited />

    <@siteKeywords showInherited />
    
    <@generalKeywords showInherited />

    <@resourceNoteSection showInherited />

    <#if !resource.resourceType.document>
      <@relatedCollections showInherited />
    </#if>
    
    <@edit.fullAccessRights />
    
    <#if !resource.resourceType.project>
      <@edit.submit fileReminder=((resource.id == -1) && fileReminder) />
    <#else>
      <@edit.submit fileReminder=false />
    </#if>
</#macro>

<#macro title>
<#if resource.id == -1>
<title>Create a new ${resource.resourceType.label}</title>
<#else>
<title>Editing ${resource.resourceType.label} Metadata for ${resource.title} (${siteAcronym} id: ${resource.id?c})</title>
</#if>
</#macro>

<#macro sidebar>
<div id="sidebar" parse="true">
    <div id="notice">
    <h3>Introduction</h3>
    This is the page for editing metadata associated with ${resource.resourceType.plural}.
    </div>
</div>
</#macro>


<#macro inheritTips id>
    <div id="${id}hint" class="inherit-tips">
        <em>Note: This section supports <strong>inheritance</strong>: values can be re-used by resources associated with your project.</em>
    </div>
</#macro>


<#macro resourceDataTable showDescription=true selectable=false>
<div>
<@s.textfield name="query" id="query" label="Title" cssClass='input-xxlarge' /><br/>
<div class="row">
<div class="span4">
<label for="project-selector">Project:</label>
<select id="project-selector">
    <option value="" selected='selected'>All Editable Projects</option>
  <#if allSubmittedProjects?? && !allSubmittedProjects.empty>
  <optgroup label="Your Projects">
    <@s.iterator value='allSubmittedProjects' status='projectRowStatus' var='submittedProject'>
        <option value="${submittedProject.id?c}" title="${submittedProject.title!""?html}"><@truncate submittedProject.title 70 /> </option>
    </@s.iterator>
  </optgroup>
  </#if>
  
  <optgroup label="Projects you have been given access to">
    <@s.iterator value='fullUserProjects' var='editableProject'>
        <option value="${editableProject.id?c}" title="${editableProject.title!""?html}"><@truncate editableProject.title 70 /></option>
    </@s.iterator>
  </optgroup>
</select>
</div>
<div class="span4">
<label for="collection-selector">Collection:</label>
<select id="collection-selector">
    <option value="" selected='selected'>All Collections</option>
    <@s.iterator value='resourceCollections' var='rc'>
        <option value="${rc.id?c}" title="${rc.name!""?html}"><@truncate rc.name!"(No Name)" 70 /></option>
    </@s.iterator>
</select>
</div>
</div>
<div class="row">
	<div class="span4">
	    <@s.select labelposition='left' id="statuses" headerKey="" headerValue="Any" label='Status' name='status'  emptyOption='false' listValue='label' list='%{statuses}'/></span>
	</div>
	<div class="span4">
	    
	    <@s.select labelposition='left' id="resourceTypes" label='Resource Type' name='resourceType'  headerKey="" headerValue="All" emptyOption='false' listValue='label' list='%{resourceTypes}'/></span>
	</div>
</div>

    <@s.select labelposition='left' label='Sort By' emptyOption='false' name='sortBy' 
     listValue='label' list='%{resourceDatatableSortOptions}' id="sortBy"
     value="ID_REVERSE" title="Sort resource by" />

<!-- <ul id="proj-toolbar" class="projectMenu"><li></li></ul> -->
</div>
<table cellpadding="0" cellspacing="0" border="0" class="display tableFormat" id="resource_datatable" width="650px">
<thead>
     <tr>
         <#if selectable><th><input type="checkbox" onclick="checkAllToggle()" id="cbCheckAllToggle">id</th></#if>
         <th>Title</th>
         <th>Type</th>
     </tr>
</thead>
<tbody>
</tbody>
</table>
<br/>
<script>
function checkAllToggle() {
var unchecked = $('#resource_datatable td input[type=checkbox]:unchecked');
var checked = $('#resource_datatable td input[type=checkbox]:checked');
  if (unchecked.length > 0) {
    $(unchecked).click();
  } else {
    $(checked).click();
  }
}

</script>

</#macro>


<#macro resourceDataTableJavascript showDescription=true selectable=false >
<script type="text/javascript">

 function projToolbarItem(link, image, text) {
    return '<li><a href="' + link + '"><img alt="toolbar item" src="' + image + '"/>' + text + '</a></li>';
 }
 
 
 
$(function() {
    // set the project selector to the last project viewed from this page
    // if not found, then select the first item 
    var prevSelected = $.cookie("tdar_datatable_selected_project");
    if (prevSelected != null) {
        var elem = $('#project-selector option[value=' + prevSelected + ']');
        if(elem.length) {
            elem.attr("selected", "selected");
        } else {
            $("#project-selector").find("option :first").attr("selected", "selected");
        }

    }
    drawToolbar($("#project-selector").val());
    var prevSelected = $.cookie("tdar_datatable_selected_collection");
    if (prevSelected != null) {
        var elem = $('#collection-selector option[value=' + prevSelected + ']');
        if(elem.length) {
            elem.attr("selected", "selected");
        } else {
            $("#collection-selector").find("option :first").attr("selected", "selected");
        }

    }

});
 
var $dataTable = null; //define at page-level, set after onload

$(function(){
    var isAdministrator = ${administrator?string};
    var isSelectable = ${selectable?string};
    jQuery.fn.dataTableExt.oPagination.iFullNumbersShowPages =3;
    
    $dataTable = registerLookupDataTable({
        tableSelector: '#resource_datatable',
        sAjaxSource:'/lookup/resource',
        "bLengthChange": true,
        "bFilter": false,
        aoColumns: [
          <#if selectable>{ "mDataProp": "id", tdarSortOption: "ID", sWidth:'5em' ,"bSortable":false},</#if>
          { "mDataProp": "title",  sWidth: '65%', fnRender: fnRenderTitle, bUseRendered:false ,"bSortable":false},
          { "mDataProp": "resourceTypeLabel",  sWidth: '15%',"bSortable":false }
        ],
        sDom:'<"datatabletop"ilrp>t<>', //omit the search box
        sPaginationType:"full_numbers",
        sAjaxDataProp: 'resources',
        requestCallback: function(searchBoxContents){
                return {title: searchBoxContents,
                    'resourceTypes': $("#resourceTypes").val() == undefined ? "" : $("#resourceTypes").val(),
                    'includedStatuses': $("#statuses").val() == undefined ? "" : $("#statuses").val() ,
                    'sortField':$("#sortBy").val(),
                    'term':$("#query").val(),
                    'projectId':$("#project-selector").val(),
                    'collectionId':$("#collection-selector").val(),
                     useSubmitterContext: !isAdministrator
            }
        },
        selectableRows: isSelectable,
        rowSelectionCallback: function(id, obj, isAdded){
            if(isAdded) {
                rowSelected(obj);
            } else {
                rowUnselected(obj);
            }
        }
    });

});
 
$(document).ready(function() {
    
    $("#project-selector").change(function() {
        var projId = $(this).val();
        $.cookie("tdar_datatable_selected_project", projId);
        drawToolbar(projId);
        $("#resource_datatable").dataTable().fnDraw();
    });

    $("#collection-selector").change(function() {
        var projId = $(this).val();
        $.cookie("tdar_datatable_selected_collection", projId);
        drawToolbar(projId);
        $("#resource_datatable").dataTable().fnDraw();
    });
    
    $("#resourceTypes").change(function() {
      $("#resource_datatable").dataTable().fnDraw();
      $.cookie($(this).attr("id"), $(this).val());
    });


    $("#statuses").change(function() {
      $("#resource_datatable").dataTable().fnDraw();
      $.cookie($(this).attr("id"), $(this).val());
    });
    
    $("#sortBy").change(function() {
      $("#resource_datatable").dataTable().fnDraw();
      $.cookie($(this).attr("id"), $(this).val());
    });
    
    $("#query").change(function() {
      $("#resource_datatable").dataTable().fnDraw();
      $.cookie($(this).attr("id"), $(this).val());
    });
    
    $("#query").bindWithDelay("keyup", function() {$("#resource_datatable").dataTable().fnDraw();} ,500);

});

function fnRenderTitle(oObj) {
    //in spite of name, aData is an object containing the resource record for this row
    var objResource = oObj.aData;
    var html = '<a href="'  + getURI(objResource.urlNamespace + '/' + objResource.id) + '">' + htmlEncode(objResource.title) + '</a>';
    html += ' (ID: ' + objResource.id 
    if (objResource.status != 'ACTIVE') {
    html += " " + objResource.status;
    }
    html += ')';
    <#if showDescription>
    html += '<br /> <p>' + htmlEncode(objResource.description) + '</p>';
    </#if> 
    return html;
}

function drawToolbar(projId) {
    var toolbar = $("#proj-toolbar");
    toolbar.empty();
    if (projId != undefined && projId != '') {
        toolbar.append(projToolbarItem('/project/' + projId + '/view', '/images/zoom.png', ' View selected project'));
        toolbar.append(projToolbarItem('/project/' + projId + '/edit', '/images/pencil.png', ' Edit project'));
        toolbar.append(projToolbarItem('/resource/add?projectId=' + projId, '/images/database_add.png', ' Add new resource to project'));
    }
}

</script>
</#macro>


<#macro checkedif arg1 arg2><#t>
<@valif "checked='checked'" arg1 arg2 />
</#macro>

<#macro selectedif arg1 arg2>
<@valif "selected='selected'" arg1 arg2 />
</#macro>

<#macro valif val arg1 arg2><#t>
<#if arg1=arg2>${val}</#if><#t>
</#macro>

<#macro boolfield name label id  value labelPosition="left" type="checkbox" labelTrue="Yes" labelFalse="No" cssClass="">
    <@boolfieldCheckbox name label id  value labelPosition cssClass />
</#macro>

<#macro boolfieldCheckbox name label id value labelPosition cssClass>
<#if value?? && value?string == 'true'>
    <@s.checkbox name="${name}" label="${label}" labelPosition="${labelPosition}" id="${id}"  value=value cssClass="${cssClass}" 
        checked="checked"/>
<#else>
    <@s.checkbox name="${name}" label="${label}" labelPosition="${labelPosition}" id="${id}"  value=value cssClass="${cssClass}" />
</#if>
</#macro>

<#macro boolfieldRadio name label id value labelPosition labelTrue labelFalse>
    <label>${label}</label>
    <input type="radio" name="${name}" id="${id}-true" value="true"  <@checkedif true value />  />
    <label for="${id}-true" class="datatable-cell-unstyled"> ${labelTrue}</label>
    <#if (labelPosition=="top")><br />
    <input type="radio" name="${name}" id="${id}-false" value="false" <@checkedif false value />   />
    <label for="${id}-false" class="datatable-cell-unstyled"> ${labelFalse}</label>
    <#else>
    <input type="radio" name="${name}" id="${id}-false" value="false"   />
    <label for="${id}-false" class="datatable-cell-unstyled"> ${labelFalse}</label>
    </#if>
</#macro>

<#macro boolfieldSelect name label id value labelPosition labelTrue labelFalse>
    <label>${label}</label>
    <select id="${id}" name="${name}">
    <#if (labelPosition=="top")><br /></#if>
        <option id="${id}-true" value="true" <@selectedif true value/> />${labelTrue}</option>
        <option id="${id}-false" value="false" <@selectedif false value/> />${labelFalse}</option>
    </select>
</#macro>

<#macro repeat count>
    <#list 1..count as idx> <#t>
        <#nested><#t>
    </#list><#t>
</#macro>


<#macro keywordNodeOptions node selectedKeyword>
    <option>not implemented</option>
</#macro>

<#macro keywordNodeSelect label node selectedKeywordId selectTagId='keywordSelect'>
    <label for="${selectTagId}">${label}</label>
    <select id="${selectTagId}">
        <@keywordNodeOptions node selectedKeywordId />
    </select>
</#macro>


<#macro copyrightHolders sectionTitle copyrightHolderProxy inline=false showInherited=false>
<#if copyrightMandatory>
    <#-- 
    FIXME replace with shared macros wherever possible:
    Martin & Daniel: This is a derivation of the creatorProxyRow macro, but the functionality is slightly different. The container here is not going to 
    allow for an arbitrary number of entries. We haven't been able to spend the time working out how to do this without significantly complicating the 
    creatorProxyRow and creatorProxy.
    We will hopefully do the FIXME when we have the time.
    -->
    <#if !inline>
        <div class="glide" tiplabel="Primary Copyright Holder" tooltipcontent="Use this field to nominate a primary copyright holder. Other information about copyright can be added in the 'notes' section by creating a new 'Rights & Attribution note.">
            <h3>${sectionTitle}</h3>
    <#else>
        <label class="toplabel">${sectionTitle}</label> <br />
    </#if>
        <div>
        <#assign creatorType  =copyrightHolderProxy.actualCreatorType!"PERSON" />
        <table id="copyrightHolderTable" class="tableFormat">
        <tr><td>
            <input type="radio" id="copyright_holder_type_person" name="copyrightHolderType" value="Person" <#if creatorType=='PERSON'>checked='checked'</#if> />
            <label for="copyright_holder_type_person">Person</label>
            <input type="radio" id="copyright_holder_type_institution" name="copyrightHolderType" value="Institution" <#if creatorType=='INSTITUTION'>checked='checked'</#if> />
            <label for="copyright_holder_type_institution">Institution</label>
        </td></tr>
        <tr><td>
            <div class="creatorInstitution <#if creatorType =='PERSON'>hidden</#if>" id="copyrightInstitution">
            <span class="creatorInstitution" id="copyrightInstitution">
                <span class="smallLabel">Institution</span>
                <@s.hidden name="copyrightHolderProxy.institution.id" id="copyright_institution_id" value="${(copyrightHolderProxy.institution.id)!}"/>
                <div class="width60percent marginLeft10">
                    <#if creatorType=='INSTITUTION'><#assign institution_name_required="required"/></#if>
                        <@s.textfield id="copyright_holder_institution_name" cssClass="institutionAutoComplete institution ${institution_name_required!}" watermark="Institution Name"
                            autocompleteName="name" autocompleteIdElement="#copyright_institution_id" autocompleteParentElement="#copyrightInstitution"
                            name="copyrightHolderProxy.institution.name" value="${(copyrightHolderProxy.institution.name)!}" maxlength="255" 
                            title="Please enter a copyright holder institution" />
                </div>
            </span>
            </div>
            
            <div class="creatorPerson <#if creatorType=='INSTITUTION'>hidden</#if>" id="copyrightPerson">
            <span class="creatorPerson" id="copyrightPerson">
                <span class="smallLabel">Person</span>
                <div class="width30percent marginLeft10" >
                    <#if creatorType=='PERSON'><#assign person_name_required="required"/></#if>
                    <@s.hidden name="copyrightHolderProxy.person.id" id="copyright_person_id" onchange="this.valid()"  autocompleteParentElement="#copyrightPerson"  />
                    <@s.textfield id="copyright_holder_person_last_name" cssClass="nameAutoComplete ${person_name_required!}" watermark="Last Name"
                        autocompleteName="lastName" autocompleteIdElement="#copyright_person_id" autocompleteParentElement="#copyrightPerson"
                        name="copyrightHolderProxy.person.lastName" maxlength="255" autocomplete="off"
                        title="Please enter the copyright holder's last name" />
                    <@s.textfield id="copyright_holder_person_first_name" cssClass="nameAutoComplete ${person_name_required!}" watermark="First Name"
                        autocompleteName="firstName" autocompleteIdElement="#copyright_person_id" autocompleteParentElement="#copyrightPerson"
                        name="copyrightHolderProxy.person.firstName"  maxlength="255" autocomplete="off"
                        title="Please enter the copyright holder's first name" />
                    <@s.textfield cssClass="nameAutoComplete" watermark="Email"
                        autocompleteName="email" autocompleteIdElement="#copyright_person_id" autocompleteParentElement="#copyrightPerson"
                        name="copyrightHolderProxy.person.email" maxlength="255" autocomplete="off"/>
                    <br />
                </div>
                <div class="width60percent marginLeft10">
                    <@s.textfield id="copyright_holder_institution_name" cssClass="nameAutoComplete" watermark="Institution Name"
                        autocompleteName="institution" autocompleteIdElement="#copyright_person_id" autocompleteParentElement="#copyrightPerson"
                        name="copyrightHolderProxy.person.institution.name" maxlength="255" />
                </div>
            </span>
            </div>
        </td></tr>
        </table>
        </div>
    <#if !inline>
        </div>
    </#if>
</#if>
</#macro>

<#macro license>
<#assign currentLicenseType = defaultLicenseType/>
<#if resource.licenseType?has_content>
    <#assign currentLicenseType = resource.licenseType/>
</#if>
<div class="glide" id="license_section">
        <h3>License</h3>
<@s.radio name='resource.licenseType' groupLabel='License Type' emptyOption='false' listValue="label" 
    list='%{licenseTypesList}' numColumns="1" cssClass="licenseRadio" value="%{'${currentLicenseType}'}" />

    <table id="license_details">
    <#list licenseTypesList as licenseCursor>
        <#if (licenseCursor != currentLicenseType)>
            <#assign visible="hidden"/>
        <#else>
            <#assign visible="">
        </#if>
        <tr id="license_details_${licenseCursor}" class="${visible}">
                <td>
                    <#if (licenseCursor.imageURI != "")>
                        <a href="${licenseCursor.URI}" target="_blank"><img src="${licenseCursor.imageURI}"/></a>
                    </#if>
                </td>
                <td>
                    <h4>${licenseCursor.licenseName}</h4>
                    <p>${licenseCursor.descriptionText}</p>
                    <#if (licenseCursor.URI != "")>
                        <p><a href="${licenseCursor.URI}" target="_blank">view details</a></p>
                    <#else>
                        <p><label style="position: static"  for="licenseText">License text:</label></p>
                        <p><@s.textarea id="licenseText" name='resource.licenseText' rows="3" cols="60" /></p>
                    </#if>
                </td>
            </tr>
    </#list>
    </table>
</div>
</#macro>

<#macro asyncUploadTemplates formId="resourceMetadataForm">

<!-- The template to display files available for upload (uses tmpl.min.js) -->
<script id="template-upload" type="text/x-tmpl">
{% for (var i=0, file; file=o.files[i]; i++) { %}
    <tr class="template-upload fade">
        <td class="preview"><span class="fade"></span></td>
        <td class="name"><span>{%=file.name%}</span></td>
        <td class="size"><span>{%=o.formatFileSize(file.size)%}</span></td>
        {% if (file.error) { %}
            <td class="error" colspan="2"><span class="label label-important">{%=locale.fileupload.error%}</span> {%=locale.fileupload.errors[file.error] || file.error%}</td>
        {% } else if (o.files.valid && !i) { %}
            <td>
                <div class="progress progress-success progress-striped active" role="progressbar" aria-valuemin="0" aria-valuemax="100" aria-valuenow="0"><div class="bar" style="width:0%;"></div></div>
            </td>
            <td class="start">{% if (!o.options.autoUpload) { %}
                <button class="btn btn-primary">
                    <i class="icon-upload icon-white"></i>
                    <span>{%=locale.fileupload.start%}</span>
                </button>
            {% } %}</td>
        {% } else { %}
            <td colspan="2"></td>
        {% } %}
        <td class="cancel">{% if (!i) { %}
            <button class="btn btn-warning">
                <i class="icon-ban-circle icon-white"></i>
                <span>{%=locale.fileupload.cancel%}</span>
            </button>
        {% } %}</td>
    </tr>
{% } %}
</script>

<!-- The template to display files available for download (uses tmpl.min.js) -->

<script id="template-download" type="text/x-tmpl">
{% for (var i=0, file; file=o.files[i]; i++) { %}
{% var idx = '' + i;%}
{% var rowclass = file.fileId ? "existing-file" : "new-file" ;%}
    <tr class="template-download fade {%=rowclass%}">
        {% if (file.error) { %}        
            <td></td>
            <td class="name"><span>{%=file.name%}</span></td>
            <td class="size"><span>{%=o.formatFileSize(file.size)%}</span></td>
            <td class="error" colspan="2"><span class="label label-important">{%=locale.fileupload.error%}</span> {%=locale.fileupload.errors[file.error] || file.error%}</td>
        {% } else { %}
            <td class="preview"></td>
            <td class="name">
                {% if (file.url) { %}        
                <a href="{%=file.url%}" title="{%=file.name%}" rel="{%=file.thumbnail_url&&'gallery'%}" download="{%=file.name%}">{%=file.name%}</a>
                {% } else { %}
                {%=file.name%}
                {% } %} 
                <span class="replacement-text" style="display:none"></span>
            </td>
            <td class="size"><span>{%=o.formatFileSize(file.size)%}</span></td>
            <td colspan="2">
                <@s.select id="proxy{%=idx%}_conf"  name="fileProxies[{%=idx%}].restriction" 
                style="padding-left: 20px;" list=fileAccessRestrictions listValue="label"  
                onchange="TDAR.fileupload.updateFileAction(this)" 
                cssClass="fileProxyConfidential"/>
            </td>
        {% } %}
        <td class="delete">
                <button class="btn btn-danger delete-button" data-type="{%=file.delete_type%}" data-url="{%=file.delete_url%}">
                    <i class="icon-trash icon-white"></i>
                    <span>{%=locale.fileupload.destroy%}</span>
                </button>
        </td>
        <td>
            {%if (file.fileId) { %}
               <#--
                <button class="btn btn-warning disabled replace-button" disabled="disabled">
                    <i class="icon-retweet icon-white"></i>
                    <span>Replace</span>
                </button>
                -->
                  <div class="btn-group">
                    <button class="btn btn-warning disabled dropdown-toggle replace-button" disabled="disabled" data-toggle="dropdown">Replace <span class="caret"></span></button>
                    <ul class="dropdown-menu" id="tempul">
                      <li><a href="#">file 1</a></li>
                      <li><a href="#">file 2</a></li>
                      <li class="divider"></li>
                      <li><a href="#">cancel replace operation</a></li>
                    </ul>
                  </div> 
            {% } %}
            
            
            <#-- TODO: this widget has a "bulk actions" convention, but I don't want it.  is it safe to remove the html,  or do we also need to handle this in javascript --> 
            <#-- <input type="checkbox" name="delete"  value="1"> -->


            <input type="hidden" class="fileAction" name="fileProxies[{%=idx%}].action" value="{%=file.action||'ADD'%}"/>
            <input type="hidden" class="fileId" name="fileProxies[{%=idx%}].fileId" value="{%=file.fileId%}"/>
            <input type="hidden" class="fileReplaceName" name="fileProxies[{%=idx%}].filename" value="{%=file.name%}"/>
            <input type="hidden" class="fileSequenceNumber" name="fileProxies[{%=idx%}].sequenceNumber" value="{%=idx%}"/>
            
        </td>
    </tr>
{% } %}
</script>

<script id="template-replace-menu" type="text/x-tmpl">
{% for(var i = 0, row; row = o.jqnewfiles[i]; i++) { %}
   <li><a href="#" 
            class="replace-menu-item"
            data-action="rename"
            data-filename="{%=$('.fileReplaceName', row).val()%}" 
            data-target="" >{%=$('.fileReplaceName', row).val()%}</a></li>
{% } %}
   <li class="divider"></li> 
   <li><a href="#" class="cancel" >Cancel previous operation</a></li>
</script>
</#macro>

<#macro acceptedFileTypesRegex>
/\.(<@join sequence=validFileExtensions delimiter="|"/>)$/i<#t>
</#macro>


</#escape>