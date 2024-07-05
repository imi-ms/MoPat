package de.imi.mopat.helper.controller;

import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.dao.QuestionnaireDao;
import de.imi.mopat.dao.QuestionnaireVersionDao;
import de.imi.mopat.model.Answer;
import de.imi.mopat.model.BodyPartAnswer;
import de.imi.mopat.model.DateAnswer;
import de.imi.mopat.model.ImageAnswer;
import de.imi.mopat.model.NumberInputAnswer;
import de.imi.mopat.model.BundleQuestionnaire;
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
import java.util.Set;

import de.imi.mopat.validator.QuestionnaireDTOValidator;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
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
    @Qualifier("MoPat")
    private PlatformTransactionManager transactionManager;

    @Autowired
    private ConfigurationDao configurationDao;

    @Autowired
    private AuthService authService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private BundleService bundleService;

    @Autowired
    private ClinicService clinicService;

    @Autowired
    private QuestionnaireDao questionnaireDao;

    @Autowired
    private QuestionnaireVersionDao questionnaireVersionDao;

    @Autowired
    private QuestionnaireDTOValidator questionnaireDTOValidator;

    @Autowired
    private MessageSource messageSource;

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

    public void validateQuestionnaire(QuestionnaireDTO questionnaireDTO, MultipartFile logo, BindingResult result) {
        validateQuestionnaireDTO(questionnaireDTO, result);
        validateLogo(logo, result);
    }

    private void validateQuestionnaireDTO(QuestionnaireDTO questionnaireDTO, BindingResult result) {
        questionnaireDTOValidator.validate(questionnaireDTO, result);
    }

    @Transactional(transactionManager = "MoPat")
    public Questionnaire saveOrUpdateQuestionnaire(QuestionnaireDTO questionnaireDTO, MultipartFile logo, Long userId) {

        // Update questionnaire directly if editing is allowed
        if (questionnaireDTO.getId() != null && editingQuestionnaireAllowed(questionnaireDTO)) {
            return updateExistingQuestionnaire(questionnaireDTO, logo, userId);
        }

        // Create a copy of the existing questionnaire if editing is not allowed
        if (questionnaireDTO.getId() != null && !editingQuestionnaireAllowed(questionnaireDTO)) {
            return createQuestionnaireCopy(questionnaireDTO, logo, userId);
        }

        // Default: Create new questionnaire if ID is null
        return createNewQuestionnaire(questionnaireDTO, logo, userId);
    }

    public boolean editingQuestionnaireAllowed(QuestionnaireDTO questionnaireDTO) {
        Questionnaire questionnaire = questionnaireDao.getElementById(questionnaireDTO.getId());

        // Admins and moderators can edit if there are no encounters
        if (authService.hasRoleOrAbove("ROLE_MODERATOR")) {
            return questionnaire.isModifiable();
        }

        // Editors can edit if questionnaire is not part of any bundle that is enabled or if the bundle has no encounters
        if (authService.hasExactRole("ROLE_EDITOR")) {
            return questionnaire.isModifiable() && !isQuestionnairePartOfEnabledBundle(questionnaire);
        }

        // By default, editing is not allowed
        return false;
    }


    private boolean isQuestionnairePartOfEnabledBundle(Questionnaire questionnaire) {
        List<BundleQuestionnaire> bundleQuestionnaires = bundleService.findByQuestionnaire(questionnaire.getId());
        for (BundleQuestionnaire bundleQuestionnaire : bundleQuestionnaires) {
            if(bundleQuestionnaire.getIsEnabled()){
                return true;
            }
        }
        return false;
    }

    private Questionnaire createNewQuestionnaire(QuestionnaireDTO questionnaireDTO, MultipartFile logo, Long userId) {
        Questionnaire newQuestionnaire = new Questionnaire(
                questionnaireDTO.getName(),
                questionnaireDTO.getDescription(),
                userId,
                Boolean.TRUE
        );
        newQuestionnaire.setCreatedBy(userId);

        setCommonAttributes(newQuestionnaire, questionnaireDTO);
        questionnaireDao.merge(newQuestionnaire);
        handleLogoUpload(newQuestionnaire, questionnaireDTO, logo);
        return newQuestionnaire;
    }

    private Questionnaire updateExistingQuestionnaire(QuestionnaireDTO questionnaireDTO, MultipartFile logo, Long userId) {
        Questionnaire existingQuestionnaire = questionnaireDao.getElementById(questionnaireDTO.getId());

        existingQuestionnaire.setDescription(questionnaireDTO.getDescription());
        existingQuestionnaire.setName(questionnaireDTO.getName());
        existingQuestionnaire.setChangedBy(userId);

        setCommonAttributes(existingQuestionnaire, questionnaireDTO);
        questionnaireDao.merge(existingQuestionnaire);
        handleLogoUpload(existingQuestionnaire, questionnaireDTO, logo);
        return existingQuestionnaire;
    }

    private Questionnaire createQuestionnaireCopy(QuestionnaireDTO questionnaireDTO, MultipartFile logo, Long userId) {
        Questionnaire existingQuestionnaire = questionnaireDao.getElementById(questionnaireDTO.getId());
        Questionnaire newQuestionnaire = new Questionnaire(
                questionnaireDTO.getName(),
                questionnaireDTO.getDescription(),
                userId,
                Boolean.TRUE
        );
        newQuestionnaire.setCreatedBy(userId);

        newQuestionnaire.setVersion(existingQuestionnaire.getVersion() + 1);
        saveVersioningInformation(newQuestionnaire, existingQuestionnaire);

        Set<Question> newQuestions = questionService.copyQuestionsToQuestionnaire(existingQuestionnaire.getQuestions(), newQuestionnaire);
        newQuestionnaire.setQuestions(newQuestions);

        setCommonAttributes(newQuestionnaire, questionnaireDTO);
        questionnaireDao.merge(newQuestionnaire);
        handleLogoUpload(newQuestionnaire, questionnaireDTO, logo);
        return newQuestionnaire;
    }

    private void saveVersioningInformation(Questionnaire newQuestionnaire, Questionnaire existingQuestionnaire) {
        QuestionnaireVersion version = new QuestionnaireVersion();
        version.setCurrentQuestionnaire(newQuestionnaire);
        version.setPreviousQuestionnaire(existingQuestionnaire);
        questionnaireVersionDao.merge(version);
    }

    private void setCommonAttributes(Questionnaire questionnaire, QuestionnaireDTO questionnaireDTO) {
        questionnaire.setLocalizedDisplayName(questionnaireDTO.getLocalizedDisplayName());
        questionnaire.setLocalizedWelcomeText(questionnaireDTO.getLocalizedWelcomeText());
        questionnaire.setLocalizedFinalText(questionnaireDTO.getLocalizedFinalText());
    }


    private void handleLogoUpload(Questionnaire questionnaire, QuestionnaireDTO questionnaireDTO, MultipartFile logo) {
        String imagePath = configurationDao.getImageUploadPath() + "/" + Constants.IMAGE_QUESTIONNAIRE + "/" + questionnaire.getId().toString();

        if (questionnaireDTO.isDeleteLogo() || !logo.isEmpty()) {
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

    public void validateLogo(MultipartFile logo, BindingResult result) {
        if (logo != null && !logo.isEmpty()) {
            String logoExtension = FilenameUtils.getExtension(logo.getOriginalFilename());
            if (!logoExtension.equalsIgnoreCase("png") && !logoExtension.equalsIgnoreCase("jpg")
                    && !logoExtension.equalsIgnoreCase("jpeg")) {
                result.rejectValue("logo", "error.wrongImageType",
                        messageSource.getMessage("bundle.error.wrongImageType", new Object[]{},
                                LocaleContextHolder.getLocale()));
            }
        }
    }
}