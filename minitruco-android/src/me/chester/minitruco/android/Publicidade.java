package me.chester.minitruco.android;

import android.app.Activity;
import android.util.Log;

import com.bcfg.adsclient.BCFAds;
import com.bcfg.adsclient.ads.Popup;
import com.bcfg.adsclient.ads.Popup.OnLoadListener;

public class Publicidade {

	private static final String BCFGADS_APP_ID_PRODUCTION = "4f075f1f044a0d0003012e0a";
	private static final String BCFGADS_APP_ID_STAGING = "4f110f6e4824950003000005";
	private boolean mPodeMostrar = false;
	private Popup popup;
	private OnLoadListener listener = new OnLoadListener() {

		public void onAdReceived(Popup p) {
			Log.w("MINITRUCO", "mostrar");
			popup = p;
			mPodeMostrar = true;
		}

		public void onAdNotReceived(LoadError error, String response) {
			Log.w("MINITRUCO", "nao mostrar: " + error);
			mPodeMostrar = false;
		}
	};

	public Publicidade(Activity activity) {
		String id = BCFGADS_APP_ID_PRODUCTION;
		if (BCFAds.isStagingMode()) {
			id = BCFGADS_APP_ID_STAGING;
		}
		BCFAds.loadPopup(activity, id, listener);
	}

	public boolean podeMostrar() {
		return mPodeMostrar;
	}

	public String getMensagem() {
		return mPodeMostrar ? popup.getMessage() : "";
	}

	public void click() {
		if (mPodeMostrar && popup != null) {
			popup.click();
			mPodeMostrar = false;
		}
	}

}
