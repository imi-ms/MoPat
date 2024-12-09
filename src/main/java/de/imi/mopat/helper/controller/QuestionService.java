package de.imi.mopat.helper.controller;

import de.imi.mopat.dao.AnswerDao;
import de.imi.mopat.dao.BundleDao;
import de.imi.mopat.dao.QuestionnaireDao;
import de.imi.mopat.model.Answer;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.conditions.Condition;
import de.imi.mopat.model.Questionnaire;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QuestionService {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(
            QuestionService.class);
    
    @Autowired
    private AnswerDao answerDao;
    
    @Autowired
    private QuestionnaireDao questionnaireDao;
    
    @Autowired
    private BundleDao bundleDao;

    /**
     * Clones conditions from a set of original questions to corresponding new questions and answers
     * based on provided mappings. This method iterates through each question and its answers in the
     * original set, then duplicates each condition attached to those answers. The method properly
     * assigns the cloned conditions to the new corresponding answers and questions, according to
     * the mappings specified.
     *
     * @param oldQuestionToNewAnswerMap A mapping from original {@link Question} objects to another
     *                                  map, which further maps original {@link Answer} objects to
     *                                  their corresponding new {@link Answer} objects. This is used
     *                                  to find the new answer instances where conditions should
     *                                  point.
     * @param questionMap               A mapping from original {@link Question} objects to new
     *                                  {@link Question} objects. This is used to update the
     *                                  question targets of cloned conditions.
     */
    public Set<Condition> cloneConditions(
        Map<Question, Map<Answer, Answer>> oldQuestionToNewAnswerMap,
        Map<Question, Question> questionMap) {
        Set<Condition> newConditions = new HashSet<>();
        for (Question originalQuestion : questionMap.keySet()) {
            for (Answer answer : originalQuestion.getAnswers()) {
                Set<Condition> conditions = answer.getConditions();
                for (Condition condition : conditions) {
                    Condition newCondition = null;
                    if (condition.getTrigger().getClass() == answer.getClass()
                        && condition.getTarget().getClass() == originalQuestion.getClass()) {
                        newCondition = condition.cloneCondition(
                            oldQuestionToNewAnswerMap.get(originalQuestion).get(answer),
                            questionMap.get((Question) condition.getTarget()));
                    } else if (condition.getTrigger().getClass() == answer.getClass()
                        && condition.getTarget().getClass() == answer.getClass()) {
                        Question taq = condition.getTargetAnswerQuestion();
                        newCondition = condition.cloneCondition(
                            oldQuestionToNewAnswerMap.get(originalQuestion).get(answer),
                            oldQuestionToNewAnswerMap.get(taq).get((Answer) condition.getTarget()));
                    }
                    if (newCondition != null) {
                        newConditions.add(newCondition);
                    }
                }
            }
        }

        return newConditions;
    }

    Questionnaire duplicateQuestionsToNewQuestionnaire(
        Set<Question> originalQuestions,
        Questionnaire newQuestionnaire
    ) {
        Set<Question> copiedQuestions = new HashSet<>();
        
        for (Question originalQuestion : originalQuestions) {
            Question newQuestion = new Question(
                new HashMap<>(originalQuestion.getLocalizedQuestionText()),
                originalQuestion.getIsRequired(), originalQuestion.getIsEnabled(),
                originalQuestion.getQuestionType(), originalQuestion.getPosition(),
                newQuestionnaire
            );
            
            newQuestion.setMinMaxNumberAnswers(
                originalQuestion.getMinNumberAnswers(),
                originalQuestion.getMaxNumberAnswers()
            
            );
            
            for (Answer answer : originalQuestion.getAnswers()) {
                newQuestion.addAnswer(answer.cloneWithoutReferences());
            }
            
            newQuestion.setQuestionnaire(newQuestionnaire);
            copiedQuestions.add(newQuestion);
        }
        newQuestionnaire.setQuestions(copiedQuestions);
        questionnaireDao.merge(newQuestionnaire);
        //Refetch questionnaire to get ids from db
        return questionnaireDao.getElementById(newQuestionnaire.getId());
    }
    
    MapHolder getMappingForDuplicatedQuestions(
        Questionnaire originalQuestionnnaire,
        Questionnaire newQuestionnaire
    ) {
        Map<Question, Question> questionMap = new HashMap<>();
        Map<Question, Map<Answer, Answer>> oldQuestionToNewAnswerMap = new HashMap<>();
        
        
        for (int i = 0; i < originalQuestionnnaire.getQuestions().size(); i++) {
            
            Question originalQuestion = new ArrayList<>(originalQuestionnnaire.getQuestions()).get(i);
            Question newQuestion = new ArrayList<>(newQuestionnaire.getQuestions()).get(i);
            
            if (originalQuestion != null && newQuestion != null) {
                questionMap.put(originalQuestion, newQuestion);
                
                Map<Answer, Answer> answerMap = new HashMap<>();
                
                for (int j = 0; j < originalQuestion.getAnswers().size(); j++) {
                    Answer originalAnswer = originalQuestion.getAnswers().get(j);
                    Answer newAnswer = newQuestion.getAnswers().get(j);
                    answerMap.put(originalAnswer, newAnswer);
                }
                
                oldQuestionToNewAnswerMap.put(originalQuestion, answerMap);
            }
        }
        return new MapHolder(questionMap, oldQuestionToNewAnswerMap);
    }
    
}
