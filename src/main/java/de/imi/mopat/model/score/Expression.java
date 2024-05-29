package de.imi.mopat.model.score;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.imi.mopat.helper.model.UUIDGenerator;
import de.imi.mopat.model.Encounter;
import de.imi.mopat.model.dto.ExpressionDTO;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * The database table model for table <i>expression</i>. Every expression must support the evaluate
 * and the getFormula method. It also must have an {@link Operator}.
 */
@Entity
@Table(name = "expression")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "expression_type", discriminatorType = DiscriminatorType.STRING)
public abstract class Expression implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;
    @JsonIgnore
    @Column(name = "uuid")
    private String uuid = UUIDGenerator.createUUID();
    @ManyToOne(cascade = CascadeType.ALL)
    private Expression parent;

    /**
     * Returns the {@link ExpressionDTO ExpressionDTO} representation of this Expression.
     *
     * @return A new {@link ExpressionDTO ExpressionDTO}.
     */
    public abstract ExpressionDTO toExpressionDTO();

    /**
     * Returns the {@link Operator} of this object.
     *
     * @return The {@link Operator} of this object
     */
    public abstract Operator getOperator();

    /**
     * Returns the id of the current expression object.
     *
     * @return id The current id of this expression object. Might be
     * <code>null</code> for newly created objects. Is never
     * <code> &lt;= 0</code>.
     */
    public Long getId() {
        return id;
    }

    /**
     * Returns the uuid of the current expression object.
     *
     * @return uuid The uuid of this expression object. Can not be
     * <code>null</code>.
     */
    private String getUUID() {
        return this.uuid;
    }

    /**
     * Returns the parent expression of the current expression object.
     *
     * @return parent The parent expression of this expression object.
     */
    public Expression getParent() {
        return parent;
    }

    /**
     * Sets the parent expression of the current expression object.
     *
     * @param parent The parent expression of this expression object.
     */
    public void setParent(final Expression parent) {
        this.parent = parent;
    }

    /**
     * Evaluate this expression for the given {@link Encounter}.
     *
     * @param encounter The {@link Encounter} for which this {@link Expression} should be
     *                  evaluated.
     * @return The value of the calculation. Null if the calculation fails.
     */
    public abstract Object evaluate(Encounter encounter);

    /**
     * Get the formula of this expression for the given {@link Encounter}.
     *
     * @param encounter       The {@link Encounter} for which a formula with this {@link Expression}
     *                        should be created.
     * @param defaultLanguage The language in which the formula should be returned.
     * @return The formula of this {@link Expression}.
     */
    public abstract String getFormula(Encounter encounter, String defaultLanguage);

    /**
     * Checks if this {@link Expression} contains the given {@link Score}
     *
     * @param score {@link Score} to check
     * @return true, if this {@link Expression} contains the given {@link Score}, else false
     */
    public boolean includesScore(final Score score) {
        Expression expression = this;
        if (expression instanceof UnaryExpression) {
            if (((UnaryExpression) expression).getScore() == score) {
                return true;
            }
            // Else if this Expression is a BinaryExpression check if any of
            // its Expressions contains this Score
        } else if (expression instanceof BinaryExpression) {
            List<Expression> expressionList = new ArrayList<>(
                ((BinaryExpression) expression).getExpressions());
            for (Expression expressionFromBinaryExpression : expressionList) {
                if (expressionFromBinaryExpression.includesScore(score)) {
                    return true;
                }
            }
            // Else if this Expression is a MultiExpression check if any of
            // its Expressions contains this Score
        } else if (expression instanceof MultiExpression) {
            List<Expression> expressionList = new ArrayList<>(
                ((MultiExpression) expression).getExpressions());
            for (Expression expressionFromMultiExpression : expressionList) {
                if (expressionFromMultiExpression.includesScore(score)) {
                    return true;
                }
            }
        }
        return false;
    }
}
