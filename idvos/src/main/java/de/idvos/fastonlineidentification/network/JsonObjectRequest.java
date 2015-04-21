package de.idvos.fastonlineidentification.network;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.util.Log;

public class JsonObjectRequest extends NetworkRequest<JSONObject> {

	private static final String TAG = "JsonObjectRequest";
	
	public static interface JsonObjectRequestCallback extends RequestResultCallback<JSONObject> {
		
	}

	public JsonObjectRequest(String uri, int method, JsonObjectRequestCallback callback) {
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
