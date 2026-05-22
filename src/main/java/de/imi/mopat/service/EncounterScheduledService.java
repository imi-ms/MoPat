package de.imi.mopat.service;

import de.imi.mopat.cron.EncounterScheduledExecutor;
import de.imi.mopat.dao.BundleDao;
import de.imi.mopat.dao.ClinicDao;
import de.imi.mopat.dao.EncounterScheduledDao;
import de.imi.mopat.model.Bundle;
import de.imi.mopat.model.Clinic;
import de.imi.mopat.model.EncounterScheduled;
import de.imi.mopat.model.dto.EncounterScheduledDTO;
import de.imi.mopat.service.helper.SetRepeatConfiguration;
import de.imi.mopat.validator.EncounterScheduledDTOValidator;
import org.springframework.stereotype.Service;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Service
public class EncounterScheduledService {

    private final BundleDao bundleDao;
    private final ClinicDao clinicDao;
    private final EncounterScheduledDao encounterScheduledDao;
    private final EncounterScheduledDTOValidator encounterScheduledDTOValidator;
    private final MailService mailService;
    private final LocalValidatorFactoryBean validator;
    private final ImmediateMailService immediateMailService;
    private final AuditService auditService;
    private final SetRepeatConfiguration setRepeatConfiguration;

    public EncounterScheduledService(BundleDao bundleDao, ClinicDao clinicDao,
        EncounterScheduledDao encounterScheduledDao,
        EncounterScheduledDTOValidator encounterScheduledDTOValidator, MailService mailService,
        LocalValidatorFactoryBean validator, ImmediateMailService immediateMailService,
        AuditService auditService, SetRepeatConfiguration setRepeatConfiguration) {
        this.bundleDao = bundleDao;
        this.clinicDao = clinicDao;
        this.encounterScheduledDao = encounterScheduledDao;
        this.encounterScheduledDTOValidator = encounterScheduledDTOValidator;
        this.mailService = mailService;
        this.validator = validator;
        this.immediateMailService = immediateMailService;
        this.auditService = auditService;
        this.setRepeatConfiguration = setRepeatConfiguration;
    }

    public EncounterScheduled mapToEntity(EncounterScheduledDTO dto) {
        Bundle bundle = bundleDao.getElementById(dto.getBundleDTO().getId());
        Clinic clinic = clinicDao.getElementById(dto.getClinicDTO().getId());

        if (dto.getId() == null) {
            return new EncounterScheduled(dto.getCaseNumber(), bundle, clinic, dto.getStartDate(),
                dto.getEncounterScheduledSerialType(), dto.getEndDate(), dto.getRepeatPeriod(),
                dto.getEmail(), dto.getLocale().toString(), dto.getPersonalText(),
                dto.getReplyMail());
        }
        EncounterScheduled entity = encounterScheduledDao.getElementById(dto.getId());
        updateEntity(entity, dto, bundle, clinic);
        return entity;

    }

    private void updateEntity(EncounterScheduled entity, EncounterScheduledDTO dto, Bundle bundle,
        Clinic clinic) {
        entity.setCaseNumber(dto.getCaseNumber());
        entity.setBundle(bundle);
        entity.setClinic(clinic);
        entity.setStartDate(dto.getStartDate());
        entity.setEncounterScheduledSerialType(dto.getEncounterScheduledSerialType());
        entity.setEndDate(dto.getEndDate());
        entity.setRepeatPeriod(dto.getRepeatPeriod());
        entity.setEmail(dto.getEmail());
        entity.setLocale(dto.getLocale().toString());
        entity.setPersonalText(dto.getPersonalText());
        if (dto.getReplyMail().equalsIgnoreCase("empty")) {
            entity.setReplyMail(null);
        } else {
            entity.setReplyMail(dto.getReplyMail());
        }
    }

    public MailSendingStatus save(EncounterScheduledDTO dto, EncounterScheduledExecutor executor) {
        setRepeatConfiguration.apply(dto);

        EncounterScheduled scheduled = mapToEntity(dto);
        encounterScheduledDao.merge(scheduled);
        Bundle bundle = scheduled.getBundle();

        if (bundle != null) {
            bundleDao.merge(bundle);
        }

        auditService.writeScheduledEncounterAudit(EncounterScheduledService.class,
            "save(encounterScheduledDTO, bindingResult, executor)", scheduled);

        if (immediateMailService.shouldSendEmailImmediately(scheduled, executor)) {
            return immediateMailService.createAndSendEncounter(scheduled);
        }
        return MailSendingStatus.SUCCESS;
    }


}





