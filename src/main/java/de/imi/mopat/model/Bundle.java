package de.imi.mopat.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.imi.mopat.helper.model.UUIDGenerator;
import de.imi.mopat.model.conditions.Condition;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.eclipse.persistence.annotations.CascadeOnDelete;

/**
 * The database table model for table <i>bundle</i>. A bundle summarises a number of questionnaires.
 * A bundle's name is the label presented to the user, while a bundle's description gives additional
 * (longer) info (maybe via a tooltip). The welcomeText is a text shown to the patient in the very
 * beginning, no matter whether the questionnaires contain welcome texts or not. The finalText is a
 * closing information, shown at the very end of a bundle. Different bundles can be associated with
 * a number of clinics (see {@link BundleClinic}) to configure which bundle can be accessed by which
 * clinic. Since various bundles can contain various {@link Questionnaire Questionnaires} and vice
 * versa, a mapping model (see {@link BundleQuestionnaire}) is utilised to keep track of the
 * combinations.
 */
@Entity
@Table(name = "bundle")
public class Bundle implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;
    @JsonIgnore
    @Column(name = "uuid")
    private String uuid = UUIDGenerator.createUUID();
    // @Size and @NotNull have to be combined to realize @NotEmpty
    // If the annotations are changed, they have to be changed in the
    // corresponding data transfer object class
    @NotNull(message = "{bundle.name.notNull}")
    @Size(min = 1, message = "{bundle.name.notNull}")
    @Column(name = "name")
    private String name; // short name of the bundle
    @JsonIgnore
    // @Size and @NotNull have to be combined to realize @NotEmpty
    // If the annotations are changed, they have to be changed in the
    // corresponding data transfer object class
    @NotNull(message = "{bundle.description.notNull}")
    @Size(min = 1, message = "{bundle.description.notNull}")
    @Column(name = "description", columnDefinition = "TEXT NOT NULL")
    private String description;
    @JsonIgnore
    @NotNull(message = "{bundle.createdAt.notNull}")
    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt = new Timestamp(System.currentTimeMillis());
    // @NotNull missing, because of jakarta.validation.valid annotation (used in BundleController)
    @JsonIgnore
    @Column(name = "changed_by", nullable = false)
    private Long changedBy;
    @JsonIgnore
    @Column(name = "updated_at")
    private Timestamp updatedAt;
    @JsonIgnore
    @NotNull(message = "{bundle.isPublished.notNull}")
    @Column(name = "is_published", nullable = false)
    private Boolean isPublished;
    @NotNull(message = "{bundle.showProgressPerBundle.notNull}")
    @Column(name = "show_progress_per_bundle", nullable = false)
    private Boolean showProgressPerBundle;
    @NotNull(message = "{bundle.deactivateProgressAndNameDuringSurvey.notNull}")
    @Column(name = "deactivate_progress_and_name_during_survey", nullable = false)
    private Boolean deactivateProgressAndNameDuringSurvey;
    @Transient
    private Boolean hasConditions;
    @ElementCollection
    @MapKeyColumn(name = "language")
    @Column(name = "welcome_text", columnDefinition = "TEXT NOT NULL")
    @CollectionTable(name = "bundle_welcome_text", joinColumns = @JoinColumn(name = "id"))
    private Map<String, String> localizedWelcomeText;
    @ElementCollection
    @MapKeyColumn(name = "language")
    @Column(name = "final_text", columnDefinition = "TEXT NOT NULL")
    @CollectionTable(name = "bundle_final_text", joinColumns = @JoinColumn(name = "id"))
    private Map<String, String> localizedFinalText;

    @JsonIgnore
    @OneToMany(mappedBy = "bundle", cascade = CascadeType.ALL, orphanRemoval = true)
    @Valid
    @CascadeOnDelete
    private Set<BundleClinic> bundleClinics = new HashSet<>();
    @JsonIgnore
    @OneToMany(mappedBy = "bundle", fetch = FetchType.LAZY)
    private Set<Encounter> encounters = new HashSet<>();
    @OneToMany(mappedBy = "bundle", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Valid
    private Set<BundleQuestionnaire> bundleQuestionnaires = new HashSet<>();
    @OneToMany(mappedBy = "bundle", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews;

    public Bundle() { //default constructor (in protected state), should not
        // be accessible to anything else but the JPA implementation (here:
        // Hibernate) and the JUnit tests
    }

    /**
     * Uses the setters to set attributes.See setters for constraints.
     *
     * @param name                                  The new short name for this bundle object. Must
     *                                              not be
     *                                              <code>null</code>. Has to
     *                                              be at least 3 characters after trimming and at
     *                                              most 255 characters after trimming.
     * @param description                           The new description for this bundle object. Must
     *                                              not be <code>null</code>. Has to be at least 3
     *                                              and at most 255 characters in length (after
     *                                              trimming).
     * @param changedBy                             The id of the user that updates the bundle. Must
     *                                              not be
     *                                              <code>null</code>. Must
     *                                              be <code>&gt; 0</code>.
     * @param isPublished                           <code>true</code> if the
     *                                              bundle should be published<br>
     *                                              <code>false</code> if it
     *                                              should not be published. Must not be
     *                                              <code>null</code>.
     * @param showProgressPerBundle                 <code>true</code>:
     *                                              progress will be shown for the whole
     *                                              {@link Bundle}<br>
     *                                              <code>false</code>:
     *                                              progress will be shown for every
     *                                              {@link Questionnaire} seperately. Must not be
     *                                              <code>null</code>.
     * @param deactivateProgressAndNameDuringSurvey <code>True</code> if the
     *                                              progress and name of questionnaires will not be
     *                                              displayed,
     *                                              <code>false</code>
     *                                              otherwise. Must not be
     *                                              <code>null</code>.
     */
    public Bundle(final String name, final String description, final Long changedBy,
        final Boolean isPublished, final Boolean showProgressPerBundle,
        final Boolean deactivateProgressAndNameDuringSurvey) {
        setName(name);
        setDescription(description);
        setChangedBy(changedBy);
        setIsPublished(isPublished);
        setShowProgressPerBundle(showProgressPerBundle);
        setDeactivateProgressAndNameDuringSurvey(deactivateProgressAndNameDuringSurvey);
    }

    /**
     * Returns the id of the current bundle object.
     *
     * @return The current id of this bundle object. Might be <code>null</code> for newly created
     * objects. If <code>!null</code>, it's never
     * <code> &lt;= 0</code>.
     */
    public Long getId() {
        return id;
    }

    private String getUUID() {
        return this.uuid;
    }

    /**
     * Returns all {@link BundleClinic BundleClinic} objects of the current bundle object.
     * <p>
     * A {@link BundleClinic} object represents the association of a bundle to a clinic.
     *
     * @return The current {@link BundleClinic BundleClinic} objects of this bundle object. Is never
     * <code>null</code>. Might be empty. Is unmodifiable.
     */
    @JsonIgnore
    public Set<BundleClinic> getBundleClinics() {
        return Collections.unmodifiableSet(bundleClinics);
    }

    /**
     * Adds all given {@link BundleClinic BundleClinic} objects that are not already associated with
     * this bundle to the corresponding set of BundleClinics.Takes care that the bundleClinic
     * objects refer to this one, too.
     *
     * @param bundleClinics The set of additional {@link BundleClinic BundleClinic} objects for this
     *                      bundle object. Must not be <code>null</code>. Can be empty.
     */
    public void addBundleClinics(final Set<BundleClinic> bundleClinics) {
        assert bundleClinics != null : "The given set was null";
        for (BundleClinic bundleClinic : bundleClinics) {
            addBundleClinic(bundleClinic);
        }
    }

    /**
     * Adds a new {@link BundleClinic BundleClinic} object to the corresponding set of
     * BundleClinics.Takes care that the bundleClinic object refers to this one, too.
     *
     * @param bundleClinic The {@link BundleClinic BundleClinic} object, which will be added to this
     *                     bundle. Must not be
     *                     <code>null</code>.
     */
    public void addBundleClinic(final BundleClinic bundleClinic) {
        assert bundleClinic != null : "The given BundleClinic object was null";

        this.bundleClinics.add(bundleClinic);
        //take care that the objects know each other
        if (bundleClinic.getBundle() == null || !bundleClinic.getBundle().equals(this)) {
            // Add this bundle to the BundleClinic
            bundleClinic.setBundle(this);
        }
    }

    /**
     * Removes a {@link BundleClinic BundleClinic} object from the set of BundleClinics.Has to be
     * called together with {@link Clinic#removeBundleClinic(BundleClinic)} to avoid false
     * mappings/references.
     * <p>
     * Takes care that the {@link BundleClinic} does not refer to this object anymore.
     *
     * @param bundleClinic The {@link BundleClinic BundleClinic} object, which will be removed. Must
     *                     not be <code>null</code>.
     */
    public void removeBundleClinic(final BundleClinic bundleClinic) {
        assert bundleClinic != null : "The given BundleClinic was null";
        bundleClinics.remove(bundleClinic);
        //Take care of bidirectional removal
        if (bundleClinic.getBundle() != null && bundleClinic.getBundle().equals(this)) {
            bundleClinic.removeBundle();
        }
    }

    /**
     * Removes all {@link BundleClinic BundleClinic} objects from this bundle. Takes care that the
     * {@link BundleClinic} objects do not refer to this object anymore.
     */
    public void removeAllBundleClinics() {
        Collection<BundleClinic> tempBundleClinics = new HashSet<>(bundleClinics);
        for (BundleClinic bundleClinic : tempBundleClinics) {
            removeBundleClinic(bundleClinic);
        }
        this.bundleClinics.clear();
    }

    /**
     * Returns all {@link BundleQuestionnaire BundleQuestionnaire} objects of the current bundle
     * object. Sorted in their natural order using
     * BundleQuestionnairePositionComparator{@code <BundleQuestionnaire>}
     *
     * @return The current {@link BundleQuestionnaire BundleQuestionnaire} objects of this bundle
     * object. Is never <code>null</code>. Might be empty. Is unmodifiable.
     */
    public SortedSet<BundleQuestionnaire> getBundleQuestionnaires() {
        return Collections.unmodifiableSortedSet(new TreeSet<>(bundleQuestionnaires));
    }

    /**
     * Adds all given {@link BundleQuestionnaire BundleQuestionnaire} objects that are not already
     * associated with this bundle to the corresponding set of BundleQuestionnaires.Takes care that
     * the bundleQuestionnaire objects refer to this one, too.
     *
     * @param bundleQuestionnaires The set of additional
     *                             {@link BundleQuestionnaire BundleQuestionnaire} objects for this
     *                             bundle object. Must not be <code>null</code>.
     */
    public void addBundleQuestionnaires(final Set<BundleQuestionnaire> bundleQuestionnaires) {
        assert bundleQuestionnaires != null : "The given set was null";
        for (BundleQuestionnaire bundleQuestionnaire : bundleQuestionnaires) {
            addBundleQuestionnaire(bundleQuestionnaire);
        }
    }

    /**
     * Adds a new {@link BundleQuestionnaire BundleQuestionnaire} object to the corresponding set of
     * BundleQuestionnaires.Takes care that the {@link BundleQuestionnaire} object refers to this
     * one, too.
     *
     * @param bundleQuestionnaire The {@link BundleQuestionnaire BundleQuestionnaire} object, which
     *                            will be added to this bundle. Must not be
     *                            <code>null</code>.
     */
    public void addBundleQuestionnaire(final BundleQuestionnaire bundleQuestionnaire) {
        assert bundleQuestionnaire != null : "The given BundleQuestionnaire was null";

        this.bundleQuestionnaires.add(bundleQuestionnaire);
        if (bundleQuestionnaire.getBundle() == null || !bundleQuestionnaire.getBundle()
            .equals(this)) {
            // Add this bundle to the BundleQuestionnaire
            bundleQuestionnaire.setBundle(this);
        }
    }

    /**
     * Removes a {@link BundleQuestionnaire BundleQuestionnaire} object from the set of
     * BundleQuestionnaires
     *
     * @param bundleQuestionnaire The {@link BundleQuestionnaire BundleQuestionnaire} object, which
     *                            will be removed. Must not be <code>null</code>.
     */
    public void removeBundleQuestionnaire(final BundleQuestionnaire bundleQuestionnaire) {
        assert bundleQuestionnaire != null : "The given BundleQuestionnaire was null";
        //Do removal stuff only if the given bundleQuestionnaire is
        // associated with this bundle
        bundleQuestionnaires.remove(bundleQuestionnaire);
        //Take care of bidirectional removal
        if (bundleQuestionnaire.getBundle() != null && bundleQuestionnaire.getBundle()
            .equals(this)) {
            bundleQuestionnaire.removeBundle();
        }
    }

    /**
     * Removes all {@link BundleQuestionnaire BundleQuestionnaire} objects from this bundle. Takes
     * care that the {@link BundleQuestionnaire} objects do not refer to this object anymore.
     */
    public void removeAllBundleQuestionnaires() {
        Collection<BundleQuestionnaire> tempBundleQuestionnaires = new HashSet<>(
            bundleQuestionnaires);
        for (BundleQuestionnaire bundleQuestionnaire : tempBundleQuestionnaires) {
            removeBundleQuestionnaire(bundleQuestionnaire);
        }

        this.bundleQuestionnaires.clear();
    }

    /**
     * Returns all {@link Encounter Encounter} objects of the current bundle object.
     *
     * @return The current {@link Encounter Encounter} objects of this bundle object. Is never
     * <code>null</code>. Might be empty. Is unmodifiable.
     */
    @JsonIgnore
    public Set<Encounter> getEncounters() {
        return Collections.unmodifiableSet(encounters);
    }

    /**
     * Adds all given {@link Encounter Encounter} objects that are not already associated with this
     * bundle to the corresponding set of Encounters.Takes care that the encounter objects refer to
     * this one, too.
     *
     * @param encounters The set of additional {@link Encounter Encounter} objects for this bundle
     *                   object. Must not be
     *                   <code>null</code>.
     */
    public void addEncounters(final Set<Encounter> encounters) {
        assert encounters != null : "The given set was null";
        for (Encounter encounter : encounters) {
            addEncounter(encounter);
        }
    }

    /**
     * Adds a new {@link Encounter Encounter} object to the corresponding set of Encounters.Takes
     * care that the {@link Encounter} object refers to this one, too.
     *
     * @param encounter The {@link Encounter Encounter} object, which will be added to this bundle.
     *                  Must not be <code>null</code>.
     */
    public void addEncounter(final Encounter encounter) {
        assert encounter != null : "The given Encounter was null";
        encounters.add(encounter);
        // Take care that the objects know each other
        if (encounter.getBundle() == null || !encounter.getBundle().equals(this)) {
            encounter.setBundle(this);
        }
    }

    /**
     * Takes care that the {@link Encounter} object does not refer to this one anymore.
     *
     * @param encounter Must not be <code>null</code>.
     */
    public void removeEncounter(final Encounter encounter) {
        assert encounter != null : "The given Encounter was null";
        encounters.remove(encounter);
        if (encounter.getBundle() != null && encounter.getBundle().equals(this)) {
            encounter.removeBundle();
        }
    }

    /**
     * Returns the short name of the current bundle object.
     *
     * @return The short name of the current bundle object. Is never
     * <code>null</code>. Has at least 3 characters and at most 255 characters.
     */
    public String getName() {
        return name;
    }

    /**
     * See {@link Bundle#getName()} for a description.Sets a new short name for this bundle object.
     * <p>
     * Trims it beforehand.
     *
     * @param name The new short name for this bundle object. Must not be
     *             <code>null</code>. Has to be at least 3 characters after
     *             trimming and at most 255 characters after trimming.
     */
    public void setName(final String name) {
        assert name != null : "The given name was null";
        assert name.trim().length() >= 3 : "The name was < 3 characters long (after trimming)";
        assert name.trim().length() <= 255 : "The name was > 255 characters long (after trimming)";
        this.name = name.trim();
    }

    /**
     * Returns the description of the current bundle object. A description might be a longer name
     * for a bundle, or something else that clearly identifies it, such as the study it is used
     * for.
     *
     * @return The description of the current bundle object. Is never
     * <code>null</code>. Is at least 3 and at most 255 characters long.
     */
    public String getDescription() {
        return description;
    }

    /**
     * See {@link Bundle#getDescription()} for a description.Sets a new description for this bundle
     * object.
     * <p>
     * Trims it before setting.
     *
     * @param description The new description for this bundle object. Must not be <code>null</code>.
     *                    Has to be at least 3 and at most 255 characters in length (after
     *                    trimming).
     */
    public void setDescription(final String description) {
        assert description != null : "The given description was null";
        assert
            description.trim().length() >= 3 :
            "The description was < 3 characters long (after " + "trimming)";
        assert
            description.trim().length() <= 255 :
            "The description was > 255 characters long (after " + "trimming)";
        this.description = description.trim();
    }

    /**
     * Returns the {@link Timestamp} for when the bundle object has been created.
     *
     * @return The create time of the bundle. Is never <code>null</code>. Is not in the future.
     */
    public Timestamp getCreatedAt() {
        return createdAt;
    }

    /**
     * Returns the id of the user that updated the bundle most recently.
     *
     * @return Is never <code>null</code>. Is never <code> &lt;= 0</code>.
     */
    public Long getChangedBy() {
        return changedBy;
    }

    /**
     * Sets a new id of the user that has updated the bundle.
     *
     * @param changedBy The id of the user that updates the bundle. Must not be
     *                  <code>null</code>. Must be <code>&gt; 0</code>.
     */
    public void setChangedBy(final Long changedBy) {
        assert changedBy != null : "The given changedBy-ID was null";
        assert changedBy > 0 : "The given changedBy-ID was <= 0";
        this.changedBy = changedBy;
    }

    /**
     * Returns the time the bundle has been updated.
     *
     * @return The update time of the bundle. Can be <code>null</code>. If not: Is not in the
     * future.
     */
    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets the time the bundle has been updated.
     *
     * @param updatedAt The update time of the bundle. Must not be
     *                  <code>null</code>. Must not be in the future.
     */
    public void setUpdatedAt(final Timestamp updatedAt) {
        assert updatedAt != null : "The timestamp given was null";
        assert !updatedAt.after(
            new Timestamp(System.currentTimeMillis())) : "The given timestamp is in the future";
        this.updatedAt = updatedAt;
    }

    /**
     * Returns whether the progress and name of questionnaires will be displayed during a survey for
     * this bundle.
     *
     * @return <code>True</code> if the progress and name of questionnaires will
     * not be displayed, <code>false</code> otherwise. Is never
     * <code>null</code>.
     */
    public Boolean getDeactivateProgressAndNameDuringSurvey() {
        return deactivateProgressAndNameDuringSurvey;
    }

    /**
     * Sets whether the progress and name of questionnaires will be displayed during a survey for
     * this bundle.
     *
     * @param deactivateProgressAndNameDuringSurvey <code>True</code> if the
     *                                              progress and name of questionnaires will not be
     *                                              displayed,
     *                                              <code>false</code>
     *                                              otherwise. Must not be
     *                                              <code>null</code>.
     */
    public void setDeactivateProgressAndNameDuringSurvey(
        final Boolean deactivateProgressAndNameDuringSurvey) {
        assert deactivateProgressAndNameDuringSurvey != null : "The given value was null";
        this.deactivateProgressAndNameDuringSurvey = deactivateProgressAndNameDuringSurvey;
    }

    /**
     * Returns whether the bundle is published or not.
     *
     * @return <code>true</code> if the bundle is published<br>
     * <code>false</code> if it is not published. Is never <code>null</code>.
     */
    public Boolean getIsPublished() {
        return isPublished;
    }

    /**
     * Sets the bundle as published or not.
     *
     * @param isPublished <code>true</code> if the bundle should be
     *                    published<br> <code>false</code> if it should not be published. Must not
     *                    be <code>null</code>.
     */
    public void setIsPublished(final Boolean isPublished) {
        assert isPublished != null : "The given value was null";
        this.isPublished = isPublished;
    }

    /**
     * Indicates whether the progress of the {@link Bundle} is displayed completely per
     * {@link Bundle}, or per {@link Questionnaire}.
     *
     * @return <code>true</code> if the progress is shown per bundle<br>
     * <code>false</code> if the progress is shown per {@link Questionnaire}. Is
     * never <code>null</code>.
     */
    public Boolean getShowProgressPerBundle() {
        return showProgressPerBundle;
    }

    /**
     * Sets whether the progress is shown per {@link Bundle} or per {@link Questionnaire}.
     *
     * @param showProgressPerBundle <code>true</code>: progress will be shown
     *                              for the whole {@link Bundle}<br>
     *                              <code>false</code>: progress will be
     *                              shown for every {@link Questionnaire} seperately. Must not be
     *                              <code>null</code>.
     */
    public void setShowProgressPerBundle(final Boolean showProgressPerBundle) {
        assert showProgressPerBundle != null : "The given value was null";
        this.showProgressPerBundle = showProgressPerBundle;
    }

    /**
     * Indicates whether the {@link Bundle} has {@link Condition conditions} attached or not.
     *
     * @return <code>true</code>: the {@link Bundle} has
     * {@link Condition conditions} attached <br> <code>false</code>: the {@link Bundle} has no
     * {@link Condition conditions} attached.
     */
    public boolean isHasConditions() {
        return hasConditions;
    }

    /**
     * Sets whether the {@link Bundle} has {@link Condition conditions} attached or not.
     *
     * @param hasConditions <code>true</code>: the {@link Bundle} has
     *                      {@link Condition conditions} attached <br>
     *                      <code>false</code>: the
     *                      {@link Bundle} has no {@link Condition conditions } attached.
     */
    public void setHasConditions(final Boolean hasConditions) {
        this.hasConditions = hasConditions;
    }

    /**
     * A welcome text is the text that will be shown to the patient in the very beginning of
     * answering a bundle of questionnaires. Thus, it is the first screen/text shown to the patient.
     * Each questionnaire of a bundle might have a welcome text, too. If yes, they will be shown
     * additionally before starting the questionnaire, respectively. If a tutorial is shown to the
     * patient, the bundle's welcome text also will be shown in the very beginning.
     *
     * @return A map with localized welcome texts of the bundle. The entries are never
     * <code>null</code>. They might be empty. An empty welcome text entry
     * should be interpreted as having no welcome text. Thus, the user should not see a welcome
     * screen but the first screen of the first questionnaire when starting this bundle.
     */
    public Map<String, String> getLocalizedWelcomeText() {
        return localizedWelcomeText;
    }

    /**
     * See {@link Bundle#getLocalizedWelcomeText()} for description of a bundle's welcome text
     *
     * @param localizedWelcomeText The map with new localized welcome texts of the bundle. Non of
     *                             the entries in the map must be <code>null</code>. But they can be
     *                             empty (after trimming). An empty welcome text should be
     *                             interpreted as having no welcome text. Thus, the user should not
     *                             see a welcome screen but the first screen of the first
     *                             questionnaire when starting this bundle. Will be trimmed before
     *                             setting.
     */
    public void setLocalizedWelcomeText(final Map<String, String> localizedWelcomeText) {
        this.localizedWelcomeText = localizedWelcomeText;
    }

    /**
     * Returns the map with localized final texts of the bundle. The final text is the text shown to
     * the user after all questionnaires have been finished. It is also shown if the questionnaires
     * have not been answered completely and are thus unfinished.
     *
     * @return A map with localized final texts of the bundle. The entries are never
     * <code>null</code>. Might be empty. An empty final text should be
     * interpreted as having no final text. Thus, the user should not see a final screen of the
     * bundle (but maybe a final screen of the app) after the last screen of the last
     * questionnaire.
     */
    public Map<String, String> getLocalizedFinalText() {
        return localizedFinalText;
    }

    /**
     * See {@link Bundle#getLocalizedFinalText()} for description of a bundle's final text
     * <p>
     * Sets a new final text for the bundle.
     *
     * @param localizedFinalText The map with new localized final texts of the bundle. Must not be
     *                           <code>null</code>. An entry can be empty (after trimming). Will be
     *                           trimmed before setting. An empty final text should be interpreted
     *                           as having no final text. Thus, the user should not see a final
     *                           screen of the bundle (but maybe a final screen of the app) after
     *                           the last screen of the last questionnaire.
     */
    public void setLocalizedFinalText(final Map<String, String> localizedFinalText) {
        this.localizedFinalText = localizedFinalText;
    }

    /**
     * Returns a list with all available languages for this bundle. An available languages for this
     * bundle must be available in every question of every questionnaire of the bundle. Also the
     * available language must be present in the welcome/finaltexts of this bundle. If the
     * welcome/finaltexts in this bundle are empty, it will be ignored for the list of available
     * languages.
     *
     * @return A list with language codes which are available for this bundle.
     */
    public List<String> getAvailableLanguages() {
        List<String> availableLanguages = new ArrayList<>();
        Set<String> availableQuestionnaireLanguages = new HashSet<>();

        // If this bundle contains any questionnaires, get the available
        // languages
        // from this questionnaires
        if (!this.getBundleQuestionnaires().isEmpty()) {
            // Add the languages from the first questionnaire to the set
            availableQuestionnaireLanguages.addAll(
                this.getBundleQuestionnaires().first().getQuestionnaire().getAvailableLanguages());
            for (BundleQuestionnaire bundleQuestionnaire : this.getBundleQuestionnaires()) {
                Questionnaire questionnaire = bundleQuestionnaire.getQuestionnaire();
                // Retain all languages from the current questionnaire -->
                // get the intersection
                availableQuestionnaireLanguages.retainAll(questionnaire.getAvailableLanguages());
            }
        }

        // Check if this bundle contains not empty welcometexts and create a new
        // list with all available languages for the welcometext
        List<String> availableWelcomeTextLanguages = new ArrayList<>();
        for (Map.Entry<String, String> entry : this.getLocalizedWelcomeText().entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                availableWelcomeTextLanguages.add(entry.getKey());
            }
        }

        // Check if this bundle contains not empty finaltexts and create a new
        // list with all available languages for the finaltext
        List<String> availableFinalTextLanguages = new ArrayList<>();
        for (Map.Entry<String, String> entry : this.getLocalizedFinalText().entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                availableFinalTextLanguages.add(entry.getKey());
            }
        }

        // Add the set with all available languages for each questionnaire to
        // the
        // availableLanguages list of this bundle
        availableLanguages.addAll(availableQuestionnaireLanguages);

        // If the availableWelcomeTextLanguages list is not empty, take the
        // intersection
        // of the avalailableLanguage list and the availableWelcomeTextLanguages
        if (!availableWelcomeTextLanguages.isEmpty()) {
            availableLanguages.retainAll(availableWelcomeTextLanguages);
        }
        // If the availableFinalTextLanguages list is not empty, take the
        // intercection
        // of the avalailableLanguage list and the availableFinalTextLanguages
        if (!availableFinalTextLanguages.isEmpty()) {
            availableLanguages.retainAll(availableFinalTextLanguages);
        }

        Collections.sort(availableLanguages);
        return availableLanguages;
    }

    /**
     * Get the number of assigned export templates.
     *
     * @return The number of assigned export templates. Is greater than or equal 0.
     */
    @JsonIgnore
    public int getNumberOfAssignedExportTemplate() {
        int num = 0;
        for (BundleQuestionnaire bundleQuestionnaire : this.getBundleQuestionnaires()) {
            num += bundleQuestionnaire.getExportTemplates().size();
        }
        return num;
    }

    /**
     * Returns a Set of all {@link ExportTemplate ExportTemplate} objects associates with this
     * bundle through bundle questionnaire.
     *
     * @return A Set of all {@link ExportTemplate ExportTemplate} objects associates with this
     * bundle through bundle questionnaire. Can not be
     * <code>null</code>. Might be empty.
     */
    @JsonIgnore
    public Set<ExportTemplate> getAllAssignedExportTemplates() {
        Set<ExportTemplate> exportTemplates = new HashSet<>();
        // iterate over all bundle questionnaires
        for (BundleQuestionnaire bundleQuestionnaire : this.getBundleQuestionnaires()) {
            // add all export templates associated with the bundle questionnaire
            exportTemplates.addAll(bundleQuestionnaire.getExportTemplates());
        }
        return exportTemplates;
    }

    /**
     * Returns whether the {@link Bundle} is deletable or not.
     *
     * @return True if the {@link Bundle} is deletable, false otherwise.
     */
    public boolean isDeletable() {
        // If there is an encounter attached to this bundle, the bundle
        // should not be deleted
        return encounters.isEmpty();
    }

    /**
     * Returns whether the {@link Bundle} is modifiable or not.
     *
     * @return True if the {@link Bundle} is modifiable, false otherwise.
     */
    public Boolean isModifiable() {
        // If there is one unfinished encounter attached to this bundle, the
        // bundle should not be modified
        for (Encounter encounter : getEncounters()) {
            if (encounter.getEndTime() == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns whether the {@link Bundle} is used in at least one clinic.
     *
     * @return True if the {@link Bundle} is used in at least one clinic, false otherwise.
     */
    public boolean usedInClinics() {
        return !this.getBundleClinics().isEmpty();
    }


    public Boolean hasActiveQuestionnaire() {
        for (BundleQuestionnaire bundleQuestionnaire : this.bundleQuestionnaires) {
            if (bundleQuestionnaire.getIsEnabled()) {
                return true;
            }
        }
        return false;
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
        if (!(obj instanceof Bundle other)) {
            return false;
        }
        return getUUID().equals(other.getUUID());
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }
}
