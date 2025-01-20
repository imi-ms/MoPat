#!/usr/bin/env python3
# -*- coding: utf-8 -*-

from time import gmtime, strftime
import unittest
import json
import os
import traceback
import time
from abc import ABC, abstractmethod
import unittest
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC

from helper.Authentication import AuthenticationHelper, AuthenticationAssertHelper
from helper.Bundle import BundleHelper
from helper.Clinic import ClinicHelper
from helper.Condition import ConditionHelper, ConditionSelectors, ConditionAssertHelper
from helper.Login import LoginHelper
from helper.Navigation import NavigationHelper
from helper.Question import QuestionHelper, QuestionAssertHelper, QuestionType
from helper.Questionnaire import QuestionnaireHelper, QuestionnaireAssertHelper
from helper.Score import ScoreHelper, ScoreAssertHelper
from helper.SeleniumUtils import SeleniumUtils
from helper.Survey import SurveyHelper, SurveyAssertHelper

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
        cls.secret = cls._loadSecretFile(cls, "secret")
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
        print(f"=================== RUNNING TEST '{test_name}' ===================")
        self.currentResult = result
        unittest.TestCase.run(self, result)
        
        if result.wasSuccessful():
            print("Successfully ran Test without Errors")
        else:
            print("Test ran with errors:")
            for failed, error in result.failures + result.errors:
                if failed == self:
                    print("\n--- Stack Trace ---")
                    self._printError(error)
                    print("--- End of Trace ---")
        print(f"=================== END OF '{test_name}' ===================\n")

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
        traceback.print_exception(exc_type, exc_value, tb)

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
        self.survey_helper = SurveyHelper(self.driver, self.navigation_helper)
        self.condition_helper = ConditionHelper(self.driver, self.navigation_helper)
        self.score_helper = ScoreHelper(self.driver, self.navigation_helper)
        self.question_assert_helper = QuestionAssertHelper(self.driver, self.navigation_helper)
        self.survey_assert_helper = SurveyAssertHelper(self.driver, self.navigation_helper)
        self.authentication_assert_helper = AuthenticationAssertHelper(self.driver)
        self.questionnaire_assert_helper = QuestionnaireAssertHelper(self.driver, self.navigation_helper)
        self.score_assert_helper = ScoreAssertHelper(self.driver, self.navigation_helper)
        self.condition_assert_helper = ConditionAssertHelper(self.driver, self.navigation_helper)

    def test_login_admin(self):
        if(self.secret['admin-username']!='' and self.secret['admin-password']!=''):
            self.driver.get(self.https_base_url)

            loginHelper.login(self.driver,self.secret['admin-username'], self.secret['admin-password'])

            WebDriverWait(self.driver, 10).until(EC.url_to_be(self.https_base_url + "admin/index"))

            self.driver.find_element(By.CSS_SELECTOR, "#headerNav > div:nth-child(2) > li:nth-child(3) > a:nth-child(1)").click()

            WebDriverWait(self.driver, 10).until(EC.url_to_be(self.https_base_url + "mobile/user/login?lang=de_DE"))

            assert True
        else:
            pass

    def test_admin_interface_tests(self):
        self.driver.get(self.https_base_url)
        # a
        self.authentication_assert_helper.assert_mobile_user_login()
        # b
        self.authentication_assert_helper.assert_mobile_user_password()

        self.driver.get(self.https_base_url)
        self.authentication_helper.login(self.secret['admin-username'], self.secret['admin-password'])

        # c
        self.authentication_assert_helper.assert_admin_index()
        #
        self.navigation_helper.navigate_to_manage_questionnaires()
        # d
        self.questionnaire_assert_helper.assert_questionnaire_list()
        self.questionnaire_helper.click_add_questionnaire_button()
        # e
        questionnaire = self.questionnaire_assert_helper.assert_questionnaire_fill_page()

        # f
        self.navigation_helper.search_and_open_questionnaire(questionnaire['name'])
        self.questionnaire_helper.save_questionnaire_edit_question()
        self.questionnaire_helper.click_add_question_button()
        self.question_assert_helper.assert_question_fill_page()
        # f (question types)
        # TODO [LJ] implement for all types
        question_list = list()
        excluded_question_types = {QuestionType.IMAGE}
        question_types = [question_type for question_type in QuestionType if
                          question_type not in excluded_question_types]
        for question_type in question_types:
            self.questionnaire_helper.click_add_question_button()
            question_by_type = self.question_assert_helper.assert_question_by_type(question_type)
            question_list.append(question_by_type)

        # g
        self.question_assert_helper.assert_question_table_functionality(len(question_list))

        # h
        self.navigation_helper.navigate_to_scores_of_questionnaire(questionnaire['id'], questionnaire['name'])
        self.score_assert_helper.assert_scores_list()

        # i
        self.navigation_helper.navigate_to_scores_of_questionnaire(questionnaire['id'], questionnaire['name'])
        self.score_assert_helper.assert_score_fill()

        # j
        source_questionnaire = self.questionnaire_helper.create_questionnaire_with_questions(
            question_types={QuestionType.SLIDER, QuestionType.MULTIPLE_CHOICE, QuestionType.DROP_DOWN})
        target_questionnaire = self.questionnaire_helper.create_questionnaire_with_questions(
            question_types={QuestionType.INFO_TEXT})
        bundle = self.bundle_helper.create_bundle(questionnaires=[source_questionnaire, target_questionnaire])

        source_questionnaire_slider_question = next((question for question in source_questionnaire['questions'] if question['type'] == QuestionType.SLIDER), None)

        self.navigation_helper.navigate_to_questions_of_questionnaire(source_questionnaire['id'], source_questionnaire['name'])
        self.question_helper.reorder_question(source_questionnaire_slider_question['id'], 0)

        # Adding conditions to the slider question
        self.condition_helper.open_conditions_of_question(source_questionnaire_slider_question['id'])

        self.condition_helper.add_condition_for_slider(threshold_steps=1, target_type=ConditionSelectors.TargetType.QUESTION)
        self.condition_helper.add_condition_for_slider(threshold_steps=2, target_type=ConditionSelectors.TargetType.ANSWER)
        self.condition_helper.add_condition_for_slider(threshold_steps=3, target_type=ConditionSelectors.TargetType.QUESTIONNAIRE)

        self.condition_assert_helper.assert_question_condition_list_de()

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
        print("\n======================== TEST SUMMARY ========================")
        print(f"Total Tests Run: {self.testsRun}")
        
        print(f"\nSuccessful Tests ({len(self.successful_tests)}/{self.testsRun}):")
        if self.successful_tests:
            for test in self.successful_tests:
                print(f" - {test}")
        else:
            print(" None")

        print(f"\nFailed Tests ({len(self.failures)}/{self.testsRun}):")
        if self.failures:
            for test, _ in self.failures:
                print(f" - {test}")
        else:
            print(" None")

        print(f"\nErrored Tests ({len(self.errors)}/{self.testsRun}):")
        if self.errors:
            for test, _ in self.errors:
                print(f" - {test}")
        else:
            print(" None")

        print("=============================================================\n")

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
