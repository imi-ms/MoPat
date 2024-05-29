package de.imi.mopat.model.score;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 */
public class ExpressionTest {

    private static final Random random = new Random();
    private Expression testExpression;

    public ExpressionTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Returns a new valid {@link Expression}.
     *
     * @return New valid Expression
     */
    public static Expression getNewValidExpression() {
        switch (random.nextInt(3)) {
            case 0:
                return BinaryExpressionTest.getNewValidBinaryExpression();
            case 1:
                return UnaryExpressionTest.getNewValidUnaryExpression();
            default:
                return MultiExpressionTest.getNewValidMultiExpression();
        }
    }

    @Before
    public void setUp() {
        testExpression = ExpressionTest.getNewValidExpression();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of {@link Expression#setParent} and {@link Expression#getParent} methods.<br> Valid
     * input: random {@link Expression}
     */
    @Test
    public void testSetAndGetParent() {
        Expression parent = ExpressionTest.getNewValidExpression();
        testExpression.setParent(parent);
        assertNotNull(
            "Setting parent failed. The returned value was null although not-null value was expected.",
            testExpression.getParent());
        assertEquals("Setting parent failed. The returned value didn't match the expected value.",
            parent, testExpression.getParent());
    }

    /**
     * Test of {@link Expression#includesScore}.<br> Valid input: random {@link UnaryExpression},
     * random {@link BinaryExpression} and random {@link MultiExpression}
     */
    @Test
    public void testIncludesScore() {
        testExpression = UnaryExpressionTest.getNewValidUnaryExpression();
        Score testScore = ScoreTest.getNewValidScore();
        assertFalse(
            "The includesScore method returned true although the given Score was not set before (unaryExpression)",
            testExpression.includesScore(testScore));
        ((UnaryExpression) testExpression).setScore(testScore);
        assertTrue(
            "The includesScore method returned false although the given score was set right before (unaryExpression)",
            testExpression.includesScore(testScore));

        testExpression = BinaryExpressionTest.getNewValidBinaryExpression();
        List<Expression> expressions = new ArrayList<>();
        UnaryExpression unaryExpression1 = UnaryExpressionTest.getNewValidUnaryExpression();
        UnaryExpression unaryExpression2 = UnaryExpressionTest.getNewValidUnaryExpression();
        unaryExpression1.setScore(ScoreTest.getNewValidScore());
        unaryExpression2.setScore(ScoreTest.getNewValidScore());
        expressions.add(unaryExpression1);
        expressions.add(unaryExpression2);
        ((BinaryExpression) testExpression).setExpressions(expressions);
        assertFalse(
            "The includesScore method returned true although the given Score was not set before (binaryExpression)",
            testExpression.includesScore(testScore));
        unaryExpression2.setScore(testScore);
        expressions = new ArrayList<>();
        expressions.add(unaryExpression1);
        expressions.add(unaryExpression2);
        ((BinaryExpression) testExpression).setExpressions(expressions);
        assertTrue(
            "The includesScore method returned false although the given score was set right before (binaryExpression)",
            testExpression.includesScore(testScore));

        testExpression = MultiExpressionTest.getNewValidMultiExpression();
        int countMultiExpressions = random.nextInt(5) + 3;
        expressions = new ArrayList<>();
        for (int i = 0; i < countMultiExpressions; i++) {
            unaryExpression1 = UnaryExpressionTest.getNewValidUnaryExpression();
            unaryExpression1.setScore(ScoreTest.getNewValidScore());
            expressions.add(unaryExpression1);
        }
        ((MultiExpression) testExpression).setExpressions(expressions);
        assertFalse(
            "The includesScore method returned true although the given Score was not set before (multiExpression)",
            testExpression.includesScore(testScore));
        unaryExpression1 = UnaryExpressionTest.getNewValidUnaryExpression();
        unaryExpression1.setScore(testScore);
        expressions.add(unaryExpression1);
        assertTrue(
            "The includesScore method returned false although the given score was set right before (multiExpression)",
            testExpression.includesScore(testScore));
    }
}
