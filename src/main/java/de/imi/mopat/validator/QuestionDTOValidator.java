package de.imi.mopat.validator;

import de.imi.mopat.model.enumeration.QuestionType;
import de.imi.mopat.model.dto.AnswerDTO;
import de.imi.mopat.model.dto.QuestionDTO;

import java.util.ArrayList;
import java.util.List;
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
 * The validator for {@link QuestionDTO QuestionDTO} objects.
 */
@Component
public class QuestionDTOValidator implements Validator {

    private static final String MIN_NUMBER_ANSWERS = "minNumberAnswers";
    private static final String MAX_NUMBER_ANSWERS = "maxNumberAnswers";
    @Autowired
    private SelectAnswerDTOValidator selectAnswerDTOValidator;
    @Autowired
    private SliderAnswerDTOValidator sliderAnswerDTOValidator;
    @Autowired
    private NumberInputAnswerDTOValidator numberInputAnswerDTOValidator;
    @Autowired
    private DateAnswerDTOValidator dateAnswerDTOValidator;
    @Autowired
    private ImageAnswerDTOValidator imageAnswerDTOValidator;
    @Autowired
    private SpringValidatorAdapter validator;
    @Autowired
    private MessageSource messageSource;

    @Override
    public boolean supports(Class<?> type) {
        return QuestionDTO.class.isAssignableFrom(type);
    }

    @Override
    public void validate(final Object target, final Errors errors) {

        // [bt] first, let the standard validator validate the target object
        // with respect to it's JSR-303 constraints (jakarta.validation
        // .constraints annotations)
        validator.validate(target, errors);

        // [bt] now it's my time to validate the more complex stuff
        QuestionDTO questionDTO = (QuestionDTO) target;

        // [sw] Check if any added language contains an empty questionText
        for (Map.Entry<String, String> entry : questionDTO.getLocalizedQuestionText().entrySet()) {
            if (entry.getValue() == null || entry.getValue().trim().isEmpty() || Pattern.matches(
                "<p>(<p>|</p>|\\s|&nbsp;|<br>)+<\\/p>", entry.getValue())) {
                questionDTO.getLocalizedQuestionText().put(entry.getKey(), "");
                if (questionDTO.getQuestionType() == QuestionType.INFO_TEXT) {
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

        switch (questionDTO.getQuestionType()) {
            case MULTIPLE_CHOICE:
            case DROP_DOWN:
                if (questionDTO.getCodedValueType() == null) {
                    errors.rejectValue("codedValueType", MoPatValidator.ERRORCODE_NOT_NULL,
                        messageSource.getMessage("question.error" + ".codedValueTypeMissing",
                            new Object[]{}, LocaleContextHolder.getLocale()));
                }
                // Check if coded values of answer are unique
                List<String> existingCodedValues = new ArrayList<>();
                for (Long i : questionDTO.getAnswers().keySet()) {
                    AnswerDTO answerDTO = questionDTO.getAnswers().get(i);
                    if (answerDTO.getCodedValue() == null || answerDTO.getCodedValue().isEmpty()) {
                        errors.rejectValue("answers[" + i + "].codedValue",
                            MoPatValidator.ERRORCODE_ERRORMESSAGE,
                            messageSource.getMessage("question.error" + ".codedValueEmpty",
                                new Object[]{}, LocaleContextHolder.getLocale()));
                    } else {
                        if (existingCodedValues.contains(answerDTO.getCodedValue())) {
                            errors.rejectValue("answers[" + i + "].codedValue",
                                MoPatValidator.ERRORCODE_ERRORMESSAGE,
                                messageSource.getMessage("question.error" + ".codedValueDuplicate",
                                    new Object[]{}, LocaleContextHolder.getLocale()));
                        }
                        existingCodedValues.add(answerDTO.getCodedValue());
                    }
                }
            case BODY_PART: {
                int realNumberOfAnswers = 0;
                if (questionDTO.getAnswers() != null && !questionDTO.getAnswers().isEmpty()) {
                    for (Long i : questionDTO.getAnswers().keySet()) {
                        AnswerDTO answerDTO = questionDTO.getAnswers().get(i);
                        if (answerDTO.getId() != null && answerDTO.getId() < 0L) {
                            continue;
                        }
                        realNumberOfAnswers++;

                        // [bt] tell the errors object that from now on the
                        // validation refers to the first of the question's
                        // answers.
                        errors.pushNestedPath("answers[" + i + "]");
                        // [bt] sub-validation
                        selectAnswerDTOValidator.validate(questionDTO.getAnswers().get(i), errors);
                        // [bt] tell the errors object that validation of the
                        // sub-element/property of this question is over.
                        errors.popNestedPath();
                    }
                } else {
                    errors.rejectValue(MIN_NUMBER_ANSWERS, MoPatValidator.ERRORCODE_NOT_NULL,
                        messageSource.getMessage("question.error" + ".noAnswerSelected",
                            new Object[]{}, LocaleContextHolder.getLocale()));
                }

                Integer minNumberAnswers = null;
                Integer maxNumberAnswers = null;
                try {
                    // [bt] validation of the minimum and maximum number of
                    // answers to be given.
                    if (questionDTO.getMinNumberAnswers() == null) {
                        errors.rejectValue(MIN_NUMBER_ANSWERS, MoPatValidator.ERRORCODE_NOT_NULL,
                            messageSource.getMessage("question.error" + ".minNumberAnswersMissing",
                                new Object[]{}, LocaleContextHolder.getLocale()));
                    } else {
                        minNumberAnswers = questionDTO.getMinNumberAnswers();
                    }
                    if (questionDTO.getMaxNumberAnswers() == null) {
                        errors.rejectValue(MAX_NUMBER_ANSWERS, MoPatValidator.ERRORCODE_NOT_NULL,
                            messageSource.getMessage("question.error" + ".maxNumberAnswersMissing",
                                new Object[]{}, LocaleContextHolder.getLocale()));
                    } else {
                        maxNumberAnswers = questionDTO.getMaxNumberAnswers();
                    }
                    // [bt] validation of minNumberAnswers <= 0 not
                    // implemented due to existent annotations in Question class
                    // [bt] validation of maxNumberAnswers <= 1 not
                    // implemented due to existent annotations in Question class
                    if (minNumberAnswers != null && maxNumberAnswers != null
                        && minNumberAnswers > maxNumberAnswers) {
                        errors.rejectValue(MIN_NUMBER_ANSWERS,
                            MoPatValidator.ERRORCODE_ERRORMESSAGE, messageSource.getMessage(
                                "question.error" + ".minNumberBiggerThanMaxNumber", new Object[]{},
                                LocaleContextHolder.getLocale()));
                        //errors.rejectValue(MAX_NUMBER_ANSWERS,
                        // MoPatValidator.ERRORCODE_ERRORMESSAGE,
                        // messageSource.getMessage("question.error
                        // .maxNumberSmallerThanMinNumber", new Object[]{},
                        // LocaleContextHolder.getLocale()));
                    }

                    if (minNumberAnswers != null && minNumberAnswers > realNumberOfAnswers) {
                        errors.rejectValue(MIN_NUMBER_ANSWERS,
                            MoPatValidator.ERRORCODE_ERRORMESSAGE, messageSource.getMessage(
                                "question.error" + ".minNumberBiggerThanAmountOfAnswers",
                                new Object[]{}, LocaleContextHolder.getLocale()));
                    }
                    if (maxNumberAnswers != null && maxNumberAnswers > realNumberOfAnswers) {
                        errors.rejectValue(MAX_NUMBER_ANSWERS,
                            MoPatValidator.ERRORCODE_ERRORMESSAGE, messageSource.getMessage(
                                "question.error" + ".maxNumberBiggerThanAmountOfAnswers",
                                new Object[]{}, LocaleContextHolder.getLocale()));
                    }
                } catch (NumberFormatException ex) {
                    // already handled by javax validation
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
                sliderAnswerDTOValidator.validate(questionDTO.getAnswers().get(0L), errors);
                // [bt] tell the errors object that validation of the
                // sub-element/property of this question is over.
                errors.popNestedPath();
                break;
            }
            case DATE: {
                // [bt] tell the errors object that from now on the
                // validation refers to the first of the question's answers.
                errors.pushNestedPath("answers[0]");
                // [bt] sub-validation
                dateAnswerDTOValidator.validate(questionDTO.getAnswers().get(0L), errors);
                // [bt] tell the errors object that validation of the
                // sub-element/property of this question is over.
                errors.popNestedPath();
                break;
            }
            case NUMBER_INPUT: {
                // [bt] tell the errors object that from now on the
                // validation refers to the first of the question's answers.
                errors.pushNestedPath("answers[0]");
                // [sw] sub-validation
                numberInputAnswerDTOValidator.validate(questionDTO.getAnswers().get(0L), errors);
                // [bt] tell the errors object that validation of the
                // sub-element/property of this question is over.
                errors.popNestedPath();
                break;
            }
            case IMAGE: {
                // [bt] tell the errors object that from now on the
                // validation refers to the first of the question's answers.
                errors.pushNestedPath("answers[0]");
                // [bt] sub-validation
                imageAnswerDTOValidator.validate(questionDTO.getAnswers().get(0L), errors);
                // [bt] tell the errors object that validation of the
                // sub-element/property of this question is over.
                errors.popNestedPath();
                break;
            }
            case FREE_TEXT:
            case INFO_TEXT:
            case BARCODE:
            default:
                break;
        }
    }
}
