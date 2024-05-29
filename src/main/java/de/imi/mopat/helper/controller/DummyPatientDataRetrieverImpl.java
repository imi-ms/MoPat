package de.imi.mopat.helper.controller;

import de.imi.mopat.model.dto.EncounterDTO;
import de.imi.mopat.model.enumeration.Gender;

import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Properties;

import org.slf4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

/**
 * Retrieves dummy patient data configured in the mopat properties file. If a property's value is
 * 'null', a <code>null</code> value will be used. If properties are missing, it retrieves fixed
 * dummy data.
 *
 * @version 1.0
 */
public class DummyPatientDataRetrieverImpl extends PatientDataRetriever {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(
        DummyPatientDataRetrieverImpl.class);
    public static final String FIRSTNAME_LOOKUP =
        "de.imi.mopat.helper" + ".controller.DummyPatientDataRetrieverImpl.firstname";
    public static final String LASTNAME_LOOKUP =
        "de.imi.mopat.helper" + ".controller.DummyPatientDataRetrieverImpl.lastname";
    public static final String BIRTHDATE_LOOKUP =
        "de.imi.mopat.helper" + ".controller.DummyPatientDataRetrieverImpl.birthdate";
    public static final String SEX_LOOKUP =
        "de.imi.mopat.helper.controller" + ".DummyPatientDataRetrieverImpl.sex";
    public static final String PID_LOOKUP =
        "de.imi.mopat.helper.controller" + ".DummyPatientDataRetrieverImpl.pid";
    public static final String FIRSTNAME_BACKUP = "Bruce";
    public static final String LASTNAME_BACKUP = "Wayne";
    public static final String BIRTHDATE_BACKUP = "19-02-1939";
    public static final String SEX_BACKUP = Gender.MALE.getTextValue();
    public static final String PID_BACKUP = "42";
    public static final String BIRTHDATE_PATTERN = "dd-MM-yyyy";
    private String firstname;
    private String lastname;
    private Date birthdate;
    private Gender sex;
    private Long pid;

    public DummyPatientDataRetrieverImpl() {
        Resource resource = new ClassPathResource("/" + Constants.CONFIGURATION);
        try {
            LOGGER.info(
                "[SETUP] Accessing properties file to look up dummy " + "patient data in {}...",
                Constants.CONFIGURATION);
            Properties props = PropertiesLoaderUtils.loadProperties(resource);

            // [bt] configure the firstname
            firstname = props.getProperty(FIRSTNAME_LOOKUP);
            if (firstname == null) {
                LOGGER.info("[SETUP] no firstname found. Using {} instead. To "
                        + "configure the firstname, provide a value for" + " {} in the {} file",
                    FIRSTNAME_BACKUP, FIRSTNAME_LOOKUP, Constants.CONFIGURATION);
                firstname = FIRSTNAME_BACKUP;
            } else {
                LOGGER.info("[SETUP] firstname configuration found. Using {}.", firstname);
                if (firstname.equalsIgnoreCase("null")) {
                    LOGGER.info("[SETUP] a 'null' value has been set for the "
                        + "firstname. Will retrieve NULL for " + "the patient's first name");
                    firstname = null;
                }
            }

            // [bt] configure the lastname
            lastname = props.getProperty(LASTNAME_LOOKUP);
            if (lastname == null) {
                LOGGER.info("[SETUP] no lastname found. Using {} instead. To "
                        + "configure the lastname, provide a value for " + "{} in the {} file",
                    new String[]{LASTNAME_BACKUP, LASTNAME_LOOKUP, Constants.CONFIGURATION});
                lastname = LASTNAME_BACKUP;
            } else {
                LOGGER.info("[SETUP] lastname configuration found. Using {}.", lastname);
                if (lastname.equalsIgnoreCase("null")) {
                    LOGGER.info("[SETUP] a 'null' value has been set for the "
                        + "lastname. Will retrieve NULL for the" + " patient's last name");
                    lastname = null;
                }
            }

            // [bt] configure the birthdate
            String tempBirthdate = props.getProperty(BIRTHDATE_LOOKUP);
            if (tempBirthdate == null) {
                LOGGER.error("[SETUP] no birthdate found. Using {} instead. To "
                        + "configure the birthdate, provide a value for" + " {} in the {} file",
                    new String[]{BIRTHDATE_BACKUP, BIRTHDATE_LOOKUP, Constants.CONFIGURATION});
                tempBirthdate = BIRTHDATE_BACKUP;
            } else {
                LOGGER.info("[SETUP] birthdate configuration found. Using {}", tempBirthdate);
            }
            if (tempBirthdate.equalsIgnoreCase("null")) {
                LOGGER.info("[SETUP] a 'null' value has been set for the "
                    + "birthdate. Will retrieve NULL for the " + "patient's date of birth");
                birthdate = null;
            } else {
                try {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(BIRTHDATE_PATTERN);
                    birthdate = new java.sql.Date(simpleDateFormat.parse(tempBirthdate).getTime());
                    LOGGER.info("[SETUP] date of birth successfully parsed and "
                        + "set. Using {} for the patient's date of " + "birth", birthdate);
                } catch (Exception e) {
                    birthdate = null;
                    LOGGER.error("[SETUP] something went wrong while parsing and "
                        + "setting the patient's date of birth. "
                        + "Please provide the patient's date of "
                        + "birth in the pattern {} .Will retrieve "
                        + "NULL for the patient's date of birth", BIRTHDATE_PATTERN);
                    LOGGER.debug("[SETUP] error: {}", e);
                }
            }

            // [bt] configure the sex
            String tempGender = props.getProperty(SEX_LOOKUP);
            if (tempGender == null) {
                LOGGER.error("[SETUP] no sex found. Using {} instead. To configure"
                        + " the sex, provide a value for {} in the {} " + "file",
                    new String[]{SEX_BACKUP, SEX_LOOKUP, Constants.CONFIGURATION});
                tempGender = SEX_BACKUP;
            } else {
                LOGGER.info("[SETUP] sex configuration found. Using {}", tempGender);
            }
            if (tempGender.equalsIgnoreCase("null")) {
                LOGGER.info("[SETUP] a 'null' value has been set for the sex."
                    + " Will retrieve NULL for the patient's " + "sex");
                sex = null;
            } else {
                try {
                    sex = Gender.fromString(tempGender);
                    LOGGER.info("[SETUP] sex successfully parsed and set. Using "
                        + "{} for the patient's sex", sex);
                } catch (Exception e) {
                    sex = null;
                    LOGGER.error("[SETUP] something went wrong while parsing and "
                        + "setting the patient's sex. Please "
                        + "provide the patient's sex in the values "
                        + "{} .Will retrieve NULL for the patient's" + " sex", Gender.values());
                    LOGGER.debug("[SETUP] error: {}", e);
                }
            }

            // [bt] configure the pid
            String tempPid = props.getProperty(PID_LOOKUP);
            if (tempPid == null) {
                LOGGER.info("[SETUP] no patient ID found. Using {} instead. To "
                        + "configure the PID, provide a value for {} in" + " the {} file",
                    new String[]{PID_BACKUP, PID_LOOKUP, Constants.CONFIGURATION});
                tempPid = PID_BACKUP;
            } else {
                LOGGER.info("[SETUP] patient ID configuration found. Using {}.", tempPid);
            }
            if (tempPid.equalsIgnoreCase("null")) {
                LOGGER.info("[SETUP] a 'null' value has been set for the "
                    + "patient ID. Will retrieve NULL for the " + "patient's ID");
                pid = null;
            } else {
                try {
                    pid = Long.valueOf(tempPid);
                    LOGGER.info("[SETUP] patient ID successfully parsed and set. "
                        + "Using {} for the patient's ID", pid);
                } catch (Exception e) {
                    pid = null;
                    LOGGER.error("[SETUP] something went wrong while parsing "
                        + "and setting the patient's ID. " + "Please provide the patient's ID as "
                        + "a number .Will retrieve NULL for " + "the patient's ID");
                    LOGGER.debug("[SETUP] error: {}", e);
                }
            }

            LOGGER.info("[SETUP] Accessing properties file to look up dummy "
                + "patient data in {}...[DONE]", Constants.CONFIGURATION);
        } catch (IOException e) {
            LOGGER.error("[SETUP] Was not able to open configuration file {} " + "because of: {}",
                Constants.CONFIGURATION, e);
        }
    }

    @Override
    public EncounterDTO retrievePatientData(final String caseNumber) {
        LOGGER.debug("Case number is: {}", caseNumber);
        assert caseNumber != null : "The given caseNumber was null";
        EncounterDTO result = new EncounterDTO();
        result.setCaseNumber(caseNumber);
        if (firstname != null) {
            result.setFirstname(firstname);
        }
        if (lastname != null) {
            result.setLastname(lastname);
        }
        if (birthdate != null) {
            result.setBirthdate(birthdate);
        }
        if (sex != null) {
            result.setGender(sex);
        }
        if (pid != null) {
            result.setPatientID(pid);
        }
        return result;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public Date getBirthdate() {
        return birthdate;
    }

    public Gender getSex() {
        return sex;
    }

    public void setFirstname(final String firstname) {
        this.firstname = firstname;
    }

    public void setLastname(final String lastname) {
        this.lastname = lastname;
    }

    public void setBirthdate(final Date birthdate) {
        this.birthdate = birthdate;
    }

    public void setSex(final Gender sex) {
        this.sex = sex;
    }
}