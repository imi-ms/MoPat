package de.imi.mopat.model;

import de.imi.mopat.model.dto.ClinicConfigurationDTO;
import de.imi.mopat.model.enumeration.ConfigurationType;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.List;

/**
 * The database table model for table <i>configuration</i> join together with the table
 * <i>SelectClinicConfiguration_OPTIONS</i>. The select configuration object is a configuration object
 * with the type= 'SELECT' and configurationType = 'SELECT'.
 */
@Entity
@DiscriminatorValue("SELECT")
public class SelectClinicConfiguration extends ClinicConfiguration implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ElementCollection
    @Column(name = "options")
    @ManyToOne(cascade = {CascadeType.ALL})
    private List<String> options;

    //default constructor (in protected state), should not be accessible to
    // anything else but the JPA implementation (here: Hibernate) and the
    // JUnit tests
    protected SelectClinicConfiguration() {
    }

    public SelectClinicConfiguration(final List<String> options, final Long id, final String entityClass,
        final String attribute, final String value, final ConfigurationType configurationType,
        final String labelMessageCode, final String descriptionMessageCode, final String testMethod,
        final String updateMethod, final String name, final Integer position) {
        super(entityClass, attribute, configurationType, labelMessageCode, descriptionMessageCode,
            testMethod, updateMethod, position);
        this.options = options;
    }

    @Override
    public ClinicConfigurationDTO toClinicConfigurationDTO() {
        ClinicConfigurationDTO configurationDTO = super.toClinicConfigurationDTO();
        configurationDTO.setOptions(this.options);

        return configurationDTO;
    }

    /**
     * Returns a list with options for this select configuration. Each option is a string.
     *
     * @return A list with options for this select configuration.
     */
    public List<String> getOptions() {
        return options;
    }

    public void setOptions(final List<String> options) {
        this.options = options;
    }
}