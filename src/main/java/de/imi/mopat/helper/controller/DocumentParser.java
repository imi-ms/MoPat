package de.imi.mopat.helper.controller;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Hardened Document reader to handle document
 * parsing safely and prevent ssrf attacks
 */
public class DocumentParser {

    private DocumentBuilder documentBuilder;

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(DocumentParser.class);

    public DocumentParser() {
        try {
            this.init();
        } catch (ParserConfigurationException ex) {
            LOGGER.error("Could not initialize DocumentReader", ex);
        }
    }

    /**
     * Initializes a secure XML {@link DocumentBuilder}.
     *
     * <p>Configures the parser to be namespace-aware and disables DOCTYPE declarations,
     * external entities, external DTD loading, XInclude, and access to external DTDs
     * and schemas to reduce XXE-related risks.
     *
     * @throws ParserConfigurationException if the builder cannot be created or a
     *         feature is unsupported
     */
    private void init() throws ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        documentBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        documentBuilderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        documentBuilderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        documentBuilderFactory.setXIncludeAware(false);
        documentBuilderFactory.setExpandEntityReferences(false);
        documentBuilderFactory.setAttribute(javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD, "");
        documentBuilderFactory.setAttribute(javax.xml.XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

        this.documentBuilder = documentBuilderFactory.newDocumentBuilder();
    }

    /**
     * Helper function that parses a file with the configured DocumentBuilder
     * @param file to be parsed
     * @return Document
     * @throws IOException if file cannot be read
     * @throws SAXException if parsing fails
     */
    public Document parse(MultipartFile file) throws IOException, SAXException {
        return this.documentBuilder.parse(file.getInputStream());
    }


    /**
     * Helper function that parses a FileInputStream with the configured DocumentBuilder
     * @param fileInputStream to be parsed
     * @return Document
     * @throws IOException if FileInputStream cannot be read
     * @throws SAXException if parsing fails
     */
    public Document parse(InputStream fileInputStream) throws IOException, SAXException {
        return this.documentBuilder.parse(fileInputStream);
    }

    /**
     * Creates a new Document and returns it
     * @return Document
     */
    public Document newDocument() {
        return this.documentBuilder.newDocument();
    }

}
