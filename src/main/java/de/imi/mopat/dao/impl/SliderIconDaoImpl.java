package de.imi.mopat.dao.impl;

import de.imi.mopat.dao.SliderIconDao;
import de.imi.mopat.model.Answer;
import de.imi.mopat.model.SliderIcon;
import org.springframework.stereotype.Component;

import jakarta.persistence.Query;
import java.util.ArrayList;
import java.util.List;

@Component
public class SliderIconDaoImpl extends MoPatDaoImpl<SliderIcon> implements SliderIconDao {

    @Override
    public List<SliderIcon> getElementsForAnswer(final Answer answer) {
        List<SliderIcon> resultSliderIconList = new ArrayList<>();

        try {
            Query query = moPatEntityManager.createQuery(
                "SELECT c FROM SliderIcon c WHERE c.answer.id = :answerId", Long.class);
            query.setParameter("answerId", answer.getId());
            resultSliderIconList = query.getResultList();
        } catch (Exception e) {
            return resultSliderIconList;
        }
        return resultSliderIconList;
    }
}
