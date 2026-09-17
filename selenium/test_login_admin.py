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
from selenium import webdriver

loginHelper = LoginHelper()

from base_test import CustomChromeTest


class CustomTest(CustomChromeTest):
    def test_login_admin(self):
        if (self.secret['admin-username'] != '' and self.secret[
            'admin-password'] != ''):
            self.driver.get(self.https_base_url)

            self.authentication_helper.login(self.secret['admin-username'],
                                             self.secret['admin-password'])

            WebDriverWait(self.driver, 10).until(
                EC.url_to_be(self.https_base_url + "admin/index"))

            self.driver.find_element(By.CSS_SELECTOR,
                                     "#headerNav > div:nth-child(2) > li:nth-child(3) > a:nth-child(1)").click()

            WebDriverWait(self.driver, 10).until(
                EC.url_to_be(
                    self.https_base_url + "mobile/user/login?lang=de_DE"))

            assert True
        else:
            pass


if __name__ == "__main__":
    unittest.main(verbosity=2)
