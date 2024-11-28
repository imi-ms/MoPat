package de.imi.mopat.helper.model;

import de.imi.mopat.model.ClinicConfiguration;
import de.imi.mopat.model.dto.ClinicConfigurationDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.springframework.stereotype.Component;

@Component
public class ClinicConfigurationDTOMapper implements Function<ClinicConfiguration, ClinicConfigurationDTO> {

    /*
     * Converts this {@link ClinicConfiguration} object to an {@link ClinicConfigurationDTO} object.
     *
     * @return An {@link ClinicConfigurationDTO} object based on this {@link ClinicConfiguration}
     * object.
     */
    @Override
    public ClinicConfigurationDTO apply(ClinicConfiguration clinicConfiguration) {
        ClinicConfigurationDTO clinicConfigurationDTO = new ClinicConfigurationDTO();

        clinicConfigurationDTO.setId(clinicConfiguration.getId());
        clinicConfigurationDTO.setEntityClass(clinicConfiguration.getEntityClass());
        clinicConfigurationDTO.setAttribute(clinicConfiguration.getAttribute());
        clinicConfigurationDTO.setValue(clinicConfiguration.getValue());
        clinicConfigurationDTO.setConfigurationType(clinicConfiguration.getConfigurationType());
        clinicConfigurationDTO.setLabelMessageCode(clinicConfiguration.getLabelMessageCode());
        clinicConfigurationDTO.setDescriptionMessageCode(clinicConfiguration.getDescriptionMessageCode());
        clinicConfigurationDTO.setTestMethod(clinicConfiguration.getTestMethod());
        clinicConfigurationDTO.setUpdateMethod(clinicConfiguration.getUpdateMethod());
        clinicConfigurationDTO.setPosition(clinicConfiguration.getPosition());
        clinicConfigurationDTO.setMappedConfigurationGroup(clinicConfiguration.getMappedConfigurationGroup());

        //If parent not null set the DTO's parent
        if (clinicConfiguration.getParent() != null) {
            ClinicConfigurationDTO parentDTO = new ClinicConfigurationDTO();
            parentDTO.setId(clinicConfiguration.getParent().getId());
            parentDTO.setValue(clinicConfiguration.getParent().getValue());
            clinicConfigurationDTO.setParent(parentDTO);
        }

        //If children not empty or null set the DTO's children
        if (clinicConfiguration.getChildren() != null && !clinicConfiguration.getChildren().isEmpty()) {
            List<ClinicConfigurationDTO> childrenDTOs = new ArrayList<>();
            for (ClinicConfiguration child : clinicConfiguration.getChildren()) {
                ClinicConfigurationDTO childDTO = this.apply(child);
                childDTO.setId(child.getId());
                childDTO.setParent(clinicConfigurationDTO);
                childrenDTOs.add(childDTO);
            }
            clinicConfigurationDTO.setChildren(childrenDTOs);
        }

        return clinicConfigurationDTO;
    }
}