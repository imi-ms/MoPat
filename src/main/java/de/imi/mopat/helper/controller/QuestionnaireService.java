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





    public void processLocalizedText(QuestionnaireDTO questionnaireDTO) {
        questionnaireDTO.setLocalizedWelcomeText(
                processLocalizedMap(questionnaireDTO.getLocalizedWelcomeText()));
        questionnaireDTO.setLocalizedFinalText(
                processLocalizedMap(questionnaireDTO.getLocalizedFinalText()));
    }

    /**
     * Helper method to process the localized text map by removing unnecessary HTML tags
     *
     * @param localizedTextMap The map containing localized texts to be processed
     * @return A processed map with unnecessary HTML tags removed
     */
    private SortedMap<String, String> processLocalizedMap(SortedMap<String, String> localizedTextMap) {
        return localizedTextMap.entrySet().stream()
                .peek(entry -> {
                    if ("<p><br></p>".equals(entry.getValue()) || "<br>".equals(entry.getValue())) {
                        entry.setValue("");
                    }
                })
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        TreeMap::new
                ));
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
    private String getLocalizedMessage(String messageKey) {
        String defaultMessage = "The questionnaire cannot be edited. You can duplicate it instead.";
        return messageSource.getMessage(messageKey, new Object[]{}, defaultMessage, LocaleContextHolder.getLocale());
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
        Questionnaire newQuestionnaire = questionnaireFactory.createQuestionnaire(
                questionnaireDTO.getName(),
                questionnaireDTO.getDescription(),
                userId,
                userId,
                Boolean.TRUE
        );

        questionnaireDao.merge(newQuestionnaire);
        copyLocalizedTextsToQuestionnaire(newQuestionnaire, questionnaireDTO);
        handleLogoUpload(newQuestionnaire, questionnaireDTO, logo);
        questionnaireDao.merge(newQuestionnaire);
        return newQuestionnaire;
    }

    private Questionnaire updateExistingQuestionnaire(QuestionnaireDTO questionnaireDTO, MultipartFile logo, Long userId) {
        Questionnaire existingQuestionnaire = questionnaireDao.getElementById(questionnaireDTO.getId());

        existingQuestionnaire.setDescription(questionnaireDTO.getDescription());
        existingQuestionnaire.setName(questionnaireDTO.getName());
        existingQuestionnaire.setChangedBy(userId);

        questionnaireDao.merge(existingQuestionnaire);
        copyLocalizedTextsToQuestionnaire(existingQuestionnaire, questionnaireDTO);
        handleLogoUpload(existingQuestionnaire, questionnaireDTO, logo);
        questionnaireDao.merge(existingQuestionnaire);
        return existingQuestionnaire;
    }

    private Questionnaire createQuestionnaireCopy(QuestionnaireDTO questionnaireDTO, MultipartFile logo, Long userId) {
        Questionnaire existingQuestionnaire = questionnaireDao.getElementById(questionnaireDTO.getId());
        Questionnaire newQuestionnaire = questionnaireFactory.createQuestionnaire(
                existingQuestionnaire.getName(),
                questionnaireDTO.getDescription(),
                userId,
                userId,
                Boolean.TRUE
        );

        newQuestionnaire.setVersion(existingQuestionnaire.getVersion() + 1);
        saveVersioningInformation(newQuestionnaire, existingQuestionnaire);

        Set<Question> newQuestions = questionService.copyQuestionsToQuestionnaire(existingQuestionnaire.getQuestions(), newQuestionnaire);
        newQuestionnaire.setQuestions(newQuestions);

        questionnaireDao.merge(newQuestionnaire);
        copyLocalizedTextsToQuestionnaire(newQuestionnaire, questionnaireDTO);
        handleLogoUpload(newQuestionnaire, questionnaireDTO, logo);
        questionnaireDao.merge(newQuestionnaire);
        return newQuestionnaire;
    }

    private String generateUniqueName(QuestionnaireDTO questionnaireDTO, Questionnaire existingQuestionnaire) {
        // If the names are different, use the name from questionnaireDTO directly.
        if (!questionnaireDTO.getName().equals(existingQuestionnaire.getName())) {
            return questionnaireDTO.getName();
        }

        // Get the original questionnaire name.
        String originalQuestionnaireName = findOriginalQuestionnaire(existingQuestionnaire).getName();

        // Determine the next available version number.
        int nextVersion = determineNextAvailableVersion(findOriginalQuestionnaire(existingQuestionnaire));

        // Generate a new name with the version number.
        String newName;
        do {
            newName = originalQuestionnaireName + " v" + nextVersion;
            nextVersion++;
        } while (questionnaireDao.isQuestionnaireNameUsed(newName));

        return newName;
    }

    private int determineNextAvailableVersion(Questionnaire existingQuestionnaire) {
        // Find the original questionnaire if the existing one is a duplicate
        Questionnaire originalQuestionnaire = findOriginalQuestionnaire(existingQuestionnaire);

        // Find the max version for duplicates of the original questionnaire
        int maxVersion = getMaxVersionForOriginal(originalQuestionnaire);

        return maxVersion + 1;
    }

    private Questionnaire findOriginalQuestionnaire(Questionnaire questionnaire) {
        Optional<QuestionnaireVersion> originalVersion = questionnaireVersionDao.getAllElements().stream()
                .filter(version -> version.getCurrentQuestionnaire().equals(questionnaire))
                .findFirst();



        return originalVersion.map(QuestionnaireVersion::getPreviousQuestionnaire).orElse(questionnaire);
    }

    private int getMaxVersionForOriginal(Questionnaire originalQuestionnaire) {
        return questionnaireVersionDao.getAllElements().stream()
                .filter(version -> version.getPreviousQuestionnaire().equals(originalQuestionnaire))
                .map(version -> version.getCurrentQuestionnaire().getVersion())
                .max(Integer::compare)
                .orElse(originalQuestionnaire.getVersion());
    }

    private void saveVersioningInformation(Questionnaire newQuestionnaire, Questionnaire existingQuestionnaire) {
        QuestionnaireVersion version = new QuestionnaireVersion();
        version.setCurrentQuestionnaire(newQuestionnaire);
        version.setPreviousQuestionnaire(existingQuestionnaire);

        // Ensure the new version has been persisted
        if (newQuestionnaire.getId() == null) {
            throw new IllegalStateException("The new questionnaire must be persisted before saving versioning information.");
        }

        questionnaireVersionDao.merge(version);
    }

    /**
     * Copies the localized text fields from the {@link QuestionnaireDTO} to the {@link Questionnaire}
     *
     * @param questionnaire      The target {@link Questionnaire} to copy text to
     * @param questionnaireDTO   The source {@link QuestionnaireDTO} containing the text
     */
    private void copyLocalizedTextsToQuestionnaire(Questionnaire questionnaire, QuestionnaireDTO questionnaireDTO) {
        if (questionnaire == null || questionnaireDTO == null) {
            throw new IllegalArgumentException("Questionnaire and QuestionnaireDTO must not be null");
        }

        questionnaire.setLocalizedDisplayName(new TreeMap<>(questionnaireDTO.getLocalizedDisplayName()));
        questionnaire.setLocalizedWelcomeText(new TreeMap<>(questionnaireDTO.getLocalizedWelcomeText()));
        questionnaire.setLocalizedFinalText(new TreeMap<>(questionnaireDTO.getLocalizedFinalText()));
    }


    private void handleLogoUpload(Questionnaire questionnaire, QuestionnaireDTO questionnaireDTO, MultipartFile logo) {
        String imagePath = configurationDao.getImageUploadPath() + "/" + Constants.IMAGE_QUESTIONNAIRE + "/" + questionnaire.getId().toString();

        if (questionnaireDTO.isDeleteLogo() || !logo.isEmpty()) {
            deleteExistingLogo(questionnaire, imagePath);
        }

        if (!logo.isEmpty()) {
            uploadNewLogo(questionnaire, logo, imagePath);
        }
    }

    /**
     * Uploads a new logo for the questionnaire
     *
     * @param questionnaire The {@link Questionnaire} entity
     * @param logo          The {@link MultipartFile} containing the new logo file
     * @param imagePath     The path to the image directory
     */
    private void uploadNewLogo(Questionnaire questionnaire, MultipartFile logo, String imagePath) {
        String logoExtension = FilenameUtils.getExtension(logo.getOriginalFilename());
        String logoFilename = "logo." + logoExtension;
        questionnaire.setLogo(logoFilename);

        File uploadDir = new File(imagePath);
        if (!uploadDir.exists() && !uploadDir.mkdirs()) {
            LOGGER.warn("Failed to create directory for questionnaire with id {}", questionnaire.getId());
        }

        File uploadFile = new File(imagePath, logoFilename);
        try {
            BufferedImage uploadImage = ImageIO.read(logo.getInputStream());
            BufferedImage resizedImage = GraphicsUtilities.resizeImage(uploadImage, 300);
            ImageIO.write(resizedImage, logoExtension, uploadFile);
        } catch (IOException ex) {
            LOGGER.error("Error uploading the picture for the questionnaire with id {}: {}", questionnaire.getId(), ex.getLocalizedMessage());
        }
    }

    /**
     * Deletes the existing logo of the questionnaire if it exists
     *
     * @param questionnaire The {@link Questionnaire} entity
     * @param imagePath     The path to the image directory
     */
    private void deleteExistingLogo(Questionnaire questionnaire, String imagePath) {
        if (questionnaire.getLogo() != null) {
            File deleteFile = new File(imagePath + "/" + questionnaire.getLogo());
            if (deleteFile.exists() && !deleteFile.delete()) {
                LOGGER.warn("Failed to delete the existing logo for questionnaire with id {}", questionnaire.getId());
            }
            questionnaire.setLogo(null);
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