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

    def _test_clinic_list(self):
        clinic_name = "Test Clinic"
        clinic_description = "Test Clinic Description"
        clinic_id = None

        # Arrange
        if not self.secret.get('admin-username') or not self.secret.get('admin-username'):
            self.skipTest("User AD credentials missing. Test skipped.")
        self.driver.get(self.https_base_url)
        self.authentication_helper.login(self.secret['admin-username'], self.secret['admin-password'])

        try:
            self.navigation_helper.navigate_to_manage_clinics()
        except Exception as e:
            self.fail(f"Failed to navigate to 'Clinic' page: {e}")

        # Act
        try:
            self.clinic_helper.create_clinic(clinic_name, clinic_description,configurations=[{'selector': (By.CSS_SELECTOR, '#usePseudonymizationService > div:nth-child(1) > div:nth-child(3) > label:nth-child(1)')}])
            clinic_id = self.clinic_helper.save_clinic(clinic_name)
        except Exception as e:
            self.fail(f"Failed to create clinic: {e}")

        #Assert - Check if the clinic table is displayed
        try:
            WebDriverWait(self.driver, 10).until(
                EC.visibility_of_element_located(ClinicSelectors.TABLE_CLINIC)
            )
        except TimeoutException:
            self.fail(
                f"Clinic table not found")
            
        #Assert - Check if the clinic table pagination is displayed
        try:
            WebDriverWait(self.driver, 10).until(
                EC.visibility_of_element_located(ClinicSelectors.PAGINATION_CLINIC_TABLE)
            )
        except TimeoutException:
            self.fail(
                f"Clinic table pagination not found")
            
        #Assert - Check if the clinic table search is displayed
        try:
            WebDriverWait(self.driver, 10).until(
                EC.visibility_of_element_located(ClinicSelectors.TABLE_SEARCH)
            )
        except TimeoutException:
            self.fail(
                f"Clinic table pagination not found")
        
        #Assert - Check if the clinic table action buttons are displayed
        try:
            WebDriverWait(self.driver, 10).until(
                EC.visibility_of_element_located(ClinicSelectors.TABLE_ACTION_BUTTONS)
            )
        except TimeoutException:  
            self.fail(
                f"Clinic table action buttons not found")
        
        #Assert - Check if the add new clinic button is displayed
        try:
            WebDriverWait(self.driver, 10).until(
                EC.visibility_of_element_located(ClinicSelectors.BUTTON_ADD_CLINIC)
            )
        except TimeoutException:
            self.fail(
                f"Add new clinic button not found")
            
        finally:
            self.utils.search_and_delete_item(clinic_name,clinic_id,"clinic")
            self.authentication_helper.logout()
            
    def test_clinic_fill(self):
        clinic_name = "Test Clinic"
        clinic_description = "Test Clinic Description"
        clinic_id = None
        bundle_name = "Test Bundle"
        bundle_id=None


        # Arrange
        if not self.secret.get('admin-username') or not self.secret.get('admin-username'):
            self.skipTest("User AD credentials missing. Test skipped.")
        self.driver.get(self.https_base_url)
        self.authentication_helper.login(self.secret['admin-username'], self.secret['admin-password'])

        #Arrange
        try:
            created_questionnaire = self.questionnaire_helper.create_questionnaire_with_questions(questionnaire_name="Test", questionnaire_description="Test",
                                            questionnaire_language_code="de_DE", questionnaire_display_name="Test",
                                            questionnaire_welcome_text="Test", questionnaire_final_text="Test", 
                                            question_types=[QuestionSelectors.QuestionTypes.INFO_TEXT])
            self.navigation_helper.navigate_to_manage_bundles()
            self.bundle_helper.create_bundle(bundle_name, True, [created_questionnaire])
            bundle_id=self.bundle_helper.save_bundle(bundle_name)
        except Exception as e:
            self.fail(f"Failed to setup questionnaire and bundle: {e}")

        self.navigation_helper.navigate_to_manage_clinics()
        WebDriverWait(self.driver, 10).until(EC.visibility_of_element_located(ClinicSelectors.BUTTON_ADD_CLINIC))
        self.utils.click_element(ClinicSelectors.BUTTON_ADD_CLINIC)
        #Assert - Check if the clinic name input is displayed
        try:
            WebDriverWait(self.driver, 10).until(
                EC.visibility_of_element_located(ClinicSelectors.INPUT_CLINIC_NAME)
            )
        except TimeoutException:
            self.fail(
                f"Clinic name input not found")

        #Assert - Check if the clinic description input is displayed
        try:
            WebDriverWait(self.driver, 10).until(
                EC.visibility_of_element_located(ClinicSelectors.INPUT_EDITABLE_DESCRIPTION)
            )
        except TimeoutException:
            self.fail(
                f"Clinic description input not found")

        #Assert - Check if the clinic email input is displayed
        try:
            WebDriverWait(self.driver, 10).until(
                EC.visibility_of_element_located(ClinicSelectors.INPUT_CLINIC_EMAIL)
            )
        except TimeoutException:
            self.fail(
                f"Clinic email input not found")
        
        #Assert - Check if the clinic configuration is displayed
        try:
            WebDriverWait(self.driver, 10).until(
                EC.visibility_of_element_located(ClinicSelectors.DIV_CLINIC_CONFIGURATION)
            )
            clinic_configuration = self.driver.find_element(*ClinicSelectors.DIV_CLINIC_CONFIGURATION)
            clinic_configuration_list = clinic_configuration.find_elements(*ClinicSelectors.CLINIC_CONFIGURATION_LIST)
            self.assertGreaterEqual(len(clinic_configuration_list), 1, "Clinic configuration list should have at least one item")
        except:
            self.fail(
                f"Clinic configuration not found")
            
        #Assert - Check if the tables with available bundles is displayed
        try:
            WebDriverWait(self.driver, 10).until(
                EC.visibility_of_element_located(ClinicSelectors.TABLE_AVAIALBLE_BUNDLES)
            )
        except TimeoutException:
            self.fail(
                f"Table with available bundles not found")
            
        #Assert - Check if the tables with selected bundles is displayed
        try:
            WebDriverWait(self.driver, 10).until(
                EC.visibility_of_element_located(ClinicSelectors.TABLE_ASSIGNED_BUNDLES)
            )
        except TimeoutException:
            self.fail(
                f"Table with assigned bundles not found")
            
        #Assert - Check if the bundles can be added to the clinic
        try:
            self.clinic_helper.assign_multiple_bundes_to_clinic([{'id': bundle_id, 'name': bundle_name}])
        except Exception as e:
            self.fail(f"Failed to assign bundle to clinic: {e}")
        
        #Assert - Check if the bundles can be removed from the clinic
        try:
            self.clinic_helper.remove_multiple_bundes_from_clinic([{'id': bundle_id, 'name': bundle_name}])
        except Exception as e:
            self.fail(f"Failed to remove bundle from clinic: {e}")

         #Assert - Check if the tables with available users is displayed
        try:
            WebDriverWait(self.driver, 10).until(
                EC.visibility_of_element_located(ClinicSelectors.TABLE_AVAIALBLE_USERS)
            )
        except TimeoutException:
            self.fail(
                f"Table with available users not found")
            
        #Assert - Check if the tables with selected bundles is displayed
        try:
            WebDriverWait(self.driver, 10).until(
                EC.visibility_of_element_located(ClinicSelectors.TABLE_ASSIGNED_USERS)
            )
        except TimeoutException:
            self.fail(
                f"Table with assigned users not found")
            

        #Assert - Check if the users can be added to the clinic
        try:
            self.clinic_helper.assign_multiple_users_to_clinic([self.secret.get('admin-username')])
        except Exception as e:
            self.fail(f"Failed to assign users to clinic: {e}")

        #Assert - Check if the users can be removed from the clinic
        try:
            self.clinic_helper.remove_multiple_users_from_clinic([self.secret.get('admin-username')])
        except Exception as e:
            self.fail(f"Failed to remove users from clinic: {e}")

        #Assert - Check form validation
        try:
            self.utils.click_element(ClinicSelectors.BUTTON_SAVE)
            WebDriverWait(self.driver, 10).until(
            EC.visibility_of_element_located(ErrorSelectors.INPUT_VALIDATION_SELECTOR)
            )
            validation_errors = self.driver.find_elements(*ErrorSelectors.INPUT_VALIDATION_SELECTOR)
            configuration_errors = self.driver.find_elements(*ErrorSelectors.CONFIGURATION_ERROR_SELECTOR)
            self.assertEqual(len(validation_errors), 2, "Expected 2 validation errors, but found {len(validation_errors)}")
            self.assertEqual(len(configuration_errors), 1, "Expected 1 configuration errors, but found {len(configuration_errors)}")
        except Exception as e:
            self.fail(f"Failed to save bundle: {e}")

        #Assert - Check if the clinic can be created
        try:
            self.navigation_helper.navigate_to_manage_clinics()
            self.clinic_helper.create_clinic(clinic_name, clinic_description,
                                             configurations=[{'selector': (By.CSS_SELECTOR, '#usePseudonymizationService > div:nth-child(1) > div:nth-child(3) > label:nth-child(1)')}],
                                             bundles=[{'id': bundle_id, 'name': bundle_name}], users=[self.secret.get('admin-username')])
            clinic_id = self.clinic_helper.save_clinic(clinic_name)
            WebDriverWait(self.driver, 10).until(
                EC.visibility_of_element_located(ClinicSelectors.TABLE_CLINIC)
            )
        except Exception as e:
            self.fail(f"Failed to create clinic: {e}")

        finally:
            if clinic_id:
                self.utils.search_and_delete_item(clinic_name,clinic_id,"clinic")
            if bundle_id:
                self.utils.search_and_delete_item(bundle_name,bundle_id,"bundle")
            if created_questionnaire:
                self.utils.search_and_delete_item(created_questionnaire['name'], created_questionnaire['id'], "questionnaire")
            self.authentication_helper.logout()

    def tearDown(self):
        if self.driver:
            self.driver.quit()


if __name__ == "__main__":
    unittest.main()