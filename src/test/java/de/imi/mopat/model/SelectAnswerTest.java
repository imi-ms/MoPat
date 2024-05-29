package de.imi.mopat.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import de.imi.mopat.utils.Helper;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 */
public class SelectAnswerTest {

    private static final Random random = new Random();
    private SelectAnswer testSelectAnswer;

    public SelectAnswerTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Returns a valid new SelectAnswer
     *
     * @return Returns a valid new SelectAnswer
     */
    public static SelectAnswer getNewValidSelectAnswer() {
        Question question = QuestionTest.getNewValidQuestion();
        Boolean isEnabled = random.nextBoolean();
        Boolean isOther = random.nextBoolean();
        Map<String, String> localizedLabel = new HashMap<>();
        int count = random.nextInt(50) + 1;
        for (int i = 0; i < count; i++) {
            localizedLabel.put(Helper.getRandomLocale(),
                Helper.getRandomString(random.nextInt(50) + 1));
        }

        SelectAnswer selectAnswer = new SelectAnswer(question, isEnabled, localizedLabel, isOther);

        return selectAnswer;
    }

    @Before
    public void setUp() {
        testSelectAnswer = getNewValidSelectAnswer();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of {@link SelectAnswer#cloneWithoutReferences}.<br> Valid input: valid
     * {@link SelectAnswer}
     */
    @Test
    public void testCloneWithoutReferences() {
        SelectAnswer testClone = testSelectAnswer.cloneWithoutReferences();
        assertEquals("The getting isEnabled was not the expected one",
            testSelectAnswer.getIsEnabled(), testClone.getIsEnabled());
        assertEquals("The getting isEnabled was not the expected one",
            testSelectAnswer.getLocalizedLabel(), testClone.getLocalizedLabel());
    }

    /**
     * Test of {@link SelectAnswer#getLocalizedLabel} and
     * {@link SelectAnswer#setLocalizedLabel}.<br> Invalid input: <code>null</code>, empty Map<br>
     * Valid input: not empty Map
     */
    @Test
    public void testGetAndSetLocalizedLabel() {
        Map<String, String> testLocalizedLabel = null;
        Throwable e = null;
        try {
            testSelectAnswer.setLocalizedLabel(testLocalizedLabel);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set a null as localizedLabel", e instanceof AssertionError);

        testLocalizedLabel = new HashMap<>();
        e = null;
        try {
            testSelectAnswer.setLocalizedLabel(testLocalizedLabel);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set an empty Map as localizedLabel",
            e instanceof AssertionError);

        int count = random.nextInt(50) + 1;
        for (int i = 0; i < count; i++) {
            testLocalizedLabel.put(Helper.getRandomLocale(),
                Helper.getRandomString(random.nextInt(50) + 1));
        }
        testSelectAnswer.setLocalizedLabel(testLocalizedLabel);
        assertEquals("The getting localizedLabel was not the expected one", testLocalizedLabel,
            testSelectAnswer.getLocalizedLabel());
    }

    /**
     * Test of {@link SelectAnswer#getLocalizedAnswerLabelGroupedByCountry}.<br> Valid input: A map
     * of random AnswerLabels as {@link String} and a random Locale and three manually added Locales
     * and Answerlabels, that belong to one Country: de_CH, fr_Ch and it_CH
     */
    @Test
    public void testGetLocalizedAnswerLabelGroupedByCountry() {
        SortedMap<String, Map<String, String>> testMap = new TreeMap<>();
        Map<String, String> testLocalizedLabel = new HashMap<>();
        // Creating some Locales
        int countLocales = random.nextInt(10) + 1;
        String[] testLocales = new String[countLocales];
        for (int i = 0; i < countLocales; i++) {
            if (random.nextBoolean()) {
                testLocales[i] = Helper.getRandomLocale();
            } else {
                testLocales[i] = Helper.getRandomLocale().split("_")[1];
            }
        }

        // Adding some labels with country code CH and different language codes
        Map<String, String> testLocaleQuestionTextMap = new HashMap<>();
        String testQuestiontext = Helper.getRandomString(random.nextInt(50) + 1);
        testLocalizedLabel.put("de_CH", testQuestiontext);
        testLocaleQuestionTextMap.put("de_CH", testQuestiontext);
        testQuestiontext = Helper.getRandomString(random.nextInt(50) + 1);
        testLocalizedLabel.put("fr_CH", testQuestiontext);
        testLocaleQuestionTextMap.put("fr_CH", testQuestiontext);
        testQuestiontext = Helper.getRandomString(random.nextInt(50) + 1);
        testLocalizedLabel.put("it_CH", testQuestiontext);
        testLocaleQuestionTextMap.put("it_CH", testQuestiontext);
        testMap.put("CH", testLocaleQuestionTextMap);

        // Creating some labels
        int countLabels = random.nextInt(200) + 1;
        for (int i = 0; i < countLabels; i++) {
            int positionLocale = random.nextInt(countLocales);
            String testLocale = testLocales[positionLocale];
            String testLabel = Helper.getRandomString(random.nextInt(50) + 1);
            testLocalizedLabel.put(testLocale, testLabel);
            String testCountry = testLocale;
            if (testCountry.contains("_")) {
                testCountry = testCountry.split("_")[1];
            }
            if (testMap.containsKey(testCountry)) {
                testMap.get(testCountry).put(testLocale, testLabel);
            } else {
                Map<String, String> testLocaleLabelMap = new HashMap<>();
                testLocaleLabelMap.put(testLocale, testLabel);
                testMap.put(testCountry, testLocaleLabelMap);
            }
        }

        testSelectAnswer.setLocalizedLabel(testLocalizedLabel);
        assertEquals("The getting localizedAnswerLabelGroupedByCountry was not the expected one",
            testMap, testSelectAnswer.getLocalizedAnswerLabelGroupedByCountry());
    }

    /**
     * Test of {@link SelectAnswer#getValue} and {@link SelectAnswer#setValue}.<br> Valid input:
     * random Double
     */
    @Test
    public void testGetAndSetValue() {
        Double testValue = random.nextDouble();
        testSelectAnswer.setValue(testValue);
        assertEquals("The getting value was not the expected one", testValue,
            testSelectAnswer.getValue());
    }
}
