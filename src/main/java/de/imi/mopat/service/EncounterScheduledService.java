package de.imi.mopat.service;

import de.imi.mopat.cron.EncounterScheduledExecutor;
import de.imi.mopat.dao.BundleDao;
import de.imi.mopat.dao.ClinicDao;
import de.imi.mopat.dao.EncounterScheduledDao;
import de.imi.mopat.helper.model.EncounterScheduledDTOMapper;
import de.imi.mopat.model.Bundle;
import de.imi.mopat.model.EncounterScheduled;
import de.imi.mopat.model.dto.EncounterScheduledDTO;
import de.imi.mopat.service.helper.SetRepeatConfiguration;
import de.imi.mopat.validator.EncounterScheduledDTOValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Service
public class EncounterScheduledService {

    private final BundleDao bundleDao;
    private final EncounterScheduledDao encounterScheduledDao;
    private final EncounterScheduledDTOMapper mapper;
    private final ImmediateMailService immediateMailService;
    private final AuditService auditService;
    private final SetRepeatConfiguration setRepeatConfiguration;

    public EncounterScheduledService(BundleDao bundleDao,
        EncounterScheduledDao encounterScheduledDao,
        EncounterScheduledDTOMapper mapper, ImmediateMailService immediateMailService,
        AuditService auditService, SetRepeatConfiguration setRepeatConfiguration) {
        this.bundleDao = bundleDao;
        this.encounterScheduledDao = encounterScheduledDao;
        this.mapper = mapper;
        this.immediateMailService = immediateMailService;
        this.auditService = auditService;
        this.setRepeatConfiguration = setRepeatConfiguration;
    }

    @Transactional
    public MailSendingStatus save(EncounterScheduledDTO dto, EncounterScheduledExecutor executor) {
        setRepeatConfiguration.apply(dto);

        EncounterScheduled scheduled = mapper.mapToEntity(dto);

        encounterScheduledDao.merge(scheduled);
        Bundle bundle = scheduled.getBundle();

        if (bundle != null) {
            bundleDao.merge(bundle);
        }

        auditService.writeScheduledEncounterAudit(EncounterScheduledService.class,
            "save", scheduled);

        if (immediateMailService.shouldSendEmailImmediately(scheduled, executor)) {
            return immediateMailService.createAndSendEncounter(scheduled);
        }
        return MailSendingStatus.SUCCESS;
    }


}





