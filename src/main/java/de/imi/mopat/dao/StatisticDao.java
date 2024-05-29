package de.imi.mopat.dao;

import de.imi.mopat.model.Statistic;
import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Interface for the data access for objects of type {@link Statistic}.
 * <p>
 * Provides specific methods for the objects of type {@link Statistic}.
 */
@Component
public interface StatisticDao extends MoPatDao<Statistic> {

    /**
     * Returns the earliest date for available {@link Statistic statistics}.
     *
     * @return The earliest date for available {@link Statistic statistics}.
     */
    Date getEarliestDate();

    /**
     * Returns the latest date for available {@link Statistic statistics}.
     *
     * @return The latest date for available {@link Statistic statistics}.
     */
    Date getLatestDate();

    /**
     * Returns a list of {@link Statistic statistics} for given dates.
     *
     * @param dates The dates of the expected {@link Statistic statistics}.
     * @return A list of {@link Statistic statistics} for the given dates.
     */
    List<Statistic> getStatisticsByDates(List<Date> dates);


}
