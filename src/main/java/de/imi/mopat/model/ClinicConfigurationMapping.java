package de.imi.mopat.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "clinic_configuration_mapping")
public class ClinicConfigurationMapping implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "clinic_id", referencedColumnName = "id")
    private Clinic clinic;

    @ManyToOne
    @JoinColumn(name = "clinic_configuration_id", referencedColumnName = "id")
    private ClinicConfiguration clinicConfiguration;

    @Column(name = "value")
    private String value;

    @OneToMany(mappedBy = "clinicConfigurationMapping", cascade = {CascadeType.ALL}, orphanRemoval = true)
    private List<ClinicConfigurationGroupMapping> clinicConfigurationGroupMappings;


    public ClinicConfigurationMapping() {
    }

    public ClinicConfigurationMapping(final Clinic clinic, final ClinicConfiguration clinicConfiguration,
        final String value) {
        this.clinic = clinic;
        this.clinicConfiguration = clinicConfiguration;
        this.value = value;
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
     * Returns the value of the current configuration object.
     *
     * @return The value of the current configuration object. Might be
     * <code>null</code>.
     */
    public String getValue() {
        return value;
    }


    public ClinicConfiguration getClinicConfiguration() {
        return clinicConfiguration;
    }

    public void setClinicConfiguration(ClinicConfiguration clinicConfiguration) {
        this.clinicConfiguration = clinicConfiguration;
    }

    /**
     * See {@link Configuration#getValue()} for a description.
     * <p>
     * Sets a value for this configuration object. Trims it before setting.
     *
     * @param value The new value for this configuration object. Might be
     *              <code>null</code>.
     */
    public void setValue(final String value) {
        if (value == null) {
            this.value = null;
        } else {
            this.value = value.trim();
        }
    }

    public Clinic getClinic() {
        return clinic;
    }

    public void setClinic(Clinic clinic) {
        this.clinic=clinic;
    }

    public List<ClinicConfigurationGroupMapping> getClinicConfigurationGroupMappings() {
        return clinicConfigurationGroupMappings;
    }

    public void setClinicConfigurationGroupMappings(
        List<ClinicConfigurationGroupMapping> clinicConfigurationGroupMappings) {
        this.clinicConfigurationGroupMappings = clinicConfigurationGroupMappings;
    }


}
