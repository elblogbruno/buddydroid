package org.yuttadhammo.buddydroid.interfaces;

import java.net.URI;
import java.util.HashMap;
import java.util.Vector;
import org.xmlrpc.android.XMLRPCClient;
import org.xmlrpc.android.XMLRPCException;
import org.yuttadhammo.buddydroid.BPSettingsActivity;
import org.yuttadhammo.buddydroid.Buddypress;
import org.yuttadhammo.buddydroid.R;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

public class BPRequest {


	public static final int MSG_ERROR = 9999;
	public Vector<String> imageUrl = new Vector<String>();
	Vector<String> selectedCategories = new Vector<String>();
	private static Context context;
	private static String protocol;
	private static int what;
	private static HashMap<String, Object> data;
	public static String error;
	public static String TAG = "BPRequest";
	private static Handler handler;



	public BPRequest(Context c, Handler h, String _protocol, HashMap<String, Object> _data, int _what) {
		context = c;
		handler = h;
		data = _data;
		protocol = _protocol;
		what = _what;
	}

	public void execute() {

		new getStreamTask().execute(this);

	}
	
	private static class getStreamTask extends
			AsyncTask<BPRequest, Boolean, Boolean> {

		private boolean success = false;
		private Object obj;

		@Override
		protected void onPostExecute(Boolean result) {

			Message msg = new Message();
			if (success) {
				msg.arg1 = 0;
				msg.obj = obj;
				msg.what = what;
			}
			else {
				msg.arg1 = R.string.error;
				msg.obj = error;
				msg.what = MSG_ERROR;
			}
			handler.sendMessage(msg);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(BPRequest... streams) {
			if(!isInternetOn()) {
				error = context.getString(R.string.checkSetupInternet);
				return false;
			}
			
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			
			String api = prefs.getString("api_key", null);
			String service = prefs.getString("service_name", "BuddyDroid");
			String username = prefs.getString("username", null);
			String website = Buddypress.getWebsite();
			
			if(username == null || website == null)
				return false;
			
			Object[] params;
			
			if(api == null) { 
				if(what == BPSettingsActivity.MSG_API) // api request
					params = new Object[] { 
						username,
						service,
						data 
					};
				else
					return false;
			}
			else
				params = new Object[] { 
					username,
					service,
					api, 
					data 
				};
			XMLRPCClient client = new XMLRPCClient(URI.create(website+"index.php?bp_xmlrpc=true"),
					username, api); // not used
			try {
				obj = client.call(protocol, params);
				success = true;
				return true;
			} catch (final XMLRPCException e) {
				error = e.toString();
				e.printStackTrace();
			}
			return false;
		}

	}

	public static boolean isInternetOn() {
	    ConnectivityManager cm =
	        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
	        return true;
	    }
	    return false;
	}
	
}
