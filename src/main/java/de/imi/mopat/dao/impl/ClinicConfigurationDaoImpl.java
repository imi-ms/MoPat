package de.imi.mopat.dao.impl;

import de.imi.mopat.dao.ClinicConfigurationDao;
import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.helper.controller.Constants;
import de.imi.mopat.helper.controller.StringUtilities;
import de.imi.mopat.model.ClinicConfiguration;
import de.imi.mopat.model.Configuration;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Locale;

@Component
public class ClinicConfigurationDaoImpl extends MoPatDaoImpl<ClinicConfiguration> implements ClinicConfigurationDao {

}
