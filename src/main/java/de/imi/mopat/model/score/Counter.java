package de.imi.mopat.model.score;

import de.imi.mopat.model.Encounter;

import java.util.Iterator;
import java.util.List;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * This operator returns either the sum of all {@link Expression Expressions} when they are true for
 * the {@link Operator} (evaluate) or the formula of all {@link Expression Expressions} for the
 * {@link Operator} (getFormula) seperated by comma.
 */
@Entity
@DiscriminatorValue("Counter")
public class Counter extends MultiOperator {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(
        ValueOfQuestionOperator.class);

    @Override
    public Double evaluate(final Expression expression, final Encounter encounter) {
        if (expression instanceof MultiExpression) {
            Double counter = 0.0;
            MultiExpression multiExpression = (MultiExpression) expression;
            List<Expression> counterExpressions = multiExpression.getExpressions();

            if (counterExpressions == null || counterExpressions.isEmpty()) {
                LOGGER.error("This counter operator contains no expressions.");
                return null;
            }

            Iterator<Expression> iterator = counterExpressions.iterator();

            while (iterator.hasNext()) {
                Boolean counterExpressionResult = (Boolean) iterator.next().evaluate(encounter);
                if (counterExpressionResult == null) {
                    return null;
                }
                if (counterExpressionResult) {
                    counter += 1;
                }
            }
            return counter;
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
            List<Expression> counterExpressions = multiExpression.getExpressions();
            if (counterExpressions == null || counterExpressions.isEmpty()) {
                LOGGER.error("This counter operator contains no expressions.");
                return null;
            }

            StringBuilder counterBuffer = new StringBuilder();
            counterBuffer.append("Count of(");
            Iterator<Expression> iterator = counterExpressions.iterator();
            while (iterator.hasNext()) {
                counterBuffer.append(iterator.next().getFormula(encounter, defaultLanguage));
                if (iterator.hasNext()) {
                    counterBuffer.append(", ");
                }
            }

            counterBuffer.append(")");
            return counterBuffer.toString();
        } else {
            LOGGER.error("Wrong type of Expression. Must be a multi " + "expression.");
            return null;
        }
    }
}
