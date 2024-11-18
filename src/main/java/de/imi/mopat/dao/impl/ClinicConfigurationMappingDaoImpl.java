package de.imi.mopat.dao.impl;

import de.imi.mopat.dao.ClinicConfigurationMappingDao;
import de.imi.mopat.helper.controller.Constants;
import de.imi.mopat.model.ClinicConfigurationMapping;
import de.imi.mopat.model.Configuration;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ClinicConfigurationMappingDaoImpl extends MoPatDaoImpl<ClinicConfigurationMapping> implements
    ClinicConfigurationMappingDao {

    @Override
    public List<ClinicConfigurationMapping> getAllElementsByClinicId(final Integer clinicId) {
        List<ClinicConfigurationMapping> clinicConfigurationMappings = new ArrayList<>();
        if (clinicId == null) {
            return clinicConfigurationMappings; // Return empty list if clinicId is null
        }

        try {
            String jpql = "SELECT c FROM ClinicConfigurationMapping c WHERE c.clinic.id = :clinicId";
            TypedQuery<ClinicConfigurationMapping> query = moPatEntityManager.createQuery(jpql,
                ClinicConfigurationMapping.class);
            query.setParameter("clinicId", clinicId);
            clinicConfigurationMappings = query.getResultList();
        } catch (Exception e) {
            // Ideally log the exception; for now, we're simply printing the stack trace.
            e.printStackTrace();
            return null; // Perhaps consider handling this case more gracefully
        }
        return clinicConfigurationMappings;
    }

    @Override
    public ClinicConfigurationMapping getConfigurationByAttributeAndClass(final Long clinicId, final String attribute,
        final String clazz) {
        try {
            String jpql = "SELECT ccm FROM ClinicConfigurationMapping ccm " + "JOIN ccm.clinic cl "
                + "JOIN ccm.clinicConfiguration cc "
                + "WHERE cl.id = :clinicId AND cc.attribute = :attribute AND cc.entityClass = :entityClass";

            TypedQuery<ClinicConfigurationMapping> query = moPatEntityManager.createQuery(jpql,
                ClinicConfigurationMapping.class);
            query.setParameter("clinicId", clinicId);
            query.setParameter("attribute", attribute);
            query.setParameter("entityClass", clazz);

            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Boolean isRegistryOfPatientActivated(final Long clinicId) {
        ClinicConfigurationMapping clinicConfigurationMapping = getConfigurationByAttributeAndClass(clinicId,
            Constants.REGISTER_PATIENT_DATA, Constants.CLASS_GLOBAL);
        return Boolean.valueOf(clinicConfigurationMapping != null ? clinicConfigurationMapping.getValue() : "false");
    }

    @Override
    public Boolean isUsePatientDataLookupActivated(final Long clinicId) {
        ClinicConfigurationMapping clinicConfigurationMapping = getConfigurationByAttributeAndClass(clinicId,
            Constants.USE_PATIENT_DATA_LOOKUP, Constants.CLASS_GLOBAL);
        return Boolean.valueOf(clinicConfigurationMapping != null ? clinicConfigurationMapping.getValue() : "false");
    }

    @Override
    public Boolean isPseudonymizationServiceActivated(final Long clinicId) {
        ClinicConfigurationMapping clinicConfigurationMapping = getConfigurationByAttributeAndClass(clinicId,
            Constants.USE_PSEUDONYMIZATION_SERVICE, Constants.CLASS_GLOBAL);
        return Boolean.valueOf(clinicConfigurationMapping != null ? clinicConfigurationMapping.getValue() : "false");
    }
}
