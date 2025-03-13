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
from helper.Survey import SurveyHelper, SurveyAssertHelper, SurveySelectors
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
        
        self.survey_helper.end_survey()
        
        self.authentication_helper.logout()

    def tearDown(self):
        if self.driver:
            self.driver.quit()


if __name__ == "__main__":
    unittest.main()