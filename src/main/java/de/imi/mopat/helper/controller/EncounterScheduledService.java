package de.imi.mopat.helper.controller;

import de.imi.mopat.model.Encounter;
import de.imi.mopat.model.EncounterScheduled;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.Response;
import de.imi.mopat.model.dto.EncounterDTO;
import de.imi.mopat.model.dto.EncounterScheduledDTO;
import de.imi.mopat.model.dto.ResponseDTO;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EncounterScheduledService {

    private static final org.slf4j.Logger LOGGER =
        org.slf4j.LoggerFactory.getLogger(Question.class);

    @Autowired
    private EncounterService encounterService;
    @Autowired
    private BundleService bundleService;

    public EncounterScheduledDTO toEncounterScheduledDTO(EncounterScheduled encounterScheduled) {
        EncounterScheduledDTO encounterScheduledDTO =
            new EncounterScheduledDTO();
        encounterScheduledDTO.setBundleDTO(bundleService.toBundleDTO(true,encounterScheduled.getBundle()));
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
            encounterDTOs.add(encounterService.toEncounterDTO(false,encounter));
        }

        Collections.sort(
            encounterDTOs,
            (EncounterDTO o1, EncounterDTO o2) -> {
                if (o1.getStartTime()
                    .compareTo(o2.getStartTime())
                    != 0) {
                    return o1.getStartTime()
                        .compareTo(o2.getStartTime());
                } else if (o1.getEndTime()
                    != null
                    && o2.getEndTime()
                    != null) {
                    return o1.getEndTime()
                        .compareTo(o2.getEndTime());
                }
                return 0;
            });

        encounterScheduledDTO.setEncounterDTOs(encounterDTOs);

        return encounterScheduledDTO;
    }


}
