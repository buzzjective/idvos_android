To use IdvosSDK in Eclipse you need to download it as .zip: 
https://github.com/buzzjective/idvos_android/blob/master/Eclipse.zip

Unpack and add it to your Eclipse workspace.

Make a folowing changes to AndroidManifest.xml of your project:

Permissions:

    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />

Features:

    <uses-feature android:name="android.hardware.camera" />
    <uses-feature android:name="android.hardware.camera.autofocus" />

Activities:

    <activity
        android:name="de.idvos.fastonlineidentification.activity.StartActivity"
        android:label="@string/app_name"
        android:screenOrientation="portrait"
        android:exported="true">
            
        <intent-filter>
            <action android:name="idvos.intent.action.IDENTIFY"/>
            <category android:name="android.intent.category.DEFAULT"/>
        </intent-filter>
            
    </activity>
       	
    <activity
        android:name="de.idvos.fastonlineidentification.activity.TermsActivity"
        android:label="@string/terms_label"
        android:screenOrientation="portrait"></activity>
        
    <activity
        android:name="de.idvos.fastonlineidentification.activity.InfoActivity"
        android:label="@string/info_label"
        android:screenOrientation="portrait"></activity>
        
    <activity
        android:name="de.idvos.fastonlineidentification.activity.LoginActivity"
        android:label="@string/login_label"
        android:screenOrientation="portrait"
        android:windowSoftInputMode="stateAlwaysHidden|adjustPan"></activity>
        
    <activity
        android:name="de.idvos.fastonlineidentification.activity.IdentificationActivity"
        android:label="@string/identification_title"
        android:screenOrientation="portrait"></activity>
        
    <activity
        android:name="de.idvos.fastonlineidentification.activity.SummaryActivity"
        android:label="@string/identification_summary"
        android:screenOrientation="portrait"></activity>
        
    <activity
        android:name="de.idvos.fastonlineidentification.activity.VerificationResultActivity"
        android:screenOrientation="portrait"></activity>
