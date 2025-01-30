package de.imi.mopat.io.impl;

import de.imi.mopat.io.MetadataExporter;
import de.imi.mopat.model.enumeration.MetadataFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * A factory to get a {@link MetadataExporter} for a specific {@link MetadataFormat}.
 */
@Service
public class MetadataExporterFactory {

    @Autowired
    private MetadataExporterMoPat metadataExporterMoPat;

    @Autowired
    private MetadataExporterODM metadataExporterODM;

    @Autowired
    private MetadataExporterPDF metadataExporterPDF;

    @Autowired
    private MetadataExporterFHIR metadataExporterFHIR;

    @Autowired
    private MetadataExporterODMExportTemplate metadataExporterODMExportTemplate;
    /**
     * Returns a {@link MetadataExporter} for the given {@link MetadataFormat}.
     *
     * @param metadataFormat {@link MetadataFormat} of the requested {@link MetadataExporter}
     * @return a {@link MetadataExporter} for the given {@link MetadataFormat}
     */
    public MetadataExporter getMetadataExporter(final MetadataFormat metadataFormat) {
        switch (metadataFormat) {
            case MoPat:
                return metadataExporterMoPat;
            case ODM:
                return metadataExporterODM;
            case PDF:
                return metadataExporterPDF;
            case FHIR:
                return metadataExporterFHIR;
            case ODMExportTemplate:
                return metadataExporterODMExportTemplate;
            default:
                return null;
        }
    }
}
