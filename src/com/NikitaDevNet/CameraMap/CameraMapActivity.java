package com.NikitaDevNet.CameraMap;

import android.app.Activity;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

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

// TODO: for debug
		cameraWebView.setMap(53.91456666667, 27.237);
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

}
