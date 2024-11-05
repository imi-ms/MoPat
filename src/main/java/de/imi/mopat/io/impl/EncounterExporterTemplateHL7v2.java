package de.imi.mopat.io.impl;

import ca.uhn.hl7v2.hoh.sockets.CustomCertificateTlsSocketFactory;
import ca.uhn.hl7v2.hoh.util.HapiSocketTlsFactoryWrapper;
import ca.uhn.hl7v2.model.v23.datatype.ST;
import ca.uhn.hl7v2.model.v23.group.ORU_R01_PATIENT;
import ca.uhn.hl7v2.model.v23.message.ORU_R01;
import ca.uhn.hl7v2.model.v23.segment.MSH;
import ca.uhn.hl7v2.model.v23.segment.OBR;
import ca.uhn.hl7v2.model.v23.segment.OBX;
import de.imi.mopat.helper.controller.Constants;
import de.imi.mopat.io.EncounterExporter;
import de.imi.mopat.io.EncounterExporterTemplate;
import de.imi.mopat.model.ExportTemplate;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.app.Connection;
import ca.uhn.hl7v2.app.Initiator;
import ca.uhn.hl7v2.llp.MinLowerLayerProtocol;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.DefaultXMLParser;
import ca.uhn.hl7v2.parser.PipeParser;
import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.model.Configuration;
import de.imi.mopat.model.Encounter;
import de.imi.mopat.model.enumeration.ExportStatus;

import java.nio.charset.Charset;
import java.text.DateFormat;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class EncounterExporterTemplateHL7v2 implements EncounterExporterTemplate {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(
        EncounterExporterTemplateHL7v2.class);
    private static final String TRUE = "TRUE";
    private static final String FALSE = "FALSE";
    private static final String HL7_SUFFIX = "hl7";
    private static final String DOT = ".";
    private static final String UNDERSCORE = "_";
    private static final SimpleDateFormat HL7XMLFileNameDateFormat = new SimpleDateFormat(
        "dd.MM.yyyy_HH.mm.ss");

    private Document document;
    private Encounter encounter;
    private ExportTemplate exportTemplate;

    private final ConfigurationDao configurationDao;

    private ORU_R01 hl7Message;
    private ST clinicalDataString;

    /**
     * Constructor with given {@link ConfigurationDao} to get configuration informations within this
     * instance.
     *
     * @param configurationDao The {@link ConfigurationDao} from the context.
     */
    public EncounterExporterTemplateHL7v2(final ConfigurationDao configurationDao) {
        this.configurationDao = configurationDao;
    }

    @Override
    public void load(final Encounter encounter, final ExportTemplate exportTemplate)
        throws Exception {

        this.encounter = encounter;
        this.exportTemplate = exportTemplate;

        hl7Message = new ORU_R01();

        LOGGER.info("Creating a HL7 message to send patient data...");
        // Message initialization
        hl7Message.initQuickstart("ORU", "R01", "P");

        // Include export template path
        String objectStoragePath = configurationDao.getObjectStoragePath();
        if (objectStoragePath == null) {
            LOGGER.error("[SETUP] No object storage path found. Please provide a "
                    + "value for {} in the {} file", Constants.OBJECT_STORAGE_PATH_PROPERTY,
                Constants.CONFIGURATION);
        } else {
            LOGGER.info("[SETUP] Object storage path configuration found.");
        }
        LOGGER.info("[SETUP] Accessing properties file to look up the export " + "path"
            + " in  {}...[DONE]", Constants.CONFIGURATION);

        String templatePath = objectStoragePath + Constants.EXPORT_TEMPLATE_SUB_DIRECTORY;

        String filename = exportTemplate.getFilename();
        File file = new File(templatePath, filename);

        // This code is adapted from http://www.mkyong.com/java/how-to-read-xml-file-in-java-dom-parser/
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        dBuilder = dbFactory.newDocumentBuilder();
        // Load inputStream into w3c Document object..
        document = dBuilder.parse(new FileInputStream(file));
        document.setXmlStandalone(true);

        // Set HL7 message header
        MSH msh = hl7Message.getMSH();

        for (Configuration configuration : exportTemplate.getConfigurationGroup()
            .getConfigurations()) {
            if (configuration.getAttribute().equals("sendingFacility")) {
                msh.getMsh4_SendingFacility().getHd1_NamespaceID()
                    .setValue(configuration.getValue());
            }
            if (configuration.getAttribute().equals("receivingApplication")) {
                msh.getMsh5_ReceivingApplication().getHd1_NamespaceID()
                    .setValue(configuration.getValue());
            }
            if (configuration.getAttribute().equals("reveivingFacility")) {
                msh.getMsh6_ReceivingFacility().getHd1_NamespaceID()
                    .setValue(configuration.getValue());
            }
        }
        msh.getMsh3_SendingApplication().getHd1_NamespaceID()
            .setValue(getNode("Formname").getTextContent());

        msh.getMessageControlID().setValue(encounter.getId() + "_" + exportTemplate.getId());
        msh.getMsh14_ContinuationPointer().setValue("L");

        LOGGER.info("Creating Patient data");
        ORU_R01_PATIENT patientData = hl7Message.getRESPONSE().getPATIENT();
        if (encounter.getPatientID() != null) {
            patientData.getPID().getPatientIDInternalID(0).getCx1_ID()
                .setValue(Long.toString(encounter.getPatientID()));
        } else {
            LOGGER.error("The patient does not have an ID");
        }
        if (encounter.getCaseNumber() != null) {
            patientData.getVISIT().getPV1().getPv119_VisitNumber().getCx1_ID()
                .setValue(encounter.getCaseNumber());
        } else {
            LOGGER.error("The patient does not have a case number");
        }

        LOGGER.info("Creating Observation data");

        OBR obr = hl7Message.getRESPONSE().getORDER_OBSERVATION().getOBR();
        for (Configuration configuration : exportTemplate.getConfigurationGroup()
            .getConfigurations()) {
            if (configuration.getAttribute().equals("OBRFillerOrderNumber")) {
                obr.getObr3_FillerOrderNumber().getEi1_EntityIdentifier()
                    .setValue(configuration.getValue());
            }
        }
        DateFormat dateFormatter = new SimpleDateFormat("yyyyMMddhhmm");
        obr.getObr7_ObservationDateTime().getTimeOfAnEvent()
            .setValue(dateFormatter.format(encounter.getStartTime()));

        OBX obx = hl7Message.getRESPONSE().getORDER_OBSERVATION().getOBSERVATION(0).getOBX();
        obx.getObx1_SetIDOBX().setValue("1");
        obx.getObx2_ValueType().setValue("ST");
        clinicalDataString = new ST(hl7Message);
        obx.getObservationValue(0).setData(clinicalDataString);
        obx.getObx10_NatureOfAbnormalTest().setValue("F");

    }

    @Override
    public void write(final String exportField, String value) throws Exception {
        Node node = getNode(exportField);
        if (value != null && !value.isEmpty()) {
            // Replace boolean placeholder values with HL7 specific values
            if (value.equals(EncounterExporter.TRUE)) {
                value = TRUE;
            } else if (value.equals(EncounterExporter.FALSE)) {
                value = FALSE;
            }
            // Replace spaces with unicode to avoid ulter errors
            node.setTextContent(value.replaceAll(" ", "_u0020"));
        }
    }

    @Override
    public ExportStatus flush() throws Exception {
        // Transform doc to string
        // Text copied from
        // http://www.journaldev.com/1237/java-convert-string-to-xml-document-and-xml-document-to-string
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = transformerFactory.newTransformer();
            // Remove XML declaration
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(writer));
            String output = writer.getBuffer().toString();
            // Replace spaces and new lines
            output = output.replaceAll("\n", "").replaceAll("\\s", "");
            // Restore replaced spaces and set value
            clinicalDataString.setValue(output.replaceAll("_u0020", " "));
            //
        } catch (TransformerException exception) {
            LOGGER.error(exception.toString());
        }

        Boolean isExportServer = null;
        String hostname = null;
        Integer port = null;
        Boolean isExportInDirectory = null;
        String exportPathDirectory = null;
        Boolean useTLS = null;
        Boolean useClientAuth = null;
        String clientPKCSPath = null;
        String clientPKCSPassword = null;
        String serverCertificatePath = null;
        // Get export configurations
        for (Configuration configuration : exportTemplate.getConfigurationGroup()
            .getConfigurations()) {
            if (configuration.getAttribute().equals("exportViaCommunicationServer")) {
                isExportServer = Boolean.parseBoolean(configuration.getValue());
            }
            if (configuration.getAttribute().equals("exportHost")) {
                hostname = configuration.getValue();
            }
            if (configuration.getAttribute().equals("exportPort")) {
                try {
                    port = Integer.valueOf(configuration.getValue());
                } catch (NumberFormatException e) {
                    LOGGER.error("The port could not be converted to a number, as it was " + configuration.getValue(), e);
                }
            }
            if (configuration.getAttribute().equals("exportInDirectory")) {
                isExportInDirectory = Boolean.parseBoolean(configuration.getValue());
            }
            if (configuration.getAttribute().equals("exportPath")) {
                exportPathDirectory = configuration.getValue();
            }
            if (configuration.getAttribute().equals("useTLS")) {
                useTLS = Boolean.parseBoolean(configuration.getValue());
            }
            if (configuration.getAttribute().equals("useClientAuth")) {
                useClientAuth = Boolean.parseBoolean(configuration.getValue());
            }
            if (configuration.getAttribute().equals("clientPKCSPath")) {
                clientPKCSPath = configuration.getValue();
            }
            if (configuration.getAttribute().equals("serverCert")) {
                serverCertificatePath = configuration.getValue();
            }
            if (configuration.getAttribute().equals("clientPKCSPassword")) {
                clientPKCSPassword = configuration.getValue();
            }
        }

        KeyStore keyStore = null;
        if (Boolean.TRUE.equals(useTLS)) {
            if (Boolean.TRUE.equals(useClientAuth)) {
                keyStore = buildKeyStore(clientPKCSPath, clientPKCSPassword, serverCertificatePath);
            } else {
                keyStore = buildKeyStore(serverCertificatePath);
            }
        }

        // If export to server is activated
        if (Boolean.TRUE.equals(isExportServer)) {
            // Export it
            sendMessageViaComServer(hostname, port, useTLS, keyStore, clientPKCSPassword);
        }
        // If filebased export is activated
        if (Boolean.TRUE.equals(isExportInDirectory)) {
            // Export it
            exportToFolder(exportPathDirectory);
        }
        return ExportStatus.SUCCESS;
    }

    /**
     * Creates a new Keystore by loading the existing Client keystore and adding the given server
     * certificate
     *
     * @param clientPKCSPath        absolute Path to the client pkcs archive
     * @param clientPKCSPassword    password for client pkcs archive
     * @param serverCertificatePath absolute path to server certificate
     * @return KeyStore with the certificates loaded
     */
    private KeyStore buildKeyStore(final String clientPKCSPath, final String clientPKCSPassword,
        final String serverCertificatePath) {
        try {
            KeyStore keyStore = KeyStore.getInstance("pkcs12");
            keyStore.load(new FileInputStream(clientPKCSPath), clientPKCSPassword.toCharArray());
            addCertificateToKeyStore(keyStore, serverCertificatePath, "server");

            return keyStore;
        } catch (Exception ex) {
            LOGGER.error("Could not load keystore." + ex.getMessage());
            return null;
        }
    }

    /**
     * Creates a new Keystore and adds a single certificate to authorize the server
     *
     * @param serverCertificatePath absolute path to the server certificate
     * @return new KeyStore Object with the server certificate
     */
    private KeyStore buildKeyStore(final String serverCertificatePath) {
        try {
            KeyStore keyStore = KeyStore.getInstance("pkcs12");
            //Init KeyStore
            keyStore.load(null, null);
            addCertificateToKeyStore(keyStore, serverCertificatePath, "server");
            return keyStore;
        } catch (Exception ex) {
            LOGGER.error("Could not load keystore." + ex.getMessage());
            return null;
        }
    }

    /**
     * Adds the certificate under the given path to the keystore.
     *
     * @param keyStore        that the certificate should be added to
     * @param certificatePath absolute path for the certificate
     */
    private void addCertificateToKeyStore(final KeyStore keyStore, final String certificatePath,
        final String alias) {
        try {
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            FileInputStream is = new FileInputStream(certificatePath);
            X509Certificate serverCertificate = (X509Certificate) factory.generateCertificate(is);

            keyStore.setCertificateEntry(alias, serverCertificate);
        } catch (Exception ex) {
            LOGGER.error("Could not add certificate to keystore." + ex.getMessage());
        }
    }


    /**
     * Exports the HL7 message via a communication server specified by host and port.
     *
     * @param hostname           Host name of the communication server.
     * @param port               Port of the communication server
     * @param useTLS             weather TLS should be used or not
     * @param keyStore           the Keystore with the necessary certificates
     * @param keyStorePassphrase the password to use the keystore
     * @throws java.lang.Exception
     */
    private void sendMessageViaComServer(final String hostname, final Integer port,
        final Boolean useTLS, final KeyStore keyStore, final String keyStorePassphrase)
        throws Exception {
        // Set up a context: factory for connections and parsers and so on
        HapiContext context = new DefaultHapiContext();
        MinLowerLayerProtocol mllp = new MinLowerLayerProtocol();
        mllp.setCharset("ISO-8859-1");
        context.setLowerLayerProtocol(mllp);
        // Let the default Pipe parser parse our message
        PipeParser parser = context.getPipeParser();
        LOGGER.debug("HL7 message created: {}", parser.encode(hl7Message));
        LOGGER.debug("Opening a Connection for HL7 messaging...");
        // Open a new connection with the given hostname, port, and
        // Check configuration before setting keystore
        if (useTLS) {
            CustomCertificateTlsSocketFactory sfac = new CustomCertificateTlsSocketFactory(keyStore,
                keyStorePassphrase);
            context.setSocketFactory(new HapiSocketTlsFactoryWrapper(sfac));
        }

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
     * Exports the HL7 Message to a given path.
     *
     * @param exportPath Export path for the HL7 message
     * @throws java.lang.Exception if a problem occurs
     */
    public void exportToFolder(final String exportPath) throws Exception {
        // Make sure the path exists
        File path = new File(exportPath);
        if (!path.isDirectory()) {
            path.mkdirs();
        }
        //Create a sub-directory for the exported files
        File subDirectory = new File(
            exportPath + File.separator + exportTemplate.getQuestionnaire().getName()
                .replaceAll(":", "_") + "/" + exportTemplate.getName().replaceAll(":", "_") + "/");
        if (!subDirectory.isDirectory()) {
            subDirectory.mkdirs();
        }
        // Write to disk
        File exportFile = new File(subDirectory, this.createHL7FileName());
        FileUtils.writeStringToFile(exportFile, hl7Message.encode(), StandardCharsets.UTF_8);
        LOGGER.info("The hl7 message has been exported");
    }

    /**
     * Returns the Node with the tag name equal to the attribute
     * <code>name</code>.
     *
     * @param name The node name.
     * @return An {link Node Node} object. Can not be <code>null</code>.
     * @throws SAXException If node does not exists or is not unique.
     */
    private Node getNode(final String name) throws SAXException {
        NodeList nodeList = document.getElementsByTagName(name);
        if (nodeList.getLength() == 0) {
            throw new SAXException("Node does not exist");
        } else if (nodeList.getLength() > 1) {
            throw new SAXException("More than one node with the name found");
        }
        return nodeList.item(0);
    }

    /**
     * Creates a unique HL7 XML Filename.
     *
     * @return The newly created unique HL7 XML Filename.
     */
    private String createHL7FileName() {
        String result =
            encounter.getCaseNumber() + UNDERSCORE + exportTemplate.getOriginalFilename()
                + UNDERSCORE + HL7XMLFileNameDateFormat.format(new Date()) + DOT + HL7_SUFFIX;
        return result;
    }
}
