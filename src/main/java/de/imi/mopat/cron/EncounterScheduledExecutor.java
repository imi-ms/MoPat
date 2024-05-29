package de.imi.mopat.cron;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import de.imi.mopat.dao.BundleDao;
import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.dao.EncounterDao;
import de.imi.mopat.dao.EncounterScheduledDao;
import de.imi.mopat.helper.controller.ApplicationMailer;
import de.imi.mopat.helper.controller.Constants;
import de.imi.mopat.model.Bundle;
import de.imi.mopat.model.Encounter;
import de.imi.mopat.model.EncounterScheduled;
import de.imi.mopat.model.enumeration.EncounterScheduledSerialType;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * This class generates the {@link Encounter encounters} on a daily basis.
 */
@Service
public class EncounterScheduledExecutor {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(
        EncounterScheduledExecutor.class);

    @Autowired
    private BundleDao bundleDao;
    @Autowired
    private ConfigurationDao configurationDao;
    @Autowired
    private EncounterDao encounterDao;
    @Autowired
    private EncounterScheduledDao encounterScheduledDao;
    @Autowired
    private ApplicationMailer applicationMailer;
    @Autowired
    private MessageSource messageSource;
    @Value("${de.imi.mopat.cron.EncounterScheduledExecutor.scheduleEncounter}")
    private String cronPattern;
    private Date lastExecutionTime = null;
    private Date nextExecutionTime = null;

    /**
     * Initializes the EncounterScheduledExecutor after construction by Spring and calculates the
     * nextExecutionTime of the scheduled method.
     */
    @PostConstruct
    private void initialize() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        // Parse the execution time from the configured cron pattern
        ArrayList<String> time = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(cronPattern);
        while (matcher.find()) {
            time.add(matcher.group());
        }

        // Get the second, minute and hour parameter for the cron job to set
        // the nextExecutionDate
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, Integer.parseInt(time.get(0)));
        calendar.set(Calendar.MINUTE, Integer.parseInt(time.get(1)));
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time.get(2)));

        // If the cron job should have run today, calculate the
        // nextExecutionTime for tommorow
        if (calendar.getTime().getTime() < new Date().getTime()) {
            calendar.add(Calendar.DATE, 1);
        }

        nextExecutionTime = calendar.getTime();
    }

    /**
     * Returns the date of the last execution of the {@link #scheduleEncounter()} method.
     *
     * @return The last time when the {@link #scheduleEncounter()} method was executed. Might be
     * <code>null</code>.
     */
    public Date getLastExecutionTime() {
        return lastExecutionTime;
    }

    /**
     * Returns the date of the next execution of the {@link #scheduleEncounter()} method.
     *
     * @return The next time when the {@link #scheduleEncounter()} method will be executed. Is never
     * <code>null</code>.
     */
    public Date getNextExecutionTime() {
        return nextExecutionTime;
    }

    /**
     * Gets triggered by the value de.imi.mopat.cron.EncounterScheduledExecutor.sendEncounterMail
     * set in the mopat.properties. This method generates all {@link Encounter} that were scheduled
     * for today in {@link EncounterScheduled}.
     */
    @Scheduled(cron = "${de.imi.mopat.cron.EncounterScheduledExecutor" + ".scheduleEncounter}")
    public void scheduleEncounter() {
        Date now = new Date();
        // Generate date today at midnight
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(now);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        Date today = calendar.getTime();

        //update lastExecutionTime and nextExecutionTime
        this.lastExecutionTime = now;
        calendar.add(Calendar.DATE, 1);
        this.nextExecutionTime = calendar.getTime();

        List<EncounterScheduled> listEncounterScheduled = encounterScheduledDao.getEncounterScheduledByDate(
            today);
        for (EncounterScheduled encounterScheduled : listEncounterScheduled) {
            long timeDifference = today.getTime() - encounterScheduled.getStartDate().getTime();
            long daysBetween = TimeUnit.DAYS.convert(timeDifference, TimeUnit.MILLISECONDS);
            if ((encounterScheduled.getEncounterScheduledSerialType()
                .equals(EncounterScheduledSerialType.UNIQUELY) && encounterScheduled.getStartDate()
                .equals(today)) || (encounterScheduled.getEncounterScheduledSerialType()
                .equals(EncounterScheduledSerialType.WEEKLY) && daysBetween % 7 == 0) || (
                encounterScheduled.getEncounterScheduledSerialType()
                    .equals(EncounterScheduledSerialType.MONTHLY) && daysBetween % 30 == 0) || (
                encounterScheduled.getEncounterScheduledSerialType()
                    .equals(EncounterScheduledSerialType.REPEATEDLY)
                    && daysBetween % encounterScheduled.getRepeatPeriod() == 0)) {

                Encounter encounter = new Encounter();
                Bundle bundle = bundleDao.getElementById(encounterScheduled.getBundle().getId());
                encounter.setBundle(bundle);
                encounter.setCaseNumber(encounterScheduled.getCaseNumber());
                encounter.setStartTime(new Timestamp(today.getTime()));
                encounter.setEncounterScheduled(encounterScheduled);

                encounter.sendMail(applicationMailer, messageSource, configurationDao.getBaseURL());
                // Store last reminder date, which was set in sendMail
                encounterScheduledDao.merge(encounterScheduled);
                bundle.addEncounter(encounter);
                bundleDao.merge(bundle);
            }
        }
    }

    /**
     * Gets triggered by the value de.imi.mopat.cron.EncounterScheduledExecutor
     * .deleteFinishedEncounterMailaddress set in the mopat.properties. This method deletes all mail
     * addresses of finished {@link EncounterScheduled}.
     */
    @Scheduled(cron = "${de.imi.mopat.cron.EncounterScheduledExecutor"
        + ".deleteFinishedEncounterMailaddress}")
    public void deleteFinishedEncounterMailadress() {
        Long finishedEncounterMailaddressTimeWindowInMillis = configurationDao.getFinishedEncounterMailaddressTimeWindow();
        if (finishedEncounterMailaddressTimeWindowInMillis == null) {
            LOGGER.info("Could not find a value for the property {}; will take "
                    + "the default (30 days) instead",
                Constants.FINISHED_ENCOUNTER_MAILADDRESS_TIME_WINDOW_IN_MILLIS);
            finishedEncounterMailaddressTimeWindowInMillis = (30L * 24L * 60L * 60L * 1000L);
        } else if (finishedEncounterMailaddressTimeWindowInMillis == -1) {
            return;
        }

        List<EncounterScheduled> pastEncounterScheduled = new ArrayList<>();
        long now = System.currentTimeMillis();

        // Try to get all past EncounterScheduled from the database
        try {
            pastEncounterScheduled.addAll(encounterScheduledDao.getPastEncounterScheduled());
        } catch (Exception e) {
            LOGGER.error("Something went wrong while checking for past "
                + "EncounterScheduled. Since this is important for "
                + "not storing the patients email adresses too "
                + "long, investigate this error ASAP", e);
        }

        // Check if every Encounter of a past EncounterScheduled is done and
        // the time window is over and remove the mail address of this
        // EncounterScheduled
        outerloop:
        for (EncounterScheduled encouterScheduled : pastEncounterScheduled) {
            if (!encouterScheduled.getEncounters().isEmpty()) {
                for (Encounter encounter : encouterScheduled.getEncounters()) {
                    if (encounter.getEndTime() == null || (encounter.getEndTime().getTime()
                        + finishedEncounterMailaddressTimeWindowInMillis) > now) {
                        continue outerloop;
                    }
                }
                encouterScheduled.setEmail(null);
                encounterScheduledDao.merge(encouterScheduled);
            }
        }
    }
}
