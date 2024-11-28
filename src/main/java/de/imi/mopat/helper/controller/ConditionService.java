package de.imi.mopat.helper.controller;

import de.imi.mopat.dao.AnswerDao;
import de.imi.mopat.dao.BundleDao;
import de.imi.mopat.dao.QuestionDao;
import de.imi.mopat.dao.QuestionnaireDao;
import de.imi.mopat.model.Answer;
import de.imi.mopat.model.Bundle;
import de.imi.mopat.model.NumberInputAnswer;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.SelectAnswer;
import de.imi.mopat.model.SliderAnswer;
import de.imi.mopat.model.SliderFreetextAnswer;
import de.imi.mopat.model.conditions.ConditionActionType;
import de.imi.mopat.model.conditions.SelectAnswerCondition;
import de.imi.mopat.model.conditions.SliderAnswerThresholdCondition;
import de.imi.mopat.model.dto.ConditionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConditionService {
    
    @Autowired
    private QuestionnaireDao questionnaireDao;
    
    @Autowired
    private BundleDao bundleDao;
    
    @Autowired
    private QuestionDao questionDao;
    
    @Autowired
    private AnswerDao answerDao;

    public void mergeCondition(ConditionDTO conditionDTO) {
        Answer targetAnswer = null;
        Question targetQuestion = null;
        Questionnaire targetQuestionnaire = null;
        Bundle bundle = null;
        
        //Check the target class and
        //get the appropriate objects that are necessary for merging the
        // condition into the database
        if (conditionDTO.getBundleId() != null && conditionDTO.getTargetClass()
            .equalsIgnoreCase("de.imi.mopat.model.Questionnaire")) {
            targetQuestionnaire = questionnaireDao.getElementById(conditionDTO.getTargetId());
            bundle = bundleDao.getElementById(conditionDTO.getBundleId());
            conditionDTO.setTargetAnswerQuestionId(null);
        } else {
            if (conditionDTO.getTargetClass()
                .equalsIgnoreCase("de.imi.mopat.model" + ".Question")) {
                targetQuestion = questionDao.getElementById(conditionDTO.getTargetId());
                conditionDTO.setTargetAnswerQuestionId(null);
            } else if (conditionDTO.getTargetClass()
                .equalsIgnoreCase("de.imi.mopat" + ".model" + ".SelectAnswer")) {
                targetAnswer = answerDao.getElementById(conditionDTO.getTargetId());
            }
            
            if (conditionDTO.getBundleId() != null) {
                //If the condition's targetClass was questionnaire before
                // and
                //the targetClass has changed reset bundleId
                conditionDTO.setBundleId(null);
            }
        }
        
        Answer triggerAnswer = answerDao.getElementById(conditionDTO.getTriggerId());
        
        ConditionActionType action;
        if (conditionDTO.getAction().equalsIgnoreCase("disable")) {
            action = ConditionActionType.DISABLE;
        } else {
            action = ConditionActionType.ENABLE;
        }
        // Add a new condition and call constructor depending on the
        // condition's type
        // and the target's type which the condition is aiming at
        if (targetQuestion != null) {
            if (triggerAnswer instanceof SelectAnswer) {
                new SelectAnswerCondition(triggerAnswer, targetQuestion, action,
                    null);
            } else if (triggerAnswer instanceof SliderAnswer) {
                new SliderAnswerThresholdCondition(triggerAnswer, targetQuestion,
                    action, null, conditionDTO.getThresholdType(),
                    conditionDTO.getThresholdValue());
            } else if (triggerAnswer instanceof SliderFreetextAnswer) {
                new SliderAnswerThresholdCondition(triggerAnswer,
                    targetQuestion, action, null, conditionDTO.getThresholdType(),
                    conditionDTO.getThresholdValue());
            } else if (triggerAnswer instanceof NumberInputAnswer) {
                new SliderAnswerThresholdCondition(triggerAnswer,
                    targetQuestion, action, null, conditionDTO.getThresholdType(),
                    conditionDTO.getThresholdValue());
            }
        } else if (targetQuestionnaire != null) {
            if (triggerAnswer instanceof SelectAnswer) {
                new SelectAnswerCondition(triggerAnswer, targetQuestionnaire,
                    action, bundle);
            } else if (triggerAnswer instanceof SliderAnswer) {
                new SliderAnswerThresholdCondition(triggerAnswer,
                    targetQuestionnaire, action, bundle, conditionDTO.getThresholdType(),
                    conditionDTO.getThresholdValue());
            } else if (triggerAnswer instanceof SliderFreetextAnswer) {
                new SliderAnswerThresholdCondition(triggerAnswer,
                    targetQuestionnaire, action, bundle, conditionDTO.getThresholdType(),
                    conditionDTO.getThresholdValue());
            } else if (triggerAnswer instanceof NumberInputAnswer) {
                new SliderAnswerThresholdCondition(triggerAnswer,
                    targetQuestionnaire, action, bundle, conditionDTO.getThresholdType(),
                    conditionDTO.getThresholdValue());
            }
        } else if (targetAnswer != null) {
            if (triggerAnswer instanceof SelectAnswer) {
                new SelectAnswerCondition(triggerAnswer, targetAnswer, action,
                    null);
            } else if (triggerAnswer instanceof SliderAnswer) {
                new SliderAnswerThresholdCondition(triggerAnswer, targetAnswer,
                    action, null, conditionDTO.getThresholdType(),
                    conditionDTO.getThresholdValue());
            } else if (triggerAnswer instanceof SliderFreetextAnswer) {
                new SliderAnswerThresholdCondition(triggerAnswer,
                    targetAnswer, action, null, conditionDTO.getThresholdType(),
                    conditionDTO.getThresholdValue());
            } else if (triggerAnswer instanceof NumberInputAnswer) {
                new SliderAnswerThresholdCondition(triggerAnswer,
                    targetAnswer, action, null, conditionDTO.getThresholdType(),
                    conditionDTO.getThresholdValue());
            }
        }
        // [bt] Condition does not need to be added to the triggering
        // answer,
        // since the constructor of Condition already takes care of this
        answerDao.merge(triggerAnswer);
    }

}
