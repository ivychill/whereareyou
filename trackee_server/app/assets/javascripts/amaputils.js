/* maputils.js */

var map = null;

$ = jQuery;

function initMap() {
	map = new AMap.Map("map_canvas");
	//map.centerAndZoom("深圳", 12);
	//var trafficLayer = new BMap.TrafficLayer();
	//map.addTileLayer(trafficLayer);
	//map.setMinZoom(12);
        // update the navbar title using jQuery
        $('#marker-nav .marker-title')
            .html("...Navigator Bar Here...")
            .removeClass('has-detail')
            .unbind('click');
	
	if (navigator.geolocation) {
		watchLocation();
	}
	else {
		alert("Oops, no geolocation support");
	}
}

function watchLocation() {
	watchId = navigator.geolocation.watchPosition(
					updateLoc, 
					displayError,
					{
						enableHighAccuracy: true
					});
}

function updateLoc(position) {
	addMarker(new AMap.LngLat(position.coords.longitude, position.coords.latitude));
	//Ajax post to server
	var track_event = {
		id:19751974,
		trackee_x:position.coords.longitude,
		trackee_y:position.coords.latitude
	};
	
	$.ajax({
		type: 'PUT',
		url: "/events/19751974",
		data: track_event,
		success: function() {
			return;
		},
		dataType: "json"
	});							
}

function displayError(error) {
	var errorTypes = {
		0: "Unknown error",
		1: "Permission denied",
		2: "Position is not available",
		3: "Request timeout"
	};
	var errorMessage = errorTypes[error.code];
	if (error.code == 0 || error.code == 2) {
		errorMessage = errorMessage + " " + error.message;
	}
	alert(errorMessage);
}

function addMarker(latlong, title, content) {
	var marker = new AMap.Marker({
		position: latlong, 
		icon: "http://api.amap.com/webapi/static/Images/marker_sprite.png",
		offset:{x:-8,y:-34}
	});
	map.addOverlays(marker);
	map.panTo(latlong);
}