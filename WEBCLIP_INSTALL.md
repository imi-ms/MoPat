# About 

As MoPat is a web application, it can be used with any browser. However, this also means, that users could accidentally 
quit the application by using the browser navigation. To mitigate this, we have created an iOS shortcut to install MoPat
as a webclip, which behaves like a native application. In the following, we describe the installation step by step.


## 1. Open your Browser
<h4>On your iOS device, please open the browser. </h4>

![webclip step 1](./img/webclip_01.png)

## 2. Enter the URL for the iOS shortcut
<h4>We have uploaded a shortcut to iCloud, which automates the process of installing the application. 
Just enter the following URL on your device: www.icloud.com/shortcuts/a4a954ef22d64d15a66a0d58b1808c4d </h4>

![webclip step 2](./img/webclip_02.png)

## 3. Install the shortcut
<h4>The browser will open a shortcut page. Click the button at the bottom of the page to add it to your local shortcuts.</h4>

![webclip step 3](./img/webclip_03.png)

<h4>The shortcut will be opened in the Shortcuts app. It should be called "Make MoPat Webclip". Click on "Add Shortcut" to install 
it locally.</h4>

![webclip step 4](./img/webclip_04.png)

## 4. Start the Shortcut
<h4>The shortcut should now be installed locally as "Make MoPat Webclip". Click on it inside the Shortcut app to run it.
The app will sometimes ask you if the shortcut is allowed to access web information. Accept this and proceed.</h4>

![webclip step 5](./img/webclip_05.png)

<h4>The shortcut will automatically perform all steps to install MoPat as a webclip. In the process you will need to enter 
the name which should be shown on your Home Page: </h4>

![webclip step 6](./img/webclip_06.png)

<h4>and the URL to access your local MoPat installation. 
<br/>Important: For newer iOS versions, Apple only allows webclips to 
open in fullscreen, if the URL in the webclip is the same as the final URL. MoPat performs an immediate redirect to the
login page, so you need to enter the URL of the login page: https://<YOURMOPATDOMAIN>/mobile/user/login</h4>

![webclip step 7](./img/webclip_07.png)

<h4>After the shortcut is finished, it will open a page in your browser that will try to install a profile. This is 
necessary in order to install MoPat as an app. Click on "Allow" to proceed.</h4>

![webclip step 8](./img/webclip_08.png)

<h4>If everything went well, the iOS device will report, that it successfully installed a new profile for our generated
webclip.</h4>

![webclip step 9](./img/webclip_09.png)

## 5. Install the Profile to add MoPat as an App
<h4>Proceed by going back to your Home Screen and open the Settings application.</h4>

![webclip step 10](./img/webclip_10.png)

<h4>In the settings, you should now see a new option pop up which says "Profile downloaded". Click on it to open up
the installation. </h4>

![webclip step 11](./img/webclip_11.png)

<h4>A new window should open, which shows the overview of the created profile. Click on "Install" to
proceed.</h4>

![webclip step 12](./img/webclip_12.png)

<h4>Enter the passcode of your device to proceed.</h4>

![webclip step 13](./img/webclip_13.png)

<h4>Click next to continue the installation.</h4>

![webclip step 14](./img/webclip_14.png)

<h4>Confirm the installation by clicking on "Install"</h4>

![webclip step 15](./img/webclip_15.png)

<h4>And finally confirm once more by clicking on "Install" again.</h4>

![webclip step 16](./img/webclip_16.png)

<h4>This will finish the installation of the app and MoPat should now be available on your Home Screen.</h4>

![webclip step 17](./img/webclip_17.png)

<h4>Open the App and make sure, that you can see the MoPat Login page as a full screen app.</h4>

![webclip step 18](./img/webclip_18.png)

### Troubleshooting
<h4>I can see the login page, but it opens in my browser instead of its own app<br/></h4>

This can happen if you entered a faulty URL during the installation. If a redirect happens upon opening our new app, 
Apple will identify this as a security risk and will therefore open it in a browser instead, so users can see the
page they were redirected to. Make sure you entered the URL correctly: `https://<YOURMOPATDOMAIN>/mobile/user/login`.
Uninstall the Profile from your settings and try again. 
<br/>

<h4>The App shows up on my Home Screen, but when I open it, I cannot see MoPat<br/></h4>

This could have multiple reasons. Maybe you entered a faulty URL in the setup process. It could also be, that your 
device is not able to access the server, where MoPat is running. To make sure, that this is possible, open the URL
in your browser. If you can access MoPat, uninstall the profile from your settings and try again with the correct URL.

## 6. Activate Guided Access
<h4>For more security it makes sense to lock users inside the app, once a new survey has been started. Apple devices have
a feature called Guided Access, so that it is only possible to leave an app, if a pin is entered. This setting can be found
in the "Accessibility" section in the settings app.</h4>

![webclip step 19](./img/webclip_19.png)

<h4>Activate the Feature by enabling the switch and open the Passcode Settings.</h4>

![webclip step 20](./img/webclip_20.png)

<h4>Click on "Set Guided Access Passcode" and enter a PIN of your choice. Remember it, as it is needed to exit MoPat again.
Do not write it anywhere on the device, so that only authorized personell is able to exit the app.</h4>

![webclip step 21](./img/webclip_21.png)

<h4>You have now activated Guided Access. To trigger it, open up your app and Triple-Click on your Home- or Side Button. 
To deactivate it, do the same and enter your pin. It is also possible to limit the available touch region or activate certain buttons again. 
To find out more about Guided Access, look at Apples description: https://support.apple.com/en-us/111795 </h4>

## Uninstall MoPat
If you want to uninstall MoPat from your device, this can not be done like a normal application. Instead, the profile has to be
removed from your settings: 
1. Open the Settings app
2. Navigate: General > VPN & Device Management
3. Tap the desired profile
4. Tap Remove Profile
5. If prompted, enter the passcode and then tap Remove

The application should then automatically be removed from the device 









