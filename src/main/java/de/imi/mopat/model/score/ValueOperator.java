package de.imi.mopat.model.score;

import de.imi.mopat.model.Encounter;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * This operator returns either the given value (evaluate) or the value as text (getFormula).
 */
@Entity
@DiscriminatorValue("Value")
public class ValueOperator extends UnaryOperator {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(
        ValueOfQuestionOperator.class);

    @Override
    public Double evaluate(final Expression expression, final Encounter encounter) {
        if (expression instanceof UnaryExpression) {
            UnaryExpression unaryExpression = (UnaryExpression) expression;
            return unaryExpression.getValue();
        } else {
            LOGGER.error("Wrong type of Expression. Must be an unary " + "expression.");
            return null;
        }
    }

    @Override
    public String getFormula(final Expression expression, final Encounter encounter,
        String defaultLanguage) {
        if (expression instanceof UnaryExpression) {
            UnaryExpression unaryExpression = (UnaryExpression) expression;
            if (unaryExpression.getValue() == null) {
                return "null";
            } else {
                return unaryExpression.getValue().toString();
            }
        } else {
            LOGGER.error("Wrong type of Expression. Must be an unary " + "expression.");
            return null;
        }

    }
}
