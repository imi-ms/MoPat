package de.imi.mopat.io.impl;

import de.imi.mopat.io.MetadataExporter;
import de.imi.mopat.model.enumeration.MetadataFormat;

/**
 * A factory to get a {@link MetadataExporter} for a specific {@link MetadataFormat}.
 */
public class MetadataExporterFactory {

    /**
     * Returns a {@link MetadataExporter} for the given {@link MetadataFormat}.
     *
     * @param metadataFormat {@link MetadataFormat} of the requested {@link MetadataExporter}
     * @return a {@link MetadataExporter} for the given {@link MetadataFormat}
     */
    public static MetadataExporter getMetadataExporter(final MetadataFormat metadataFormat) {
        switch (metadataFormat) {
            case MoPat:
                return new MetadataExporterMoPat();
            case ODM:
                return new MetadataExporterODM();
            case PDF:
                return new MetadataExporterPDF();
            case FHIR:
                return new MetadataExporterFHIR();
            case ODMExportTemplate:
                return new MetadataExporterODMExportTemplate();
            default:
                return null;
        }
    }
}
