package de.imi.mopat.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.imi.mopat.cron.EncounterScheduledExecutor;
import de.imi.mopat.model.EncounterScheduled;
import de.imi.mopat.model.EncounterScheduledTest;
import java.util.Calendar;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ImmediateMailServiceTest {

    @Mock
    private MailService mailService;

    @InjectMocks
    private ImmediateMailService immediateMailService;

    @Mock
    private EncounterScheduled scheduled;

    @Mock
    private EncounterScheduledExecutor executor;

    private Date today;
    private Date yesterday;
    private Date tomorrow;

    @Before
    public void setUp() {
        today = new Date();

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -1);
        yesterday = cal.getTime();

        cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        tomorrow = cal.getTime();
    }
    /**
     * Test of {@link ImmediateMailService#shouldSendEmailImmediately(EncounterScheduled, EncounterScheduledExecutor)}, when StartState is tomorrow
     */
    @Test
    public void testShouldSendEmailImmediatelyWhenStartDateIsTomorrow() {
        when(scheduled.getStartDate()).thenReturn(tomorrow);

        assertFalse("Email does not send immediately, when start date not today",
            immediateMailService.shouldSendEmailImmediately(scheduled, executor));
    }

    /**
     * Test of {@link ImmediateMailService#shouldSendEmailImmediately(EncounterScheduled, EncounterScheduledExecutor)},
     * when StartDate is today and executor never executed
     */
    @Test
    public void testShouldSendEmailImmediatelyWhenStartDateTodayNeverExecuted() {
        when(scheduled.getStartDate()).thenReturn(today);
        when(executor.getLastExecutionTime()).thenReturn(null);
        when(executor.getNextExecutionTime()).thenReturn(tomorrow);

        assertTrue("Email sends immediately, when it start date today and executor never executed",
            immediateMailService.shouldSendEmailImmediately(scheduled, executor));
    }
    /**
     * Test of {@link ImmediateMailService#shouldSendEmailImmediately(EncounterScheduled, EncounterScheduledExecutor)},
     * when StartDate is today but next execution time is today
     */
    @Test
    public void testShouldSendEmailImmediatelyWhenStartDateTodayNeverExecutedNextExecIsToday() {
        when(scheduled.getStartDate()).thenReturn(today);
        when(executor.getLastExecutionTime()).thenReturn(null);
        when(executor.getNextExecutionTime()).thenReturn(today);

        assertFalse("Email does not send immediately, when start date is today, executor never executed but next execution time is today",
            immediateMailService.shouldSendEmailImmediately(scheduled, executor));
    }

    /**
     * Test of {@link ImmediateMailService#shouldSendEmailImmediately(EncounterScheduled, EncounterScheduledExecutor)},
     * when StartDate is today but already executed
     */
    @Test
    public void testShouldSendEmailImmediatelyWhenStartDateTodayAndAlreadyExecutedToday() {
        when(scheduled.getStartDate()).thenReturn(today);
        when(executor.getLastExecutionTime()).thenReturn(today);

        assertTrue("Email sends immediately, when start date is today and last execution time was today",
            immediateMailService.shouldSendEmailImmediately(scheduled, executor));
    }

    @Test
    public void testShouldSendEmailImmediatelyWhenStartDateTodayAndLastExecutionWasYesterday() {
        when(scheduled.getStartDate()).thenReturn(today);
        when(executor.getLastExecutionTime()).thenReturn(yesterday);

        assertFalse("Email does not send immediately, when start date is today and last execution time was yesterday",
            immediateMailService.shouldSendEmailImmediately(scheduled, executor));
    }
    /**
     * Test of {@link ImmediateMailService#createAndSendEncounter(EncounterScheduled)},
     */
    @Test
    public void testCreateAndSendEncounterDelegatesToMailService() {
        EncounterScheduled realScheduled = EncounterScheduledTest.getNewValidEncounterScheduled();
        when(mailService.sendEncounterMail(any())).thenReturn(MailSendingStatus.SUCCESS);

        MailSendingStatus result = immediateMailService.createAndSendEncounter(realScheduled);

        assertEquals("returns SUCCESS", MailSendingStatus.SUCCESS, result);
        verify(mailService).sendEncounterMail(any());
    }

    @Test
    public void testCreateAndSendEncounterWhenMailFails() {
        EncounterScheduled realScheduled = EncounterScheduledTest.getNewValidEncounterScheduled();
        when(mailService.sendEncounterMail(any())).thenReturn(MailSendingStatus.FAILURE);

        MailSendingStatus result = immediateMailService.createAndSendEncounter(realScheduled);

        assertEquals("returns FAILURE", MailSendingStatus.FAILURE, result);
    }

    @Test
    public void testCreateAndSendEncounterWhenAddressInvalid() {
        EncounterScheduled realScheduled = EncounterScheduledTest.getNewValidEncounterScheduled();
        when(mailService.sendEncounterMail(any())).thenReturn(MailSendingStatus.INVALID_ADDRESS);

        MailSendingStatus result = immediateMailService.createAndSendEncounter(realScheduled);

        assertEquals("returns INVALID_ADDRESS", MailSendingStatus.INVALID_ADDRESS, result);
    }
}