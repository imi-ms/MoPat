package de.imi.mopat.dao.impl;

import de.imi.mopat.dao.PredefinedSliderIconDao;
import de.imi.mopat.dao.SliderIconConfigDao;
import de.imi.mopat.model.PredefinedSliderIcon;
import de.imi.mopat.model.SliderIconConfig;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SliderIconConfigDaoImpl extends MoPatDaoImpl<SliderIconConfig> implements SliderIconConfigDao {

    @Override
    public SliderIconConfig getElementByName(String name){
        SliderIconConfig sliderIconConfig;

        try {
            Query query = moPatEntityManager.createQuery(
                "SELECT c FROM SliderIconConfig c WHERE c.configName = :name", Long.class);
            query.setParameter("name", name);
            sliderIconConfig = (SliderIconConfig) query.getSingleResult();
        } catch (Exception e) {
            return null;
        }
        return sliderIconConfig;
    }
}
