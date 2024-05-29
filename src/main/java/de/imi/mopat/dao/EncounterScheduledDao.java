package de.imi.mopat.dao;

import org.springframework.stereotype.Component;
import de.imi.mopat.model.EncounterScheduled;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

/**
 *
 */
@Component

public interface EncounterScheduledDao extends MoPatDao<EncounterScheduled> {

    /**
     * Returns all elements of Type {@link EncounterScheduled} in the database.
     *
     * @return All elements of type {@link EncounterScheduled} within the database.
     */

    // TODO Once use of Encountermanagers is readded to the application, the filter has to be applied again.
    //@PostFilter("hasRole('ROLE_ENCOUNTERMANAGER') OR hasPermission(filterObject, 'READ')")
    @Override
    List<EncounterScheduled> getAllElements();

    /**
     * Returns all {@link EncounterScheduled} where the given date is in the time period for
     * scheduling a new encounter.
     *
     * @param date The date for which the {@link EncounterScheduled} should be searched.
     * @return All {@link EncounterScheduled} where the given date is in the time period for
     * scheduling a new encounter. Is never <code>null</code>. Might be empty.
     */
    List<EncounterScheduled> getEncounterScheduledByDate(Date date);

    /**
     * Returns all {@link EncounterScheduled} where the end date is in the past or if it is an
     * uniquely {@link EncounterScheduled} where the start date is in the past.
     *
     * @return All {@link EncounterScheduled} where the end date is in the past or if it is an
     * uniquely {@link EncounterScheduled} where the start date is in the past.
     */
    List<EncounterScheduled> getPastEncounterScheduled();

    /**
     * Returns all completed {@link EncounterScheduled}, which are older than the given timestamp.
     *
     * @param timestamp must not be <code>null</code>.
     * @return All encounterScheduleds that are finished and older than the given timestamp.
     */
    List<EncounterScheduled> getFinishedEncounterScheduledOlderThan(Timestamp timestamp);

    /**
     * Returns all incompleted {@link EncounterScheduled}, which are older than the given
     * timestamp.
     *
     * @param timestamp must not be <code>null</code>.
     * @return All encounterScheduleds that contains incomplete encounters and that are older than
     * the given timestamp.
     */
    List<EncounterScheduled> getEncounterScheduledOlderThan(Timestamp timestamp);
}
