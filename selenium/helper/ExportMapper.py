import datetime
import os
import time

from selenium.common import TimeoutException
from selenium.webdriver.chrome.webdriver import WebDriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.support.ui import Select

from helper.Navigation import NavigationHelper
from helper.Navigation import QuestionnaireTableSelectors
from helper.Question import QuestionHelper, QuestionType, QuestionSelectors
from helper.SeleniumUtils import SeleniumUtils


class ExportSelectors:
    BUTTON_ADD_QUESTIONNAIRE = (By.ID, "addQuestionnaire")
    BUTTON_ADD_QUESTION = (By.ID, "addQuestion")
    BUTTON_SAVE = (By.ID, "saveButton")

    TABLE_FIRST_ROW = (By.XPATH, "//tbody/tr[not(@id='emptyRow')][1]")
    PAGINATION = (By.ID, "questionnaireTable_paginate")
    SEARCH_BOX = (By.CSS_SELECTOR, "#questionnaireTable_filter input[type='search']")

    EDIT_EXPORT_BUTTON_FIRST_ROW = (By.XPATH, "//table[@id='questionnaireTable']//tbody//tr[not(@id='emptyRow')][1]//td[contains(@class, 'actionColumn')]//a[3]")

    UPLOAD_TEMPLATE_BUTTON = (By.ID, "uploadtemplate")

    # Export template form selectors
    EXPORT_TYPE_DROPDOWN = (By.ID, "type")
    EXPORT_TYPE_ODM_OPTION = (By.XPATH, "//select[@id='exportType']/option[@value='ODM']")
    TEMPLATE_NAME_INPUT = (By.ID, "name")
    FILE_INPUT = (By.ID, "file")
    UPLOAD_FORM_SUBMIT = (By.ID, "uploadButton")
    FILE_MAPPING_EDIT_BUTTON = (By.XPATH, "//table//tbody//tr[not(@id='emptyRow')][1]//td[contains(@class, 'actionColumn')]//a[1]")

    # Mapping screen selectors
    MAP_DATA_BUTTON = (By.ID, "mapDataButton")
    CLEAR_MAPPING_BUTTON = (By.ID, "clearMappingButton")

    # Template and questionnaire nodes
    DRAGGABLE_FIELDS = (By.CSS_SELECTOR, "#templateNodes .draggable:not(.node-assigned)")
    AVAILABLE_ANSWERS = (By.CSS_SELECTOR, "#questionnaireNodes .map-question-box .map-answer:not(.has-node)")
    MAPPED_NODES = (By.CSS_SELECTOR, ".map-node.node-assigned")
    MAPPED_NODES = (By.CSS_SELECTOR, ".map-node.node-assigned")
    MAPPING_SAVE_BUTTON = (By.ID,"saveButton")

class ExportHelper:

    DEFAULT_DESCRIPTION = "This description of the questionnaire is a dummy text."
    DEFAULT_LANGUAGE_CODE = "de_DE"
    DEFAULT_LANGUAGE_CODE_EN = "en"
    DEFAULT_LOCALIZED_WELCOME_TEXT = 'A welcome text for the questionnaire. Nothing special to see.'
    DEFAULT_LOCALIZED_FINAL_TEXT = 'This text is shown at the end of the questionnaire.'

    def __init__(self, driver: WebDriver, navigation_helper: NavigationHelper):
        self.driver = driver
        self.utils = SeleniumUtils(driver, navigation_helper)
        self.navigation_helper = navigation_helper

    def click_add_questionnaire_button(self):
        """Clicks the 'Add Questionnaire' button."""
        self.utils.click_element(QuestionnaireSelectors.BUTTON_ADD_QUESTIONNAIRE)

    def click_edit_export_button_for_questionnaire(self, questionnaire_name):
        """
        Finds a questionnaire by name and clicks its edit export button.

        """
        # Search for the questionnaire
        search_box = WebDriverWait(self.driver, 10).until(
            EC.element_to_be_clickable(ExportSelectors.SEARCH_BOX)
        )
        search_box.clear()
        search_box.send_keys(questionnaire_name)

        # Wait for the table to filter
        WebDriverWait(self.driver, 10).until(
            EC.presence_of_element_located(ExportSelectors.TABLE_FIRST_ROW)
        )

        # Click the edit export button in the first row
        edit_export_button = WebDriverWait(self.driver, 10).until(
                    EC.element_to_be_clickable(ExportSelectors.EDIT_EXPORT_BUTTON_FIRST_ROW)
                )
        edit_export_button.click()


    def click_upload_template_button(self):
        """
        Clicks the Upload Template button on the export templates page.
        """

        upload_button = WebDriverWait(self.driver, 10).until(
                            EC.element_to_be_clickable(ExportSelectors.UPLOAD_TEMPLATE_BUTTON)
                        )
        upload_button.click()



    def fill_upload_form(self, template_name="test", export_type="ODM", file_path=None):
        try:
            # Select export type from dropdown
            export_dropdown = WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(ExportSelectors.EXPORT_TYPE_DROPDOWN)
            )
            select = Select(export_dropdown)
            select.select_by_value(export_type)

            # Enter template name
            name_input = WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(ExportSelectors.TEMPLATE_NAME_INPUT)
            )
            name_input.clear()
            name_input.send_keys(template_name)

            # Upload file
            self.upload_file(file_path)

        except TimeoutException as e:
            print(f"Timeout while filling upload form: {e}")
            raise

    def upload_file(self, file_path):
        """
        Uploads a file using the file input element.

        :param file_path: Path to the file to upload (relative to resources folder)
        """
        try:
            # Get absolute path to resources folder
            current_dir = os.path.dirname(os.path.abspath(__file__))
            resources_dir = os.path.join(current_dir, "..", "resources")
            absolute_file_path = os.path.abspath(os.path.join(resources_dir, file_path))

            if not os.path.exists(absolute_file_path):
                raise FileNotFoundError(f"File not found: {absolute_file_path}")

            # Find and use the file input
            file_input = WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(ExportSelectors.FILE_INPUT)
            )
            file_input.send_keys(absolute_file_path)

        except Exception as e:
            print(f"Error uploading file: {e}")
            raise

    def submit_upload_form(self):
        """
        Submits the upload form by clicking the submit button.
        """
        try:
            submit_button = WebDriverWait(self.driver, 10).until(
                EC.element_to_be_clickable(ExportSelectors.UPLOAD_FORM_SUBMIT)
            )
            submit_button.click()
        except TimeoutException:
            print("Could not find or click submit button")
            raise

    def click_edit_mapping_for_file(self):
        """
        Finds a questionnaire by name and clicks its share button.

        :param questionnaire_name: The name of the questionnaire to find
        """

        edit_button = WebDriverWait(self.driver, 10).until(
                    EC.element_to_be_clickable(ExportSelectors.FILE_MAPPING_EDIT_BUTTON)
                )
        edit_button.click()

        try:
            # Wait for mapping-specific elements to appear
            WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(ExportSelectors.MAP_DATA_BUTTON)
            )
            return True
        except TimeoutException:
            print(f"Mapping page did not load within {10} seconds")
            return False

    def get_available_template_fields_count(self):
        """
        Returns the count of available (unmapped) template fields.
        """
        try:
            fields = self.driver.find_elements(*ExportSelectors.DRAGGABLE_FIELDS)
            count = len(fields)
            return count
        except Exception as e:
            print(f"Error counting template fields: {e}")
            return 0

    def get_available_answer_fields_count(self):
        """
        Returns the count of available (unmapped) answer fields.
        """
        try:
            answers = self.driver.find_elements(*ExportSelectors.AVAILABLE_ANSWERS)
            count = len(answers)
            return count
        except Exception as e:
            print(f"Error counting answer fields: {e}")
            return 0

    def get_mapped_nodes_count(self):
        """
        Returns the count of currently mapped nodes.
        """
        try:
            mapped = self.driver.find_elements(*ExportSelectors.MAPPED_NODES)
            count = len(mapped)
            return count
        except Exception as e:
            print(f"Error counting mapped nodes: {e}")
            return 0

    def click_map_data_button(self):
        """
        Clicks the 'Automatically map fields' button.
        """
        try:
            map_button = WebDriverWait(self.driver, 10).until(
                EC.element_to_be_clickable(ExportSelectors.MAP_DATA_BUTTON)
            )
            map_button.click()
        except TimeoutException:
            print("Could not click Map Data button")
            raise

    def click_clear_mapping_button(self):
        """
        Clicks the 'Reset mapping' button.
        """
        try:
            clear_button = WebDriverWait(self.driver, 10).until(
                EC.element_to_be_clickable(ExportSelectors.CLEAR_MAPPING_BUTTON)
            )
            clear_button.click()
        except TimeoutException:
            print("Could not click Clear Mapping button")
            raise

    def verify_automatic_mapping_success(self):
        """
        Verifies that the automatic mapping was successful by checking:
        1. That some fields were mapped
        2. That the number of available fields decreased
        3. That mapped nodes exist
        """
        try:
            # Wait a moment for the mapping to complete
            self.driver.implicitly_wait(2)

            # Check that nodes are mapped
            mapped_count = self.get_mapped_nodes_count()
            available_template_count = self.get_available_template_fields_count()
            available_answer_count = self.get_available_answer_fields_count()

            # Verify that at least some mapping occurred
            if mapped_count > 0:
                return True
            else:
                print("Automatic mapping failed - no fields were mapped")
                return False

        except Exception as e:
            print(f"Error verifying mapping success: {e}")
            return False

    def verify_clear_mapping_success(self):
        """
        Verifies that clearing the mapping was successful by checking:
        1. That no mapped nodes exist
        2. That all fields are reset to available state
        """
        try:
            # Wait a moment for the clearing to complete
            self.driver.implicitly_wait(2)

            mapped_count = self.get_mapped_nodes_count()
            available_template_count = self.get_available_template_fields_count()

            # Verify that no mappings exist
            if mapped_count == 0:
                return True
            else:
                print("Clear mapping failed - some mappings still exist")
                return False

        except Exception as e:
            print(f"Error verifying clear mapping success: {e}")
            return False


    def validate_mapping_state_before_test(self):
        try:
            template_count = self.get_available_template_fields_count()
            answer_count = self.get_available_answer_fields_count()

            if template_count == 0:
                print("Warning: No template fields available for mapping")
                return False

            if answer_count == 0:
                print("Warning: No answer fields available for mapping")
                return False

            return True

        except Exception as e:
            print(f"Error validating mapping state: {e}")
            return False

    def click_save_mapping(self):
        save_button = WebDriverWait(self.driver, 10).until(
                    EC.element_to_be_clickable(ExportSelectors.MAPPING_SAVE_BUTTON)
                )
        save_button.click()