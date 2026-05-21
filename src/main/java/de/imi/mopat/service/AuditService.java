package de.imi.mopat.service;

import de.imi.mopat.dao.AuditEntryDao;
import de.imi.mopat.model.EncounterScheduled;
import de.imi.mopat.model.enumeration.AuditEntryActionType;
import de.imi.mopat.model.enumeration.AuditPatientAttribute;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.Set;

@Service
public class AuditService {

    private final AuditEntryDao auditEntryDao;

    public AuditService(AuditEntryDao auditEntryDao) {
        this.auditEntryDao = auditEntryDao;
    }

    public void writeScheduledEncounterAudit(
            Class<?> sourceClass,
            String methodName,
            EncounterScheduled scheduled
    ) {

        Set<AuditPatientAttribute> patientAttributes =
                EnumSet.of(
                        AuditPatientAttribute.CASE_NUMBER,
                        AuditPatientAttribute.EMAIL_ADDRESS,
                        AuditPatientAttribute.FIRST_NAME,
                        AuditPatientAttribute.LAST_NAME,
                        AuditPatientAttribute.DATE_OF_BIRTH
                );

        auditEntryDao.writeAuditEntry(
                sourceClass.getSimpleName(),
                methodName,
                scheduled.getCaseNumber(),
                patientAttributes,
                AuditEntryActionType.WRITE
        );
    }
}