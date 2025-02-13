import datetime
import os
from enum import Enum
from typing import Optional

from selenium.common import TimeoutException
import xml.etree.ElementTree as ET
from selenium.webdriver.common.by import By
from selenium.webdriver.support.wait import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC

from helper.Navigation import NavigationHelper
from helper.SeleniumUtils import SeleniumUtils, DropdownMethod


class TemplateType(Enum):
    ORBIS = "ORBIS"
    ODM = "ODM"
    HL7V2 = "HL7v2"
    FHIR = "FHIR"
    REDCap = "REDCap"

class MappingSelectors:
    BUTTON_UPLOAD_TEMPLATE = (By.ID, "uploadtemplate")
    BUTTON_TRIGGER_UPLOAD_TEMPLATE = (By.ID, "uploadButton")
    BUTTON_UPLOAD_MAPPING = (By.ID, "uploadMappingButton")
    BUTTON_SUBMIT_UPLOAD = (By.ID, "submitUploadButton")

    FILE_UPLOAD_CONTAINER = (By.CSS_SELECTOR, "div.btn-file")
    
    DROPDOWN_TEMPLATE_TYPE = (By.ID, "type")
    
    INPUT_TEMPLATE_NAME = (By.ID, "name")
    INPUT_FILE_UPLOAD = (By.ID, "fileUpload")
    INPUT_TEMPLATE_UPLOAD = (By.CSS_SELECTOR, "#uploadForm input[type='file'][id='file']")

    LINK_TEMPLATE_MAPPING = lambda mapping_id: (By.XPATH, f'//a[@href="/mapping/map?id={mapping_id}"]')

    SEARCH_BOX_SELECTOR = (By.CSS_SELECTOR, "#DataTables_Table_0_filter input[type='search']")

    TABLE_LAST_ROW = (By.XPATH, "//tbody/tr[last()]")
    PAGINATION = (By.ID, "DataTables_Table_0_paginate")

    class MappingPage:
        BUTTON_SAVE = (By.ID, "saveButton")

        FIELD_QUESTIONNAIRE_NAME = (By.ID, "questionnaireName")
        FIELD_MAPPING_TYPE = (By.ID, "mappingType")
        FIELD_TEMPLATE_NAME = (By.ID, "templateName")
        FIELD_FILE_NAME = (By.ID, "fileName")
        FIELD_ORIGINAL_FILE_NAME = (By.ID, "originalFileName")

        EXPORT_MAPPING_CONTAINER = (By.ID, "exportMappingContainer")
        MAP_NODE = (By.CLASS_NAME, "map-node")
        MAP_ANSWER = (By.CLASS_NAME, "map-answer")
        TEMPLATE_NODES = (By.ID, "templateNodes")


class MappingHelper:
    def __init__(self, driver, navigation_helper: NavigationHelper):
        self.DEFAULT_TEMPLATE_UPLOAD_PATH = "export_templates"
        self.DEFAULT_TEMPLATE_FILE_NAME = "test_odm.xml"
        self.driver = driver
        self.utils = SeleniumUtils(driver, navigation_helper)
        self.navigation_helper = navigation_helper

    def upload_export_mapping_template(self, template_type: Optional[TemplateType] = None, template_name=None, upload_path=None, file_name=None):
        """
        :param template_type: The type of export mapping to select.
        :param template_name: The name of the export mapping.
        :param upload_path: The relative path to the export template directory (default: self.DEFAULT_TEMPLATE_UPLOAD_PATH).
        :param file_name: The absolute path to the file to be uploaded.
        """
        template_type = template_type or TemplateType.ODM
        timestamp: str = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        template_name = template_name or f"Export Template {timestamp} (Type: {template_type.value})"
        upload_path = upload_path or self.DEFAULT_TEMPLATE_UPLOAD_PATH
        file_name = file_name or self.DEFAULT_TEMPLATE_FILE_NAME

        if "/mapping/list" in self.driver.current_url:
            self.click_upload_template_button()

        try:
            # Select export mapping type
            self.utils.select_dropdown(MappingSelectors.DROPDOWN_TEMPLATE_TYPE, template_type.value, DropdownMethod.VALUE)

            # Enter mapping name
            self.utils.fill_text_field(MappingSelectors.INPUT_TEMPLATE_NAME, template_name)

            # Upload file
            image_input = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
                MappingSelectors.INPUT_TEMPLATE_UPLOAD))

            file_path = os.path.join(os.path.dirname(__file__), upload_path, file_name)
            assert os.path.exists(file_path), f"Test template not found at path: {file_path}"
            image_input.send_keys(file_path)

            return {
                "type": template_type,
                "name": template_name
            }
        except Exception as e:
            raise Exception(f"Error while uploading export mapping template: {e}")

    def trigger_template_upload(self, item_name):
        self.utils.click_element(MappingSelectors.BUTTON_TRIGGER_UPLOAD_TEMPLATE)
        return self.get_last_added_condition_id(item_name)

    def click_upload_template_button(self):
        self.utils.click_element(MappingSelectors.BUTTON_UPLOAD_TEMPLATE)

    def get_last_added_condition_id(self, item_name):
        """
        :param item_name: The name of the export mapping.
        :return: ID of the template mapping as a string.
        """
        self.utils.fill_text_field(MappingSelectors.SEARCH_BOX_SELECTOR, item_name)

        # Wait until the table rows are loaded
        WebDriverWait(self.driver, 30).until(EC.presence_of_element_located(
            MappingSelectors.TABLE_LAST_ROW))

        # Find the last row in the table
        last_row = self.driver.find_element(*MappingSelectors.TABLE_LAST_ROW)

        # Extract the ID from the `id` attribute of the last row
        template_id = last_row.get_attribute("id")

        # Ensure the ID is found
        if not template_id:
            raise Exception("The ID of the last question could not be found.")

        return template_id

    def navigate_to_mapping_page(self, mapping_info):
        self.utils.fill_text_field(MappingSelectors.SEARCH_BOX_SELECTOR, mapping_info['name'])
        mapping_link = WebDriverWait(self.driver, 10).until(EC.element_to_be_clickable(
            MappingSelectors.LINK_TEMPLATE_MAPPING(mapping_info['id'])))
        mapping_link.click()


class MappingAssertHelper(MappingHelper):

    def assert_template_list_page(self):
        try:
            # Verify the table containing the export mappings
            template_table = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
                MappingSelectors.TABLE_LAST_ROW))
            assert template_table.is_displayed(), "The table with export mappings is not displayed."

            # Verify search box for filtering export mappings
            search_box = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
                MappingSelectors.SEARCH_BOX_SELECTOR))
            assert search_box.is_displayed(), "Search box for export mappings is not displayed."

            # Verify pagination controls
            pagination_controls = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
                MappingSelectors.PAGINATION))
            assert pagination_controls.is_displayed(), "Pagination controls for export mappings are not displayed."

            # Verify button to upload a new export mapping
            upload_button = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
                MappingSelectors.BUTTON_UPLOAD_TEMPLATE))
            assert upload_button.is_displayed(), "Button to upload a new export mapping is not displayed."

        except TimeoutException:
            raise AssertionError("Timed out waiting for elements on the export mapping list page.")
        except AssertionError as e:
            raise e

    def assert_template_upload_page(self):
        try:
            # Verify dropdown for selecting export mapping type
            dropdown = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
                MappingSelectors.DROPDOWN_TEMPLATE_TYPE))
            assert dropdown.is_displayed(), "Dropdown for selecting export mapping type is not displayed."

            # Verify input for export mapping name
            template_name_input = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
                MappingSelectors.INPUT_TEMPLATE_NAME))
            assert template_name_input.is_displayed(), "Input for export mapping name is not displayed."

            # Verify file upload input
            file_upload_container = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
                MappingSelectors.FILE_UPLOAD_CONTAINER
            ))
            assert file_upload_container.is_displayed(), "File upload container for the export template is not displayed."

            # Verify upload button
            upload_button = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
                MappingSelectors.BUTTON_TRIGGER_UPLOAD_TEMPLATE))
            assert upload_button.is_displayed(), "Button to upload the export mapping template is not displayed."

        except TimeoutException:
            raise AssertionError("Timed out waiting for elements on the export mapping template upload page.")
        except AssertionError as e:
            raise e

    def assert_template_mapping_page(self, upload_path=None, file_name=None):
        """
        :param upload_path: The relative path to the export template directory (default: self.DEFAULT_TEMPLATE_UPLOAD_PATH).
        :param file_name: The absolute path to the file to be uploaded.
        """
        try:
            # Verify questionnaire name is displayed
            questionnaire_name = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
                MappingSelectors.MappingPage.FIELD_QUESTIONNAIRE_NAME))
            assert questionnaire_name.is_displayed(), "Questionnaire name is not displayed."

            # Verify export mapping type
            mapping_type = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
                MappingSelectors.MappingPage.FIELD_MAPPING_TYPE))
            assert mapping_type.is_displayed(), "Mapping type is not displayed."

            # Verify export template name
            template_name = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
                MappingSelectors.MappingPage.FIELD_TEMPLATE_NAME))
            assert template_name.is_displayed(), "Export template name is not displayed."

            # Verify file name and original file name
            file_name_element = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
                MappingSelectors.MappingPage.FIELD_FILE_NAME))
            assert file_name_element.is_displayed(), "File name is not displayed."

            original_file_name = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
                MappingSelectors.MappingPage.FIELD_ORIGINAL_FILE_NAME))
            assert original_file_name.is_displayed(), "Original file name is not displayed."

            questionnaire_data = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
                MappingSelectors.MappingPage.EXPORT_MAPPING_CONTAINER))
            assert questionnaire_data.is_displayed(), "Questionnaire data (answers) is not displayed."

            # Verify template fields
            template_fields = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
                MappingSelectors.MappingPage.TEMPLATE_NODES))
            assert template_fields.is_displayed(), "Template fields from export template are not displayed."

            # Verify draggable fields for mapping
            drag_sources = WebDriverWait(self.driver, 10).until(EC.presence_of_all_elements_located(
                MappingSelectors.MappingPage.MAP_NODE))
            assert len(drag_sources) > 0, "No draggable fields for mapping found."

            # Verify drop targets for mapping
            drop_targets = WebDriverWait(self.driver, 10).until(EC.presence_of_all_elements_located(
                MappingSelectors.MappingPage.MAP_ANSWER))
            assert len(drop_targets) > 0, "No drop targets for mapping found."

            # Verify button to save mapping

            save_button = WebDriverWait(self.driver, 10).until(EC.presence_of_element_located(
                MappingSelectors.MappingPage.BUTTON_SAVE))
            assert save_button.is_displayed(), "Button to save export mapping is not displayed."

            self.assert_template_fields_match_xml(upload_path, file_name)

        except TimeoutException:
            raise AssertionError("Timed out waiting for elements on the export mapping page.")
        except AssertionError as e:
            raise e

    def assert_template_fields_match_xml(self, upload_path=None, file_name=None):
        """
        :param upload_path: The relative path to the export template directory (default: self.DEFAULT_TEMPLATE_UPLOAD_PATH).
        :param file_name: The absolute path to the file to be uploaded.
        """
        html_fields = {
            elem.get_attribute("data-export-field")
            for elem in self.driver.find_elements(By.CSS_SELECTOR, "[data-export-field]")
        }

        try:
            xml_oids = self.extract_oid_values_from_xml(upload_path, file_name)
            html_fields_normalized = {self.normalize_html_oid(oid) for oid in html_fields}

            missing_fields = xml_oids - html_fields_normalized
            extra_fields = html_fields_normalized - xml_oids

            assert not missing_fields, f"The following template fields are missing in the HTML: {missing_fields}"
            assert not extra_fields, f"The following template fields are in the HTML, but not in the XML: {extra_fields}"

        except Exception as e:
            raise AssertionError(f"Error when comparing the template fields with the XML: {e}")

    def extract_oid_values_from_xml(self, upload_path=None, file_name=None):
        """
        :param upload_path: The relative path to the export template directory (default: self.DEFAULT_TEMPLATE_UPLOAD_PATH).
        :param file_name: The absolute path to the file to be uploaded.
        """
        upload_path = upload_path or self.DEFAULT_TEMPLATE_UPLOAD_PATH
        file_name = file_name or self.DEFAULT_TEMPLATE_FILE_NAME

        template_path = os.path.join(os.path.dirname(__file__), upload_path, file_name)
        print(template_path)
        tree = ET.parse(template_path)
        root = tree.getroot()

        # Namespace of the XML (ODM 1.3)
        ns = {"odm": "http://www.cdisc.org/ns/odm/v1.3"}

        # Extract the prefix from the `ItemGroupDef` (e.g. “SELENIUM_TEST_QUESTIONS”)
        item_group = root.find(".//odm:ItemGroupDef", ns)
        if item_group is None:
            raise ValueError("No `ItemGroupDef` with `OID` found.")

        prefix = item_group.get("OID")

        # Mapping of `CodeListOID` -> list of options
        code_lists = self.extract_code_lists(root, ns)

        # Extract all relevant OIDs
        return self.extract_item_oids(root, ns, prefix, code_lists)

    def extract_code_lists(self, root, ns):
        code_lists = {}

        for code_list in root.findall(".//odm:CodeList", ns):
            code_list_oid = code_list.get("OID")
            options = [item.get("CodedValue") for item in code_list.findall(".//odm:CodeListItem", ns)]
            code_lists[code_list_oid] = options

        return code_lists

    def extract_item_oids(self, root, ns, prefix, code_lists):
        extracted_oids = set()
        oids_to_remove = set()

        for item in root.findall(".//odm:ItemDef", ns):
            item_oid = item.get("OID")
            formatted_oid = f"{prefix}_{item_oid}"

            # Check whether this item uses a CodeListRef
            code_list_ref = item.find(".//odm:CodeListRef", ns)
            if code_list_ref is not None:
                code_list_oid = code_list_ref.get("CodeListOID")

                if code_list_oid in code_lists:
                    # Replace the main OID with the CodeList options
                    for option in code_lists[code_list_oid]:
                        extracted_oids.add(f"{formatted_oid}_{option}")
                    oids_to_remove.add(formatted_oid)

                else:
                    extracted_oids.add(formatted_oid)

            else:
                extracted_oids.add(formatted_oid)

        # Remove the OIDs that have been replaced by `CodeListItem
        return extracted_oids - oids_to_remove

    def normalize_html_oid(self, html_oid):
        return html_oid.replace("u005F", "_")