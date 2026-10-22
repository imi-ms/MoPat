#!/usr/bin/env python3

import datetime
import json
import os
import re
import sys
import time
import traceback
import unittest
import unittest
from abc import ABC, abstractmethod
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait
from time import gmtime, strftime

from helper.ExportMapper import ExportSelectors
from helper.Login import LoginHelper
from helper.Question import QuestionType
from selenium import webdriver

loginHelper = LoginHelper()

from base_test import CustomChromeTest


class CustomTest(CustomChromeTest):
    def test_questionnaire_export_automatic_mapping(self):

        self.driver.get(self.https_base_url)
        self.authentication_helper.login(self.secret['admin-username'],
                                         self.secret['admin-password'])
        self.navigation_helper.navigate_to_manage_questionnaires()

        # Create a new questionnaire for testing
        created_questionnaire = self.questionnaire_helper.create_questionnaire_with_questions(
            question_types={QuestionType.MULTIPLE_CHOICE})

        # Navigate back to questionnaire list
        self.navigation_helper.navigate_to_manage_questionnaires()

        # Click the edit export for the newly created questionnaire
        self.export_helper.click_edit_export_button_for_questionnaire(
            created_questionnaire['name'])
        WebDriverWait(self.driver, 15).until(
            lambda
                driver: driver.current_url != self.https_base_url + "admin/questionnaire/list"
        )

        self.export_helper.click_upload_template_button()

        # Fill and submit upload form
        self.export_helper.fill_upload_form(
            template_name="test",
            export_type="ODM",
            file_path="odm_mapping.xml"
        )
        self.export_helper.submit_upload_form()

        # Navigate to mapping page
        if not self.export_helper.click_edit_mapping_for_file():
            raise AssertionError("Mapping page failed to load")

        # Validate initial state before mapping
        if not self.export_helper.validate_mapping_state_before_test():
            print("Warning: Initial state may not be optimal for testing")

        # Get initial field information
        initial_template_count = self.export_helper.get_available_template_fields_count()

        # Test automatic mapping functionality
        self.export_helper.click_map_data_button()

        # Verify automatic mapping worked
        if not self.export_helper.verify_automatic_mapping_success():
            raise AssertionError("Automatic mapping verification failed")

        # Get field counts after mapping
        after_mapping_mapped_count = self.export_helper.get_mapped_nodes_count()

        # Test clear mapping functionality
        self.export_helper.click_clear_mapping_button()

        # Verify clear mapping worked
        if not self.export_helper.verify_clear_mapping_success():
            raise AssertionError("Clear mapping verification failed")

        # Get final field counts
        final_template_count = self.export_helper.get_available_template_fields_count()
        final_mapped_count = self.export_helper.get_mapped_nodes_count()

        # Check that we ended up with the same number of available fields as we started with
        if final_template_count != initial_template_count:
            print(
                f"Warning: Template field count mismatch - Initial: {initial_template_count}, Final: {final_template_count}")

        if final_mapped_count != 0:
            raise AssertionError(
                f"Clear mapping failed - still have {final_mapped_count} mapped nodes")

        # Verify that mapping actually happened during the test
        if after_mapping_mapped_count == 0:
            raise AssertionError(
                "Automatic mapping did not create any mappings")

        # Test automatic mapping one more time to ensure repeatability and test save
        self.export_helper.click_map_data_button()

        repeat_mapped_count = self.export_helper.get_mapped_nodes_count()
        if repeat_mapped_count == 0:
            raise AssertionError("Automatic mapping failed on second attempt")

        time.sleep(1)
        self.utils.click_element(ExportSelectors.MAPPING_SAVE_BUTTON)
        # Navigate to mapping page
        if not self.export_helper.click_edit_mapping_for_file():
            raise AssertionError("Mapping page failed to load")

        # Get field counts after mapping
        after_save_mapping_mapped_count = self.export_helper.get_mapped_nodes_count()

        # Check that mapping is saved
        if after_mapping_mapped_count != after_save_mapping_mapped_count:
            raise AssertionError("Mapped fields not saved")

        self.authentication_helper.logout()


if __name__ == "__main__":
    unittest.main(verbosity=2)
