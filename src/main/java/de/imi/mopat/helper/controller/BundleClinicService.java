package de.imi.mopat.helper.controller;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.imi.mopat.model.Bundle;
import de.imi.mopat.model.BundleClinic;
import de.imi.mopat.model.BundleQuestionnaire;
import de.imi.mopat.model.Clinic;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.dto.BundleClinicDTO;
import de.imi.mopat.model.dto.BundleDTO;
import de.imi.mopat.model.dto.BundleQuestionnaireDTO;
import de.imi.mopat.model.dto.ClinicDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BundleClinicService {

    private static final org.slf4j.Logger LOGGER =
        org.slf4j.LoggerFactory.getLogger(Question.class);

    @Autowired
    private BundleService bundleService;

    /*
     * Converts this {@link BundleClinic} object to an {@link
     * BundleClinicDTO} object.
     *
     * @return An {@link BundleClinicDTO} object based on this {@link
     * BundleClinic}
     * object.
     */
    public BundleClinicDTO toBundleClinicDTO(final ClinicDTO clinicDTO, BundleClinic bundleClinic) {
        BundleClinicDTO bundleClinicDTO = new BundleClinicDTO();
        bundleClinicDTO.setBundleDTO(bundleService.toBundleDTO(true,bundleClinic.getBundle()));
        bundleClinicDTO.setClinicDTO(clinicDTO);
        bundleClinicDTO.setPosition(bundleClinic.getPosition());
        return bundleClinicDTO;
    }


}
