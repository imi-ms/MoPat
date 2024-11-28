package de.imi.mopat.helper.model;

import de.imi.mopat.model.Encounter;
import de.imi.mopat.model.Response;
import de.imi.mopat.model.dto.EncounterDTO;
import de.imi.mopat.model.dto.ResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;

@Component
public class EncounterDTOMapper implements BiFunction<Boolean, Encounter, EncounterDTO> {

    @Autowired
    private BundleDTOMapper bundleDTOMapper;

    @Override
    public EncounterDTO apply(Boolean activeSurvey, Encounter encounter) {
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
                encounterDTO.setBundleDTO(bundleDTOMapper.apply(true, encounter.getBundle()));
            }

            encounterDTO.setIsAtHome(encounter.getEncounterScheduled() != null);
        } else {
            // Set successfullyExports to "-" and change it, if there is a
            // Bundle
            String successfullExports = "-";
            if (encounter.getBundle() != null) {
                encounterDTO.setBundleDTO(bundleDTOMapper.apply(false, encounter.getBundle()));
                successfullExports =
                        encounter.getNumberOfAssignedAndSuccessfullyExportedExportTemplates() + "/"
                                + encounter.getBundle().getNumberOfAssignedExportTemplate();
            }
            encounterDTO.setSuccessfullExports(successfullExports);
        }

        return encounterDTO;
    }

}
