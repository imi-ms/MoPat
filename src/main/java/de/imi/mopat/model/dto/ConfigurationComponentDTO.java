package de.imi.mopat.model.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The class for {@link ConfigurationComponentDTO configurationComponentDTO} objects which store
 * {@link ConfigurationGroupDTO configurationGroupDTO} objects in a map to show and group those ones
 * in the view.
 */
public class ConfigurationComponentDTO {

    private Map<String, List<ConfigurationGroupDTO>> configurationGroupDTOs;
    private List<Long> configurationsToDelete = new ArrayList<>();
    private Map<Long, Boolean> imageDeleteMap = new HashMap<>();

    public ConfigurationComponentDTO(
        final Map<String, List<ConfigurationGroupDTO>> configurationGroupDTOs) {
        this.configurationGroupDTOs = configurationGroupDTOs;
    }

    /**
     * Returns the list of ids of {@link ConfigurationDTO} objects which are disposed to be
     * deleted.
     *
     * @return The id of the {@link ConfigurationDTO} objects.
     */
    public List<Long> getConfigurationsToDelete() {
        return configurationsToDelete;
    }

    /**
     * Sets the list of the ids of {@link ConfigurationDTO} objects which are disposed to be
     * deleted.
     *
     * @param configurationsToDelete List of the {@link ConfigurationDTO} objects' ids.
     */
    public void setConfigurationsToDelete(final List<Long> configurationsToDelete) {
        this.configurationsToDelete = configurationsToDelete;
    }

    /**
     * Returns a map with the id of a configuration object as key and a boolean as value. One entry
     * indicates if the image associated with the configuration object should be deleted.
     *
     * @return A map with configuration ids as keys.
     */
    public Map<Long, Boolean> getImageDeleteMap() {
        return imageDeleteMap;
    }

    /**
     * See {@link #getImageDeleteMap()} for a description.
     * <p>
     * Sets a new imageDeleteMap for this configurationDTO object.
     *
     * @param imageDeleteMap The new map, which indicates which configuration images should be
     *                       deleted
     */
    public void setImageDeleteMap(final Map<Long, Boolean> imageDeleteMap) {
        this.imageDeleteMap = imageDeleteMap;
    }

    /**
     * Returns the map of {@link ConfigurationGroupDTO} objects as value and a String as key.
     *
     * @return Map containing {@link ConfigurationGroupDTO} objects.
     */
    public Map<String, List<ConfigurationGroupDTO>> getConfigurationGroupDTOs() {
        return configurationGroupDTOs;
    }

    /**
     * Set the map of {ConfigurationGroupDTO} objects as value and a String as key
     *
     * @param configurationGroupDTOs Map containing {@link ConfigurationGroupDTO} objects to be
     *                               set.
     */
    public void setConfigurationGroupDTOs(
        final Map<String, List<ConfigurationGroupDTO>> configurationGroupDTOs) {
        this.configurationGroupDTOs = configurationGroupDTOs;
    }
}