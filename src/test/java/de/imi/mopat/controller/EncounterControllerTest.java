package de.imi.mopat.controller;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.helper.model.EncounterScheduledDTOMapper;
import de.imi.mopat.model.dto.EncounterScheduledApiRequestDTO;
import de.imi.mopat.model.dto.EncounterScheduledDTO;
import de.imi.mopat.service.EncounterScheduledService;
import de.imi.mopat.service.MailSendingStatus;
import de.imi.mopat.validator.EncounterScheduledApiRequestDTOValidator;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

public class EncounterControllerTest {

    private static final String CONFIGURED_API_KEY = "secret-key";

    private ConfigurationDao configurationDao;
    private EncounterScheduledDTOMapper encounterScheduledDTOMapper;
    private EncounterScheduledService encounterScheduledService;
    private BindingResult result;
    private EncounterController encounterController;

    private EncounterScheduledApiRequestDTO requestDto;

    @Before
    public void setUp() {
        configurationDao = mock(ConfigurationDao.class);
        encounterScheduledDTOMapper = mock(EncounterScheduledDTOMapper.class);
        encounterScheduledService = mock(EncounterScheduledService.class);
        EncounterScheduledApiRequestDTOValidator encounterScheduledApiRequestDTOValidator = mock(
            EncounterScheduledApiRequestDTOValidator.class);

        requestDto = new EncounterScheduledApiRequestDTO();
        requestDto.setEmail("test@example.com");
        result = new BeanPropertyBindingResult(requestDto, "encounterScheduledApiRequestDTO");

        encounterController = new EncounterController();
        ReflectionTestUtils.setField(encounterController, "configurationDao", configurationDao);
        ReflectionTestUtils.setField(encounterController, "encounterScheduledDTOMapper",
            encounterScheduledDTOMapper);
        ReflectionTestUtils.setField(encounterController, "encounterSchedulingService",
            encounterScheduledService);
        ReflectionTestUtils.setField(encounterController, "encounterScheduledApiRequestDTOValidator",
            encounterScheduledApiRequestDTOValidator);

        requestDto = new EncounterScheduledApiRequestDTO();
        requestDto.setEmail("test@example.com");
    }

    /**
     * isApiKeyAccessEnabled() == true && apiKey.equals(key) == true
     * -> Access granted, encounter is created successfully
     */
    @Test
    public void accessEnabledAndKeyMatches_returnsOk() {
        when(configurationDao.isApiKeyAccessEnabled()).thenReturn(true);
        when(configurationDao.getApiKey()).thenReturn(CONFIGURED_API_KEY);
        when(encounterScheduledDTOMapper.mapFromApiRequest(any()))
            .thenReturn(new EncounterScheduledDTO());
        when(encounterScheduledService.save(any(), any()))
            .thenReturn(MailSendingStatus.SUCCESS);

        ResponseEntity<?> response = encounterController.scheduleEncounterFromApi(requestDto, result,
            CONFIGURED_API_KEY);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Map.of("message", "Scheduled encounter created successfully"),
            response.getBody());
    }

    /**
     * isApiKeyAccessEnabled() == true && apiKey.equals(key) == false
     * -> Access denied, wrong key provided
     */
    @Test
    public void accessEnabledAndKeyDoesNotMatch_returnsUnauthorized() {
        when(configurationDao.isApiKeyAccessEnabled()).thenReturn(true);
        when(configurationDao.getApiKey()).thenReturn(CONFIGURED_API_KEY);

        ResponseEntity<?> response = encounterController.scheduleEncounterFromApi(requestDto, result,
            "wrong-key");

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(Map.of("error", "Unauthorized"), response.getBody());
    }

    /**
     * isApiKeyAccessEnabled() == false && apiKey.equals(key) == true
     * -> Access denied because the feature is disabled, even though the key would match
     */
    @Test
    public void accessDisabledAndKeyWouldMatch_returnsUnauthorized() {
        when(configurationDao.isApiKeyAccessEnabled()).thenReturn(false);
        when(configurationDao.getApiKey()).thenReturn(CONFIGURED_API_KEY);

        ResponseEntity<?> response = encounterController.scheduleEncounterFromApi(requestDto, result,
            CONFIGURED_API_KEY);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(Map.of("error", "Unauthorized"), response.getBody());
    }

    /**
     * isApiKeyAccessEnabled() == true && apiKey.equals(null) == false
     * -> No x-api-key header sent (key == null), access denied.
     * Assumes the condition is implemented as "apiKey.equals(key)"
     * (instead of "key.equals(apiKey)"), since the latter would throw
     * a NullPointerException.
     */
    @Test
    public void accessEnabledAndNoApiKeyHeaderSent_returnsUnauthorized() {
        when(configurationDao.isApiKeyAccessEnabled()).thenReturn(true);
        when(configurationDao.getApiKey()).thenReturn(CONFIGURED_API_KEY);

        ResponseEntity<?> response = encounterController.scheduleEncounterFromApi(requestDto, result,
            null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(Map.of("error", "Unauthorized"), response.getBody());
    }
}