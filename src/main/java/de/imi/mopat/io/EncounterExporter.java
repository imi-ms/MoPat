package de.imi.mopat.io;

import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.dao.EncounterDao;
import de.imi.mopat.helper.controller.StringUtilities;
import de.imi.mopat.model.Answer;
import de.imi.mopat.model.BodyPartAnswer;
import de.imi.mopat.model.BundleQuestionnaire;
import de.imi.mopat.model.Encounter;
import de.imi.mopat.model.EncounterExportTemplate;
import de.imi.mopat.model.ExportRule;
import de.imi.mopat.model.ExportRuleAnswer;
import de.imi.mopat.model.ExportRuleEncounter;
import de.imi.mopat.model.ExportRuleFormat;
import de.imi.mopat.model.ExportRuleQuestion;
import de.imi.mopat.model.ExportRuleScore;
import de.imi.mopat.model.ExportTemplate;
import de.imi.mopat.model.FreetextAnswer;
import de.imi.mopat.model.ImageAnswer;
import de.imi.mopat.model.PointOnImage;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.Response;
import de.imi.mopat.model.SelectAnswer;
import de.imi.mopat.model.enumeration.ExportDecimalDelimiterType;
import de.imi.mopat.model.enumeration.ExportNumberType;
import de.imi.mopat.model.enumeration.ExportRoundingStrategyType;
import de.imi.mopat.model.enumeration.ExportStatus;
import de.imi.mopat.model.enumeration.ExportTemplateType;
import de.imi.mopat.model.enumeration.Gender;
import de.imi.mopat.model.enumeration.QuestionType;
import de.imi.mopat.model.score.Score;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

/**
 * This class provides the functionality to start an export. It is possible to export an complete
 * {@link Encounter Encounter} with all assigned {@link ExportTemplate ExportTemplate}. In addition
 * it is possible to export a list of {@link Questionnaire Questionnaire} object, a single
 * questionnaire object or a single export template which all need to belong to the given encounter.
 * Based on the export template a specialized exporter (implements
 * {@link EncounterExporterTemplate EncounterExporterTemplate}) will be executed. The values from
 * the {@link Response Response} objects from the encounter, and the meta data from the encounter
 * will be formatted based on the {@link ExportRule ExportRule} objects from the export templates.
 * Those formatted values will be forwarded to the specialized exporter which fills the template and
 * exports it. Every export (successful or failed) will be saved as an
 * {@link EncounterExportTemplate EncounterExportTemplate} object to provide a export history.
 */
@Component
public class EncounterExporter {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(
        EncounterExporter.class);
    public static String TRUE = "TRUE";
    public static String FALSE = "FALSE";
    private Map<Answer, Response> answerResponseMap = null;
    @Autowired
    private EncounterDao encounterDao;
    @Autowired
    private ConfigurationDao configurationDao;
    @Autowired
    private MessageSource messageSource;

    /**
     * Exports the {@link Questionnaire} objects of a given {@link Encounter} object.
     *
     * @param encounter An object of {@link Encounter}. Must not be
     *                  <code>null</code>.
     * @return The list of {@link Questionnaire} objects that have successfully been exported. Is
     * never <code>null</code>. Might be empty.
     */
    public Set<Questionnaire> export(final Encounter encounter) {
        assert encounter != null : "The Encounter was null";
        Set<Questionnaire> questionnairesToExport = new HashSet<>();
        for (BundleQuestionnaire bundleQuestionnaire : encounter.getBundle()
            .getBundleQuestionnaires()) {
            Questionnaire questionnaire = bundleQuestionnaire.getQuestionnaire();
            if (questionnaire == null) {
                LOGGER.error("The bundle-to-questionnaire-mapping did not "
                    + "refer to a questionnaire. Thus, I " + "cannot access the questionnaire for "
                    + "the export. Moving to next " + "questionnaire...");
            } else {
                questionnairesToExport.add(questionnaire);
            }
        }

        Set<Questionnaire> result = export(encounter, questionnairesToExport);

        return result;
    }

    /**
     * Exports the {@link Questionnaire} objects of a given {@link Encounter} object.
     *
     * @param encounter      An object of {@link Encounter}. Must not be
     *                       <code>null</code>.
     * @param questionnaires the {@link Questionnaire} objects to export. Must not be
     *                       <code>null</code>. Only questionnaires that are both part of the given
     *                       encounter's {@link de.imi.mopat.model.Bundle} and this set will be
     *                       considered to be exported.
     * @return The list of {@link Questionnaire} objects that have successfully been exported. Is
     * never <code>null</code>. Might be empty.
     */
    public Set<Questionnaire> export(final Encounter encounter,
        final Set<Questionnaire> questionnaires) {
        assert encounter != null : "The Encounter was null";
        assert questionnaires != null : "The Set of Questionnaires was null";
        Set<Questionnaire> result = new HashSet<>();

        for (Questionnaire questionnaire : questionnaires) {
            boolean exportSucceeded = export(encounter, questionnaire);
            if (exportSucceeded) {
                result.add(questionnaire);
            }
        }
        return result;
    }

    /**
     * Exports the {@link Questionnaire} objects of a given {@link Encounter} objects.
     *
     * @param encounter     An object of {@link Encounter}. Must not be
     *                      <code>null</code>.
     * @param questionnaire The {@link Questionnaire} object to export. Must not be
     *                      <code>null</code>. Will only be exported if it is part of the given
     *                      encounter's {@link de.imi.mopat.model.Bundle}.
     * @return <code>true</code> if exporting the given {@link Questionnaire}
     * object worked, <code>false</code> otherwise.
     */
    public boolean export(final Encounter encounter, final Questionnaire questionnaire) {
        assert encounter != null : "The Encounter was null";
        assert questionnaire != null : "The Questionnaire was null";
        Set<ExportTemplate> exportTemplates = new HashSet<>();
        for (BundleQuestionnaire bs : encounter.getBundle().getBundleQuestionnaires()) {
            if (bs.getQuestionnaire().equals(questionnaire)) {
                exportTemplates = bs.getExportTemplates();
            }
        }
        boolean allSucceeded = true;
        for (ExportTemplate exportTemplate : exportTemplates) {
            boolean exportSucceeded = export(encounter, exportTemplate);
            if (!exportSucceeded) {
                allSucceeded = false;
            }
        }
        // only if all exportTemplates succeded return true
        return allSucceeded;
    }

    /**
     * Exports the {@link ExportTemplate} object of a given {@link Encounter} object.
     *
     * @param encounter      An object of {@link Encounter}. Must not be
     *                       <code>null</code>.
     * @param exportTemplate The {@link ExportTemplate} object to export. Must not be
     *                       <code>null</code>.
     * @return <code>true</code> if exporting the given {@link ExportTemplate}
     * object worked, <code>false</code> otherwise.
     */
    public boolean export(final Encounter encounter, final ExportTemplate exportTemplate) {
        return export(encounter, exportTemplate, false);
    }

    /**
     * Exports the {@link ExportTemplate} object of a given {@link Encounter Encounter} object.
     *
     * @param encounter      An object of {@link Encounter}. Must not be
     *                       <code>null</code>.
     * @param exportTemplate The {@link ExportTemplate} object to export. Must not be
     *                       <code>null</code>.
     * @param manual         The {@link ExportTemplate} was exported manually from a user.
     * @return <code>true</code> if exporting the given {@link ExportTemplate}
     * object worked, <code>false</code> otherwise.
     */
    public boolean export(final Encounter encounter, final ExportTemplate exportTemplate,
        final boolean manual) {
        assert encounter != null : "The Encounter was null";
        assert exportTemplate != null : "The ExportTemplate was null";
        // Remove reference from answerResponseMap so it gets filled with data
        // from the recent encounter
        answerResponseMap = null;
        // Set initial export status to failure
        ExportStatus exportStatus = ExportStatus.FAILURE;
        // Instantiate an object based on the exportTemplateType
        try {
            ExportTemplateType exportTemplateType = exportTemplate.getExportTemplateType();
            // Instantiate a new object based on the type of the export template
            // with the ConfigurationGroupDao and ConfigurationDao from the
            // context
            EncounterExporterTemplate exporter = exportTemplateType.createNewExporterInstance(
                configurationDao);
            // If no implementation for the exporter exists throw an exception
            if (exporter == null) {
                LOGGER.error("No Implementation found for {}",
                    exportTemplate.getExportTemplateType());
                throw new Exception(
                    "No Implementation found for " + exportTemplate.getExportTemplateType());
            }
            // Initialize the exporter
            exporter.load(encounter, exportTemplate);

            LOGGER.info("Export Encounter: {}", encounter);
            for (ExportRule rule : exportTemplate.getExportRules()) {
                String value = this.getFormattedValue(encounter, rule);
                exporter.write(rule.getExportField(), value);
            }

            // Flush out the export template to the export folder
            exportStatus = exporter.flush();

        } catch (Exception ex) {
            LOGGER.error(MarkerFactory.getMarker("FATAL"),
                "fatal error while exporting [exportTemplate={}, " + "Encounter={}]: {}",
                exportTemplate.getId(), encounter.getId(), ex.toString());
            // add a new failed export entry for the history
            EncounterExportTemplate encounterExportTemplate = new EncounterExportTemplate(encounter,
                exportTemplate, ExportStatus.FAILURE);
            encounterExportTemplate.setIsManuallyExported(manual);
            encounter.addEncounterExportTemplate(encounterExportTemplate);
            encounterDao.merge(encounter);
            return false;
        }
        // add a successful export entry for the history
        EncounterExportTemplate encounterExportTemplate = new EncounterExportTemplate(encounter,
            exportTemplate, exportStatus);
        encounterExportTemplate.setIsManuallyExported(manual);
        encounter.addEncounterExportTemplate(encounterExportTemplate);
        encounterDao.merge(encounter);
        return true;
    }

    /**
     * Returns the formatted value specified in the export rule from the given encounter.
     *
     * @param encounter  An {@link Encounter} object which holds the value.
     * @param exportRule An export rule which holds the information how the value should be
     *                   formatted.
     * @return The formatted value. The value is an empty string if no value could be aquired.
     */
    private String getFormattedValue(final Encounter encounter, final ExportRule exportRule)
        throws Exception {
        // initialize the first time otherwise re-use
        if (answerResponseMap == null) {
            // an answer-response map to easily access the responses based on
            // the answer object
            answerResponseMap = new HashMap<>();
            for (Response response : encounter.getResponses()) {
                answerResponseMap.put(response.getAnswer(), response);
            }
        }
        String value = "";
        // rule is of the type answer
        if (exportRule instanceof ExportRuleAnswer ruleAnswer) {
            // there exists an response to the answer
            if (answerResponseMap.containsKey(ruleAnswer.getAnswer())) {
                // get the response value based on the export rule
                value = this.getAnswerValue(ruleAnswer,
                    answerResponseMap.get(ruleAnswer.getAnswer()));
                // no response exists for the answer and is from type
                // multiple choice
                // this is a special case because no responses are saved for
                // not selected answers
            } else if (ruleAnswer.getAnswer().getQuestion().getQuestionType()
                == QuestionType.MULTIPLE_CHOICE
                || ruleAnswer.getAnswer().getQuestion().getQuestionType()
                == QuestionType.BODY_PART) {
                value = this.getAnswerValue(ruleAnswer, null);
            }
            // rule is of the type question
        } else if (exportRule instanceof ExportRuleQuestion ruleQuestion) {
            // get the value of the given answer of the question based on the
            // export rule
            value = this.getQuestionValue(ruleQuestion, encounter);
            // rule is from the type encounter
        } else if (exportRule instanceof ExportRuleEncounter ruleEncounter) {
            // get the encounter field value based on the export rule
            value = this.getEncounterValue(ruleEncounter, encounter);
            // rule is from the type score
        } else if (exportRule instanceof ExportRuleScore ruleScore) {
            // get the encounter field value based on the export rule
            value = this.getScoreValue(ruleScore, encounter);
        }
        return value;
    }

    /**
     * Returns the value of the {@link Response} object based on the {@link QuestionType} of the
     * corresponding answer. The value is an empty string if no value could be aquired.
     *
     * @param rule     A export rule which holds the information how the value should be formatted.
     * @param response A response from which the value should be aquired.
     * @return The value of the {@link Response} object based on the {@link QuestionType} of the
     * corresponding answer. The value is an empty string if no value could be aquired.
     */
    private String getAnswerValue(final ExportRuleAnswer rule, final Response response) {
        String value = "";
        ExportRuleFormat exportRuleFormat = rule.getExportRuleFormat();
        // get the value from the response based on the question type and answer
        QuestionType questionType = rule.getAnswer().getQuestion().getQuestionType();
        switch (questionType) {
            case BODY_PART:
            case MULTIPLE_CHOICE:
                if (rule.getAnswer() instanceof SelectAnswer
                    || rule.getAnswer() instanceof BodyPartAnswer) {
                    value = (response != null) ? TRUE : FALSE;
                } else if (rule.getAnswer() instanceof FreetextAnswer) {
                    value = response != null ? response.getCustomtext() : "";
                }
                break;
            case SLIDER:
            case NUMBER_CHECKBOX:
            case NUMBER_CHECKBOX_TEXT:
            case NUMBER_INPUT:
                if (rule.getUseFreetextValue()) {
                    // special case (can be removed as soon as the appended
                    // freetext fields are implemented as a real answer
                    value = response.getCustomtext();
                } else if (response.getValue() != null) {
                    // default case
                    value = formatNumber(exportRuleFormat, response.getValue());
                }
                break;
            case DATE:
                value = formatDate(exportRuleFormat, response.getDate());
                break;
            case FREE_TEXT:
                value = response.getCustomtext();
                break;
            case IMAGE:
                value = getImageValue(rule, response);
                break;
            default:
                LOGGER.error("no handler for the answer type {} found.",
                    rule.getAnswer().getQuestion().getQuestionType());
                break;
        }
        return value;
    }

    /**
     * Returns the value of the given answerto the {@link -Question} given in the
     * {@link ExportRuleQuestion}. The value is an empty string if no value could be aquired.
     *
     * @param rule      A export rule which holds the information how the value should be
     *                  formatted.
     * @param encounter An {@link Encounter} object which holds the {@link Response responses}.
     * @return The value of the given answerto the {@link -Question} given in the
     * {@link ExportRuleQuestion}. The value is an empty string if no value could be aquired.
     */
    private String getQuestionValue(final ExportRuleQuestion rule, final Encounter encounter) {
        String value = "";
        List<Answer> answers = rule.getQuestion().getAnswers();
        Set<Response> responses = encounter.getResponses();
        for (Answer answer : answers) {
            for (Response response : responses) {
                if (response.getAnswer().equals(answer)) {
                    try {
                        value = ((SelectAnswer) answer).getCodedValue();
                        return value;
                    } catch (Exception e) {
                    }
                }
            }
        }
        return value;
    }

    /**
     * Returns the formatted value of the field specified in the rule from the given
     * {@link Encounter}.
     *
     * @param rule      The rule contains the field name of the encounter.
     * @param encounter The {@link Encounter} which holds the values.
     * @return The value of the {@link Encounter} field. The value is an empty string if no value
     * could be aquired.
     */
    private String getEncounterValue(final ExportRuleEncounter rule, final Encounter encounter) {
        String value = "";
        try {
            // invoke encounter to get the value of the specific encounter field
            String methodName = rule.getEncounterField().getMethodName();
            Method encounterMethod = Encounter.class.getMethod(methodName);
            Object methodValue = encounterMethod.invoke(encounter);
            // If the value is null return an empty string
            if (methodValue == null) {
                return "";
            }
            // get the format of the rule to know how the values should be
            // formatted
            ExportRuleFormat exportRuleFormat = rule.getExportRuleFormat();

            // apply the appropriate formatting based on the type of the
            // encounter field
            String encounterFieldType = rule.getEncounterField().getType();
            switch (encounterFieldType) {
                case "java.lang.Long":
                    Long longValue = (Long) methodValue;
                    value = formatNumber(exportRuleFormat, longValue.doubleValue());
                    break;
                case "java.sql.Timestamp":
                    Timestamp timestamp = (Timestamp) methodValue;
                    value = formatDate(exportRuleFormat, new Date(timestamp.getTime()));
                    break;
                case "java.sql.Date":
                    java.sql.Date sqlDateValue = (java.sql.Date) methodValue;
                    value = formatDate(exportRuleFormat, new Date(sqlDateValue.getTime()));
                    break;
                case "java.lang.String":
                    value = (String) methodValue;
                    break;
                case "de.imi.mopat.model.Gender":
                    Gender val = (Gender) methodValue;
                    value = val.getTextValue();
                    break;
                default:
                    LOGGER.error("no handler for the encounter field type {} found.",
                        rule.getEncounterField().getType());
                    break;
            }
        } catch (ReflectiveOperationException ex) {
            LOGGER.error("Reflection error while getting encounter meta data: {}", ex);
        }
        return value;
    }

    /**
     * Returns the formatted value of the field specified in the {@link ExportRuleScore} from the
     * given {@link Score}.
     *
     * @param rule      The {@link ExportRuleScore} contains the field name and the score itself.
     * @param encounter The encounter which holds the values.
     * @return The value of the {@link Score} field. The value is an empty string if no value could
     * be aquired.
     */
    private String getScoreValue(final ExportRuleScore rule, final Encounter encounter)
        throws Exception {
        String value = "";
        try {
            // invoke encounter to get the value of the specific encounter field
            String methodName = rule.getScoreField().getMethodName();

            Object methodValue;
            // If the method is get formula, simply call this method from the
            // score
            if (methodName.equals("getFormula")) {
                methodValue = rule.getScore()
                    .getFormula(encounter, configurationDao.getDefaultLanguage());
                // Otherwise call the method via reflexion and pass the
                // encounter to the score method
            } else {
                Class[] arguments = new Class[1];
                arguments[0] = Encounter.class;
                Method scoreMethod = Score.class.getMethod(methodName, arguments);
                methodValue = scoreMethod.invoke(rule.getScore(), encounter);
            }

            // If the value is null, a string is returned that the value can
            // not be calculated
            if (methodValue == null) {
                LOGGER.debug("Calculation for the score {} and export field {} "
                        + "failed. Score will be ignored for export.", rule.getScore().getId(),
                    rule.getExportField());
                return messageSource.getMessage("encounter.export.scoreNotCalculable",
                    new Object[]{}, configurationDao.getDefaultLocale());
            }
            // get the format of the rule to know how the values should be
            // formatted
            ExportRuleFormat exportRuleFormat = rule.getExportRuleFormat();

            // apply the appropriate formatting based on the type of the
            // encounter field
            String encounterFieldType = rule.getScoreField().getType();
            switch (encounterFieldType) {
                case "java.lang.String":
                    value = (String) methodValue;
                    break;
                case "java.lang.Object":
                    if (rule.getScore().isBooleanScore()) {
                        Boolean booleanValue = (Boolean) methodValue;
                        value = booleanValue.toString();
                        break;
                    } else {
                        Double doubleValue = (Double) methodValue;
                        value = formatNumber(exportRuleFormat, doubleValue);
                        break;
                    }
                default:
                    LOGGER.error("no handler for the encounter field type {} found.",
                        rule.getScoreField().getType());
                    break;
            }
        } catch (ReflectiveOperationException ex) {
            LOGGER.error("Reflection error while getting encounter meta data: {}", ex);
        }
        return value;
    }

    /**
     * Returns a Base64 representation of the image with the given {@link Response responses} marked
     * as X.
     *
     * @param rule     An export rule, which holds the information how the value should be
     *                 formatted.
     * @param response A {@link Response response}, from which the value should be aquired.
     * @return A Base64 representation of the image with the given {@link Response responses} marked
     * as X.
     */
    private String getImageValue(final ExportRuleAnswer rule, final Response response) {
        // Try to load the image from the disk as a BufferedImage and get the
        // Graphics2D
        ImageAnswer answer = (ImageAnswer) rule.getAnswer();
        BufferedImage image;
        try {
            image = ImageIO.read(new File(
                configurationDao.getImageUploadPath() + "/question/" + answer.getImagePath()));
        } catch (IOException e) {
            return "Image not readable";
        }
        Graphics2D g2 = image.createGraphics();

        // Set the line width and round stroke style
        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // Iterate over each point that was set and draw an X on the image
        for (int i = 0; i < response.getPointsOnImage().size(); i++) {
            PointOnImage currentPoint = response.getPointsOnImage().get(i);
            int xCoordinate = (int) (currentPoint.getxCoordinate() * image.getWidth());
            int yCoordinate = (int) (currentPoint.getyCoordinate() * image.getHeight());
            g2.setColor(currentPoint.getColor().getColorClass());
            g2.drawLine(xCoordinate - 5, yCoordinate - 5, xCoordinate + 5, yCoordinate + 5);
            g2.drawLine(xCoordinate + 5, yCoordinate - 5, xCoordinate - 5, yCoordinate + 5);
        }
        g2.dispose();

        try {
            String imagePath =
                configurationDao.getImageUploadPath() + "/question/" + answer.getImagePath();
            String fileName = answer.getImagePath()
                .substring(answer.getImagePath().lastIndexOf("/"));
            return StringUtilities.convertImageToBase64String(imagePath, fileName);
        } catch (IOException e) {
            return "Image not readable";
        }
    }

    /**
     * Formats a date based on the given format.
     *
     * @param exportRuleFormat An {@link ExportRuleFormat} object.
     * @param date             The date to be formatted.
     * @return The formatted date.
     */
    private String formatDate(final ExportRuleFormat exportRuleFormat, final Date date) {
        if (exportRuleFormat.getDateFormat() != null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                exportRuleFormat.getDateFormat().getFormat());
            return simpleDateFormat.format(date);
        }
        // TODO what happens if there's no dateFormat??
        return date.toString();
    }

    /**
     * Formats a number based on the given format.
     *
     * @param exportRuleFormat An {@link ExportRuleFormat} object.
     * @param number           The number to be formatted.
     * @return The formatted number.
     */
    private String formatNumber(final ExportRuleFormat exportRuleFormat, final Double number) {
        // number is a float
        if (exportRuleFormat.getNumberType() == ExportNumberType.FLOAT) {
            DecimalFormat formatter = new DecimalFormat();
            // set decimal delimiter
            ExportDecimalDelimiterType decimalSeparator = exportRuleFormat.getDecimalDelimiter();
            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
            if (decimalSeparator != null) {
                symbols.setDecimalSeparator(decimalSeparator.getDelimiter());
            }
            formatter.setDecimalFormatSymbols(symbols);
            // Deactivate digit grouping
            formatter.setGroupingUsed(false);

            // set number of decimal places
            Integer decimalPlaces = exportRuleFormat.getDecimalPlaces();
            if (decimalPlaces != null) {
                formatter.setMaximumFractionDigits(decimalPlaces);
                formatter.setMinimumFractionDigits(decimalPlaces);
            }
            // round number depending on rounding strategy
            if (exportRuleFormat.getRoundingStrategy() != null) {
                formatter.setRoundingMode(exportRuleFormat.getRoundingStrategy().getRoundingMode());
            } else {
                // otherwise use standard rounding
                formatter.setRoundingMode(ExportRoundingStrategyType.STANDARD.getRoundingMode());
            }
            return formatter.format(number);
            // number is an integer
        } else if (exportRuleFormat.getNumberType() == ExportNumberType.INTEGER) {
            // round number depending on rounding strategy
            switch (exportRuleFormat.getRoundingStrategy()) {
                case CEIL:
                    return String.valueOf((int) Math.ceil(number));
                case FLOOR:
                    return String.valueOf((int) Math.floor(number));
                case STANDARD:
                default:
                    return String.valueOf((int) Math.round(number));
            }
        } else {
            // just return the number as a string
            return String.valueOf(number.intValue());
        }
    }
}
