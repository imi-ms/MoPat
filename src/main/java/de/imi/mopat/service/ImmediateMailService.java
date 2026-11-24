package de.imi.mopat.service;

import de.imi.mopat.cron.EncounterScheduledExecutor;
import de.imi.mopat.model.Bundle;
import de.imi.mopat.model.Encounter;
import de.imi.mopat.model.EncounterScheduled;
import de.imi.mopat.model.dto.EncounterScheduledDTO;
import de.imi.mopat.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
@Service
public class ImmediateMailService {
    private final MailService mailService;

    public ImmediateMailService(MailService mailService) {
        this.mailService = mailService;
    }

    public boolean shouldSendEmailImmediately(EncounterScheduled scheduled, EncounterScheduledExecutor executor){
        return (shouldSendMailToday(scheduled) && (shouldSendBecauseNeverExecuted(executor)
                || shouldSendBecauseAlreadyExecutedToday(executor))
        );
    }

    private boolean shouldSendMailToday(EncounterScheduled scheduled){
        return (isSameDay(scheduled.getStartDate(), new Date()));
    }

    private boolean shouldSendBecauseNeverExecuted(EncounterScheduledExecutor executor){
        if (executor.getLastExecutionTime() != null) return false;
        return (!isSameDay(executor.getNextExecutionTime(), new Date()));
    }

    private boolean shouldSendBecauseAlreadyExecutedToday(EncounterScheduledExecutor executor){
        if (executor.getLastExecutionTime() == null) return false;
        return (isSameDay(executor.getLastExecutionTime(), new Date()));
    };

    private boolean isSameDay(Date dateOne, Date dateTwo) {

        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(dateOne);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(dateTwo);

        return cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
                && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)
                && cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
    }

public MailSendingStatus createAndSendEncounter(
        EncounterScheduled scheduled
) {

    Encounter encounter = createEncounterForImmediateSending(scheduled);

    return mailService.sendEncounterMail(encounter);
}

    private Encounter createEncounterForImmediateSending(
            EncounterScheduled scheduled
    ) {

        Encounter encounter = new Encounter();

        encounter.setEncounterScheduled(scheduled);
        encounter.setBundle(scheduled.getBundle());
        encounter.setClinic(scheduled.getClinic());
        encounter.setCaseNumber(scheduled.getCaseNumber());

        Bundle bundle = scheduled.getBundle();
        bundle.addEncounter(encounter);

        encounter.setStartTime(
                new Timestamp(getTodayAtMidnight().getTime())
        );

        return encounter;
    }

    private Date getTodayAtMidnight() {

        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);

        return calendar.getTime();
    }
}
