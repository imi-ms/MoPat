package de.imi.mopat.helper.model;

import de.imi.mopat.helper.controller.ClinicConfigurationMappingService;
import de.imi.mopat.model.Clinic;
import de.imi.mopat.model.ClinicConfiguration;
import de.imi.mopat.model.ClinicConfigurationMapping;
import de.imi.mopat.model.dto.BundleClinicDTO;
import de.imi.mopat.model.dto.ClinicDTO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ClinicDTOMapper implements Function<Clinic, ClinicDTO> {

    @Autowired
    private BundleClinicDTOMapper bundleClinicDTOMapper;

    @Autowired
    private ClinicConfigurationMappingService clinicConfigurationMappingService;

    /*
     * Converts this {@link Clinic} object to an {@link ClinicDTO} object.
     *
     * @return An {@link ClinicDTO} object based on this {@link Clinic}
     * object.
     */
    @Override
    public ClinicDTO apply(Clinic clinic) {
        ClinicDTO clinicDTO = new ClinicDTO();
        clinicDTO.setId(clinic.getId());
        clinicDTO.setDescription(clinic.getDescription());
        clinicDTO.setName(clinic.getName());
        clinicDTO.setEmail(clinic.getEmail());

        List<BundleClinicDTO> bundleClinicDTOs = clinic.getBundleClinics().stream()
            .map(bundleClinic -> bundleClinicDTOMapper.apply(clinicDTO, bundleClinic))
            .collect(Collectors.toList());

        clinicDTO.setBundleClinicDTOs(bundleClinicDTOs);

        Map<ClinicConfigurationMapping, List<ClinicConfigurationMapping>> relation = new HashMap<>();
        if (clinic.getClinicConfigurationMappings() != null) {
            for (ClinicConfigurationMapping clinicConfigurationMapping : clinic.getClinicConfigurationMappings()) {
                ClinicConfiguration parent = clinicConfigurationMapping.getClinicConfiguration().getParent();
                if (parent != null) {
                    ClinicConfigurationMapping result = clinic.getClinicConfigurationMappings().stream()
                        .filter(obj -> obj.getClinicConfiguration()
                            .equals(clinicConfigurationMapping.getClinicConfiguration().getParent()))
                        .findFirst()
                        .orElse(null);
                    List<ClinicConfigurationMapping> newList = relation.get(result);
                    if (newList == null) {
                        newList = new ArrayList<>();
                    }
                    newList.add(clinicConfigurationMapping);
                    relation.put(result, newList);
                } else {
                    if (!relation.containsKey(clinicConfigurationMapping)) {
                        relation.put(clinicConfigurationMapping, new ArrayList<>());
                    }
                }
            }
        }

        clinicDTO.setClinicConfigurationMappingDTOS(
            clinicConfigurationMappingService.processClinicConfigurationMappingHashmap(relation));

        return clinicDTO;
    }

    /*
     * Converts this {@link Clinic} object to an {@link ClinicDTO} object dropping bundle dto to avoid infinite loop.
     *
     * @return An {@link ClinicDTO} object based on this {@link Clinic}
     * object.
     */
    public ClinicDTO mapWithoutBundle(Clinic clinic) {
        ClinicDTO clinicDTO = new ClinicDTO();
        clinicDTO.setId(clinic.getId());
        clinicDTO.setDescription(clinic.getDescription());
        clinicDTO.setName(clinic.getName());
        clinicDTO.setEmail(clinic.getEmail());

        Map<ClinicConfigurationMapping, List<ClinicConfigurationMapping>> relation = new HashMap<>();
        if (clinic.getClinicConfigurationMappings() != null) {
            for (ClinicConfigurationMapping clinicConfigurationMapping : clinic.getClinicConfigurationMappings()) {
                ClinicConfiguration parent = clinicConfigurationMapping.getClinicConfiguration().getParent();
                if (parent != null) {
                    ClinicConfigurationMapping result = clinic.getClinicConfigurationMappings().stream()
                        .filter(obj -> obj.getClinicConfiguration()
                            .equals(clinicConfigurationMapping.getClinicConfiguration().getParent()))
                        .findFirst()
                        .orElse(null);
                    List<ClinicConfigurationMapping> newList = relation.get(result);
                    if (newList == null) {
                        newList = new ArrayList<>();
                    }
                    newList.add(clinicConfigurationMapping);
                    relation.put(result, newList);
                } else {
                    if (!relation.containsKey(clinicConfigurationMapping)) {
                        relation.put(clinicConfigurationMapping, new ArrayList<>());
                    }
                }
            }
        }

        clinicDTO.setClinicConfigurationMappingDTOS(
            clinicConfigurationMappingService.processClinicConfigurationMappingHashmap(relation));

        return clinicDTO;
    }
}