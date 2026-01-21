import os
from selenium.common import NoSuchElementException, TimeoutException
from selenium.webdriver import Keys, ActionChains
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.wait import WebDriverWait

from helper.Question import QuestionType
from helper.Questionnaire import QuestionnaireHelper
from helper.SeleniumUtils import DropdownMethod, SeleniumUtils
from helper.Navigation import NavigationHelper


class ConfigurationSelectors:
    SELECT_LANGUAGE = (By.ID, "configurationGroupDTOsgeneral0\\.configurationDTOs0\\.value")
    INPUT_ADDITIONAL_LOGO = (By.CSS_SELECTOR, "#configurationGroup\\.label\\.general > fieldset:nth-child(2) > ul:nth-child(2) > li:nth-child(2) > div:nth-child(1) > div:nth-child(2) > div:nth-child(4) > div:nth-child(3) > div:nth-child(1) > div:nth-child(5) > input:nth-child(3)")
    INPUT_CASE_NUMBER_TYPE= (By.ID, "configurationGroupDTOsgeneral0\\.configurationDTOs2\\.value")
    INPUT_STORAGE_PATH_FOR_UPLOADS = (By.ID, "configurationGroupDTOsgeneral0\\.configurationDTOs3\\.value")
    INPUT_BASE_URL = (By.ID, "configurationGroupDTOsgeneral0\\.configurationDTOs4\\.value")
    INPUT_PATH_UPLOAD_IMAGES = (By.ID, "configurationGroupDTOsgeneral0\\.configurationDTOs5\\.value")
    INPUT_PATH_MAIN_DIRECTORY = (By.ID, "configurationGroupDTOsgeneral0\\.configurationDTOs6\\.value")

    CHECKBOX_AD_AUTH = (By.CSS_SELECTOR, "#configurationGroup\\.label\\.activeDirectoryAuthentication > fieldset:nth-child(2) > ul:nth-child(2) > li:nth-child(1) > div:nth-child(1) > div:nth-child(2) > label:nth-child(1) > input:nth-child(2)")
    INPUT_URL_AD = (By.ID, "configurationGroupDTOsactiveDirectoryAuthentication0.configurationDTOs0.children0.value")
    INPUT_DOMAIN_AD = (By.ID, "configurationGroupDTOsactiveDirectoryAuthentication0.configurationDTOs0.children1.value")
    SELECT_DEFAULT_LANGUAGE_AD = (By.ID, "configurationGroupDTOsactiveDirectoryAuthentication0.configurationDTOs0.children2.value")
    INPUT_PHONE_NUMBER_AD = (By.ID, "configurationGroupDTOsactiveDirectoryAuthentication0.configurationDTOs0.children3.value")

    CHECKBOX_PATIENT_LOOKUP = (By.CSS_SELECTOR, "#configurationGroup\\.label\\.usePatientLookUp > fieldset:nth-child(2) > ul:nth-child(2) > li:nth-child(1) > div:nth-child(1) > div:nth-child(2) > label:nth-child(1) > input:nth-child(2)")
    SELECT_PATIENT_LOOKUP_IMPLEMENTATION = (By.ID, "configurationGroupDTOsusePatientLookUp0\\.configurationDTOs1\\.value")
    INPUT_HOST_PATIENT_LOOKUP = (By.ID, "configurationGroupDTOsusePatientLookUp0\\.configurationDTOs2\\.value")
    INPUT_PORT_PATIENT_LOOKUP = (By.ID, "configurationGroupDTOsusePatientLookUp0\\.configurationDTOs3\\.value")

    INPUT_PSEUDONYMIZATION_URL = (By.ID, "configurationGroupDTOspseudonymization0\\.configurationDTOs0\\.value")
    INPUT_PSEUDONYMIZATION_API_KEY = (By.ID, "configurationGroupDTOspseudonymization0\\.configurationDTOs1\\.value")

    INPUT_OID = (By.ID, "configurationGroupDTOsmetadataExporter0\\.configurationDTOs0\\.value")
    INPUT_URL_ODM_TO_PDF = (By.ID, "configurationGroupDTOsmetadataExporter0\\.configurationDTOs1\\.value")
    INPUT_SYSTEM_URI_FOR_FHIR = (By.ID, "configurationGroupDTOsmetadataExporter0\\.configurationDTOs2\\.value")

    CHECKBOX_EXPORT_HL7_INTO_DIRECTORY = (By.CSS_SELECTOR, "#configurationGroup\\.label\\.HLSeven > fieldset:nth-child(2) > ul:nth-child(2) > li:nth-child(1) > div:nth-child(1) > div:nth-child(2) > label:nth-child(1) > input:nth-child(2)")
    INPUT_EXPORT_PATH_HL7_DIRECTORY = (By.ID, "configurationGroupDTOsHLSeven0\\.configurationDTOs0\\.children0\\.value")

    CHECKBOX_EXPORT_HL7_VIA_SERVER = (By.CSS_SELECTOR, "#configurationGroup\\.label\\.HLSeven > fieldset:nth-child(2) > ul:nth-child(2) > li:nth-child(3) > div:nth-child(1) > div:nth-child(2) > label:nth-child(1) > input:nth-child(2)")
    INPUT_HL7_HOST = (By.ID, "configurationGroupDTOsHLSeven0\\.configurationDTOs1\\.children0\\.value")
    INPUT_HL7_PORT = (By.ID, "configurationGroupDTOsHLSeven0\\.configurationDTOs1\\.children1\\.value")
    INPUT_SENDING_FACILITY = (By.ID, "configurationGroupDTOsHLSeven0\\.configurationDTOs1\\.children2\\.value")
    INPUT_RECEIVING_APPLICATION = (By.ID, "configurationGroupDTOsHLSeven0\\.configurationDTOs1\\.children3\\.value")
    INPUT_RECEIVING_FACILITY = (By.ID, "configurationGroupDTOsHLSeven0\\.configurationDTOs1\\.children4\\.value")
    INPUT_FILE_ORDER_NUMBER = (By.ID, "configurationGroupDTOsHLSeven0\\.configurationDTOs1\\.children5\\.value")

    CHECKBOX_ENCRYPT_MESSAGE_TLS = (By.CSS_SELECTOR, "#configurationGroup\\.label\\.HLSeven > fieldset:nth-child(2) > ul:nth-child(2) > li:nth-child(10) > div:nth-child(1) > div:nth-child(2) > label:nth-child(1) > input:nth-child(2)")
    FILE_PATH_CERTIFICATE = (By.CSS_SELECTOR, "#configurationGroup\\.label\\.HLSeven > fieldset:nth-child(2) > ul:nth-child(2) > li:nth-child(11) > div:nth-child(1) > div:nth-child(2) > div:nth-child(2) > div:nth-child(2) > div:nth-child(3) > div:nth-child(1) > input:nth-child(1)")

    CHECKBOX_USE_CERTIFICATE = (By.CSS_SELECTOR, "#configurationGroup\\.label\\.HLSeven > fieldset:nth-child(2) > ul:nth-child(2) > li:nth-child(12) > div:nth-child(1) > div:nth-child(2) > label:nth-child(1) > input:nth-child(2)")
    FILE_PKCS_ARCHIVE = (By.CSS_SELECTOR, "#configurationGroup\\.label\\.HLSeven > fieldset:nth-child(2) > ul:nth-child(2) > li:nth-child(13) > div:nth-child(1) > div:nth-child(2) > div:nth-child(2) > div:nth-child(2) > div:nth-child(3) > div:nth-child(1) > input:nth-child(1)")
    INPUT_PASSWORD_PKCS_ARCHIVE = (By.ID, "configurationGroupDTOsHLSeven0\\.configurationDTOs3\\.children1\\.value")

    CHECKBOX_EXPORT_ODM_INTO_DIRECTORY = (By.CSS_SELECTOR, "#configurationGroup\\.label\\.ODM > fieldset:nth-child(2) > ul:nth-child(2) > li:nth-child(1) > div:nth-child(1) > div:nth-child(2) > label:nth-child(1) > input:nth-child(2)")
    INPUT_EXPORT_PATH_ODM_DIRECTORY = (By.ID, "configurationGroupDTOsODM0\\.configurationDTOs0\\.children0\\.value")

    CHECKBOX_EXPORT_ODM_VIA_REST = (By.CSS_SELECTOR, "#configurationGroup\\.label\\.ODM > fieldset:nth-child(2) > ul:nth-child(2) > li:nth-child(3) > div:nth-child(1) > div:nth-child(2) > label:nth-child(1) > input:nth-child(2)")
    INPUT_URL_REST_INTERFACE = (By.ID, "configurationGroupDTOsODM0\\.configurationDTOs1\\.children0\\.value")

    CHECKBOX_EXPORT_ODM_VIA_HL7 = (By.CSS_SELECTOR, "#configurationGroup\\.label\\.ODM > fieldset:nth-child(2) > ul:nth-child(2) > li:nth-child(5) > div:nth-child(1) > div:nth-child(2) > label:nth-child(1) > input:nth-child(2)")
    INPUT_ODM_HL7_HOST = (By.ID, "configurationGroupDTOsODM0\\.configurationDTOs2\\.children0\\.value")
    INPUT_ODM_HL7_PORT = (By.ID, "configurationGroupDTOsODM0\\.configurationDTOs2\\.children1\\.value")
    INPUT_ODM_SENDING_FACILITY = (By.ID, "configurationGroupDTOsODM0\\.configurationDTOs2\\.children2\\.value")
    INPUT_ODM_RECEIVING_APPLICATION = (By.ID, "configurationGroupDTOsODM0\\.configurationDTOs2\\.children3\\.value")
    INPUT_ODM_RECEIVING_FACILITY = (By.ID, "configurationGroupDTOsODM0\\.configurationDTOs2\\.children4\\.value")
    INPUT_ODM_FILE_ORDER_NUMBER = (By.ID, "configurationGroupDTOsODM0\\.configurationDTOs2\\.children5\\.value")

    CHECKBOX_EXPORT_FHIR_INTO_DIRECTORY = (By.CSS_SELECTOR, "#configurationGroup\\.label\\.FHIR > fieldset:nth-child(2) > ul:nth-child(2) > li:nth-child(1) > div:nth-child(1) > div:nth-child(2) > label:nth-child(1) > input:nth-child(2)")
    INPUT_EXPORT_PATH_FHIR_DIRECTORY = (By.ID, "configurationGroupDTOsFHIR0\\.configurationDTOs0\\.children0\\.value")

    CHECKBOX_EXPORT_FHIR_INTO_COMMUNICATION_SERVER = (By.CSS_SELECTOR, "#configurationGroup\\.label\\.FHIR > fieldset:nth-child(2) > ul:nth-child(2) > li:nth-child(3) > div:nth-child(1) > div:nth-child(2) > label:nth-child(1) > input:nth-child(2)")
    INPUT_FHIR_HOST = (By.ID, "configurationGroupDTOsFHIR0\\.configurationDTOs1\\.children0\\.value")

    CHECKBOX_EXPORT_REDCAP_INTO_DIRECTORY = (By.CSS_SELECTOR, "#configurationGroup\\.label\\.REDCap > fieldset:nth-child(2) > ul:nth-child(2) > li:nth-child(1) > div:nth-child(1) > div:nth-child(2) > label:nth-child(1) > input:nth-child(2)")
    INPUT_EXPORT_PATH_REDCAP = (By.ID, "configurationGroupDTOsREDCap0\\.configurationDTOs0\\.children0\\.value")

    CHECKBOX_EXPORT_REDCAP_VIA_REST = (By.CSS_SELECTOR, "#configurationGroup\\.label\\.REDCap > fieldset:nth-child(2) > ul:nth-child(2) > li:nth-child(3) > div:nth-child(1) > div:nth-child(2) > label:nth-child(1) > input:nth-child(2)")
    INPUT_URL_REDCAP = (By.ID, "configurationGroupDTOsREDCap0\\.configurationDTOs1\\.children0\\.value")
    INPUT_TOKEN_REDCAP = (By.ID, "configurationGroupDTOsREDCap0\\.configurationDTOs1\\.children1\\.value")

    INPUT_TIME_DELETE_INCOMPLETE_ENCOUNTER = (By.ID, "configurationGroupDTOsencounter0\\.configurationDTOs0\\.value")    
    INPUT_TIME_DELETE_INCOMPLETE_ENCOUNTER_AND_NOT_SENT_QUESTIONNAIRE = (By.ID, "configurationGroupDTOsencounter0\\.configurationDTOs1\\.value")
    INPUT_TIME_DELETE_EMAIL_COMPLETED_ENCOUNTER = (By.ID, "configurationGroupDTOsencounter0\\.configurationDTOs2\\.value")
    INPUT_TIME_DELETE_INCOMPLETE_SCHEDULED_ENCOUNTERS = (By.ID, "configurationGroupDTOsencounter0\\.configurationDTOs3\\.value")
    INPUT_TIME_DELETE_INCOMPLETE_SCHEDULED_ENCOUNTER_AND_NOT_SENT_QUESTIONNAIRE = (By.ID, "configurationGroupDTOsencounter0\\.configurationDTOs4\\.value")

    CHECKBOX_UTILIZE_MAILER = (By.CSS_SELECTOR, "#configurationGroup\\.label\\.mail > fieldset:nth-child(2) > ul:nth-child(2) > li:nth-child(1) > div:nth-child(1) > div:nth-child(2) > label:nth-child(1) > input:nth-child(2)")
    INPUT_MAIL_HOST = (By.ID, "configurationGroupDTOsmail0\\.configurationDTOs0\\.children0\\.value")
    INPUT_MAIL_PORT = (By.ID, "configurationGroupDTOsmail0\\.configurationDTOs0\\.children1\\.value")

    CHECKBOX_ENABLE_TLS = (By.CSS_SELECTOR, "#configurationGroup\\.label\\.mail > fieldset:nth-child(2) > ul:nth-child(2) > li:nth-child(4) > div:nth-child(1) > div:nth-child(2) > label:nth-child(1) > input:nth-child(2)")
    CHECKBOX_SMTP_AUTHENTICATION = (By.CSS_SELECTOR, "#configurationGroup\\.label\\.mail > fieldset:nth-child(2) > ul:nth-child(2) > li:nth-child(5) > div:nth-child(1) > div:nth-child(2) > label:nth-child(1) > input:nth-child(2)")

    INPUT_USERNAME_MAILER = (By.ID, "configurationGroupDTOsmail0\\.configurationDTOs0\\.children4\\.value")
    INPUT_PASSWORD_MAILER = (By.ID, "configurationGroupDTOsmail0\\.configurationDTOs0\\.children5\\.value")
    INPUT_SENDER_MAILER = (By.ID, "configurationGroupDTOsmail0\\.configurationDTOs0\\.children6\\.value")
    INPUT_EMAIL_ADDRESS_MAILER = (By.ID, "configurationGroupDTOsmail0\\.configurationDTOs0\\.children7\\.value")
    INPUT_PHONE_MAILER = (By.ID, "configurationGroupDTOsmail0\\.configurationDTOs0\\.children8\\.value")

    INPUT_MAIL_SUPPORT = (By.ID,"configurationGroupDTOssupport0\\.configurationDTOs0\\.value")
    INPUT_PHONE_SUPPORT = (By.ID,"configurationGroupDTOssupport0\\.configurationDTOs1\\.value")

    BUTTON_SAVE_CONFIGURATION = (By.ID, "saveButton")

    IMAGE_ADDITIONAL_LOGO = (By.CSS_SELECTOR, "div.footer-bar.d-flex.align-items-center.justify-content-between > div.d-bp-flex.d-none.justify-content-end.d-flex")
    

class ConfigurationHelper:

    DEFAULT_IMAGE_UPLOAD_PATH = "test_images"
    DEFAULT_IMAGE_FILE_NAME = "test_logo.png"

    DEFAUL_EXPORT_IMAGE_UPLOAD_PATH = "/var/lib/mopatImages"
    DEFAULT_EXPORT_PATH = "/var/lib/mopatExport"
    DEFAULT_EXPORT_PATH_ODM = os.path.join(DEFAULT_EXPORT_PATH, "ODM")
    DEFAULT_EXPORT_PATH_FHIR = os.path.join(DEFAULT_EXPORT_PATH, "FHIR")
    DEFAULT_EXPORT_PATH_REDCap = os.path.join(DEFAULT_EXPORT_PATH, "REDCap")
    DEFAULT_PORT_HL7_PATIENT_LOOKUP = 1234

    DEFAULT_MAIL_SUPPORT = "test@test.com"

    def __init__(self, driver, navigation_helper: NavigationHelper):
        self.driver = driver
        self.utils = SeleniumUtils(driver, navigation_helper)
    
    def add_additional_logo(self, upload_path=DEFAULT_IMAGE_UPLOAD_PATH, file_name=DEFAULT_IMAGE_FILE_NAME):
        try:
            image_path = os.path.join(os.path.dirname(__file__), upload_path, file_name)
            assert os.path.exists(image_path), f"Test image not found at path: {image_path}"

            self.driver.find_element(*ConfigurationSelectors.INPUT_ADDITIONAL_LOGO).send_keys(image_path)
        except (NoSuchElementException, TimeoutException) as e:
            print(f"An error occurred while adding additional logo: {e}")

    def save_configuration(self):
        self.utils.click_element(ConfigurationSelectors.BUTTON_SAVE_CONFIGURATION)