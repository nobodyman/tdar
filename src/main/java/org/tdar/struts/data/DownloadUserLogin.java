package org.tdar.struts.data;

import org.tdar.core.service.external.UserLogin;

public class DownloadUserLogin extends UserLogin {

    private static final long serialVersionUID = -6157970041213328371L;

    public DownloadUserLogin(AntiSpamHelper h) {
        super(h);
    }

    @Override
    public String getPrefix() {
        return "downloadUserLogin.";
    };

    public DownloadUserLogin() {
        super(null);
    }

}
