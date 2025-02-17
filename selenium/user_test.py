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

    def test_user_list(self):
        # Arrange
        self.driver.get(self.https_base_url)
        self.authentication_helper.login(self.secret['admin-username'], self.secret['admin-password'])

        # Act
        self.navigation_helper.navigate_to_manager_user()

        self.utils.check_visibility_of_element(UserSelector.TABLE_USERS, "User list not displayed")
        self.utils.check_visibility_of_element(UserSelector.PAGINATION_USER_TABLE, "Pagination not displayed")
        self.utils.check_visibility_of_element(UserSelector.TABLE_ACTION_BUTTONS, "Action buttons not displayed")
        self.utils.check_visibility_of_element(UserSelector.BUTTON_INVITE_USER, "Invite user button not displayed")
        
    def test_invitation_edit(self):
        # Arrange
        self.driver.get(self.https_base_url)
        self.authentication_helper.login(self.secret['admin-username'], self.secret['admin-password'])

        #Arrange - Create a new clinic
        clinic={}
        self.navigation_helper.navigate_to_manage_clinics()

        try:
            clinic["name"]=self.clinic_helper.create_clinic(configurations=[{'selector': (By.CSS_SELECTOR, '#usePseudonymizationService > div:nth-child(1) > div:nth-child(3) > label:nth-child(1)')}],)
            clinic["id"]=self.clinic_helper.save_clinic(clinic["name"])
        except TimeoutException:
            self.fail("Failed to create clinic")

        #Arrange - Click on the user menu
        self.navigation_helper.navigate_to_manager_user()

        self.utils.click_element(UserSelector.BUTTON_INVITE_USER)
        
        self.utils.check_visibility_of_element(UserSelector.INPUT_USER_FIRST_NAME(0), "First name input field not displayed")
        self.utils.check_visibility_of_element(UserSelector.INPUT_USER_LAST_NAME(0), "Last name input field not displayed")
        self.utils.check_visibility_of_element(UserSelector.INPUT_USER_EMAIL(0), "Email input field not displayed")
        self.utils.check_visibility_of_element(UserSelector.BUTTON_ADD_USER, "Add user button not displayed")
        self.utils.click_element(UserSelector.BUTTON_ADD_USER)
        self.utils.check_visibility_of_element(UserSelector.INPUT_USER_FIRST_NAME(1), "Second user's first name input field not displayed")
        self.utils.check_visibility_of_element(UserSelector.INPUT_USER_LAST_NAME(1), "Second user's last name input field not displayed")
        self.utils.check_visibility_of_element(UserSelector.INPUT_USER_EMAIL(1), "Second user's email input field not displayed")
        self.utils.click_element(UserSelector.BUTTON_REMOVE_INVITATION)        
        
        #Assert - Check if the fields were removed
        try:
            WebDriverWait(self.driver, 10).until_not(
                EC.presence_of_element_located(UserSelector.INPUT_USER_FIRST_NAME(0))
            )
        except TimeoutException:
            self.fail("First name input field still displayed")

        try:
            WebDriverWait(self.driver, 10).until_not(
                EC.presence_of_element_located(UserSelector.INPUT_USER_LAST_NAME(0))
            )
        except TimeoutException:
            self.fail("Last name input field still displayed")
        
        try:
            WebDriverWait(self.driver, 10).until_not(
                EC.presence_of_element_located(UserSelector.INPUT_USER_EMAIL(0))
            )
        except TimeoutException:
            self.fail("Email input field still displayed")

        self.utils.check_visibility_of_element(UserSelector.INPUT_CSV, "File upload button not displayed")
        self.utils.check_visibility_of_element(UserSelector.SELECT_USER_ROLE, "Role dropdown not displayed")
        self.utils.check_visibility_of_element(UserSelector.SELECT_USER_LANGUAGE, "Language dropdown not displayed")
        self.utils.check_visibility_of_element(UserSelector.INPUT_PERSONAL_TEXT, "Invite message input field not displayed")
        self.utils.check_visibility_of_element(UserSelector.TABLE_AVAILABLE_CLINICS, "Available clinic table not displayed")
        self.utils.check_visibility_of_element(UserSelector.TABLE_ASSIGNED_CLINICS, "Assigned clinic table not displayed")
        
        self.utils.click_element(UserSelector.BUTTON_MOVE_CLINIC(clinic["id"]))
        
        self.utils.click_element(UserSelector.BUTTON_MOVE_CLINIC(clinic["id"]))
        

        #Assert - Check validations
        try:
            WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(UserSelector.BUTTON_SEND_INVITE)
            )
            self.utils.click_element(UserSelector.BUTTON_SEND_INVITE)
            WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(ErrorSelectors.INPUT_VALIDATION_SELECTOR)
            )
            validation_errors = self.driver.find_elements(*ErrorSelectors.INPUT_VALIDATION_SELECTOR)
            self.assertEqual(len(validation_errors), 3, "Expected 3 validation errors, but found {len(validation_errors)}")

        except TimeoutException:
            self.fail("Validation error not displayed")

        #Assert - Check if preview button works
        try:
            self.utils.fill_text_field(UserSelector.INPUT_USER_FIRST_NAME(0), "Test1")
            self.utils.fill_text_field(UserSelector.INPUT_USER_LAST_NAME(0), "Test2")
            self.utils.fill_text_field(UserSelector.INPUT_USER_EMAIL(0), "test@test.com")
            WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(UserSelector.BUTTON_PREVIEW)
            )
            self.utils.click_element(UserSelector.BUTTON_PREVIEW)
            WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(UserSelector.DIV_PREVIEW)
            )
        except TimeoutException:
            self.fail("Preview not displayed")
        finally:
            if clinic["id"]:
                self.utils.search_and_delete_item(clinic["name"], clinic["id"], "clinic")
        
    def test_user_mail_to_all(self):
        test_subject = "Test Subject"
        test_content = "Test Content"
        # Arrange
        self.driver.get(self.https_base_url)
        self.authentication_helper.login(self.secret['admin-username'], self.secret['admin-password'])

        self.navigation_helper.navigate_to_email_to_all_users()
        
        self.utils.check_visibility_of_element(EmailSelectors.SUBJECT_INPUT, "Subject input field not displayed")
        self.utils.check_visibility_of_element(EmailSelectors.CONTENT_INPUT, "Content input field not displayed")
        self.utils.check_visibility_of_element(UserSelector.SELECT_MAIL_LANGUAGE, "Language dropdown not displayed")
        self.utils.check_visibility_of_element(EmailSelectors.MAIL_PREVIEW_BUTTON, "Preview button not displayed")
        self.utils.check_visibility_of_element(EmailSelectors.SEND_BUTTON, "Send button not displayed")


        #Assert - Check if the preview button works
        try:
            self.utils.fill_text_field(EmailSelectors.SUBJECT_INPUT, test_subject)
            self.utils.fill_text_field(EmailSelectors.CONTENT_INPUT, test_content)
            self.utils.click_element(EmailSelectors.MAIL_PREVIEW_BUTTON)
            WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(UserSelector.DIV_PREVIEW_MAIL)
            )
        except TimeoutException:
            self.fail("Preview not displayed")

        self.authentication_helper.logout()

    def test_invitation_list(self):
        # Arrange
        self.driver.get(self.https_base_url)
        self.authentication_helper.login(self.secret['admin-username'], self.secret['admin-password'])

        # Act
        self.navigation_helper.navigate_to_manage_invitations()
        self.utils.check_visibility_of_element(UserSelector.TABLE_INVITAIONS, "User list not displayed")
        self.utils.check_visibility_of_element(UserSelector.PAGINATION_INVITATION_TABLE, "Pagination not displayed")
        self.utils.check_visibility_of_element(UserSelector.BUTTON_INVITE_USER, "Invite user button not displayed")

        self.authentication_helper.logout()

    def tearDown(self):
        if self.driver:
            self.driver.quit()


if __name__ == "__main__":
    unittest.main()