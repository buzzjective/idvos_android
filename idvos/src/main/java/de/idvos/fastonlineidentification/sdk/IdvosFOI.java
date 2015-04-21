package de.idvos.fastonlineidentification.sdk;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

public class IdvosFOI {
	
	private static final String TAG = "IdvosFOI";
	
	private static final String ACTION_IDENTIFY = "idvos.intent.action.IDENTIFY";
	
	public static final String KEY_IDENTIFICATION_HASH = "identification_hash";
	
	private static final int REQUEST_IDENTIFICATION = 37232453;
	
	public static interface IdentificationResultCallback {
		
		public void onIdentificationResultReady(IdentificationResult result);
		
	}
	
//	private static IdvosFOI instance = null;
//	
//	public static IdvosFOI getInstance(Context context) {
//		if (instance==null) {
//			instance = new IdvosFOI(context.getApplicationContext());
//		}
//		
//		return instance;
//	}
	
	private Activity mActivity;
	
	private IdentificationResultCallback mCallback;
	
	public IdvosFOI(Activity activity) {
		mActivity = activity;
	}
	
	public boolean requestIdentification(String identificationHash, IdentificationResultCallback callback) {
		
		if (identificationHash==null) {
			throw new IllegalArgumentException("Identification hash can not be null.");
		}
		
		if (callback==null) {
			throw new IllegalArgumentException("No callback is set for identification request.");
		}
		
		mCallback = callback;
		
		Intent intent = new Intent(ACTION_IDENTIFY);
		intent.setPackage("de.idvos.fastonlineidentification");
		intent.putExtra(KEY_IDENTIFICATION_HASH, identificationHash);

		try {
			mActivity.startActivityForResult(intent, REQUEST_IDENTIFICATION);
		}
		catch (Exception e) {
			Log.w(TAG, "Could not launch identification app", e);
			return false;
		}
		
		return true;
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode==REQUEST_IDENTIFICATION) {
			IdentificationResult result = data.getParcelableExtra(IdentificationResult.IDENTIFICATION_RESULT);
			mCallback.onIdentificationResultReady(result);
		}
	}
	
}
