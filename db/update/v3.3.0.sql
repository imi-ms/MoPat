USE moPat;

UPDATE export_template
SET export_template_type = REPLACE(export_template_type, 'FHIR', 'FHIR_DSTU3')
WHERE export_template_type LIKE 'FHIR';