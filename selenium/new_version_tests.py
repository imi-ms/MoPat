#!/usr/bin/env python3
# -*- coding: utf-8 -*-
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
from helper.Dashboard import DashboardHelper, DashboardSelectors
from helper.SeleniumUtils import SeleniumUtils, ErrorSelectors
from helper.Survey import SurveyHelper, SurveyAssertHelper
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
        self.dashboard_helper = DashboardHelper(self.driver, navigation_helper=self.navigation_helper)
        self.questionnaire_helper = QuestionnaireHelper(self.driver, navigation_helper=self.navigation_helper)
        self.question_helper = QuestionHelper(self.driver, navigation_helper=self.navigation_helper)
        self.condition_helper = ConditionHelper(self.driver, navigation_helper=self.navigation_helper)
        self.score_helper = ScoreHelper(self.driver, navigation_helper=self.navigation_helper)

        self.bundle_helper = BundleHelper(self.driver, self.navigation_helper)
        self.clinic_helper = ClinicHelper(self.driver, self.navigation_helper)
        self.survey_helper = SurveyHelper(self.driver, self.navigation_helper)

        self.survey_assert_helper = SurveyAssertHelper(self.driver, self.navigation_helper)
        self.language_helper = LanguageHelper(self.driver, self.navigation_helper)

    def test_git_info(self):
        # Arrange
        self.driver.get(self.https_base_url)
        self.authentication_helper.login(self.secret['admin-username'], self.secret['admin-password'])
        
        self.dashboard_helper.open_git_info()
        
        self.utils.check_visibility_of_element(DashboardSelectors.TABLE_GITINFO,"Git Information Table not found")
        
        # Assert git information elements are present
        self.utils.check_visibility_of_element(DashboardSelectors.BLOCK_GIT_BUILD_VERSION, "Git Build Version not found")
        self.utils.check_visibility_of_element(DashboardSelectors.BLOCK_GIT_BRANCH, "Git Branch not found")
        self.utils.check_visibility_of_element(DashboardSelectors.BLOCK_GIT_COMMIT_ID, "Git Commit ID not found")
        self.utils.check_visibility_of_element(DashboardSelectors.BLOCK_GIT_COMMIT_MESSAGE, "Git Commit Message not found")
        
        self.authentication_helper.logout()
        
    def tearDown(self):
        if self.driver:
            self.driver.quit()


if __name__ == "__main__":
    unittest.main()