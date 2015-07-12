package de.idvos.fastonlineidentification.network;

import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

import de.idvos.fastonlineidentification.sdk.IdvosSDK;

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

	protected HttpClient getClient() {
		IdvosSDK.Mode mode = IdvosSDK.getInstance().getMode();
		if (mode == IdvosSDK.Mode.PRODUCTION) {
			return new DefaultHttpClient();
		}

		HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;

		DefaultHttpClient client = new DefaultHttpClient();

		SchemeRegistry registry = new SchemeRegistry();
		SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
		socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
		registry.register(new Scheme("https", socketFactory, 443));
		SingleClientConnManager mgr = new SingleClientConnManager(client.getParams(), registry);
		DefaultHttpClient httpClient = new DefaultHttpClient(mgr, client.getParams());

		HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
		return httpClient;
	}
	
}
