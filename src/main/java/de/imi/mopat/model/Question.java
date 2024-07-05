package de.imi.mopat.model;

import de.imi.mopat.model.dto.export.SliderIconDTO;
import de.imi.mopat.model.enumeration.QuestionType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import de.imi.mopat.helper.controller.Constants;
import de.imi.mopat.helper.controller.ServletContextInfo;
import de.imi.mopat.helper.controller.StringUtilities;
import de.imi.mopat.helper.model.UUIDGenerator;
import de.imi.mopat.model.conditions.Condition;
import de.imi.mopat.model.conditions.ConditionTarget;
import de.imi.mopat.model.conditions.ConditionTrigger;
import de.imi.mopat.model.dto.AnswerDTO;
import de.imi.mopat.model.dto.ConditionDTO;
import de.imi.mopat.model.dto.QuestionDTO;
import de.imi.mopat.model.enumeration.BodyPart;
import de.imi.mopat.model.enumeration.CodedValueType;
import de.imi.mopat.model.score.Score;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.eclipse.persistence.annotations.CascadeOnDelete;

/**
 * The database table model for table <i>question</i>. This model holds the general information
 * about a question. It contains the actual questionText and the question's position within a
 * questionnaire. Since there are many different types of question, this model refers to
 * {@link QuestionType}.
 */
@Entity
@Table(name = "question")
public class Question implements Serializable, Comparable<Question>, ConditionTarget,
    ConditionTrigger {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;
    @JsonIgnore
    @Column(name = "uuid")
    private String uuid = UUIDGenerator.createUUID();

    @ElementCollection
    @MapKeyColumn(name = "language")
    @Column(name = "question_text", columnDefinition = "TEXT NOT NULL")
    @CollectionTable(name = "question_question_text", joinColumns = @JoinColumn(name = "id"))
    private Map<String, String> localizedQuestionText;

    @NotNull(message = "{question.isRequired.notNull}")
    @Column(name = "is_required", nullable = false)
    private Boolean isRequired;
    @NotNull(message = "{question.isEnabled.notNull}")
    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled;
    @NotNull(message = "{question.questionType.notNull}")
    @Enumerated(EnumType.STRING)
    @Column(name = "question_type")
    private QuestionType questionType;

    // @NotNull wanted, but not implementable, since Spring creates a new,
    // empty Question for new questions and with this, auto-validation (see
    // QuestionController.editQuestion, param @Valid Question question, would
    // fail
    @Column(name = "position", nullable = false)
    private Integer position;
    @Min(value = 0, message = "{question.minNumberAnswers.min}")
    @Column(name = "min_number_answers")
    private Integer minNumberAnswers;
    @Min(value = 0, message = "{question.maxNumberAnswers.min}")
    @Column(name = "max_number_answers")
    private Integer maxNumberAnswers;
    @Column(name = "coded_value_type")
    @Enumerated(EnumType.STRING)
    private CodedValueType codedValueType;
    @Transient
    private Boolean hasConditionsAsTarget;
    @Transient
    private Boolean hasScores;

    // [bt] @Valid might be possible, but is chosen not to be taken, because
    // our complex validators call the JSR-303 validation stuff cascadingly
    // and with this, @Valid is implicitly covered.
    @OneToMany(mappedBy = "question", cascade = {CascadeType.ALL}, orphanRemoval = true)
    @CascadeOnDelete
    private List<Answer> answers = new ArrayList<>();
    // @NotNull wanted, but not implementable, since Spring creates a new,
    // emtpy Question for new questions and with this, auto-validation (see
    // QuestionController.editQuestion, param @Valid Question question, would
    // fail
    // [bt] @Valid might be possible, but is chosen not to be taken, because
    // our complex validators call the JSR-303 validation stuff cascadingly
    // and with this, @Valid is implicitly covered.
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "questionnaire_id", referencedColumnName = "id")
    private Questionnaire questionnaire;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(name = "mopat_condition_question", joinColumns = {
        @JoinColumn(name = "question_id", referencedColumnName = "id")}, inverseJoinColumns = {
        @JoinColumn(name = "condition_id", referencedColumnName = "id")})
    private Set<Condition> conditions = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "question", cascade = {CascadeType.ALL}, orphanRemoval = true)
    private Set<ExportRuleQuestion> exportRules = new HashSet<>();

    public Question() { // default constructor, should not be accessible to
        // anything else but the JPA implementation (here: Hibernate) and the
        // JUnit tests
    }

    /**
     * Uses the setters to initialize the object.See setters for constraints.
     *
     * @param localizedQuestionText A map with locale codes as key and the localized question text
     *                              as value
     * @param isRequired            Marks this question whether it is as required or not
     * @param isEnabled             Marks this question whether it is initially enabled or not
     * @param questiontype          The {@link QuestionType} of this question
     * @param position              The position of this question within the {@link Questionnaire}
     * @param questionnaire         The {@link Questionnaire} the question belongs to
     */
    public Question(final Map<String, String> localizedQuestionText, final Boolean isRequired,
        final Boolean isEnabled, final QuestionType questiontype, final Integer position,
        final Questionnaire questionnaire) {
        setLocalizedQuestionText(localizedQuestionText);
        setIsRequired(isRequired);
        setIsEnabled(isEnabled);
        setQuestionType(questiontype);
        setPosition(position);
        setQuestionnaire(questionnaire);
    }

    /**
     * Returns the id of the current question object.
     *
     * @return The current id of this question object. Might be
     * <code>null</code> for newly created objects. Is never <code> &lt;= 0
     * </code>.
     */
    @Override
    public Long getId() {
        return id;
    }

    private String getUUID() {
        return this.uuid;
    }

    /**
     * Returns a map with locale codes as keys and localized question texts as values.
     *
     * @return Map with strings as keys and strings as values. Is never
     * <code>null</code>.
     */
    public Map<String, String> getLocalizedQuestionText() {
        return localizedQuestionText;
    }

    /**
     * Sets a new map with locale codes as keys and localized question texts as values.
     *
     * @param localizedQuestionText A map with strings as keys and strings as values
     */
    public void setLocalizedQuestionText(final Map<String, String> localizedQuestionText) {
        this.localizedQuestionText = localizedQuestionText;
    }

    /**
     * The "isRequired" value enables a questionnaire to be completed without having answered all
     * questions. This can be used for skipable questions and context sensitive questionnaires, in
     * which some questions will be skipped automatically.
     *
     * @return <code>true</code> if the question must be answered for the
     * questionnaire to be correctly completed <br> <code>false</code> if the question is optional.
     * Is never <code>null</code>.
     */
    public Boolean getIsRequired() {
        return isRequired;
    }

    /**
     * See {@link Question#getIsRequired()} for a description
     * <p>
     * Sets the required state of the question object.
     *
     * @param isRequired <code>true</code> if the question must be answered for
     *                   the questionnaire to be correctly completed <br>
     *                   <code>false</code> if
     *                   the question is optional. Must not be
     *                   <code>null</code>.
     */
    public void setIsRequired(final Boolean isRequired) {
        assert isRequired != null : "The given isRequired value was null";
        this.isRequired = isRequired;
    }

    public Boolean getIsEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(final Boolean isEnabled) {
        assert isEnabled != null : "The given isEnabled value was null";
        this.isEnabled = isEnabled;
    }

    /**
     * Returns the type of the question. See {@link QuestionType} for type definition.
     *
     * @return the question's {@link QuestionType}. Is never <code>null</code>.
     */
    public QuestionType getQuestionType() {
        return questionType;
    }

    /**
     * See {@link QuestionType} for type definition.
     *
     * @param questiontype the type of question to be set. Must not be
     *                     <code>null</code>.
     */
    public void setQuestionType(final QuestionType questiontype) {
        assert questiontype != null : "The question type given was null.";
        this.questionType = questiontype;
    }

    /**
     * Returns the position of the question within a questionnaire.
     * <s>Identical positions to other questions of the same questionnaire are
     * possible. </s>
     *
     * @return The position of the question within the questionnaire, starting at 1. Is never
     * <code>null</code>.
     */
    public Integer getPosition() {
        return position;
    }

    /**
     * Sets a new position for this question object.See {@link Question#getPosition()} for a
     * description
     *
     * @param position Must not be <code>null</code>. Must be <code> &gt;0
     *                 </code>.
     */
    public void setPosition(final Integer position) {
        assert position != null : "The given position was null";
        assert position >= 1 : "The given position was negative";
        this.position = position;
    }

    /**
     * Returns the minimum number of answers a person is able to choose from. Is only important for
     * questions of type {@link QuestionType#MULTIPLE_CHOICE} or {@link QuestionType#DROP_DOWN}
     *
     * @return Might be <code>null</code>.
     */
    public Integer getMinNumberAnswers() {
        return minNumberAnswers;
    }

    /**
     * Sets the minimum number of answers a person is able to choose from.
     *
     * @param minNumberAnswers Can be <code>null</code>. If not
     *                         <code>null</code>, it must be <code> &gt;=0
     *                         </code> and <code> &lt;= maxNumberAnswers
     *                         </code>.
     */
    public void setMinNumberAnswers(final Integer minNumberAnswers) {
        if (minNumberAnswers != null) {
            assert minNumberAnswers >= 0 : "The given minimum number of answers was negative";
            if (maxNumberAnswers != null) {
                assert
                    minNumberAnswers <= maxNumberAnswers : "The given minimum number of "
                    + "answers was bigger than the maximum number of " + "answers";
            }
        }
        this.minNumberAnswers = minNumberAnswers;
    }

    /**
     * Returns the maximum number of answers a person is able to choose from. Is only important for
     * questions of type {@link QuestionType#MULTIPLE_CHOICE} or {@link QuestionType#DROP_DOWN}
     *
     * @return Might be <code>null</code>.
     */
    public Integer getMaxNumberAnswers() {
        return maxNumberAnswers;
    }

    /**
     * Sets the maximum number of answers a person is able to choose from.
     *
     * @param maxNumberAnswers Can be <code>null</code>. If not
     *                         <code>null</code>, it must be <code> &gt;=0
     *                         </code> and <code> &gt;= minNumberAnswers
     *                         </code>.
     */
    public void setMaxNumberAnswers(final Integer maxNumberAnswers) {
        if (maxNumberAnswers != null) {
            assert maxNumberAnswers >= 0 : "The given maximum number of answers was negative";
            if (minNumberAnswers != null) {
                assert
                    maxNumberAnswers >= minNumberAnswers : "The given maximum number of "
                    + "answers was smaller than the minimum number of " + "answers";
            }
        }
        this.maxNumberAnswers = maxNumberAnswers;
    }

    /**
     * Sets the minimum and maximum number of answers a person is able to choose from.
     *
     * @param minNumberAnswers Can be <code>null</code>. If not
     *                         <code>null</code>, it must be <code> &gt;=0
     *                         </code>; if <code>maxNumber != null
     *                         </code>, must be <code> &lt;= maxNumberAnswers
     *                         </code>.
     * @param maxNumberAnswers Can be <code>null</code>. If not
     *                         <code>null</code>, it must be <code> &gt;=0
     *                         </code>; if <code>minNumber != null
     *                         </code>, must be <code> &gt;= minNumberAnswers
     *                         </code>.
     */
    public void setMinMaxNumberAnswers(final Integer minNumberAnswers,
        final Integer maxNumberAnswers) {
        if (minNumberAnswers != null) {
            assert minNumberAnswers >= 0 : "The given minimum number of answers was negative";
        }
        if (maxNumberAnswers != null) {
            assert maxNumberAnswers >= 0 : "The given maximum number of answers was negative";
        }
        if (minNumberAnswers != null && maxNumberAnswers != null) {
            assert
                maxNumberAnswers >= minNumberAnswers : "The given maximum number of "
                + "answers was smaller than the minimum number of answers";
        }
        this.minNumberAnswers = minNumberAnswers;
        this.maxNumberAnswers = maxNumberAnswers;
    }

    /**
     * Returns the coded value type.
     *
     * @return The coded value type
     */
    public CodedValueType getCodedValueType() {
        return codedValueType;
    }

    /**
     * Sets the coded value type of this {@link Question}.
     *
     * @param codedValueType New coded value type
     */
    public void setCodedValueType(final CodedValueType codedValueType) {
        this.codedValueType = codedValueType;
    }

    /**
     * Returns all {@link Answer Answer} objects of the current question object.
     *
     * @return The current {@link Answer Answer} objects of this question object. Is never
     * <code>null</code>. Might be empty (questions without answers but only info). Is
     * unmodifiable.
     */
    public List<Answer> getAnswers() {
        return Collections.unmodifiableList(answers);
    }

    /**
     * Adds all given {@link Answer Answer} objects that are not already associated with this
     * question to the corresponding set of Answers.Takes care that the {@link Answer} objects refer
     * to this one, too.
     *
     * @param answers The set of additional {@link Answer Answer} objects for this question object.
     *                Must not be <code>null</code>.
     */
    public void addAnswers(final Set<Answer> answers) {
        assert answers != null : "The given set was null";
        for (Answer answer : answers) {
            addAnswer(answer);
        }

    }

    /**
     * Adds a new {@link Answer Answer} object to the corresponding set of Answers. Takes care that
     * the {@link Answer} object refers to this one, too.
     *
     * @param answer The {@link Answer Answer} object, which will be added to this question. Must
     *               not be <code>null</code>.
     */
    public void addAnswer(final Answer answer) {
        assert answer != null : "The given Answer was null";
        answers.add(answer);
        // Take care that the objects know each other
        if (answer.getQuestion() == null || !answer.getQuestion().equals(this)) {
            answer.setQuestion(this);
        }
    }

    /**
     * Takes care that the {@link Answer} doesn't refer to this object anymore. But removes the
     * reference from {@link Answer} to this object only if the given {@link Answer} was part of
     * this question and the answer referred to this question.
     *
     * @param answer Must not be <code>null</code>.
     */
    public void removeAnswer(final Answer answer) {
        assert answer != null : "The given Answer was null";
        answers.remove(answer);
        if (answer.getQuestion() != null && answer.getQuestion().equals(this)) {
            answer.removeQuestion();
        }
    }

    public void removeAllAnswers() {
        Collection<Answer> tempAnswers = new ArrayList<Answer>(answers);
        for (Answer answer : tempAnswers) {
            removeAnswer(answer);
        }
    }

    /**
     * Returns the questionnaire the question is associated with. A question is always associated
     * with <i>exactly</i> one questionnaire.
     *
     * @return The {@link Questionnaire Questionnaire} object the question is associated with. Might
     * be <code>null</code> for newly created questions, will never be <code>null</code> after
     * persisting it.
     */
    @JsonIgnore
    public Questionnaire getQuestionnaire() {
        return questionnaire;
    }

    /**
     * Sets a new {@link Questionnaire} this {@link Question} should be associated with.Takes care
     * that the {@link Questionnaire} refers to this object, too.
     *
     * @param questionnaire The {@link Questionnaire} object this question should be associated
     *                      with. Must not be
     *                      <code>null</code>.
     */
    public void setQuestionnaire(final Questionnaire questionnaire) {
        assert questionnaire != null : "The given questionnaire was null";
        this.questionnaire = questionnaire;
        // Take care that the objects know each other
        if (!questionnaire.getQuestions().contains(this)) {
            questionnaire.addQuestion(this);
        }
    }

    /**
     * See {@link Question#getQuestionnaire()} for a description.
     * <p>
     * Takes care that the {@link Questionnaire} does not longer refer to this object. Also removes
     * this objects reference to the {@link Questionnaire}.
     */
    @JsonIgnore
    public void removeQuestionnaire() {
        if (questionnaire != null) {
            Questionnaire questionnaireTemp = questionnaire;
            questionnaire = null;
            if (questionnaireTemp.getQuestions().contains(this)) {
                questionnaireTemp.removeQuestion(this);
            }
        }
    }

    /**
     * Add a {@link Condition} to this question.
     *
     * @param condition The {@link Condition} to be added
     */
    @Override
    public void addCondition(final Condition condition) {
        assert condition != null : "The Condition given was null";
        this.conditions.add(condition);
        if (condition.getTrigger() == null || !condition.getTrigger().equals(this)) {
            condition.setTrigger((ConditionTrigger) this);
        }
    }

    /**
     * Add a set of conditions to this question.
     *
     * @param conditions The set of {@link Condition Conditions} to be added
     */
    @Override
    public void addConditions(final Set<Condition> conditions) {
        assert conditions != null : "The given set of Conditions was null";
        for (Condition condition : conditions) {
            addCondition(condition);
        }
    }

    /**
     * Check if this {@link Question} contains the given {@link Condition}. Returns true if it
     * contains it, false otherwise.
     *
     * @param condition The given {@link Condition} for which will be searched
     * @return True if this {@link Question} contains the given {@link Condition}, false otherwise.
     */
    @Override
    public boolean contains(final Condition condition) {
        assert condition != null : "The given Condition was null";
        return this.conditions.contains(condition);
    }

    /**
     * Returns all {@link Condition} objects of the current {@link Question} object.
     *
     * @return The current {@link Condition} objects of this {@link Question} object.
     */
    @Override
    public Set<Condition> getConditions() {
        return Collections.unmodifiableSet(this.conditions);
    }

    /**
     * Removes a given {@link Condition} from the this question.
     *
     * @param condition The {@link Condition} which should be removed
     */
    @Override
    public void removeCondition(final Condition condition) {
        assert condition != null : "The given Condition was null";
        this.conditions.remove(condition);
        if (condition.getTrigger() != null && condition.getTrigger().equals(this)) {
            condition.removeTrigger();
        }
    }

    /**
     * Removes a given set {@link Condition} objects from the {@link Question} question.
     *
     * @param conditions The set of {@link Condition} objects which should be removed
     */
    @Override
    public void removeConditions(final Set<Condition> conditions) {
        assert conditions != null : "The given Set of Conditions was null";
        Collection<Condition> tempConditions = new HashSet<Condition>(conditions);
        for (Condition condition : tempConditions) {
            removeCondition(condition);
        }
    }

    /**
     * Check if this {@link Question} has any {@link Condition}. Returns true if there is at least
     * one {@link Condition} for this {@link Question} and false otherwise.
     *
     * @return True if there is a {@link Condition} for this {@link Question}. False otherwise
     */
    @JsonIgnore
    public boolean hasConditionsAsTrigger() {
        for (Answer answer : this.getAnswers()) {
            if (answer instanceof SelectAnswer) {
                if (!((SelectAnswer) answer).getConditions().isEmpty()) {
                    return true;
                }
            } else if (answer instanceof SliderAnswer) {
                if (!((SliderAnswer) answer).getConditions().isEmpty()) {
                    return true;
                }
            }
        }
        return !this.getConditions().isEmpty();
    }

    /**
     * Indicates whether the {@link Question} has {@link Condition conditions} attached as a target
     * or not.
     *
     * @return <code>true</code>: the {@link Question} has
     * {@link Condition conditions} attached <br> <code>false</code>: the {@link Question} has no
     * {@link Condition conditions} attached. Is never
     * <code>null</code>.
     */
    public boolean hasConditionsAsTarget() {
        return hasConditionsAsTarget;
    }

    /**
     * Sets whether the {@link Question} has {@link Condition conditions} attached as a target or
     * not.
     *
     * @param hasConditions <code>true</code>: the {@link Question} has
     *                      {@link Condition conditions} attached <br>
     *                      <code>false</code>: the
     *                      {@link Question} has no {@link Condition conditions} attached. Must not
     *                      be <code>null</code>.
     */
    public void setHasConditionsAsTarget(final Boolean hasConditions) {
        this.hasConditionsAsTarget = hasConditions;
    }

    /**
     * Indicates whether the {@link Question} has {@link Score scores} attached or not.
     *
     * @return <code>true</code>: the {@link Question} has {@link Score scores}
     * attached <br> <code>false</code>: the {@link Question} has no {@link Score scores} attached.
     * Is never <code>null</code>.
     */
    public boolean hasScores() {
        return hasScores;
    }

    /**
     * Sets whether the {@link Question} has {@link Score scores} attached or not.
     *
     * @param hasScores <code>true</code>: the {@link Question} has
     *                  {@link Score scores} attached <br>
     *                  <code>false</code>: the
     *                  {@link Question} has no {@link Score scores} attached . Must not be
     *                  <code>null</code>.
     */
    public void setHasScores(final Boolean hasScores) {
        this.hasScores = hasScores;
    }

    /**
     * Check if this {@link Question} has any {@link ExportRule}. Returns true if there is at least
     * one {@link ExportRule} for this {@link Question} and false otherwise.
     *
     * @return True if there is a {@link ExportRule} for this {@link Question}. False otherwise
     */
    @JsonIgnore
    public boolean hasExportRule() {
        for (Answer answer : this.getAnswers()) {
            if (!answer.getExportRules().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns an {@link ExportRuleFormat} object if an answer to this {@link Question} has an
     * {@link ExportRule} object. Can be
     * <code>null</code>.
     *
     * @param exportTemplate An {@link ExportTemplate} object.
     * @return An {@link ExportRuleFormat} object. Can be <code>null</code>.
     */
    public ExportRuleFormat getExportRuleFormatFromAnswers(final ExportTemplate exportTemplate) {
        for (Answer answer : this.getAnswers()) {
            Set<ExportRule> otherRules = answer.getExportRulesByExportTemplate(exportTemplate);
            if (!otherRules.isEmpty()) {
                return otherRules.iterator().next().getExportRuleFormat();
            }
        }
        return null;
    }

    /**
     * Returns an {@link ExportRuleFormat} object if this {@link Question} has an {@link ExportRule}
     * object. Can be <code>null</code>.
     *
     * @param exportTemplate An {@link ExportTemplate} object.
     * @return An {@link ExportRuleFormat} object. Can be <code>null</code>.
     */
    public ExportRuleFormat getExportRuleFormatFromQuestion(final ExportTemplate exportTemplate) {
        Set<ExportRule> otherRules = this.getExportRulesByExportTemplate(exportTemplate);
        for (ExportRule rule : otherRules) {
            if (rule.getExportRuleFormat() != null) {
                return rule.getExportRuleFormat();
            }
        }
        return null;
    }

    /**
     * Groups all localized question texts for this {@link Question} by country. The country code is
     * the key in the outer map. The inner map contains the language code as key and the localized
     * question text as value.
     *
     * @return A map with localized question texts grouped by country
     */
    public SortedMap<String, Map<String, String>> getLocalizedQuestionTextGroupedByCountry() {
        SortedMap<String, Map<String, String>> groupedLocalizedQuestionTextByCountry = new TreeMap<>();
        // Loop through each localized question text
        for (Map.Entry<String, String> entry : this.getLocalizedQuestionText().entrySet()) {
            // Get the locale code
            String localeCode = entry.getKey();
            // Set the country to the locale code by default
            String country = localeCode.toUpperCase();
            // Set the question text to the localized question text by default
            String questionText = entry.getValue();
            // If the locale contains country and language code seperated by '_'
            // split this locale code and get the country from the second part.
            // (i.e. de_DE --> country is DE). The first part of the split
            // result is the language code.
            if (localeCode.contains("_")) {
                String[] parts = localeCode.split("_");
                country = parts[1];
                questionText = entry.getValue();
            }

            // If the sorted map already contains the country, add this
            // localized
            // question text with its related language code
            if (groupedLocalizedQuestionTextByCountry.containsKey(country)) {
                groupedLocalizedQuestionTextByCountry.get(country).put(localeCode, questionText);
                // Otherwise this is the first question text for this country
                // and a new
                // map for the question texts has to be setup and filled with
                // the first
                // question text and its related language code
            } else {
                Map<String, String> localeQuestionTextMap = new HashMap<>();
                localeQuestionTextMap.put(localeCode, questionText);
                groupedLocalizedQuestionTextByCountry.put(country, localeQuestionTextMap);
            }
        }
        return groupedLocalizedQuestionTextByCountry;
    }

    /**
     * Returns the ID of the {@link Answer} that is marked as other.
     *
     * @return The ID of the {@link Answer} that is marked as other, null if this answer does not
     * exist.
     */
    public Long getIsOtherAnswerId() {
        for (Answer answer : this.getAnswers()) {
            if (answer instanceof SelectAnswer && ((SelectAnswer) answer).getIsOther()) {
                return answer.getId();
            }
        }
        return null;
    }

    /**
     * See {@link ExportTemplate#getExportRules()} for a description for export rules
     * <p>
     * Returns all {@link ExportRuleQuestion} objects of this question object and therefore contains
     * all export rules ever given to this question
     *
     * @return A set of {@link ExportRuleQuestion} objects. Can be empty, but is never
     * <code>null</code>. Is unmodifiable.
     */
    public Set<ExportRuleQuestion> getExportRules() {
        return Collections.unmodifiableSet(exportRules);
    }

    /**
     * Adds all given {@link ExportRuleQuestion} objects that are not already associated with this
     * question to the corresponding set of ExportRules. Takes care that the export rules refer to
     * this question, too.
     *
     * @param exportRules The set of additional {@link ExportRuleQuestion} objects for this question
     *                    object. Must not be
     *                    <code>null</code>.
     * @throws AssertionError} If the given parameter is invalid.
     */
    public void addExportRules(final Set<ExportRuleQuestion> exportRules) {
        assert exportRules != null : "The given export rules were null";
        for (ExportRuleQuestion exportRule : exportRules) {
            addExportRule(exportRule);
        }
    }

    /**
     * See {@link Question#getExportRules()} for a description for exportRule.
     * <p>
     * Takes care that the {@link ExportRuleQuestion} refers to this question as well.
     *
     * @param exportRule An {@link ExportRule} object. Must not be
     *                   <code>null</code>.
     * @throws AssertionError If the given parameter is invalid.
     */
    public void addExportRule(final ExportRuleQuestion exportRule) {
        assert exportRule != null : "The given ExportRule was null";
        if (!exportRules.contains(exportRule)) {
            exportRules.add(exportRule);
        }
        // take care the objects know each other
        if (exportRule.getQuestion() == null || !exportRule.getQuestion().equals(this)) {
            exportRule.setQuestion(this);
        }
    }

    /**
     * See {@link Question#getExportRules()} for a description for exportRule
     * <p>
     * Takes care that the {@link ExportRuleQuestion} no more refers to this question as well.
     *
     * @param exportRule Must not be <code>null</code>.
     * @throws AssertionError If the given parameter is invalid.
     */
    public void removeExportRule(final ExportRuleQuestion exportRule) {
        assert exportRule != null : "The given ExportRule was null";
        // If the ExportRule is in the set, remove it and remove this
        // Question from the ExportRule
        if (exportRules.remove(exportRule)) {
            exportRule.removeQuestion();
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
        if (!(obj instanceof Question)) {
            return false;
        }
        Question other = (Question) obj;
        return getUUID().equals(other.getUUID());
    }

    @Override
    public int compareTo(final Question o) {
        return getPosition().compareTo(o.getPosition());
    }

    /**
     * Checks whether this question is modifiable or not. If this question has already been answered
     * during a survey the question is not modifiable anymore.
     *
     * @return true if this question is modifiable, otherwise false.
     */
    public Boolean isModifiable() {
        for (Answer answer : this.answers) {
            if (!answer.getResponses().isEmpty()) {
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

    /**
     * Returns if the Question is deletable. It is not deletable if there is already an response to
     * a corresponding answer.
     *
     * @return If the Question is deletable.
     */
    @JsonIgnore
    public boolean isDeletable() {
        for (Answer answer : this.getAnswers()) {
            if (!answer.getResponses().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Provides a new {@link Question} instance with a new UUID and no id, but the same attributes
     * as <code>this</code>. The references {@link Questionnaire} is referenced in the new instance,
     * too, but not cloned. The underlying {@link Answer} instances are cloned, too, using their
     * {@link Answer#cloneWithoutReferences()} method.
     *
     * @return a new {@link Question} instance with the same attributes as
     * <code>this</code>, but a different <code>UUID</code>, a non-valid id and
     * (some missing) references as described above. Is never <code>null</code>.
     */
    public Question cloneWithAnswersAndReferenceToQuestionnaire() {
        Question newQuestion = new Question(new HashMap<>(getLocalizedQuestionText()),
            getIsRequired(), getIsEnabled(), getQuestionType(),
            (getQuestionnaire().getQuestions().size() + 1), getQuestionnaire());
        newQuestion.setMinMaxNumberAnswers(getMinNumberAnswers(), getMaxNumberAnswers());
        for (Answer answer : getAnswers()) {
            Answer newAnswer = answer.cloneWithoutReferences();
            newQuestion.addAnswer(newAnswer);
        }
        return newQuestion;
    }

    public Question cloneWithAnswersAndReferenceToQuestionnaire(Questionnaire questionnaire) {
        Question newQuestion = new Question(new HashMap<>(getLocalizedQuestionText()),
                getIsRequired(), getIsEnabled(), getQuestionType(), getPosition(), questionnaire); // Set questionnaire to null initially
        newQuestion.setMinMaxNumberAnswers(getMinNumberAnswers(), getMaxNumberAnswers());
        for (Answer answer : getAnswers()) {
            Answer newAnswer = answer.cloneWithoutReferences();
            newQuestion.addAnswer(newAnswer);
        }
        return newQuestion;
    }
}
