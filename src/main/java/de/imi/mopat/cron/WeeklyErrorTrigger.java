package de.imi.mopat.cron;

import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Weekly creation of a dummy error log entry to trigger the weekly report of errors
 */
@Service
public class WeeklyErrorTrigger {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(
        WeeklyErrorTrigger.class);

    /* [bt] notice, as told in the Spring documentation (http://docs.spring
    .io/spring/docs/3.0.x/api/org/springframework/scheduling/annotation
    /Scheduled): A cron-like expression, extending the usual UN*X definition
    to include triggers on the second */
    @Scheduled(cron = "${de.imi.mopat.cron.WeeklyErrorTrigger.trigger}")
    public void refresh() {
        Marker weeklyMailMarker = MarkerFactory.getMarker("WEEKLY_EMAIL");
        LOGGER.error(weeklyMailMarker,
            "This is a weekly dummy error log entry to trigger the weekly"
                + " error reporting. If you can only see this, "
                + "everything's running smoothly :)");
    }
}
