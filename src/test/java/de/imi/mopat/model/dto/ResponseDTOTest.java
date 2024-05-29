package de.imi.mopat.model.dto;

import static org.junit.Assert.assertEquals;

import de.imi.mopat.model.enumeration.MoPatColor;
import de.imi.mopat.utils.Helper;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 */
public class ResponseDTOTest {

    private static final Random random = new Random();
    private ResponseDTO testResponseDTO;

    public ResponseDTOTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        testResponseDTO = new ResponseDTO();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of {@link ResponseDTO#isEnabled} and {@link ResponseDTO#setEnabled}.<br> Valid input:
     * random Boolean
     */
    @Test
    public void testIsAndSetEnabled() {
        Boolean testEnabled = random.nextBoolean();
        testResponseDTO.setEnabled(testEnabled);
        assertEquals("The getting Enabled was not the expected one", testEnabled,
            testResponseDTO.isEnabled());
    }

    /**
     * Test of {@link ResponseDTO#getAnswerId} and {@link ResponseDTO#setAnswerId}.<br> Valid input:
     * random Long
     */
    @Test
    public void testGetAndSetAnswerId() {
        Long testAnswerID = Math.abs(random.nextLong());
        testResponseDTO.setAnswerId(testAnswerID);
        assertEquals("The getting AnswerID was not the expected one", testAnswerID,
            testResponseDTO.getAnswerId());
    }

    /**
     * Test of {@link ResponseDTO#getCustomtext} and {@link ResponseDTO#setCustomtext}.<br> Valid
     * input: random String
     */
    @Test
    public void testGetAndSetCustomtext() {
        String testCustomtext = Helper.getRandomString(random.nextInt(255) + 1);
        testResponseDTO.setCustomtext(testCustomtext);
        assertEquals("The getting Customtext was not the expected one", testCustomtext,
            testResponseDTO.getCustomtext());
    }

    /**
     * Test of {@link ResponseDTO#getValue} and {@link ResponseDTO#setValue}.<br> Valid input:
     * random Double
     */
    @Test
    public void testGetAndSetValue() {
        Double testValue = random.nextDouble();
        testResponseDTO.setValue(testValue);
        assertEquals("The getting Value was not the expected one", testValue,
            testResponseDTO.getValue());
    }

    /**
     * Test of {@link ResponseDTO#getDate} and {@link ResponseDTO#setDate}.<br> Valid input: random
     * Date
     */
    @Test
    public void testGetAndSetDate() {
        Date testDate = new Date(random.nextLong());
        testResponseDTO.setDate(testDate);
        assertEquals("The getting Date was not the expected one", testDate,
            testResponseDTO.getDate());
    }

    /**
     * Test of {@link ResponseDTO#getPointsOnImage} and {@link ResponseDTO#setPointsOnImage}.<br>
     * Valid input: random list of {@link PointOnImageDTO PointOnImageDTOs}
     */
    @Test
    public void testGetAndSetPointsOnImage() {
        List<PointOnImageDTO> testPointsOnImage = new ArrayList<>();
        int count = random.nextInt(25);
        for (int i = 0; i < count; i++) {
            int testPosition = Math.abs(random.nextInt() + 1);
            Float testxCoordinate = random.nextFloat();
            Float testyCoordinate = random.nextFloat();
            String testColor = Helper.getRandomEnum(MoPatColor.class).getColorCode();
            PointOnImageDTO testPointOnImageDTO = new PointOnImageDTO(testPosition, testxCoordinate,
                testyCoordinate, testColor);
            testPointsOnImage.add(testPointOnImageDTO);
        }
        testResponseDTO.setPointsOnImage(testPointsOnImage);
        assertEquals("The getting list of pointOnImageDTOs was not the expected one",
            testResponseDTO.getPointsOnImage(), testPointsOnImage);
    }

    /**
     * Test of {@link ResponseDTO#getJSON}.<br> Valid input: random valid {@link ResponseDTO} with
     * answer id, customtext, value, date and enabled
     */
    @Test
    public void testGetJSON() {
        testResponseDTO.setAnswerId(Math.abs(random.nextLong()));
        testResponseDTO.setCustomtext(Helper.getRandomString(random.nextInt(50)));
        testResponseDTO.setValue(Math.abs(random.nextDouble()));
        testResponseDTO.setDate(
            new Date(946684800000L + (long) (random.nextInt(Integer.MAX_VALUE)) * 1000));
        testResponseDTO.setEnabled(random.nextBoolean());

        StringBuilder testJSON = new StringBuilder();
        testJSON.append("{\"answerId\":").append(testResponseDTO.getAnswerId());
        testJSON.append(",\"customtext\":\"").append(testResponseDTO.getCustomtext());
        testJSON.append("\",\"value\":").append(testResponseDTO.getValue());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        testJSON.append(",\"date\":\"").append(sdf.format(testResponseDTO.getDate()));
        testJSON.append("\",\"enabled\":").append(testResponseDTO.isEnabled());
        testJSON.append(",\"pointsOnImage\":").append(testResponseDTO.getPointsOnImage());
        testJSON.append("}");

        assertEquals("The getting JSON was not the expected one", testJSON.toString(),
            testResponseDTO.getJSON());
    }
}
