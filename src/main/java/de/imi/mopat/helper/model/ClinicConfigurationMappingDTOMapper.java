package de.imi.mopat.helper.model;

import de.imi.mopat.dao.ConfigurationGroupDao;
import de.imi.mopat.helper.controller.ClinicConfigurationMappingService;
import de.imi.mopat.helper.controller.ConfigurationService;
import de.imi.mopat.model.ClinicConfigurationMapping;
import de.imi.mopat.model.Configuration;
import de.imi.mopat.model.ConfigurationGroup;
import de.imi.mopat.model.dto.ClinicConfigurationDTO;
import de.imi.mopat.model.dto.ClinicConfigurationGroupMappingDTO;
import de.imi.mopat.model.dto.ClinicConfigurationMappingDTO;
import de.imi.mopat.model.dto.ConfigurationDTO;
import de.imi.mopat.model.dto.ConfigurationGroupDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ClinicConfigurationMappingDTOMapper implements
    Function<ClinicConfigurationMapping, ClinicConfigurationMappingDTO> {

    @Autowired
    private ClinicConfigurationDTOMapper clinicConfigurationDTOMapper;

    @Autowired
    private ConfigurationGroupDao configurationGroupDao;

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private ClinicConfigurationMappingService clinicConfigurationMappingService;

    /**
     * Converts this {@link ClinicConfigurationMapping} object to an {@link ClinicConfigurationMappingDTO} object.
     *
     * @return An {@link ClinicConfigurationMappingDTO} object based on this {@link ClinicConfigurationMapping} object.
     */
    @Override
    public ClinicConfigurationMappingDTO apply(ClinicConfigurationMapping clinicConfigurationMapping) {
        ClinicConfigurationMappingDTO clinicConfigurationMappingDTO = new ClinicConfigurationMappingDTO();
        ClinicConfigurationDTO clinicConfigurationDTO = clinicConfigurationDTOMapper.apply(
            clinicConfigurationMapping.getClinicConfiguration());

        clinicConfigurationMappingDTO.setId(clinicConfigurationMapping.getId());
        clinicConfigurationMappingDTO.setValue(clinicConfigurationMapping.getValue());
        clinicConfigurationMappingDTO.setConfigurationType(clinicConfigurationDTO.getConfigurationType());
        clinicConfigurationMappingDTO.setClinicConfigurationId(clinicConfigurationDTO.getId());
        clinicConfigurationMappingDTO.setUpdateMethod(clinicConfigurationDTO.getUpdateMethod());
        clinicConfigurationMappingDTO.setLabelMessageCode(clinicConfigurationDTO.getLabelMessageCode());
        clinicConfigurationMappingDTO.setPosition(clinicConfigurationDTO.getPosition());
        clinicConfigurationMappingDTO.setDescriptionMessageCode(clinicConfigurationDTO.getDescriptionMessageCode());
        clinicConfigurationMappingDTO.setAttribute(clinicConfigurationDTO.getAttribute());
        clinicConfigurationMappingDTO.setTestMethod(clinicConfigurationDTO.getTestMethod());
        if (clinicConfigurationDTO.getOptions() != null && !clinicConfigurationDTO.getOptions().isEmpty()) {
            clinicConfigurationMappingDTO.setOptions(clinicConfigurationDTO.getOptions());
        }
        List<ClinicConfigurationGroupMappingDTO> clinicConfigurationGroupMappingDTOS = new ArrayList<>();
        if (clinicConfigurationDTO.getMappedConfigurationGroup() != null) {
            List<ConfigurationGroupDTO> configurationGroupDTOS = new ArrayList<>();
            for (ConfigurationGroup configurationGroup : configurationGroupDao.getConfigurationGroups(
                clinicConfigurationDTO.getMappedConfigurationGroup())) {
                ConfigurationGroupDTO configurationGroupDTO = configurationGroup.toConfigurationGroupDTO();
                //Go through all adherent configurations
                List<ConfigurationDTO> configurationDTOs = new ArrayList<>();

                for (Configuration configuration1 : configurationGroup.getConfigurations()) {
                    if (clinicConfigurationMapping.getClinicConfiguration().getParent() == null) {
                        ConfigurationDTO configurationDTO1 = configuration1.toConfigurationDTO();

                        if (clinicConfigurationMapping.getClinicConfiguration().getChildren() != null
                            && !clinicConfigurationMapping.getClinicConfiguration().getChildren()
                            .isEmpty()) {
                            configurationService.processChildrenElements(configuration1, configurationDTO1);
                        }
                        configurationDTOs.add(configurationDTO1);
                    }
                }
                configurationGroupDTO.setConfigurationDTOs(configurationDTOs);
                configurationGroupDTOS.add(configurationGroupDTO);
            }
            for (ConfigurationGroupDTO configurationGroupDTO : configurationGroupDTOS) {
                clinicConfigurationGroupMappingDTOS.add(
                    new ClinicConfigurationGroupMappingDTO(configurationGroupDTO,
                        clinicConfigurationMappingService.getInfoName(configurationGroupDTO)));
            }
            clinicConfigurationMappingDTO.setMappedConfigurationGroupDTOS(clinicConfigurationGroupMappingDTOS);
        }
        if (clinicConfigurationMapping.getClinicConfigurationGroupMappings() != null
            && !clinicConfigurationMapping.getClinicConfigurationGroupMappings().isEmpty()) {
            //TODO single to multiple groups
            clinicConfigurationMappingDTO.setMappedConfigurationGroup(
                clinicConfigurationMapping.getClinicConfigurationGroupMappings().get(0).getConfigurationGroup()
                    .getName());
        }
        return clinicConfigurationMappingDTO;
    }
}