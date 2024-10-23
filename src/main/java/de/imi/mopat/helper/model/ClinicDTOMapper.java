package de.imi.mopat.helper.model;

import de.imi.mopat.model.Clinic;
import de.imi.mopat.model.dto.BundleClinicDTO;
import de.imi.mopat.model.dto.ClinicDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ClinicDTOMapper implements Function<Clinic, ClinicDTO> {

    private final BundleClinicDTOMapper bundleClinicDTOMapper;

    @Autowired
    public ClinicDTOMapper(BundleClinicDTOMapper bundleClinicDTOMapper) {
        this.bundleClinicDTOMapper = bundleClinicDTOMapper;
    }

    /*
     * Converts this {@link Clinic} object to an {@link ClinicDTO} object.
     *
     * @return An {@link ClinicDTO} object based on this {@link Clinic}
     * object.
     */
    @Override
    public ClinicDTO apply(Clinic clinic) {
        ClinicDTO clinicDTO = new ClinicDTO();
        clinicDTO.setId(clinic.getId());
        clinicDTO.setDescription(clinic.getDescription());
        clinicDTO.setName(clinic.getName());
        clinicDTO.setEmail(clinic.getEmail());

        List<BundleClinicDTO> bundleClinicDTOs = clinic.getBundleClinics().stream()
                .map(bundleClinic -> bundleClinicDTOMapper.apply(clinicDTO, bundleClinic))
                .collect(Collectors.toList());

        clinicDTO.setBundleClinicDTOs(bundleClinicDTOs);

        return clinicDTO;
    }
}