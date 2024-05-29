package de.imi.mopat.io.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.imi.mopat.io.ExportTemplateImporter;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

/**
 * An instance of the class {@link ExportTemplateImporterREDCap} shall be used to provide the
 * structure of JSON-files. It is designed to load an JSON file according to the structure provided
 * by the model of the REDCap Server.
 */
public class ExportTemplateImporterREDCap implements ExportTemplateImporter {

    @Override
    public List<String> importFile(final InputStream inputStream)
        throws IOException, SAXException, ParserConfigurationException {
        List<String> aggregatedExportFields = new ArrayList<>();

        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> jsonAttributes = mapper.convertValue(
            mapper.readTree(inputStream).get(0), new TypeReference<Map<String, String>>() {
            });

        for (String key : jsonAttributes.keySet()) {
            if (key.equals("redcap_event_name") || key.equals("redcap_data_access_group")
                || key.equals("redcap_repeat_instrument") || key.equals("redcap_repeat_instance")
                || key.endsWith("_complete") || key.equals("record_id")) {
                continue;
            }
            aggregatedExportFields.add(key);
        }
        return aggregatedExportFields;
    }
}
