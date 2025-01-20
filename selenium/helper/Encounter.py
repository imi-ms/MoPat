from enum import Enum
import time

from selenium.common import TimeoutException
from selenium.webdriver.chrome.webdriver import WebDriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import Select

from helper.Navigation import NavigationHelper
from helper.Question import QuestionSelectors
from helper.SeleniumUtils import SearchBoxSelectors, SeleniumUtils

class EncounterSurveyLanguages(Enum):
    DE_DE = "de_DE"
    EN_GB = "en_GB"
    ES_ES = "es_ES"
    FR_FR = "fr_FR"
    HI_IN = "hi_IN"
    IT_IT = "it_IT"
    NL_NL = "nl_NL"
    NO_NO = "no_NO"
    PL_PL = "pl_PL"
    PT_PT = "pt_PT"
    RU_RU = "ru_RU"
    SV_SE = "sv_SE"
    TR_TR = "tr_TR"
    AR = "ar"
    FA_IR = "fa_IR"
    DARI = "dari"
    KU = "ku"

class EncounterScheduleType(Enum):
    UNIQUELY = "UNIQUELY"
    REPEATEDLY = "REPEATEDLY"
    WEEKLY = "WEEKLY"
    MONTHLY = "MONTHLY"


class EncounterSelectors:
    BUTTON_ENCOUNTER_TABLE = (By.ID, "encounter-tablink")
    BUTTON_ENCOUNTER_SCHEDULE_TABLE = (By.ID, "encounterScheduled-tablink")
    BUTTON_EXECUTE_ENCOUNTER = (By.ID, "executeEncounter")
    BUTTON_SCHEDULE_ENCOUNTER = (By.ID, "scheduleEncounter")
    BUTTON_SAVE_SCHEDULED_ENCOUNTER = (By.ID, "saveButton")

    INPUT_SCHEDULE_CASE_NUMBER = (By.ID, "caseNumber")
    INPUT_SCHEDULE_EMAIL = (By.ID, "email")
    INPUT_DATE= (By.ID, "startDate")
    INPUT_TIME_PERIOD = (By.ID, "repeatPeriod")
    INPUT_END_DATE = (By.ID, "endDate")
    INPUT_PERSONAL_TEXT= (By.ID, "personalText")

    SELECT_SCHEDULE_CLINIC = (By.ID, "activeClinicDTO")
    SELECT_SCHEDULE_BUNDLE = (By.ID, "bundleDTO.id")
    SELECT_SURVEY_TYPE = (By.ID, "encounterScheduledSerialType")
    SELECT_LANGUAGE = (By.ID, "locale")

    TABLE_ALL_ENCOUNTERS = (By.ID, "encounterTable")
    TABLE_SCHEDULED_ENCOUNTERS = (By.ID, "encounterScheduled")

    PAGINATION_ENCOUNTER_SCHEDULE_TABLE = (By.ID, "encounterScheduled_paginate")
    PAGINATION_ENCOUNTER_TABLE = (By.ID, "encounterTable_paginate")

    SEARCH_ALL_ENCOUNTERS = (By.ID, "encounterTable_filter")
    SEARCH_SCHEDULED_ENCOUNTERS = (By.ID, "encounterScheduled_filter")

class EncounterHelper:

    def __init__(self, driver: WebDriver, navigation_helper: NavigationHelper):
        self.driver = driver
        self.utils = SeleniumUtils(driver)
        self.navigation_helper = navigation_helper

    def schedule_encounter(self, case_number, clinic, bundle, email, survey_type, start_date, time_period_days=7, end_date=None, language=EncounterSurveyLanguages.DE_DE.value, message=""):
        """
        :param case_number: case number of type string.
        :param clinic: clinic name of type string.
        :param bundle: bundle name of type string.
        :param email: email of type string.
        :param survey_type: survey type of type EncounterScheduleType enum.
        :param start_date: A string representing the date to set (e.g., "2024-12-20").
        :param time_period_days: time period in days of type int.
        :param end_date: A string representing the date to set (e.g., "2024-12-20").
        :param language: language of type EncounterSurveyLanguages enum.
        :param message: message of type string.
        """

        try:
            self.utils.fill_text_field(EncounterSelectors.INPUT_SCHEDULE_CASE_NUMBER, case_number)
            self.utils.select_dropdown(EncounterSelectors.SELECT_SCHEDULE_CLINIC, clinic)
            self.utils.select_dropdown(EncounterSelectors.SELECT_SCHEDULE_BUNDLE, bundle)
            self.utils.fill_text_field(EncounterSelectors.INPUT_SCHEDULE_EMAIL, email)
            self.driver.execute_script("document.getElementById('startDate').valueAsDate = new Date(arguments[0]);", start_date)
            self.driver.execute_script("document.getElementById('startDate').dispatchEvent(new Event('blur'));")
        
        except Exception as e:
            print("Error filling schedule encounter form elements ", e)
            
        try:
            if survey_type == EncounterScheduleType.REPEATEDLY:
                self.utils.select_dropdown(EncounterSelectors.SELECT_SURVEY_TYPE, "REPEATEDLY", method="value")
                self.utils.fill_text_field(EncounterSelectors.INPUT_TIME_PERIOD, time_period_days)
                if end_date is not None:
                    self.driver.execute_script("document.getElementById('endDate').valueAsDate = new Date(arguments[0]);", end_date)
                    self.driver.execute_script("document.getElementById('endDate').dispatchEvent(new Event('blur'));")
            elif survey_type == EncounterScheduleType.WEEKLY:
                self.utils.select_dropdown(EncounterSelectors.SELECT_SURVEY_TYPE, "WEEKLY", method="value")
                if end_date is not None:
                    self.driver.execute_script("document.getElementById('endDate').valueAsDate = new Date(arguments[0]);", end_date)
                    self.driver.execute_script("document.getElementById('endDate').dispatchEvent(new Event('blur'));")        
            elif survey_type == EncounterScheduleType.MONTHLY:
                self.utils.select_dropdown(EncounterSelectors.SELECT_SURVEY_TYPE, "MONTHLY", method="value")
                if end_date is not None:
                    self.driver.execute_script("document.getElementById('endDate').valueAsDate = new Date(arguments[0]);", end_date)
                    self.driver.execute_script("document.getElementById('endDate').dispatchEvent(new Event('blur'));")
            else:
                self.utils.select_dropdown(EncounterSelectors.SELECT_SURVEY_TYPE, "UNIQUELY", method="value")
            
        except Exception as e:
            print("Error filling schedule encounter form elements ", e)

        try:
            self.utils.select_dropdown(EncounterSelectors.SELECT_LANGUAGE, language, method="value")
            self.utils.fill_text_field(EncounterSelectors.INPUT_PERSONAL_TEXT, message)
            self.utils.click_element(EncounterSelectors.BUTTON_SAVE_SCHEDULED_ENCOUNTER)

        except Exception as e:
            print("Error clicking schedule encounter button ", e)

        self.navigation_helper.navigate_to_manage_surveys()
        self.utils.click_element(EncounterSelectors.BUTTON_ENCOUNTER_SCHEDULE_TABLE)

        # Search for the bundle by name
        self.utils.fill_text_field(SearchBoxSelectors.SCHEDULED_ENCOUNTER, case_number)

        scheduled_encounter_id=None
        try:
            WebDriverWait(self.driver, 10).until(
            EC.presence_of_element_located((By.XPATH, f"//span[text()='{email}']"))
            )
            span = self.driver.find_element(By.XPATH, f"//span[text()='{email}']")

            href = span.get_attribute("href")
            scheduled_encounter_id = href.split("#")[1].split("_")[0]
        except TimeoutException:
            print(f"Could not find the span with email: {email}")

        return scheduled_encounter_id
    

    def delete_scheduled_encounter(self, scheduled_encounter_id, case_number):

        self.navigation_helper.navigate_to_manage_surveys()
        self.utils.click_element(EncounterSelectors.BUTTON_ENCOUNTER_SCHEDULE_TABLE)
        search_box_selector = SearchBoxSelectors.SCHEDULED_ENCOUNTER
        button_id = f"removeScheduleEncounter_{scheduled_encounter_id}"


        try:
            # Search for the element using the appropriate search box
            self.utils.fill_text_field(search_box_selector, case_number)

            # click the remove button
            self.utils.click_element((By.ID, button_id))
        except TimeoutException:
            print(f"Failed to delete scheduled encounter '{scheduled_encounter_id}'.")