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
from selenium import webdriver

loginHelper = LoginHelper()

from base_test import CustomChromeTest


class CustomTest(CustomChromeTest):
    def test_clinic_list(self):
        clinic = {}

        self.driver.get(self.https_base_url)
        self.authentication_helper.login(self.secret['admin-username'],
                                         self.secret['admin-password'])

        try:
            self.navigation_helper.navigate_to_manage_clinics()
        except Exception as e:
            self.fail(f"Failed to navigate to 'Clinic' page: {e}")

        try:
            self.navigation_helper.navigate_to_manage_clinics()
            clinic["name"] = self.clinic_helper.create_clinic(configurations=[{
                                                                                  'selector': (
                                                                                      By.CSS_SELECTOR,
                                                                                      '#usePseudonymizationService > div:nth-child(1) > div:nth-child(3) > label:nth-child(1)')}])
            clinic['id'] = self.clinic_helper.save_clinic(
                clinic_name=clinic['name'])

        except Exception as e:
            self.fail(f"Failed to create clinic: {e}")

        self.utils.check_visibility_of_element(ClinicSelectors.TABLE_CLINIC,
                                               "Clinic table not found")
        self.utils.check_visibility_of_element(
            ClinicSelectors.PAGINATION_CLINIC_TABLE,
            "Clinic table pagination not found")
        self.utils.check_visibility_of_element(ClinicSelectors.TABLE_SEARCH,
                                               "Clinic table search not found")
        self.utils.check_visibility_of_element(
            ClinicSelectors.TABLE_ACTION_BUTTONS,
            "Clinic table action buttons not found")
        self.utils.check_visibility_of_element(
            ClinicSelectors.BUTTON_ADD_CLINIC,
            "Add new clinic button not found")

        try:
            pass
        finally:
            self.utils.search_and_delete_item(clinic["name"], clinic["id"],
                                              "clinic")
            self.authentication_helper.logout()


if __name__ == "__main__":
    unittest.main(verbosity=2)
