
var map;
var carMarker;
var camerasArray = [];


function initJS(carLat, carLng) {
	var carPosition = new google.maps.LatLng(carLat, carLng);

	var mapOptions = {
		zoom: 6,
//		zoom: 14,
		center: carPosition,
		mapTypeId: google.maps.MapTypeId.ROADMAP
	};

	map =  new google.maps.Map(document.getElementById("map_canvas"), mapOptions);

	carMarker = new google.maps.Marker({
		position: carPosition,
		map: map,
		draggable: true
	});

	google.maps.event.addListener(carMarker, 'position_changed', function() {
		getDistanceToCameraJS(carMarker.getPosition());
	});

	mapCameras.setCameras();
}


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


function setCarLocationJS(carLat, carLng) {
	var carPosition = new google.maps.LatLng(carLat, carLng);
	map.setCenter(carPosition);
	carMarker.setPosition(carPosition);
}


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

		mapCameras.setDistanceToCamera(minDistance);
	}
}
