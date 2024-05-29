package de.imi.mopat.model;

import de.imi.mopat.model.dto.ConfigurationDTO;
import de.imi.mopat.model.enumeration.ConfigurationType;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * The database table model for table <i>configuration</i> within the column 'pattern'. The pattern
 * configuration object is a configuration object with the type= 'PATTERN' and configurationType =
 * 'PATTERN'.
 */
@Entity
@DiscriminatorValue("PATTERN")
public class PatternConfiguration extends Configuration implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "pattern")
    private String pattern;

    //default constructor (in protected state), should not be accessible to
    // anything else but the JPA implementation (here: Hibernate) and the
    // JUnit tests
    protected PatternConfiguration() {
    }

    public PatternConfiguration(final String pattern, final Long id, final String entityClass,
        final String attribute, final String value, final ConfigurationType configurationType,
        final String labelMessageCode, final String descriptionMessageCode, final String testMethod,
        final String updateMethod, final String name, final Integer position,
        final ConfigurationGroup configurationGroup) {
        super(entityClass, attribute, configurationType, labelMessageCode, descriptionMessageCode,
            testMethod, updateMethod, position, configurationGroup);
        this.pattern = pattern;
    }

    public ConfigurationDTO toConfigurationDTO() {
        ConfigurationDTO configurationDTO = super.toConfigurationDTO();
        configurationDTO.setPattern(this.pattern);

        return configurationDTO;
    }

    /**
     * Returns the pattern of this pattern configuration.
     *
     * @return The pattern of this pattern configuration.
     */
    public String getPattern() {
        return pattern;
    }

    public void setPattern(final String pattern) {
        this.pattern = pattern;
    }
}