package de.idvos.fastonlineidentification.config;

import android.app.Application;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.crashlytics.android.Crashlytics;

/**
 * Created by mohammadrezanajafi on 2/2/15.
 */
public class AppConfig extends Application {

    private static AppConfig instance = null;
    private RequestQueue mRequestQueue = null;
    public static final String TAG = "RING_APPLICATION";


    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Crashlytics.start(this);

    }

    public static AppConfig getInstance(){
        if(instance == null){
            throw new IllegalStateException("Application is not initialized !");
        }
        return instance;
    }





    public RequestQueue getRequestQueue(){
        if(mRequestQueue == null){
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
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
