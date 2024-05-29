package de.imi.mopat.model.score;

import de.imi.mopat.model.Encounter;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.dto.ExpressionDTO;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/**
 * This {@link Expression} contains a unary operator and will not have children.
 */
@Entity
@DiscriminatorValue("UNARY")
public class UnaryExpression extends Expression {

    @ManyToOne
    @JoinColumn(name = "question", referencedColumnName = "id")
    private Question question;
    @Column(name = "value")
    private Double value;
    @JoinColumn(name = "score", referencedColumnName = "id")
    @ManyToOne
    private Score score;
    @JoinColumn(name = "operator", referencedColumnName = "id")
    @ManyToOne
    private UnaryOperator unaryOperator;

    public UnaryExpression() {
    }

    /**
     * Returns the value of the current {@link Expression}.
     *
     * @return The value of the current {@link Expression}.
     */
    public Double getValue() {
        return value;
    }

    /**
     * See {@link UnaryExpression#getValue()} for a description.
     * <p>
     * Sets a new value for this {@link Expression}.
     *
     * @param value The new value for this {@link Expression}.
     */
    public void setValue(final Double value) {
        this.value = value;
    }

    /**
     * Returns the {@link Question} of the current {@link Expression}.
     *
     * @return The {@link Question} of the current {@link Expression}.
     */
    public Question getQuestion() {
        return question;
    }

    /**
     * See {@link UnaryExpression#getQuestion()} for a description.
     * <p>
     * Sets a new {@link Question} for this {@link Expression}.
     *
     * @param question The new {@link Question} for this {@link Expression}.
     */
    public void setQuestion(final Question question) {
        this.question = question;
    }

    /**
     * Returns the {@link Score} of the current {@link Expression}.
     *
     * @return The {@link Score} of the current {@link Expression}.
     */
    public Score getScore() {
        return score;
    }

    /**
     * See {@link UnaryExpression#getScore()} for a description.
     * <p>
     * Sets a new {@link Score} for this {@link Expression}.
     *
     * @param score The new {@link Score} for this {@link Expression}.
     */
    public void setScore(final Score score) {
        this.score = score;
    }

    @Override
    public Operator getOperator() {
        return unaryOperator;
    }

    /**
     * Set a {@link UnaryOperator UnaryOperator} to this {@link Expression}.
     *
     * @param unaryOperator The {@link UnaryOperator UnaryOperator} which should be set.
     */
    public void setOperator(final UnaryOperator unaryOperator) {
        this.unaryOperator = unaryOperator;
    }

    @Override
    public Object evaluate(final Encounter encounter) {
        return this.unaryOperator.evaluate(this, encounter);
    }

    @Override
    public String getFormula(final Encounter encounter, final String defaultLanguage) {
        return this.unaryOperator.getFormula(this, encounter, defaultLanguage);
    }

    @Override
    public ExpressionDTO toExpressionDTO() {
        ExpressionDTO expressionDTO = new ExpressionDTO();
        expressionDTO.setOperatorId(this.getOperator().getId());

        if (this.getOperator() instanceof ValueOfQuestionOperator) {
            if (this.getQuestion() != null) {
                expressionDTO.setQuestionId(this.getQuestion().getId());
            }
        } else if (this.getOperator() instanceof ValueOfScoreOperator) {
            if (this.getScore() != null) {
                expressionDTO.setScoreId(this.getScore().getId());
            }
        } else if (this.getOperator() instanceof ValueOperator) {
            expressionDTO.setValue(this.getValue().toString());
        }
        return expressionDTO;
    }
}
