package de.imi.mopat.service.helper;

import de.imi.mopat.model.dto.EncounterScheduledDTO;
import org.springframework.stereotype.Component;

@Component
public class SetRepeatConfiguration {

    // in case of REPEATEDLY, RepeatPeriod is already set in dto
    public void apply(EncounterScheduledDTO dto) {
        int WEEKLY_PERIOD_DAYS = 7;
        int MONTHLY_PERIOD_DAYS = 30;
        switch (dto.getEncounterScheduledSerialType()) {
            case UNIQUELY -> {
                dto.setRepeatPeriod(null);
                dto.setEndDate(null);
                break;
            }
            case WEEKLY -> {
                dto.setRepeatPeriod(WEEKLY_PERIOD_DAYS);
                break;
            }
            case MONTHLY -> {
                dto.setRepeatPeriod(MONTHLY_PERIOD_DAYS);
                break;
            }
            default -> {
                break;
            }
        }
    }
}