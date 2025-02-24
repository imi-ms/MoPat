#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
Script containing the base class of selenium tests.
Handles driver initialization and server/local switches.
"""
import json
import os
from abc import ABC, abstractmethod
import requests
import unittest
import inspect
from selenium import webdriver
import enum
from time import gmtime, strftime


# noinspection PyStatementEffect
class IMISeleniumBaseTest(ABC):
    """
        Base class of all IMI selenium tests.
        * Handles the initialization of the driver.
        * Switches between server and local mode
    """


    class SeleniumMode(enum.Enum):
        """
            Helper to configure if selenium should run on server or locally
            Default mode is AUTO
        """
        SERVER = 1 # run on server
        LOCAL  = 2 # run local
        AUTO   = 3 # try server and if an error occurs switch to local


    currentResult = None
    """ Attribute used to set a cookie for validation purposes. """
    driver = None
    """ Selenium driver used in the tests. """
    seleniumMode: SeleniumMode = SeleniumMode.AUTO
    """ Use selenium on the server or locally? """

    @classmethod
    def setUpClass(cls) -> None:
        """
            Used to initialize constants.
        """
        # get filename of calling script (url)
        url = os.path.basename(inspect.getouterframes(inspect.currentframe())[-1].filename)[:-3]
        cls.base_url = url
        cls.https_base_url = f"https://{url}"
        # secret used in the subclass
        cls.secret = cls._loadSecretFile(cls, url)
        cls.basic_auth = cls._loadSecretFile(cls, "basic-auth")
        if cls.basic_auth is not None:
            cls.selenium_grid_url = f"https://{cls.basic_auth['user']}:{cls.basic_auth['passwd']}@selenoid.uni-muenster.de/wd/hub/"
        else:
            cls.selenium_grid_url = None

    def setUp(self) -> None:
        """
            Start a new driver for each test.
            Checks, if the script is called on the server or locally.
        """
        if (self.seleniumMode == self.SeleniumMode.SERVER):
                try:
                    self._setServerDriver()
                except Exception as e:
                    print(e)
        elif self.seleniumMode == self.SeleniumMode.LOCAL:
                try:
                    # create folder for driver
                    directory = os.path.join(os.getcwd(), "driver")
                    if not os.path.exists(directory):
                        os.makedirs(directory)
                    # init driver
                    self._setLocalDriver(directory)
                except Exception as e:
                    print(e)
        elif self.seleniumMode == self.SeleniumMode.AUTO:
                # check if script runs on server or local
                try:
                    # if this call fails, we are local
                    requests.get(self.selenium_grid_url)
                    # init driver
                    self._setServerDriver()
                except Exception as e:
                    # create folder for driver
                    directory = os.path.join(os.getcwd(), "driver")
                    if not os.path.exists(directory):
                        os.makedirs(directory)
                    # init driver
                    self._setLocalDriver(directory)

        # maximize window to full-screen
        self.driver.maximize_window()

    def run(self, result=None) -> None:
        """
            Main function of the unit test.
        """
        self.currentResult = result
        unittest.TestCase.run(self, result)

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

    def _loadSecretFile(self, filename):
        """
        Used to try loading file from server or locally.

        :param filename: file to be loaded without .json
        :return: The loaded json object or None
        """
        secret_server = f"/etc/selenium/{filename}.json"
        if os.path.exists(secret_server):
            with open(secret_server) as f:
                return json.load(f)
        else:
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


class IMISeleniumChromeTest(IMISeleniumBaseTest):
    """
        Test class for Chrome tests.
    """
    def _setServerDriver(self):
        name: str = f"{strftime('%Y-%m-%d-%H-%M-%S', gmtime())}_{self.base_url}_chrome"
        options = webdriver.ChromeOptions()
        options.set_capability("acceptInsecureCerts", True)
        options.set_capability("selenoid:options", {
                                                    "enableVNC": True,
                                                    "enableVideo": True,
                                                    "enableLog": True,
                                                    "name": name,
                                                    "videoName": f"{name}.mp4",
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



class IMISeleniumFirefoxTest(IMISeleniumBaseTest):
    """
        Test class for Firefox tests.
    """
    def _setServerDriver(self):
        name: str = f"{strftime('%Y-%m-%d-%H-%M-%S', gmtime())}_{self.base_url}_firefox"
        options = webdriver.FirefoxOptions()
        options.set_capability("acceptInsecureCerts", True)
        options.set_capability("selenoid:options", {
                                                    "enableVNC": True,
                                                    "enableVideo": True,
                                                    "enableLog": True,
                                                    "name": name,
                                                    "videoName": f"{name}.mp4",
                                                    "logName": f"{name}.log"
                                                    })
        self.driver = webdriver.Remote(options=options,
                                       command_executor=self.selenium_grid_url)

    def _setLocalDriver(self, directory):
        # download latest driver
        from webdriver_manager.firefox import GeckoDriverManager
        from selenium.webdriver.chrome.service import Service
        from webdriver_manager.core.driver_cache import DriverCacheManager
        # init driver
        self.driver = webdriver.Firefox(service=Service(GeckoDriverManager(cache_manager=DriverCacheManager(directory)).install()))

