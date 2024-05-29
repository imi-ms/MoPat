package de.imi.mopat.helper.controller;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.imi.mopat.model.BundleClinic;
import de.imi.mopat.model.Clinic;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.dto.BundleClinicDTO;
import de.imi.mopat.model.dto.ClinicDTO;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClinicService {

    private static final org.slf4j.Logger LOGGER =
        org.slf4j.LoggerFactory.getLogger(Question.class);

    @Autowired
    private BundleClinicService bundleClinicService;

    /*
     * Converts this {@link Clinic} object to an {@link ClinicDTO} object.
     *
     * @return An {@link ClinicDTO} object based on this {@link Clinic}
     * object.
     */
    @JsonIgnore
    public ClinicDTO toClinicDTO(Clinic clinic) {
        ClinicDTO clinicDTO = new ClinicDTO();
        clinicDTO.setId(clinic.getId());
        clinicDTO.setDescription(clinic.getDescription());
        clinicDTO.setName(clinic.getName());
        clinicDTO.setEmail(clinic.getEmail());

        List<BundleClinicDTO> bundleClinicDTOs = new ArrayList<>();
        for (BundleClinic bundleClinic : clinic.getBundleClinics()) {
            bundleClinicDTOs.add(bundleClinicService.toBundleClinicDTO(clinicDTO,bundleClinic));
        }
        clinicDTO.setBundleClinicDTOs(bundleClinicDTOs);

        return clinicDTO;
    }


}
