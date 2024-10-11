package de.imi.mopat.helper.model;

import de.imi.mopat.model.BundleClinic;
import de.imi.mopat.model.dto.BundleClinicDTO;
import de.imi.mopat.model.dto.ClinicDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.BiFunction;

@Component
public class BundleClinicDTOMapper implements BiFunction<ClinicDTO, BundleClinic, BundleClinicDTO> {

    private final BundleDTOMapper bundleDTOMapper;

    @Autowired
    public BundleClinicDTOMapper(BundleDTOMapper bundleDTOMapper) {
        this.bundleDTOMapper = bundleDTOMapper;
    }

    /*
     * Converts this {@link BundleClinic} object to an {@link
     * BundleClinicDTO} object.
     *
     * @return An {@link BundleClinicDTO} object based on this {@link
     * BundleClinic}
     * object.
     */
    @Override
    public BundleClinicDTO apply(ClinicDTO clinicDTO, BundleClinic bundleClinic) {
        BundleClinicDTO bundleClinicDTO = new BundleClinicDTO();
        bundleClinicDTO.setBundleDTO(bundleDTOMapper.apply(true, bundleClinic.getBundle()));
        bundleClinicDTO.setClinicDTO(clinicDTO);
        bundleClinicDTO.setPosition(bundleClinic.getPosition());
        return bundleClinicDTO;
    }
}