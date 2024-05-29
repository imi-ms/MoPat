package de.imi.mopat.cron;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import de.imi.mopat.dao.BundleDao;
import de.imi.mopat.dao.ClinicDao;
import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.dao.EncounterDao;
import de.imi.mopat.dao.ExportTemplateDao;
import de.imi.mopat.dao.QuestionnaireDao;
import de.imi.mopat.dao.StatisticDao;
import de.imi.mopat.dao.user.UserDao;
import de.imi.mopat.helper.controller.Constants;
import de.imi.mopat.model.Statistic;
import de.imi.mopat.model.enumeration.ExportTemplateType;

import java.sql.Timestamp;
import java.util.Date;

import org.slf4j.Logger;

/**
 * This class generates the statistics on a daily basis. Thus, a {@link Statistic} entry is
 * generated for each day.
 */
@Service
public class StatisticFiller {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(StatisticFiller.class);

    @Autowired
    private ClinicDao clinicDao;
    @Autowired
    private StatisticDao statisticDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    private BundleDao bundleDao;
    @Autowired
    private EncounterDao encounterDao;
    @Autowired
    private QuestionnaireDao questionnaireDao;
    @Autowired
    private ExportTemplateDao exportTemplateDao;
    @Autowired
    private ConfigurationDao configurationDao;

    /**
     * Gets triggered by the value provided in de.imi.mopat.cron.StatisticFiller.fill The method
     * generates one {@link Statistic} object for this day and fills it with the current statistic
     * data.
     */
    @Scheduled(cron = "${de.imi.mopat.cron.StatisticFiller.fill}")
    public void fill() {
        Date date = new Date();
        Statistic statistic = new Statistic();
        statistic.setDate(date);
        statistic.setQuestionnaireCount(questionnaireDao.getCount());
        statistic.setBundleCount(bundleDao.getCount());
        statistic.setClinicCount(clinicDao.getCount());
        statistic.setUserCount(userDao.getCount());
        statistic.setEncounterCount(encounterDao.getCount());
        statistic.setIncompleteEncounterCount(encounterDao.getCountIncompleteEncounter());
        // Evaluate the number of todays deleted finished encounters
        boolean deleteFinishedEncounters = true;
        Long finishedEncounterTimeWindowInMillis = configurationDao.getFinishedEncounterTimeWindow();
        if (finishedEncounterTimeWindowInMillis == null) {
            LOGGER.info("Could not find a value for the property {}; will take "
                    + "the default (30 days) instead",
                Constants.FINISHED_ENCOUNTER_TIME_WINDOW_IN_MILLIS);
            finishedEncounterTimeWindowInMillis = (30L * 24L * 60L * 60L * 1000L);
        } else if (finishedEncounterTimeWindowInMillis == -1) {
            deleteFinishedEncounters = false;
        }
        if (deleteFinishedEncounters) {
            statistic.setCompleteEncounterDeletedCount(
                encounterDao.getCountCompleteEncountersOlderThan(new Timestamp(
                    System.currentTimeMillis() - finishedEncounterTimeWindowInMillis)));
        } else {
            statistic.setCompleteEncounterDeletedCount(0L);
        }
        // Evaluate the number of todays deleted incomplete encounters
        boolean deleteIncompleteEncounters = true;
        Long incompleteEncounterTimeWindowInMillis = configurationDao.getIncompleteEncounterTimeWindow();
        if (incompleteEncounterTimeWindowInMillis == null) {
            LOGGER.info("Could not find a value for the property {}; will take "
                    + "the default (180 days) instead",
                Constants.INCOMPLETE_ENCOUNTER_TIME_WINDOW_IN_MILLIS);
            incompleteEncounterTimeWindowInMillis = (180L * 24L * 60L * 60L * 1000L);
        } else if (incompleteEncounterTimeWindowInMillis == -1) {
            deleteIncompleteEncounters = false;
        }
        if (deleteIncompleteEncounters) {
            statistic.setIncompleteEncounterDeletedCount(
                encounterDao.getCountIncompleteEncountersOlderThan(new Timestamp(
                    System.currentTimeMillis() - incompleteEncounterTimeWindowInMillis)));
        } else {
            statistic.setIncompleteEncounterDeletedCount(0L);
        }

        statistic.setHL7ExportCount(exportTemplateDao.getExportCount(ExportTemplateType.HL7v2));
        statistic.setODMExportCount(exportTemplateDao.getExportCount(ExportTemplateType.ODM));
        statistic.setORBISExportCount(exportTemplateDao.getExportCount(ExportTemplateType.ORBIS));

        statisticDao.merge(statistic);
    }
}
