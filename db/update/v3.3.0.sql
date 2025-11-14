USE `moPat`;

ALTER TABLE moPat.configuration MODIFY COLUMN value TEXT DEFAULT NULL NULL;


INSERT INTO moPat.configuration
(configuration_group_id, parent, `position`, `type`, `attribute`, configuration_type, description_message_code, class, label_message_code, test_method, update_method, uuid, value, pattern)
VALUES(1, NULL, 16, 'GENERAL', 'imprintText', 'RICH_TEXT', 'configuration.description.imprint', 'GLOBAL', 'configuration.label.imprint', NULL, NULL, 'ff890137-bfb5-4e3a-a2a0-b51b7ff6b088', 'Universität Münster<br>Schlossplatz 2, 48149 Münster<br>Telephone: +49 (251) 83-0<br>Fax: +49 (251) 83-3 20 90<br>E-mail: verwaltung@uni-muenster.de<br><br>The University of Münster is a statutory body and an institution of the Land of North Rhine- Westphalia. It is represented by the Rector, Professor Dr. Johannes Wessels.<br><br>Turnover tax identification number: DE 126118759<br><br>Edited in accordance with §5 TMG by:<br>Univ.-Prof. Dr. rer. nat. Dominik Heider<br>Institute of Medical Informatics<br>Albert-Schweizer-Campus 1, Building A11<br>48149 Münster, Germany<br>Telephone: +49 (251) 83-55262<br>E-Mail:&nbsp;<a href="mailto:imi@uni-muenster.de" style="color: rgb(13, 110, 253);">imi@uni-muenster.de</a>', NULL);

UPDATE export_template
SET export_template_type = REPLACE(export_template_type, 'FHIR', 'FHIR_DSTU3')
WHERE export_template_type LIKE 'FHIR';