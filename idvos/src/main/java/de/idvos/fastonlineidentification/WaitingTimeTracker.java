package de.idvos.fastonlineidentification;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Provides client waiting time tracking
 */
public class WaitingTimeTracker {

    private static final String WAITING_TIME_START = "waiting_time_start";
    private static final String WAITING_TIME_END = "waiting_time_end";

    private Context context;
    private SharedPreferences sharedPreferences;

    public WaitingTimeTracker(Context context) {
        this.context = context;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * Start tracking of waiting time
     */
    public void startTracking() {
        sharedPreferences
                .edit()
                .putLong(WAITING_TIME_START, System.currentTimeMillis())
                .putLong(WAITING_TIME_END, 0)
                .apply();
    }

    /**
     * End tracking of waiting time
     */
    public void endTracking() {
        sharedPreferences
                .edit()
                .putLong(WAITING_TIME_END, System.currentTimeMillis())
                .apply();
    }

    /**
     * @return waiting time of customer in milliseconds
     */
    public long getWaitingTimeMillis(){
        long start = sharedPreferences.getLong(WAITING_TIME_START, -1);
        long end = sharedPreferences.getLong(WAITING_TIME_END, 0);
        return start < end
                ? end - start
                : System.currentTimeMillis() - start;
    }
}
