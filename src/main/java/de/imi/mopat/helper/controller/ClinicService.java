package de.imi.mopat.helper.controller;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.imi.mopat.model.*;
import de.imi.mopat.model.dto.BundleClinicDTO;
import de.imi.mopat.dao.ClinicDao;
import de.imi.mopat.helper.model.ClinicDTOMapper;
import de.imi.mopat.model.Clinic;
import de.imi.mopat.model.dto.ClinicDTO;
import java.util.List;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClinicService {

    private static final org.slf4j.Logger LOGGER =
        org.slf4j.LoggerFactory.getLogger(ClinicService.class);

    @Autowired
    ClinicDTOMapper clinicDTOMapper;
    @Autowired
    private ClinicDao clinicDao;

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

    public List<ClinicDTO> getAllClinics(){
        return clinicDao.getAllElements().stream()
            .map(clinicDTOMapper)
            .toList();
    }

    public void merge(Clinic clinic) {
        clinicDao.merge(clinic);
    }
}
