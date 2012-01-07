package me.chester.minitruco.android;

import android.content.Context;
import android.util.Log;

import com.bestcoolfungames.adsclient.BCFGAds;
import com.bestcoolfungames.adsclient.BCFGAdsListener;

public class Publicidade {

	private static final String BCFGADS_APP_ID = "4f075f1f044a0d0003012e0a";
	private static boolean mPodeMostrar = false;
	private static BCFGAds ads;
	private static BCFGAdsListener listener = new BCFGAdsListener() {
		public void adReceived() {
			Log.w("MINITRUCO", "mostrar");
			mPodeMostrar = true;
		}

		public void adNotReceived() {
			Log.w("MINITRUCO", "nao mostrar");
			mPodeMostrar = false;
		}
	};

	public static void inicializa(Context context) {

		ads = new BCFGAds(context, listener, BCFGADS_APP_ID) {
			@Override
			public void showAd(Context c) {
				// Does nothing. It is just to avoid the bizarre
				// show-ad-out-of-nowhere behavior
			}

		};
		ads.requestAd();
	}

	public static boolean podeMostrar() {
		return mPodeMostrar;
	}

	public static String getMensagem() {
		return mPodeMostrar ? ads.message() : "";
	}

	public static void click() {
		if (mPodeMostrar) {
			ads.yesPressed();
			mPodeMostrar = false;
		}
	}

}
