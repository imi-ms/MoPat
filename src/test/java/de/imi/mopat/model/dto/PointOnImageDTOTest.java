package de.imi.mopat.model.dto;

import static org.junit.Assert.assertEquals;

import de.imi.mopat.model.PointOnImage;
import de.imi.mopat.model.enumeration.MoPatColor;
import de.imi.mopat.utils.Helper;
import java.util.Random;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 */
public class PointOnImageDTOTest {

    private static final Random random = new Random();
    private PointOnImageDTO testPointOnImageDTO;

    public PointOnImageDTOTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        testPointOnImageDTO = new PointOnImageDTO();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of {@link PointOnImageDTO#PointOnImageDTO}.
     */
    @Test
    public void testConstructor() {
        int testPosition = Math.abs(random.nextInt() + 1);
        Float testxCoordinate = random.nextFloat();
        Float testyCoordinate = random.nextFloat();
        String testColor = Helper.getRandomEnum(MoPatColor.class).getColorCode();
        PointOnImageDTO testPointOnImageDTOContructor = new PointOnImageDTO(testPosition,
            testxCoordinate, testyCoordinate, testColor);
        assertEquals("The getting position was not the expected one", testPosition,
            testPointOnImageDTOContructor.getPosition());
        assertEquals("The getting xCoordinate was not the expected one", testxCoordinate,
            testPointOnImageDTOContructor.getxCoordinate());
        assertEquals("The getting yCoordinate was not the expected one", testyCoordinate,
            testPointOnImageDTOContructor.getyCoordinate());
        assertEquals("The getting color was not the expected one", testColor,
            testPointOnImageDTOContructor.getColor());
    }

    /**
     * Test of {@link PointOnImageDTO#getPosition} and {@link PointOnImageDTO#setPosition}.<br>
     * Valid input: random int
     */
    @Test
    public void testGetAndSetPosition() {
        int testPosition = Math.abs(random.nextInt() + 1);
        testPointOnImageDTO.setPosition(testPosition);
        assertEquals("The getting position was not the expected one", testPosition,
            testPointOnImageDTO.getPosition());
    }

    /**
     * Test of {@link PointOnImageDTO#getxCoordinate} and
     * {@link PointOnImageDTO#setxCoordinate}.<br> Valid input: random Float
     */
    @Test
    public void testGetAndSetxCoordinate() {
        Float testxCoordinate = random.nextFloat();
        testPointOnImageDTO.setxCoordinate(testxCoordinate);
        assertEquals("The getting xCoordinate was not the expected one", testxCoordinate,
            testPointOnImageDTO.getxCoordinate());
    }

    /**
     * Test of {@link PointOnImageDTO#getyCoordinate} and
     * {@link PointOnImageDTO#setyCoordinate}.<br> Valid input: random Float
     */
    @Test
    public void testGetAndSetyCoordinate() {
        Float testyCoordinate = random.nextFloat();
        testPointOnImageDTO.setyCoordinate(testyCoordinate);
        assertEquals("The getting yCoordinate was not the expected one", testyCoordinate,
            testPointOnImageDTO.getyCoordinate());
    }

    /**
     * Test of {@link PointOnImageDTO#getColor} and {@link PointOnImageDTO#setColor}.<br> Valid
     * input: random {@link MoPatColor} as String
     */
    @Test
    public void testGetAndSetColor() {
        String testColor = Helper.getRandomEnum(MoPatColor.class).getColorCode();
        testPointOnImageDTO.setColor(testColor);
        assertEquals("The getting color was not the expected one", testColor,
            testPointOnImageDTO.getColor());
    }

    /**
     * Test of {@link PointOnImageDTO#toPointOnImage}.<br> Valid input: random
     * {@link PointOnImageDTO}
     */
    @Test
    public void testToPointOnImage() {
        int testPosition = Math.abs(random.nextInt() + 1);
        Float testxCoordinate = random.nextFloat();
        Float testyCoordinate = random.nextFloat();
        String testColor = Helper.getRandomEnum(MoPatColor.class).getColorCode();
        testPointOnImageDTO.setPosition(testPosition);
        testPointOnImageDTO.setxCoordinate(testxCoordinate);
        testPointOnImageDTO.setyCoordinate(testyCoordinate);
        testPointOnImageDTO.setColor(testColor);
        PointOnImage testPointOnImage = testPointOnImageDTO.toPointOnImage();
        assertEquals("The getting position was not the expected one",
            testPointOnImageDTO.getPosition(), testPointOnImage.getPosition());
        assertEquals("The getting xCoordinate was not the expected one",
            testPointOnImageDTO.getxCoordinate(), testPointOnImage.getxCoordinate());
        assertEquals("The getting yCoordinate was not the expected one",
            testPointOnImageDTO.getyCoordinate(), testPointOnImage.getyCoordinate());
        assertEquals("The getting color was not the expected one",
            MoPatColor.fromColorCode(testPointOnImageDTO.getColor()), testPointOnImage.getColor());
    }
}
