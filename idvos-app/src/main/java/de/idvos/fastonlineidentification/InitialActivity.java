package de.idvos.fastonlineidentification;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import de.idvos.fastonlineidentification.activity.StartActivity;
import de.idvos.fastonlineidentification.sdk.IdentificationResult;

/**
 * Entry point to the Application
 */
public class InitialActivity extends Activity{

    private static final int START_ACTIVITY_REQUEST_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startActivityForResult(
                StartActivity.getIntent(this, false),
                START_ACTIVITY_REQUEST_CODE
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_CANCELED && data.hasExtra(IdentificationResult.IDENTIFICATION_RESULT)){
            startActivityForResult(
                    StartActivity.getIntent(this, true),
                    START_ACTIVITY_REQUEST_CODE
            );
        }
    }
}
