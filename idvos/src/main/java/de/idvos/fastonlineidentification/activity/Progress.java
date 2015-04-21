package de.idvos.fastonlineidentification.activity;

import org.json.JSONException;
import org.json.JSONObject;

import de.idvos.fastonlineidentification.network.JsonObjectRequest;
import de.idvos.fastonlineidentification.network.VoidRequest;
import de.idvos.fastonlineidentification.network.ZStringRequest;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.Map;

public class Progress implements Parcelable {

	private static final String TAG = "Progress";
	
	protected static final String KEY_IDENTIFICATION_PROGRESS = "progress";
	
	private static final String IDENTIFICATION_HASH = "identification_hash";
	private static final String LAST_NAME = "last_name";
	private static final String SHORT_CODE = "short_code";
	private static final String P2P_SESSION_ID = "p2p_session_id";
	private static final String P2P_TOKEN = "p2p_token";
	private static final String PROGRESS_CHANNEL = "progress_channel";
	private static final String CHAT_CHANNEL = "chat_channel";
	private static final String TRANSACTION_ID = "client_transaction_id";
	
	protected static final int STATE_START = 0;
	protected static final int STATE_FAILED = 1;
	protected static final int STATE_RETRIEVING_IDENTIFICATION = 2;
	protected static final int STATE_IDENTIFICATION_RETRIEVED = 3;
	protected static final int STATE_CONNECTING_TO_PUSHER = 4;
	protected static final int STATE_CONNECTED_TO_PUSHER = 5;
	protected static final int STATE_CONNECTING_TO_TOKBOX = 6;
	protected static final int STATE_CONNECTED_TO_TOKBOX = 7;
	protected static final int STATE_SIGNALING_READY = 8;
	protected static final int STATE_READY = 9;
	protected static final int STATE_VERIFIED = 10;
	protected static final int STATE_RETURN_FROM_LOGIN = 11;

	protected static final int FAIL_REASON_INVALID_IDENTIFICATION = 0;
	protected static final int FAIL_REASON_INVALID_INPUT = 1;
	protected static final int FAIL_REASON_SERVER_ERROR = 2;
	protected static final int FAIL_REASON_OTHER = 3;
	protected static final int FAIL_REASON_PUSHER_AUTHENTICATION = 4;
	protected static final int FAIL_REASON_TOKBOX_ERROR = 5;
	protected static final int FAIL_REASON_INVALID_P2P_SESSION_ID = 6;
	protected static final int FAIL_REASON_UNVERIFIED = 7;
	
	private String mIdentificationHash = null;
	private String mLastName = null;
	private String mShortCode = null;
	private String mTransactionId = null;
	private int mState = STATE_START;
	private int mFailureReason = -1;
	
	private String mP2PSessionId;
	private String mP2PToken;
	private String mProgressChannel;
	private String mChatChannel;
	
	public void setIdentificationHash(String identificationHash) {
		mIdentificationHash = identificationHash;
	}
	
	public void setIdentificationWithCode(String lastName, String shortCode) {
		mLastName = lastName;
		mShortCode = shortCode;
	}
	
	public boolean hasIdentificationData() {
		return mIdentificationHash!=null || (mLastName!=null && mShortCode!=null);
	}

    public int getmState() {
        return mState;
    }

    public void updateState(int state) {
		mState = state;
	}


	
	public boolean getVerificationResult() {
		if (mState==STATE_VERIFIED) {
			return true;
		}
		
		if (mFailureReason==FAIL_REASON_UNVERIFIED) {
			return false;
		}
		
		Log.w(TAG, "No result for verification. Defaulting to failure.");
		return false;
	}
	
	public void setFailureReason(int reason) {
		mFailureReason = reason;
	}
	
	public int getFailureReason() {
		return mFailureReason;
	}
	
	public String getTransactionId() {
		return mTransactionId;
	}
	
	protected String getIdentificationHash() {
		return mIdentificationHash;
	}
	
	protected String getTokBoxSessionId() {
		return mP2PSessionId;
	}
	
	protected String getTokBoxToken() {
		return mP2PToken;
	}
	
	protected String getProgressChannel() {
		return mProgressChannel;
	}
	
	protected void addSingalReadyData(VoidRequest request) throws JSONException {
		JSONObject jEntity = new JSONObject();
		
		jEntity.put(IDENTIFICATION_HASH, mIdentificationHash);
		jEntity.put(P2P_SESSION_ID, mP2PSessionId);
		
		request.setJsonEntity(jEntity);
	}
	
	protected void addRetrieveIdentification(JsonObjectRequest request) throws JSONException {
		JSONObject jEntity = new JSONObject();
		
		if (mIdentificationHash!=null) {
			jEntity.put(IDENTIFICATION_HASH, mIdentificationHash);
		}else
        {
			if (mLastName==null || mShortCode==null) {
				throw new RuntimeException("Last name or short code are not available.");
			}
			
			jEntity.put(LAST_NAME, mLastName);
			jEntity.put(SHORT_CODE, mShortCode);
//            jEntity.put(IDENTIFICATION_HASH, "EZBpfu7smfWEx51ByGS_");

        }

	}

    protected void addRetrieveIdentification(Map<String,String> paramsMap) {
		if (mIdentificationHash!=null) {
            paramsMap.put(IDENTIFICATION_HASH, mIdentificationHash);
		}else
        {
			if (mLastName==null || mShortCode==null) {
				throw new RuntimeException("Last name or short code are not available.");
			}
            paramsMap.put(LAST_NAME, mLastName);
            paramsMap.put(SHORT_CODE, mShortCode);
        }
	}



    protected void addRetrieveIdentification(ZStringRequest request) throws JSONException {
		JSONObject jEntity = new JSONObject();

		if (mIdentificationHash!=null) {
			jEntity.put(IDENTIFICATION_HASH, mIdentificationHash);
		}else
        {
			if (mLastName==null || mShortCode==null) {
				throw new RuntimeException("Last name or short code are not available.");
			}

			jEntity.put(LAST_NAME, mLastName);
			jEntity.put(SHORT_CODE, mShortCode);
//            jEntity.put(IDENTIFICATION_HASH, "EZBpfu7smfWEx51ByGS_");

        }

		request.setJsonEntity(jEntity);
	}


	
	protected void parseRetrieveIdentification(JSONObject jIdentification) throws JSONException {
		mIdentificationHash = jIdentification.getString(IDENTIFICATION_HASH);
		mP2PSessionId = jIdentification.getString(P2P_SESSION_ID);
		mP2PToken = jIdentification.getString(P2P_TOKEN);
		mProgressChannel = jIdentification.getString(PROGRESS_CHANNEL);
		mChatChannel = jIdentification.getString(CHAT_CHANNEL);
	}
	
	protected boolean parseTanResponse(JSONObject jTANResponse) throws JSONException {
		mTransactionId = null;
		
		if (jTANResponse.has(TRANSACTION_ID)) {
			mTransactionId = jTANResponse.getString(TRANSACTION_ID);
		}
		else {
			mTransactionId = null;
		}
		
		return mTransactionId!=null;
	}
	
	protected Progress() {
		
	}

	private Progress(Parcel source) {
		mIdentificationHash = source.readString();
		mLastName = source.readString();
		mShortCode = source.readString();
		mTransactionId = source.readString();
		mState = source.readInt();
		mFailureReason = source.readInt();
		mProgressChannel = source.readString();
		mP2PSessionId = source.readString();
		mP2PToken = source.readString();
		mChatChannel = source.readString();
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mIdentificationHash);
		dest.writeString(mLastName);
		dest.writeString(mShortCode);
		dest.writeString(mTransactionId);
		dest.writeInt(mState);
		dest.writeInt(mFailureReason);
		dest.writeString(mProgressChannel);
		dest.writeString(mP2PSessionId);
		dest.writeString(mP2PToken);
		dest.writeString(mChatChannel);
	}
	
	public static final Creator<Progress> CREATOR = new Creator<Progress>() {
		
		@Override
		public Progress[] newArray(int size) {
			return new Progress[size];
		}
		
		@Override
		public Progress createFromParcel(Parcel source) {
			return new Progress(source);
		}
	};
	
}
