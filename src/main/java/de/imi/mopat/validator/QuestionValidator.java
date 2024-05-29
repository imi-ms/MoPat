package de.imi.mopat.validator;

import de.imi.mopat.model.DateAnswer;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.enumeration.QuestionType;

import de.imi.mopat.model.SelectAnswer;
import de.imi.mopat.model.SliderAnswer;

import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;

/**
 * The validator for {@link Question Question} objects.
 */
@Component
public class QuestionValidator implements Validator {

    private static final String MIN_NUMBER_ANSWERS = "minNumberAnswers";
    private static final String MAX_NUMBER_ANSWERS = "maxNumberAnswers";
    @Autowired
    private SliderAnswerValidator sliderAnswerValidator;
    @Autowired
    private SelectAnswerValidator selectAnswerValidator;
    @Autowired
    private NumberInputAnswerValidator numberInputAnswerValidator;
    @Autowired
    private ImageAnswerValidator imageAnswerValidator;
    @Autowired
    private SpringValidatorAdapter validator;
    @Autowired
    private MessageSource messageSource;

    @Override
    public boolean supports(Class<?> type) {
        return Question.class.isAssignableFrom(type);
    }

    @Override
    public void validate(final Object target, final Errors errors) {

        // [bt] first, let the standard validator validate the target object
        // with respect to it's JSR-303 constraints (jakarta.validation
        // .constraints annotations)
        validator.validate(target, errors);

        // [bt] now it's my time to validate the more complex stuff
        Question question = (Question) target;
        // [sw] Check if any added language contains an empty questionText
        for (Map.Entry<String, String> entry : question.getLocalizedQuestionText().entrySet()) {
            if (entry.getValue() == null || entry.getValue().trim().isEmpty() || Pattern.matches(
                "<p>(<p>|</p>|\\s|&nbsp;|<br>)+<\\/p>", entry.getValue())) {
                question.getLocalizedQuestionText().put(entry.getKey(), "");
                if (question.getQuestionType() == QuestionType.INFO_TEXT) {
                    errors.rejectValue("localizedQuestionText[" + entry.getKey() + "]",
                        MoPatValidator.ERRORCODE_NOT_NULL,
                        messageSource.getMessage("question.error" + ".infoTextIsNull",
                            new Object[]{}, LocaleContextHolder.getLocale()));
                } else {
                    errors.rejectValue("localizedQuestionText[" + entry.getKey() + "]",
                        MoPatValidator.ERRORCODE_NOT_NULL,
                        messageSource.getMessage("question.error" + ".questionTextIsNull",
                            new Object[]{}, LocaleContextHolder.getLocale()));
                }
            }
        }

        switch (question.getQuestionType()) {
            case MULTIPLE_CHOICE:
            case DROP_DOWN: {
                // [bt] validation of the minimum and maximum number of
                // answers to be given.
                if (question.getMinNumberAnswers() == null) {
                    errors.rejectValue(MIN_NUMBER_ANSWERS, MoPatValidator.ERRORCODE_NOT_NULL,
                        messageSource.getMessage("question.error" + ".minNumberAnswersMissing",
                            new Object[]{}, LocaleContextHolder.getLocale()));
                }
                if (question.getMaxNumberAnswers() == null) {
                    errors.rejectValue(MAX_NUMBER_ANSWERS, MoPatValidator.ERRORCODE_NOT_NULL,
                        messageSource.getMessage("question.error" + ".maxNumberAnswersMissing",
                            new Object[]{}, LocaleContextHolder.getLocale()));
                }
                // [bt] validation of minNumberAnswers <= 0 not implemented
                // due to existent annotations in Question class
                // [bt] validation of maxNumberAnswers <= 1 not implemented
                // due to existent annotations in Question class

                if (question.getMinNumberAnswers() != null && question.getMaxNumberAnswers() != null
                    && question.getMinNumberAnswers() > question.getMaxNumberAnswers()) {
                    errors.rejectValue(MIN_NUMBER_ANSWERS, MoPatValidator.ERRORCODE_ERRORMESSAGE,
                        messageSource.getMessage("question.error" + ".minNumberBiggerThanMaxNumber",
                            new Object[]{}, LocaleContextHolder.getLocale()));
                    errors.rejectValue(MAX_NUMBER_ANSWERS, MoPatValidator.ERRORCODE_ERRORMESSAGE,
                        messageSource.getMessage(
                            "question.error" + ".maxNumberSmallerThanMinNumber", new Object[]{},
                            LocaleContextHolder.getLocale()));
                }

                if (question.getMinNumberAnswers() != null
                    && question.getMinNumberAnswers() > question.getAnswers().size()) {
                    errors.rejectValue(MIN_NUMBER_ANSWERS, MoPatValidator.ERRORCODE_ERRORMESSAGE,
                        messageSource.getMessage(
                            "question.error" + ".minNumberBiggerThanAmountOfAnswers",
                            new Object[]{}, LocaleContextHolder.getLocale()));
                }
                if (question.getMaxNumberAnswers() != null
                    && question.getMaxNumberAnswers() > question.getAnswers().size()) {
                    errors.rejectValue(MAX_NUMBER_ANSWERS, MoPatValidator.ERRORCODE_ERRORMESSAGE,
                        messageSource.getMessage(
                            "question.error" + ".maxNumberBiggerThanAmountOfAnswers",
                            new Object[]{}, LocaleContextHolder.getLocale()));
                }

                for (int i = 0; i < question.getAnswers().size(); i++) {
                    if (question.getAnswers().get(i) instanceof SelectAnswer selectAnswer) {
                        // tell the errors object that from now on the
                        // validation refers to the first of the question's
                        // answers.
                        errors.pushNestedPath("answers[" + i + "]");
                        // sub-validation
                        selectAnswerValidator.validate(selectAnswer, errors);
                        // tell the errors object that validation of the
                        // sub-element/property of this question is over.
                        errors.popNestedPath();
                    }
                }
                break;
            }
            case SLIDER:
            case NUMBER_CHECKBOX:
            case NUMBER_CHECKBOX_TEXT: {
                // [bt] tell the errors object that from now on the
                // validation refers to the first of the question's answers.
                errors.pushNestedPath("answers[0]");
                // [bt] sub-validation
                sliderAnswerValidator.validate(question.getAnswers().get(0), errors);
                // [bt] tell the errors object that validation of the
                // sub-element/property of this question is over.
                errors.popNestedPath();
                break;
            }
            case DATE: {
                DateAnswer dateAnswer = (DateAnswer) question.getAnswers().get(0);
                if (dateAnswer.getStartDate() != null && dateAnswer.getEndDate() != null
                    && dateAnswer.getStartDate().getTime() > dateAnswer.getEndDate().getTime()) {
                    errors.rejectValue("answers[0].endDate", MoPatValidator.ERRORCODE_ERRORMESSAGE,
                        messageSource.getMessage("dateAnswer.validator" + ".endEarlierThanStart",
                            new Object[]{}, LocaleContextHolder.getLocale()));
                }
                break;
            }
            case NUMBER_INPUT: {
                // [bt] tell the errors object that from now on the
                // validation refers to the first of the question's answers.
                errors.pushNestedPath("answers[0]");
                // [sw] sub-validation
                numberInputAnswerValidator.validate(question.getAnswers().get(0), errors);
                // [bt] tell the errors object that validation of the
                // sub-element/property of this question is over.
                errors.popNestedPath();
                break;
            }
            case IMAGE: {
                // [bt] tell the errors object that from now on the
                // validation refers to the first of the question's answers.
                errors.pushNestedPath("answers[0]");
                // [sw] sub-validation
                imageAnswerValidator.validate(question.getAnswers().get(0), errors);
                // [bt] tell the errors object that validation of the
                // sub-element/property of this question is over.
                errors.popNestedPath();
                break;
            }
            case FREE_TEXT:
            case INFO_TEXT:
            default:
                break;
        }
    }
}
