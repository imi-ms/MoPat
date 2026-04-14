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
}
