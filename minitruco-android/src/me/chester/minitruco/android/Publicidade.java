package me.chester.minitruco.android;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.bcfg.adsclient.BCFAds;
import com.bcfg.adsclient.ads.Popup;
import com.bcfg.adsclient.ads.Popup.OnLoadListener;

public class Publicidade {

	private static final String BCFGADS_APP_ID = "4f075f1f044a0d0003012e0a";
	private static boolean mPodeMostrar = false;
	private static Popup popup;
	private static OnLoadListener listener = new OnLoadListener() {

		public void onAdNotReceived(LoadError error) {
			Log.w("MINITRUCO", "nao mostrar: " + error);
			mPodeMostrar = false;
		}

		public void onAdReceived(Popup popup) {
			Log.w("MINITRUCO", "mostrar");
			Publicidade.popup = popup;
			mPodeMostrar = true;
		}
	};

	public static void inicializa(Activity activity) {
		BCFAds.loadPopup(activity, BCFGADS_APP_ID, listener);
	}

	public static boolean podeMostrar() {
		return mPodeMostrar;
	}

	public static String getMensagem() {
		return mPodeMostrar ? popup.getMessage() : "";
	}

	public static void click() {
		if (mPodeMostrar && popup != null) {
			popup.click();
			mPodeMostrar = false;
		}
	}

}
