package de.imi.mopat.io.importer.odm;

import de.unimuenster.imi.org.cdisc.odm.v132.ODM;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Custom Bean to handle reading of ODM files since JAXB.unmarshal(...) is not able to convert
 * complex data types anymore
 */
@Component
public class ODMProcessingBean {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(
        ODMProcessingBean.class);
    private final String namespace = "http://www.cdisc.org/ns/odm/v1.3";
    private final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    private Unmarshaller unmarshaller;
    private Marshaller marshaller;
    private DocumentBuilder documentBuilder;

    /**
     * Initialize Bean with unmarshaller for ODM class
     */
    public ODMProcessingBean() {
        try {
            documentBuilderFactory.setNamespaceAware(true);
            this.documentBuilder = documentBuilderFactory.newDocumentBuilder();

            JAXBContext context = JAXBContext.newInstance(ODM.class);

            unmarshaller = context.createUnmarshaller();

            marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        } catch (Exception e) {
            LOGGER.error("Could not create ODM processor: ", e);
        }
    }

    /**
     * Method to read in an ODM file by converting the input stream to ODM data class
     *
     * @param inputStream of the ODM file
     * @return ODM object
     */
    public ODM unmarshal(InputStream inputStream) {
        try {
            InputStream namespaceInjectedStream = addNamespace(inputStream);
            return (ODM) unmarshaller.unmarshal(namespaceInjectedStream);
        } catch (Exception e) {
            LOGGER.error("Error reading in ODM file: ", e);
            return null;
        }
    }

    public void marshal(ODM odm, OutputStream outputStream) {
        try {
            marshaller.marshal(odm, outputStream);
        } catch (Exception e) {
            LOGGER.error("Could not marshal ODM file: " + e);
        }
    }

    public InputStream marshal(ODM odm) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            marshaller.marshal(odm, outputStream);
        } catch (Exception e) {
            LOGGER.error("Could not marshal ODM file: " + e);
        }
        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    public void marshal(ODM odm, File file) {
        FileOutputStream outputStream;
        try {
            outputStream = new FileOutputStream(file);
            marshaller.marshal(odm, outputStream);
        } catch (Exception e) {
            LOGGER.error("Could not marshal ODM file: " + e);
        }
    }

    /**
     * Helper method to add the ODM namespace to a document because the ODM data classes are only
     * able to process files with a namespace, but files without a namespace are also valid.
     *
     * @param odmInputStream The input Stream of the ODM file
     * @return a copied InputStream with an added namespace
     */
    private InputStream addNamespace(InputStream odmInputStream) {
        Document document = parseODMFile(odmInputStream);
        Document newDocument = documentBuilder.newDocument(); // Create a new document for the new namespace

        // Retrieve the root element from the original document
        Element oldRoot = document.getDocumentElement();

        // Create a new root element with the new namespace
        Element newRoot = newDocument.createElementNS(this.namespace, oldRoot.getNodeName());
        newDocument.appendChild(newRoot);

        // Copy attributes to the new root element, if needed
        if (oldRoot.hasAttributes()) {
            for (int i = 0; i < oldRoot.getAttributes().getLength(); i++) {
                Node attr = oldRoot.getAttributes().item(i);
                newRoot.setAttribute(attr.getNodeName(), attr.getNodeValue());
            }
        }

        this.copyChildren(oldRoot, newRoot, this.namespace);

        return getDocumentAsStream(newDocument);
    }

    /**
     * Helper function to parse InputStream as a Document instance
     *
     * @param odmFileInputStream InputStream of the ODM file
     * @return Document object
     */
    private Document parseODMFile(InputStream odmFileInputStream) {
        Document doc = null;
        try {
            doc = documentBuilder.parse(odmFileInputStream);
        } catch (Exception ex) {

        }
        return doc;
    }

    /**
     * Helper function to transform a Document Object back to ODM
     *
     * @param doc Document object
     * @return InputStream of Document
     */
    private InputStream getDocumentAsStream(Document doc) {
        try {
            DOMSource domSource = new DOMSource(doc);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.transform(domSource, result);
            return new ByteArrayInputStream(writer.toString().getBytes(StandardCharsets.UTF_8));
        } catch (TransformerException ex) {
            return null;
        }
    }

    /**
     * Helper function to recursively copy all children elements from a source element to a target
     * element with a given namespae
     *
     * @param source       The source document
     * @param target       The target
     * @param namespaceURI
     */
    private void copyChildren(Element source, Element target, String namespaceURI) {
        for (Node node = source.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node instanceof Element sourceElement) {
                // Create elements with the new namespace and copy all children
                Element targetElement = target.getOwnerDocument()
                    .createElementNS(namespaceURI, sourceElement.getNodeName());
                target.appendChild(targetElement);

                if (sourceElement.hasAttributes()) {
                    for (int i = 0; i < sourceElement.getAttributes().getLength(); i++) {
                        Node attr = sourceElement.getAttributes().item(i);
                        targetElement.setAttribute(attr.getNodeName(), attr.getNodeValue());
                    }
                }

                // Recursive copy
                copyChildren(sourceElement, targetElement, namespaceURI);
            } else if (node.getNodeType() == Node.TEXT_NODE) {
                target.appendChild(target.getOwnerDocument().importNode(node, true));
            }
        }
    }

}
