TDAR.vuejs.uploadWidget = (function(console, $, ctx, Vue) {
    "use strict";
    var ERROR_TIMEOUT = 5000;


    var _init = function(widgetId) {

        // //https://github.com/blueimp/jQuery-File-Upload/wiki/API

        var config = {
            files : [],
            url : TDAR.uri('upload/upload'),
            ticketId : -1,
            resourceId : -1,
            userId : -1,
            validFormats : [],
            sideCarOnly : false,
            ableToUpload : false,
            maxNumberOfFiles : 50,
            requiredOptionalPairs : [ {
                required : [],
                optional : []
            } ]
        };
        if ($(widgetId).data('config') != undefined) {
            console.log($($(widgetId).data('config')).text());
            $.extend(config, JSON.parse($($(widgetId).data('config')).text()));
        }
        Vue.component('fpart', {
            template : "#fpart-template",
            props : [ "file", "index", "editable" ],
            data : function() {
                return {
                    previousDeleteState : '',
                    xhr : undefined,
                    previousReplaceState : ''
                }
            },
            mounted : function() {
                var $picker = $("input.datepicker", this.el);
                TDAR.datepicker.applyHidden($picker);
                var _app = this;
                $picker.on("datechanged", function(e) {
                    console.log("changing date", $picker.val());
                    _app.updateCreatedDate($picker.val());
                });

            },
            computed : {
                rowId : function() {
                    return "files-row-" + this.index;
                },
                restrictionFieldName : function() {
                    return "fileProxies[" + this.index + "].restriction";
                },
                inputDisabled : function() {
                    return !this.editable;
                },
                createdDateFieldName : function() {
                    return "fileProxies[" + this.index + "].fileCreatedDate";
                },
                fileIdFieldName : function() {
                    return "fileProxies[" + this.index + "].fileId";
                },
                filenameFieldName : function() {
                    return "fileProxies[" + this.index + "].filename";
                },
                sequenceNumberFieldName : function() {
                    return "fileProxies[" + this.index + "].sequenceNumber";
                },
                actionFieldName : function() {
                    return "fileProxies[" + this.index + "].action";
                },
                descriptionFieldName : function() {
                    return "fileProxies[" + this.index + "].description";
                },
                wrapperId : function() {
                    return "fileupload" + this.index + "Wrapper";
                },
                fileuploadId : function() {
                    return "fileupload" + this.index;
                },

            },
            methods : {
                updateCreatedDate : function(date) {
                    Vue.set(this.file, 'fileCreatedDate', date);
                    _app.markModified();
                },
                markModified : function() {
                    if (this.file.action == 'NONE' || this.file.action == undefined) {
                        Vue.set(this.file, "action", "MODIFY_METADATA");
                    }
                },
                deleteFile : function() {
                    if (this.file.action == 'DELETE') {
                        return;
                    }
                    Vue.set(this, "previousDeleteState", this.file.action);
                    Vue.set(this.file, "action", "DELETE");
                },
                unDeleteFile : function() {
                    if (this.file.action == 'DELETE') {
                        Vue.set(this.file, "action", this.previousDeleteState);
                    }
                },
                undoReplace : function(e) {
                    Vue.set(this.file, "action", this.previousReplaceState);
                    console.log($("#fileupload" + this.index));
                    this.xhr.abort();
                    Vue.set(this.file, "replaceFile", undefined);
                    Vue.set(this.file, "filename", this.file.originalFileName);
                    Vue.set(this.file, "name", this.file.originalFileName);
                    TDAR.vuejs.upload.setProgress(0);
                    Vue.set(this.file, "progress", undefined);
                    // $("#fileupload" + this.index).reset();
                },
                replaceFileChange : function(e) {
                    var files = e.target.files || e.dataTransfer.files;
                    if (!files.length) {
                        return;
                    }
                    Vue.set(this, "previousReplaceState", this.file.action);
                    Vue.set(this, "originalFileName", this.file.filename);
                    Vue.set(this.file, "action", "REPLACE");
                    
                    var valid = this.$parent.validateAdd(files[0],true);
                    if (valid) {
                        Vue.set(this.file, "replaceFile", files[0].filename);
                        Vue.set(this.file, "filename", files[0].filename);
                        Vue.set(this.file, "name", files[0].filename);
                        files[0].dontCreate = true;
                        files[0].filename = files[0].name;
                        var xhr = $('#fileupload').fileupload('send', {
                            files : files
                        });
                        Vue.set(this, "xhr", xhr);
                    }

                }
            },
            watch : {
                "file.description" : function(val, old) {
                    this.markModified();
                },
                "file.fileCreatedDate" : function(val, old) {
                    this.markModified();
                },
                "file.restriction" : function(val, old) {
                    this.markModified();
                }
            }
        });

        var app = new Vue({
            el : widgetId,
            data : {
                files : config.files,
                url : config.url,
                ticketId : config.ticketId,
                resourceId : config.resourceId,
                userId : config.userId,
                validFormats : config.validFormats,
                sideCarOnly : config.sideCarOnly,
                maxNumberOfFiles : config.maxNumberOfFiles,
                requiredOptionalPairs : config.requiredOptionalPairs,
                ableToUpload : config.ableToUpload
            },
            computed : {
                valid : function() {
                    return this.validatePackage();
                },
                inputDisabled : function() {
                    return !this.ableToUpload;
                }
            },
            methods : {
                getCurrentNumberOfFiles : function(files) {
                    var _app = this;
                    var currentNumberOfFiles = 0;
                    files.forEach(function(f) {
                        if (f.action != 'DELETE') {
                            var partOfPair = false;
                            var ext = "." + f.filename.split('.').pop().toLowerCase();
                            for (var i = 0; i < _app.requiredOptionalPairs.length; i++) {
                                var pair = _app.requiredOptionalPairs[i];
                                if ($.inArray(ext, pair.optional) && files.length > 1) {
                                    currentNumberOfFiles++;
                                    partOfPair = true;
                                    break;
                                }
                            }
                            if (partOfPair == false) {
                                currentNumberOfFiles++;
                            }
                        }
                    });
                    return currentNumberOfFiles;
                },
                validatePackage : function() {
                    return TDAR.vuejs.upload.validatePackage(this.files, this.requiredOptionalPairs);
                },
                validateAdd : function(file, replace) {
                    return TDAR.vuejs.upload.validateAdd(file, replace, this.validFormats, this.getCurrentNumberOfFiles(this.files), this.maximumNumberOfFiles , this.sideCarOnly  )
                },
                updateFileProgress : function(e, data) {
                    // update the progress of uploading a file
                    var _app = this;
                    if (data.files != undefined) {
                        var active = TDAR.vuejs.upload._matching(data.files, _app.files, "filename");
                        active.forEach(function(pair) {
                            var file = pair[0];
                            var fileContainer = pair[1];
                            fileContainer.progress = parseInt(data.loaded / data.total * 100, 10);
                            _app.$forceUpdate();
                        });
                    }
                },
                fileUploadSubmit : function(e, data) {
                },
                fileUploadAdd : function(e, data) {
                    // add a file
                    console.log('fileUploadAdd:', e, data);
                    this._disable();
                    var $upload = $('#fileupload');
                    return TDAR.vuejs.upload.fileUploadAdd($upload, data, this);
                },
                _disable: function() {
                    $(".submitButton, #fileAsyncUpload").prop("disabled", true);
                    $(".fileinput-button").addClass("disabled");
                },
                _enable: function() {
                    $(".submitButton, #fileAsyncUpload").prop("disabled", false);
                    $(".fileinput-button").removeClass("disabled");
                },
                fileUploadAddDone : function(e, data) {
                    // complete the add action
                    var _app = this;
                    this._enable();
                    return TDAR.vuejs.upload.fileUploadAddDone(data,_app.files, _app);
                }
            },
            mounted : function() {
                // setup
                if (this.ableToUpload == undefined || this.ableToUpload == false) {
                    console.log('file upload disabled');
                    return;
                }

                if (this.files.length == undefined || this.files.length == 0) {
                    var val = $("#vueFilesFallback").val();
                    if (val != undefined && val.trim().length > 0) {
                        var _files = JSON.parse(val);
                        console.log("loading files fallback from:", _files);
                        if (_files.length > 0) {
                            Vue.set(this, "files", files);
                        }

                    }

                }
                var _app = this;
                var up = $('#fileupload').fileupload({
                    url : this.url,
                    dataType : 'json',
                    paramName : "uploadFile",
                    // required to ensure that we don't send two files separately and not use the same ticket
                    singleFileUploads: false,
                    formData : function(form) {
                        // override formData
                        var data = [];
                        if (_app.ticketId == undefined || _app.ticketId == -1) {
                            data.push({
                                name : "ticketRequested",
                                value : true
                            });
                        } else {
                            data.push({
                                name : "ticketId",
                                value : _app.ticketId
                            });
                        }
                        console.log(data);
                        return data;
                    },
                    progressall : function(e, data) {
                        var progress = parseInt(data.loaded / data.total * 100, 10);
                        TDAR.vuejs.upload.setProgress(progress);
                    }
                }).prop('disabled', !$.support.fileInput).parent().addClass($.support.fileInput ? undefined : 'disabled');
                var _app = this;

                up.bind('fileuploadadd', _app.fileUploadAdd).bind('fileuploaddone', _app.fileUploadAddDone).bind('fileuploadsubmit', _app.fileUploadSubmit)
                        .bind('fileuploadprogress', _app.updateFileProgress);
                // .bind('fileuploadfail', function (e, data) {console.log('fileUploadFail:',e);})
                // .bind('fileuploadstart', function (e) {console.log('fileUploadStart:',e);})
                // .bind('fileuploadstop', function (e) {console.log('fileUploadStop:',e);})
                // .bind('fileuploadchange', function (e, data) {console.log('fileUploadChange:',e);});
                // .bind('fileuploadsend', function (e, data) {console.log('fileUploadSend:',e);})
                // .bind('fileuploadalways', function (e, data) {console.log('fileUploadAlways:',e);})
                // .bind('fileuploaddrop', function (e, data) {/* ... */})
                // .bind('fileuploadchunksend', function (e, data) {/* ... */})
                // .bind('fileuploadchunkdone', function (e, data) {/* ... */})
                // .bind('fileuploadchunkfail', function (e, data) {/* ... */})
                // .bind('fileuploadchunkalways', function (e, data) {/* ... */});
            }
        });
        return app;
    }

    return {
        init : _init,
        main : function() {
            var appId = '#uploadWidget';
            if ($(appId).length == 1) {
                TDAR.vuejs.uploadWidget.init(appId);
            }
        }
    }
})(console, jQuery, window, Vue);
