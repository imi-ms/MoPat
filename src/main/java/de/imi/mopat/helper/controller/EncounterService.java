package de.imi.mopat.helper.controller;

import de.imi.mopat.model.Encounter;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.Response;
import de.imi.mopat.model.dto.EncounterDTO;
import de.imi.mopat.model.dto.ResponseDTO;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EncounterService {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(
        Question.class);

    @Autowired
    private BundleService bundleService;

    public EncounterDTO toEncounterDTO(final Boolean activeSurvey, Encounter encounter) {
        EncounterDTO encounterDTO = new EncounterDTO();

        encounterDTO.setId(encounter.getId());
        encounterDTO.setUuid(encounter.getUUID());
        encounterDTO.setCaseNumber(encounter.getCaseNumber());
        encounterDTO.setIsCompleted(encounter.getEndTime() != null);
        encounterDTO.setStartTime(encounter.getStartTime());
        encounterDTO.setEndTime(encounter.getEndTime());
        encounterDTO.setActiveQuestionnaireIds(encounter.getActiveQuestionnaires());
        encounterDTO.setLastReminderDate(encounter.getLastReminderDate());

        if (activeSurvey) {
            encounterDTO.setPatientID(encounter.getPatientID());
            encounterDTO.setLastSeenQuestionId(encounter.getLastSeenQuestionId());
            encounterDTO.setBundleLanguage(encounter.getBundleLanguage());
            encounterDTO.setIsTest(false);
            List<ResponseDTO> responseDTOs = new ArrayList<>();
            Iterator<Response> responsesIterator = encounter.getResponses().iterator();
            while (responsesIterator.hasNext()) {
                ResponseDTO responseDTO = responsesIterator.next().toResponseDTO();
                responseDTOs.add(responseDTO);
            }
            encounterDTO.setResponses(responseDTOs);

            if (encounter.getBundle() != null) {
                encounterDTO.setBundleDTO(bundleService.toBundleDTO(true, encounter.getBundle()));
            }

            encounterDTO.setIsAtHome(encounter.getEncounterScheduled() != null);
        } else {
            // Set successfullyExports to "-" and change it, if there is a
            // Bundle
            String successfullExports = "-";
            if (encounter.getBundle() != null) {
                encounterDTO.setBundleDTO(bundleService.toBundleDTO(false, encounter.getBundle()));
                successfullExports =
                    encounter.getNumberOfAssignedAndSuccessfullyExportedExportTemplates() + "/"
                        + encounter.getBundle().getNumberOfAssignedExportTemplate();
            }
            encounterDTO.setSuccessfullExports(successfullExports);
        }

        return encounterDTO;
    }

}
