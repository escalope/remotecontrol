This sample project is a pervasive computing solution where a user manipulates a TV with a remote control. The remote control is a mobile device with a touch screen. The problem in the operation of the device are the hand dexterity issues the user can have. The case study defines two series of screen tapping that resemble a device interaction similar to one with tremor issues. The operation of the device includes a loop of other actions to simulate an activity of the daily living. This repetition allows to continuosly test the application performance.

The project exemplifies the basic components a developer would need to start working with the construction of pervasive solutions:

- the folder ambientapps contains the android applications to be developed and installed
- the folder scenario contains a description of the domain problem that can be rendered into a 3D simulation where android emulators, which host the android applications, receive input from the environment and redirect them to the applications.  

The development of a pervasive solution requires deciding how many devices are there in the environment and what services they provide. Also, there should be model describing the physical environment as well as the actors living inside. 

It is assumed that, on using these tools, three possible situations are:

- you want to start desiging the applications, so, initially, you need not to address the scenario modeling
- you want to start capturing requirements by modeling the scenario, and then move to the applications
- you already have some scenario modeled, have designed applications, and want to use them in your development

The first requires working exclusively in the *ambientapps* folder. There, you create different applications by duplicating some of the sample projects. Do not forget to modify accordingly the *ambientapps/pom.xml* file to include the new app. Also, modify the new subfolder *pom.xml* file to customize it to the new app name. Then, the *scenario/pom.xml* file has to be modified to include the reference to the new app. Once this is done, you can refer to the application from the scenario specification file in the same terms as other existing ones.

The second requires start working with the *scenario* folder. It is a modeling work made with the visual tools provided in this distribution. When you consider appropriate, you can try to compile and run the specification. Without devices, what you can check is the behavior of the actors. 

The third implies there are already applications and a modeled scenario. In those cases, there will times when the effort is focused on the development of the application and that will enforce an update of the application copies in the *scenarios* folder. Also, it may be the case that you want to define different runs of the same application with different actor behaviors. After, or in between, applications can still be updated. The only limitation is that two people cannot edit the scenario specification file at the same time. 

To support the evaluation of the execution, the system incorporates a normative monitoring system. This system is a separated component that observes the events inside the simulation and scores the simulation performance in terms of how many times the simulation performed as expected and when did it not. See the demo launch instructions to check this.

The system has been designed with PHAT framework.PHAT Framework is a set of tools (coded using Java) to model and simulate activities of daily living. Relevant components for this are:

- **SociAALML Editor** is a graphical editor to model the elements for the simulation.
- **PHAT Simulator** is a simulator developed from scratch using jMonkeyEngine.
- **PHAT Generator** is a tool that transforms the model defined with SociAALML in java code. The code extends PHAT Simulator and can be simulated.


### REQUIREMENTS:
 
- Java 1.7 (set variable JAVA_HOME to the path where JAVA is installed)
- Maven 3.1.1+ installed, see http://maven.apache.org/download.html (set variable M2_HOME to the path where maven is installed)
- Ant (set variable ANT_HOME to the path where ant installed)
- Android SDK (r21.1 or later, latest is best supported) installed, preferably with all platforms, see http://developer.android.com/sdk/index.html Make sure you have images for API 19 installed in your android SDK. It is required to have the IntelAtomx86 image to permit hardware acceleration. Instructions for Android are available in the [Android site](http://developer.android.com/tools/devices/emulator.html#acceleration)
- Set environment variable ANDROID_HOME to the path of your installed Android SDK and add $ANDROID_HOME/tools as well as $ANDROID_HOME/platform-tools to your $PATH. (or on Windows %ANDROID_HOME%\tools and %ANDROID_HOME%\platform-tools).
- Add binaries to environment variable PHAT (PATH=$PATH:$HOME/bin:$JAVA_HOME/bin:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools:$M2_HOME/bin)

### Launching the demo

The first time, go to the *ambientapps* folder and do

	mvn clean install

Then go to the *scenario* folder 

	cd scenario

and type at least once:

	mvn -P ambient compile

This will process the current scenario definition and generate install scripts. Scripts will be allocated in scenario/target/generated/scripts. These scripts achieve the android devices creation. It has to be run only once. The system creates one script per deployment scenario. If the devices are the same across deployments, any deployment script is enough. It will also import the *apk* files from the ambient folder and leave a copy in your resources folder.

	sh target/generated/scripts/createAVDsSim1.sh

If it fails, it may be because of a defective android install. 

Afterwards, you have to start the agent platform so that agents inside the ambient applications can be properly initialize

	sh startPlatform.sh

If you want the monitoring solution also to identify how it is performing, type

	sh startMonitoring.sh

This monitoring is an under research development. It tells when the system is assisting when it shouldnt and when the system is not assisting and it should. The first situation is called an *undesired interruption* while the second is a *tolerated failure*. 

Now, the scenario can be launched with

	ant runSim1

which runs a simulation with the actor having medium tremors or

	ant runSim2

which runs the simulation with the actor having light tremors.

Any of the previous will launch the 3D simulation, with the android devices, it will install the required applications inside the android devices, and will send them instructions to start specific activities. In paralell, the actors in the simulation will start performing tasks.

There are variants to these run command, like

	ant runSim2NoDevices

Which launchs the environment without connecting and launching the android devices. 

	ant runSim2OnlyDevices

Which launches only the involved android devices

### Modifying the specification 

The scenario specification can be opened and modified. Modification requires some knowledge of the modeling language, SociAALML, and it is reccomended to do first the tutorial in http://grasia.fdi.ucm.es/sociaal

The steps are simple. You need to be at the scenario folder. To open the specification

	ant edit

Once prepared, save your work, and return to the command line (or open another terminal). To render the new specification, run 

	ant compile

Once compiled, the selected simulation can be run with:

	ant runNAMEOFTHESIMULATION

if it does not have devices or

	antrunNAMEOFTHESIMULATIONNoDevices

If it has them and you don't want to launch them. If you type

	ant runNAMEOFTHESIMULATIONOnlyDevices

Only the devices of the simulation will be launched.

To define a simulation, please check the tutorial at http://grasia.fdi.ucm.es/sociaal

You can also document the specification in HTML format with:

	mvn clean site

The report generation may take too long to generate, due to the construction of the dependencies tree. To save time, you can try

	mvn -o clean site

The resulting HTML specification will be stored in target/site

### Creating apps

Apps are just regular android applications. They differ from conventional ones in that the access to sensors is not done directly, but through wrappers. The development of these apps can be made  with Android Studio, or ADT. The compilation and deployment is made through maven, here.

The apps will be installed as a maven artifact and retrieved during the simulation launch to initialize properly the android devices.

The compilation of the project leads to an *apk* file in the *target* folder. All applications can be compiled by running 

	mvn clean compile

Compilation does not produce the *apk* files that will be installed in the emulated or real devices. And if the *apk* files  need to be available to the scenario, you ought to run

	mvn clean install

*Install* implies a compilation of the application. These commands can be run in the in the *ambientapps* folder and obtain all the *apks* belonging to each sub application. Moving to a subfolder that contains an application, the same could commands could be used.

	cd SmartTVApp
	mvn clean install

In case you want to use the *apk* files right away in the emulator or in a real phone, there are two ways. One is directly executing

	mvn clean compile android:deploy

This command will deploy to the emulator *Smartphone1*, because the corresponding *pom.xml* file tells that's the name of the AVD that will host the application. It is mandatory that this emulator is already running.

Another way to do this is through the native android's *adb* command. For the Smart TV App, if you are already in the smart tv app folder, the command would be:

	adb install target SmartTVApp-1.0.0-SNAPSHOT.apk

This command will deploy the *apk* to either the emulator or a connected real phone. To select a particular emulator, do first a

	adb devices

To get the names of running emulators and connected real phones

	adb -s SERIAL_OR_OTHER_IDENTIFIER install target SmartTVApp-1.0.0-SNAPSHOT.apk

As an alternative, you can use the Android Studio or Eclipse ADT to perform the same tasks. The project is a maven one, so it should be easy to import into either of these two environments. 

In the case of Eclipse, if you import, do it as an *Existing Maven Project*, not as an android project. 
In all cases, ensure that the *pom.xml* is using the same target platform as the one defined in your chosen android development IDE.

To use these applications in the scenario, at least once you have to make them available. To do so, just run this

	mvn -P ambient clean compile

Or any other mvn command with the "-P ambient". This will bring defined *apks* to your scenario, so that they can be referred from the specification. 

To add more apps, it is recommended to duplicate any of the projects under the *ambientapps* folder and modify the *ambientapps/pom.xml* to include a reference to this new artifact, and the new folder's pom.xml file to define a proper artifact id for the new application.  Also, it is important to change the *scenario/pom.xml* file in the *ambient* profile section to include instructions to consider this new application. A new dependence and a new copy artifact action will be needed. You can use the code for Smart TV app or TV Remote as examples.


## CREDITS

SociAAL project: http://grasia.fdi.ucm.es/sociaal
Pablo Campillo Sanchez (pabcampi@ucm.es)
Jorge J. Gomez-Sanz (jjgomez@ucm.es)
