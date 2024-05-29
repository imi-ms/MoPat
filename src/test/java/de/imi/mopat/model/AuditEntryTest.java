package de.imi.mopat.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.model.enumeration.AuditEntryActionType;
import de.imi.mopat.model.enumeration.AuditPatientAttribute;
import de.imi.mopat.utils.Helper;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
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
public class AuditEntryTest {

    private static final Random random = new Random();
    AuditEntryActionType action;
    AuditEntry testAuditEntry;
    private String module, method, caseNumber, senderReceiver;
    private Set<AuditPatientAttribute> patientAttributes;

    public AuditEntryTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Returns a valid new AuditEntry
     *
     * @return Returns a valid new AuditEntry
     */
    public static AuditEntry getNewValidAuditEntry() {
        String module = Helper.getRandomString(random.nextInt(50) + 1);
        String method = Helper.getRandomString(random.nextInt(50) + 1);
        String caseNumber = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);
        Set<AuditPatientAttribute> patientAttributes = new HashSet<>();
        int patientAttributesCount = random.nextInt(50) + 1;
        for (int i = 0; i < patientAttributesCount; i++) {
            patientAttributes.add(Helper.getRandomEnum(AuditPatientAttribute.class));
        }
        AuditEntryActionType action = Helper.getRandomEnum(AuditEntryActionType.class);

        AuditEntry auditEntry = new AuditEntry(module, method, caseNumber, patientAttributes,
            action);

        return auditEntry;
    }

    @Before
    public void setUp() {
        module = Helper.getRandomString(random.nextInt(50) + 1);
        method = Helper.getRandomString(random.nextInt(50) + 1);
        caseNumber = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);
        patientAttributes = new HashSet<>();
        int patientAttributesCount = random.nextInt(50) + 1;
        for (int i = 0; i < patientAttributesCount; i++) {
            patientAttributes.add(Helper.getRandomEnum(AuditPatientAttribute.class));
        }
        action = Helper.getRandomEnum(AuditEntryActionType.class);
        senderReceiver = Helper.getRandomString(random.nextInt(50) + 1);

        testAuditEntry = new AuditEntry(module, method, caseNumber, patientAttributes, action,
            senderReceiver);
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of {@link AuditEntry#AuditEntry}.<br> Invalid input: caseNumber <code>null</code>,
     * caseNumber empty String, patientAttributes <code>null</code>, patientAttributes empty set and
     * action <code>null</code>
     */
    @Test
    public void testConstructor() {
        module = Helper.getRandomString(random.nextInt(50) + 1);
        method = Helper.getRandomString(random.nextInt(50) + 1);
        caseNumber = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);
        patientAttributes = new HashSet<>();
        int patientAttributesCount = random.nextInt(50) + 1;
        for (int i = 0; i < patientAttributesCount; i++) {
            patientAttributes.add(Helper.getRandomEnum(AuditPatientAttribute.class));
        }
        action = Helper.getRandomEnum(AuditEntryActionType.class);
        senderReceiver = Helper.getRandomString(random.nextInt(50) + 1);

        Throwable e = null;
        try {
            testAuditEntry = new AuditEntry(module, method, null, patientAttributes, action);

        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the caseNumber", e instanceof AssertionError);

        e = null;
        try {
            testAuditEntry = new AuditEntry(module, method, "", patientAttributes, action);

        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set an empty String as the caseNumber",
            e instanceof AssertionError);

        e = null;
        try {
            testAuditEntry = new AuditEntry(module, method, caseNumber, null, action);

        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the patientAttributes",
            e instanceof AssertionError);

        e = null;
        try {
            testAuditEntry = new AuditEntry(module, method, caseNumber, new HashSet<>(), action);

        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set an empty set as the patientAttributes",
            e instanceof AssertionError);

        e = null;
        try {
            testAuditEntry = new AuditEntry(module, method, caseNumber, patientAttributes, null);

        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the action", e instanceof AssertionError);

        action = AuditEntryActionType.SENT;
        e = null;
        try {
            testAuditEntry = new AuditEntry(module, method, caseNumber, patientAttributes, action,
                null);

        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the senderReceiver",
            e instanceof AssertionError);

        e = null;
        try {
            testAuditEntry = new AuditEntry(module, method, caseNumber, patientAttributes, action,
                "");

        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set an empty String as the senderReceiver",
            e instanceof AssertionError);
    }

    /**
     * Test of {@link AuditEntry#AuditEntry}.<br> Valid input: User with name "admin"
     */
    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "MoPatUserDetailsService")
    public void testConstructorWithSeleniumAdmin() {
        module = Helper.getRandomString(random.nextInt(50) + 1);
        method = Helper.getRandomString(random.nextInt(50) + 1);
        caseNumber = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);
        patientAttributes = new HashSet<>();
        int patientAttributesCount = random.nextInt(50) + 1;
        for (int i = 0; i < patientAttributesCount; i++) {
            patientAttributes.add(Helper.getRandomEnum(AuditPatientAttribute.class));
        }
        action = Helper.getRandomEnum(AuditEntryActionType.class);
        senderReceiver = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);

        testAuditEntry = new AuditEntry(module, method, caseNumber, patientAttributes, action);
        assertEquals("The UserId of the admin was not 1", 1,
            testAuditEntry.getUserId().longValue());
    }

    /**
     * Test of {@link AuditEntry#AuditEntry}.<br> Principal null is not tested, because it can never
     * be null in SpringSecurity.<br> Valid input: Principal which is not an instance of User
     */
    @Test
    @WithMockUser
    public void testConstructorWithUser() {
        module = Helper.getRandomString(random.nextInt(50) + 1);
        method = Helper.getRandomString(random.nextInt(50) + 1);
        caseNumber = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);
        patientAttributes = new HashSet<>();
        int patientAttributesCount = random.nextInt(50) + 1;
        for (int i = 0; i < patientAttributesCount; i++) {
            patientAttributes.add(Helper.getRandomEnum(AuditPatientAttribute.class));
        }
        action = Helper.getRandomEnum(AuditEntryActionType.class);
        senderReceiver = Helper.getRandomString(random.nextInt(50) + 1);

        testAuditEntry = new AuditEntry(module, method, caseNumber, patientAttributes, action);
        assertEquals("The UserId of an not existing user was not -1", -1,
            testAuditEntry.getUserId().longValue());
    }

    /**
     * Test of {@link AuditEntry#toString}.<br>
     */
    @Test
    public void testToString() {
        AuditEntry spyAuditEntry = spy(testAuditEntry);
        Mockito.when(spyAuditEntry.getId()).thenReturn(Math.abs(random.nextLong()));
        Mockito.when(spyAuditEntry.getUserId()).thenReturn(Math.abs(random.nextLong()));
        String randomContent = Helper.getRandomString(random.nextInt(500) + 1);
        Mockito.when(spyAuditEntry.getContent()).thenReturn(randomContent);
        Mockito.when(spyAuditEntry.getLogTime())
            .thenReturn(new Timestamp(Math.abs(random.nextLong())));
        String testString =
            "id: " + spyAuditEntry.getId() + ", userId: " + spyAuditEntry.getUserId() + ", module: "
                + this.module + ", method: " + this.method + ", content: " + randomContent
                + ", sender/receiver: " + this.senderReceiver + ", logTime: "
                + spyAuditEntry.getLogTime();
        assertEquals("The getting String was not the expected one", testString,
            spyAuditEntry.toString());
    }

    /**
     * Test of {@link AuditEntry#getAction}.<br>
     */
    @Test
    public void testGetAction() {
        assertEquals("The getting action was not the expected one", action,
            testAuditEntry.getAction());
    }

    /**
     * Test of {@link AuditEntry#getSenderReceiver}.<br>
     */
    @Test
    public void testGetSenderReceiver() {
        assertEquals("The getting senderReceiver was not the expected one", senderReceiver,
            testAuditEntry.getSenderReceiver());
    }
}
