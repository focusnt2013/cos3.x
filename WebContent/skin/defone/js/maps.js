var basic_map,
	markers_map,
	hybrid_map,
	street_view_map,
	search_address_map,
	visitors_map;

var marker, i, markers = [];

var infowindow = new google.maps.InfoWindow();

var locations = [
	['Bondi Beach', -33.890542, 151.274856, 4],
	['Coogee Beach', -33.923036, 151.259052, 5],
	['Cronulla Beach', -34.028249, 151.157507, 3],
	['Manly Beach', -33.80010128657071, 151.28747820854187, 2],
	['Maroubra Beach', -33.950198, 151.259302, 1]
];

function genBasicMap() {
	
	var basic_map_options = {
		zoom: 8,
		center: new google.maps.LatLng(-34.397, 150.644)
	};

	basic_map = new google.maps.Map(document.getElementById('google-map-basic'), basic_map_options);
}

function genVisitorsMap() {

	$('#world-map-markers').vectorMap({
	    map: 'world_mill_en',
	    scaleColors: [successColor, '#0071A4'],
	    normalizeFunction: 'polynomial',
	    hoverOpacity: 0.7,
	    hoverColor: false,
	    markerStyle: {
	      initial: {
	        fill: '#18bc9c',
	        stroke: primaryColor
	      }
	    },
	    regionStyle: {
	    	initial: {
	    		fill: '#18bc9c',
	    		stroke: '#18bc9c'
	    	}
	    },
	    backgroundColor: '#fff',
	    markers: [
	      {latLng: [41.90, 12.45], name: 'Vatican City'},
	      {latLng: [43.73, 7.41], name: 'Monaco'},
	      {latLng: [-0.52, 166.93], name: 'Nauru'},
	      {latLng: [-8.51, 179.21], name: 'Tuvalu'},
	      {latLng: [43.93, 12.46], name: 'San Marino'},
	      {latLng: [47.14, 9.52], name: 'Liechtenstein'},
	      {latLng: [7.11, 171.06], name: 'Marshall Islands'},
	      {latLng: [17.3, -62.73], name: 'Saint Kitts and Nevis'},
	      {latLng: [3.2, 73.22], name: 'Maldives'},
	      {latLng: [35.88, 14.5], name: 'Malta'},
	      {latLng: [12.05, -61.75], name: 'Grenada'},
	      {latLng: [13.16, -61.23], name: 'Saint Vincent and the Grenadines'},
	      {latLng: [13.16, -59.55], name: 'Barbados'},
	      {latLng: [17.11, -61.85], name: 'Antigua and Barbuda'},
	      {latLng: [-4.61, 55.45], name: 'Seychelles'},
	      {latLng: [7.35, 134.46], name: 'Palau'},
	      {latLng: [42.5, 1.51], name: 'Andorra'},
	      {latLng: [14.01, -60.98], name: 'Saint Lucia'},
	      {latLng: [6.91, 158.18], name: 'Federated States of Micronesia'},
	      {latLng: [1.3, 103.8], name: 'Singapore'},
	      {latLng: [1.46, 173.03], name: 'Kiribati'},
	      {latLng: [-21.13, -175.2], name: 'Tonga'},
	      {latLng: [15.3, -61.38], name: 'Dominica'},
	      {latLng: [-20.2, 57.5], name: 'Mauritius'},
	      {latLng: [26.02, 50.55], name: 'Bahrain'},
	      {latLng: [0.33, 6.73], name: 'São Tomé and Príncipe'}
	    ]
	  });
}

function genHybridMap() {
	
	var hybrid_map_options = {
		zoom: 8,
		center: new google.maps.LatLng(-34.397, 150.644),
		mapTypeId: google.maps.MapTypeId.HYBRID
	};

	basic_map = new google.maps.Map(document.getElementById('google-map-hybrid'), hybrid_map_options);
}

function genStreetViewMap() {

	var coords = new google.maps.LatLng(42.345573,-71.098326);

	var panoramaOptions = {
		position: coords,
		pov: {
			heading: 34,
			pitch: 10
		}
	};

	panorama = new google.maps.StreetViewPanorama(document.getElementById('google-map-street-view'), panoramaOptions);
}

function genMarkerMap() {
	
	var markers_map_options = {
		zoom: 10,
		center: new google.maps.LatLng(-33.92, 151.25),
		mapTypeId: google.maps.MapTypeId.ROADMAP
	};

	markers_map = new google.maps.Map(document.getElementById('google-map-markers'), markers_map_options);

	for (i = 0; i < locations.length; i++) {  
		marker = new google.maps.Marker({
			position: new google.maps.LatLng(locations[i][1], locations[i][2]),
			map: markers_map
		});

		google.maps.event.addListener(marker, 'click', (function(marker, i) {
			return function() {
				infowindow.setContent(locations[i][0]);
				infowindow.open(markers_map, marker);
			}
		})(marker, i));
	}
}

function genSearchMap() {
	search_address_map = new google.maps.Map(document.getElementById('google-map-search-address'), {
		mapTypeId: google.maps.MapTypeId.ROADMAP
	});

	var defaultBounds = new google.maps.LatLngBounds(
		new google.maps.LatLng(-33.8902, 151.1759),
		new google.maps.LatLng(-33.8474, 151.2631));

	search_address_map.fitBounds(defaultBounds);

	var input = (document.getElementById('pac-input'));
	search_address_map.controls[google.maps.ControlPosition.TOP_LEFT].push(input);

	var searchBox = new google.maps.places.SearchBox((input));

	google.maps.event.addListener(searchBox, 'places_changed', function() {
		var places = searchBox.getPlaces();

		for (var i = 0, marker; marker = markers[i]; i++) {
			marker.setMap(null);
		}

		markers = [];
		
		var bounds = new google.maps.LatLngBounds();
	
		for (var i = 0, place; place = places[i]; i++) {
			var image = {
				url: place.icon,
				size: new google.maps.Size(71, 71),
				origin: new google.maps.Point(0, 0),
				anchor: new google.maps.Point(17, 34),
				scaledSize: new google.maps.Size(25, 25)
			};

			var marker = new google.maps.Marker({
				map: search_address_map,
				icon: image,
				title: place.name,
				position: place.geometry.location
			});

			markers.push(marker);

			bounds.extend(place.geometry.location);
		}

		search_address_map.fitBounds(bounds);
	});
}

$(function () {
	if ( typeof load == 'undefined') {
		genBasicMap();
		genHybridMap();
		genStreetViewMap();
		genMarkerMap();
		genSearchMap();
	}
	genVisitorsMap();
});