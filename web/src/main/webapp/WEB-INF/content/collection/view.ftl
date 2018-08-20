<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#import "/WEB-INF/macros/resource/common-resource.ftl" as common>
    <#import "common-collection.ftl" as commonCollection>

<head>
    <@commonCollection.head />
</head>
<body>

    <@commonCollection.header />

        <h1>${resourceCollection.name!"untitled collection"}</h1>
    
    <#if !visible>
    This collection is not accessible
    
    <#else>

        <@commonCollection.sidebar />

        <@commonCollection.descriptionSection/>

        <@commonCollection.keywordSection />

        <@commonCollection.resultsSection/>

        <@commonCollection.adminSection/>
    </#if>

    <@commonCollection.javascript />
    <#if id?has_content && id == 22070>
<script id="datasetfields">
[{"name":"max_diam","displayName":"max_diam","description":"","columnDataType":"TEXT","columnEncodingType":"UNCODED_VALUE","mappingColumn":false,"values":[],"intValues":[],"floatValues":[],"ignoreFileExtension":true,"searchField":true,"importOrder":30,"actuallyMapped":false,"id":74158,"sequenceNumber":31,"dataTableRef":"DataTable:9263"},{"name":"height","displayName":"height","description":"","columnDataType":"TEXT","columnEncodingType":"UNCODED_VALUE","mappingColumn":false,"values":[],"intValues":[],"floatValues":[],"ignoreFileExtension":true,"searchField":true,"importOrder":29,"actuallyMapped":false,"id":74157,"sequenceNumber":30,"dataTableRef":"DataTable:9263"},{"name":"temporal_style","displayName":"temporal_style","description":"","columnDataType":"TEXT","columnEncodingType":"UNCODED_VALUE","mappingColumn":false,"values":[],"intValues":[],"floatValues":[],"ignoreFileExtension":true,"searchField":true,"importOrder":32,"actuallyMapped":false,"id":74160,"sequenceNumber":33,"dataTableRef":"DataTable:9263"},{"name":"color_scheme","displayName":"color_scheme","description":"","columnDataType":"TEXT","columnEncodingType":"UNCODED_VALUE","mappingColumn":false,"values":[],"intValues":[],"floatValues":[],"ignoreFileExtension":true,"searchField":true,"importOrder":31,"actuallyMapped":false,"id":74159,"sequenceNumber":32,"dataTableRef":"DataTable:9263"},{"name":"design_class","displayName":"design_class","description":"","columnDataType":"TEXT","columnEncodingType":"UNCODED_VALUE","mappingColumn":false,"values":[],"intValues":[],"floatValues":[],"ignoreFileExtension":true,"searchField":true,"importOrder":34,"actuallyMapped":false,"id":74162,"sequenceNumber":35,"dataTableRef":"DataTable:9263"},{"name":"temporal_style_basis","displayName":"temporal_style_basis","description":"","columnDataType":"TEXT","columnEncodingType":"UNCODED_VALUE","mappingColumn":false,"values":[],"intValues":[],"floatValues":[],"ignoreFileExtension":true,"searchField":true,"importOrder":33,"actuallyMapped":false,"id":74161,"sequenceNumber":34,"dataTableRef":"DataTable:9263"},{"name":"layout_detail","displayName":"layout_detail","description":"","columnDataType":"TEXT","columnEncodingType":"UNCODED_VALUE","mappingColumn":false,"values":[],"intValues":[],"floatValues":[],"ignoreFileExtension":true,"searchField":true,"importOrder":36,"actuallyMapped":false,"id":74164,"sequenceNumber":37,"dataTableRef":"DataTable:9263"},{"name":"layout","displayName":"layout","description":"","columnDataType":"TEXT","columnEncodingType":"UNCODED_VALUE","mappingColumn":false,"values":[],"intValues":[],"floatValues":[],"ignoreFileExtension":true,"searchField":true,"importOrder":35,"actuallyMapped":false,"id":74163,"sequenceNumber":36,"dataTableRef":"DataTable:9263"},{"name":"figurative_specific","displayName":"figurative_specific","description":"","columnDataType":"TEXT","columnEncodingType":"UNCODED_VALUE","mappingColumn":false,"values":[],"intValues":[],"floatValues":[],"ignoreFileExtension":true,"searchField":true,"importOrder":38,"actuallyMapped":false,"id":74166,"sequenceNumber":39,"dataTableRef":"DataTable:9263"},{"name":"figurative_general","displayName":"figurative_general","description":"","columnDataType":"TEXT","columnEncodingType":"UNCODED_VALUE","mappingColumn":false,"values":[],"intValues":[],"floatValues":[],"ignoreFileExtension":true,"searchField":true,"importOrder":37,"actuallyMapped":false,"id":74165,"sequenceNumber":38,"dataTableRef":"DataTable:9263"},{"name":"geometric_specific","displayName":"geometric_specific","description":"","columnDataType":"TEXT","columnEncodingType":"UNCODED_VALUE","mappingColumn":false,"values":[],"intValues":[],"floatValues":[],"ignoreFileExtension":true,"searchField":true,"importOrder":40,"actuallyMapped":false,"id":74168,"sequenceNumber":41,"dataTableRef":"DataTable:9263"},{"name":"geometric_general","displayName":"geometric_general","description":"","columnDataType":"TEXT","columnEncodingType":"UNCODED_VALUE","mappingColumn":false,"values":[],"intValues":[],"floatValues":[],"ignoreFileExtension":true,"searchField":true,"importOrder":39,"actuallyMapped":false,"id":74167,"sequenceNumber":40,"dataTableRef":"DataTable:9263"},{"name":"wide_rim_bands","displayName":"wide_rim_bands","description":"","columnDataType":"TEXT","columnEncodingType":"UNCODED_VALUE","mappingColumn":false,"values":[],"intValues":[],"floatValues":[],"ignoreFileExtension":true,"searchField":true,"importOrder":42,"actuallyMapped":false,"id":74170,"sequenceNumber":43,"dataTableRef":"DataTable:9263"},{"name":"narrow_rim_bands","displayName":"narrow_rim_bands","description":"","columnDataType":"TEXT","columnEncodingType":"UNCODED_VALUE","mappingColumn":false,"values":[],"intValues":[],"floatValues":[],"ignoreFileExtension":true,"searchField":true,"importOrder":41,"actuallyMapped":false,"id":74169,"sequenceNumber":42,"dataTableRef":"DataTable:9263"},{"name":"exterior_design","displayName":"exterior_design","description":"","columnDataType":"TEXT","columnEncodingType":"UNCODED_VALUE","mappingColumn":false,"values":[],"intValues":[],"floatValues":[],"ignoreFileExtension":true,"searchField":true,"importOrder":44,"actuallyMapped":false,"id":74172,"sequenceNumber":45,"dataTableRef":"DataTable:9263"},{"name":"interior_band","displayName":"interior_band","description":"","columnDataType":"TEXT","columnEncodingType":"UNCODED_VALUE","mappingColumn":false,"values":[],"intValues":[],"floatValues":[],"ignoreFileExtension":true,"searchField":true,"importOrder":43,"actuallyMapped":false,"id":74171,"sequenceNumber":44,"dataTableRef":"DataTable:9263"},{"name":"other_data","displayName":"other_data","description":"","columnDataType":"TEXT","columnEncodingType":"UNCODED_VALUE","mappingColumn":false,"values":[],"intValues":[],"floatValues":[],"ignoreFileExtension":true,"searchField":true,"importOrder":46,"actuallyMapped":false,"id":74174,"sequenceNumber":47,"dataTableRef":"DataTable:9263"},{"name":"publication","displayName":"publication","description":"","columnDataType":"TEXT","columnEncodingType":"UNCODED_VALUE","mappingColumn":false,"values":[],"intValues":[],"floatValues":[],"ignoreFileExtension":true,"searchField":true,"importOrder":45,"actuallyMapped":false,"id":74173,"sequenceNumber":46,"dataTableRef":"DataTable:9263"},{"name":"b_w_image_filename","displayName":"b_w_image_filename","description":"","columnDataType":"TEXT","columnEncodingType":"FILENAME","mappingColumn":true,"values":[],"intValues":[],"floatValues":[],"delimiterValue":";","ignoreFileExtension":true,"searchField":true,"importOrder":47,"actuallyMapped":false,"id":74175,"sequenceNumber":48,"dataTableRef":"DataTable:9263"},{"name":"color_image_filename","displayName":"color_image_filename","description":"","columnDataType":"TEXT","columnEncodingType":"FILENAME","mappingColumn":true,"values":[],"intValues":[],"floatValues":[],"delimiterValue":";","ignoreFileExtension":true,"searchField":true,"importOrder":49,"actuallyMapped":false,"id":74177,"sequenceNumber":50,"dataTableRef":"DataTable:9263"},{"name":"hachure_type","displayName":"hachure_type","columnDataType":"TEXT","columnEncodingType":"UNCODED_VALUE","mappingColumn":false,"values":[],"intValues":[],"floatValues":[],"ignoreFileExtension":true,"searchField":true,"importOrder":51,"actuallyMapped":false,"id":74180,"sequenceNumber":52,"dataTableRef":"DataTable:9263"},{"name":"artist_set_archive_no","displayName":"artist_set_archive_no","columnDataType":"TEXT","columnEncodingType":"UNCODED_VALUE","mappingColumn":false,"values":[],"intValues":[],"floatValues":[],"ignoreFileExtension":true,"searchField":true,"importOrder":53,"actuallyMapped":false,"id":74182,"sequenceNumber":54,"dataTableRef":"DataTable:9263"},{"name":"artist_set","displayName":"artist_set","columnDataType":"TEXT","columnEncodingType":"UNCODED_VALUE","mappingColumn":false,"values":[],"intValues":[],"floatValues":[],"ignoreFileExtension":true,"searchField":true,"importOrder":52,"actuallyMapped":false,"id":74181,"sequenceNumber":53,"dataTableRef":"DataTable:9263"},{"name":"inaa_sample_number_and_da","displayName":"inaa_sample_number_and_da","columnDataType":"TEXT","columnEncodingType":"UNCODED_VALUE","mappingColumn":false,"values":[],"intValues":[],"floatValues":[],"ignoreFileExtension":true,"searchField":true,"importOrder":54,"actuallyMapped":false,"id":74183,"sequenceNumber":55,"dataTableRef":"DataTable:9263"},{"name":"age","displayName":"age","description":"","columnDataType":"TEXT","columnEncodingType":"UNCODED_VALUE","mappingColumn":false,"values":[],"intValues":[],"floatValues":[],"ignoreFileExtension":true,"searchField":true,"importOrder":3,"actuallyMapped":false,"id":74186,"sequenceNumber":4,"dataTableRef":"DataTable:9263"},{"name":"burial_number","displayName":"burial_number","description":"","columnDataType":"TEXT","columnEncodingType":"UNCODED_VALUE","mappingColumn":false,"values":[],"intValues":[],"floatValues":[],"ignoreFileExtension":true,"searchField":true,"importOrder":2,"actuallyMapped":false,"id":74185,"sequenceNumber":3,"dataTableRef":"DataTable:9263"},{"name":"sex","displayName":"sex","description":"","columnDataType":"TEXT","columnEncodingType":"UNCODED_VALUE","mappingColumn":false,"values":[],"intValues":[],"floatValues":[],"ignoreFileExtension":true,"searchField":true,"importOrder":5,"actuallyMapped":false,"id":74188,"sequenceNumber":6,"dataTableRef":"DataTable:9263"},{"name":"age_age_basis","displayName":"age_age_basis","description":"","columnDataType":"TEXT","columnEncodingType":"UNCODED_VALUE","mappingColumn":false,"values":[],"intValues":[],"floatValues":[],"ignoreFileExtension":true,"searchField":true,"importOrder":4,"actuallyMapped":false,"id":74187,"sequenceNumber":5,"dataTableRef":"DataTable:9263"},{"name":"sex_basis","displayName":"sex_basis","description":"","columnDataType":"TEXT","columnEncodingType":"UNCODED_VALUE","mappingColumn":false,"values":[],"intValues":[],"floatValues":[],"ignoreFileExtension":true,"searchField":true,"importOrder":6,"actuallyMapped":false,"id":74189,"sequenceNumber":7,"dataTableRef":"DataTable:9263"},{"name":"archive_no__related_bowls","displayName":"archive_no__related_bowls","description":"","columnDataType":"TEXT","columnEncodingType":"UNCODED_VALUE","mappingColumn":false,"values":[],"intValues":[],"floatValues":[],"ignoreFileExtension":true,"searchField":true,"importOrder":9,"actuallyMapped":false,"id":74192,"sequenceNumber":10,"dataTableRef":"DataTable:9263"},{"name":"col_comments","displayName":"comments","description":"","columnDataType":"TEXT","columnEncodingType":"UNCODED_VALUE","mappingColumn":false,"values":[],"intValues":[],"floatValues":[],"ignoreFileExtension":true,"searchField":true,"importOrder":12,"actuallyMapped":false,"id":74195,"sequenceNumber":13,"dataTableRef":"DataTable:9263"},{"name":"record_created","displayName":"record_created","description":"","columnDataType":"TEXT","columnEncodingType":"UNCODED_VALUE","mappingColumn":false,"values":[],"intValues":[],"floatValues":[],"ignoreFileExtension":true,"searchField":true,"importOrder":14,"actuallyMapped":false,"id":74142,"sequenceNumber":15,"dataTableRef":"DataTable:9263"},{"name":"mimpidd_id","displayName":"mimpidd_id","description":"","columnDataType":"TEXT","columnEncodingType":"UNCODED_VALUE","mappingColumn":false,"values":[],"intValues":[],"floatValues":[],"ignoreFileExtension":true,"searchField":true,"importOrder":0,"actuallyMapped":false,"id":74141,"sequenceNumber":1,"dataTableRef":"DataTable:9263"},{"name":"museum_no","displayName":"museum_no","description":"","columnDataType":"TEXT","columnEncodingType":"UNCODED_VALUE","mappingColumn":false,"values":[],"intValues":[],"floatValues":[],"ignoreFileExtension":true,"searchField":true,"importOrder":16,"actuallyMapped":false,"id":74144,"sequenceNumber":17,"dataTableRef":"DataTable:9263"},{"name":"site_number","displayName":"site_number","description":"","columnDataType":"TEXT","columnEncodingType":"UNCODED_VALUE","mappingColumn":false,"values":[],"intValues":[],"floatValues":[],"ignoreFileExtension":true,"searchField":true,"importOrder":18,"actuallyMapped":false,"id":74146,"sequenceNumber":19,"dataTableRef":"DataTable:9263"},{"name":"site_name","displayName":"site_name","description":"","columnDataType":"TEXT","columnEncodingType":"UNCODED_VALUE","mappingColumn":false,"values":[],"intValues":[],"floatValues":[],"ignoreFileExtension":true,"searchField":true,"importOrder":17,"actuallyMapped":false,"id":74145,"sequenceNumber":18,"dataTableRef":"DataTable:9263"},{"name":"within_site_category","displayName":"within_site_category","description":"","columnDataType":"TEXT","columnEncodingType":"UNCODED_VALUE","mappingColumn":false,"values":[],"intValues":[],"floatValues":[],"ignoreFileExtension":true,"searchField":true,"importOrder":20,"actuallyMapped":false,"id":74148,"sequenceNumber":21,"dataTableRef":"DataTable:9263"},{"name":"within_site","displayName":"within_site","description":"","columnDataType":"TEXT","columnEncodingType":"UNCODED_VALUE","mappingColumn":false,"values":[],"intValues":[],"floatValues":[],"ignoreFileExtension":true,"searchField":true,"importOrder":19,"actuallyMapped":false,"id":74147,"sequenceNumber":20,"dataTableRef":"DataTable:9263"},{"name":"field_num_or_old_num","displayName":"field_num_or_old_num","description":"","columnDataType":"TEXT","columnEncodingType":"UNCODED_VALUE","mappingColumn":false,"values":[],"intValues":[],"floatValues":[],"ignoreFileExtension":true,"searchField":true,"importOrder":22,"actuallyMapped":false,"id":74150,"sequenceNumber":23,"dataTableRef":"DataTable:9263"},{"name":"room_number","displayName":"room_number","description":"","columnDataType":"TEXT","columnEncodingType":"UNCODED_VALUE","mappingColumn":false,"values":[],"intValues":[],"floatValues":[],"ignoreFileExtension":true,"searchField":true,"importOrder":21,"actuallyMapped":false,"id":74149,"sequenceNumber":22,"dataTableRef":"DataTable:9263"},{"name":"col_condition","displayName":"condition","description":"","columnDataType":"TEXT","columnEncodingType":"UNCODED_VALUE","mappingColumn":false,"values":[],"intValues":[],"floatValues":[],"ignoreFileExtension":true,"searchField":true,"importOrder":24,"actuallyMapped":false,"id":74152,"sequenceNumber":25,"dataTableRef":"DataTable:9263"},{"name":"vessel_form","displayName":"vessel_form","description":"","columnDataType":"TEXT","columnEncodingType":"UNCODED_VALUE","mappingColumn":false,"values":[],"intValues":[],"floatValues":[],"ignoreFileExtension":true,"searchField":true,"importOrder":23,"actuallyMapped":false,"id":74151,"sequenceNumber":24,"dataTableRef":"DataTable:9263"},{"name":"kill_hole","displayName":"kill_hole","description":"","columnDataType":"TEXT","columnEncodingType":"UNCODED_VALUE","mappingColumn":false,"values":[],"intValues":[],"floatValues":[],"ignoreFileExtension":true,"searchField":true,"importOrder":26,"actuallyMapped":false,"id":74154,"sequenceNumber":27,"dataTableRef":"DataTable:9263"},{"name":"wear","displayName":"wear","description":"","columnDataType":"TEXT","columnEncodingType":"UNCODED_VALUE","mappingColumn":false,"values":[],"intValues":[],"floatValues":[],"ignoreFileExtension":true,"searchField":true,"importOrder":25,"actuallyMapped":false,"id":74153,"sequenceNumber":26,"dataTableRef":"DataTable:9263"},{"name":"rim_diam","displayName":"rim_diam","description":"","columnDataType":"TEXT","columnEncodingType":"UNCODED_VALUE","mappingColumn":false,"values":[],"intValues":[],"floatValues":[],"ignoreFileExtension":true,"searchField":true,"importOrder":28,"actuallyMapped":false,"id":74156,"sequenceNumber":29,"dataTableRef":"DataTable:9263"},{"name":"kill_hole_form","displayName":"kill_hole_form","description":"","columnDataType":"TEXT","columnEncodingType":"UNCODED_VALUE","mappingColumn":false,"values":[],"intValues":[],"floatValues":[],"ignoreFileExtension":true,"searchField":true,"importOrder":27,"actuallyMapped":false,"id":74155,"sequenceNumber":28,"dataTableRef":"DataTable:9263"}]
</script>
</#if>
</body>

</#escape>