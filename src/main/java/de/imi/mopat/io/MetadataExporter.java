package de.imi.mopat.io;

import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.dao.ConfigurationGroupDao;
import de.imi.mopat.dao.ExportTemplateDao;
import de.imi.mopat.dao.QuestionDao;
import de.imi.mopat.dao.QuestionnaireDao;
import de.imi.mopat.dao.ScoreDao;
import de.imi.mopat.helper.controller.SliderIconDetailService;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.enumeration.QuestionType;
import org.springframework.context.MessageSource;

/**
 * This Interface provides methods to export the metadata from an {@link Questionnaire} and its
 * Questions.
 */
public interface MetadataExporter {

    /**
     * Returns a byte array representation of the given {@link Questionnaire}.
     *
     * @param questionnaire         The {@link Questionnaire} to export
     * @param messageSource         {@link MessageSource} to get the descriptions for the variant
     *                              {@link QuestionType QuestionTypes}
     * @param configurationDao      {@link ConfigurationDao} to get information for the export
     * @param configurationGroupDao {@link ConfigurationGroupDao} to get information for storing and
     *                              mapping of the export template
     * @param exportTemplateDao     {@link ExportTemplateDao} to get information for storing and
     *                              mapping of the export template
     * @param questionnaireDao      {@link QuestionnaireDao} to merge the questionnaire
     * @param questionDao           {@link QuestionDao} to get information about the question for
     *                              storing and mapping of the export template
     * @param scoreDao              {@link ScoreDao} to get information about the scores for storing
     *                              and mapping of the export template
     * @return A byte array representation of the given {@link Questionnaire}
     */
    byte[] export(Questionnaire questionnaire, MessageSource messageSource,
                  ConfigurationDao configurationDao, ConfigurationGroupDao configurationGroupDao,
                  ExportTemplateDao exportTemplateDao, QuestionnaireDao questionnaireDao,
                  QuestionDao questionDao, ScoreDao scoreDao, SliderIconDetailService sliderIconDetailService);
}
