package de.imi.mopat.dao.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import de.imi.mopat.dao.AuditEntryDao;
import de.imi.mopat.dao.EncounterDao;
import de.imi.mopat.model.enumeration.AuditEntryActionType;
import de.imi.mopat.model.enumeration.AuditPatientAttribute;
import de.imi.mopat.model.Encounter;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import jakarta.persistence.PersistenceException;

/**
 *
 */
@Component
public class EncounterDaoImpl extends MoPatDaoImpl<Encounter> implements EncounterDao {

    @Autowired
    private AuditEntryDao auditEntryDao;

    @Override
    public List<Encounter> getIncompleteEncounters(final String caseNumber) {
        assert caseNumber != null : "The given case number was null";
        List<Encounter> result;
        List<Encounter> resultList;
        try {
            TypedQuery<Encounter> query = moPatEntityManager.createQuery(
                "SELECT e FROM Encounter e WHERE e.caseNumber = " + ":casenumber "
                    + "and e.endTime is null ORDER BY " + "e.startTime DESC", getEntityClass());
            query.setParameter("casenumber", caseNumber);
            resultList = query.getResultList();

        } catch (PersistenceException e) {
            resultList = new ArrayList<>();
        }

        result = Collections.unmodifiableList(resultList);
        return result;
    }

    @Override
    public List<Encounter> getFinishedEncounterOlderThan(final Timestamp timestamp) {
        assert timestamp != null : "The given timestamp was null";
        List<Encounter> result;
        List<Encounter> resultList;

        TypedQuery<Encounter> query = moPatEntityManager.createQuery(
            "SELECT e FROM Encounter e WHERE e.endTime IS NOT NULL AND e" + ".endTime < :timestamp",
            Encounter.class);
        query.setParameter("timestamp", timestamp);
        resultList = query.getResultList();
        if (!resultList.isEmpty()) {
            Set<AuditPatientAttribute> patientAttributes = new HashSet<AuditPatientAttribute>();
            patientAttributes.add(AuditPatientAttribute.CASE_NUMBER);
            patientAttributes.add(AuditPatientAttribute.TREATMENT_DATA);
            Set<String> caseNumbers = new HashSet<>();
            for (Encounter encounter : resultList) {
                caseNumbers.add(encounter.getCaseNumber());
            }
            auditEntryDao.writeAuditEntries(this.getClass().getSimpleName(),
                "getFinishedEncounterOlderThan(Date)", caseNumbers, patientAttributes,
                AuditEntryActionType.READ);
        }
        result = Collections.unmodifiableList(resultList);

        return result;
    }

    @Override
    public Collection<? extends Encounter> getIncompleteEncountersOlderThan(
        final Timestamp timestamp) {
        assert timestamp != null : "The given timestamp was null";
        List<Encounter> result;
        List<Encounter> resultList;

        TypedQuery<Encounter> query = moPatEntityManager.createQuery(
            "SELECT e FROM Encounter e WHERE e.endTime IS NULL AND e" + ".startTime < :timestamp",
            Encounter.class);
        query.setParameter("timestamp", timestamp);
        resultList = query.getResultList();
        if (!resultList.isEmpty()) {
            Set<AuditPatientAttribute> patientAttributes = new HashSet<AuditPatientAttribute>();
            patientAttributes.add(AuditPatientAttribute.CASE_NUMBER);
            patientAttributes.add(AuditPatientAttribute.TREATMENT_DATA);
            Set<String> caseNumbers = new HashSet<>();
            for (Encounter encounter : resultList) {
                caseNumbers.add(encounter.getCaseNumber());
            }
            auditEntryDao.writeAuditEntries(this.getClass().getSimpleName(),
                "getIncompleteEncountersOlderThan(Date)", caseNumbers, patientAttributes,
                AuditEntryActionType.READ);
        }
        result = Collections.unmodifiableList(resultList);

        return result;
    }

    @Override
    public Long getCountIncompleteEncounter() {
        TypedQuery<Long> query = moPatEntityManager.createQuery(
            "SELECT count(e) FROM Encounter e WHERE e.endTime IS NULL", Long.class);
        long count = query.getSingleResult();
        return count;
    }

    @Override
    public Long getCountCompleteEncountersOlderThan(final Timestamp timestamp) {
        TypedQuery<Long> query = moPatEntityManager.createQuery(
            "SELECT count(e) FROM Encounter e WHERE  e.endTime IS NOT "
                + "NULL AND e.endTime < :timestamp", Long.class);
        query.setParameter("timestamp", timestamp);
        Long count = query.getSingleResult();
        return count;
    }

    @Override
    public Long getCountIncompleteEncountersOlderThan(final Timestamp timestamp) {
        TypedQuery<Long> query = moPatEntityManager.createQuery(
            "SELECT count(e) FROM Encounter e WHERE e.endTime IS NULL " + "AND"
                + " e.startTime < :timestamp", Long.class);
        query.setParameter("timestamp", timestamp);
        Long count = query.getSingleResult();
        return count;
    }

    @Override
    public List<String> getAllCaseNumbers() {
        TypedQuery<String> query = moPatEntityManager.createQuery(
            "SELECT distinct e.caseNumber FROM Encounter e order by e" + ".caseNumber",
            String.class);

        return query.getResultList();
    }

    @Override
    public List<Encounter> getEncountersByClinicId(Long clinicId){
        TypedQuery<Encounter> query = moPatEntityManager.createQuery(
            "SELECT e FROM Encounter e WHERE e.clinic.id=" + clinicId,
            Encounter.class);

        return query.getResultList();
    }

    @Override
    public Long getEncounterCountByBundleInInterval(final Long bundleId, final Date startDate,
        Date endDate) {
        TypedQuery<Long> query = moPatEntityManager.createQuery(
            "select count(e) from Encounter e, Bundle b where e.bundle" + ".id"
                + " = b.id and b.id = :bundleId and ((e.endTime" + " between"
                + " :startDate and :endDate ) or (e" + ".endTime is null and"
                + " e.startTime between " + ":startDate and :endDate) )", Long.class);
        query.setParameter("bundleId", bundleId);
        query.setParameter("startDate", startDate);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(endDate);
        calendar.add(Calendar.DAY_OF_YEAR, +1);
        endDate = calendar.getTime();
        query.setParameter("endDate", endDate);
        return query.getSingleResult();
    }

    @Override
    public Long getEncounterCountByCaseNumberInInterval(final String caseNumber,
        final Date startDate, Date endDate) {
        TypedQuery<Long> query = moPatEntityManager.createQuery(
            "select count(e) from Encounter e where e.caseNumber = "
                + ":caseNumber and ((e.endTime between :startDate and "
                + ":endDate ) or (e.endTime is null and e.startTime "
                + "between :startDate and :endDate) )", Long.class);

        query.setParameter("caseNumber", caseNumber);
        query.setParameter("startDate", startDate);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(endDate);
        calendar.add(Calendar.DAY_OF_YEAR, +1);
        endDate = calendar.getTime();
        query.setParameter("endDate", endDate);
        return query.getSingleResult();
    }

    @Override
    public Long getEncounterCountByCaseNumberByBundleInInterval(final Long bundleId,
        final String caseNumber, final Date startDate, Date endDate) {
        TypedQuery<Long> query = moPatEntityManager.createQuery(
            "select count(e) from Encounter e where e.bundle.id = "
                + ":bundleId and e.caseNumber = :caseNumber and ((e"
                + ".endTime between :startDate and :endDate ) or (e"
                + ".endTime is null and e.startTime between :startDate " + "and :endDate) )",
            Long.class);
        query.setParameter("bundleId", bundleId);
        query.setParameter("caseNumber", caseNumber);
        query.setParameter("startDate", startDate);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(endDate);
        calendar.add(Calendar.DAY_OF_YEAR, +1);
        endDate = calendar.getTime();
        query.setParameter("endDate", endDate);
        return query.getSingleResult();
    }
}
