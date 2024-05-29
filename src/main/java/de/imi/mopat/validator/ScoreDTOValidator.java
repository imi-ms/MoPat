package de.imi.mopat.validator;

import de.imi.mopat.dao.OperatorDao;
import de.imi.mopat.model.dto.ExpressionDTO;
import de.imi.mopat.model.dto.ScoreDTO;
import de.imi.mopat.model.score.BinaryOperator;
import de.imi.mopat.model.score.BinaryOperatorBoolean;
import de.imi.mopat.model.score.MultiOperator;
import de.imi.mopat.model.score.Operator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * The validator for {@link ScoreDTO} objects.
 */
@Component
public class ScoreDTOValidator implements Validator {

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private OperatorDao operatorDao;

    @Override
    public boolean supports(final Class<?> type) {
        return ScoreDTO.class.isAssignableFrom(type);
    }

    @Override
    public void validate(final Object o, final Errors errors) {
        ScoreDTO score = (ScoreDTO) o;
        // The name must not be null or empty
        if (score.getName() == null || score.getName().equals("")) {
            errors.rejectValue("name", MoPatValidator.ERRORCODE_ERRORMESSAGE,
                messageSource.getMessage("score.error.name.notNull", new Object[]{},
                    LocaleContextHolder.getLocale()));
        } //The name may not contain any commas
        else if (score.getName().contains(",")) {
            errors.rejectValue("name", MoPatValidator.ERRORCODE_ERRORMESSAGE,
                messageSource.getMessage("score.error.name.noComma", new Object[]{},
                    LocaleContextHolder.getLocale()));
        }

        // The expression must not be null
        if (score.getExpression() == null) {
            errors.rejectValue("expression", MoPatValidator.ERRORCODE_ERRORMESSAGE,
                messageSource.getMessage("score.error.expression.notNull", new Object[]{},
                    LocaleContextHolder.getLocale()));
            return;
        }

        ExpressionDTO currentExpressionDTO = score.getExpression();
        // Validate the expression
        validateExpressionDTO(currentExpressionDTO, errors, "expression");

    }

    /**
     * Validates the given {@link ExpressionDTO} and adds a field error with the given fieldPath, if
     * validation fails. If the current {@link ExpressionDTO} has a {@link BinaryOperator} or a
     * {@link MultiOperator} this method is called recursively.
     *
     * @param currentExpressionDTO The {@link ExpressionDTO} which will be validated.
     * @param errors               The errors object containing all errors of the validation.
     * @param fieldPath            The path of the field, to make it possible to add a field error.
     */
    private void validateExpressionDTO(final ExpressionDTO currentExpressionDTO,
        final Errors errors, final String fieldPath) {
        // If the operator must not be null
        if (currentExpressionDTO.getOperatorId() == null) {
            errors.rejectValue(fieldPath, MoPatValidator.ERRORCODE_ERRORMESSAGE,
                messageSource.getMessage("score.error.operator.notNull", new Object[]{},
                    LocaleContextHolder.getLocale()));
            return;
        }

        // Get the current operator from the database
        Operator currentOperator = operatorDao.getElementById(currentExpressionDTO.getOperatorId());
        Operator firstOperator = null;
        Operator secondOperator = null;
        // Switch over the displaySign of the current operator
        switch (currentOperator.getDisplaySign()) {
            case "value":
                // If this is a value operator, the value must not be null
                if (currentExpressionDTO.getValue() == null || currentExpressionDTO.getValue()
                    .isEmpty()) {
                    errors.rejectValue(fieldPath + ".value", MoPatValidator.ERRORCODE_ERRORMESSAGE,
                        messageSource.getMessage("score.error.value" + ".notNull", new Object[]{},
                            LocaleContextHolder.getLocale()));
                }
                break;
            case "valueOf":
                // If this is a valueOf operator, the questionId must not be
                // null
                if (currentExpressionDTO.getQuestionId() == null) {
                    errors.rejectValue(fieldPath + ".questionId",
                        MoPatValidator.ERRORCODE_ERRORMESSAGE,
                        messageSource.getMessage("score.error.questionId" + ".notNull",
                            new Object[]{}, LocaleContextHolder.getLocale()));
                }
                break;
            case "valueOfScore":
                if (currentExpressionDTO.getScoreId() == null) {
                    errors.rejectValue(fieldPath + ".scoreId",
                        MoPatValidator.ERRORCODE_ERRORMESSAGE,
                        messageSource.getMessage("score.error.scoreId" + ".notNull", new Object[]{},
                            LocaleContextHolder.getLocale()));
                }
                break;
            case "==":
            case "!=":
                // Check if the current expression has exactly 2 children
                if (currentExpressionDTO.getExpressions().size() != 2) {
                    errors.rejectValue(fieldPath + ".questionId",
                        MoPatValidator.ERRORCODE_ERRORMESSAGE,
                        messageSource.getMessage("score.error.expressions" + ".notTwoExpressions",
                            new Object[]{}, LocaleContextHolder.getLocale()));
                    return;
                }
                // Retrieve the first and second operators
                firstOperator = operatorDao.getElementById(
                    currentExpressionDTO.getExpressions().get(0).getOperatorId());
                secondOperator = operatorDao.getElementById(
                    currentExpressionDTO.getExpressions().get(1).getOperatorId());
                // And check if they are of the same type: Boolean-Boolean or
                // Numeric-Numeric
                if ((firstOperator instanceof BinaryOperatorBoolean
                    && !(secondOperator instanceof BinaryOperatorBoolean)) || (
                    !(firstOperator instanceof BinaryOperatorBoolean)
                        && secondOperator instanceof BinaryOperatorBoolean)) {
                    errors.rejectValue(fieldPath + ".questionId",
                        MoPatValidator.ERRORCODE_ERRORMESSAGE,
                        messageSource.getMessage("score.error.expressions" + ".twoExpressionsEqual",
                            new Object[]{}, LocaleContextHolder.getLocale()));
                } else {
                    validateExpressionDTO(currentExpressionDTO.getExpressions().get(0), errors,
                        fieldPath + ".expressions[" + 0 + "]");
                    validateExpressionDTO(currentExpressionDTO.getExpressions().get(1), errors,
                        fieldPath + ".expressions[" + 1 + "]");
                }
                break;
            case ">":
            case "<":
            case ">=":
            case "<=":
            case "+":
            case "-":
            case "/":
            case "*":
                // Check if the current expression has exactly 2 children
                if (currentExpressionDTO.getExpressions().size() != 2) {
                    errors.rejectValue(fieldPath + ".questionId",
                        MoPatValidator.ERRORCODE_ERRORMESSAGE,
                        messageSource.getMessage("score.error.expressions" + ".notTwoExpressions",
                            new Object[]{}, LocaleContextHolder.getLocale()));
                    return; //Further validation is not possible
                }
                // Retrieve the first and second operators
                firstOperator = operatorDao.getElementById(
                    currentExpressionDTO.getExpressions().get(0).getOperatorId());
                secondOperator = operatorDao.getElementById(
                    currentExpressionDTO.getExpressions().get(1).getOperatorId());
                // And check if they are both of numeric type
                if (firstOperator instanceof BinaryOperatorBoolean
                    || secondOperator instanceof BinaryOperatorBoolean) {
                    errors.rejectValue(fieldPath + ".questionId",
                        MoPatValidator.ERRORCODE_ERRORMESSAGE,
                        messageSource.getMessage("score.error.expressions" + ".numeric",
                            new Object[]{}, LocaleContextHolder.getLocale()));
                } else {
                    validateExpressionDTO(currentExpressionDTO.getExpressions().get(0), errors,
                        fieldPath + ".expressions[" + 0 + "]");
                    validateExpressionDTO(currentExpressionDTO.getExpressions().get(1), errors,
                        fieldPath + ".expressions[" + 1 + "]");
                }
                break;
            case "minimum":
            case "maximum":
            case "average":
            case "sum":
                // Make a recursive call with the child expressions
                for (int i = 0; i < currentExpressionDTO.getExpressions().size(); i++) {
                    // Check if the children are all numeric (NOT
                    // binary-boolean)
                    if (currentExpressionDTO.getExpressions().get(i).getOperatorId() == null) {
                        errors.rejectValue(fieldPath, MoPatValidator.ERRORCODE_ERRORMESSAGE,
                            messageSource.getMessage("score.error.operator.notNull", new Object[]{},
                                LocaleContextHolder.getLocale()));
                    }
                    Operator currentOperatorSum = operatorDao.getElementById(
                        currentExpressionDTO.getExpressions().get(i).getOperatorId());
                    if (currentOperatorSum instanceof BinaryOperatorBoolean) {
                        errors.rejectValue(fieldPath + ".questionId",
                            MoPatValidator.ERRORCODE_ERRORMESSAGE,
                            messageSource.getMessage("score.error.expressions.numeric",
                                new Object[]{}, LocaleContextHolder.getLocale()));
                    } else {
                        validateExpressionDTO(currentExpressionDTO.getExpressions().get(i), errors,
                            fieldPath + ".expressions[" + i + "]");
                    }
                }
                break;
            case "counter":
                // Make a recursive call with the child expressions
                for (int i = 0; i < currentExpressionDTO.getExpressions().size(); i++) {
                    // Check if the children are all binary-boolean
                    if (currentExpressionDTO.getExpressions().get(i).getOperatorId() == null) {
                        errors.rejectValue(fieldPath, MoPatValidator.ERRORCODE_ERRORMESSAGE,
                            messageSource.getMessage("score.error.operator.notNull", new Object[]{},
                                LocaleContextHolder.getLocale()));
                        return;
                    }
                    Operator currentOperatorSum = operatorDao.getElementById(
                        currentExpressionDTO.getExpressions().get(i).getOperatorId());
                    if (!(currentOperatorSum instanceof BinaryOperatorBoolean)) {
                        errors.rejectValue(fieldPath + ".questionId",
                            MoPatValidator.ERRORCODE_ERRORMESSAGE,
                            messageSource.getMessage("score.error.expressions.boolean",
                                new Object[]{}, LocaleContextHolder.getLocale()));
                    } else {
                        validateExpressionDTO(currentExpressionDTO.getExpressions().get(i), errors,
                            fieldPath + ".expressions[" + i + "]");
                    }
                }
                break;
        }
    }
}
