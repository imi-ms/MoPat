package de.imi.mopat.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.helper.controller.ApplicationMailer;
import de.imi.mopat.model.Encounter;
import de.imi.mopat.model.EncounterScheduled;
import de.imi.mopat.model.enumeration.EncounterScheduledMailStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.MessageSource;

@RunWith(MockitoJUnitRunner.class)
public class MailServiceTest {

    private static final String BASE_URL = "http://localhost/";

    @Mock
    private ApplicationMailer applicationMailer;

    @Mock
    private MessageSource messageSource;

    @Mock
    private ConfigurationDao configurationDao;

    @Mock
    private ConfigurationService configurationService;

    @InjectMocks
    private MailService mailService;

    @Mock
    private Encounter encounter;

    @Mock
    private EncounterScheduled encounterScheduled;

    @Before
    public void setUp() {
        when(configurationDao.getBaseURL()).thenReturn(BASE_URL);
        when(encounter.getEncounterScheduled()).thenReturn(encounterScheduled);
    }

    /**
     * Test of {@link MailService#sendEncounterMail(Encounter)}
     */

    @Test
    public void testSendEncounterMailWhenMailSentSuccessfully() {
        when(encounter.sendMail(applicationMailer, messageSource, BASE_URL)).thenReturn(true);

        MailSendingStatus result = mailService.sendEncounterMail(encounter);

        assertEquals("should return SUCCESS", MailSendingStatus.SUCCESS, result);
    }

    @Test
    public void testSendEncounterMailWhenMailFailsAndAddressIsRejected() {
        when(encounter.sendMail(applicationMailer, messageSource, BASE_URL)).thenReturn(false);
        when(encounterScheduled.getMailStatus())
            .thenReturn(EncounterScheduledMailStatus.ADDRESS_REJECTED);

        MailSendingStatus result = mailService.sendEncounterMail(encounter);

        assertEquals("should return INVALID_ADDRESS", MailSendingStatus.INVALID_ADDRESS, result);
    }

    @Test
    public void testSendEncounterMailWhenMailFailsAndStatusIsActive() {
        when(encounter.sendMail(applicationMailer, messageSource, BASE_URL)).thenReturn(false);
        when(encounterScheduled.getMailStatus()).thenReturn(EncounterScheduledMailStatus.ACTIVE);

        MailSendingStatus result = mailService.sendEncounterMail(encounter);

        assertEquals("should return FAILURE", MailSendingStatus.FAILURE, result);
    }

    @Test
    public void testSendEncounterMailWhenMailFailsAndStatusIsDeactivatedPatient() {
        when(encounter.sendMail(applicationMailer, messageSource, BASE_URL)).thenReturn(false);
        when(encounterScheduled.getMailStatus())
            .thenReturn(EncounterScheduledMailStatus.DEACTIVATED_PATIENT);

        MailSendingStatus result = mailService.sendEncounterMail(encounter);

        assertEquals("should return FAILURE", MailSendingStatus.FAILURE, result);
    }
}