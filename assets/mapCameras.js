
var map;
var carMarker;


function initialize(carLat, carLng) {
  var carCoordinates = new google.maps.LatLng(carLat, carLng);

  var mapOptions = {
//    zoom: 6,
    zoom: 12,
    center: carCoordinates,
    mapTypeId: google.maps.MapTypeId.ROADMAP
  };

  map =  new google.maps.Map(document.getElementById("map_canvas"), mapOptions);

  carMarker = new google.maps.Marker({
    position: carCoordinates,
    map: map,
    draggable: true
  });

  google.maps.event.addListener(carMarker, 'position_changed', function() {
    mapCameras.carPositionChanged(carMarker.getPosition().lat(),
        carMarker.getPosition().lng());
  });

  mapCameras.setCameras();
}


function setCameraJS(cameraLat, cameraLng) {
  var cameraCoordinates = new google.maps.LatLng(cameraLat, cameraLng);

  var cameraMarker = new google.maps.Marker({
    position: cameraCoordinates,
    map: map,
    icon: "http://speed-control.by/administrator/components/com_zhyandexmap/assets/icons/default%23.png"
  });
}


function setCarLocationJS(carLat, carLng) {
  var carCoordinates = new google.maps.LatLng(carLat, carLng);
  map.setCenter(carCoordinates);
  carMarker.setPosition(carCoordinates);
}


function getDistanceJS(posLat_1, posLng_1, posLat_2, posLng_2) {
  var pos_1 = new google.maps.LatLng(posLat_1, posLng_1);
  var pos_2 = new google.maps.LatLng(posLat_2, posLng_2);

  var distance = google.maps.geometry.spherical.computeDistanceBetween(pos_1, pos_2);
  mapCameras.setDistanceToCamera(distance);
}
