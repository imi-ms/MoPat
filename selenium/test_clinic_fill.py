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
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait
from time import gmtime, strftime

from helper.Clinic import ClinicSelectors
from helper.Login import LoginHelper
from helper.SeleniumUtils import ErrorSelectors
from selenium import webdriver

# !/usr/bin/env python3
# -*- coding: utf-8 -*-

loginHelper = LoginHelper()

from base_test import CustomChromeTest


class CustomTest(CustomChromeTest):
    def test_clinic_fill(self):
        clinic = {}
        created_questionnaire = {}
        bundle = {}

        self.driver.get(self.https_base_url)
        self.authentication_helper.login(self.secret['admin-username'],
                                         self.secret['admin-password'])

        # Arrange
        try:
            created_questionnaire = self.questionnaire_helper.create_questionnaire_with_questions()
            self.navigation_helper.navigate_to_manage_bundles()
            bundle = self.bundle_helper.create_bundle(publish_bundle=True,
                                                      questionnaires=[
                                                          created_questionnaire])
            bundle['id'] = self.bundle_helper.save_bundle(
                bundle_name=bundle['name'])
        except Exception as e:
            self.fail(f"Failed to setup questionnaire and bundle: {e}")

        self.navigation_helper.navigate_to_manage_clinics()
        WebDriverWait(self.driver, 10).until(
            EC.visibility_of_element_located(ClinicSelectors.BUTTON_ADD_CLINIC))
        self.utils.click_element(ClinicSelectors.BUTTON_ADD_CLINIC)

        self.utils.check_visibility_of_element(
            ClinicSelectors.INPUT_CLINIC_NAME, "Clinic name input not found")
        self.utils.check_visibility_of_element(
            ClinicSelectors.INPUT_EDITABLE_DESCRIPTION,
            "Clinic description input not found")
        self.utils.check_visibility_of_element(
            ClinicSelectors.INPUT_CLINIC_EMAIL, "Clinic email input not found")

        # Assert - Check if the clinic configuration is displayed
        try:
            WebDriverWait(self.driver, 10).until(
                EC.visibility_of_element_located(
                    ClinicSelectors.DIV_CLINIC_CONFIGURATION)
            )
            clinic_configuration = self.driver.find_element(
                *ClinicSelectors.DIV_CLINIC_CONFIGURATION)
            clinic_configuration_list = clinic_configuration.find_elements(
                *ClinicSelectors.CLINIC_CONFIGURATION_LIST)
            self.assertGreaterEqual(len(clinic_configuration_list), 1,
                                    "Clinic configuration list should have at least one item")
        except:
            self.fail(
                f"Clinic configuration not found")

        self.utils.check_visibility_of_element(
            ClinicSelectors.TABLE_AVAIALBLE_BUNDLES,
            "Available bundles table not found")
        self.utils.check_visibility_of_element(
            ClinicSelectors.TABLE_ASSIGNED_BUNDLES,
            "Assigned bundles table not found")

        # Assert - Check if the bundles can be added to the clinic
        try:
            self.clinic_helper.assign_multiple_bundes_to_clinic(
                [{'id': bundle["id"], 'name': bundle["name"]}])
        except Exception as e:
            self.fail(f"Failed to assign bundle to clinic: {e}")

        # Assert - Check if the bundles can be removed from the clinic
        try:
            self.clinic_helper.remove_multiple_bundes_from_clinic(
                [{'id': bundle["id"], 'name': bundle["name"]}])
        except Exception as e:
            self.fail(f"Failed to remove bundle from clinic: {e}")

        self.utils.check_visibility_of_element(
            ClinicSelectors.TABLE_AVAIALBLE_USERS,
            "Available users table not found")
        self.utils.check_visibility_of_element(
            ClinicSelectors.TABLE_ASSIGNED_USERS,
            "Assigned users table not found")

        # Assert - Check if the users can be added to the clinic
        try:
            self.clinic_helper.assign_multiple_users_to_clinic(
                [self.secret.get('admin-username')])
        except Exception as e:
            self.fail(f"Failed to assign users to clinic: {e}")

        # Assert - Check if the users can be removed from the clinic
        try:
            self.clinic_helper.remove_multiple_users_from_clinic(
                [self.secret.get('admin-username')])
        except Exception as e:
            self.fail(f"Failed to remove users from clinic: {e}")

        # Assert - Check form validation
        try:
            self.utils.click_element(ClinicSelectors.BUTTON_SAVE)
            WebDriverWait(self.driver, 10).until(
                EC.visibility_of_element_located(
                    ErrorSelectors.INPUT_VALIDATION_SELECTOR)
            )
            validation_errors = self.driver.find_elements(
                *ErrorSelectors.INPUT_VALIDATION_SELECTOR)
            configuration_errors = self.driver.find_elements(
                *ErrorSelectors.CONFIGURATION_ERROR_SELECTOR)
            self.assertEqual(len(validation_errors), 2,
                             "Expected 2 validation errors, but found {len(validation_errors)}")
            self.assertEqual(len(configuration_errors), 1,
                             "Expected 1 configuration errors, but found {len(configuration_errors)}")
        except Exception as e:
            self.fail(f"Failed to save bundle: {e}")

        # Assert - Check if the clinic can be created
        try:
            self.navigation_helper.navigate_to_manage_clinics()
            clinic['name'] = self.clinic_helper.create_clinic(configurations=[{
                                                                                  'selector': (
                                                                                      By.CSS_SELECTOR,
                                                                                      '#usePseudonymizationService > div:nth-child(1) > div:nth-child(3) > label:nth-child(1)')}],
                                                              bundles=[{'id':
                                                                            bundle[
                                                                                "id"],
                                                                        'name':
                                                                            bundle[
                                                                                "name"]}],
                                                              users=[
                                                                  self.secret.get(
                                                                      'admin-username')])
            clinic['id'] = self.clinic_helper.save_clinic(clinic["name"])
            WebDriverWait(self.driver, 10).until(
                EC.visibility_of_element_located(ClinicSelectors.TABLE_CLINIC)
            )
        except Exception as e:
            self.fail(f"Failed to create clinic: {e}")

        finally:
            if clinic["id"]:
                self.utils.search_and_delete_item(clinic['name'], clinic["id"],
                                                  "clinic")
            if bundle["id"]:
                self.utils.search_and_delete_item(bundle["name"], bundle["id"],
                                                  "bundle")
            if created_questionnaire:
                self.utils.search_and_delete_item(created_questionnaire['name'],
                                                  created_questionnaire['id'],
                                                  "questionnaire")
            self.authentication_helper.logout()


if __name__ == "__main__":
    unittest.main(verbosity=2)
