package de.imi.mopat.model.score;

import de.imi.mopat.model.Encounter;

import java.util.Iterator;
import java.util.List;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * This {@link Operator} returns eihter the value of the first {@link Expression} plus the value of
 * the second {@link Expression} (evaluate) or the formula of the first {@link Expression} '+' the
 * formular of the second {@link Expression} (getFormula).
 */
@Entity
@DiscriminatorValue("Plus")
public class Plus extends BinaryOperatorNumeric {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(
        ValueOfQuestionOperator.class);

    @Override
    public Double evaluate(final Expression expression, final Encounter encounter) {
        if (expression instanceof BinaryExpression) {
            BinaryExpression binaryExpression = (BinaryExpression) expression;
            List<Expression> plusExpressions = binaryExpression.getExpressions();
            if (plusExpressions.size() != 2) {
                LOGGER.error("The number of expressions for a plus operator " + "must be 2.");
                return null;
            }

            Iterator<Expression> iterator = plusExpressions.iterator();
            Double firstExpression = (Double) iterator.next().evaluate(encounter);
            Double secondExpression = (Double) iterator.next().evaluate(encounter);
            if (firstExpression != null && secondExpression != null) {
                return firstExpression + secondExpression;
            } else {
                return null;
            }
        }
        LOGGER.error("Wrong type of Expression. Must be a binary expression.");
        return null;
    }

    @Override
    public String getFormula(final Expression expression, final Encounter encounter,
        final String defaultLanguage) {
        if (expression instanceof BinaryExpression) {
            BinaryExpression binaryExpression = (BinaryExpression) expression;
            List<Expression> plusExpressions = binaryExpression.getExpressions();
            if (plusExpressions.size() != 2) {
                LOGGER.error("The number of expressions for a plus operator " + "must be 2.");
                return null;
            }

            Iterator<Expression> iterator = plusExpressions.iterator();

            return "(" + iterator.next().getFormula(encounter, defaultLanguage) + " + "
                + iterator.next().getFormula(encounter, defaultLanguage) + ")";
        } else {
            LOGGER.error("Wrong type of Expression. Must be a binary " + "expression.");
            return null;
        }
    }
}
