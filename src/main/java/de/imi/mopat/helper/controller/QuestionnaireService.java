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
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Set;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import de.imi.mopat.model.dto.QuestionnaireDTO;
import de.imi.mopat.model.score.BinaryExpression;
import de.imi.mopat.model.score.BinaryOperator;
import de.imi.mopat.model.score.Expression;
import de.imi.mopat.model.score.MultiExpression;
import de.imi.mopat.model.score.MultiOperator;
import de.imi.mopat.model.score.Score;
import de.imi.mopat.model.score.UnaryExpression;
import de.imi.mopat.model.score.UnaryOperator;
import de.imi.mopat.validator.LogoValidator;
import org.apache.commons.lang3.tuple.Pair;
import de.imi.mopat.helper.model.QuestionnaireDTOMapper;
import de.imi.mopat.helper.model.QuestionnaireFactory;
@Service
public class QuestionnaireService {

    private static final org.slf4j.Logger LOGGER =
        org.slf4j.LoggerFactory.getLogger(Question.class);

    @Autowired
    LogoValidator logoValidator;
    @Autowired
    QuestionnaireFactory questionnaireFactory;
    @Autowired
    QuestionnaireDTOMapper questionnaireDTOMapper;

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
        logoValidator.validateLogo(logo, result);
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
    /**
     * Retrieves a {@link QuestionnaireDTO} by its ID
     *
     * @param questionnaireId The ID of the {@link Questionnaire} to retrieve
     * @return An {@link Optional} containing the {@link QuestionnaireDTO} if found, or an empty {@link Optional} otherwise
     */
    public Optional<QuestionnaireDTO> getQuestionnaireDTOById(Long questionnaireId) {
        if (questionnaireId == null || questionnaireId <= 0) {
            return Optional.empty();
        }
        return Optional.ofNullable(questionnaireDao.getElementById(questionnaireId))
                .map(questionnaireDTOMapper);
    }

    /**
     * Determines if editing the given {@link QuestionnaireDTO} is allowed.
     * Provides specific reasons if editing is not allowed.
     *
     * @param questionnaireDTO The {@link QuestionnaireDTO} to check.
     * @return A {@link Pair} containing a boolean indicating if editing is allowed
     *         and a message explaining why editing is not allowed (if applicable).
     */
    public Pair<Boolean, String> canEditQuestionnaireWithReason(QuestionnaireDTO questionnaireDTO) {
        Questionnaire questionnaire = questionnaireDao.getElementById(questionnaireDTO.getId());

        if (questionnaire == null) {
            return Pair.of(true, null);
        }

        boolean isModifiable = questionnaire.isModifiable();
        boolean partOfEnabledBundle = isQuestionnairePartOfEnabledBundle(questionnaire);

        if (authService.hasRoleOrAbove("ROLE_MODERATOR") && !isModifiable) {
            return Pair.of(false, getLocalizedMessage("questionnaire.message.executedEncounters"));
        }

        if (authService.hasExactRole("ROLE_EDITOR")) {
            if (!isModifiable && partOfEnabledBundle) {
                return Pair.of(false, getLocalizedMessage("questionnaire.message.executedEncountersAndEnabledBundle"));
            }
            if (!isModifiable) {
                return Pair.of(false, getLocalizedMessage("questionnaire.message.executedEncounters"));
            }
            if (partOfEnabledBundle) {
                return Pair.of(false, getLocalizedMessage("questionnaire.message.enabledBundle"));
            }
        }

        return Pair.of(true, null);
    }

    /**
     * Copies scores from the original questionnaire to the new questionnaire
     * This method should handle the copying in memory and return a set of copied scores
     *
     * @param originalScores  The set of original scores to copy
     * @param newQuestionnaire The new questionnaire to which scores are being copied
     * @param questionMap      A map of original questions to copied questions
     * @return A set of copied scores
     */
    private Set<Score> copyScores(Set<Score> originalScores, Questionnaire newQuestionnaire, Map<Question, Question> questionMap) {
        Set<Score> copiedScores = new HashSet<>();
        Map<Score, Score> scoreMap = new HashMap<>();

        for (Score originalScore : originalScores) {
            Score copiedScore = new Score();
            copiedScore.setName(originalScore.getName());
            copiedScore.setQuestionnaire(newQuestionnaire);

            // Copy expression using the modified copyExpression method
            Expression copiedExpression = copyExpression(originalScore.getExpression(), questionMap, scoreMap);
            copiedScore.setExpression(copiedExpression);

            copiedScores.add(copiedScore);
            scoreMap.put(originalScore, copiedScore);
        }

        newQuestionnaire.setScores(copiedScores);
        return copiedScores;
    }

    /**
     * Copies the given {@link Expression} object based on its type
     *
     * @param originalExpression The original {@link Expression} to copy
     * @param questionMap        The map of original to copied {@link Question} objects
     * @param scoreMap           The map of original to copied {@link Score} objects
     *
     * @return The copied {@link Expression}
     */
    private Expression copyExpression(Expression originalExpression, Map<Question, Question> questionMap, Map<Score, Score> scoreMap) {
        if (originalExpression instanceof UnaryExpression unary) {
            return copyUnaryExpression(unary, questionMap, scoreMap);
        } else if (originalExpression instanceof BinaryExpression binary) {
            return copyBinaryExpression(binary, questionMap, scoreMap);
        } else if (originalExpression instanceof MultiExpression multi) {
            return copyMultiExpression(multi, questionMap, scoreMap);
        } else {
            throw new IllegalArgumentException("Unknown expression type: " + originalExpression.getClass());
        }
    }

    /**
     * Copies a {@link UnaryExpression}
     *
     * @param originalUnary The original {@link UnaryExpression} to copy
     * @param questionMap   The map of original to copied {@link Question} objects
     * @param scoreMap      The map of original to copied {@link Score} objects
     * @return The copied {@link UnaryExpression}
     */
    private UnaryExpression copyUnaryExpression(UnaryExpression originalUnary, Map<Question, Question> questionMap, Map<Score, Score> scoreMap) {
        UnaryExpression copyUnary = new UnaryExpression();
        copyUnary.setOperator((UnaryOperator) originalUnary.getOperator());
        if (originalUnary.getQuestion() != null) {
            copyUnary.setQuestion(questionMap.get(originalUnary.getQuestion()));
        } else if (originalUnary.getScore() != null) {
            copyUnary.setScore(scoreMap.get(originalUnary.getScore()));
        } else {
            copyUnary.setValue(originalUnary.getValue());
        }
        return copyUnary;
    }

    /**
     * Copies a {@link BinaryExpression}.
     *
     * @param originalBinary The original {@link BinaryExpression} to copy
     * @param questionMap    The map of original to copied {@link Question} objects
     * @param scoreMap       The map of original to copied {@link Score} objects
     * @return The copied {@link BinaryExpression}
     */
    private BinaryExpression copyBinaryExpression(BinaryExpression originalBinary, Map<Question, Question> questionMap, Map<Score, Score> scoreMap) {
        BinaryExpression copyBinary = new BinaryExpression();
        copyBinary.setOperator((BinaryOperator) originalBinary.getOperator());
        List<Expression> copiedChildren = new ArrayList<>();
        for (Expression child : originalBinary.getExpressions()) {
            Expression copiedChild = copyExpression(child, questionMap, scoreMap);
            copiedChild.setParent(copyBinary);
            copiedChildren.add(copiedChild);
        }
        copyBinary.setExpressions(copiedChildren);
        return copyBinary;
    }

    /**
     * Copies a {@link MultiExpression}.
     *
     * @param originalMulti The original {@link MultiExpression} to copy
     * @param questionMap   The map of original to copied {@link Question} objects
     * @param scoreMap      The map of original to copied {@link Score} objects
     * @return The copied {@link MultiExpression}
     */
    private MultiExpression copyMultiExpression(MultiExpression originalMulti, Map<Question, Question> questionMap, Map<Score, Score> scoreMap) {
        MultiExpression copyMulti = new MultiExpression();
        copyMulti.setOperator((MultiOperator) originalMulti.getOperator());
        List<Expression> copiedChildren = new ArrayList<>();
        for (Expression child : originalMulti.getExpressions()) {
            Expression copiedChild = copyExpression(child, questionMap, scoreMap);
            copiedChild.setParent(copyMulti);
            copiedChildren.add(copiedChild);
        }
        copyMulti.setExpressions(copiedChildren);
        return copyMulti;
    }
}