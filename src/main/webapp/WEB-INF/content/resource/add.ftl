<#macro link type title>
    <#if (projectId?? && projectId > 0)>
    <a href="<@s.url value="/${type}/add" />?projectId=${projectId?c}">${title}</a>
    <#else>
    <a href="<@s.url value="/${type}/add" />">${title}</a>
    </#if>
</#macro>
<head>
<title>Add a New Resource</title>
<meta name="lastModifiedDate" content="$Date$"/>
<script type='text/javascript'>

</script>
<style type="text/css">
.two-column .left { float: left; clear: left; display: block; width: 45%; border-right: 1px solid #ddd; margin-left:3% }
.two-column .right { display: block; margin-left: 52%; width: 45%; }
.fancy-clickable dd {
    margin-bottom: 1em;
    margin-right : 2em;
}

.fancy-clickable dd {
    height: 10em;
}

.fancy-clickable dt { font-size:110%}
</style>
</head>
<body>
    <h3>Create Resources </h3>
    <div class="row">
    
        <div class="span45">
            <dl class='fancy-clickable'>
                <dt><@link "document" "Document" /></dt>
                <dd>A written, printed record of information, evidence, or analysis. Examples from archaeology include published articles, books, excavation reports, field notes, or doctoral dissertations. </dd>

                <dt><@link "dataset" "Dataset" /></dt>
                <dd>A collection of data, usually in tabular form with columns representing variables and rows representing cases. A database usually refers to a set of linked or related datasets. Examples from archaeology include small spreadsheets documenting measurements and/or analysis of artifacts, as well as large databases cataloging all artifacts from a site. </dd>

                 <#if editor!false>
                        <dt><@link "video" "Video" /></dt>
                        <dd>A video</dd>
                </#if>
                            
                <dt><@link "ontology" "Ontology"/></dt>
                <dd>In ${siteAcronym}, an ontology is a small file used with a dataset column (and/or coding sheet) to hierarchically organize values in the data in order to facilitate integrating datasets from different sources. (Please see the tutorials on data integration for a complete explanation).</dd>
            
            </dl>
        </div>
        <div class="span45">
            <dl class='fancy-clickable'>
                <dt><@link "image" "Image" /></dt>
                <dd>A visual representation of an object or location. Examples from archaeology include photographs (born digital or scanned) of artifacts or sites, drawings or figures, and some maps.</dd>

                <dt><@link "sensory-data" "Sensory Data or 3D Laser Scan" /></dt>
                <dd>Certain images and/or datasets fall under the heading of Sensory Data. 3-D scans, for example. </dd>
            
                <dt><@link "coding-sheet" "Coding Sheet"/></dt>
                <dd>A list of codes and their meanings, usually associated with a single column in a dataset. An example from archaeology might be a list of ceramic type codes from a particular analysis project, linked to a specific dataset within ${siteAcronym}. A collection of coding sheets make up a coding packet, and are part of the proper documentation of a dataset.  </dd>
            
            </dl>
        </div>
    </div>

<#if (projectId!-1) == -1>
<div class="row" id="divOrganizeResources">
    <h3>Organize Resources </h3>
    <div class="span45">
        <div class="left">
            <dl class='fancy-clickable'>
                <dt><@link "project" "Project" /></dt>
                <dd>
                In ${siteAcronym}, a project is an organizational tool for working with groups of related resources. Projects in ${siteAcronym} are flexible and can be used 
                in different ways, but it is useful as a starting point to think of a ${siteAcronym} project as a digital archive for materials generated by an 
                archaeological research project. It may contain related reports, photographs, maps, and databases, and those resources may inherit metadata 
                from the parent project. 
                </dd>
            </dl>
        </div>
        <div class="span45">
            <dl class='fancy-clickable'>
                <dt><@link "collection" "Collection"/></dt>
                <dd>In ${siteAcronym}, a collection is an organizational tool with two purposes. The first is to allow contributors and users to create groups and 
                hierarchies of resources in any way they find useful. A secondary use of collections allows users to easily administer view and edit 
                permissions for large numbers of persons and resources.</dd>
            </dl>
        </div>
    </div>
</div>
</#if>



</body>