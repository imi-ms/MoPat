package de.imi.mopat.dao.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.dao.AnswerDao;
import de.imi.mopat.dao.BundleDao;
import de.imi.mopat.dao.EncounterDao;
import de.imi.mopat.dao.QuestionDao;
import de.imi.mopat.dao.QuestionnaireDao;
import de.imi.mopat.dao.ResponseDao;
import de.imi.mopat.model.Answer;
import de.imi.mopat.model.Bundle;
import de.imi.mopat.model.BundleTest;
import de.imi.mopat.model.DateAnswer;
import de.imi.mopat.model.DateAnswerTest;
import de.imi.mopat.model.Encounter;
import de.imi.mopat.model.EncounterTest;
import de.imi.mopat.model.FreetextAnswer;
import de.imi.mopat.model.FreetextAnswerTest;
import de.imi.mopat.model.ImageAnswer;
import de.imi.mopat.model.ImageAnswerTest;
import de.imi.mopat.model.PointOnImage;
import de.imi.mopat.model.PointOnImageTest;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.QuestionTest;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.QuestionnaireTest;
import de.imi.mopat.model.Response;
import de.imi.mopat.model.SelectAnswerTest;
import de.imi.mopat.model.SliderAnswer;
import de.imi.mopat.model.SliderAnswerTest;
import de.imi.mopat.model.SliderFreetextAnswer;
import de.imi.mopat.model.SliderFreetextAnswerTest;
import de.imi.mopat.utils.Helper;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
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
public class ResponseDaoImplTest {

    private static final Random random = new Random();
    @Autowired
    ResponseDao testResponseDao;
    @Autowired
    AnswerDao answerDao;
    @Autowired
    QuestionDao questionDao;
    @Autowired
    QuestionnaireDao questionnaireDao;
    @Autowired
    BundleDao bundleDao;
    @Autowired
    EncounterDao encounterDao;

    /**
     * Test of {@link ResponseDaoImpl#getResponseByAnswerInEncounter}.<br> Valid input: valid
     * {@link Answer}-Id and {@link Encounter}-Id without {@link Response} and with
     * {@link Response}
     */
    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "MoPatUserDetailsService")
    public void testGetResponseByAnswerInEncounter() {
        Question testQuestion = QuestionTest.getNewValidQuestion();
        Answer testAnswer = SelectAnswerTest.getNewValidSelectAnswer();
        testQuestion.addAnswer(testAnswer);
        questionnaireDao.merge(testQuestion.getQuestionnaire());
        questionDao.merge(testQuestion);
        Bundle testBundle = BundleTest.getNewValidBundle();
        bundleDao.merge(testBundle);
        Encounter testEncounter = EncounterTest.getNewValidEncounter(testBundle);
        encounterDao.merge(testEncounter);
        assertNull("The getting Response was not null although there was no Response",
            testResponseDao.getResponseByAnswerInEncounter(testAnswer.getId(),
                testEncounter.getId()));

        Response testResponse = new Response(testAnswer, testEncounter);
        testResponseDao.merge(testResponse);
        assertEquals("", testResponse,
            testResponseDao.getResponseByAnswerInEncounter(testAnswer.getId(),
                testEncounter.getId()));
        testResponseDao.remove(testResponse);
    }

    /**
     * Test of {@link ResponseDaoImpl#getElementById}.<br> Valid input: random {@link Response}
     * with
     * <code>null</code>-values and correct values
     */
    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "MoPatUserDetailsService")
    public void testGetResponseNullValues() {
        Questionnaire testQuestionnaire = QuestionnaireTest.getNewValidQuestionnaire();
        questionnaireDao.merge(testQuestionnaire);
        Question testQuestion = QuestionTest.getNewValidQuestion(testQuestionnaire);
        questionDao.merge(testQuestion);
        SliderFreetextAnswer testAnswer = SliderFreetextAnswerTest.getNewValidSliderFreetextAnswer();
        testAnswer.setQuestion(testQuestion);
        answerDao.merge(testAnswer);
        Bundle testBundle = BundleTest.getNewValidBundle();
        bundleDao.merge(testBundle);
        Encounter testEncounter = EncounterTest.getNewValidEncounter(testBundle);
        encounterDao.merge(testEncounter);
        Response testResponse = new Response(testAnswer, testEncounter);
        testResponseDao.merge(testResponse);

        Response databaseResponse = testResponseDao.getElementById(testResponse.getId());
        assertEquals("The getting response was not the expected one", testResponse,
            databaseResponse);
        assertNull("The getting customtext was not null although it was not set before merging",
            databaseResponse.getCustomtext());
        assertNull("The getting date was not null although it was not set before merging",
            databaseResponse.getDate());
        assertNull("The getting value was not null although it was not set before merging",
            databaseResponse.getValue());
        assertTrue("The getting pointsOnImage was not empty although it was not set before merging",
            databaseResponse.getPointsOnImage().isEmpty());
        assertEquals("The getting answer was not the expected one", testAnswer,
            databaseResponse.getAnswer());
        assertEquals("The getting encounter was not the expected one", testEncounter,
            databaseResponse.getEncounter());

        testResponseDao.remove(testResponse);
        encounterDao.remove(testEncounter);
        bundleDao.remove(testBundle);
        answerDao.remove(testAnswer);
        questionDao.remove(testQuestion);
        questionnaireDao.remove(testQuestionnaire);
    }

    /**
     * Test of {@link ResponseDaoImpl#getElementById}.<br> Valid input: random {@link Response} with
     * Date value
     */
    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "MoPatUserDetailsService")
    public void testGetDateResponse() {
        Questionnaire testQuestionnaire = QuestionnaireTest.getNewValidQuestionnaire();
        questionnaireDao.merge(testQuestionnaire);
        Question testQuestion = QuestionTest.getNewValidQuestion(testQuestionnaire);
        questionDao.merge(testQuestion);
        DateAnswer testAnswer = DateAnswerTest.getNewValidDateAnswer(
            new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis() + 1000000L));
        testAnswer.setQuestion(testQuestion);
        answerDao.merge(testAnswer);
        Bundle testBundle = BundleTest.getNewValidBundle();
        bundleDao.merge(testBundle);
        Encounter testEncounter = EncounterTest.getNewValidEncounter(testBundle);
        encounterDao.merge(testEncounter);
        Response testResponse = new Response(testAnswer, testEncounter);
        testResponseDao.merge(testResponse);

        testResponse.setDate(new Date(System.currentTimeMillis()));
        testResponseDao.merge(testResponse);
        Response databaseResponse = testResponseDao.getElementById(testResponse.getId());

        assertEquals("The getting response was not the expected one", testResponse,
            databaseResponse);
        assertNull("The getting customtext was not the expected one",
            databaseResponse.getCustomtext());
        assertEquals("The getting date was not the expected one", testResponse.getDate(),
            databaseResponse.getDate());
        assertNull("The getting value was not the expected one", databaseResponse.getValue());
        assertTrue("The getting pointsOnImage the expected one",
            databaseResponse.getPointsOnImage().isEmpty());
        assertEquals("The getting answer was not the expected one", testAnswer,
            databaseResponse.getAnswer());
        assertEquals("The getting encounter was not the expected one", testEncounter,
            databaseResponse.getEncounter());

        testResponseDao.remove(testResponse);
        encounterDao.remove(testEncounter);
        bundleDao.remove(testBundle);
        answerDao.remove(testAnswer);
        questionDao.remove(testQuestion);
        questionnaireDao.remove(testQuestionnaire);
    }

    /**
     * Test of {@link ResponseDaoImpl#getElementById}.<br> Valid input: random {@link Response} with
     * customtext value
     */
    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "MoPatUserDetailsService")
    public void testGetCustomtextResponse() {
        Questionnaire testQuestionnaire = QuestionnaireTest.getNewValidQuestionnaire();
        questionnaireDao.merge(testQuestionnaire);
        Question testQuestion = QuestionTest.getNewValidQuestion(testQuestionnaire);
        questionDao.merge(testQuestion);
        FreetextAnswer testAnswer = FreetextAnswerTest.getNewValidFreetextAnswer();
        testAnswer.setQuestion(testQuestion);
        answerDao.merge(testAnswer);
        Bundle testBundle = BundleTest.getNewValidBundle();
        bundleDao.merge(testBundle);
        Encounter testEncounter = EncounterTest.getNewValidEncounter(testBundle);
        encounterDao.merge(testEncounter);
        Response testResponse = new Response(testAnswer, testEncounter);
        testResponseDao.merge(testResponse);

        testResponse.setCustomtext(Helper.getRandomString(random.nextInt(500) + 1));
        testResponseDao.merge(testResponse);
        Response databaseResponse = testResponseDao.getElementById(testResponse.getId());

        assertEquals("The getting response was not the expected one", testResponse,
            databaseResponse);
        assertEquals("The getting customtext was not the expected one",
            testResponse.getCustomtext(), databaseResponse.getCustomtext());
        assertNull("The getting date was not the expected one", databaseResponse.getDate());
        assertNull("The getting value was not the expected one", databaseResponse.getValue());
        assertTrue("The getting pointsOnImage the expected one",
            databaseResponse.getPointsOnImage().isEmpty());
        assertEquals("The getting answer was not the expected one", testAnswer,
            databaseResponse.getAnswer());
        assertEquals("The getting encounter was not the expected one", testEncounter,
            databaseResponse.getEncounter());

        testResponseDao.remove(testResponse);
        encounterDao.remove(testEncounter);
        bundleDao.remove(testBundle);
        answerDao.remove(testAnswer);
        questionDao.remove(testQuestion);
        questionnaireDao.remove(testQuestionnaire);
    }

    /**
     * Test of {@link ResponseDaoImpl#getElementById}.<br> Valid input: random {@link Response} with
     * value
     */
    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "MoPatUserDetailsService")
    public void testGetValueResponse() {
        Questionnaire testQuestionnaire = QuestionnaireTest.getNewValidQuestionnaire();
        questionnaireDao.merge(testQuestionnaire);
        Question testQuestion = QuestionTest.getNewValidQuestion(testQuestionnaire);
        questionDao.merge(testQuestion);
        SliderAnswer testAnswer = SliderAnswerTest.getNewValidSliderAnswer();
        testAnswer.setQuestion(testQuestion);
        answerDao.merge(testAnswer);
        Bundle testBundle = BundleTest.getNewValidBundle();
        bundleDao.merge(testBundle);
        Encounter testEncounter = EncounterTest.getNewValidEncounter(testBundle);
        encounterDao.merge(testEncounter);
        Response testResponse = new Response(testAnswer, testEncounter);
        testResponseDao.merge(testResponse);

        testResponse.setValue(
            testAnswer.getMinValue() + 0.01 * random.nextInt(100) * (testAnswer.getMaxValue()
                - testAnswer.getMinValue()));
        testResponseDao.merge(testResponse);
        Response databaseResponse = testResponseDao.getElementById(testResponse.getId());

        assertEquals("The getting response was not the expected one", testResponse,
            databaseResponse);
        assertNull("The getting customtext was not the expected one",
            databaseResponse.getCustomtext());
        assertNull("The getting date was not the expected one", databaseResponse.getDate());
        assertEquals("The getting value was not the expected one", testResponse.getValue(),
            databaseResponse.getValue());
        assertTrue("The getting pointsOnImage the expected one",
            databaseResponse.getPointsOnImage().isEmpty());
        assertEquals("The getting answer was not the expected one", testAnswer,
            databaseResponse.getAnswer());
        assertEquals("The getting encounter was not the expected one", testEncounter,
            databaseResponse.getEncounter());

        testResponseDao.remove(testResponse);
        encounterDao.remove(testEncounter);
        bundleDao.remove(testBundle);
        answerDao.remove(testAnswer);
        questionDao.remove(testQuestion);
        questionnaireDao.remove(testQuestionnaire);
    }

    /**
     * Test of {@link ResponseDaoImpl#getElementById}.<br> Valid input: random {@link Response} with
     * pointsOnImage list
     */
    @Test
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "MoPatUserDetailsService")
    public void testGetPointsOnImageResponse() {
        Questionnaire testQuestionnaire = QuestionnaireTest.getNewValidQuestionnaire();
        questionnaireDao.merge(testQuestionnaire);
        Question testQuestion = QuestionTest.getNewValidQuestion(testQuestionnaire);
        questionDao.merge(testQuestion);
        ImageAnswer testAnswer = ImageAnswerTest.getNewValidImageAnswer();
        testAnswer.setQuestion(testQuestion);
        answerDao.merge(testAnswer);
        Bundle testBundle = BundleTest.getNewValidBundle();
        bundleDao.merge(testBundle);
        Encounter testEncounter = EncounterTest.getNewValidEncounter(testBundle);
        encounterDao.merge(testEncounter);
        Response testResponse = new Response(testAnswer, testEncounter);
        testResponseDao.merge(testResponse);

        List<PointOnImage> testPointsOnImage = new ArrayList<>();
        int countPoints = random.nextInt(25) + 1;
        for (int i = 0; i < countPoints; i++) {
            testPointsOnImage.add(PointOnImageTest.getNewValidPointOnImage());
        }
        testResponse.setPointsOnImage(testPointsOnImage);
        testResponseDao.merge(testResponse);
        Response databaseResponse = testResponseDao.getElementById(testResponse.getId());

        assertEquals("The getting response was not the expected one", testResponse,
            databaseResponse);
        assertNull("The getting customtext was not the expected one",
            databaseResponse.getCustomtext());
        assertNull("The getting date was not the expected one", databaseResponse.getDate());
        assertNull("The getting value was not the expected one", databaseResponse.getValue());
        assertEquals("The getting pointsOnImage the expected one", testResponse.getPointsOnImage(),
            databaseResponse.getPointsOnImage());
        assertEquals("The getting answer was not the expected one", testAnswer,
            databaseResponse.getAnswer());
        assertEquals("The getting encounter was not the expected one", testEncounter,
            databaseResponse.getEncounter());

        testResponseDao.remove(testResponse);
        encounterDao.remove(testEncounter);
        bundleDao.remove(testBundle);
        answerDao.remove(testAnswer);
        questionDao.remove(testQuestion);
        questionnaireDao.remove(testQuestionnaire);
    }
}
