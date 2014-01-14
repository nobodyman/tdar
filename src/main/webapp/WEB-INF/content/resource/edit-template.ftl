-<#escape _untrusted as _untrusted?html>
<#--
   This template is designed to try and reduce duplicated code in each of the tDAR resource-edit pages.  As we have almost 10 of them, 
   it becomes a challenge to maintain them in parallel without introducing bugs. The goal and function of this template is to (a) centralize logic
   (b) set basic defaults (c) provide variables to turn on/off various settings and (d) allow for methods to override functions as needed.

-->

<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "/WEB-INF/macros/resource/common.ftl" as common>
<#import "/WEB-INF/content/${namespace}/edit.ftl" as local_ />
<#-- We define local_ as a reference to the actual template in question. It's going to define for us any functions/methods that get overriden 
	 in the form. if local_.method?? && local_.method?is_macro ... then execute it...  -->

<#import "/${themeDir}/local-helptext.ftl" as  helptext>
<#-- helptext can be overriden by the theme so we import it, it, in turn override the default helptext -->
<head>
<@edit.title />

<meta name="lastModifiedDate" content="$Date$"/>

</head>
<body>
<@edit.sidebar />
<@edit.subNavMenu>
	<#-- include local scrollspy menu details -->
	<#if local_.subNavMenu?? && local_.subNavMenu?is_macro>
		<@local_.subNavMenu />
	</#if>

</@edit.subNavMenu>

<#-- allow for overriding of the page title -->
<#if  local_.customH1?? && local_.customH1?is_macro>
	<@local_.customH1 />
<#else>
	<#assign newTitle>New <#noescape>${resource.resourceType.label}</#noescape></#assign>
	<h1><#if resource.id == -1>Creating<#else>Editing</#if>:<span> <#if resource.title?has_content>${resource.title}<#else>${newTitle}</#if> </span></h1>
</#if>

<#assign fileReminder=true />
<#assign prefix="${resource.resourceType.label?lower_case}" />

<@s.form name='metadataForm' id='metadataForm'   cssClass="form-horizontal" method='post' enctype='multipart/form-data' action='save'>
	<@common.jsErrorLog />

	<#-- custom section ahead of the basic information -->
    <#if local_.topSection?? && local_.topSection?is_macro>
		<@local_.topSection />
	</#if>

	<div class="well-alt" id="basicInformationSection">
	    <h2>Basic Information</h2>
	
	  <#if resource.id?? &&  resource.id != -1>
	      <@s.hidden name="id"  value="${resource.id?c}" />
	  </#if>
	  
	  <@s.hidden name="startTime" value="${currentTime?c}" />
	
	        <div id="spanStatus" data-tooltipcontent="#spanStatusToolTip" class="control-group">
		        <#if editor && !administrator>
			        <p><b>note:</b> because you are an "editor" we've defaulted your default resource status to DRAFT</p>
		        </#if>
	            <label class="control-label">Status</label>
	            <div class="controls">
		            <#if guestUserId != -1 && guestUserId == authenticatedUser.id>
		            	<select name="status">
		            		<option value='DRAFT' selected>Draft</option>
		            	</select>
					<#else>
	                	<@s.select theme="tdar" value="resource.status" name='status'  emptyOption='false' listValue='label' list='%{statuses}'/>
					</#if>
	                <#if resource.resourceType.project><span class="help-block">Note: project status does not affect status of child resources.</span></#if>
	            </div>  
	        </div>
	    
	        <@helptext.status />

	<#-- the bulk upload hides some common fields, but the validator requires them, so we set the values to hidden ones -->
	<#if bulkUpload >
	
	    <@s.hidden labelposition='left' id='resourceTitle' label='Title' name='image.title' cssClass="" value="BULK_TEMPLATE_TITLE"/>
	    <@s.hidden labelposition='left' id='dateCreated' placeholder='YYYY' label='Year Created' name='image.date' cssClass="" value="-100"/>
	    <@s.hidden id='ImageDescription' name='image.description' value="placeholder description"/>
	
	<#else>
	    <div data-tiplabel="Title"
	    data-tooltipcontent="Enter the entire title, including sub-title, if appropriate.">
	        <@s.textfield label="Title" id="resourceRegistrationTitle"  
	            title="A title is required for all ${resource.resourceType.label}s" name='${itemPrefix}.title'
	             cssClass="required descriptiveTitle input-xxlarge" required=true maxlength="512"/>
	    </div>
	    <#if resource.resourceType != 'PROJECT'>
	    <div data-tiplabel="Year" data-tooltipcontent="Four digit year, e.g. 1966 or 2005.">
	        <#assign dateVal = ""/>
	        <#if resource.date?? && resource.date != -1>
	        <#assign dateVal = resource.date?c />
	        </#if>
	        <@s.textfield label="Year" id='dateCreated' name='${itemPrefix}.date' value="${dateVal}" cssClass="reasonableDate required input-mini" required=true
	          maxlength=7 title="Please enter the year this ${resource.resourceType.label} was created" />
	    </div>
	    </#if>
	</#if>

	<#-- add custom basic information if needed -->	
	<#if local_.basicInformation?? && local_.basicInformation?is_macro>
		<@local_.basicInformation />
	</#if>

	<#-- if we're an editor or administrator, we allow them to set the submitter of the resource to 'not them' -->
    <#if editor>
        <div class="control-group" id="divSubmitter">
            <label class="control-label">Submitter</label>
            <div class="controls controls-row">
            <#if submitter?has_content>
                <@edit.registeredUserRow person=submitter isDisabled=disabled   _personPrefix="" _indexNumber='' 
                    prefix="submitter" includeRights=false includeRepeatRow=false />
 	        <#else>
                <@edit.registeredUserRow person=authenticatedUser isDisabled=disabled   _personPrefix="" _indexNumber='' 
                    prefix="submitter" includeRights=false includeRepeatRow=false />
            </#if>
	       </div>
       </div>
     </#if>
	</div>
	
	<@edit.accountSection />

	<#if !resource.resourceType.project>
	<@edit.resourceCreators '${resource.resourceType.label} Creators' authorshipProxies 'authorship' />
	</#if>

	<div id="citationInformation" class="well-alt"> 
	    <h2>Additional Citation Information</h2>
	
     <#if resource.resourceType.hasLanguage && !resource.resourceType.document >
		<@s.select labelposition='left' label='Language'  name='resourceLanguage'  emptyOption='false' listValue='label' list='%{languages}'/>
     </#if>

	<#-- ontologies and coding sheets have fewer fields -->
     <#if !resource.resourceType.codingSheet && !resource.resourceType.ontology>
	
	    <#if !resource.resourceType.project>
	    <div data-tiplabel="Department / Publisher Location" data-tooltipcontent="Department name, or City,State (and Country, if relevant)">
	        <span id="publisher-hints"  book="Publisher" book_section="Publisher" journal_article="Publisher"  conference_presentation="Conference" thesis="Institution" other="Publisher">
	            <@s.textfield id='publisher'  maxlength=255 label="Publisher" name='publisherName' cssClass="institution input-xxlarge"  />
	        </span>
	
	        <span id="publisherLocation-hints" book="Publisher Loc." book_section="Publisher Loc." journal_article="Publisher Loc." conference_presentation="Location"  thesis="Department" other="Publisher Loc.">
	            <@s.textfield id='publisherLocation'  maxlength=255 label="Publisher Loc." name='${itemPrefix}.publisherLocation' cssClass='input-xxlarge' />
	        </span>
	    </div>
	    </#if>

		<#-- if the resource type include citation information -->	
		<#if local_.citationInformation?? && local_.citationInformation?is_macro>
			<@local_.citationInformation />
		</#if>
	
	    <div id="divUrl" data-tiplabel="URL" data-tooltipcontent="Website address for this resource, if applicable">
	        <@s.textfield name="${itemPrefix}.url"  maxlength=255 id="txtUrl" label="URL" labelposition="left" cssClass="url input-xxlarge" placeholder="http://" />
	    </div>
	    
    </#if>
	</div>
    
	<#if !bulkUpload >
		<div class="well-alt">
		    <h2>Abstract / Description</h2>
		    <div id="t-abstract" class="clear"
		        data-tiplabel="Abstract / Description"
		        data-tooltipcontent="Short description of the <@edit.resourceTypeLabel />.">
		            <@s.textarea rows="4" id='resourceDescription'  label="Abstract / Description" name='${itemPrefix}.description' cssClass='required resizable resize-vertical input-xxlarge' required=true title="A description is required" />
		    </div>
		</div>
    </#if>

	<#-- if tdarConfiguration has copyright holders enabled -->
    <#if resource.resourceType.label?lower_case != 'project'>
        <@edit.copyrightHolders 'Primary Copyright Holder' copyrightHolderProxies />
    </#if>


	<#-- allow for more content before file uploads -->
	<#if local_.beforeUpload?? && local_.beforeUpload?is_macro>
		<@local_.beforeUpload />
	</#if>
	
	<#-- if the resource allows for file uploads -- this is set to something that's not null -->
	<#if multipleUpload??>
		<#-- if true -- we use the async file upload / otherwise we use the traditional file field -->
		<#if multipleUpload>
			<@edit.asyncFileUpload  uploadLabel="Attach ${resource.resourceType.label} Files" showMultiple=multipleUpload />
		<#else>
			<@edit.upload "${resource.resourceType.label} file" />
		</#if>
	</#if>

	<#-- allow for additional content after the file upload -->	
	<#if local_.localSection?? && local_.localSection?is_macro>
		<@local_.localSection />
	</#if>
	
	
	
	<div class="" id="organizeSection">
	    <#if !resource.resourceType.project>
	    <h2>${siteAcronym} Collections &amp; Project</h2>
	    <h4>Add to a Collection</h4>
	    <@edit.resourceCollectionSection />
	    <@edit.chooseProjectSection />
	    <#else>
	    <h2>${siteAcronym} Collections</h2>
	    <@edit.resourceCollectionSection />
	    </#if>   
	</div>

    <#if !resource.resourceType.project>
      <@edit.resourceProvider inheritanceEnabled />
      <#if licensesEnabled?? && licensesEnabled >
          <@edit.license />
      </#if>
    </#if>


	<#if !(hideCreditSection??)>
	    <@edit.resourceCreators 'Individual and Institutional Roles' creditProxies 'credit'  />
    </#if>
	<@helptext.resourceCreator />

	<#if !(hideKeywordsAndIdentifiersSection??)>
	    <@edit.identifiers inheritanceEnabled />
	
	    <@edit.spatialContext inheritanceEnabled />

	    <@edit.temporalContext inheritanceEnabled />
	
	    <@edit.investigationTypes inheritanceEnabled />
	    
	    <@edit.materialTypes inheritanceEnabled />
	
	    <@edit.culturalTerms inheritanceEnabled />
	
	    <@edit.siteKeywords inheritanceEnabled />
	    
	    <@edit.generalKeywords inheritanceEnabled />
	</#if>

    <@edit.resourceNoteSection inheritanceEnabled />



    <#if !(hideRelatedCollections??)>
      <@edit.relatedCollections inheritanceEnabled />
    </#if>
    
    <@edit.fullAccessRights />
    
    <#if !resource.resourceType.project>
      <@edit.submit fileReminder=((resource.id == -1) && fileReminder) />
    <#else>
      <@edit.submit fileReminder=false />
    </#if>
</@s.form>

<#-- include any JS templates -->
<@edit.asyncUploadTemplates />

<#-- include footer on resource page -->
<#if local_.footer?? && local_.footer?is_macro>
	<@local_.footer />
</#if>
	
	
	

<script type='text/javascript'>
<#noescape>

var formSelector = "#metadataForm";
var includeInheritance = ${inheritanceEnabled?string("true", "false")};
var acceptFileTypes  = <@edit.acceptedFileTypesRegex />;
/*

 * FIXME: move to common.js once we figure out how to control and set javascript based on freemarker values that have "Rights" implications.
 */
$(function(){
    'use strict';
    var form = $(formSelector)[0];
    
    <#if multipleUpload??>
    //init fileupload
    var id = $('input[name=id]').val();
    <#if ableToUploadFiles && multipleUpload>
	    TDAR.fileupload.registerUpload({
	       informationResourceId: id, 
	       acceptFileTypes: acceptFileTypes, 
	       formSelector: formSelector,
	       inputSelector: '#fileAsyncUpload',
           fileuploadSelector: '#divFileUpload'
	       });

        var fileValidator = new FileuploadValidator("metadataForm");
        fileValidator.addRule("nodupes");
        TDAR.fileupload.validator = fileValidator;
    </#if>
    </#if>

    //wire up jquery-ui datepicker to our date fields
    $(".singleFileUpload .date, .existing-file .date, .date.datepicker").datepicker({dateFormat: "mm/dd/yy"});

    TDAR.common.initEditPage(form);
    
    //register maps, if any
    if($('#divSpatialInformation').length) {
        $(function() {
            //fixme: implicitly init when necessary
            TDAR.maps.initMapApi();
            var mapdiv = $('#editmapv3')[0];
            var inputCoordsContainer = $("#explicitCoordinatesDiv")[0];
            TDAR.maps.setupEditMap(mapdiv, inputCoordsContainer);
        });
    }
    
<#if inheritanceEnabled>
TDAR.inheritance.project = ${projectAsJson};
TDAR.inheritance.applyInheritance(formSelector);
</#if>    
    
    
    <#if validFileExtensions??>
    var validate = $('.validateFileType');
    if ($(validate).length > 0) {
        $(validate).rules("add", {
            extension: "<@edit.join sequence=validFileExtensions delimiter="|"/>",
            messages: {
                extension: "Please enter a valid file (<@edit.join sequence=validFileExtensions delimiter=", "/>)"
            }
        });
    } 
    </#if>
    
	<#if resource.resourceType.dataTableSupported>
    TDAR.fileupload.addDataTableValidation(TDAR.fileupload.validator);
	</#if>

    <#-- //FIXME: I don't like this convention because it is invisible to syntax highlighting / code analysis tools -->
    <#if local_.localJavascript?? && local_.localJavascript?is_macro>
	<@local_.localJavascript />
	</#if>

	<#assign _filesJson = "''">
	<#if filesJson?has_content>
	<#assign _filesJson = filesJson>
	</#if>
	<#-- setup the page; if the page has files, include the JSON data for those files -->
	    if(TDAR) {
	        TDAR.filesJson = ${_filesJson!"''"};
	    }
	    
	    $("#fileUploadField").change(function(){
	            if ($("#fileUploadField").val().length > 0) {
	                $("#reminder").hide();
	            }        
	    });

});
</#noescape>
</script>


<@edit.personAutocompleteTemplate />

</body>
</#escape>