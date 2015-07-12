package de.idvos.fastonlineidentification.network;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

import de.idvos.fastonlineidentification.sdk.IdvosSDK;

public class VoidRequest extends NetworkRequest<Void> {

    private static final String TAG = "VoidRequest";

    public static interface VoidRequestResultCallback extends RequestResultCallback<Void> {

    }

    public VoidRequest(String uri, int method, VoidRequestResultCallback callback) {
        super(uri, method, callback);
    }

    @Override
    protected RequestResult run(HttpUriRequest request) {
        HttpClient client = getClient();

        RequestResult result = new RequestResult();

        try {
            HttpResponse response = client.execute(request);

            result.statusCode = response.getStatusLine().getStatusCode();

            String content = EntityUtils.toString(response.getEntity(), "UTF-8");
            Log.d(TAG, request.getURI().toString() + " > (" + result.statusCode + ") " + content);

            result.success = true;
        } catch (ClientProtocolException e) {
            result.success = false;
            result.exception = e;
        } catch (IOException e) {
            result.success = false;
            result.exception = e;
        }

        return result;
    }

    @Override
    protected String getContentType() {
        return "application/json";
    }

}
