import time

from selenium.common import TimeoutException
from selenium.webdriver.chrome.webdriver import WebDriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import Select

from helper.Navigation import NavigationHelper
from helper.Question import QuestionSelectors
from helper.SeleniumUtils import SeleniumUtils

class EncounterSelectors:
    BUTTON_ENCOUNTER_TABLE = (By.ID, "encounter-tablink")
    BUTTON_ENCOUNTER_SCHEDULE_TABLE = (By.ID, "encounterScheduled-tablink")
    BUTTON_EXECUTE_ENCOUNTER = (By.ID, "executeEncounter")
    BUTTON_SCHEDULE_ENCOUNTER = (By.ID, "scheduleEncounter")
    
    TABLE_ALL_ENCOUNTERS = (By.ID, "encounterTable")
    TABLE_SCHEDULED_ENCOUNTERS = (By.ID, "encounterScheduled")

class EncounterHelper:

    def __init__(self, driver: WebDriver, navigation_helper: NavigationHelper):
        self.driver = driver
        self.utils = SeleniumUtils(driver)
        self.navigation_helper = navigation_helper

    