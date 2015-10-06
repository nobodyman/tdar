package org.tdar.core.service.external;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.entity.HasEmail;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.notification.Email.Status;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.GenericDao;
import org.tdar.core.service.FreemarkerService;
import org.tdar.utils.EmailMessageType;
import org.tdar.utils.MessageHelper;

/**
 * $Id$
 * 
 * Provides email capabilities.
 * 
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
@Service
public class EmailService {

    private static final TdarConfiguration CONFIG = TdarConfiguration.getInstance();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private MailSender mailSender;

    @Autowired
    private GenericDao genericDao;

    @Autowired
    private FreemarkerService freemarkerService;

    /*
     * sends a message using a freemarker template instead of a string; templates are stored in src/main/resources/freemarker-templates
     */
    public void queueWithFreemarkerTemplate(String templateName, Object dataModel, Email email) {
        try {
            email.setMessage(freemarkerService.render(templateName, dataModel));
            queue(email);
        } catch (IOException fnf) {
            logger.error("Email template file not found (" + templateName + ")", fnf);
        }
    }

    /**
     * Sends an email message to the given recipients. If no recipients are passed in, defaults to TdarConfiguration.getSystemAdminEmail().
     * 
     * @param emailMessage
     * @param subject
     * @param recipients
     *            set of String varargs
     */
    public void queue(Email email) {
        logger.debug("Queuing email {}", email);
        enforceFromAndTo(email);
        genericDao.save(email);
    }

    private void enforceFromAndTo(Email email) {
        if (StringUtils.isBlank(email.getTo())) {
            email.addToAddress(CONFIG.getSystemAdminEmail());
        }
        if (StringUtils.isBlank(email.getFrom())) {
            email.setFrom(getFromEmail());
        }
    }

    /**
     * Sends an email message to the given recipients. If no recipients are passed in, defaults to TdarConfiguration.getSystemAdminEmail().
     * 
     * @param emailMessage
     * @param subject
     * @param recipients
     *            set of String varargs
     */
    public void send(Email email) {
        logger.debug("sending: {}", email);
        if (email.getNumberOfTries() < 1) {
            logger.debug("too many tries {}", email.getStatus());
            email.setStatus(Status.ERROR);
            genericDao.saveOrUpdate(email);
        }
        if (email.getStatus() != Status.QUEUED) {
            logger.trace("email rejected -- not queued {}", email.getStatus());
            return;
        }
        enforceFromAndTo(email);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            // Message message = new MimeMessage(session);
            message.setFrom(email.getFrom());
            message.setSubject(email.getSubject());
            message.setTo(email.getToAsArray());
            message.setText(email.getMessage());
            mailSender.send(message);
            email.setStatus(Status.SENT);
            email.setDateSent(new Date());
        } catch (MailException me) {
            email.setNumberOfTries(email.getNumberOfTries() - 1);
            email.setErrorMessage(me.getMessage());
            logger.error("email error: {} {}", email, me);
        }
        genericDao.saveOrUpdate(email);
    }

    public String getFromEmail() {
        return CONFIG.getDefaultFromEmail();
    }

    /**
     * The mailSender allows us to plug in our own SMTP server to test
     * 
     * @return the mailSender
     */
    public MailSender getMailSender() {
        return mailSender;
    }

    /**
     * @param mailSender
     *            the mailSender to set
     */
    @Autowired
    @Qualifier("mailSender")
    public void setMailSender(MailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Transactional(readOnly = false)
    public Email constructEmail(Person from, HasEmail to, Resource resource, String subject, String messageBody, EmailMessageType type) {
        return constructEmail(from, to, resource, subject, messageBody, type, null);
    }

    @Transactional(readOnly = false)
    public Email constructEmail(Person from, HasEmail to, Resource resource, String subjectSuffix, String messageBody, EmailMessageType type,  Map<String, String[]> params) {
        Email email = new Email();
        genericDao.markWritable(email);
        email.setFrom(CONFIG.getDefaultFromEmail());
        if (CONFIG.isSendEmailToTester()) {
            email.setTo(from.getEmail());
        }
        email.setTo(to.getEmail());
        String subject = String.format("%s: %s [id: %s] %s", CONFIG.getSiteAcronym(), MessageHelper.getMessage(type.getLocaleKey()), resource.getId(),
                from.getProperName());
        if (StringUtils.isNotBlank(subjectSuffix)) {
            subject += " - " + subjectSuffix;
        }
        email.setSubject(subject);
        email.setStatus(Status.IN_REVIEW);
        Map<String, Object> map = new HashMap<>();
        map.put("from", from);
        map.put("to", to);
        map.put("baseUrl", CONFIG.getBaseUrl());
        map.put("siteAcronym", CONFIG.getSiteAcronym());
        map.put("serviceProvider", CONFIG.getServiceProvider());
        if (MapUtils.isNotEmpty(params)) {
            map.putAll(params);
        }
        if (resource != null) {
            map.put("resource", resource);
        }
        map.put("message", messageBody);
        map.put("type", type);
        email.setMessage(messageBody);
        queueWithFreemarkerTemplate(type.getTemplateName(), map, email);
        return email;

    }

    @Transactional(readOnly = false)
    public void changeEmailStatus(Status action, List<Email> emails) {
        for (Email email : emails) {
            genericDao.markUpdatable(email);
            logger.debug("changing email[id={}] status from: {} to: {}", email.getId(), email.getStatus(), action);
            email.setStatus(action);
            genericDao.saveOrUpdate(email);
        }

    }

    public List<Email> findEmailsWithStatus(Status status) {
        List<Email> allEmails = genericDao.findAll(Email.class);
        List<Email> toReturn = new ArrayList<>();
        for (Email email : allEmails) {
            if (email.getStatus() == status) {
                toReturn.add(email);
            }
        }
        return toReturn;
    }

}