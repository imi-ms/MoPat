package de.imi.mopat.model.score;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.imi.mopat.helper.controller.StringUtilities;
import de.imi.mopat.helper.model.UUIDGenerator;
import de.imi.mopat.model.Encounter;
import de.imi.mopat.model.ExportRuleScore;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.dto.ScoreDTO;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * The database table model for table <i>score</i>.
 */
@Entity
@Table(name = "score")
public class Score implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;
    @JsonIgnore
    @Column(name = "uuid")
    private String uuid = UUIDGenerator.createUUID();
    @Column(name = "name")
    private String name;
    @JoinColumn(name = "expression", referencedColumnName = "id")
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    Expression expression;
    @ManyToOne
    @JoinColumn(name = "questionnaire_id", referencedColumnName = "id")
    private Questionnaire questionnaire;
    @OneToMany(mappedBy = "score", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ExportRuleScore> exportRules = new HashSet<>();

    /**
     * Get the formula of this {@link Score} for the given {@link Encounter}.
     *
     * @param encounter       The {@link Encounter} for which a formula with this {@link Expression}
     *                        should be created.
     * @param defaultLanguage The language in which the formula should be returned.
     * @return The formula of this {@link Score}.
     */
    public String getFormula(final Encounter encounter, final String defaultLanguage) {
        StringBuffer formula = new StringBuffer();

        formula.append(this.getExpression().getFormula(encounter, defaultLanguage));
        return StringUtilities.stripHTML(formula.toString());
    }

    /**
     * Evaluate this {@link Score}.
     *
     * @param encounter The {@link Encounter} for which this {@link Score} should be evaluated.
     * @return The value of the calculation. Null if the calculation fails.
     */
    public Object evaluate(final Encounter encounter) {
        return this.getExpression().evaluate(encounter);
    }

    /**
     * Returns the name of the current {@link Score}.
     *
     * @return The name of the current {@link Score}.
     */
    public String getName() {
        return name;
    }

    /**
     * See {@link Score#getName()} for a description.
     * <p>
     * Sets a new name for this {@link Score}.
     *
     * @param name The new name for this {@link Score}. Must not be
     *             <code>null</code>.
     */
    public void setName(final String name) {
        if (name != null) {
            this.name = name;
        }
    }

    /**
     * Returns the {@link Expression} of the current {@link Score}.
     *
     * @return The {@link Expression} of the current {@link Score}.
     */
    public Expression getExpression() {
        return expression;
    }

    /**
     * See {@link Score#getExpression()} for a description.
     * <p>
     * Sets a new {Expression} for this {score}.
     *
     * @param expression The new {Expression} for this {Score}.
     */
    public void setExpression(final Expression expression) {
        this.expression = expression;
    }

    /**
     * Returns the id of the current {@link Score}.
     *
     * @return id The current id of this {@link Score}. Might be
     * <code>null</code> for newly created objects. Is never
     * <code> &lt;= 0</code>.
     */
    public Long getId() {
        return id;
    }

    /**
     * Returns the uuid of the current {@link Score}.
     *
     * @return uuid The uuid of this {@link Score}. Can not be
     * <code>null</code>.
     */
    private String getUUID() {
        return this.uuid;
    }

    /**
     * Returns the {@link Questionnaire} of the current {@link Score}.
     *
     * @return The {@link Questionnaire} of the current {@link Score}.
     */
    public Questionnaire getQuestionnaire() {
        return questionnaire;
    }

    /**
     * See {@link Score#getQuestionnaire()} for a description.
     * <p>
     * Sets a new {@link Questionnaire} for this {@link Score}.
     *
     * @param questionnaire The new {@link Questionnaire} for this {@link Score}.
     */
    public void setQuestionnaire(final Questionnaire questionnaire) {
        this.questionnaire = questionnaire;
        // Take care that the objects know each other
        if (!questionnaire.getScores().contains(this)) {
            questionnaire.addScore(this);
        }
    }

    /**
     * See {@link de.imi.mopat.model.ExportTemplate#getExportRules()} for a description for export
     * rules
     * <p>
     * Returns all {@link ExportRuleScore} objects of this {@link Score} and therefore contains all
     * {@link ExportRuleScore ExportRules} ever given to this {@link Score}.
     *
     * @return A set of {@link ExportRuleScore} objects. Can be empty, but is never
     * <code>null</code>. Is unmodifiable.
     */
    public Set<ExportRuleScore> getExportRules() {
        return Collections.unmodifiableSet(exportRules);
    }

    /**
     * Adds all given {@link ExportRuleScore} objects that are not already associated with this
     * {@link Score} to the corresponding set of {@link ExportRuleScore ExportRules}. Takes care
     * that the {@link ExportRuleScore ExportRules} refer to this {@link Score}, too.
     *
     * @param exportRules The set of additional {@link ExportRuleScore} objects for this
     *                    {@link Score}. Must not be <code>null</code>.
     * @throws AssertionError} If the given parameter is invalid.
     */
    public void addExportRules(final Set<ExportRuleScore> exportRules) {
        assert exportRules != null : "The given export rules were null";
        for (ExportRuleScore exportRule : exportRules) {
            addExportRule(exportRule);
        }
    }

    /**
     * See {@link Score#getExportRules()} for a description for exportRule.
     * <p>
     * Takes care that the {@link ExportRuleScore} refers to this {@link Score} as well.
     *
     * @param exportRule An {@link ExportRuleScore} object. Must not be
     *                   <code>null</code>.
     * @throws AssertionError If the given parameter is invalid.
     */
    public void addExportRule(final ExportRuleScore exportRule) {
        assert exportRule != null : "The given ExportRule was null";
        if (!exportRules.contains(exportRule)) {
            exportRules.add(exportRule);
        }
        // Take care the objects know each other
        if (exportRule.getScore() == null || !exportRule.getScore().equals(this)) {
            exportRule.setScore(this);
        }
    }

    /**
     * See {@link Score#getExportRules()} for a description for exportRule
     * <p>
     * Takes care that the {@link ExportRuleScore} no more refers to this {@link Score} as well.
     *
     * @param exportRule Must not be <code>null</code>.
     * @throws AssertionError If the given parameter is invalid.
     */
    public void removeExportRule(final ExportRuleScore exportRule) {
        assert exportRule != null : "The given ExportRule was null";
        exportRules.remove(exportRule);
        if (exportRule.getScore() != null && exportRule.getScore().equals(this)) {
            exportRule.removeScore();
        }
    }

    /**
     * Returns true if the {@link Operator} of the {@link Expression} is an
     * {@link BinaryOperatorBoolean} else returns false
     *
     * @return true if the {@link Operator} of the {@link Expression} is an
     * {@link BinaryOperatorBoolean} else false
     */
    public boolean isBooleanScore() {
        return this.getExpression().getOperator() instanceof BinaryOperatorBoolean;
    }

    /**
     * Get all depending {@link Score Scores} to this {@link Score}. That are all
     * {@link Score Scores} that use this {@link Score} or that use a {@link Score} that use this
     * {@link Score} and so on.
     *
     * @return A {@link List} of all depending {@link Score Scores}
     */
    public List<Score> getDependingScores() {
        // A set for all depending Scores that is all Scores that use this
        // Score or that use a Score that use this Score and so on
        Set<Score> dependingScores = new HashSet<>();
        // Add the given Score
        dependingScores.add(this);
        // Get all Scores depending to this Score
        Set<Score> usedInScores = new HashSet<>();
        usedInScores.add(this);
        // A temporary list
        Set<Score> usedInScoresTemp;
        do {
            // Add all used Scores of this Iteration to the set of depending
            // Scores
            dependingScores.addAll(usedInScores);
            usedInScoresTemp = new HashSet<>();
            // Check if the used Scores are also used in other Scores and add
            // them to the list
            for (Score usedScore : usedInScores) {
                // Get all Scores of the Questionnaire
                List<Score> possibleScores = new ArrayList<>(
                    usedScore.getQuestionnaire().getScores());
                for (Score possibleScore : possibleScores) {
                    Expression expression = possibleScore.getExpression();
                    // If this Expression includes this Score add it to the
                    // temporary list
                    if (expression instanceof UnaryExpression) {
                        if (((UnaryExpression) expression).getScore() == usedScore) {
                            usedInScoresTemp.add(possibleScore);
                        }
                        // Else if this Expression is a BinaryExpression
                        // check if any of its Expressions contains this
                        // Score
                    } else if (expression instanceof BinaryExpression) {
                        List<Expression> expressionList = new ArrayList<>(
                            ((BinaryExpression) expression).getExpressions());
                        for (Expression expressionFromBinaryExpression : expressionList) {
                            if (expressionFromBinaryExpression.includesScore(usedScore)) {
                                usedInScoresTemp.add(possibleScore);
                                break;
                            }
                        }
                        // Else if this Expression is a MultiExpression check
                        // if any of its Expressions contains this Score
                    } else if (expression instanceof MultiExpression) {
                        List<Expression> expressionList = new ArrayList<>(
                            ((MultiExpression) expression).getExpressions());
                        for (Expression expressionFromMultiExpression : expressionList) {
                            if (expressionFromMultiExpression.includesScore(usedScore)) {
                                usedInScoresTemp.add(possibleScore);
                                break;
                            }
                        }
                    }
                }
            }
            // Renew the list of the for loop
            usedInScores = usedInScoresTemp;
            // Do this while new Scores come to the set of depending Scores
        } while (!dependingScores.containsAll(usedInScoresTemp));
        dependingScores.remove(this);
        return new ArrayList<>(dependingScores);
    }

    /*
     * Converts this {@link Score} object to a {@link ScoreDTO} object.
     *
     * @return A {@link ScoreDTO} object based on this {@link Score}
     * object.
     */
    @JsonIgnore
    public ScoreDTO toScoreDTO() {
        ScoreDTO scoreDTO = new ScoreDTO();
        scoreDTO.setId(this.getId());
        scoreDTO.setName(this.getName());
        scoreDTO.setQuestionnaireId(this.getQuestionnaire().getId());
        scoreDTO.setExpression(this.getExpression().toExpressionDTO());
        scoreDTO.setHasExportRules(!this.getExportRules().isEmpty());

        List<String> dependingScoreNames = new ArrayList<>();
        for (Score score : this.getDependingScores()) {
            dependingScoreNames.add(score.getName());
        }
        scoreDTO.setDependingScoreNames(dependingScoreNames);

        return scoreDTO;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Score)) {
            return false;
        }
        Score other = (Score) obj;
        return getUUID().equals(other.getUUID());
    }

    @Override
    public int hashCode() {
        return getUUID().hashCode();
    }
}
