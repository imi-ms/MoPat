package de.imi.mopat.model.score;

import de.imi.mopat.model.Encounter;

import java.util.Iterator;
import java.util.List;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * This class returns the minimum of all {@link Expression Expressions} values that are returned by
 * its evaluate function or returns the formula of those {@link Expression Expressions} by
 * collecting its formulas.
 */
@Entity
@DiscriminatorValue("Minimum")
public class Minimum extends MultiOperator {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(
        ValueOfQuestionOperator.class);

    @Override
    public Object evaluate(final Expression expression, final Encounter encounter) {
        if (expression instanceof MultiExpression) {
            MultiExpression multiExpression = (MultiExpression) expression;
            List<Expression> expressions = multiExpression.getExpressions();

            if (expressions == null || expressions.isEmpty()) {
                LOGGER.error("This minimum operator contains no expressions.");
                return null;
            }

            Iterator<Expression> iterator = expressions.iterator();
            Expression minimumExpression = iterator.next();
            Double minimumValue = (Double) minimumExpression.evaluate(encounter);

            while (iterator.hasNext()) {
                Expression currentExpression = iterator.next();
                Double value = (Double) currentExpression.evaluate(encounter);

                // Ignore NULL values evaluating the minimum
                if (minimumValue != null) {
                    if (value != null && value < minimumValue) {
                        minimumValue = value;
                    }
                } else {
                    minimumValue = value;
                }
            }
            return minimumValue;
        } else {
            LOGGER.error("Wrong type of expression. Must be a Multi " + "Expression");
            return null;
        }
    }

    @Override
    public String getFormula(final Expression expression, final Encounter encounter,
        final String defaultLanguage) {
        if (expression instanceof MultiExpression) {
            MultiExpression multiExpression = (MultiExpression) expression;
            List<Expression> expressions = multiExpression.getExpressions();

            if (expressions == null || expressions.isEmpty()) {
                LOGGER.error("This maximum operator contains no expressions.");
                return null;
            }

            Iterator<Expression> iterator = expressions.iterator();
            Expression currentExpression = iterator.next();

            StringBuilder formula = new StringBuilder();
            formula.append("Minimum of(");
            formula.append(currentExpression.getFormula(encounter, defaultLanguage));

            while (iterator.hasNext()) {
                currentExpression = iterator.next();
                formula.append(", ");
                formula.append(currentExpression.getFormula(encounter, defaultLanguage));
            }
            formula.append(")");

            return formula.toString();
        } else {
            LOGGER.error("Wrong type of expression. Must be a Multi " + "Expression");
            return null;
        }
    }
}
