package de.imi.mopat.dao;

import de.imi.mopat.model.Bundle;
import java.sql.Timestamp;
import java.util.List;
import org.springframework.stereotype.Component;
import de.imi.mopat.model.Encounter;
import java.util.Collection;
import java.util.Date;

/**
 *
 */
@Component
public interface EncounterDao extends MoPatDao<Encounter> {

    /**
     * Returns all incompleted {@link Encounter encounters} for a given casenumber.
     *
     * @param caseNumber must not be <code>null</code>.
     * @return an unmodifiable {@link List} of {@link Encounter encounters} that have the same case
     * number as the given one and are not completed, sorted by their start date, descending. Is
     * never <code>null</code>. Might be empty.
     */
    List<Encounter> getIncompleteEncounters(String caseNumber);

    /**
     * Returns all completed {@link Encounter encounters}, which are older than the given
     * timestamp.
     *
     * @param timestamp must not be <code>null</code>.
     * @return is never <code>null</code>. Might be empty.
     */
    List<Encounter> getFinishedEncounterOlderThan(Timestamp timestamp);

    /**
     * Returns all incompleted {@link Encounter encounters}, which are older than the given
     * timestamp.
     *
     * @param timestamp must not be <code>null</code>.
     * @return is never <code>null</code>. Might be empty.
     */
    Collection<? extends Encounter> getIncompleteEncountersOlderThan(Timestamp timestamp);

    /**
     * Gets the number of incompleted {@link Encounter} objects.
     *
     * @return The number of incomplete {@link Encounter} objects.
     */
    Long getCountIncompleteEncounter();

    /**
     * Returns the number of the completed {@link Encounter encounters}, which are older than the
     * timestamp.
     *
     * @param timestamp must not be <code>null</code>.
     * @return The number of complete {@link Encounter encounters} older than the timestamp.
     */
    Long getCountCompleteEncountersOlderThan(Timestamp timestamp);

    /**
     * Returns the number of the incompleted {@link Encounter encounters}, which are older than the
     * timestamp.
     *
     * @param timestamp must not be <code>null</code>.
     * @return The number of incomplete {@link Encounter encounters} older than the timestamp.
     */
    Long getCountIncompleteEncountersOlderThan(Timestamp timestamp);

    /**
     * Returns the count of {@link Encounter encounters} concerning the given {@link Bundle}
     * answered during a given time period.
     *
     * @param bundleId  The Id of the bundle which should be taken in account.
     * @param startDate The start date of the given time period.
     * @param endDate   The end date of the given time period.
     * @return The count of {@link Encounter encounters} concerning the given {@link Bundle}
     * answered during a given time period.
     */
    Long getEncounterCountByBundleInInterval(Long bundleId, Date startDate, Date endDate);

    /**
     * Returns the count of {@link Encounter encounters} answered by a patient with the given case
     * number during a given time period.
     *
     * @param caseNumber The case number of the given patient.
     * @param startDate  The start date of the given time period.
     * @param endDate    The end date of the given time period.
     * @return The count of {@link Encounter encounters} answered by a patient with the given case
     * number during a given time period.
     */
    Long getEncounterCountByCaseNumberInInterval(String caseNumber, Date startDate, Date endDate);

    /**
     * Returns the count of {@link Encounter encounters} concerning the given {@link Bundle}
     * answered by a patient with the given case number during a given time period.
     *
     * @param bundleId   The Id of the bundle which should be taken in account.
     * @param caseNumber The case number of the given patient.
     * @param startDate  The start date of the given time period.
     * @param endDate    The end date of the given time period.
     * @return The count of {@link Encounter encounters} concerning the given {@link Bundle}
     * answered by a patient with the given case number during a given time period.
     */
    Long getEncounterCountByCaseNumberByBundleInInterval(Long bundleId, String caseNumber,
        Date startDate, Date endDate);

    /**
     * Returns all case numbers from stored {@link Encounter encounters}.
     *
     * @return All case numbers from stored {@link Encounter encounters}
     */
    List<String> getAllCaseNumbers();

    /**
     * Returns all encounters from stored {@link Encounter encounters} for a clinic.
     *
     * @return All encounter from stored {@link Encounter encounters} for a clinic
     */
    List<Encounter> getEncountersByClinicId(Long clinicId);
}
