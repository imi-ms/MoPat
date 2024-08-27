package de.imi.mopat.dao;

import de.imi.mopat.model.PredefinedSliderIcon;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public interface PredefinedSliderIconDao extends MoPatDao<PredefinedSliderIcon> {

    public List<String> getAllIcons();

    public PredefinedSliderIcon getIconByName(String iconName);
}
