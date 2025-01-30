package de.imi.mopat.io.impl;

import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.dao.ConfigurationGroupDao;
import de.imi.mopat.dao.ExportTemplateDao;
import de.imi.mopat.dao.QuestionDao;
import de.imi.mopat.dao.QuestionnaireDao;
import de.imi.mopat.dao.ScoreDao;
import de.imi.mopat.io.MetadataExporter;
import de.imi.mopat.model.Questionnaire;
import jakarta.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * An exporter for a PDF representation of the metadata of a {@link Questionnaire}.
 */
@Service
public class MetadataExporterPDF implements MetadataExporter {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(
        MetadataExporterPDF.class);

    @Override
    public byte[] export(final Questionnaire questionnaire, final MessageSource messageSource,
        final ConfigurationDao configurationDao, final ConfigurationGroupDao configurationGroupDao,
        final ExportTemplateDao exportTemplateDao, final QuestionnaireDao questionnaireDao,
        final QuestionDao questionDao, final ScoreDao scoreDao) {

        // Get the ODM metadata representation
        MetadataExporterODM odmExporter = new MetadataExporterODM();
        byte[] odmByteArray = odmExporter.export(questionnaire, messageSource, configurationDao,
            configurationGroupDao, exportTemplateDao, questionnaireDao, questionDao, scoreDao);

        // Send this representation to the ODMToPDF converter and return the
        // received byte array
        HttpEntity entity = MultipartEntityBuilder.create()
            .addBinaryBody("odmFile", odmByteArray, ContentType.MULTIPART_FORM_DATA, "odmFile")
            .addBinaryBody("logoFile",
                new File((configurationDao.getWebappRootPath() + "/images/logo.png"))).build();

        String connectionUrl = configurationDao.getMetadataExporterPDF();
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost request = new HttpPost(connectionUrl);
        request.setEntity(entity);
        try {
            HttpResponse httpResponse = httpClient.execute(request);
            InputStream connectionResponse = httpResponse.getEntity().getContent();
            return IOUtils.toByteArray(connectionResponse);
        } catch (IOException exception) {
            LOGGER.error("Error while connecting to the ODMToPDF server: {}",
                exception.getLocalizedMessage());
        }

        return new byte[0];
    }
}
