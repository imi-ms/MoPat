package de.imi.mopat.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.dao.BundleDao;
import de.imi.mopat.helper.controller.QuestionnaireService;
import de.imi.mopat.helper.model.QuestionnaireDTOMapper;
import de.imi.mopat.model.Bundle;
import de.imi.mopat.model.BundleQuestionnaire;
import de.imi.mopat.model.BundleQuestionnaireTest;
import de.imi.mopat.model.BundleTest;
import de.imi.mopat.model.dto.BundleDTO;
import de.imi.mopat.model.dto.BundleQuestionnaireDTO;
import de.imi.mopat.model.dto.QuestionnaireDTO;
import de.imi.mopat.utils.Helper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
public class BundleDTOValidatorTest {

    private static final Random random = new Random();
    @Autowired
    private BundleDTOValidator bundleDTOValidator;
    @Autowired
    @Qualifier("messageSource")
    private MessageSource messageSource;
    @Autowired
    private BundleDao bundleDao;

    @Autowired
    private QuestionnaireService questionnaireService;

    @Autowired
    private QuestionnaireDTOMapper questionnaireDTOMapper;

    /**
     * Test of {@link BundleDTOValidator#supports(java.lang.Class)} Valid input:
     * {@link BundleDTO#class} Invalid input: Other class than {@link BundleDTO#class}
     */
    @Test
    public void testSupports() {
        assertTrue(
            "Supports method for BundleDTO.class failed. BundleDTOValidator didn't support BundleDTO.class except it was expected to do.",
            bundleDTOValidator.supports(BundleDTO.class));
        assertFalse(
            "Supports method for random class failed. BundleDTOValidator supported that class except it wasn't expected to do.",
            bundleDTOValidator.supports(Random.class));
    }

    /**
     * Test of
     * {@link BundleDTOValidator#validate(java.lang.Object,
     * org.springframework.validation.Errors)}<br> Valid input: Instance of {@link BundleDTO} not
     * fitting the lower conditions for an invalid object and instance of {@link Errors}<br> Invalid
     * input: Instance of {@link BundleDTO} whose first bundleQuestionnaire is not active, or
     * instance where at least one welcome text or final text of any language is set and one is
     * not.<br> Last condition which makes an invalid instance: Its name is already in use.
     */
    @Test
    public void testValidate() {
        BundleDTO bundleDTO = new BundleDTO();
        TreeMap<String, String> localizedWelcomeText = new TreeMap<>();
        TreeMap<String, String> localizedFinalText = new TreeMap<>();

        bundleDTO.setId(Math.abs(random.nextLong()));
        bundleDTO.setName(Helper.getRandomAlphabeticString(random.nextInt(23) + 3));
        bundleDTO.setLocalizedWelcomeText(localizedWelcomeText);
        bundleDTO.setLocalizedFinalText(localizedFinalText);
        BindingResult result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        bundleDTOValidator.validate(bundleDTO, result);

        assertFalse(
            "The validation of valid instance failed. The result has caught an error except it wasn't expected to do.",
            result.hasErrors());

        for (int i = 0; i < random.nextInt(5) + 1; i++) {
            localizedWelcomeText.put(Helper.getRandomLocale(),
                Helper.getRandomAlphabeticString(random.nextInt(125) + 1));
            localizedFinalText.put(Helper.getRandomLocale(),
                Helper.getRandomAlphabeticString(random.nextInt(125) + 1));
        }

        bundleDTO.setLocalizedWelcomeText(localizedWelcomeText);
        bundleDTO.setLocalizedFinalText(localizedFinalText);
        bundleDTOValidator.validate(bundleDTO, result);
        assertFalse(
            "The validation of valid instance failed. The result has caught an error except it wasn't expected to do.",
            result.hasErrors());

        List<BundleQuestionnaireDTO> bundleQuestionnaires = new ArrayList<>();
        BundleQuestionnaireDTO bundleQuestionnaireDTO = new BundleQuestionnaireDTO();
        bundleQuestionnaires.add(bundleQuestionnaireDTO);
        bundleDTO.setBundleQuestionnaireDTOs(bundleQuestionnaires);
        bundleDTOValidator.validate(bundleDTO, result);
        assertFalse(
            "The validation of valid instance failed. The result has caught an error except it wasn't expected to do.",
            result.hasErrors());
        bundleQuestionnaires.clear();

        QuestionnaireDTO questionnaireDTO = new QuestionnaireDTO();
        bundleQuestionnaireDTO.setQuestionnaireDTO(questionnaireDTO);
        bundleQuestionnaires.add(bundleQuestionnaireDTO);
        bundleDTO.setBundleQuestionnaireDTOs(bundleQuestionnaires);
        bundleDTOValidator.validate(bundleDTO, result);
        assertFalse(
            "The validation of valid instance failed. The result has caught an error except it wasn't expected to do.",
            result.hasErrors());
        bundleQuestionnaires.clear();

        questionnaireDTO = Mockito.spy(new QuestionnaireDTO());
        Mockito.when(questionnaireDTO.getId()).thenReturn(Math.abs(random.nextLong()));
        bundleQuestionnaireDTO.setQuestionnaireDTO(questionnaireDTO);
        bundleQuestionnaires.add(bundleQuestionnaireDTO);
        bundleDTO.setBundleQuestionnaireDTOs(bundleQuestionnaires);
        bundleDTOValidator.validate(bundleDTO, result);
        assertTrue(
            "The validation of invalid instance failed. The result hasn't caught errors except it was expected to do.",
            result.hasErrors());
        bundleQuestionnaires.clear();

        String message = messageSource.getMessage("bundle.error.firstQuestionnaireNotActive",
            new Object[]{}, LocaleContextHolder.getLocale());
        String testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "The validation of the first bundleQuestionnaire failed. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        bundleQuestionnaireDTO.setIsEnabled(false);
        bundleQuestionnaires.add(bundleQuestionnaireDTO);
        bundleDTO.setBundleQuestionnaireDTOs(bundleQuestionnaires);
        bundleDTOValidator.validate(bundleDTO, result);
        assertTrue(
            "The validation of invalid instance failed. The result hasn't caught errors except it was expected to do.",
            result.hasErrors());
        bundleQuestionnaires.clear();

        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "The validation of the first bundleQuestionnaire failed. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        bundleQuestionnaireDTO.setIsEnabled(true);
        bundleQuestionnaires.add(bundleQuestionnaireDTO);
        bundleDTO.setBundleQuestionnaireDTOs(bundleQuestionnaires);
        bundleDTOValidator.validate(bundleDTO, result);
        assertFalse(
            "The validation of valid instance failed. The result has caught an error except it wasn't expected to do.",
            result.hasErrors());
        bundleQuestionnaires.clear();
        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        BundleQuestionnaire bundleQuestionnaire = BundleQuestionnaireTest.getNewValidBundleQuestionnaire();
        bundleQuestionnaire.getQuestionnaire().setLocalizedFinalText(new TreeMap<>());
        bundleQuestionnaire.getQuestionnaire().setLocalizedDisplayName(new TreeMap<>());
        bundleQuestionnaire.getQuestionnaire().setLocalizedWelcomeText(new TreeMap<>());
        bundleQuestionnaireDTO = bundleQuestionnaire.toBundleQuestionnaireDTO();
        questionnaireDTO = Mockito.spy(
                questionnaireDTOMapper.apply(bundleQuestionnaire.getQuestionnaire())
        );
        Mockito.when(questionnaireDTO.getId()).thenReturn(Math.abs(random.nextLong()));
        bundleQuestionnaireDTO.setQuestionnaireDTO(questionnaireDTO);
        bundleQuestionnaireDTO.setIsEnabled(false);
        bundleQuestionnaires.add(bundleQuestionnaireDTO);
        bundleDTO.setBundleQuestionnaireDTOs(bundleQuestionnaires);
        bundleDTOValidator.validate(bundleDTO, result);
        assertTrue(
            "The validation of the first bundleQuestionnaire failed. The result hasn't caught an error except it was expected to do.",
            result.hasErrors());

        message = messageSource.getMessage("bundle.error.firstQuestionnaireNotActive",
            new Object[]{}, LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "The validation of the first bundleQuestionnaire failed. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        bundleDTO.setBundleQuestionnaireDTOs(null);
        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));

        String locale = Helper.getRandomLocale();
        localizedWelcomeText.put(locale, "");
        localizedFinalText.put(locale, Helper.getRandomAlphabeticString(random.nextInt(125) + 1));
        bundleDTO.setLocalizedWelcomeText(localizedWelcomeText);
        bundleDTO.setLocalizedFinalText(localizedFinalText);
        bundleDTOValidator.validate(bundleDTO, result);
        assertTrue(
            "The validation of the welcome text failed. It hasn't caught an error except it was expected to do.",
            result.hasErrors());

        message = messageSource.getMessage("bundle.validator.welcomeText.notNull", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "The validation of the welcome text failed. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        localizedWelcomeText.put(locale, Helper.getRandomAlphabeticString(random.nextInt(125) + 1));

        localizedFinalText.put(locale, "");
        bundleDTO.setLocalizedWelcomeText(localizedWelcomeText);
        bundleDTO.setLocalizedFinalText(localizedFinalText);
        bundleDTOValidator.validate(bundleDTO, result);
        assertTrue(
            "The validation of the final text failed. It hasn't caught an error except it was expected to do.",
            result.hasErrors());

        message = messageSource.getMessage("bundle.validator.finalText.notNull", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "The validation of the final text failed. The returned error message didn't match the expected one.",
            message, testErrorMessage);

        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        localizedWelcomeText.clear();
        localizedFinalText.clear();
        localizedWelcomeText.put(locale, "");
        localizedFinalText.put(locale, Helper.getRandomAlphabeticString(random.nextInt(125) + 1));
        bundleDTO.setLocalizedWelcomeText(localizedWelcomeText);
        bundleDTO.setLocalizedFinalText(localizedFinalText);
        bundleDTOValidator.validate(bundleDTO, result);
        assertFalse(
            "The validation of the welcome text failed. It hasn't caught an error except it was expected to do.",
            result.hasErrors());

        localizedWelcomeText.clear();
        localizedFinalText.clear();
        localizedWelcomeText.put(locale, Helper.getRandomAlphabeticString(random.nextInt(125) + 1));
        localizedFinalText.put(locale, "");
        bundleDTO.setLocalizedWelcomeText(localizedWelcomeText);
        bundleDTO.setLocalizedFinalText(localizedFinalText);
        bundleDTOValidator.validate(bundleDTO, result);
        assertFalse(
            "The validation of the welcome text failed. It hasn't caught an error except it was expected to do.",
            result.hasErrors());

        Bundle bundle = BundleTest.getNewValidBundle();
        String name = Helper.getRandomAlphabeticString(random.nextInt(13) + 3);
        bundle.setName(name);
        bundleDao.merge(bundle);
        bundleDTO.setName(name);
        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        bundleDTOValidator.validate(bundleDTO, result);
        assertTrue(
            "The validation of the name failed. There was no bundle containing the bundleDTO's name except it was expected to do.",
            result.hasErrors());

        message = messageSource.getMessage("bundle.error.nameInUse", new Object[]{},
            LocaleContextHolder.getLocale());
        testErrorMessage = result.getAllErrors().get(0).getDefaultMessage();
        assertEquals(
            "The validation of the name failed. The returned error message didn't match the expected one.",
            message, testErrorMessage);
        bundleDao.remove(bundle);
    }
}
