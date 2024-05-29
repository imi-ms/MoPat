package de.imi.mopat.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.model.dto.AnswerDTO;
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
public class SelectAnswerDTOValidatorTest {

    public static Random random = new Random();
    @Autowired
    SelectAnswerDTOValidator selectAnswerDTOValidator;
    @Autowired
    MessageSource messageSource;

    /**
     * Test of {@link SelectAnswerDTOValidator#supports(java.lang.Class)}<br> Valid input:
     * {@link AnswerDTO#class}<br> Invalid input: Other classes than {@link AnswerDTO#class}
     */
    @Test
    public void testSupports() {
        assertTrue(
            "Supports method for AnswerDTO.class failed. SelectAnswerDTOValidator didn't support AnswerDTO.class except it was expected to do.",
            selectAnswerDTOValidator.supports(AnswerDTO.class));
        assertFalse(
            "Supports method for random class failed. SelectAnswerDTOValidator supported that class except it wasn't expected to do.",
            selectAnswerDTOValidator.supports(Random.class));
    }

    /**
     * Test of
     * {@link SelectAnswerDTOValidator#validate(java.lang.Object,
     * org.springframework.validation.Errors)} Valid input: Instance of {@link BindingResult} and
     * {@link AnswerDTO} with a map containing the localized label texts where none is empty.<br>
     * Invalid input: Instance of {@link AnswerDTO} with a map containing a localized label where at
     * least one localized label text is
     * <code>null</code> or empty.<br>
     */
    @Test
    public void testValidate() {
        AnswerDTO answerDTO = new AnswerDTO();
        SortedMap<String, String> localizedLabel = new TreeMap<>();
        String locale;
        for (int i = 0; i < random.nextInt(7) + 1; i++) {
            locale = Helper.getRandomLocale();
            localizedLabel.put(locale, Helper.getRandomAlphabeticString(random.nextInt(50) + 1));
        }
        answerDTO.setLocalizedLabel(localizedLabel);
        String key = Helper.getRandomLocale();

        BindingResult result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(50) + 1));
        selectAnswerDTOValidator.validate(answerDTO, result);
        assertFalse(
            "Validation of selectAnswerDTO failed for valid instance. The result has caught errors except it wasn't expected to do.",
            result.hasErrors());

        answerDTO.getLocalizedLabel().put(key, "");
        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(50) + 1));
        selectAnswerDTOValidator.validate(answerDTO, result);
        assertTrue(
            "Validation of selectAnswerDTO failed for invalid instance with empty label. The result hasn't caught errors except it was expected to do.",
            result.hasErrors());

        String message = messageSource.getMessage("selectAnswer.validator.labelNotNull",
            new Object[]{}, LocaleContextHolder.getLocale());
        String testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of selectAnswerDTO failed for invalid instance. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        answerDTO.getLocalizedLabel().put(key, null);
        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(50) + 1));
        selectAnswerDTOValidator.validate(answerDTO, result);
        assertTrue(
            "Validation of selectAnswerDTO failed for invalid instance with null label. The result hasn't caught errors except it was expected to do.",
            result.hasErrors());

        message = messageSource.getMessage("selectAnswer.validator.labelNotNull", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of selectAnswer failed for invalid instance. The returned error message didn't match the expected one.",
            message, testErrorMessage);
    }
}
