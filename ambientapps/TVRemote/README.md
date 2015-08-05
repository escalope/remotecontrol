Compile and install the app on all avds and android devices connected.

	mvn clean install android:deploy

Or just make this available to the scenario

	mvn clean install

The generated *apk* can be also installed in your phone with

	adb push target/NAME_OF_THE_APK.apk

## CREDITS

SociAAL project: http://grasia.fdi.ucm.es/sociaal
Pablo Campillo Sanchez (pabcampi@ucm.es)
Jorge J. Gomez-Sanz (jjgomez@ucm.es)

