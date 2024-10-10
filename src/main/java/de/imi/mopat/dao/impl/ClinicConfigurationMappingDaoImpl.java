package de.imi.mopat.dao.impl;

import de.imi.mopat.dao.ClinicConfigurationDao;
import de.imi.mopat.dao.ClinicConfigurationMappingDao;
import de.imi.mopat.model.ClinicConfiguration;
import de.imi.mopat.model.ClinicConfigurationMapping;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ClinicConfigurationMappingDaoImpl extends MoPatDaoImpl<ClinicConfigurationMapping> implements ClinicConfigurationMappingDao {

    @Override
    public List<ClinicConfigurationMapping> getAllElementsByClinicId(final Integer clinicId){
        List<ClinicConfigurationMapping> clinicConfigurationMappings = new ArrayList<>();
        if (clinicId == null) {
            return clinicConfigurationMappings; // Return empty list if clinicId is null
        }

        try {
            String jpql = "SELECT c FROM ClinicConfigurationMapping c WHERE c.clinic.id = :clinicId";
            TypedQuery<ClinicConfigurationMapping> query = moPatEntityManager.createQuery(jpql, ClinicConfigurationMapping.class);
            query.setParameter("clinicId", clinicId);
            clinicConfigurationMappings = query.getResultList();
        } catch (Exception e) {
            // Ideally log the exception; for now, we're simply printing the stack trace.
            e.printStackTrace();
            return null; // Perhaps consider handling this case more gracefully
        }
        return clinicConfigurationMappings;
    }
}
