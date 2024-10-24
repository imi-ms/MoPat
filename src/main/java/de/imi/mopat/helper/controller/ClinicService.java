package de.imi.mopat.helper.controller;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.imi.mopat.model.*;
import de.imi.mopat.model.dto.BundleClinicDTO;
import de.imi.mopat.model.dto.ClinicDTO;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClinicService {

    private static final org.slf4j.Logger LOGGER =
        org.slf4j.LoggerFactory.getLogger(Question.class);

    @Autowired
    private BundleClinicService bundleClinicService;

    @Autowired
    private ClinicConfigurationMappingService clinicConfigurationMappingService;

    /*
     * Converts this {@link Clinic} object to an {@link ClinicDTO} object.
     *
     * @return An {@link ClinicDTO} object based on this {@link Clinic}
     * object.
     */
    @JsonIgnore
    public ClinicDTO toClinicDTO(Clinic clinic) {
        ClinicDTO clinicDTO = new ClinicDTO();
        clinicDTO.setId(clinic.getId());
        clinicDTO.setDescription(clinic.getDescription());
        clinicDTO.setName(clinic.getName());
        clinicDTO.setEmail(clinic.getEmail());

        List<BundleClinicDTO> bundleClinicDTOs = new ArrayList<>();
        for (BundleClinic bundleClinic : clinic.getBundleClinics()) {
            bundleClinicDTOs.add(bundleClinicService.toBundleClinicDTO(clinicDTO,bundleClinic));
        }
        clinicDTO.setBundleClinicDTOs(bundleClinicDTOs);

        Map<ClinicConfigurationMapping, List<ClinicConfigurationMapping>> relation = new HashMap<>();
        for(ClinicConfigurationMapping clinicConfigurationMapping : clinic.getClinicConfigurationMappings()){
            ClinicConfiguration parent = clinicConfigurationMapping.getClinicConfiguration().getParent();
            if(parent != null){
                ClinicConfigurationMapping result = clinic.getClinicConfigurationMappings().stream()
                        .filter(obj -> obj.getClinicConfiguration().equals(clinicConfigurationMapping.getClinicConfiguration().getParent()))
                        .findFirst()
                        .orElse(null);
                List<ClinicConfigurationMapping> newList = relation.get(result);
                if(newList == null){
                    newList = new ArrayList<>();
                }
                newList.add(clinicConfigurationMapping);
                relation.put(result, newList);
            } else {
                if(!relation.containsKey(clinicConfigurationMapping)){
                    relation.put(clinicConfigurationMapping,new ArrayList<>());
                }
            }
        }

        clinicDTO.setClinicConfigurationMappingDTOS(clinicConfigurationMappingService.processClinicConfigurationMappingHashmap(relation));


        return clinicDTO;
    }


}
