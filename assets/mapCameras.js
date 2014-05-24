
// Карта
var map;
// Маркер автомобиля
var carMarker;
// Массив маркеров камер
var camerasArray = [];


// Инициализация карты
function initJS(carLat, carLng) {
	var carPosition = new google.maps.LatLng(carLat, carLng);

	// Устанавливаем центр карты по координатам автомобиля
	var mapOptions = {
		zoom: 6,
//		zoom: 14,
		center: carPosition,
		mapTypeId: google.maps.MapTypeId.ROADMAP
	};

	map =  new google.maps.Map(document.getElementById("map_canvas"), mapOptions);

	// Маркер автомобиля, перемещаемый
	carMarker = new google.maps.Marker({
		position: carPosition,
		map: map,
		draggable: true
	});

	// При изменении позиции маркера автомобиля вычисляем расстояние до камер.
	// При приближении выдаем предупреждение.
	google.maps.event.addListener(carMarker, 'position_changed', function() {
		getDistanceToCameraJS(carMarker.getPosition());
	});

	// Устанавливем камеры на карту
	mapCameras.setCameras();
}


// Усанавливает маркер камеры в заданные координаты.
// Если флаг доступности сайта зброшен,
// устанавливает значок для маркера камеры по умолчанию.
function setCameraJS(cameraLat, cameraLng, isSiteOK) {
	var cameraPosition = new google.maps.LatLng(cameraLat, cameraLng);

	var cameraMarker;

	if (isSiteOK) {
		cameraMarker = new google.maps.Marker({
			position: cameraPosition,
			map: map,
			icon: "http://speed-control.by/administrator/components/com_zhyandexmap/assets/icons/default%23.png"
		});

	} else {
		cameraMarker = new google.maps.Marker({
			position: cameraPosition,
			map: map
		});
	}

	camerasArray.push(cameraMarker);
}


// Усанавливает маркер автомобиля и центр карты в заданную позицию.
function setCarLocationJS(carLat, carLng) {
	var carPosition = new google.maps.LatLng(carLat, carLng);
	map.setCenter(carPosition);
	carMarker.setPosition(carPosition);
}


// По заданному положению автомобиля вычисляет ближайшую камеру.
function getDistanceToCameraJS(carPosition) {

	if (camerasArray) {
		var minDistance = google.maps.geometry.spherical
				.computeDistanceBetween(
						carPosition, camerasArray[0].getPosition());

		for (i in camerasArray) {
			var distance = google.maps.geometry.spherical
					.computeDistanceBetween(carPosition, camerasArray[i].getPosition());

			if (distance < minDistance)
				minDistance = distance;
	   }

		// Передаем расстояние до ближайшей камеры
		mapCameras.setDistanceToCamera(minDistance);
	}
}
