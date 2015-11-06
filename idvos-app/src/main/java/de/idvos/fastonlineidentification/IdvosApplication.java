package de.idvos.fastonlineidentification;

import android.app.Application;

import de.idvos.fastonlineidentification.sdk.IdvosSDK;

/**
 * Application entry point
 */
public class IdvosApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        IdvosSDK.initialize(this, IdvosSDK.Mode.PRODUCTION);
    }
}
