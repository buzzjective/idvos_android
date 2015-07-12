package de.idvos.fastonlineidentification;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.util.Log;

import com.pusher.client.Pusher;
import com.pusher.client.PusherOptions;
import com.pusher.client.channel.PrivateChannelEventListener;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;
import com.pusher.client.util.HttpAuthorizer;

import de.idvos.fastonlineidentification.InstructionBar.Instruction;
import de.idvos.fastonlineidentification.activity.IdentificationActivity;
import de.idvos.fastonlineidentification.sdk.IdvosSDK;

public class PusherManager implements ConnectionEventListener, PrivateChannelEventListener {
	
	private static final String TAG = "PusherManager";

	private static final String EVENT_SESSION_SHOW_DIALOG = "show_dialog_instruction";
	private static final String EVENT_SHOW_TAN_INSTRUCTION = "show_second_instruction";
	private static final String EVENT_TAN_GENERATED = "show_tan_instruction";
	private static final String EVENT_TAN_SUCCESSFUL = "successful_tan_code";
	private static final String EVENT_TAN_FAILED = "failed_tan_attemp";
	private static final String EVENT_USER_VERIFIED = "user_verified";
	private static final String EVENT_USER_NOT_VERIFIED = "user_unidentified";
	
	private static PusherManager mInstance = null;

	public static interface PusherCallback {
		
		public void onPusherConnected();
		public void onPusherChannelConnected(String channelName);
		public void onPusherChannelAuthenticationFailure(String message, Exception e);
		
		public void onPusherInstruction(Instruction instruction);
		public void onShowTanInstruction();
		public void onTanGenerated();
		public void onPusherError(String message, String code, Exception e);
		public void onTanResult(boolean successful);
		public void onUserVerificationResult(boolean verified);
		
	}
	
	public static PusherManager getInstance(PusherCallback callback) {
		if (mInstance==null) {
			mInstance = new PusherManager(callback);
		}else{
            if(callback != null){
                mInstance.setmCallback(callback);
            }
        }
		
		return mInstance;
	}

    public void setmCallback(PusherCallback mCallback) {
        this.mCallback = mCallback;
    }

    private Pusher mPusher = null;
	private ConnectionState mConnectionState = null;
	private PusherCallback mCallback;
	
	private String mChannelName;
	
	private Handler mHandler;
	
	private PusherManager(PusherCallback callback) {
		mCallback = callback;
		mHandler = new Handler();
		
		PusherOptions options = new PusherOptions();
		options.setEncrypted(true);

		IdvosSDK.Mode mode = IdvosSDK.getInstance().getMode();

		options.setAuthorizer(new HttpAuthorizer(
				mode.getEndpoint() + "api/v1/mobile/pusher/auth"
		));
		
		mPusher = new Pusher(mode.getPublicApiKey(), options);
	}


    public void disconnect(){
        if(mPusher != null) {
			mPusher.disconnect();

            PusherOptions options = new PusherOptions();
            options.setEncrypted(true);

            IdvosSDK.Mode mode = IdvosSDK.getInstance().getMode();

            options.setAuthorizer(new HttpAuthorizer(
                    mode.getEndpoint() + "api/v1/mobile/pusher/auth"
            ));

            mPusher = new Pusher(mode.getPublicApiKey(), options);
		}
    }
	public void connect(String channelName) throws Exception{
		Log.i(TAG, "Subscribing to channel: " + channelName);


		switch (mPusher.getConnection().getState()) {
		case DISCONNECTED:
			mChannelName = channelName;
			mPusher.connect(this);
			mPusher.subscribePrivate(channelName, this,
					EVENT_SESSION_SHOW_DIALOG,
					EVENT_SHOW_TAN_INSTRUCTION,
					EVENT_TAN_FAILED,
					EVENT_TAN_GENERATED,
					EVENT_TAN_SUCCESSFUL,
					EVENT_USER_NOT_VERIFIED,
					EVENT_USER_VERIFIED
					);
			return;
		case CONNECTING:
		case CONNECTED:
			if (!mChannelName.equals(channelName)) {
				mPusher.unsubscribe(mChannelName);
				mPusher.disconnect();
				
				mChannelName = channelName;
				
				mPusher.connect(this);

				mPusher.subscribePrivate(channelName, this,
						EVENT_SESSION_SHOW_DIALOG,
						EVENT_SHOW_TAN_INSTRUCTION,
						EVENT_TAN_FAILED,
						EVENT_TAN_GENERATED,
						EVENT_TAN_SUCCESSFUL,
						EVENT_USER_NOT_VERIFIED,
						EVENT_USER_VERIFIED
						);
			}
			return;
		case DISCONNECTING:
			mPusher.unsubscribe(mChannelName);
			
			mChannelName = channelName;
			
			mPusher.connect(this);
			mPusher.subscribePrivate(channelName, this,
					EVENT_SESSION_SHOW_DIALOG,
					EVENT_SHOW_TAN_INSTRUCTION,
					EVENT_TAN_FAILED,
					EVENT_TAN_GENERATED,
					EVENT_TAN_SUCCESSFUL,
					EVENT_USER_NOT_VERIFIED,
					EVENT_USER_VERIFIED
					);
			return;
		case ALL:
			Log.e(TAG, "Unexpected pusher connection state: ALL");
			break;
		}
	}

	@Override
	public void onConnectionStateChange(final ConnectionStateChange change) {
		mConnectionState = change.getCurrentState();
		Log.i(TAG, "pusher state change: " + mConnectionState.name());
	}

	@Override
	public void onError(String message, String code, Exception e) {
		Log.e(TAG, message, e);
        if(mCallback != null)
            mCallback.onPusherError(message, code, e);
	}

	@Override
	public void onSubscriptionSucceeded(final String channelName) {
		Log.i(TAG, "Subscribed to channel: " + channelName);
		mHandler.post(new Runnable() {
			
			@Override
			public void run() {
				mCallback.onPusherChannelConnected(channelName);
			}
			
		});
	}

	@Override
	public void onEvent(final String channelName, final String eventName, final String data) {
		Log.i(TAG, "event: " + eventName + "\n" + data);
		if (EVENT_SESSION_SHOW_DIALOG.equals(eventName)) {
			try {
				JSONObject jData = new JSONObject(data);
				final Instruction instruction = Instruction.parseInstruction(jData);

				mHandler.post(new Runnable() {
					
					@Override
					public void run() {
						mCallback.onPusherInstruction(instruction);
					}
					
				});
				
			} catch (JSONException e) {
				Log.e(TAG, "Event data was not as expected: " + data, e);
			}
		}
		else if (EVENT_SHOW_TAN_INSTRUCTION.equals(eventName)) {

			mHandler.post(new Runnable() {
				
				@Override
				public void run() {
					mCallback.onShowTanInstruction();
				}
				
			});
			
		}
		else if (EVENT_TAN_GENERATED.equals(eventName)) {
			
			mHandler.post(new Runnable() {
				
				@Override
				public void run() {
					mCallback.onTanGenerated();
				}
				
			});
			
		}
		else if (EVENT_TAN_SUCCESSFUL.equals(eventName)) {
			
			mHandler.post(new Runnable() {
				
				@Override
				public void run() {
					mCallback.onTanResult(true);
				}
				
			});
			
		}
		else if (EVENT_TAN_FAILED.equals(eventName)) {
			
			mHandler.post(new Runnable() {
				
				@Override
				public void run() {
					mCallback.onTanResult(false);
				}
				
			});
			
		}
		else if (EVENT_USER_VERIFIED.equals(eventName)) {
			
			mHandler.post(new Runnable() {
				
				@Override
				public void run() {
					mCallback.onUserVerificationResult(true);
				}
				
			});
			
		}
		else if (EVENT_USER_NOT_VERIFIED.equals(eventName)) {
			
			mHandler.post(new Runnable() {
				
				@Override
				public void run() {
					mCallback.onUserVerificationResult(false);
				}
				
			});
		}
	}

	@Override
	public void onAuthenticationFailure(final String message, final Exception e) {
		mHandler.post(new Runnable() {
			
			@Override
			public void run() {
				mCallback.onPusherChannelAuthenticationFailure(message, e);
			}
			
		});
	}

}
