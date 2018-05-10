TDAR.vuejs.balk = (function(console, $, ctx, Vue) {
    "use strict";

    var _formatDate = function(date) {
        if (date == undefined ) {
            return "";
        }
      return new Date(date).toLocaleString(['en-US'], {month: '2-digit', day: '2-digit', year: '2-digit'});
    }
    
    var _init = function(appId) {

        Vue.component("comment", {
            template:"#comment-entry-template",
            props: ["comment", "fileid"],
            data: function() {return {
                resolved :false
            }},
            mounted: function() {
                Vue.set(this,"resolved", this.comment.resolved);
            },
            methods: {
                formatDate: function(date) {
                    return _formatDate(date);
                },
                resolveComment: function() {
                    var _comment = this.comment;
                    console.log(_comment);
                    var _app = this;
                    var fileId = this.fileid;
                    console.log(_comment.id, fileId);
                    $.post("/api/file/comment/resolve", {"id": fileId,"commentId":_comment.id}).done(function(comments){
                        Vue.set(_comment, "resolver", comments.resolver);
                        Vue.set(_comment, "dateResolved", comments.dateResolved);
                        Vue.set(_comment, "resolved", true);
                        Vue.set(_app, "resolved", true);
                    });

                },
                assignComment: function() {
                    
                }
            },
            computed: {
                commentClass: function() {
                    if (this.comment.type == undefined) {
                        return "comment";
                    }
                    return this.comment.type + " comment";
                }
            }
        });

        Vue.component("pentry", {
            template:"#part-entry-template",
            props: ['date','initials','name', 'url'],
            data: function() {return {}},
            methods: {
                formatDate: function(date) {
                    return _formatDate(date);
                }
            }
        });
        Vue.component('fileEntry', {
            template : "#file-entry-template",
            props : [ "file", "index", "editable" , "studentreviewed", "externalreviewed", "fullservice"],
            data : function() {
                return {
                    previousDeleteState : '',
                    xhr : undefined,
                    initialNote: undefined,
                    previousReplaceState : ''
                }
            },
            mounted: function() {
                Vue.set(this, 'initialNote', this.file.note);
            },
            methods: {
                cd : function(file) {
                    this.$parent.cd(file);
                },
                _mark: function(role, date, name, initials) {
                    var id = this.file.id;
                    var _file= this.file;
                    var ret = {};
                    $.post("/api/file/mark", {"ids[0]": id,"role":role}).done(function(files){
                        ret = files[0];
                        console.log(ret);
                        Vue.set(_file, date, ret[date]);
                        Vue.set(_file, name, ret[name]);
                        Vue.set(_file, initials, ret[initials]);
                    });
                    return ret;
                },
                markCurated: function() {
                    var ret = this._mark("CURATED","dateCurated", "curatedByName", "curatedByInitials");
                },
                markStudentCurated: function() {
                    var ret = this._mark("STUDENT_CURATED", "dateStudentReviewed", "studentReviewedByName", "studentReviewedByInitials");
                },
                markReviewed: function() {
                    var ret = this._mark("REVIEWED", "dateReviewed", "reviewedByName","reviewedByInitials");
                },
                markExternalReviewed: function() {
                    var ret = this._mark("EXTERNAL_REVIEWED", "dateExternalReviewed", "externalReviewedByName", "externalReviewedByInitials");
                },
                _editMetadata: function (note, ocr, curate) {
                    var id = this.file.id;
                    var _file= this.file;
                    var _app = this;
                    var ret = {};
                    $.post("/api/file/editMetadata", {"id": id,"note":note, "needOcr":ocr, "curate":curate}).done(function(file){
                        ret = file;
                        Vue.set(_app,"initialNote",file.note);
                    });

                },
                wontCurate: function() {
                    this._editMetadata(this.file.note, this.file.needsOcr, false);
                },
                updateNote: function() {
                    this._editMetadata(this.file.note, this.file.needsOcr, this.file.curated);
                },
                showComments: function() {
                    this.$parent.showComments(this.file);
                },
                moveUI : function() {
                    console.log('move');
                },
                deleteFile: function(){
                    console.log('delete');
                    this.$parent.deleteFile(this.file);
                }
            },
            computed: {
                noteChanged: function() {
                    var _note = this.file.note;
                    if (_note == undefined || _note == '') {
                        _note = undefined;
                    }
                    var note = this.initialNote;
                    if (note == undefined || note == '') {
                        note = undefined;
                    }
                  if (note == _note) {
                      return false;
                  }
                  return true;
                },
                fileLink : function() {
                    return "/document/add?fileIds=" + this.file.id; 
                },
                downloadUrl : function() {
                    return "/file/download/" + this.file.id; 
                },
                rowId : function() {
                    return "files-row-" + this.index;
                },
                canStudentReview: function () {
                    if (this.file.dateStudentReviewed != undefined) {
                        return false;
                    }

                    if (this.file.studentReviewed && this.file.dateCurated != undefined) {
                        return true;
                    }
                    return false;
                },
                canCurate: function () {
                    if (this.file.dateCurated != undefined) {
                        return false;
                    }
                    if (this.file.resourceId != undefined) {
                        return true;
                    }
                    return false;
                },
                cannotCurate: function() {
                    if (this.file.size == 0 || this.file.size == undefined) {
                        return true;
                    }
                    
                    if (this.file.dateCurated == undefined ) {
                        if (this.file.curated != undefined && this.file.curated == false) {
                            return true;
                        }
                        return false;
                    }
                    return true;
                },
                canReview: function () {
                    if (this.file.dateReviewed != undefined) {
                        return false;
                    }
                    if (this.studentReviewed && this.file.dateStudentReviewed != undefined || 
                            this.studentReviewed != true && this.file.dateCurated != undefined) {
                        return true;
                    }
                    return false;
                },
                canExternalReview: function () {
                    if (this.file.dateExternalReviewed != undefined) {
                        return false;
                    }
                    if (this.externalReviewed && this.file.dateReviewed != undefined) {
                        return true;
                    }
                    return false;
                },
                
            }
        });

        var config = {
                fullService : true,
                studentReviewed : false,
                externalReviewed : true
        };
        var app = new Vue({
            el : appId,
            data : {
                listUrl : "/api/file/list",
                url : "/api/file/upload",
                validFormats : ['doc','pdf','docx','png','tif','tiff','jpg','jpeg'],
                ableToUpload : true,
                parentId: undefined,
                studentReviewed: config.studentReviewed,
                fullService: config.fullService,
                externalReviewed: config.externalReviewed,
                files : [],
                errors: [],
                comment: "",
                comments: [],
                search: "",
                commentFile:undefined,
                path : "" 
            },
            computed : {
                inputDisabled : function() {
                    return !this.ableToUpload;
                }
            },
            watch: {
                search: function(after, before) {
                    console.log(before, after);
                    if (after == undefined || after == "") {
                        this.loadFiles(this.parentId, this.path);
                    } else {
                        this.searchFiles(after);
                    }
                }
            },
            methods : {
                loadFiles: function (parentId, path) {
                    var _app = this;
                    var accountId = undefined;
                    var $acc = $("#accountId");
                    if ($acc.length > 0) {
                       accountId = $acc.val();
                    }
                    
                    $.get(this.listUrl, {"parentId": parentId, "accountId":accountId}, {
                        dataType:'jsonp'
                    }).done(function(msg){
                        Vue.set(_app,"files", msg);
                    });
                    Vue.set(this, "parentId", parentId);
                    Vue.set(this, "path", path);
                },
                searchFiles: function (search) {
                    var _app = this;
                    var accountId = undefined;
                    var $acc = $("#accountId");
                    if ($acc.length > 0) {
                       accountId = $acc.val();
                    }
                    console.log(search);
                    $.get(this.listUrl, {"term":search, "accountId":accountId}, {
                        dataType:'jsonp'
                    }).done(function(msg){
                        console.log(msg);
                        Vue.set(_app,"files", msg);
                    });
                },
                showComments: function(file) {
                    var _app = this;
                    Vue.set(_app,"commentFile",file);
                    console.log(this.commentFile);
                    $.get('/api/file/comment/list', {"id": file.id}, {
                        dataType:'jsonp'
                    }).done(function(msg){
                        msg.push({comment:'uploaded', dateCreated: file.dateCreated, commentorName: file.uploaderName, commentorInitials: file.uploaderInitials, type:'internal'});
                        if (file.dateCurated != undefined) {
                            msg.push({comment:'curated', dateCreated: file.dateCurated, commentorName: file.curatedByName, commentorInitials: file.curatedByInitials, type:'internal'});
                        }
                        if (file.dateReviewed != undefined) {
                            msg.push({comment:'reviewed', dateCreated: file.dateReviewed, commentorName: file.reviewedByName, commentorInitials: file.reviewedByInitials, type:'internal'});
                        }
                        if (file.dateStudentReviewed != undefined) {
                            msg.push({comment:'student reviewed', dateCreated: file.dateStudentReviewed, commentorName: file.studentReviewedByName, commentorInitials: file.studentReviewedByInitials, type:'internal'});
                        }
                        if (file.dateExternalReviewed != undefined) {
                            msg.push({comment:'external reviewed', dateCreated: file.dateExternalReviewed , commentorName: file.externalReviewedByName, commentorInitials: file.externalReviewedByInitials, type:'internal'});
                        }

                        msg.sort(function(a,b){
                            // Turn your strings into dates, and then subtract them
                            // to get a value that is either negative, positive, or zero.
                            return new Date(a.dateCreated) - new Date(b.dateCreated);
                          });
                        Vue.set(_app,"comments", msg);
                    });
                    
                    $("#comments-template-modal").modal('show');
                },
                addComment: function() {
                    console.log('add comment', this.comment);
                    var _app = this;
                    if (this.comment == undefined || this.comment == '') {
                        return;
                    }
                    $.post("/api/file/comment/add", {"id": this.commentFile.id,"comment": this.comment}, {
                        dataType:'jsonp'
                    }).done(function(msg){
                        _app.comments.push(msg);
                        Vue.set(_app,"comment","");
                    });
                },
                deleteFile: function(file) {
                    console.log('delete file!');
                    var id = file.id;
                    var _file= file;
                    var _app = this;
                    $.post("/api/file/delete", {"id": id}).done(function(file){
                        console.log(file);
                        var index = $.inArray(_file, _app.files);
                        if (index != -1) {
                            _app.splice(index, 1);
                        }
                    });

                },
                mkdir: function() {
                  var dir = this.dir;
                  var _app = this;
                  $.post("/api/file/mkdir", {"parentId": _app.parentId, "name": $("#dirName").val() , accountId: $("#accountId").val()}
                    ).done(function(msg) {
                        _app.files.push(msg);
                        $("#dirName").clear();
                    });
                },
                cd : function(file) {
                    console.log(JSON.stringify(file));
                    var id = undefined;
                    var displayName = "";
                    if (file != undefined) {
                        id = file.id;
                        displayName = file.displayName;
                    }
                    console.log(id, displayName);
                    this.loadFiles(id, displayName);
                },
                validateAdd : function(file, replace) {
                
                    return TDAR.vuejs.upload.validateAdd(file, this.files, replace, ['doc','docx','pdf'], 0 , 100000 , false ,this )
                },
                updateFileProgress : function(e, data) {
                    // update the progress of uploading a file
                    var _app = this;
                    if (data.files != undefined) {
                        var active = TDAR.vuejs.upload._matching(data.files, _app.files, "name");
                        active.forEach(function(pair) {
                            var file = pair[0];
                            var fileContainer = pair[1];
                            fileContainer.progress = parseInt(data.loaded / data.total * 100, 10);
                            _app.$forceUpdate();
                        });
                    }
                },
                addError : function(error) {
                    this.errors.push(error);
                },
                append: function(a, b) {
                  return a + "" + b;  
                },
                fileUploadSubmit : function(e, data) {
                },
                fileUploadAdd : function(e, data) {
                    console.log('fileUploadAdd:', e, data);
                    this._disable();
                    var $upload = $('#fileupload');
                    return TDAR.vuejs.upload.fileUploadAdd($upload, data, this);
                },
                _enable: function() {
                    $(".submitButton, #fileAsyncUpload").prop("disabled", false);
                    $(".fileinput-button").removeClass("disabled");
                },
                _disable: function() {
                    $(".submitButton, #fileAsyncUpload").prop("disabled", true);
                    $(".fileinput-button").addClass("disabled");
                },
                fileUploadAddDone : function(e, data) {
                    // complete the add action
                    var _app = this;
                    this._enable();
                    return TDAR.vuejs.upload.fileUploadAddDone(data,_app.files, _app);
                },
                addFile: function(file) {
                    this.files.push(file);
                }
            },
            mounted : function() {
                // setup
                if (this.ableToUpload == undefined || this.ableToUpload == false) {
                    console.log('file upload disabled');
                    return;
                }
                this.loadFiles(undefined,"/");
                                var _app = this;
                var up = $('#fileupload').fileupload({
                    url : this.url,
                    dataType : 'json',
                    paramName : "uploadFile",
                    dropZone: "#filesTool",
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
                        if (_app.parentId != undefined) {
                            data.push({
                                name : "parentId",
                                value : _app.parentId
                            });
                        }
                        data.push({
                            name : "accountId",
                            value : $("#accountId").val()
                        });
//                        console.log(data);
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
            }
        });
    };
    
    return {
        init : _init,
        main : function() {
            var appId = "#filesTool";
            if ($(appId).length == 1) {
                TDAR.vuejs.balk.init(appId);
            }
        }
    }

})(console, jQuery, window, Vue);
