#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import re
from time import gmtime, strftime
import datetime
import unittest
import json
import os
import sys
import io
import traceback
import time
from abc import ABC, abstractmethod
import unittest
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC

from helper.Authentication import AuthenticationHelper, AuthenticationAssertHelper
from helper.Bundle import BundleHelper, BundleSelectors
from helper.Clinic import ClinicHelper, ClinicSelectors
from helper.Condition import ConditionHelper, ConditionSelectors, ConditionAssertHelper
from helper.Configuration import ConfigurationHelper, ConfigurationSelectors
from helper.Encounter import EncounterHelper, EncounterSelectors, EncounterScheduleType
from helper.Login import LoginHelper
from helper.Navigation import NavigationHelper
from helper.Question import QuestionHelper, QuestionAssertHelper, QuestionType
from helper.Questionnaire import QuestionnaireHelper, QuestionnaireAssertHelper
from helper.Score import ScoreHelper, ScoreAssertHelper
from helper.SeleniumUtils import SeleniumUtils, ErrorSelectors
from helper.Survey import SurveyHelper, SurveyAssertHelper, SurveySelectors
from helper.Language import LanguageSelectors, LanguageHelper
from helper.User import UserHelper, UserRoles, UserSelector, EmailSelectors
from helper.Statistic import StatisticSelector
from helper.Dashboard import DashboardHelper, DashboardSelectors
from helper.ExportMapper import ExportHelper, ExportSelectors
from helper.Preview import PreviewHelper, PreviewAssertHelper, PreviewSelectors, PREVIEW_PRESETS

loginHelper = LoginHelper()

abspath = os.path.abspath(__file__)
dname = os.path.dirname(abspath)
os.chdir(dname)

# noinspection PyStatementEffect
class IMISeleniumBaseTest(ABC):
    """
        Base class of all IMI selenium tests.
        * Handles the initialization of the driver.
        * Switches between server and local mode
    """

    currentResult = None
    """ Attribute used to set a cookie for validation purposes. """
    driver = None
    """ Selenium driver used in the tests. """

    @classmethod
    def setUpClass(cls) -> None:
        """
            Used to initialize constants.
        """
        # get filename of calling script (url)
        url = "webapp-container:8080/"
        cls.base_url = url
        cls.https_base_url = f"http://{url}"
        # secret used in the subclass
        secret_filename = os.getenv('SECRET_FILENAME', "secret")
        cls.secret = cls._loadSecretFile(cls, secret_filename)
        cls.selenium_grid_url = f"http://localhost:4444/wd/hub/"


    def setUp(self) -> None:
        """
            Start a new driver for each test.
            Checks, if the script is called on the server or locally.
        """
        test_name = self._testMethodName
        self._setServerDriver(test_name)
        self._initializeHelpers()

        if self.driver is None:
            raise RuntimeError("Driver was not initialized.")


    def run(self, result=None):
        test_name = self._testMethodName
        number_of_failures_old = len(result.failures)
        number_of_errors_old = len(result.errors)

        # Printing the start of the test with Markdown-friendly format
        print(f"\n### Running Test: `{test_name}`\n")

        # Running the actual test
        self.currentResult = result
        unittest.TestCase.run(self, result)

        number_of_failures_new = len(result.failures)
        number_of_errors_new = len(result.errors)

        # Success or error handling
        if result.wasSuccessful() or (number_of_failures_old == number_of_failures_new and number_of_errors_old == number_of_errors_new):
            # Successfully ran the test without errors
            print("```txt\nSuccessfully ran Test without Errors\n```")
        else:
            # Test ran with errors
            print("```txt\nTest ran with errors:\n")
            for failed, error in result.failures + result.errors:
                if failed == self:
                    print("\n--- Stack Trace ---")
                    # Printing the stack trace in a code block
                    print(f"```\n{self._printError(error)}\n```")
                    print("--- End of Trace ---")
            print("```")

        # Printing the end of the test with Markdown-friendly format
        #print(f"\n### End of Test: `{test_name}`\n")

    def tearDown(self) -> None:
        self.driver.quit()

    def _printError(self, error):
        if isinstance(error, str):
            return error

        if isinstance(error, tuple) and len(error) >= 3:
            exc_type, exc_value, traceback_object = error[:3]

            return "".join(
                traceback.format_exception(
                    exc_type,
                    exc_value,
                    traceback_object,
                )
            )

        return repr(error)

    def _loadSecretFile(self, filename):
        """
        Used to try loading file from server or locally.

        :param filename: file to be loaded without .json
        :return: The loaded json object or None
        """
        secret_local = os.path.join(os.getcwd(), "secrets", f"{filename}.json")
        if os.path.exists(secret_local):
            with open(secret_local) as f:
                return json.load(f)
        return None

    def _initializeHelpers(self):
      self.navigation_helper = NavigationHelper(self.driver)
      self.utils = SeleniumUtils(self.driver, navigation_helper=self.navigation_helper)
      self.navigation_helper.utils = self.utils

      self.authentication_helper = AuthenticationHelper(self.driver)
      self.questionnaire_helper = QuestionnaireHelper(self.driver, self.navigation_helper)
      self.question_helper = QuestionHelper(self.driver, self.navigation_helper)
      self.bundle_helper = BundleHelper(self.driver, self.navigation_helper)
      self.clinic_helper = ClinicHelper(self.driver, self.navigation_helper)
      self.configuration_helper = ConfigurationHelper(self.driver, self.navigation_helper)
      self.survey_helper = SurveyHelper(self.driver, self.navigation_helper)
      self.condition_helper = ConditionHelper(self.driver, self.navigation_helper)
      self.score_helper = ScoreHelper(self.driver, self.navigation_helper)
      self.question_assert_helper = QuestionAssertHelper(self.driver, self.navigation_helper)
      self.survey_assert_helper = SurveyAssertHelper(self.driver, self.navigation_helper)
      self.authentication_assert_helper = AuthenticationAssertHelper(self.driver)
      self.questionnaire_assert_helper = QuestionnaireAssertHelper(self.driver, self.navigation_helper)
      self.score_assert_helper = ScoreAssertHelper(self.driver, self.navigation_helper)
      self.condition_assert_helper = ConditionAssertHelper(self.driver, self.navigation_helper)
      self.language_helper = LanguageHelper(self.driver, self.navigation_helper)
      self.encounter_helper = EncounterHelper(self.driver, self.navigation_helper)
      self.dashboard_helper = DashboardHelper(self.driver, self.navigation_helper)
      self.export_helper = ExportHelper(self.driver, self.navigation_helper)
      self.preview_assert_helper = PreviewAssertHelper(self.driver, self.navigation_helper)


    @abstractmethod
    def _setServerDriver(self, testname):
        self.driver = None

    @abstractmethod
    def _setLocalDriver(self, directory):
        self.driver = None


class CustomTest(IMISeleniumBaseTest):

    def setUp(self):
        super().setUp()

    def tearDown(self):
        self.driver.quit()


class IMISeleniumChromeTest(IMISeleniumBaseTest):
    """
        Test class for Chrome tests.
    """
    def _setServerDriver(self, test_name):
        safe_test_name = re.sub(r"[^A-Za-z0-9_.-]", "_", test_name)
        name = f"{strftime('%Y-%m-%d-%H-%M-%S', gmtime())}_{safe_test_name}_chrome"

        options = webdriver.ChromeOptions()
        options.add_argument("--window-size=1920,1080")
        options.set_capability("selenoid:options", {
            "enableVNC": True,
            "enableVideo": True,
            "videoName": f"{name}.mp4",
            "screenResolution": "1920x1080x24",
            "enableLog": True,
            "name": name,
            "logName": f"{name}.log"
        })
        options.set_capability("goog:loggingPrefs", {"browser": "ALL"})

        self.driver = webdriver.Remote(
            options=options,
            command_executor=self.selenium_grid_url
        )

    def _setLocalDriver(self, directory):
        # download latest driver
        from selenium.webdriver.chrome.service import Service
        from webdriver_manager.chrome import ChromeDriverManager
        from webdriver_manager.core.driver_cache import DriverCacheManager
        # init driver
        self.driver = webdriver.Chrome(service=Service(ChromeDriverManager(cache_manager=DriverCacheManager(directory)).install()))


class CustomChromeTest(CustomTest, IMISeleniumChromeTest, unittest.TestCase):
    # Do not touch this function. This is the main entry point for selenium
    pass

class CustomTestResult(unittest.TextTestResult):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.successful_tests = []

    def addSuccess(self, test):
        super().addSuccess(test)
        self.successful_tests.append(test)

    def addError(self, test, err):
        # Converts the exception into unittest's formatted error string
        super().addError(test, err)

    def addFailure(self, test, err):
        # Converts the exception into unittest's formatted failure string
        super().addFailure(test, err)

    def printErrors(self):
        pass

    def printFailures(self):
        pass

    def printSummary(self):
        print("# Test Summary\n")
        print(f"**Total Tests Run:** {self.testsRun}\n")

        print(
            f"**Successful Tests "
            f"({len(self.successful_tests)}/{self.testsRun}):**"
        )

        for test in self.successful_tests:
            print(f"- `{test}`")

        if not self.successful_tests:
            print(" None")

        print(f"\n**Failed Tests ({len(self.failures)}/{self.testsRun}):**")

        for test, _ in self.failures:
            print(f"- `{test}`")

        if not self.failures:
            print(" None")

        print(f"\n**Errored Tests ({len(self.errors)}/{self.testsRun}):**")

        for test, _ in self.errors:
            print(f"- `{test}`")

        if not self.errors:
            print(" None")

        print("\n---\n")

