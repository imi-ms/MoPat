package de.imi.mopat.model.dto;

import de.imi.mopat.dao.OperatorDao;
import de.imi.mopat.dao.QuestionDao;
import de.imi.mopat.dao.ScoreDao;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.score.BinaryExpression;
import de.imi.mopat.model.score.BinaryOperator;
import de.imi.mopat.model.score.Expression;
import de.imi.mopat.model.score.MultiExpression;
import de.imi.mopat.model.score.MultiOperator;
import de.imi.mopat.model.score.Operator;
import de.imi.mopat.model.score.Score;
import de.imi.mopat.model.score.UnaryExpression;
import de.imi.mopat.model.score.ValueOfQuestionOperator;
import de.imi.mopat.model.score.ValueOfScoreOperator;
import de.imi.mopat.model.score.ValueOperator;

import java.util.ArrayList;
import java.util.List;

public class ExpressionDTO {

    private List<ExpressionDTO> expressions = new ArrayList<>();
    private Long operatorId;
    private Long questionId;
    private Long scoreId;
    private String value;

    public ExpressionDTO() {
    }

    public List<ExpressionDTO> getExpressions() {
        return expressions;
    }

    public void setExpressions(final List<ExpressionDTO> expressions) {
        this.expressions = expressions;
    }

    public void addExpressions(final ExpressionDTO expression) {
        this.expressions.add(expression);
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(final Long operatorId) {
        this.operatorId = operatorId;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(final Long questionId) {
        this.questionId = questionId;
    }

    public Long getScoreId() {
        return scoreId;
    }

    public void setScoreId(final Long scoreId) {
        this.scoreId = scoreId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    /**
     * Create a {@link Expression} from this {@link ExpressionDTO} by creating the right
     * {@link Expression Expression} instance and setting the right {@link Operator Operator}
     * instance for the expressionDTO and its children.
     *
     * @param operatorDao The {@link OperatorDao}
     * @param questionDao The {@link QuestionDao}
     * @param scoreDao    The {@link ScoreDao}
     * @return The converted {@link Expression}.
     */
    public Expression toExpression(final OperatorDao operatorDao, final QuestionDao questionDao,
        final ScoreDao scoreDao) {
        Expression expression = null;
        Operator operator = operatorDao.getElementById(this.getOperatorId());
        List<Expression> expressions;
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
                expressions = new ArrayList<>();
                ((BinaryExpression) expression).setOperator((BinaryOperator) operator);
                for (ExpressionDTO childExpressionDTO : this.getExpressions()) {
                    Expression childExpression = childExpressionDTO.toExpression(operatorDao,
                        questionDao, scoreDao);
                    childExpression.setParent(expression);
                    expressions.add(childExpression);
                }
                ((BinaryExpression) expression).setExpressions(expressions);
                break;
            case "maximum":
            case "minimum":
            case "counter":
            case "average":
            case "sum":
                expression = new MultiExpression();
                expressions = new ArrayList<>();
                ((MultiExpression) expression).setOperator((MultiOperator) operator);
                for (ExpressionDTO childExpressionDTO : this.getExpressions()) {
                    Expression childExpression = childExpressionDTO.toExpression(operatorDao,
                        questionDao, scoreDao);
                    childExpression.setParent(expression);
                    expressions.add(childExpression);
                }
                ((MultiExpression) expression).setExpressions(expressions);
                break;
            case "valueOf":
                expression = new UnaryExpression();
                ((UnaryExpression) expression).setOperator((ValueOfQuestionOperator) operator);
                // If this expression contains a question, add it
                if (this.getQuestionId() != null) {
                    Question question = questionDao.getElementById(this.getQuestionId());
                    ((UnaryExpression) expression).setQuestion(question);
                }
                break;
            case "value":
                expression = new UnaryExpression();
                ((UnaryExpression) expression).setOperator((ValueOperator) operator);
                // If this expression contains a value, add it
                if (this.getValue() != null) {
                    ((UnaryExpression) expression).setValue(Double.valueOf(this.getValue()));
                }
                break;
            case "valueOfScore":
                expression = new UnaryExpression();
                ((UnaryExpression) expression).setOperator((ValueOfScoreOperator) operator);
                // If this expression contains a score, add it
                if (this.getScoreId() != null) {
                    Score score = scoreDao.getElementById(this.getScoreId());
                    ((UnaryExpression) expression).setScore(score);
                }
                break;
            default:
                break;
        }
        return expression;
    }
}
