package de.imi.mopat.helper.controller;

import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.dao.QuestionnaireDao;
import de.imi.mopat.dao.QuestionnaireVersionDao;
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
import de.imi.mopat.model.QuestionnaireVersion;
import de.imi.mopat.model.dto.export.SliderIconDTO;
import de.imi.mopat.model.enumeration.BodyPart;

import java.awt.image.BufferedImage;
import java.io.File;
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

import de.imi.mopat.validator.QuestionnaireDTOValidator;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;

@Service
public class QuestionnaireService {

    private static final org.slf4j.Logger LOGGER =
        org.slf4j.LoggerFactory.getLogger(Question.class);

    @Autowired
    private QuestionService questionService;

    @Autowired
    private ConfigurationDao configurationDao;

    @Autowired
    private QuestionnaireDao questionnaireDao;

    @Autowired
    private QuestionnaireDTOValidator questionnaireDTOValidator;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private QuestionnaireVersionDao questionnaireVersionDao;

    @Autowired
    @Qualifier("MoPat")
    private PlatformTransactionManager transactionManager;

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

    public void processLocalizedText(QuestionnaireDTO questionnaireDTO) {
        // Verarbeitung des Willkommenstextes
        SortedMap<String, String> tempLocalizedWelcomeText = questionnaireDTO.getLocalizedWelcomeText();
        for (SortedMap.Entry<String, String> entry : tempLocalizedWelcomeText.entrySet()) {
            if (entry.getValue().equals("<p><br></p>") || entry.getValue().equals("<br>")) {
                entry.setValue("");
            }
        }
        questionnaireDTO.setLocalizedWelcomeText(tempLocalizedWelcomeText);

        // Verarbeitung des Abschlusstextes
        SortedMap<String, String> tempLocalizedFinalText = questionnaireDTO.getLocalizedFinalText();
        for (SortedMap.Entry<String, String> entry : tempLocalizedFinalText.entrySet()) {
            if (entry.getValue().equals("<p><br></p>") || entry.getValue().equals("<br>")) {
                entry.setValue("");
            }
        }
        questionnaireDTO.setLocalizedFinalText(tempLocalizedFinalText);
    }

    public void validateQuestionnaire(QuestionnaireDTO questionnaireDTO, BindingResult result) {
        questionnaireDTOValidator.validate(questionnaireDTO, result);
    }

    @Transactional(transactionManager = "MoPat")
    public Questionnaire saveOrUpdateQuestionnaire(QuestionnaireDTO questionnaireDTO, MultipartFile logo, Long userId) {
        Questionnaire newQuestionnaire;
        if (questionnaireDTO.getId() != null) {
            Questionnaire existingQuestionnaire = questionnaireDao.getElementById(questionnaireDTO.getId());
            newQuestionnaire = existingQuestionnaire.deepCopy();
            newQuestionnaire.setVersion(existingQuestionnaire.getVersion() + 1);
            newQuestionnaire.setChangedBy(userId);

            // Save the versioning information
            QuestionnaireVersion version = new QuestionnaireVersion();
            version.setCurrentQuestionnaire(newQuestionnaire);
            version.setPreviousQuestionnaire(existingQuestionnaire);
            questionnaireVersionDao.merge(version);
        } else {
            newQuestionnaire = new Questionnaire(questionnaireDTO.getName(),
                    questionnaireDTO.getDescription(), userId, Boolean.TRUE);
            newQuestionnaire.setCreatedBy(userId);
        }

        newQuestionnaire.setLocalizedWelcomeText(questionnaireDTO.getLocalizedWelcomeText());
        newQuestionnaire.setLocalizedFinalText(questionnaireDTO.getLocalizedFinalText());
        newQuestionnaire.setLocalizedDisplayName(questionnaireDTO.getLocalizedDisplayName());

        if (!logo.isEmpty() || questionnaireDTO.isDeleteLogo()) {
            handleLogoUpload(newQuestionnaire, logo, questionnaireDTO.isDeleteLogo());
        }

        questionnaireDao.merge(newQuestionnaire);
        return newQuestionnaire;
    }

    private void handleLogoUpload(Questionnaire questionnaire, MultipartFile logo, boolean isDeleteLogo) {
        String imagePath = configurationDao.getImageUploadPath() + "/" + Constants.IMAGE_QUESTIONNAIRE + "/" + questionnaire.getId().toString();

        if (isDeleteLogo || !logo.isEmpty()) {
            // Altes Logo löschen
            if (questionnaire.getLogo() != null) {
                File deleteFile = new File(imagePath + "/" + questionnaire.getLogo());
                deleteFile.delete();
                questionnaire.setLogo(null);
            }
        }

        if (!logo.isEmpty()) {
            // Neues Logo hochladen
            String logoExtension = FilenameUtils.getExtension(logo.getOriginalFilename());
            questionnaire.setLogo("logo." + logoExtension);

            File uploadDir = new File(imagePath);
            if (!uploadDir.isDirectory()) {
                uploadDir.mkdirs();
            }
            File uploadFile = new File(imagePath, "logo." + logoExtension);
            try {
                BufferedImage uploadImage = ImageIO.read(logo.getInputStream());
                BufferedImage resizedImage = GraphicsUtilities.resizeImage(uploadImage, 300);
                ImageIO.write(resizedImage, logoExtension, uploadFile);
            } catch (IOException ex) {
                LOGGER.debug("Error uploading the picture for the questionnaire with id {}: {}", questionnaire.getId(), ex.getLocalizedMessage());
            }
        }
    }

}
