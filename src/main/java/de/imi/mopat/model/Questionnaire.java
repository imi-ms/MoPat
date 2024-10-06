package de.imi.mopat.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.imi.mopat.helper.model.UUIDGenerator;
import de.imi.mopat.model.conditions.Condition;
import de.imi.mopat.model.conditions.ConditionTarget;
import de.imi.mopat.model.enumeration.QuestionType;
import de.imi.mopat.model.score.Score;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.eclipse.persistence.annotations.CascadeOnDelete;

/**
 * The database table model for table <i>questionnaire</i>. This model represents a set of
 * questions.
 */
@Entity
@Table(name = "questionnaire")
public class Questionnaire implements ConditionTarget, Serializable {

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
    @NotNull(message = "{questionnaire.name.notNull}")
    @Size(min = 3, max = 255, message = "{questionnaire.name.size}")
    @Column(name = "name", nullable = false)
    private String name; // short name of the questionnaire
    @ElementCollection
    @MapKeyColumn(name = "language")
    @Column(name = "display_name")
    @CollectionTable(name = "questionnaire_display_name", joinColumns = @JoinColumn(name = "id"))
    private Map<String, String> localizedDisplayName;
    @JsonIgnore
    // @Size and @NotNull have to be combined to realize @NotEmpty
    // If the annotations are changed, they have to be changed in the
    // corresponding data transfer object class
    @NotNull(message = "{questionnaire.description.notNull}")
    @Size(min = 1, message = "{questionnaire.description.notNull}")
    @Column(name = "description", columnDefinition = "TEXT NOT NULL")
    private String description; // full name of the questionnaire
    @JsonIgnore
    @NotNull(message = "{questionnaire.createdAt.notNull}")
    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt = new Timestamp(System.currentTimeMillis());
    // @NotNull missing, because of jakarta.validation.valid annotation (used
    // in QuestionnaireController)
    @JsonIgnore
    @Column(name = "changed_by", nullable = false)
    private Long changedBy;
    @JsonIgnore
    @Column(name = "updated_at")
    private Timestamp updatedAt;
    @Column(name = "logo")
    private String logo;
    @JsonIgnore
    @NotNull(message = "{questionnaire.isPublished.notNull}")
    @Column(name = "is_published", nullable = false)
    private Boolean isPublished = false;
    @Transient
    private Boolean hasConditions;

    @ElementCollection(fetch = FetchType.LAZY)
    @MapKeyColumn(name = "language")
    @Column(name = "welcome_text", columnDefinition = "TEXT NOT NULL")
    @CollectionTable(name = "questionnaire_welcome_text", joinColumns = @JoinColumn(name = "id"))
    private Map<String, String> localizedWelcomeText;

    @ElementCollection(fetch = FetchType.LAZY)
    @MapKeyColumn(name = "language")
    @Column(name = "final_text", columnDefinition = "TEXT NOT NULL")
    @CollectionTable(name = "questionnaire_final_text", joinColumns = @JoinColumn(name = "id"))
    private Map<String, String> localizedFinalText;

    @JsonIgnore
    @Valid
    @OneToMany(mappedBy = "questionnaire", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BundleQuestionnaire> bundleQuestionnaires = new HashSet<BundleQuestionnaire>();

    @Valid
    @OneToMany(mappedBy = "questionnaire", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @CascadeOnDelete
    private Set<Question> questions = new HashSet<Question>();

    @JsonIgnore
    @Valid
    @OneToMany(mappedBy = "questionnaire", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST,
        CascadeType.MERGE, CascadeType.REMOVE})
    private Set<ExportTemplate> exportTemplates = new HashSet<ExportTemplate>();

    @JsonIgnore
    @OneToMany(mappedBy = "questionnaire", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Score> scores = new HashSet<Score>();

    @Column(name = "version", nullable = false)
    private Integer version = 1;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "version_group_id")
    private QuestionnaireVersionGroup questionnaireVersionGroup;

    public Questionnaire() { //default constructor (in protected state),
        // should not be accessible to anything else but the JPA
        // implementation (here: Hibernate) and the JUnit tests
    }

    /**
     * Uses the setters to set attributes.See setters for constraints.
     *
     * @param name        The new short name for this questionnaire object. Must not be
     *                    <code>null</code>, has a least 3 (after trimming)
     *                    and at most 255 Characters (after trimming). Will be trimmed while
     *                    setting.
     * @param description The new description for this questionnaire object. Must not be
     *                    <code>null</code>. Has to be at least 3 and at most 255 characters in
     *                    length (after trimming).
     * @param changedBy   The given changedBy must be not null and must be positive
     * @param isPublished <code>true</code> if the questionnaire should be
     *                    published<br> <code>false</code> if it should not be published. Must not
     *                    be <code>null</code>.
     */
    public Questionnaire(final String name, final String description, final Long changedBy, final Long createdBy,
        final Boolean isPublished) {
        setName(name);
        setDescription(description);
        setChangedBy(changedBy);
        setCreatedBy(createdBy);
        setPublished(isPublished);
    }

    /**
     * Returns the id of the current questionnaire object.
     *
     * @return The current id of this questionnaire object. Might be
     * <code>null</code> for newly created objects. Is never
     * <code> &lt;= 0</code>
     */
    public Long getId() {
        return id;
    }

    public String getUUID() {
        return this.uuid;
    }

    /**
     * Returns all {@link BundleQuestionnaire} objects of the current questionnaire object.
     *
     * @return The current {@link BundleQuestionnaire} objects of this questionnaire object. Is
     * never <code>null</code>. Might be empty. Is unmodifiable.
     */
    public Set<BundleQuestionnaire> getBundleQuestionnaires() {
        return Collections.unmodifiableSet(bundleQuestionnaires);
    }

    /**
     * Adds all given {@link BundleQuestionnaire} objects that are not already associated with this
     * questionnaire to the corresponding set of BundleQuestionnaires.Takes care that the
     * {@link BundleQuestionnaire} objects refer to this one, too.
     *
     * @param bundleQuestionnaires The set of additional
     *                             {@link BundleQuestionnaire BundleQuestionnaire} objects for this
     *                             questionnaire object. Must not be
     *                             <code>null</code>.
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
     *                            will be added to this questionnaire. Must not be
     *                            <code>null</code>.
     */
    public void addBundleQuestionnaire(final BundleQuestionnaire bundleQuestionnaire) {
        assert bundleQuestionnaire != null : "The given BundleQuestionnaire is null";

        bundleQuestionnaires.add(bundleQuestionnaire);
        if (bundleQuestionnaire.getQuestionnaire() == null
            || !bundleQuestionnaire.getQuestionnaire().equals(this)) {
            bundleQuestionnaire.setQuestionnaire(this);
        }
    }

    /**
     * Removes a {@link BundleQuestionnaire BundleQuestionnaire} object from the set of
     * BundleQuestionnaires.Takes care that the {@link BundleQuestionnaire} does not refer to this
     * object anymore.
     *
     * @param bundleQuestionnaire The {@link BundleQuestionnaire BundleQuestionnaire} object, which
     *                            will be removed. Must not be <code>null</code>.
     */
    public void removeBundleQuestionnaire(final BundleQuestionnaire bundleQuestionnaire) {
        assert bundleQuestionnaire != null : "The given BundleQuestionnaire is null";

        bundleQuestionnaires.remove(bundleQuestionnaire);

        if (bundleQuestionnaire.getQuestionnaire() != null && bundleQuestionnaire.getQuestionnaire()
            .equals(this)) {
            bundleQuestionnaire.removeQuestionnaire();
        }
    }

    /**
     * Removes all {@link BundleQuestionnaire BundleQuestionnaire} objects from this questionnaire.
     * Takes care that the {@link BundleQuestionnaire} objects do not refer to this object anymore.
     */
    public void removeAllBundleQuestionnaires() {
        Collection<BundleQuestionnaire> tempBundleQuestionnaires = new HashSet<BundleQuestionnaire>(
            bundleQuestionnaires);
        for (BundleQuestionnaire bundleQuestionnaire : tempBundleQuestionnaires) {
            removeBundleQuestionnaire(bundleQuestionnaire);
        }
    }

    /**
     * Returns all {@link Question Question} objects of the current questionnaire object, ordered by
     * their natural order, which is {@link Question#getPosition()}.
     *
     * @return The current {@link Question Question} objects of this questionnaire object. Is never
     * <code>null</code>. Might be empty. Is unmodifiable.
     */
    public SortedSet<Question> getQuestions() {
        return Collections.unmodifiableSortedSet(new TreeSet<Question>(questions));
    }

    /**
     * Adds all given {@link Question Question} objects that are not already associated with this
     * questionnaire to the corresponding set of Questions.Takes care that the {@link Question}
     * objects refer to this one, too.
     *
     * @param questions The collection of additional {@link Question Question} objects for this
     *                  questionnaire object. Must not be
     *                  <code>null</code>.
     */
    public void addQuestions(final Collection<Question> questions) {
        assert questions != null : "The given Set was null";
        for (Question question : questions) {
            addQuestion(question);
        }
    }

    /**
     * Adds a new {@link Question Question} object to the corresponding set of Questions.Takes care
     * that the {@link Question} object refers to this one, too.
     *
     * @param question The {@link Question Question} object, which will be added to this
     *                 questionnaire. Must not be <code>null</code>.
     */
    public void addQuestion(final Question question) {
        assert question != null : "The given Question was null";
        if (!questions.contains(question)) {
            questions.add(question);
        }
        //Take care that the objects know each other
        if (question.getQuestionnaire() == null || !question.getQuestionnaire().equals(this)) {
            question.setQuestionnaire(this);
        }
    }

    public void removeQuestion(final Question question) {
        assert question != null : "The given Question was null";
        questions.remove(question);
        if (question.getQuestionnaire() != null && question.getQuestionnaire().equals(this)) {
            question.removeQuestionnaire();
        }
    }

    public void removeAllQuestions() {
        Collection<Question> tempQuestions = new ArrayList<>(questions);
        for (Question question : tempQuestions) {
            removeQuestion(question);
        }
    }

    /**
     * Returns the short name of the current questionnaire object.
     *
     * @return The short name of the current questionnaire object. Is never
     * <code>null</code>, has a least 3 and at most 255 characters.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets a new short name for this questionnaire object.
     *
     * @param name The new short name for this questionnaire object. Must not be
     *             <code>null</code>, has a least 3 (after trimming) and at
     *             most 255 Characters (after trimming). Will be trimmed while setting.
     */
    public void setName(final String name) {
        assert name != null : "The given name was null";
        assert name.trim().length() >= 3 : "The given name has < 3 characters (after trimming)";
        assert
            name.trim().length() <= 255 :
            "The given name has more than 255 Characters (after " + "trimming)";
        this.name = name.trim();
    }

    /**
     * Returns a map with locale codes as keys and localized display name as values.
     *
     * @return Map with strings as keys and strings as values. Is never
     * <code>null</code>.
     */
    public Map<String, String> getLocalizedDisplayName() {
        return localizedDisplayName;
    }

    /**
     * Sets a new map with locale codes as keys and localized display name as values.
     *
     * @param localizedDisplayName A map with strings as keys and strings as values
     */
    public void setLocalizedDisplayName(final Map<String, String> localizedDisplayName) {
        this.localizedDisplayName = localizedDisplayName;
    }

    /**
     * Returns all available languages for this questionnaire. A available language must be present
     * in every question of this questionnaire an in the list of localized display names.
     *
     * @return A list with available language codes for this questionnaire.
     */
    public List<String> getAvailableLanguages() {
        // Get the languages which are available for every question of this
        // questionnaire
        List<String> availableLanguages = getAvailableQuestionLanguages();

        // Get a set with all languages of the display name of this
        // questionnaire
        Set<String> availableDisplayNameLanguages = new HashSet<>();
        for (Map.Entry<String, String> entry : this.getLocalizedDisplayName().entrySet()) {
            // Get the locale code
            String localeCode = entry.getKey();
            // Add all languages to the temp set
            availableDisplayNameLanguages.add(localeCode);
        }

        // Get the intersection of this list and make it to the new available
        // language list
        availableLanguages.retainAll(availableDisplayNameLanguages);

        return availableLanguages;
    }

    /**
     * Returns all languages which are available in each question of this questionnaire.
     *
     * @return A list with language codes which are available for every question of this
     * questionnaire.
     */
    public List<String> getAvailableQuestionLanguages() {
        // Languages which are availables for all questions in this
        // questionnaire
        List<String> availableQuestionLanguages = new ArrayList<>();
        for (Question question : this.getQuestions()) {
            // Languages which are availables for all questions in this
            // questionnaire
            Set<String> availableQuestionLanguagesTemp = new HashSet<>();
            for (Map.Entry<String, String> entry : question.getLocalizedQuestionText().entrySet()) {
                // Get the locale code
                String localeCode = entry.getKey();
                // Add all languages to the temp set
                availableQuestionLanguagesTemp.add(localeCode);
            }
            // If the availableLanuages set is empty, simply add the temp set
            // within all languages of the question
            if (availableQuestionLanguages.isEmpty()) {
                availableQuestionLanguages.addAll(availableQuestionLanguagesTemp);
            } else {
                // Otherwise delete all languages which are not in both sets
                availableQuestionLanguages.retainAll(availableQuestionLanguagesTemp);
            }
        }
        Collections.sort(availableQuestionLanguages);

        return availableQuestionLanguages;
    }

    /**
     * Returns the description of the current questionnaire object. A description might be a longer
     * name for a questionnaire, or something else that clearly identifies it.
     *
     * @return The description of the current questionnaire object. Is never
     * <code>null</code>. Is at least 3 and at most 255 characters long.
     */
    public String getDescription() {
        return description;
    }

    /**
     * See {@link Questionnaire#getDescription()} for a description.Sets a new description for this
     * questionnaire object.
     *
     * @param description The new description for this questionnaire object. Must not be
     *                    <code>null</code>. Has to be at least 3 and at most 255 characters in
     *                    length (after trimming).
     */
    public void setDescription(final String description) {
        assert description != null : "The given description was null";
        assert
            description.trim().length() <= 255 :
            "The given description was longer than 255 " + "characters (after trimming)";
        assert
            description.trim().length() >= 3 :
            "The given description has less than 3 characters " + "after (trimming)";
        this.description = description.trim();
    }

    /**
     * Returns the create time of the questionnaire.
     *
     * @return The create time of the questionnaire. Is never <code>null</code>. Is not in the
     * future.
     */
    public Timestamp getCreatedAt() {
        return createdAt;
    }

    /**
     * Returns the id of the last user that changed this questionnaire object.
     *
     * @return The id of the user that changed this questionnaire object. Is never
     * <code>null</code>. Is never <code> &lt;=0</code>.
     */
    public Long getChangedBy() {
        return changedBy;
    }

    /**
     * Sets the id of the last user that changed this questionnaire object
     *
     * @param changedBy The given changedBy must be not null and must be positive
     */
    public void setChangedBy(final Long changedBy) {
        assert changedBy != null : "The given changedBy-ID was null";
        assert changedBy > 0 : "The given Id is <= 0";
        this.changedBy = changedBy;
    }

    /**
     * Returns the most recent time the questionnaire has been updated.
     *
     * @return The most recent update time the questionnaire has been updated. Might be
     * <code>null</code>. If not, it's not in the future.
     */
    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets the most recent time the questionnaire has been updated.
     *
     * @param updatedAt The most recent update time of the questionnaire. Must not be
     *                  <code>null</code>. Must not be in the future.
     */
    public void setUpdatedAt(final Timestamp updatedAt) {
        assert updatedAt != null : "The given timestamp was null";
        assert !updatedAt.after(
            new Timestamp(System.currentTimeMillis())) : "The given timestamp was in the future";
        this.updatedAt = updatedAt;
    }

    /**
     * Returns whether the questionnaire is published or not.
     *
     * @return <code>true</code> if the questionnaire is published<br>
     * <code>false</code> if it is not published. Is never <code>null</code>.
     */
    @JsonIgnore
    public Boolean isPublished() {
        return isPublished;
    }

    /**
     * Sets the questionnaire as published or not.
     *
     * @param isPublished <code>true</code> if the questionnaire should be
     *                    published<br> <code>false</code> if it should not be published. Must not
     *                    be <code>null</code>.
     */
    public void setPublished(final Boolean isPublished) {
        assert isPublished != null : "The isPublished value was null";
        this.isPublished = isPublished;
    }

    /**
     * The welcome text is the very first screen shown to a patient when starting to conduct a
     * questionnaire. If the questionnaire's {@link Bundle} has a welcome text, too (see
     * {@link Bundle#getLocalizedWelcomeText()}, the bundle's welcome text will be shown first,
     * directly followed by the first questionnaire's welcome text
     * <p>
     * Returns a map with locale codes as keys and localized welcome text as values.
     *
     * @return Map with strings as keys and strings as values. The entries must not be
     * <code>null</code>.
     */
    public Map<String, String> getLocalizedWelcomeText() {
        return localizedWelcomeText;
    }

    /**
     * See {@link Questionnaire#getLocalizedWelcomeText()} for definition.
     * <p>
     * Sets a new map with locale codes as keys and localized welcome text as values.
     *
     * @param localizedWelcomeText A map with strings as keys and strings as values
     */
    public void setLocalizedWelcomeText(final Map<String, String> localizedWelcomeText) {
        this.localizedWelcomeText = localizedWelcomeText;
    }

    /**
     * The final text is the very last screen shown to a patient when finishing a questionnaire. If
     * the questionnaire's {@link Bundle} has a final text, too (see
     * {@link Bundle#getLocalizedFinalText()}, the bundle's final text will be shown directly after
     * the last questionnaire's final text.
     * <p>
     * Returns a map with locale codes as keys and localized display name as values.
     *
     * @return Map with strings as keys and strings as values. Is never
     * <code>null</code>.
     */
    public Map<String, String> getLocalizedFinalText() {
        return localizedFinalText;
    }

    /**
     * See {@link Questionnaire#getLocalizedFinalText() )} for definition.
     * <p>
     * Sets a new map with locale codes as keys and localized final text as values.
     *
     * @param localizedFinalText A map with strings as keys and strings as values
     */
    public void setLocalizedFinalText(final Map<String, String> localizedFinalText) {
        this.localizedFinalText = localizedFinalText;
    }

    /**
     * The logo of the questionnaire is the <code>filename.extension</code> of the corrsponding logo
     * for this questionnaire object.
     *
     * @return <code>filename.extension</code> of this questionnaires's logo.
     */
    public String getLogo() {
        return logo;
    }

    /**
     * See {@link Questionnaire#getLogo()} for definition.
     * <p>
     * Sets a new <code>filename.extension</code> for this questionnaire object.
     *
     * @param logo the new <code>filename.extension</code> for this questionnaire object.
     */
    public void setLogo(final String logo) {
        this.logo = logo;
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
        if (!(obj instanceof Questionnaire)) {
            return false;
        }
        Questionnaire other = (Questionnaire) obj;
        return getUUID().equals(other.getUUID());
    }

    /**
     * Adds an {@link ExportTemplate ExportTemplate} object.
     *
     * @param export An {@link ExportTemplate ExportTemplate} object.
     */
    public void addExportTemplate(final ExportTemplate export) {
        assert export != null : "The ExportTemplate was null";
        this.exportTemplates.add(export);
    }

    /**
     * Returns a set of {@link ExportTemplate ExportTemplate} objects.
     *
     * @return A set of {@link ExportTemplate ExportTemplate} objects. Con not be <code>null</code>.
     * Might be empty. Is unmodifiable.
     */
    public Set<ExportTemplate> getExportTemplates() {
        return Collections.unmodifiableSet(this.exportTemplates);
    }

    /**
     * Removes an {@link ExportTemplate} object from the set of ExportTemplates.
     *
     * @param exportTemplate The {@link ExportTemplate} object, which will be removed. Must not be
     *                       <code>null</code>.
     */
    public void removeExportTemplate(final ExportTemplate exportTemplate) {
        assert exportTemplate != null : "The given ExportTemplate is null";

        exportTemplates.remove(exportTemplate);
    }

    public Set<Score> getScores() {
        return scores;
    }

    public void setScores(final Set<Score> scores) {
        this.scores = scores;
    }

    /**
     * Adds a new {@link Score} object to the corresponding set of Scores.Takes care that the
     * {@link Score} object refers to this one, too.
     *
     * @param score The {@link Score} object, which will be added to this questionnaire. Must not be
     *              <code>null</code>.
     */
    public void addScore(final Score score) {
        assert score != null : "The given Question was null";
        if (!scores.contains(score)) {
            scores.add(score);
        }
        //Take care that the objects know each other
        if (score.getQuestionnaire() == null || !score.getQuestionnaire().equals(this)) {
            score.setQuestionnaire(this);
        }
    }

    public void removeScore(final Score score) {
        assert score != null : "The given Question was null";
        scores.remove(score);
    }

    /**
     * Groups all localized display names for this {@link Questionnaire} by country. The country
     * code is the key in the outer map. The inner map contains the language code as key and the
     * localized display name as value.
     *
     * @return A map with localized display names grouped by country
     */
    public SortedMap<String, Map<String, String>> getLocalizedDisplayNamesGroupedByCountry() {
        SortedMap<String, Map<String, String>> groupedLocalizedQuestionTextByCountry = new TreeMap<>();
        // Loop through each localized question text
        for (Map.Entry<String, String> entry : this.getLocalizedDisplayName().entrySet()) {
            // Get the locale code
            String localeCode = entry.getKey();
            // Set the country to the locale code by default
            String country = localeCode.toUpperCase();
            // Set the question text to the localized question text by default
            String displayName = entry.getValue();
            // If the locale contains country and language code seperated by '_'
            // split this locale code and get the country from the second part.
            // (i.e. de_DE --> country is DE). The first part of the split
            // result is the language code.
            if (localeCode.contains("_")) {
                String[] parts = localeCode.split("_");
                country = parts[1];
                displayName = entry.getValue();
            }

            // If the sorted map already contains the country, add this
            // localized
            // question text with its related language code
            if (groupedLocalizedQuestionTextByCountry.containsKey(country)) {
                groupedLocalizedQuestionTextByCountry.get(country).put(localeCode, displayName);
                // Otherwise this is the first question text for this country
                // and a new
                // map for the question texts has to be setup and filled with
                // the first
                // question text and its related language code
            } else {
                Map<String, String> localeQuestionTextMap = new HashMap<>();
                localeQuestionTextMap.put(localeCode, displayName);
                groupedLocalizedQuestionTextByCountry.put(country, localeQuestionTextMap);
            }
        }
        return groupedLocalizedQuestionTextByCountry;
    }

    /**
     * Returns if the Questionnaire is deletable. It is not deletable if there is already an
     * response to a corresponding question/answer.
     *
     * @return If the Questionnaire is deletable.
     */
    public boolean isDeletable() {
        for (Question question : this.getQuestions()) {
            if (!question.isDeletable()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Indicates whether the {@link Questionnaire} has {@link Condition conditions} attached or
     * not.
     *
     * @return <code>true</code>: the {@link Questionnaire} has
     * {@link Condition conditions} attached <br> <code>false</code>: the {@link Questionnaire} has
     * no {@link Condition conditions} attached. Is never <code>null</code>.
     */
    public boolean isHasConditions() {
        return hasConditions;
    }

    /**
     * Sets whether the {@link Questionnaire} has {@link Condition conditions} attached or not.
     *
     * @param hasConditions <code>true</code>: the {@link Questionnaire} has
     *                      {@link Condition conditions} attached <br>
     *                      <code>false</code>: the
     *                      {@link Questionnaire} has no {@link Condition conditions} attached. Must
     *                      not be <code>null</code>.
     */
    public void setHasConditions(final Boolean hasConditions) {
        this.hasConditions = hasConditions;
    }

    /**
     * Get all {@link Question Questions} which are available in this questionnaire to be used by a
     * {@link Score}.
     *
     * @return A {@link List} with {@link Question Questions} available for a {@link Score}
     */
    public List<Question> getAvailableQuestionsForScore() {
        List<Question> availableQuestionsForScore = new ArrayList<>();
        for (Question question : this.getQuestions()) {
            if (question.getQuestionType() == QuestionType.NUMBER_INPUT
                || question.getQuestionType() == QuestionType.SLIDER
                || question.getQuestionType() == QuestionType.NUMBER_CHECKBOX
                || question.getQuestionType() == QuestionType.NUMBER_CHECKBOX_TEXT) {

                availableQuestionsForScore.add(question);
                // Add multiple choice and dropdown questions only if it
                // allows exactly one answer
            } else if ((question.getQuestionType() == QuestionType.MULTIPLE_CHOICE
                || question.getQuestionType() == QuestionType.DROP_DOWN)
                && question.getMaxNumberAnswers() == 1) {
                availableQuestionsForScore.add(question);
            }
        }
        return availableQuestionsForScore;
    }

    /**
     * Get all {@link Score Scores} which are available in this questionnaire to be used by a given
     * {@link Score}.
     *
     * @param score The {@link Score} for which the available {@link Score Scores} should be
     *              returned.
     * @return A {@link List} with {@link Score Scores} available for the given {@link Score}
     */
    public List<Score> getAvailableScoresForScore(final Score score) {
        // Get all possible Scores that is all Scores of the Questionnaire
        Set<Score> availableScores = new HashSet<>(this.getScores());
        // Get all depending Scores
        List<Score> dependingScores = score.getDependingScores();
        // Remove the depending Scores fom the possible Scores and return it
        availableScores.removeAll(dependingScores);
        availableScores.remove(score);
        return new ArrayList<>(availableScores);
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public void setQuestions(Set<Question> questions) {
        this.questions = questions;
    }

    public boolean isModifiable() {
        for (Question question : this.getQuestions()) {
            if (!question.isModifiable()) {
                return false;
            }
        }
        return true;
    }

    public boolean isOriginal() {
        return version == 1;
    }

    public QuestionnaireVersionGroup getQuestionnaireVersionGroup() {
        return questionnaireVersionGroup;
    }
    
    public void setQuestionnaireVersionGroup(QuestionnaireVersionGroup questionnaireVersionGroup) {
        this.questionnaireVersionGroup = questionnaireVersionGroup;
    }

    public Long getQuestionnaireVersionGroupId() {
        return (questionnaireVersionGroup != null) ? questionnaireVersionGroup.getId() : null;
    }
}
