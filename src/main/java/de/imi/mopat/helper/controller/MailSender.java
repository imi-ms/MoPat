package de.imi.mopat.helper.controller;

import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.model.Configuration;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 *
 */
public class MailSender extends JavaMailSenderImpl {

    // Initialize every needed configuration information as a final string
    private final String className = this.getClass().getName();
    private final String mailSenderHostProperty = "mailSenderHost";
    private final String mailSenderPortProperty = "mailSenderPort";
    private final String mailSenderUsernameProperty = "mailSenderUsername";
    private final String mailSenderPasswordProperty = "mailSenderPassword";
    private final String mailSenderAuthProperty = "mailSenderAuth";
    private final String mailSenderStartTLSProperty = "mailSenderStartTLS";
    // Configuration: The name of the attribute for the email
    // which appears in application mail footer
    private final String mailFooterEmailProperty = "mailFooterMail";
    // Configuration: The name of the attribute for the phone number
    // which appears in application mail footer
    private static final String mailFooterPhoneProperty = "mailFooterPhone";
    // Configuration: The name of the attribute for the phone number
    // which appears in application mail footer
    private final String mailFromProperty = "mailFrom";

    @Autowired
    private ConfigurationDao configurationDao;

    public MailSender() {
    }

    /**
     * This method will be called if one of the configuration entries which is used in this class
     * has changed in the configuration interface {@link Configuration#getUpdateMethod()}.
     */
    @PostConstruct
    public void update() {
        this.setHost(getMailSenderHost());
        this.setPort(getMailSenderPort());
        this.setUsername(getMailSenderUsername());
        this.setPassword(getMailSenderPassword());

        // TODO Final STRINGS für die Properties
        this.getJavaMailProperties().setProperty("mail.smtp.from", getMailSenderFrom());
        this.getJavaMailProperties().setProperty("mail.smtp.auth", isMailSenderAuth().toString());
        this.getJavaMailProperties()
            .setProperty("mail.smtp.starttls.enable", isMailSenderTLSEnabled().toString());
        this.getJavaMailProperties().setProperty("mail.smtp.ssl.protocols", "TLSv1.2");
    }

    @Override
    public void send(final SimpleMailMessage simpleMessage) throws MailException {
        super.send(simpleMessage);
    }

    /**
     * Returns the mail sender host as a string from the {@link ConfigurationDao} by using the name
     * of this class and the appropriate attribute name.
     *
     * @return The mail sender host as a string.
     */
    private String getMailSenderHost() {
        Configuration configuration = configurationDao.getConfigurationByAttributeAndClass(
            mailSenderHostProperty, className);
        return configuration.getValue();
    }

    /**
     * Returns the mail sender port as a integer from the{@link ConfigurationDao} by using the name
     * of this class and the appropriate attribute name.
     *
     * @return The mail sender port as a integer.
     */
    private Integer getMailSenderPort() {
        Configuration configuration = configurationDao.getConfigurationByAttributeAndClass(
            mailSenderPortProperty, className);
        return Integer.valueOf(configuration.getValue());
    }

    /**
     * Returns the mail sender username as a string from the {@link ConfigurationDao} by using the
     * name of this class and the appropriate attribute name.
     *
     * @return The mail sender username as a string.
     */
    private String getMailSenderUsername() {
        Configuration configuration = configurationDao.getConfigurationByAttributeAndClass(
            mailSenderUsernameProperty, className);
        return configuration.getValue();
    }

    /**
     * Returns the mail sender password as a string from the {@link ConfigurationDao} by using the
     * name of this class and the appropriate attribute name.
     *
     * @return The mail sender password as a string.
     */
    private String getMailSenderPassword() {
        Configuration configuration = configurationDao.getConfigurationByAttributeAndClass(
            mailSenderPasswordProperty, className);
        return configuration.getValue();
    }

    /**
     * Returns true if the mail sender authentication is activated and false otherwise. Get this
     * boolean from the {@link ConfigurationDao} by using the name of this class and the appropriate
     * attribute name.
     *
     * @return The configured mailSenderAuth boolean.
     */
    private Boolean isMailSenderAuth() {
        Configuration configuration = configurationDao.getConfigurationByAttributeAndClass(
            mailSenderAuthProperty, className);
        return Boolean.valueOf(configuration.getValue());
    }

    /**
     * Returns true if TLS ist enabled for the mail sender and false otherwise. Get this boolean
     * from the {@link ConfigurationDao} by using the name of this class and the appropriate
     * attribute name.
     *
     * @return The configured mailSenderTLSEnabled boolean.
     */
    private Boolean isMailSenderTLSEnabled() {
        Configuration configuration = configurationDao.getConfigurationByAttributeAndClass(
            mailSenderStartTLSProperty, className);
        return Boolean.valueOf(configuration.getValue());
    }

    /**
     * Returns the email address from which the {@link MailSender} sends mails.
     *
     * @return The email address from which the {@link MailSender} sends mails.
     */
    public String getMailSenderFrom() {
        Configuration configuration = configurationDao.getConfigurationByAttributeAndClass(
            mailFromProperty, className);
        return configuration.getValue();
    }

    /**
     * Returns the email address which appears in the footer of application mails.
     *
     * @return The email address which appears in the footer of application mails.
     */
    public String getMailFooterEMail() {
        Configuration configuration = configurationDao.getConfigurationByAttributeAndClass(
            mailFooterEmailProperty, className);
        return configuration.getValue();
    }

    /**
     * Returns the phone number which appears in the footer of application mails.
     *
     * @return The phone number which appears in the footer of application mails.
     */
    public String getMailFooterPhone() {
        Configuration configuration = configurationDao.getConfigurationByAttributeAndClass(
            mailFooterPhoneProperty, className);
        return configuration.getValue();
    }
}