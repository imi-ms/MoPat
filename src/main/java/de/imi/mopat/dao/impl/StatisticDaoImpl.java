package de.imi.mopat.dao.impl;

import de.imi.mopat.dao.StatisticDao;
import de.imi.mopat.model.Statistic;

import java.util.Date;
import java.util.List;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class StatisticDaoImpl extends MoPatDaoImpl<Statistic> implements StatisticDao {

    @Override
    public Date getEarliestDate() {
        try {
            Query query = moPatEntityManager.createQuery(
                "SELECT s.date " + "FROM" + " " + "Statistic" + " s order " + "by s.date " + "ASC");
            query.setMaxResults(1);
            Date date = (Date) query.getSingleResult();
            return date;
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Date getLatestDate() {
        try {
            Query query = moPatEntityManager.createQuery(
                "SELECT s.date " + "FROM" + " " + "Statistic" + " s order " + "by s.date "
                    + "DESC");
            query.setMaxResults(1);
            Date date = (Date) query.getSingleResult();
            return date;
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Statistic> getStatisticsByDates(final List<Date> dates) {
        TypedQuery<Statistic> query = moPatEntityManager.createQuery(
            "SELECT s FROM Statistic s where s.date IN :dates", getEntityClass());
        query.setParameter("dates", dates);
        return query.getResultList();
    }
}
