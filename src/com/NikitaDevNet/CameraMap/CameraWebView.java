package com.NikitaDevNet.CameraMap;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.AttributeSet;
import android.webkit.*;
import android.widget.Toast;

import java.lang.Double;


/**
 * Created with IntelliJ IDEA.
 * User: Nikita DevNet
 * Date: 22.05.14
 * Time: 9:29
 * To change this template use File | Settings | File Templates.
 */


public class CameraWebView extends WebView {

	private final Double carLat = 53.91456666667;
	private final Double carLng = 27.237;

	private final Double cameraLat = 53.91213888889;
	private final Double cameraLng = 27.25102777778;

	private final Double distanceThreshold = 500.0;
	private Double distanceToCameraOld = distanceThreshold;
	private Double distanceToCamera = distanceThreshold;


	private SoundPool soundPool;
	private int soundID;
	private boolean soundLoaded = false;


	public CameraWebView(Context mContext, AttributeSet attributeSet) {
		super(mContext, attributeSet);

		WebSettings settings = getSettings();
		settings.setJavaScriptEnabled(true);
		settings.setBuiltInZoomControls(true);
		settings.setSupportZoom(true);

		setFocusable(true);
		setFocusableInTouchMode(true);
//		setWebViewClient(new WebViewClient());

		addJavascriptInterface(new JavaScriptInterface(mContext), "mapCameras");


		soundPool = new SoundPool(10, AudioManager.STREAM_ALARM, 0);
		soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
			@Override
			public void onLoadComplete(SoundPool soundPool, int sampleId,
												int status) {
				soundLoaded = true;
			}
		});
		soundID = soundPool.load(mContext, R.raw.sound1, 1);


		setHtml();
	}


	public void setHtml() {
		StringBuilder html = new StringBuilder();
		html.append("<!DOCTYPE html>\r\n");
		html.append("<html>\r\n");
		html.append("<head>\r\n");
		html.append("<meta name=\"viewport\" content=\"initial-scale=1.0, " +
				"user-scalable=no\" />\r\n");

		html.append("<style type=\"text/css\">\r\n");
		html.append("html { height: 100% }\r\n");
		html.append("body { height: 100%; margin: 0; padding: 0 }\r\n");
		html.append("</style>\r\n");

		html.append("<script language=\"JavaScript\" " +
				"src=\"http://maps.googleapis.com/maps/api/js?" +
				"key=AIzaSyCHiUkI0Vyxbrzo4AGRT6qmsdXV0INmQd0&" +
				"libraries=geometry&" +
				"sensor=false\" " +
				"type=\"text/javascript\"></script>\r\n");

		html.append("<script language=\"JavaScript\" " +
				"src=\"file:///android_asset/mapCameras.js\" " +
				"type=\"text/javascript\"></script>\r\n");

		html.append("</head>\r\n");

		html.append("<body onload=\"initialize(" +
				carLat + ", " + carLng + ")\">\r\n");

		html.append("<div id=\"map_canvas\" " +
				"style=\"width:100%; height:100%\"></div>\r\n");

		html.append("</body>\r\n");
		html.append("</html>");

		loadDataWithBaseURL("about:blank", html.toString(),
				"text/html", "UTF-8", "about:blank");
	}


	private final class JavaScriptInterface {

		private Context mContext;

		public JavaScriptInterface(Context context) {
			mContext = context;
		}


// for API +17
//		@JavascriptInterface
		public void setCameras() {
//			for (final Integer camera : cameras) {
				post(new Runnable() {
					@Override
					public void run() {
						loadUrl("javascript: setCameraJS(" +
								cameraLat + ", " + cameraLng + ");");
					}
				});
//			}
		}


// for API +17
//		@JavascriptInterface
		public void setDistanceToCamera(String distance) {
			distanceToCameraOld = distanceToCamera;
			distanceToCamera = Double.parseDouble(distance);

			if ((distanceToCamera < distanceToCameraOld)
					&& (distanceToCamera < distanceThreshold)) {

				Toast.makeText(mContext, "Distance to camera = " +
						distanceToCamera.intValue() + " m",
						Toast.LENGTH_SHORT).show();

				if (soundLoaded) {
					soundPool.play(soundID, 1, 1, 1, 2, 1f);
				}

			}
		}


// for API +17
//		@JavascriptInterface
		public void carPositionChanged(final String carLat, final String carLng) {
			post(new Runnable() {
				@Override
				public void run() {
					loadUrl("javascript: getDistanceJS(" +
							carLat + ", " + carLng + ", " +
							cameraLat + ", " + cameraLng + ");");
				}
			});
		}

	}
}
