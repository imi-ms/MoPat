package de.imi.mopat.dao.impl;

import de.imi.mopat.dao.ExportTemplateDao;
import de.imi.mopat.dao.QuestionnaireDao;
import de.imi.mopat.dao.ScoreDao;
import de.imi.mopat.model.ExportRuleScore;
import de.imi.mopat.model.ExportTemplate;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.score.Expression;
import de.imi.mopat.model.score.Score;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 */
@Component
public class ScoreDaoImpl extends MoPatDaoImpl<Score> implements ScoreDao {

    @Autowired
    private ExportTemplateDao exportTemplateDao;
    @Autowired
    private QuestionnaireDao questionnaireDao;

    @Override
    public boolean hasScore(final Question question) {
        Query query = moPatEntityManager.createQuery(
            "SELECT count(e) FROM " + "UnaryExpression e " + "WHERE e.question.id "
                + "= :questionId");
        query.setParameter("questionId", question.getId());
        Long result = (Long) query.getSingleResult();
        return result > 0;
    }

    @Override
    public boolean hasScore(final Questionnaire questionnaire) {
        Query query = moPatEntityManager.createQuery(
            "SELECT count(e) FROM " + "Score e WHERE " + "e" + ".questionnaire.id " + "= "
                + ":questionnaireId");
        query.setParameter("questionnaireId", questionnaire.getId());
        Long result = (Long) query.getSingleResult();
        return result > 0;
    }

    @Override
    public Set<Long> findQuestionnairesWithScores(List<Long> questionnaireIds) {
        Query query = moPatEntityManager.createQuery(
            "SELECT DISTINCT e.questionnaire.id FROM Score e WHERE e.questionnaire.id IN :questionnaireIds",
            Long.class);
        query.setParameter("questionnaireIds", questionnaireIds);
        List<Long> result = query.getResultList();
        return new HashSet<>(result);
    }

    @Override
    public List<Score> getScores(final Question question) {
        List<Score> scoreList = new ArrayList<>();
        try {
            TypedQuery expressionQuery = moPatEntityManager.createQuery(
                "SELECT e FROM UnaryExpression e WHERE e.question.id = " + ":questionId",
                Expression.class);
            expressionQuery.setParameter("questionId", question.getId());
            List<Expression> resultExpressionList = expressionQuery.getResultList();
            Set<Long> rootExpressionIds = new HashSet<>();
            // Get the root expression for every expression associated to the
            // question and save the id
            for (Expression expression : resultExpressionList) {
                while (expression.getParent() != null) {
                    expression = expression.getParent();
                }
                rootExpressionIds.add(expression.getId());
            }
            if (!rootExpressionIds.isEmpty()) {
                // Get all scores associated with these root expressions
                TypedQuery<Score> scoreQuery = moPatEntityManager.createQuery(
                    "SELECT s FROM Score s WHERE s.expression.id IN " + ":expressionIds",
                    Score.class);
                scoreQuery.setParameter("expressionIds", rootExpressionIds);
                scoreList = scoreQuery.getResultList();
            }
            return scoreList;
        } catch (Exception e) {
            return scoreList;
        }
    }

    /**
     * Method to safely remove a {@link Score}.
     *
     * @param score The {@link Score} object, which should be removed
     */
    @Override
    @Transactional("MoPat")
    public void remove(final Score score) {
        Questionnaire questionnaire = score.getQuestionnaire();
        questionnaire.removeScore(score);
        // Delete all export rules for scores from the according export template
        for (ExportRuleScore exportRule : score.getExportRules()) {
            ExportTemplate exportTemplate = exportRule.getExportTemplate();
            exportTemplate.removeExportRule(exportRule);
            exportTemplateDao.merge(exportTemplate);
        }
        moPatEntityManager.remove(moPatEntityManager.merge(score));
        questionnaireDao.merge(questionnaire);
    }
}
