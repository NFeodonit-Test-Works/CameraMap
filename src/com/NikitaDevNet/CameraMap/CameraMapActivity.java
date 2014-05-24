package com.NikitaDevNet.CameraMap;

import android.app.Activity;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;



public class CameraMapActivity extends Activity {

	private CameraWebView cameraWebView;
	private LocationManager locationManager;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		cameraWebView = (CameraWebView) findViewById(R.id.cameraWebView);

		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

		Location location = null;

		if (locationManager.getProvider(LocationManager.NETWORK_PROVIDER) != null) {
			location = locationManager.getLastKnownLocation(
					LocationManager.NETWORK_PROVIDER);
		}

		if (locationManager.getProvider(LocationManager.GPS_PROVIDER) != null) {
			location = locationManager.getLastKnownLocation(
					LocationManager.GPS_PROVIDER);
		}

		if (location != null) {
			cameraWebView.setMap(location.getLatitude(), location.getLongitude());
		} else {
			cameraWebView.setMap(53.645, 28.125);
		}


		new AsyncTaskRequestCameras()
				.execute("http://speed-control.by/index.php/ru/equipment-ru");
	}


	@Override
	protected void onResume() {
		super.onResume();

		if (locationManager.getProvider(LocationManager.NETWORK_PROVIDER) != null) {
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
					0, 50, locationListener);
		}

		if (locationManager.getProvider(LocationManager.GPS_PROVIDER) != null) {
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
					0, 50, locationListener);
		}
	}


	@Override
	protected void onPause() {
		super.onPause();
		locationManager.removeUpdates(locationListener);
	}


	private LocationListener locationListener = new LocationListener() {

		@Override
		public void onLocationChanged(Location location) {
			cameraWebView.setCarLocation(location);
		}

		@Override
		public void onProviderDisabled(String provider) {
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	};


	public static String getHttpResponse(String url){
		InputStream inputStream = null;
		String result = "";

		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse httpResponse = httpclient.execute(new HttpGet(url));
			inputStream = httpResponse.getEntity().getContent();

			if(inputStream != null)
				result = convertInputStreamToString(inputStream);

		} catch (Exception e) {
			return "";
		}

		return result;
	}


	private static String convertInputStreamToString(InputStream inputStream)
			throws IOException{

		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(inputStream));

		String line;
		String result = "";
		while((line = bufferedReader.readLine()) != null)
			result += line;

		inputStream.close();
		return result;
	}


	private class AsyncTaskRequestCameras extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... urls) {
			return getHttpResponse(urls[0]);
		}

		@Override
		protected void onPostExecute(String result) {

			boolean isSiteOK = true;

			if (result.length() == 0) {
				isSiteOK = false;

				InputStream inputStream = getApplication().getApplicationContext()
						.getResources().openRawResource(R.raw.speedcontrol);

				if(inputStream != null)
					try {
						result = convertInputStreamToString(inputStream);
					} catch (IOException e) {
						e.printStackTrace();
					}
			}

			cameraWebView.setCameras(result, isSiteOK);
		}
	}
}
