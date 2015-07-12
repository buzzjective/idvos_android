# idvos Android App

Folowing instructions are for Gradle. 

For Maven see https://github.com/buzzjective/idvos_android/blob/master/MAVEN.md

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

And those to root build.gradle allprojects:

    repositories {
        maven { url "https://jitpack.io" }
        maven { url 'http://download.crashlytics.com/maven' }
    }


## SDK Setup

Initialize the SDK before using and choose mode of operating(either `Mode.TEST` or `Mode.PRODUCTION`):

    public class ExampleApplication extends Application {
        @Override
        public void onCreate() {
            super.onCreate();
            IdvosSDK.initialize(this, IdvosSDK.Mode.PRODUCTION);
       }
    }

## SDK Usage

Pass your identification hash in order to start identification process:

    IdvosSDK.getInstance().startIdentification(this, REQUEST_CODE, USER_HASHCODE);

You'll recive a result in `onActivityResult`

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_CODE || resultCode != RESULT_OK){
            return;
        }
        IdentificationResult result = data.getParcelableExtra(IdentificationResult.IDENTIFICATION_RESULT);
    }
