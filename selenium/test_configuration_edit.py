#!/usr/bin/env python3

import datetime
import json
import os
import re
import sys
import time
import traceback
import unittest
import unittest
from abc import ABC, abstractmethod
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait
from time import gmtime, strftime

from helper.Configuration import ConfigurationSelectors
from helper.Login import LoginHelper
from selenium import webdriver

loginHelper = LoginHelper()

from base_test import CustomChromeTest


class CustomTest(CustomChromeTest):
    def test_configuration_edit(self):
        # Arrange
        self.driver.get(self.https_base_url)
        self.authentication_helper.login(self.secret['admin-username'],
                                         self.secret['admin-password'])

        # Act
        self.navigation_helper.navigate_to_configuration()

        self.utils.check_visibility_of_element(
            ConfigurationSelectors.SELECT_LANGUAGE, "Select Language not found")
        self.utils.check_visibility_of_element(
            ConfigurationSelectors.INPUT_CASE_NUMBER_TYPE,
            "Case Number Type input not found")
        self.utils.check_visibility_of_element(
            ConfigurationSelectors.INPUT_STORAGE_PATH_FOR_UPLOADS,
            "Storage Path for Uploads input not found")
        self.utils.check_visibility_of_element(
            ConfigurationSelectors.INPUT_BASE_URL, "Base URL input not found")
        self.utils.fill_text_field(
            ConfigurationSelectors.INPUT_PATH_UPLOAD_IMAGES,
            self.configuration_helper.DEFAUL_EXPORT_IMAGE_UPLOAD_PATH)
        self.utils.check_visibility_of_element(
            ConfigurationSelectors.INPUT_PATH_MAIN_DIRECTORY,
            "Path for Main Directory input not found")
        self.utils.toggle_checkbox(ConfigurationSelectors.CHECKBOX_AD_AUTH)
        self.utils.check_visibility_of_element(
            ConfigurationSelectors.INPUT_URL_AD, "URL AD input not found")
        self.utils.check_visibility_of_element(
            ConfigurationSelectors.INPUT_DOMAIN_AD, "Domain AD input not found")
        self.utils.check_visibility_of_element(
            ConfigurationSelectors.SELECT_DEFAULT_LANGUAGE_AD,
            "Default Language AD select not found")
        self.utils.check_visibility_of_element(
            ConfigurationSelectors.INPUT_PHONE_NUMBER_AD,
            "Phone Number AD input not found")
        self.utils.toggle_checkbox(
            ConfigurationSelectors.CHECKBOX_PATIENT_LOOKUP)
        self.utils.check_visibility_of_element(
            ConfigurationSelectors.SELECT_PATIENT_LOOKUP_IMPLEMENTATION,
            "Patient Lookup Implementation select not found")
        self.utils.check_visibility_of_element(
            ConfigurationSelectors.INPUT_HOST_PATIENT_LOOKUP,
            "Host Patient Lookup input not found")
        self.utils.fill_text_field(
            ConfigurationSelectors.INPUT_PORT_PATIENT_LOOKUP,
            self.configuration_helper.DEFAULT_PORT_HL7_PATIENT_LOOKUP)
        self.utils.check_visibility_of_element(
            ConfigurationSelectors.INPUT_PORT_PATIENT_LOOKUP,
            "Port Patient Lookup input not found")
        self.utils.check_visibility_of_element(
            ConfigurationSelectors.INPUT_PSEUDONYMIZATION_URL,
            "Pseudonymization URL input not found")
        self.utils.check_visibility_of_element(
            ConfigurationSelectors.INPUT_PSEUDONYMIZATION_API_KEY,
            "Pseudonymization API Key input not found")
        self.utils.check_visibility_of_element(ConfigurationSelectors.INPUT_OID,
                                               "OID input not found")
        self.utils.check_visibility_of_element(
            ConfigurationSelectors.INPUT_URL_ODM_TO_PDF,
            "URL ODM to PDF input not found")
        self.utils.check_visibility_of_element(
            ConfigurationSelectors.INPUT_SYSTEM_URI_FOR_FHIR,
            "System URI for FHIR input not found")
        self.utils.toggle_checkbox(
            ConfigurationSelectors.CHECKBOX_EXPORT_HL7_INTO_DIRECTORY)
        self.utils.check_visibility_of_element(
            ConfigurationSelectors.INPUT_EXPORT_PATH_HL7_DIRECTORY,
            "Export Path HL7 Directory input not found")
        self.utils.toggle_checkbox(
            ConfigurationSelectors.CHECKBOX_EXPORT_HL7_VIA_SERVER)
        self.utils.check_visibility_of_element(
            ConfigurationSelectors.INPUT_HL7_HOST, "HL7 Host input not found")
        self.utils.check_visibility_of_element(
            ConfigurationSelectors.INPUT_HL7_PORT, "HL7 Port input not found")
        self.utils.check_visibility_of_element(
            ConfigurationSelectors.INPUT_SENDING_FACILITY,
            "Sending Facility input not found")
        self.utils.check_visibility_of_element(
            ConfigurationSelectors.INPUT_RECEIVING_APPLICATION,
            "Receiving Application input not found")
        self.utils.check_visibility_of_element(
            ConfigurationSelectors.INPUT_RECEIVING_FACILITY,
            "Receiving Facility input not found")
        self.utils.check_visibility_of_element(
            ConfigurationSelectors.INPUT_FILE_ORDER_NUMBER,
            "File Order Number input not found")
        self.utils.toggle_checkbox(
            ConfigurationSelectors.CHECKBOX_ENCRYPT_MESSAGE_TLS)
        self.utils.check_visibility_of_element(
            ConfigurationSelectors.FILE_PATH_CERTIFICATE,
            "File Path Certificate input not found")
        self.utils.toggle_checkbox(
            ConfigurationSelectors.CHECKBOX_USE_CERTIFICATE)
        self.utils.check_visibility_of_element(
            ConfigurationSelectors.FILE_PKCS_ARCHIVE,
            "File PKCS Archive input not found")
        self.utils.check_visibility_of_element(
            ConfigurationSelectors.INPUT_PASSWORD_PKCS_ARCHIVE,
            "Password PKCS Archive input not found")
        self.utils.toggle_checkbox(
            ConfigurationSelectors.CHECKBOX_EXPORT_ODM_INTO_DIRECTORY)
        self.utils.check_visibility_of_element(
            ConfigurationSelectors.INPUT_EXPORT_PATH_ODM_DIRECTORY,
            "Export Path ODM Directory input not found")
        self.utils.toggle_checkbox(
            ConfigurationSelectors.CHECKBOX_EXPORT_ODM_VIA_REST)
        self.utils.check_visibility_of_element(
            ConfigurationSelectors.INPUT_URL_REST_INTERFACE,
            "URL REST Interface input not found")
        self.utils.toggle_checkbox(
            ConfigurationSelectors.CHECKBOX_EXPORT_ODM_VIA_HL7)
        self.utils.check_visibility_of_element(
            ConfigurationSelectors.INPUT_ODM_HL7_HOST,
            "ODM HL7 Host input not found")
        self.utils.check_visibility_of_element(
            ConfigurationSelectors.INPUT_ODM_HL7_PORT,
            "ODM HL7 Port input not found")
        self.utils.check_visibility_of_element(
            ConfigurationSelectors.INPUT_ODM_SENDING_FACILITY,
            "ODM Sending Facility input not found")
        self.utils.check_visibility_of_element(
            ConfigurationSelectors.INPUT_ODM_RECEIVING_APPLICATION,
            "ODM Receiving Application input not found")
        self.utils.check_visibility_of_element(
            ConfigurationSelectors.INPUT_ODM_RECEIVING_FACILITY,
            "ODM Receiving Facility input not found")
        self.utils.check_visibility_of_element(
            ConfigurationSelectors.INPUT_ODM_FILE_ORDER_NUMBER,
            "ODM File Order Number input not found")
        self.utils.toggle_checkbox(
            ConfigurationSelectors.CHECKBOX_EXPORT_FHIR_INTO_DIRECTORY)
        self.utils.check_visibility_of_element(
            ConfigurationSelectors.INPUT_EXPORT_PATH_FHIR_DIRECTORY,
            "Export Path FHIR Directory input not found")
        self.utils.toggle_checkbox(
            ConfigurationSelectors.CHECKBOX_EXPORT_FHIR_INTO_COMMUNICATION_SERVER)
        self.utils.check_visibility_of_element(
            ConfigurationSelectors.INPUT_FHIR_HOST, "FHIR Host input not found")
        self.utils.toggle_checkbox(
            ConfigurationSelectors.CHECKBOX_EXPORT_REDCAP_INTO_DIRECTORY)
        self.utils.check_visibility_of_element(
            ConfigurationSelectors.INPUT_EXPORT_PATH_REDCAP,
            "Export Path REDCap input not found")
        self.utils.toggle_checkbox(
            ConfigurationSelectors.CHECKBOX_EXPORT_REDCAP_VIA_REST)
        self.utils.check_visibility_of_element(
            ConfigurationSelectors.INPUT_URL_REDCAP,
            "URL REDCap input not found")
        self.utils.check_visibility_of_element(
            ConfigurationSelectors.INPUT_TOKEN_REDCAP,
            "Token REDCap input not found")
        self.utils.check_visibility_of_element(
            ConfigurationSelectors.INPUT_TIME_DELETE_INCOMPLETE_ENCOUNTER,
            "Time Delete Incomplete Encounter input not found")
        self.utils.check_visibility_of_element(
            ConfigurationSelectors.INPUT_TIME_DELETE_INCOMPLETE_ENCOUNTER_AND_NOT_SENT_QUESTIONNAIRE,
            "Time Delete Incomplete Encounter and Not Sent Questionnaire input not found")
        self.utils.check_visibility_of_element(
            ConfigurationSelectors.INPUT_TIME_DELETE_EMAIL_COMPLETED_ENCOUNTER,
            "Time Delete Email Completed Encounter input not found")
        self.utils.check_visibility_of_element(
            ConfigurationSelectors.INPUT_TIME_DELETE_INCOMPLETE_SCHEDULED_ENCOUNTERS,
            "Time Delete Incomplete Scheduled Encounters input not found")
        self.utils.check_visibility_of_element(
            ConfigurationSelectors.INPUT_TIME_DELETE_INCOMPLETE_SCHEDULED_ENCOUNTER_AND_NOT_SENT_QUESTIONNAIRE,
            "Time Delete Incomplete Scheduled Encounter and Not Sent Questionnaire input not found")
        self.utils.toggle_checkbox(
            ConfigurationSelectors.CHECKBOX_UTILIZE_MAILER)
        self.utils.check_visibility_of_element(
            ConfigurationSelectors.INPUT_MAIL_HOST, "Mail Host input not found")
        self.utils.check_visibility_of_element(
            ConfigurationSelectors.INPUT_MAIL_PORT, "Mail Port input not found")
        self.utils.toggle_checkbox(ConfigurationSelectors.CHECKBOX_ENABLE_TLS)
        self.utils.toggle_checkbox(
            ConfigurationSelectors.CHECKBOX_SMTP_AUTHENTICATION)
        self.utils.check_visibility_of_element(
            ConfigurationSelectors.INPUT_USERNAME_MAILER,
            "Username Mailer input not found")
        self.utils.check_visibility_of_element(
            ConfigurationSelectors.INPUT_PASSWORD_MAILER,
            "Password Mailer input not found")
        self.utils.check_visibility_of_element(
            ConfigurationSelectors.INPUT_SENDER_MAILER,
            "Sender Mailer input not found")
        self.utils.check_visibility_of_element(
            ConfigurationSelectors.INPUT_EMAIL_ADDRESS_MAILER,
            "Email Address Mailer input not found")
        self.utils.check_visibility_of_element(
            ConfigurationSelectors.INPUT_PHONE_MAILER,
            "Phone Mailer input not found")
        self.utils.fill_text_field(ConfigurationSelectors.INPUT_MAIL_SUPPORT,
                                   self.configuration_helper.DEFAULT_MAIL_SUPPORT)
        self.utils.check_visibility_of_element(
            ConfigurationSelectors.INPUT_MAIL_SUPPORT,
            "Mail Support input not found")
        self.utils.check_visibility_of_element(
            ConfigurationSelectors.INPUT_PHONE_SUPPORT,
            "Phone Support input not found")

        # Purposely set wrong field to check validation
        self.utils.fill_text_field(ConfigurationSelectors.INPUT_MAIL_SUPPORT,
                                   "abc")

        self.configuration_helper.save_configuration()

        errorDivs = WebDriverWait(self.driver, 10).until(
            EC.presence_of_all_elements_located((By.CLASS_NAME, "config_error"))
        )

        self.driver.execute_script(
            "arguments[0].scrollIntoView({block:'center', inline:'nearest'});",
            errorDivs[0]
        )

        error_count = len(errorDivs)
        # Expect at least one error with the provided config
        assert error_count > 0, "Validation is not working for the configuration elements."

        self.utils.toggle_checkbox(ConfigurationSelectors.CHECKBOX_AD_AUTH,
                                   False)
        self.utils.toggle_checkbox(
            ConfigurationSelectors.CHECKBOX_PATIENT_LOOKUP, False)
        self.utils.toggle_checkbox(
            ConfigurationSelectors.CHECKBOX_EXPORT_HL7_INTO_DIRECTORY, False)
        self.utils.toggle_checkbox(
            ConfigurationSelectors.CHECKBOX_EXPORT_HL7_VIA_SERVER, False)
        self.utils.toggle_checkbox(
            ConfigurationSelectors.CHECKBOX_ENCRYPT_MESSAGE_TLS, False)
        self.utils.toggle_checkbox(
            ConfigurationSelectors.CHECKBOX_USE_CERTIFICATE, False)
        self.utils.toggle_checkbox(
            ConfigurationSelectors.CHECKBOX_EXPORT_ODM_INTO_DIRECTORY, False)
        self.utils.toggle_checkbox(
            ConfigurationSelectors.CHECKBOX_EXPORT_ODM_VIA_REST, False)
        self.utils.toggle_checkbox(
            ConfigurationSelectors.CHECKBOX_EXPORT_ODM_VIA_HL7, False)
        self.utils.toggle_checkbox(
            ConfigurationSelectors.CHECKBOX_EXPORT_FHIR_INTO_DIRECTORY, False)
        self.utils.toggle_checkbox(
            ConfigurationSelectors.CHECKBOX_EXPORT_FHIR_INTO_COMMUNICATION_SERVER,
            False)
        self.utils.toggle_checkbox(
            ConfigurationSelectors.CHECKBOX_EXPORT_REDCAP_INTO_DIRECTORY, False)
        self.utils.toggle_checkbox(
            ConfigurationSelectors.CHECKBOX_EXPORT_REDCAP_VIA_REST, False)
        self.utils.toggle_checkbox(
            ConfigurationSelectors.CHECKBOX_SMTP_AUTHENTICATION, False)
        self.utils.toggle_checkbox(ConfigurationSelectors.CHECKBOX_ENABLE_TLS,
                                   False)
        self.utils.toggle_checkbox(
            ConfigurationSelectors.CHECKBOX_UTILIZE_MAILER, False)

        self.configuration_helper.add_additional_logo()

        # TODO: Cannot be saved right now, as it can't be ensured that config works on every server
        # self.configuration_helper.save_configuration()
        # self.utils.scroll_to_bottom()
        # self.utils.check_visibility_of_element(ConfigurationSelectors.IMAGE_ADDITIONAL_LOGO, "Additional Logo not found")
        self.authentication_helper.logout()


if __name__ == "__main__":
    unittest.main(verbosity=2)
