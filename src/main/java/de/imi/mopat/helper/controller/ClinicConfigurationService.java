package de.imi.mopat.helper.controller;

import de.imi.mopat.model.ClinicConfiguration;
import de.imi.mopat.model.dto.ClinicConfigurationDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class ClinicConfigurationService {


    /**
     * Function that recursively processes all children elements to allow the use of multiple nested elements
     *
     * @param clinicConfiguration    The configuration element
     * @param clinicConfigurationDTO The currently processed DTO
     */
    public void processChildrenElements(final ClinicConfiguration clinicConfiguration,
        final ClinicConfigurationDTO clinicConfigurationDTO) {
        //Set the children DTOs
        clinicConfigurationDTO.setChildren(new ArrayList<>());
        for (ClinicConfiguration child : clinicConfiguration.getChildren()) {
            ClinicConfigurationDTO childDTO = child.toClinicConfigurationDTO();

            if (child.getChildren() != null && !child.getChildren().isEmpty()) {
                processChildrenElements(child, childDTO);
            }
            //Add dto after processing its children
            clinicConfigurationDTO.getChildren().add(childDTO);

        }
    }


}
