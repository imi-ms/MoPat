package de.imi.mopat.model.score;

import static org.junit.Assert.assertEquals;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.dao.OperatorDao;
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
public class OperatorTest {

    private static final Random random = new Random();
    @Autowired
    private OperatorDao operatorDao;

    /**
     * Returns a new valid instance of {@link Operator}
     *
     * @return New valid Operator
     */
    public static Operator getNewValidOperator() {
        switch (random.nextInt(3)) {
            case 0:
                return UnaryOperatorTest.getNewValidUnaryOperator();
            case 1:
                return BinaryOperatorTest.getNewValidBinaryOperator();
            default:
                return MultiOperatorTest.getNewValidMultiOperator();
        }
    }

    /**
     * Test of {@link Operator#getUUID}.<br> Valid input: Element by id and by DisplaySign
     */
    @Test
    public void testGetUUID() {
        Operator testOperator = operatorDao.getElementById(1L);
        assertEquals("The getting UUID was not the expected one",
            "81d2abc0-13f9-11e5-b939-0800200c9a66", testOperator.getUUID());
        testOperator = operatorDao.getOperatorByDisplaySign("<");
        assertEquals("The getting UUID was not the expected one",
            "178aef8a-d6df-4a5f-b884-e042a7d03f3d", testOperator.getUUID());
    }

    /**
     * Test of {@link Operator#getDisplaySign}.<br> Valid input: Element by id and by uuid
     */
    @Test
    public void testGetDisplaySign() {
        Operator testOperator = operatorDao.getElementById(1L);
        assertEquals("The getting DisplaySign was not the expected one", "+",
            testOperator.getDisplaySign());
        testOperator = operatorDao.getElementByUUID("c46c2380-516b-11e5-b970-0800200c9a66");
        assertEquals("The getting DisplaySign was not the expected one", "sum",
            testOperator.getDisplaySign());
    }
}
