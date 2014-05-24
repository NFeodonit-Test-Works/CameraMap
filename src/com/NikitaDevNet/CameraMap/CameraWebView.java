package com.NikitaDevNet.CameraMap;

import android.content.Context;
import android.location.Location;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.AttributeSet;
import android.webkit.*;
import android.widget.Toast;

import java.lang.Double;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created with IntelliJ IDEA.
 * User: Nikita DevNet
 * Date: 22.05.14
 * Time: 9:29
 * To change this template use File | Settings | File Templates.
 */


// Отображает карту и взаимодействует с ней.
public class CameraWebView extends WebView {

	// Порог срабатывания для расстояния до камеры
	private final Double distanceThreshold = 500.0;

	// Старое и новое расстояния до камеры для определения факта приближения
	private Double distanceToCameraOld = distanceThreshold;
	private Double distanceToCamera = distanceThreshold;


	private JavaScriptInterface javaScriptInterface;
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
		// Необходим, если нужен переход по ссылкам в нашем окне программы
//		setWebViewClient(new WebViewClient());

		// Устанавливаем интерфейс взаимодействия со JS-скриптом
		javaScriptInterface = new JavaScriptInterface(mContext);
		// По имени "mapCameras" обращаемся к функциям интерфейса из JS-скрипта
		addJavascriptInterface(javaScriptInterface, "mapCameras");


		// STREAM_ALARM -- выдаем звук, даже если звук отключен
		soundPool = new SoundPool(10, AudioManager.STREAM_ALARM, 0);

		// Выставляем флаг успешности загрузки файла звука
		soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
			@Override
			public void onLoadComplete(SoundPool soundPool, int sampleId,
												int status) {
				soundLoaded = true;
			}
		});

		// Загружаем файл звука
		soundID = soundPool.load(mContext, R.raw.sound1, 1);
	}


	// Загружает карту, устанавливая центр по координатам carLat и carLng
	public void setMap(Double carLat, Double carLng) {
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

		// Подключаем Google Maps API,
		// указываем требуемый ключ key, подключаем доп. библиотеку geometry
		html.append("<script language=\"JavaScript\" " +
				"src=\"http://maps.googleapis.com/maps/api/js?" +
				"key=AIzaSyCHiUkI0Vyxbrzo4AGRT6qmsdXV0INmQd0&" +
				"libraries=geometry&" +
				"sensor=false\" " +
				"type=\"text/javascript\"></script>\r\n");

		// Подключаем наш скрипт
		html.append("<script language=\"JavaScript\" " +
				"src=\"file:///android_asset/mapCameras.js\" " +
				"type=\"text/javascript\"></script>\r\n");

		html.append("</head>\r\n");

		// Точка входа в скрипт
		html.append("<body onload=\"initJS(" +
				carLat + ", " + carLng + ")\">\r\n");

		html.append("<div id=\"map_canvas\" " +
				"style=\"width:100%; height:100%\"></div>\r\n");

		html.append("</body>\r\n");
		html.append("</html>");

		loadDataWithBaseURL("about:blank", html.toString(),
				"text/html", "UTF-8", "about:blank");
	}


	// По полученному тексту запроса парсит координаты камер,
	// по которым устанавливает маркеры камер на карту.
	// Если флаг доступности сайта зброшен,
	// устанавливает значок для маркера камеры по умолчанию.
	public void setCameras(String httpResponse, boolean isSiteOK) {

		// Начинаем с "default%23.png", чтобы отсеять ПКО
		Matcher matcher = Pattern.compile(
				"default%23\\.png\";\\s{0,}" +
						"s\\d{1,20}\\.iconStyle\\.size\\s{0,}=\\s{0,}new\\s{0,}" +
						"YMaps\\.Point\\(\\d{1,20}\\s{0,},\\s{0,}\\d{1,20}\\);\\s{0,}" +
						"var\\s{1,}lnglat\\d{1,20}\\s{0,}=\\s{0,}new\\s{1,}YMaps\\.GeoPoint\\(" +
						"(\\d{1,2}\\.\\d{1,20}),\\s{0,}" +
						"(\\d{1,2}\\.\\d{1,20})\\);").matcher(httpResponse);

		// У Yandex обратный порядок указания координат
		while (matcher.find()) {
			String sLat = matcher.group(2);
			String sLng = matcher.group(1);

			javaScriptInterface.setCamera(sLat, sLng, isSiteOK);

			matcher.region(matcher.end(), httpResponse.length());
		}
	}


	// Устанавливает маркер автомобиля в заданную позицию
	public void setCarLocation(Location location) {
		javaScriptInterface.setCarLocation(location);
	}


	// Интерфейс для взаимодействия со JS-скриптом
	private final class JavaScriptInterface {

		private Context mContext;

		public JavaScriptInterface(Context context) {
			mContext = context;
		}


		// Усанавливает маркер камеры в заданные координаты.
		// Если флаг доступности сайта зброшен,
		// устанавливает значок для маркера камеры по умолчанию.
// for API +17
//		@JavascriptInterface
		public void setCamera(final String camLat, final String camLng,
									 final boolean isSiteOK) {
			post(new Runnable() {
				@Override
				public void run() {
					loadUrl("javascript: setCameraJS(" +
							camLat + ", " + camLng +  ", " + isSiteOK + ");");
				}
			});
		}


		// Усанавливает маркер автомобиля в заданную позицию.
// for API +17
//		@JavascriptInterface
		public void setCarLocation(final Location location) {
			post(new Runnable() {
				@Override
				public void run() {
					loadUrl("javascript: setCarLocationJS(" +
							location.getLatitude() + ", " + location.getLongitude() + ");");
				}
			});
		}


		// Выдает предупреждение (текст и звук) при приближении к камере,
		// если дистанция до камеры меньше порога срабатывания.
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

				// Выдаем звук на полной громкости на обоих каналах,
				// повторяем сигнал три раза
				if (soundLoaded) {
					soundPool.play(soundID, 1, 1, 1, 2, 1f);
				}

			}
		}
	}
}
