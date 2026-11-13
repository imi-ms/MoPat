package de.imi.mopat.io;

import de.imi.mopat.model.Encounter;
import de.imi.mopat.model.ExportTemplate;
import de.imi.mopat.model.enumeration.ExportStatus;
import org.springframework.stereotype.Component;

/**
 * This interface defines how an exporter should be implemented to be called by the
 * {@link EncounterExporter}.
 * <p>
 * Implement this interface if you want to implement an exporter to a new KIS. Every exporter belongs
 * to a single {@link de.imi.mopat.model.enumeration.ExportTemplateType}.
 */
@Component
public interface EncounterExporterTemplate {

    /**
     * Used to initialize the exporter with all it's dependencies. Typically opens and reads the
     * blank export template from disk.
     *
     * @param encounter      which should be exported
     * @param exportTemplate hold the information how the encounter should be exported
     * @throws java.lang.Exception if initializing went wrong
     */
    void load(Encounter encounter, ExportTemplate exportTemplate) throws Exception;

    /**
     * Writes a single value to a single exportField.
     *
     * @param exportField name of the field in the export template
     * @param value       value which should be written in the export field
     * @throws java.lang.Exception if write a value went wrong
     */
    void write(String exportField, String value) throws Exception;

    /**
     * Flushes the filled export template to the export path with a corresponding file name
     *
     * @return export status for the filled export template that was  flushed
     * @throws java.lang.Exception if flush to disk went wrong
     */
    ExportStatus flush() throws Exception;

    /**
     * Builds and returns the fully assembled export content for the currently loaded and filled
     * export template, without triggering any of the side effects performed by {@link #flush()}
     * (e.g. writing the export to disk, sending it to a communication server, or delivering it via
     * HL7v2). This method is intended for on-demand, read-only access to the export data, such as
     * a manual download triggered by the user.

     * Repeated calls to this method are safe and will not cause duplicate exports, since no data
     * is persisted or transmitted as part of its execution.
     *
     * @return the assembled export content as a {@link String}, ready to be presented to the user
     *         (e.g. as a file download)
     * @throws java.lang.Exception if the export content could not be built
     */
    String getExportContent() throws Exception;
}
