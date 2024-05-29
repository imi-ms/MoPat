package de.imi.mopat.helper.controller;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.model.Answer;
import de.imi.mopat.model.BodyPartAnswer;
import de.imi.mopat.model.DateAnswer;
import de.imi.mopat.model.ImageAnswer;
import de.imi.mopat.model.NumberInputAnswer;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.SelectAnswer;
import de.imi.mopat.model.SliderAnswer;
import de.imi.mopat.model.SliderFreetextAnswer;
import de.imi.mopat.model.SliderIcon;
import de.imi.mopat.model.conditions.Condition;
import de.imi.mopat.model.dto.AnswerDTO;
import de.imi.mopat.model.dto.ConditionDTO;
import de.imi.mopat.model.dto.QuestionDTO;
import de.imi.mopat.model.dto.QuestionnaireDTO;
import de.imi.mopat.model.dto.export.SliderIconDTO;
import de.imi.mopat.model.enumeration.BodyPart;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.SortedMap;
import java.util.TreeMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QuestionnaireService {

    private static final org.slf4j.Logger LOGGER =
        org.slf4j.LoggerFactory.getLogger(Question.class);

    @Autowired
    private QuestionService questionService;

    @Autowired
    private ConfigurationDao configurationDao;

    /**
     * Converts this {@link Questionnaire} object to an
     * {@link QuestionnaireDTO} object.
     *
     * @return An {@link QuestionnaireDTO} object based on this
     * {@link Questionnaire} object.
     */
    public QuestionnaireDTO toQuestionnaireDTO(Questionnaire questionnaire) {
        QuestionnaireDTO questionnaireDTO = new QuestionnaireDTO();
        questionnaireDTO.setDescription(questionnaire.getDescription());
        questionnaireDTO.setLocalizedFinalText(new TreeMap<>(questionnaire.getLocalizedFinalText()));
        questionnaireDTO.setId(questionnaire.getId());
        questionnaireDTO.setName(questionnaire.getName());
        questionnaireDTO.setLocalizedWelcomeText(new TreeMap<>(questionnaire.getLocalizedWelcomeText()));
        questionnaireDTO.setLocalizedDisplayName(new TreeMap<>(questionnaire.getLocalizedDisplayName()));
        questionnaireDTO.setExportTemplates(questionnaire.getExportTemplates());
        try{
            String fileName = questionnaire.getLogo();
            if(fileName != null){
                String realPath = configurationDao.getImageUploadPath() + "/questionnaire/"+ questionnaire.getId()+"/"+ questionnaire.getLogo();
                questionnaireDTO.setLogoBase64(StringUtilities.convertImageToBase64String(realPath, fileName));
            }
        } catch (IOException e) {
        }

        List<QuestionDTO> questionDTOs = new ArrayList<>();
        Iterator<Question> questionIterator = questionnaire.getQuestions()
            .iterator();
        while (questionIterator.hasNext()) {
            QuestionDTO questionDTO = questionService.toQuestionDTO(questionIterator.next());
            questionDTOs.add(questionDTO);
        }

        questionnaireDTO.setQuestionDTOs(questionDTOs);
        questionnaireDTO.setLogo(questionnaire.getLogo());

        return questionnaireDTO;
    }

}
