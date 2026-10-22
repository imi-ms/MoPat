package de.imi.mopat.cron;

import de.imi.mopat.dao.AuditEntryDao;
import de.imi.mopat.dao.BundleDao;
import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.dao.EncounterDao;
import de.imi.mopat.dao.EncounterScheduledDao;
import de.imi.mopat.helper.controller.Constants;
import de.imi.mopat.model.Bundle;
import de.imi.mopat.model.Encounter;
import de.imi.mopat.model.EncounterScheduled;
import de.imi.mopat.model.enumeration.AuditEntryActionType;
import de.imi.mopat.model.enumeration.AuditPatientAttribute;
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
        List<Encounter> oldEncounters = collectOldEncounters();
        List<EncounterScheduled> oldEncounterScheduleds = collectOldEncounterScheduleds();

        Set<String> deletedCaseNumbers = new HashSet<>();
        deleteEncounters(oldEncounters, deletedCaseNumbers);
        deleteEncounterScheduleds(oldEncounterScheduleds, deletedCaseNumbers);

        writeAuditLog(deletedCaseNumbers);
    }

    /**
     * Resolves a configured time-window value, falling back to a default if not set,
     * or returning null if the window is explicitly disabled (-1).
     */
    private Long resolveTimeWindow(Long configuredValue, long defaultValue, String propertyName) {
        if (configuredValue == null) {
            LOGGER.info("Could not find a value for the property {}; will take the default ({} ms) instead",
                propertyName, defaultValue);
            return defaultValue;
        }
        if (configuredValue == -1) {
            return null; // disabled
        }
        return configuredValue;
    }

    private Timestamp nowMinus(long millis) {
        return new Timestamp(System.currentTimeMillis() - millis);
    }

    private List<Encounter> collectOldEncounters() {
        List<Encounter> oldEncounters = new ArrayList<>();

        Long finishedWindow = resolveTimeWindow(
            configurationDao.getFinishedEncounterTimeWindow(),
            THIRTY_DAYS_IN_MILLISECONDS,
            Constants.FINISHED_ENCOUNTER_TIME_WINDOW_IN_MILLIS);

        Long incompleteWindow = resolveTimeWindow(
            configurationDao.getIncompleteEncounterTimeWindow(),
            ONEHUNDREDEIGHTY_DAYS_IN_MILLISECONDS,
            Constants.INCOMPLETE_ENCOUNTER_TIME_WINDOW_IN_MILLIS);

        try {
            if (finishedWindow != null) {
                oldEncounters.addAll(encounterDao.getFinishedEncounterOlderThan(nowMinus(finishedWindow)));
            }
            if (incompleteWindow != null) {
                oldEncounters.addAll(encounterDao.getIncompleteEncountersOlderThan(nowMinus(incompleteWindow)));
            }
        } catch (Exception e) {
            LOGGER.error("Something went wrong while checking for old finished Encounters. "
                + "Since this is important for not having a database of old finished encounters, "
                + "investigate this error ASAP", e);
        }
        return oldEncounters;
    }

    private List<EncounterScheduled> collectOldEncounterScheduleds() {
        List<EncounterScheduled> oldEncounterScheduleds = new ArrayList<>();

        Long finishedWindow = resolveTimeWindow(
            configurationDao.getFinishedEncounterScheduledTimeWindow(),
            NINETY_DAYS_IN_MILLISECONDS,
            Constants.FINISHED_ENCOUNTER_SCHEDULED_TIME_WINDOW_IN_MILLIS);

        Long incompleteWindow = resolveTimeWindow(
            configurationDao.getIncompleteEncounterScheduledTimeWindow(),
            ONEHUNDREDEIGHTY_DAYS_IN_MILLISECONDS,
            Constants.INCOMPLETE_ENCOUNTER_SCHEDULED_TIME_WINDOW_IN_MILLIS);

        try {
            if (finishedWindow != null) {
                oldEncounterScheduleds.addAll(
                    encounterScheduledDao.getFinishedEncounterScheduledOlderThan(nowMinus(finishedWindow)));
            }
            if (incompleteWindow != null) {
                for (EncounterScheduled es : encounterScheduledDao.getEncounterScheduledOlderThan(nowMinus(incompleteWindow))) {
                    if (!oldEncounterScheduleds.contains(es)) {
                        oldEncounterScheduleds.add(es);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Something went wrong while checking for old finished EncounterScheduleds. "
                + "Since this is important for not having a database of old finished encounters, "
                + "investigate this error ASAP", e);
        }
        return oldEncounterScheduleds;
    }

    private void deleteEncounters(List<Encounter> oldEncounters, Set<String> deletedCaseNumbers) {
        for (Encounter encounter : oldEncounters) {
            // Do not remove encounters that belong to scheduled encounters
            if (encounter.getEncounterScheduled() != null) {
                continue;
            }
            try {
                Bundle bundle = encounter.getBundle();
                bundle.removeEncounter(encounter);
                bundleDao.merge(bundle);
                encounterDao.remove(encounter);
                deletedCaseNumbers.add(encounter.getCaseNumber());
            } catch (Exception e) {
                LOGGER.error("Something went wrong while deleting an old finished Encounter. "
                    + "Since this is important for not having a database of old finished encounters, "
                    + "investigate this error ASAP", e);
            }
        }
    }

    private void deleteEncounterScheduleds(List<EncounterScheduled> oldEncounterScheduleds, Set<String> deletedCaseNumbers) {
        for (EncounterScheduled encounterScheduled : oldEncounterScheduleds) {
            try {
                for(Encounter nestedEncounter: encounterScheduled.getEncounters()) {
                    encounterDao.removeEncounterExportTemplatesForEncounter(nestedEncounter);
                }

                encounterScheduledDao.remove(encounterScheduled);
                deletedCaseNumbers.add(encounterScheduled.getCaseNumber());
            } catch (Exception e) {
                LOGGER.error("Something went wrong while deleting an old EncounterScheduled. "
                    + "Since this is important for not having a database of old finished encounters, "
                    + "investigate this error ASAP", e);
            }
        }
    }

    private void writeAuditLog(Set<String> deletedCaseNumbers) {
        Set<AuditPatientAttribute> patientAttributes = new HashSet<>(Arrays.asList(AuditPatientAttribute.values()));
        try {
            auditEntryDao.writeAuditEntries(this.getClass().getSimpleName(),
                "deleteOldEncounters()", deletedCaseNumbers, patientAttributes,
                AuditEntryActionType.DELETE);
        } catch (Exception e) {
            LOGGER.error("Something went wrong while writing audit logs of deleted old Encounters "
                + "and old EncounterScheduleds. Since this is important for having a complete "
                + "audit log, investigate this error ASAP", e);
        }
    }

}
