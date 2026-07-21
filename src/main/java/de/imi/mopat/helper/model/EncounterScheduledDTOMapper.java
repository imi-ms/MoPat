package de.imi.mopat.helper.model;

import de.imi.mopat.dao.BundleDao;
import de.imi.mopat.dao.ClinicDao;
import de.imi.mopat.dao.EncounterScheduledDao;
import de.imi.mopat.helper.controller.LocaleHelper;
import de.imi.mopat.model.Bundle;
import de.imi.mopat.model.Clinic;
import de.imi.mopat.model.Encounter;
import de.imi.mopat.model.EncounterScheduled;
import de.imi.mopat.model.dto.BundleDTO;
import de.imi.mopat.model.dto.ClinicDTO;
import de.imi.mopat.model.dto.EncounterDTO;
import de.imi.mopat.model.dto.EncounterScheduledApiRequestDTO;
import de.imi.mopat.model.dto.EncounterScheduledDTO;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@Component
public class EncounterScheduledDTOMapper implements Function<EncounterScheduled, EncounterScheduledDTO> {

    private final BundleDTOMapper bundleDTOMapper;
    private final EncounterDTOMapper encounterDTOMapper;
    private final BundleDao bundleDao;
    private final ClinicDao clinicDao;
    private final EncounterScheduledDao encounterScheduledDao;

    public EncounterScheduledDTOMapper(BundleDTOMapper bundleDTOMapper,
        EncounterDTOMapper encounterDTOMapper,
        BundleDao bundleDao,
        ClinicDao clinicDao,
        EncounterScheduledDao encounterScheduledDao) {
        this.bundleDTOMapper = bundleDTOMapper;
        this.encounterDTOMapper = encounterDTOMapper;
        this.bundleDao = bundleDao;
        this.clinicDao = clinicDao;
        this.encounterScheduledDao = encounterScheduledDao;
    }

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

    public EncounterScheduled mapToEntity(EncounterScheduledDTO dto) {
        if (dto.getBundleDTO() == null || dto.getBundleDTO().getId() == null) {
            throw new IllegalArgumentException("EncounterScheduledDTO must contain a bundle id");
        }

        if (dto.getClinicDTO() == null || dto.getClinicDTO().getId() == null) {
            throw new IllegalArgumentException("EncounterScheduledDTO must contain a clinic id");
        }
        Clinic clinic = clinicDao.getElementById(dto.getClinicDTO().getId());
        Bundle bundle = bundleDao.getElementById(dto.getBundleDTO().getId());

        if (dto.getId() == null) {
            return new EncounterScheduled(
                dto.getCaseNumber(), bundle, clinic, dto.getStartDate(),
                dto.getEncounterScheduledSerialType(), dto.getEndDate(), dto.getRepeatPeriod(),
                dto.getEmail(), dto.getLocale().toString(), dto.getPersonalText(),
                dto.getReplyMail());
        }

        EncounterScheduled entity = encounterScheduledDao.getElementById(dto.getId());
        updateEntity(entity, dto, bundle, clinic);
        return entity;
    }

    private void updateEntity(EncounterScheduled entity, EncounterScheduledDTO dto, Bundle bundle, Clinic clinic) {
        entity.setCaseNumber(dto.getCaseNumber());
        entity.setBundle(bundle);
        entity.setClinic(clinic);
        entity.setStartDate(dto.getStartDate());
        entity.setEncounterScheduledSerialType(dto.getEncounterScheduledSerialType());
        entity.setEndDate(dto.getEndDate());
        entity.setRepeatPeriod(dto.getRepeatPeriod());
        entity.setEmail(dto.getEmail());
        entity.setLocale(dto.getLocale().toString());
        entity.setPersonalText(dto.getPersonalText());

        if ("empty".equalsIgnoreCase(dto.getReplyMail())) {
            entity.setReplyMail(null);
        } else {
            entity.setReplyMail(dto.getReplyMail());
        }
    }

    public EncounterScheduledDTO mapFromApiRequest(EncounterScheduledApiRequestDTO request) {
        EncounterScheduledDTO dto = new EncounterScheduledDTO();

        dto.setCaseNumber(request.getCaseNumber());
        dto.setEmail(request.getEmail());
        dto.setStartDate(request.getStartDate());
        dto.setEndDate(request.getEndDate());
        dto.setEncounterScheduledSerialType(request.getEncounterScheduledSerialType());
        dto.setReplyMail(request.getReplyMail());
        dto.setPersonalText(request.getPersonalText());

        BundleDTO bundleDTO = new BundleDTO();
        bundleDTO.setId(request.getBundleId());
        dto.setBundleDTO(bundleDTO);

        ClinicDTO clinicDTO = new ClinicDTO();
        clinicDTO.setId(request.getClinicId());
        dto.setClinicDTO(clinicDTO);
        dto.setLocale(request.getLocale());

        dto.setLocale(request.getLocale() != null ? request.getLocale() : Locale.getDefault());

        return dto;
    }


}
