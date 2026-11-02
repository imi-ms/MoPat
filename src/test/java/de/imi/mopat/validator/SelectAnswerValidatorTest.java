package de.imi.mopat.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.model.Question;
import de.imi.mopat.model.QuestionnaireTest;
import de.imi.mopat.model.SelectAnswer;
import de.imi.mopat.model.enumeration.QuestionType;
import de.imi.mopat.utils.Helper;
import java.util.HashMap;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

/**
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, ApplicationSecurityConfig.class,
    MvcWebApplicationInitializer.class, PersistenceConfig.class})
@TestPropertySource(locations = {"classpath:mopat-test.properties"})
@WebAppConfiguration
public class SelectAnswerValidatorTest {

    private static final Random random = new Random();
    @Autowired
    SelectAnswerValidator selectAnswerValidator;
    @Autowired
    MessageSource messageSource;

    /**
     * Test of {@link SelectAnswerValidator#supports(java.lang.Class)}<br> Valid input: Class
     * property of {@link SelectAnswer}<br> Invalid input: Class property of a different class
     */
    @Test
    public void testSupports() {
        assertTrue(
            "Supports method for SelectAnswer.class failed. selectAnswerValidator didn't support SelectAnswer.class except it was expected to do.",
            selectAnswerValidator.supports(SelectAnswer.class));
        assertFalse(
            "Supports method for random class failed. selectAnswerValidator supported that class except it wasn't expected to do.",
            selectAnswerValidator.supports(Random.class));
    }

    /**
     * Test of
     * {@link SelectAnswerValidator#validate(java.lang.Object,
     * org.springframework.validation.Errors)}<br> Valid input: Instance of {@link BindingResult}
     * and {@link SelectAnswer} with a map containing the localized label texts where none is
     * empty.<br> Invalid input: Instance of {@link SelectAnswer} with a map containing a localized
     * label where at least one localized label text is
     * <code>null</code> or empty.<br>
     */
    @Test
    public void testValidate() {
        SortedMap<String, String> localizedLabel = new TreeMap<>();

        for (int i = 0; i < random.nextInt(23) + 1; i++) {
            localizedLabel.put(Helper.getRandomLocale(),
                Helper.getRandomAlphabeticString(random.nextInt(50) + 1));
        }

        SelectAnswer selectAnswer = new SelectAnswer(
            new Question(localizedLabel, random.nextBoolean(), random.nextBoolean(),
                Helper.getRandomEnum(QuestionType.class), random.nextInt(5) + 1,
                QuestionnaireTest.getNewValidQuestionnaire(), false), random.nextBoolean(), localizedLabel,
            random.nextBoolean());
        String key = Helper.getRandomLocale();

        BindingResult result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(50) + 1));
        selectAnswerValidator.validate(selectAnswer, result);
        assertFalse(
            "Validation of selectAnswer failed for valid instance. The result has caught errors except it wasn't expected to do.",
            result.hasErrors());

        selectAnswer.getLocalizedLabel().put(key, "");
        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(50) + 1));
        selectAnswerValidator.validate(selectAnswer, result);
        assertTrue(
            "Validation of selectAnswer failed for invalid instance with empty label. The result hasn't caught errors except it was expected to do.",
            result.hasErrors());

        String message = messageSource.getMessage("selectAnswer.validator.labelNotNull",
            new Object[]{}, LocaleContextHolder.getLocale());
        String testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of selectAnswer failed for invalid instance. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        selectAnswer.getLocalizedLabel().put(key, null);
        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(50) + 1));
        selectAnswerValidator.validate(selectAnswer, result);
        assertTrue(
            "Validation of selectAnswer failed for invalid instance with null label. The result hasn't caught errors except it was expected to do.",
            result.hasErrors());

        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of selectAnswer failed for invalid instance. The returned error message didn't match the expected one.",
            message, testErrorMessage);
    }
}
