#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import time
import unittest

from helper.User import UserSelector
from selenium import webdriver
from selenium.common.exceptions import TimeoutException
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait
from webdriver_manager.chrome import ChromeDriverManager

from helper.Authentication import AuthenticationHelper
from helper.Bundle import BundleHelper
from helper.Clinic import ClinicHelper
from helper.Condition import ConditionHelper
from helper.Language import LanguageHelper
from helper.Navigation import NavigationHelper
from helper.Question import QuestionHelper
from helper.Questionnaire import QuestionnaireHelper
from helper.Score import ScoreHelper
from helper.SeleniumUtils import SeleniumUtils, ErrorSelectors
from helper.Survey import SurveyHelper, SurveyAssertHelper
from helper.Statistic import StatisticSelector
from utils.imiseleniumtest import IMISeleniumBaseTest, IMISeleniumChromeTest


class URLPaths:
    ADMIN_INDEX = "/admin/index"
    ADMIN_INDEX_DE = "/admin/index?lang=de_DE"
    MOBILE_USER_LOGIN = "/mobile/user/login"
    MOBILE_USER_LOGIN_DE = "/mobile/user/login?lang=de_DE"
    MOBILE_SURVEY_INDEX = "/mobile/survey/index"
    LOGIN_BAD_CREDENTIALS = "/mobile/user/login?message=BadCredentialsException"
    LOGIN_DISABLED_EXCEPTION = "/mobile/user/login?message=DisabledException"
    PASSWORD_FORGOT = "/mobile/user/password"


class EmailSelectors:
    SUBJECT_INPUT = (By.ID, "subject")
    CONTENT_INPUT = (By.ID, "mailContent")
    MAIL_PREVIEW_BUTTON = (By.ID, "mailPreviewButton")
    SEND_BUTTON = (By.ID, "mailButton")


class PasswordResetSelectors:
    FORGOT_PASSWORD_LINK = (By.ID, "forgotPasswordLink")
    USERNAME_INPUT = (By.ID, "username")
    ERROR_MESSAGE = (By.CLASS_NAME, "error")


class CustomTest(IMISeleniumChromeTest, unittest.TestCase):
    seleniumMode: IMISeleniumBaseTest.SeleniumMode = IMISeleniumBaseTest.SeleniumMode.LOCAL

    @classmethod
    def setUpClass(cls):
        super().setUpClass()
        cls.base_url = "localhost:8080"
        cls.https_base_url = "http://localhost:8080"

    def setUp(self):
        chrome_options = Options()
        # chrome_options.add_argument("--headless=new")
        chrome_options.add_argument("start-maximized")
        driver = webdriver.Chrome(options=chrome_options, service=Service(ChromeDriverManager().install()))
        # Initialize the WebDriver
        self.driver = webdriver.Chrome(options=chrome_options)

        # Initialize Navigation and Utils
        self.navigation_helper = NavigationHelper(self.driver)
        self.utils = SeleniumUtils(self.driver, navigation_helper=self.navigation_helper)
        self.navigation_helper.utils = self.utils

        # Initialize other helpers
        self.authentication_helper = AuthenticationHelper(self.driver)
        self.questionnaire_helper = QuestionnaireHelper(self.driver, navigation_helper=self.navigation_helper)
        self.question_helper = QuestionHelper(self.driver, navigation_helper=self.navigation_helper)
        self.condition_helper = ConditionHelper(self.driver, navigation_helper=self.navigation_helper)
        self.score_helper = ScoreHelper(self.driver, navigation_helper=self.navigation_helper)

        self.bundle_helper = BundleHelper(self.driver, self.navigation_helper)
        self.clinic_helper = ClinicHelper(self.driver, self.navigation_helper)
        self.survey_helper = SurveyHelper(self.driver, self.navigation_helper)

        self.survey_assert_helper = SurveyAssertHelper(self.driver, self.navigation_helper)
        self.language_helper = LanguageHelper(self.driver, self.navigation_helper)

    def test_one_time_statistic(self):
        # Arrange
        self.driver.get(self.https_base_url)
        self.authentication_helper.login(self.secret['admin-username'], self.secret['admin-password'])

        # Act
        self.navigation_helper.navigate_to_one_time_statistic()
        
        self.utils.check_visibility_of_element(StatisticSelector.BUNDLE_DROP_DOWN, "Bundle dropdown not found")
        self.utils.check_visibility_of_element(StatisticSelector.BUNDLE_START_DATE, "Bundle start date not found")
        self.utils.check_visibility_of_element(StatisticSelector.BUNDLE_END_DATE, "Bundle end date not found")
        self.utils.check_visibility_of_element(StatisticSelector.PATIENT_ID, "Patient ID not found")
        self.utils.check_visibility_of_element(StatisticSelector.PATIENT_START_DATE, "Patient start date not found")
        self.utils.check_visibility_of_element(StatisticSelector.PATIENT_END_DATE, "Patient end date not found")
        self.utils.check_visibility_of_element(StatisticSelector.BUNDLE_PATIENT_PATIENT_ID, "Bundle patient ID not found")
        self.utils.check_visibility_of_element(StatisticSelector.BUNDLE_PATIENT_BUNDLE_ID, "Bundle patient bundle ID not found")
        self.utils.check_visibility_of_element(StatisticSelector.BUNDLE_PATIENT_START_DATE, "Bundle patient start date not found")
        self.utils.check_visibility_of_element(StatisticSelector.BUNDLE_PATIENT_END_DATE, "Bundle patient end date not found")
        
        self.utils.click_element(StatisticSelector.BUTTON_BERECHNEN)
        
        self.utils.check_visibility_of_element(StatisticSelector.ANZAHL_1, "Anzahl 1 not found")
        self.utils.check_visibility_of_element(StatisticSelector.ANZAHL_2, "Anzahl 2 not found")
        self.utils.check_visibility_of_element(StatisticSelector.ANZAHL_3, "Anzahl 3 not found")

        self.authentication_helper.logout()

    def tearDown(self):
        if self.driver:
            self.driver.quit()


if __name__ == "__main__":
    unittest.main()