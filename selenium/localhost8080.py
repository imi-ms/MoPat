#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import random
import string
import unittest
import datetime

from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.support import expected_conditions as EC

from helper.Authentication import AuthenticationHelper
from helper.Bundle import BundleHelper
from helper.Clinic import ClinicHelper, ClinicSelectors
from helper.Navigation import NavigationHelper
from helper.Question import QuestionHelper, QuestionErrorHelper, QuestionSelectors
from helper.Questionnaire import QuestionnaireHelper, ConditionHelper, ScoreHelper
from helper.SeleniumUtils import SeleniumUtils
from helper.Survey import SurveyHelper, SurveySelectors, SurveyAssertHelper
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
        # Initialize the WebDriver
        self.driver = webdriver.Chrome()

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

    def test_login_ad_admin(self):
        # Arrange
        if not self.secret.get('admin-username-ad') or not self.secret.get('admin-password-ad'):
            self.skipTest("Admin login data is missing. Test skipped.")
        self.driver.get(self.https_base_url)

        # Act
        self.authentication_helper.login(self.secret['admin-username-ad'], self.secret['admin-password-ad'])

        # Assert - Verify login
        try:
            WebDriverWait(self.driver, 10).until(
                EC.url_to_be(self.https_base_url + URLPaths.ADMIN_INDEX)
            )
        except TimeoutException:
            current_url = self.driver.current_url
            self.fail(
                f"Login failed: Expected URL '{self.https_base_url + URLPaths.ADMIN_INDEX}', but got '{current_url}'")

        self.authentication_helper.logout()

        # Assert - Verify logout
        try:
            WebDriverWait(self.driver, 10).until(
                EC.url_to_be(self.https_base_url + URLPaths.MOBILE_USER_LOGIN_DE)
            )
        except TimeoutException:
            current_url = self.driver.current_url
            self.fail(
                f"Logout failed: Expected URL '{self.https_base_url + URLPaths.MOBILE_USER_LOGIN_DE}', but got '{current_url}'")

    def test_login_local_admin(self):
        # Arrange
        if not self.secret.get('admin-username-local') or not self.secret.get('admin-password-ad'):
            self.skipTest("Admin login data is missing. Test skipped.")
        self.driver.get(self.https_base_url)

        self.authentication_helper.login(self.secret['admin-username-local'], self.secret['admin-password-ad'])

        # Assert - Verify login
        try:
            WebDriverWait(self.driver, 10).until(
                EC.url_to_be(self.https_base_url + URLPaths.ADMIN_INDEX)
            )
        except TimeoutException:
            current_url = self.driver.current_url
            self.fail(
                f"Login failed: Expected URL '{self.https_base_url + URLPaths.ADMIN_INDEX}', but got '{current_url}'")

        # Logout
        self.authentication_helper.logout()

        # Assert - Verify logout
        try:
            WebDriverWait(self.driver, 10).until(
                EC.url_to_be(self.https_base_url + URLPaths.MOBILE_USER_LOGIN_DE)
            )
        except TimeoutException:
            current_url = self.driver.current_url
            self.fail(
                f"Logout failed: Expected URL '{self.https_base_url + URLPaths.MOBILE_USER_LOGIN_DE}', but got '{current_url}'")

    def test_login_ad_user(self):
        # Arrange
        if not self.secret.get('user-username-ad') or not self.secret.get('user-password-ad'):
            self.skipTest("User AD credentials missing. Test skipped.")
        self.driver.get(self.https_base_url)

        # Act
        self.authentication_helper.login(self.secret['user-username-ad'], self.secret['user-password-ad'])

        # Assert - Verify login
        try:
            WebDriverWait(self.driver, 10).until(
                EC.url_to_be(self.https_base_url + URLPaths.MOBILE_SURVEY_INDEX)
            )
        except TimeoutException:
            current_url = self.driver.current_url
            self.fail(
                f"Login failed: Expected URL '{self.https_base_url + URLPaths.MOBILE_SURVEY_INDEX}', but got '{current_url}'")

        # Logout
        self.authentication_helper.logout()

        # Assert - Verify logout
        try:
            WebDriverWait(self.driver, 10).until(
                EC.url_to_be(self.https_base_url + URLPaths.MOBILE_USER_LOGIN_DE)
            )
        except TimeoutException:
            current_url = self.driver.current_url
            self.fail(
                f"Logout failed: Expected URL '{self.https_base_url + URLPaths.MOBILE_USER_LOGIN_DE}', but got '{current_url}'")

    def test_login_local_user(self):
        # Arrange
        if not self.secret.get('user-username-local') or not self.secret.get('user-password-local'):
            self.skipTest("Local user credentials missing. Test skipped.")
        self.driver.get(self.https_base_url)

        # Act
        self.authentication_helper.login(self.secret['user-username-local'], self.secret['user-password-local'])

        # Assert - Verify login
        try:
            WebDriverWait(self.driver, 10).until(
                EC.url_to_be(self.https_base_url + URLPaths.MOBILE_SURVEY_INDEX)
            )
        except TimeoutException:
            current_url = self.driver.current_url
            self.fail(
                f"Login failed: Expected URL '{self.https_base_url + URLPaths.MOBILE_SURVEY_INDEX}', but got '{current_url}'")

        # Logout
        self.authentication_helper.logout()

        # Assert - Verify logout
        try:
            WebDriverWait(self.driver, 10).until(
                EC.url_to_be(self.https_base_url + URLPaths.MOBILE_USER_LOGIN_DE)
            )
        except TimeoutException:
            current_url = self.driver.current_url
            self.fail(
                f"Logout failed: Expected URL '{self.https_base_url + URLPaths.MOBILE_USER_LOGIN_DE}', but got '{current_url}'")

    def test_login_unknown_user(self):
        # Arrange
        self.driver.get(self.https_base_url)

        # Generate random login username and password
        username = ''.join(random.choice(string.ascii_lowercase + string.digits) for _ in range(7))
        password = ''.join(random.choice(string.ascii_lowercase + string.digits) for _ in range(7))

        # Act
        self.authentication_helper.login(username, password)

        # Assert - Verify bad credentials URL
        expected_url = self.https_base_url + URLPaths.LOGIN_BAD_CREDENTIALS
        try:
            WebDriverWait(self.driver, 10).until(
                EC.url_to_be(expected_url)
            )
        except TimeoutException:
            current_url = self.driver.current_url
            self.fail(f"URL mismatch. Expected '{expected_url}', got '{current_url}'")

    def test_login_ad_user_wrong_password(self):
        # Arrange
        if not self.secret.get('user-username-local'):
            self.skipTest("User login data is missing. Test skipped.")
        self.driver.get(self.https_base_url)

        # Generate random login password
        username = self.secret['user-username-local']
        password = ''.join(random.choice(string.ascii_lowercase + string.digits) for _ in range(7))

        # Act
        self.authentication_helper.login(username, password)

        # Check expected URL
        expected_url = self.https_base_url + URLPaths.LOGIN_BAD_CREDENTIALS
        try:
            WebDriverWait(self.driver, 10).until(
                EC.url_to_be(expected_url)
            )
        except TimeoutException:
            current_url = self.driver.current_url
            self.fail(f"URL mismatch. Expected part of URL '{expected_url}', but got '{current_url}'")

    def test_login_local_user_wrong_password(self):
        # Arrange
        if not self.secret.get('user-username-local'):
            self.skipTest("Local user login data is missing. Test skipped.")
        self.driver.get(self.https_base_url)

        # Generate random login password
        username = self.secret['user-username-local']
        password = ''.join(random.choice(string.ascii_lowercase + string.digits) for _ in range(7))

        # Act
        self.authentication_helper.login(username, password)

        # Check expected URL
        expected_url = self.https_base_url + URLPaths.LOGIN_BAD_CREDENTIALS
        try:
            WebDriverWait(self.driver, 10).until(EC.url_to_be(expected_url))
        except TimeoutException:
            current_url = self.driver.current_url
            self.fail(f"URL mismatch. Expected '{expected_url}', but got '{current_url}'")

    def test_login_disabled_ad_user(self):
        # Arrange
        if not self.secret.get('user-username-disabled-ad') or not self.secret.get('user-password-disabled-ad'):
            self.skipTest("Disabled user login data is missing. Test skipped.")
        self.driver.get(self.https_base_url)
        username = self.secret['user-username-disabled-ad']
        password = self.secret['user-password-disabled-ad']

        # Act
        self.authentication_helper.login(username, password)

        # Assert
        expected_url = self.https_base_url + URLPaths.LOGIN_DISABLED_EXCEPTION
        try:
            WebDriverWait(self.driver, 10).until(EC.url_to_be(expected_url))
        except TimeoutException:
            current_url = self.driver.current_url
            self.fail(f"URL mismatch. Expected URL '{expected_url}', but got '{current_url}'")

    def test_password_reset_unknown_user(self):
        # Arrange
        self.driver.get(self.https_base_url)

        # Act
        # Click on “Forgot your password?” Link
        try:
            forgot_password_link = WebDriverWait(self.driver, 10).until(
                EC.element_to_be_clickable(PasswordResetSelectors.FORGOT_PASSWORD_LINK)
            )
            forgot_password_link.click()
        except TimeoutException:
            self.fail("Failed to locate or click the 'Forgot your password?' link.")

        # Assert - Verify navigation to password reset page
        expected_url = self.https_base_url + URLPaths.PASSWORD_FORGOT
        try:
            WebDriverWait(self.driver, 10).until(EC.url_to_be(expected_url))
        except TimeoutException:
            current_url = self.driver.current_url
            self.fail(f"URL mismatch: Expected '{expected_url}', but got '{current_url}'")

        # Act - Input random username
        random_username = ''.join(random.choice(string.ascii_lowercase) for _ in range(7))
        try:
            username_input = WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(PasswordResetSelectors.USERNAME_INPUT)
            )
            username_input.clear()
            username_input.send_keys(random_username)
            username_input.send_keys(Keys.ENTER)
        except TimeoutException:
            self.fail("Failed to locate or fill the username input field.")

        # Assert - Verify error message
        try:
            error_message_element = WebDriverWait(self.driver, 10).until(
                EC.presence_of_element_located(PasswordResetSelectors.ERROR_MESSAGE)
            )
            error_message_text = error_message_element.text.strip()
            assert "Der Nutzername ist nicht vergeben oder es ist diesem keine E-Mail Adresse zugeordnet." in error_message_text, \
                f"Error message mismatch. Expected text not found. Got: '{error_message_text}'"
        except TimeoutException:
            self.fail("Error message not displayed after entering invalid username.")

    def test_rundmail(self):
        # Arrange
        self.driver.get(self.https_base_url)
        self.authentication_helper.admin_login()

        try:
            self.navigation_helper.navigate_to_email_to_all_users()
        except Exception as e:
            self.fail(f"Failed to navigate to 'Email to All Users' page: {e}")

        try:
            # Act
            # Input subject
            self.utils.fill_text_field(EmailSelectors.SUBJECT_INPUT, "[MoPat3] Selenium Test")

            # Input email content
            email_content = (
                "Sehr geehrte Damen und Herren,\n\n"
                "diese E-Mail dient dem Test der Rundmailfunktion von MoPat3. "
                "Diese E-Mail sollte an Nutzer mit identischer E-Mail Adresse nur genau einmal versandt werden. "
                "Zusätzlich wird diese E-Mail an die konfigurierte Absenderadresse verschickt und sollte auch an diese Adresse nur genau einmal versandt werden.\n\n"
                "Viele Grüße,\n\nMoPat3"
            )
            self.utils.fill_text_field(EmailSelectors.CONTENT_INPUT, email_content)

            # Assert / Confirm the email sending
            self.utils.click_element(EmailSelectors.SEND_BUTTON)
        except TimeoutException as e:
            self.fail(f"Test failed due to timeout: {e}")
        except Exception as e:
            self.fail(f"Unexpected error occurred: {e}")
        finally:
            # Logout to clean up
            self.authentication_helper.logout()

    def test_questionnaire_create_all_types(self):
        # Arrange
        questionnaire_name = f"Fragebogen alle Typen - Conditions - Scores - Bundles - Clinics {datetime.datetime.now()}"
        description = questionnaire_name
        language_code = "de_DE"
        localized_display_name = questionnaire_name
        self.driver.get(self.https_base_url)
        self.authentication_helper.admin_login()

        questionnaire_id = None
        try:
            # Act
            # Navigate to 'Manage Questionnaires', add a new questionnaire, and fill in its name
            self.navigation_helper.navigate_to_manage_questionnaires()
            self.questionnaire_helper.click_add_questionnaire_button()
            self.questionnaire_helper.fill_questionnaire_details(questionnaire_name, description, language_code,
                                                                 localized_display_name)

            # Save the questionnaire and extract its ID
            self.questionnaire_helper.save_questionnaire_edit_question()
            WebDriverWait(self.driver, 15).until(EC.url_contains("id="))
            current_url = self.driver.current_url
            questionnaire_id = current_url.split("id=")[1]

            # Add all question types to the questionnaire
            for add_question_method in self.question_helper.QUESTION_TYPES:
                try:
                    self.questionnaire_helper.click_add_question_button()
                    add_question_method()
                    self.question_helper.save_question()
                except TimeoutException:
                    self.fail(f"Failed to add a question of type {add_question_method.__name__}.")

            # Assert
            # Verify the correct number of questions were added
            WebDriverWait(self.driver, 30).until(
                EC.presence_of_element_located(QuestionSelectors.TABLE_QUESTION)
            )
            rows = self.utils.get_visible_table_rows(*QuestionSelectors.TABLE_QUESTION)
            expected_count = len(self.question_helper.QUESTION_TYPES)
            assert len(rows) == expected_count, f"Expected {expected_count} questions, found {len(rows)}."

        finally:
            # Cleanup
            if questionnaire_id:
                self.utils.search_and_delete_item(questionnaire_name, questionnaire_id, "questionnaire")
            self.authentication_helper.logout()

    def test_questionnaire_create_all_types_with_error(self):
        # Arrange
        questionnaire_id = None
        questionnaire_name = f"Fragebogen alle Typen Selenium Test Mit Fehlertest {datetime.datetime.now()}"
        questionnaire_description = "Dieser Fragebogen enthält alle bisher implementierten Fragetypen"
        language_code = "de_DE"
        questionnaire_display_name = questionnaire_name

        # Login as Admin
        self.driver.get(self.https_base_url)
        self.authentication_helper.admin_login()

        try:
            # Act
            # Create Questionnaire
            self.navigation_helper.navigate_to_manage_questionnaires()
            self.questionnaire_helper.click_add_questionnaire_button()
            self.questionnaire_helper.fill_questionnaire_details(
                questionnaire_name, questionnaire_description, language_code, questionnaire_display_name)
            questionnaire_id = self.questionnaire_helper.save_questionnaire_edit_question()

            # Define question-adding methods with error cases
            question_methods = [
                self.questionnaire_error_helper.add_question_info_text,
                self.questionnaire_error_helper.add_question_multiple_choice,
                self.questionnaire_error_helper.add_numeric_checkbox_with_failed_cases,
                self.questionnaire_error_helper.add_question_slider_question,
                self.questionnaire_error_helper.add_question_numeric_checkbox,
                self.questionnaire_error_helper.add_question_numeric_checkbox_freetext,
                self.questionnaire_error_helper.add_question_date_question,
                self.questionnaire_error_helper.add_question_freetext,
            ]

            # Add Questions to questionnaire (all question types)
            for add_question_method in question_methods:
                try:
                    self.questionnaire_helper.click_add_question_button()
                    add_question_method()
                    self.question_helper.save_question()
                except Exception as e:
                    self.fail(f"Failed to add question using method {add_question_method.__name__}: {e}")

            # Assert
            # Verify the correct number of questions are added
            WebDriverWait(self.driver, 30).until(
                EC.visibility_of_element_located(QuestionSelectors.TABLE_QUESTION)
            )
            WebDriverWait(self.driver, 30).until(
                lambda driver: driver.execute_script("return document.readyState") == "complete"
            )
            rows = [
                row for row in self.driver.find_element(*QuestionSelectors.TABLE_QUESTION)
                .find_element(By.TAG_NAME, "tbody")
                .find_elements(By.TAG_NAME, "tr") if row.is_displayed()
            ]
            assert len(rows) == len(
                question_methods
            ), f"Expected {len(question_methods)} questions, found {len(rows)}."

        finally:
            # Cleanup
            if questionnaire_id:
                self.utils.search_and_delete_item(questionnaire_name, questionnaire_id, "questionnaire")
            self.authentication_helper.logout()

    # TODO [] Add assertions to survey execution (add to class SurveyAssertHelper)
    def test_complete_workflow(self):
        # Arrange
        timestamp = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")

        username_creator = "admin"
        username_survey_executor = "user"

        questionnaire_id = None
        questionnaire_name = f"Fragebogen alle Typen {timestamp}"
        questionnaire_description = "Dieser Fragebogen enthält alle bisher implementierten Fragetypen"
        questionnaire_language_code = "de_DE"
        questionnaire_welcome_text = 'Ein Willkommenstext für das Paket "Selenium-Paket". Nichts besonderes zu sehen. 2'
        questionnaire_final_text = 'Dieser Text wird am Ende des Fragebogens gezeigt.'
        questionnaire_display_name = questionnaire_name
        added_questions = []

        bundle_id = None
        bundle_name = f"Test Bundle {random.randint(1, 999999)}"

        clinic_id = None
        clinic_name = f"Test Klinik {timestamp}"
        clinic_description = "Diese Klinik enthält das Fragebogenpaket \'Fragebogenpaket Alle Typen CBSC\'"
        configurations = [
            {'selector': ClinicSelectors.CONFIG_REGISTER_PATIENT_DATA,
             'config_selector': SurveySelectors.RADIO_REGISTER},
            {'selector': ClinicSelectors.CONFIG_USE_PATIENT_DATA_LOOKUP, 'dropdown_value': "patient lookup 1",
             'config_selector': SurveySelectors.RADIO_SEARCH_HIS},
            {'selector': ClinicSelectors.CONFIG_USE_PSEUDONYMIZATION_SERVICE, 'dropdown_value': "pseudonymization 1",
             'config_selector': SurveySelectors.RADIO_PSEUDONYMIZATION}
        ]

        # Login as Admin
        self.driver.get(self.https_base_url)
        self.authentication_helper.admin_login(username=username_creator)

        try:
            # Act
            # Step 1: Create Questionnaire
            self.navigation_helper.navigate_to_manage_questionnaires()
            self.questionnaire_helper.click_add_questionnaire_button()
            self.questionnaire_helper.fill_questionnaire_details(questionnaire_name, questionnaire_description,
                                                                 questionnaire_language_code,
                                                                 questionnaire_display_name, questionnaire_welcome_text,
                                                                 questionnaire_final_text)
            questionnaire_id = self.questionnaire_helper.save_questionnaire_edit_question()
            questionnaires = [{"id": questionnaire_id, "name": questionnaire_name}]

            # Step 2: Add Questions to questionnaire (all question types)
            for add_question_method in self.question_helper.QUESTION_TYPES:
                self.questionnaire_helper.click_add_question_button()
                question_info = add_question_method()
                added_questions.append(question_info)
                self.question_helper.save_question()

            # Assert
            # Verify the number of questions added
            self.questionnaire_error_helper.verify_number_of_questions(len(self.question_helper.QUESTION_TYPES))

            # Act
            # Step 3: Add Bundle
            self.navigation_helper.navigate_to_manage_bundles()
            bundle_id = None
            if not self.bundle_helper.bundle_exists(bundle_name):
                self.bundle_helper.create_bundle(bundle_name, True, questionnaires)
                bundle_id = self.bundle_helper.save_bundle(bundle_name)
            bundles = [{"id": bundle_id, "name": bundle_name}]

            # Step 4: Add Clinic
            self.navigation_helper.navigate_to_manage_clinics()
            self.clinic_helper.create_clinic(clinic_name, clinic_description, configurations, bundles,
                                             [username_creator, username_survey_executor])
            clinic_id = self.clinic_helper.save_clinic(clinic_name)

        finally:
            # Logout
            try:
                self.authentication_helper.logout()
            except Exception as e:
                print(f"Logout failed: {e}")

        # Step 5: Execute survey

        # Arrange
        case_number = "55755388"
        language_value = "de_DE"
        welcome_text = 'Ein Willkommenstext für das Paket "Selenium-Paket". Nichts besonderes zu sehen.'

        # Login as User
        self.driver.get(self.https_base_url)
        self.authentication_helper.user_login(username=username_survey_executor)

        try:
            # Act
            self.survey_helper.start_survey(configurations[0], clinic_name, case_number)
            self.survey_helper.proceed_to_bundle_selection(bundle_name, language_value)
            self.survey_helper.click_next_button()
            # Assert
            self.survey_assert_helper.assertion_for_welcome_text(welcome_text)
            # Act
            self.survey_helper.click_next_button()

            for question in added_questions:
                if question["type"] == QuestionSelectors.QuestionTypes.MULTIPLE_CHOICE:
                    self.survey_helper.answer_multiple_choice_question(question)
                elif question["type"] == QuestionSelectors.QuestionTypes.SLIDER:
                    self.survey_helper.answer_slider_question(question)
                elif question["type"] == QuestionSelectors.QuestionTypes.NUMBER_CHECKBOX:
                    self.survey_helper.answer_number_checkbox_question(question)
                elif question["type"] == QuestionSelectors.QuestionTypes.NUMBER_CHECKBOX_TEXT:
                    self.survey_helper.answer_number_checkbox_text_question(question)
                elif question["type"] == QuestionSelectors.QuestionTypes.FREE_TEXT:
                    self.survey_helper.answer_text_question()
                elif question["type"] == QuestionSelectors.QuestionTypes.DATE:
                    self.survey_helper.answer_date_question(question)
                elif question["type"] == QuestionSelectors.QuestionTypes.DROP_DOWN:
                    self.survey_helper.select_dropdown_option(question)
                elif question["type"] == QuestionSelectors.QuestionTypes.NUMBER_INPUT:
                    self.survey_helper.answer_numbered_input_question(question)

                self.survey_helper.click_next_button()

            # Complete the survey
            self.survey_helper.complete_survey()
            # End survey
            self.survey_helper.end_survey()

        finally:
            # Cleanup (teardown)
            # Logout
            try:
                self.authentication_helper.logout()
            except Exception as e:
                print(f"Logout failed: {e}")

    # TODO [] complete creation of the test 'test_TestCase_Questionnaire_Create_and_Remove_All_Types_with_Bundle_Clinic_Conditions'
    def test_case_questionnaire_Create_and_Remove_All_Types_with_Bundle_Clinic_Conditions(self):

        questionnaire_id = None
        questionnaire_name = ""
        bundle_id = None
        bundle_name = ""
        clinic_id = None
        clinic_name = ""

        try:
            pass
            # Step 1: Create Questionnaire
            # Step 2: Add Questions to questionnaire (all question types)
            # Step 3: Add Bundle
            # Step 4: Add Clinic
            # Step 5: Add Conditions
            # TODO [] implement methods
            # self.condition_helper.add_condition_to_questionnaire()
            # Step 6: Add Scores
            # TODO [] implement methods
            # self.score_helper.add_score_to_questionnaire()

        finally:
            # Cleanup (teardown): Logout and clean up created test data (questionnaire, bundle, clinic)
            # Delete clinic
            try:
                if clinic_id:
                    self.utils.search_and_delete_item(clinic_name, clinic_id, "clinic")
            except Exception as e:
                print(f"Cleanup failed for clinic '{clinic_name}': {e}")
            # Delete bundle
            try:
                if bundle_id:
                    self.utils.search_and_delete_item(bundle_name, bundle_id, "bundle")
            except Exception as e:
                print(f"Cleanup failed for bundle '{bundle_name}': {e}")
            # Delete questionnaire
            try:
                if questionnaire_id:
                    self.utils.search_and_delete_item(questionnaire_name, questionnaire_id, "questionnaire")
            except Exception as e:
                print(f"Cleanup failed for questionnaire '{questionnaire_name}': {e}")
            # Logout
            try:
                self.authentication_helper.logout()
            except Exception as e:
                print(f"Logout failed: {e}")

    # TODO [] complete creation of the test 'test_TestCase_Questionnaire_All_Types_with_Bundle_Clinic_Conditions_Create_and_Remove_Scores'
    def test_case_questionnaire_all_types_with_bundle_clinic_conditions_create_and_remove_scores(self):
        pass


    def tearDown(self):
        if self.driver:
            self.driver.quit()


if __name__ == "__main__":
    unittest.main()