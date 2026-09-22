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

from helper.Encounter import EncounterSelectors, EncounterScheduleType
from helper.Login import LoginHelper
from selenium import webdriver

loginHelper = LoginHelper()

from base_test import CustomChromeTest


class CustomTest(CustomChromeTest):
    def test_encounter_schedule(self):
        clinic = {}
        bundle = {}
        created_questionnaire = {}

        # Arrange
        self.driver.get(self.https_base_url)
        self.authentication_helper.login(self.secret['admin-username'],
                                         self.secret['admin-password'])

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
                                                                                      '#usePatientDataLookup > div:nth-child(1) > div:nth-child(3) > label:nth-child(1)')}],
                                                              bundles=[bundle])
            clinic["id"] = self.clinic_helper.save_clinic(clinic["name"])

        except Exception as e:
            self.fail(f"Failed to create clinic: {e}")

        try:
            self.navigation_helper.navigate_to_manage_surveys()
            self.utils.click_element(
                EncounterSelectors.BUTTON_ENCOUNTER_SCHEDULE_TABLE)
            self.utils.click_element(
                EncounterSelectors.BUTTON_SCHEDULE_ENCOUNTER)
        except Exception as e:
            self.fail(f"Failed to navigate to Schedule Encounter form: {e}")

        self.utils.check_visibility_of_element(
            EncounterSelectors.INPUT_SCHEDULE_CASE_NUMBER,
            "Case Number input not found")
        self.utils.check_visibility_of_element(
            EncounterSelectors.SELECT_SCHEDULE_CLINIC,
            "Clinic select not found")
        self.utils.check_visibility_of_element(
            EncounterSelectors.SELECT_SCHEDULE_BUNDLE,
            "Bundle select not found")
        self.utils.check_visibility_of_element(
            EncounterSelectors.INPUT_SCHEDULE_EMAIL, "Email input not found")
        self.utils.check_visibility_of_element(
            EncounterSelectors.SELECT_SURVEY_TYPE,
            "Survey Type select not found")
        self.utils.check_visibility_of_element(EncounterSelectors.INPUT_DATE,
                                               "Date input not found")
        self.utils.check_visibility_of_element(
            EncounterSelectors.INPUT_END_DATE, "End Date input not found")
        self.utils.check_visibility_of_element(
            EncounterSelectors.INPUT_TIME_PERIOD, "Time Period input not found")
        self.utils.check_visibility_of_element(
            EncounterSelectors.SELECT_LANGUAGE, "Language select not found")
        self.utils.check_visibility_of_element(
            EncounterSelectors.INPUT_PERSONAL_TEXT,
            "Personal Text input not found")

        encounter_id = None
        try:
            encounter_id = self.encounter_helper.schedule_encounter("123456",
                                                                    clinic[
                                                                        "name"],
                                                                    bundle[
                                                                        "name"],
                                                                    "test@email.com",
                                                                    EncounterScheduleType.UNIQUELY,
                                                                    (
                                                                            datetime.date.today() + datetime.timedelta(
                                                                            days=1)).strftime(
                                                                        "%Y-%m-%d"))
        except Exception as e:
            self.fail(f"Failed to schedule encounter: {e}")

        finally:
            self.encounter_helper.delete_scheduled_encounter(encounter_id,
                                                             "123456")
            self.utils.search_and_delete_item(clinic["name"], clinic["id"],
                                              "clinic")
            self.utils.search_and_delete_item(bundle["name"], bundle["id"],
                                              "bundle")
            self.utils.search_and_delete_item(created_questionnaire['name'],
                                              created_questionnaire['id'],
                                              "questionnaire")
            self.authentication_helper.logout()


if __name__ == "__main__":
    unittest.main(verbosity=2)
