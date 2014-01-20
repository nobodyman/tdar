<#import "resource/common.ftl" as common>

<#macro scripts combine=false>
<!--[if lte IE 8]><script language="javascript" type="text/javascript" src="<@s.url value="/includes/jqplot-1.08/excanvas.js"/>"></script><![endif]--> 

    <#--if not using mergeservlet, use a fake directory name that corresponds to build number so that client will pull up-to-date version -->
    <#local fakeDirectory = combine?string("", "/vc/${common.tdarBuildId}") />

    <#local srcs = [
                     "/includes/jquery.cookie.js",
                     "/includes/jquery.metadata.2.1/jquery.metadata.js",
                     "/includes/jquery.maphighlight.local.js",
                     "/includes/jquery.textarearesizer.js",
                     "/includes${fakeDirectory}/jquery.FormNavigate.js",
                     "/includes/jquery.watermark-3.1.3.min.js",
                     "/includes/jquery.datatables-1.8.2/media/js/jquery.dataTables.js",
                     "/includes/jquery.datatables-1.8.2/extras/bootstrap-paging.js",
                     "/includes/jquery-treeview/jquery.treeview.js",
                     "/includes/blueimp-javascript-templates/tmpl.min.js",
                     "/includes/blueimp-jquery-file-upload-5.31.6/js/vendor/jquery.ui.widget.js", 
                     "/includes/blueimp-jquery-file-upload-5.31.6/js/jquery.iframe-transport.js", 
                     "/includes/blueimp-jquery-file-upload-5.31.6/js/jquery.fileupload.js",
                     "/includes/blueimp-jquery-file-upload-5.31.6/js/jquery.fileupload-process.js",
                     "/includes/blueimp-jquery-file-upload-5.31.6/js/jquery.fileupload-validate.js",
                     "/includes/blueimp-jquery-file-upload-5.31.6/js/jquery.fileupload-ui.js",
                     "/includes/jquery.populate.js",
                     "/includes/jquery.tabby-0.12.js",
                     "/includes/latLongUtil-1.0.js",
                     "/includes/jquery.orgChart/jquery.orgchart.js",
  					 "/includes/jqplot-1.08/jquery.jqplot.js",
                     "/includes/jqplot-1.08/plugins/jqplot.cursor.js",
					 "/includes/jqplot-1.08/plugins/jqplot.logAxisRenderer.js",
					 "/includes/jqplot-1.08/plugins/jqplot.highlighter.js",
					 "/includes/jqplot-1.08/plugins/jqplot.dateAxisRenderer.js",
					 "/includes/jqplot-1.08/plugins/jqplot.barRenderer.js",
					 "/includes/jqplot-1.08/plugins/jqplot.categoryAxisRenderer.js",
					 "/includes/jqplot-1.08/plugins/jqplot.canvasTextRenderer.js",
					 "/includes/jqplot-1.08/plugins/jqplot.canvasAxisTickRenderer.js",
					 "/includes/jqplot-1.08/plugins/jqplot.canvasAxisLabelRenderer.js",
					 "/includes/jqplot-1.08/plugins/jqplot.enhancedLegendRenderer.js",
					 "/includes/jqplot-1.08/plugins/jqplot.pieRenderer.js",
					 "/includes/jqplot-1.08/plugins/jqplot.pointLabels.js",
                     "/includes${fakeDirectory}/tdar.gmaps.js",
                     "/includes${fakeDirectory}/tdar.common.js",
                     "/includes${fakeDirectory}/tdar.upload.js",
                     "/includes${fakeDirectory}/tdar.repeatrow.js",
                     "/includes${fakeDirectory}/tdar.autocomplete.js",
                     "/includes${fakeDirectory}/tdar.datatable.js",
                     "/includes${fakeDirectory}/tdar.dataintegration.js", 
                     "/includes${fakeDirectory}/tdar.advanced-search.js",
                     "/includes${fakeDirectory}/tdar.authority-management.js",
                     "/includes${fakeDirectory}/tdar.inheritance.js",
                     "/includes${fakeDirectory}/tdar.pricing.js",
                     "/includes${fakeDirectory}/tdar.heightevents.js",
                     "/includes${fakeDirectory}/tdar.contexthelp.js",
	                 "/includes${fakeDirectory}/tdar.formValidateExtensions.js",
	                 "/includes${fakeDirectory}/tdar.jquery-upload-validation.js",
                     "/includes/bindWithDelay.js"
    ] />
<#--                     "/includes/ivaynberg-select2-817453b/select2.js" -->

    <#if production> <#local srcs = srcs + ["/includes${fakeDirectory}/tdar.test.js"]> </#if>

<#if combine>
    <!-- calls to http://code.google.com/p/webutilities/wiki/JSCSSMergeServlet#URLs_in_CSS -->
    <!-- IE8 has 2048 char max for url+path, so we split up our merged javascript into two requests -->
    <#local idx1 = (srcs?size/2)>
    <#local idx2 = (srcs?size/2 + 1)>
    <script type="text/javascript" src="<#list srcs[0..idx1] as src><#if src_index != 0>,</#if>${src?replace(".js","")}</#list>.js?build=${common.tdarBuildId}"></script>
    <script type="text/javascript" src="<#list srcs[idx2..]as src><#if src_index != 0>,</#if>${src?replace(".js","")}</#list>.js?build=${common.tdarBuildId}"></script>
<#else>
<#list srcs as src>
  <script type="text/javascript" src="${staticHost}${src}"></script>
</#list>

</#if>


</#macro>


<#macro css combine=true>
    <#--if not using mergeservlet, use a fake directory name that corresponds to build number so that client will pull up-to-date version -->
    <#local fakeDirectory = combine?string("", "/vc/${common.tdarBuildId}") />
    <#local srcs = [
                    "/css${fakeDirectory}/tdar-bootstrap.css",
                    "/css/famfamfam.css",
                    "/includes/ivaynberg-select2-817453b/select2.css",
                    "/includes/blueimp-jquery-file-upload-5.31.6/css/jquery.fileupload-ui.css",
                    "/includes/jquery-treeview/jquery.treeview.css",
					 "/includes/jqplot-1.08/jquery.jqplot.min.css",
                     "/includes/jquery.orgChart/jquery.orgchart.css",
                    "/includes/datatables.css"
                    
                    ] />
<#if combine>
<!-- call to http://code.google.com/p/webutilities/wiki/JSCSSMergeServlet#URLs_in_CSS -->
    <link rel="stylesheet" type="text/css" href="<#list srcs as src><#if src_index != 0>,</#if>${src?replace(".css","")}</#list>.css">

<#else>
<#list srcs as src>
  <link rel="stylesheet" type="text/css" href="${staticHost}${src}" data-version="${common.tdarBuildId}">
</#list>

</#if>


</#macro>