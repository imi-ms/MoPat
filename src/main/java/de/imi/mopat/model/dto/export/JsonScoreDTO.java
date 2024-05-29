package de.imi.mopat.model.dto.export;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.dto.ExpressionDTO;
import de.imi.mopat.model.score.BinaryExpression;
import de.imi.mopat.model.score.BinaryOperator;
import de.imi.mopat.model.score.Expression;
import de.imi.mopat.model.score.MultiExpression;
import de.imi.mopat.model.score.MultiOperator;
import de.imi.mopat.model.score.Operator;
import de.imi.mopat.model.score.Score;
import de.imi.mopat.model.score.UnaryExpression;
import de.imi.mopat.model.score.UnaryOperator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class represents the data transfer obejct of model {@link Score} to convert a model to json
 * for import and export.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonTypeName("score")
public class JsonScoreDTO {

    private Long id = null;
    private String name = null;
    private Long questionnaireId;
    private ExpressionDTO expression;

    public JsonScoreDTO() {
    }

    public JsonScoreDTO(final Score score) {
        this.setId(score.getId());

        if (score.getExpression() != null) {
            this.setExpression(score.getExpression().toExpressionDTO());
        }

        this.setName(score.getName());
        this.setQuestionnaireId(score.getQuestionnaire().getId());
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Long getQuestionnaireId() {
        return questionnaireId;
    }

    public void setQuestionnaireId(final Long questionnaireId) {
        this.questionnaireId = questionnaireId;
    }

    public ExpressionDTO getExpression() {
        return expression;
    }

    public void setExpression(final ExpressionDTO expression) {
        this.expression = expression;
    }

    /**
     * Convert instance of this class to {@link Score} object.
     *
     * @param operators          Map containing all {@link Operator operators } as values related to
     *                           its ids as key.
     * @param questions          Map containing all {@link Question questions } of this score's
     *                           {Questionnaire} as values related to its ids as key.
     * @param scoreIdExpressions Map containing all {@link Score scores} of this score's
     *                           {Questionnaire} as values related to its ids as key.
     * @return This object converted to instance of Score model.
     */
    public Score convertToScore(final Map<Long, Operator> operators,
        final Map<Long, Question> questions, final Map<Long, UnaryExpression> scoreIdExpressions) {
        Score score = new Score();
        score.setExpression(this.getExpressions(this.getExpression(), null, operators, questions,
            scoreIdExpressions));
        score.setName(this.getName());
        return score;
    }

    /**
     * Collect all {@link ExpressionDTO expressionDTOs} of a {@link ExpressionDTO} by iterating
     * recursively through the expressionDTO tree and convert them to {@link Expression} objects.
     *
     * @param expressionDTO Object containing the expressions to collect.
     * @param parent        Object the current expression belongs to.
     * @param operators     Map containing all {@link Operator operators} as values related to its
     *                      ids as key.
     * @return ExpressionDTO instance converted to Expression model.
     */
    private Expression getExpressions(final ExpressionDTO expressionDTO, final Expression parent,
        final Map<Long, Operator> operators, final Map<Long, Question> questions,
        final Map<Long, UnaryExpression> scoreIdExpressions) {
        List<Expression> expressions = new ArrayList<>();
        //Get expression type by operator type
        switch (expressionDTO.getOperatorId().intValue()) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
                BinaryExpression binaryExpression = new BinaryExpression();
                binaryExpression.setOperator(
                    (BinaryOperator) operators.get(expressionDTO.getOperatorId()));
                //walk through all sub expressions
                for (ExpressionDTO currentExpressionDTO : expressionDTO.getExpressions()) {
                    expressions.add(
                        this.getExpressions(currentExpressionDTO, binaryExpression, operators,
                            questions, scoreIdExpressions));
                }
                binaryExpression.setExpressions(expressions);
                binaryExpression.setParent(parent);
                return binaryExpression;
            case 5:
            case 6:
            case 16:
                //recursion anchor, a unary expression servers as a leaf of
                // the expression tree
                UnaryExpression unaryExpression = new UnaryExpression();
                unaryExpression.setOperator(
                    (UnaryOperator) operators.get(expressionDTO.getOperatorId()));
                unaryExpression.setParent(parent);

                if (expressionDTO.getQuestionId() != null) {
                    unaryExpression.setQuestion(questions.get(expressionDTO.getQuestionId()));
                } else if (expressionDTO.getScoreId() != null) {
                    scoreIdExpressions.put(expressionDTO.getScoreId(), unaryExpression);
                } else if (expressionDTO.getValue() != null) {
                    unaryExpression.setValue(Double.parseDouble(expressionDTO.getValue()));
                }

                return unaryExpression;
            case 7:
            case 14:
            case 15:
            case 17:
            case 18:
                MultiExpression multiExpression = new MultiExpression();
                multiExpression.setOperator(
                    (MultiOperator) operators.get(expressionDTO.getOperatorId()));
                //walk through all sub expressions
                for (ExpressionDTO currentExpressionDTO : expressionDTO.getExpressions()) {
                    expressions.add(
                        this.getExpressions(currentExpressionDTO, multiExpression, operators,
                            questions, scoreIdExpressions));
                }
                multiExpression.setExpressions(expressions);
                multiExpression.setParent(parent);
                return multiExpression;
        }
        return null;
    }
}
