package de.imi.mopat.cron;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import de.imi.mopat.dao.AuditEntryDao;
import de.imi.mopat.dao.BundleDao;
import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.dao.EncounterDao;
import de.imi.mopat.dao.EncounterScheduledDao;
import de.imi.mopat.helper.controller.Constants;
import de.imi.mopat.model.Bundle;
import de.imi.mopat.model.enumeration.AuditEntryActionType;
import de.imi.mopat.model.enumeration.AuditPatientAttribute;
import de.imi.mopat.model.Encounter;
import de.imi.mopat.model.EncounterScheduled;

/**
 * This class checks on a regular basis (set in the mopat.properties) (see
 * {@link FrequentEncounterDeletor#deleteOldEncounters()} for {@link Encounter Encounters} that are
 * finished (i.e. their {@link Encounter#getEndTime()} is not <code>null</code>) and older than a
 * specific time (set in the mopat.properties). Encounters that fit into this filter will be deleted
 * from the database.
 *
 * @since v1.1
 */
@Service
public class FrequentEncounterDeletor {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(
        FrequentEncounterDeletor.class);
    private static final Long THIRTY_DAYS_IN_MILLISECONDS = 30L * 24L * 60L * 60L * 1000L;
    private static final Long NINETY_DAYS_IN_MILLISECONDS = 90L * 24L * 60L * 60L * 1000L;
    private static final Long ONEHUNDREDEIGHTY_DAYS_IN_MILLISECONDS =
        180L * 24L * 60L * 60L * 1000L;

    @Autowired
    private ConfigurationDao configurationDao;
    @Autowired
    private AuditEntryDao auditEntryDao;
    @Autowired
    private BundleDao bundleDao;
    @Autowired
    private EncounterDao encounterDao;
    @Autowired
    private EncounterScheduledDao encounterScheduledDao;

    /**
     * Gets triggered by the value provided in de.imi.mopat.cron.FrequentEncounterDeletor.checkTime.
     * Runs only if the {@link FrequentEncounterDeletor} is activated via the configuration. Checks
     * whether there are finished Encounters older than a given time period in milliseconds in the
     * configuration (if not given: default is 30 days) and deletes them. If anything goes wrong, an
     * error is logged. Further deletes {@link EncounterScheduled EncounterScheduleds} if all
     * adhering encounters are completed and the endDate of the scheduled encounter is a certain
     * time ago. This time can be configured. Otherwise, if there are incomplete encounters adhering
     * to the scheduled encounter delete it after a also configurable time period.
     */
    /*
     * [bt] notice, as told in the Spring documentation
     * (http://docs.spring.io/spring
     * /docs/3.0.x/api/org/springframework/scheduling
     * /annotation/Scheduled): A cron-like expression, extending the usual
     * UN*X definition to include triggers on the second
     */
    @Scheduled(cron = "${de.imi.mopat.cron.FrequentEncounterDeletor" + ".checkTime}")
    public void deleteOldEncounters() {
        //First delete the independent encounters
        boolean deleteFinishedEncounters = true;
        Long finishedEncounterTimeWindowInMillis = configurationDao.getFinishedEncounterTimeWindow();
        if (finishedEncounterTimeWindowInMillis == null) {
            LOGGER.info("Could not find a value for the property {}; will take "
                    + "the default (30 days) instead",
                Constants.FINISHED_ENCOUNTER_TIME_WINDOW_IN_MILLIS);
            finishedEncounterTimeWindowInMillis = THIRTY_DAYS_IN_MILLISECONDS;
        } else if (finishedEncounterTimeWindowInMillis == -1) {
            deleteFinishedEncounters = false;
        }

        boolean deleteIncompleteEncounters = true;
        Long incompleteEncounterTimeWindowInMillis = configurationDao.getIncompleteEncounterTimeWindow();
        if (incompleteEncounterTimeWindowInMillis == null) {
            LOGGER.info("Could not find a value for the property {}; will take "
                    + "the default (180 days) instead",
                Constants.INCOMPLETE_ENCOUNTER_TIME_WINDOW_IN_MILLIS);
            incompleteEncounterTimeWindowInMillis = ONEHUNDREDEIGHTY_DAYS_IN_MILLISECONDS;
        } else if (incompleteEncounterTimeWindowInMillis == -1) {
            deleteIncompleteEncounters = false;
        }

        List<Encounter> oldEncounters = new ArrayList<>();

        Timestamp nowMinusFinishedEncounterTimeWindow = new Timestamp(
            System.currentTimeMillis() - finishedEncounterTimeWindowInMillis);
        Timestamp nowMinusInclompleteEncounterTimeWindow = new Timestamp(
            System.currentTimeMillis() - incompleteEncounterTimeWindowInMillis);
        try {
            if (deleteFinishedEncounters) {
                oldEncounters.addAll(encounterDao.getFinishedEncounterOlderThan(
                    nowMinusFinishedEncounterTimeWindow));
            }
            if (deleteIncompleteEncounters) {
                oldEncounters.addAll(encounterDao.getIncompleteEncountersOlderThan(
                    nowMinusInclompleteEncounterTimeWindow));
            }
        } catch (Exception e) {
            LOGGER.error("Something went wrong while checking for old finished "
                    + "Encounters. Since this is important for not "
                    + "having a database of old finished encounters, " + "investigate this error ASAP",
                e);
        }

        //Now delete all encounters and encounters scheduled that are connected
        boolean deleteFinishedEncounterScheduleds = true;
        Long finishedEncounterScheduledTimeWindowInMillis = configurationDao.getFinishedEncounterScheduledTimeWindow();
        if (finishedEncounterScheduledTimeWindowInMillis == null) {
            LOGGER.info("Could not find a value for the property {}; will take "
                    + "the default (90 days) instead",
                Constants.FINISHED_ENCOUNTER_SCHEDULED_TIME_WINDOW_IN_MILLIS);
            finishedEncounterScheduledTimeWindowInMillis = NINETY_DAYS_IN_MILLISECONDS;
        } else if (finishedEncounterScheduledTimeWindowInMillis == -1) {
            deleteFinishedEncounterScheduleds = false;
        }

        boolean deleteIncompleteEncounterScheduleds = true;
        Long incompleteEncounterScheduledTimeWindowInMillis = configurationDao.getIncompleteEncounterScheduledTimeWindow();
        if (incompleteEncounterScheduledTimeWindowInMillis == null) {
            LOGGER.info("Could not find a value for the property {}; will take "
                    + "the default (180 days) instead",
                Constants.INCOMPLETE_ENCOUNTER_SCHEDULED_TIME_WINDOW_IN_MILLIS);
            incompleteEncounterScheduledTimeWindowInMillis = ONEHUNDREDEIGHTY_DAYS_IN_MILLISECONDS;
        } else if (incompleteEncounterScheduledTimeWindowInMillis == -1) {
            deleteIncompleteEncounterScheduleds = false;
        }

        List<EncounterScheduled> oldEncounterScheduleds = new ArrayList<>();

        Timestamp nowMinusFinishedEncounterScheduledTimeWindow = new Timestamp(
            System.currentTimeMillis() - finishedEncounterScheduledTimeWindowInMillis);
        Timestamp nowMinusInclompleteEncounterScheduledTimeWindow = new Timestamp(
            System.currentTimeMillis() - incompleteEncounterScheduledTimeWindowInMillis);
        try {
            if (deleteFinishedEncounterScheduleds) {
                oldEncounterScheduleds.addAll(
                    encounterScheduledDao.getFinishedEncounterScheduledOlderThan(
                        nowMinusFinishedEncounterScheduledTimeWindow));
            }
            if (deleteIncompleteEncounterScheduleds) {
                for (EncounterScheduled encounterScheduled : encounterScheduledDao.getEncounterScheduledOlderThan(
                    nowMinusInclompleteEncounterScheduledTimeWindow)) {
                    //prevent to add encounterScheduled twice
                    if (!oldEncounterScheduleds.contains(encounterScheduled)) {
                        oldEncounterScheduleds.add(encounterScheduled);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Something went wrong while checking for old finished "
                + "EncounterScheduleds. Since this is important for"
                + " not having a database of old finished "
                + "encounters, investigate this error ASAP", e);
        }

        Set<AuditPatientAttribute> patientAttributes = new HashSet<>(
            Arrays.asList(AuditPatientAttribute.values()));
        Set<String> deletedCaseNumbers = new HashSet<>();
        for (Encounter encounter : oldEncounters) {
            // Do not remove encounters that belong to scheduled encounters
            if (encounter.getEncounterScheduled() == null) {
                try {
                    Bundle bundle = encounter.getBundle();
                    bundle.removeEncounter(encounter);
                    bundleDao.merge(bundle);
                    encounterDao.remove(encounter);
                    deletedCaseNumbers.add(encounter.getCaseNumber());
                } catch (Exception e) {
                    LOGGER.error("Something went wrong while deleting an old "
                        + "finished Encounter. Since this is "
                        + "important for not having a database of "
                        + "old finished encounters, investigate " + "this error ASAP", e);
                }
            }
        }

        for (EncounterScheduled encounterScheduled : oldEncounterScheduleds) {
            try {
                encounterScheduledDao.remove(encounterScheduled);
                deletedCaseNumbers.add(encounterScheduled.getCaseNumber());
            } catch (Exception e) {
                LOGGER.error("Something went wrong while deleting an old "
                    + "EncounterScheduled. Since this is important "
                    + "for not having a database of old finished "
                    + "encounters, investigate this error ASAP", e);
            }
        }

        try {
            auditEntryDao.writeAuditEntries(this.getClass().getSimpleName(),
                "deleteOldEncounters()", deletedCaseNumbers, patientAttributes,
                AuditEntryActionType.DELETE);
        } catch (Exception e) {
            LOGGER.error("Something went wrong while writing audit logs of " + "deleted"
                + " old Encounters and old " + "EncounterScheduleds. " + "Since this is "
                + "important for having a complete " + "audit log, "
                + "investigate this error ASAP", e);
        }
    }
}
