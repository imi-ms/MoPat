package de.imi.mopat.service;

import de.imi.mopat.cron.EncounterScheduledExecutor;
import de.imi.mopat.dao.BundleDao;
import de.imi.mopat.dao.ClinicDao;
import de.imi.mopat.dao.EncounterScheduledDao;
import de.imi.mopat.model.Bundle;
import de.imi.mopat.model.Clinic;
import de.imi.mopat.model.Encounter;
import de.imi.mopat.model.EncounterScheduled;
import de.imi.mopat.model.dto.EncounterScheduledDTO;
import de.imi.mopat.model.enumeration.AuditEntryActionType;
import de.imi.mopat.model.enumeration.AuditPatientAttribute;
import de.imi.mopat.validator.EncounterScheduledDTOValidator;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.Set;

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

    public EncounterScheduledService(BundleDao bundleDao, ClinicDao clinicDao, EncounterScheduledDao encounterScheduledDao, EncounterScheduledDTOValidator encounterScheduledDTOValidator, MailService mailService, LocalValidatorFactoryBean validator, ImmediateMailService immediateMailService,
        AuditService auditService) {
        this.bundleDao = bundleDao;
        this.clinicDao = clinicDao;
        this.encounterScheduledDao = encounterScheduledDao;
        this.encounterScheduledDTOValidator = encounterScheduledDTOValidator;
        this.mailService = mailService;
        this.validator = validator;
        this.immediateMailService = immediateMailService;
        this.auditService = auditService;
    }

    // in case of REPEATEDLY, RepeatPeriod is already set in dto
    public void setRepeatConfiguration(EncounterScheduledDTO dto){
        int WEEKLY_PERIOD_DAYS = 7;
        int MONTHLY_PERIOD_DAYS = 30;
        switch (dto.getEncounterScheduledSerialType()){
            case UNIQUELY -> {
                dto.setRepeatPeriod(null);
                dto.setEndDate(null);
                break;
            }
            case WEEKLY ->{
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

    public EncounterScheduled mapToEntity(EncounterScheduledDTO dto){
        Bundle bundle = bundleDao.getElementById(dto.getBundleDTO().getId());
        Clinic clinic = clinicDao.getElementById(dto.getClinicDTO().getId());

        if (dto.getId() == null){
            return new EncounterScheduled(
                    dto.getCaseNumber(),
                    bundle,
                    clinic,
                    dto.getStartDate(),
                    dto.getEncounterScheduledSerialType(),
                    dto.getEndDate(),
                    dto.getRepeatPeriod(),
                    dto.getEmail(),
                    dto.getLocale().toString(),
                    dto.getPersonalText(),
                    dto.getReplyMail()
            );
        }
        EncounterScheduled entity = encounterScheduledDao.getElementById(dto.getId());
        updateEntity(entity, dto, bundle, clinic);
        return entity;

    }

        private void updateEntity(EncounterScheduled entity, EncounterScheduledDTO dto, Bundle bundle, Clinic clinic){
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
            }
            else entity.setReplyMail(dto.getReplyMail());
            };

    public MailSendingStatus save(EncounterScheduledDTO dto,
                                  EncounterScheduledExecutor executor){
        setRepeatConfiguration(dto);
        //TODO remove if really not necessary
//        //duplicate validation
//        encounterScheduledDTOValidator.validate(dto, bindingResult);
//        if (bindingResult.hasErrors()){
//            return null;
//        }
        EncounterScheduled scheduled = mapToEntity(dto);
        encounterScheduledDao.merge(scheduled);
        Bundle bundle = scheduled.getBundle();

        if (bundle != null){
            bundleDao.merge(bundle);
        }

        auditService.writeScheduledEncounterAudit(
            EncounterScheduledService.class,
            "save(encounterScheduledDTO, bindingResult, executor)",
            scheduled
        );

        if (immediateMailService.shouldSendEmailImmediately(scheduled,executor)){
            return immediateMailService.createAndSendEncounter(scheduled);
        }
        return MailSendingStatus.SUCCESS;
    }


}





