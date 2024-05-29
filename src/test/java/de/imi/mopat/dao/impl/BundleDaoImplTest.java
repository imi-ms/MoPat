package de.imi.mopat.dao.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.dao.BundleDao;
import de.imi.mopat.model.Bundle;
import de.imi.mopat.model.BundleTest;
import de.imi.mopat.utils.Helper;
import java.util.Random;
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
public class BundleDaoImplTest {

    private static final Random random = new Random();
    @Autowired
    BundleDao testBundleDao;

    /**
     * Test of {@link BundleDaoImpl#isBundleNameUnused}.<br> Valid input: random name and random Id,
     * existing name and null, existing name and random Id, existing name and associated Id
     */
    @Test
    public void testIsBundleNameUnused() {
        Bundle testBundle = BundleTest.getNewValidBundle();
        String testName = Helper.getRandomAlphabeticString(random.nextInt(253) + 3);

        assertTrue("A random new name was already used",
            testBundleDao.isBundleNameUnused(testName, random.nextLong()));

        testBundle.setName(testName);
        testBundleDao.merge(testBundle);

        assertFalse("The method returned true although the given Id was null",
            testBundleDao.isBundleNameUnused(testName, null));
        assertFalse("The method returned true although the given Id was random",
            testBundleDao.isBundleNameUnused(testName, random.nextLong()));
        assertTrue("The name was already used in another Bundle",
            testBundleDao.isBundleNameUnused(testName, testBundle.getId()));
    }
}
