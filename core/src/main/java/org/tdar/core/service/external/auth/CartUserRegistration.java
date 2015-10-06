package org.tdar.core.service.external.auth;

import org.tdar.core.service.ErrorTransferObject;
import org.tdar.core.service.external.AuthenticationService;
import org.tdar.core.service.external.RecaptchaService;

public class CartUserRegistration extends UserRegistration {

    private static final long serialVersionUID = 6764554347927128792L;

    private boolean acceptTermsOfUseAndContributorAgreement;

    public CartUserRegistration(AntiSpamHelper h) {
        super(h);
    }

    @Override
    public String getPrefix() {
        return "registrationInfo.";
    };

    @Override
    public ErrorTransferObject validate(AuthenticationService authService, RecaptchaService recaptchaService, String remoteHost) {
        if (isAcceptTermsOfUseAndContributorAgreement()) {
            setAcceptTermsOfUse(true);
            setRequestingContributorAccess(true);
        }
        ErrorTransferObject validate = super.validate(authService, recaptchaService, remoteHost);
        if (!isRequestingContributorAccess()) {
            validate.addFieldError(getPrefix() + "requestingContributorAccess", "userAccountController.require_contributor_agreement");
        }
        return validate;
    }

    public boolean isAcceptTermsOfUseAndContributorAgreement() {
        return acceptTermsOfUseAndContributorAgreement;
    }

    public void setAcceptTermsOfUseAndContributorAgreement(boolean acceptTermsOfUseAndContributorAgreement) {
        this.acceptTermsOfUseAndContributorAgreement = acceptTermsOfUseAndContributorAgreement;
    }
}