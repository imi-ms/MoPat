package de.imi.mopat.model.score;

import de.imi.mopat.model.Encounter;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This operator returns either the average of all {@link Expression Expression}, except the last
 * expression, which indicates the number of allowed missing expressions, for this {@link Operator}
 * (evaluate) or the formula of all {@link Expression Expressions} for this {@link Operator}
 * seperated by comma.
 */
@Entity
@DiscriminatorValue("Average")
public class Average extends MultiOperator {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(Average.class);

    @Override
    public Object evaluate(final Expression expression, final Encounter encounter) {
        Double result = null;
        if (expression instanceof MultiExpression multiExpression) {
            List<Expression> avgExpressions = multiExpression.getExpressions();
            // If there is no expression or only the
            // missing-expressions-value throw an error
            if (avgExpressions == null || avgExpressions.size() <= 1) {
                LOGGER.error("This average operator contains no expressions.");
                return null;
            }

            // Save the number of allowed missing expressions
            Double allowedMissingExpressions = (Double) avgExpressions.get(
                avgExpressions.size() - 1).evaluate(encounter);
            Double missingExpressions = 0.0;
            Double sum = 0.0;
            Double counter = 0.0;
            // Iterate over all Expressions and add up all evaluated results
            Iterator<Expression> iterator = avgExpressions.iterator();
            // Set iterator.next() here, so that the last expression (number
            // of missing values) will not be noted
            Double sumExpressionResult = (Double) iterator.next().evaluate(encounter);
            while (iterator.hasNext()) {
                // Check if there is a missing expression
                if (sumExpressionResult == null) {
                    missingExpressions++;
                    // If there are more missing expressions than allowed
                    // return null
                    if (missingExpressions > allowedMissingExpressions) {
                        return null;
                    }
                } else {
                    counter++;
                    sum += sumExpressionResult;
                }
                sumExpressionResult = (Double) iterator.next().evaluate(encounter);
            }
            if (counter == 0) {
                result = 0.0;
            } else {
                result = sum / counter;
            }
        } else {
            LOGGER.error("Wrong type of Expression. Must be a multi " + "expression.");
        }
        return result;
    }

    @Override
    public String getFormula(final Expression expression, final Encounter encounter,
        final String defaultLanguage) {
        if (expression instanceof MultiExpression multiExpression) {
            List<Expression> avgExpressions = multiExpression.getExpressions();
            // If there is no expression or only the
            // missing-expressions-value throw an error
            if (avgExpressions == null || avgExpressions.size() <= 1) {
                LOGGER.error("This average operator contains no expressions.");
                return null;
            }

            List<Expression> usedExpressions = new ArrayList<>();
            List<Expression> missingExpressions = new ArrayList<>();
            // Get a list of all used expressions and all missing expressions
            Iterator<Expression> iterator = avgExpressions.iterator();
            // Set iterator.next() here, so that the last expression (number
            // of missing values) will not be noted
            Expression tempExpression = iterator.next();
            while (iterator.hasNext()) {
                Double sumExpressionResult = (Double) tempExpression.evaluate(encounter);
                if (sumExpressionResult == null) {
                    missingExpressions.add(tempExpression);
                } else {
                    usedExpressions.add(tempExpression);
                }
                tempExpression = iterator.next();
            }

            // Create the return string
            StringBuilder avgBuffer = new StringBuilder();
            if (usedExpressions.size() > 0) {
                avgBuffer.append("Average(");
                // Add all used expressions to the string
                for (Expression usedExpression : usedExpressions) {
                    avgBuffer.append(usedExpression.getFormula(encounter, defaultLanguage));
                    avgBuffer.append(", ");
                }
                // Delete the last two characters ", " and replace them with
                // a ")"
                avgBuffer.replace(avgBuffer.length() - 2, avgBuffer.length() - 1, ")");
            }
            if (missingExpressions.size() > 0) {
                // If there are used expressions before the missing
                // Expressions add a delimiter
                if (usedExpressions.size() > 0) {
                    avgBuffer.append(", ");
                }
                avgBuffer.append("Missing Values(");
                // Add all missing expressions to the string
                for (Expression missingExpression : missingExpressions) {
                    avgBuffer.append(missingExpression.getFormula(encounter, defaultLanguage));
                    avgBuffer.append(", ");
                }
                // Delete the last two characters ", " and replace them with
                // a ")"
                avgBuffer.replace(avgBuffer.length() - 2, avgBuffer.length() - 1, ")");
            }
            return avgBuffer.toString();
        } else {
            LOGGER.error("Wrong type of Expression. Must be a multi " + "expression.");
            return null;
        }
    }
}
