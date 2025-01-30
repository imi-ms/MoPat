#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import datetime
import unittest

from helper.Encounter import EncounterSelectors, EncounterHelper, EncounterScheduleType
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
from helper.Question import QuestionHelper, QuestionType
from helper.Questionnaire import QuestionnaireHelper
from helper.Score import ScoreHelper
from helper.SeleniumUtils import SeleniumUtils
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
        self.encounter_helper = EncounterHelper(self.driver, self.navigation_helper)

        self.survey_assert_helper = SurveyAssertHelper(self.driver, self.navigation_helper)
        self.language_helper = LanguageHelper(self.driver, self.navigation_helper)

    def test_encounter_list(self):
        bundle_name = "TestBundle"
        clinic_name = "TestClinic"
        clinic_description = "TestDescription"
        
        # Arrange
        if not self.secret.get('admin-username') or not self.secret.get('admin-username'):
            self.skipTest("User AD credentials missing. Test skipped.")
        self.driver.get(self.https_base_url)
        self.authentication_helper.login(self.secret['admin-username'], self.secret['admin-password'])


        try:
            created_questionnaire = self.questionnaire_helper.create_questionnaire_with_questions(questionnaire_name="Test", questionnaire_description="Test",
                                            questionnaire_language_code="de_DE", questionnaire_display_name="Test",
                                            questionnaire_welcome_text="Test", questionnaire_final_text="Test", 
                                            question_types=[QuestionType.INFO_TEXT])
        except Exception as e:
            self.fail(f"Failed to create questionnaire: {e}")

        try:
            self.navigation_helper.navigate_to_manage_bundles()
            self.bundle_helper.create_bundle(bundle_name, True, [created_questionnaire])
            self.bundle_helper.save_bundle(bundle_name)
        except Exception as e:
            self.fail(f"Failed to create bundle: {e}")


        self.utils.search_item(bundle_name, "bundle")
        bundle_id = self.bundle_helper.get_bundle_id()

        try:
            self.navigation_helper.navigate_to_manage_clinics()
            self.clinic_helper.create_clinic(clinic_name=clinic_name, 
                                             clinic_description="Test Clinic Description",
                                             configurations=[{'selector': (By.CSS_SELECTOR, '#usePatientDataLookup > div:nth-child(1) > div:nth-child(3) > label:nth-child(1)')}],
                                             bundles=[{'id': bundle_id, 'name': bundle_name}])
            clinic_id=self.clinic_helper.save_clinic(clinic_name)

        except Exception as e:
            self.fail(f"Failed to create clinic: {e}")

        # Act
        self.navigation_helper.navigate_to_manage_surveys()

        #Assert - Check if tabs for "All Encounters" and "Scheduled Encounters" are present
        try:
            WebDriverWait(self.driver, 10).until(
                EC.element_to_be_clickable(EncounterSelectors.BUTTON_ENCOUNTER_TABLE)
            )
            WebDriverWait(self.driver, 10).until(
                EC.element_to_be_clickable(EncounterSelectors.BUTTON_ENCOUNTER_SCHEDULE_TABLE)
            )
        except TimeoutException:
            self.fail("Encounter tabs not found")
        
        # Act - Click on "All Encounters" tab
        self.utils.click_element(EncounterSelectors.BUTTON_ENCOUNTER_TABLE)
        # Assert - Check if the table for all encounters is present
        try:
            WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(EncounterSelectors.TABLE_ALL_ENCOUNTERS)
            )
        except TimeoutException:
            self.fail("All Encounters table not found")
        
        #Assert - Check if table pagination is present
        try:
            WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(EncounterSelectors.PAGINATION_ENCOUNTER_TABLE)
            )
        except TimeoutException:
            self.fail("Pagination for All Encounters table not found")

        #Assert - Check if table search is present
        try:
            WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(EncounterSelectors.SEARCH_ALL_ENCOUNTERS)
            )
        except TimeoutException:
            self.fail("Search for All Encounters table not found")

        #TODO: Action column, number of exports [after create survey function implementation]

        #Assert - Check for button for starting an encounter
        try:
            WebDriverWait(self.driver, 10).until(
                EC.element_to_be_clickable(EncounterSelectors.BUTTON_EXECUTE_ENCOUNTER)
            )
        except TimeoutException:
            self.fail("Execute Encounter button not found")


        # Act - Click on "Scheduled Encounters" tab
        self.utils.click_element(EncounterSelectors.BUTTON_ENCOUNTER_SCHEDULE_TABLE)
        # Assert - Check if the table for scheduled encounters is present
        try:
            WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(EncounterSelectors.TABLE_SCHEDULED_ENCOUNTERS)
            )
        except TimeoutException:
            self.fail("Scheduled Encounters table not found")

        #Assert - Check if table for scheduled encounters is present
        try:
            WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(EncounterSelectors.TABLE_SCHEDULED_ENCOUNTERS)
            )
        except TimeoutException:
            self.fail("Scheduled Encounters table not found")

        #Assert - Check if table pagination is present
        try:
            WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(EncounterSelectors.PAGINATION_ENCOUNTER_SCHEDULE_TABLE)
            )
        except TimeoutException:
            self.fail("Pagination for Scheduled Encounters table not found")
        
        #Assert - Check if table search is present
        try:
            WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(EncounterSelectors.SEARCH_SCHEDULED_ENCOUNTERS)
            )
        except TimeoutException:
            self.fail("Search for Scheduled Encounters table not found")
        encounter_id = None
        try:
            self.utils.click_element(EncounterSelectors.BUTTON_SCHEDULE_ENCOUNTER)
            encounter_id = self.encounter_helper.schedule_encounter("123456", clinic_name, bundle_name, "test@email.com", EncounterScheduleType.UNIQUELY,(datetime.date.today() + datetime.timedelta(days=1)).strftime("%Y-%m-%d"))
        except Exception as e:
            self.fail(f"Failed to schedule encounter: {e}")

        self.utils.click_element(EncounterSelectors.BUTTON_ENCOUNTER_SCHEDULE_TABLE)

        #Assert - Check if the action column is present
        try:
            WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(EncounterSelectors.TABLE_ACTION_COLUMN)
            )
        except TimeoutException:
            self.fail("Action column for Scheduled Encounters table not found")

        #TODO: number of exports [after survey schedule function implementation]

        #Assert - Check for button for scheduling an encounter
        try:
            WebDriverWait(self.driver, 10).until(
                EC.element_to_be_clickable(EncounterSelectors.BUTTON_SCHEDULE_ENCOUNTER)
            )
        except TimeoutException:
            self.fail("Schedule Encounter button not found")

        finally:
            self.encounter_helper.delete_scheduled_encounter(encounter_id, "123456")
            self.utils.search_and_delete_item(clinic_name,clinic_id, "clinic")
            self.utils.search_and_delete_item(bundle_name,bundle_id, "bundle")
            self.utils.search_and_delete_item(created_questionnaire['name'], created_questionnaire['id'], "questionnaire")


    def test_encounter_schedule(self):
        bundle_name = "TestBundle"
        clinic_name = "TestClinic"
        clinic_description = "TestDescription"
        
        # Arrange
        if not self.secret.get('admin-username') or not self.secret.get('admin-username'):
            self.skipTest("User AD credentials missing. Test skipped.")
        self.driver.get(self.https_base_url)
        self.authentication_helper.login(self.secret['admin-username'], self.secret['admin-password'])    

        try:
            created_questionnaire = self.questionnaire_helper.create_questionnaire_with_questions(questionnaire_name="Test", questionnaire_description="Test",
                                            questionnaire_language_code="de_DE", questionnaire_display_name="Test",
                                            questionnaire_welcome_text="Test", questionnaire_final_text="Test", 
                                            question_types=[QuestionType.INFO_TEXT])
        except Exception as e:
            self.fail(f"Failed to create questionnaire: {e}")

        try:
            self.navigation_helper.navigate_to_manage_bundles()
            self.bundle_helper.create_bundle(bundle_name, True, [created_questionnaire])
            self.bundle_helper.save_bundle(bundle_name)
        except Exception as e:
            self.fail(f"Failed to create bundle: {e}")


        self.utils.search_item(bundle_name, "bundle")
        bundle_id = self.bundle_helper.get_bundle_id()
        clinic_id = None
        try:
            self.navigation_helper.navigate_to_manage_clinics()
            self.clinic_helper.create_clinic(clinic_name=clinic_name, 
                                             clinic_description=clinic_description,
                                             configurations=[{'selector': (By.CSS_SELECTOR, '#usePatientDataLookup > div:nth-child(1) > div:nth-child(3) > label:nth-child(1)')}],
                                             bundles=[{'id': bundle_id, 'name': bundle_name}])
            clinic_id=self.clinic_helper.save_clinic(clinic_name)

        except Exception as e:
            self.fail(f"Failed to create clinic: {e}")
        
        try:
            self.navigation_helper.navigate_to_manage_surveys()
            self.utils.click_element(EncounterSelectors.BUTTON_ENCOUNTER_SCHEDULE_TABLE)
            self.utils.click_element(EncounterSelectors.BUTTON_SCHEDULE_ENCOUNTER)
        except Exception as e:
            self.fail(f"Failed to navigate to Schedule Encounter form: {e}")

        # Assert - Check if all the inputs are present
        try:
            WebDriverWait(self.driver, 10).until(
            EC.presence_of_element_located(EncounterSelectors.INPUT_SCHEDULE_CASE_NUMBER)
            )
        except TimeoutException:
            self.fail("Schedule Encounter form element 'Case Number' not found")

        try:
            WebDriverWait(self.driver, 10).until(
            EC.presence_of_element_located(EncounterSelectors.SELECT_SCHEDULE_CLINIC)
            )
        except TimeoutException:
            self.fail("Schedule Encounter form element 'Clinic' not found")

        try:
            WebDriverWait(self.driver, 10).until(
            EC.presence_of_element_located(EncounterSelectors.SELECT_SCHEDULE_BUNDLE)
            )
        except TimeoutException:
            self.fail("Schedule Encounter form element 'Bundle' not found")

        try:
            WebDriverWait(self.driver, 10).until(
            EC.presence_of_element_located(EncounterSelectors.INPUT_SCHEDULE_EMAIL)
            )
        except TimeoutException:
            self.fail("Schedule Encounter form element 'Email' not found")

        try:
            WebDriverWait(self.driver, 10).until(
            EC.presence_of_element_located(EncounterSelectors.SELECT_SURVEY_TYPE)
            )
        except TimeoutException:
            self.fail("Schedule Encounter form element 'Survey Type' not found")

        try:
            WebDriverWait(self.driver, 10).until(
            EC.presence_of_element_located(EncounterSelectors.INPUT_DATE)
            )
        except TimeoutException:
            self.fail("Schedule Encounter form element 'Date' not found")

        try:
            WebDriverWait(self.driver, 10).until(
            EC.presence_of_element_located(EncounterSelectors.INPUT_END_DATE)
            )
        except TimeoutException:
            self.fail("Schedule Encounter form element 'End Date' not found")

        try:
            WebDriverWait(self.driver, 10).until(
            EC.presence_of_element_located(EncounterSelectors.INPUT_TIME_PERIOD)
            )
        except TimeoutException:
            self.fail("Schedule Encounter form element 'Time Period' not found")

        try:
            WebDriverWait(self.driver, 10).until(
            EC.presence_of_element_located(EncounterSelectors.SELECT_LANGUAGE)
            )
        except TimeoutException:
            self.fail("Schedule Encounter form element 'Language' not found")

        try:
            WebDriverWait(self.driver, 10).until(
            EC.presence_of_element_located(EncounterSelectors.INPUT_PERSONAL_TEXT)
            )
        except TimeoutException:
            self.fail("Schedule Encounter form element 'Personal Text' not found")
        
        encounter_id = None
        try:
            encounter_id = self.encounter_helper.schedule_encounter("123456", clinic_name, bundle_name, "test@email.com", EncounterScheduleType.UNIQUELY,(datetime.date.today() + datetime.timedelta(days=1)).strftime("%Y-%m-%d"))
        except Exception as e:
            self.fail(f"Failed to schedule encounter: {e}")
            
        finally:
            self.encounter_helper.delete_scheduled_encounter(encounter_id, "123456")
            self.utils.search_and_delete_item(clinic_name,clinic_id, "clinic")
            self.utils.search_and_delete_item(bundle_name,bundle_id, "bundle")
            self.utils.search_and_delete_item(created_questionnaire['name'], created_questionnaire['id'], "questionnaire")
        
    def tearDown(self):
        if self.driver:
            self.driver.quit()


if __name__ == "__main__":
    unittest.main()