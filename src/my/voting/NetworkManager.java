package my.voting;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetworkManager {

	private Context context;

	public NetworkManager(Context context) {
		this.context = context;
	}

	private boolean isConnected() {
		ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = manager.getActiveNetworkInfo();
		return info != null && info.isConnected();
	}

	/**
	 * Connects to the mobile data service. Returns true if successful, false if an exception was thrown or the
	 * connection could not be established after 3 tries.
	 */
	public boolean connect() {
		if (!isConnected()) {
			try {
				setMobileData(true);
			} catch (Exception e) {
				Log.e(Vote.ERROR, "Failed to connect: " + e.getMessage());
				return false;
			}
			int tries = 0;
			while (tries < 3 && !isConnected()) {
				tries++;
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					Log.e(Vote.ERROR, "Failed to sleep when connecting: " + e.getMessage());
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Disconnects the mobile data service. Returns true if successful, false if an exception was thrown or the
	 * connection could not be disconnected after 3 tries.
	 */
	public boolean disconnect() {
		if (isConnected()) {
			try {
				setMobileData(false);
			} catch (Exception e) {
				Log.e(Vote.ERROR, "Failed to disconnect: " + e.getMessage());
				return false;
			}
			int tries = 0;
			while (tries < 3 && isConnected()) {
				tries++;
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					Log.e(Vote.ERROR, "Failed to sleep when disconnecting: " + e.getMessage());
					return false;
				}
			}
		}
		return true;
	}

	private void setMobileData(boolean enabled) throws Exception {
		final ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		final Class<?> cmClass = Class.forName(connectivityManager.getClass().getName());
		final Field mServiceField = cmClass.getDeclaredField("mService");
		mServiceField.setAccessible(true);
		final Object mService = mServiceField.get(connectivityManager);
		final Class<?> mServiceClass = Class.forName(mService.getClass().getName());
		final Method setMobileDataEnabledMethod = mServiceClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
		setMobileDataEnabledMethod.setAccessible(true);
		setMobileDataEnabledMethod.invoke(mService, enabled);
	}

}
