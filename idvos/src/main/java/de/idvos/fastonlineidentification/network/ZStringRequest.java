package de.idvos.fastonlineidentification.network;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

/**
 * Created by mohammadrezanajafi on 2/2/15.
 */
public class ZStringRequest extends NetworkRequest<String> {

    private static final String TAG = "JsonObjectRequest";

    public static interface StringRequestCallback extends RequestResultCallback<String> {

    }


    public ZStringRequest(String uri, int method, StringRequestCallback callback) {
        super(uri, method, callback);
    }

    @Override
    protected RequestResult run(HttpUriRequest request) {
        RequestResult result = new RequestResult();

        HttpClient client = new DefaultHttpClient();

        int statusCode = STATUS_CODE_UNKNOWN;

        try {
            HttpResponse response = client.execute(request);

            statusCode = response.getStatusLine().getStatusCode();

            result.statusCode = statusCode;

            String entity = EntityUtils.toString(response.getEntity(), "UTF-8");

            Log.d(TAG, request.getURI().toString() + " > " + entity);

            result.result = new JSONObject(entity);
            result.success = true;

        } catch (Exception e) {
            e.printStackTrace();
            result.exception = new Exception(e);
            result.success = false;
        }

        return result;
    }

    @Override
    protected String getContentType() {
        return "application/json";
    }
}
