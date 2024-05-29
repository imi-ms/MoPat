package de.imi.mopat.model;

import static org.junit.Assert.assertEquals;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.utils.Helper;
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
public class ImageAnswerTest {

    private static final Random random = new Random();
    private ImageAnswer testImageAnswer;

    public ImageAnswerTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Returns a valid new ImageAnswer
     *
     * @return Returns a valid new ImageAnswer
     */
    public static ImageAnswer getNewValidImageAnswer() {
        Question question = QuestionTest.getNewValidQuestion();
        Boolean isEnabled = random.nextBoolean();
        String imagePath = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);

        ImageAnswer imageAnswer = new ImageAnswer(question, isEnabled, imagePath);

        return imageAnswer;
    }

    @Before
    public void setUp() {
        testImageAnswer = getNewValidImageAnswer();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of {@link ImageAnswer#cloneWithoutReferences}.<br> Valid input: valid
     * {@link ImageAnswer}
     */
    @Test
    public void testCloneWithoutReferences() {
        ImageAnswer testClone = testImageAnswer.cloneWithoutReferences();
        assertEquals("The getting isEnabled was not the expected one", testClone.getIsEnabled(),
            testImageAnswer.getIsEnabled());
        assertEquals("The getting imagePath was not the expected one", testClone.getImagePath(),
            testImageAnswer.getImagePath());
    }

    /**
     * Test of {@link ImageAnswer#getImagePath} and {@link ImageAnswer#setImagePath}.<br> Valid
     * input: random String
     */
    @Test
    public void testGetAndSetImagePath() {
        String testImagePath = Helper.getRandomAlphanumericString(random.nextInt(50) + 1);
        testImageAnswer.setImagePath(testImagePath);
        assertEquals("The getting imagePath was not the expected one", testImagePath,
            testImageAnswer.getImagePath());
    }
}
