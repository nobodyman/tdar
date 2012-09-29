<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/list-macros.ftl" as rlist>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>

<head>
<title>Filter Ontology Values</title>
<meta name="lastModifiedDate" content="$Date$"/>
<meta http-equiv="content-type" content="text/html; charset=utf-8" />
</head>
<style type='text/css'>
input+label {position: relative; }
input[disabled] + label {
font-weight: normal !important;
}
</style>
<@edit.toolbar "filter-values"/>

<div class='glide'>
You can filter data values for the datasets listed below.  Only checked values mapped to an 
ontology will be reported below.  Select checkboxes next to the
values that you would like to be included or aggregated to that level.  Checkboxes are automatically checked if values are present in ALL datatables.
<br/>
Indented unchecked values are aggregated to the next higher level that is checked.
Unchecked values at the top (leftmost) level are ignored, along with  any unchecked
subdivision categories.  Values that occur in each dataset are indicated with blue
checks, absent values are indicated with red x's.
</div>

<#assign integrationcolumn_index =0>

<div class="glide">
<!--
<div class="parent-info">
  tDAR has automatically selected values that occur accross all datasets below.  To clear this, please click "clear all" below.
</div>-->
<#-- display links with taxonomy expanded -->
<@s.form method='post' action='display-filtered-results' id="filterForm">

<#assign totalCheckboxCount=0>
<#list integrationColumns as integrationColumn>
 <#if integrationColumn.displayColumn >

  <input type="hidden" name="integrationColumns[${integrationcolumn_index}].columnType" value="${integrationColumn.columnType}" />
  <#list integrationColumn.columns as col_ >
   <#if col_??>
    <input type="hidden" name="integrationColumns[${integrationcolumn_index}].columns[${col__index}].id" value="${col_.id?c}" />
   </#if>
  </#list>
 <#else>
 <#if integrationColumn.sharedOntology??>
 <table class='tableFormat width99percent zebracolors integrationTable'>
    <thead>
        <tr>
        <th>Ontology labels from ${integrationColumn.sharedOntology.title} [${integrationColumn.name}]<br/>

        (<span class="link" onclick='selectAllChildren("${integrationcolumn_index}", true);'>Select All</span> | <span class="autocheck link">Select All With Shared Values</span>
        | <span class="link"onclick='selectAllChildren("${integrationcolumn_index}", false);'>Clear All</span> | <span class="link hideElements">Hide Unmapped</span>)</th>
        <#list integrationColumn.columns as column>
        <th>${column.name}<br/> <small>(${column.dataTable.dataset.title})</small></th>
        </#list>
        </tr>
    </thead>
    <tbody>
  <input type="hidden" name="integrationColumns[${integrationcolumn_index}].columnType" value="${integrationColumn.columnType}" />
  <#list integrationColumn.columns as col>
    <input type="hidden" name="integrationColumns[${integrationcolumn_index}].columns[${col_index}].id" value="${col.id?c}" />
  </#list>

<#list integrationColumn.flattenedOntologyNodeList as ontologyNode>
    <#assign numberOfParents=ontologyNode.numberOfParents>
    <#assign checkForUser=true />
    <#assign disabled=true />
    <#if ontologyNode.parent><#assign disabled=false /></#if>
    <#list ontologyNode.columnHasValueArray as hasValue>
        <#if !hasValue>
            <#assign checkForUser=false />
        <#else>
            <#assign disabled=false />
        </#if>
    </#list>
    <tr class="<#if disabled>disabled</#if>">
    <td style="white-space: nowrap;">
    <#list 1..numberOfParents as indentationLevel>
        &nbsp;&nbsp;&nbsp;&nbsp;
    </#list>
     <input type='checkbox' id='ontologyNodeCheckboxId_${integrationcolumn_index}_${ontologyNode.index}'
    name='integrationColumns[${integrationcolumn_index}].filteredOntologyNodes[${ontologyNode_index}].id' value='${ontologyNode.id?c}'
    <#if checkForUser>canautocheck="true"</#if>     <#if disabled>disabled="disabled"</#if> />
    <label for='ontologyNodeCheckboxId_${integrationcolumn_index}_${ontologyNode.index}'>
    <#assign totalCheckboxCount=totalCheckboxCount+1>
	    <#if !disabled><b></#if>
		    ${ontologyNode.displayName} <!--(${ontologyNode.index})-->
	    <#if !disabled></b></#if>
    </label>
    <#if ontologyNode.parent >
    &nbsp;(<span class="link" onclick='selectChildren("${integrationcolumn_index}_${ontologyNode.index}", true);'>all</span>
    | <span class="link" onclick='selectChildren("${integrationcolumn_index}_${ontologyNode.index}", false);'>clear</span>)
    </#if>
    </td>
    <#list ontologyNode.columnHasValueArray as hasValue>
    <td>
        <#if hasValue>
        <@edit.img "/images/checked.gif" />
        <#else>
        <@edit.img "/images/unchecked.gif" />
        </#if>
    </td>
    </#list>
</tr>
</#list>
</tbody>
</table>
 
 <#else>
    These columns do not share a common ontology but ontology integration has not been
    fully implemented yet.  
 </#if>
</#if>
<#assign integrationcolumn_index = integrationcolumn_index+1>
 
 </#list>
</div>

<@edit.submit "Next: Apply filter" false/>

  <#list selectedDataTables as table>
      <!-- setting for error condition -->
       <input type="hidden" name="tableIds[${table_index}]" value="${table.id?c}"/>
  </#list>

</@s.form>


<script type='text/javascript'>
function selectAllChildren(id, value) {
    $("input[id*='ontologyNodeCheckboxId_" + id + "']").prop('checked', value);
    return false;
}
function selectChildren(index, value) {
    $("input[id$='" + index + "']").prop('checked', value);
    $("input[id*='" + index + "\\.']").prop('checked', value);
    return false;
}

$("#filterForm").submit(function() {
	var errors = "";
	$(".integrationTable").each(function() {
		if ($(":checked ",$(this)).length == 0) {
			errors = "at least one table does not have any filter values checked";
		}
	});
	
	if (errors != '') {
		alert(errors);
		return false;
	};
  if ($("#filterForm :checked").length < 1) {
    alert("please select at least one variable");
    return false;
  }
});
$(document).ready(function() {
  $(".autocheck").click(function() {
  	$("[canautocheck]",$(this).closest("table")).attr("checked","checked");
  });

  $(".hideElements").click(function() {
  	$("tr.disabled",$(this).closest("table")).hide();
  });

  applyZebraColors();
  });
</script>
</#escape>