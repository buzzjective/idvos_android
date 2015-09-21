package de.idvos.fastonlineidentification.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import de.idvos.fastonlineidentification.InstructionBar;
import de.idvos.fastonlineidentification.PusherManager;
import de.idvos.fastonlineidentification.sdk.R;
import de.idvos.fastonlineidentification.TokBoxManager;
import de.idvos.fastonlineidentification.config.AppConfig;
import de.idvos.fastonlineidentification.sdk.IdvosSDK;

public class LoginActivity extends BaseActivity implements OnClickListener, PusherManager.PusherCallback, TokBoxManager.TokBoxCallback {

    private String serverUrl;

    protected static Intent getIntent(Context context, Progress progress) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.putExtra(Progress.KEY_IDENTIFICATION_PROGRESS, progress);
        return intent;
    }

    private static final String TAG = "LoginActivity";


    private View mButtonLastNameInfo;
    private View mButtonCodeInfo;
    private View mButtonStart;
    private EditText mEditLastName;
    private EditText mEditCode;

    private Progress mProgress;
    private ProgressDialog mProgressDialog;


    Handler mHandler = new Handler();
    Runnable mRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        serverUrl = IdvosSDK.getInstance().getMode().getEndpoint() + "api/v1/mobile/";

        setContentView(de.idvos.fastonlineidentification.sdk.R.layout.activity_login);

        mButtonLastNameInfo = findViewById(R.id.button_info_lastname);
        mButtonCodeInfo = findViewById(R.id.button_info_code);
        mButtonStart = findViewById(R.id.button_start);
        mEditLastName = (EditText) findViewById(R.id.edit_lastname);
        mEditCode = (EditText) findViewById(R.id.edit_code);

        mButtonLastNameInfo.setOnClickListener(this);
        mButtonCodeInfo.setOnClickListener(this);
        mButtonStart.setOnClickListener(this);

        setMenuButton(R.drawable.ic_action_bac, true);

        Intent intent = getIntent();
        mProgress = intent.getParcelableExtra(Progress.KEY_IDENTIFICATION_PROGRESS);


        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage(getString(R.string.idvos_please_wait_simple));


        findViewById(R.id.button_call).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse(getString(R.string.idvos_phone))));
            }
        });


        mRunnable = new Runnable() {
            @Override
            public void run() {
                mProgressDialog.dismiss();
                Toast.makeText(LoginActivity.this, R.string.idvos_timeout_error, Toast.LENGTH_LONG).show();
                setResult(RESULT_CANCELED);
                finish();

            }
        };

    }

    @Override
    protected void onLeftMenuButtonClicked() {
        onBackPressed();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.button_info_lastname) {
            Toast.makeText(LoginActivity.this, R.string.idvos_surname_request, Toast.LENGTH_SHORT).show();
            return;
        }

        if (id == R.id.button_info_code) {
            Toast.makeText(LoginActivity.this, R.string.idvos_reference_number_request, Toast.LENGTH_SHORT).show();
            return;
        }

        if (id == R.id.button_start) {
            mHandler.postDelayed(mRunnable, 2 * 60 * 1000);

            PusherManager.getInstance(this).disconnect();
            TokBoxManager.getInstance(this, this).finishSession();

            String lastName = mEditLastName.getText().toString().trim();
            String shortCode = mEditCode.getText().toString().trim();

            if (lastName.equals("") || shortCode.equals("")) {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(LoginActivity.this);
                alertBuilder.setMessage(R.string.idvos_incorrect_data).setPositiveButton(R.string.idvos_ok, null).setCancelable(true).create().show();
                return;
            }

            mProgress.setIdentificationWithCode(lastName, shortCode);
            mState = Progress.STATE_START;
            followProgress();
        }
    }

    private int mState;

    private void followProgress() {
        mProgress.updateState(mState);

        switch (mState) {
            case Progress.STATE_START:

                mProgressDialog.show();
                flowStart();
                Log.e("LoginActivity", "Progress.STATE_START");
                return;
            case Progress.STATE_IDENTIFICATION_RETRIEVED:
                Log.e("LoginActivity", "STATE_IDENTIFICATION_RETRIEVED");

                startPusher();

                return;
            case Progress.STATE_CONNECTING_TO_PUSHER:
                Log.e("LoginActivity", "STATE_CONNECTING_TO_PUSHER");

                return;
            case Progress.STATE_CONNECTED_TO_PUSHER:
                Log.e("LoginActivity", "STATE_CONNECTED_TO_PUSHER");

                startTokBox();
                return;
            case Progress.STATE_CONNECTING_TO_TOKBOX:
                Log.e("LoginActivity", "STATE_CONNECTING_TO_TOKBOX");

//                mTextFlow.setText("Verbindung herstellen...");
                return;
            case Progress.STATE_CONNECTED_TO_TOKBOX:
                Log.e("LoginActivity", "STATE_CONNECTED_TO_TOKBOX");

                mHandler.removeCallbacks(mRunnable);
                mHandler.removeCallbacksAndMessages(null);

//                mTextFlow.setText("TokBox Connected");
                mProgressDialog.dismiss();
                Intent data = new Intent();
                mProgress.updateState(Progress.STATE_RETURN_FROM_LOGIN);
                data.putExtra(Progress.KEY_IDENTIFICATION_PROGRESS, mProgress);
                setResult(RESULT_OK, data);
                finish();
                return;

            case Progress.STATE_FAILED:
                Log.e("LoginActivity", "STATE_FAILED");
                mHandler.removeCallbacks(mRunnable);

                mProgressDialog.dismiss();
                Toast.makeText(LoginActivity.this, "Failed :-(", Toast.LENGTH_SHORT).show();
                return;
        }
    }

    private void flowStart() {


        StringRequest request = new StringRequest(Request.Method.PUT, serverUrl + "identifications", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i(TAG, response);

                try {
                    mProgress.parseRetrieveIdentification(new JSONObject(response));
                    mState = Progress.STATE_IDENTIFICATION_RETRIEVED;
                } catch (JSONException e) {
                    Log.e(TAG, "", e);

                    mState = Progress.STATE_FAILED;
                    mProgress.setFailureReason(Progress.FAIL_REASON_OTHER);
                }

                followProgress();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                mState = Progress.STATE_FAILED;
                mProgress.setFailureReason(Progress.FAIL_REASON_OTHER);
                followProgress();

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> paramsMap = new HashMap<>();
                mProgress.addRetrieveIdentification(paramsMap);
                return paramsMap;
            }
        };

        AppConfig.getInstance().getRequestQueue().getCache().invalidate(serverUrl + "identifications", true);
        AppConfig.getInstance().addToRequestQueue(request);
        mState = Progress.STATE_RETRIEVING_IDENTIFICATION;
    }

    private void startPusher() {
        try {
            PusherManager.getInstance(this).connect(mProgress.getProgressChannel());
            mState = Progress.STATE_CONNECTING_TO_PUSHER;
        } catch (Exception e) {
            e.printStackTrace();
//            mState = Progress.STATE_FAILED;
        }

        followProgress();
    }

    private void startTokBox() {
        mState = Progress.STATE_CONNECTING_TO_TOKBOX;
        TokBoxManager.getInstance(this, this).connect(this, mProgress.getTokBoxSessionId(), mProgress.getTokBoxToken());
        followProgress();
    }


    @Override
    public void onPusherConnected() {

    }

    @Override
    public void onPusherChannelConnected(String channelName) {
        mState = Progress.STATE_CONNECTED_TO_PUSHER;
        followProgress();
    }

    @Override
    public void onPusherChannelAuthenticationFailure(String message, Exception e) {
        e.printStackTrace();
        mProgressDialog.dismiss();
        Toast.makeText(LoginActivity.this, "Pusher failed to connect", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPusherInstruction(InstructionBar.Instruction instruction) {

    }

    @Override
    public void onShowTanInstruction() {

    }

    @Override
    public void onTanGenerated() {

    }

    @Override
    public void onTanResult(boolean successful) {

    }

    @Override
    public void onUserVerificationResult(boolean verified) {

    }

    @Override
    public void onTokBoxConnected() {
        mState = Progress.STATE_CONNECTED_TO_TOKBOX;
        followProgress();
    }

    @Override
    public void onPusherError(String message, String code, Exception e) {
        e.printStackTrace();
        mProgressDialog.dismiss();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                Toast.makeText(LoginActivity.this, "Pusher failed to connect", Toast.LENGTH_SHORT).show();

            }
        });

        try {
            PusherManager.getInstance(this).disconnect();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void onTokBoxError(Exception e) {
        e.printStackTrace();
        mProgressDialog.dismiss();
        Toast.makeText(LoginActivity.this, "Tokbox failed to connect", Toast.LENGTH_SHORT).show();

        try {
            TokBoxManager.getInstance(this, this).finishSession();
        } catch (Exception e1) {
            e1.printStackTrace();
        }


    }

    @Override
    public void publishingStarted() {

    }

    @Override
    public ViewGroup getSubscribtionFrame() {
        return null;
    }
}
