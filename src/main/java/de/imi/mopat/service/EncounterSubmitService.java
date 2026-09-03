package de.imi.mopat.service;

import de.imi.mopat.dao.AnswerDao;
import de.imi.mopat.dao.AuditEntryDao;
import de.imi.mopat.dao.BundleDao;
import de.imi.mopat.dao.EncounterDao;
import de.imi.mopat.dao.QuestionnaireDao;
import de.imi.mopat.dao.ResponseDao;
import de.imi.mopat.error.EncounterSubmitException;
import de.imi.mopat.io.EncounterExporter;
import de.imi.mopat.model.Answer;
import de.imi.mopat.model.Bundle;
import de.imi.mopat.model.BundleQuestionnaire;
import de.imi.mopat.model.Encounter;
import de.imi.mopat.model.EncounterExportTemplate;
import de.imi.mopat.model.ExportTemplate;
import de.imi.mopat.model.PointOnImage;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.Response;
import de.imi.mopat.model.dto.EncounterDTO;
import de.imi.mopat.model.dto.EncounterSubmitResponseDTO;
import de.imi.mopat.model.dto.PointOnImageDTO;
import de.imi.mopat.model.dto.ResponseDTO;
import de.imi.mopat.model.enumeration.AuditEntryActionType;
import de.imi.mopat.model.enumeration.AuditPatientAttribute;
import de.imi.mopat.model.enumeration.ExportStatus;
import de.imi.mopat.model.enumeration.ExportTemplateType;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service responsible for managing the submission, validation, and persistence of patient encounter data.
 * Handles the updating of existing encounters, processing of questionnaire responses, and generation
 * of audit entries. Coordinates interactions between the encounter, answer, response, and questionnaire
 * repositories to ensure data integrity and correct state transitions.
 */
@Service
public class EncounterSubmitService {

    private final EncounterDao encounterDao;
    private final AnswerDao answerDao;
    private final ResponseDao responseDao;
    private final QuestionnaireDao questionnaireDao;
    private final AuditEntryDao auditEntryDao;
    private final EncounterExporter encounterExporter;
    private final BundleDao bundleDao;

    public EncounterSubmitService(EncounterDao encounterDao,
        AnswerDao answerDao,
        ResponseDao responseDao,
        QuestionnaireDao questionnaireDao,
        AuditEntryDao auditEntryDao,
        EncounterExporter encounterExporter,
        BundleDao bundleDao
    ) {
        this.encounterDao = encounterDao;
        this.answerDao = answerDao;
        this.responseDao = responseDao;
        this.questionnaireDao = questionnaireDao;
        this.auditEntryDao = auditEntryDao;
        this.encounterExporter = encounterExporter;
        this.bundleDao = bundleDao;
    }

    /**
     * Updates an existing encounter based on the provided data.
     *
     * Validates the input, loads the existing encounter, and ensures the associated bundle is published.
     * If the encounter is marked as a test or the bundle is not published, the operation is ignored.
     * Otherwise, the base data and responses are updated. If the encounter is marked as completed, the completion
     * process is executed. The encounter is then persisted and an audit entry is recorded.
     *
     * @param encounterDTO the data transfer object containing the updated encounter details
     * @return an EncounterSubmitResponseDTO indicating whether the encounter was stored or ignored
     */
    @Transactional("MoPat")
    public EncounterSubmitResponseDTO updateEncounter(final EncounterDTO encounterDTO) {
        validateEncounterDTO(encounterDTO);

        if (encounterDTO.getIsTest()) {
            return EncounterSubmitResponseDTO.ignored("Test encounter was not stored.");
        }

        Encounter encounter = loadEncounter(encounterDTO);

        if (encounter.getBundle() == null ||
            !Boolean.TRUE.equals(encounter.getBundle().getIsPublished())) {
            return EncounterSubmitResponseDTO.ignored("Test encounter was not stored.");
        }

        updateEncounterBaseData(encounter, encounterDTO);
        updateEncounterResponses(encounter, encounterDTO);

        if (encounterDTO.getIsCompleted()) {
            completeEncounter(encounter, encounterDTO);
        }

        encounterDao.merge(encounter);
        writeAuditEntry(encounter);

        return EncounterSubmitResponseDTO.stored();
    }

    /**
     * Validates the completion of a questionnaire for a specific encounter and optionally executes an export test.
     * This method checks if the associated bundle exists and is unpublished. If the questionnaire is included
     * in the active questionnaires of the encounter, it constructs the encounter and response objects
     * from the provided DTOs. If the export test flag is enabled, it triggers the export process for the
     * constructed encounter and questionnaire.
     *
     * @param questionnaireId the identifier of the questionnaire to be finalized
     * @param encounterDTO the data transfer object containing the encounter details, bundle reference, active questionnaires, and responses
     * @param performExportTest flag indicating whether to execute the export logic for the encounter
     */
    public void finishQuestionnaireTest(final Long questionnaireId,
        final EncounterDTO encounterDTO, final Boolean performExportTest) {

        Bundle bundle = bundleDao.getElementById(encounterDTO.getBundleDTO().getId());

        if (bundle != null && !bundle.getIsPublished()) {

            Questionnaire questionnaire = questionnaireDao.getElementById(questionnaireId);
            if (encounterDTO.getActiveQuestionnaireIds().contains(questionnaire.getId())) {
                Encounter encounter = new Encounter();
                encounter.setCaseNumber(encounterDTO.getCaseNumber());
                encounter.setBundleLanguage(encounterDTO.getBundleLanguage());
                encounter.setBundle(bundle);
                Set<Response> responses = new HashSet<>();
                for (ResponseDTO responseDTO : encounterDTO.getResponses()) {

                    Answer currentAnswer = answerDao.getElementById(responseDTO.getAnswerId());

                    Response response = createResponseObject(responseDTO, encounter, currentAnswer);
                    responses.add(response);

                }
                encounter.setResponses(responses);
                if (performExportTest) {
                    encounterExporter.export(encounter, questionnaire, true);
                }
            }
        }
    }

    /**
     * Validates the required fields of the provided EncounterDTO.
     * Throws an EncounterSubmitException if the DTO is null or if any of the
     * required fields (id, uuid) are missing.
     *
     * @param encounterDTO the encounter data transfer object to validate
     */
    private void validateEncounterDTO(final EncounterDTO encounterDTO) {
        if (encounterDTO == null) {
            throw new EncounterSubmitException(
                HttpStatus.BAD_REQUEST,
                "Encounter payload is missing."
            );
        }

        if (encounterDTO.getId() == null) {
            throw new EncounterSubmitException(
                HttpStatus.BAD_REQUEST,
                "Encounter id is missing."
            );
        }

        if (encounterDTO.getUuid() == null) {
            throw new EncounterSubmitException(
                HttpStatus.BAD_REQUEST,
                "Encounter uuid is missing."
            );
        }
    }

    /**
     * Loads an Encounter entity from the data access object using the UUID provided in the DTO.
     *
     * @param encounterDTO the DTO containing the UUID of the encounter to load
     * @return the loaded Encounter object
     */
    private Encounter loadEncounter(final EncounterDTO encounterDTO) {
        Encounter encounter = encounterDao.getElementByUUID(encounterDTO.getUuid());

        if (encounter == null) {
            throw new EncounterSubmitException(
                HttpStatus.NOT_FOUND,
                "Encounter not found."
            );
        }

        return encounter;
    }

    /**
     * Updates the base data of an encounter object with information from the provided DTO.
     * This method sets the last seen question ID and active questionnaires on the encounter.
     *
     * @param encounter the encounter object to be updated with new data
     * @param encounterDTO the DTO containing the data to transfer to the encounter
     */
    private void updateEncounterBaseData(
        final Encounter encounter,
        final EncounterDTO encounterDTO) {

        encounter.setLastSeenQuestionId(encounterDTO.getLastSeenQuestionId());
        encounter.setActiveQuestionnaires(encounterDTO.getActiveQuestionnaireIds());
    }

    /**
     * Updates the responses associated with an encounter based on the data provided in the DTO.
     * This method processes new or modified responses, removes responses that are no longer present
     * in the DTO, and updates the encounter's response set accordingly.
     *
     * @param encounter the encounter entity to be updated
     * @param encounterDTO the data transfer object containing the new response data
     */
    private void updateEncounterResponses(
        final Encounter encounter,
        final EncounterDTO encounterDTO) {

        if (encounterDTO.getResponses() == null) {
            return;
        }

        Set<Response> existingResponses = new HashSet<>(encounter.getResponses());

        Set<Long> existingAnswerIds = new HashSet<>();
        for (Response response : existingResponses) {
            existingAnswerIds.add(response.getAnswer().getId());
        }

        Set<Long> givenAnswerIds = new HashSet<>();

        for (ResponseDTO responseDTO : encounterDTO.getResponses()) {
            processResponseDTO(
                encounter,
                responseDTO,
                existingResponses,
                existingAnswerIds,
                givenAnswerIds
            );
        }

        removeResponsesMissingFromDTO(
            encounter,
            existingResponses,
            existingAnswerIds,
            givenAnswerIds
        );

        encounter.setResponses(existingResponses);
    }

    /**
     * Processes a single response DTO to determine whether to create, update,
     * or disable a response associated with the given encounter.
     *
     * @param encounter the Encounter object associated with the responses
     * @param responseDTO the response data transfer object containing answer information
     * @param existingResponses the set of existing Response objects to be modified
     * @param existingAnswerIds the set of answer IDs that already have associated responses
     * @param givenAnswerIds the set of answer IDs from the incoming request
     */
    private void processResponseDTO(
        final Encounter encounter,
        final ResponseDTO responseDTO,
        final Set<Response> existingResponses,
        final Set<Long> existingAnswerIds,
        final Set<Long> givenAnswerIds) {

        if (responseDTO == null || responseDTO.getAnswerId() == null) {
            return;
        }

        Long answerId = responseDTO.getAnswerId();
        givenAnswerIds.add(answerId);

        Answer currentAnswer = answerDao.getElementById(answerId);

        if (currentAnswer == null) {
            givenAnswerIds.remove(answerId);
            return;
        }

        if (!responseDTO.isEnabled()) {
            removeDisabledResponse(
                encounter,
                responseDTO,
                currentAnswer,
                existingResponses,
                existingAnswerIds
            );
            return;
        }

        if (existingAnswerIds.contains(answerId)) {
            updateExistingResponse(
                responseDTO,
                currentAnswer,
                existingResponses,
                givenAnswerIds
            );
        } else {
            Response response = createResponseObject(responseDTO, encounter, currentAnswer);

            if (response != null) {
                existingResponses.add(response);
            } else {
                givenAnswerIds.remove(answerId);
            }
        }

        questionnaireDao.merge(currentAnswer.getQuestion().getQuestionnaire());
    }

    /**
     * Removes a disabled response from the encounter and the associated answer.
     * If the answer ID of the given response DTO is found in the set of existing answer IDs,
     * the corresponding response object is retrieved and removed from the current answer and
     * the existing responses set. Finally, the questionnaire associated with the current answer
     * is merged to persist the changes.
     *
     * @param encounter the encounter context in which the response is being managed
     * @param responseDTO the response data transfer object containing the answer ID to be removed
     * @param currentAnswer the answer object from which the response is to be removed
     * @param existingResponses the set of existing responses to update by removing the disabled one
     * @param existingAnswerIds the set of answer IDs currently present in the encounter
     */
    private void removeDisabledResponse(
        final Encounter encounter,
        final ResponseDTO responseDTO,
        final Answer currentAnswer,
        final Set<Response> existingResponses,
        final Set<Long> existingAnswerIds) {

        Long answerId = responseDTO.getAnswerId();

        if (existingAnswerIds.contains(answerId)) {
            Response responseToDelete = responseDao.getResponseByAnswerInEncounter(
                answerId,
                encounter.getId()
            );

            if (responseToDelete != null) {
                currentAnswer.removeResponse(responseToDelete);
                existingResponses.remove(responseToDelete);
            }
        }

        questionnaireDao.merge(currentAnswer.getQuestion().getQuestionnaire());
    }

    /**
     * Updates an existing response within the provided set based on the type of the associated question.
     * If no corresponding response is found for the answer ID, it removes the ID from the given set.
     * Otherwise, it delegates the update process to the appropriate method based on the question type.
     *
     * @param responseDTO the data transfer object containing the new response details
     * @param currentAnswer the answer object associated with the question type
     * @param existingResponses the set of existing response objects to search and update
     * @param givenAnswerIds the set of answer IDs that have been processed
     */
    private void updateExistingResponse(
        final ResponseDTO responseDTO,
        final Answer currentAnswer,
        final Set<Response> existingResponses,
        final Set<Long> givenAnswerIds) {

        Response currentResponse = findResponseByAnswerId(
            existingResponses,
            responseDTO.getAnswerId()
        );

        if (currentResponse == null) {
            givenAnswerIds.remove(responseDTO.getAnswerId());
            return;
        }

        switch (currentAnswer.getQuestion().getQuestionType()) {
            case SLIDER:
            case NUMBER_CHECKBOX:
            case NUMBER_INPUT:
                updateNumberResponse(responseDTO, currentResponse, givenAnswerIds);
                break;

            case NUMBER_CHECKBOX_TEXT:
                updateNumberCheckboxTextResponse(responseDTO, currentResponse, givenAnswerIds);
                break;

            case FREE_TEXT:
                updateFreeTextResponse(responseDTO, currentResponse, givenAnswerIds);
                break;

            case DATE:
                updateDateResponse(responseDTO, currentResponse, givenAnswerIds);
                break;

            case IMAGE:
                updateImageResponse(responseDTO, currentResponse, givenAnswerIds);
                break;

            default:
                break;
        }
    }

    /**
     * Updates the value of a response if the new value differs from the current one
     * and removes the answer ID from the set if the new value is null.
     *
     * @param responseDTO the response data transfer object containing the new value and answer ID
     * @param currentResponse the current response object to be updated with the new value
     * @param givenAnswerIds the set of answer IDs representing given answers
     */
    private void updateNumberResponse(
        final ResponseDTO responseDTO,
        final Response currentResponse,
        final Set<Long> givenAnswerIds) {

        if (responseDTO.getValue() == null) {
            givenAnswerIds.remove(responseDTO.getAnswerId());
            return;
        }

        if (currentResponse.getValue() == null
            || !currentResponse.getValue().equals(responseDTO.getValue())) {
            currentResponse.setValue(responseDTO.getValue());
        }
    }

    /**
     * Updates a response with a number checkbox text value.
     *
     * @param responseDTO the response data transfer object containing the new value and answer ID
     * @param currentResponse the current response object to be updated with the new value
     * @param givenAnswerIds the set of answer IDs representing given answers
     */
    private void updateNumberCheckboxTextResponse(
        final ResponseDTO responseDTO,
        final Response currentResponse,
        final Set<Long> givenAnswerIds) {

        boolean hasValue = responseDTO.getValue() != null;
        boolean hasCustomText = responseDTO.getCustomtext() != null
            && !responseDTO.getCustomtext().equals("");

        if (!hasValue && !hasCustomText) {
            givenAnswerIds.remove(responseDTO.getAnswerId());
            return;
        }

        if (hasValue) {
            if (currentResponse.getValue() == null
                || !currentResponse.getValue().equals(responseDTO.getValue())) {
                currentResponse.setValue(responseDTO.getValue());
            }
        } else {
            currentResponse.setValue(null);
        }

        if (hasCustomText) {
            if (currentResponse.getCustomtext() == null
                || currentResponse.getCustomtext().equals("")
                || !currentResponse.getCustomtext().equals(responseDTO.getCustomtext())) {
                currentResponse.setCustomtext(responseDTO.getCustomtext());
            }
        } else {
            currentResponse.setCustomtext("");
        }
    }

    /**
     * Updates a response with a free text value.
     *
     * @param responseDTO the response data transfer object containing the new value and answer ID
     * @param currentResponse the current response object to be updated with the new value
     * @param givenAnswerIds the set of answer IDs representing given answers
     */
    private void updateFreeTextResponse(
        final ResponseDTO responseDTO,
        final Response currentResponse,
        final Set<Long> givenAnswerIds) {

        if (responseDTO.getCustomtext() == null || responseDTO.getCustomtext().equals("")) {
            givenAnswerIds.remove(responseDTO.getAnswerId());
            return;
        }

        if (currentResponse.getCustomtext() == null
            || !currentResponse.getCustomtext().equals(responseDTO.getCustomtext())) {
            currentResponse.setCustomtext(responseDTO.getCustomtext());
        }
    }

    /**
     * Updates a response with a date value.
     *
     * @param responseDTO the response data transfer object containing the new value and answer ID
     * @param currentResponse the current response object to be updated with the new value
     * @param givenAnswerIds the set of answer IDs representing given answers
     */
    private void updateDateResponse(
        final ResponseDTO responseDTO,
        final Response currentResponse,
        final Set<Long> givenAnswerIds) {

        if (responseDTO.getDate() == null) {
            givenAnswerIds.remove(responseDTO.getAnswerId());
            return;
        }

        if (currentResponse.getDate() == null
            || !currentResponse.getDate().equals(responseDTO.getDate())) {
            currentResponse.setDate(responseDTO.getDate());
        }
    }

    /**
     * Updates a response with an image value.
     *
     * @param responseDTO the response data transfer object containing the new value and answer ID
     * @param currentResponse the current response object to be updated with the new value
     * @param givenAnswerIds the set of answer IDs representing given answers
     */
    private void updateImageResponse(
        final ResponseDTO responseDTO,
        final Response currentResponse,
        final Set<Long> givenAnswerIds) {

        if (responseDTO.getPointsOnImage() == null
            || responseDTO.getPointsOnImage().isEmpty()) {
            givenAnswerIds.remove(responseDTO.getAnswerId());
            return;
        }

        List<PointOnImage> pointsOnImage = new ArrayList<>();

        for (PointOnImageDTO currentPointOnImageDTO : responseDTO.getPointsOnImage()) {
            PointOnImage pointOnImage = currentPointOnImageDTO.toPointOnImage();
            pointOnImage.setResponse(currentResponse);
            pointsOnImage.add(pointOnImage);
        }

        currentResponse.setPointsOnImage(pointsOnImage);
    }

    /**
     * Finds a response by its answer ID.
     *
     * @param existingResponses the set of existing responses
     * @param answerId the answer ID to search for
     * @return the response with the given answer ID, or null if not found
     */
    private Response findResponseByAnswerId(
        final Set<Response> existingResponses,
        final Long answerId) {

        for (Response response : existingResponses) {
            if (response.getAnswer().getId().equals(answerId)) {
                return response;
            }
        }

        return null;
    }

    /**
     * Removes responses that are no longer associated with the encounter based on the provided data transfer object changes.
     * Identifies answers that were present previously but are absent from the new set of given answer IDs,
     * then removes the corresponding responses from the answers and the existing response set.
     * Also ensures the questionnaires associated with the modified answers are merged to persist the changes.
     *
     * @param encounter the encounter to which the responses are related
     * @param existingResponses the set of current responses to be modified
     * @param existingAnswerIds the set of answer IDs that are currently recorded
     * @param givenAnswerIds the set of answer IDs provided in the new data
     */
    private void removeResponsesMissingFromDTO(
        final Encounter encounter,
        final Set<Response> existingResponses,
        final Set<Long> existingAnswerIds,
        final Set<Long> givenAnswerIds) {

        existingAnswerIds.removeAll(givenAnswerIds);

        for (Long id : existingAnswerIds) {
            Answer answer = answerDao.getElementById(id);

            if (answer == null) {
                continue;
            }

            Response responseToDelete = responseDao.getResponseByAnswerInEncounter(
                id,
                encounter.getId()
            );

            if (responseToDelete != null) {
                answer.removeResponse(responseToDelete);
                existingResponses.remove(responseToDelete);
            }

            questionnaireDao.merge(answer.getQuestion().getQuestionnaire());
        }
    }

    /**
     * Finalizes the processing of a clinical encounter by handling export tasks and updating the end time.
     * This method waits for any currently running export operations to complete, determines which export templates
     * are still pending, and performs exports for those templates associated with active questionnaires.
     * The encounter's end time is set to the current timestamp, and the updated state is persisted to the data store.
     *
     * @param encounter the encounter entity object currently being processed
     * @param encounterDTO the data transfer object containing encounter details such as the UUID
     */
    private void completeEncounter(
        Encounter encounter,
        final EncounterDTO encounterDTO) {

        waitForPossibleRunningExport();

        Set<ExportTemplate> remainingExportTemplates = collectExportTemplates(encounter);

        waitForRunningExports(encounterDTO, remainingExportTemplates);

        encounter = encounterDao.getElementByUUID(encounterDTO.getUuid());
        encounter.setEndTime(new Timestamp(System.currentTimeMillis()));

        removeAlreadyExportedTemplates(encounter, remainingExportTemplates);

        for (ExportTemplate exportTemplate : remainingExportTemplates) {
            if (encounter.getActiveQuestionnaires()
                .contains(exportTemplate.getQuestionnaire().getId())) {
                encounterExporter.export(encounter, exportTemplate);
            }
        }

        encounterDao.merge(encounter);
    }

    /**
     * Pauses the current thread for five seconds to allow any potentially in-progress
     * export operation to complete before proceeding.
     */
    private void waitForPossibleRunningExport() {
        try {
            Thread.sleep(5000L);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Collects all export templates associated with the provided encounter.
     *
     * @param encounter the encounter object containing the bundle questionnaires
     * @return a set of all {@link ExportTemplate} instances found in the encounter's bundle questionnaires
     */
    private Set<ExportTemplate> collectExportTemplates(final Encounter encounter) {
        Set<ExportTemplate> remainingExportTemplates = new HashSet<>();

        for (BundleQuestionnaire bundleQuestionnaire : encounter.getBundle()
            .getBundleQuestionnaires()) {
            remainingExportTemplates.addAll(bundleQuestionnaire.getExportTemplates());
        }

        return remainingExportTemplates;
    }

    /**
     * Waits for the specified export templates to be completed by polling the encounter status.
     *
     * This method polls the database up to a maximum of 16 times (initial check plus 15 iterations),
     * sleeping for 2 seconds between each check, to determine if the remaining export templates
     * have been processed. It updates the remaining set by removing templates that are no longer
     * active in the encounter. The method stops early if all remaining templates are exported or
     * if the current thread is interrupted.
     *
     * @param encounterDTO the encounter data transfer object containing the UUID of the encounter
     * @param remainingExportTemplates the set of export templates that are still pending and need to be checked; templates are removed from this set once they are confirmed as exported
     */
    private void waitForRunningExports(
        final EncounterDTO encounterDTO,
        final Set<ExportTemplate> remainingExportTemplates) {

        int loopCounter = 0;

        while (loopCounter <= 15) {
            Encounter encounter = encounterDao.getElementByUUID(encounterDTO.getUuid());

            removeAlreadyExportedTemplates(encounter, remainingExportTemplates);

            if (remainingExportTemplates.isEmpty()) {
                break;
            }

            try {
                Thread.sleep(2000L);
                loopCounter++;
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * Removes templates from the remaining set that have already been successfully exported for the given encounter.
     * A template is considered already exported if it is associated with the encounter and is not a failed ODM export.
     *
     * @param encounter the encounter associated with the export templates
     * @param remainingExportTemplates the set of export templates to be modified by removing those already exported
     */
    private void removeAlreadyExportedTemplates(
        final Encounter encounter,
        final Set<ExportTemplate> remainingExportTemplates) {

        for (EncounterExportTemplate encounterExportTemplate : encounter.getEncounterExportTemplates()) {
            boolean isFailedOdmExport =
                encounterExportTemplate.getExportTemplate().getExportTemplateType()
                    == ExportTemplateType.ODM
                    && encounterExportTemplate.getExportStatus() == ExportStatus.FAILURE;

            if (!isFailedOdmExport) {
                remainingExportTemplates.remove(encounterExportTemplate.getExportTemplate());
            }
        }
    }

    /**
     * Creates and persists an audit log entry for the update of an encounter.
     *
     * This method evaluates the provided encounter object to determine the relevant
     * patient attributes involved in the update. If the encounter contains a patient
     * ID, the PATIENT_ID attribute is included. If the encounter contains non-empty
     * responses, the TREATMENT_DATA attribute is included. The method then delegates
     * to the auditEntryDao to write the audit entry with the class name, method
     * description, case number, determined patient attributes, and a WRITE action
     * type.
     *
     * @param encounter The Encounter object representing the updated encounter for
     *                  which the audit entry should be generated.
     */
    private void writeAuditEntry(final Encounter encounter) {
        Set<AuditPatientAttribute> patientAttributes = new HashSet<>();

        if (encounter.getPatientID() != null) {
            patientAttributes.add(AuditPatientAttribute.PATIENT_ID);
        }

        if (encounter.getResponses() != null && !encounter.getResponses().isEmpty()) {
            patientAttributes.add(AuditPatientAttribute.TREATMENT_DATA);
        }

        auditEntryDao.writeAuditEntry(
            this.getClass().getSimpleName(),
            "updateEncounter(EncounterDTO)",
            encounter.getCaseNumber(),
            patientAttributes,
            AuditEntryActionType.WRITE
        );
    }

    /**
     * Creates a Response object from a ResponseDTO, associating it with the
     * current Encounter and Answer. It populates the response with custom text,
     * value, date, and points on image data if available in the DTO.
     *
     * @param responseDTO the ResponseDTO containing the data to transfer
     * @param encounter the Encounter associated with this response
     * @param currentAnswer the Answer associated with this response
     * @return the newly created Response object
     */
    private Response createResponseObject(ResponseDTO responseDTO, Encounter encounter,
        Answer currentAnswer) {

        Response response = new Response(currentAnswer, encounter);

        if (responseDTO.getCustomtext() != null) {
            response.setCustomtext(responseDTO.getCustomtext());
        }

        if (responseDTO.getValue() != null) {
            response.setValue(responseDTO.getValue());
        }

        if (responseDTO.getDate() != null) {
            response.setDate(responseDTO.getDate());
        }

        if (responseDTO.getPointsOnImage() != null) {

            List<PointOnImage> pointsOnImage = new ArrayList<>();
            for (PointOnImageDTO currentPointOnImageDTO : responseDTO.getPointsOnImage()) {
                PointOnImage pointOnImage = currentPointOnImageDTO.toPointOnImage();
                pointOnImage.setResponse(response);
                pointsOnImage.add(pointOnImage);
            }
            response.setPointsOnImage(pointsOnImage);
        }
        return response;
    }
}
