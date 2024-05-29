package de.imi.mopat.model.score;

import de.imi.mopat.model.Encounter;

import java.util.Iterator;
import java.util.List;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * This class returns either the result of the operation A != B between the first {@link Expression}
 * A and the second {@link Expression} B (evaluate) or the formula of {@link Expression} A '!=' the
 * formula of {@link Expression} B (getFormula).
 */
@Entity
@DiscriminatorValue("Different")
public class Different extends BinaryOperatorBoolean {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(
        ValueOfQuestionOperator.class);

    @Override
    public Boolean evaluate(final Expression expression, final Encounter encounter) {
        if (expression instanceof BinaryExpression) {
            BinaryExpression binaryExpression = (BinaryExpression) expression;
            List<Expression> notequalExpressions = binaryExpression.getExpressions();

            if (notequalExpressions.size() != 2) {
                LOGGER.error("The number of expressions for a != operator " + "must be 2.");
                return null;
            }

            Iterator<Expression> iterator = notequalExpressions.iterator();
            Double firstExpression = (Double) iterator.next().evaluate(encounter);
            Double secondExpression = (Double) iterator.next().evaluate(encounter);

            if (firstExpression != null && secondExpression != null) {
                return (!firstExpression.equals(secondExpression));
            } else {
                return null;
            }
        } else {
            LOGGER.error("Wrong type of Expression. Must be an binary " + "expression.");
            return null;
        }
    }

    @Override
    public String getFormula(final Expression expression, final Encounter encounter,
        final String defaultLanguage) {
        if (expression instanceof BinaryExpression) {
            BinaryExpression binaryExpression = (BinaryExpression) expression;
            List<Expression> notequalExpressions = binaryExpression.getExpressions();
            if (notequalExpressions.size() != 2) {
                LOGGER.error("The number of expressions for a != operator " + "must be 2.");
                return null;
            }
            Iterator<Expression> iterator = notequalExpressions.iterator();

            return "(" + iterator.next().getFormula(encounter, defaultLanguage) + " != "
                + iterator.next().getFormula(encounter, defaultLanguage) + ")";
        } else {
            LOGGER.error("Wrong type of Expression. Must be an binary " + "expression.");
            return null;
        }
    }
}
