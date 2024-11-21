package de.imi.mopat.helper.model;

import de.imi.mopat.helper.controller.LocaleHelper;
import de.imi.mopat.model.Encounter;
import de.imi.mopat.model.EncounterScheduled;
import de.imi.mopat.model.dto.EncounterDTO;
import de.imi.mopat.model.dto.EncounterScheduledDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@Component
public class EncounterScheduledDTOMapper implements Function<EncounterScheduled, EncounterScheduledDTO> {

    @Autowired
    private BundleDTOMapper bundleDTOMapper;
    
    @Autowired
    private EncounterDTOMapper encounterDTOMapper;

    @Override
    public EncounterScheduledDTO apply(EncounterScheduled encounterScheduled) {
        EncounterScheduledDTO encounterScheduledDTO = new EncounterScheduledDTO();
        encounterScheduledDTO.setBundleDTO(bundleDTOMapper.apply(true, encounterScheduled.getBundle()));
        encounterScheduledDTO.setStartDate(encounterScheduled.getStartDate());
        encounterScheduledDTO.setEmail(encounterScheduled.getEmail());
        encounterScheduledDTO.setEndDate(encounterScheduled.getEndDate());
        encounterScheduledDTO.setRepeatPeriod(encounterScheduled.getRepeatPeriod());
        encounterScheduledDTO.setReplyMail(encounterScheduled.getReplyMail());
        encounterScheduledDTO.setCaseNumber(encounterScheduled.getCaseNumber());
        encounterScheduledDTO.setId(encounterScheduled.getId());
        encounterScheduledDTO.setUuid(encounterScheduled.getUUID());
        encounterScheduledDTO.setEncounterScheduledSerialType(encounterScheduled.getEncounterScheduledSerialType());
        encounterScheduledDTO.setMailStatus(encounterScheduled.getMailStatus());
        encounterScheduledDTO.setLocale(LocaleHelper.getLocaleFromString(encounterScheduled.getLocale()));

        List<EncounterDTO> encounterDTOs = new ArrayList<>();

        for (Encounter encounter : encounterScheduled.getEncounters()) {
            encounterDTOs.add(encounterDTOMapper.apply(false, encounter));
        }

        // Sorting the EncounterDTOs
        Collections.sort(encounterDTOs, (EncounterDTO o1, EncounterDTO o2) -> {
            int startTimeComparison = o1.getStartTime().compareTo(o2.getStartTime());
            if (startTimeComparison != 0) {
                return startTimeComparison;
            } else if (o1.getEndTime() != null && o2.getEndTime() != null) {
                return o1.getEndTime().compareTo(o2.getEndTime());
            }
            return 0;
        });

        encounterScheduledDTO.setEncounterDTOs(encounterDTOs);

        return encounterScheduledDTO;
    }


}
