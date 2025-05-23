#!/usr/bin/env python3
# -*- coding: utf-8 -*-

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
        #url = "localhost:8080/"
        cls.base_url = url
        cls.https_base_url = f"http://{url}"
        # secret used in the subclass
        #secret_filename = os.getenv('SECRET_FILENAME', "secret")
        #cls.secret = cls._loadSecretFile(cls, secret_filename)
        cls.secret = cls._loadSecretFile(cls, "mopat-ukm-dev_2025-05-02_13-46-29_secrets")
        cls.selenium_grid_url = f"http://localhost:4444/wd/hub/"


    def setUp(self) -> None:
        """
            Start a new driver for each test.
            Checks, if the script is called on the server or locally.
        """
        
        try:
            self._setServerDriver()
        except Exception as e:
            print(e)

        # maximize window to full-screen
        self.driver.maximize_window()

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
        """
            Sets the cookie to validate, if the test was successful or not.
        """
        if self.currentResult.wasSuccessful():
            cookie = {'name': 'zaleniumTestPassed', 'value': 'true'}
        else:
            cookie = {'name': 'zaleniumTestPassed', 'value': 'false'}
        self.driver.add_cookie(cookie)
        self.driver.quit()
        
    def _printError(self, error):
        exc_type, exc_value, tb = error
    
        captured_stderr = io.StringIO()
        sys.stderr = captured_stderr

        formatted_exception = ''.join(traceback.format_exception(exc_type, exc_value, tb))
        
        sys.stderr = sys.__stderr__

        print(formatted_exception)
        print(captured_stderr.getvalue()) 

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

    @abstractmethod
    def _setServerDriver(self):
        self.driver = None

    @abstractmethod
    def _setLocalDriver(self, directory):
        self.driver = None


class CustomTest(IMISeleniumBaseTest):

    def setUp(self):
        super().setUp()

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

    def test_mobile_encounter_interface_test(self):
        # Arrange
        self.driver.get(self.https_base_url)
        self.authentication_helper.login(self.secret['admin-username'], self.secret['admin-password'])

        clinic={}
        bundle={}
        created_questionnaire = {}
        
        try:
            created_questionnaire = self.questionnaire_helper.create_questionnaire_with_questions()
        except Exception as e:
            self.fail(f"Failed to create questionnaire: {e}")

        try:
            self.navigation_helper.navigate_to_manage_bundles()
            bundle=self.bundle_helper.create_bundle(publish_bundle=True, questionnaires=[created_questionnaire])
            bundle["id"]=self.bundle_helper.save_bundle(bundle["name"])
        except Exception as e:
            self.fail(f"Failed to create bundle: {e}")

        try:
            self.navigation_helper.navigate_to_manage_clinics()
            clinic["name"]=self.clinic_helper.create_clinic(configurations=[{'selector': (By.CSS_SELECTOR, '#registerPatientData > div > div > label')}, {'selector': (By.CSS_SELECTOR, '#usePseudonymizationService > div > div > label')}, {'selector': (By.CSS_SELECTOR, '#usePatientDataLookup > div > div > label')}],
                                             bundles=[bundle],
                                             users=[self.secret['admin-username']])
            clinic["id"]=self.clinic_helper.save_clinic(clinic["name"])

        except Exception as e:
            self.fail(f"Failed to create clinic: {e}")
            
            
            
        self.navigation_helper.navigate_to_execute_survey()
        
        
        self.utils.check_visibility_of_element(SurveySelectors.BUTTON_ADDITIONAL_INFORMATION, "Additional Information Button not found")
        self.utils.check_visibility_of_element(SurveySelectors.DROPDOWN_LANGUAGE_SELECTOR, "Language selector not found")
        
        self.utils.check_visibility_of_element(SurveySelectors.TAB_PATIENT_REGISTRATION, "Patient Registration tab not found")
        self.utils.check_visibility_of_element(SurveySelectors.TAB_PATIENT_DATA_AUTOMATION, "Patient Data Automation tab not found")
        self.utils.check_visibility_of_element(SurveySelectors.TAB_PATIENT_PSEUDONYMIZATION, "Patient Pseudonymization tab not found")
        
        self.utils.click_element(SurveySelectors.TAB_PATIENT_REGISTRATION)
        self.utils.check_visibility_of_element(SurveySelectors.BUTTON_CHECK_CASE_NUMBER, "Check Case Number Button not found")
        button_text = self.driver.find_element(*SurveySelectors.BUTTON_CHECK_CASE_NUMBER).text

        self.utils.click_element(SurveySelectors.TAB_PATIENT_DATA_AUTOMATION)
        self.utils.check_visibility_of_element(SurveySelectors.BUTTON_CHECK_CASE_NUMBER, "Check Case Number Button not found in Patient Data Automation tab")
        assert self.driver.find_element(*SurveySelectors.BUTTON_CHECK_CASE_NUMBER).text != button_text, "Button text changed in Patient Data Automation tab"

        self.utils.click_element(SurveySelectors.TAB_PATIENT_PSEUDONYMIZATION)
        self.utils.check_visibility_of_element(SurveySelectors.BUTTON_CHECK_CASE_NUMBER, "Check Case Number Button not found in Patient Pseudonymization tab")
        assert self.driver.find_element(*SurveySelectors.BUTTON_CHECK_CASE_NUMBER).text != button_text, "Button text changed in Patient Pseudonymization tab"    
            
        self.survey_helper.start_survey(clinic_name=clinic["name"])
        
        self.survey_helper.proceed_to_bundle_selection(bundle_name=bundle["name"])
        
        self.survey_helper.click_next_button()
        
        self.utils.check_visibility_of_element(SurveySelectors.TEXT_QUESTIONNAIRE_TITLE, "Questionnaire title not found")
        self.survey_helper.click_next_button()
        
        self.utils.click_element(SurveySelectors.BUTTON_ADDITIONAL_INFORMATION)
        self.utils.click_element(SurveySelectors.BUTTON_HELP)
        
        self.utils.check_visibility_of_element(SurveySelectors.BLOCK_HELP_MODE, "Help mode next button not found")
        
        self.utils.click_element(SurveySelectors.BUTTON_ADDITIONAL_INFORMATION)
        
        self.utils.check_visibility_of_element(SurveySelectors.BLOCK_PROGRESS_BAR, "Progress bar not found")
        self.utils.check_visibility_of_element(SurveySelectors.BUTTON_FONT_SIZE, "Font size button not found")
        self.survey_helper.answer_numbered_input_question({})
        self.survey_helper.click_next_button()
        
        self.survey_helper.click_next_button()
        
        self.survey_helper.answer_multiple_choice_question({})
        self.survey_helper.click_next_button()
        
        self.survey_helper.answer_slider_question({})
        self.survey_helper.click_next_button()
        
        self.survey_helper.answer_number_checkbox_question({})
        self.survey_helper.click_next_button()
        
        self.survey_helper.answer_number_checkbox_text_question({})
        self.survey_helper.click_next_button()
        
        self.survey_helper.select_dropdown_option({})
        self.survey_helper.click_next_button()
        
        self.survey_helper.answer_text_question()
        self.survey_helper.click_next_button()
        
        self.survey_helper.answer_date_question()
        self.survey_helper.click_next_button()
        
        self.survey_helper.click_next_button()
        
        self.utils.check_visibility_of_element(SurveySelectors.TEXT_BUNDLE_FINAL_INFO, "Bundle final info not found")

        self.survey_helper.end_survey()
        
        self.authentication_helper.logout()
        
    def tearDown(self): 
        self.driver.quit()


class IMISeleniumChromeTest(IMISeleniumBaseTest):
    """
        Test class for Chrome tests.
    """
    def _setServerDriver(self):
        name: str = f"{strftime('%Y-%m-%d-%H-%M-%S', gmtime())}_{self.base_url}_chrome"
        options = webdriver.ChromeOptions()
        options.set_capability("acceptInsecureCerts", True)
        options.add_argument("--headless=new")
        options.add_argument("--window-size=1920,1080")
        options.set_capability("selenoid:options", {
                                                    "enableVNC": False,
                                                    "enableVideo": False,
                                                    "enableLog": True,
                                                    "name": name,
                                                    "logName": f"{name}.log"
                                                    })
        self.driver = webdriver.Remote(options=options,
                                       command_executor=self.selenium_grid_url)


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
        self.errors.append((test, err))  # Store errors
        # Do not call the super method to prevent the default printing

    def addFailure(self, test, err):
        self.failures.append((test, err))  # Store failures
        # Do not call the super method to prevent the default printing

    def printErrors(self):
        # Override this method to suppress default error printing
        pass

    def printFailures(self):
        # Override this method to suppress default failure printing
        pass

    def printSummary(self):
        # Total Tests
        print(f"# Test Summary\n")
        print(f"**Total Tests Run:** {self.testsRun}\n")
        
        # Successful Tests
        print(f"**Successful Tests ({len(self.successful_tests)}/{self.testsRun}):**")
        if self.successful_tests:
            for test in self.successful_tests:
                print(f"- `{test}`")
        else:
            print(" None")
        
        # Failed Tests
        print(f"\n**Failed Tests ({len(self.failures)}/{self.testsRun}):**")
        if self.failures:
            for test, _ in self.failures:
                print(f"- `{test}`")
        else:
            print(" None")
        
        # Errored Tests
        print(f"\n**Errored Tests ({len(self.errors)}/{self.testsRun}):**")
        if self.errors:
            for test, _ in self.errors:
                print(f"- `{test}`")
        else:
            print(" None")
        
        print("\n---\n")

class CustomTestRunner(unittest.TextTestRunner):
    def _makeResult(self):
        return CustomTestResult(self.stream, self.descriptions, self.verbosity)

    def run(self, test):
        result = self._makeResult()
        result.failfast = self.failfast
        result.buffer = self.buffer
        result.tb_locals = self.tb_locals

        startTime = time.perf_counter()
        try:
            test(result)
        finally:
            stopTime = time.perf_counter()
            timeTaken = stopTime - startTime

            result.printErrors()  # Suppress default error print
            result.printFailures()  # Suppress default failure print

            result.printSummary()  # Print custom summary
            print(f"Time Taken: {timeTaken:.3f}s")  # Custom time output

        return result
    
if __name__ == "__main__":
    unittest.main(testRunner=CustomTestRunner(verbosity=2))
