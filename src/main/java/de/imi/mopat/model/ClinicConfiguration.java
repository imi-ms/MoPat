package de.imi.mopat.model;

import de.imi.mopat.model.dto.ClinicConfigurationDTO;
import de.imi.mopat.model.enumeration.ConfigurationType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import de.imi.mopat.helper.controller.Constants;
import de.imi.mopat.helper.model.UUIDGenerator;
import de.imi.mopat.model.dto.ConfigurationDTO;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.CascadeType;
import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.eclipse.persistence.annotations.CascadeOnDelete;

@Entity
@Table(name = "clinic_configuration")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("GENERAL")
public class ClinicConfiguration implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @JsonIgnore
    @Column(name = "uuid")
    private String uuid = UUIDGenerator.createUUID();

    @ManyToOne
    @JoinColumn(name = "parent")
    private ClinicConfiguration parent = null;

    @Column(name = "position", nullable = false)
    private Integer position;

    @Column(name = "class", nullable = false)
    private String entityClass;

    @Column(name = "attribute", nullable = false)
    private String attribute;

    @Column(name = "value", nullable = true)
    private String value;

    @Enumerated(EnumType.STRING)
    @Column(name = "configuration_type", nullable = false)
    private ConfigurationType configurationType;

    @Column(name = "label_message_code", nullable = false)
    private String labelMessageCode;

    @Column(name = "description_message_code", nullable = true)
    private String descriptionMessageCode;

    @Column(name = "test_method", nullable = true)
    private String testMethod;

    @Column(name = "update_method", nullable = true)
    private String updateMethod;

    @Column(name = "mapped_configuration_group", nullable = true)
    private String mappedConfigurationGroup;

    @OneToMany(mappedBy = "parent", cascade = {CascadeType.ALL}, orphanRemoval = true)
    @CascadeOnDelete
    private List<ClinicConfiguration> children = null;

    public ClinicConfiguration() {
    }

    public ClinicConfiguration(final String entityClass, final String attribute,
        final ConfigurationType configurationType, final String labelMessageCode,
        final String descriptionMessageCode, final String testMethod, final String updateMethod,
        final Integer position) {
        this.entityClass = entityClass;
        this.attribute = attribute;
        this.configurationType = configurationType;
        this.labelMessageCode = labelMessageCode;
        this.descriptionMessageCode = descriptionMessageCode;
        this.testMethod = testMethod;
        this.updateMethod = updateMethod;
        this.position = position;
    }

    /**
     * Converts the {@link Configuration} object to a {@link ConfigurationDTO} object.
     *
     * @return ConfigurationDTO The configurationDTO object based on this configuration object.
     */
    public ClinicConfigurationDTO toClinicConfigurationDTO() {
        ClinicConfigurationDTO clinicConfigurationDTO = new ClinicConfigurationDTO();

        clinicConfigurationDTO.setId(this.getId());
        clinicConfigurationDTO.setEntityClass(this.getEntityClass());
        clinicConfigurationDTO.setAttribute(this.getAttribute());
        clinicConfigurationDTO.setValue(this.getValue());
        clinicConfigurationDTO.setConfigurationType(this.getConfigurationType());
        clinicConfigurationDTO.setLabelMessageCode(this.getLabelMessageCode());
        clinicConfigurationDTO.setDescriptionMessageCode(this.getDescriptionMessageCode());
        clinicConfigurationDTO.setTestMethod(this.getTestMethod());
        clinicConfigurationDTO.setUpdateMethod(this.getUpdateMethod());
        clinicConfigurationDTO.setPosition(this.getPosition());
        clinicConfigurationDTO.setMappedConfigurationGroup(this.getMappedConfigurationGroup());

        //If parent not null set the DTO's parent
        if (this.parent != null) {
            ClinicConfigurationDTO parentDTO = new ClinicConfigurationDTO();
            parentDTO.setId(this.getParent().getId());
            parentDTO.setValue(this.getParent().getValue());
            clinicConfigurationDTO.setParent(parentDTO);
        }

        //If children not empty or null set the DTO's children
        if (this.getChildren() != null && !this.getChildren().isEmpty()) {
            List<ClinicConfigurationDTO> childrenDTOs = new ArrayList<>();
            for (ClinicConfiguration child : this.getChildren()) {
                ClinicConfigurationDTO childDTO = new ClinicConfigurationDTO();
                childDTO.setId(child.getId());
                childDTO.setParent(clinicConfigurationDTO);
                childrenDTOs.add(childDTO);
            }
            clinicConfigurationDTO.setChildren(childrenDTOs);
        }

        return clinicConfigurationDTO;
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
     * Returns the uuid of the current configuration object.
     *
     * @return The current uuid of this configuration object. Might be
     * <code>null</code> for newly created objects.
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Returns the entityClass of the current configuration object. The entityClass indicates where this conifguration
     * is used. If its used in more than one class, the entityClass is CLASS_GLOBAL defined in {@link Constants}.
     *
     * @return The current uuid of this configuration object. Might be
     * <code>null</code> for newly created objects.
     */
    public String getEntityClass() {
        return entityClass;
    }

    public void setAttribute(String attribute) {
        this.attribute= attribute;
    }

    /**
     * Returns the attribute of the current configuration object.
     *
     * @return The attribute of the current configuration object. Is never
     * <code>null</code>. characters.
     */
    public String getAttribute() {
        return attribute;
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

    /**
     * Returns the name of the testMethod of the current configuration object. If its not
     * <code>null</code> this method will be called before saving this object to validate the
     * value.
     *
     * @return The name of the test method of the current configuration object. Might be
     * <code>null</code>.
     */
    public String getTestMethod() {
        return testMethod;
    }

    /**
     * Returns the name of the updateMethod of the current configuration object. If its not
     * <code>null</code> this method will be called after saving this object.
     *
     * @return The name of the update method of the current configuration object. Might be
     * <code>null</code>.
     */
    public String getUpdateMethod() {
        return updateMethod;
    }

    /**
     * Returns the descriptionMessageCode of the current configuration object.
     *
     * @return The descriptionMessageCode of the current configuration object. Might be
     * <code>null</code>.
     */
    public String getDescriptionMessageCode() {
        return descriptionMessageCode;
    }

    /**
     * Returns the labelMessageCode of the current configuration object.
     *
     * @return The labelMessageCode of the current configuration object. Is never <code>null</code>.
     */
    public String getLabelMessageCode() {
        return labelMessageCode;
    }

    /**
     * Returns the {@link ConfigurationType} of the current configuration object.
     *
     * @return The {@link ConfigurationType} of the current configuration object. Is never
     * <code>null</code>.
     */
    public ConfigurationType getConfigurationType() {
        return configurationType;
    }


    public void setConfigurationType(ConfigurationType configurationType) {
        this.configurationType = configurationType;
    }
    /**
     * Returns the Configuration object that is parent of the current configuration object.
     *
     * @return The parent configuration object.
     */
    public ClinicConfiguration getParent() {
        return parent;
    }

    /**
     * Sets the configuration parent object to the current configuration object.
     *
     * @param parent The configuration object that's going to be set as parent.
     */
    public void setParent(final ClinicConfiguration parent) {
        this.parent = parent;
    }

    /**
     * Returns the {@link List} object which contains children configurations of the current configuration object.
     *
     * @return The list containing the children configurations of the current configuration object.
     */
    public List<ClinicConfiguration> getChildren() {
        if (this.children != null) {
            Collections.sort(children, new Comparator<ClinicConfiguration>() {
                @Override
                public int compare(final ClinicConfiguration o1, final ClinicConfiguration o2) {
                    return o1.getPosition().compareTo(o2.getPosition());
                }
            });
        }
        return children;
    }

    /**
     * Sets the children configurations to the current configuration object.
     *
     * @param children The child configurations list object of the current configuration object.
     */
    public void setChildren(final List<ClinicConfiguration> children) {
        this.children = children;
    }

    /**
     * Returns the position of the current configuration object it has got in the order of {@link ConfigurationGroup}'s
     * adhering list of configurations.
     *
     * @return Integer The position of the current configuration object.
     */
    public Integer getPosition() {
        return position;
    }

    /**
     * Sets the position of the current configuration object it has got in the order of the {@link ConfigurationGroup}'s
     * adhering list of configurations.
     *
     * @param position The position to be set for the configuration object.
     */
    public void setPosition(final Integer position) {
        assert position != null : "The given position was null";
        assert position > 0 : "The given position was zero or less";
        this.position = position;
    }

    /**
     * Returns the mappedConfigurationGroup.
     *
     * @return The current mappedConfigurationGroup. Might be
     * <code>null</code>.
     */
    public String getMappedConfigurationGroup() {
        return mappedConfigurationGroup;
    }

    /**
     * Sets the mappedConfigurationGroup for the current ClinicConfiguration object.
     *
     * @param mappedConfigurationGroup The mapped configuration group from global config.
     */
    public void setMappedConfigurationGroup(String mappedConfigurationGroup) {
        this.mappedConfigurationGroup = mappedConfigurationGroup;
    }


}
