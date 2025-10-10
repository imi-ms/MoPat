package de.imi.mopat.io.impl;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.dao.ConfigurationGroupDao;
import de.imi.mopat.dao.ExportTemplateDao;
import de.imi.mopat.dao.QuestionDao;
import de.imi.mopat.dao.QuestionnaireDao;
import de.imi.mopat.dao.ScoreDao;
import de.imi.mopat.helper.model.JSONHelper;
import de.imi.mopat.io.MetadataExporter;
import de.imi.mopat.io.serializer.SliderIconSerializer;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.SliderIcon;
import de.imi.mopat.model.dto.export.JsonCompleteQuestionnaireDTO;
import java.nio.charset.Charset;
import org.springframework.context.MessageSource;

/**
 * An exporter for a JSON representation of the metadata of a
 * {@link Questionnaire} and everything that is associated with it.
 */
public class MetadataExporterMoPatComplete implements MetadataExporter {

    private static final org.slf4j.Logger LOGGER =
        org.slf4j.LoggerFactory.getLogger(MetadataExporterMoPatComplete.class);

    @Override
    public byte[] export(Questionnaire questionnaire, MessageSource messageSource,
        ConfigurationDao configurationDao, ConfigurationGroupDao configurationGroupDao,
        ExportTemplateDao exportTemplateDao, QuestionnaireDao questionnaireDao,
        QuestionDao questionDao, ScoreDao scoreDao) {


        String jsonString = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
            SimpleModule module = new SimpleModule();
            module.addSerializer(SliderIcon.class, new SliderIconSerializer());
            objectMapper.registerModule(module);

            // Generate DTO from model
            JsonCompleteQuestionnaireDTO jsonCompleteQuestionnaireDTO = new JsonCompleteQuestionnaireDTO();
            JSONHelper jsonHelper = new JSONHelper();
            jsonHelper.initializeJsonQuestionnaireDTO(jsonCompleteQuestionnaireDTO, questionnaire, configurationDao);

            jsonHelper.initializeJsonExportTemplateDTO(jsonCompleteQuestionnaireDTO, questionnaire, configurationDao);

            jsonString = objectMapper.writeValueAsString(jsonCompleteQuestionnaireDTO);

        } catch (JsonProcessingException e) {
            LOGGER.info(
                "ERROR: Parsing questionnaire to JSON String failed. "
                    + "Following error occurred: {}",
                e.getMessage());
        }

        return jsonString.getBytes(Charset.forName("UTF-8"));
    }
}
