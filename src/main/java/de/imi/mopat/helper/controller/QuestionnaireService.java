package de.imi.mopat.helper.controller;

import de.imi.mopat.dao.ConditionDao;
import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.dao.ExportTemplateDao;
import de.imi.mopat.dao.QuestionnaireDao;
import de.imi.mopat.helper.model.QuestionnaireDTOMapper;
import de.imi.mopat.helper.model.QuestionnaireFactory;
import de.imi.mopat.model.Answer;
import de.imi.mopat.model.BundleQuestionnaire;
import de.imi.mopat.model.ConfigurationGroup;
import de.imi.mopat.model.ExportTemplate;
import de.imi.mopat.model.conditions.Condition;
import de.imi.mopat.model.QuestionnaireVersionGroup;
import de.imi.mopat.model.dto.QuestionnaireDTO;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.enumeration.ExportTemplateType;
import de.imi.mopat.model.score.BinaryExpression;
import de.imi.mopat.model.score.BinaryOperator;
import de.imi.mopat.model.score.Expression;
import de.imi.mopat.model.score.MultiExpression;
import de.imi.mopat.model.score.MultiOperator;
import de.imi.mopat.model.score.Score;
import de.imi.mopat.model.score.UnaryExpression;
import de.imi.mopat.model.score.UnaryOperator;
import de.imi.mopat.model.user.UserRole;
import de.imi.mopat.validator.LogoValidator;
import de.imi.mopat.validator.QuestionnaireDTOValidator;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Collectors;
import java.util.TreeMap;
import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.Pair;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

@Service
public class QuestionnaireService {

    private static final org.slf4j.Logger LOGGER =
        org.slf4j.LoggerFactory.getLogger(QuestionnaireService.class);

    @Autowired
    private LogoValidator logoValidator;

    @Autowired
    private AuthService authService;

    @Autowired
    private BundleService bundleService;

    @Autowired
    private ConfigurationDao configurationDao;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private QuestionnaireDao questionnaireDao;

    @Autowired
    private QuestionnaireDTOValidator questionnaireDTOValidator;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private QuestionnaireDTOMapper questionnaireDTOMapper;

    @Autowired
    private QuestionnaireFactory questionnaireFactory;

    @Autowired
    private QuestionnaireVersionGroupService questionnaireVersionGroupService;

    @Autowired
    private ConditionDao conditionDao;

    @Autowired
    private ExportTemplateDao exportTemplateDao;

    @Autowired
    private FileUtils fileUtils;

    /**
     * Processes the localized welcome and final texts in the given {@link QuestionnaireDTO}.
     * It removes unnecessary HTML tags such as "<p><br></p>", "<br>".
     *
     * @param questionnaireDTO The {@link QuestionnaireDTO} containing localized texts to be processed.
     */
    public void processLocalizedText(QuestionnaireDTO questionnaireDTO) {
        questionnaireDTO.setLocalizedWelcomeText(
                processLocalizedMap(questionnaireDTO.getLocalizedWelcomeText()));
        questionnaireDTO.setLocalizedFinalText(
                processLocalizedMap(questionnaireDTO.getLocalizedFinalText()));
    }

    /**
     * Retrieves all questionnaires from the data source and maps them to {@link QuestionnaireDTO} objects.
     *
     * @return A list of all {@link QuestionnaireDTO} objects.
     */
    public List<QuestionnaireDTO> getAllQuestionnaireDTOs() {
        return questionnaireDao.getAllElements().stream()
                .map(questionnaireDTOMapper)
                .collect(Collectors.toList());
    }

    public List<Questionnaire> getAllQuestionnaires() {
        return questionnaireDao.getAllElements();
    }

    /**
     * Validates the questionnaire and logo file.
     *
     * @param questionnaireDTO The {@link QuestionnaireDTO} to validate.
     * @param logo             The {@link MultipartFile} containing the logo.
     * @param result           The {@link BindingResult} to record validation errors.
     */
    public void validateQuestionnaire(QuestionnaireDTO questionnaireDTO, MultipartFile logo, BindingResult result) {
        validateQuestionnaireDTO(questionnaireDTO, result);
        logoValidator.validateLogo(logo, result);
    }

    /**
     * Saves or updates a {@link Questionnaire} based on the given {@link QuestionnaireDTO}.
     * If the questionnaire ID is null, a new questionnaire is created.
     * If the questionnaire ID is not null and editing is allowed, the existing questionnaire is updated.
     * If the questionnaire ID is not null and editing is not allowed, a copy of the questionnaire is created.
     *
     * @param questionnaireDTO The {@link QuestionnaireDTO} containing the questionnaire data.
     * @param logo             The logo file associated with the questionnaire.
     * @param userId           The ID of the user performing the action.
     * @return The saved or updated {@link Questionnaire}.
     */
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

    /**
     * Retrieves a {@link QuestionnaireDTO} by its ID.
     *
     * @param questionnaireId The ID of the {@link Questionnaire} to retrieve.
     * @return An {@link Optional} containing the {@link QuestionnaireDTO} if found, or an empty {@link Optional} otherwise.
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
     *
     * @param questionnaireDTO The {@link QuestionnaireDTO} to check.
     * @return True if editing is allowed, otherwise false.
     */
    public boolean editingQuestionnaireAllowed(QuestionnaireDTO questionnaireDTO) {
        Questionnaire questionnaire = questionnaireDao.getElementById(questionnaireDTO.getId());

        // Admins and moderators can edit if there are no encounters
        if (authService.hasRoleOrAbove(UserRole.ROLE_MODERATOR)) {
            return questionnaire.isModifiable();
        }

        // Editors can edit if questionnaire is not part of any bundle that is enabled or if the bundle has executed encounters
        if (authService.hasExactRole(UserRole.ROLE_EDITOR)) {
            return questionnaire.isModifiable() && !isQuestionnairePartOfEnabledBundle(questionnaire);
        }

        // By default, editing is not allowed
        return false;
    }

    /**
     * Determines if editing the given {@link QuestionnaireDTO} is allowed.
     * Provides specific reasons if editing is not allowed.
     *
     * @param questionnaireDTO The {@link QuestionnaireDTO} to check.
     * @return A {@link Pair} containing a boolean indicating if editing is allowed.
     *         and a message explaining why editing is not allowed (if applicable).
     */
    public Pair<Boolean, String> canEditQuestionnaireWithReason(QuestionnaireDTO questionnaireDTO) {
        Questionnaire questionnaire = questionnaireDao.getElementById(questionnaireDTO.getId());

        if (questionnaire == null) {
            return Pair.of(true, null);
        }

        boolean isModifiable = questionnaire.isModifiable();
        boolean partOfEnabledBundle = isQuestionnairePartOfEnabledBundle(questionnaire);

        if (authService.hasRoleOrAbove(UserRole.ROLE_MODERATOR) && !isModifiable) {
            return Pair.of(false, getLocalizedMessage("questionnaire.message.executedEncounters"));
        }

        if (authService.hasExactRole(UserRole.ROLE_EDITOR)) {
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
     * Helper method to process the localized text map by removing unnecessary HTML tags.
     *
     * @param localizedTextMap The map containing localized texts to be processed.
     * @return A processed map with unnecessary HTML tags removed.
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

    /**
     * Validates the given {@link QuestionnaireDTO}.
     *
     * @param questionnaireDTO The {@link QuestionnaireDTO} to be validated.
     * @param result           The {@link BindingResult} to hold validation errors.
     */
    private void validateQuestionnaireDTO(QuestionnaireDTO questionnaireDTO, BindingResult result) {
        questionnaireDTOValidator.validate(questionnaireDTO, result);
    }

    /**
     * Checks if the given {@link Questionnaire} is part of any enabled bundle.
     *
     * @param questionnaire The {@link Questionnaire} to check.
     * @return True if the questionnaire is part of an enabled bundle, otherwise false.
     */
    private boolean isQuestionnairePartOfEnabledBundle(Questionnaire questionnaire) {
        List<BundleQuestionnaire> bundleQuestionnaires = bundleService.findByQuestionnaireId(questionnaire.getId());
        for (BundleQuestionnaire bundleQuestionnaire : bundleQuestionnaires) {
            if(bundleQuestionnaire.getIsEnabled()){
                return true;
            }
        }
        return false;
    }

    /**
     * Creates a new {@link Questionnaire} based on the given {@link QuestionnaireDTO}.
     * This includes setting the initial details and handling logo upload.
     *
     * @param questionnaireDTO The {@link QuestionnaireDTO} containing the questionnaire data.
     * @param logo             The logo file associated with the questionnaire.
     * @param userId           The ID of the user performing the action.
     * @return The newly created {@link Questionnaire}.
     */
    private Questionnaire createNewQuestionnaire(QuestionnaireDTO questionnaireDTO, MultipartFile logo, Long userId) {
        Questionnaire newQuestionnaire = questionnaireFactory.createQuestionnaire(
                questionnaireDTO.getName(),
                questionnaireDTO.getDescription(),
                userId,
                Boolean.TRUE
        );
        questionnaireDao.merge(newQuestionnaire);

        QuestionnaireVersionGroup questionnaireVersionGroup = questionnaireVersionGroupService.createQuestionnaireGroup(newQuestionnaire.getName());
        questionnaireVersionGroupService.addQuestionnaireToGroup(questionnaireVersionGroup, newQuestionnaire);

        copyLocalizedTextsToQuestionnaire(newQuestionnaire, questionnaireDTO);
        handleLogoUpload(newQuestionnaire, questionnaireDTO, logo);
        questionnaireDao.merge(newQuestionnaire);
        return newQuestionnaire;
    }

    /**
     * Updates an existing {@link Questionnaire} based on the given {@link QuestionnaireDTO}.
     * This includes setting the updated details and handling logo upload.
     *
     * @param questionnaireDTO The {@link QuestionnaireDTO} containing the questionnaire data.
     * @param logo             The logo file associated with the questionnaire.
     * @param userId           The ID of the user performing the action.
     * @return The updated {@link Questionnaire}.
     */
    private Questionnaire updateExistingQuestionnaire(QuestionnaireDTO questionnaireDTO, MultipartFile logo, Long userId) {
        Questionnaire existingQuestionnaire = questionnaireDao.getElementById(questionnaireDTO.getId());

        existingQuestionnaire.setDescription(questionnaireDTO.getDescription());
        existingQuestionnaire.setName(questionnaireDTO.getName());
        existingQuestionnaire.setChangedBy(userId);

        copyLocalizedTextsToQuestionnaire(existingQuestionnaire, questionnaireDTO);
        handleLogoUpload(existingQuestionnaire, questionnaireDTO, logo);
        questionnaireDao.merge(existingQuestionnaire);
        return existingQuestionnaire;
    }

    /**
     * Creates a copy of an existing {@link Questionnaire} based on the given {@link QuestionnaireDTO}.
     * This includes setting the new version, copying questions, and handling logo upload.
     *
     * @param questionnaireDTO The {@link QuestionnaireDTO} containing the questionnaire data.
     * @param logo             The logo file associated with the questionnaire.
     * @param userId           The ID of the user performing the action.
     * @return The newly created copy of the {@link Questionnaire}.
     */
    private Questionnaire createQuestionnaireCopy(QuestionnaireDTO questionnaireDTO, MultipartFile logo, Long userId) {
        Questionnaire existingQuestionnaire = questionnaireDao.getElementById(questionnaireDTO.getId());
        String newName = generateUniqueName(questionnaireDTO, existingQuestionnaire);
        Questionnaire newQuestionnaire = questionnaireFactory.createQuestionnaire(
                newName,
                questionnaireDTO.getDescription(),
                userId,
                Boolean.TRUE
        );
        questionnaireDao.merge(newQuestionnaire);

        MapHolder questionCopyMaps = questionService.duplicateQuestionsToNewQuestionnaire(existingQuestionnaire.getQuestions(), newQuestionnaire);
        Map<Question, Question> questionMap = questionCopyMaps.questionMap();
        Map<Question, Map<Answer, Answer>> oldQuestionToNewAnswerMap = questionCopyMaps.oldQuestionToNewAnswerMap();

        Set<Condition> clonedConditions = questionService.cloneConditions(oldQuestionToNewAnswerMap, questionMap);

        // Copy scores
        copyScores(existingQuestionnaire.getScores(), newQuestionnaire, questionMap);

        copyLocalizedTextsToQuestionnaire(newQuestionnaire, questionnaireDTO);
        handleLogoUpload(newQuestionnaire, questionnaireDTO, logo);

        for(Condition condition : clonedConditions)
            conditionDao.merge(condition);

        copyExportTemplates(existingQuestionnaire.getExportTemplates(), newQuestionnaire);

        // Set version in Questionnaire
        setVersionForNewQuestionnaire(newQuestionnaire, existingQuestionnaire);
        QuestionnaireVersionGroup existingGroup = existingQuestionnaire.getQuestionnaireVersionGroup();
        questionnaireVersionGroupService.addQuestionnaireToGroup(existingGroup, newQuestionnaire);

        questionnaireDao.merge(newQuestionnaire);
        return newQuestionnaire;
    }


    /**
     * Sets the version for the new {@link Questionnaire} based on the existing {@link Questionnaire}.
     * If the existing questionnaire is part of a group, the new version will be incremented based on the maximum version in the group.
     * If not, the new version will be incremented based on the version of the existing questionnaire.
     *
     * @param newQuestionnaire      The new {@link Questionnaire} to set the version for.
     * @param existingQuestionnaire The existing {@link Questionnaire} used as a reference for the version.
     * @throws IllegalStateException    if the new questionnaire has not been persisted (i.e., its ID is null).
     * @throws IllegalArgumentException if the existing questionnaire is null or has not been persisted (i.e., its ID is null).
     */
    private void setVersionForNewQuestionnaire(Questionnaire newQuestionnaire, Questionnaire existingQuestionnaire) {
        // Ensure the new questionnaire is persisted
        if (newQuestionnaire.getId() == null) {
            throw new IllegalStateException("The new questionnaire must be persisted before saving versioning information.");
        }

        // Ensure the existing questionnaire is not null and is persisted
        if (existingQuestionnaire == null || existingQuestionnaire.getId() == null) {
            throw new IllegalArgumentException("The existing questionnaire must not be null and must be persisted.");
        }

        int version;
        Optional<QuestionnaireVersionGroup> groupForQuestionnaire = questionnaireVersionGroupService.findGroupForQuestionnaire(existingQuestionnaire);

        if (groupForQuestionnaire.isPresent()) {
            int maxVersionInGroup = questionnaireVersionGroupService.findMaxVersionInGroup(groupForQuestionnaire.get());
            version = maxVersionInGroup + 1;
        } else {
            version = existingQuestionnaire.getVersion() + 1;
        }

        newQuestionnaire.setVersion(version);
    }


    /**
     * Copies scores from the original questionnaire to the new questionnaire.
     * This method should handle the copying in memory and return a set of copied scores.
     *
     * @param originalScores  The set of original scores to copy.
     * @param newQuestionnaire The new questionnaire to which scores are being copied.
     * @param questionMap      A map of original questions to copied questions.
     * @return A set of copied scores.
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
     * Copies a set of {@link ExportTemplate} objects from the source to a new destination while modifying
     * the filenames to include the new {@link Questionnaire} ID. Each copied export template's data is cloned
     * from the existing templates, but associated with a new {@link Questionnaire} and stored under a newly generated filename.
     * The method also copies the file data from the old storage path to a new path with the new filename.
     *
     * @param exportTemplates the set of {@link ExportTemplate} to be copied.
     * @param newQuestionnaire the {@link Questionnaire} to associate with the copied export templates.
     * @return a {@link Set} of {@link ExportTemplate} that contains the cloned copies of the provided export templates.
     */
    public Set<ExportTemplate> copyExportTemplates(Set<ExportTemplate> exportTemplates, Questionnaire newQuestionnaire){
        Set<ExportTemplate> copiedExportTemplates = new HashSet<>();
        for(ExportTemplate templateToCopy : exportTemplates){
            ExportTemplate copiedTemplate = createExportTemplate(templateToCopy, newQuestionnaire);
            exportTemplateDao.merge(copiedTemplate);  // Persist to DB to get the ID
            String newFileName = fileUtils.generateFileNameForExportTemplate(templateToCopy.getFilename(), copiedTemplate.getId());
            copiedTemplate.setFilename(newFileName);
            try {
                // Copy the file
                fileUtils.copyTemplateFile(templateToCopy.getFilename(), newFileName);
                exportTemplateDao.merge(copiedTemplate);
                copiedExportTemplates.add(copiedTemplate);
                newQuestionnaire.addExportTemplate(copiedTemplate);
            } catch (IOException e) {
                LOGGER.error("Failed to copy export template '{}': {}", templateToCopy.getFilename(), e.getMessage());
                // Clean up: remove only the failed template from the DB and the filesystem
                fileUtils.deleteExportTemplateFrom(copiedTemplate.getFilename());
                exportTemplateDao.remove(copiedTemplate);
                newQuestionnaire.removeExportTemplate(copiedTemplate);
            }
        }
        return copiedExportTemplates;
    }

    /**
     * Creates a new {@link ExportTemplate} instance by copying the attributes from an existing template and associating
     * it with a new {@link Questionnaire}. The ID is not copied, and a new ID will be generated when the new template
     * is persisted in the database.
     *
     * @param templateToCopy the {@link ExportTemplate} that should be copied.
     * @param newQuestionnaire the {@link Questionnaire} that will be associated with the new template.
     * @return a new {@link ExportTemplate} instance with the same attributes as the original, but associated with the provided {@link Questionnaire}.
     */
    private ExportTemplate createExportTemplate(ExportTemplate templateToCopy, Questionnaire newQuestionnaire) {
        ExportTemplate newExportTemplate = new ExportTemplate();
        newExportTemplate.setName(templateToCopy.getName());
        if(templateToCopy.getOriginalFilename() != null){
            newExportTemplate.setOriginalFilename(templateToCopy.getOriginalFilename());
        }
        newExportTemplate.setExportTemplateType(templateToCopy.getExportTemplateType());
        newExportTemplate.setConfigurationGroup(templateToCopy.getConfigurationGroup());
        newExportTemplate.setQuestionnaire(newQuestionnaire);
        return newExportTemplate;
    }

    /**
     * Copies the localized text fields from the {@link QuestionnaireDTO} to the {@link Questionnaire}.
     *
     * @param questionnaire      The target {@link Questionnaire} to copy text to.
     * @param questionnaireDTO   The source {@link QuestionnaireDTO} containing the text.
     */
    private void copyLocalizedTextsToQuestionnaire(Questionnaire questionnaire, QuestionnaireDTO questionnaireDTO) {
        if (questionnaire == null || questionnaireDTO == null) {
            throw new IllegalArgumentException("Questionnaire and QuestionnaireDTO must not be null");
        }

        questionnaire.setLocalizedDisplayName(new TreeMap<>(questionnaireDTO.getLocalizedDisplayName()));
        questionnaire.setLocalizedWelcomeText(new TreeMap<>(questionnaireDTO.getLocalizedWelcomeText()));
        questionnaire.setLocalizedFinalText(new TreeMap<>(questionnaireDTO.getLocalizedFinalText()));
    }

    public Boolean hasQuestionnaireConditions(Questionnaire questionnaire){
        if (questionnaire == null){
            return false;
        }
        for (Question originalQuestion : questionnaire.getQuestions()) {
            for (Answer answer : originalQuestion.getAnswers()) {
                for (Condition condition : answer.getConditions()) {
                    return condition.getTarget().getClass() == Questionnaire.class;
                }
            }
        }
        return false;
    }
    /**
     * Handles the upload and deletion of the questionnaire's logo.
     *
     * @param questionnaire      The {@link Questionnaire} entity to update.
     * @param questionnaireDTO   The {@link QuestionnaireDTO} containing the logo information.
     * @param logo               The {@link MultipartFile} containing the new logo file.
     */
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
     * Uploads a new logo for the questionnaire.
     *
     * @param questionnaire The {@link Questionnaire} entity.
     * @param logo          The {@link MultipartFile} containing the new logo file.
     * @param imagePath     The path to the image directory.
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
     * Deletes the existing logo of the questionnaire if it exists.
     *
     * @param questionnaire The {@link Questionnaire} entity.
     * @param imagePath     The path to the image directory.
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
     * Generates a unique name for the new questionnaire version.
     *
     * @param questionnaireDTO The {@link QuestionnaireDTO} containing the new name.
     * @param existingQuestionnaire The existing {@link Questionnaire} to be copied.
     * @return The unique name for the new questionnaire version.
     */
    private String generateUniqueName(QuestionnaireDTO questionnaireDTO, Questionnaire existingQuestionnaire) {
        String newName = questionnaireDTO.getName();

        // Check if the name from questionnaireDTO is unique
        if (!questionnaireDao.isQuestionnaireNameUsed(newName)) {
            return newName;
        }

        // Get the base name from the existing questionnaire
        String baseName = getBaseNameWithoutVersion(existingQuestionnaire.getName());

        // Determine the next available version number
        int nextVersion = determineNextAvailableVersion(existingQuestionnaire);

        // Generate a new name with the version number
        do {
            newName = baseName + " v" + nextVersion;
            nextVersion++;
        } while (questionnaireDao.isQuestionnaireNameUsed(newName));

        return newName;
    }

    /**
     * Determines the next available version number for a questionnaire.
     *
     * @param existingQuestionnaire The existing {@link Questionnaire}.
     * @return The next available version number.
     */
    private int determineNextAvailableVersion(Questionnaire existingQuestionnaire) {
        Optional<QuestionnaireVersionGroup> group = questionnaireVersionGroupService.findGroupForQuestionnaire(existingQuestionnaire);
        if (group.isPresent()) {
            return questionnaireVersionGroupService.findMaxVersionInGroup(group.get()) + 1;
        } else {
            return existingQuestionnaire.getVersion() + 1;
        }
    }

    /**
     * Gets the base name of a questionnaire by removing the version suffix (e.g., " v1" or "v1").
     *
     * @param name The original name of the questionnaire.
     * @return The base name without the version suffix.
     */
    public String getBaseNameWithoutVersion(String name) {
        if (name.matches(".* v\\d+$")) {
            return name.replaceAll(" v\\d+$", "");
        } else if (name.matches(".*v\\d+$")) {
            return name.replaceAll("v\\d+$", "");
        }
        return name;
    }

    /**
     * Helper method to retrieve a localized message with a default message.
     *
     * @param messageKey The key of the message to retrieve.
     * @return The localized message or the default message if the key is not found.
     */
    private String getLocalizedMessage(String messageKey) {
        String defaultMessage = "The questionnaire cannot be edited. You can duplicate it instead.";
        return messageSource.getMessage(messageKey, new Object[]{}, defaultMessage, LocaleContextHolder.getLocale());
    }

    /**
     * Copies the given {@link Expression} object based on its type.
     *
     * @param originalExpression The original {@link Expression} to copy.
     * @param questionMap        The map of original to copied {@link Question} objects.
     * @param scoreMap           The map of original to copied {@link Score} objects.
     *
     * @return The copied {@link Expression}.
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
     * Copies a {@link UnaryExpression}.
     *
     * @param originalUnary The original {@link UnaryExpression} to copy.
     * @param questionMap   The map of original to copied {@link Question} objects.
     * @param scoreMap      The map of original to copied {@link Score} objects.
     * @return The copied {@link UnaryExpression}.
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
     * @param originalBinary The original {@link BinaryExpression} to copy.
     * @param questionMap    The map of original to copied {@link Question} objects.
     * @param scoreMap       The map of original to copied {@link Score} objects.
     * @return The copied {@link BinaryExpression}.
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
     * @param originalMulti The original {@link MultiExpression} to copy.
     * @param questionMap   The map of original to copied {@link Question} objects.
     * @param scoreMap      The map of original to copied {@link Score} objects.
     * @return The copied {@link MultiExpression}.
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