package org.tdar.struts.action.api.collection;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.exception.APIException;
import org.tdar.core.exception.StatusCode;
import org.tdar.core.service.ImportService;
import org.tdar.core.service.SerializationService;
import org.tdar.struts.action.api.AbstractApiController;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts_base.action.TdarActionSupport;
import org.tdar.struts_base.interceptor.annotation.HttpForbiddenErrorResponseOnly;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
import org.tdar.struts_base.interceptor.annotation.RequiresTdarUserGroup;
import org.tdar.utils.jaxb.JaxbParsingException;

import com.opensymphony.xwork2.Preparable;

@SuppressWarnings("serial")
@Namespace("/api/collection")
@Component
@Scope("prototype")
@ParentPackage("secured")
@RequiresTdarUserGroup(TdarGroup.TDAR_API_USER)
@HttpForbiddenErrorResponseOnly
@HttpsOnly
public class CollectionAPIAction extends AbstractApiController implements Preparable {

    private static final String SUCCESS_JSON = "success_json";
    private static final String ERROR_JSON = "error_json";

    @Autowired
    private transient SerializationService serializationService;

    @Autowired
    private transient ImportService importService;

    private ResourceCollection importedRecord;
    private String type = "xml";

    @Override
    public void prepare() throws Exception {

    }

    @Action(value = "upload",
            interceptorRefs = { @InterceptorRef("editAuthenticatedStack") },
            results = {
                    @Result(name = SUCCESS, type = TdarActionSupport.XMLDOCUMENT, params = { "statusCode", "${status.httpStatusCode}" }),
                    @Result(name = ERROR, type = TdarActionSupport.XMLDOCUMENT, params = { "statusCode", "${status.httpStatusCode}" }),
                    @Result(name = SUCCESS_JSON, type = TdarActionSupport.JSONRESULT,
                            params = { "statusCode", "${status.httpStatusCode}" }),
                    @Result(name = ERROR_JSON, type = TdarActionSupport.JSONRESULT,
                            params = { "statusCode", "${status.httpStatusCode}" })
            })
    @PostOnly
    // @WriteableSession
    public String upload() {
        List<String> stackTraces = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        if (StringUtils.isEmpty(getRecord())) {
            getLogger().info("no record defined");
            errorResponse(StatusCode.BAD_REQUEST, null, null, null);
            return ERROR;
        }

        try {
            if (getRecord().contains("<?xml")) {
                setImportedRecord((ResourceCollection) serializationService.parseXml(new StringReader(getRecord())));
            } else {
                type = "json";
                setImportedRecord(serializationService.readObjectFromJson(getRecord(), ResourceCollection.class));
            }

            getResultObject().setRecordId(getImportedRecord().getId());
            TdarUser authenticatedUser = getAuthenticatedUser();

            ResourceCollection loadedRecord = importService.bringCollectionOntoSession(getImportedRecord(), authenticatedUser, true);
            setImportedRecord(loadedRecord);
            setId(loadedRecord.getId());

            setStatusMessage(StatusCode.UPDATED, "updated:" + loadedRecord.getId());
            int statuscode = StatusCode.UPDATED.getHttpStatusCode();
            if (loadedRecord.isCreated()) {
                setStatusMessage(StatusCode.CREATED, "created:" + loadedRecord.getId());
                getResultObject().setRecordId(loadedRecord.getId());
                getResultObject().setId(loadedRecord.getId());
                statuscode = StatusCode.CREATED.getHttpStatusCode();
            }

            String title = "no title";
            if (loadedRecord instanceof ResourceCollection) {
                title = ((ResourceCollection) loadedRecord).getTitle();
            }
            logMessage(" API " + getStatus().name(), loadedRecord.getClass(), loadedRecord.getId(), title);

            getResultObject().setStatusCode(statuscode);
            getResultObject().setStatus(getStatus().toString());
            getResultObject().setMessage(getErrorMessage());
            if (getLogger().isTraceEnabled()) {
                getLogger().trace(serializationService.convertToXML(loadedRecord));
            }

            if (!"xml".equals(type)) {
                return SUCCESS_JSON;
            }

            return SUCCESS;
        } catch (Throwable e) {
            setErrorMessage("");
            if (e instanceof JaxbParsingException) {
                getLogger().debug("Could not parse the xml import", e);
                final List<String> events = ((JaxbParsingException) e).getEvents();
                errors = new ArrayList<>(events);

                errorResponse(StatusCode.BAD_REQUEST, errors, getErrorMessage(), null);
                if (!"xml".equals(type)) {
                    return ERROR_JSON;
                }
                return ERROR;
            }
            getLogger().debug("an exception occured when processing the xml import", e);
            Throwable cause = ExceptionUtils.getRootCause(e);
            if (cause == null) {
                cause = e;
            }
            stackTraces.add(ExceptionUtils.getFullStackTrace(cause));
            if (cause.getLocalizedMessage() != null) {
                try {
                    errors.add(cause.getLocalizedMessage());
                } catch (Exception ex) {
                    errors.add(cause.getMessage());
                }
            }

            if (e instanceof APIException) {
                errorResponse(((APIException) e).getCode(), errors, e.getMessage(), stackTraces);
                if (!"xml".equals(type)) {
                    return ERROR_JSON;
                }

                return ERROR;
            }
        }
        errorResponse(StatusCode.UNKNOWN_ERROR, errors, getErrorMessage(), stackTraces);
        if (!"xml".equals(type)) {
            return ERROR_JSON;
        }
        return ERROR;

    }

    public ResourceCollection getImportedRecord() {
        return importedRecord;
    }

    public void setImportedRecord(ResourceCollection importedRecord) {
        this.importedRecord = importedRecord;
    }

}
