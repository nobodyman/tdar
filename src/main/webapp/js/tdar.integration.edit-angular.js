(function($, angular, console){
    "use strict";
    var app,_projects, _collections, _categories, _documentData;
    app = angular.module('integrationApp', ['angularModalService']);

    //Our integration "Model" object.
    function Integration() {
        var self = this;
        var datasetMap = {};
        self.title = "";
        self.description = "";
        self.columns = [];
        self.datatables = [];
        self.ontologies = [];

    }

    //model for search filter that can be used for datasets or ontologies
    function SearchFilter() {
        var self = this;
        $.extend(self, {
            title: "",
            projectId: null,
            collectionId: null,
            categoryId: null,
            unbookmarked: false,
            incompatible: false
        });
    }

    //read json embedded in script elements
    function _loadDocumentData() {
        var dataElements = $('[type="application/json"][id]').toArray();
        var map = {};
        dataElements.forEach(function(elem){
            var key = elem.id;
            var val = JSON.parse(elem.innerHTML);
            map[key] = val;
        });
        return map;
    }
    //fixme: move to init section (or consider moving this to angular 'service')
    _documentData = _loadDocumentData();

    /**
     * Add specified object to specified array if specified array does not already contain the object.
     *
     * @param arr an array of objects
     * @param obj the object to potentially add to the array
     * @param propName the property to consider when looking for duplicate items. If undefined, function uses strict identity to find dupes.
     * @private
     */
    function _setAdd(arr, obj, propName) {
        var fn, dupes=[];
        if(propName) {
            fn = function(testObj) {
                return testObj[propName] === obj[propName]
            };
        } else {
            fn = function(testObj) {
                return testObj === obj;
            }
        }
        dupes = arr.filter(fn);
        if(dupes.length === 0) {
            arr.push(obj);
        }
        return dupes.length === 0
    }

    function _setAddAll(arr, objs, propName) {
        objs.forEach(function(obj) {
           _setAdd(arr, obj, propName);
        });
    }

    /**
     * Retrieve item from an array (by key), treating array as a set.  Yes, I realize setGet is a stupid function name.  Go away.
     * @param arr
     * @param keyName
     * @param key
     * @returns  object located by key, or null if not found
     * @private
     */
    function _setGet(arr, keyName, key) {
        var val = null;
        for(var i = 0; i < arr.length; i++) {
            if(arr[i][keyName] === key) {
                val = arr[i][keyName];
                break;
            }
        }
        return val;
    }

    /**
     * Remove specified item from the specified array
     * @param arr
     * @param item
     * @returns {*} original, mutated array
     * @private
     */
    function _setRemove(arr, item) {
        if(arr.indexOf(item) === -1) return arr;
        arr.splice(arr.indexOf(item), 1);
        return arr;
    }



    //top-level controller for the integration viewmodel
    app.controller('IntegrationCtrl', ['$scope', 'ModalService', '$http', function($scope, ModalService, $http){
        var self = this,
            integration = new Integration(),
            openModal,
            designatedOntologies = {};

        //fixme: remove later, expose viewmodel for debugging
        window.__viewModel= self;

        //controller public fields
        this.integration = integration;
        this.tab = 0;
        this.sharedOntologies = [];

        //'private' methods
        openModal = function(options) {
            ModalService.showModal({
                templateUrl: "workspace/modal-dialog.html",
                controller: "ModalDialogController",
                inputs: {
                    options: $.extend({categoryFilter: false}, options)
                }
            }).then(function(modal){
                //model.element is a jqSelection containing the top-level element for this control (e.g. $("#modalContainer")
                modal.element.modal();

                //model.close is a promise that is resolved when the modal closes
                modal.close.then(function(result){
                    //modal.element.modal('hide');
                    console.log("modal destroyed  result:%s", result);
                    $scope.message = result ? "result was yes" : "result was no";

                    if(options.close) {
                        options.close(result);
                    }
                });

            //if the service cannot create the modal or catches an exception, the promise calls an error callback
            }).catch(function(error) {
                console.error("ModalService error: %s", error);
            });
        };


        //controller public methods
        this.setTab  = function(idx) {
            this.tab = idx;
        }

        this.isTabSet = function(idx) {
            return this.tab === idx;
        }

        this.closeTab = function(idx) {
            integration.columns.splice(idx, 1);
        }

        this.saveClicked = function() {
            console.log("Saving.")
            console.log(JSON.stringify(integration, null, 4));
        };

        function updateSharedOntologies(ontologies) {
            _setAddAll(self.sharedOntologies, ontologies, "id");
            console.log("updateSharedOntologies::");
        }

        /**
         * Called after user selects list of dataset id's from 'add datasets' modal.
         * @param datasetIds
         */
        this.addDatasets = function(datasetIds) {
            if(datasetIds.length === 0) return;

            $http.get('/workspace/ajax/table-details', {
                params: {
                    datasetIds: datasetIds
                }
            }).success(function(data) {
                _setAddAll(self.integration.datatables, data.dataTables, "data_table_id");
                updateSharedOntologies(data.sharedOntologies);
            });
        };

        /**
         * Build list of integration columns.  Called after user selects list of ontology id's.
         * @param ontologyIds
         */
        this.addIntegrationColumns = function(ontologyIds) {
            //If user chooses ontologies that arent (yet) in the sharedOntologies list, we'll need to look them up.
            var columnsToAdd = [];
            var missingIds = [];
            ontologyIds.forEach(function(id){
                var ontology = _setGet(self.sharedOntologies, "id", id);
                if(ontology) {
                    columnsToAdd.push(ontology);
                } else {
                    missingIds.push(id);
                }
            });

            //Lookup any missing ontologies and then add them to integration.columns.
            if(missingIds.length) {
                $http.get("/workspace/ajax/table-details", {
                    params: {
                        ontologyIds: missingIds
                    }
                }).success(function(data) {
                    data.forEach(function(ontology){
                        columnsToAdd.push(ontology);
                        processAddedIntegrationColumns();
                    });
                });

            //if no missing id's add the integration columns immediately
            } else {
                processAddedIntegrationColumns(columnsToAdd);
            }

        };

        var processAddedIntegrationColumns = function(ontologies) {
            ontologies.forEach(function(ontology){
                //todo: build list of participating dataTable columns.
                self.integration.columns.push({
                    type: "integration",
                    data: ontology,
                    dataTableColumns: []
                });
            });
        }

        this.integrateClicked = function() {
            console.log('integrate clicked');
        };

        this.addDatasetsClicked = function(arg) {
            console.log('Add Datasets clicked');
            openModal({
                title: "Add Datasets",
                searchType: "dataset",
                url: "/workspace/ajax/find-datasets",
                transformData: function(data) {
                    return data.map(function(item){
                        var result = item;
                        result.title = item.dataset_title;
                        if(item.sibling_count > 1) {
                            result.title += ' - ' + item.display_name;
                        }
                        result.id = item.data_table_id;
                        result.submitter_display_name  = item.submitter_display_name;
                        result.date_registered = item.date_registered;
                        return result;
                    })
                },
                close: function(data) {
                    console.debug("datasetsClicked.close::");
                    self.addDatasets(data);
                }

            });
        };

        this.addIntegrationColumnsClicked = function(arg) {
            console.log('Add Integration Columns Clicked');
            integration.columns.push({name: 'integration column: ' + integration.columns.length });
            openModal({
                title: "Add Ontologies",
                searchType: "ontology",
                url: "/workspace/ajax/find-ontologies",
                categoryFilter: true,
                close: function(data) {
                    self.addIntegrationColumns(data);
                }
            });
        };

        this.addDisplayColumnClicked = function(arg) {
            console.log('add display column clicked');
            integration.columns.push({name: 'display column: ' + integration.columns.length })
        };

        this.removeSelectedDatasetClicked = function() {
            if($scope.selectedDatatables) {
                $scope.selectedDatatables.forEach(function(item){
                    console.debug("removing %s from %s (index:%s)", item , self.integration.datatables, self.integration.datatables.indexOf(item));
                    _setRemove(self.integration.datatables, item);
                });
            } else {
                console.warn("removeSelectedDatasetClicked:: no tables specified")
            }
        }
    }]);

    //Controller that drives the add-integration-column controller
    app.controller('ModalDialogController', ['$scope', '$http', 'close', 'options',  function($scope, $http, close, options){
        var url = options.url, closeWait = 500;
        console.debug("ModalDialogController:: url:%s", url);
        $scope.title = options.title;
        $scope.filter = new SearchFilter();
        $scope.selectedItems = [];
        $scope.results = [];

        //initialize lookup lists
        $scope.projects = _documentData.allProjects;
        $scope.collections = _documentData.allCollections;
        $scope.categories = _documentData.allCategories;

        $scope.categoryFilter = options.categoryFilter;

        //when non-null
        $scope.errorMessage = null;

        //ajax search fires up at launch and whenever search terms change
        $scope.search = function() {
            console.debug("$scope.search::");
            console.debug(options);
            var config = {};
            config.params = $.extend({}, $scope.filter);

            var promise = $http.get(options.url, config);
            promise.success(function(data){
                //transform date strings into dates
                //FIXME: ajax should return date in ISO 8601 format
                data.forEach(function(item) {
                    item.date = new Date(item.date_registered);
                    //console.log("translating: %s", item);
                });

                if(options.transformData) {
                    $scope.results = options.transformData(data);
                } else {
                    $scope.results = data;
                }

            });

        };

        //update the filter whenever user updates filter UI
        $scope.updateFilter = function() {
            var data = JSON.stringify($scope.filter, null, 4);
            $scope.search();
        }

        //called when user clicks 'Add Selected Items'
        $scope.confirm = function(obj) {
            var data = JSON.stringify(obj);
            console.info("confirm:: %s", data);
            close(obj, closeWait);
        }

        //convenience function - true if specified item is in the selecteditems list
        $scope.isSelected = function(item) {
            return $scope.selectedItems.indexOf(item) > -1;
        }

        //TODO:  this could be pulled out into a commmon function,  e.g.  TDAR.common.toggleArrayValue(arr, value)

        //called when user selects/deslects one of the items in the select
        $scope.toggleSelection = function(itemId, obj) {
            console.debug("toggleSelected::");
            var items = $scope.selectedItems,
                    idx = items.indexOf(itemId);
            //if in list, remove it
            if(idx > -1 ) {
                items.splice(idx, 1);
            //otherwise, add it
            } else {
                items.push(itemId);
            }

        };

        //Execute a search() whenever user updates form control bound to the filter object
        $scope.$watch('filter', function() {
            console.log("filter changed");
            $scope.search();
        }, true);

    }]);

    //FIXME: these are hacks to auto-open the various popups
    function _hashUrl() {
        var wlh = "" + window.location.hash;
        return wlh.substring(1);
    }

    $(function() {
        var scope = angular.element($("#divIntegrationMain")).scope();
        //fire dataset modal if url ends in #dataset
        if(_hashUrl() === "dataset")
            scope.ctrl.openDatasetModal();
    })

/* global jQuery, angular  */
})(jQuery, angular, console);