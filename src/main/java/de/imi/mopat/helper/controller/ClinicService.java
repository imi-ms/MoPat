package de.imi.mopat.helper.controller;

import de.imi.mopat.dao.user.AclEntryDao;
import de.imi.mopat.dao.ClinicDao;
import de.imi.mopat.helper.model.ClinicDTOMapper;
import de.imi.mopat.model.Clinic;
import de.imi.mopat.model.dto.ClinicDTO;
import de.imi.mopat.model.enumeration.PermissionType;
import de.imi.mopat.model.user.User;
import java.util.List;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClinicService {

    private static final org.slf4j.Logger LOGGER =
        org.slf4j.LoggerFactory.getLogger(ClinicService.class);

    @Autowired
    ClinicDTOMapper clinicDTOMapper;
    @Autowired
    private ClinicDao clinicDao;
    @Autowired
    private AclEntryDao aclEntryDao;

    @Autowired
    private ClinicConfigurationMappingService clinicConfigurationMappingService;

    public List<ClinicDTO> getAllClinics() {
        return clinicDao.getAllElements().stream()
            .map(clinicDTOMapper)
            .toList();
    }
    
    public ClinicDTO getClinicDTOById(Long id) {
        return clinicDTOMapper.apply(
            clinicDao.getElementById(id)
        );
    }
    
    public List<Clinic> getAssignedClinics(User user) {
        return new ArrayList<>(clinicDao.getElementsById(
            aclEntryDao.getObjectIdsForClassUserAndRight(
                Clinic.class, user, PermissionType.READ
            )
        ));
    }
    
    public List<ClinicDTO> transformClinicsToDTOs(List<Clinic> clinics) {
        return clinics.stream().map(clinic -> clinicDTOMapper.apply(clinic)).toList();
    }
    
    public Clinic getClinicByIdFromList(List<Clinic> clinics, Long id) {
        try {
            return clinics.stream().filter(clinic -> clinic.getId().equals(id)).findFirst().get();
        } catch(NoSuchElementException e) {
            return null;
        }
    }

    public void merge(Clinic clinic) {
        clinicDao.merge(clinic);
    }
}
