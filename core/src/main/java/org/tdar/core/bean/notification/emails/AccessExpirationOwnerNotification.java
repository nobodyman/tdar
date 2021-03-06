package org.tdar.core.bean.notification.emails;

import java.util.Arrays;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.tdar.configuration.TdarConfiguration;
import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.notification.EmailType;
import org.tdar.utils.MessageHelper;

@Entity
@DiscriminatorValue("ACCESS_EXP_OWNER")
public class AccessExpirationOwnerNotification extends Email {

    /**
     * 
     */
    private static final long serialVersionUID = 6672547635830710656L;

    @Override
    public String createSubjectLine() {
        return MessageHelper.getMessage(EmailType.ACCESS_EXPIRE_OWNER_NOTIFICATION.getLocaleKey(),
                Arrays.asList(TdarConfiguration.getInstance().getSiteAcronym()));
    }
}
