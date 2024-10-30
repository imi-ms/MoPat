package de.imi.mopat.helper.model;

import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.helper.controller.StringUtilities;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.dto.QuestionnaireDTO;
import de.imi.mopat.model.dto.QuestionnaireVersionGroupDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;
import java.util.function.Function;

@Component
public class QuestionnaireDTOMapper implements Function<Questionnaire, QuestionnaireDTO> {

    private static final org.slf4j.Logger LOGGER =
            org.slf4j.LoggerFactory.getLogger(QuestionnaireDTOMapper.class);

    private final ConfigurationDao configurationDao;

    private final QuestionDTOMapper questionDTOMapper;

    @Autowired
    public QuestionnaireDTOMapper(ConfigurationDao configurationDao, QuestionDTOMapper questionDTOMapper) {
        this.configurationDao = configurationDao;
        this.questionDTOMapper = questionDTOMapper;
    }

    @Override
    public QuestionnaireDTO apply(Questionnaire questionnaire) {
        return applyWithGroup(questionnaire, true);
    }

    public QuestionnaireDTO applyWithoutGroup(Questionnaire questionnaire) {
        return applyWithGroup(questionnaire, false);
    }

    private QuestionnaireDTO applyWithGroup(Questionnaire questionnaire, boolean includeGroup) {
        QuestionnaireDTO questionnaireDTO = basicApply(questionnaire);
        if (includeGroup && questionnaire.getQuestionnaireVersionGroup() != null) {
            QuestionnaireVersionGroupDTO groupDTO = new QuestionnaireVersionGroupDTO();
            groupDTO.setGroupId(questionnaire.getQuestionnaireVersionGroup().getId());
            groupDTO.setGroupName(questionnaire.getQuestionnaireVersionGroup().getName());
            Set<Questionnaire> questionnaires = questionnaire.getQuestionnaireVersionGroup().getQuestionnaires();
            List<QuestionnaireDTO> groupQuestionnaireDTOs = new ArrayList<>();
            
            for(Questionnaire nestedQuestionnaire : questionnaires) {
                groupQuestionnaireDTOs.add(applyWithGroup(nestedQuestionnaire, false));
            }
            
            groupDTO.setQuestionnaireDTOS(groupQuestionnaireDTOs);
            questionnaireDTO.setQuestionnaireGroupDTO(groupDTO);
        }
        return questionnaireDTO;
    }

    public QuestionnaireDTO basicApply(Questionnaire questionnaire) {
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
        
        Map<String, String> localizedWelcomeMap = questionnaire.getLocalizedWelcomeText();
        if (localizedWelcomeMap != null) {
            questionnaireDTO.setLocalizedWelcomeText(new TreeMap<>(localizedWelcomeMap));
        }
        
        Map<String, String> localizedFinalMap = questionnaire.getLocalizedFinalText();
        if (localizedFinalMap != null) {
            questionnaireDTO.setLocalizedFinalText(new TreeMap<>(localizedFinalMap));
        }
        
        Map<String, String> localizedDisplayMap = questionnaire.getLocalizedDisplayName();
        if (localizedDisplayMap != null) {
            questionnaireDTO.setLocalizedDisplayName(new TreeMap<>(localizedDisplayMap));
        }
        
        
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