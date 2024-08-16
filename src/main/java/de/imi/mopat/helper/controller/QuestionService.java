package de.imi.mopat.helper.controller;

import de.imi.mopat.dao.ConditionDao;
import de.imi.mopat.dao.QuestionDao;
import de.imi.mopat.model.Answer;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.conditions.Condition;
import de.imi.mopat.model.Questionnaire;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QuestionService {

    private static final org.slf4j.Logger LOGGER =
        org.slf4j.LoggerFactory.getLogger(QuestionService.class);

    @Autowired
    private QuestionDao questionDao;

    @Autowired
    private ConditionDao conditionDao;

    public void cloneConditions(Set<Question> originalQuestions, Map<Question, Map<Answer, Answer>> oldQuestionToNewAnswerMap, Map<Question, Question> questionMap){
        for(Question originalQuestion : originalQuestions){
            for(Answer answer : originalQuestion.getAnswers()){
                Set<Condition> conditions = answer.getConditions();
                Set<Condition> newConditions = new HashSet<>();
                for(Condition condition: conditions){
                    Condition newCondition;
                    if(condition.getTrigger().getClass() == answer.getClass() && condition.getTarget().getClass() == originalQuestion.getClass()){
                    newCondition = condition.cloneCondition(oldQuestionToNewAnswerMap.get(originalQuestion).get(answer), questionMap.get((Question) condition.getTarget()));
                    }
                    else if(condition.getTrigger().getClass() == answer.getClass() && condition.getTarget().getClass() == answer.getClass()){
                        Question taq = condition.getTargetAnswerQuestion();
                    newCondition = condition.cloneCondition(oldQuestionToNewAnswerMap.get(originalQuestion).get(answer), oldQuestionToNewAnswerMap.get(taq).get((Answer) condition.getTarget()));
                    }
                    else {
                        newCondition = null;
                    }
                newConditions.add(newCondition);
                conditionDao.merge(newCondition);
                }
            }
        }
    }

    Map<Question, Question> duplicateQuestionsToNewQuestionnaire(Set<Question> originalQuestions, Questionnaire newQuestionnaire) {
        Map<Question, Question> questionMap = new HashMap<>();
        Set<Question> copiedQuestions = new HashSet<>();
        Map<Question, Map<Answer, Answer>> oldQuestionToNewAnswerMap = new HashMap<>();
        for (Question originalQuestion : originalQuestions) {
            Question newQuestion = new Question(new HashMap<>(originalQuestion.getLocalizedQuestionText()),
                originalQuestion.getIsRequired(), originalQuestion.getIsEnabled(), originalQuestion.getQuestionType(), originalQuestion.getPosition(), newQuestionnaire);
            newQuestion.setMinMaxNumberAnswers(originalQuestion.getMinNumberAnswers(), originalQuestion.getMaxNumberAnswers());
            Map<Answer, Answer> answerMap = new HashMap<>();
            for (Answer answer : originalQuestion.getAnswers()) {
                Answer newAnswer = answer.cloneWithoutReferences();
                answerMap.put(answer, newAnswer);
                newQuestion.addAnswer(newAnswer);
            }
            newQuestion.setQuestionnaire(newQuestionnaire);
            questionMap.put(originalQuestion, newQuestion);
            oldQuestionToNewAnswerMap.put(originalQuestion, answerMap);
            copiedQuestions.add(newQuestion);
            questionDao.merge(newQuestion);
        }
            cloneConditions(originalQuestions, oldQuestionToNewAnswerMap, questionMap);

            newQuestionnaire.setQuestions(copiedQuestions);
        return questionMap;
    }
}
