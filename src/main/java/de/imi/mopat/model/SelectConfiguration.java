package de.imi.mopat.model;

import de.imi.mopat.model.dto.ConfigurationDTO;
import de.imi.mopat.model.enumeration.ConfigurationType;

import java.io.Serializable;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

/**
 * The database table model for table <i>configuration</i> join together with the table
 * <i>SelectConfiguration_OPTIONS</i>. The select configuration object is a configuration object
 * with the type= 'SELECT' and configurationType = 'SELECT'.
 */
@Entity
@DiscriminatorValue("SELECT")
public class SelectConfiguration extends Configuration implements Serializable {

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
    protected SelectConfiguration() {
    }

    public SelectConfiguration(final List<String> options, final Long id, final String entityClass,
        final String attribute, final String value, final ConfigurationType configurationType,
        final String labelMessageCode, final String descriptionMessageCode, final String testMethod,
        final String updateMethod, final Integer position,
        final ConfigurationGroup configurationGroup) {
        super(entityClass, attribute, configurationType, labelMessageCode, descriptionMessageCode,
            testMethod, updateMethod, position, configurationGroup);
        this.options = options;
    }

    @Override
    public ConfigurationDTO toConfigurationDTO() {
        ConfigurationDTO configurationDTO = super.toConfigurationDTO();
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