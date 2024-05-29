package de.imi.mopat.dao.impl;

import java.util.Set;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import de.imi.mopat.dao.AuditEntryDao;
import de.imi.mopat.model.AuditEntry;
import de.imi.mopat.model.enumeration.AuditEntryActionType;
import de.imi.mopat.model.enumeration.AuditPatientAttribute;

@Component
public class AuditEntryDaoImpl implements AuditEntryDao {

    @PersistenceContext(unitName = "MoPat_Audit")
    protected EntityManager moPatAuditEntityManager;

    @Override
    @Transactional("MoPat_Audit")
    public void writeAuditEntry(final String module, final String method, final String caseNumber,
        final Set<AuditPatientAttribute> patientAttributes, final AuditEntryActionType action) {
        AuditEntry auditEntry = new AuditEntry(module, method, caseNumber, patientAttributes,
            action);
        moPatAuditEntityManager.persist(auditEntry);
    }

    @Override
    @Transactional("MoPat_Audit")
    public void writeAuditEntries(final String module, final String method,
        final Set<String> caseNumbers, final Set<AuditPatientAttribute> patientAttributes,
        final AuditEntryActionType action) {
        for (String caseNumber : caseNumbers) {
            AuditEntry auditEntry = new AuditEntry(module, method, caseNumber, patientAttributes,
                action);
            moPatAuditEntityManager.persist(auditEntry);
        }
    }

    @Override
    @Transactional("MoPat_Audit")
    public void writeAuditEntry(final String module, final String method, final String caseNumber,
        final Set<AuditPatientAttribute> patientAttributes, final AuditEntryActionType action,
        final String senderReceiver) {
        AuditEntry auditEntry = new AuditEntry(module, method, caseNumber, patientAttributes,
            action, senderReceiver);
        moPatAuditEntityManager.persist(auditEntry);
    }

    @Override
    @Transactional("MoPat_Audit")
    public void writeAuditEntries(final String module, final String method,
        final Set<String> caseNumbers, final Set<AuditPatientAttribute> patientAttributes,
        final AuditEntryActionType action, final String senderReceiver) {
        for (String caseNumber : caseNumbers) {
            AuditEntry auditEntry = new AuditEntry(module, method, caseNumber, patientAttributes,
                action, senderReceiver);
            moPatAuditEntityManager.persist(auditEntry);
        }
    }
}
