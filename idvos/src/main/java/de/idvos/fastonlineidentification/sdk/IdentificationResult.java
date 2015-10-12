package de.idvos.fastonlineidentification.sdk;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Result of identification.
 */
public class IdentificationResult implements Parcelable {

    public static final String IDENTIFICATION_RESULT = "result";

    private boolean result;
    private String transactionId;
    private long waitingTimeMillis;

    public IdentificationResult(boolean result, String transactionId, long waitingTimeMillis) {
        this.result = result;
        this.transactionId = transactionId;
        this.waitingTimeMillis = waitingTimeMillis;
    }

    private IdentificationResult(Parcel source) {
        result = source.readInt() == 1;
        transactionId = source.readString();
        waitingTimeMillis = source.readLong();
    }

    public boolean isSuccessful() {
        return result;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public long getWaitingTimeMillis() { return waitingTimeMillis; }

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
        dest.writeInt(result ? 1 : 0);
        dest.writeString(transactionId);
        dest.writeLong(waitingTimeMillis);
    }
}
