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
from helper.Survey import SurveyHelper, SurveyAssertHelper
from helper.Configuration import ConfigurationSelectors, ConfigurationHelper
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
        self.configuration_helper = ConfigurationHelper(self.driver, self.navigation_helper)

    def test_configuration_edit(self):
        # Arrange
        self.driver.get(self.https_base_url)
        self.authentication_helper.login(self.secret['admin-username'], self.secret['admin-password'])

        # Act        
        self.navigation_helper.navigate_to_configuration()

        self.utils.check_visibility_of_element(ConfigurationSelectors.SELECT_LANGUAGE, "Select Language not found")
        self.utils.check_visibility_of_element(ConfigurationSelectors.INPUT_CASE_NUMBER_TYPE, "Case Number Type input not found")
        self.utils.check_visibility_of_element(ConfigurationSelectors.INPUT_STORAGE_PATH_FOR_UPLOADS, "Storage Path for Uploads input not found")
        self.utils.check_visibility_of_element(ConfigurationSelectors.INPUT_BASE_URL, "Base URL input not found")
        self.utils.fill_text_field(ConfigurationSelectors.INPUT_PATH_UPLOAD_IMAGES, self.configuration_helper.DEFAUL_EXPORT_IMAGE_UPLOAD_PATH)
        self.utils.check_visibility_of_element(ConfigurationSelectors.INPUT_PATH_MAIN_DIRECTORY, "Path for Main Directory input not found")
        self.utils.toggle_checkbox(ConfigurationSelectors.CHECKBOX_AD_AUTH)
        self.utils.check_visibility_of_element(ConfigurationSelectors.INPUT_URL_AD, "URL AD input not found")
        self.utils.check_visibility_of_element(ConfigurationSelectors.INPUT_DOMAIN_AD, "Domain AD input not found")
        self.utils.check_visibility_of_element(ConfigurationSelectors.SELECT_DEFAULT_LANGUAGE_AD, "Default Language AD select not found")
        self.utils.check_visibility_of_element(ConfigurationSelectors.INPUT_PHONE_NUMBER_AD, "Phone Number AD input not found")
        self.utils.toggle_checkbox(ConfigurationSelectors.CHECKBOX_PATIENT_LOOKUP)
        self.utils.check_visibility_of_element(ConfigurationSelectors.SELECT_PATIENT_LOOKUP_IMPLEMENTATION, "Patient Lookup Implementation select not found")
        self.utils.check_visibility_of_element(ConfigurationSelectors.INPUT_HOST_PATIENT_LOOKUP, "Host Patient Lookup input not found")
        self.utils.fill_text_field(ConfigurationSelectors.INPUT_PORT_PATIENT_LOOKUP, self.configuration_helper.DEFAULT_PORT_HL7_PATIENT_LOOKUP)
        self.utils.check_visibility_of_element(ConfigurationSelectors.INPUT_PORT_PATIENT_LOOKUP, "Port Patient Lookup input not found")
        self.utils.check_visibility_of_element(ConfigurationSelectors.INPUT_PSEUDONYMIZATION_URL, "Pseudonymization URL input not found")
        self.utils.check_visibility_of_element(ConfigurationSelectors.INPUT_PSEUDONYMIZATION_API_KEY, "Pseudonymization API Key input not found")
        self.utils.check_visibility_of_element(ConfigurationSelectors.INPUT_OID, "OID input not found")
        self.utils.check_visibility_of_element(ConfigurationSelectors.INPUT_URL_ODM_TO_PDF, "URL ODM to PDF input not found")
        self.utils.check_visibility_of_element(ConfigurationSelectors.INPUT_SYSTEM_URI_FOR_FHIR, "System URI for FHIR input not found")
        self.utils.fill_text_field(ConfigurationSelectors.INPUT_EXPORT_PATH_ORBIS, self.configuration_helper.DEFAULT_EXPORT_PATH)
        self.utils.toggle_checkbox(ConfigurationSelectors.CHECKBOX_EXPORT_HL7_INTO_DIRECTORY)
        self.utils.check_visibility_of_element(ConfigurationSelectors.INPUT_EXPORT_PATH_HL7_DIRECTORY, "Export Path HL7 Directory input not found")
        self.utils.toggle_checkbox(ConfigurationSelectors.CHECKBOX_EXPORT_HL7_VIA_SERVER)
        self.utils.check_visibility_of_element(ConfigurationSelectors.INPUT_HL7_HOST, "HL7 Host input not found")
        self.utils.check_visibility_of_element(ConfigurationSelectors.INPUT_HL7_PORT, "HL7 Port input not found")
        self.utils.check_visibility_of_element(ConfigurationSelectors.INPUT_SENDING_FACILITY, "Sending Facility input not found")
        self.utils.check_visibility_of_element(ConfigurationSelectors.INPUT_RECEIVING_APPLICATION, "Receiving Application input not found")
        self.utils.check_visibility_of_element(ConfigurationSelectors.INPUT_RECEIVING_FACILITY, "Receiving Facility input not found")
        self.utils.check_visibility_of_element(ConfigurationSelectors.INPUT_FILE_ORDER_NUMBER, "File Order Number input not found")
        self.utils.toggle_checkbox(ConfigurationSelectors.CHECKBOX_ENCRYPT_MESSAGE_TLS)
        self.utils.check_visibility_of_element(ConfigurationSelectors.FILE_PATH_CERTIFICATE, "File Path Certificate input not found")
        self.utils.toggle_checkbox(ConfigurationSelectors.CHECKBOX_USE_CERTIFICATE)
        self.utils.check_visibility_of_element(ConfigurationSelectors.FILE_PKCS_ARCHIVE, "File PKCS Archive input not found")
        self.utils.check_visibility_of_element(ConfigurationSelectors.INPUT_PASSWORD_PKCS_ARCHIVE, "Password PKCS Archive input not found")
        self.utils.toggle_checkbox(ConfigurationSelectors.CHECKBOX_EXPORT_ODM_INTO_DIRECTORY)
        self.utils.check_visibility_of_element(ConfigurationSelectors.INPUT_EXPORT_PATH_ODM_DIRECTORY, "Export Path ODM Directory input not found")
        self.utils.toggle_checkbox(ConfigurationSelectors.CHECKBOX_EXPORT_ODM_VIA_REST)
        self.utils.check_visibility_of_element(ConfigurationSelectors.INPUT_URL_REST_INTERFACE, "URL REST Interface input not found")
        self.utils.toggle_checkbox(ConfigurationSelectors.CHECKBOX_EXPORT_ODM_VIA_HL7)
        self.utils.check_visibility_of_element(ConfigurationSelectors.INPUT_ODM_HL7_HOST, "ODM HL7 Host input not found")
        self.utils.check_visibility_of_element(ConfigurationSelectors.INPUT_ODM_HL7_PORT, "ODM HL7 Port input not found")
        self.utils.check_visibility_of_element(ConfigurationSelectors.INPUT_ODM_SENDING_FACILITY, "ODM Sending Facility input not found")
        self.utils.check_visibility_of_element(ConfigurationSelectors.INPUT_ODM_RECEIVING_APPLICATION, "ODM Receiving Application input not found")
        self.utils.check_visibility_of_element(ConfigurationSelectors.INPUT_ODM_RECEIVING_FACILITY, "ODM Receiving Facility input not found")
        self.utils.check_visibility_of_element(ConfigurationSelectors.INPUT_ODM_FILE_ORDER_NUMBER, "ODM File Order Number input not found")
        self.utils.toggle_checkbox(ConfigurationSelectors.CHECKBOX_EXPORT_FHIR_INTO_DIRECTORY)
        self.utils.check_visibility_of_element(ConfigurationSelectors.INPUT_EXPORT_PATH_FHIR_DIRECTORY, "Export Path FHIR Directory input not found")
        self.utils.toggle_checkbox(ConfigurationSelectors.CHECKBOX_EXPORT_FHIR_INTO_COMMUNICATION_SERVER)
        self.utils.check_visibility_of_element(ConfigurationSelectors.INPUT_FHIR_HOST, "FHIR Host input not found")
        self.utils.toggle_checkbox(ConfigurationSelectors.CHECKBOX_EXPORT_REDCAP_INTO_DIRECTORY)
        self.utils.check_visibility_of_element(ConfigurationSelectors.INPUT_EXPORT_PATH_REDCAP, "Export Path REDCap input not found")
        self.utils.toggle_checkbox(ConfigurationSelectors.CHECKBOX_EXPORT_REDCAP_VIA_REST)
        self.utils.check_visibility_of_element(ConfigurationSelectors.INPUT_URL_REDCAP, "URL REDCap input not found")
        self.utils.check_visibility_of_element(ConfigurationSelectors.INPUT_TOKEN_REDCAP, "Token REDCap input not found")
        self.utils.check_visibility_of_element(ConfigurationSelectors.INPUT_TIME_DELETE_INCOMPLETE_ENCOUNTER, "Time Delete Incomplete Encounter input not found")
        self.utils.check_visibility_of_element(ConfigurationSelectors.INPUT_TIME_DELETE_INCOMPLETE_ENCOUNTER_AND_NOT_SENT_QUESTIONNAIRE, "Time Delete Incomplete Encounter and Not Sent Questionnaire input not found")
        self.utils.check_visibility_of_element(ConfigurationSelectors.INPUT_TIME_DELETE_EMAIL_COMPLETED_ENCOUNTER, "Time Delete Email Completed Encounter input not found")
        self.utils.check_visibility_of_element(ConfigurationSelectors.INPUT_TIME_DELETE_INCOMPLETE_SCHEDULED_ENCOUNTERS, "Time Delete Incomplete Scheduled Encounters input not found")
        self.utils.check_visibility_of_element(ConfigurationSelectors.INPUT_TIME_DELETE_INCOMPLETE_SCHEDULED_ENCOUNTER_AND_NOT_SENT_QUESTIONNAIRE, "Time Delete Incomplete Scheduled Encounter and Not Sent Questionnaire input not found")
        self.utils.toggle_checkbox(ConfigurationSelectors.CHECKBOX_UTILIZE_MAILER)
        self.utils.check_visibility_of_element(ConfigurationSelectors.INPUT_MAIL_HOST, "Mail Host input not found")
        self.utils.check_visibility_of_element(ConfigurationSelectors.INPUT_MAIL_PORT, "Mail Port input not found")
        self.utils.toggle_checkbox(ConfigurationSelectors.CHECKBOX_ENABLE_TLS)
        self.utils.toggle_checkbox(ConfigurationSelectors.CHECKBOX_SMTP_AUTHENTICATION)
        self.utils.check_visibility_of_element(ConfigurationSelectors.INPUT_USERNAME_MAILER, "Username Mailer input not found")
        self.utils.check_visibility_of_element(ConfigurationSelectors.INPUT_PASSWORD_MAILER, "Password Mailer input not found")
        self.utils.check_visibility_of_element(ConfigurationSelectors.INPUT_SENDER_MAILER, "Sender Mailer input not found")
        self.utils.check_visibility_of_element(ConfigurationSelectors.INPUT_EMAIL_ADDRESS_MAILER, "Email Address Mailer input not found")
        self.utils.check_visibility_of_element(ConfigurationSelectors.INPUT_PHONE_MAILER, "Phone Mailer input not found")
        self.utils.fill_text_field(ConfigurationSelectors.INPUT_MAIL_SUPPORT, self.configuration_helper.DEFAULT_MAIL_SUPPORT)
        self.utils.check_visibility_of_element(ConfigurationSelectors.INPUT_MAIL_SUPPORT, "Mail Support input not found")
        self.utils.check_visibility_of_element(ConfigurationSelectors.INPUT_PHONE_SUPPORT, "Phone Support input not found")

        self.configuration_helper.save_configuration()

        # Count all divs with class config_error
        error_divs = self.driver.find_elements(By.CLASS_NAME, "config_error")
        error_count = len(error_divs)

        # Expect 7 and throw error if less
        expected_error_count = 7
        if error_count < expected_error_count:
            raise AssertionError(f"Expected at least {expected_error_count} divs with class 'config_error', but found {error_count}")
        self.utils.toggle_checkbox(ConfigurationSelectors.CHECKBOX_AD_AUTH, False)
        self.utils.toggle_checkbox(ConfigurationSelectors.CHECKBOX_PATIENT_LOOKUP, False)
        self.utils.toggle_checkbox(ConfigurationSelectors.CHECKBOX_EXPORT_HL7_INTO_DIRECTORY, False)
        self.utils.toggle_checkbox(ConfigurationSelectors.CHECKBOX_EXPORT_HL7_VIA_SERVER, False)
        self.utils.toggle_checkbox(ConfigurationSelectors.CHECKBOX_ENCRYPT_MESSAGE_TLS, False)
        self.utils.toggle_checkbox(ConfigurationSelectors.CHECKBOX_USE_CERTIFICATE, False)
        self.utils.toggle_checkbox(ConfigurationSelectors.CHECKBOX_EXPORT_ODM_INTO_DIRECTORY, False)
        self.utils.toggle_checkbox(ConfigurationSelectors.CHECKBOX_EXPORT_ODM_VIA_REST, False)
        self.utils.toggle_checkbox(ConfigurationSelectors.CHECKBOX_EXPORT_ODM_VIA_HL7, False)
        self.utils.toggle_checkbox(ConfigurationSelectors.CHECKBOX_EXPORT_FHIR_INTO_DIRECTORY, False)
        self.utils.toggle_checkbox(ConfigurationSelectors.CHECKBOX_EXPORT_FHIR_INTO_COMMUNICATION_SERVER, False)
        self.utils.toggle_checkbox(ConfigurationSelectors.CHECKBOX_EXPORT_REDCAP_INTO_DIRECTORY, False)
        self.utils.toggle_checkbox(ConfigurationSelectors.CHECKBOX_EXPORT_REDCAP_VIA_REST, False)
        self.utils.toggle_checkbox(ConfigurationSelectors.CHECKBOX_SMTP_AUTHENTICATION, False)
        self.utils.toggle_checkbox(ConfigurationSelectors.CHECKBOX_ENABLE_TLS, False)
        self.utils.toggle_checkbox(ConfigurationSelectors.CHECKBOX_UTILIZE_MAILER, False)

        self.configuration_helper.add_additional_logo()

        self.configuration_helper.save_configuration()
        self.utils.scroll_to_bottom()
        self.utils.check_visibility_of_element(ConfigurationSelectors.IMAGE_ADDITIONAL_LOGO, "Additional Logo not found")

    def tearDown(self):
        if self.driver:
            self.driver.quit()


if __name__ == "__main__":
    unittest.main()