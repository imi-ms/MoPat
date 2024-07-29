package de.imi.mopat.helper.model;

import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.helper.controller.StringUtilities;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.dto.QuestionnaireDTO;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.TreeMap;
import java.util.function.Function;

@Component
public class QuestionnaireDTOMapper implements Function<Questionnaire, QuestionnaireDTO> {

    private static final org.slf4j.Logger LOGGER =
            org.slf4j.LoggerFactory.getLogger(QuestionnaireDTOMapper.class);

    private final ConfigurationDao configurationDao;

    private final QuestionDTOMapper questionDTOMapper;

    public QuestionnaireDTOMapper(ConfigurationDao configurationDao, QuestionDTOMapper questionDTOMapper) {
        this.configurationDao = configurationDao;
        this.questionDTOMapper = questionDTOMapper;
    }

    @Override
    public QuestionnaireDTO apply(Questionnaire questionnaire) {
        QuestionnaireDTO questionnaireDTO = new QuestionnaireDTO();
        String logoBase64 = null;
        if (questionnaire.getLogo() != null) {
            String fileName = questionnaire.getLogo();
            String realPath = configurationDao.getImageUploadPath() + "/questionnaire/" + questionnaire.getId() + "/" + fileName;
            try {
                logoBase64 = StringUtilities.convertImageToBase64String(realPath, fileName);
            } catch (IOException e) {
                LOGGER.error("Error converting logo image to base64 for questionnaire with id {}: {}", questionnaire.getId(), e.getMessage());
            }
        }

        questionnaireDTO.setId(questionnaire.getId());
        questionnaireDTO.setName(questionnaire.getName());
        questionnaireDTO.setDescription(questionnaire.getDescription());
        questionnaireDTO.setVersion(questionnaire.getVersion());
        questionnaireDTO.setLocalizedWelcomeText(new TreeMap<>(questionnaire.getLocalizedWelcomeText()));
        questionnaireDTO.setLocalizedFinalText(new TreeMap<>(questionnaire.getLocalizedFinalText()));
        questionnaireDTO.setLocalizedDisplayName(new TreeMap<>(questionnaire.getLocalizedDisplayName()));
        questionnaireDTO.setLogo(questionnaire.getLogo());
        questionnaireDTO.setLogoBase64(logoBase64);
        questionnaireDTO.setExportTemplates(questionnaire.getExportTemplates());
        questionnaireDTO.setQuestionDTOs(
                questionnaire.getQuestions()
                        .stream()
                        .map(questionDTOMapper)
                        .toList()
        );
        return questionnaireDTO;
    }
}

