package de.imi.mopat.io.impl;

import ca.uhn.hl7v2.model.v23.message.ORU_R01;
import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.helper.controller.Constants;
import de.imi.mopat.io.EncounterExporter;
import de.imi.mopat.io.EncounterExporterTemplate;
import de.imi.mopat.model.Configuration;
import de.imi.mopat.model.Encounter;
import de.imi.mopat.model.ExportTemplate;
import de.imi.mopat.model.enumeration.ExportStatus;
import java.io.File;
import java.io.FileInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
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
    private final ConfigurationDao configurationDao;
    private Document document;
    private Encounter encounter;
    private ExportTemplate exportTemplate;

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
        String sendingFacility = null;
        String receivingApplication = null;
        String receivingFacility = null;
        String obrFillerOrderNumber = null;
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
                    LOGGER.error("The port could not be converted to a number, as it was "
                        + configuration.getValue(), e);
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
            if (configuration.getAttribute().equals("sendingFacility")) {
                sendingFacility = configuration.getValue();
            }
            if (configuration.getAttribute().equals("receivingApplication")) {
                receivingApplication = configuration.getValue();
            }
            if (configuration.getAttribute().equals("receivingFacility")) {
                receivingFacility = configuration.getValue();
            }
            if (configuration.getAttribute().equals("OBRFillerOrderNumber")) {
                obrFillerOrderNumber = configuration.getValue();
            }
        }

        return doHandleExports(
            isExportInDirectory, exportPathDirectory, isExportServer, hostname,
            port, useTLS, useClientAuth, clientPKCSPath, clientPKCSPassword, serverCertificatePath,
            sendingFacility, receivingApplication, receivingFacility, obrFillerOrderNumber
        );
    }

    /**
     * Handles the export of data either to a file-based directory or to a server using the HL7
     * protocol. Constructs and sends an HL7 message based on provided parameters and export
     * configurations. Returns the status of the export operation.
     *
     * @param isExportInDirectory   Flag indicating if the export should be saved to a local
     *                              directory.
     * @param exportPathDirectory   Path to the directory where the export file should be saved.
     * @param isExportServer        Flag indicating if the export should be sent to a server.
     * @param hostname              The hostname of the destination server for the export.
     * @param port                  The port number to connect to on the export server.
     * @param useTLS                Flag indicating if TLS encryption should be used for the server
     *                              connection.
     * @param useClientAuth         Flag indicating if client-side authentication should be
     *                              utilized.
     * @param clientPKCSPath        Path to the client's PKCS#12 certificate file for
     *                              authentication.
     * @param clientPKCSPassword    Password for the client's PKCS#12 certificate file.
     * @param serverCertificatePath Path to the server's certificate for TLS validation.
     * @param sendingFacility       The identifier of the facility sending the message.
     * @param receivingApplication  Application identifier of the recipient.
     * @param receivingFacility     Identifier of the facility receiving the message.
     * @param obrFillerOrderNumber  Unique order number for the associated medical order.
     * @return An {@code ExportStatus} indicating the result of the export operation. Returns
     * {@code ExportStatus.SUCCESS} if the operation is successful, or {@code ExportStatus.FAILURE}
     * if an error occurs.
     * @throws Exception If an error occurs during message construction, file export, or server
     *                   communication.
     */
    private ExportStatus doHandleExports(
        Boolean isExportInDirectory, String exportPathDirectory, Boolean isExportServer,
        String hostname, Integer port, Boolean useTLS, Boolean useClientAuth,
        String clientPKCSPath, String clientPKCSPassword, String serverCertificatePath,
        String sendingFacility, String receivingApplication,
        String receivingFacility, String obrFillerOrderNumber
    ) throws Exception {
        HL7MessageHelper hl7MessageHelper = new HL7MessageHelper();

        //Properties have to be set, at least empty strings
        if (sendingFacility != null && receivingApplication != null
            && receivingFacility != null && obrFillerOrderNumber != null) {

            // Build Template specific message
            String output = buildHL7MessageContent();
            ORU_R01 hl7Message = hl7MessageHelper.createMessageWithBlob(
                exportTemplate, encounter, sendingFacility, receivingApplication,
                receivingFacility, obrFillerOrderNumber, output
            );
            hl7Message = hl7MessageHelper.overwriteMsh3NamespaceId(hl7Message,
                getNode("Formname").getTextContent());

            //Handle Server Export
            try {
                doHandleServerExport(isExportServer, hostname, port, useTLS, useClientAuth,
                    clientPKCSPath, clientPKCSPassword, serverCertificatePath, hl7Message);
            } catch (Exception e) {
                LOGGER.error("Could not send message via server.", e);
                return ExportStatus.FAILURE;
            }

            //Handle Filebased Export
            try {
                doHandleFilebasedExport(isExportInDirectory, exportPathDirectory, hl7Message);
            } catch (Exception e) {
                LOGGER.error("Could not export message to file.", e);
                return ExportStatus.FAILURE;
            }

            //Return Success if no exception was thrown
            return ExportStatus.SUCCESS;
        } else {
            LOGGER.error("Missing configuration for sendingFacility, receivingApplication, " +
                "receivingFacility or OBRFillerOrderNumber. Could not export message.");
            return ExportStatus.FAILURE;
        }
    }

    /**
     * Handles the export of an HL7 message to a server, optionally utilizing TLS and client
     * authentication for secure communication. The method performs the export only if the
     * {@code isExportServer} flag is set to true and valid server details (hostname and port) are
     * provided. In the case of TLS-secured communication, a keystore is built based on the provided
     * paths and authentication configuration.
     *
     * @param isExportServer        A Boolean flag indicating if the export should be performed to a
     *                              server. When set to true, the export process begins.
     * @param hostname              The hostname or IP address of the server to which the HL7
     *                              message will be exported. This should not be null or empty.
     * @param port                  The port number of the server to which the HL7 message will be
     *                              exported.
     * @param useTLS                A Boolean flag indicating whether TLS (Transport Layer Security)
     *                              should be used for secure communication with the server.
     * @param useClientAuth         A Boolean flag indicating if client-side authentication is to be
     *                              performed as part of the TLS handshake. This parameter is
     *                              relevant only if {@code useTLS} is set to true.
     * @param clientPKCSPath        The file path to the client PKCS archive, which contains client
     *                              certificates and keys. This parameter is required when
     *                              {@code useClientAuth} is set to true.
     * @param clientPKCSPassword    The password for accessing the client PKCS archive. This is
     *                              required when {@code useClientAuth} is true.
     * @param serverCertificatePath The file path to the server certificate. This is required when
     *                              {@code useTLS} is enabled, regardless of whether client
     *                              authentication is used.
     * @param hl7Message            The HL7 message object of type {@code ORU_R01} that will be
     *                              exported to the server.
     * @throws Exception If an error occurs during the export process, such as failure in message
     *                   transmission, keystore creation, or TLS setup.
     */
    private void doHandleServerExport(
        Boolean isExportServer, String hostname, Integer port, Boolean useTLS,
        Boolean useClientAuth, String clientPKCSPath, String clientPKCSPassword,
        String serverCertificatePath, ORU_R01 hl7Message
    ) throws Exception {
        HL7MessageHelper hl7MessageHelper = new HL7MessageHelper();
        if (Boolean.TRUE.equals(isExportServer) &&
            hostname != null && !hostname.isEmpty() && port != null
        ) {
            KeyStore keyStore = null;
            if (Boolean.TRUE.equals(useTLS)) {
                if (Boolean.TRUE.equals(useClientAuth)) {
                    keyStore = buildKeyStore(clientPKCSPath, clientPKCSPassword,
                        serverCertificatePath);
                } else {
                    keyStore = buildKeyStore(serverCertificatePath);
                }
            }

            hl7MessageHelper.sendMessageViaComServer(hostname, port, hl7Message, useTLS,
                keyStore, clientPKCSPassword);
        }
    }

    /**
     * Handles the file-based export of an HL7 message to the specified directory. If the export is
     * configured to be performed in a directory, this method encodes the HL7 message and writes it
     * to the designated export path directory.
     *
     * @param isExportInDirectory A Boolean flag indicating if the export should be performed within
     *                            a directory. If set to true, the export will proceed.
     * @param exportPathDirectory The path of the directory where the HL7 message will be exported.
     *                            This should be a valid directory path.
     * @param hl7Message          The HL7 message object (of type ORU_R01) that will be encoded and
     *                            exported.
     * @throws Exception If an error occurs during the export process, such as issues with encoding
     *                   the message or writing to the file system.
     */
    private void doHandleFilebasedExport(
        Boolean isExportInDirectory,
        String exportPathDirectory,
        ORU_R01 hl7Message
    ) throws Exception {
        if (Boolean.TRUE.equals(isExportInDirectory)) {
            // Make sure the path exists
            File path = new File(exportPathDirectory);
            if (!path.isDirectory()) {
                path.mkdirs();
            }
            //Create a sub-directory for the exported files
            File subDirectory = new File(
                exportPathDirectory + File.separator + exportTemplate.getQuestionnaire().getName()
                    .replaceAll(":", "_") + "/" + exportTemplate.getName().replaceAll(":", "_")
                    + "/");
            if (!subDirectory.isDirectory()) {
                subDirectory.mkdirs();
            }
            // Write to disk
            File exportFile = new File(subDirectory, this.createHL7FileName());
            FileUtils.writeStringToFile(exportFile, hl7Message.encode(), StandardCharsets.UTF_8);
            LOGGER.info("The hl7 message has been exported");
        }
    }

    /**
     * Builds the content of an HL7 message by transforming the current XML document into a string
     * and applying necessary formatting adjustments, such as removing XML declarations, replacing
     * spaces, and handling specific character sequences.
     *
     * @return The formatted string representation of the HL7 message content. Returns an empty
     * string if an exception occurs during the transformation process.
     */
    private String buildHL7MessageContent() {
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
            return output.replaceAll("_u0020", " ");
            //
        } catch (TransformerException exception) {
            LOGGER.error(exception.toString());
        }
        return "";
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
                + UNDERSCORE + Constants.EXPORT_DATE_FORMAT.format(new Date()) + DOT + HL7_SUFFIX;
        return result;
    }
}
