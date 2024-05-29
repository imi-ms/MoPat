package de.imi.mopat.model.score;

import de.imi.mopat.model.Encounter;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * This operator returns either the value of its {@link Score} (evaluate) or the formula of its
 * {@link Score}
 */
@Entity
@DiscriminatorValue("ValueOfScore")
public class ValueOfScoreOperator extends UnaryOperator {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(Average.class);

    @Override
    public Object evaluate(final Expression expression, final Encounter encounter) {
        if (expression instanceof UnaryExpression) {
            UnaryExpression unaryExpression = (UnaryExpression) expression;

            if (unaryExpression.getScore() == null) {
                LOGGER.error("This expression contains no score.");
                return null;
            }
            return unaryExpression.getScore().evaluate(encounter);
        } else {
            LOGGER.error("Wrong type of Expression. Must be an unary " + "expression.");
            return null;
        }
    }

    @Override
    public String getFormula(final Expression expression, final Encounter encounter,
        final String defaultLanguage) {
        if (expression instanceof UnaryExpression) {
            UnaryExpression unaryExpression = (UnaryExpression) expression;

            if (unaryExpression.getScore() == null) {
                LOGGER.error("This expression contains no score.");
                return null;
            }
            return unaryExpression.getScore().getFormula(encounter, defaultLanguage);
        } else {
            LOGGER.error("Wrong type of Expression. Must be an unary " + "expression.");
            return null;
        }
    }
}
