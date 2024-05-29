package de.imi.mopat.dao.impl;

import de.imi.mopat.dao.QuestionDao;
import de.imi.mopat.model.Question;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 */
@Component
public class QuestionDaoImpl extends MoPatDaoImpl<Question> implements QuestionDao {

    @Override
    @Transactional("MoPat")
    public void remove(final Question element) {
        // because answer is on the owner side, we first have to remove the
        // answers from the question
        element.removeAllAnswers();
        super.remove(element);
    }
}
