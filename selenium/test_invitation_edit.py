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

from helper.Login import LoginHelper
from helper.SeleniumUtils import ErrorSelectors
from helper.User import UserSelector
from selenium import webdriver

loginHelper = LoginHelper()

from base_test import CustomChromeTest


class CustomTest(CustomChromeTest):
    def test_invitation_edit(self):
        # Arrange
        self.driver.get(self.https_base_url)
        self.authentication_helper.login(self.secret['admin-username'],
                                         self.secret['admin-password'])

        # Arrange - Create a new clinic
        clinic = {}
        self.navigation_helper.navigate_to_manage_clinics()

        try:
            clinic["name"] = self.clinic_helper.create_clinic(configurations=[{
                                                                                  'selector': (
                                                                                      By.CSS_SELECTOR,
                                                                                      '#usePseudonymizationService > div:nth-child(1) > div:nth-child(3) > label:nth-child(1)')}], )
            clinic["id"] = self.clinic_helper.save_clinic(clinic["name"])
        except Exception:
            self.fail("Failed to create clinic")

        # Arrange - Click on the user menu
        self.navigation_helper.navigate_to_manager_user()

        self.utils.click_element(UserSelector.BUTTON_INVITE_USER)

        self.utils.check_visibility_of_element(
            UserSelector.INPUT_USER_FIRST_NAME(0),
            "First name input field not displayed")
        self.utils.check_visibility_of_element(
            UserSelector.INPUT_USER_LAST_NAME(0),
            "Last name input field not displayed")
        self.utils.check_visibility_of_element(UserSelector.INPUT_USER_EMAIL(0),
                                               "Email input field not displayed")
        self.utils.check_visibility_of_element(UserSelector.BUTTON_ADD_USER,
                                               "Add user button not displayed")
        self.utils.click_element(UserSelector.BUTTON_ADD_USER)
        self.utils.check_visibility_of_element(
            UserSelector.INPUT_USER_FIRST_NAME(1),
            "Second user's first name input field not displayed")
        self.utils.check_visibility_of_element(
            UserSelector.INPUT_USER_LAST_NAME(1),
            "Second user's last name input field not displayed")
        self.utils.check_visibility_of_element(UserSelector.INPUT_USER_EMAIL(1),
                                               "Second user's email input field not displayed")
        self.utils.click_element(UserSelector.BUTTON_REMOVE_INVITATION)

        # Assert - Check if the fields were removed
        try:
            WebDriverWait(self.driver, 10).until_not(
                EC.presence_of_element_located(
                    UserSelector.INPUT_USER_FIRST_NAME(0))
            )
        except Exception:
            self.fail("First name input field still displayed")

        try:
            WebDriverWait(self.driver, 10).until_not(
                EC.presence_of_element_located(
                    UserSelector.INPUT_USER_LAST_NAME(0))
            )
        except Exception:
            self.fail("Last name input field still displayed")

        try:
            WebDriverWait(self.driver, 10).until_not(
                EC.presence_of_element_located(UserSelector.INPUT_USER_EMAIL(0))
            )
        except Exception:
            self.fail("Email input field still displayed")

        self.utils.check_visibility_of_element(UserSelector.INPUT_CSV,
                                               "File upload button not displayed")
        self.utils.check_visibility_of_element(UserSelector.SELECT_USER_ROLE,
                                               "Role dropdown not displayed")
        self.utils.check_visibility_of_element(
            UserSelector.SELECT_USER_LANGUAGE,
            "Language dropdown not displayed")
        self.utils.check_visibility_of_element(UserSelector.INPUT_PERSONAL_TEXT,
                                               "Invite message input field not displayed")
        self.utils.check_visibility_of_element(
            UserSelector.TABLE_AVAILABLE_CLINICS,
            "Available clinic table not displayed")
        self.utils.check_visibility_of_element(
            UserSelector.TABLE_ASSIGNED_CLINICS,
            "Assigned clinic table not displayed")

        self.utils.click_element(UserSelector.BUTTON_MOVE_CLINIC(clinic["id"]))

        self.utils.click_element(UserSelector.BUTTON_MOVE_CLINIC(clinic["id"]))

        # Assert - Check validations
        try:
            WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(UserSelector.BUTTON_SEND_INVITE)
            )
            self.utils.click_element(UserSelector.BUTTON_SEND_INVITE)
            WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(
                    ErrorSelectors.INPUT_VALIDATION_SELECTOR)
            )
            validation_errors = self.driver.find_elements(
                *ErrorSelectors.INPUT_VALIDATION_SELECTOR)
            self.assertEqual(len(validation_errors), 3,
                             "Expected 3 validation errors, but found {len(validation_errors)}")

        except Exception as e:
            self.fail("Validation error not displayed")

        # Assert - Check if preview button works
        try:
            self.utils.fill_text_field(UserSelector.INPUT_USER_FIRST_NAME(0),
                                       "Test1")
            self.utils.fill_text_field(UserSelector.INPUT_USER_LAST_NAME(0),
                                       "Test2")
            self.utils.fill_text_field(UserSelector.INPUT_USER_EMAIL(0),
                                       "test@test.com")
            WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(UserSelector.BUTTON_PREVIEW)
            )
            self.utils.click_element(UserSelector.BUTTON_PREVIEW)
            WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(UserSelector.DIV_PREVIEW)
            )
        except Exception as e:
            self.fail("Preview not displayed")
        finally:
            if clinic["id"]:
                self.utils.search_and_delete_item(clinic["name"], clinic["id"],
                                                  "clinic")


if __name__ == "__main__":
    unittest.main(verbosity=2)
