package de.imi.mopat.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.imi.mopat.helper.model.UUIDGenerator;
import de.imi.mopat.model.dto.ConfigurationDTO;
import de.imi.mopat.model.dto.ConfigurationGroupDTO;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.eclipse.persistence.annotations.CascadeOnDelete;

/**
 * The database table model for table <i>configuration_group</i>. The configuration group object
 * contains associated configurations and assists with grouping those.
 */
@Entity
@Table(name = "configuration_group")
public class ConfigurationGroup implements Serializable {

    @JsonIgnore
    @Column(name = "uuid")
    private String uuid = UUIDGenerator.createUUID();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "position")
    private Integer position;

    @Column(name = "label_message_code", nullable = false)
    private String labelMessageCode;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "repeating")
    private boolean repeating;

    @OneToMany(mappedBy = "configurationGroup", cascade = {CascadeType.ALL}, orphanRemoval = true)
    @CascadeOnDelete
    private List<Configuration> configurations = new ArrayList<>();

    public ConfigurationGroup() {
    }

    /**
     * Transforms the configuration object to {@link ConfigurationDTO configurationDTO} object
     *
     * @return The new configurationGroupDTO object
     */
    public ConfigurationGroupDTO toConfigurationGroupDTO() {
        ConfigurationGroupDTO configurationGroupDTO = new ConfigurationGroupDTO();
        configurationGroupDTO.setId(this.id);
        configurationGroupDTO.setPosition(this.position);
        configurationGroupDTO.setLabelMessageCode(this.labelMessageCode);
        configurationGroupDTO.setName(this.name);
        configurationGroupDTO.setRepeating(this.repeating);

        return configurationGroupDTO;
    }

    /**
     * Returns the positon of the current ConfigurationGroup object.
     *
     * @return The position of the current ConfigurationGroup object.
     */
    public Integer getPosition() {
        return position;
    }

    /**
     * Sets the position of the current ConfigurationGroup object.
     *
     * @param position The position to be set.
     */
    public void setPosition(final Integer position) {
        this.position = position;
    }

    /**
     * Returns the name of the current ConfigurationGroup object.
     *
     * @return The name of the current ConfigurationGroup object.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the current ConfigurationGroup object.
     *
     * @param name The name to be set.
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Returns the UUID of the current ConfigurationGroup object.
     *
     * @return The ConfigurationGroup's uuid.
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Returns the id of the current ConfigurationGroup object.
     *
     * @return The id of the current ConfigurationGroup object.
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the LabelMessageCode of the current ConfigurationGroup object.
     *
     * @param labelMessageCode The labelMessageCode to be set.
     */
    public void setLabelMessageCode(final String labelMessageCode) {
        this.labelMessageCode = labelMessageCode;
    }

    /**
     * Returns the LabelMessageCode of the current ConfigurationGroup object.
     *
     * @return The labelMessageCode of the current ConfigurationGroup object.
     */
    public String getLabelMessageCode() {
        return labelMessageCode;
    }

    /**
     * Returns repeating which contains true or false if the ConfigurationGroup is repeating or
     * not.
     *
     * @return True if ConfigurationGroup object is repeating, false if it's not.
     */
    public boolean isRepeating() {
        return repeating;
    }

    /**
     * Sets the repeating value of the ConfigurationGroup object.
     *
     * @param repeating The value to be set.
     */
    public void setRepeating(final boolean repeating) {
        this.repeating = repeating;
    }

    /**
     * Returns the list of {@link Configuration} objects adhering to the current ConfigurationGroup
     * object sorted by position.
     *
     * @return List of {@link Configuration} objects.
     */
    public List<Configuration> getConfigurations() {
        Collections.sort(configurations, new Comparator<Configuration>() {
            @Override
            public int compare(final Configuration o1, final Configuration o2) {
                return o1.getPosition().compareTo(o2.getPosition());
            }
        });
        return configurations;
    }

    /**
     * Sets the list of {@link Configuration} objects of the current ConfigurationGroup object.
     *
     * @param configurations The list of {@link Configuration} objects to be set.
     */
    public void setConfigurations(final List<Configuration> configurations) {
        this.configurations = configurations;
    }

    /**
     * The equals method to compare the current ConfigurationGroup object to another object by
     * comparing the objects' uuids.
     *
     * @param object The object to compare with this object.
     * @return True if the objects' uuids equals, false if not.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == null) {
            return false;
        }
        if (!(object instanceof ConfigurationGroup)) {
            return false;
        }
        ConfigurationGroup other = (ConfigurationGroup) object;
        return other.getUuid().equals(this.uuid);
    }
}