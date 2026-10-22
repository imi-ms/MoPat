package de.imi.mopat.service;

import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.helper.controller.ApplicationMailer;
import de.imi.mopat.model.Encounter;
import de.imi.mopat.model.enumeration.EncounterScheduledMailStatus;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private final ApplicationMailer applicationMailer;
    private final MessageSource messageSource;
    private final ConfigurationDao configurationDao;

    public MailService(ApplicationMailer applicationMailer, MessageSource messageSource,
        ConfigurationService configurationService, ConfigurationDao configurationDao) {
        this.applicationMailer = applicationMailer;
        this.messageSource = messageSource;
        this.configurationDao = configurationDao;
    }

    public MailSendingStatus sendEncounterMail(Encounter encounter) {
        boolean success = encounter.sendMail(applicationMailer, messageSource,
            configurationDao.getBaseURL());
        if (success) {
            return MailSendingStatus.SUCCESS;
        }
        if (EncounterScheduledMailStatus.ADDRESS_REJECTED.equals(
            encounter.getEncounterScheduled().getMailStatus())) {
            return MailSendingStatus.INVALID_ADDRESS;
        } else {
            return MailSendingStatus.FAILURE;
        }
    }

}
