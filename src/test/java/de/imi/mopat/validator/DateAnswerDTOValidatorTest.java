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
public class DateAnswerDTOValidatorTest {

    private static final Random random = new Random();
    @Autowired
    DateAnswerDTOValidator dateAnswerDTOValidator;
    @Autowired
    MessageSource messageSource;

    /**
     * Test of {@link DateAnswerDTOValidator#supports(java.lang.Class)}<br> Valid input:
     * {@link AnswerDTO#class}<br> Invalid input: Other class than {@link AnswerDTO#class}
     */
    @Test
    public void testSupports() {
        assertTrue(
            "Supports method for AnswerDTO.class failed. DateAnswerDTOValidator didn't support AnswerDTO.class except it was expected to do.",
            dateAnswerDTOValidator.supports(AnswerDTO.class));
        assertFalse(
            "Supports method for random class failed. DateAnswerDTOValidator supported that class except it wasn't expected to do.",
            dateAnswerDTOValidator.supports(Random.class));
    }

    /**
     * Test of
     * {@link DateAnswerDTOValidator#validate(java.lang.Object,
     * org.springframework.validation.Errors)}.<br> Valid input: Instance of {@link AnswerDTO} with
     * any startDate set before endDate and instance of {@link Errors}.<br> Invalid input: Instance
     * of {@link AnswerDTO} with any startDate set after endDate.
     */
    @Test
    public void testValidate() {
        BindingResult result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        AnswerDTO answerDTO = new AnswerDTO();

        Integer year = random.nextInt(2000) + 1000;
        Integer month = random.nextInt(12) + 1;
        Integer day = random.nextInt(25) + 1;
        String startDate = year + "-" + month + "-" + day;
        answerDTO.setStartDate(startDate);

        year = year + (random.nextInt(25) + 1);
        month = (random.nextInt(12) + 1);
        day = (random.nextInt(25) + 1);
        String endDate = year + "-" + month + "-" + day;
        answerDTO.setEndDate(endDate);

        dateAnswerDTOValidator.validate(answerDTO, result);
        assertFalse(
            "Validation of dateAnswerDTO failed. The result caught errors except it wasn't expected to do.",
            result.hasErrors());

        answerDTO.setStartDate(endDate);
        answerDTO.setEndDate(startDate);
        dateAnswerDTOValidator.validate(answerDTO, result);
        assertTrue(
            "Validation of dateAnswerDTO failed. The result hasn't caught errors except it was expected to do.",
            result.hasErrors());

        String message = messageSource.getMessage("dateAnswer.validator.endEarlierThanStart",
            new Object[]{}, LocaleContextHolder.getLocale());
        String testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of dateAnswerDTO failed. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        answerDTO.setStartDate(null);
        answerDTO.setEndDate(null);
        dateAnswerDTOValidator.validate(answerDTO, result);
        assertFalse(
            "Validation of dateAnswerDTO failed. The result caught errors except it wasn't expected to do.",
            result.hasErrors());

        answerDTO.setStartDate("");
        answerDTO.setEndDate("");
        dateAnswerDTOValidator.validate(answerDTO, result);
        assertFalse(
            "Validation of dateAnswerDTO failed. The result caught errors except it wasn't expected to do.",
            result.hasErrors());

        answerDTO.setEndDate(null);
        dateAnswerDTOValidator.validate(answerDTO, result);
        assertFalse(
            "Validation of dateAnswerDTO failed. The result caught errors except it wasn't expected to do.",
            result.hasErrors());

        answerDTO.setStartDate(null);
        answerDTO.setEndDate("");
        dateAnswerDTOValidator.validate(answerDTO, result);
        assertFalse(
            "Validation of dateAnswerDTO failed. The result caught errors except it wasn't expected to do.",
            result.hasErrors());

        answerDTO.setStartDate(
            (random.nextInt(11) + 1) + "-" + (random.nextInt(11) + 1) + "-" + (random.nextInt(11)
                + 1));
        answerDTO.setEndDate("");
        dateAnswerDTOValidator.validate(answerDTO, result);
        assertFalse(
            "Validation of dateAnswerDTO failed. The result caught errors except it wasn't expected to do.",
            result.hasErrors());

        answerDTO.setStartDate(
            (random.nextInt(11) + 1) + "-" + (random.nextInt(11) + 1) + "-" + (random.nextInt(11)
                + 1));
        answerDTO.setEndDate(null);
        dateAnswerDTOValidator.validate(answerDTO, result);
        assertFalse(
            "Validation of dateAnswerDTO failed. The result caught errors except it wasn't expected to do.",
            result.hasErrors());

        answerDTO.setStartDate("");
        answerDTO.setEndDate(
            (random.nextInt(11) + 1) + "-" + (random.nextInt(11) + 1) + "-" + (random.nextInt(11)
                + 1));
        dateAnswerDTOValidator.validate(answerDTO, result);
        assertFalse(
            "Validation of dateAnswerDTO failed. The result caught errors except it wasn't expected to do.",
            result.hasErrors());

        answerDTO.setStartDate(null);
        answerDTO.setEndDate(
            (random.nextInt(11) + 1) + "-" + (random.nextInt(11) + 1) + "-" + (random.nextInt(11)
                + 1));
        dateAnswerDTOValidator.validate(answerDTO, result);
        assertFalse(
            "Validation of dateAnswerDTO failed. The result caught errors except it wasn't expected to do.",
            result.hasErrors());
    }
}
