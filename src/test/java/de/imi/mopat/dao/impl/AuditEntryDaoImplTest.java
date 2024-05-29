package de.imi.mopat.dao.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.dao.AuditEntryDao;
import de.imi.mopat.model.AuditEntry;
import de.imi.mopat.model.enumeration.AuditEntryActionType;
import de.imi.mopat.model.enumeration.AuditPatientAttribute;
import de.imi.mopat.utils.Helper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, ApplicationSecurityConfig.class,
    MvcWebApplicationInitializer.class, PersistenceConfig.class})
@TestPropertySource(locations = {"classpath:mopat-test.properties"})
@WebAppConfiguration
public class AuditEntryDaoImplTest {

    private static final Random random = new Random();
    @PersistenceContext(unitName = "MoPat_Audit")
    protected EntityManager moPatAuditEntityManager;
    @Autowired
    AuditEntryDao testAuditEntryDao;

    /**
     * Test of {@link AuditEntryDaoImpl#writeAuditEntry}.<br> Valid input: random valid
     * {@link AuditEntry} with and without senderReceiver
     */
    @Test
    public void testWriteAuditEntry() {
        String module = Helper.getRandomString(random.nextInt(50) + 1);
        String method = Helper.getRandomString(random.nextInt(50) + 1);
        String caseNumber = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);
        Set<AuditPatientAttribute> patientAttributes = new HashSet<>();
        int patientAttributesCount = random.nextInt(50) + 1;
        for (int i = 0; i < patientAttributesCount; i++) {
            patientAttributes.add(Helper.getRandomEnum(AuditPatientAttribute.class));
        }
        AuditEntryActionType action = Helper.getRandomEnum(AuditEntryActionType.class);
        String senderReceiver = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);
        AuditEntry testAuditEntry = new AuditEntry(module, method, caseNumber, patientAttributes,
            action);
        AuditEntry databaseAuditEntry;
        testAuditEntryDao.writeAuditEntry(module, method, caseNumber, patientAttributes, action);

        try {
            TypedQuery<AuditEntry> query = moPatAuditEntityManager.createQuery(
                "SELECT a FROM AuditEntry a ORDER BY a.id DESC", AuditEntry.class);
            databaseAuditEntry = query.setMaxResults(1).getSingleResult();
        } catch (NoResultException e) {
            databaseAuditEntry = null;
        }
        assertEquals("The getting module was not the expected one", testAuditEntry.getModule(),
            databaseAuditEntry.getModule());
        assertEquals("The getting method was not the expected one", testAuditEntry.getMethod(),
            databaseAuditEntry.getMethod());
        assertEquals("The getting content was not the expected one", testAuditEntry.getContent(),
            databaseAuditEntry.getContent());
        assertNull("The senderReceiver was not null", databaseAuditEntry.getSenderReceiver());

        testAuditEntry = new AuditEntry(module, method, caseNumber, patientAttributes, action,
            senderReceiver);
        testAuditEntryDao.writeAuditEntry(module, method, caseNumber, patientAttributes, action,
            senderReceiver);

        try {
            TypedQuery<AuditEntry> query = moPatAuditEntityManager.createQuery(
                "SELECT a FROM AuditEntry a ORDER BY a.id DESC", AuditEntry.class);
            databaseAuditEntry = query.setMaxResults(1).getSingleResult();
        } catch (NoResultException e) {
            databaseAuditEntry = null;
        }
        assertEquals("The getting module was not the expected one", testAuditEntry.getModule(),
            databaseAuditEntry.getModule());
        assertEquals("The getting method was not the expected one", testAuditEntry.getMethod(),
            databaseAuditEntry.getMethod());
        assertEquals("The getting content was not the expected one", testAuditEntry.getContent(),
            databaseAuditEntry.getContent());
        assertEquals("The getting senderReceiver was not the expected one",
            testAuditEntry.getSenderReceiver(), databaseAuditEntry.getSenderReceiver());

    }

    /**
     * Test of {@link AuditEntryDaoImpl#writeAuditEntries}.<br> Invalid input:  <br> Valid input:
     */
    @Test
    public void testWriteAuditEntries() {
        String module = Helper.getRandomString(random.nextInt(50) + 1);
        String method = Helper.getRandomString(random.nextInt(50) + 1);
        Set<String> caseNumbers = new HashSet<>();
        int caseNumberCount = random.nextInt(5) + 2;
        for (int i = 0; i < caseNumberCount; i++) {
            caseNumbers.add(Helper.getRandomAlphanumericString(random.nextInt(50) + 1));
        }
        Set<AuditPatientAttribute> patientAttributes = new HashSet<>();
        int patientAttributesCount = random.nextInt(50) + 1;
        for (int i = 0; i < patientAttributesCount; i++) {
            patientAttributes.add(Helper.getRandomEnum(AuditPatientAttribute.class));
        }
        AuditEntryActionType action = Helper.getRandomEnum(AuditEntryActionType.class);
        String senderReceiver = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);

        testAuditEntryDao.writeAuditEntries(module, method, caseNumbers, patientAttributes, action);
        List<AuditEntry> databaseAuditEntries;
        try {
            TypedQuery<AuditEntry> query = moPatAuditEntityManager.createQuery(
                "SELECT a FROM AuditEntry a ORDER BY a.id DESC", AuditEntry.class);
            databaseAuditEntries = query.setMaxResults(caseNumberCount).getResultList();
        } catch (NoResultException e) {
            databaseAuditEntries = null;
        }

        for (AuditEntry databaseAuditEntry : databaseAuditEntries) {
            assertEquals("The getting module was not the expected one", module,
                databaseAuditEntry.getModule());
            assertEquals("The getting method was not the expected one", method,
                databaseAuditEntry.getMethod());
            assertNull("The senderReceiver was not null", databaseAuditEntry.getSenderReceiver());
        }

        testAuditEntryDao.writeAuditEntries(module, method, caseNumbers, patientAttributes, action,
            senderReceiver);
        try {
            TypedQuery<AuditEntry> query = moPatAuditEntityManager.createQuery(
                "SELECT a FROM AuditEntry a ORDER BY a.id DESC", AuditEntry.class);
            databaseAuditEntries = query.setMaxResults(caseNumberCount).getResultList();
        } catch (NoResultException e) {
            databaseAuditEntries = null;
        }

        for (AuditEntry databaseAuditEntry : databaseAuditEntries) {
            assertEquals("The getting module was not the expected one", module,
                databaseAuditEntry.getModule());
            assertEquals("The getting method was not the expected one", method,
                databaseAuditEntry.getMethod());
            assertEquals("The getting senderReceiver was not the expected one", senderReceiver,
                databaseAuditEntry.getSenderReceiver());
        }
    }
}
