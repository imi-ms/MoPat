package de.imi.mopat.model.enumeration;

import de.imi.mopat.dao.ConfigurationDao;
import de.imi.mopat.io.EncounterExporterTemplate;
import de.imi.mopat.io.ExportTemplateImporter;
import de.imi.mopat.io.impl.EncounterExporterTemplateFhirDstu3;
import de.imi.mopat.io.impl.EncounterExporterTemplateFhirR4b;
import de.imi.mopat.io.impl.EncounterExporterTemplateODM;
import de.imi.mopat.io.impl.EncounterExporterTemplateOrbis;
import de.imi.mopat.io.impl.EncounterExporterTemplateHL7v2;
import de.imi.mopat.io.impl.EncounterExporterTemplateREDCap;
import de.imi.mopat.io.impl.ExportTemplateImporterFhirDstu3;
import de.imi.mopat.io.impl.ExportTemplateImporterFhirR4b;
import de.imi.mopat.io.impl.ExportTemplateImporterODM;
import de.imi.mopat.io.impl.ExportTemplateImporterOrbis;
import de.imi.mopat.io.impl.ExportTemplateImporterREDCap;
import org.slf4j.MarkerFactory;

/**
 * Definition of export template types. Used in {@link de.imi.mopat.model.ExportTemplate} objects.
 */
public enum ExportTemplateType {

    ORBIS(
            ExportTemplateImporterOrbis.class,
            EncounterExporterTemplateOrbis.class,
            "configurationGroup.label.ORBIS"
    ),
    ODM(
            ExportTemplateImporterODM.class,
            EncounterExporterTemplateODM.class,
            "configurationGroup.label.ODM"
    ),
    HL7v2(
            ExportTemplateImporterOrbis.class,
            EncounterExporterTemplateHL7v2.class,
            "configurationGroup.label.HLSeven"
    ),
    FHIR(
            null,
            null,
            "configurationGroup.label.FHIR"
    ),
    FHIR_DSTU3(
        ExportTemplateImporterFhirDstu3.class,
        EncounterExporterTemplateFhirDstu3.class,
        "configurationGroup.label.FHIR"
    ),
    FHIR_R4B(
        ExportTemplateImporterFhirR4b.class,
        EncounterExporterTemplateFhirR4b.class,
        "configurationGroup.label.FHIR"
    ),
    REDCap(
            ExportTemplateImporterREDCap.class,
            EncounterExporterTemplateREDCap.class,
            "configurationGroup.label.REDCap"
    );

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(
        ExportTemplateType.class);
    private final Class<?> importer;
    private final Class<?> exporter;
    private final String configurationMessageCode;

    ExportTemplateType(final Class<?> importer, final Class<?> exporter,
        final String configurationMessageCode) {
        this.importer = importer;
        this.exporter = exporter;
        this.configurationMessageCode = configurationMessageCode;
    }

    /**
     * Creates a new {@link EncounterExporterTemplate} instance.
     *
     * @param configurationDao The {@link ConfigurationDao} from the context.
     * @return A new {@link EncounterExporterTemplate} instance. Can be
     * <b>null</b>, which indicates that no exporter is available for the type.
     */
    public EncounterExporterTemplate createNewExporterInstance(
        final ConfigurationDao configurationDao) {
        if (exporter == null) {
            return null;
        }
        try {
            return (EncounterExporterTemplate) exporter.getConstructor(ConfigurationDao.class)
                .newInstance(configurationDao);
        } catch (ReflectiveOperationException ex) {
            LOGGER.error(MarkerFactory.getMarker("FATAL"),
                "fatal error while creating exporter instance from type " + "{}: {}", this, ex);
        }
        return null;
    }

    /**
     * Creates a new {@link ExportTemplateImporter} instance.
     *
     * @return A new {@link ExportTemplateImporter} instance. Can be
     * <b>null</b>, which indicates that no importer is available for the type.
     */
    public ExportTemplateImporter createNewImporterInstance() {
        if (importer == null) {
            return null;
        }
        try {
            return (ExportTemplateImporter) importer.getConstructor().newInstance();
        } catch (ReflectiveOperationException ex) {
            LOGGER.error(MarkerFactory.getMarker("FATAL"),
                "fatal error while creating importer instance from type " + "{}: {}", this, ex);
        }
        return null;
    }
    
    public static boolean isExportTemplateTypeAFhirType(ExportTemplateType exportTemplateType) {
        switch (exportTemplateType) {
            case FHIR_DSTU3, FHIR_R4B -> {
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    /**
     * Returns the class which exports the template by the specific type.
     *
     * @return Can be <b>null</b>, which indicates that no exporter is available for the type.
     */
    public Class<?> getExporter() {
        return exporter;
    }

    /**
     * Returns the class which reads the template by the specific type.
     *
     * @return Can be <b>null</b>, which indicates that no importer is available for the type.
     */
    public Class<?> getImporter() {
        return importer;
    }

    /**
     * Returns the label message code for the appropriate configuration groups. Must not be
     * <b>null</b>.
     *
     * @return The label message code for the appropriate configuration groups.
     */
    public String getConfigurationMessageCode() {
        return configurationMessageCode;
    }

}
