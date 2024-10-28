package de.imi.mopat.helper.controller;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.imi.mopat.dao.ClinicConfigurationDao;
import de.imi.mopat.dao.ClinicConfigurationMappingDao;
import de.imi.mopat.model.*;
import de.imi.mopat.model.dto.ClinicConfigurationDTO;
import de.imi.mopat.model.dto.ClinicConfigurationMappingDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class ClinicConfigurationMappingService {

    @Autowired
    private ClinicConfigurationService clinicConfigurationService;

    @Autowired
    private ClinicConfigurationMappingDao clinicConfigurationMappingDao;

    @Autowired
    private ClinicConfigurationDao clinicConfigurationDao;

    /**
     * Initializes ClinicConfigurationMappingDTO using {@link ClinicConfigurationDTO} with default values.
     *
     * @return An {@link ClinicConfigurationMappingDTO} object based on this {@link ClinicConfigurationDTO} object.
     */
    @JsonIgnore
    public ClinicConfigurationMappingDTO toClinicConfigurationMappingDTO(
        ClinicConfigurationDTO clinicConfigurationDTO) {
        ClinicConfigurationMappingDTO clinicConfigurationMappingDTO = new ClinicConfigurationMappingDTO();
        clinicConfigurationMappingDTO.setValue(clinicConfigurationDTO.getValue());
        clinicConfigurationMappingDTO.setConfigurationType(clinicConfigurationDTO.getConfigurationType());
        clinicConfigurationMappingDTO.setAttribute(clinicConfigurationMappingDTO.getAttribute());
        clinicConfigurationMappingDTO.setValue(clinicConfigurationDTO.getValue());
        clinicConfigurationMappingDTO.setPattern(clinicConfigurationMappingDTO.getPattern());
        clinicConfigurationMappingDTO.setDescriptionMessageCode(clinicConfigurationDTO.getDescriptionMessageCode());
        clinicConfigurationMappingDTO.setLabelMessageCode(clinicConfigurationDTO.getLabelMessageCode());
        clinicConfigurationMappingDTO.setPosition(clinicConfigurationMappingDTO.getPosition());
        clinicConfigurationMappingDTO.setUpdateMethod(clinicConfigurationMappingDTO.getUpdateMethod());
        clinicConfigurationMappingDTO.setTestMethod(clinicConfigurationMappingDTO.getTestMethod());
        clinicConfigurationMappingDTO.setClinicConfigurationId(clinicConfigurationDTO.getId());
        if (clinicConfigurationDTO.getOptions() != null && !clinicConfigurationDTO.getOptions().isEmpty()) {
            clinicConfigurationMappingDTO.setOptions(clinicConfigurationDTO.getOptions());
        }
        return clinicConfigurationMappingDTO;
    }

    /**
     * Initializes ClinicConfigurationMappingDTO using {@link ClinicConfigurationMapping}.
     *
     * @return An {@link ClinicConfigurationMappingDTO} object based on this {@link ClinicConfigurationMapping} object.
     */
    @JsonIgnore
    public ClinicConfigurationMappingDTO toClinicConfigurationMappingDTO(
        ClinicConfigurationMapping clinicConfigurationMapping) {
        ClinicConfigurationMappingDTO clinicConfigurationMappingDTO = new ClinicConfigurationMappingDTO();
        ClinicConfigurationDTO clinicConfigurationDTO = clinicConfigurationMapping.getClinicConfiguration()
            .toClinicConfigurationDTO();

        clinicConfigurationMappingDTO.setId(clinicConfigurationMapping.getId());
        clinicConfigurationMappingDTO.setValue(clinicConfigurationMapping.getValue());
        clinicConfigurationMappingDTO.setConfigurationType(clinicConfigurationDTO.getConfigurationType());
        clinicConfigurationMappingDTO.setClinicConfigurationId(clinicConfigurationDTO.getId());
        clinicConfigurationMappingDTO.setUpdateMethod(clinicConfigurationDTO.getUpdateMethod());
        clinicConfigurationMappingDTO.setLabelMessageCode(clinicConfigurationDTO.getLabelMessageCode());
        clinicConfigurationMappingDTO.setPosition(clinicConfigurationDTO.getPosition());
        clinicConfigurationMappingDTO.setDescriptionMessageCode(clinicConfigurationDTO.getDescriptionMessageCode());
        clinicConfigurationMappingDTO.setAttribute(clinicConfigurationMappingDTO.getAttribute());
        clinicConfigurationMappingDTO.setTestMethod(clinicConfigurationDTO.getTestMethod());
        if (clinicConfigurationDTO.getOptions() != null && !clinicConfigurationDTO.getOptions().isEmpty()) {
            clinicConfigurationMappingDTO.setOptions(clinicConfigurationDTO.getOptions());
        }
        return clinicConfigurationMappingDTO;
    }

    /**
     * Initializes a list of ClinicConfigurationMappingDTO using a hashmap of {@link ClinicConfigurationMapping} and its
     * children.
     *
     * @return An {@link List<ClinicConfigurationMappingDTO>} object based on this {@link ClinicConfigurationMapping}
     * object.
     */
    @JsonIgnore
    public List<ClinicConfigurationMappingDTO> processClinicConfigurationMappingHashmap(
        Map<ClinicConfigurationMapping, List<ClinicConfigurationMapping>> clinicConfigurationMappingListMap) {
        List<ClinicConfigurationMappingDTO> clinicConfigurationMappingDTOS = new ArrayList<>();
        for (ClinicConfigurationMapping clinicConfigurationMapping : clinicConfigurationMappingListMap.keySet()) {
            ClinicConfigurationMappingDTO clinicConfigurationMappingDTO = processClinicConfigurationMapping(
                clinicConfigurationMapping, clinicConfigurationMappingListMap);
            clinicConfigurationMappingDTOS.add(clinicConfigurationMappingDTO);
        }
        clinicConfigurationMappingDTOS.sort(new Comparator<ClinicConfigurationMappingDTO>() {
            @Override
            public int compare(final ClinicConfigurationMappingDTO o1, final ClinicConfigurationMappingDTO o2) {
                return o1.getPosition().compareTo(o2.getPosition());
            }
        });
        return clinicConfigurationMappingDTOS;
    }

    /**
     * Initializes ClinicConfigurationMappingDTO using {@link ClinicConfigurationMapping} recursively.
     *
     * @return An {@link ClinicConfigurationMappingDTO} object based on this {@link ClinicConfigurationMapping} object.
     */
    @JsonIgnore
    public ClinicConfigurationMappingDTO processClinicConfigurationMapping(
        ClinicConfigurationMapping clinicConfigurationMapping,
        Map<ClinicConfigurationMapping, List<ClinicConfigurationMapping>> clinicConfigurationMappingListMap) {
        ClinicConfigurationMappingDTO clinicConfigurationMappingDTO = toClinicConfigurationMappingDTO(
            clinicConfigurationMapping);
        if (clinicConfigurationMappingListMap.containsKey(clinicConfigurationMapping)
            && !clinicConfigurationMappingListMap.get(clinicConfigurationMapping).isEmpty()) {
            List<ClinicConfigurationMappingDTO> childDTOS = new ArrayList<>();
            for (ClinicConfigurationMapping childClinicConfigurationMapping : clinicConfigurationMappingListMap.get(
                clinicConfigurationMapping)) {
                processClinicConfigurationMapping(childClinicConfigurationMapping, clinicConfigurationMappingListMap);
                childDTOS.add(toClinicConfigurationMappingDTO(childClinicConfigurationMapping));
            }
            childDTOS.sort(new Comparator<ClinicConfigurationMappingDTO>() {
                @Override
                public int compare(final ClinicConfigurationMappingDTO o1, final ClinicConfigurationMappingDTO o2) {
                    return o1.getPosition().compareTo(o2.getPosition());
                }
            });
            clinicConfigurationMappingDTO.setChildren(childDTOS);
        }
        return clinicConfigurationMappingDTO;
    }

    /**
     * Initializes ClinicConfigurationMappingDTO using {@link ClinicConfigurationMappingDTO} recursively.
     *
     * @return An {@link ClinicConfigurationMappingDTO} object based on this {@link ClinicConfigurationMappingDTO}
     * object.
     */
    @JsonIgnore
    public ClinicConfigurationMappingDTO processClinicConfigurationMappingDTO(
        ClinicConfigurationMappingDTO clinicConfigurationMappingDTO) {
        ClinicConfigurationDTO clinicConfigurationDTO = clinicConfigurationDao.getElementById(
            clinicConfigurationMappingDTO.getClinicConfigurationId()).toClinicConfigurationDTO();
        ClinicConfigurationMappingDTO clinicConfigurationMappingDTO1 = toClinicConfigurationMappingDTO(
            clinicConfigurationDTO);
        clinicConfigurationMappingDTO1.setValue(
            clinicConfigurationMappingDTO.getValue() != null ? clinicConfigurationMappingDTO.getValue() : "");
        clinicConfigurationMappingDTO1.setId(
            clinicConfigurationMappingDTO.getId() != null ? clinicConfigurationMappingDTO.getId() : null);
        if (clinicConfigurationMappingDTO.getChildren() != null) {
            List<ClinicConfigurationMappingDTO> children = new ArrayList<>();
            for (ClinicConfigurationMappingDTO clinicConfigurationMappingDTO2 : clinicConfigurationMappingDTO.getChildren()) {
                ClinicConfigurationMappingDTO child = processClinicConfigurationMappingDTO(
                    clinicConfigurationMappingDTO2);
                children.add(child);
            }
            clinicConfigurationMappingDTO1.setChildren(children);
        }
        return clinicConfigurationMappingDTO1;
    }

    /**
     * Checks if clinic has a config using{@link Clinic}.
     *
     * @return true if any config enabled in {@link Clinic} object.
     */
    @JsonIgnore
    public Boolean clinicHasConfig(Clinic clinic) {
        return clinicConfigurationMappingDao.isPseudonymizationServiceActivated(clinic.getId()) ||
            clinicConfigurationMappingDao.isRegistryOfPatientActivated(clinic.getId()) ||
            clinicConfigurationMappingDao.isUsePatientDataLookupActivated(clinic.getId());
    }

}
