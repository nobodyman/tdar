package org.tdar.core.bean.notification;

import java.util.ArrayList;
import java.util.List;

import org.tdar.core.bean.HasLabel;
import org.tdar.core.bean.Localizable;
import org.tdar.core.bean.notification.aws.AccessRequestCustomMessage;
import org.tdar.core.bean.notification.aws.AccessRequestGrantedMessage;
import org.tdar.core.bean.notification.aws.AccessRequestRejectedMessage;
import org.tdar.core.bean.notification.aws.AdminQuarantineReviewMessage;
import org.tdar.core.bean.notification.aws.AdminReportNewUsersMessage;
import org.tdar.core.bean.notification.aws.InviteAcceptedMessage;
import org.tdar.core.bean.notification.aws.InviteMessage;
import org.tdar.core.bean.notification.aws.MonthlyUserStatisticsMessage;
import org.tdar.core.bean.notification.aws.TestAwsMessage;
import org.tdar.utils.MessageHelper;

public enum EmailType implements Localizable, HasLabel {
	INVITE("invite/invite.ftl","test@tdar.org",InviteMessage.class),
	INVITE_ACCEPTED("invite/invite-accepted.ftl","test@tdar.org",InviteAcceptedMessage.class),
	NEW_USER_NOTIFY("email_new_users.ftl"),
	NEW_USER_WELCOME("email-welcome.ftl"),
	TRANSACTION_COMPLETE_ADMIN("transaction-complete-admin.ftl"),
	
	PERMISSION_REQUEST_ACCEPTED("email-form/access-request-granted.ftl","test@tdar.org",AccessRequestGrantedMessage.class),
	PERMISSION_REQUEST_REJECTED("email-form/access-request-rejected.ftl","test@tdar.org",AccessRequestRejectedMessage.class),
	PERMISSION_REQUEST_CUSTOM("email-form/custom-accept.ftl","test@tdar.org",AccessRequestCustomMessage.class),
	
	OVERDRAWN_NOTIFICATION("overdrawn-user.ftl"),
	RESOURCE_EXPORT("resource-export-email.ftl"),
	
	ADMIN_NOTIFICATION("auth-report.ftl"),
	ADMIN_NEW_USER_REPORT("email_new_users.ftl",null,AdminReportNewUsersMessage.class),
	ADMIN_QUARANTINE_REVIEW("email_review_message.ftl",null,AdminQuarantineReviewMessage.class),
	ADMIN_EMBARGO_EXPIRE("embargo/expiration-admin.ftl"),
	ADMIN_OVERDRAWN_NOTIFICATION("overdrawn-admin.ftl"),
	
	MONTHLY_USER_STATISTICS("monthly_user_statistics.ftl","test@tdar.org",MonthlyUserStatisticsMessage.class),
	TEST_EMAIL("test-email.ftl", "test@tdar.org", TestAwsMessage.class),

	
	//Refactored from EmailMessageType.
    CONTACT("email-form/contact.ftl"),
    REQUEST_ACCESS("email-form/access-request.ftl"),
    SUGGEST_CORRECTION("email-form/correction.ftl"),
    MERGE_PEOPLE("email-form/merge-people.ftl"),
    CUSTOM("email-form/custom-request.ftl");
	
	/**
	 * a string representation of the .ftl template to use
	 */
	private final String templateLocation; 
	/**
	 * The sending address to populate on the email
	 */
	private String fromAddress;
		
	private Class<? extends Email> emailClass;
	
	private EmailType(String template){
		this.templateLocation = template;
	}
	
	private EmailType(String template, String fromAddress){
		this(template);
		this.setFromAddress(fromAddress);
	}
	
	private EmailType(String template,  String fromAddress, Class<? extends Email> emailClass) {
		this(template, fromAddress);
		this.setEmailClass(emailClass);
    }
	
    public boolean requiresResource() {
        return true;
    }


    @Override
    public String getLabel() {
        return MessageHelper.getMessage(getLocaleKey());
    }

    @Override
    public String getLocaleKey() {
        return MessageHelper.formatLocalizableKey(this);
    }
	
    public static List<EmailType> valuesWithoutConfidentialFiles() {
        ArrayList<EmailType> types = new ArrayList<EmailType>();
        for (EmailType type : valuesForUserSelection()) {
            switch (type) {
                case REQUEST_ACCESS:
                case CUSTOM:
                case MERGE_PEOPLE:
                    break;
                default:
                    types.add(type);
            }
        }
        return types;
    }

	public static List<EmailType> valuesWithoutCustom() {
        ArrayList<EmailType> types = new ArrayList<EmailType>();
        for (EmailType type : valuesForUserSelection()) {
            if (type != CUSTOM && type != MERGE_PEOPLE) {
                types.add(type);
            }
        }
        return types;
	}
	
	public static List<EmailType> valuesForUserSelection(){
        ArrayList<EmailType> types = new ArrayList<EmailType>();
        for (EmailType type : values()) {
            switch (type) {
                case CONTACT:
                case REQUEST_ACCESS:
                case SUGGEST_CORRECTION:
                case MERGE_PEOPLE:
                case CUSTOM:
                    types.add(type);
                    break;
			default:
				break;
            }
        }
        return types;
		
	}
	
	
	public String getTemplateLocation() {
		return templateLocation;
	}

	public Class<? extends Email> getEmailClass() {
		return emailClass;
	}

	public void setEmailClass(Class<? extends Email> emailClass) {
		this.emailClass = emailClass;
	}
	
	public String getFromAddress() {
		return fromAddress;
	}

	public void setFromAddress(String fromAddress) {
		this.fromAddress = fromAddress;
	}
}
