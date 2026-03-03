package de.imi.mopat.io.impl;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.app.Connection;
import ca.uhn.hl7v2.app.Initiator;
import ca.uhn.hl7v2.hoh.sockets.CustomCertificateTlsSocketFactory;
import ca.uhn.hl7v2.hoh.util.HapiSocketTlsFactoryWrapper;
import ca.uhn.hl7v2.llp.MinLowerLayerProtocol;
import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v23.datatype.ST;
import ca.uhn.hl7v2.model.v23.group.ORU_R01_PATIENT;
import ca.uhn.hl7v2.model.v23.message.ORU_R01;
import ca.uhn.hl7v2.model.v23.segment.MSH;
import ca.uhn.hl7v2.model.v23.segment.OBR;
import ca.uhn.hl7v2.model.v23.segment.OBX;
import ca.uhn.hl7v2.parser.DefaultEscaping;
import ca.uhn.hl7v2.parser.DefaultXMLParser;
import ca.uhn.hl7v2.parser.EncodingCharacters;
import ca.uhn.hl7v2.parser.PipeParser;
import de.imi.mopat.model.Encounter;
import de.imi.mopat.model.ExportTemplate;
import de.imi.mopat.model.enumeration.ExportStatus;
import java.io.IOException;
import java.security.KeyStore;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;

public class HL7MessageHelper {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(HL7MessageHelper.class);

    /**
     * Sends an HL7 ORU^R01 message containing clinical data in the form of a blob to a
     * communication server. This method uses default settings for secure connections and non-secure
     * communication with null key store and passphrase.
     *
     * @param hostname             The hostname of the communication server to which the message
     *                             will be sent.
     * @param port                 The port number of the communication server.
     * @param exportTemplate       The export template containing metadata configurations for the
     *                             message.
     * @param encounter            The patient encounter details, including patient ID and case
     *                             number.
     * @param sendingFacility      The identifier of the sending facility.
     * @param receivingApplication The identifier of the receiving application.
     * @param receivingFacility    The identifier of the receiving facility.
     * @param obrFillerOrderNumber The filler order number for the OBR segment.
     * @param messageBlob          The clinical data blob to include in the observation segment
     *                             (OBX).
     * @return {@code ExportStatus.SUCCESS} if the message is sent successfully,
     * {@code ExportStatus.FAILURE} if an error occurs during message transmission.
     */
    public ExportStatus createAndSendMessageWithBlob(
            final String hostname,
            final Integer port,
            ExportTemplate exportTemplate,
            Encounter encounter,
            String sendingFacility,
            String receivingApplication,
            String receivingFacility,
            String obrFillerOrderNumber,
            String messageBlob) {
        return createAndSendMessageWithBlob(
                hostname,
                port,
                exportTemplate,
                encounter,
                sendingFacility,
                receivingApplication,
                receivingFacility,
                obrFillerOrderNumber,
                messageBlob,
                false,
                null,
                null);
    }

    /**
     * Sends an HL7 ORU^R01 message with a clinical data blob to a communication server. The method
     * supports both secure (TLS) and non-secure communication modes. Handles any exceptions that
     * occur during the communication process and returns an appropriate export status.
     *
     * @param hostname             The hostname of the communication server to which the message
     *                             will be sent.
     * @param port                 The port number of the communication server.
     * @param exportTemplate       The export template containing metadata configurations for the
     *                             message.
     * @param encounter            The patient encounter details, including patient ID and case
     *                             number.
     * @param sendingFacility      The identifier of the sending facility.
     * @param receivingApplication The identifier of the receiving application.
     * @param receivingFacility    The identifier of the receiving facility.
     * @param obrFillerOrderNumber The filler order number for the OBR segment.
     * @param messageBlob          The clinical data blob to include in the observation segment
     *                             (OBX).
     * @param useTLS               A Boolean flag indicating whether to use a secure TLS connection.
     *                             If true, the connection will be secured using a TLS socket.
     * @param keyStore             The key store containing private and public certificates for
     *                             establishing a secure connection. Required if useTLS is true.
     * @param keyStorePassphrase   The passphrase for accessing the provided key store.
     * @return {@code ExportStatus.SUCCESS} if the message is sent successfully,
     * {@code ExportStatus.FAILURE} if an error occurs during message transmission.
     */
    public ExportStatus createAndSendMessageWithBlob(
            final String hostname,
            final Integer port,
            ExportTemplate exportTemplate,
            Encounter encounter,
            String sendingFacility,
            String receivingApplication,
            String receivingFacility,
            String obrFillerOrderNumber,
            String messageBlob,
            Boolean useTLS,
            KeyStore keyStore,
            String keyStorePassphrase) {
        try {
            if (useTLS) {
                sendMessageViaComServer(
                        hostname,
                        port,
                        createMessageWithBlob(
                                exportTemplate,
                                encounter,
                                sendingFacility,
                                receivingApplication,
                                receivingFacility,
                                obrFillerOrderNumber,
                                messageBlob),
                        useTLS,
                        keyStore,
                        keyStorePassphrase);
            } else {
                sendMessageViaComServer(
                        hostname,
                        port,
                        createMessageWithBlob(
                                exportTemplate,
                                encounter,
                                sendingFacility,
                                receivingApplication,
                                receivingFacility,
                                obrFillerOrderNumber,
                                messageBlob));
            }
            return ExportStatus.SUCCESS;
        } catch (Exception e) {
            LOGGER.error("Error while sending message via HL7 communication server: {}", e);
            return ExportStatus.FAILURE;
        }
    }

    /**
     * Creates an HL7 ORU^R01 message containing patient encounter data and clinical observations.
     * The method sets various fields in the message header, patient data, and observation details,
     * including a message blob containing clinical information.
     *
     * @param exportTemplate       The export template containing metadata configurations for the
     *                             message.
     * @param encounter            The patient encounter details, including patient ID and case
     *                             number.
     * @param sendingFacility      The sending facility's identifier in the message.
     * @param receivingApplication The receiving application's identifier in the message.
     * @param receivingFacility    The receiving facility's identifier in the message.
     * @param obrFillerOrderNumber The filler order number for the OBR segment.
     * @param messageBlob          The clinical data blob to include in the observation segment
     *                             (OBX).
     * @return The constructed HL7 ORU^R01 message containing the patient data and observations.
     * @throws HL7Exception If an error occurs while constructing the HL7 message.
     * @throws IOException  If an input or output exception occurs during message handling.
     */
    public ORU_R01 createMessageWithBlob(
            ExportTemplate exportTemplate,
            Encounter encounter,
            String sendingFacility,
            String receivingApplication,
            String receivingFacility,
            String obrFillerOrderNumber,
            String messageBlob)
            throws HL7Exception, IOException {
        ORU_R01 hl7Message = new ORU_R01();

        LOGGER.info("[" + exportTemplate.getExportTemplateType().name()
                + " via HL7] Creating a HL7 message to send patient data...");
        // Message initialization
        hl7Message.initQuickstart("ORU", "R01", "P");

        // Set HL7 message header
        MSH msh = hl7Message.getMSH();

        msh.getMsh4_SendingFacility().getHd1_NamespaceID().setValue(sendingFacility);
        msh.getMsh5_ReceivingApplication().getHd1_NamespaceID().setValue(receivingApplication);
        msh.getMsh6_ReceivingFacility().getHd1_NamespaceID().setValue(receivingFacility);

        msh.getMsh3_SendingApplication()
                .getHd1_NamespaceID()
                .setValue(exportTemplate.getQuestionnaire().getName());

        msh.getMessageControlID().setValue(encounter.getId() + "_" + exportTemplate.getId());
        msh.getMsh14_ContinuationPointer().setValue("L");

        LOGGER.info("Creating Patient data");
        ORU_R01_PATIENT patientData = hl7Message.getRESPONSE().getPATIENT();
        if (encounter.getPatientID() != null) {
            patientData
                    .getPID()
                    .getPatientIDInternalID(0)
                    .getCx1_ID()
                    .setValue(Long.toString(encounter.getPatientID()));
        } else {
            LOGGER.error("The patient does not have an ID");
        }
        if (encounter.getCaseNumber() != null) {
            patientData.getVISIT().getPV1().getPv119_VisitNumber().getCx1_ID().setValue(encounter.getCaseNumber());
        } else {
            LOGGER.error("The patient does not have a case number");
        }

        LOGGER.info("Creating Observation data");

        OBR obr = hl7Message.getRESPONSE().getORDER_OBSERVATION().getOBR();
        obr.getObr3_FillerOrderNumber().getEi1_EntityIdentifier().setValue(obrFillerOrderNumber);

        DateFormat dateFormatter = new SimpleDateFormat("yyyyMMddhhmm");
        obr.getObr7_ObservationDateTime().getTimeOfAnEvent().setValue(dateFormatter.format(encounter.getStartTime()));

        OBX obx = hl7Message
                .getRESPONSE()
                .getORDER_OBSERVATION()
                .getOBSERVATION(0)
                .getOBX();
        obx.getObx1_SetIDOBX().setValue("1");
        obx.getObx2_ValueType().setValue("ST");
        ST clinicalDataString = new ST(hl7Message);
        obx.getObservationValue(0).setData(clinicalDataString);
        obx.getObx10_NatureOfAbnormalTest().setValue("F");

        EncodingCharacters encodingCharacters = EncodingCharacters.getInstance(hl7Message);

        DefaultEscaping escaping = new DefaultEscaping();
        String escapedMessageBlob = escaping.escape(messageBlob, encodingCharacters);

        clinicalDataString.setValue(escapedMessageBlob);

        return hl7Message;
    }

    /**
     * Overwrites the Namespace ID in the MSH-3 (Sending Application) field of the given HL7 ORU^R01
     * message with the specified namespace ID.
     *
     * @param message     The HL7 ORU_R01 message whose MSH-3 Namespace ID will be updated.
     * @param namespaceId The new Namespace ID to set in the MSH-3 Sending Application field.
     * @return The updated HL7 ORU_R01 message with the modified MSH-3 Namespace ID.
     * @throws DataTypeException If an error occurs while setting the Namespace ID.
     */
    public ORU_R01 overwriteMsh3NamespaceId(ORU_R01 message, String namespaceId) throws DataTypeException {
        MSH msh = message.getMSH();
        msh.getMsh3_SendingApplication().getHd1_NamespaceID().setValue(namespaceId);

        return message;
    }

    /**
     * Sends an HL7 ORU^R01 message to a communication server over a specified connection. The
     * method supports both secure (TLS) and non-secure connections, allowing the message to be sent
     * securely if required. The acknowledgment message is logged upon successful communication.
     *
     * @param hostname           The hostname of the communication server to which the HL7 message
     *                           will be sent.
     * @param port               The port number of the communication server.
     * @param hl7Message         The HL7 ORU^R01 message to be sent.
     * @param useTLS             A Boolean flag indicating whether to use a secure TLS connection.
     *                           If true, the connection will be secured using a TLS socket.
     * @param keyStore           The key store containing private and public certificates for
     *                           establishing a secure connection, required if useTLS is true.
     * @param keyStorePassphrase The passphrase for accessing the provided key store.
     * @throws Exception If an error occurs during message transmission or connection
     *                   establishment.
     */
    public void sendMessageViaComServer(
            final String hostname,
            final Integer port,
            final ORU_R01 hl7Message,
            final Boolean useTLS,
            final KeyStore keyStore,
            final String keyStorePassphrase)
            throws Exception {
        // Set up a context: factory for connections and parsers and so on
        HapiContext context = new DefaultHapiContext();
        MinLowerLayerProtocol mllp = new MinLowerLayerProtocol();
        mllp.setCharset("ISO-8859-1");
        context.setLowerLayerProtocol(mllp);
        // Let the default Pipe parser parse our message
        PipeParser parser = context.getPipeParser();
        LOGGER.debug("[Export via HL7] HL7 message created: {}", parser.encode(hl7Message));
        LOGGER.debug("Opening a Connection for HL7 messaging...");
        // Create new keystore for client
        if (Boolean.TRUE.equals(useTLS) && keyStore != null && keyStorePassphrase != null) {
            CustomCertificateTlsSocketFactory sfac =
                    new CustomCertificateTlsSocketFactory(keyStore, keyStorePassphrase);
            context.setSocketFactory(new HapiSocketTlsFactoryWrapper(sfac));
        }
        // Open a new connection with the given hostname and port
        // Specify tls connection
        Connection connection = context.newClient(hostname, port, useTLS);

        Initiator initiator = connection.getInitiator();
        LOGGER.debug("Opening a Connection for HL7 messaging...[DONE]");
        LOGGER.debug("Sending HL7 message...");
        initiator.setTimeout(30, TimeUnit.SECONDS);
        Message response = initiator.sendAndReceive(hl7Message);
        // Log the ACK which is an empty message (OPTIONAL)
        LOGGER.info((new DefaultXMLParser()).encode(response));
        LOGGER.debug("Sending HL7 message...[DONE]");
        connection.close();
    }

    /**
     * Sends an HL7 ORU^R01 message to a communication server over a specified connection. This
     * method uses default settings for secure connections, assuming non-secure communication with
     * null key store and passphrase.
     *
     * @param hostname   The hostname of the communication server to which the HL7 message will be
     *                   sent.
     * @param port       The port number of the communication server.
     * @param hl7Message The HL7 ORU^R01 message to be sent.
     * @throws Exception If an error occurs during message transmission or connection
     *                   establishment.
     */
    public void sendMessageViaComServer(final String hostname, final Integer port, final ORU_R01 hl7Message)
            throws Exception {
        this.sendMessageViaComServer(hostname, port, hl7Message, false, null, null);
    }
}
