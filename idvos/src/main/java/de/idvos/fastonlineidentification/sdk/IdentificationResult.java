package de.idvos.fastonlineidentification.sdk;

import android.os.Parcel;
import android.os.Parcelable;

public class IdentificationResult implements Parcelable {
	
	public static final String IDENTIFICATION_RESULT = "result";
	
	private boolean mResult;
	private String mTransactionId;
	
	public IdentificationResult(boolean result, String transactionId) {
		mResult = result;
		mTransactionId = transactionId;
	}
	
	private IdentificationResult(Parcel source) {
		mResult = source.readInt()==1;
		mTransactionId = source.readString();
	}
	
	public boolean isSuccessful() {
		return mResult;
	}
	
	public String getTransactionId() {
		return mTransactionId;
	}
	
	public static final Creator<IdentificationResult> CREATOR = new Creator<IdentificationResult>() {
		
		@Override
		public IdentificationResult[] newArray(int size) {
			return new IdentificationResult[size];
		}
		
		@Override
		public IdentificationResult createFromParcel(Parcel source) {
			return new IdentificationResult(source);
		}
		
	};
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(mResult?1:0);
		dest.writeString(mTransactionId);
	}

}
