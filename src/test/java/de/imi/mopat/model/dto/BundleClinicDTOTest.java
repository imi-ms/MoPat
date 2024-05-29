package de.imi.mopat.model.dto;

import static org.junit.Assert.assertEquals;

import java.util.Random;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 */
public class BundleClinicDTOTest {

    private static final Random random = new Random();
    private BundleClinicDTO testBundleClinicDTO;

    public BundleClinicDTOTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        testBundleClinicDTO = new BundleClinicDTO();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of {@link BundleClinicDTO#getPosition} and {@link BundleClinicDTO#setPosition}.<br>
     * Valid input: random Integer
     */
    @Test
    public void testGetAndSetPosition() {
        Integer testPosition = random.nextInt();
        testBundleClinicDTO.setPosition(testPosition);
        assertEquals("The getting position was not the expected one", testPosition,
            testBundleClinicDTO.getPosition());
    }

    /**
     * Test of {@link BundleClinicDTO#getClinicDTO} and {@link BundleClinicDTO#setClinicDTO}.<br>
     * Valid input: new {@link ClinicDTO}
     */
    @Test
    public void testGetAndSetClinicDTO() {
        ClinicDTO testClinicDTO = new ClinicDTO();
        testClinicDTO.setId(random.nextLong());
        testBundleClinicDTO.setClinicDTO(testClinicDTO);
        assertEquals("The getting ClinicDTO was not the expected one", testClinicDTO,
            testBundleClinicDTO.getClinicDTO());
    }

    /**
     * Test of {@link BundleClinicDTO#getBundleDTO} and {@link BundleClinicDTO#setBundleDTO}.<br>
     * Valid input: new {@link Bundle}
     */
    @Test
    public void testGetAndSetBundleDTO() {
        BundleDTO testBundleDTO = new BundleDTO();
        testBundleDTO.setId(random.nextLong());
        testBundleClinicDTO.setBundleDTO(testBundleDTO);
        assertEquals("The getting BundleDTO was not the expected one", testBundleDTO,
            testBundleClinicDTO.getBundleDTO());
    }
}