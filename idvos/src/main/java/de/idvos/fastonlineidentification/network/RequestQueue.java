package de.idvos.fastonlineidentification.network;

import java.util.LinkedList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import de.idvos.fastonlineidentification.network.NetworkRequest.RequestResult;
import android.os.AsyncTask;

import com.android.volley.toolbox.StringRequest;

public class RequestQueue {
	
	private static RequestQueue mInstance = null;
	
	public static RequestQueue getInstance() {
		if (mInstance==null) {
			mInstance = new RequestQueue();
		}
		
		return mInstance;
	}
	
	private LinkedList<NetworkRequest<?>> mRequests;
	
	private Executor mExecutor;
	
	private NetworkRequest<?> mCurrentRequest = null;
	
	private RequestQueue() {
		mRequests = new LinkedList<NetworkRequest<?>>();
		mExecutor = Executors.newSingleThreadExecutor();
	}
	
	public void addRequestToQueue(NetworkRequest<?> request) {
		mRequests.addLast(request);
		checkRequests();
	}



	
	private void checkRequests() {
		if (mCurrentRequest!=null) {
			return;
		}
		
		if (mRequests.size()==0) {
			return;
		}
		
		mCurrentRequest = mRequests.pollFirst();
		
		runCurrentRequest();
	}
	
	private void runCurrentRequest() {
		new AsyncTask<Void, Void, RequestResult>() {

			@Override
			protected RequestResult doInBackground(Void... params) {
				return mCurrentRequest.run();
			}
			
			protected void onPostExecute(RequestResult result) {
				mCurrentRequest.publishResult(result);
				mCurrentRequest = null;
				checkRequests();
			};
			
		}.executeOnExecutor(mExecutor, (Void[]) null);
	}
	
}
