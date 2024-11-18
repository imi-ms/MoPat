package de.imi.mopat.controller;

import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.model.Configuration;

import java.io.IOException;
import java.io.InputStream;

import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * The Controller servlet communicates with the front end of the model and loads the
 * HttpServletRequest or HttpSession with appropriate data, before forwarding the HttpServletRequest
 * and Response to the JSP using a RequestDispatcher.
 */
@RestController
public class PseudonymizationController {

    @Autowired
    private ConfigurationDao configurationDao;

    // Initialize every needed configuration information as a final string
    private final String className = this.getClass().getName();
    // Configuration: The name of the attribute for the pseudonymizationUrl
    public static final String PSEUDONYMIZATION_SERVICE_URL = "pseudonymizationServiceUrl";
    private static final String PSEUDONYMIZATION_SERVICE_API_KEY = "pseudonymizationServiceApiKey";
    public final String usePseudonymizationServiceGroupName = "configurationGroup.label.pseudonymization";
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(
        PseudonymizationController.class);

    /**
     * Handles the tokening with the psuedonymization server and returns the url with the
     * appropriate token to get a pseudonym.
     *
     * @return The url with the appropriate token to get a pseudonym from the pseudonymization
     * service.
     */
    @RequestMapping(value = "/pseudonymization/pseudonym", method = RequestMethod.GET)
    public String pseudo(@RequestParam(value = "clinicId", required = true) final Long clinicId) {
        HttpClient httpClient = HttpClientBuilder.create().build();
        String sessionURL = getSessionURL(httpClient, clinicId);
        String tokenId = getTokenId(sessionURL, httpClient,clinicId);
        return getPseudonymizationServiceURL(clinicId) + "patients?tokenId=" + tokenId;
    }

    /**
     * Gets a session url with a valid session token from the pseudonymization server with
     * configured baseURL.
     *
     * @param httpClient A HTTP-Client for the connection.
     * @return The session url with a valid session token from the pseudonymization server.
     */
    private String getSessionURL(final HttpClient httpClient, Long clinicId) {
        String connectionUrl = getPseudonymizationServiceURL(clinicId) + "sessions/";
        HttpPost request = new HttpPost(connectionUrl);
        request.addHeader("mainzellisteApiKey", getPseudonymizationServiceAPIKey(clinicId));
        try {
            HttpResponse httpResponse = httpClient.execute(request);
            InputStream connectionResponse = httpResponse.getEntity().getContent();
            String response = IOUtils.toString(connectionResponse, StandardCharsets.UTF_8);
            JSONObject jsonResponse = new JSONObject(response);
            return jsonResponse.getString("uri");

        } catch (IOException exception) {
            LOGGER.debug("Error while connecting to the pseudonymization server: {}",
                exception.getLocalizedMessage());
        }
        return "";
    }

    /**
     * Gets a query token which allows to add a new patient from the pseudonymization server with
     * given sessionURL.
     *
     * @param sessionUrl A session url with a valid session token from the pseudonymization server.
     * @param httpClient A HTTP-Client for the connection.
     * @return The query token from the pseudonymization server.
     */
    private String getTokenId(final String sessionUrl, final HttpClient httpClient, Long clinicId) {
        String connectionUrl = sessionUrl + "tokens/";
        HttpPost request = new HttpPost(connectionUrl);
        request.addHeader("content-type", "application/json");
        request.addHeader("mainzellisteApiKey", getPseudonymizationServiceAPIKey(clinicId));
        JSONObject type = new JSONObject();
        JSONObject callback = new JSONObject();
        type.put("data", callback);
        type.put("type", "addPatient");
        request.setEntity(new StringEntity(type.toString(), ContentType.APPLICATION_JSON));
        try {
            HttpResponse httpResponse = httpClient.execute(request);
            InputStream connectionResponse = httpResponse.getEntity().getContent();
            String response = IOUtils.toString(connectionResponse, StandardCharsets.UTF_8);
            JSONObject jsonResponse = new JSONObject(response);
            return jsonResponse.getString("tokenId");
        } catch (IOException exception) {
            LOGGER.debug("Error while connecting to the pseudonymization server: {}",
                exception.getLocalizedMessage());
        }
        return "";
    }

    /**
     * Returns the URL of the server that is responsible for pseudonymization.
     *
     * @return The URL as string.
     */
    public String getPseudonymizationServiceURL(Long clinicId) {
        Configuration configuration = configurationDao.getConfigurationByGroupName(
            clinicId, PSEUDONYMIZATION_SERVICE_URL, className, usePseudonymizationServiceGroupName);
        return String.valueOf(configuration.getValue());
    }

    /**
     * Returns the API key of the server that is responsible for. pseudonymization
     *
     * @return The API key as string.
     */
    public String getPseudonymizationServiceAPIKey(Long clinicId) {
        Configuration configuration = configurationDao.getConfigurationByGroupName(
            clinicId, PSEUDONYMIZATION_SERVICE_API_KEY, className, usePseudonymizationServiceGroupName);
        return String.valueOf(configuration.getValue());
    }
}
