package de.idvos.fastonlineidentification.config;

import android.content.Context;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by mohammadrezanajafi on 2/2/15.
 */
public class AppConfig {

    public static final String TAG = "RING_APPLICATION";

    private static Context context = null;
    private static AppConfig instance = new AppConfig();

    private RequestQueue mRequestQueue = null;

    private AppConfig() {
    }

    public static AppConfig getInstance() {
        if (instance == null) {
            throw new IllegalStateException("AppConfig is not initialized");
        }
        return instance;
    }

    public static void initialize(Context context) {
        if (AppConfig.context != null) {
            throw new IllegalStateException("AppConfig was already initialized");
        }

        if (context == null) {
            throw new NullPointerException("Context can't be null");
        }

        AppConfig.context = context;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(context);
        }
        return mRequestQueue;

    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }
}
