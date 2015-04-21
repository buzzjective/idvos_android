package de.idvos.fastonlineidentification.network;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.util.Log;

public class VoidRequest extends NetworkRequest<Void> {

	private static final String TAG = "VoidRequest";
	
	public static interface VoidRequestResultCallback extends RequestResultCallback<Void> {
		
	}
	
	public VoidRequest(String uri, int method, VoidRequestResultCallback callback) {
		super(uri, method, callback);
	}

	@Override
	protected RequestResult run(HttpUriRequest request) {
		HttpClient client = new DefaultHttpClient();
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
