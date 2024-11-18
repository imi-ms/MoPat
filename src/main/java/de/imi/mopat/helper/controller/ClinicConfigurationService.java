package de.imi.mopat.helper.controller;

import de.imi.mopat.dao.ConfigurationGroupDao;
import de.imi.mopat.model.ClinicConfiguration;
import de.imi.mopat.model.ConfigurationGroup;
import de.imi.mopat.model.dto.ClinicConfigurationDTO;
import de.imi.mopat.model.dto.ConfigurationGroupDTO;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class ClinicConfigurationService {
    @Autowired
    private ConfigurationGroupDao configurationGroupDao;

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
            List<ConfigurationGroupDTO> configurationGroupDTOS = new ArrayList<>();
            for(ConfigurationGroup configurationGroup : configurationGroupDao.getConfigurationGroups(childDTO.getMappedConfigurationGroup())){
                configurationGroupDTOS.add(configurationGroup.toConfigurationGroupDTO());
            }
            childDTO.setMappedConfigurationGroupDTOS(configurationGroupDTOS);
            if (child.getChildren() != null && !child.getChildren().isEmpty()) {
                processChildrenElements(child, childDTO);
            }
            //Add dto after processing its children
            clinicConfigurationDTO.getChildren().add(childDTO);

        }
    }


}
