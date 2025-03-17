#!/usr/bin/env python3
# -*- coding: utf-8 -*-

from time import gmtime, strftime
import datetime
import unittest
import json
import os
import traceback
import time
from abc import ABC, abstractmethod
import unittest
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC

from helper.Authentication import AuthenticationHelper, AuthenticationAssertHelper
from helper.Bundle import BundleHelper, BundleSelectors
from helper.Clinic import ClinicHelper, ClinicSelectors
from helper.Condition import ConditionHelper, ConditionSelectors, ConditionAssertHelper
from helper.Configuration import ConfigurationHelper, ConfigurationSelectors
from helper.Encounter import EncounterHelper, EncounterSelectors, EncounterScheduleType
from helper.Login import LoginHelper
from helper.Navigation import NavigationHelper
from helper.Question import QuestionHelper, QuestionAssertHelper, QuestionType
from helper.Questionnaire import QuestionnaireHelper, QuestionnaireAssertHelper
from helper.Score import ScoreHelper, ScoreAssertHelper
from helper.SeleniumUtils import SeleniumUtils, ErrorSelectors
from helper.Survey import SurveyHelper, SurveyAssertHelper, SurveySelectors
from helper.Language import LanguageSelectors, LanguageHelper
from helper.User import UserHelper, UserRoles, UserSelector, EmailSelectors
from helper.Statistic import StatisticSelector

loginHelper = LoginHelper()

abspath = os.path.abspath(__file__)
dname = os.path.dirname(abspath)
os.chdir(dname)

# noinspection PyStatementEffect
class IMISeleniumBaseTest(ABC):
    """
        Base class of all IMI selenium tests.
        * Handles the initialization of the driver.
        * Switches between server and local mode
    """

    currentResult = None
    """ Attribute used to set a cookie for validation purposes. """
    driver = None
    """ Selenium driver used in the tests. """
    
    @classmethod
    def setUpClass(cls) -> None:
        """
            Used to initialize constants.
        """
        # get filename of calling script (url)
        url = "webapp-container:8080/"
        cls.base_url = url
        cls.https_base_url = f"http://{url}"
        # secret used in the subclass
        cls.secret = cls._loadSecretFile(cls, "secret")
        cls.selenium_grid_url = f"http://localhost:4444/wd/hub/"


    def setUp(self) -> None:
        """
            Start a new driver for each test.
            Checks, if the script is called on the server or locally.
        """
        
        try:
            self._setServerDriver()
        except Exception as e:
            print(e)

        # maximize window to full-screen
        self.driver.maximize_window()

    def run(self, result=None):
        test_name = self._testMethodName
        print(f"=================== RUNNING TEST '{test_name}' ===================")
        self.currentResult = result
        unittest.TestCase.run(self, result)
        
        if result.wasSuccessful():
            print("Successfully ran Test without Errors")
        else:
            print("Test ran with errors:")
            for failed, error in result.failures + result.errors:
                if failed == self:
                    print("\n--- Stack Trace ---")
                    self._printError(error)
                    print("--- End of Trace ---")
        print(f"=================== END OF '{test_name}' ===================\n")

    def tearDown(self) -> None:
        """
            Sets the cookie to validate, if the test was successful or not.
        """
        if self.currentResult.wasSuccessful():
            cookie = {'name': 'zaleniumTestPassed', 'value': 'true'}
        else:
            cookie = {'name': 'zaleniumTestPassed', 'value': 'false'}
        self.driver.add_cookie(cookie)
        self.driver.quit()
        
    def _printError(self, error):
        exc_type, exc_value, tb = error
        traceback.print_exception(exc_type, exc_value, tb)

    def _loadSecretFile(self, filename):
        """
        Used to try loading file from server or locally.

        :param filename: file to be loaded without .json
        :return: The loaded json object or None
        """
        secret_local = os.path.join(os.getcwd(), "secrets", f"{filename}.json")
        if os.path.exists(secret_local):
            with open(secret_local) as f:
                return json.load(f)
        return None

    @abstractmethod
    def _setServerDriver(self):
        self.driver = None

    @abstractmethod
    def _setLocalDriver(self, directory):
        self.driver = None


class CustomTest(IMISeleniumBaseTest):

    def setUp(self):
        super().setUp()

        self.navigation_helper = NavigationHelper(self.driver)
        self.utils = SeleniumUtils(self.driver, navigation_helper=self.navigation_helper)
        self.navigation_helper.utils = self.utils

        self.authentication_helper = AuthenticationHelper(self.driver)
        self.questionnaire_helper = QuestionnaireHelper(self.driver, self.navigation_helper)
        self.question_helper = QuestionHelper(self.driver, self.navigation_helper)
        self.bundle_helper = BundleHelper(self.driver, self.navigation_helper)
        self.clinic_helper = ClinicHelper(self.driver, self.navigation_helper)
        self.configuration_helper = ConfigurationHelper(self.driver, self.navigation_helper)
        self.survey_helper = SurveyHelper(self.driver, self.navigation_helper)
        self.condition_helper = ConditionHelper(self.driver, self.navigation_helper)
        self.score_helper = ScoreHelper(self.driver, self.navigation_helper)
        self.question_assert_helper = QuestionAssertHelper(self.driver, self.navigation_helper)
        self.survey_assert_helper = SurveyAssertHelper(self.driver, self.navigation_helper)
        self.authentication_assert_helper = AuthenticationAssertHelper(self.driver)
        self.questionnaire_assert_helper = QuestionnaireAssertHelper(self.driver, self.navigation_helper)
        self.score_assert_helper = ScoreAssertHelper(self.driver, self.navigation_helper)
        self.condition_assert_helper = ConditionAssertHelper(self.driver, self.navigation_helper)
        self.language_helper = LanguageHelper(self.driver, self.navigation_helper)
        self.encounter_helper = EncounterHelper(self.driver, self.navigation_helper)

    def test_login_admin(self):
        if(self.secret['admin-username']!='' and self.secret['admin-password']!=''):
            self.driver.get(self.https_base_url)

            self.authentication_helper.login(self.secret['admin-username'], self.secret['admin-password'])

            WebDriverWait(self.driver, 10).until(EC.url_to_be(self.https_base_url + "admin/index"))

            self.driver.find_element(By.CSS_SELECTOR, "#headerNav > div:nth-child(2) > li:nth-child(3) > a:nth-child(1)").click()

            WebDriverWait(self.driver, 10).until(EC.url_to_be(self.https_base_url + "mobile/user/login?lang=de_DE"))

            assert True
        else:
            pass

    def test_admin_interface_login(self):
        self.driver.get(self.https_base_url)
        # a
        self.authentication_assert_helper.assert_mobile_user_login()
        # b
        self.authentication_assert_helper.assert_mobile_user_password()

    def test_admin_interface_index(self):
        self.driver.get(self.https_base_url)
        self.authentication_helper.login(self.secret['admin-username'], self.secret['admin-password'])

        # c
        self.authentication_assert_helper.assert_admin_index()

        self.authentication_helper.logout()

    def test_admin_interface_questionnaire_question_types_score(self):
        self.driver.get(self.https_base_url)
        self.authentication_helper.login(self.secret['admin-username'], self.secret['admin-password'])
        self.navigation_helper.navigate_to_manage_questionnaires()

        # d
        self.questionnaire_assert_helper.assert_questionnaire_list()
        # e
        self.questionnaire_helper.click_add_questionnaire_button()
        questionnaire = self.questionnaire_assert_helper.assert_questionnaire_fill_page()
        # f
        self.navigation_helper.search_and_open_questionnaire(questionnaire['name'])
        self.questionnaire_helper.save_questionnaire_edit_question()
        self.questionnaire_helper.click_add_question_button()
        self.question_assert_helper.assert_question_fill_page()
        # f (question types)
        # TODO [LJ] implement for all types
        question_list = list()
        excluded_question_types = {QuestionType.IMAGE}
        question_types = [question_type for question_type in QuestionType if
                          question_type not in excluded_question_types]
        for question_type in question_types:
            self.questionnaire_helper.click_add_question_button()
            question_by_type = self.question_assert_helper.assert_question_by_type(question_type)
            question_list.append(question_by_type)
        # g
        self.question_assert_helper.assert_question_table_functionality(len(question_list))
        # h
        self.navigation_helper.navigate_to_scores_of_questionnaire(questionnaire['id'], questionnaire['name'])
        self.score_assert_helper.assert_scores_list()
        # i
        self.navigation_helper.navigate_to_scores_of_questionnaire(questionnaire['id'], questionnaire['name'])
        self.score_assert_helper.assert_score_fill()

        self.authentication_helper.logout()

    def test_admin_interface_conditions(self):
        self.driver.get(self.https_base_url)
        self.authentication_helper.login(self.secret['admin-username'], self.secret['admin-password'])

        # j
        # Create source and target questionnaires with specific question types and add the questionnaires to a bundle to enable selection as a condition target
        source_questionnaire = self.questionnaire_helper.create_questionnaire_with_questions(question_types={QuestionType.SLIDER, QuestionType.MULTIPLE_CHOICE, QuestionType.DROP_DOWN})
        target_questionnaire = self.questionnaire_helper.create_questionnaire_with_questions(question_types={QuestionType.INFO_TEXT})
        bundle = self.bundle_helper.create_bundle(questionnaires=[source_questionnaire, target_questionnaire])
        bundle['id'] = self.bundle_helper.save_bundle(bundle['name'])

        threshold_supported_question_types = {QuestionType.SLIDER, QuestionType.NUMBER_CHECKBOX, QuestionType.NUMBER_INPUT}

        # Select a question where condition can be added with threshold value from the source questionnaire
        threshold_condition_question = next((question for question in source_questionnaire['questions']
                                                                   if question['type'] in threshold_supported_question_types), None)

        # Navigate to the source questionnaire's questions and reorder the slider question to the first position
        self.navigation_helper.navigate_to_questions_of_questionnaire(source_questionnaire['id'], source_questionnaire['name'])
        self.questionnaire_helper.reorder_question(threshold_condition_question['id'], 0)
        # Open the condition page for the question and add conditions targeting question, answer, and questionnaire
        self.condition_helper.open_conditions_of_question(threshold_condition_question['id'])
        condition_id_1 = self.condition_helper.add_condition_for_threshold_questions(threshold_steps=1, target_type=ConditionSelectors.TargetType.QUESTION)
        condition_id_2 = self.condition_helper.add_condition_for_threshold_questions(threshold_steps=2, target_type=ConditionSelectors.TargetType.ANSWER)
        condition_id_3 = self.condition_helper.add_condition_for_threshold_questions(threshold_steps=3, target_type=ConditionSelectors.TargetType.QUESTIONNAIRE)

        # Assert the conditions are correctly listed in the tables
        self.condition_assert_helper.assert_condition_list_and_search_de()
        self.condition_helper.delete_condition(condition_id_1)
        self.condition_helper.delete_condition(condition_id_2)
        self.condition_helper.delete_condition(condition_id_3)
        self.condition_helper.navigate_back_to_questions_of_questionnaire()

        # k
        self.condition_assert_helper.assert_add_condition_page(source_questionnaire)

        self.authentication_helper.logout()

    def test_bundle_list(self):
        bundle={}
        created_questionnaire={}
        clinic={}

        self.driver.get(self.https_base_url)
        self.authentication_helper.login(self.secret['admin-username'], self.secret['admin-password'])

        try:
            created_questionnaire = self.questionnaire_helper.create_questionnaire_with_questions()
        except Exception as e:
            self.fail(f"Failed to create questionnaire: {e}")

        try:
            self.navigation_helper.navigate_to_manage_bundles()
        except Exception as e:
            self.fail(f"Failed to navigate to 'Bundles' page: {e}")

        self.utils.check_visibility_of_element(BundleSelectors.TABLE_BUNDLE, "Bundle table not found")

        try:
            bundle = self.bundle_helper.create_bundle(publish_bundle=True, questionnaires=[created_questionnaire])
            bundle['id']=self.bundle_helper.save_bundle(bundle_name=bundle['name'])
        except Exception as e:
            self.fail(f"Failed to create bundle: {e}")
    
        self.utils.check_visibility_of_element(BundleSelectors.CELL_FLAGICON, "Flag icon not found")
    
        try:
            self.navigation_helper.navigate_to_manage_clinics()
            clinic["name"] = self.clinic_helper.create_clinic(bundles=[bundle],
                                                              configurations=[{'selector': (By.CSS_SELECTOR, '#usePseudonymizationService > div:nth-child(1) > div:nth-child(3) > label:nth-child(1)')}])
            clinic['id']=self.clinic_helper.save_clinic(clinic_name=clinic['name'])

        except Exception as e:
            self.fail(f"Failed to create clinic: {e}")

        # Assert - Find clinics assigned to the bundle
        try:
            self.navigation_helper.navigate_to_manage_bundles()
            self.utils.search_item(bundle["name"], "bundle")
            bundle_row = self.bundle_helper.get_first_bundle_row()
            bundle_row.find_element(By.CSS_SELECTOR, "td:nth-child(3)")
            clinic_link = bundle_row.find_element(By.CSS_SELECTOR, "ul > li > a")
            self.assertEqual(clinic_link.text, clinic["name"], f"Clinic name '{clinic["name"]}' not found in bundle row.")

        except Exception as e:
            self.fail(f"Failed to find clinic assigned to bundle: {e}")

        self.utils.check_visibility_of_element(BundleSelectors.INPUT_BUNDLE_SEARCH, "Bundle table search box not found")
        self.utils.check_visibility_of_element(BundleSelectors.PAGINATION_BUNDLE, "Bundle table pagination not found")
        self.utils.check_visibility_of_element(BundleSelectors.BUTTON_ADD_BUNDLE, "Bundle add button not found")

        try:
            pass
        finally:
            self.utils.search_and_delete_item(clinic["name"],clinic["id"], "clinic")
            self.utils.search_and_delete_item(bundle["name"],bundle["id"], "bundle")
            self.utils.search_and_delete_item(created_questionnaire['name'], created_questionnaire['id'], "questionnaire")

        self.authentication_helper.logout()

    def test_bundle_fill(self):
        created_questionnaire = {}
        self.driver.get(self.https_base_url)
        self.authentication_helper.login(self.secret['admin-username'], self.secret['admin-password'])

        try:
            created_questionnaire = self.questionnaire_helper.create_questionnaire_with_questions()
        except Exception as e:
            self.fail(f"Failed to create questionnaire: {e}")

        try:
            self.navigation_helper.navigate_to_manage_bundles()
        except Exception as e:
            self.fail(f"Failed to navigate to 'Bundles' page: {e}")

        self.utils.click_element(BundleSelectors.BUTTON_ADD_BUNDLE)
        self.language_helper.open_language_dropdown()
        self.utils.check_visibility_of_element(LanguageSelectors.LANGUAGE_DROPDOWN, "Failed to open language dropdown")
        self.utils.check_visibility_of_element(BundleSelectors.INPUT_NAME, "Failed to locate bundle input")
        self.utils.check_visibility_of_element(BundleSelectors.INPUT_EDITABLE_DESCRIPTION, "Failed to locate bundle description input")
        self.utils.check_visibility_of_element(BundleSelectors.INPUT_WELCOME_TEXT, "Failed to locate welcome input")
        self.utils.check_visibility_of_element(BundleSelectors.INPUT_END_TEXT, "Failed to locate end input")
        self.utils.check_visibility_of_element(BundleSelectors.CHECKBOX_PUBLISH, "Failed to locate publish checkbox")
        self.utils.check_visibility_of_element(BundleSelectors.CHECKBOX_NAME_PROGRESS, "Failed to locate name progress checkbox")
        self.utils.check_visibility_of_element(BundleSelectors.CHECKBOX_PROGRESS_WHOLE_PACKAGE, "Failed to locate progress whole package checkbox")
        self.utils.check_visibility_of_element(BundleSelectors.TABLE_AVAILABLE_QUESTIONNAIRES, "Available questionnaires table not found")
        self.utils.check_visibility_of_element(BundleSelectors.TABLE_ASSIGNED_QUESTIONNAIRES, "Assigned questionnaires table not found")

        #Assert - Test for assigning questionnaire to bundle
        try:
            self.bundle_helper.assign_multiple_questionnaires_to_bundle([created_questionnaire])
        except Exception as e:
            self.fail(f"Failed to assign questionnaire to bundle: {e}")

        #Assert - Test for removing questionnaire to bundle
        try:
            self.bundle_helper.remove_multiple_questionnaires_from_bundle([created_questionnaire])
        except Exception as e:
            self.fail(f"Failed to assign questionnaire to bundle: {e}")

        #Assert - Check form validation
        try:
            self.utils.click_element(BundleSelectors.BUTTON_SAVE)
            WebDriverWait(self.driver, 10).until(
            EC.visibility_of_element_located(ErrorSelectors.INPUT_VALIDATION_SELECTOR)
            )
            validation_errors = self.driver.find_elements(*ErrorSelectors.INPUT_VALIDATION_SELECTOR)
            self.assertEqual(len(validation_errors), 2, "Expected 2 validation errors, but found {len(validation_errors)}")
        except Exception as e:
            self.fail(f"Failed to save bundle: {e}")
    
        
        #Finally
        finally:
            if(created_questionnaire):
                self.utils.search_and_delete_item(created_questionnaire['name'], created_questionnaire['id'], "questionnaire")

        self.authentication_helper.logout()

    def test_clinic_list(self):
        clinic={}

        self.driver.get(self.https_base_url)
        self.authentication_helper.login(self.secret['admin-username'], self.secret['admin-password'])

        try:
            self.navigation_helper.navigate_to_manage_clinics()
        except Exception as e:
            self.fail(f"Failed to navigate to 'Clinic' page: {e}")

        try:
            self.navigation_helper.navigate_to_manage_clinics()
            clinic["name"] = self.clinic_helper.create_clinic(configurations=[{'selector': (By.CSS_SELECTOR, '#usePseudonymizationService > div:nth-child(1) > div:nth-child(3) > label:nth-child(1)')}])
            clinic['id']=self.clinic_helper.save_clinic(clinic_name=clinic['name'])

        except Exception as e:
            self.fail(f"Failed to create clinic: {e}")

        self.utils.check_visibility_of_element(ClinicSelectors.TABLE_CLINIC, "Clinic table not found")
        self.utils.check_visibility_of_element(ClinicSelectors.PAGINATION_CLINIC_TABLE, "Clinic table pagination not found")
        self.utils.check_visibility_of_element(ClinicSelectors.TABLE_SEARCH, "Clinic table search not found")
        self.utils.check_visibility_of_element(ClinicSelectors.TABLE_ACTION_BUTTONS, "Clinic table action buttons not found")
        self.utils.check_visibility_of_element(ClinicSelectors.BUTTON_ADD_CLINIC, "Add new clinic button not found")
        
        try:
            pass
        finally:
            self.utils.search_and_delete_item(clinic["name"],clinic["id"],"clinic")
            self.authentication_helper.logout()
            
    def test_clinic_fill(self):
        clinic={}
        created_questionnaire={}
        bundle={}

        self.driver.get(self.https_base_url)
        self.authentication_helper.login(self.secret['admin-username'], self.secret['admin-password'])

        #Arrange
        try:
            created_questionnaire = self.questionnaire_helper.create_questionnaire_with_questions()
            self.navigation_helper.navigate_to_manage_bundles()
            bundle = self.bundle_helper.create_bundle(publish_bundle=True, questionnaires=[created_questionnaire])
            bundle['id']=self.bundle_helper.save_bundle(bundle_name=bundle['name'])
        except Exception as e:
            self.fail(f"Failed to setup questionnaire and bundle: {e}")

        self.navigation_helper.navigate_to_manage_clinics()
        WebDriverWait(self.driver, 10).until(EC.visibility_of_element_located(ClinicSelectors.BUTTON_ADD_CLINIC))
        self.utils.click_element(ClinicSelectors.BUTTON_ADD_CLINIC)

        self.utils.check_visibility_of_element(ClinicSelectors.INPUT_CLINIC_NAME, "Clinic name input not found")
        self.utils.check_visibility_of_element(ClinicSelectors.INPUT_EDITABLE_DESCRIPTION, "Clinic description input not found")
        self.utils.check_visibility_of_element(ClinicSelectors.INPUT_CLINIC_EMAIL, "Clinic email input not found")
        
        #Assert - Check if the clinic configuration is displayed
        try:
            WebDriverWait(self.driver, 10).until(
                EC.visibility_of_element_located(ClinicSelectors.DIV_CLINIC_CONFIGURATION)
            )
            clinic_configuration = self.driver.find_element(*ClinicSelectors.DIV_CLINIC_CONFIGURATION)
            clinic_configuration_list = clinic_configuration.find_elements(*ClinicSelectors.CLINIC_CONFIGURATION_LIST)
            self.assertGreaterEqual(len(clinic_configuration_list), 1, "Clinic configuration list should have at least one item")
        except:
            self.fail(
                f"Clinic configuration not found")
            
        self.utils.check_visibility_of_element(ClinicSelectors.TABLE_AVAIALBLE_BUNDLES, "Available bundles table not found")
        self.utils.check_visibility_of_element(ClinicSelectors.TABLE_ASSIGNED_BUNDLES, "Assigned bundles table not found")            

                    
        #Assert - Check if the bundles can be added to the clinic
        try:
            self.clinic_helper.assign_multiple_bundes_to_clinic([{'id': bundle["id"], 'name': bundle["name"]}])
        except Exception as e:
            self.fail(f"Failed to assign bundle to clinic: {e}")
        
        #Assert - Check if the bundles can be removed from the clinic
        try:
            self.clinic_helper.remove_multiple_bundes_from_clinic([{'id': bundle["id"], 'name': bundle["name"]}])
        except Exception as e:
            self.fail(f"Failed to remove bundle from clinic: {e}")

        self.utils.check_visibility_of_element(ClinicSelectors.TABLE_AVAIALBLE_USERS, "Available users table not found")
        self.utils.check_visibility_of_element(ClinicSelectors.TABLE_ASSIGNED_USERS, "Assigned users table not found") 

        #Assert - Check if the users can be added to the clinic
        try:
            self.clinic_helper.assign_multiple_users_to_clinic([self.secret.get('admin-username')])
        except Exception as e:
            self.fail(f"Failed to assign users to clinic: {e}")

        #Assert - Check if the users can be removed from the clinic
        try:
            self.clinic_helper.remove_multiple_users_from_clinic([self.secret.get('admin-username')])
        except Exception as e:
            self.fail(f"Failed to remove users from clinic: {e}")

        #Assert - Check form validation
        try:
            self.utils.click_element(ClinicSelectors.BUTTON_SAVE)
            WebDriverWait(self.driver, 10).until(
            EC.visibility_of_element_located(ErrorSelectors.INPUT_VALIDATION_SELECTOR)
            )
            validation_errors = self.driver.find_elements(*ErrorSelectors.INPUT_VALIDATION_SELECTOR)
            configuration_errors = self.driver.find_elements(*ErrorSelectors.CONFIGURATION_ERROR_SELECTOR)
            self.assertEqual(len(validation_errors), 2, "Expected 2 validation errors, but found {len(validation_errors)}")
            self.assertEqual(len(configuration_errors), 1, "Expected 1 configuration errors, but found {len(configuration_errors)}")
        except Exception as e:
            self.fail(f"Failed to save bundle: {e}")

        #Assert - Check if the clinic can be created
        try:
            self.navigation_helper.navigate_to_manage_clinics()
            clinic['name']=self.clinic_helper.create_clinic(configurations=[{'selector': (By.CSS_SELECTOR, '#usePseudonymizationService > div:nth-child(1) > div:nth-child(3) > label:nth-child(1)')}],
                                             bundles=[{'id': bundle["id"], 'name': bundle["name"]}], users=[self.secret.get('admin-username')])
            clinic['id'] = self.clinic_helper.save_clinic(clinic["name"])
            WebDriverWait(self.driver, 10).until(
                EC.visibility_of_element_located(ClinicSelectors.TABLE_CLINIC)
            )
        except Exception as e:
            self.fail(f"Failed to create clinic: {e}")

        finally:
            if clinic["id"]:
                self.utils.search_and_delete_item(clinic['name'],clinic["id"],"clinic")
            if bundle["id"]:
                self.utils.search_and_delete_item(bundle["name"],bundle["id"],"bundle")
            if created_questionnaire:
                self.utils.search_and_delete_item(created_questionnaire['name'], created_questionnaire['id'], "questionnaire")
            self.authentication_helper.logout()
    
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
        except Exception as e:
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
            self.authentication_helper.logout()

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
        except Exception:
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
        except Exception:
            self.fail("First name input field still displayed")

        try:
            WebDriverWait(self.driver, 10).until_not(
                EC.presence_of_element_located(UserSelector.INPUT_USER_LAST_NAME(0))
            )
        except Exception:
            self.fail("Last name input field still displayed")
        
        try:
            WebDriverWait(self.driver, 10).until_not(
                EC.presence_of_element_located(UserSelector.INPUT_USER_EMAIL(0))
            )
        except Exception:
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

        except Exception as e:
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
        except Exception as e:
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
        except Exception as e:
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
        self.authentication_helper.logout()
        
    def test_one_time_statistic(self):
        # Arrange
        self.driver.get(self.https_base_url)
        self.authentication_helper.login(self.secret['admin-username'], self.secret['admin-password'])

        # Act
        self.navigation_helper.navigate_to_one_time_statistic()
        
        self.utils.check_visibility_of_element(StatisticSelector.BUNDLE_DROP_DOWN, "Bundle dropdown not found")
        self.utils.check_visibility_of_element(StatisticSelector.BUNDLE_START_DATE, "Bundle start date not found")
        self.utils.check_visibility_of_element(StatisticSelector.BUNDLE_END_DATE, "Bundle end date not found")
        self.utils.check_visibility_of_element(StatisticSelector.PATIENT_ID, "Patient ID not found")
        self.utils.check_visibility_of_element(StatisticSelector.PATIENT_START_DATE, "Patient start date not found")
        self.utils.check_visibility_of_element(StatisticSelector.PATIENT_END_DATE, "Patient end date not found")
        self.utils.check_visibility_of_element(StatisticSelector.BUNDLE_PATIENT_PATIENT_ID, "Bundle patient ID not found")
        self.utils.check_visibility_of_element(StatisticSelector.BUNDLE_PATIENT_BUNDLE_ID, "Bundle patient bundle ID not found")
        self.utils.check_visibility_of_element(StatisticSelector.BUNDLE_PATIENT_START_DATE, "Bundle patient start date not found")
        self.utils.check_visibility_of_element(StatisticSelector.BUNDLE_PATIENT_END_DATE, "Bundle patient end date not found")
        
        self.utils.click_element(StatisticSelector.BUTTON_BERECHNEN)
        
        self.utils.check_visibility_of_element(StatisticSelector.ANZAHL_1, "Anzahl 1 not found")
        self.utils.check_visibility_of_element(StatisticSelector.ANZAHL_2, "Anzahl 2 not found")
        self.utils.check_visibility_of_element(StatisticSelector.ANZAHL_3, "Anzahl 3 not found")

        self.authentication_helper.logout()
        
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
        
        self.utils.check_visibility_of_element(SurveySelectors.TEXT_BUNDLE_FINAL_INFO, "Bundle final info not found")

        self.survey_helper.end_survey()
        
        self.authentication_helper.logout()
    def tearDown(self): 
        self.driver.quit()


class IMISeleniumChromeTest(IMISeleniumBaseTest):
    """
        Test class for Chrome tests.
    """
    def _setServerDriver(self):
        name: str = f"{strftime('%Y-%m-%d-%H-%M-%S', gmtime())}_{self.base_url}_chrome"
        options = webdriver.ChromeOptions()
        options.set_capability("acceptInsecureCerts", True)
        options.add_argument("--headless=new")
        options.add_argument("--window-size=1920,1080")
        options.set_capability("selenoid:options", {
                                                    "enableVNC": False,
                                                    "enableVideo": False,
                                                    "enableLog": True,
                                                    "name": name,
                                                    "logName": f"{name}.log"
                                                    })
        self.driver = webdriver.Remote(options=options,
                                       command_executor=self.selenium_grid_url)


    def _setLocalDriver(self, directory):
        # download latest driver
        from selenium.webdriver.chrome.service import Service
        from webdriver_manager.chrome import ChromeDriverManager
        from webdriver_manager.core.driver_cache import DriverCacheManager
        # init driver
        self.driver = webdriver.Chrome(service=Service(ChromeDriverManager(cache_manager=DriverCacheManager(directory)).install()))


class CustomChromeTest(CustomTest, IMISeleniumChromeTest, unittest.TestCase):
    # Do not touch this function. This is the main entry point for selenium
    pass

class CustomTestResult(unittest.TextTestResult):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.successful_tests = []

    def addSuccess(self, test):
        super().addSuccess(test)
        self.successful_tests.append(test)

    def addError(self, test, err):
        self.errors.append((test, err))  # Store errors
        # Do not call the super method to prevent the default printing

    def addFailure(self, test, err):
        self.failures.append((test, err))  # Store failures
        # Do not call the super method to prevent the default printing

    def printErrors(self):
        # Override this method to suppress default error printing
        pass

    def printFailures(self):
        # Override this method to suppress default failure printing
        pass

    def printSummary(self):
        print("\n======================== TEST SUMMARY ========================")
        print(f"Total Tests Run: {self.testsRun}")
        
        print(f"\nSuccessful Tests ({len(self.successful_tests)}/{self.testsRun}):")
        if self.successful_tests:
            for test in self.successful_tests:
                print(f" - {test}")
        else:
            print(" None")

        print(f"\nFailed Tests ({len(self.failures)}/{self.testsRun}):")
        if self.failures:
            for test, _ in self.failures:
                print(f" - {test}")
        else:
            print(" None")

        print(f"\nErrored Tests ({len(self.errors)}/{self.testsRun}):")
        if self.errors:
            for test, _ in self.errors:
                print(f" - {test}")
        else:
            print(" None")

        print("=============================================================\n")

class CustomTestRunner(unittest.TextTestRunner):
    def _makeResult(self):
        return CustomTestResult(self.stream, self.descriptions, self.verbosity)

    def run(self, test):
        result = self._makeResult()
        result.failfast = self.failfast
        result.buffer = self.buffer
        result.tb_locals = self.tb_locals

        startTime = time.perf_counter()
        try:
            test(result)
        finally:
            stopTime = time.perf_counter()
            timeTaken = stopTime - startTime

            result.printErrors()  # Suppress default error print
            result.printFailures()  # Suppress default failure print

            result.printSummary()  # Print custom summary
            print(f"Time Taken: {timeTaken:.3f}s")  # Custom time output

        return result
    
if __name__ == "__main__":
    unittest.main(testRunner=CustomTestRunner(verbosity=2))
