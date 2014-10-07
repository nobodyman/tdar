<div class="inthead">
    <div id="divIntegrationNav" class="">
        <ol class="breadcrumb">
            <li><a href="filter-ng">Choose Ontologies & Datasets</a> <span class="divider"> / </span> </li>
            <li class="active"><a href="select-columns-ng">Choose Display Columns</a> <span class="divider"> / </span> </li>
            <li><a href="select-columns-ng">Display Integration Results</a></li>
        </ol>
    </div>
    <h1><b>Editing Integration</b>:
        Jim's Cool Dataset integration
    </h1>
    <h2><b>Step 2 of 3</b>: Choose Display Columns</h2>
</div>



<ul class="nav nav-tabs"  id="ulOntologyTabs" data-bind="foreach:$data">
    <li><a data-toggle="tab" data-bind="attr: {href:'#' + name}, text:display_name">foo</a></li>
</ul>

<div class="tab-content" data-bind="foreach:$data">
    <div class="tab-pane" data-bind="attr:{id: name}">
        <table class="table bordered table-condensed">
            <thead>
                <tr>
                    <th>#</th>
                    <th>Selected</th>
                    <th>Column Name</th>
                    <th>Description</th>
                    <th>Encoding</th>
                </tr>
            </thead>
            <tbody data-bind="foreach:columns">
                <tr>
                    <td data-bind="text: sequence_number"></td>
                    <td>
                        <input type="checkbox" data-bind="attr:{id: $parent.name + '_' + sequence_number}, checked: isselected">
                    </td>
                    <td>
                        <label data-bind="text: display_name, attr:{for: $parent.name + '_' + sequence_number}"></label>
                    </td>
                    <td class="nowrap" style="width:79%" data-bind="text: description"></td>
                    <td class="nowrap" data-bind="text: column_encoding_type"></td>
                </tr>
            </tbody>
        </table>
    </div>
</div>

<script src='//cdnjs.cloudflare.com/ajax/libs/knockout/3.2.0/knockout-min.js'></script>
<script src='/includes/knockout.mapping.js'></script>
<script id="jsonColumnData" type="text/plain">[
{"dataset_title":"Ojo Bonito Faunal Database","name":"e_321876_obap","display_name":"OBAP","columns":[
{"display_name":"Spec. No.","column_encoding_type":"UNCODED_VALUE","description":"","sequence_number":1,"isselected":false},
{"display_name":"Site/Rmbk","column_encoding_type":"UNCODED_VALUE","description":"","sequence_number":2,"isselected":false},
{"display_name":"Unit","column_encoding_type":"UNCODED_VALUE","description":"","sequence_number":3,"isselected":false},
{"display_name":"Level","column_encoding_type":"UNCODED_VALUE","description":"","sequence_number":4,"isselected":false},
{"display_name":"Locus","column_encoding_type":"UNCODED_VALUE","description":"","sequence_number":5,"isselected":false},
{"display_name":"Species","column_encoding_type":"CODED_VALUE","description":"","sequence_number":6,"isselected":false},
{"display_name":"Element","column_encoding_type":"CODED_VALUE","description":"Element","sequence_number":7,"isselected":false},
{"display_name":"Side","column_encoding_type":"CODED_VALUE","description":"Side","sequence_number":8,"isselected":false},
{"display_name":"Condition","column_encoding_type":"CODED_VALUE","description":"Completeness","sequence_number":9,"isselected":false},
{"display_name":"Frag","column_encoding_type":"CODED_VALUE","description":"","sequence_number":10,"isselected":false},
{"display_name":"Proximal/Distal","column_encoding_type":"CODED_VALUE","description":"","sequence_number":11,"isselected":false},
{"display_name":"Dorsal/Ventral","column_encoding_type":"CODED_VALUE","description":"","sequence_number":12,"isselected":false},
{"display_name":"Fusion","column_encoding_type":"CODED_VALUE","description":"","sequence_number":13,"isselected":false},
{"display_name":"Burning","column_encoding_type":"CODED_VALUE","description":"","sequence_number":14,"isselected":false},
{"display_name":"Artifact","column_encoding_type":"CODED_VALUE","description":"","sequence_number":15,"isselected":false},
{"display_name":"Gnawing","column_encoding_type":"CODED_VALUE","description":"","sequence_number":16,"isselected":false},
{"display_name":"NatMod","column_encoding_type":"CODED_VALUE","description":"","sequence_number":17,"isselected":false},
{"display_name":"Butchering","column_encoding_type":"CODED_VALUE","description":"","sequence_number":18,"isselected":false},
{"display_name":"Actual Number","column_encoding_type":"COUNT","description":"","sequence_number":19,"isselected":false},
{"display_name":"Minimal Number","column_encoding_type":"COUNT","description":"","sequence_number":20,"isselected":false},
{"display_name":"Weight","column_encoding_type":"MEASUREMENT","description":"","sequence_number":21,"isselected":false},
{"display_name":"Period","column_encoding_type":"UNCODED_VALUE","description":"Three periods represented 1000-1200, 1200-1275, 1275-1350","sequence_number":22,"isselected":false},
{"display_name":"Context","column_encoding_type":"CODED_VALUE","description":"Taken in most cases from unit designation.","sequence_number":23,"isselected":false}]},
{"dataset_title":"Guadalupe Ruin Fauna","name":"e_318592_prpfauna","display_name":"PRPFAUNA","columns":[
{"display_name":"Site or Roomblock","column_encoding_type":"UNCODED_VALUE","description":"Site/roomblock Number","sequence_number":1,"isselected":false},
{"display_name":"spec","column_encoding_type":"UNCODED_VALUE","description":"Guadalupe Ruin Specimen Number","sequence_number":2,"isselected":false},
{"display_name":"unit","column_encoding_type":"UNCODED_VALUE","description":"Guadalupe Ruin Unit","sequence_number":3,"isselected":false},
{"display_name":"room","column_encoding_type":"UNCODED_VALUE","description":"Guadalupe Ruin Room Number","sequence_number":4,"isselected":false},
{"display_name":"room_2","column_encoding_type":"UNCODED_VALUE","description":"Guadalupe Ruin Room Number 2","sequence_number":5,"isselected":false},
{"display_name":"Context","column_encoding_type":"UNCODED_VALUE","description":"Guadalupe Ruin Context","sequence_number":6,"isselected":false},
{"display_name":"Time","column_encoding_type":"UNCODED_VALUE","description":"Guadalupe Ruin Time Period","sequence_number":7,"isselected":false},
{"display_name":"strat","column_encoding_type":"UNCODED_VALUE","description":"Guadalupe Ruin strat","sequence_number":8,"isselected":false},
{"display_name":"mbdu","column_encoding_type":"UNCODED_VALUE","description":"Guadalupe Ruin MBDU (meters below datum upper)","sequence_number":9,"isselected":false},
{"display_name":"mbdl","column_encoding_type":"UNCODED_VALUE","description":"Guadalupe Ruin MBDL (meters below datum lower)","sequence_number":10,"isselected":false},
{"display_name":"rkey","column_encoding_type":"UNCODED_VALUE","description":"Guadalupe Ruin rkey","sequence_number":11,"isselected":false},
{"display_name":"species","column_encoding_type":"CODED_VALUE","description":"Guadalupe Ruin Species","sequence_number":12,"isselected":false},
{"display_name":"element","column_encoding_type":"CODED_VALUE","description":"Guadalupe Ruin Element","sequence_number":13,"isselected":false},
{"display_name":"side","column_encoding_type":"CODED_VALUE","description":"Guadalupe Ruin faunal side","sequence_number":14,"isselected":false},
{"display_name":"cond","column_encoding_type":"CODED_VALUE","description":"Guadalupe Ruin Condition","sequence_number":15,"isselected":false},
{"display_name":"org","column_encoding_type":"CODED_VALUE","description":"Guadalupe Ruin Origin of Breakage","sequence_number":16,"isselected":false},
{"display_name":"p_d","column_encoding_type":"CODED_VALUE","description":"Guadalupe Ruin Proximal-Distal","sequence_number":17,"isselected":false},
{"display_name":"d_v","column_encoding_type":"CODED_VALUE","description":"Guadalupe Ruin Dorsal/Ventral","sequence_number":18,"isselected":false},
{"display_name":"fuse","column_encoding_type":"CODED_VALUE","description":"Guadalupe Ruin Fusion","sequence_number":19,"isselected":false},
{"display_name":"burn","column_encoding_type":"CODED_VALUE","description":"Guadalupe Ruin Burning","sequence_number":20,"isselected":false},
{"display_name":"arti","column_encoding_type":"CODED_VALUE","description":"Guadalupe Ruin Artifacts","sequence_number":21,"isselected":false},
{"display_name":"gnaw","column_encoding_type":"CODED_VALUE","description":"Guadalupe Ruin Gnawing","sequence_number":22,"isselected":false},
{"display_name":"mod","column_encoding_type":"CODED_VALUE","description":"Guadalupe Ruin Natural Modification","sequence_number":23,"isselected":false},
{"display_name":"bmark","column_encoding_type":"CODED_VALUE","description":"Guadalupe Ruin Butchering","sequence_number":24,"isselected":false},
{"display_name":"numfrags","column_encoding_type":"COUNT","description":"Guadalupe Ruin Number of Fragments","sequence_number":25,"isselected":false},
{"display_name":"min","column_encoding_type":"COUNT","description":"Guadalupe Ruin Minimum Number of Fragments ","sequence_number":26,"isselected":false},
{"display_name":"weight","column_encoding_type":"MEASUREMENT","description":"Guadalupe Ruin Weight","sequence_number":27,"isselected":false},
{"display_name":"conf","column_encoding_type":"CODED_VALUE","description":"Guadalupe Ruin Confidence ","sequence_number":28,"isselected":false},
{"display_name":"elemcat","column_encoding_type":"CODED_VALUE","description":"Guadalupe Ruin Element Category","sequence_number":29,"isselected":false}]}]
</script>
<script>
(function(data){
    var coldata = JSON.parse(data);
    console.log(coldata);

    ko.applyBindings(coldata);
})(document.getElementById("jsonColumnData").innerHTML);

//pick the first tab
$(function(){
    $("#ulOntologyTabs a:first").tab('show');
})

</script>