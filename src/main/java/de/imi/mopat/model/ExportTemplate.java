package de.imi.mopat.model;

import de.imi.mopat.dao.ConfigurationGroupDao;
import de.imi.mopat.dao.ExportTemplateDao;
import de.imi.mopat.helper.model.UUIDGenerator;
import de.imi.mopat.model.enumeration.ExportEncounterFieldType;
import de.imi.mopat.model.enumeration.ExportScoreFieldType;
import de.imi.mopat.model.enumeration.ExportTemplateType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * The database table model for table <i>export_template</i>. An export template belongs to a
 * questionnaire and represents a collection of {@link ExportRule export rules}. In addition an
 * export template belongs to a specific {@link ExportTemplateType} to indicate how the template
 * should be exported.
 */
@Entity
@Table(name = "export_template")
public class ExportTemplate implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @Column(name = "uuid")
    private String uuid = UUIDGenerator.createUUID();

    @Size(max = 255)
    @Column(name = "name")
    private String name;

    @Column(name = "export_template_type")
    @Enumerated(EnumType.STRING)
    private ExportTemplateType exportTemplateType;

    @Size(max = 255)
    @Column(name = "filename")
    private String filename;

    @Size(max = 255)
    @Column(name = "original_filename")
    private String originalFilename;

    @OneToOne
    @JoinColumn(name = "configuration_group", referencedColumnName = "id")
    private ConfigurationGroup configurationGroup;

    @ManyToOne
    @JoinColumn(name = "questionnaire_id", referencedColumnName = "id")
    private Questionnaire questionnaire;

    @OneToMany(mappedBy = "exportTemplate", cascade = {CascadeType.ALL}, orphanRemoval = true)
    private Set<ExportRule> exportRules = new HashSet<>();

    @ManyToMany(mappedBy = "exportTemplates", cascade = CascadeType.ALL)
    private Set<BundleQuestionnaire> bundleQuestionnaires = new HashSet<>();

    @OneToMany(mappedBy = "exportTemplate", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<EncounterExportTemplate> encounterExportTemplates = new HashSet<>();

    /**
     * Default constructor (in protected state), should not be accessible to anything else but the
     * JPA implementation (here: Hibernate) and the JUnit tests.
     */
    public ExportTemplate() {

    }

    public ExportTemplate(final String name, final ExportTemplateType exportTemplateType,
        final String filename, final ConfigurationGroup configurationGroup,
        final Questionnaire questionnaire) {
        this.name = name;
        this.exportTemplateType = exportTemplateType;
        this.filename = filename;
        this.configurationGroup = configurationGroup;
        this.questionnaire = questionnaire;
    }

    /**
     * Creates new ExportTemplates for each configured
     * {@link ConfigurationGroup ConfigurationGroups} and returns them as list.
     *
     * @param exportTemplateName    Name of the stored
     *                              {@link ExportTemplateType ExportTemplateTypes}.
     * @param exportTemplateType    Containing the {@link ExportTemplateType} to specify for which
     *                              configurationGroups the templates has to be created.
     * @param file                  The uploaded file.
     * @param configurationGroupDao Data access object holding connection to the
     *                              {@link ConfigurationGroup} data base.
     * @param exportTemplateDao     Data access object holding connection to the ExportTemplate data
     *                              base.
     * @return List of {@link ExportTemplateType ExportTemplateTypes} created for the configured
     * configuration groups.
     */
    public static List<ExportTemplate> createExportTemplates(final String exportTemplateName,
        final ExportTemplateType exportTemplateType, final MultipartFile file,
        final ConfigurationGroupDao configurationGroupDao,
        final ExportTemplateDao exportTemplateDao) {
        List<ConfigurationGroup> configurationGroups = configurationGroupDao.getConfigurationGroups(
            exportTemplateType.getConfigurationMessageCode());
        List<ExportTemplate> exportTemplates = new ArrayList<>();

        String fileNameWithOutExt = exportTemplateName;
        String originalFilename = exportTemplateName;
        if (file != null) {
            originalFilename = file.getOriginalFilename();
            fileNameWithOutExt = FilenameUtils.removeExtension(originalFilename);
        }

        for (ConfigurationGroup configurationGroup : configurationGroups) {
            // Include the original ODM as export template
            ExportTemplate exportTemplate = new ExportTemplate();
            // Set the export template properties
            exportTemplate.setName(exportTemplateName);
            exportTemplate.setFilename(fileNameWithOutExt);
            exportTemplate.setOriginalFilename(originalFilename);
            exportTemplate.setExportTemplateType(exportTemplateType);
            exportTemplate.setConfigurationGroup(configurationGroup);
            exportTemplateDao.merge(exportTemplate);
            exportTemplates.add(exportTemplate);
        }
        return exportTemplates;
    }

    /**
     * Returns the id of this export template object.
     *
     * @return The id of this export template object.
     */
    public Long getId() {
        return id;
    }

    /**
     * Returns the uuid of this export template object.
     *
     * @return The uuid of this export template object.
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Return the name of this export template object. The name is used for display purposes.
     *
     * @return The name of this export template object.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name which is set by the user who uploaded the template.
     *
     * @param name The name of this export template object.
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Returns the {@link ExportTemplateType} for this export template object.
     * <p>
     * An {@link ExportTemplateType} object represents how an export template object should be
     * exported.
     *
     * @return An {@link ExportTemplateType} object. Can not be
     * <code>null</code>.
     */
    public ExportTemplateType getExportTemplateType() {
        return this.exportTemplateType;
    }

    /**
     * Sets the {@link ExportTemplateType} object for this export template object.
     *
     * @param exportTemplateType The new {@link ExportTemplateType} object to be set.
     */
    public void setExportTemplateType(final ExportTemplateType exportTemplateType) {
        assert exportTemplateType != null : "The ExportTemplateType was null";
        this.exportTemplateType = exportTemplateType;
    }

    /**
     * Returns the filename on disk of this export template. Under this name the template file is
     * found in the object storage.
     *
     * @return The filename on disk. Can not be <code>null</code>.
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Sets the filename of this export template object. The template file is saved under this name
     * in the object storage.
     *
     * @param filename The filename of this export template object. The template file is saved under
     *                 this name in the object storage.
     */
    public void setFilename(final String filename) {
        assert filename != null : "the filename was null";
        this.filename = filename;
    }

    /**
     * Returns the original filename of this export template object. It reflects the name of the
     * file at the creation time of this object.
     *
     * @return Returns the original filename of this export template object. It reflects the name of
     * the file at the creation time of this object. Can not be <code>null</code>.
     */
    public String getOriginalFilename() {
        return originalFilename;
    }

    /**
     * Sets the original filename of this export template object.
     *
     * @param originalFilename The original filename of this export template object.
     */
    public void setOriginalFilename(final String originalFilename) {
        assert originalFilename != null : "The originalFilename was null";
        this.originalFilename = originalFilename;
    }

    /**
     * Returns the {@link ConfigurationGroup} of this export template object.
     *
     * @return Returns the {@link ConfigurationGroup} of this export template object. Must not be
     * <code>null</code>.
     */
    public ConfigurationGroup getConfigurationGroup() {
        return configurationGroup;
    }

    /**
     * Sets the {@link ConfigurationGroup} of this export template object.
     *
     * @param configurationGroup The configuratin group of this export template object.
     */
    public void setConfigurationGroup(final ConfigurationGroup configurationGroup) {
        this.configurationGroup = configurationGroup;
    }

    /**
     * Return the {@link Questionnaire} object which belongs to this export template.
     *
     * @return An {@link Questionnaire} object. Is never <code>null</code>.
     */
    public Questionnaire getQuestionnaire() {
        return questionnaire;
    }

    /**
     * Sets the {@link Questionnaire} object.
     *
     * @param questionnaire The new {@link Questionnaire} object to be set. Can not be
     *                      <code>null</code>.
     */
    public void setQuestionnaire(final Questionnaire questionnaire) {
        assert questionnaire != null : "The given Questionnaire is null";
        this.questionnaire = questionnaire;
        if (!questionnaire.getExportTemplates().contains(this)) {
            questionnaire.addExportTemplate(this);
        }
    }

    /**
     * Returns all {@link ExportRule} objects which belongs to this export template object.
     *
     * @return The current set of {@link ExportRule} objects. Can not be
     * <code>null</code>. Is unmodifiable.
     */
    public Set<ExportRule> getExportRules() {
        return Collections.unmodifiableSet(exportRules);
    }

    /**
     * Adds a set of {@link ExportRule} objects to this export template.
     *
     * @param exportRules The set of {@link ExportRule} objects to be added. Can not be
     *                    <code>null</code>.
     */
    public void addExportRules(final Set<ExportRule> exportRules) {
        assert exportRules != null : "The given ExportRules were null";
        for (ExportRule exportRule : exportRules) {
            addExportRule(exportRule);
        }
    }

    /**
     * Adds an {@link ExportRule} object to this export template.
     *
     * @param exportRule The {@link ExportRule} object to be added. Can not be
     *                   <code>null</code>.
     */
    public void addExportRule(final ExportRule exportRule) {
        assert exportRule != null : "The given ExportRule was null";
        exportRules.add(exportRule);
        // make sure the objects know each other
        if (exportRule.getExportTemplate() == null || !exportRule.getExportTemplate()
            .equals(this)) {
            exportRule.setExportTemplate(this);
        }
    }

    /**
     * Removes an {@link ExportRule} object from this object.
     *
     * @param exportRule The {@link ExportRule} object to be removed. Can not be
     *                   <code>null</code>.
     */
    public void removeExportRule(final ExportRule exportRule) {
        assert exportRule != null : "The given ExportRule was null";
        exportRules.remove(exportRule);
        // make sure the objects know each other
        if (exportRule.getExportTemplate() != null && exportRule.getExportTemplate().equals(this)) {
            exportRule.removeExportTemplate();
        }
    }

    /**
     * Returns the set of {@link ExportRuleEncounter} objects which belong to the given encounter
     * field.
     *
     * @param encounterField The {@link ExportEncounterFieldType} object to be searched for.
     * @return The set of {@link ExportRuleEncounter} objects which belong to the given encounter
     * field. Can not be <code>null</code>. Might be empty.
     */
    public Set<ExportRuleEncounter> getExportRulesByEncounterField(
        final ExportEncounterFieldType encounterField) {
        Set<ExportRuleEncounter> exportRuleSet = new HashSet<>();
        for (ExportRule rule : this.exportRules) {
            if (rule instanceof ExportRuleEncounter
                && ((ExportRuleEncounter) rule).getEncounterField() == encounterField) {
                exportRuleSet.add(((ExportRuleEncounter) rule));
            }
        }
        return exportRuleSet;
    }

    /**
     * Returns a list of all export fields which belong to the given
     * {@link ExportEncounterFieldType} object.
     *
     * @param encounterField The {@link ExportEncounterFieldType} object to be searched for.
     * @return A list of export fields which belong to the given {@link ExportEncounterFieldType}
     * object. Can not be <code>null</code>.
     */
    public Set<String> getExportFieldsByEncounterField(
        final ExportEncounterFieldType encounterField) {
        Set<String> exportFields = new HashSet<>();
        for (ExportRule rule : this.exportRules) {
            if (rule instanceof ExportRuleEncounter
                && ((ExportRuleEncounter) rule).getEncounterField() == encounterField) {
                exportFields.add(rule.getExportField());
            }
        }
        return exportFields;
    }

    /**
     * Returns the {@link ExportRuleFormat} object which belongs to the given encounter field.
     *
     * @param encounterField The {@link ExportEncounterFieldType} object to be searched for.
     * @return a {@link ExportRuleFormat} object which belongs to the given encounter field. Can be
     * <code>null</code>.
     */
    public ExportRuleFormat getExportRuleFormatFromEncounterField(
        final ExportEncounterFieldType encounterField) {
        for (ExportRule rule : this.exportRules) {
            if (rule instanceof ExportRuleEncounter
                && ((ExportRuleEncounter) rule).getEncounterField() == encounterField) {
                return rule.getExportRuleFormat();
            }
        }
        return null;
    }

    /**
     * Returns the {@link ExportRuleFormat} object which belongs to the given score field with the
     * given score id.
     *
     * @param scoreField The {@link ExportScoreFieldType} object to be searched for.
     * @param scoreId    The scoreId to be searched for.
     * @return a {@link ExportRuleFormat} object which belongs to the given score field. Can be
     * <code>null</code>.
     */
    public ExportRuleFormat getExportRuleFormatFromScoreField(final ExportScoreFieldType scoreField,
        final Long scoreId) {
        for (ExportRule rule : this.exportRules) {
            if (rule instanceof ExportRuleScore exportRuleScore) {
                if (exportRuleScore.getScore().getId().equals(scoreId)
                    && exportRuleScore.getScoreField() == scoreField) {
                    return rule.getExportRuleFormat();
                }
            }
        }
        return null;
    }

    /**
     * Returns a list of all export fields which belong to the given {@link ExportScoreFieldType}
     * object.
     *
     * @param scoreField The {@link ExportScoreFieldType} object to be searched for.
     * @param scoreId    The id of the score for which the export field should be returned
     * @return A list of export fields which belong to the given {@link ExportScoreFieldType}
     * object. Can not be <code>null</code>.
     */
    public Set<String> getExportFieldsByScoreField(final ExportScoreFieldType scoreField,
        final Long scoreId) {
        Set<String> exportFields = new HashSet<>();
        for (ExportRule rule : this.exportRules) {

            if (rule instanceof ExportRuleScore exportRuleScore) {
                if (exportRuleScore.getScore().getId().equals(scoreId)
                    && exportRuleScore.getScoreField() == scoreField) {
                    exportFields.add(rule.getExportField());
                }
            }
        }
        return exportFields;
    }

    /**
     * Returns the set of {@link ExportRuleScore} objects which belong to the given score field.
     *
     * @param scoreField The {@link ExportScoreFieldType} object to be searched for.
     * @param scoreId    The score id to be searched for the given score field. Can not be
     *                   <code>null</code>. Might be empty.
     * @return The set of {@link ExportRuleScore} objects which belong to the given
     * {@link ExportScoreFieldType} Object. Can not be
     * <code>null</code>. Might be empty.
     */
    public Set<ExportRuleScore> getExportRulesByScoreField(final ExportScoreFieldType scoreField,
        final Long scoreId) {
        Set<ExportRuleScore> exportRuleSet = new HashSet<>();
        for (ExportRule rule : this.exportRules) {
            if (rule instanceof ExportRuleScore exportRuleScore) {
                if (exportRuleScore.getScore().getId().equals(scoreId)
                    && exportRuleScore.getScoreField() == scoreField) {
                    exportRuleSet.add(((ExportRuleScore) rule));
                }
            }
        }
        return exportRuleSet;
    }

    /**
     * Returns a list of all export fields associated with the given {@link Answer} object.
     *
     * @param answer The {@link Answer} object to be searched for.
     * @return A list of export fields associated with the given {@link Answer} object. Can not be
     * <code>null</code>. Might be empty.
     */
    public List<String> getExportFieldsByAnswer(final Answer answer) {
        return getExportFieldsByAnswer(answer, false);
    }

    public List<String> getExportFieldsByAnswer(final Answer answer,
        final boolean useFreetextValue) {
        List<String> exportFields = new ArrayList<>();
        for (ExportRule rule : this.exportRules) {
            if (rule instanceof ExportRuleAnswer && ((ExportRuleAnswer) rule).getAnswer()
                .equals(answer)) {
                if (useFreetextValue == ((ExportRuleAnswer) rule).getUseFreetextValue()) {
                    exportFields.add(rule.getExportField());
                }
            }
        }
        return exportFields;
    }

    /**
     * Returns a list of all export fields associated with the given {@link Question} object.
     *
     * @param question The {@link Question} object to be searched for.
     * @return A list of export fields associated with the given {@link Question} object. Can not be
     * <code>null</code>. Might be empty.
     */
    public List<String> getExportFieldsByQuestion(final Question question) {
        List<String> exportFields = new ArrayList<>();
        for (ExportRule rule : this.exportRules) {
            if (rule instanceof ExportRuleQuestion && ((ExportRuleQuestion) rule).getQuestion()
                .equals(question)) {
                exportFields.add(rule.getExportField());
            }
        }
        return exportFields;
    }

    /**
     * Returns all {@link BundleQuestionnaire} objects associated with this export template object.
     * <p>
     * A {@link BundleQuestionnaire} object associated with this object indicates that the
     * questionnaire should be exported.
     *
     * @return Returns all {@link BundleQuestionnaire} objects associated with this export template
     * object. Can not be <code>null</code>. Might be empty. Is unmodifiable.
     */
    public Set<BundleQuestionnaire> getBundleQuestionnaires() {
        return Collections.unmodifiableSet(bundleQuestionnaires);
    }

    /**
     * Removes a given {@link BundleQuestionnaire} object from the list.
     *
     * @param bundleQuestionnaire A {@link BundleQuestionnaire} object. Can not be
     *                            <code>null</code>.
     */
    public void removeBundleQuestionnaire(final BundleQuestionnaire bundleQuestionnaire) {
        assert bundleQuestionnaire != null : "The bundleQuestionnaire was null";
        bundleQuestionnaires.remove(bundleQuestionnaire);
    }

    /**
     * Add a {@link BundleQuestionnaire} to the list of all associated
     * {@link BundleQuestionnaire BundleQuestionnaires}.
     *
     * @param bundleQuestionnaire A {@link BundleQuestionnaire} object. Can not be
     *                            <code>null</code>.
     */
    public void addBundleQuestionnaire(final BundleQuestionnaire bundleQuestionnaire) {
        assert bundleQuestionnaire != null : "The given BundleQuestionnaire was null";
        if (!bundleQuestionnaires.contains(bundleQuestionnaire)) {
            this.bundleQuestionnaires.add(bundleQuestionnaire);
        }
        // Take care the objects know each other
        if (!bundleQuestionnaire.getExportTemplates().contains(this)) {
            bundleQuestionnaire.addExportTemplate(this);
        }
    }

    /**
     * Returns all {@link EncounterExportTemplate} objects which where exported with the help of
     * this {@link ExportTemplate} object.
     * <p>
     * An {@link EncounterExportTemplate} object holds information about an executed export.
     *
     * @return The current {@link EncounterExportTemplate} objects of this export template object.
     * Is never <code>null</code>. Might be empty. Is unmodifiable.
     */
    public Set<EncounterExportTemplate> getEncounterExportTemplates() {
        return Collections.unmodifiableSet(encounterExportTemplates);
    }

    /**
     * Add all {@link EncounterExportTemplate} objects with help from this export template.
     * <p>
     * An {@link EncounterExportTemplate} represents an export attempt whether successful or
     * failed.
     *
     * @param encounterExportTemplates Set of {@link EncounterExportTemplate} objects. Can not be
     *                                 <code>null</code>.
     */
    public void addEncounterExportTemplates(
        final Set<EncounterExportTemplate> encounterExportTemplates) {
        assert encounterExportTemplates != null : "The given set was null";
        for (EncounterExportTemplate encounterExportTemplate : encounterExportTemplates) {
            addEncounterExportTemplate(encounterExportTemplate);
        }
    }

    /**
     * Add an {@link EncounterExportTemplate} object which was exported with the help of this export
     * template object.
     * <p>
     * An {@link EncounterExportTemplate} represents an export attempt whether successful or
     * failed.
     *
     * @param encounterExportTemplate A single {@link EncounterExportTemplate} object. Can not be
     *                                <code>null</code>.
     */
    public void addEncounterExportTemplate(final EncounterExportTemplate encounterExportTemplate) {
        assert encounterExportTemplate != null : "The given EncounterExportTemplate was null";
        if (!encounterExportTemplates.contains(encounterExportTemplate)) {
            this.encounterExportTemplates.add(encounterExportTemplate);
        }
        // take care the objects know each other
        if (encounterExportTemplate.getExportTemplate() != null
            && !encounterExportTemplate.getExportTemplate().equals(this)) {
            encounterExportTemplate.setExportTemplate(this);
        }
    }

    @Override
    public int hashCode() {
        return getUuid().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ExportTemplate other)) {
            return false;
        }
        return getUuid().equals(other.getUuid());
    }

    @Override
    public String toString() {
        return "de.imi.mopat.model.ExportTemplate[ id=" + this.getId() + " ]";
    }
}
