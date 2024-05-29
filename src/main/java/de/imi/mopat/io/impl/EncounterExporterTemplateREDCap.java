package de.imi.mopat.io.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.helper.controller.Constants;
import de.imi.mopat.io.EncounterExporterTemplate;
import de.imi.mopat.model.Configuration;
import de.imi.mopat.model.Encounter;
import de.imi.mopat.model.ExportTemplate;
import de.imi.mopat.model.enumeration.ExportStatus;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

/**
 * The class EncounterExporterTemplateREDCap generates an JSON file with clinical data from an
 * Encounter provided.
 */
public class EncounterExporterTemplateREDCap implements EncounterExporterTemplate {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(
        EncounterExporterTemplateREDCap.class);
    private static final String FILE_SUFFIX = "json";
    private static final String DOT = ".";
    private static final String UNDERSCORE = "_";
    private static final SimpleDateFormat FILENAMEDATEFORMAT = new SimpleDateFormat(
        "dd.MM.yyyy_HH.mm.ss");

    private Encounter encounter;
    private ExportTemplate exportTemplate;
    private final ConfigurationDao configurationDao;
    private final ObjectMapper mapper;

    private Map<String, String> exportJSON;

    /**
     * Constructor with given {@link ConfigurationDao} to get configuration informations within this
     * instance.
     *
     * @param configurationDao The {@link ConfigurationDao} from the context.
     */
    public EncounterExporterTemplateREDCap(final ConfigurationDao configurationDao) {
        this.configurationDao = configurationDao;
        mapper = new ObjectMapper();
    }

    @Override
    public void load(final Encounter encounter, final ExportTemplate exportTemplate)
        throws Exception {
        assert encounter != null : "The Encounter was null";
        assert exportTemplate != null : "The ExportTemplate was null";
        this.encounter = encounter;
        this.exportTemplate = exportTemplate;

        String objectStoragePath = configurationDao.getObjectStoragePath();
        if (objectStoragePath == null) {
            LOGGER.error("[SETUP] No object storage path found. Please provide a "
                    + "value for {} in the {} file", Constants.OBJECT_STORAGE_PATH_PROPERTY,
                Constants.CONFIGURATION);
        } else {
            LOGGER.info("[SETUP] Object storage path configuration found.");
        }
        LOGGER.info(
            "[SETUP] Accessing properties file to look up the export path" + " in  {}...[DONE]",
            Constants.CONFIGURATION);

        String templatePath = objectStoragePath + Constants.EXPORT_TEMPLATE_SUB_DIRECTORY;
        String filename = exportTemplate.getFilename();
        File file = new File(templatePath, filename);

        exportJSON = mapper.convertValue(mapper.readTree(new FileInputStream(file)).get(0),
            new TypeReference<Map<String, String>>() {
            });

        if (exportJSON == null) {
            LOGGER.error("[SETUP] Could not convert template file to REDCap " + "JSON object");
        } else {
            LOGGER.info("[SETUP] Successfully converted template file to " + "REDCap JSON object.");
        }

        // Get export configurations
        String exportUrl = null;
        String apiToken = null;
        for (Configuration configuration : exportTemplate.getConfigurationGroup()
            .getConfigurations()) {
            if (configuration.getAttribute().equals("exportUrl")) {
                exportUrl = configuration.getValue();
            }
            if (configuration.getAttribute().equals("apiToken")) {
                apiToken = configuration.getValue();
            }
        }

        // Remove all given values except redcap_repeat_instrument
        for (String key : exportJSON.keySet()) {
            if (key.equals("record_id")) {
                exportJSON.put(key, encounter.getCaseNumber());
            } else if (key.endsWith("_complete")) {
                exportJSON.put(key, "1");
            } else if (key.equals("redcap_repeat_instrument") || key.equals("redcap_event_name")
                || key.equals("redcap_data_access_group")) {
            } else if (key.equals("redcap_repeat_instance")) {
                if (exportUrl != null && apiToken != null) {
                    exportJSON.put(key, "new");
                }
            } else {
                exportJSON.put(key, null);
            }
        }
    }

    @Override
    public void write(final String exportField, String value) throws Exception {
        switch (value) {
            case "TRUE":
                value = "1";
                break;
            case "FALSE":
                value = "0";
                break;
            default:
                break;
        }
        exportJSON.put(exportField, value);
    }

    @Override
    public ExportStatus flush() throws Exception {
        Boolean exportInDirectory = null;
        String exportPath = null;
        Boolean exportViaRest = null;
        String exportUrl = null;
        String apiToken = null;

        // Get export configurations
        for (Configuration configuration : exportTemplate.getConfigurationGroup()
            .getConfigurations()) {
            if (configuration.getAttribute().equals("exportInDirectory")) {
                exportInDirectory = Boolean.parseBoolean(configuration.getValue());
            }
            if (configuration.getAttribute().equals("exportPath")) {
                exportPath = configuration.getValue();
            }
            if (configuration.getAttribute().equals("exportViaRest")) {
                exportViaRest = Boolean.parseBoolean(configuration.getValue());
            }
            if (configuration.getAttribute().equals("exportUrl")) {
                exportUrl = configuration.getValue();
            }
            if (configuration.getAttribute().equals("apiToken")) {
                apiToken = configuration.getValue();
            }
        }

        if (exportInDirectory) {
            exportToDirectory(exportPath);
        }

        ExportStatus exportStatus = ExportStatus.SUCCESS;
        if (exportViaRest) {
            exportStatus = exportToHTTP(exportUrl, apiToken);
        }

        return exportStatus;
    }

    /**
     * Exports the resultant REDCap JSON to a given path
     *
     * @param exportPath Export path for the REDCap JSON file
     * @throws java.lang.Exception if a problem occurs
     */
    public void exportToDirectory(final String exportPath) throws Exception {
        // Make sure the path exists
        File path = new File(exportPath);
        if (!path.isDirectory()) {
            path.mkdirs();
        }

        //Create a sub-directory for the exported files
        String filepath = exportPath + File.separator + exportTemplate.getQuestionnaire().getName()
            .replaceAll(":", "_") + "/" + exportTemplate.getName().replaceAll(":", "_") + "/";
        File subDirectory = new File(filepath);
        if (!subDirectory.isDirectory()) {
            subDirectory.mkdirs();
        }

        // Write to disk
        File exportFile = new File(subDirectory, this.createFileName());
        mapper.writeValue(exportFile, "[" + mapper.writeValueAsString(exportJSON) + "]");
    }

    /**
     * Exports the resultant REDCap JSON to a URL via REST-Interface
     *
     * @param stringURL Export URL for the REST export
     * @param apiToken  API token for the REST export
     * @return ExportStatus can be SUCCESSFUL, CONFLICT or FAILURE
     * @throws java.lang.Exception if a problem occurs
     */
    public ExportStatus exportToHTTP(final String stringURL, final String apiToken)
        throws Exception {
        // Set the export parameter
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("token", apiToken));
        params.add(new BasicNameValuePair("content", "record"));
        params.add(new BasicNameValuePair("format", "json"));
        params.add(new BasicNameValuePair("type", "flat"));
        params.add(new BasicNameValuePair("overwriteBehavior", "normal"));
        params.add(new BasicNameValuePair("forceAutoNumber", "false"));
        params.add(
            new BasicNameValuePair("data", "[" + mapper.writeValueAsString(exportJSON) + "]"));
        params.add(new BasicNameValuePair("returnContent", "count"));
        params.add(new BasicNameValuePair("returnFormat", "json"));

        HttpPost post = new HttpPost(stringURL);
        post.setHeader("Content-Type", "application/x-www-form-urlencoded");
        post.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));

        // Build the request and execute it
        HttpClient client = HttpClientBuilder.create().build();
        HttpResponse resp = client.execute(post);

        switch (resp.getStatusLine().getStatusCode()) {
            case 200:
                return ExportStatus.SUCCESS;
            case 409:
                return ExportStatus.CONFLICT;
            default:
                return ExportStatus.FAILURE;
        }
    }

    /**
     * Creates a unique REDCap JSON filename
     *
     * @return The newly created unique REDCap JSON filename.
     */
    private String createFileName() {
        String result =
            encounter.getCaseNumber() + UNDERSCORE + exportTemplate.getOriginalFilename()
                + UNDERSCORE + FILENAMEDATEFORMAT.format(new Date()) + DOT + FILE_SUFFIX;
        return result;
    }
}
