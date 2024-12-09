package de.imi.mopat.model.dto;

public class ClinicConfigurationGroupMappingDTO {

    private Long id;
    private ConfigurationGroupDTO configurationGroupDTO;
    private String infoName;

    public ClinicConfigurationGroupMappingDTO() {
    }

    public ClinicConfigurationGroupMappingDTO(ConfigurationGroupDTO configurationGroupDTO, String infoName){
        this.configurationGroupDTO=configurationGroupDTO;
        this.infoName=infoName;
    }

    public ConfigurationGroupDTO getConfigurationGroupDTO(){
        return configurationGroupDTO;
    }

    public void setConfigurationGroupDTO(ConfigurationGroupDTO configurationGroupDTO){
        this.configurationGroupDTO=configurationGroupDTO;
    }

    public String getInfoName(){
        return this.infoName;
    }

    public void setInfoName(String infoName){
        this.infoName=infoName;

    }


}