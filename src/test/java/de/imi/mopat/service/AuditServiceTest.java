package de.imi.mopat.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.imi.mopat.dao.AuditEntryDao;
import de.imi.mopat.model.EncounterScheduled;
import de.imi.mopat.model.enumeration.AuditEntryActionType;
import de.imi.mopat.model.enumeration.AuditPatientAttribute;
import java.util.EnumSet;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AuditServiceTest {

    @Mock
    private AuditEntryDao auditEntryDao;

    @InjectMocks
    private AuditService auditService;

    @Mock
    private EncounterScheduled encounterScheduled;

    private static final Set<AuditPatientAttribute> EXPECTED_ATTRIBUTES = EnumSet.of(
        AuditPatientAttribute.CASE_NUMBER,
        AuditPatientAttribute.EMAIL_ADDRESS,
        AuditPatientAttribute.FIRST_NAME,
        AuditPatientAttribute.LAST_NAME,
        AuditPatientAttribute.DATE_OF_BIRTH
    );

    /**
     * Test of {@link AuditService#writeScheduledEncounterAudit(Class, String, EncounterScheduled)}
     */

    @Test
    public void testWriteScheduledEncounterAuditWritesCorrectAuditEntry() {
        String caseNumber = "TEST-CASE-123";
        when(encounterScheduled.getCaseNumber()).thenReturn(caseNumber);

        auditService.writeScheduledEncounterAudit(
            EncounterScheduledService.class, "save", encounterScheduled);

        verify(auditEntryDao).writeAuditEntry(
            EncounterScheduledService.class.getSimpleName(),
            "save",
            caseNumber,
            EXPECTED_ATTRIBUTES,
            AuditEntryActionType.WRITE
        );
    }

    @Test
    public void testWriteScheduledEncounterAuditUsesSimpleNameOfSourceClass() {
        String caseNumber = "CASE-456";
        when(encounterScheduled.getCaseNumber()).thenReturn(caseNumber);

        auditService.writeScheduledEncounterAudit(
            AuditService.class, "testMethod", encounterScheduled);

        verify(auditEntryDao).writeAuditEntry(
            "AuditService",
            "testMethod",
            caseNumber,
            EXPECTED_ATTRIBUTES,
            AuditEntryActionType.WRITE
        );
    }

    @Test
    public void testWriteScheduledEncounterAuditAlwaysUsesActionTypeWrite() {
        when(encounterScheduled.getCaseNumber()).thenReturn("ANY");

        auditService.writeScheduledEncounterAudit(
            AuditService.class, "anyMethod", encounterScheduled);

        verify(auditEntryDao).writeAuditEntry(
            any(),
            any(),
            any(),
            any(),
            eq(AuditEntryActionType.WRITE)
        );
    }
}