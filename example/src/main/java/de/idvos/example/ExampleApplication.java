package de.idvos.example;

import android.app.Application;

import de.idvos.fastonlineidentification.sdk.IdvosSDK;

public class ExampleApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        IdvosSDK.initialize(this, IdvosSDK.Mode.TEST);
    }
}
