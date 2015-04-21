package de.idvos.fastonlineidentification.network;

import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class NetworkRequest<T> {

	public static final int METHOD_PUT = 0;
	
	public static final int STATUS_CODE_UNKNOWN = -1;

	public static interface RequestResultCallback<T> {
		
		public void onResult(int statusCode, T result, NetworkRequest<T> request);
		
		public void onError(int statusCode, Exception exception, NetworkRequest<T> request);
		
	}
	
	protected static class RequestResult {

		protected boolean success;
		protected int statusCode = -1;
		protected Object result;
		protected Exception exception;
		
		protected RequestResult() { }
		
	}



	private String mUrl;
	private int mMethod;
	private RequestResultCallback<T> mCallback;
	private HttpEntity mEntity = null;
	private String mContentType = null;
	
	public NetworkRequest(String uri, int method, RequestResultCallback<T> callback) {
		mUrl = uri;
		mMethod = method;
		mCallback = callback;
	}
	
	public void setEntity(HttpEntity entity) {
		mEntity = entity;
	}
	
	public void setJsonEntity(JSONObject jEntity) throws JSONException {
		try {
			mEntity = new StringEntity(jEntity.toString(), "UTF-8");
			mContentType = "application/json";
		} catch (UnsupportedEncodingException e) { }
	}
	
	private HttpUriRequest getRequest() {
		HttpUriRequest request = null;
		
		switch (mMethod) {
            case METHOD_PUT:
                request = new HttpPut(mUrl);
                if (mEntity!=null) {
                    ((HttpPut) request).setEntity(mEntity);
                }
                break;
		}
		
		addHeaders(request);
		
		return request;
	}
	
	final protected RequestResult run() {
		HttpUriRequest request = getRequest();
		return run(request);
	}
	
	abstract protected RequestResult run(HttpUriRequest request);
	
	abstract protected String getContentType();
	
	@SuppressWarnings("unchecked")
	final protected void publishResult(RequestResult result) {

        if (result.success) {
			mCallback.onResult(result.statusCode, (T) result.result, this);
		}
		else {
			mCallback.onError(result.statusCode, result.exception, this);
		}
	}
	
	private void addHeaders(HttpRequest request) {
		if (mContentType!=null) {
			request.addHeader("Content-Type", getContentType());
		}
	}
	
}
