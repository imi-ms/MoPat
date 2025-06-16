package de.imi.mopat.helper.controller;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.app.Connection;
import ca.uhn.hl7v2.app.Initiator;
import ca.uhn.hl7v2.llp.MinLowerLayerProtocol;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v22.group.ADR_A19_QUERY_RESPONSE;
import ca.uhn.hl7v2.model.v22.message.ADR_A19;
import ca.uhn.hl7v2.model.v22.message.QRY_Q01;
import ca.uhn.hl7v2.model.v22.segment.MSH;
import ca.uhn.hl7v2.model.v22.segment.PID;
import ca.uhn.hl7v2.model.v22.segment.QRD;
import ca.uhn.hl7v2.parser.PipeParser;
import de.imi.mopat.dao.AuditEntryDao;
import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.model.Clinic;
import de.imi.mopat.model.Configuration;
import de.imi.mopat.model.Encounter;
import de.imi.mopat.model.dto.EncounterDTO;
import de.imi.mopat.model.enumeration.AuditEntryActionType;
import de.imi.mopat.model.enumeration.AuditPatientAttribute;
import de.imi.mopat.model.enumeration.Gender;
import de.imi.mopat.model.user.User;
import org.slf4j.Logger;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Looks up patient data using HL7 v2.2 messages (QRY^01 to query and ADR_A19 to retrieve). Tries to
 * connect to a system using a given hostname (see
 * {@link HL7v22PatientInformationRetrieverByPID#getHL7v22PatientInformationRetrieverHostname() }) and
 * port (see {@link HL7v22PatientInformationRetrieverByPID#getHL7v22PatientInformationRetrieverPort() })
 * that have to be provided in the configuration.<br> Different to the official HL7 specification,
 * this HL7 patient data retriever does not only allow the value 'AA' as an MSA acknowledgement
 * code, but also the value 'CA'. If either one of these values is present as an MSA acknowledgement
 * code in the communication server's answer, this patient data retriever tries to get patient data
 * out of this very answer and populate the {@link Encounter}.
 *
 * @version 1.0
 */
public class HL7v22PatientInformationRetrieverByPID extends PatientDataRetriever {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(
        HL7v22PatientInformationRetrieverByPID.class);
    /**
     * Codes from HL7 v2 specification telling that the message sent to a communication server
     * resulted in acceptance and thus valid data sent back, not error or reject
     */
    public static final String MSA_APPLICATION_ACCEPT_CODE = "AA";
    public static final String MSA_COMMIT_ACCEPT_CODE = "CA";

    // Use the same properties for this exporter
    private final String className = "de.imi.mopat.helper.controller.HL7v22PatientInformationRetriever";
    private final String hostnameProperty = "HL7v22PatientInformationRetrieverHostname";
    private final String portProperty = "HL7v22PatientInformationRetrieverPort";
    public final String usePatientDataLookupGroupName = "configurationGroup.label.usePatientLookUp";
    

    public HL7v22PatientInformationRetrieverByPID() {
        LOGGER.info("[SETUP] To configure this PatientDataRetriever, please set "
                + "HL7 hostname ({}) and port ({}) in the " + "configuration...", hostnameProperty,
            portProperty);
    }

    @Override
    public EncounterDTO retrievePatientData(Clinic clinic, String patientNumber) {
        LOGGER.debug("patientNumber is: {}", patientNumber);
        assert patientNumber != null : "The given patientNumber was null";
        patientNumber = patientNumber.trim();
        EncounterDTO result = null;
        String hostname = getHL7v22PatientInformationRetrieverHostname(clinic);
        Integer port = getHL7v22PatientInformationRetrieverPort(clinic);
        if (hostname != null && port != null) {
            LOGGER.info("[SETUP] hostname is: {}", hostname);
            LOGGER.info("[SETUP] port is: {}", port);
            // [bt] bean was able to retrieve a hostname and port
            // (configuration),
            // thus setting up a connection is worth it
            QRY_Q01 hl7message = new QRY_Q01();
            Connection connection = null;
            Set<AuditPatientAttribute> patientAttributes = new HashSet<AuditPatientAttribute>();
            try {
                LOGGER.debug("Creating a HL7 message to retrieve patient data" + "...");
                // [bt] We sent a QRY^01 message (see MoPat1,
                // orbis_interface.rb:create_hl7_msg(patientID)) and set the
                // processingID to "P" for PRODUCTION (again, see MoPat1,
                // orbis_interface.rb:create_hl7_msg(patientID) and HAPI
                // AbstractMessage.initQuickstart())
                hl7message.initQuickstart("QRY", "01", "P");
                // [bt] set the 2 important segments, 'MSH' and 'QRD'
                MSH msh = hl7message.getMSH();
                msh.getSendingApplication().setValue("PATB");
                msh.getReceivingApplication().setValue("ORBIS");
                QRD qrd = hl7message.getQRD();
                qrd.getQrd8_WhoSubjectFilter(0).setValue(patientNumber);

                qrd.getQrd9_WhatSubjectFilter(0).setValue("MRO");

                // [bt] set up a context (well, factory for connections and
                // parsers and so on
                HapiContext context = new DefaultHapiContext();
                MinLowerLayerProtocol mllp = new MinLowerLayerProtocol();
                mllp.setCharset("ISO-8859-1");
                context.setLowerLayerProtocol(mllp);
                // [bt] let the default Pipe parser parse our message
                PipeParser parser = context.getPipeParser();
                LOGGER.debug("HL7 message created: {}", parser.encode(hl7message));

                LOGGER.debug("Opening a Connection for HL7 messaging...");
                // [bt] open a new connection with the given hostname, port, and
                // don't use TLS
                connection = context.newClient(hostname, port, false);
                Initiator initiator = connection.getInitiator();
                initiator.setTimeout(30, TimeUnit.SECONDS);
                LOGGER.debug("Opening a Connection for HL7 messaging...[DONE]");
                LOGGER.debug("Sending HL7 message...");
                Message response = initiator.sendAndReceive(hl7message);
                patientAttributes.add(AuditPatientAttribute.CASE_NUMBER);
                this.getAuditEntryDao()
                    .writeAuditEntry(this.getClass().getSimpleName(), "retrievePatientData(String)",
                        patientNumber, patientAttributes, AuditEntryActionType.SENT,
                        "HL7 Communication server as given in " + "configuration");
                LOGGER.debug("Sending HL7 message...[DONE]");

                ADR_A19 hl7response = (ADR_A19) response;

                String acknowledgementCode = hl7response.getMSA().getMsa1_AcknowledgementCode()
                    .getValue();

                if (acknowledgementCode.equalsIgnoreCase(MSA_APPLICATION_ACCEPT_CODE)
                    || acknowledgementCode.equalsIgnoreCase(MSA_COMMIT_ACCEPT_CODE)) {
                    ADR_A19_QUERY_RESPONSE queryResponse = hl7response.getQUERY_RESPONSE();

                    PID pid = queryResponse.getPID();
                    if (pid != null) {
                        patientAttributes.clear();
                        result = new EncounterDTO();

                        //Try to set case number from PID4.1
                        try {
                            String caseNumber = pid.getPid4_AlternatePatientID().getValue();

                            if (caseNumber != null) {
                                result.setCaseNumber(caseNumber);
                                patientAttributes.add(AuditPatientAttribute.CASE_NUMBER);
                            }
                        } catch(Throwable t) {
                            LOGGER.error("Although a HL7 response was sent, "
                                    + "setting the case number did " + "not work, because of: {}. Use "
                                    + "debug output for stacktrace.", t.getMessage());
                            LOGGER.debug("Case ID could no be set because of:" + " {}", t.getMessage());
                        }


                        // [bt] separation of tries and error handling for each
                        // of
                        // the encounter's fields
                        try {
                            if (pid.getDateOfBirth() != null
                                && pid.getDateOfBirth().getTimeOfAnEvent() != null) {
                                // [bt] getting the values for the day of birth.
                                // CAVE:
                                // Month is from 0 to 11 in GregorianCalendar
                                int year = pid.getDateOfBirth().getTimeOfAnEvent().getYear();
                                int month = pid.getDateOfBirth().getTimeOfAnEvent().getMonth() - 1;
                                int day = pid.getDateOfBirth().getTimeOfAnEvent().getDay();

                                GregorianCalendar calendar = new GregorianCalendar(year, month,
                                    day);
                                java.sql.Date dayOfBirth = new java.sql.Date(
                                    calendar.getTimeInMillis());

                                result.setBirthdate(dayOfBirth);
                                patientAttributes.add(AuditPatientAttribute.DATE_OF_BIRTH);
                                LOGGER.debug("Date of Birth was available and" + " set");
                            }
                        } catch (Throwable t) {
                            LOGGER.error("Although a HL7 response was sent, "
                                + "setting the day of birth did " + "not work, because of: {}. Use "
                                + "debug output for stacktrace.", t.getMessage());
                            LOGGER.debug("Day of birth could no be set because of:" + " {}", t);
                        }

                        try {
                            if (pid.getPatientName() != null
                                && pid.getPatientName().getGivenName() != null
                                && pid.getPatientName().getGivenName().getValue() != null) {
                                result.setFirstname(pid.getPatientName().getGivenName().getValue());
                                patientAttributes.add(AuditPatientAttribute.FIRST_NAME);
                                LOGGER.debug("Patient's first name was " + "available and set");
                            }
                        } catch (Throwable t) {
                            LOGGER.error("Although a HL7 response was sent, "
                                + "setting the first name did not" + " work, because of: {}. Use "
                                + "debug output for stacktrace.", t.getMessage());
                            LOGGER.debug("First name could no be set because of: {}", t);
                        }

                        try {
                            if (pid.getPatientName() != null
                                && pid.getPatientName().getFamilyName() != null
                                && pid.getPatientName().getFamilyName().getValue() != null) {
                                result.setLastname(pid.getPatientName().getFamilyName().getValue());
                                patientAttributes.add(AuditPatientAttribute.LAST_NAME);
                                LOGGER.debug("Patient's last name was " + "available and set");
                            }
                        } catch (Throwable t) {
                            LOGGER.error("Although a HL7 response was sent, "
                                + "setting the last name did not " + "work, because of: {}. Use "
                                + "debug output for stacktrace.", t.getMessage());
                            LOGGER.debug("Last name could no be set because of: {}", t);
                        }

                        try {
                            if (pid.getSex() != null && pid.getSex().getValue() != null) {
                                switch (pid.getSex().getValue()) {
                                    case "M": {
                                        result.setGender(Gender.MALE);
                                        patientAttributes.add(AuditPatientAttribute.GENDER);
                                        LOGGER.debug(
                                            "Patient's gender was " + "available and" + " set");
                                        break;
                                    }
                                    case "F": // [bt] although the word document
                                        // lists
                                        // 'W', the communication server
                                        // sends
                                        // 'F' (see MoPat1, additionally
                                        // tested
                                        // with a real life case at 08th of
                                        // August, 2013)
                                    {
                                        result.setGender(Gender.FEMALE);
                                        patientAttributes.add(AuditPatientAttribute.GENDER);
                                        LOGGER.debug(
                                            "Patient's gender was " + "available and" + " set");
                                        break;
                                    }
                                }
                            }
                        } catch (Throwable t) {
                            LOGGER.error(
                                "Although a HL7 response was sent, " + "setting the gender did not "
                                    + "work, because of: {}. Use " + "debug output for stacktrace.",
                                t.getMessage());
                            LOGGER.debug("Gender could no be set because of: {}", t);
                        }

                        try {
                            if (pid.getPatientIDInternalID(0) != null
                                && pid.getPatientIDInternalID(0).getIDNumber() != null
                                && pid.getPatientIDInternalID(0).getIDNumber().getValue() != null) {
                                result.setPatientID(Long.valueOf(
                                    pid.getPatientIDInternalID(0).getIDNumber().getValue()));
                                patientAttributes.add(AuditPatientAttribute.PATIENT_ID);
                                LOGGER.debug("Patient's ID was available and " + "set");
                            }
                        } catch (Throwable t) {
                            LOGGER.error("Although a HL7 response was sent, "
                                + "setting the patient ID did not" + " work, because of: {}. Use "
                                + "debug output for stacktrace.", t.getMessage());
                            LOGGER.debug("Patient ID could no be set because of: {}", t);
                        }

                        this.getAuditEntryDao().writeAuditEntry(this.getClass().getSimpleName(),
                            "retrievePatientData(String)", patientNumber, patientAttributes,
                            AuditEntryActionType.RECEIVED,
                            "HL7 communication server as given in " + "configuration");
                    }
                }
            } catch (Exception e) {
                Long currentUserId = null;
                if (SecurityContextHolder.getContext().getAuthentication()
                    .getPrincipal() instanceof User) {
                    currentUserId = ((User) SecurityContextHolder.getContext().getAuthentication()
                        .getPrincipal()).getId();
                }

                String loggingAttributes = null;
                if (result != null) {
                    loggingAttributes = result.getLoggingAttributes();
                }

                if (loggingAttributes != null && loggingAttributes.isEmpty()) {
                    LOGGER.error("User ID {} requested patient data via HL7 for "
                            + "case number {}. Something went wrong "
                            + "while retrieving patient data. No "
                            + "patient data has successfully been "
                            + "retrieved. Because of the error, no "
                            + "patient data will be listet for the "
                            + "given case number. Use debug output " + "for more information",
                        currentUserId, patientNumber);
                } else {
                    this.getAuditEntryDao().writeAuditEntry(this.getClass().getSimpleName(),
                        "retrievePatientData(String)", patientNumber, patientAttributes,
                        AuditEntryActionType.RECEIVED,
                        "HL7 communication server as given in " + "configuration");
                    LOGGER.error("User ID {} requested patient data via HL7 for "
                            + "case number {}. Something went wrong "
                            + "while retrieving patient data. " + "Nonetheless, the following data has "
                            + "successfully been retrieved: {}. "
                            + "Because of the error, no patient data "
                            + "will be listet for the given case "
                            + "number. Use debug output for more " + "information", currentUserId,
                        patientNumber, loggingAttributes);
                }
                LOGGER.debug("Retrieving patient data failed because of: {}", e);

            } finally {
                if (connection != null) {
                    LOGGER.debug("Closing the connection...");
                    try {
                        connection.close();
                        LOGGER.debug("Closing the connection...[DONE]");
                    } catch (Exception e) {
                        LOGGER.error("Could not close connection. This could cause followup errors.", e);
                    }
                }
            }
        } else {
            LOGGER.error("hostname is {}, port is {}, thus no HL7 communication is"
                + " set up. Returning null", hostname, port);
        }
        return result;
    }

    /**
     * Returns the {@link AuditEntryDao} from the ApplicationContext.
     *
     * @return The {@link AuditEntryDao} from the ApplicationContext.
     */
    private AuditEntryDao getAuditEntryDao() {
        return ApplicationContextService.getApplicationContext().getBean(AuditEntryDao.class);
    }

    /**
     * Returns the HL7v22PatientRetriever hostname from the {@link ConfigurationDao} by using the
     * name of this class and the appropriate attribute name.
     *
     * @return The HL7v22PatientRetriever hostname string.
     */
    private String getHL7v22PatientInformationRetrieverHostname(Clinic clinic) {
        ConfigurationDao configurationDao = ApplicationContextService.getApplicationContext()
            .getBean(ConfigurationDao.class);
        Configuration configuration = configurationDao.getConfigurationByGroupName(
            clinic.getId(), hostnameProperty, className, usePatientDataLookupGroupName);
        return configuration.getValue();
    }

    /**
     * Returns the HL7v22PatientRetriever port from the {@link ConfigurationDao} by using the name
     * of this class and the appropriate attribute name.
     *
     * @return The HL7v22PatientRetriever port number.
     */
    private Integer getHL7v22PatientInformationRetrieverPort(Clinic clinic) {
        ConfigurationDao configurationDao = ApplicationContextService.getApplicationContext()
            .getBean(ConfigurationDao.class);
        Configuration configuration = configurationDao.getConfigurationByGroupName(
            clinic.getId(), portProperty, className, usePatientDataLookupGroupName);
        return Integer.parseInt(configuration.getValue());
    }
}
