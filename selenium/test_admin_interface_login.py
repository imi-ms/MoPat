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

from helper.Login import LoginHelper
from selenium import webdriver

# !/usr/bin/env python3
# -*- coding: utf-8 -*-

loginHelper = LoginHelper()

from base_test import CustomChromeTest


class CustomTest(CustomChromeTest):
    def test_admin_interface_login(self):
        self.driver.get(self.https_base_url)
        # a
        self.authentication_assert_helper.assert_mobile_user_login()
        # b
        self.authentication_assert_helper.assert_mobile_user_password()


if __name__ == "__main__":
    unittest.main(verbosity=2)
