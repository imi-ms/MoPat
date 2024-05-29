package de.imi.mopat.dao.impl;

import de.imi.mopat.dao.AuditEntryDao;
import org.springframework.stereotype.Component;
import de.imi.mopat.dao.EncounterScheduledDao;
import de.imi.mopat.model.Encounter;
import de.imi.mopat.model.EncounterScheduled;
import de.imi.mopat.model.enumeration.AuditEntryActionType;
import de.imi.mopat.model.enumeration.AuditPatientAttribute;
import org.springframework.transaction.annotation.Transactional;
import de.imi.mopat.model.enumeration.EncounterScheduledSerialType;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 */
@Component
public class EncounterScheduledDaoImpl extends MoPatDaoImpl<EncounterScheduled> implements
    EncounterScheduledDao {

    @Autowired
    private AuditEntryDao auditEntryDao;

    @Override
    @Transactional("MoPat")
    public List<EncounterScheduled> getAllElements() {
        TypedQuery<EncounterScheduled> query = moPatEntityManager.createQuery(
            "SELECT e FROM " + getEntityClass().getSimpleName() + " e", getEntityClass());
        List<EncounterScheduled> elements = query.getResultList();
        return elements;
    }

    @Override
    public List<EncounterScheduled> getEncounterScheduledByDate(final Date date) {
        TypedQuery<EncounterScheduled> query = moPatEntityManager.createQuery(
            "SELECT e FROM EncounterScheduled e WHERE (e"
                + ".encounterScheduledSerialType !=  :uniquely AND "
                + ":date >= e.startDate AND :date <= e.endDate) OR (e"
                + ".encounterScheduledSerialType = :uniquely AND e" + ".startDate = :date)",
            EncounterScheduled.class);
        query.setParameter("date", date);
        query.setParameter("uniquely", EncounterScheduledSerialType.UNIQUELY);
        return query.getResultList();
    }

    @Override
    public List<EncounterScheduled> getPastEncounterScheduled() {
        Date today = new Date();
        //If encounterScheduled is not of encounterSerialType uniquely,
        // there's an endDate defined and
        //you can compare it the current timestamp, otherwise you need the
        // startDate for comparison
        //because there's no endDate
        TypedQuery<EncounterScheduled> query = moPatEntityManager.createQuery(
            "SELECT e FROM EncounterScheduled e WHERE (e"
                + ".encounterScheduledSerialType !=  :uniquely AND " + ":today >= e.endDate) OR (e"
                + ".encounterScheduledSerialType = :uniquely AND " + ":today" + " >= e.startDate)",
            EncounterScheduled.class);
        query.setParameter("today", today);
        query.setParameter("uniquely", EncounterScheduledSerialType.UNIQUELY);
        return query.getResultList();
    }

    @Override
    public List<EncounterScheduled> getFinishedEncounterScheduledOlderThan(
        final Timestamp timestamp) {
        assert timestamp != null : "The given date was null";
        List<EncounterScheduled> result = new ArrayList<>();
        List<EncounterScheduled> resultList = getEncounterScheduledOlderThan(timestamp);
        result.addAll(resultList);
        //Walk through resultList and remove all encounterScheduleds that are
        // not finished
        for (EncounterScheduled encounterScheduled : result) {
            Boolean containsIncompleteEncounters = false;
            for (Encounter encounter : encounterScheduled.getEncounters()) {
                if (encounter.getEndTime() == null) {
                    containsIncompleteEncounters = true;
                    break;
                }
            }

            if (containsIncompleteEncounters) {
                resultList.remove(encounterScheduled);
            }
        }

        return resultList;
    }

    @Override
    public List<EncounterScheduled> getEncounterScheduledOlderThan(final Timestamp timestamp) {
        assert timestamp != null : "The given timestamp was null";
        //If encounterScheduled is not of encounterSerialType uniquely,
        // there's an endDate defined and
        //you can compare it the current timestamp, otherwise you need the
        // startDate for comparison
        //because there's no endDate
        TypedQuery<EncounterScheduled> query = moPatEntityManager.createQuery(
            "SELECT e FROM EncounterScheduled e WHERE e.endDate < "
                + ":timestamp OR e.startDate < :timestamp AND e"
                + ".encounterScheduledSerialType = :uniquely", EncounterScheduled.class);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(timestamp);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        query.setParameter("timestamp", calendar.getTime());
        query.setParameter("uniquely", EncounterScheduledSerialType.UNIQUELY);
        List<EncounterScheduled> resultList = query.getResultList();

        if (!resultList.isEmpty()) {
            Set<AuditPatientAttribute> patientAttributes = new HashSet<AuditPatientAttribute>();
            patientAttributes.add(AuditPatientAttribute.CASE_NUMBER);
            patientAttributes.add(AuditPatientAttribute.TREATMENT_DATA);
            Set<String> caseNumbers = new HashSet<>();
            for (EncounterScheduled encounterScheduled : resultList) {
                caseNumbers.add(encounterScheduled.getCaseNumber());
                for (Encounter encounter : encounterScheduled.getEncounters()) {
                    caseNumbers.add(encounter.getCaseNumber());
                }
            }
            auditEntryDao.writeAuditEntries(this.getClass().getSimpleName(),
                "getIncompleteEncountersOlderThan(Date)", caseNumbers, patientAttributes,
                AuditEntryActionType.READ);
        }

        return resultList;
    }
}
