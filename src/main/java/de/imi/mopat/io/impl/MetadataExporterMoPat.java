package de.imi.mopat.io.impl;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.dao.ConfigurationGroupDao;
import de.imi.mopat.dao.ExportTemplateDao;
import de.imi.mopat.dao.QuestionDao;
import de.imi.mopat.dao.QuestionnaireDao;
import de.imi.mopat.dao.ScoreDao;
import de.imi.mopat.helper.controller.SliderIconDetailService;
import de.imi.mopat.helper.model.JSONHelper;
import de.imi.mopat.io.serializer.SliderIconConfigSerializer;
import de.imi.mopat.io.serializer.SliderIconSerializer;
import de.imi.mopat.model.SliderIcon;
import de.imi.mopat.model.SliderIconConfig;
import de.imi.mopat.model.dto.export.JsonQuestionnaireDTO;
import de.imi.mopat.io.MetadataExporter;
import de.imi.mopat.model.Questionnaire;

import java.nio.charset.Charset;

import org.springframework.context.MessageSource;

/**
 * An exporter for a JSON representation of the metadata of a
 * {@link Questionnaire} and everything that is associated with it.
 */
public class MetadataExporterMoPat implements MetadataExporter {
    private static final org.slf4j.Logger LOGGER =
            org.slf4j.LoggerFactory.getLogger(MetadataExporterMoPat.class);


    @Override
    public byte[] export(
            final Questionnaire questionnaire,
            final MessageSource messageSource,
            final ConfigurationDao configurationDao,
            final ConfigurationGroupDao configurationGroupDao,
            final ExportTemplateDao exportTemplateDao,
            final QuestionnaireDao questionnaireDao,
            final QuestionDao questionDao,
            final ScoreDao scoreDao,
            final SliderIconDetailService sliderIconDetailService) {

        String jsonQuestionnaire = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            // Enable pretty print
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            // Deactivate serialization of null or empty properties
            objectMapper.setSerializationInclusion(
                    JsonInclude.Include.NON_DEFAULT);
            SimpleModule module = new SimpleModule();
            module.addSerializer(SliderIcon.class, new SliderIconSerializer());
            module.addSerializer(SliderIconConfig.class, new SliderIconConfigSerializer(configurationDao));
            objectMapper.registerModule(module);
            // Generate DTO from model

            JsonQuestionnaireDTO jsonQuestionnaireDTO =
                    new JsonQuestionnaireDTO();
            JSONHelper jsonHelper = new JSONHelper(configurationDao, sliderIconDetailService);
            jsonHelper.initializeJsonQuestionnaireDTO(jsonQuestionnaireDTO, questionnaire);
            jsonQuestionnaire =
                    objectMapper.writeValueAsString(jsonQuestionnaireDTO);
            return jsonQuestionnaire.getBytes(Charset.forName("UTF-8"));
        } catch (JsonProcessingException e) {
            LOGGER.info(
                    "ERROR: Parsing questionnaire to JSON String failed. "
                            + "Following error occurred: {}",
                    e.getMessage());
        }

        return new byte[0];
    }
}
