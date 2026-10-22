#!/usr/bin/env python3

import datetime
import json
import os
import re
import sys
import time
import traceback
import unittest
from abc import ABC, abstractmethod
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait
from time import gmtime, strftime

from base_test import CustomChromeTest
from helper.Login import LoginHelper
from selenium import webdriver

# !/usr/bin/env python3
# -*- coding: utf-8 -*-

loginHelper = LoginHelper()


class CustomTest(CustomChromeTest):
    def test_admin_interface_index(self):
        self.driver.get(self.https_base_url)
        self.authentication_helper.login(self.secret['admin-username'],
                                         self.secret['admin-password'])

        self.authentication_assert_helper.assert_admin_index()

        self.authentication_helper.logout()


if __name__ == "__main__":
    unittest.main(verbosity=2)
