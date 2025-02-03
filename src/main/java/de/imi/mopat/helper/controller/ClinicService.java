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

    public List<ClinicDTO> getAllClinicsWithoutBundle() {
        return clinicDao.getAllElements().stream()
            .map(clinic -> clinicDTOMapper.mapWithoutBundle(clinic))
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
    
    public List<ClinicDTO> transformClinicsToDTOs(Boolean fullVersion, List<Clinic> clinics) {
        if(fullVersion){
            return clinics.stream().map(clinic -> clinicDTOMapper.apply(clinic)).toList();
        }
        return clinics.stream().map(clinic -> clinicDTOMapper.mapWithoutBundle(clinic)).toList();
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

    /**
     * Returns the list of clinics sorted by their name property (ascending).
     *
     * @param clinics to sort
     * @return sorted List<Clinic>
     */
    public List<Clinic> sortClinicsByNameAsc(List<Clinic> clinics) {
        clinics.sort(Comparator.comparing(clinic -> clinic.getName().toLowerCase()));
        return clinics;
    }
}
