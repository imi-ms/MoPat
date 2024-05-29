package de.imi.mopat.io.impl;

import com.google.common.io.Files;
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
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 */
public class EncounterExporterTemplateOrbis implements EncounterExporterTemplate {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(
        EncounterExporterTemplateOrbis.class);

    private static final String TRUE = "TRUE";
    private static final String FALSE = "FALSE";
    private static final String XML_SUFFIX = "xml";
    private static final String DOT = ".";
    private static final String UNDERSCORE = "_";
    private static final String TODO = "TODO";
    private static final String IMPORT = "Import";
    private static final SimpleDateFormat orbisXMLFileNameDateFormat = new SimpleDateFormat(
        "dd.MM.yyyy_HH.mm.ss");

    // Initialize every needed configuration information as a final string
    private final String exportPathProperty = "exportPath";

    private Document doc;
    private Encounter encounter;
    private ExportTemplate exportTemplate;
    private final ConfigurationDao configurationDao;

    /**
     * Constructor with given {@link ConfigurationDao} to get configuration informations within this
     * instance.
     *
     * @param configurationDao The {@link ConfigurationDao} from the context.
     */
    public EncounterExporterTemplateOrbis(final ConfigurationDao configurationDao) {
        this.configurationDao = configurationDao;
    }

    @Override
    public void load(final Encounter encounter, final ExportTemplate exportTemplate)
        throws Exception {
        this.encounter = encounter;
        this.exportTemplate = exportTemplate;

        String objectStoragePath = configurationDao.getObjectStoragePath();
        if (objectStoragePath == null) {
            LOGGER.error("[SETUP] No object storage path found. Please provide a "
                    + "value for {} in the {} file", Constants.OBJECT_STORAGE_PATH_PROPERTY,
                Constants.CONFIGURATION);
        } else {
            LOGGER.info("[SETUP] Object storage path configuration found.");
        }
        LOGGER.info(
            "[SETUP] Accessing properties file to look up the export path" + " in  {}...[DONE]",
            Constants.CONFIGURATION);

        String templatePath = objectStoragePath + Constants.EXPORT_TEMPLATE_SUB_DIRECTORY;
        String filename = exportTemplate.getFilename();
        File file = new File(templatePath, filename);

        // This code is adapted from http://www.mkyong.com/java/how-to-read-xml-file-in-java-dom-parser/
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        dBuilder = dbFactory.newDocumentBuilder();
        // Load inputStream into w3c Document object..
        doc = dBuilder.parse(new FileInputStream(file));
        doc.setXmlStandalone(true);
    }

    @Override
    public void write(final String exportField, String value) throws Exception {
        Node node = getNode(exportField);
        if (value != null && !value.isEmpty()) {
            // replace boolean placeholder values with orbis specific values
            if (value.equals(EncounterExporter.TRUE)) {
                value = TRUE;
            } else if (value.equals(EncounterExporter.FALSE)) {
                value = FALSE;
            }
            node.setTextContent(value);
        }
    }

    @Override
    public ExportStatus flush() throws Exception {
        // write the content into xml file,
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
        DOMSource source = new DOMSource(doc);

        String exportPath = null;
        //Exported once for each configuration
        for (Configuration configuration : exportTemplate.getConfigurationGroup()
            .getConfigurations()) {
            if (configuration.getAttribute().equals(exportPathProperty)) {
                exportPath = configuration.getValue();
            }
            String todoPath = exportPath + File.separator + TODO;
            String importPath = exportPath + File.separator + IMPORT;
            // use the "Formname" content text for the directory
            // if no content text exists use the original filename of the
            // export template
            // this CAN be removed when an Orbis template validator exists
            try {
                Node formname = getNode("Formname");
                if (formname.getTextContent().isEmpty()) {
                    throw new SAXException("node is empty");
                }
                importPath += File.separator + formname.getTextContent().replace(" ", "_");
            } catch (SAXException sexe) {
                LOGGER.info("[WARNING] Could not find a value for the node "
                        + "Formname of the orbis export template with"
                        + " id '{}'; will take the original filename " + "instead. Exception: {}",
                    this.exportTemplate.getId(), sexe);
                importPath +=
                    File.separator + exportTemplate.getOriginalFilename().replace(" ", "_");
            }
            String orbisXMLFileName = createOrbisXMLFileName();

            // make sure the directories exist
            File todoExportPath = new File(todoPath);
            if (!todoExportPath.isDirectory()) {
                todoExportPath.mkdirs();
            }
            File importExportPath = new File(importPath);
            if (!importExportPath.isDirectory()) {
                importExportPath.mkdirs();
            }

            File todoExportFile = new File(todoPath, orbisXMLFileName);
            File importExportFile = new File(importPath, orbisXMLFileName);
            importExportFile.createNewFile();
            URI fileURI = importExportFile.toURI();
            StreamResult result = new StreamResult(fileURI.getPath());
            // write to disk
            transformer.transform(source, result);
            // copy from import folder to todo folder
            Files.copy(importExportFile, todoExportFile);
        }

        return ExportStatus.SUCCESS;
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
        NodeList nodeList = doc.getElementsByTagName(name);
        if (nodeList.getLength() == 0) {
            throw new SAXException("node does not exist");
        } else if (nodeList.getLength() > 1) {
            throw new SAXException("more than one node with the name found");
        }
        return nodeList.item(0);
    }

    /**
     * Creates a unique Orbis XML Filename.
     *
     * @return The newly created unique Orbis XML Filename.
     */
    private String createOrbisXMLFileName() {
        String result =
            encounter.getCaseNumber() + UNDERSCORE + exportTemplate.getOriginalFilename()
                + UNDERSCORE + orbisXMLFileNameDateFormat.format(new Date()) + DOT + XML_SUFFIX;
        return result;
    }
}
