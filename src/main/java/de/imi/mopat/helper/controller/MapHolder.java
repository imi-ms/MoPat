package de.imi.mopat.helper.controller;

import de.imi.mopat.model.Answer;
import de.imi.mopat.model.Question;
import java.util.Map;

public record MapHolder(Map<Question, Question> questionMap, Map<Question, Map<Answer, Answer>> oldQuestionToNewAnswerMap) {

}
