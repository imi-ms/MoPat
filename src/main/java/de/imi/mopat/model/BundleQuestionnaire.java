package de.imi.mopat.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.imi.mopat.helper.model.UUIDGenerator;
import de.imi.mopat.model.dto.BundleQuestionnaireDTO;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

/**
 * The database table model for table <i>bundle_questionnaire</i>. Represents the joinTable for
 * {@link Bundle} and {@link Questionnaire}. The position attribute puts the questionnaires in
 * order. On basis of this order the patient has to answer the questionnaires. If you want the
 * {@link Bundle} to contain a tutorial, a {@link Questionnaire} representing such a tutorial should
 * be added in first position of this mapping.
 */
@Entity
@Table(name = "bundle_questionnaire")
public class BundleQuestionnaire implements Serializable, Comparable<BundleQuestionnaire> {

    @JsonIgnore
    @Column(name = "uuid")
    private String uuid = UUIDGenerator.createUUID();
    @NotNull(message = "{bundleQuestionnaire.position.notNull}")
    @Column(nullable = false)
    private Integer position;
    @Id
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "bundle_id", referencedColumnName = "id")
    private Bundle bundle;
    @Id
    @ManyToOne
    @JoinColumn(name = "questionnaire_id", referencedColumnName = "id")
    private Questionnaire questionnaire;
    @JsonIgnore
    @ManyToMany(cascade = CascadeType.MERGE)
    @JoinTable(name = "bundle_questionnaire_export_template", joinColumns = {
        @JoinColumn(name = "bundle_id", referencedColumnName = "bundle_id"),
        @JoinColumn(name = "questionnaire_id", referencedColumnName = "questionnaire_id")}, inverseJoinColumns = @JoinColumn(name = "export_template_id", referencedColumnName = "id"))
    private Set<ExportTemplate> exportTemplates = new HashSet<>();

    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled;
    @Column(name = "show_scores", nullable = false)
    private Boolean showScores = false;

    protected BundleQuestionnaire() { //default constructor (in protected
        // state), should not be accessible to anything else but the JPA
        // implementation (here: Hibernate) and the JUnit tests
    }

    /**
     * uses the setters to set the attributes.See setters for constraints.
     *
     * @param bundle        Object
     * @param questionnaire Object
     * @param position      States the position for a questionnaire within a bundle
     * @param isEnabled     The state of this BundleQuestionnaire object. Must not be
     *                      <code>null</code>.
     * @param showScores    States whether the scores should be shown at the end of a survey or not
     */
    public BundleQuestionnaire(Bundle bundle, Questionnaire questionnaire, Integer position,
        Boolean isEnabled, Boolean showScores) {
        setPosition(position);
        setBundle(bundle);
        setQuestionnaire(questionnaire);
        setIsEnabled(isEnabled);
        setShowScores(showScores);
    }

    /**
     * Returns the bundle of the bundle-questionnaire association.
     *
     * @return The {@link Bundle Bundle} object of the
     * {@link Bundle Bundle}-{@link Questionnaire Questionnaire} association. Might be
     * <code>null</code> (if no {@link Bundle} is currently set; quite unusual).
     */
    @JsonIgnore
    public Bundle getBundle() {
        return bundle;
    }

    /**
     * Sets a new bundle for the bundle-questionnaire association.Takes care that the {@link Bundle}
     * objects refers to this one, too.
     *
     * @param bundle The new {@link Bundle Bundle} object of the
     *               {@link Bundle Bundle}-{@link Questionnaire Questionnaire } association. Must
     *               not be <code>null</code>.
     */
    public void setBundle(Bundle bundle) {
        assert bundle != null : "The given Bundle was null";
        this.bundle = bundle;
        //take care that the objects know each other
        if (!bundle.getBundleQuestionnaires().contains(this)) {
            bundle.addBundleQuestionnaire(this);
        }
    }

    /**
     * Returns the questionnaire of the bundle-questionnaire association.
     *
     * @return The {@link Questionnaire Questionnaire} object of the
     * {@link Bundle Bundle}-{@link Questionnaire Questionnaire} association. Might be
     * <code>null</code> (if no {@link Bundle} is currently set; quite unusual).
     */
    public Questionnaire getQuestionnaire() {
        return questionnaire;
    }

    /**
     * Sets a new questionnaire for the bundle-questionnaire association.Takes care that the
     * {@link Questionnaire} objects refers to this one, too.
     *
     * @param questionnaire The new {@link Questionnaire Questionnaire} object of the
     *                      {@link Bundle Bundle}- {@link Questionnaire Questionnaire} association.
     *                      Must not be <code>null</code>.
     */
    public void setQuestionnaire(final Questionnaire questionnaire) {
        assert questionnaire != null : "The given Questionnaire was null";
        this.questionnaire = questionnaire;
        //take care that the objects know each other
        if (!questionnaire.getBundleQuestionnaires().contains(this)) {
            questionnaire.addBundleQuestionnaire(this);
        }
    }

    /**
     * Returns the position of the questionnaire within the list of questionnaires associated with
     * the bundle. The numbering starts with 1.
     *
     * @return The position of the {@link Questionnaire Questionnaire} within the list of
     * questionnaires associated with the {@link Bundle Bundle}. Is always <code> &gt;= 1</code>.
     */
    public Integer getPosition() {
        return position;
    }

    /**
     * Sets a new position for the questionnaire within the list of questionnaires associated with
     * the bundle.
     *
     * @param position The new position of the {@link Questionnaire Questionnaire} within the list
     *                 of questionnaires associated with the {@link Bundle Bundle}. Must be
     *                 <code> &gt;= 1</code>.
     */
    public void setPosition(final Integer position) {
        assert position != null : "The given position was null";
        assert position >= 1 : "The given position was < 1";
        this.position = position;
    }

    /**
     * Returns true if the {@link Questionnaire} object is enabled in the corresponding
     * {@link Bundle} object.
     *
     * @return The value of isEnabled attribute of this BundleQuestionnaire object. Is never
     * <code>null</code>.
     */
    public Boolean getIsEnabled() {
        return isEnabled;
    }

    /**
     * See {@link BundleQuestionnaire#getIsEnabled()} for a description.
     * <p>
     * Sets the isEnabled attribute of of this BundleQuestionnaire object.
     *
     * @param isEnabled The new isEnabled state of of this BundleQuestionnaire object. Must not be
     *                  <code>null</code>.
     */
    public void setIsEnabled(final Boolean isEnabled) {
        assert isEnabled != null : "The given isEnabled state was null";
        this.isEnabled = isEnabled;
    }

    /**
     * Returns true if the associated {Score Scores} should be shown at the end of the survey.
     *
     * @return The value of showScores attribute of this BundleQuestionnaire object. Is never
     * <code>null</code>.
     */
    public Boolean getShowScores() {
        return showScores;
    }

    /**
     * See {@link BundleQuestionnaire#getShowScores()} for a description.
     * <p>
     * Sets the showScores attribute of of this BundleQuestionnaire object.
     *
     * @param showScores The new showScores state of of this BundleQuestionnaire object. Must not be
     *                   <code>null</code>.
     */
    public void setShowScores(final Boolean showScores) {
        assert showScores != null : "The given showScores state was null";
        this.showScores = showScores;
    }

    /**
     * Shall never be called by something else but
     * {@link Bundle#removeBundleQuestionnaire(BundleQuestionnaire)}.
     */
    protected void removeBundle() {
        Bundle bundleTemp = bundle;
        this.bundle = null;
        if (bundleTemp.getBundleQuestionnaires().contains(this)) {
            bundleTemp.removeBundleQuestionnaire(this);
        }
    }

    /**
     * Shall never be called by something else but
     * {@link Questionnaire#removeBundleQuestionnaire(BundleQuestionnaire)}.
     */
    protected void removeQuestionnaire() {
        Questionnaire questionnaireTemp = questionnaire;
        this.questionnaire = null;
        if (questionnaireTemp.getBundleQuestionnaires().contains(this)) {
            questionnaireTemp.removeBundleQuestionnaire(this);
        }
    }

    private String getUUID() {
        return this.uuid;
    }

    /**
     * Sets all {@link ExportTemplate} objects which should be exported from the questionnaire of
     * this bundleQuestionnaire object.
     *
     * @param exportTemplates A set of {@link ExportTemplate} objects. Can not be
     *                        <code>null</code>.
     */
    public void setExportTemplates(final Set<ExportTemplate> exportTemplates) {
        assert exportTemplates != null : "The Set was null";
        this.removeExportTemplates();
        for (ExportTemplate exportTemplate : exportTemplates) {
            this.addExportTemplate(exportTemplate);
        }
    }

    /**
     * Removes all {@link ExportTemplate ExportTemplate} objects from the
     * {@link BundleQuestionnaire BundleQuestionnaire} object.
     */
    public void removeExportTemplates() {
        // take care the export template also removes the bundle questionnaire
        for (ExportTemplate exportTemplate : this.getExportTemplates()) {
            exportTemplate.removeBundleQuestionnaire(this);
        }
        this.exportTemplates.clear();
    }

    /**
     * Add an {@link ExportTemplate} to the list of all associated
     * {@link ExportTemplate ExportTemplates}.
     *
     * @param exportTemplate A {@link ExportTemplate} object. Can not be
     *                       <code>null</code>.
     */
    public void addExportTemplate(final ExportTemplate exportTemplate) {
        assert exportTemplate != null : "The given ExportTemplate was null";
        if (!exportTemplates.contains(exportTemplate)) {
            this.exportTemplates.add(exportTemplate);
        }
        // Take care the objects know each other
        if (!exportTemplate.getBundleQuestionnaires().contains(this)) {
            exportTemplate.addBundleQuestionnaire(this);
        }
    }

    /**
     * Returns all {@link ExportTemplate ExportTemplate} objects related with this
     * bundleQuestionnaire object.
     * <p>
     * Every entry indicates that the export template will be used to export the questionnaire of
     * this bundleQuestionnaire object.
     *
     * @return Returns all {@link ExportTemplate ExportTemplate} objects related with this
     * bundleQuestionnaire object. Can not be <code>null</code>. Might be empty. Is unmodifiable.
     */
    public Set<ExportTemplate> getExportTemplates() {
        return Collections.unmodifiableSet(exportTemplates);
    }

    @Override
    public int hashCode() {
        return getUUID().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof BundleQuestionnaire)) {
            return false;
        }
        BundleQuestionnaire other = (BundleQuestionnaire) obj;
        return getUUID().equals(other.getUUID());
    }

    public BundleQuestionnaireDTO toBundleQuestionnaireDTO() {
        BundleQuestionnaireDTO bundleQuestionnaireDTO = new BundleQuestionnaireDTO();
        bundleQuestionnaireDTO.setPosition((long) this.getPosition());
        bundleQuestionnaireDTO.setIsEnabled(this.isEnabled);
        bundleQuestionnaireDTO.setShowScores(this.showScores);

        Set<Long> exportTemplateIds = new HashSet<>();
        for (ExportTemplate exportTemplate : this.getExportTemplates()) {
            exportTemplateIds.add(exportTemplate.getId());
        }
        bundleQuestionnaireDTO.setExportTemplates(exportTemplateIds);

        return bundleQuestionnaireDTO;
    }

    /**
     * Compares another {@link BundleQuestionnaire} object to this one based on its
     * {@link BundleQuestionnaire#getPosition() }. Note: this class has a natural ordering that is
     * inconsistent with equals.
     */
    @Override
    public int compareTo(final BundleQuestionnaire o) {
        return getPosition().compareTo(o.getPosition());
    }
}
