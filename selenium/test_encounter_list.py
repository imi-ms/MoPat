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
    def test_encounter_list(self):
        created_questionnaire = {}
        bundle = {}
        clinic = {}

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

        # Act
        self.navigation_helper.navigate_to_manage_surveys()

        self.utils.check_visibility_of_element(
            EncounterSelectors.BUTTON_ENCOUNTER_TABLE,
            "Encounter Table button not found")
        self.utils.check_visibility_of_element(
            EncounterSelectors.BUTTON_ENCOUNTER_SCHEDULE_TABLE,
            "Encounter Schedule Table button not found")

        # Act - Click on "All Encounters" tab
        self.utils.click_element(EncounterSelectors.BUTTON_ENCOUNTER_TABLE)
        self.utils.check_visibility_of_element(
            EncounterSelectors.TABLE_ALL_ENCOUNTERS,
            "All Encounters table not found")

        self.utils.check_visibility_of_element(
            EncounterSelectors.PAGINATION_ENCOUNTER_TABLE,
            "Pagination for All Encounters table not found")
        self.utils.check_visibility_of_element(
            EncounterSelectors.SEARCH_ALL_ENCOUNTERS,
            "Search for All Encounters table not found")

        # TODO: Action column, number of exports [after create survey function implementation]

        self.utils.check_visibility_of_element(
            EncounterSelectors.BUTTON_EXECUTE_ENCOUNTER,
            "Execute Encounter button not found")

        # Act - Click on "Scheduled Encounters" tab
        self.utils.click_element(
            EncounterSelectors.BUTTON_ENCOUNTER_SCHEDULE_TABLE)
        self.utils.check_visibility_of_element(
            EncounterSelectors.TABLE_SCHEDULED_ENCOUNTERS,
            "Scheduled Encounters table not found")
        self.utils.check_visibility_of_element(
            EncounterSelectors.PAGINATION_ENCOUNTER_SCHEDULE_TABLE,
            "Pagination for Scheduled Encounters table not found")

        self.utils.check_visibility_of_element(
            EncounterSelectors.SEARCH_SCHEDULED_ENCOUNTERS,
            "Search for Scheduled Encounters table not found")

        encounter_id = None
        try:
            self.utils.click_element(
                EncounterSelectors.BUTTON_SCHEDULE_ENCOUNTER)
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

        self.utils.click_element(
            EncounterSelectors.BUTTON_ENCOUNTER_SCHEDULE_TABLE)

        self.utils.check_presence_of_element(
            EncounterSelectors.TABLE_ENCOUNTER_SCHEDULED_ACTION_COLUMN,
            "Action column for Scheduled Encounters table not found")

        # TODO: number of exports [after survey schedule function implementation]

        # Assert - Check for button for scheduling an encounter
        try:
            WebDriverWait(self.driver, 10).until(
                EC.element_to_be_clickable(
                    EncounterSelectors.BUTTON_SCHEDULE_ENCOUNTER)
            )
        except Exception as e:
            self.fail("Schedule Encounter button not found")

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


if __name__ == "__main__":
    unittest.main(verbosity=2)
