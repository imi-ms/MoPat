package de.imi.mopat.validator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import de.imi.mopat.config.AppConfig;
import de.imi.mopat.config.ApplicationSecurityConfig;
import de.imi.mopat.config.MvcWebApplicationInitializer;
import de.imi.mopat.config.PersistenceConfig;
import de.imi.mopat.helper.controller.Constants;
import de.imi.mopat.model.dto.ExportRuleFormatDTO;
import de.imi.mopat.model.dto.ExportRulesDTO;
import de.imi.mopat.utils.Helper;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
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
public class ExportRulesDTOValidatorTest {

    private static final Random random = new Random();
    @Autowired
    ExportRulesDTOValidator exportRulesDTOValidator;

    /**
     * Test of {@link ExportRulesDTOValidator#supports(java.lang.Class)}<br> Valid input:
     * {@link ExportRulesDTO#class}<br> Invalid input: Other class than
     * {@link ExportRulesDTO#class}
     */
    @Test
    public void testSupports() {
        assertTrue(
            "Supports method for ExportRulesDTO.class failed. ExportRulesDTOValidator didn't support AnswerDTO.class except it was expected to do.",
            exportRulesDTOValidator.supports(ExportRulesDTO.class));
        assertFalse(
            "Supports method for random class failed. ExportRulesDTOValidator supported that class except it wasn't expected to do.",
            exportRulesDTOValidator.supports(Random.class));
    }

    /**
     * Test of
     * {@link ExportRulesDTOValidator#validate(java.lang.Object,
     * org.springframework.validation.Errors)}.
     * <br>
     * Valid input: Instance of {@link ExportRulesDTO} which contains a map of
     * {@link ExportRuleFormatDTO} instances whose {@link ExportRuleFormatDTO#decimalPlaces} is
     * <code>null</code> or matches {@link Constants#NUMBER_FORMAT} pattern.<br> Invalid input:
     * Instance of {@link ExportRulesDTO} which contains a map of {@link ExportRuleFormatDTO}
     * instances whose {@link ExportRuleFormatDTO#decimalPlaces} isn't <code>null</code> and doesn't
     * match {@link Constants#NUMBER_FORMAT} pattern.
     */
    @Test
    public void testValidate() {
        ExportRulesDTO exportRulesDTO = new ExportRulesDTO();
        Map<Long, ExportRuleFormatDTO> exportRuleFormats = new HashMap<>();
        Integer size = random.nextInt(23) + 1, randomPosition = random.nextInt(size);
        Long randomId = -1L;

        for (int i = 0; i < size; i++) {
            ExportRuleFormatDTO exportRuleFormatDTO = new ExportRuleFormatDTO();
            exportRuleFormatDTO.setId(Math.abs(random.nextLong()));
            exportRuleFormatDTO.setDecimalPlaces(randomPosition.toString());
            exportRuleFormats.put(exportRuleFormatDTO.getId(), exportRuleFormatDTO);

            if (i == randomPosition) {
                randomId = exportRuleFormatDTO.getId();
            }
        }

        BindingResult result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        exportRulesDTO.setExportRuleFormats(exportRuleFormats);
        exportRulesDTOValidator.validate(exportRulesDTO, result);
        assertFalse(
            "Validation of exportRulesDTO failed. The returned result has caught errors except it wasn't expected to do.",
            result.hasErrors());

        exportRulesDTO.getExportRuleFormats().get(randomId)
            .setDecimalPlaces(Helper.getRandomAlphabeticString(random.nextInt(23)));
        exportRulesDTOValidator.validate(exportRulesDTO, result);
        assertTrue(
            "Validation of exportRulesDTO failed. The returned result hasn't caught errors except it was expected to do.",
            result.hasErrors());

        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        exportRulesDTO.setExportRuleFormats(null);
        exportRulesDTOValidator.validate(exportRulesDTO, result);
        assertFalse(
            "Validation of exportRulesDTO failed. The returned result has caught errors except it wasn't expected to do.",
            result.hasErrors());

        result = new MapBindingResult(new HashMap<>(),
            Helper.getRandomAlphabeticString(random.nextInt(13)));
        exportRuleFormats = new HashMap<>();
        ExportRuleFormatDTO exportRuleFormatDTO;
        for (int i = 0; i < size; i++) {
            exportRuleFormatDTO = new ExportRuleFormatDTO();
            exportRuleFormatDTO.setId(Math.abs(random.nextLong()));
            exportRuleFormatDTO.setDecimalPlaces(randomPosition.toString());
            exportRuleFormats.put(exportRuleFormatDTO.getId(), exportRuleFormatDTO);

            if (i == randomPosition) {
                exportRuleFormatDTO.setDecimalPlaces(null);
            } else {
                exportRuleFormatDTO.setDecimalPlaces(randomPosition.toString());
            }
        }
        exportRulesDTO.setExportRuleFormats(exportRuleFormats);
        exportRulesDTOValidator.validate(exportRulesDTO, result);
        assertFalse(
            "Validation of exportRulesDTO failed. The returned result has caught errors except it wasn't expected to do.",
            result.hasErrors());

    }
}
