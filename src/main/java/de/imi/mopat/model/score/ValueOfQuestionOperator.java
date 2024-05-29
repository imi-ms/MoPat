package de.imi.mopat.model.score;

import de.imi.mopat.model.Answer;
import de.imi.mopat.model.Encounter;
import de.imi.mopat.model.Response;
import de.imi.mopat.model.SelectAnswer;

import java.util.Map;
import java.util.Set;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * This operator returns either the value of the {@link Response} from a
 * {@link de.imi.mopat.model.Question} (evaluate) or the localized question text of a
 * {@link de.imi.mopat.model.Question} as formula (getFormula).
 */
@Entity
@DiscriminatorValue("ValueOfQuestion")
public class ValueOfQuestionOperator extends UnaryOperator {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(
        ValueOfQuestionOperator.class);

    @Override
    public Double evaluate(final Expression expression, final Encounter encounter) {
        if (expression instanceof UnaryExpression) {
            UnaryExpression unaryExpression = (UnaryExpression) expression;

            if (unaryExpression.getQuestion() == null || unaryExpression.getQuestion().getAnswers()
                .isEmpty()) {
                LOGGER.error("This expression contains not a question or " + "answers.");
                return null;
            }

            Set<Response> encounterResponses = encounter.getResponses();
            for (Response response : encounterResponses) {
                for (Answer answer : unaryExpression.getQuestion().getAnswers()) {
                    if (response.getAnswer().getId() == answer.getId().longValue()) {
                        if (response.getValue() == null) {
                            LOGGER.debug(
                                "There is no given value for the " + "selected answer with the "
                                    + "id: " + answer.getId() + ". The score cannot be "
                                    + "calculated.");
                            return null;
                        }
                        if (answer instanceof SelectAnswer) {
                            return ((SelectAnswer) answer).getValue();
                        } else {
                            return response.getValue();
                        }
                    }
                }
            }

            LOGGER.debug(
                "There was no response for the question: " + unaryExpression.getQuestion().getId()
                    + ". The score cannot be calculated.");
            return null;
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

            if (unaryExpression.getQuestion() == null) {
                LOGGER.error("This expression contains not a question or " + "answers.");
                return null;
            }

            Map<String, String> localizedQuestionText = unaryExpression.getQuestion()
                .getLocalizedQuestionText();

            if (!localizedQuestionText.containsKey(defaultLanguage)) {
                if (defaultLanguage.contains("-") && localizedQuestionText.containsKey(
                    defaultLanguage.substring(0, defaultLanguage.indexOf("-")))) {
                    defaultLanguage = defaultLanguage.substring(0, defaultLanguage.indexOf("-"));
                } else {
                    defaultLanguage = localizedQuestionText.keySet().iterator().next();
                }
            }
            return localizedQuestionText.get(defaultLanguage);
        } else {
            LOGGER.error("Wrong type of Expression. Must be an unary " + "expression.");
            return null;
        }
    }
}
