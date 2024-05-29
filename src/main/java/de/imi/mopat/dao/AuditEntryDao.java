package de.imi.mopat.dao;

import de.imi.mopat.model.enumeration.AuditEntryActionType;
import de.imi.mopat.model.enumeration.AuditPatientAttribute;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public interface AuditEntryDao {

    void writeAuditEntry(String module, String method, String caseNumber,
        Set<AuditPatientAttribute> patientAttributes, AuditEntryActionType action);

    void writeAuditEntries(String module, String method, Set<String> caseNumbers,
        Set<AuditPatientAttribute> patientAttributes, AuditEntryActionType action);

    void writeAuditEntry(String module, String method, String caseNumber,
        Set<AuditPatientAttribute> patientAttributes, AuditEntryActionType action,
        String senderReceiver);

    void writeAuditEntries(String module, String method, Set<String> caseNumbers,
        Set<AuditPatientAttribute> patientAttributes, AuditEntryActionType action,
        String senderReceiver);
}