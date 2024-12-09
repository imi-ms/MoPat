package de.imi.mopat.helper.model;

import de.imi.mopat.model.conditions.Condition;
import de.imi.mopat.model.conditions.SliderAnswerThresholdCondition;
import de.imi.mopat.model.dto.ConditionDTO;
import java.util.function.Function;
import org.springframework.stereotype.Component;

@Component
public class ConditionDTOMapper implements Function<Condition, ConditionDTO> {
    
    private static final org.slf4j.Logger LOGGER =
        org.slf4j.LoggerFactory.getLogger(ConditionDTOMapper.class);
    
    
    @Override
    public ConditionDTO apply(Condition condition) {
        ConditionDTO conditionDTO = new ConditionDTO();
        conditionDTO.setId(condition.getId());
        conditionDTO.setAction(condition.getAction().name());
        conditionDTO.setTargetClass(condition.getTargetClass());
        conditionDTO.setTargetId(condition.getTarget().getId());
        conditionDTO.setTriggerId(condition.getTrigger().getId());
        if (condition.getTargetAnswerQuestion() != null) {
            conditionDTO.setTargetAnswerQuestionId(condition.getTargetAnswerQuestion().getId());
        }
        if (condition.getBundle() != null) {
            conditionDTO.setBundleId(condition.getBundle().getId());
        } else {
            conditionDTO.setBundleId(null);
        }
        
        if (condition instanceof SliderAnswerThresholdCondition) {
            // If condition is a SliderAnswerThresholdCondition set the
            // appropriate values
            SliderAnswerThresholdCondition tresholdCondition = (SliderAnswerThresholdCondition) condition;
            conditionDTO.setThresholdType(tresholdCondition.getThresholdComparisonType());
            conditionDTO.setThresholdValue(tresholdCondition.getThreshold());
        }
        return conditionDTO;
    }

}
