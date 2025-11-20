package de.imi.mopat.io.impl;

import de.imi.mopat.io.ExportTemplateImporter;
import de.imi.mopat.model.Encounter;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This is an instance of {@link ExportTemplateImporterHL7} and represents the implementation for
 * the KIS "ORBIS". It is designed to load an XML file according to the structure provided by ORBIS.
 * See {@link ExportTemplateImporter#importFile(InputStream)} for further explanation.
 */
@Service
public class ExportTemplateImporterHL7 implements ExportTemplateImporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExportTemplateImporterHL7.class);

    private final String[] ignoreTagsArray = {"Document", "Instance", "Formname"};
    private final List<String> ignoreTags;

    /**
     * Constructor only initializes the ignore list for tags.
     */
    public ExportTemplateImporterHL7() {
        // create a list of all tags which should be ignored for the import of
        // the export template file.
        this.ignoreTags = Arrays.asList(ignoreTagsArray);
    }

    /**
     * This method builds a {@link List} based on the template provided by "ORBIS". The following
     * informations have to be inside the List: <br>
     * <code>Formname</code> which represents the questionnaire name given by
     * ORBIS. Add all tags to the List. They usually look like this
     * <code>cbx1Gut</code>. See {@link Encounter} for information.
     *
     * @param inputStream {@link InputStream} containing the uploaded file from
     *                    {@link
     *                    de.imi.mopat.controller.ExportMappingController#handleUpload(java.lang.Long,
     *                    java.lang.String, org.springframework.web.multipart.MultipartFile,
     *                    java.lang.String, de.imi.mopat.model.ExportTemplate,
     *                    org.springframework.validation.BindingResult,
     *                    jakarta.servlet.http.HttpServletRequest, org.springframework.ui.Model) }.
     *                    Must not be <code>null</code>.
     * @return A list of strings which represents the necessary information for given KIS.
     * @throws ParserConfigurationException If something went wrong with parser configuration.
     * @throws SAXException                 If given file was a XML file and something during it's
     *                                      processing went wrong.
     * @throws IOException                  If given file couldn't be loaded and processed.
     */
    @Override
    public List<String> importFile(InputStream inputStream)
        throws IOException, SAXException, ParserConfigurationException {
        List<String> xmlDoc = new ArrayList<String>();
        try {
            // This code is adapted from http://www.mkyong.com/java/how-to-read-xml-file-in-java-dom-parser/
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder;
            dBuilder = dbFactory.newDocumentBuilder();

            // Load inputStream into w3c Document object..
            Document doc = dBuilder.parse(inputStream);
            doc.getDocumentElement().normalize();

            // iterate over its content
            NodeList nodeList = doc.getElementsByTagName("*");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    // only add if the tag is not ignored
                    if (!ignoreTags.contains(node.getNodeName())) {
                        xmlDoc.add(node.getNodeName());
                    }
                }
            }

        } catch (ParserConfigurationException | SAXException | IOException e) {
            LOGGER.error("Loading the XML-File failed because of {}", e);
            throw e;
        }
        return xmlDoc;
    }
}
