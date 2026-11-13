package de.imi.mopat.service;

import de.imi.mopat.io.EncounterExporter;
import de.imi.mopat.model.Encounter;
import de.imi.mopat.model.ExportTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EncounterExportService {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(
        EncounterExportService.class);

    @Autowired
    private EncounterExporter encounterExporter;

    /**
     * Application layer access point that retrieves export content without creating any side
     * effects (no file is written, no message is sent, no export history entry is created).
     * Encapsulates {@link EncounterExporter}.
     *
     * @param encounter      the {@link Encounter} whose responses should be exported
     * @param exportTemplate the {@link ExportTemplate} to export
     * @return the assembled export content as a {@link String}
     * @throws Exception if building the export content fails, e.g. due to missing configuration
     */

    public String getExportContent(final Encounter encounter, final ExportTemplate exportTemplate)
        throws Exception {
        try{
        return encounterExporter.buildExportContent(encounter, exportTemplate);
        }
        catch (Exception e){
            LOGGER.error("Could not build export content [exportTemplate={}, encounter={}] : {}",
                exportTemplate.getId(), encounter.getId(), e.getMessage());
            throw e;
        }
    }
}
