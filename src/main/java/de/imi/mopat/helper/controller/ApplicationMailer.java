package de.imi.mopat.helper.controller;

import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.model.Configuration;

import java.util.Set;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.MailParseException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * @since v1.1
 */
@Service
public class ApplicationMailer {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ApplicationMailer.class);

    @Autowired
    private MailSender mailSender;
    @Autowired
    private ConfigurationDao configurationDao;

    // Initialize every needed configuration information as a final string
    private final String className = this.getClass().getName();
    private final String isApplicationMailerIsActivatedProperty = "applicationMailerActivated";

    /**
     * This method will compose and send an email message asynchronous.
     *
     * @param recipientTo               The recipient of the email message. Can be
     *                                  <code>null</code>. Should not be
     *                                  <code>null</code> if the recipientBCC is
     *                                  <code>null</code> as well.
     * @param allEMailAddressesDistinct All email addresses that shall receive the email as blind
     *                                  copy (nobody can see they are a recipient). Can be
     *                                  <code>null</code>, can be empty.
     *                                  Should not be neither of them if the recipient is
     *                                  <code>null</code> as well.
     * @param subject                   The subject of the email message.
     * @param content                   The content of the email message.
     * @param replyTo                   The email adress the recipient can reply to.
     */
    @Async
    public void sendMail(final String recipientTo, final Set<String> allEMailAddressesDistinct,
        final String subject, final String content, final String replyTo) {
        Boolean activated = getIsApplicationMailerActivated();
        if (activated) {
            SimpleMailMessage message = new SimpleMailMessage();
            if (recipientTo != null) {
                message.setTo(recipientTo);
            }
            if (allEMailAddressesDistinct != null && !allEMailAddressesDistinct.isEmpty()) {
                try {
                    message.setBcc(allEMailAddressesDistinct.toArray(
                        new String[allEMailAddressesDistinct.size()]));
                } catch (MailParseException mpe) {
                    LOGGER.error(
                        "MailParseException caught while setting the list" + " of bccs: {}", mpe);
                }
            }
            if (replyTo != null && !replyTo.isEmpty()) {
                message.setReplyTo(replyTo);
            }
            message.setSubject(subject);
            message.setText(content);
            try {
                this.mailSender.send(message);
            } catch (MailException ex) {
                // TODO add user feedback?
                LOGGER.error("An exception occured while trying to send an email "
                    + "to {} with subject {}: ", recipientTo, subject, ex);
            }
        } else {
            LOGGER.error(
                "An attemp was made to send a mail but the " + "ApplicationMailer was not active");
        }
    }

    /**
     * Returns true if the application mailer is activated and false otherwise. Get this boolean
     * from the {@link ConfigurationDao} by using the name of this class and the appropriate
     * attribute name.
     *
     * @return The configured applicationMaierActivated boolean.
     */
    private Boolean getIsApplicationMailerActivated() {
        Configuration configuration = configurationDao.getConfigurationByAttributeAndClass(
            isApplicationMailerIsActivatedProperty, className);
        return Boolean.valueOf(configuration.getValue());
    }

    /**
     * Get the mail address from which the mail sender sends mails.
     *
     * @return The mail address, which is set to the from attribute of every mail send.
     */
    public String getMailFrom() {
        return mailSender.getMailSenderFrom();
    }

    /**
     * Get the phone number which appears in the footer in the mails sent by the mail sender
     *
     * @return The phone number which appears in the footer of mails send by the mail sender.
     */
    public String getMailFooterPhone() {
        return mailSender.getMailFooterPhone();
    }

    /**
     * Get the mail address which appears in the footer in the mails sent by the mail sender
     *
     * @return The mail address which appears in the footer of mails send by the mail sender.
     */
    public String getMailFooterEMail() {
        return mailSender.getMailFooterEMail();
    }
}
