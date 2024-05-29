package de.imi.mopat.dao;

import de.imi.mopat.model.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface SliderIconDao extends MoPatDao<SliderIcon> {

    /**
     * Returns all {@link SliderIcon} object for a given {@link Answer} object
     *
     * @param answer the answer to get sliderIcons for
     * @return
     */
    List<SliderIcon> getElementsForAnswer(Answer answer);

}
