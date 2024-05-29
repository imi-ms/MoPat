package de.imi.mopat.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.dao.QuestionnaireDao;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.dto.QuestionnaireDTO;
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

public class QuestionnaireDTOValidatorTest {

    private static final Random random = new Random();
    @Autowired
    QuestionnaireDao questionnaireDao;
    @Autowired
    QuestionnaireDTOValidator questionnaireDTOValidator;
    @Autowired
    MessageSource messageSource;

    /**
     * Test of {@link QuestionnaireDTOValidator#supports(java.lang.Class)}<br> Valid input:
     * {@link QuestionnaireDTO#class}<br> Invalid input: Other class than
     * {@link QuestionnaireDTO#class}.
     */
    @Test
    public void testSupports() {
        assertTrue(
            "Supports method for Questionnaire.class failed. QuestionnaireValidator didn't support Questionnaire.class except it was expected to do.",
            questionnaireDTOValidator.supports(QuestionnaireDTO.class));
        assertFalse(
            "Supports method for random class failed. QuestionValidator supported that class except it wasn't expected to do.",
            questionnaireDTOValidator.supports(Random.class));
    }


    /**
     * Test of
     * {@link QuestionnaireDTOValidator#validate(java.lang.Object,
     * org.springframework.validation.Errors)}. <br> Valid input: Instance of
     * {@link QuestionnaireDTO} is valid if none of the lower conditions for invalid instances is
     * satisfied and instance of {@link Errors}. <br> Invalid input: Instance of
     * {@link QuestionnaireDTO} is invalid if at least one of the following conditions is satisfied.
     * <br> The questionnaireDTO's name is already in use.<br> One of the localizedDisplayNames is
     * empty or <code>null</code>. <br> Any localizedWelcomeText or localizedFinalText is set and at
     * least one of the others is empty or
     * <code>null</code>.
     */
    @Test
    public void testValidate() {
        QuestionnaireDTO questionnaireDTO = new QuestionnaireDTO();
        BindingResult result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(23) + 1));
        Integer size = random.nextInt(23) + 1;

        //build valid instance with related properties
        questionnaireDTO.setName(Helper.getRandomAlphanumericString(random.nextInt(53) + 3));
        //fill localizedDisplayName, localizedWelcomeText and localizedFinalText
        SortedMap<String, String> localizedDisplayName = new TreeMap<>(), localizedWelcomeText = new TreeMap<>(), localizedFinalText = new TreeMap<>();

        for (int i = 0; i < size; i++) {
            String key = Helper.getRandomLocale();
            localizedDisplayName.put(key,
                Helper.getRandomAlphanumericString(random.nextInt(53) + 3));
            localizedWelcomeText.put(key,
                Helper.getRandomAlphanumericString(random.nextInt(157) + 3));
            localizedFinalText.put(key,
                Helper.getRandomAlphanumericString(random.nextInt(157) + 3));
        }

        questionnaireDTO.setLocalizedDisplayName(localizedDisplayName);
        questionnaireDTO.setLocalizedWelcomeText(localizedWelcomeText);
        questionnaireDTO.setLocalizedFinalText(localizedFinalText);
        questionnaireDTOValidator.validate(questionnaireDTO, result);
        assertFalse(
            "Validation of questionnaireDTO failed for valid instance. The result has caught errors except it wasn't expected to do.",
            result.hasErrors());

        //build invalid instance with incorrect related properties
        //case 1: questionnaireNameInUse
        Questionnaire questionnaire = new Questionnaire();
        questionnaire.setName(questionnaireDTO.getName());
        questionnaire.setChangedBy(Math.abs(random.nextLong()));
        questionnaire.setDescription(Helper.getRandomAlphanumericString(random.nextInt(251) + 3));
        questionnaire.setPublished(random.nextBoolean());
        questionnaireDao.merge(questionnaire);
        questionnaireDTOValidator.validate(questionnaireDTO, result);
        assertTrue(
            "Validation of questionnaireDTO failed for name in use. The result hasn't caught errors except it was expected to do.",
            result.hasErrors());

        String message = messageSource.getMessage("questionnaire.error.nameInUse", new Object[]{},
            LocaleContextHolder.getLocale()), testErrorMessage = result.getAllErrors().get(0)
            .getDefaultMessage();
        assertEquals(
            "Validation of questionnaireDTO failed for name in use. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        //Reset properties so as to not getting into trouble with invalid values
        questionnaireDTO.setName(Helper.getRandomAlphanumericString(random.nextInt(53) + 3));
        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(23) + 1));

        //case 2: At least one entry of localizedDisplayName is empty
        //Initialize key to set an invalid value
        String key = Helper.getRandomLocale();
        questionnaireDTO.getLocalizedDisplayName().put(key, "");
        questionnaireDTOValidator.validate(questionnaireDTO, result);
        assertTrue(
            "Validation of questionnaireDTO failed for display name is empty. The result hasn't caught errors except it was expected to do.",
            result.hasErrors());

        message = messageSource.getMessage("questionnaire.displayName.notNull", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of questionnaireDTO failed for display name is empty. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        //Reset properties so as to not getting into trouble with invalid values
        questionnaireDTO.getLocalizedDisplayName().put(key, null);
        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(23) + 1));

        questionnaireDTOValidator.validate(questionnaireDTO, result);
        assertTrue(
            "Validation of questionnaireDTO failed for display name is null. The result hasn't caught errors except it was expected to do.",
            result.hasErrors());

        message = messageSource.getMessage("questionnaire.displayName.notNull", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of questionnaireDTO failed for display name is null. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        //Reset properties so as to not getting into trouble with invalid values
        questionnaireDTO.getLocalizedDisplayName()
            .put(key, Helper.getRandomAlphabeticString(53) + 3);
        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(23) + 1));

        //case 3: LocalizedWelcomeText is set, but at least one entry is empty
        questionnaireDTO.getLocalizedWelcomeText().put(key, "");
        questionnaireDTOValidator.validate(questionnaireDTO, result);
        assertTrue(
            "Validation of questionnaireDTO failed for welcome text is empty. The result hasn't caught errors except it was expected to do.",
            result.hasErrors());

        message = messageSource.getMessage("questionnaire.validator.welcomeText.notNull",
            new Object[]{}, LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of questionnaireDTO failed for welcome text is empty. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        //Reset properties so as to not getting into trouble with invalid values
        questionnaireDTO.getLocalizedWelcomeText()
            .put(key, Helper.getRandomAlphanumericString(random.nextInt(157) + 3));
        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(23) + 1));

        //case 4: LocalizedFinalText is set, but at least one entry is empty
        questionnaireDTO.getLocalizedFinalText().put(key, "");
        questionnaireDTOValidator.validate(questionnaireDTO, result);
        assertTrue(
            "Validation of questionnaireDTO failed for final text is empty. The result hasn't caught errors except it was expected to do.",
            result.hasErrors());

        message = messageSource.getMessage("questionnaire.validator.finalText.notNull",
            new Object[]{}, LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "Validation of questionnaireDTO failed for final text is empty. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        //case 5: Valid instance without welcomeText and finalText
        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(23) + 1));
        questionnaireDTO.setLocalizedWelcomeText(new TreeMap<>());
        questionnaireDTO.setLocalizedFinalText(new TreeMap<>());
        questionnaireDTOValidator.validate(questionnaireDTO, result);
        assertFalse(
            "Validation of questionnaireDTO failed for valid instance. The result has caught errors except it wasn't expected to do.",
            result.hasErrors());

        //case 6: invalid instance with locale but empty welcome text or final text
        questionnaireDTO.getLocalizedWelcomeText().put(Helper.getRandomLocale(), "");
        questionnaireDTOValidator.validate(questionnaireDTO, result);
        assertFalse(
            "Validation of questionnaireDTO failed for valid instance. The result has caught errors except it wasn't expected to do.",
            result.hasErrors());

        questionnaireDTO.setLocalizedWelcomeText(new TreeMap<>());
        questionnaireDTO.getLocalizedFinalText().put(Helper.getRandomLocale(), "");
        questionnaireDTOValidator.validate(questionnaireDTO, result);
        assertFalse(
            "Validation of questionnaireDTO failed for valid instance. The result has caught errors except it wasn't expected to do.",
            result.hasErrors());
    }
}
