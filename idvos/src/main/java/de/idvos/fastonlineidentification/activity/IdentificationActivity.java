package de.idvos.fastonlineidentification.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
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
import de.idvos.fastonlineidentification.InstructionBar.Instruction;
import de.idvos.fastonlineidentification.PusherManager;
import de.idvos.fastonlineidentification.PusherManager.PusherCallback;
import de.idvos.fastonlineidentification.TokBoxManager;
import de.idvos.fastonlineidentification.TokBoxManager.TokBoxCallback;
import de.idvos.fastonlineidentification.WaitingTimeTracker;
import de.idvos.fastonlineidentification.config.AppConfig;
import de.idvos.fastonlineidentification.network.JsonObjectRequest;
import de.idvos.fastonlineidentification.network.JsonObjectRequest.JsonObjectRequestCallback;
import de.idvos.fastonlineidentification.network.NetworkRequest;
import de.idvos.fastonlineidentification.network.RequestQueue;
import de.idvos.fastonlineidentification.network.VoidRequest;
import de.idvos.fastonlineidentification.network.VoidRequest.VoidRequestResultCallback;
import de.idvos.fastonlineidentification.sdk.IdentificationResult;
import de.idvos.fastonlineidentification.sdk.IdvosSDK;
import de.idvos.fastonlineidentification.sdk.R;
import de.idvos.fastonlineidentification.view.ProgressBarDeterminate;
import de.idvos.fastonlineidentification.view.TANInput;
import de.idvos.fastonlineidentification.view.TANInput.OnCheckTANListener;

public class IdentificationActivity extends BaseActivity implements PusherCallback, TokBoxCallback, OnClickListener,
        OnCheckTANListener {

    private static final String TAG = "IdentificationActivity";
    private static final String TAN_CODE = "tan_code";

    private TextView mTextFlow;
    private InstructionBar mInstructionBar;
    private RelativeLayout mFrameSend;
    private RelativeLayout mFrameRecieve;
    private TextView mFrameRecieveTV;
    private TANInput mTANInput;
    private ProgressDialog mProgressDialog;
    private RequestQueue mRequestQueue;
    private Progress mProgress;
    private int mState;
    private String serverUrl;
    private WaitingTimeTracker waitingTimeTracker;

    ProgressBarDeterminate mProgressBarDeterminate;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identification);

        serverUrl = IdvosSDK.getInstance().getMode().getEndpoint() + "api/v1/mobile/";


        setMenuButton(R.drawable.ic_action_navigation_close, true);
        setMenuButton(R.drawable.ic_action_helpbutton, false);

        mInstructionBar = new InstructionBar(this);

        mTextFlow = (TextView) findViewById(R.id.text_instruction);
        mProgressBarDeterminate = (ProgressBarDeterminate) findViewById(R.id.indicatorProgress);
        mFrameSend = (RelativeLayout) findViewById(R.id.frame_send);
        mFrameRecieve = (RelativeLayout) findViewById(R.id.frame_recieve);
        mFrameRecieveTV = (TextView) findViewById(R.id.frame_receive_tv);

        mTANInput = (TANInput) findViewById(de.idvos.fastonlineidentification.sdk.R.id.tan);
        mTANInput.setOnCheckTANListener(this);

        findViewById(R.id.button_swap_camera).setOnClickListener(this);
        findViewById(R.id.button_swap_light).setOnClickListener(this);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage(getString(R.string.idvos_please_wait_simple));

        Intent intent = getIntent();
        mProgress = intent.getParcelableExtra(Progress.KEY_IDENTIFICATION_PROGRESS);

        mRequestQueue = RequestQueue.getInstance();

        mState = mProgress.getmState();
        mInstructionBar.hideMask();
        mTextFlow.setText(getString(R.string.idvos_please_wait));

        PusherManager.getInstance(this);

        waitingTimeTracker = new WaitingTimeTracker(this);
    }

    protected static Intent getIntent(Context context, Progress progress) {
        Intent intent = new Intent(context, IdentificationActivity.class);

        intent.putExtra(Progress.KEY_IDENTIFICATION_PROGRESS, progress);

        return intent;
    }

    @Override
    protected void onLeftMenuButtonClicked() {
        showWarningDialog();
    }

    @Override
    public void onBackPressed() {
        showWarningDialog();
    }

    private void showWarningDialog() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder
                .setMessage(R.string.idvos_cancel_session_confirm)
                .setPositiveButton(R.string.idvos_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        waitingTimeTracker.endTracking();

                        TokBoxManager.getInstance(IdentificationActivity.this, IdentificationActivity.this).finishSession();
                        PusherManager.getInstance(IdentificationActivity.this).disconnect();

                        IdentificationResult identificationResult = new IdentificationResult(
                                false,
                                null,
                                new WaitingTimeTracker(IdentificationActivity.this).getWaitingTimeMillis()
                        );

                        Intent result = new Intent();
                        result.putExtra(
                                IdentificationResult.IDENTIFICATION_RESULT,
                                identificationResult
                        );
                        setResult(RESULT_CANCELED, result);

                        finish();
                    }
                })
                .setNegativeButton(R.string.idvos_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create()
                .show();


//        onShowTanInstruction();
        mInstructionBar.hideMask();
    }


    @Override
    protected void onRightMenuButtonClicked() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle(R.string.idvos_identification_cancel_title).setMessage(R.string.idvos_identification_cancel_message).setPositiveButton(R.string.idvos_identification_cancel_positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse(getString(R.string.idvos_phone))));
            }
        }).setNegativeButton(R.string.idvos_identification_cancel_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).create().show();
        mInstructionBar.showMask();


    }

    @Override
    protected void onStart() {
        super.onStart();
        followProgress();
    }

    @Override
    public void onClick(View v) {
        TokBoxManager manager = TokBoxManager.getInstance(this, this);

        int id = v.getId();
        if (id == R.id.button_swap_camera) {
            manager.swapCamera();
            return;
        }
        if (id == R.id.button_swap_light) {
            if (manager.isFrontCam()) {
                Toast.makeText(IdentificationActivity.this, R.string.idvos_no_flashlight, Toast.LENGTH_SHORT).show();
            } else {
                manager.toggleFlashLight();
            }
        }
    }


    private void followProgress() {
        mProgress.updateState(mState);

        switch (mState) {
            case Progress.STATE_START:
                mProgressDialog.show();
                flowStart();
                return;
            case Progress.STATE_IDENTIFICATION_RETRIEVED:
//                mTextFlow.setText("Hash info received");
                startPusher();
                return;
            case Progress.STATE_CONNECTING_TO_PUSHER:
                mTextFlow.setText(R.string.idvos_connecting_message);
//                mTextFlow.setText("Connecting to PUSHER");
                return;
            case Progress.STATE_CONNECTED_TO_PUSHER:
//                mTextFlow.setText("Connected to pusher");
//                mTextFlow.setText("Connected to pusher");
                startTokBox();
                return;
            case Progress.STATE_RETURN_FROM_LOGIN:
                TokBoxManager.getInstance(this, this).startTransmitting(mFrameSend);
                return;
            case Progress.STATE_CONNECTING_TO_TOKBOX:
                mTextFlow.setText(R.string.idvos_connecting_message);
                return;
            case Progress.STATE_CONNECTED_TO_TOKBOX:
//                mTextFlow.setText("TokBox Connected");
//                onTokBoxConnected();
                signalReady();
                return;
            case Progress.STATE_SIGNALING_READY:
                new WaitingTimeTracker(this).startTracking();
                return;
            case Progress.STATE_READY:
//                mTextFlow.setText("Wir bitten um ein wenig Geduld. Ihre Identifizierung wird gleich gestartet..");
                mProgressDialog.dismiss();
                return;
            case Progress.STATE_FAILED:
                mProgressDialog.dismiss();
                Toast.makeText(IdentificationActivity.this, "Failed with result code:" + mProgress.getFailureReason(), Toast.LENGTH_SHORT).show();
                finish();
//                mTextFlow.setText("FAILED: " + mProgress.getFailureReason());
                return;
        }
    }

    @Override
    public void publishingStarted() {
        mState = Progress.STATE_CONNECTED_TO_TOKBOX;
        followProgress();
    }

    private void signalReady() {
        VoidRequest request = new VoidRequest(
                serverUrl + "identifications/ready",
                NetworkRequest.METHOD_PUT,
                new VoidRequestResultCallback() {

                    @Override
                    public void onResult(int statusCode, Void result, NetworkRequest<Void> request) {
                        if (statusCode == 202) {
                            mState = Progress.STATE_READY;
                        } else if (statusCode == 400) {
                            mState = Progress.STATE_FAILED;
                            mProgress.setFailureReason(Progress.FAIL_REASON_INVALID_P2P_SESSION_ID);
                        } else if (statusCode == 404) {
                            mState = Progress.STATE_FAILED;
                            mProgress.setFailureReason(Progress.FAIL_REASON_INVALID_IDENTIFICATION);
                        } else if (statusCode >= 500) {
                            mState = Progress.STATE_FAILED;
                            mProgress.setFailureReason(Progress.FAIL_REASON_SERVER_ERROR);
                        } else {
                            mState = Progress.STATE_FAILED;
                            mProgress.setFailureReason(Progress.FAIL_REASON_OTHER);
                        }

                        followProgress();
                    }

                    @Override
                    public void onError(int statusCode, Exception exception, NetworkRequest<Void> request) {
                        Log.e(TAG, "Failed to signal ready", exception);
                        onResult(statusCode, null, request);
                    }

                });
        try {
            mProgress.addSingalReadyData(request);
        } catch (JSONException e) {
        }
        mRequestQueue.addRequestToQueue(request);

        mState = Progress.STATE_SIGNALING_READY;
        followProgress();
    }

    private void startTokBox() {
        mState = Progress.STATE_CONNECTING_TO_TOKBOX;
        TokBoxManager.getInstance(this, this).connect(this, mProgress.getTokBoxSessionId(), mProgress.getTokBoxToken());
//        TokBoxManager.getInstance(this, this).swapCamera();

        followProgress();
    }

    @Override
    public ViewGroup getSubscribtionFrame() {
        mFrameRecieve.setVisibility(View.VISIBLE);
        mInstructionBar.showMask();
        return mFrameRecieve;
    }

    @Override
    public void onTokBoxConnected() {
        TokBoxManager.getInstance(this, this).startTransmitting(mFrameSend);
    }

    @Override
    public void onTokBoxError(Exception e) {
        mState = Progress.STATE_FAILED;
        mProgress.setFailureReason(Progress.FAIL_REASON_TOKBOX_ERROR);
        followProgress();
    }

    private void startPusher() {
        try {
            PusherManager.getInstance(this).connect(mProgress.getProgressChannel());
            mState = Progress.STATE_CONNECTING_TO_PUSHER;
        } catch (Exception e) {
            e.printStackTrace();
            mState = Progress.STATE_FAILED;
        }

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
    public void onPusherInstruction(Instruction instruction) {
        mInstructionBar.showInstruction(instruction);

        TokBoxManager manager = TokBoxManager.getInstance(this, this);
        if (instruction.getDialogId() == 1) {
            mInstructionBar.showMask();

            mFrameRecieveTV.setVisibility(View.GONE);
            mProgressBarDeterminate.setProgress(20);
            manager.swapCamera(); /* MAIN */
        } else if (instruction.getDialogId() == 2) {
            mProgressBarDeterminate.setProgress(40);

        } else if (instruction.getDialogId() == 3) {
//            manager.setFlashMode(true); /* ON */
            manager.toggleFlashLight(true);
            mProgressBarDeterminate.setProgress(60);

        } else if (instruction.getDialogId() == 4) {
            mInstructionBar.hideMask();

            mProgressBarDeterminate.setProgress(80);

            manager.toggleFlashLight(false);

//            manager.setFlashMode(false); /* OFF*/
            manager.swapCamera(); /*FRONT*/
        } else if (instruction.getDialogId() == 5) {
            mProgressBarDeterminate.setProgress(100);
            manager.swapCamera(); /*MAIN*/
        }/*else {
            manager.swapCamera(); *//*FRONT*//*
        }*/
    }

    @Override
    public void onShowTanInstruction() {
        mInstructionBar.showTanInstruction(getResources());
        TokBoxManager manager = TokBoxManager.getInstance(this, this);
        manager.swapCamera(); /*FRONT*/
        mTextFlow.setText(getString(R.string.idvos_taninput_title));
        Log.e("onShowTanInstruction", mProgress.getmState() + "");

    }

    @Override
    public void onTanGenerated() {
        startActivityForResult(SummaryActivity.getIntent(this).putExtra("hash", mProgress.getIdentificationHash()), 1337);
        mTextFlow.setText(getString(R.string.idvos_identification_enter_tan));
        mInstructionBar.showTanDialog(getResources());
        mTANInput.clean();
        Log.e("onTanGenerated", mProgress.getmState() + "");

    }

    @Override
    public void onCheckTAN(String tan) {
        mTANInput.setLoading();

        JsonObjectRequest request = new JsonObjectRequest(
                serverUrl + "identifications/" + mProgress.getIdentificationHash() + "/check",
                NetworkRequest.METHOD_PUT,
                new JsonObjectRequestCallback() {

                    @Override
                    public void onResult(int statusCode, JSONObject result, NetworkRequest<JSONObject> request) {
                        if (statusCode == 200) {
                            try {
                                boolean validity = mProgress.parseTanResponse(result);
                                mTANInput.setTanValidity(validity, false);
//                                onUserVerificationResult(validity);
                            } catch (JSONException e) {
                                onError(statusCode, e, request);
                            }
                        } else {
                            onError(statusCode, new Exception("Unexpected statusCode"), request);
                        }
                    }

                    @Override
                    public void onError(int statusCode, Exception exception, NetworkRequest<JSONObject> request) {
                        Log.e(TAG, "Checking TAN failed", exception);
                        mTANInput.setTanValidity(true, true);
                    }

                }
        );
        try {
            JSONObject jEntity = new JSONObject();
            jEntity.put(TAN_CODE, tan);
            request.setJsonEntity(jEntity);
        } catch (JSONException e) {
        }
        mRequestQueue.addRequestToQueue(request);
    }

    @Override
    public void onTanResult(boolean successful) {
        Log.w(TAG, "Unhandled pusher message on TAN result. Successful: " + successful);

        if (successful) {
//            mProgress.updateState(Progress.STATE_VERIFIED);
//            Intent intent = VerificationResultActivity.getIntent(this, mProgress);
//            startActivityForResult(intent, 0);
        } else {
            if (mTANInput.getFailTime() > 1) {
                mProgress.updateState(Progress.STATE_FAILED);
                Intent intent = VerificationResultActivity.getIntent(this, mProgress);
                startActivityForResult(intent, 0);
            }
        }
       /* else {
            mProgress.setFailureReason(Progress.FAIL_REASON_UNVERIFIED);
        }*/


    }

    @Override
    public void onUserVerificationResult(boolean verified) {
        if (verified) {
            mProgress.updateState(Progress.STATE_VERIFIED);
        } else {
            mProgress.setFailureReason(Progress.FAIL_REASON_UNVERIFIED);
        }

        Intent intent = VerificationResultActivity.getIntent(this, mProgress);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1337) {
            mTextFlow.setText(getString(R.string.idvos_taninput_title));
        } else {
            Intent result = new Intent();
            result.putExtra(Progress.KEY_IDENTIFICATION_PROGRESS, mProgress);
            setResult(RESULT_OK, result);
            finish();
        }

    }

    @Override
    public void onPusherError(String message, String code, Exception e) {

    }

    @Override
    public void onPusherChannelAuthenticationFailure(String message, Exception e) {
        Log.e(TAG, message, e);

        mState = Progress.STATE_FAILED;
        mProgress.setFailureReason(Progress.FAIL_REASON_PUSHER_AUTHENTICATION);
        followProgress();
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

                if (error.networkResponse == null || error.networkResponse.statusCode == 401) {
                    mProgress.setFailureReason(Progress.FAIL_REASON_INVALID_IDENTIFICATION);
                } else if (error.networkResponse.statusCode == 404) {
                    mProgress.setFailureReason(Progress.FAIL_REASON_INVALID_IDENTIFICATION);
                } else if (error.networkResponse.statusCode == 400) {
                    mProgress.setFailureReason(Progress.FAIL_REASON_INVALID_INPUT);
                } else if (error.networkResponse.statusCode >= 500) {
                    mProgress.setFailureReason(Progress.FAIL_REASON_SERVER_ERROR);
                } else {
                    mProgress.setFailureReason(Progress.FAIL_REASON_OTHER);
                }

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


        AppConfig.getInstance().addToRequestQueue(request);
        mState = Progress.STATE_RETRIEVING_IDENTIFICATION;


        /*ZStringRequest request = new ZStringRequest(
                SERVER_URL + "identifications",
				NetworkRequest.METHOD_PUT,
				new ZStringRequest.StringRequestCallback() {

                    @Override
                    public void onResult(int statusCode, String result, NetworkRequest<String> request) {
                        Log.i(TAG, result.toString());

                        try {
                            mProgress.parseRetrieveIdentification(new JSONObject(result));
                            mState = Progress.STATE_IDENTIFICATION_RETRIEVED;
                        } catch (JSONException e) {
                            Log.e(TAG, "", e);

                            mState = Progress.STATE_FAILED;
                            mProgress.setFailureReason(Progress.FAIL_REASON_OTHER);
                        }

                        followProgress();
                    }
			@Override
			public void onError(int statusCode, Exception exception, NetworkRequest<String> request) {
				Log.e(TAG, "", exception);

				mState = Progress.STATE_FAILED;

				if (statusCode==401) {
					mProgress.setFailureReason(Progress.FAIL_REASON_INVALID_IDENTIFICATION);
				}
				else if (statusCode==400) {
					mProgress.setFailureReason(Progress.FAIL_REASON_INVALID_INPUT);
				}
				else if (statusCode>=500) {
					mProgress.setFailureReason(Progress.FAIL_REASON_SERVER_ERROR);
				}
				else {
					mProgress.setFailureReason(Progress.FAIL_REASON_OTHER);
				}

				followProgress();
			}
			
		});*/
//		try {
//			mProgress.addRetrieveIdentification(request);
//		} catch (JSONException e) { }
//		mRequestQueue.addRequestToQueue(request);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            PusherManager.getInstance(this).disconnect();
            TokBoxManager.getInstance(this, this).finishSession();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
