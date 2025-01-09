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
from selenium.webdriver.chrome.service import Service
from webdriver_manager.chrome import ChromeDriverManager

from helper.Authentication import AuthenticationHelper
from helper.Bundle import BundleHelper, BundleSelectors
from helper.Clinic import ClinicHelper, ClinicSelectors
from helper.Navigation import NavigationHelper
from helper.Question import QuestionHelper, QuestionErrorHelper, QuestionSelectors
from helper.Questionnaire import QuestionnaireHelper
from helper.Condition import ConditionHelper, ConditionSelectors
from helper.Score import ScoreHelper
from helper.SeleniumUtils import SeleniumUtils, ErrorSelectors
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
        # chrome_options.add_argument("--headless=new")
        chrome_options.add_argument("start-maximized");
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
        self.questionnaire_error_helper = QuestionErrorHelper(self.driver, navigation_helper=self.navigation_helper)

        self.bundle_helper = BundleHelper(self.driver, self.navigation_helper)
        self.clinic_helper = ClinicHelper(self.driver, self.navigation_helper)
        self.survey_helper = SurveyHelper(self.driver, self.navigation_helper)

        self.survey_assert_helper = SurveyAssertHelper(self.driver, self.authentication_helper)
        self.language_helper = LanguageHelper(self.driver)

    def test_bundle_list(self):
        bundle_name = "Test Bundle"
        clinic_name = "Test Clinic"

        # Arrange
        if not self.secret.get('admin-username') or not self.secret.get('admin-username'):
            self.skipTest("User AD credentials missing. Test skipped.")
        self.driver.get(self.https_base_url)
        self.authentication_helper.login(self.secret['admin-username'], self.secret['admin-password'])

        try:
            created_questionnaire = self.questionnaire_helper.create_questionnaire_with_questions(questionnaire_name="Test", questionnaire_description="Test",
                                            questionnaire_language_code="de_DE", questionnaire_display_name="Test",
                                            questionnaire_welcome_text="Test", questionnaire_final_text="Test", 
                                            question_types=[QuestionSelectors.QuestionTypes.INFO_TEXT])
        except Exception as e:
            self.fail(f"Failed to create questionnaire: {e}")

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
                f"Bundle table not found")

        try:
            self.bundle_helper.create_bundle(bundle_name, True, [created_questionnaire])
            self.bundle_helper.save_bundle(bundle_name)
        except Exception as e:
            self.fail(f"Failed to create bundle: {e}")
    
        #Assert - Verify that flag icons are visible
        try:
            WebDriverWait(self.driver, 10).until(
                EC.visibility_of_element_located(BundleSelectors.CELL_FLAGICON)
            )
        except Exception as e:
            self.fail(f"Failed to verify flag icon: {e}")

        self.utils.search_item(bundle_name, "bundle")
        bundle_id = self.bundle_helper.get_bundle_id()
    
        try:
            self.navigation_helper.navigate_to_manage_clinics()
            self.clinic_helper.create_clinic(clinic_name=clinic_name, 
                                             clinic_description="Test Clinic Description",
                                             configurations=[{'selector': (By.CSS_SELECTOR, '#usePseudonymizationService > div:nth-child(1) > div:nth-child(3) > label:nth-child(1)')}],
                                             bundles=[{'id': bundle_id, 'name': bundle_name}])
            clinic_id=self.clinic_helper.save_clinic(clinic_name)

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
            self.fail("Search box not found")

        #Assert: Verify that pagination is available
        try:
            WebDriverWait(self.driver, 10).until(
                EC.visibility_of_element_located(BundleSelectors.PAGINATION_BUNDLE)
            )
        except TimeoutException:
            self.fail("Pagination not found ")

        #Assert: Verify that add new bundle button is available
        try:
            WebDriverWait(self.driver, 10).until(
                EC.visibility_of_element_located(BundleSelectors.BUTTON_ADD_BUNDLE)
            )
        except TimeoutException:
            self.fail("Add button not found ")

        #Finally
        finally:
            self.utils.search_and_delete_item(clinic_name,clinic_id, "clinic")
            self.utils.search_and_delete_item(bundle_name,bundle_id, "bundle")
            self.utils.search_and_delete_item(created_questionnaire['name'], created_questionnaire['id'], "questionnaire")

    def test_bundle_fill(self):
        bundle_name = "Test Bundle"
        # Arrange
        if not self.secret.get('admin-username') or not self.secret.get('admin-username'):
            self.skipTest("User AD credentials missing. Test skipped.")
        self.driver.get(self.https_base_url)
        self.authentication_helper.login(self.secret['admin-username'], self.secret['admin-password'])

        try:
            created_questionnaire = self.questionnaire_helper.create_questionnaire_with_questions(questionnaire_name="Test", questionnaire_description="Test",
                                            questionnaire_language_code="de_DE", questionnaire_display_name="Test",
                                            questionnaire_welcome_text="Test", questionnaire_final_text="Test", 
                                            question_types=[QuestionSelectors.QuestionTypes.INFO_TEXT])
        except Exception as e:
            self.fail(f"Failed to create questionnaire: {e}")

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

        #Assert - Visibility of bundle welcome input
        try:
            WebDriverWait(self.driver, 10).until(
                EC.visibility_of_element_located(BundleSelectors.INPUT_END_TEXT)
            )
        except Exception as e:  
            self.fail(f"Failed to locate end input: {e}")
        
        # Assert - Visibility of publish checkbox
        try:
            WebDriverWait(self.driver, 10).until(
            EC.visibility_of_element_located(BundleSelectors.CHECKBOX_PUBLISH)
            )
        except TimeoutException:
            self.fail("Publish checkbox not found ")

        # Assert - Visibility of name progress checkbox
        try:
            WebDriverWait(self.driver, 10).until(
            EC.visibility_of_element_located(BundleSelectors.CHECKBOX_NAME_PROGRESS)
            )
        except TimeoutException:
            self.fail("Name progress checkbox not found ")

        # Assert - Visibility of progress whole package checkbox
        try:
            WebDriverWait(self.driver, 10).until(
            EC.visibility_of_element_located(BundleSelectors.CHECKBOX_PROGRESS_WHOLE_PACKAGE)
            )
        except TimeoutException:
            self.fail("Progress whole package checkbox not found ")

        # Assert - Visibility of available questionnaires table
        try:
            WebDriverWait(self.driver, 10).until(
            EC.visibility_of_element_located(BundleSelectors.TABLE_AVAILABLE_QUESTIONNAIRES)
            )
        except TimeoutException:
            self.fail("Available questionnaires table not found")

        # Assert - Visibility of assigned questionnaires table
        try:
            WebDriverWait(self.driver, 10).until(
            EC.visibility_of_element_located(BundleSelectors.TABLE_ASSIGNED_QUESTIONNAIRES)
            )
        except TimeoutException:
            self.fail("Assigned questionnaires table not found")

        #Assert - Test for assigning questionnaire to bundle
        try:
            self.bundle_helper.assign_multiple_questionnaires_to_bundle([created_questionnaire])
        except Exception as e:
            self.fail(f"Failed to assign questionnaire to bundle: {e}")

        #Assert - Test for removing questionnaire to bundle
        try:
            self.bundle_helper.remove_multiple_questionnaires_from_bundle([created_questionnaire])
        except Exception as e:
            self.fail(f"Failed to assign questionnaire to bundle: {e}")

        #Assert - Check form validation
        try:
            self.utils.click_element(BundleSelectors.BUTTON_SAVE)
            WebDriverWait(self.driver, 10).until(
            EC.visibility_of_element_located(ErrorSelectors.INPUT_VALIDATION_SELECTOR)
            )
            validation_errors = self.driver.find_elements(*ErrorSelectors.INPUT_VALIDATION_SELECTOR)
            self.assertEqual(len(validation_errors), 2, "Expected 2 validation errors, but found {len(validation_errors)}")
        except Exception as e:
            self.fail(f"Failed to save bundle: {e}")
        
        #Assert - Create a bundle with a questionnaire
        try:
            self.navigation_helper.navigate_to_manage_bundles()
            self.bundle_helper.create_bundle(bundle_name, True, [created_questionnaire])
            self.bundle_helper.save_bundle(bundle_name)
        except Exception as e:
            self.fail(f"Failed to create bundle: {e}")
        
        #Finally
        finally:
            self.utils.search_item(bundle_name, "bundle")
            bundle_id = self.bundle_helper.get_bundle_id()
            if(bundle_id):
                self.utils.search_and_delete_item(bundle_name,bundle_id, "bundle")
            if(created_questionnaire):
                self.utils.search_and_delete_item(created_questionnaire['name'], created_questionnaire['id'], "questionnaire")
        
    def tearDown(self):
        if self.driver:
            self.driver.quit()


if __name__ == "__main__":
    unittest.main()