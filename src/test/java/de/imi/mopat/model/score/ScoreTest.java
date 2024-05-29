package de.imi.mopat.model.score;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import de.imi.mopat.helper.controller.StringUtilities;
import de.imi.mopat.model.EncounterTest;
import de.imi.mopat.model.ExportRule;
import de.imi.mopat.model.ExportRuleScore;
import de.imi.mopat.model.ExportRuleScoreTest;
import de.imi.mopat.model.QuestionTest;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.QuestionnaireTest;
import de.imi.mopat.model.dto.ScoreDTO;
import de.imi.mopat.utils.Helper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 *
 */
public class ScoreTest {

    Random random = new Random();
    Score score;
    Integer size;

    public ScoreTest() {
    }

    /**
     * Returns a valid new Score
     *
     * @return Returns a valid new Score
     */
    public static Score getNewValidScore() {
        return new Score();
    }

    @Before
    public void setUp() {
        score = ScoreTest.getNewValidScore();
        size = random.nextInt(25) + 2;
    }

    /**
     * Test of {@link Score#getFormula}<br> Valid input: random {@link Average}, random
     * {@link Counter} and random {@link Sum}
     */
    @Test
    public void testGetFormula() {
        List<MultiExpression> multiExpressions = new ArrayList<>();

        MultiExpression averageExpression = MultiExpressionTest.getNewValidMultiExpression();
        averageExpression.setOperator(AverageTest.getNewValidAverage());
        multiExpressions.add(averageExpression);

        MultiExpression counterExpression = MultiExpressionTest.getNewValidMultiExpression();
        counterExpression.setOperator(CounterTest.getNewValidCounter());
        multiExpressions.add(counterExpression);

        MultiExpression sumExpression = MultiExpressionTest.getNewValidMultiExpression();
        sumExpression.setOperator(SumTest.getNewValidSum());
        multiExpressions.add(sumExpression);

        for (MultiExpression multiExpression : multiExpressions) {
            MultiOperator multiOperator = (MultiOperator) multiExpression.getOperator();
            ArrayList<Expression> expressions = new ArrayList<>(), unaries = new ArrayList<>();
            BinaryOperator binaryOperator;
            BinaryExpression binary;
            UnaryExpression unary, unary1, unary2;

            StringBuffer formula = new StringBuffer();
            String operator = "";
            if (multiOperator instanceof Average) {
                operator = "average";
                formula.append("Average(");
                for (int i = 0; i < size; i++) {
                    if (i < size - 1 && random.nextBoolean()) {
                        binary = BinaryExpressionTest.getNewValidBinaryExpression();
                        unary1 = UnaryExpressionTest.getNewValidUnaryExpression(
                            QuestionTest.getNewValidQuestion());
                        unary2 = UnaryExpressionTest.getNewValidUnaryExpression(
                            QuestionTest.getNewValidQuestion());
                        Double value1 = random.nextDouble();
                        Double value2 = random.nextDouble();
                        unary1.setValue(value1);
                        unary2.setValue(value2);
                        unary1.setOperator(ValueOperatorTest.getNewValidValueOperator());
                        unary2.setOperator(ValueOperatorTest.getNewValidValueOperator());

                        unaries = new ArrayList<>();
                        unaries.add(unary1);
                        unaries.add(unary2);
                        binary.setExpressions(unaries);

                        binaryOperator = BinaryOperatorNumericTest.getNewValidBinaryOperatorNumeric();
                        binary.setOperator(binaryOperator);
                        if (binaryOperator instanceof Divide) {
                            formula.append("(" + value1 + " / " + value2 + ")");
                        }
                        if (binaryOperator instanceof Minus) {
                            formula.append("(" + value1 + " - " + value2 + ")");
                        }
                        if (binaryOperator instanceof Plus) {
                            formula.append("(" + value1 + " + " + value2 + ")");
                        }
                        expressions.add(binary);
                    } else {
                        //create at least the last expression as unary for missingExpression count
                        unary = UnaryExpressionTest.getNewValidUnaryExpression(
                            QuestionTest.getNewValidQuestion());
                        ValueOperator valueOperator = ValueOperatorTest.getNewValidValueOperator();
                        Double currentValue = random.nextDouble();
                        unary.setValue(currentValue);
                        unary.setOperator(valueOperator);
                        unary.setParent(multiExpression);
                        //last expression represents the number of missing values
                        if (i < size - 1) {
                            formula.append(currentValue);
                        }
                        expressions.add(unary);
                    }
                    if (i < size - 2) {
                        formula.append(", ");
                    }
                }
            } else if (multiOperator instanceof Counter) {
                operator = "counter";
                formula.append("Count of(");

                for (int i = 0; i < size; i++) {
                    binary = BinaryExpressionTest.getNewValidBinaryExpression();
                    unary1 = UnaryExpressionTest.getNewValidUnaryExpression(
                        QuestionTest.getNewValidQuestion());
                    unary2 = UnaryExpressionTest.getNewValidUnaryExpression(
                        QuestionTest.getNewValidQuestion());
                    Double value1 = random.nextDouble();
                    Double value2 = random.nextDouble();
                    unary1.setValue(value1);
                    unary2.setValue(value2);
                    unary1.setOperator(ValueOperatorTest.getNewValidValueOperator());
                    unary2.setOperator(ValueOperatorTest.getNewValidValueOperator());

                    unaries = new ArrayList<>();
                    unaries.add(unary1);
                    unaries.add(unary2);
                    binary.setExpressions(unaries);

                    binaryOperator = BinaryOperatorBooleanTest.getNewValidBinaryOperatorBoolean();
                    binary.setOperator(binaryOperator);
                    if (binaryOperator instanceof Equals) {
                        formula.append("(" + value1 + " == " + value2 + ")");
                    }
                    if (binaryOperator instanceof Different) {
                        formula.append("(" + value1 + " != " + value2 + ")");
                    }
                    if (binaryOperator instanceof Greater) {
                        formula.append("(" + value1 + " > " + value2 + ")");
                    }
                    if (binaryOperator instanceof GreaterEquals) {
                        formula.append("(" + value1 + " >= " + value2 + ")");
                    }
                    if (binaryOperator instanceof Less) {
                        formula.append("(" + value1 + " < " + value2 + ")");
                    }
                    if (binaryOperator instanceof LessEquals) {
                        formula.append("(" + value1 + " <= " + value2 + ")");
                    }

                    expressions.add(binary);

                    if (i < size - 1) {
                        formula.append(", ");
                    }
                }
            } else if (multiOperator instanceof Sum) {
                operator = "sum";
                formula.append("Sum(");

                for (int i = 0; i < size; i++) {
                    if (random.nextBoolean()) {
                        binary = BinaryExpressionTest.getNewValidBinaryExpression();
                        unary1 = UnaryExpressionTest.getNewValidUnaryExpression(
                            QuestionTest.getNewValidQuestion());
                        unary2 = UnaryExpressionTest.getNewValidUnaryExpression(
                            QuestionTest.getNewValidQuestion());
                        Double value1 = random.nextDouble();
                        Double value2 = random.nextDouble();
                        unary1.setValue(value1);
                        unary2.setValue(value2);
                        unary1.setOperator(ValueOperatorTest.getNewValidValueOperator());
                        unary2.setOperator(ValueOperatorTest.getNewValidValueOperator());
                        unary1.setParent(binary);
                        unary2.setParent(binary);

                        unaries = new ArrayList<>();
                        unaries.add(unary1);
                        unaries.add(unary2);
                        binary.setExpressions(unaries);

                        binaryOperator = BinaryOperatorNumericTest.getNewValidBinaryOperatorNumeric();
                        binary.setOperator(binaryOperator);
                        if (binaryOperator instanceof Divide) {
                            formula.append("(" + value1 + " / " + value2 + ")");
                        }
                        if (binaryOperator instanceof Minus) {
                            formula.append("(" + value1 + " - " + value2 + ")");
                        }
                        if (binaryOperator instanceof Plus) {
                            formula.append("(" + value1 + " + " + value2 + ")");
                        }
                        expressions.add(binary);
                    } else {
                        unary = UnaryExpressionTest.getNewValidUnaryExpression(
                            QuestionTest.getNewValidQuestion());
                        ValueOperator valueOperator = ValueOperatorTest.getNewValidValueOperator();
                        Double currentValue = random.nextDouble();
                        unary.setValue(currentValue);
                        unary.setOperator(valueOperator);
                        unary.setParent(multiExpression);
                        formula.append(currentValue);
                        expressions.add(unary);
                    }

                    if (i < size - 1) {
                        formula.append(", ");
                    }
                }
            }

            if (!(multiOperator instanceof Average)) {
                formula.append(")");
            } else if (size > 1) {
                formula.append(") ");
            } else {
                formula = new StringBuffer(); //in case that size is one, getFormula of averageOperator should return an empty string
            }
            multiExpression.setExpressions(expressions);
            score.setExpression(multiExpression);
            String testFormula;

            try {
                testFormula = score.getFormula(EncounterTest.getNewValidEncounter(),
                    Helper.getRandomLocale());
            } catch (Exception e) {
                testFormula = e.getMessage();
            }

            String expectedFormula = StringUtilities.stripHTML(formula.toString());

            if (testFormula == null) {
                expectedFormula = null;
            }

            assertNotNull("Getting formula of " + operator
                    + " score failed. The returned value was null although not null was expected.",
                testFormula);
            assertEquals("Getting formula of " + operator
                    + " score failed. The returned value didn't match the expected value.",
                expectedFormula, testFormula);
        }
    }

    /**
     * Test of {@link Score#evaluate}<br> Valid input: random {@link Average}, random
     * {@link Counter} and random {@link Sum}
     */
    @Test
    public void testEvaluate() {
        List<MultiExpression> multiExpressions = new ArrayList<>();

        MultiExpression averageExpression = MultiExpressionTest.getNewValidMultiExpression();
        averageExpression.setOperator(AverageTest.getNewValidAverage());
        multiExpressions.add(averageExpression);

        MultiExpression counterExpression = MultiExpressionTest.getNewValidMultiExpression();
        counterExpression.setOperator(CounterTest.getNewValidCounter());
        multiExpressions.add(counterExpression);

        MultiExpression sumExpression = MultiExpressionTest.getNewValidMultiExpression();
        sumExpression.setOperator(SumTest.getNewValidSum());
        multiExpressions.add(sumExpression);

        for (MultiExpression multiExpression : multiExpressions) {
            MultiOperator multiOperator = (MultiOperator) multiExpression.getOperator();
            List<Expression> expressions = new ArrayList<>();
            Double value = 0.0;
            Double sum = 0.0;
            Double counter = 0.0;
            size = random.nextInt(23);
            //fill expressions with unary-/binaryExpressions
            for (int i = 0; i < size; i++) {
                UnaryExpression unary;
                if (i < size - 1 && random.nextBoolean() || multiOperator instanceof Counter) {
                    //Create a binaryExpression
                    BinaryExpression binary = BinaryExpressionTest.getNewValidBinaryExpression();

                    UnaryExpression unary1 = UnaryExpressionTest.getNewValidUnaryExpression(
                        QuestionTest.getNewValidQuestion());
                    UnaryExpression unary2 = UnaryExpressionTest.getNewValidUnaryExpression(
                        QuestionTest.getNewValidQuestion());
                    Double value1 = random.nextDouble();
                    Double value2 = random.nextDouble();
                    unary1.setValue(value1);
                    unary2.setValue(value2);
                    unary1.setOperator(ValueOperatorTest.getNewValidValueOperator());
                    unary2.setOperator(ValueOperatorTest.getNewValidValueOperator());

                    List<Expression> unaries = new ArrayList<>();
                    unaries.add(unary1);
                    unaries.add(unary2);
                    unary1.setParent(binary);
                    unary2.setParent(binary);
                    binary.setExpressions(unaries);

                    BinaryOperator binaryOperator;
                    Double currentValue;
                    if (multiOperator instanceof Counter) {
                        //Boolean value
                        binaryOperator = BinaryOperatorBooleanTest.getNewValidBinaryOperatorBoolean();
                        if (binaryOperator instanceof Equals && value1 == value2) {
                            counter++;
                        }
                        if (binaryOperator instanceof Different && value1 != value2) {
                            counter++;
                        }
                        if (binaryOperator instanceof Greater && value1 > value2) {
                            counter++;
                        }
                        if (binaryOperator instanceof GreaterEquals && value1 >= value2) {
                            counter++;
                        }
                        if (binaryOperator instanceof Less && value1 < value2) {
                            counter++;
                        }
                        if (binaryOperator instanceof LessEquals && value1 <= value2) {
                            counter++;
                        }
                    } else {
                        //Numeric value
                        binaryOperator = BinaryOperatorNumericTest.getNewValidBinaryOperatorNumeric();
                        if (binaryOperator instanceof Divide) {
                            currentValue = value1 / value2;
                        } else if (binaryOperator instanceof Minus) {
                            currentValue = value1 - value2;
                        } else {
                            currentValue = value1 + value2;
                        }
                        sum += currentValue;
                        counter++;
                    }
                    binary.setOperator(binaryOperator);
                    expressions.add(binary);
                } //set the last expression as unaryExpression for averageOperator case
                else {
                    unary = UnaryExpressionTest.getNewValidUnaryExpression(
                        QuestionTest.getNewValidQuestion());
                    ValueOperator valueOperator = ValueOperatorTest.getNewValidValueOperator();
                    Double currentValue = random.nextDouble();
                    unary.setValue(currentValue);
                    unary.setOperator(valueOperator);
                    unary.setParent(multiExpression);
                    expressions.add(unary);

                    if (i < size - 1 && multiOperator instanceof Average) {
                        sum += currentValue;
                        counter++;
                    } else if (multiOperator instanceof Sum) {
                        sum += currentValue;
                    }
                }
            }

            String operator = "Not an operator";

            if (multiOperator instanceof Average) {
                value = sum / counter;
                if (value.isNaN() || size <= 1) {
                    value = null;
                }
                operator = "average";
            }
            if (multiOperator instanceof Counter) {
                value = counter;
                operator = "counter";
            }
            if (multiOperator instanceof Sum) {
                value = sum;
                operator = "sum";
            }

            if (size == 0) {
                value = null;
            }

            multiExpression.setExpressions(expressions);
            score.setExpression(multiExpression);
            Double testValue = (Double) score.evaluate(EncounterTest.getNewValidEncounter());

            if (value == null) {
                assertNull("Evaluating " + operator
                        + " score failed. The returned value was not null although null was expected.",
                    testValue);
            } else {
                assertNotNull("Evaluating " + operator
                        + " score failed. The returned value was null although not null was expected.",
                    testValue);
            }
            assertEquals("Evaluating " + operator
                    + " score failed. The returned value didn't match the expected value.", value,
                testValue);
        }
    }

    /**
     * Test of {@link Score#getName} and {@link Score#setName} methods.<br> Valid input:
     * <code>null</code>, random String
     */
    @Test
    public void testSetGetName() {
        String name = Helper.getRandomAlphabeticString(random.nextInt(50) + 1);
        score.setName(name);
        assertNotNull(
            "Setting name failed. The returned value was null although not-null value was expected",
            score.getName());
        assertEquals("Setting name failed. The returned value didn't match the expected value",
            name, score.getName());
        score.setName(null);
        assertEquals("The name was altered after setting it to null. There should be no changes.",
            name, score.getName());
    }

    /**
     * Test of {@link Score#getExpression} and {@link Score#setExpression}<br> Valid input: random
     * valid {@link Expression}.
     */
    @Test
    public void testSetGetExpression() {
        Expression expression = Mockito.spy(ExpressionTest.getNewValidExpression());
        Mockito.when(expression.getId()).thenReturn(Math.abs(random.nextLong()));
        score.setExpression(expression);
        Expression testExpression = score.getExpression();
        assertNotNull(
            "Setting expression failed. The returned value was null although not null was expected.",
            testExpression);
        assertEquals(
            "Setting expression failed. The returned value didn't match the expected value.",
            testExpression.getId(), expression.getId());
    }

    /**
     * Test of {@link Score#getQuestionnaire} and {@link Score#setQuestionnaire}<br> Valid input:
     * random valid {@link Questionnaire}.
     */
    @Test
    public void testSetGetQuestionnaire() {
        Questionnaire questionnaire = QuestionnaireTest.getNewValidQuestionnaire();
        score.setQuestionnaire(questionnaire);
        Questionnaire testQuestionnaire = score.getQuestionnaire();
        assertNotNull(
            "Setting questionnaire failed. The returned value was null although not-null value was expected",
            testQuestionnaire);
        assertEquals(
            "Setting questionnaire failed. The returned value didn't match the expected value",
            questionnaire, testQuestionnaire);
    }

    /**
     * Test of {@link Score#addExportRules}<br> Valid input: random set of
     * {@link ExportRule ExportRules}
     */
    @Test
    public void testAddExportRules() {
        Set<ExportRuleScore> exportRules = null;
        Throwable e = null;
        try {
            score.addExportRules(exportRules);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set null as the exportRules", e instanceof AssertionError);

        exportRules = new HashSet<>();
        for (int i = 0; i < size; i++) {
            exportRules.add(ExportRuleScoreTest.getNewValidExportRuleScore());
        }

        score.addExportRules(exportRules);
        Set<ExportRuleScore> testExportRules = score.getExportRules();
        assertNotNull(
            "Adding export rules failed. The returned value was null although not-null was expected",
            testExportRules);
        assertEquals(
            "Adding export rules failed. The returned value didn't match the expected value",
            exportRules, testExportRules);
    }

    /**
     * Test of {@link Score#addExportRule}<br> Invalid input: <code>null</code><br> Valid input:
     * random number of {@link ExportRuleScore ExportRuleScores}
     */
    @Test
    public void testAddExportRule() {
        ExportRuleScore testExportRuleScore = null;
        Throwable e = null;
        try {
            score.addExportRule(testExportRuleScore);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to add null to the ExportRules", e instanceof AssertionError);

        Set<ExportRuleScore> testSet = new HashSet<>();
        int count = random.nextInt(50) + 1;
        for (int i = 0; i < count; i++) {
            testExportRuleScore = ExportRuleScoreTest.getNewValidExportRuleScore();
            score.addExportRule(testExportRuleScore);
            testSet.add(testExportRuleScore);
        }
        score.addExportRule(testExportRuleScore);
        assertEquals("The same ExportRuleScore was added twice", testSet.size(),
            score.getExportRules().size());

        testExportRuleScore = ExportRuleScoreTest.getNewValidExportRuleScore();
        testExportRuleScore.removeScore();
        score.addExportRule(testExportRuleScore);
        testSet.add(testExportRuleScore);
        assertEquals("The getting set of ExportRuleScores was not the expected one", testSet,
            score.getExportRules());
    }

    /**
     * Test of {@link Score#removeExportRule}<br> Invalid input: <code>null</code><br> Valid input:
     * random number of {@link ExportRule ExportRules} that are in the set, an {@link ExportRule}
     * that is not in the set
     */
    @Test
    public void testRemoveExportRule() {
        ExportRuleScore testExportRule = null;
        Throwable e = null;
        try {
            score.removeExportRule(testExportRule);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to remove null from the ExportRules",
            e instanceof AssertionError);

        Set<ExportRuleScore> testSet = new HashSet<>();
        Set<ExportRuleScore> removeSet = new HashSet<>();
        int count = random.nextInt(50) + 1;
        for (int i = 0; i < count; i++) {
            testExportRule = ExportRuleScoreTest.getNewValidExportRuleScore();
            testSet.add(testExportRule);
            if (random.nextBoolean()) {
                removeSet.add(testExportRule);
            }
        }
        score.addExportRules(testSet);
        assertEquals("The getting set of ExportRules was not the expected one", testSet,
            score.getExportRules());
        testExportRule = ExportRuleScoreTest.getNewValidExportRuleScore();
        testExportRule.removeScore();
        removeSet.add(testExportRule);
        for (ExportRuleScore exportRuleScore : removeSet) {
            score.removeExportRule(exportRuleScore);
        }
        testSet.removeAll(removeSet);
        assertEquals("The getting set of ExportRules after removing was not the expected one",
            testSet, score.getExportRules());
        score.removeExportRule(ExportRuleScoreTest.getNewValidExportRuleScore());
        assertEquals(
            "The getting set of ExportRules after removing a ExportRule, that was not in the Set before, was altered",
            testSet, score.getExportRules());
    }

    /**
     * Test of {@link Score#isBooleanScore}<br> Valid input: {@link BinaryOperatorBoolean} and
     * {@link BinaryOperatorNumeric}
     */
    @Test
    public void testIsBooleanScore() {
        BinaryOperator binaryOperator = BinaryOperatorBooleanTest.getNewValidBinaryOperatorBoolean();
        BinaryExpression binaryExpression = BinaryExpressionTest.getNewValidBinaryExpressionWithUnaries(
            binaryOperator);
        score.setExpression(binaryExpression);
        assertTrue("IsBooleanScore failed. The returned value didn't match the expected value",
            score.isBooleanScore());

        binaryOperator = BinaryOperatorNumericTest.getNewValidBinaryOperatorNumeric();
        binaryExpression = BinaryExpressionTest.getNewValidBinaryExpressionWithUnaries(
            binaryOperator);
        score.setExpression(binaryExpression);
        assertFalse("IsBooleanScore failed. The returned value didn't match the expected value",
            score.isBooleanScore());
    }

    /**
     * Test of {@link Score#toScoreDTO}<br> Valid input: random valid {@link Score}
     */
    @Test
    public void testToScoreDTO() {
        ArrayList<Expression> expressions = new ArrayList<>();
        expressions.add(UnaryExpressionTest.getNewValidUnaryExpression());
        expressions.add(BinaryExpressionTest.getNewValidBinaryExpression());
        expressions.add(MultiExpressionTest.getNewValidMultiExpression());

        Score testScore = Mockito.spy(score);
        Mockito.when(testScore.getId()).thenReturn(Math.abs(random.nextLong()));
        Questionnaire questionnaire = Mockito.spy(QuestionnaireTest.getNewValidQuestionnaire());
        Mockito.when(questionnaire.getId()).thenReturn(Math.abs(random.nextLong()));
        testScore.setQuestionnaire(questionnaire);

        Score dependingScore = ScoreTest.getNewValidScore();
        dependingScore.setQuestionnaire(questionnaire);
        UnaryExpression dependingExpression = UnaryExpressionTest.getNewValidUnaryExpression();
        dependingExpression.setScore(testScore);
        dependingScore.setExpression(dependingExpression);

        for (Expression expression : expressions) {
            if (expression instanceof UnaryExpression) {
                UnaryExpression unaryExpression = Mockito.spy((UnaryExpression) expression);
                Mockito.when(unaryExpression.getId()).thenReturn(Math.abs(random.nextLong()));
                UnaryOperator unaryOperator = Mockito.spy(
                    UnaryOperatorTest.getNewValidUnaryOperator());
                Mockito.when(unaryOperator.getId()).thenReturn(Math.abs(random.nextLong()));
                unaryExpression.setOperator(unaryOperator);
                testScore.setExpression(unaryExpression);
            } else if (expression instanceof BinaryExpression) {
                BinaryExpression binaryExpression = Mockito.spy((BinaryExpression) expression);
                Mockito.when(binaryExpression.getId()).thenReturn(Math.abs(random.nextLong()));
                BinaryOperator binaryOperator = Mockito.spy(
                    BinaryOperatorTest.getNewValidBinaryOperator());
                Mockito.when(binaryOperator.getId()).thenReturn(Math.abs(random.nextLong()));
                binaryExpression.setOperator(binaryOperator);
                testScore.setExpression(binaryExpression);
            } else if (expression instanceof MultiExpression) {
                MultiExpression multiExpression = Mockito.spy((MultiExpression) expression);
                Mockito.when(multiExpression.getId()).thenReturn(Math.abs(random.nextLong()));
                MultiOperator multiOperator = Mockito.spy(
                    MultiOperatorTest.getNewValidMultiOperator());
                Mockito.when(multiOperator.getId()).thenReturn(Math.abs(random.nextLong()));
                multiExpression.setOperator(multiOperator);
                testScore.setExpression(multiExpression);
            }
        }

        String name = Helper.getRandomAlphabeticString(Math.abs(random.nextInt(19)));
        testScore.setName(name);

        ScoreDTO scoreDTO = testScore.toScoreDTO();

        assertNotNull(
            "ToScoreDTO failed. The returned value was null although not null value was expected.",
            scoreDTO);
        assertEquals("ToScoreDTO failed. The returned id didn't match the expected value.",
            testScore.getId(), scoreDTO.getId());
        assertEquals(
            "ToScoreDTO failed. The returned questionnaireId didn't match the expected value.",
            questionnaire.getId(), scoreDTO.getQuestionnaireId());
        assertEquals("ToScoreDTO failed. The returned name didn't match the expected value.", name,
            scoreDTO.getName());
        assertFalse(
            "ToScoreDTO failed. The returned hasExportRules didn't match the expected value.",
            scoreDTO.hasExportRules());

        testScore.addExportRule(ExportRuleScoreTest.getNewValidExportRuleScore());
        scoreDTO = testScore.toScoreDTO();
        assertTrue(
            "ToScoreDTO failed. The returned hasExportRules didn't match the expected value.",
            scoreDTO.hasExportRules());
    }

    /**
     * Test of {@link Score#equals}<br> Valid input: The same {@link Score}, another {@link Score},
     * <code>null</code> and a random {@link Object}
     */
    @Test
    public void equals() {
        Score testScore = ScoreTest.getNewValidScore();
        assertEquals(
            "Equals method failed. The compared scores didn't equal although they should do", score,
            score);
        assertNotEquals("Equals method failed. The compared scores equals although they shouldn't",
            score, testScore);
        assertNotEquals("Equals method failed. The score equals null although it wasn't null", null,
            score);
        assertNotEquals(
            "Equals method failed. The score equals an object that is instance of different class",
            score, new Object());
    }

    /**
     * Test of {@link Score#hashCode}<br> Valid input: The same {@link Score} in one HashSet
     */
    @Test
    public void testHashCode() {
        Set<Score> scoreSet = new HashSet<>();
        scoreSet.add(score);
        assertFalse(
            "HashCode method failed. The score was added to a set containing the given one already",
            scoreSet.add(score));
    }

    /**
     * Test of {@link Score#getDependingScores()}<br> Valid input: A {@link Score} with depending
     * {@link Score Scores}
     */
    @Test
    public void testGetDependingScores() {
        Questionnaire questionnaire = QuestionnaireTest.getNewValidQuestionnaire();
        score.setQuestionnaire(questionnaire);
        Set<Score> questionnaireScores = new HashSet<>();
        size = random.nextInt(25) + 15;

        List<Score> dependingScores = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            //create score and set it to questionnaire
            Score questionnaireScore = ScoreTest.getNewValidScore();
            questionnaireScore.setQuestionnaire(questionnaire);
            questionnaireScores.add(questionnaireScore);
            //create expression and set it to score
            Expression expression = ExpressionTest.getNewValidExpression();
            questionnaireScore.setExpression(expression);
            List<Expression> expressions = new ArrayList<>();
            //checkout which type the expressions has got and set testScore randomized
            if (expression instanceof UnaryExpression unaryExpression) {

                if (random.nextBoolean()) {
                    unaryExpression.setScore(score);
                    dependingScores.add(questionnaireScore);
                }
            }
            if (expression instanceof BinaryExpression binaryExpression) {
                Boolean depending = false;
                UnaryExpression unaryExpression = UnaryExpressionTest.getNewValidUnaryExpression();
                //first
                if (random.nextBoolean()) {
                    unaryExpression.setScore(score);
                    dependingScores.add(questionnaireScore);
                    depending = true;
                }
                expressions.add(unaryExpression);

                unaryExpression = UnaryExpressionTest.getNewValidUnaryExpression();
                //second
                if (random.nextBoolean() && !depending) {
                    unaryExpression.setScore(score);
                    dependingScores.add(questionnaireScore);
                    depending = true;
                }
                expressions.add(unaryExpression);
                binaryExpression.setExpressions(expressions);
            }
            if (expression instanceof MultiExpression multiExpression) {
                Boolean depending = false;
                for (int j = 0; j < random.nextInt(5) + 2; j++) {
                    UnaryExpression unaryExpression = UnaryExpressionTest.getNewValidUnaryExpression();
                    if (random.nextBoolean() && !depending) {
                        unaryExpression.setScore(score);
                        dependingScores.add(questionnaireScore);
                        depending = true;
                    }
                    expressions.add(unaryExpression);
                }
                multiExpression.setExpressions(expressions);
            }
            questionnaireScores.add(questionnaireScore);
        }
        questionnaire.setScores(questionnaireScores);
        List<Score> testDependingScores = score.getDependingScores();

        assertNotNull(
            "Get dependingScores failed. The returned value was null although not null was expected.",
            testDependingScores);
        assertEquals(
            "Get dependingScores failed. The returned value didn't match the expected value.",
            (Integer) dependingScores.size(), (Integer) testDependingScores.size());
    }
}
