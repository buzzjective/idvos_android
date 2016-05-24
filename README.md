# idvos Android App

Folowing instructions are for Gradle. 

For Maven see https://github.com/buzzjective/idvos_android/blob/master/MAVEN.md

For Eclipse see https://github.com/buzzjective/idvos_android/blob/master/ECLIPSE.md

## Dependencies

Add this code to your module’s build.gradle

    apply plugin: 'crashlytics'

    dependencies {
        compile 'com.github.buzzjective:idvos_android:0.5'
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

## ProGuard release configuration

Add the following lines to your proguard-rules configuration file if you have set **minifyEnabled** to true in your debug/release build type:

    # OpenTok
    -keep class com.opentok.jni.** { *; }
    -keep class com.opentok.android.** { *; }
    -keep class com.opentok.client.** { *; }
    -keep class com.opentok.impl.** { *; }

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
        if (requestCode != REQUEST_CODE){
            return;
        }
        IdentificationResult result = data.getParcelableExtra(IdentificationResult.IDENTIFICATION_RESULT);
    }

`IdentificationResult` has:

`long getWaitingTimeMillis();` - returns customer's waiting time in milliseconds.

`boolean isSuccessful();` - true if identification was successful, false - otherwise.

`String getTransactionId();` - returns transaction id.


Possible ERROR_CODES:
    
    FAIL_REASON_INVALID_IDENTIFICATION = 0;
    FAIL_REASON_INVALID_INPUT = 1;
    FAIL_REASON_SERVER_ERROR = 2;
    FAIL_REASON_OTHER = 3;
    FAIL_REASON_PUSHER_AUTHENTICATION = 4;
    FAIL_REASON_TOKBOX_ERROR = 5;
    FAIL_REASON_INVALID_P2P_SESSION_ID = 6;
    FAIL_REASON_UNVERIFIED = 7;
	
