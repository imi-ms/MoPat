#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import random
import string
import time
import unittest
import datetime

from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.chrome.options import Options

from helper.Authentication import AuthenticationHelper
from helper.Bundle import BundleHelper, BundleSelectors
from helper.Clinic import ClinicHelper, ClinicSelectors
from helper.Navigation import NavigationHelper
from helper.Question import QuestionHelper, QuestionErrorHelper, QuestionSelectors
from helper.Questionnaire import QuestionnaireHelper, ConditionHelper, ScoreHelper
from helper.SeleniumUtils import SeleniumUtils
from helper.Survey import SurveyHelper, SurveySelectors, SurveyAssertHelper
from helper.Language import LanguageHelper, LanguageSelectors
from utils.imiseleniumtest import IMISeleniumBaseTest, IMISeleniumChromeTest
from selenium.common.exceptions import TimeoutException


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
        # chrome_options.add_argument("--disable-extensions")
        # chrome_options.add_argument("--disable-gpu")
        # chrome_options.add_argument("--no-sandbox") # linux only
        # chrome_options.add_argument("--headless=new")
        chrome_options.add_argument("start-maximized");
        driver = webdriver.Chrome(options=chrome_options)
        # Initialize the WebDriver
        self.driver = webdriver.Chrome(options=chrome_options)

        # Initialize Navigation and Utils
        self.navigation_helper = NavigationHelper(self.driver)
        self.utils = SeleniumUtils(self.driver, navigator=self.navigation_helper)
        self.navigation_helper.utils = self.utils

        # Initialize other helpers
        self.authentication_helper = AuthenticationHelper(self.driver)
        self.questionnaire_helper = QuestionnaireHelper(self.driver)
        self.question_helper = QuestionHelper(self.driver)
        self.condition_helper = ConditionHelper(self.driver)
        self.score_helper = ScoreHelper(self.driver)
        self.questionnaire_error_helper = QuestionErrorHelper(self.driver)

        self.bundle_helper = BundleHelper(self.driver, self.questionnaire_helper, self.navigation_helper)
        self.clinic_helper = ClinicHelper(self.driver, self.navigation_helper)
        self.survey_helper = SurveyHelper(self.driver, self.authentication_helper)

        self.survey_assert_helper = SurveyAssertHelper(self.driver, self.authentication_helper)
        self.language_helper = LanguageHelper(self.driver)

    def _test_bundle_list(self):
        bundle_name = "Test Bundle"
        clinic_name = "Test Clinic"
        # Arrange
        if not self.secret.get('admin-username') or not self.secret.get('admin-username'):
            self.skipTest("User AD credentials missing. Test skipped.")
        self.driver.get(self.https_base_url)
        self.authentication_helper.login(self.secret['admin-username'], self.secret['admin-password'])

        try:
            self.navigation_helper.navigate_to_manage_bundles()
        except Exception as e:
            self.fail(f"Failed to navigate to 'Bundles' page: {e}")

        # Assert - Verify that the bundle table is available
        try:
            WebDriverWait(self.driver, 10).until(
                EC.visibility_of_element_located(BundleSelectors.TABLE_BUNDLE)
            )
        except TimeoutException:
            self.fail(
                f"Bundle table not found within 10 seconds. ")
        
        try:
            self.bundle_helper.create_bundle(bundle_name, True)
            self.bundle_helper.save_bundle(bundle_name)
        except Exception as e:
            self.fail(f"Failed to create bundle: {e}")
    
        #TODO: FLAGICONS DETECTION (after create questionnaire function)
        #TODO: ASSIGN QUESTIONNAIRE TO BUNDLE (after create questionnaire function)

        self.utils.search_item(bundle_name, "bundle")
        bundle_id = self.bundle_helper.get_bundle_id()
    
        try:
            self.navigation_helper.navigate_to_manage_clinics()
            self.clinic_helper.create_clinic(clinic_name=clinic_name, clinic_description="Test Clinic Description",bundles=[{'id': bundle_id, 'name': bundle_name}])
            self.clinic_helper.save_clinic(clinic_name)

        except Exception as e:
            self.fail(f"Failed to create clinic: {e}")

        # Assert - Find clinics assigned to the bundle
        try:
            self.navigation_helper.navigate_to_manage_bundles()
            self.utils.search_item(bundle_name, "bundle")
            bundle_row = self.bundle_helper.get_first_bundle_row()
            bundle_row.find_element(By.CSS_SELECTOR, "td:nth-child(3)")
            clinic_link = bundle_row.find_element(By.CSS_SELECTOR, "ul > li > a")
            self.assertEqual(clinic_link.text, clinic_name, f"Clinic name '{clinic_name}' not found in bundle row.")

        except Exception as e:
            self.fail(f"Failed to find clinic assigned to bundle: {e}")


        #Assert: Verify that search box is available
        try:
            WebDriverWait(self.driver, 10).until(
                EC.visibility_of_element_located(BundleSelectors.INPUT_BUNDLE_SEARCH)
            )
        except TimeoutException:
            self.fail("Search box not found within 10 seconds.")

        #Assert: Verify that pagination is available
        try:
            WebDriverWait(self.driver, 10).until(
                EC.visibility_of_element_located(BundleSelectors.PAGINATION_BUNDLE)
            )
        except TimeoutException:
            self.fail("Pagination not found within 10 seconds.")

        #Assert: Verify that add new bundle button is available
        try:
            WebDriverWait(self.driver, 10).until(
                EC.visibility_of_element_located(BundleSelectors.BUTTON_ADD_BUNDLE)
            )
        except TimeoutException:
            self.fail("Add button not found within 10 seconds.")

        #Finally
        self.utils.search_and_delete_item(bundle_name,bundle_id, "bundle")


    def test_bundle_fill(self):
        # Arrange
        if not self.secret.get('admin-username') or not self.secret.get('admin-username'):
            self.skipTest("User AD credentials missing. Test skipped.")
        self.driver.get(self.https_base_url)
        self.authentication_helper.login(self.secret['admin-username'], self.secret['admin-password'])

        try:
            self.navigation_helper.navigate_to_manage_bundles()
        except Exception as e:
            self.fail(f"Failed to navigate to 'Bundles' page: {e}")

        self.utils.click_element(BundleSelectors.BUTTON_ADD_BUNDLE)
        
        #Assert - Visibility of language dropdown
        try:
            self.language_helper.open_language_dropdown()
            WebDriverWait(self.driver, 10).until(
                EC.visibility_of_element_located(LanguageSelectors.LANGUAGE_DROPDOWN)
            )
        except Exception as e:  
            self.fail(f"Failed to open language dropdown: {e}")

        #Assert - Visibility of bundle name input
        try:
            WebDriverWait(self.driver, 10).until(
                EC.visibility_of_element_located(BundleSelectors.INPUT_NAME)
            )
        except Exception as e:  
            self.fail(f"Failed to locate bundle input: {e}")

        #Assert - Visibility of bundle description input
        try:
            WebDriverWait(self.driver, 10).until(
                EC.visibility_of_element_located(BundleSelectors.INPUT_EDITABLE_DESCRIPTION)
            )
        except Exception as e:  
            self.fail(f"Failed to locate bundle description: {e}")

        #Assert - Visibility of bundle welcome input
        try:
            WebDriverWait(self.driver, 10).until(
                EC.visibility_of_element_located(BundleSelectors.INPUT_WELCOME_TEXT)
            )
        except Exception as e:  
            self.fail(f"Failed to locate welcome input: {e}")


    def tearDown(self):
        if self.driver:
            self.driver.quit()


if __name__ == "__main__":
    unittest.main()