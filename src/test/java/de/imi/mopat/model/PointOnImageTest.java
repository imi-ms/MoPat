package de.imi.mopat.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.model.dto.PointOnImageDTO;
import de.imi.mopat.model.enumeration.MoPatColor;
import de.imi.mopat.utils.Helper;
import java.util.HashSet;
import java.util.Random;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
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
public class PointOnImageTest {

    private static final Random random = new Random();
    private PointOnImage testPointOnImage;

    public PointOnImageTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Returns a valid new PointOnImage
     *
     * @return Returns a valid new PointOnImage
     */
    public static PointOnImage getNewValidPointOnImage() {
        int position = Math.abs(random.nextInt() + 1);
        Float xCoordinate = random.nextFloat();
        Float yCoordinate = random.nextFloat();
        MoPatColor color = Helper.getRandomEnum(MoPatColor.class);

        PointOnImage pointOnImage = new PointOnImage(position, xCoordinate, yCoordinate, color);

        return pointOnImage;
    }

    @Before
    public void setUp() {
        testPointOnImage = getNewValidPointOnImage();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of {@link PointOnImage#getPosition} and {@link PointOnImage#setPosition}.<br> Invalid
     * input: random negative Integer<br> Valid input: random positive Integer
     */
    @Test
    public void testGetAndSetPosition() {
        int testPosition = Math.abs(random.nextInt()) * (-1);
        Throwable e = null;
        try {
            testPointOnImage.setPosition(testPosition);
        } catch (Throwable ex) {
            e = ex;
        }
        assertTrue("It was possible to set a negative position", e instanceof AssertionError);

        testPosition = Math.abs(random.nextInt() + 1);
        testPointOnImage.setPosition(testPosition);
        assertEquals("The getting position was not the expected one", testPosition,
            testPointOnImage.getPosition());
    }

    /**
     * Test of {@link PointOnImage#getxCoordinate} and {@link PointOnImage#setxCoordinate}.<br>
     * Valid input: random Float
     */
    @Test
    public void testGetAndSetxCoordinate() {
        Float testxCoordinate = random.nextFloat();
        testPointOnImage.setxCoordinate(testxCoordinate);
        assertEquals("The getting xCoordinate was not the expected one", testxCoordinate,
            testPointOnImage.getxCoordinate());
    }

    /**
     * Test of {@link PointOnImage#getyCoordinate} and {@link PointOnImage#setyCoordinate}.<br>
     * Valid input: random Float
     */
    @Test
    public void testGetAndSetyCoordinate() {
        Float testyCoordinate = random.nextFloat();
        testPointOnImage.setyCoordinate(testyCoordinate);
        assertEquals("The getting yCoordinate was not the expected one", testyCoordinate,
            testPointOnImage.getyCoordinate());
    }

    /**
     * Test of {@link PointOnImage#getColor} and {@link PointOnImage#setColor}.<br> Valid input:
     * random {@link MoPatColor}
     */
    @Test
    public void testGetAndSetColor() {
        MoPatColor testColor = Helper.getRandomEnum(MoPatColor.class);
        testPointOnImage.setColor(testColor);
        assertEquals("The getting color was not the expected one", testColor,
            testPointOnImage.getColor());
    }

    /**
     * Test of {@link PointOnImage#getResponse} and {@link PointOnImage#setResponse}.<br> Valid
     * input: random {@link Response}
     */
    @Test
    public void testGetAndSetResponse() {
        Response testResponse = ResponseTest.getNewValidResponse();
        testPointOnImage.setResponse(testResponse);
        assertEquals("The getting response was not the expected one", testResponse,
            testPointOnImage.getResponse());
    }

    /**
     * Test of {@link PointOnImage#toPointOnImageDTO}.<br> Valid input: random {@link PointOnImage}
     */
    @Test
    public void testToPointOnImageDTO() {
        PointOnImageDTO testPointOnImageDTO = testPointOnImage.toPointOnImageDTO();
        assertEquals("The getting position was not the expected one",
            testPointOnImage.getPosition(), testPointOnImageDTO.getPosition());
        assertEquals("The getting xCoordinate was not the expected one",
            testPointOnImage.getxCoordinate(), testPointOnImageDTO.getxCoordinate());
        assertEquals("The getting yCoordinate was not the expected one",
            testPointOnImage.getyCoordinate(), testPointOnImageDTO.getyCoordinate());
        assertEquals("The getting colorcode was not the expected one",
            testPointOnImage.getColor().getColorCode(), testPointOnImageDTO.getColor());
    }

    /**
     * Test of {@link PointOnImage#equals}.<br> Invalid input: the same PointOnImage twice in a
     * HashSet
     */
    @Test
    public void testEquals() {
        HashSet<PointOnImage> testSet = new HashSet<>();
        testSet.add(testPointOnImage);
        testSet.add(testPointOnImage);
        assertEquals("It was possible to add the same PointOnImage twice to one set", 1,
            testSet.size());

        assertEquals("The PointOnImage was not equal to itself", testPointOnImage,
            testPointOnImage);
        assertNotEquals("The PointOnImage was equal to null", null, testPointOnImage);
        PointOnImage otherPointOnImage = getNewValidPointOnImage();
        assertNotEquals("The PointOnImage was equal to a different PointOnImage", testPointOnImage,
            otherPointOnImage);
        Object otherObject = new Object();
        assertNotEquals("The PointOnImage was equal to a different Object", testPointOnImage,
            otherObject);
    }
}
