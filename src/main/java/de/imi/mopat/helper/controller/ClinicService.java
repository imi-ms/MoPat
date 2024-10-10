package de.imi.mopat.helper.controller;

import de.imi.mopat.dao.ClinicDao;
import de.imi.mopat.helper.model.ClinicDTOMapper;
import de.imi.mopat.model.Clinic;
import de.imi.mopat.model.dto.ClinicDTO;
import java.util.List;
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


    public List<ClinicDTO> getAllClinics(){
        return clinicDao.getAllElements().stream()
                .map(clinicDTOMapper)
                .toList();
    }

    public void merge(Clinic clinic) {
        clinicDao.merge(clinic);
    }
}
