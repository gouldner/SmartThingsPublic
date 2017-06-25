# H1 Ron's Guide to installing the ZXT-120 device.

You can find a copy of the manual for this device here also in my github repo.  Note: This may not be your device's version so keep that in mind.  I find this version of the manual describes certain things quite well.  For example how to place the device into "Always Listening Mode.  Older versions of the manual that may come with your device have some errors in describing the steps.

[ZXT-120 Manual v1.0](https://github.com/gouldner/SmartThingsPublic/blob/master/devicetypes/gouldner/docs/zxt-120/rm-zxt-Zipato-Ir-Extender-User-Manual-v1.0.pdf)

## H2 Install Custom Device Code
First you must install the device code into your ide.  For instructions on how to do this see the very helpful FAQ on the community site.
[How to install Custom Device Code](https://community.smartthings.com/t/faq-an-overview-of-using-custom-code-in-smartthings/16772)
If you are too lazy to read this FAQ you will miss out on some excellent features for linking github to your ide for easy updates in the future etc.  But I understand if you only want to do this once for this device it is a lot to digest so here is the quick and dirty steps.
1. Get the raw code copy of the device code and copy into your copy/paste buffer.  [RAW COPY](https://raw.githubusercontent.com/gouldner/SmartThingsPublic/master/devicetypes/gouldner/zxt-120-ir-sender-improved.src/zxt-120-ir-sender-improved.groovy)
2. Open the ide and switch to the [device tab](https://graph.api.smartthings.com/ide/devices).
3. Select "+Create New Device Handler" button.
4. Switch to "From Code".  Paste code you copied from the Raw Code Page.
5. Press "Create" to save this code and create your copy of the custom device code.
6. You should now see a device handler in your device handler list with the label "gouldner : ZXT-120 IR Sender Improved" assuming you made no changes to the namespace or name of the device handler.

## H2 Join ZXT-120 to SmartThings
1. Make sure your deviced in connected to a USB charger.  It works best this way.  Battery mode doesn't respond as needed so I recommend connecting the device to a wired charger at all times.  The next step requires a battery.  Continuous listening mode doesn't work with batteries according to the manual.
2. Make sure the device is in Continuous Listening Mode.  To do this follow the instructions in the [manual] (https://github.com/gouldner/SmartThingsPublic/blob/master/devicetypes/gouldner/docs/zxt-120/rm-zxt-Zipato-Ir-Extender-User-Manual-v1.0.pdf) on page 13/14.  
3. Now select add a device in the Smart Things App.  I am going to document how to do this because Smart Things likes to change the app very often moving things around making it impossible to really document how these things are done.  If you don't know how to add a device visit the [community](https://community.smartthings.com/) and search for instructions or contact Smart Things for support.
4. Once the Smart Things App is looking for devices press the button on the ZXT-120 so the Samrt Things Hud will see it and include the device.  The correct device Handler should be discovered as I have configured the fingerprint to match the device.  If it doesn't you may have to visit the "My Devices" page, select the ZXT-120 you just added, click edit and change the device handler to the "ZXT-120 IR Sender Improved" you created above.  NOTE: if you already included the device before installing the device handler you can switch it using this technique as well.

## H2 Configure ZXT-120 to work with your AC
1. Launch the Device you added, and select the configuration button (Gear in top right hand corner)
2. Change the device Name to suit your purposes.
3. Set your Remote Code.  Remote codes are listed in the Manual (https://github.com/gouldner/SmartThingsPublic/blob/master/devicetypes/gouldner/docs/zxt-120/rm-zxt-Zipato-Ir-Extender-User-Manual-v1.0.pdf) starting on page 34. You can also choose the set the optional shortname.  Select Done when finished with the screen.
4. Click the Configure Button (See reference Image at bottom to locate this button) to set the Remote Code.
5. Click the Refresh button (Bottom Row of Icon List) to update the SmartApp display.
6. Test by Trying to change modes(By selecting Either Heat/Cool/Dry/Off).  If it is successful, continue on.  If not, repeat Steps 3-6 with a new code.

### H3 ZXT-120 Learning
Optionally, you can attempt to "Learn" your current remote's codes
1. Select the Configuration (Gear Icon) in the top right hand side, and Set your Remote code to 000 (it will display as 0). Select Done.
2. Use the learning slider to select the learning position you want to program.
3. Select "learn" and the zxt-120 will flash once.
4. Press the remote for the code to be learned while aiming at the zxt-120.  It should flash twice for success and 4 times for fail. If it fails just try again.

## H2 Operation.
Depending on the Mode you are in, use the slider to set the desired temperature, and then press the corrosponding Mode button.  
For example, Use the Cool Slider to drag it to 72 degrees F.  Then select the Cool Command to have the ZXT-120 transmist the command.

Reference Images
![Image 1](https://lh3.googleusercontent.com/qrZHYVAi_MlVdJoI4kJya_wvBoSb-LEcV5Fsfm1D3i4byDS__RlOnKeQOtNoDOEaz_EEFXGi-snacH8gnXH3kX2JlMm-SESPBdf8-ejebaAkkeD5jBZ8iqzhvJear-nzCXvpKDPTDoYdkLUDPTbEvhgchoqMSh_zyA2vAR_t5XEd-aT_pL8YOVJ2DWQKWHUwVCWfK73tKHAVR8hhknsII_cIH_nAPn3CbOjPDjUdgihx3wTvztSE459m6lbCfXqXwn9d1Sy1f5SiX8W3PanpuZ5wWxv8EZn1jvw3zlKeWObr47wP58vdQ-M174Ud9_aW80pObe3G1KMAYdhOoN5un4LWeGSYpMfcA8xHoeta4PwexGNuqH-H5gLcbP5FAmEM3LWYgLbQY9yYzLi7ClYfDdU323O8bpTzqfUDJB4Y7ogVbKixGvsz24KH7fcfUz6CAmoNLZD1aZKpyUMnPFT-_pDkllC3txKNF1mYOnAs3tA5z8sC4unpB2wkZ8TsIQ8yRXl7nB6PBLdDPS70Z-fwiZ9Ew8pwLBamGEbKBeGouZ31xxexjCMEvKBIEh-DnWwWBSnCYI6MZG3tjH6UbiymJ82IBNL21Xc89KsTbSWuJf4uOcEgtzaOTFCAoZTiZs2HwDtIEEWllesMg4Z5QS1K9s4f3GpKpjmDul7osfI3xIQ=w912-h925-no "Image 1")

![Image 2](https://lh3.googleusercontent.com/X7YjbrnLJiTV1Y6GgksBFDhAMozfnHEdFO-zp0IWHV2UMTbB1VubFZHVv-xE8vHUJ7HK8xDoOD11jpn1bsBa3mu4LIalllZplnOpDtIrZEj_AnJpg8O7nTE4ytnWNX8_Bqljan1CmcJXhjoD9i1-9FwtiAB512F6Rd4Dy58dMvKaK9vcDSlrizlRzCe9U38Edwq-Jema2lJE757GaA5VDvcekdvZUqdNrvTjUZhLUP7ub6-MnH6btKBfw7lWrBzVcAi01dpMsdlZnabcmUtghaccRYU0V73OYjF95ASkcB6G6PKDKA18Da7-2fEJJKebkktMN7jNmJelB9DA9AjZmdInN18zrjdPX7q2UKRmCeHqiaY7J5m8pK08rHNFSUQATypgOfQOhX9YSfeRXzk1AkKy51bQKdxyz3WhJN1kTfj_5_0iKVZbwPLniE9jbGy6Osj-Iy3N6KnoGsJytM2y3ad5hjXdzECFxGtvzGuGKCSyVhvmwk-PvHV-N6p55QURNjcy5VYWxYeQZxw_Vh0F4XBQf329Kosu3rgZeLJJl2RMPrTs6VlTQwHSCfzwj2-5PVpfrfhyPQNNs5Joeo1d8-JC3Hog4zqNPl9L8xERHM-DA74UXRLxXYqFscipO4lzTV0Ae28X6rMSg1hcohUJdHJIvjCH_2qYVyirnT5vE1s=w960-h925-no "Image 2")

![Image 3](https://lh3.googleusercontent.com/nbRpQ6j6AKs_OWqyjWb0zPDvlwQJ4pGWzgq_1rp6kDMopoB4E-zpLvJ_65caqzW8z_p3k8tBt7Pgg8MX7RmkwBWYCE8amNi6MFoUblacbOgjEjiQbbt5-6l3HCq-7PY9VsKeoPwiXLgZ4WHy_MKWgJ-Y6ZfhN3yExszUdN1qoGq3z8kdKOFidZDbS-vZDR-BgnyIUhQMbZqEs8A_Z_rynu3UAul-Ud2eHCm1nevfB3hLVhrZU1v5z3J4aQYNN_j65cglj6iC0hCqStWfRBTelJQsTcGBNzUh6mZOp8vGg8cwI0k80kLtfHNn8zbpWreLRLHPkeEm7ncXZO8Gvtn_fi97E2NR4xDHv3kXnQwBBnlIazlUoapcKy1C_PYp9agga36EKWAJxESkxzdX6vDrmfb9uXE64MILBCoJ1MqrMeJdsZnUx3gCIrEAGinwpxV4V4hwTc_QSH3qu0G9xg-bW1wEKr9vlBhgLi_9Px1O5ElnJLu6EuRWyfbUWBD1_0JjVYThaoyJYvSvVzhOfr-xeJQIgyvCJ1PwSxIcPjgXNXBTSS598XV3-fj3tz6n9osOtPPBl1rbcEb-jIbjXTg37vyhpKD1WrqjTc3Su3BQ0pa_JUk0F6UAm45WyfJiOPV4St004bhKBCeD-z3uTASzaBIUqTTXofXZK6IFpgXuVuQ=w960-h925-no "Image 3")
