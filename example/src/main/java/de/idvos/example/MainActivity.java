package de.idvos.example;

import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import de.idvos.fastonlineidentification.sdk.IdentificationResult;
import de.idvos.fastonlineidentification.sdk.IdvosSDK;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 100;
    private static final String HASH = "mxqC1jsvV4uaL_zRtFn7";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View button = findViewById(R.id.start_identification_btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IdvosSDK.getInstance().startIdentification(MainActivity.this, REQUEST_CODE, HASH);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_CODE || resultCode != RESULT_OK){
            return;
        }
        IdentificationResult result = data.getParcelableExtra(IdentificationResult.IDENTIFICATION_RESULT);
        Toast.makeText(this,
                "Success: " + String.valueOf(result.isSuccessful() + "; " +
                "Transaction ID: " + result.getTransactionId()),
                Toast.LENGTH_LONG
        ).show();
    }
}
