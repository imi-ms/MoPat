from selenium.webdriver.common.by import By

class LoginHelper:
    def login(self, driver, username, password):
        username_input = driver.find_element(By.ID, "username")
        username_input.send_keys(username)

        password_input = driver.find_element(By.ID, "password")
        password_input.send_keys(password)

        # Click the submit button
        submit_button = driver.find_element(By.XPATH, "//button[@type='submit']")
        submit_button.click()
    
    def logout(self, driver):
        logout_button = driver.find_element(By.CSS_SELECTOR, "#headerNav > div:nth-child(2) > li:nth-child(3) > a:nth-child(1)")
        logout_button.click()