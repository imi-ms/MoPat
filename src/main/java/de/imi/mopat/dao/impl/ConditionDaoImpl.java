package de.imi.mopat.dao.impl;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import de.imi.mopat.dao.ConditionDao;
import de.imi.mopat.model.Answer;
import de.imi.mopat.model.Bundle;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.SelectAnswer;
import de.imi.mopat.model.conditions.Condition;
import de.imi.mopat.model.conditions.SliderAnswerThresholdCondition;
import de.imi.mopat.model.conditions.ThresholdComparisonType;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.Query;

/**
 *
 */
@Component
public class ConditionDaoImpl extends MoPatDaoImpl<Condition> implements ConditionDao {

    @Override
    public boolean isConditionTarget(final Object target) {
        if (target instanceof Bundle) {
            Query query = moPatEntityManager.createQuery(
                "SELECT count(c) " + "FROM " + "Condition " + "c" + " where c" + ".bundle.id" + " "
                    + "= :bundleId");
            query.setParameter("bundleId", ((Bundle) target).getId());
            Long result = (Long) query.getSingleResult();
            return result > 0;
        } else if (target instanceof Questionnaire) {
            List<Condition> conditionList;
            // Get a list of all conditions
            Query query = moPatEntityManager.createQuery("SELECT c FROM Condition c",
                Condition.class);
            conditionList = query.getResultList();
            // Check if one condition is associated to the given questionnaire
            for (Condition condition : conditionList) {
                if (condition.getTarget() instanceof Questionnaire && condition.getTarget().getId()
                    .equals(((Questionnaire) target).getId())) {
                    return true;
                }
            }
        } else if (target instanceof Question) {
            // Get all conditions where either the question is a target or
            // any of this questions answers
            Query queryQuestion = moPatEntityManager.createNativeQuery(
                "SELECT count(*) FROM mopat_condition WHERE " + "target_class"
                    + " LIKE 'Question' AND target_id " + "= " + ((Question) target).getId());
            Query queryAnswers = moPatEntityManager.createNativeQuery(
                "SELECT count(*) FROM mopat_condition WHERE " + "target_class"
                    + " LIKE 'SelectAnswer' AND " + "target_answer_question_id = "
                    + ((Question) target).getId());
            Long result = (Long) queryQuestion.getSingleResult();
            result += (Long) queryAnswers.getSingleResult();
            // Return true if at least one condition is associated to this
            // question
            return result > 0;
        } else if (target instanceof Answer) {
            // Get all condtions where the given answer is the target
            Query query = moPatEntityManager.createNativeQuery("SELECT count" + "(*) " + "FROM"
                + " mopat_condition WHERE target_class LIKE 'SelectAnswer' AND target_id = "
                + ((Answer) target).getId());
            Long result = (Long) query.getSingleResult();
            // Return true if at least one condition is associated to this
            // question
            return result > 0;
        }
        // Otherwise return false
        return false;
    }

    @Override
    public Set<Long> findConditionTargetIds(List<Long> ids, String type) {
        List<BigInteger> results = moPatEntityManager.createNativeQuery(
                "SELECT target_id FROM mopat_condition WHERE target_class = '" + type + "' AND target_id IN (?1)")
            .setParameter(1, ids)
            .getResultList();
        return results.stream().map(BigInteger::longValue).collect(Collectors.toSet());
    }

    @Override
    public List<Condition> getConditionsByTarget(final Object target) {
        List<Condition> resultConditionList = new ArrayList<>();

        if (target instanceof Bundle) {
            try {
                Query query = moPatEntityManager.createQuery(
                    "SELECT c FROM Condition c WHERE c.bundle.id = " + ":bundleId", Long.class);
                query.setParameter("bundleId", ((Bundle) target).getId());
                resultConditionList = query.getResultList();
            } catch (Exception e) {
                return resultConditionList;
            }
        } else if (target instanceof Questionnaire) {
            try {
                // Get a list of all conditions
                Query query = moPatEntityManager.createQuery("SELECT c FROM Condition c",
                    Condition.class);
                List<Condition> conditionList = query.getResultList();
                // Filter the conditions associated to the given questionnaire
                for (Condition condition : conditionList) {
                    if (condition.getTarget() instanceof Questionnaire && condition.getTarget()
                        .getId().equals(((Questionnaire) target).getId())) {
                        resultConditionList.add(condition);
                    }
                }
            } catch (Exception e) {
                return resultConditionList;
            }
        } else if (target instanceof Question) {
            try {
                // Get all conditions where either the question is a target
                // or any of this questions answers
                Query queryQuestion = moPatEntityManager.createNativeQuery(
                    "SELECT * FROM mopat_condition WHERE target_class "
                        + "LIKE 'Question' AND target_id = " + ((Question) target).getId(),
                    Condition.class);
                Query queryAnswers = moPatEntityManager.createNativeQuery(
                    "SELECT * FROM mopat_condition WHERE target_class " + "LIKE 'SelectAnswer' AND "
                        + "target_answer_question_id = " + ((Question) target).getId(),
                    Condition.class);

                resultConditionList = queryQuestion.getResultList();
                resultConditionList.addAll(queryAnswers.getResultList());
            } catch (Exception e) {
                return resultConditionList;
            }
        } else if (target instanceof Answer) {
            try {
                // Get all condtions where the given answer is the target
                Query query = moPatEntityManager.createNativeQuery(
                    "SELECT * FROM mopat_condition WHERE target_class "
                        + "LIKE 'SelectAnswer' AND target_id = " + ((Answer) target).getId(),
                    Condition.class);
                resultConditionList = query.getResultList();

            } catch (Exception e) {
                return resultConditionList;
            }
        }
        return resultConditionList;
    }

    @Override
    public List<Condition> getConditionsByTriggerCondition(final Condition condition) {
        List<Condition> resultConditionList = new ArrayList<>();

        if (condition != null) {
            // Check the type of the condition
            if (condition instanceof SliderAnswerThresholdCondition) {
                // Add the needed attributes for threshold conditions
                Query query = moPatEntityManager.createNativeQuery(
                    "SELECT * FROM mopat_condition WHERE trigger_id = " + condition.getTrigger()
                        .getId() + " AND threshold = "
                        + ((SliderAnswerThresholdCondition) condition).getThreshold()
                        + " AND threshold_comparison_type = '"
                        + ((SliderAnswerThresholdCondition) condition).getThresholdComparisonType()
                        .name() + "'", Condition.class);
                resultConditionList = query.getResultList();
            } else {
                // Add the needed attributes for select conditions
                Query query = moPatEntityManager.createNativeQuery(
                    "SELECT * FROM mopat_condition WHERE trigger_id = " + condition.getTrigger()
                        .getId(), Condition.class);
                resultConditionList = query.getResultList();
            }
        }
        return resultConditionList;
    }

    @Override
    public List<Condition> getConditionsByTriggerAnswer(final Answer triggerAnswer,
        final Double thresholdValue, final ThresholdComparisonType thresholdComparisonType) {
        List<Condition> resultConditionList = new ArrayList<>();

        if (triggerAnswer != null) {
            if (triggerAnswer instanceof SelectAnswer) {
                Query query = moPatEntityManager.createNativeQuery(
                    "SELECT * FROM mopat_condition WHERE trigger_id = " + triggerAnswer.getId(),
                    Condition.class);
                resultConditionList = query.getResultList();
            } else if (thresholdValue != null && thresholdComparisonType != null) {
                Query query = moPatEntityManager.createNativeQuery(
                    "SELECT * FROM mopat_condition WHERE trigger_id = " + triggerAnswer.getId()
                        + " AND threshold = " + thresholdValue
                        + " AND threshold_comparison_type = '" + thresholdComparisonType.name()
                        + "'", Condition.class);
                resultConditionList = query.getResultList();
            }
        }

        return resultConditionList;
    }
}
