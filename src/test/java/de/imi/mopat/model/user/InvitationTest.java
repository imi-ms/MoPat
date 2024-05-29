package de.imi.mopat.model.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.helper.controller.ApplicationMailer;
import de.imi.mopat.model.dto.InvitationDTO;
import de.imi.mopat.utils.Helper;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
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
public class InvitationTest {

    private static final Random random = new Random();
    @Autowired
    ApplicationMailer applicationMailer;
    @Autowired
    @Qualifier("messageSource")
    MessageSource messageSource;
    private Invitation testInvitation;

    public InvitationTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    public static Invitation getNewValidInvitation() {
        return new Invitation(Helper.getRandomMailAddress(), UserTest.getNewValidUser());
    }

    @Before
    public void setUp() {
        testInvitation = getNewValidInvitation();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of {@link Invitation#getFirstName} and {@link Invitation#setFirstName}.<br> Invalid
     * input: <code>null</code>, less than 1 or more than 255 characters after trimming<br> Valid
     * input: random String with 1 to 255 characters after trimming
     */
    @Test
    public void testGetAndSetFirstName() {
        String testFirstName = null;
        Throwable e = null;
        try {
            testInvitation.setFirstName(testFirstName);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the FirstName", e instanceof AssertionError);

        testFirstName = "    ";
        e = null;
        try {
            testInvitation.setFirstName(testFirstName);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set a FirstName with less than 1 character",
            e instanceof AssertionError);

        testFirstName = Helper.getRandomAlphabeticString(random.nextInt(50) + 256);
        e = null;
        try {
            testInvitation.setFirstName(testFirstName);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set a FirstName with more than 255 characters",
            e instanceof AssertionError);

        testFirstName = Helper.getRandomAlphabeticString(random.nextInt(255) + 1);
        testInvitation.setFirstName(testFirstName);
        assertEquals("The getting FirstName was not the expected one", testFirstName,
            testInvitation.getFirstName());
    }

    /**
     * Test of {@link Invitation#getLastName} and {@link Invitation#setLastName}.<br> Invalid
     * input:
     * <code>null</code>, less than 1 or more than 255 characters after trimming<br> Valid input:
     * random String with 1 to 255 characters after trimming
     */
    @Test
    public void testGetAndSetLastName() {
        String testLastName = null;
        Throwable e = null;
        try {
            testInvitation.setLastName(testLastName);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the LastName", e instanceof AssertionError);

        testLastName = "    ";
        e = null;
        try {
            testInvitation.setLastName(testLastName);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set a LastName with less than 1 character",
            e instanceof AssertionError);

        testLastName = Helper.getRandomAlphabeticString(random.nextInt(50) + 256);
        e = null;
        try {
            testInvitation.setLastName(testLastName);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set a LastName with more than 255 characters",
            e instanceof AssertionError);

        testLastName = Helper.getRandomAlphabeticString(random.nextInt(255) + 1);
        testInvitation.setLastName(testLastName);
        assertEquals("The getting LastName was not the expected one", testLastName,
            testInvitation.getLastName());
    }

    /**
     * Test of {@link Invitation#getEmail} and {@link Invitation#setEmail}.<br> Invalid input:
     * <code>null</code>, mail address that does not match the pattern<br> Valid input: random
     * valid mail address
     */
    @Test
    public void testGetAndSetEmail() {
        String testEmail = null;
        Throwable e = null;
        try {
            testInvitation.setEmail(testEmail);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the Email", e instanceof AssertionError);

        testEmail = Helper.getRandomAlphanumericString(random.nextInt(25) + 1);
        e = null;
        try {
            testInvitation.setEmail(testEmail);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set an Email that does not match the pattern",
            e instanceof AssertionError);

        testEmail = Helper.getRandomMailAddress();
        testInvitation.setEmail(testEmail);
        assertEquals("The getting Email was not the expected one", testEmail,
            testInvitation.getEmail());
    }

    /**
     * Test of {@link Invitation#getRole} and {@link Invitation#setRole}.<br> Invalid input:
     * <code>null</code><br> Valid input: random String
     */
    @Test
    public void testGetAndSetRole() {
        String testRole = null;
        Throwable e = null;
        try {
            testInvitation.setRole(testRole);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the Role", e instanceof AssertionError);

        testRole = Helper.getRandomAlphabeticString(random.nextInt(10) + 3);
        testInvitation.setRole(testRole);
        assertEquals("The getting Role was not the expected one", testRole,
            testInvitation.getRole());
    }

    /**
     * Test of {@link Invitation#getExpirationDate}, {@link Invitation#setExpirationDate},
     * {@link Invitation#refreshExpirationDate} and {@link Invitation#isActive}.<br> Invalid input:
     * <code>null</code><br> Valid input: yesterday, and refreshed ExpirationDate
     */
    @Test
    public void testGetSetAndRefreshExpirationDateAndIsActive() {
        Throwable e = null;
        try {
            testInvitation.setExpirationDate(null);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the ExpirationDate",
            e instanceof AssertionError);

        Calendar in8days = Calendar.getInstance();
        in8days.set(Calendar.DAY_OF_YEAR, in8days.get(Calendar.DAY_OF_YEAR) + 8);
        Calendar in6days = Calendar.getInstance();
        in6days.set(Calendar.DAY_OF_YEAR, in6days.get(Calendar.DAY_OF_YEAR) + 6);
        assertTrue("The expiration date was not between 6 and 8 days",
            testInvitation.getExpirationDate().before(in8days.getTime())
                && testInvitation.getExpirationDate().after(in6days.getTime()));

        Calendar yesterday = Calendar.getInstance();
        yesterday.set(Calendar.DAY_OF_YEAR, yesterday.get(Calendar.DAY_OF_YEAR) - 1);
        testInvitation.setExpirationDate(yesterday.getTime());
        assertFalse("The invitation was still active although the ExpirationDate was yesterday",
            testInvitation.isActive());

        testInvitation.refreshExpirationDate();
        assertTrue("The invitation was not active although the ExpirationDate was refreshed",
            testInvitation.isActive());
    }

    /**
     * Test of {@link Invitation#getOwner} and {@link Invitation#setOwner}.<br> Invalid input:
     * <code>null</code><br> Valid input: random {@link User}
     */
    @Test
    public void testGetAndSetOwner() {
        User testOwner = null;
        Throwable e = null;
        try {
            testInvitation.setOwner(testOwner);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the Owner", e instanceof AssertionError);

        testOwner = UserTest.getNewValidUser();
        testInvitation.setOwner(testOwner);
        assertEquals("The getting Owner was not the expected one", testOwner,
            testInvitation.getOwner());
    }

    /**
     * Test of {@link Invitation#getPersonalText} and {@link Invitation#setPersonalText}.<br>
     * Invalid input: <code>null</code><br> Valid input: random String
     */
    @Test
    public void testGetAndSetPersonalText() {
        String testPersonalText = null;
        Throwable e = null;
        try {
            testInvitation.setPersonalText(testPersonalText);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the PersonalText", e instanceof AssertionError);

        testPersonalText = Helper.getRandomAlphabeticString(random.nextInt(500) + 1);
        testInvitation.setPersonalText(testPersonalText);
        assertEquals("The getting PersonalText was not the expected one", testPersonalText,
            testInvitation.getPersonalText());
    }

    /**
     * Test of {@link Invitation#getLocale} and {@link Invitation#setLocale}.<br> Invalid input:
     * <code>null</code><br> Valid input: random Locale as String
     */
    @Test
    public void testGetAndSetLocale() {
        String testLocale = null;
        Throwable e = null;
        try {
            testInvitation.setLocale(testLocale);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the Locale", e instanceof AssertionError);

        testLocale = Helper.getRandomLocale();
        testInvitation.setLocale(testLocale);
        assertEquals("The getting Locale was not the expected one", testLocale,
            testInvitation.getLocale());
    }

    /**
     * Test of {@link Invitation#getAssignedClinics} and {@link Invitation#setAssignedClinics}.<br>
     * Invalid input: <code>null</code><br> Valid input: random set of
     * {@link AclObjectIdentity AclObjectIdentities}
     */
    @Test
    public void testGetAndSetAssignedClinics() {
        Set<AclObjectIdentity> testAssignedClinics = null;
        Throwable e = null;
        try {
            testInvitation.setAssignedClinics(testAssignedClinics);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the assignedClinics",
            e instanceof AssertionError);

        testAssignedClinics = new HashSet<>();
        int countAssignedClinics = random.nextInt(50);
        for (int i = 0; i < countAssignedClinics; i++) {
            testAssignedClinics.add(AclObjectIdentityTest.getNewValidAclObjectIdentity());
        }
        testInvitation.setAssignedClinics(testAssignedClinics);
        assertEquals("The getting set of assignedClinics was not the expected one",
            testAssignedClinics, testInvitation.getAssignedClinics());
    }

    /**
     * Test of {@link Invitation#removeClinic}.<br> Invalid input: <code>null</code><br> Valid
     * input: random number of associated {@link AclObjectIdentity AclClinics}
     */
    @Test
    public void testRemoveClinic() {
        AclObjectIdentity testAclClinic = null;
        Throwable e = null;
        try {
            testInvitation.removeClinic(testAclClinic);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the assignedClinics",
            e instanceof AssertionError);

        Set<AclObjectIdentity> testSet = new HashSet<>();
        Set<AclObjectIdentity> removeSet = new HashSet<>();
        int countAssignedClinics = random.nextInt(50) + 1;
        for (int i = 0; i < countAssignedClinics; i++) {
            testAclClinic = AclObjectIdentityTest.getNewValidAclObjectIdentity();
            testSet.add(testAclClinic);
            if (random.nextBoolean()) {
                removeSet.add(testAclClinic);
            }
        }
        removeSet.add(testAclClinic);
        testInvitation.setAssignedClinics(testSet);
        assertTrue("The last aclClinic was not added",
            testInvitation.getAssignedClinics().contains(testAclClinic));
        assertEquals("The getting set of assignedClinics was not the expected one", testSet,
            testInvitation.getAssignedClinics());

        for (AclObjectIdentity removeAclClinic : removeSet) {
            testInvitation.removeClinic(removeAclClinic);
        }
        testSet.removeAll(removeSet);
        assertFalse("The aclClinic was not deleted correctly",
            testInvitation.getAssignedClinics().contains(testAclClinic));
        assertEquals(
            "The getting set of assignedClinics was not the expected one after removing some items",
            testSet, testInvitation.getAssignedClinics());
    }

    /**
     * Test of {@link Invitation#equals}.<br> Valid input: the same {@link Invitation} twice in a
     * HashSet, the same {@link Invitation}, <code>null</code>, another {@link Invitation}, another
     * Object
     */
    @Test
    public void testEquals() {
        HashSet<Invitation> testSet = new HashSet<>();
        testSet.add(testInvitation);
        testSet.add(testInvitation);
        assertEquals("It was possible to set the same Invitation in one set", 1, testSet.size());

        assertEquals("The Invitation was not equal to itself", testInvitation, testInvitation);
        assertNotEquals("The Invitation was equal to null", null, testInvitation);
        Invitation otherInvitation = getNewValidInvitation();
        assertNotEquals("The Invitation was equal to a different Invitation", testInvitation,
            otherInvitation);
        Object otherObject = new Object();
        assertNotEquals("The Invitation was equal to a different Object", testInvitation,
            otherObject);
    }

    /**
     * Test of {@link Invitation#toInvitationDTO}.<br> Valid input: random {@link Invitation}
     */
    @Test
    public void testToInvitationDTO() {
        testInvitation.setEmail(Helper.getRandomMailAddress());
        testInvitation.setFirstName(Helper.getRandomAlphabeticString(random.nextInt(255) + 1));
        testInvitation.setLastName(Helper.getRandomAlphabeticString(random.nextInt(255) + 1));
        testInvitation.setLocale(Helper.getRandomLocale());
        testInvitation.setPersonalText(Helper.getRandomAlphabeticString(random.nextInt(500) + 1));
        testInvitation.setRole(Helper.getRandomAlphabeticString(random.nextInt(10) + 3));
        InvitationDTO testInvitationDTO = testInvitation.toInvitationDTO();
        assertEquals("The getting Id was not the expected one", testInvitation.getId(),
            testInvitationDTO.getId());
        //assertEquals("The getting Email was not the expected one", testInvitation.getEmail(), testInvitationDTO.getEmail());
        //assertEquals("The getting Firstname was not the expected one", testInvitation.getFirstName(), testInvitationDTO.getFirstName());
        //assertEquals("The getting Lastname was not the expected one", testInvitation.getLastName(), testInvitationDTO.getLastName());
        assertEquals("The getting Locale was not the expected one", testInvitation.getLocale(),
            testInvitationDTO.getLocale());
        assertEquals("The getting PersonalText was not the expected one",
            testInvitation.getPersonalText(), testInvitationDTO.getPersonalText());
        assertEquals("The getting Role was not the expected one", testInvitation.getRole(),
            testInvitationDTO.getRole());
    }

    /**
     * Test of {@link Invitation#sendMail}.<br> Valid input: valid aplicationMailer, messageSource
     * und baseURL
     */
    @Test
    public void testSendMail() {
        String baseURL = "http://localhost/";
        testInvitation.setLocale(Helper.getRandomLocale());
        // PersonalText null
        assertTrue("1", testInvitation.sendMail(applicationMailer, messageSource, baseURL));
        // PersonalText empty
        testInvitation.setPersonalText("          ");
        assertTrue("2", testInvitation.sendMail(applicationMailer, messageSource, baseURL));
        // PersonalText not empty
        testInvitation.setPersonalText(Helper.getRandomString(random.nextInt(400) + 100));
        assertTrue("3", testInvitation.sendMail(applicationMailer, messageSource, baseURL));
    }
}
