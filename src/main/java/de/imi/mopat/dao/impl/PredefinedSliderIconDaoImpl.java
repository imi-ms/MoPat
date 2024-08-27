package de.imi.mopat.dao.impl;

import de.imi.mopat.dao.PredefinedSliderIconDao;
import de.imi.mopat.model.PredefinedSliderIcon;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class PredefinedSliderIconDaoImpl extends MoPatDaoImpl<PredefinedSliderIcon> implements PredefinedSliderIconDao {

    @Override
    public List<String> getAllIcons(){
        List<String> icons= new ArrayList<>();

        for(PredefinedSliderIcon icon: this.getAllElements()){
            icons.add(icon.getIconName());
        }
        return  icons;
    }

    @Override
    public PredefinedSliderIcon getIconByName(String iconName) {
        try {
            TypedQuery<PredefinedSliderIcon> query = moPatEntityManager.createQuery(
                "SELECT e FROM PredefinedSliderIcon e WHERE e.iconName = :iconName", PredefinedSliderIcon.class);
            query.setParameter("iconName", iconName);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

}
