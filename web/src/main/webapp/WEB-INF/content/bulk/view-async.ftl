<body>
<title>Bulk Upload Status</title>

<div id="divUploadStatus"
        data-async-url="<@s.url value="checkstatus"><@s.param name="ticketId" value="${ticketId?c}" /></@s.url>">
    <h3>Bulk Upload Status (this may take some time)</h3>

    <div>
	<div>
	    <div class="progress progress-success"  id="progressbar">
	  		<div class="progress-bar bar" role="progressbar" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100"> 0% </div>
	  </div>
	</div>
        <span id="buildStatus"></span>
    </div>

    <div id="asyncErrors" class="alert alert-error error-banner" style="display:none">
        <h4>tDAR Encountered Errors During Processing</h4>

        <div id="unspecifiedError" style="display:none">
            <p>An error occurred while asking the server for an upload status update. This means your bulk upload likely failed.
                <!--                Please check the <a href="<@s.url value="/dashboard"/>">dashboard</a> to determine
                whether you successfully uploaded your files. Please notify an administrator if this problem persists. -->
            </p>
        </div>
        <div id="errorDetails"></div>
    <#if !ticketId??>
        The system has not received any files. Please try again or notify an administrator if the problem persists.
    </#if>
    </div>
</div>

<div id="divUploadComplete" class="glide" style="display:none">
    <h3>Upload Complete</h3>

    <p class="success">The upload process is complete.

    <div class="form-actions">
    <@s.a cssClass="btn btn-large btn-primary" href="/dashboard" id="btnDashboard">Continue to add titles, descriptions, and dates</@s.a>
    <@s.a cssClass="btn btn-large " href="/bulk/add" id="btnBatch">Return to Batch Upload Page</@s.a>

    </div>
</div>
<#if ticketId??>
<script type="text/javascript">
    $(function(){
        TDAR.bulk.init($('#divUploadStatus'));
    })
</script>
</#if>

</body>
