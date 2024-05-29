package de.imi.mopat.model.enumeration;

/**
 * Definition of metadata formats supported by MoPat.
 */
public enum MetadataFormat {
    // For now supported formats and their file extensions
    ODM("ODM", ".xml"), PDF("PDF", ".zip"), FHIR("FHIR", ".xml"), ODMExportTemplate(
        "ODMExportTemplate", ".xml"), MoPat("MoPat", ".json");

    private final String textValue;
    private final String fileExtension;

    MetadataFormat(final String textValue, final String fileExtension) {
        this.textValue = textValue;
        this.fileExtension = fileExtension;
    }

    public String getTextValue() {
        return textValue;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    @Override
    public String toString() {
        return textValue;
    }
}
