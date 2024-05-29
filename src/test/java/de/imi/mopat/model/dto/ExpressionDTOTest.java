package de.imi.mopat.model.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.dao.OperatorDao;
import de.imi.mopat.dao.QuestionDao;
import de.imi.mopat.dao.QuestionnaireDao;
import de.imi.mopat.dao.ScoreDao;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.QuestionTest;
import de.imi.mopat.model.score.Average;
import de.imi.mopat.model.score.BinaryExpression;
import de.imi.mopat.model.score.Counter;
import de.imi.mopat.model.score.Different;
import de.imi.mopat.model.score.Divide;
import de.imi.mopat.model.score.Equals;
import de.imi.mopat.model.score.Expression;
import de.imi.mopat.model.score.Greater;
import de.imi.mopat.model.score.GreaterEquals;
import de.imi.mopat.model.score.Less;
import de.imi.mopat.model.score.LessEquals;
import de.imi.mopat.model.score.Maximum;
import de.imi.mopat.model.score.Minimum;
import de.imi.mopat.model.score.Minus;
import de.imi.mopat.model.score.MultiExpression;
import de.imi.mopat.model.score.Plus;
import de.imi.mopat.model.score.Score;
import de.imi.mopat.model.score.ScoreTest;
import de.imi.mopat.model.score.Sum;
import de.imi.mopat.model.score.UnaryExpression;
import de.imi.mopat.model.score.ValueOfQuestionOperator;
import de.imi.mopat.model.score.ValueOfScoreOperator;
import de.imi.mopat.model.score.ValueOperator;
import de.imi.mopat.utils.Helper;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
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
public class ExpressionDTOTest {

    private static final Random random = new Random();
    @Autowired
    OperatorDao operatorDao;
    @Autowired
    QuestionDao questionDao;
    @Autowired
    QuestionnaireDao questionnaireDao;
    @Autowired
    ScoreDao scoreDao;
    private ExpressionDTO testExpressionDTO;

    public ExpressionDTOTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        testExpressionDTO = new ExpressionDTO();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of {@link ExpressionDTO#getExpressions} and {@link ExpressionDTO#setExpressions}.<br>
     * Valid input: random list of{@link ExpressionDTO ExpressionDTOs}
     */
    @Test
    public void testGetAndSetExpressions() {
        List<ExpressionDTO> testExpressionDTOs = new ArrayList<>();
        int count = random.nextInt(200) + 1;
        for (int i = 0; i < count; i++) {
            testExpressionDTOs.add(new ExpressionDTO());
        }
        testExpressionDTO.setExpressions(testExpressionDTOs);
        assertEquals("The getting list of ExpressionDTOs was not the expected one",
            testExpressionDTOs, testExpressionDTO.getExpressions());
    }

    /**
     * Test of {@link ExpressionDTO#addExpressions}.<br> Valid input: random number of
     * {@link ExpressionDTO ExpressionDTOs}
     */
    @Test
    public void testAddExpressions() {
        List<ExpressionDTO> testExpressionDTOs = new ArrayList<>();
        int count = random.nextInt(200) + 1;
        for (int i = 0; i < count; i++) {
            ExpressionDTO tempExpressionDTO = new ExpressionDTO();
            testExpressionDTOs.add(tempExpressionDTO);
            testExpressionDTO.addExpressions(tempExpressionDTO);
        }
        assertEquals("The getting list of ExpressionDTOs was not the expected one",
            testExpressionDTOs, testExpressionDTO.getExpressions());
    }

    /**
     * Test of {@link ExpressionDTO#getOperatorId} and {@link ExpressionDTO#setOperatorId}.<br>
     * Valid input: random Long
     */
    @Test
    public void testGetAndSetOperatorId() {
        Long testOperatorID = Math.abs(random.nextLong());
        testExpressionDTO.setOperatorId(testOperatorID);
        assertEquals("The getting operatorID was not the expected one", testOperatorID,
            testExpressionDTO.getOperatorId());
    }

    /**
     * Test of {@link ExpressionDTO#getQuestionId} and {@link ExpressionDTO#setQuestionId}.<br>
     * Valid input: random Long
     */
    @Test
    public void testGetAndSetQuestionId() {
        Long testQuestionID = Math.abs(random.nextLong());
        testExpressionDTO.setQuestionId(testQuestionID);
        assertEquals("The getting questionID was not the expected one", testQuestionID,
            testExpressionDTO.getQuestionId());
    }

    /**
     * Test of {@link ExpressionDTO#getValue} and {@link ExpressionDTO#setValue}.<br> Valid input:
     * random String
     */
    @Test
    public void testGetAndSetValue() {
        String testValue = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);
        testExpressionDTO.setValue(testValue);
        assertEquals("The getting value was not the expected one", testValue,
            testExpressionDTO.getValue());
    }

    /**
     * Test of {@link ExpressionDTO#getScoreId} and {@link ExpressionDTO#setScoreId}.<br> Valid
     * input: random Long
     */
    @Test
    public void testGetAndSetScoreId() {
        Long testScoreId = Math.abs(random.nextLong());
        testExpressionDTO.setScoreId(testScoreId);
        assertEquals("The getting scoreId was not the expected one", testScoreId,
            testExpressionDTO.getScoreId());
    }

    /**
     * Test of {@link ExpressionDTO#toExpression}.<br> Valid input: random {@link ExpressionDTO}
     */
    @Test
    public void testToExpression() {
        Question testQuestion = QuestionTest.getNewValidQuestion();
        Score testScore = ScoreTest.getNewValidScore();
        Double testValue = random.nextDouble();

        questionnaireDao.merge(testQuestion.getQuestionnaire());
        questionDao.merge(testQuestion);
        scoreDao.merge(testScore);

        // Maximum Operator
        testExpressionDTO.setOperatorId(17L);

        // ValueOfQuestion Operator
        ExpressionDTO testValueOfExpressionDTO = new ExpressionDTO();
        testValueOfExpressionDTO.setOperatorId(5L);

        // Value Operator
        ExpressionDTO testValueExpressionDTO = new ExpressionDTO();
        testValueExpressionDTO.setOperatorId(6L);

        // ValueOfScore Operator
        ExpressionDTO testValueOfScoreExpressionDTO = new ExpressionDTO();
        testValueOfScoreExpressionDTO.setOperatorId(16L);

        // Plus Operator
        ExpressionDTO testPlusExpressionDTO = new ExpressionDTO();
        testPlusExpressionDTO.setOperatorId(1L);
        List<ExpressionDTO> plusExpressions = new ArrayList<>();
        plusExpressions.add(testValueOfExpressionDTO);
        plusExpressions.add(testValueExpressionDTO);
        testPlusExpressionDTO.setExpressions(plusExpressions);

        List<ExpressionDTO> maximumExpressions = new ArrayList<>();
        maximumExpressions.add(testValueOfScoreExpressionDTO);
        maximumExpressions.add(testPlusExpressionDTO);
        testExpressionDTO.setExpressions(maximumExpressions);

        // Maximum with Plus (with ValueOf and Value) and ValueOfScore
        Expression testExpression = testExpressionDTO.toExpression(operatorDao, questionDao,
            scoreDao);
        assertTrue("The getting Expression was not a instance of MultiExpression",
            testExpression instanceof MultiExpression);
        assertTrue("The getting Operator was not an instanceof Maximum",
            testExpression.getOperator() instanceof Maximum);
        assertTrue("The getting Expression was not a instance of UnaryExpression",
            ((MultiExpression) testExpression).getExpressions().get(0) instanceof UnaryExpression);
        assertTrue("The getting Operator was not an instanceof ValueOfScoreOperator",
            ((MultiExpression) testExpression).getExpressions().get(0)
                .getOperator() instanceof ValueOfScoreOperator);
        assertTrue("The getting Expression was not a instance of BinaryExpression",
            ((MultiExpression) testExpression).getExpressions().get(1) instanceof BinaryExpression);
        assertTrue("The getting Operator was not an instanceof Greater",
            ((MultiExpression) testExpression).getExpressions().get(1)
                .getOperator() instanceof Plus);
        assertTrue("The getting Expression was not a instance of UnaryExpression",
            ((BinaryExpression) ((MultiExpression) testExpression).getExpressions()
                .get(1)).getExpressions().get(0) instanceof UnaryExpression);
        assertTrue("The getting Operator was not an instanceof ValueOfQuestionOperator",
            ((BinaryExpression) ((MultiExpression) testExpression).getExpressions()
                .get(1)).getExpressions().get(0).getOperator() instanceof ValueOfQuestionOperator);
        assertTrue("The getting Expression was not a instance of UnaryExpression",
            ((BinaryExpression) ((MultiExpression) testExpression).getExpressions()
                .get(1)).getExpressions().get(1) instanceof UnaryExpression);
        assertTrue("The getting Operator was not an instanceof ValueOperator",
            ((BinaryExpression) ((MultiExpression) testExpression).getExpressions()
                .get(1)).getExpressions().get(1).getOperator() instanceof ValueOperator);

        // Test with values
        testValueOfExpressionDTO.setQuestionId(testQuestion.getId());
        testValueExpressionDTO.setValue(testValue.toString());
        testValueOfScoreExpressionDTO.setScoreId(testScore.getId());

        testExpression = testExpressionDTO.toExpression(operatorDao, questionDao, scoreDao);
        assertEquals("The getting Operator was not an instanceof ValueOfScoreOperator",
            ((UnaryExpression) ((MultiExpression) testExpression).getExpressions()
                .get(0)).getScore(), testScore);
        assertEquals("The getting Operator was not an instanceof ValueOfQuestionOperator",
            ((UnaryExpression) ((BinaryExpression) ((MultiExpression) testExpression).getExpressions()
                .get(1)).getExpressions().get(0)).getQuestion(), testQuestion);
        assertEquals("The getting Operator was not an instanceof ValueOperator",
            ((UnaryExpression) ((BinaryExpression) ((MultiExpression) testExpression).getExpressions()
                .get(1)).getExpressions().get(1)).getValue(), testValue);

        // Counter Operator
        testExpressionDTO = new ExpressionDTO();
        testExpressionDTO.setOperatorId(14L);

        // Two ValueExpressions for binary Operators
        Double valueLeft = random.nextDouble();
        Double valueRight = random.nextDouble();
        ExpressionDTO testValueLeftExpressionDTO = new ExpressionDTO();
        testValueLeftExpressionDTO.setOperatorId(6L);
        testValueLeftExpressionDTO.setValue(valueLeft.toString());
        ExpressionDTO testValueRightExpressionDTO = new ExpressionDTO();
        testValueRightExpressionDTO.setOperatorId(6L);
        testValueRightExpressionDTO.setValue(valueRight.toString());

        // List with two UnaryExpressions
        List<ExpressionDTO> unaryExpressions = new ArrayList<>();
        unaryExpressions.add(testValueLeftExpressionDTO);
        unaryExpressions.add(testValueRightExpressionDTO);

        // Greater Operator
        ExpressionDTO testGreaterExpressionDTO = new ExpressionDTO();
        testGreaterExpressionDTO.setOperatorId(8L);
        testGreaterExpressionDTO.setExpressions(unaryExpressions);

        // Less Operator
        ExpressionDTO testLessExpressionDTO = new ExpressionDTO();
        testLessExpressionDTO.setOperatorId(10L);
        testLessExpressionDTO.setExpressions(unaryExpressions);

        // GreaterEquals Operator
        ExpressionDTO testGreaterEqualsExpressionDTO = new ExpressionDTO();
        testGreaterEqualsExpressionDTO.setOperatorId(9L);
        testGreaterEqualsExpressionDTO.setExpressions(unaryExpressions);

        // LessEquals Operator
        ExpressionDTO testLessEqualsExpressionDTO = new ExpressionDTO();
        testLessEqualsExpressionDTO.setOperatorId(11L);
        testLessEqualsExpressionDTO.setExpressions(unaryExpressions);

        // Equals Operator
        ExpressionDTO testEqualsExpressionDTO = new ExpressionDTO();
        testEqualsExpressionDTO.setOperatorId(12L);
        testEqualsExpressionDTO.setExpressions(unaryExpressions);

        // Different Operator
        ExpressionDTO testDifferentExpressionDTO = new ExpressionDTO();
        testDifferentExpressionDTO.setOperatorId(13L);
        testDifferentExpressionDTO.setExpressions(unaryExpressions);

        // Set all boolean binary Operators in Counter Expression
        List<ExpressionDTO> counterExpressions = new ArrayList<>();
        counterExpressions.add(testGreaterExpressionDTO);
        counterExpressions.add(testLessExpressionDTO);
        counterExpressions.add(testGreaterEqualsExpressionDTO);
        counterExpressions.add(testLessEqualsExpressionDTO);
        counterExpressions.add(testEqualsExpressionDTO);
        counterExpressions.add(testDifferentExpressionDTO);
        testExpressionDTO.setExpressions(counterExpressions);

        // Counter with Greater, Less, GreaterEquals, LessEquals, Equals and Different
        testExpression = testExpressionDTO.toExpression(operatorDao, questionDao, scoreDao);
        assertTrue("The getting Expression was not a instance of MultiExpression",
            testExpression instanceof MultiExpression);
        assertTrue("The getting Operator was not an instanceof Counter",
            testExpression.getOperator() instanceof Counter);
        assertTrue("The getting Expression was not a instance of BinaryExpression",
            ((MultiExpression) testExpression).getExpressions().get(0) instanceof BinaryExpression);
        assertTrue("The getting Operator was not an instanceof Greater",
            ((MultiExpression) testExpression).getExpressions().get(0)
                .getOperator() instanceof Greater);
        assertTrue("The getting Expression was not a instance of BinaryExpression",
            ((MultiExpression) testExpression).getExpressions().get(1) instanceof BinaryExpression);
        assertTrue("The getting Operator was not an instanceof Less",
            ((MultiExpression) testExpression).getExpressions().get(1)
                .getOperator() instanceof Less);
        assertTrue("The getting Expression was not a instance of BinaryExpression",
            ((MultiExpression) testExpression).getExpressions().get(2) instanceof BinaryExpression);
        assertTrue("The getting Operator was not an instanceof GreaterEquals",
            ((MultiExpression) testExpression).getExpressions().get(2)
                .getOperator() instanceof GreaterEquals);
        assertTrue("The getting Expression was not a instance of BinaryExpression",
            ((MultiExpression) testExpression).getExpressions().get(3) instanceof BinaryExpression);
        assertTrue("The getting Operator was not an instanceof LessEquals",
            ((MultiExpression) testExpression).getExpressions().get(3)
                .getOperator() instanceof LessEquals);
        assertTrue("The getting Expression was not a instance of BinaryExpression",
            ((MultiExpression) testExpression).getExpressions().get(4) instanceof BinaryExpression);
        assertTrue("The getting Operator was not an instanceof Equals",
            ((MultiExpression) testExpression).getExpressions().get(4)
                .getOperator() instanceof Equals);
        assertTrue("The getting Expression was not a instance of BinaryExpression",
            ((MultiExpression) testExpression).getExpressions().get(5) instanceof BinaryExpression);
        assertTrue("The getting Operator was not an instanceof Different",
            ((MultiExpression) testExpression).getExpressions().get(5)
                .getOperator() instanceof Different);

        // Minimum Operator
        testExpressionDTO = new ExpressionDTO();
        testExpressionDTO.setOperatorId(18L);

        // Minus Operator
        ExpressionDTO testMinusExpressionDTO = new ExpressionDTO();
        testMinusExpressionDTO.setOperatorId(2L);
        testMinusExpressionDTO.setExpressions(unaryExpressions);

        // Divide Operator
        ExpressionDTO testDivideExpressionDTO = new ExpressionDTO();
        testDivideExpressionDTO.setOperatorId(4L);
        testDivideExpressionDTO.setExpressions(unaryExpressions);

        // Set Minus, Divide and two UnaryExpressions in MinimumExpression
        List<ExpressionDTO> minimumExpressions = new ArrayList<>();
        minimumExpressions.add(testMinusExpressionDTO);
        minimumExpressions.add(testDivideExpressionDTO);
        minimumExpressions.add(testValueLeftExpressionDTO);
        minimumExpressions.add(testValueRightExpressionDTO);
        testExpressionDTO.setExpressions(minimumExpressions);

        // Minimum with Minus, Plus and two UnaryExpressions
        testExpression = testExpressionDTO.toExpression(operatorDao, questionDao, scoreDao);
        assertTrue("The getting Expression was not a instance of MultiExpression",
            testExpression instanceof MultiExpression);
        assertTrue("The getting Operator was not an instanceof Minimum",
            testExpression.getOperator() instanceof Minimum);
        assertTrue("The getting Expression was not a instance of BinaryExpression",
            ((MultiExpression) testExpression).getExpressions().get(0) instanceof BinaryExpression);
        assertTrue("The getting Operator was not an instanceof Minus",
            ((MultiExpression) testExpression).getExpressions().get(0)
                .getOperator() instanceof Minus);
        assertTrue("The getting Expression was not a instance of BinaryExpression",
            ((MultiExpression) testExpression).getExpressions().get(1) instanceof BinaryExpression);
        assertTrue("The getting Operator was not an instanceof Divide",
            ((MultiExpression) testExpression).getExpressions().get(1)
                .getOperator() instanceof Divide);
        assertTrue("The getting Expression was not a instance of UnaryExpression",
            ((MultiExpression) testExpression).getExpressions().get(2) instanceof UnaryExpression);
        assertTrue("The getting Expression was not a instance of UnaryExpression",
            ((MultiExpression) testExpression).getExpressions().get(3) instanceof UnaryExpression);

        // Average Operator
        testExpressionDTO = new ExpressionDTO();
        testExpressionDTO.setOperatorId(15L);
        testExpressionDTO.setExpressions(minimumExpressions);

        // Average with Minus, Plus and two UnaryExpressions
        testExpression = testExpressionDTO.toExpression(operatorDao, questionDao, scoreDao);
        assertTrue("The getting Expression was not a instance of MultiExpression",
            testExpression instanceof MultiExpression);
        assertTrue("The getting Operator was not an instanceof Average",
            testExpression.getOperator() instanceof Average);
        assertTrue("The getting Expression was not a instance of BinaryExpression",
            ((MultiExpression) testExpression).getExpressions().get(0) instanceof BinaryExpression);
        assertTrue("The getting Operator was not an instanceof Minus",
            ((MultiExpression) testExpression).getExpressions().get(0)
                .getOperator() instanceof Minus);
        assertTrue("The getting Expression was not a instance of BinaryExpression",
            ((MultiExpression) testExpression).getExpressions().get(1) instanceof BinaryExpression);
        assertTrue("The getting Operator was not an instanceof Divide",
            ((MultiExpression) testExpression).getExpressions().get(1)
                .getOperator() instanceof Divide);
        assertTrue("The getting Expression was not a instance of UnaryExpression",
            ((MultiExpression) testExpression).getExpressions().get(2) instanceof UnaryExpression);
        assertTrue("The getting Expression was not a instance of UnaryExpression",
            ((MultiExpression) testExpression).getExpressions().get(3) instanceof UnaryExpression);

        // Sum Operator
        testExpressionDTO = new ExpressionDTO();
        testExpressionDTO.setOperatorId(7L);
        testExpressionDTO.setExpressions(minimumExpressions);

        // Sum with Minus, Plus and two UnaryExpressions
        testExpression = testExpressionDTO.toExpression(operatorDao, questionDao, scoreDao);
        assertTrue("The getting Expression was not a instance of MultiExpression",
            testExpression instanceof MultiExpression);
        assertTrue("The getting Operator was not an instanceof Sum",
            testExpression.getOperator() instanceof Sum);
        assertTrue("The getting Expression was not a instance of BinaryExpression",
            ((MultiExpression) testExpression).getExpressions().get(0) instanceof BinaryExpression);
        assertTrue("The getting Operator was not an instanceof Minus",
            ((MultiExpression) testExpression).getExpressions().get(0)
                .getOperator() instanceof Minus);
        assertTrue("The getting Expression was not a instance of BinaryExpression",
            ((MultiExpression) testExpression).getExpressions().get(1) instanceof BinaryExpression);
        assertTrue("The getting Operator was not an instanceof Divide",
            ((MultiExpression) testExpression).getExpressions().get(1)
                .getOperator() instanceof Divide);
        assertTrue("The getting Expression was not a instance of UnaryExpression",
            ((MultiExpression) testExpression).getExpressions().get(2) instanceof UnaryExpression);
        assertTrue("The getting Expression was not a instance of UnaryExpression",
            ((MultiExpression) testExpression).getExpressions().get(3) instanceof UnaryExpression);
    }
}
