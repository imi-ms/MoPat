package de.imi.mopat.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.imi.mopat.cron.EncounterScheduledExecutor;
import de.imi.mopat.dao.BundleDao;
import de.imi.mopat.dao.EncounterScheduledDao;
import de.imi.mopat.helper.model.EncounterScheduledDTOMapper;
import de.imi.mopat.model.Bundle;
import de.imi.mopat.model.EncounterScheduled;
import de.imi.mopat.model.dto.EncounterScheduledDTO;
import de.imi.mopat.service.helper.SetRepeatConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EncounterScheduledServiceTest {

    @Mock
    private BundleDao bundleDao;

    @Mock
    private EncounterScheduledDao encounterScheduledDao;

    @Mock
    private EncounterScheduledDTOMapper mapper;

    @Mock
    private ImmediateMailService immediateMailService;

    @Mock
    private AuditService auditService;

    @Mock
    private SetRepeatConfiguration setRepeatConfiguration;

    @InjectMocks
    private EncounterScheduledService encounterScheduledService;

    @Mock
    private EncounterScheduledDTO dto;

    @Mock
    private EncounterScheduled scheduled;

    @Mock
    private Bundle bundle;

    @Mock
    private EncounterScheduledExecutor executor;

    @Before
    public void setUp() {
        when(mapper.mapToEntity(dto)).thenReturn(scheduled);
        when(scheduled.getBundle()).thenReturn(bundle);
    }

    /**
     * Test of {@link EncounterScheduledService#save(EncounterScheduledDTO, EncounterScheduledExecutor)} )},
     */

    @Test
    public void testSaveWhenShouldSendImmediatelyAndMailSucceeds() {
        when(immediateMailService.shouldSendEmailImmediately(scheduled, executor)).thenReturn(true);
        when(immediateMailService.createAndSendEncounter(scheduled)).thenReturn(MailSendingStatus.SUCCESS);

        MailSendingStatus result = encounterScheduledService.save(dto, executor);

        assertEquals("should return SUCCESS", MailSendingStatus.SUCCESS, result);
    }

    @Test
    public void testSaveWhenShouldSendImmediatelyAndAddressInvalid() {
        when(immediateMailService.shouldSendEmailImmediately(scheduled, executor)).thenReturn(true);
        when(immediateMailService.createAndSendEncounter(scheduled)).thenReturn(MailSendingStatus.INVALID_ADDRESS);

        MailSendingStatus result = encounterScheduledService.save(dto, executor);

        assertEquals("Should return INVALID_ADDRESS", MailSendingStatus.INVALID_ADDRESS, result);
    }

    @Test
    public void testSaveWhenShouldSendImmediatelyAndMailFails() {
        when(immediateMailService.shouldSendEmailImmediately(scheduled, executor)).thenReturn(true);
        when(immediateMailService.createAndSendEncounter(scheduled)).thenReturn(MailSendingStatus.FAILURE);

        MailSendingStatus result = encounterScheduledService.save(dto, executor);

        assertEquals("Should return FAILURE", MailSendingStatus.FAILURE, result);
    }

    @Test
    public void testSaveWhenShouldNotSendImmediately() {
        when(immediateMailService.shouldSendEmailImmediately(scheduled, executor)).thenReturn(false);

        MailSendingStatus result = encounterScheduledService.save(dto, executor);

        assertEquals("Should return SUCCESS when not sending immediately ",
            MailSendingStatus.SUCCESS, result);
        verify(immediateMailService, never()).createAndSendEncounter(any());
    }

    @Test
    public void testSaveAlwaysAppliesRepeatConfiguration() {
        when(immediateMailService.shouldSendEmailImmediately(scheduled, executor)).thenReturn(false);

        encounterScheduledService.save(dto, executor);

        verify(setRepeatConfiguration).apply(dto);
    }

    @Test
    public void testSaveAlwaysMergesScheduledEncounter() {
        when(immediateMailService.shouldSendEmailImmediately(scheduled, executor)).thenReturn(false);

        encounterScheduledService.save(dto, executor);

        verify(encounterScheduledDao).merge(scheduled);
    }

    @Test
    public void testSaveWhenBundleIsNotNullMergesBundle() {
        when(immediateMailService.shouldSendEmailImmediately(scheduled, executor)).thenReturn(false);

        encounterScheduledService.save(dto, executor);

        verify(bundleDao).merge(bundle);
    }

    @Test
    public void testSaveWhenBundleIsNullDoesNotMergeBundle() {
        when(scheduled.getBundle()).thenReturn(null);
        when(immediateMailService.shouldSendEmailImmediately(scheduled, executor)).thenReturn(false);

        encounterScheduledService.save(dto, executor);

        verify(bundleDao, never()).merge(any());
    }

    @Test
    public void testSaveAlwaysWritesAuditEntry() {
        when(immediateMailService.shouldSendEmailImmediately(scheduled, executor)).thenReturn(false);

        encounterScheduledService.save(dto, executor);

        verify(auditService).writeScheduledEncounterAudit(
            EncounterScheduledService.class, "save", scheduled);
    }

    @Test
    public void testSaveWhenShouldNotSendImmediatelyNeverCallsCreateAndSend() {
        when(immediateMailService.shouldSendEmailImmediately(scheduled, executor)).thenReturn(false);

        encounterScheduledService.save(dto, executor);

        verify(immediateMailService, never()).createAndSendEncounter(any());
    }
}