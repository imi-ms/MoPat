package de.imi.mopat.dao.impl;

import static org.junit.Assert.assertEquals;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.dao.ExportTemplateDao;
import de.imi.mopat.model.enumeration.ExportTemplateType;
import de.imi.mopat.utils.Helper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, ApplicationSecurityConfig.class,
    MvcWebApplicationInitializer.class, PersistenceConfig.class})
@TestPropertySource(locations = {"classpath:mopat-test.properties"})
@WebAppConfiguration
public class ExportTemplateDaoImplTest {

    @Autowired
    ExportTemplateDao testExportTemplateDao;

    /**
     * Test of {@link ExportTemplateDaoImpl#getExportCount}.<br> Valid input: Random
     * {@link ExportTemplateType}
     */
    @Test
    public void testGetExportCount() {
        assertEquals("The getting count of ExportTemplates was not 0", 0,
            testExportTemplateDao.getExportCount(Helper.getRandomEnum(ExportTemplateType.class)));
    }
}
