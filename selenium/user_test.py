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
from helper.User import UserSelector
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

    def test_user_list(self):
        # Arrange
        if not self.secret.get('admin-username') or not self.secret.get('admin-username'):
            self.skipTest("User AD credentials missing. Test skipped.")
        self.driver.get(self.https_base_url)
        self.authentication_helper.login(self.secret['admin-username'], self.secret['admin-password'])

        # Act
        self.navigation_helper.navigate_to_manager_user()

        # Assert - Check if the user list is displayed
        try:
            WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(UserSelector.TABLE_USERS)
            )
        except:
            self.fail("User list not displayed")

        #Assert - Check if the elements for pagination is displayed
        try:
            WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(UserSelector.PAGINATION_USER_TABLE)
            )
        except:
            self.fail("Pagination not displayed")

        #Assert - Check if the elements for action buttons is displayed
        try:
            WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(UserSelector.TABLE_ACTION_BUTTONS)
            )
        except:
            self.fail("Action buttons not displayed")
        
        #Assert - Check if the button to invite a user is displayed
        try:
            WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(UserSelector.BUTTON_INVITE_USER)
            )
        except:
            self.fail("Invite user button not displayed")
        
    def test_invitation_edit(self):
        # Arrange
        if not self.secret.get('admin-username') or not self.secret.get('admin-username'):
            self.skipTest("User AD credentials missing. Test skipped.")
        self.driver.get(self.https_base_url)
        self.authentication_helper.login(self.secret['admin-username'], self.secret['admin-password'])

        #Arrange - Create a new clinic
        clinic_name = "Test Clinic"
        clinic_description = "This is a test clinic"
        clinic_id = None
        self.navigation_helper.navigate_to_manage_clinics()

        try:
            self.clinic_helper.create_clinic(clinic_name, clinic_description,
                                             configurations=[{'selector': (By.CSS_SELECTOR, '#usePseudonymizationService > div:nth-child(1) > div:nth-child(3) > label:nth-child(1)')}],)
            clinic_id=self.clinic_helper.save_clinic(clinic_name)
        except TimeoutException:
            self.fail("Failed to create clinic")

        #Arrange - Click on the user menu
        self.navigation_helper.navigate_to_manager_user()

        # Act
        try:
            WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(UserSelector.BUTTON_INVITE_USER)
            )
            self.utils.click_element(UserSelector.BUTTON_INVITE_USER)
        except TimeoutException:
            self.fail("User list not displayed")

        #Assert - Check if the input fields are displayed
        try:
            WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(UserSelector.INPUT_USER_FIRST_NAME(0))
            )
        except TimeoutException:
            self.fail("First name input field not displayed")
        
        try:
            WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(UserSelector.INPUT_USER_LAST_NAME(0))
            )
        except TimeoutException:
            self.fail("Last name input field not displayed")
        
        try:
            WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(UserSelector.INPUT_USER_EMAIL(0))
            )
        except TimeoutException:
            self.fail("Email input field not displayed")
        
        #Assert - Check if add new user button is displayed
        try:
            WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(UserSelector.BUTTON_ADD_USER)
            )
            self.utils.click_element(UserSelector.BUTTON_ADD_USER)
        except TimeoutException:
            self.fail("Add user button not displayed")
        
        #Assert - Check if new user inputs are created
        try:
            WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(UserSelector.INPUT_USER_FIRST_NAME(1))
            )
        except TimeoutException:
            self.fail("Second user's first name input field not displayed")
        
        try:
            WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(UserSelector.INPUT_USER_LAST_NAME(1))
            )
        except TimeoutException:
            self.fail("Second user's last name input field not displayed")
        
        try:
            WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(UserSelector.INPUT_USER_EMAIL(1))
            )
        except TimeoutException:
            self.fail("Second user's email input field not displayed")
        
        #Assert - Check if the remove user button is displayed
        try:
            WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(UserSelector.BUTTON_REMOVE_INVITATION)
            )
            self.utils.click_element(UserSelector.BUTTON_REMOVE_INVITATION)
        except TimeoutException:
            self.fail("Remove user button not displayed")
        
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

        #Assert - Check if the file upload button is displayed
        try:
            WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(UserSelector.INPUT_CSV)
            )
        except TimeoutException:
            self.fail("File upload button not displayed")
        #Assert - Check if the role dropdown is displayed
        try:
            WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(UserSelector.SELECT_USER_ROLE)
            )
        except TimeoutException:
            self.fail("Role dropdown not displayed")

        #Assert - Check if the language dropdown is displayed
        try:
            WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(UserSelector.SELECT_USER_LANGUAGE)
            )
        except TimeoutException:
            self.fail("Language dropdown not displayed")

        #Assert - Check if input for invitation message is displayed
        try:
            WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(UserSelector.INPUT_PERSONAL_TEXT)
            )
        except TimeoutException:
            self.fail("Invite message input field not displayed")
        
        #Assert - Check if the clinic table is displayed
        try:
            WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(UserSelector.TABLE_AVAILABLE_CLINICS)
            )
        except TimeoutException:
            self.fail("Available clinic table not displayed")

        #Assert - Check if the assigned clinics table is displayed
        try:
            WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(UserSelector.TABLE_ASSIGNED_CLINICS)
            )
        except TimeoutException:
            self.fail("Assigned clinic table not displayed")

        #Assert - Move clinic from available to assigned
        try:
            WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(UserSelector.BUTTON_MOVE_CLINIC(clinic_id))
            )
            self.utils.click_element(UserSelector.BUTTON_MOVE_CLINIC(clinic_id))
        except TimeoutException:
            self.fail("Move clinic button not displayed")

        #Assert - Move clinic from assigned to available
        try:
            WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(UserSelector.BUTTON_MOVE_CLINIC(clinic_id))
            )
            self.utils.click_element(UserSelector.BUTTON_MOVE_CLINIC(clinic_id))
        except TimeoutException:
            self.fail("Move clinic button not displayed")

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
            if clinic_id:
                self.utils.search_and_delete_item(clinic_name, clinic_id, "clinic")
        
    def test_user_mail_to_all(self):
        test_subject = "Test Subject"
        test_content = "Test Content"
        # Arrange
        if not self.secret.get('admin-username') or not self.secret.get('admin-username'):
            self.skipTest("User AD credentials missing. Test skipped.")
        self.driver.get(self.https_base_url)
        self.authentication_helper.login(self.secret['admin-username'], self.secret['admin-password'])

        self.navigation_helper.navigate_to_email_to_all_users()
        
        #Assert - Check if the subject input is displayed
        try:
            WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(EmailSelectors.SUBJECT_INPUT)
            )
        except TimeoutException:
            self.fail("Subject input field not displayed")

        #Assert - Check if the content input is displayed
        try:
            WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(EmailSelectors.CONTENT_INPUT)
            )
        except TimeoutException:
            self.fail("Content input field not displayed")
            
        #Assert - Check if the language dropdown is displayed
        try:
            WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(UserSelector.SELECT_MAIL_LANGUAGE)
            )
        except TimeoutException:
            self.fail("Language dropdown not displayed")

        #Assert - Check if the preview button is displayed
        try:
            WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(EmailSelectors.MAIL_PREVIEW_BUTTON)
            )
        except TimeoutException:
            self.fail("Preview button not displayed")

        #Assert - Check if the send button is displayed
        try:
            WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(EmailSelectors.SEND_BUTTON)
            )
        except TimeoutException:
            self.fail("Send button not displayed")

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

    def test_invitation_list(self):
        # Arrange
        if not self.secret.get('admin-username') or not self.secret.get('admin-username'):
            self.skipTest("User AD credentials missing. Test skipped.")
        self.driver.get(self.https_base_url)
        self.authentication_helper.login(self.secret['admin-username'], self.secret['admin-password'])

        # Act
        self.navigation_helper.navigate_to_manage_invitations()

        #Assert - Check if the user list is displayed
        try:
            WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(UserSelector.TABLE_INVITAIONS)
            )
        except:
            self.fail("User list not displayed")

        #Assert - Check if the elements for pagination is displayed
        try:
            WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(UserSelector.PAGINATION_INVITATION_TABLE)
            )
        except:
            self.fail("Pagination not displayed")

        #Assert - Check if the elements for action buttons is displayed
        try:
            WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(UserSelector.TABLE_ACTION_BUTTONS)
            )
        except:
            self.fail("Action buttons not displayed")
        
        #Assert - Check if the button to invite a user is displayed
        try:
            WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(UserSelector.BUTTON_INVITE_USER)
            )
        except:
            self.fail("Invite user button not displayed")



    def tearDown(self):
        if self.driver:
            self.driver.quit()


if __name__ == "__main__":
    unittest.main()