package de.imi.mopat.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.spy;

import de.imi.mopat.model.dto.ResponseDTO;
import de.imi.mopat.utils.Helper;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

/**
 *
 */
public class ResponseTest {

    private static final Random random = new Random();
    private Response testResponse;

    public ResponseTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Returns a valid new {@link Response}
     *
     * @return Returns a valid new Response
     */
    public static Response getNewValidResponse() {
        Answer answer = AnswerTest.getNewValidRandomAnswer();
        Encounter encounter = EncounterTest.getNewValidEncounter();

        Response response = new Response(answer, encounter);

        return response;
    }

    /**
     * Returns a valid new {@link Response}
     *
     * @param encounter {@link Encounter} associated to this Response
     * @return Returns a valid new Response
     */
    public static Response getNewValidResponse(Encounter encounter) {
        Answer answer = AnswerTest.getNewValidRandomAnswer();

        Response response = new Response(answer, encounter);

        return response;
    }

    /**
     * Returns a valid new {@link Response}
     *
     * @param answer {@link Answer} associated to the returned response
     * @return Returns a valid new Response
     */
    public static Response getNewValidResponse(Answer answer) {
        Encounter encounter = EncounterTest.getNewValidEncounter();
        Response response = new Response(answer, encounter);
        return response;
    }

    @Before
    public void setUp() {
        testResponse = ResponseTest.getNewValidResponse();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testEquals() {
        HashSet<Response> testSet = new HashSet<>();
        testSet.add(testResponse);
        testSet.add(testResponse);
        assertEquals("It was possible to set the same Response in one set", 1, testSet.size());

        assertEquals("The Response was not equal to itself", testResponse, testResponse);
        assertNotEquals("The Response was equal to null", null, testResponse);
        Response otherResponse = getNewValidResponse();
        assertNotEquals("The Response was equal to a different Response", testResponse,
            otherResponse);
        Object otherObject = new Object();
        assertNotEquals("The Response was equal to a different Object", testResponse, otherObject);
    }

    @Test
    public void testHashCode() {
        Set<Response> responseSet = new HashSet<>();
        responseSet.add(testResponse);
        assertFalse(
            "HasCode method failed. The response was able to be added to a set which already should contain it.",
            responseSet.add(testResponse));
    }

    /**
     * Test of {@link Response#getCustomtext()} and {@link Response#getAnswer()} methods
     */
    @Test
    public void testSetGetCustomtext() {
        String customText = Helper.getRandomAlphanumericString(Math.abs(random.nextInt(50)));
        testResponse.setCustomtext(customText);
        String testCustomText = testResponse.getCustomtext();
        assertNotNull(
            "Setting custom text failed. The returned customText was null although not-null value was expected.",
            testCustomText);
        assertEquals(
            "Setting custom text failed. The returned customText didn't match the expected value.",
            customText, testCustomText);
    }

    /**
     * Test of {@link Response#setCustomtext(java.lang.String)} method <br> Invalid input:
     * <code>null</code>
     */
    @Test
    public void testSetCustomTextNull() {
        try {
            testResponse.setCustomtext(null);
            fail("Setting the customtext worked although the given param was null.");
        } catch (AssertionError ae) {
            //Nothing to do here since it was expected
        } catch (Throwable t) {
            fail("Wrong throwable thrown: " + t.getMessage());
        }
    }

    /**
     * Test of {@link Response#getValue()} and {@link Response#setValue(java.lang.Double)} methods
     */
    @Test
    public void testSetGetValue() {
        Double min = (double) random.nextInt(3) + 1;
        Double max = (double) random.nextInt(11) + min + 1;
        Double stepsize = max % min;
        SliderAnswer sliderAnswer = SliderAnswerTest.getNewValidSliderAnswer(min, max,
            stepsize == 0 ? 1 : stepsize);
        testResponse.setAnswer(sliderAnswer);
        //Set value null
        testResponse.setValue(null);
        assertNull("The value was not null after setting it so", testResponse.getValue());
        //Set random double
        double testValue = sliderAnswer.getMinValue() + random.nextInt(
            Math.abs(sliderAnswer.getStepsize().intValue()));
        testResponse.setValue(testValue);
        assertEquals("", (Double) testValue, testResponse.getValue());
    }

    /**
     * Test of {@link Response#setValue(java.lang.Double)} method<br> Invalid: The response's answer
     * isn't instance of {@link SliderAnswer}
     */
    @Test
    public void testSetValueNoSliderAnswer() {
        Answer answer;
        do {
            answer = AnswerTest.getNewValidRandomAnswer();
        } while (answer instanceof SliderAnswer);
        testResponse.setAnswer(answer);
        Double value = Math.abs(random.nextDouble());
        Throwable e = null;
        try {
            testResponse.setValue(value);
            fail(
                "Setting value worked although the response's answer wasn't instanceof SliderAnswer.");
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue(
            "Setting value worked although the response's answer wasn't instanceof SliderAnswer.",
            e instanceof AssertionError);
    }

    /**
     * Test of {@link Response#setValue(java.lang.Double)} method<br> Invalid: Bigger/smaller value
     * than min/max of the assigned {@link SliderAnswer}
     */
    @Test
    public void testSetValueBiggerSmallerThanMinMax() {
        SliderAnswer sliderAnswer = SliderAnswerTest.getNewValidSliderAnswer();
        testResponse.setAnswer(sliderAnswer);
        Double value = ((SliderAnswer) testResponse.getAnswer()).getMinValue() - 0.1;
        Throwable e = null;
        try {
            testResponse.setValue(value);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set a smaller value than the min value of the answer",
            e instanceof AssertionError);

        value = ((SliderAnswer) testResponse.getAnswer()).getMaxValue() + 0.1;
        e = null;
        try {
            testResponse.setValue(value);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set a bigger value than the max value of the answer",
            e instanceof AssertionError);
    }

    /**
     * Test of {@link Response#getAnswer()} and
     * {@link Response#setAnswer(de.imi.mopat.model.Answer)} methods
     */
    @Test
    public void testSetGetAnswer() {
        Answer answer = AnswerTest.getNewValidRandomAnswer();
        testResponse.setAnswer(answer);
        Answer testAnswer = testResponse.getAnswer();
        assertNotNull(
            "Setting answer failed. The returned answer was null although not-null value was expected.",
            testAnswer);
        assertEquals("Setting answer failed. The returned answer didn't match the expected value.",
            answer, testAnswer);
    }

    /**
     * Test of {@link Response#setAnswer(de.imi.mopat.model.Answer)} method<br> Invalid input:
     * <code>null</code>
     */
    @Test
    public void testSetAnswerNull() {
        try {
            testResponse.setAnswer(null);
            fail("Setting answer worked although the given param was null.");
        } catch (AssertionError ae) {
            //Nothing to do here since it was expected
        } catch (Throwable t) {
            fail("Wrong throwable thrown: " + t.getMessage());
        }
    }

    /**
     * Test of {@link Response#getEncounter()} and
     * {@link Response#setEncounter(de.imi.mopat.model.Encounter)} methods
     */
    @Test
    public void testSetGetEncounter() {
        Encounter encounter = EncounterTest.getNewValidEncounter();
        testResponse.setEncounter(encounter);
        Encounter testEncounter = testResponse.getEncounter();
        assertNotNull(
            "Setting encounter failed. The returned encounter was null although not-null value was expected.",
            testEncounter);
        assertEquals(
            "Setting encounter failed. The returned encounter didn't match the expected value.",
            encounter, testEncounter);
    }

    /**
     * Test of {@link Response#setEncounter(de.imi.mopat.model.Encounter)} method <br> Invalid
     * input: <code>null</code>
     */
    @Test
    public void testSetEncounterNull() {
        try {
            testResponse.setEncounter(null);
            fail("Setting encounter worked although the given param was null.");
        } catch (AssertionError ae) {
            //Nothing to do here since it was expected
        } catch (Throwable t) {
            fail("Wrong throwable thrown: " + t.getMessage());
        }
    }

    /**
     * Test of {@link Response#removeEncounter()} method
     */
    @Test
    public void testRemoveEncounter() {
        testResponse.removeEncounter();
        assertNull(
            "Removing encounter failed. The returned value was not-null although null was expected.",
            testResponse.getEncounter());
        testResponse.removeEncounter();
        assertNull("The Encounter was not null after removing it twice.",
            testResponse.getEncounter());
    }

    /**
     * Test of {@link Response#getDate()} and {@link Response#setDate(java.util.Date)} methods
     */
    @Test
    public void testSetGetDate() {
        Response testResponse = ResponseTest.getNewValidResponse(
            DateAnswerTest.getNewValidDateAnswer(
                new Date(System.currentTimeMillis() - Math.abs(random.nextLong())),
                new Date(System.currentTimeMillis() + Math.abs(random.nextLong()))));
        // Set date null
        testResponse.setDate(null);
        assertNull("The date was not null after setting it so", testResponse.getDate());
        // Set date
        Date testDate = new Date();
        testResponse.setDate(testDate);
        assertEquals("Setting date failed. The returned date didn't match the expected value.",
            testDate, testResponse.getDate());

        testResponse.setAnswer(DateAnswerTest.getNewValidDateAnswer(null, null));
        testDate = new Date(random.nextLong());
        testResponse.setDate(testDate);
        assertEquals("Setting date failed. The returned date didn't match the expected value.",
            testDate, testResponse.getDate());
    }

    /**
     * Test of {@link Response#setDate(java.util.Date)} method <br> Invalid: Calling instance of
     * class other than {@link DateAnswer}
     */
    @Test
    public void testSetDateNoDateAnswer() {
        try {
            testResponse.setDate(new Date());
            fail(
                "Setting the date worked although the response's answer wasn't instance of DateAnswer.");
        } catch (AssertionError ae) {
            //Nothing to do here since it was expected
        } catch (Throwable t) {
            fail("Wrong throwable thrown: " + t.getMessage());
        }
    }

    /**
     * Test of {@link Response#setDate(java.util.Date)} method
     */
    @Test
    public void testSetDateBeforeStartDate() {
        Date startDate = new Date();
        Date endDate = new Date(System.currentTimeMillis() + Math.abs(random.nextLong()));
        DateAnswer testAnswer = DateAnswerTest.getNewValidDateAnswer(startDate, endDate);
        Response testResponse = ResponseTest.getNewValidResponse(testAnswer);

        try {
            testResponse.setDate(
                new Date(System.currentTimeMillis() - Math.abs(random.nextLong())));
            fail("Setting the date worked although it was before the answer's startDate.");
        } catch (AssertionError ae) {
            //Nothing to do here since it was expected
        } catch (Throwable t) {
            fail("Wrong throwable thrown: " + t.getMessage());
        }

    }

    @Test
    public void testSetDateAfterEndDate() {
        Date startDate = new Date(System.currentTimeMillis() - Math.abs(random.nextLong()));
        Date endDate = new Date();
        DateAnswer testAnswer = DateAnswerTest.getNewValidDateAnswer(startDate, endDate);
        Response testResponse = ResponseTest.getNewValidResponse(testAnswer);

        try {
            testResponse.setDate(
                new Date(System.currentTimeMillis() + Math.abs(random.nextLong())));
            fail("Setting the date worked although it was after the answer's startDate.");
        } catch (AssertionError ae) {
            //Nothing to do here since it was expected
        } catch (Throwable t) {
            fail("Wrong throwable thrown: " + t.getMessage());
        }
    }

    /**
     * Test of {@link Response#getPointsOnImage} and {@link Response#setPointsOnImage}.<br> Valid
     * input: random list of {@link PointOnImage PointsOnImage}
     */
    @Test
    public void testGetAndSetPointsOnImage() {
        ImageAnswer testAnswer = ImageAnswerTest.getNewValidImageAnswer();
        testResponse.setAnswer(testAnswer);
        List<PointOnImage> testPointsOnImage = new ArrayList<>();
        int count = random.nextInt(25);
        for (int i = 0; i < count; i++) {
            testPointsOnImage.add(PointOnImageTest.getNewValidPointOnImage());
        }
        testResponse.setPointsOnImage(testPointsOnImage);
        assertEquals("The getting list of pointsOnImage was not the expected one",
            testResponse.getPointsOnImage(), testPointsOnImage);
    }

    /**
     * Test of {@link Response#toResponseDTO()} method
     */
    @Test
    public void testToResponseDTO() {
        Answer answer = spy(AnswerTest.getNewValidRandomAnswer());
        Mockito.when(answer.getId()).thenReturn(Math.abs(random.nextLong() + 1));
        Long answerId = answer.getId();
        Encounter encounter = EncounterTest.getNewValidEncounter();
        Response testResponse = new Response(answer, encounter);
        String customText = Helper.getRandomAlphanumericString(random.nextInt(100));
        testResponse.setCustomtext(customText);
        ResponseDTO responseDTO = testResponse.toResponseDTO();

        assertEquals(
            "ToRepsonseDTO method failed. The returned answerId didn't match the expected value",
            answerId, responseDTO.getAnswerId());
        assertNull("ToResponseDTO method failed. The returned value was expected to be null.",
            responseDTO.getValue());
        assertNull("ToResponseDTO method failed. The returned date was expected to be null.",
            responseDTO.getDate());
        assertEquals(
            "ToResponseDTO method failed. The returned customText didn't match the expected value.",
            customText, responseDTO.getCustomtext());

        answer = spy(ImageAnswerTest.getNewValidImageAnswer());
        Mockito.when(answer.getId()).thenReturn(Math.abs(random.nextLong() + 1));
        answerId = answer.getId();
        testResponse = new Response(answer, encounter);
        List<PointOnImage> testPointsOnImage = new ArrayList<>();
        int count = random.nextInt(25);
        for (int i = 0; i < count; i++) {
            testPointsOnImage.add(PointOnImageTest.getNewValidPointOnImage());
        }
        testResponse.setPointsOnImage(testPointsOnImage);
        responseDTO = testResponse.toResponseDTO();

        assertEquals(
            "ToRepsonseDTO method failed. The returned answerId didn't match the expected value",
            answerId, responseDTO.getAnswerId());
        assertNull("ToResponseDTO method failed. The returned value was expected to be null.",
            responseDTO.getValue());
        assertNull("ToResponseDTO method failed. The returned date was expected to be null.",
            responseDTO.getDate());
        assertEquals(
            "ToResponseDTO method failed. The returned pointsOnImage size didn't match the expected value.",
            testPointsOnImage.size(), responseDTO.getPointsOnImage().size());
    }
}
