# idvos Android App

Uses Gradle.

## Dependencies

Add this code to your module’s build.gradle

    apply plugin: 'crashlytics'

    dependencies {
        compile 'com.github.buzzjective:idvos_android:0.2'
    }

Add those dependencies to root build.gradle buildscript:

    repositories {
        maven { url 'http://download.crashlytics.com/maven' }
    }
    dependencies {
        classpath 'com.crashlytics.tools.gradle:crashlytics-gradle:1.+'
    }

Add those dependencies to root build.gradle allprojects:

    repositories {
        maven { url "https://jitpack.io" }
        maven { url 'http://download.crashlytics.com/maven' }
    }


## SDK Setup

Initialize the SDK before using and choose mode of operating(either Mode.TEST or Mode.PRODUCTION):

IdvosSDK.initialize(this, IdvosSDK.Mode.TEST);

Pass your identification hash in order to start identification process:
IdvosSDK.getInstance().startIdentification(this, 100, "mxqC1jsvV4uaL_zRtFn7");
