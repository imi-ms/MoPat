from selenium.webdriver.common.by import By
from selenium.webdriver.chrome.webdriver import WebDriver

from helper.Navigation import NavigationHelper



class StatisticSelector:
    # First "Fragebogenpaket" dropdown
    BUNDLE_DROP_DOWN            = (By.ID, "bundleDropDown")
    BUNDLE_START_DATE           = (By.ID, "bundleStartDate")
    BUNDLE_END_DATE             = (By.ID, "bundleEndDate")

    # Patient-related fields
    PATIENT_ID                  = (By.ID, "patientId")
    PATIENT_START_DATE          = (By.ID, "patientStartDate")
    PATIENT_END_DATE            = (By.ID, "patientEndDate")

    # Patient & bundle combination fields (reuses "bundleDropDown" ID in HTML)
    BUNDLE_PATIENT_PATIENT_ID   = (By.ID, "bundlePatientPatientId")
    BUNDLE_PATIENT_BUNDLE_ID    = (By.ID, "bundleDropDown")  # Same ID as above
    BUNDLE_PATIENT_START_DATE   = (By.ID, "bundlePatientStartDate")
    BUNDLE_PATIENT_END_DATE     = (By.ID, "bundlePatientEndDate")
    
    ANZAHL_1 = (By.ID, "encounterCountByBundleInInterval")
    ANZAHL_2 = (By.ID, "encounterCountByCaseNumberInInterval")
    ANZAHL_3 = (By.ID, "encounterCountByCaseNumberByBundleInInterval")
    
    # “Berechnen” button (no explicit ID, so using a CSS selector)
    BUTTON_BERECHNEN            = (By.CSS_SELECTOR, "button[type='submit'].btn.btn-primary")

