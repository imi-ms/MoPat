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

from helper.Bundle import BundleSelectors
from helper.Login import LoginHelper
from selenium import webdriver

# !/usr/bin/env python3
# -*- coding: utf-8 -*-

loginHelper = LoginHelper()

from base_test import CustomChromeTest


class CustomTest(CustomChromeTest):
    def test_bundle_list(self):
        bundle = {}
        created_questionnaire = {}
        clinic = {}

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

        self.utils.check_visibility_of_element(BundleSelectors.TABLE_BUNDLE,
                                               "Bundle table not found")

        try:
            bundle = self.bundle_helper.create_bundle(publish_bundle=True,
                                                      questionnaires=[
                                                          created_questionnaire])
            bundle['id'] = self.bundle_helper.save_bundle(
                bundle_name=bundle['name'])
        except Exception as e:
            self.fail(f"Failed to create bundle: {e}")

        self.utils.check_visibility_of_element(BundleSelectors.CELL_FLAGICON,
                                               "Flag icon not found")

        try:
            self.navigation_helper.navigate_to_manage_clinics()
            clinic["name"] = self.clinic_helper.create_clinic(bundles=[bundle],
                                                              configurations=[{
                                                                                  'selector': (
                                                                                      By.CSS_SELECTOR,
                                                                                      '#usePseudonymizationService > div:nth-child(1) > div:nth-child(3) > label:nth-child(1)')}])
            clinic['id'] = self.clinic_helper.save_clinic(
                clinic_name=clinic['name'])

        except Exception as e:
            self.fail(f"Failed to create clinic: {e}")

        # Assert - Find clinics assigned to the bundle
        try:
            self.navigation_helper.navigate_to_manage_bundles()
            self.utils.search_item(bundle["name"], "bundle")
            bundle_row = self.bundle_helper.get_first_bundle_row()
            bundle_row.find_element(By.CSS_SELECTOR, "td:nth-child(3)")
            clinic_link = bundle_row.find_element(By.CSS_SELECTOR,
                                                  "ul > li > a")
            self.assertEqual(clinic_link.text, clinic["name"],
                             f'Clinic name "{clinic["name"]}" not found in bundle row.')

        except Exception as e:
            self.fail(f"Failed to find clinic assigned to bundle: {e}")

        self.utils.check_visibility_of_element(
            BundleSelectors.INPUT_BUNDLE_SEARCH,
            "Bundle table search box not found")
        self.utils.check_visibility_of_element(
            BundleSelectors.PAGINATION_BUNDLE,
            "Bundle table pagination not found")
        self.utils.check_visibility_of_element(
            BundleSelectors.BUTTON_ADD_BUNDLE, "Bundle add button not found")

        try:
            pass
        finally:
            self.utils.search_and_delete_item(clinic["name"], clinic["id"],
                                              "clinic")
            self.utils.search_and_delete_item(bundle["name"], bundle["id"],
                                              "bundle")
            self.utils.search_and_delete_item(created_questionnaire['name'],
                                              created_questionnaire['id'],
                                              "questionnaire")

        self.authentication_helper.logout()


if __name__ == "__main__":
    unittest.main(verbosity=2)
