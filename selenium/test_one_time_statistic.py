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
from helper.Statistic import StatisticSelector
from selenium import webdriver

loginHelper = LoginHelper()

from base_test import CustomChromeTest


class CustomTest(CustomChromeTest):
    def test_one_time_statistic(self):
        # Arrange
        self.driver.get(self.https_base_url)
        self.authentication_helper.login(self.secret['admin-username'],
                                         self.secret['admin-password'])

        # Act
        self.navigation_helper.navigate_to_one_time_statistic()

        self.utils.check_visibility_of_element(
            StatisticSelector.BUNDLE_DROP_DOWN, "Bundle dropdown not found")
        self.utils.check_visibility_of_element(
            StatisticSelector.BUNDLE_START_DATE, "Bundle start date not found")
        self.utils.check_visibility_of_element(
            StatisticSelector.BUNDLE_END_DATE, "Bundle end date not found")
        self.utils.check_visibility_of_element(StatisticSelector.PATIENT_ID,
                                               "Patient ID not found")
        self.utils.check_visibility_of_element(
            StatisticSelector.PATIENT_START_DATE,
            "Patient start date not found")
        self.utils.check_visibility_of_element(
            StatisticSelector.PATIENT_END_DATE, "Patient end date not found")
        self.utils.check_visibility_of_element(
            StatisticSelector.BUNDLE_PATIENT_PATIENT_ID,
            "Bundle patient ID not found")
        self.utils.check_visibility_of_element(
            StatisticSelector.BUNDLE_PATIENT_BUNDLE_ID,
            "Bundle patient bundle ID not found")
        self.utils.check_visibility_of_element(
            StatisticSelector.BUNDLE_PATIENT_START_DATE,
            "Bundle patient start date not found")
        self.utils.check_visibility_of_element(
            StatisticSelector.BUNDLE_PATIENT_END_DATE,
            "Bundle patient end date not found")

        self.utils.click_element(StatisticSelector.BUTTON_BERECHNEN)

        self.utils.check_visibility_of_element(StatisticSelector.ANZAHL_1,
                                               "Anzahl 1 not found")
        self.utils.check_visibility_of_element(StatisticSelector.ANZAHL_2,
                                               "Anzahl 2 not found")
        self.utils.check_visibility_of_element(StatisticSelector.ANZAHL_3,
                                               "Anzahl 3 not found")

        self.authentication_helper.logout()


if __name__ == "__main__":
    unittest.main(verbosity=2)
