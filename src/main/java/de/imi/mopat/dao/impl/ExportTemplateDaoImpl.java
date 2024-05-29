package de.imi.mopat.dao.impl;

import de.imi.mopat.dao.ExportTemplateDao;
import de.imi.mopat.model.ExportTemplate;
import de.imi.mopat.model.enumeration.ExportTemplateType;

import java.util.Calendar;
import java.util.Date;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class ExportTemplateDaoImpl extends MoPatDaoImpl<ExportTemplate> implements
    ExportTemplateDao {

    /**
     * The method must be carried out after 0:00 pm so the figures are calculated for the previous
     * day
     *
     * @param type Export type
     * @return count
     */
    @Override
    public long getExportCount(final ExportTemplateType type) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date startDate = calendar.getTime();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        Date endDate = calendar.getTime();
        TypedQuery<Long> query = moPatEntityManager.createQuery(
            "SELECT count(e.id) FROM EncounterExportTemplate e, "
                + "ExportTemplate t where t.exportTemplateType = :type "
                + "and e.exportTemplate.id = t.id  and e.exportTime "
                + "between :startDate and :endDate", Long.class);

        query.setParameter("type", type);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        long count = query.getSingleResult();

        return count;
    }

}
