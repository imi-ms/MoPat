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
        created_questionnaire = {}
        bundle={}
        clinic={}
        
        # Arrange
        self.driver.get(self.https_base_url)
        self.authentication_helper.login(self.secret['admin-username'], self.secret['admin-password'])


        try:
            created_questionnaire = self.questionnaire_helper.create_questionnaire_with_questions()
        except Exception as e:
            self.fail(f"Failed to create questionnaire: {e}")

        try:
            self.navigation_helper.navigate_to_manage_bundles()
            bundle = self.bundle_helper.create_bundle(publish_bundle=True, questionnaires=[created_questionnaire])
            bundle["id"]=self.bundle_helper.save_bundle(bundle["name"])
        except Exception as e:
            self.fail(f"Failed to create bundle: {e}")

        try:
            self.navigation_helper.navigate_to_manage_clinics()
            clinic["name"]=self.clinic_helper.create_clinic(configurations=[{'selector': (By.CSS_SELECTOR, '#usePatientDataLookup > div:nth-child(1) > div:nth-child(3) > label:nth-child(1)')}],
                                             bundles=[bundle])
            clinic["id"]=self.clinic_helper.save_clinic(clinic["name"])

        except Exception as e:
            self.fail(f"Failed to create clinic: {e}")

        # Act
        self.navigation_helper.navigate_to_manage_surveys()
        
        self.utils.check_visibility_of_element(EncounterSelectors.BUTTON_ENCOUNTER_TABLE, "Encounter Table button not found")
        self.utils.check_visibility_of_element(EncounterSelectors.BUTTON_ENCOUNTER_SCHEDULE_TABLE, "Encounter Schedule Table button not found")

        # Act - Click on "All Encounters" tab
        self.utils.click_element(EncounterSelectors.BUTTON_ENCOUNTER_TABLE)
        self.utils.check_visibility_of_element(EncounterSelectors.TABLE_ALL_ENCOUNTERS, "All Encounters table not found")

        self.utils.check_visibility_of_element(EncounterSelectors.PAGINATION_ENCOUNTER_TABLE, "Pagination for All Encounters table not found")
        self.utils.check_visibility_of_element(EncounterSelectors.SEARCH_ALL_ENCOUNTERS, "Search for All Encounters table not found")

        #TODO: Action column, number of exports [after create survey function implementation]

        self.utils.check_visibility_of_element(EncounterSelectors.BUTTON_EXECUTE_ENCOUNTER, "Execute Encounter button not found")

        # Act - Click on "Scheduled Encounters" tab
        self.utils.click_element(EncounterSelectors.BUTTON_ENCOUNTER_SCHEDULE_TABLE)
        self.utils.check_visibility_of_element(EncounterSelectors.TABLE_SCHEDULED_ENCOUNTERS, "Scheduled Encounters table not found")
        self.utils.check_visibility_of_element(EncounterSelectors.PAGINATION_ENCOUNTER_SCHEDULE_TABLE, "Pagination for Scheduled Encounters table not found")

        self.utils.check_visibility_of_element(EncounterSelectors.SEARCH_SCHEDULED_ENCOUNTERS, "Search for Scheduled Encounters table not found")
        
        encounter_id = None
        try:
            self.utils.click_element(EncounterSelectors.BUTTON_SCHEDULE_ENCOUNTER)
            encounter_id = self.encounter_helper.schedule_encounter("123456", clinic["name"], bundle["name"], "test@email.com", EncounterScheduleType.UNIQUELY,(datetime.date.today() + datetime.timedelta(days=1)).strftime("%Y-%m-%d"))
        except Exception as e:
            self.fail(f"Failed to schedule encounter: {e}")

        self.utils.click_element(EncounterSelectors.BUTTON_ENCOUNTER_SCHEDULE_TABLE)

        self.utils.check_visibility_of_element(EncounterSelectors.TABLE_ACTION_COLUMN, "Action column for Scheduled Encounters table not found")

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
            self.utils.search_and_delete_item(clinic["name"],clinic["id"], "clinic")
            self.utils.search_and_delete_item(bundle["name"],bundle["id"], "bundle")
            self.utils.search_and_delete_item(created_questionnaire['name'], created_questionnaire['id'], "questionnaire")


    def test_encounter_schedule(self):
        clinic={}
        bundle={}
        created_questionnaire = {}
        
        # Arrange
        self.driver.get(self.https_base_url)
        self.authentication_helper.login(self.secret['admin-username'], self.secret['admin-password'])    

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
            clinic["name"]=self.clinic_helper.create_clinic(configurations=[{'selector': (By.CSS_SELECTOR, '#usePatientDataLookup > div:nth-child(1) > div:nth-child(3) > label:nth-child(1)')}],
                                             bundles=[bundle])
            clinic["id"]=self.clinic_helper.save_clinic(clinic["name"])

        except Exception as e:
            self.fail(f"Failed to create clinic: {e}")
        
        try:
            self.navigation_helper.navigate_to_manage_surveys()
            self.utils.click_element(EncounterSelectors.BUTTON_ENCOUNTER_SCHEDULE_TABLE)
            self.utils.click_element(EncounterSelectors.BUTTON_SCHEDULE_ENCOUNTER)
        except Exception as e:
            self.fail(f"Failed to navigate to Schedule Encounter form: {e}")

        self.utils.check_visibility_of_element(EncounterSelectors.INPUT_SCHEDULE_CASE_NUMBER, "Case Number input not found")
        self.utils.check_visibility_of_element(EncounterSelectors.SELECT_SCHEDULE_CLINIC, "Clinic select not found")
        self.utils.check_visibility_of_element(EncounterSelectors.SELECT_SCHEDULE_BUNDLE, "Bundle select not found")
        self.utils.check_visibility_of_element(EncounterSelectors.INPUT_SCHEDULE_EMAIL, "Email input not found")
        self.utils.check_visibility_of_element(EncounterSelectors.SELECT_SURVEY_TYPE, "Survey Type select not found")
        self.utils.check_visibility_of_element(EncounterSelectors.INPUT_DATE, "Date input not found")
        self.utils.check_visibility_of_element(EncounterSelectors.INPUT_END_DATE, "End Date input not found")
        self.utils.check_visibility_of_element(EncounterSelectors.INPUT_TIME_PERIOD, "Time Period input not found")
        self.utils.check_visibility_of_element(EncounterSelectors.SELECT_LANGUAGE, "Language select not found")
        self.utils.check_visibility_of_element(EncounterSelectors.INPUT_PERSONAL_TEXT, "Personal Text input not found")

        
        encounter_id = None
        try:
            encounter_id = self.encounter_helper.schedule_encounter("123456", clinic["name"], bundle["name"], "test@email.com", EncounterScheduleType.UNIQUELY,(datetime.date.today() + datetime.timedelta(days=1)).strftime("%Y-%m-%d"))
        except Exception as e:
            self.fail(f"Failed to schedule encounter: {e}")
            
        finally:
            self.encounter_helper.delete_scheduled_encounter(encounter_id, "123456")
            self.utils.search_and_delete_item(clinic["name"],clinic["id"], "clinic")
            self.utils.search_and_delete_item(bundle["name"],bundle["id"], "bundle")
            self.utils.search_and_delete_item(created_questionnaire['name'], created_questionnaire['id'], "questionnaire")
        
    def tearDown(self):
        if self.driver:
            self.driver.quit()


if __name__ == "__main__":
    unittest.main()