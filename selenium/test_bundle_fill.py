#!/usr/bin/env python3

import datetime
import json
import os
import re
import sys
import time
import traceback
import unittest
from abc import ABC, abstractmethod
from selenium.webdriver.common.by import By
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait
from time import gmtime, strftime

from helper.Bundle import BundleSelectors
from helper.Language import LanguageSelectors
from helper.Login import LoginHelper
from helper.SeleniumUtils import ErrorSelectors
from selenium import webdriver

# !/usr/bin/env python3
# -*- coding: utf-8 -*-

loginHelper = LoginHelper()

from base_test import CustomChromeTest


class CustomTest(CustomChromeTest):
    def test_bundle_fill(self):
        created_questionnaire = {}
        self.driver.get(self.https_base_url)
        self.authentication_helper.login(self.secret['admin-username'],
                                         self.secret['admin-password'])

        try:
            created_questionnaire = self.questionnaire_helper.create_questionnaire_with_questions()
        except Exception as e:
            self.fail(f"Failed to create questionnaire: {e}")

        try:
            self.navigation_helper.navigate_to_manage_bundles()
        except Exception as e:
            self.fail(f"Failed to navigate to 'Bundles' page: {e}")

        self.utils.click_element(BundleSelectors.BUTTON_ADD_BUNDLE)
        self.language_helper.open_language_dropdown()
        self.utils.check_visibility_of_element(
            LanguageSelectors.LANGUAGE_DROPDOWN,
            "Failed to open language dropdown")
        self.utils.check_visibility_of_element(BundleSelectors.INPUT_NAME,
                                               "Failed to locate bundle input")
        self.utils.check_visibility_of_element(
            BundleSelectors.INPUT_EDITABLE_DESCRIPTION,
            "Failed to locate bundle description input")
        self.utils.check_visibility_of_element(
            BundleSelectors.INPUT_WELCOME_TEXT,
            "Failed to locate welcome input")
        self.utils.check_visibility_of_element(BundleSelectors.INPUT_END_TEXT,
                                               "Failed to locate end input")
        self.utils.check_visibility_of_element(BundleSelectors.CHECKBOX_PUBLISH,
                                               "Failed to locate publish checkbox")
        self.utils.check_visibility_of_element(
            BundleSelectors.CHECKBOX_NAME_PROGRESS,
            "Failed to locate name progress checkbox")
        self.utils.check_visibility_of_element(
            BundleSelectors.CHECKBOX_PROGRESS_WHOLE_PACKAGE,
            "Failed to locate progress whole package checkbox")
        self.utils.check_visibility_of_element(
            BundleSelectors.TABLE_AVAILABLE_QUESTIONNAIRES,
            "Available questionnaires table not found")
        self.utils.check_visibility_of_element(
            BundleSelectors.TABLE_ASSIGNED_QUESTIONNAIRES,
            "Assigned questionnaires table not found")

        # Assert - Test for assigning questionnaire to bundle
        try:
            self.bundle_helper.assign_multiple_questionnaires_to_bundle(
                [created_questionnaire])
        except Exception as e:
            self.fail(f"Failed to assign questionnaire to bundle: {e}")

        # Assert - Test for removing questionnaire to bundle
        try:
            self.bundle_helper.remove_multiple_questionnaires_from_bundle(
                [created_questionnaire])
        except Exception as e:
            self.fail(f"Failed to assign questionnaire to bundle: {e}")

        # Assert - Check form validation
        try:
            self.utils.click_element(BundleSelectors.BUTTON_SAVE)
            WebDriverWait(self.driver, 10).until(
                EC.visibility_of_element_located(
                    ErrorSelectors.INPUT_VALIDATION_SELECTOR)
            )
            validation_errors = self.driver.find_elements(
                *ErrorSelectors.INPUT_VALIDATION_SELECTOR)
            self.assertEqual(len(validation_errors), 2,
                             "Expected 2 validation errors, but found {len(validation_errors)}")
        except Exception as e:
            self.fail(f"Failed to save bundle: {e}")


        # Finally
        finally:
            if (created_questionnaire):
                self.utils.search_and_delete_item(created_questionnaire['name'],
                                                  created_questionnaire['id'],
                                                  "questionnaire")

        self.authentication_helper.logout()


if __name__ == "__main__":
    unittest.main(verbosity=2)
