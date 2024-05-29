package de.imi.mopat.model.score;

import de.imi.mopat.model.Encounter;

import java.util.Iterator;
import java.util.List;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * This operator returns either the sum of all {@link Expression Expressions} for this
 * {@link Operator} (evaluate) or the formula of all {@link Expression Expressions} for this
 * {@link Operator} seperated by comma.
 */
@Entity
@DiscriminatorValue("Sum")
public class Sum extends MultiOperator {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(
        ValueOfQuestionOperator.class);

    @Override
    public Double evaluate(final Expression expression, final Encounter encounter) {
        if (expression instanceof MultiExpression) {
            Double sum = 0.0;
            MultiExpression multiExpression = (MultiExpression) expression;
            List<Expression> sumExpressions = multiExpression.getExpressions();
            if (sumExpressions == null || sumExpressions.isEmpty()) {
                LOGGER.error("This sum operator contains no expressions.");
                return null;
            }

            Iterator<Expression> iterator = sumExpressions.iterator();
            while (iterator.hasNext()) {
                Double sumExpressionResult = (Double) iterator.next().evaluate(encounter);
                if (sumExpressionResult == null) {
                    return null;
                }

                sum += sumExpressionResult;
            }
            return sum;
        } else {
            LOGGER.error("Wrong type of Expression. Must be a multi " + "expression.");
        }
        return null;
    }

    @Override
    public String getFormula(final Expression expression, final Encounter encounter,
        final String defaultLanguage) {
        if (expression instanceof MultiExpression) {
            MultiExpression multiExpression = (MultiExpression) expression;
            List<Expression> sumExpressions = multiExpression.getExpressions();
            if (sumExpressions == null || sumExpressions.isEmpty()) {
                LOGGER.error("This sum operator contains no expressions.");
                return null;
            }

            StringBuilder sumBuffer = new StringBuilder();
            sumBuffer.append("Sum(");
            Iterator<Expression> iterator = sumExpressions.iterator();
            while (iterator.hasNext()) {
                sumBuffer.append(iterator.next().getFormula(encounter, defaultLanguage));
                if (iterator.hasNext()) {
                    sumBuffer.append(", ");
                }
            }

            sumBuffer.append(")");
            return sumBuffer.toString();
        } else {
            LOGGER.error("Wrong type of Expression. Must be a multi " + "expression.");
            return null;
        }
    }
}
