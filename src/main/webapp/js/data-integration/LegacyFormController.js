(function(angular){
    "use strict";
    var app = angular.module('integrationApp');

    console.log("LegacyFormController::")
    console.log("App:", app);

    app.controller('LegacyFormController', ['$scope', '$http', 'integrationService', function($scope, $http, integration){
        var self = this, fields = [];
        self.fields = fields;
        self.integration = integration;
        self.showForm = false;
        self.hideForm = function() {
            self.showForm = false;
        };

        self.dumpdata = function() {
            //strip angular properties from viewmodel
            var cleanData = angular.copy(integration);
            var cleanDataJson = JSON.stringify(cleanData);

            //replace refs with $ref objects (you will need to deserialize with JSON.retrocycle)
            var dedupedData = JSON.decycle(cleanData);
            var dedupedDataJson = JSON.stringify(dedupedData);
            console.log("viewmodel size:%sk", (cleanDataJson.length / 1000).toFixed(2));
            console.log(" decycled size:%sk", (dedupedDataJson.length / 1000).toFixed(2));
            console.log("display-filter-results data::")
            console.table($("#frmLegacy").serializeArray());
        };
    }]);

    /* global angular  */
})(angular);
