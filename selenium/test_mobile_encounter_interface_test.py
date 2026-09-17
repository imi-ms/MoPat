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

from helper.Login import LoginHelper
from helper.Survey import SurveySelectors
from selenium import webdriver

loginHelper = LoginHelper()

from base_test import CustomChromeTest


class CustomTest(CustomChromeTest):
    def test_mobile_encounter_interface_test(self):
        # Arrange
        self.driver.get(self.https_base_url)
        self.authentication_helper.login(self.secret['admin-username'],
                                         self.secret['admin-password'])

        clinic = {}
        bundle = {}
        created_questionnaire = {}

        try:
            created_questionnaire = self.questionnaire_helper.create_questionnaire_with_questions()
        except Exception as e:
            self.fail(f"Failed to create questionnaire: {e}")

        try:
            self.navigation_helper.navigate_to_manage_bundles()
            bundle = self.bundle_helper.create_bundle(publish_bundle=True,
                                                      questionnaires=[
                                                          created_questionnaire])
            bundle["id"] = self.bundle_helper.save_bundle(bundle["name"])
        except Exception as e:
            self.fail(f"Failed to create bundle: {e}")

        try:
            self.navigation_helper.navigate_to_manage_clinics()
            clinic["name"] = self.clinic_helper.create_clinic(configurations=[{
                                                                                  'selector': (
                                                                                      By.CSS_SELECTOR,
                                                                                      '#registerPatientData > div > div > label')},
                                                                              {
                                                                                  'selector': (
                                                                                      By.CSS_SELECTOR,
                                                                                      '#usePseudonymizationService > div > div > label')},
                                                                              {
                                                                                  'selector': (
                                                                                      By.CSS_SELECTOR,
                                                                                      '#usePatientDataLookup > div > div > label')}],
                                                              bundles=[bundle],
                                                              users=[
                                                                  self.secret[
                                                                      'admin-username']])
            clinic["id"] = self.clinic_helper.save_clinic(clinic["name"])

        except Exception as e:
            self.fail(f"Failed to create clinic: {e}")

        self.navigation_helper.navigate_to_execute_survey()

        self.utils.check_visibility_of_element(
            SurveySelectors.BUTTON_ADDITIONAL_INFORMATION,
            "Additional Information Button not found")
        self.utils.check_visibility_of_element(
            SurveySelectors.DROPDOWN_LANGUAGE_SELECTOR,
            "Language selector not found")

        self.utils.check_visibility_of_element(
            SurveySelectors.TAB_PATIENT_REGISTRATION,
            "Patient Registration tab not found")
        self.utils.check_visibility_of_element(
            SurveySelectors.TAB_PATIENT_DATA_AUTOMATION,
            "Patient Data Automation tab not found")
        self.utils.check_visibility_of_element(
            SurveySelectors.TAB_PATIENT_PSEUDONYMIZATION,
            "Patient Pseudonymization tab not found")

        self.utils.click_element(SurveySelectors.TAB_PATIENT_REGISTRATION)
        self.utils.check_visibility_of_element(
            SurveySelectors.BUTTON_CHECK_CASE_NUMBER,
            "Check Case Number Button not found")
        button_text = self.driver.find_element(
            *SurveySelectors.BUTTON_CHECK_CASE_NUMBER).text

        self.utils.click_element(SurveySelectors.TAB_PATIENT_DATA_AUTOMATION)
        self.utils.check_visibility_of_element(
            SurveySelectors.BUTTON_CHECK_CASE_NUMBER,
            "Check Case Number Button not found in Patient Data Automation tab")
        assert self.driver.find_element(
            *SurveySelectors.BUTTON_CHECK_CASE_NUMBER).text != button_text, "Button text changed in Patient Data Automation tab"

        self.utils.click_element(SurveySelectors.TAB_PATIENT_PSEUDONYMIZATION)
        self.utils.check_visibility_of_element(
            SurveySelectors.BUTTON_CHECK_CASE_NUMBER,
            "Check Case Number Button not found in Patient Pseudonymization tab")
        assert self.driver.find_element(
            *SurveySelectors.BUTTON_CHECK_CASE_NUMBER).text != button_text, "Button text changed in Patient Pseudonymization tab"

        self.utils.click_element(SurveySelectors.TAB_PATIENT_REGISTRATION)

        self.survey_helper.start_survey(clinic_name=clinic["name"])

        self.survey_helper.proceed_to_bundle_selection(
            bundle_name=bundle["name"])

        self.survey_helper.click_next_button()

        self.utils.check_visibility_of_element(
            SurveySelectors.TEXT_QUESTIONNAIRE_TITLE,
            "Questionnaire title not found")
        self.survey_helper.click_next_button()

        self.utils.click_element(SurveySelectors.BUTTON_ADDITIONAL_INFORMATION)
        self.utils.click_element(SurveySelectors.BUTTON_HELP)

        self.utils.check_visibility_of_element(SurveySelectors.BLOCK_HELP_MODE,
                                               "Help mode next button not found")

        self.utils.click_element(SurveySelectors.BUTTON_ADDITIONAL_INFORMATION)

        self.utils.check_visibility_of_element(
            SurveySelectors.BLOCK_PROGRESS_BAR, "Progress bar not found")
        self.utils.check_visibility_of_element(SurveySelectors.BUTTON_FONT_SIZE,
                                               "Font size button not found")
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

        self.utils.check_visibility_of_element(
            SurveySelectors.TEXT_BUNDLE_FINAL_INFO,
            "Bundle final info not found")

        self.survey_helper.end_survey()

        self.authentication_helper.logout()


if __name__ == "__main__":
    unittest.main(verbosity=2)
