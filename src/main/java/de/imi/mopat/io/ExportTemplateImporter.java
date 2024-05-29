package de.imi.mopat.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 * This abstract class describes the necessary method needed to import a given template file from
 * {@link de.imi.mopat.model.ExportTemplate}. The {@link InputStream} is provided by
 * {@link de.imi.mopat.controller.ExportMappingController} and represents an export template for a
 * questionnaire.
 * <p>
 * See {@link de.imi.mopat.io.impl.ExportTemplateImporterOrbis} for reference.
 */
public interface ExportTemplateImporter {

    /**
     * This method processes the given {@link InputStream} to a list of strings.
     * <p>
     * See {@link de.imi.mopat.io.impl.ExportTemplateImporterOrbis} for reference.
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
     * @throws SAXException                 If given file was a XML file and something during its
     *                                      processing went wrong.
     * @throws IOException                  If given file couldn't be loaded and processed.
     */
    List<String> importFile(InputStream inputStream)
        throws IOException, SAXException, ParserConfigurationException;
}
