#!/usr/bin/env python3

import unittest

from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait

from base_test import CustomChromeTest
from helper.Question import QuestionType


class CustomTest(CustomChromeTest):
    def _login_as(self, role):
        self.driver.get(self.https_base_url)
        self.authentication_helper.login(
            self.secret[f"{role}-username"], self.secret[f"{role}-password"])
        WebDriverWait(self.driver, 15).until(EC.presence_of_element_located((By.ID, "adminNav")))

    def _logout(self):
        self.authentication_helper.logout()
        WebDriverWait(self.driver, 15).until(EC.presence_of_element_located((By.ID, "username")))

    def _create_questionnaire(self, role, expected_status):
        self._login_as(role)
        questionnaire = self.questionnaire_helper.create_questionnaire_with_questions(
            question_types={QuestionType.MULTIPLE_CHOICE})
        self.questionnaire_assert_helper.assert_questionnaire_status(
            questionnaire["id"], questionnaire["name"], expected_status)
        self._logout()
        return questionnaire

    def test_questionnaire_review_flow(self):
        self._create_questionnaire("admin", "approved")
        self._create_questionnaire("moderator", "approved")
        questionnaire = self._create_questionnaire("editor", "draft")

        request_description = "Please review this questionnaire."
        rejection_reason = "Please clarify the wording of the question."
        resubmission_description = "The wording has been clarified."
        review_list_url = f"{self.https_base_url}review/pending/list"

        self._login_as("editor")
        self.driver.get(review_list_url)
        self.review_assert_helper.assert_pending_list()
        self.review_helper.request_review(
            questionnaire["name"], "Moderator User", request_description)
        self.questionnaire_assert_helper.assert_questionnaire_status(
            questionnaire["id"], questionnaire["name"], "under_review")
        self.driver.get(review_list_url)
        self.review_helper.assert_review_list_entry(questionnaire["name"])
        self.review_helper.open_review(questionnaire["name"])
        self.review_assert_helper.assert_pending_details()
        self.assertIn(request_description, self.driver.find_element(By.ID, "conversation").text)
        self._logout()

        self._login_as("moderator")
        self.driver.get(review_list_url)
        self.review_assert_helper.assert_pending_list()
        self.review_helper.assert_review_list_entry(questionnaire["name"])
        self.review_helper.open_review(questionnaire["name"])
        self.review_assert_helper.assert_pending_details(is_reviewer=True)
        self.review_helper.reject_review(rejection_reason)
        self._logout()

        self._login_as("editor")
        self.driver.get(review_list_url)
        self.review_assert_helper.assert_review_list_entry(questionnaire["name"])
        self.review_helper.open_review(questionnaire["name"])
        self.review_assert_helper.assert_pending_details()
        self.assertIn(rejection_reason, self.driver.find_element(By.ID, "conversation").text)
        self.review_helper.resubmit_review(resubmission_description)
        self._logout()

        self._login_as("admin")
        self.driver.get(review_list_url)
        self.review_assert_helper.assert_pending_list(is_admin=True)
        self.review_helper.assert_review_list_entry(questionnaire["name"], list_type="all")
        self.review_helper.open_review(questionnaire["name"], list_type="all")
        self.review_assert_helper.assert_pending_details(is_reviewer=True)
        self.review_helper.approve_review()

        self.questionnaire_assert_helper.assert_questionnaire_status(
            questionnaire["id"], questionnaire["name"], "approved")
        self.driver.get(f"{self.https_base_url}review/completed/list")
        self.review_assert_helper.assert_completed_list(is_admin=True)
        self.review_helper.assert_review_list_entry(questionnaire["name"], list_type="all")
        self.review_helper.open_review(
            questionnaire["name"], list_type="all", is_completed=True)
        self.review_assert_helper.assert_completed_details([
            request_description, rejection_reason, resubmission_description])


if __name__ == "__main__":
    unittest.main(verbosity=2)
