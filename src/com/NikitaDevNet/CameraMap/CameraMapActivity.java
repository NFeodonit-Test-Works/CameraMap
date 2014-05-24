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



// Главное окно программы.
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


		// Получаем последние известные координаты из сети ...
		if (locationManager.getProvider(LocationManager.NETWORK_PROVIDER) != null) {
			location = locationManager.getLastKnownLocation(
					LocationManager.NETWORK_PROVIDER);
		}

		// ... или от GPS (опрашиваем последним, потому что точнее)
		if (locationManager.getProvider(LocationManager.GPS_PROVIDER) != null) {
			location = locationManager.getLastKnownLocation(
					LocationManager.GPS_PROVIDER);
		}

		// Если определение координат не доступно,
		// устанавливаем примерный центр Беларуси
		if (location != null) {
			cameraWebView.setMap(location.getLatitude(), location.getLongitude());
		} else {
			cameraWebView.setMap(53.645, 28.125);
		}


		// Запрашиваем список камер
		new AsyncTaskRequestCameras()
				.execute("http://speed-control.by/index.php/ru/equipment-ru");
	}


	// При восстановлении окна программы подключаемся к провайдерам
	// для определения координат.
	// Считываем показания каждые 50 метров.
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


	// При невидимом окне программы отключаемся от провайдеров
	// для экономии ресурсов
	@Override
	protected void onPause() {
		super.onPause();
		locationManager.removeUpdates(locationListener);
	}


	// Слушатель изменения состояния провайдера координат
	private LocationListener locationListener = new LocationListener() {

		// При изменении позиции перемещаем маркер автомобиля на новое место
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


	// По заданному url возвращает полученный запрос как строку
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


	// Преобразует полученный поток в строку
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


	// Запускает задачу в фоне
	private class AsyncTaskRequestCameras extends AsyncTask<String, Void, String> {

		// В фоне запускаем получение запроса с сайта
		@Override
		protected String doInBackground(String... urls) {
			return getHttpResponse(urls[0]);
		}

		@Override
		protected void onPostExecute(String result) {

			boolean isSiteOK = true;

			// Если запрос не удался, берем текст запроса из файла ресурса и
			// сбрасываем флаг доступности сайта
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

			// По полученному запросу устанавливаем камеры на карте,
			// передаем флаг доступности сайта
			cameraWebView.setCameras(result, isSiteOK);
		}
	}
}
