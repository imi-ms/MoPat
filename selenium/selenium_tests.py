#!/usr/bin/env python3
# -*- coding: utf-8 -*-

from time import gmtime, strftime
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
from helper.Login import LoginHelper

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
    def test_login_admin(self):
        if(self.secret['admin-username']!='' and self.secret['admin-password']!=''):
            self.driver.get(self.https_base_url)

            loginHelper.login(self.driver,self.secret['admin-username'], self.secret['admin-password'])

            WebDriverWait(self.driver, 10).until(EC.url_to_be(self.https_base_url + "admin/index"))

            self.driver.find_element(By.CSS_SELECTOR, "#headerNav > div:nth-child(2) > li:nth-child(3) > a:nth-child(1)").click()

            WebDriverWait(self.driver, 10).until(EC.url_to_be(self.https_base_url + "mobile/user/login?lang=de_DE"))

            assert True
        else:
            pass
    
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
