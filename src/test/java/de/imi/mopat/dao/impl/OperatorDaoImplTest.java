package de.imi.mopat.dao.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.dao.OperatorDao;
import de.imi.mopat.model.score.Average;
import de.imi.mopat.model.score.Counter;
import de.imi.mopat.model.score.Different;
import de.imi.mopat.model.score.Divide;
import de.imi.mopat.model.score.Equals;
import de.imi.mopat.model.score.Greater;
import de.imi.mopat.model.score.GreaterEquals;
import de.imi.mopat.model.score.Less;
import de.imi.mopat.model.score.LessEquals;
import de.imi.mopat.model.score.Maximum;
import de.imi.mopat.model.score.Minimum;
import de.imi.mopat.model.score.Minus;
import de.imi.mopat.model.score.Multiply;
import de.imi.mopat.model.score.Plus;
import de.imi.mopat.model.score.Sum;
import de.imi.mopat.model.score.ValueOfQuestionOperator;
import de.imi.mopat.model.score.ValueOfScoreOperator;
import de.imi.mopat.model.score.ValueOperator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
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
public class OperatorDaoImplTest {

    @Autowired
    OperatorDao testOperatorDao;

    /**
     * Test of {@link OperatorDaoImpl#getOperators}.<br>
     */
    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "MoPatUserDetailsService")
    public void testGetOperators() {
        assertEquals("The getting size was not the expected one", 18,
            testOperatorDao.getOperators().size());

    }

    /**
     * Test of {@link OperatorDaoImpl#getOperatorByDisplaySign}.<br> Valid input: All possible
     * DisplaySigns and a DisplaySign that does not exist
     */
    @Test
    public void testGetOperatorByDisplaySign() {
        assertTrue("The getting Operator was not the expected one",
            testOperatorDao.getOperatorByDisplaySign("+") instanceof Plus);
        assertTrue("The getting Operator was not the expected one",
            testOperatorDao.getOperatorByDisplaySign("-") instanceof Minus);
        assertTrue("The getting Operator was not the expected one",
            testOperatorDao.getOperatorByDisplaySign("*") instanceof Multiply);
        assertTrue("The getting Operator was not the expected one",
            testOperatorDao.getOperatorByDisplaySign("/") instanceof Divide);
        assertTrue("The getting Operator was not the expected one",
            testOperatorDao.getOperatorByDisplaySign("valueOf") instanceof ValueOfQuestionOperator);
        assertTrue("The getting Operator was not the expected one",
            testOperatorDao.getOperatorByDisplaySign("value") instanceof ValueOperator);
        assertTrue("The getting Operator was not the expected one",
            testOperatorDao.getOperatorByDisplaySign("sum") instanceof Sum);
        assertTrue("The getting Operator was not the expected one",
            testOperatorDao.getOperatorByDisplaySign(">") instanceof Greater);
        assertTrue("The getting Operator was not the expected one",
            testOperatorDao.getOperatorByDisplaySign(">=") instanceof GreaterEquals);
        assertTrue("The getting Operator was not the expected one",
            testOperatorDao.getOperatorByDisplaySign("<") instanceof Less);
        assertTrue("The getting Operator was not the expected one",
            testOperatorDao.getOperatorByDisplaySign("<=") instanceof LessEquals);
        assertTrue("The getting Operator was not the expected one",
            testOperatorDao.getOperatorByDisplaySign("==") instanceof Equals);
        assertTrue("The getting Operator was not the expected one",
            testOperatorDao.getOperatorByDisplaySign("!=") instanceof Different);
        assertTrue("The getting Operator was not the expected one",
            testOperatorDao.getOperatorByDisplaySign("counter") instanceof Counter);
        assertTrue("The getting Operator was not the expected one",
            testOperatorDao.getOperatorByDisplaySign("average") instanceof Average);
        assertTrue("The getting Operator was not the expected one",
            testOperatorDao.getOperatorByDisplaySign(
                "valueOfScore") instanceof ValueOfScoreOperator);
        assertTrue("The getting Operator was not the expected one",
            testOperatorDao.getOperatorByDisplaySign("maximum") instanceof Maximum);
        assertTrue("The getting Operator was not the expected one",
            testOperatorDao.getOperatorByDisplaySign("minimum") instanceof Minimum);
        assertNull("The getting Operator was not null",
            testOperatorDao.getOperatorByDisplaySign("test"));
    }

    /**
     * Test of {@link OperatorDaoImpl#getElementById}.<br> Valid input: All possible Ids and an Id
     * that does not exist
     */
    @Test
    public void testGetElementById() {
        assertTrue("The getting Operator was not the expected one",
            testOperatorDao.getElementById(1L) instanceof Plus);
        assertTrue("The getting Operator was not the expected one",
            testOperatorDao.getElementById(2L) instanceof Minus);
        assertTrue("The getting Operator was not the expected one",
            testOperatorDao.getElementById(3L) instanceof Multiply);
        assertTrue("The getting Operator was not the expected one",
            testOperatorDao.getElementById(4L) instanceof Divide);
        assertTrue("The getting Operator was not the expected one",
            testOperatorDao.getElementById(5L) instanceof ValueOfQuestionOperator);
        assertTrue("The getting Operator was not the expected one",
            testOperatorDao.getElementById(6L) instanceof ValueOperator);
        assertTrue("The getting Operator was not the expected one",
            testOperatorDao.getElementById(7L) instanceof Sum);
        assertTrue("The getting Operator was not the expected one",
            testOperatorDao.getElementById(8L) instanceof Greater);
        assertTrue("The getting Operator was not the expected one",
            testOperatorDao.getElementById(9L) instanceof GreaterEquals);
        assertTrue("The getting Operator was not the expected one",
            testOperatorDao.getElementById(10L) instanceof Less);
        assertTrue("The getting Operator was not the expected one",
            testOperatorDao.getElementById(11L) instanceof LessEquals);
        assertTrue("The getting Operator was not the expected one",
            testOperatorDao.getElementById(12L) instanceof Equals);
        assertTrue("The getting Operator was not the expected one",
            testOperatorDao.getElementById(13L) instanceof Different);
        assertTrue("The getting Operator was not the expected one",
            testOperatorDao.getElementById(14L) instanceof Counter);
        assertTrue("The getting Operator was not the expected one",
            testOperatorDao.getElementById(15L) instanceof Average);
        assertTrue("The getting Operator was not the expected one",
            testOperatorDao.getElementById(16L) instanceof ValueOfScoreOperator);
        assertTrue("The getting Operator was not the expected one",
            testOperatorDao.getElementById(17L) instanceof Maximum);
        assertTrue("The getting Operator was not the expected one",
            testOperatorDao.getElementById(18L) instanceof Minimum);
        assertNull("The getting Operator was not null", testOperatorDao.getElementById(-1L));
    }
}
