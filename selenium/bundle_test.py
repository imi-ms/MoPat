#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import unittest

from selenium import webdriver
from selenium.common.exceptions import TimeoutException
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait
from webdriver_manager.chrome import ChromeDriverManager

from helper.Authentication import AuthenticationHelper
from helper.Bundle import BundleHelper, BundleSelectors
from helper.Clinic import ClinicHelper
from helper.Condition import ConditionHelper
from helper.Language import LanguageHelper, LanguageSelectors
from helper.Navigation import NavigationHelper
from helper.Question import QuestionHelper, QuestionType
from helper.Questionnaire import QuestionnaireHelper
from helper.Score import ScoreHelper
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
        self.questionnaire_helper = QuestionnaireHelper(self.driver, navigation_helper=self.navigation_helper)
        self.question_helper = QuestionHelper(self.driver, navigation_helper=self.navigation_helper)
        self.condition_helper = ConditionHelper(self.driver, navigation_helper=self.navigation_helper)
        self.score_helper = ScoreHelper(self.driver, navigation_helper=self.navigation_helper)

        self.bundle_helper = BundleHelper(self.driver, self.navigation_helper)
        self.clinic_helper = ClinicHelper(self.driver, self.navigation_helper)
        self.survey_helper = SurveyHelper(self.driver, self.navigation_helper)

        self.survey_assert_helper = SurveyAssertHelper(self.driver, self.navigation_helper)
        self.language_helper = LanguageHelper(self.driver, self.navigation_helper)

    def test_bundle_list(self):
        bundle={}
        created_questionnaire={}
        clinic={}

        # Arrange
        if not self.secret.get('admin-username') or not self.secret.get('admin-username'):
            self.skipTest("User AD credentials missing. Test skipped.")
        self.driver.get(self.https_base_url)
        self.authentication_helper.login(self.secret['admin-username'], self.secret['admin-password'])

        try:
            created_questionnaire = self.questionnaire_helper.create_questionnaire_with_questions()
        except Exception as e:
            self.fail(f"Failed to create questionnaire: {e}")

        try:
            self.navigation_helper.navigate_to_manage_bundles()
        except Exception as e:
            self.fail(f"Failed to navigate to 'Bundles' page: {e}")

        self.utils.check_visibility_of_element(BundleSelectors.TABLE_BUNDLE, "Bundle table not found")

        try:
            bundle = self.bundle_helper.create_bundle(publish_bundle=True, questionnaires=[created_questionnaire])
            bundle['id']=self.bundle_helper.save_bundle(bundle_name=bundle['name'])
        except Exception as e:
            self.fail(f"Failed to create bundle: {e}")
    
        self.utils.check_visibility_of_element(BundleSelectors.CELL_FLAGICON, "Flag icon not found")
    
        try:
            self.navigation_helper.navigate_to_manage_clinics()
            clinic["name"] = self.clinic_helper.create_clinic(bundles=[bundle],
                                                              configurations=[{'selector': (By.CSS_SELECTOR, '#usePseudonymizationService > div:nth-child(1) > div:nth-child(3) > label:nth-child(1)')}])
            clinic['id']=self.clinic_helper.save_clinic(clinic_name=clinic['name'])

        except Exception as e:
            self.fail(f"Failed to create clinic: {e}")

        # Assert - Find clinics assigned to the bundle
        try:
            self.navigation_helper.navigate_to_manage_bundles()
            self.utils.search_item(bundle["name"], "bundle")
            bundle_row = self.bundle_helper.get_first_bundle_row()
            bundle_row.find_element(By.CSS_SELECTOR, "td:nth-child(3)")
            clinic_link = bundle_row.find_element(By.CSS_SELECTOR, "ul > li > a")
            self.assertEqual(clinic_link.text, clinic["name"], f"Clinic name '{clinic["name"]}' not found in bundle row.")

        except Exception as e:
            self.fail(f"Failed to find clinic assigned to bundle: {e}")

        self.utils.check_visibility_of_element(BundleSelectors.INPUT_BUNDLE_SEARCH, "Bundle table search box not found")

        self.utils.check_visibility_of_element(BundleSelectors.PAGINATION_BUNDLE, "Bundle table pagination not found")

        self.utils.check_visibility_of_element(BundleSelectors.BUTTON_ADD_BUNDLE, "Bundle add button not found")

        try:
            pass
        finally:
            self.utils.search_and_delete_item(clinic["name"],clinic["id"], "clinic")
            self.utils.search_and_delete_item(bundle["name"],bundle["id"], "bundle")
            self.utils.search_and_delete_item(created_questionnaire['name'], created_questionnaire['id'], "questionnaire")

    def test_bundle_fill(self):
        created_questionnaire = {}
        # Arrange
        if not self.secret.get('admin-username') or not self.secret.get('admin-username'):
            self.skipTest("User AD credentials missing. Test skipped.")
        self.driver.get(self.https_base_url)
        self.authentication_helper.login(self.secret['admin-username'], self.secret['admin-password'])

        try:
            created_questionnaire = self.questionnaire_helper.create_questionnaire_with_questions()
        except Exception as e:
            self.fail(f"Failed to create questionnaire: {e}")

        try:
            self.navigation_helper.navigate_to_manage_bundles()
        except Exception as e:
            self.fail(f"Failed to navigate to 'Bundles' page: {e}")

        self.utils.click_element(BundleSelectors.BUTTON_ADD_BUNDLE)
        self.language_helper.open_language_dropdown()
        self.utils.check_visibility_of_element(LanguageSelectors.LANGUAGE_DROPDOWN, "Failed to open language dropdown")
        self.utils.check_visibility_of_element(BundleSelectors.INPUT_NAME, "Failed to locate bundle input")
        self.utils.check_visibility_of_element(BundleSelectors.INPUT_EDITABLE_DESCRIPTION, "Failed to locate bundle description input")
        self.utils.check_visibility_of_element(BundleSelectors.INPUT_WELCOME_TEXT, "Failed to locate welcome input")
        self.utils.check_visibility_of_element(BundleSelectors.INPUT_END_TEXT, "Failed to locate end input")
        self.utils.check_visibility_of_element(BundleSelectors.CHECKBOX_PUBLISH, "Failed to locate publish checkbox")
        self.utils.check_visibility_of_element(BundleSelectors.CHECKBOX_NAME_PROGRESS, "Failed to locate name progress checkbox")
        self.utils.check_visibility_of_element(BundleSelectors.CHECKBOX_PROGRESS_WHOLE_PACKAGE, "Failed to locate progress whole package checkbox")
        self.utils.check_visibility_of_element(BundleSelectors.TABLE_AVAILABLE_QUESTIONNAIRES, "Available questionnaires table not found")
        self.utils.check_visibility_of_element(BundleSelectors.TABLE_ASSIGNED_QUESTIONNAIRES, "Assigned questionnaires table not found")

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
    
        
        #Finally
        finally:
            if(created_questionnaire):
                self.utils.search_and_delete_item(created_questionnaire['name'], created_questionnaire['id'], "questionnaire")
        
    def tearDown(self):
        if self.driver:
            self.driver.quit()


if __name__ == "__main__":
    unittest.main()