package de.imi.mopat.model.score;

import de.imi.mopat.model.Encounter;

import java.util.Iterator;
import java.util.List;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * This {@link Operator} returns either the value of the first {@link Expression} divided by the
 * value of the second {@link Expression} (evaluate) or the formula of the first {@link Expression}
 * '/' the formula of the second {@link Expression} (getFormula).
 */
@Entity
@DiscriminatorValue("Divide")
public class Divide extends BinaryOperatorNumeric {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(Divide.class);

    @Override
    public Double evaluate(final Expression expression, final Encounter encounter) {
        if (expression instanceof BinaryExpression) {
            BinaryExpression binaryExpression = (BinaryExpression) expression;
            List<Expression> divideExpressions = binaryExpression.getExpressions();
            if (divideExpressions.size() != 2) {
                LOGGER.error("The number of expressions for a divide operator" + " must be 2.");
                return null;
            }

            Iterator<Expression> iterator = divideExpressions.iterator();
            Double firstExpression = (Double) iterator.next().evaluate(encounter);
            Double secondExpression = (Double) iterator.next().evaluate(encounter);
            if (firstExpression != null && secondExpression != null) {
                return (firstExpression / secondExpression);
            } else {
                return null;
            }
        } else {
            LOGGER.error("Wrong type of Expression. Must be a binary " + "expression.");
            return null;
        }
    }

    @Override
    public String getFormula(final Expression expression, final Encounter encounter,
        final String defaultLanguage) {
        if (expression instanceof BinaryExpression) {
            BinaryExpression binaryExpression = (BinaryExpression) expression;
            List<Expression> divideExpressions = binaryExpression.getExpressions();
            if (divideExpressions.size() != 2) {
                LOGGER.error("The number of expressions for a divide operator" + " must be 2.");
                return null;
            }
            Iterator<Expression> iterator = divideExpressions.iterator();
            return "(" + iterator.next().getFormula(encounter, defaultLanguage) + " / "
                + iterator.next().getFormula(encounter, defaultLanguage) + ")";
        } else {
            LOGGER.error("Wrong type of Expression. Must be a binary " + "expression.");
            return null;
        }

    }
}
