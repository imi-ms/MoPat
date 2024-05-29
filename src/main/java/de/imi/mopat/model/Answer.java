package de.imi.mopat.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.imi.mopat.helper.model.UUIDGenerator;
import de.imi.mopat.model.conditions.Condition;
import de.imi.mopat.model.conditions.ConditionTarget;
import de.imi.mopat.model.conditions.ConditionTrigger;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * The database table model for table <i>answer</i>. An answer represents an option a patient can
 * choose while conducting a questionnaire. E.g., if a patient is asked about his/her condition and
 * he/she shall pick one out of 4 different options ('super','good','meh','bad'), each of the
 * possible options is represented by an answer object. Since the type of answer varies in
 * correlation with the type of Question (see {@link de.imi.mopat.model.enumeration.QuestionType}),
 * an answer might have different attributes. <br>To separate between different types of answers,
 * inheritance is being used. Thus, this class contains all common attributes and methods for
 * answers, while the subclasses contain their own attributes.
 * <br>If you want to know which answer has been chosen by a patient within a
 * questionnaire, see {@link Response}.
 */
@Entity
@Table(name = "answer")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "answer_type", discriminatorType = DiscriminatorType.STRING)
public abstract class Answer implements Serializable, ConditionTrigger, ConditionTarget {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;
    @JsonIgnore
    @Column(name = "uuid")
    private String uuid = UUIDGenerator.createUUID();
    @JsonIgnore
    @NotNull(message = "{answer.question.notNull}")
    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "question_id", referencedColumnName = "id")
    @Valid
    private Question question;
    @JsonIgnore
    @OneToMany(mappedBy = "answer")
    private Set<Response> responses = new HashSet<>();
    @JsonIgnore
    @OneToMany(mappedBy = "answer", cascade = {CascadeType.ALL}, orphanRemoval = true)
    private Set<ExportRuleAnswer> exportRules = new HashSet<>();
    @NotNull(message = "{selectAnswer.label.notNull}")
    @Column(name = "is_enabled")
    private Boolean isEnabled;

    // [bt] as long as not all Answer classes are implementing the
    // ConditionTrigger interface, the property and methos need to be in the
    // respective sub-classes
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(name = "mopat_condition_answer", joinColumns = {
        @JoinColumn(name = "answer_id", referencedColumnName = "id")}, inverseJoinColumns = {
        @JoinColumn(name = "condition_id", referencedColumnName = "id")})
    private Set<Condition> conditions = new HashSet<>();

    protected Answer() { // default constructor (in protected state), should
        // not be accessible to
        // anything else but the JPA implementation (here: Hibernate) and the
        // JUnit tests
    }

    /**
     * Uses the setters to initialise the object. See setters for constraints.
     *
     * @param question  References to the actual {@link Question} connected to this answer.
     * @param isEnabled Indicates whether this answer is enabled or not.
     */
    public Answer(Question question, Boolean isEnabled) {
        setQuestion(question);
        setIsEnabled(isEnabled);
    }

    /**
     * Creates a new {@link Answer} instance with a new UUID and a non-valid id. Copies the Answer's
     * attributes to the new one. The referenced {@link Response Responses} and {@link Question} are
     * neither cloned nor referenced, which results in a plain new {@link Answer} instance.
     *
     * @return a new {@link Answer} as described above. Is never
     * <code>null</code>.
     */
    public abstract Answer cloneWithoutReferences();

    /**
     * Returns the id of the current answer object.
     *
     * @return id The current id of this answer object. Might be
     * <code>null</code> for newly created objects. Is never
     * <code> &lt;= 0</code>.
     */
    public Long getId() {
        return id;
    }

    private String getUUID() {
        return this.uuid;
    }

    /**
     * Returns the isEnabled value of the current answer object.
     *
     * @return The current value of this answer's object property isEnabled.
     */
    public Boolean getIsEnabled() {
        return isEnabled;
    }

    /**
     * Sets the value of the current answer's object property isEnabled.
     *
     * @param isEnabled The new value to set.
     */
    public void setIsEnabled(Boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    /**
     * Returns the question the answer is associated with. An answer is always associated with
     * <i>exactly</i> one question.
     *
     * @return The {@link Question Question} object the answer is associated with. Is never
     * <code>null</code>.
     */
    public Question getQuestion() {
        return question;
    }

    /**
     * See {@link Answer#getQuestion()} for a description.Associates the answer with a new
     * question.
     * <p>
     * Takes care that the {@link Question} objects refers to this one, too.
     *
     * @param question The new {@link Question} object of this answer. Must not be
     *                 <code>null</code>.
     */
    public void setQuestion(final Question question) {
        assert question != null : "The given Question object is null";
        this.question = question;
        // Take care that the objects know each other
        if (!question.getAnswers().contains(this)) {
            question.addAnswer(this);
        }
    }

    /**
     * See {@link Answer#getQuestion()} for a description.
     * <p>
     * Takes care that the {@link Question} does no longer refer to this object. Also removes this
     * objects reference to the {@link Question}.
     */
    public void removeQuestion() {
        if (question != null) {
            Question questionTemp = question;
            question = null;
            if (questionTemp.getAnswers().contains(this)) {
                questionTemp.removeAnswer(this);
            }
        }
    }

    /**
     * A response indicates which answer has been picked by the patient within an encounter.
     * <p>
     * Returns all {@link Response} objects of this answer object and therefore contains all
     * responses ever given to this answer
     *
     * @return Can be empty, but is never <code>null</code>. Is unmodifiable.
     */
    public Set<Response> getResponses() {
        return Collections.unmodifiableSet(responses);
    }

    /**
     * Adds all given {@link Response} objects that are not already associated with this answer to
     * the corresponding set of Responses.Takes care that the responses refer to this answer, too.
     *
     * @param responses The set of additional {@link Response} objects for this answer object. Must
     *                  not be <code>null</code>.
     */
    public void addResponses(final Set<Response> responses) {
        assert responses != null : "The given responses were null";
        for (Response response : responses) {
            addResponse(response);
        }
    }

    /**
     * See {@link Answer#getResponses()} for a description for response
     * <p>
     * Takes care that the {@link Response} refers to this answer as well.
     *
     * @param response Must not be <code>null</code>.
     */
    public void addResponse(final Response response) {
        assert response != null : "The given Response was null";
        if (!responses.contains(response)) {
            responses.add(response);
        }
        if (response.getAnswer() == null || !response.getAnswer().equals(this)) {
            response.setAnswer(this);
        }
    }

    /**
     * Removes the given {@link Response} from this answer
     *
     * @param response {@link Response} that should be removed
     * @throws AssertionError If the given parameter is invalid.
     */
    public void removeResponse(final Response response) {
        assert response != null : "The given response was null";
        responses.remove(response);
    }

    /**
     * See {@link ExportTemplate#getExportRules()} for a description for export rules
     * <p>
     * Returns all {@link ExportRuleAnswer} objects of this answer object and therefore contains all
     * export rules ever given to this answer
     *
     * @return A set of {@link ExportRuleAnswer} objects. Can be empty, but is never
     * <code>null</code>. Is unmodifiable.
     */
    public Set<ExportRuleAnswer> getExportRules() {
        return Collections.unmodifiableSet(exportRules);
    }

    /**
     * Adds all given {@link ExportRuleAnswer} objects that are not already associated with this
     * answer to the corresponding set of ExportRules. Takes care that the export rules refer to
     * this answer, too.
     *
     * @param exportRules The set of additional {@link ExportRuleAnswer} objects for this answer
     *                    object. Must not be <code>null</code>.
     * @throws AssertionError} If the given parameter is invalid.
     */
    public void addExportRules(final Set<ExportRuleAnswer> exportRules) {
        assert exportRules != null : "The given export rules were null";
        for (ExportRuleAnswer exportRule : exportRules) {
            addExportRule(exportRule);
        }
    }

    /**
     * See {@link Answer#getExportRules()} for a description for exportRule.
     * <p>
     * Takes care that the {@link ExportRuleAnswer} refers to this answer as well.
     *
     * @param exportRule An {@link ExportRule} object. Must not be
     *                   <code>null</code>.
     * @throws AssertionError If the given parameter is invalid.
     */
    public void addExportRule(final ExportRuleAnswer exportRule) {
        assert exportRule != null : "The given ExportRule was null";
        if (!exportRules.contains(exportRule)) {
            exportRules.add(exportRule);
        }
        // take care the objects know each other
        if (exportRule.getAnswer() == null || !exportRule.getAnswer().equals(this)) {
            exportRule.setAnswer(this);
        }
    }

    /**
     * See {@link Answer#getExportRules()} for a description for exportRule
     * <p>
     * Takes care that the {@link ExportRuleAnswer} no more refers to this answer as well.
     *
     * @param exportRule Must not be <code>null</code>.
     * @throws AssertionError If the given parameter is invalid.
     */
    public void removeExportRule(final ExportRuleAnswer exportRule) {
        assert exportRule != null : "The given ExportRule was null";
        // If the ExportRule is in the set, remove it and remove this Answer
        // from the ExportRule
        if (exportRules.remove(exportRule)) {
            exportRule.removeAnswer();
        }
    }

    /**
     * Return a Set of {@link ExportRule} objects depending on the given ExportTemplate.
     *
     * @param exportTemplate An {@link ExportTemplate} object. Must not be
     *                       <code>null</code>.
     * @return A set of {@link ExportRule} objects. Is never <code>null</code>. Might be empty.
     */
    public Set<ExportRule> getExportRulesByExportTemplate(final ExportTemplate exportTemplate) {
        Set<ExportRule> exportRulesByTemplate = new HashSet<>();
        for (ExportRule rule : exportRules) {
            if (rule.getExportTemplate().equals(exportTemplate)) {
                exportRulesByTemplate.add(rule);
            }
        }
        return exportRulesByTemplate;
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
        if (!(obj instanceof Answer)) {
            return false;
        }
        Answer other = (Answer) obj;
        return getUUID().equals(other.getUUID());
    }

    public void addCondition(final Condition condition) {
        assert condition != null : "The Condition was null";
        this.conditions.add(condition);
        // take care the objects know each other
        if (condition.getTrigger() == null || !condition.getTrigger().equals(this)) {
            condition.setTrigger((ConditionTrigger) this);
        }
    }

    @Override
    public void addConditions(final Set<Condition> conditions) {
        assert conditions != null : "The given set of Conditions was null";
        for (Condition condition : conditions) {
            addCondition(condition);
        }
    }

    @Override
    public boolean contains(final Condition condition) {
        assert condition != null : "The given Condition was null";
        return this.conditions.contains(condition);
    }

    @Override
    public Set<Condition> getConditions() {
        return Collections.unmodifiableSet(this.conditions);
    }

    @Override
    public void removeCondition(final Condition condition) {
        assert condition != null : "The given Condition was null";
        // If the condition is in the set, remove it and remove it as trigger
        if (this.conditions.remove(condition)) {
            condition.removeTrigger();
        }
    }

    @Override
    public void removeConditions(final Set<Condition> conditions) {
        assert conditions != null : "The given Set of Conditions was null";
        Collection<Condition> tempConditions = new HashSet<Condition>(conditions);
        for (Condition condition : tempConditions) {
            removeCondition(condition);
        }
    }
}
