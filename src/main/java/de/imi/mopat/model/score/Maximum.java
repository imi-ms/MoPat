package de.imi.mopat.model.score;

import de.imi.mopat.model.Encounter;

import java.util.Iterator;
import java.util.List;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * This class returns the maximum of all {@link Expression Expressions} values that are returned
 * with evaluate function or returns the formula of those {@link Expression Expressions} by
 * collecting its formulas.
 */
@Entity
@DiscriminatorValue("Maximum")
public class Maximum extends MultiOperator {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(
        ValueOfQuestionOperator.class);

    @Override
    public Object evaluate(final Expression expression, final Encounter encounter) {
        if (expression instanceof MultiExpression) {
            MultiExpression multiExpression = (MultiExpression) expression;
            List<Expression> expressions = multiExpression.getExpressions();

            if (expressions == null || expressions.isEmpty()) {
                LOGGER.error("This maximum operator contains no expressions.");
                return null;
            }

            Iterator<Expression> iterator = expressions.iterator();
            Expression maximumExpression = iterator.next();
            Double maximumValue = (Double) maximumExpression.evaluate(encounter);

            while (iterator.hasNext()) {
                Expression currentExpression = iterator.next();
                Double value = (Double) currentExpression.evaluate(encounter);

                // Ignore NULL values evaluating the maximum
                if (maximumValue != null) {
                    if (value != null && value > maximumValue) {
                        maximumValue = value;
                    }
                } else {
                    maximumValue = value;
                }
            }
            return maximumValue;
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
            formula.append("Maximum of(");
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
