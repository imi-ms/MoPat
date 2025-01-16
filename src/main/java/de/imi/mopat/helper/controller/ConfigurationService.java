package de.imi.mopat.helper.controller;

import de.imi.mopat.dao.ConfigurationGroupDao;
import de.imi.mopat.model.Configuration;
import de.imi.mopat.model.dto.ConfigurationDTO;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConfigurationService {
    @Autowired
    private ConfigurationGroupDao configurationGroupDao;

    /**
     * Function that recursively processes all children elements to allow the use of multiple nested
     * elements
     *
     * @param configuration    The configuration element
     * @param configurationDTO The currently processed DTO
     */
    public void processChildrenElements(final Configuration configuration,
        final ConfigurationDTO configurationDTO) {
        //Set the children DTOs
        configurationDTO.setChildren(new ArrayList<>());
        for (Configuration child : configuration.getChildren()) {
            ConfigurationDTO childDTO = child.toConfigurationDTO();

            if (child.getChildren() != null && !child.getChildren().isEmpty()) {
                processChildrenElements(child, childDTO);
            }
            //Add dto after processing its children
            configurationDTO.getChildren().add(childDTO);

        }
    }


}
