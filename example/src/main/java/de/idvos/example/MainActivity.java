package de.idvos.example;

import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import de.idvos.fastonlineidentification.sdk.IdentificationResult;
import de.idvos.fastonlineidentification.sdk.IdvosSDK;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View button = findViewById(R.id.start_identification_btn);
        final TextView editText = (TextView) findViewById(R.id.hash_edt);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CharSequence text = editText.getText();
                if(TextUtils.isEmpty(text)){
                    return;
                }
                IdvosSDK.getInstance().startIdentification(
                        MainActivity.this,
                        REQUEST_CODE,
                        text.toString()
                );
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_CODE){
            return;
        }
        IdentificationResult result = data.getParcelableExtra(IdentificationResult.IDENTIFICATION_RESULT);
        Toast.makeText(this,
                "Success: " + String.valueOf(result.isSuccessful() + "; " +
                "Transaction ID: " + result.getTransactionId()) + "; " +
                "Waiting time: " + result.getWaitingTimeMillis() / 1000,
                Toast.LENGTH_LONG
        ).show();
    }
}
