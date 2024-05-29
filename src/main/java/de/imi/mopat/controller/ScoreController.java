package de.imi.mopat.controller;

import de.imi.mopat.dao.ExportTemplateDao;
import de.imi.mopat.dao.OperatorDao;
import de.imi.mopat.dao.QuestionDao;
import de.imi.mopat.dao.QuestionnaireDao;
import de.imi.mopat.dao.ScoreDao;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.dto.ExpressionDTO;
import de.imi.mopat.model.dto.ScoreDTO;
import de.imi.mopat.model.score.BinaryExpression;
import de.imi.mopat.model.score.BinaryOperator;
import de.imi.mopat.model.score.Expression;
import de.imi.mopat.model.score.MultiExpression;
import de.imi.mopat.model.score.MultiOperator;
import de.imi.mopat.model.score.Operator;
import de.imi.mopat.model.score.Score;
import de.imi.mopat.model.score.UnaryExpression;
import de.imi.mopat.model.score.ValueOfQuestionOperator;
import de.imi.mopat.model.score.ValueOperator;
import de.imi.mopat.validator.ScoreDTOValidator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 */
@Controller
public class ScoreController {

    @Autowired
    private ExportTemplateDao exportTemplateDao;
    @Autowired
    private QuestionnaireDao questionnaireDao;
    @Autowired
    private QuestionDao questionDao;
    @Autowired
    private ScoreDao scoreDao;
    @Autowired
    private OperatorDao operatorDao;
    @Autowired
    private ScoreDTOValidator scoreDTOValidator;

    /**
     * Controls the HTTP GET requests for the URL <i>/score/list</i>. Shows the list of scores for
     * one {@link Questionnaire}.
     *
     * @param id    The id of the {@link Questionnaire}.
     * @param model The model, which holds the information for the view.
     * @return The <i>score/list</i> website.
     */
    @RequestMapping(value = "/score/list", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String showScoresForQuestionnaire(
        @RequestParam(value = "id", required = true) final Long id, final Model model) {
        Questionnaire questionnaire = questionnaireDao.getElementById(id);

        if (questionnaire == null) {
            return "redirect:/questionnaire/list";
        }

        // Sort the scores
        List<Score> scores = new ArrayList<>();
        scores.addAll(questionnaire.getScores());
        Collections.sort(scores, (Score o1, Score o2) -> o1.getName().compareTo(o2.getName()));

        List<ScoreDTO> scoreDTOs = new ArrayList<>();
        for (Score score : scores) {
            scoreDTOs.add(score.toScoreDTO());
        }

        model.addAttribute("scores", scoreDTOs);
        model.addAttribute("questionnaire", questionnaire);
        return "score/list";
    }

    /**
     * Controls the HTTP GET requests for the URL <i>/score/fill</i>. Shows the page containing the
     * form fields for a new {@link ScoreDTO} object.
     *
     * @param questionnaireId The id of the {@link Questionnaire} the current score belongs to.
     * @param scoreId         The id of the current {@link Score},
     * @param model           The model, which holds the information for the view.
     * @return The <i>score/fill</i> website.
     */
    @RequestMapping(value = "/score/fill", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_EDITOR')")
    public String fillScore(
        @RequestParam(value = "questionnaireId", required = true) final Long questionnaireId,
        @RequestParam(value = "id", required = false) final Long scoreId, final Model model) {
        // Get a new ScoreDTO
        ScoreDTO scoreDTO = new ScoreDTO();
        // Get the Questionnaire and the Score
        Questionnaire questionnaire = questionnaireDao.getElementById(questionnaireId);
        List<Score> availableScores;
        // If the scoreId is set, get the ScoreDTO representation from the score
        if (scoreId != null) {
            Score score = scoreDao.getElementById(scoreId);
            scoreDTO = score.toScoreDTO();
            availableScores = questionnaire.getAvailableScoresForScore(score);
        } else {
            availableScores = new ArrayList<>(questionnaire.getScores());
        }
        // Sort the available scores by name
        Collections.sort(availableScores,
            (Score o1, Score o2) -> o1.getName().compareTo(o2.getName()));
        scoreDTO.setQuestionnaireId(questionnaireId);

        // Get all Operators
        List<Operator> operators = new ArrayList<>(operatorDao.getOperators());
        // And delete value of score, if available scores are empty
        if (availableScores.isEmpty()) {
            operators.remove(operatorDao.getOperatorByDisplaySign("valueOfScore"));
        }
        List<Question> availableQuestionsForScore = questionnaire.getAvailableQuestionsForScore();
        if (availableQuestionsForScore == null || availableQuestionsForScore.isEmpty()) {
            operators.remove(operatorDao.getOperatorByDisplaySign("valueOf"));
        }
        model.addAttribute("questionnaire", questionnaire);
        model.addAttribute("operators", operators);
        model.addAttribute("availableQuestionsForScore", availableQuestionsForScore);
        model.addAttribute("availableScoresForScore", availableScores);
        model.addAttribute("scoreDTO", scoreDTO);
        return "score/edit";
    }

    /**
     * Controls the HTTP POST requests for the URL <i>/score/edit</i>. Provides the ability to
     * create a new or edit an existing {@link Score} object.
     *
     * @param questionnaireId The id of the {@link Questionnaire} the current score belongs to.
     * @param scoreDTO        The {@link ScoreDTO} object from the view.
     * @param action          The name of the submit button which has been clicked.
     * @param result          The result for validation of the {@link ScoreDTO} object.
     * @param model           The model, which holds the information for the view.
     * @return Redirect to the <i>score/list</i> website.
     */
    @RequestMapping(value = "/score/edit", method = RequestMethod.POST)
    @PreAuthorize("hasRole('ROLE_EDITOR')")
    public String editScore(
        @RequestParam(value = "questionnaireId", required = true) final Long questionnaireId,
        @RequestParam(required = false, value = "postAction") final String action,
        @ModelAttribute("scoreDTO") final ScoreDTO scoreDTO, BindingResult result,
        final Model model) {

        if (action.equalsIgnoreCase("cancel")) {
            return "redirect:/score/list?id=" + questionnaireId;
        }

        // Validate the scoreDTO object with a new result. The result from
        // spring contains errors that should be ignored
        result = new BindException(scoreDTO, "scoreDTO");
        scoreDTOValidator.validate(scoreDTO, result);

        if (result.hasErrors()) {
            Questionnaire questionnaire = questionnaireDao.getElementById(questionnaireId);
            List<Score> availableScores;
            if (scoreDTO.getId() != null) {
                availableScores = questionnaire.getAvailableScoresForScore(
                    scoreDao.getElementById(scoreDTO.getId()));
            } else {
                availableScores = new ArrayList<>(questionnaire.getScores());
            }
            // Sort the available scores by name
            Collections.sort(availableScores,
                (Score o1, Score o2) -> o1.getName().compareTo(o2.getName()));
            // Get all Operators
            List<Operator> operators = new ArrayList<>(operatorDao.getOperators());
            // And delete value of score, if available scores are empty
            if (availableScores.isEmpty()) {
                operators.remove(operatorDao.getOperatorByDisplaySign("valueOfScore"));
            }
            List<Question> availableQuestionsForScore = questionnaire.getAvailableQuestionsForScore();
            if (availableQuestionsForScore == null || availableQuestionsForScore.isEmpty()) {
                operators.remove(operatorDao.getOperatorByDisplaySign("valueOf"));
            }
            model.addAttribute("operators", operators);
            model.addAttribute("availableQuestionsForScore", availableQuestionsForScore);
            model.addAttribute("availableScoresForScore", availableScores);
            // overwrite the spring errors and take the new one
            model.addAttribute("org.springframework.validation.BindingResult.scoreDTO", result);
            model.addAttribute("questionnaire", questionnaire);
            return "score/edit";
        }
        Questionnaire questionnaire = questionnaireDao.getElementById(
            scoreDTO.getQuestionnaireId());

        Score score = null;

        if (scoreDTO.getId() != null) {
            score = scoreDao.getElementById(scoreDTO.getId());
            score.setExpression(null);
        } else {
            score = new Score();
            score.setQuestionnaire(questionnaire);
        }

        score.setName(scoreDTO.getName());

        Expression expression = scoreDTO.getExpression()
            .toExpression(operatorDao, questionDao, scoreDao);
        score.setExpression(expression);

        scoreDao.merge(score);
        questionnaireDao.merge(questionnaire);
        return "redirect:/score/list?id=" + questionnaireId;
    }

    /**
     * Controls the HTTP requests for the URL <i>score/remove</i>. Removes a {@link Score} object by
     * a given id and redirects to the list of {@link Score Scores}.
     *
     * @param scoreId Id of the {@link Score} object, which should be removed
     * @param model   The model, which holds the information for the view.
     * @return Redirect to the <i>score/list</i> website.
     */
    @RequestMapping(value = "/score/remove")
    @PreAuthorize("hasRole('ROLE_EDITOR')")
    public String removeScore(@RequestParam(value = "id", required = true) final Long scoreId,
        final Model model) {
        Score score = scoreDao.getElementById(scoreId);
        List<Score> dependingScores = score.getDependingScores();
        // Sort depending scores by amount of their depending scores to
        // prevent database errors
        Collections.sort(dependingScores,
            (Score o1, Score o2) -> o1.getDependingScores().size() - o2.getDependingScores()
                .size());
        // Safely delete all depending scores
        for (Score scoreToDelete : dependingScores) {
            scoreDao.remove(scoreToDelete);
        }
        // Safely delete the score
        score = scoreDao.getElementById(scoreId);
        scoreDao.remove(score);
        Questionnaire questionnaire = score.getQuestionnaire();
        return showScoresForQuestionnaire(questionnaire.getId(), model);
    }

    /**
     * Create a {@link Expression} from a {@link ExpressionDTO} by creating the right
     * {@link Expression Expression} instance and setting the right {@link Operator Operator}
     * instance for the expressionDTO and its children.
     *
     * @param expressionDTO The {@link ExpressionDTO} which should be converted into a
     *                      {@link Expression}.
     * @return The converted {@link Expression}.
     */
    private Expression getExpressionFromExpressionDTO(final ExpressionDTO expressionDTO) {
        Expression expression = null;
        Operator operator = operatorDao.getElementById(expressionDTO.getOperatorId());
        List<Expression> expressions = new ArrayList<>();
        switch (operator.getDisplaySign()) {
            case ">":
            case "<":
            case ">=":
            case "<=":
            case "==":
            case "!=":
            case "-":
            case "+":
            case "/":
            case "*":
                expression = new BinaryExpression();
                ((BinaryExpression) expression).setOperator((BinaryOperator) operator);
                for (ExpressionDTO childExpressionDTO : expressionDTO.getExpressions()) {
                    Expression childExpression = getExpressionFromExpressionDTO(childExpressionDTO);
                    childExpression.setParent(expression);
                    expressions.add(childExpression);
                }
                ((BinaryExpression) expression).setExpressions(expressions);
                break;
            case "counter":
            case "average":
            case "sum":
                expression = new MultiExpression();
                ((MultiExpression) expression).setOperator((MultiOperator) operator);
                for (ExpressionDTO childExpressionDTO : expressionDTO.getExpressions()) {
                    Expression childExpression = getExpressionFromExpressionDTO(childExpressionDTO);
                    childExpression.setParent(expression);
                    expressions.add(childExpression);
                }
                ((MultiExpression) expression).setExpressions(expressions);
                break;
            case "valueOf":
                expression = new UnaryExpression();
                ((UnaryExpression) expression).setOperator((ValueOfQuestionOperator) operator);
                // If this expression contains a question, add it
                if (expressionDTO.getQuestionId() != null) {
                    Question question = questionDao.getElementById(expressionDTO.getQuestionId());
                    ((UnaryExpression) expression).setQuestion(question);
                }
                break;
            case "value":
                expression = new UnaryExpression();
                ((UnaryExpression) expression).setOperator((ValueOperator) operator);
                // If this expression contains a value, add it
                if (expressionDTO.getValue() != null) {
                    ((UnaryExpression) expression).setValue(
                        Double.valueOf(expressionDTO.getValue()));
                }
                break;
            default:
                break;
        }
        return expression;
    }
}
