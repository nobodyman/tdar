package org.tdar.struts.action.resource.request;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.dao.external.auth.AuthenticationResult;
import org.tdar.core.service.ErrorTransferObject;
import org.tdar.core.service.external.AuthenticationService;
import org.tdar.core.service.external.auth.RequestUserRegistration;
import org.tdar.struts.action.TdarActionSupport;
import org.tdar.struts.interceptor.annotation.DoNotObfuscate;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts.interceptor.annotation.PostOnly;
import org.tdar.struts.interceptor.annotation.WriteableSession;

import com.opensymphony.xwork2.Preparable;
import com.opensymphony.xwork2.Validateable;

/**
 * Handle registration for new users and pass back to request-access-action
 * @author abrin
 *
 */
@ParentPackage("default")
@Namespace("/resource/request")
@Component
@Scope("prototype")
public class RequestAccessRegistrationController extends AbstractRequestAccessController implements Validateable, Preparable {

    private static final long serialVersionUID = -893535919691607147L;
    private RequestUserRegistration requestRegistration = new RequestUserRegistration(getH());

    @Autowired
    private AuthenticationService authenticationService;

    @Action(value = "process-request-registration",
            interceptorRefs = { @InterceptorRef("tdarDefaultStack") },
            results = {
                    @Result(name = INPUT, location = "request-access-unauthenticated.ftl"),
                    @Result(name = SUCCESS, type = TdarActionSupport.TDAR_REDIRECT, location = SUCCESS_REDIRECT_REQUEST_ACCESS)})
    @HttpsOnly
    @PostOnly
    @WriteableSession
    @DoNotObfuscate(reason = "getPerson() may have not been set on the session before sent to obfuscator, so don't want to wipe email")
    public String create() {
        if (getRequestUserRegistration() == null || getRequestUserRegistration().getPerson() == null) {
            return INPUT;
        }
        AuthenticationResult result = null;
        try {
            result = authenticationService.addAndAuthenticateUser(
                    getRequestUserRegistration(), getServletRequest(), getServletResponse(), getSessionData());
            if (result.getType().isValid()) {
                getRequestUserRegistration().setPerson(result.getPerson());
                addActionMessage(getText("userAccountController.successful_registration_message"));
                return TdarActionSupport.SUCCESS;
            }
        } catch (Throwable e) {
            addActionError(e.getLocalizedMessage());
        }
        return TdarActionSupport.INPUT;
    }

    @Override
    public void validate() {
        getLogger().debug("validating registration request");
        ErrorTransferObject errors = getRequestUserRegistration().validate(authenticationService, getRecaptchaService(), getServletRequest().getRemoteHost());
        processErrorObject(errors);
    }


    public RequestUserRegistration getRequestUserRegistration() {
        return requestRegistration;
    }

    public void setRequestUserRegistration(RequestUserRegistration requestUserRegistration) {
        this.requestRegistration = requestUserRegistration;
    }

}
