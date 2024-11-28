package de.imi.mopat.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "clinic_configuration_group_mapping")
public class ClinicConfigurationGroupMapping implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "clinic_configuration_mapping_id", referencedColumnName = "id")
    private ClinicConfigurationMapping clinicConfigurationMapping = null;

    @ManyToOne
    @JoinColumn(name = "configuration_group_id", referencedColumnName = "id")
    private ConfigurationGroup configurationGroup = null;

    public ClinicConfigurationGroupMapping() {
    }

    public ClinicConfigurationGroupMapping(final ClinicConfigurationMapping clinicConfigurationMapping, final ConfigurationGroup configurationGroup) {
        this.configurationGroup=configurationGroup;
        this.clinicConfigurationMapping=clinicConfigurationMapping;
    }

    /**
     * Returns the id of the current configuration object.
     *
     * @return The current id of this configuration object. Might be
     * <code>null</code> for newly created objects. If <code>!null</code>, it's
     * never <code> &lt;= 0</code>.
     */
    public Long getId() {
        return id;
    }

    /**
     * Returns the ClinicConfigurationMapping of the current configuration object.
     *
     * @return The current ClinicConfigurationMapping of this configuration object. Might be
     * <code>null</code> for newly created objects.
     */
    public ClinicConfigurationMapping getClinicConfigurationMapping() {
        return clinicConfigurationMapping;
    }

    /**
     * Sets the ClinicConfigurationMapping of the current configuration object.
     *
     */
    public void setClinicConfigurationMapping(ClinicConfigurationMapping clinicConfigurationMapping) {
        this.clinicConfigurationMapping = clinicConfigurationMapping;
    }

    /**
     * Returns the ConfigurationGroup of the current configuration object.
     *
     * @return The current ConfigurationGroup of this configuration object. Might be
     * <code>null</code> for newly created objects.
     */
    public ConfigurationGroup getConfigurationGroup() {
        return configurationGroup;
    }

    /**
     * Sets the ConfigurationGroup of the current configuration object.
     *
     */
    public void setConfigurationGroup(ConfigurationGroup configurationGroup) {
        this.configurationGroup = configurationGroup;
    }
}
