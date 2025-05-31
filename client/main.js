import './style.css';
import { Map, View, Feature } from 'ol';
import TileLayer from 'ol/layer/Tile';
import GeoJSON from 'ol/format/GeoJSON';
import Point from 'ol/geom/Point';
import Style from 'ol/style/Style';
import Stroke from 'ol/style/Stroke';
import VectorLayer from 'ol/layer/Vector';
import VectorSource from 'ol/source/Vector';
import OSM from 'ol/source/OSM';
import Icon from 'ol/style/Icon';
import { fromLonLat } from 'ol/proj.js';
import { toLonLat } from 'ol/proj.js';
import $ from "jquery";
import * as am5 from "@amcharts/amcharts5";
import * as am5percent from "@amcharts/amcharts5/percent";

var lineStyle = new Style({
	stroke: new Stroke({ color: '#c0c0c0', width: 3 })
});

var styleMarker = new Style({
	image: new Icon({
		scale: .6, anchor: [0.5, 1],
		src: '//raw.githubusercontent.com/jonataswalker/map-utils/master/images/marker.png'
	})
});

function styleMarkerTarget(feature) {
	if (feature.getGeometry() instanceof Point) {
		if (feature.get("icon") != null) {
			var icon = feature.get("icon").replace('.svg', '');
			var color = feature.get("color").replace('#', '');
			var url = `https://cmbaimg.s3.amazonaws.com/map/icons/${icon}%2Bcircle%2B---${color}%2Bwhite.png`

			return new Style({
				image: new Icon({
					scale: 1, anchor: [0, 0],
					src: url
				})
			});
		}
		else {
			return new Style({
				image: new Icon({
					scale: .3, anchor: [0.3, 1],
					src: '//raw.githubusercontent.com/jonataswalker/map-utils/master/images/marker.png'
				})
			});
		}
	}
	else {
		const style = new Style({
			stroke: new Stroke({ color: feature.get("stroke"), width: 2 })
		});
		return style;
	}
}

window.targetCoord = [16.6000785, 48.4526522];

const urlParams = new URLSearchParams(window.location.search);
if (urlParams.get("lng") != null && urlParams.get("lat") != null) {
	window.targetCoord = [parseFloat(urlParams.get("lng")), parseFloat(urlParams.get("lat"))];
}
if (urlParams.get("radius_meters") != null ) {
	$('#radius_meters').val(parseInt(urlParams.get("radius_meters")));
}
else {
	$('#radius_meters').val(-1);
}

const featureLayer = new VectorLayer({
	title: 'added Layer',

	style: styleMarkerTarget
})

const routeLayer = new VectorLayer({
	title: 'added Layer',
	//  source: new VectorSource({
	//    format: new GeoJSON(),
	//  }),
	declutter: true,
	style: routeStyle
})

var root = am5.Root.new("chartdiv1");

var vector = new VectorLayer({

	style: [styleMarker]
});

function targetCoordPrj() {
	return fromLonLat(window.targetCoord);
}
function reset() {

	var marker1 = new Point(targetCoordPrj());
	var featureMarker1 = new Feature(marker1);

	vector.setSource(
		new VectorSource({
			features: [featureMarker1]
		}),
	)
	routeLayer.setSource(
				null);
	showCat();
}

function routeStyle(feature) {
	const style = new Style({
		stroke: new Stroke({ color: feature.get("stroke"), width: 3 })
	});
	return style;
}


const map = new Map({
	target: 'mapId',
	layers: [
		new TileLayer({
			source: new OSM()
		}), vector, featureLayer, routeLayer
	],
	view: new View({
		center: targetCoordPrj(),
		zoom: 15
	})
});

map.getViewport().addEventListener('contextmenu', function(evt) {
	evt.preventDefault();

	window.targetCoord = toLonLat(map.getEventCoordinate(evt));
	reset();
})

map.on('click', function(evt) {

	clearChart();

	const feature = map.forEachFeatureAtPixel(evt.pixel, function(feature) {
		return feature;
	});
	if (feature) {
		const poiCoordPrj = feature.getGeometry().getCoordinates();
		const poiCoord = toLonLat(poiCoordPrj);

		$('#details').html( htmltable(feature.getProperties()));

		if (feature.get('icon')) {

			const routeUrl = baseUrl() + '/qop/rest/api/route?'
				+ `lat=${window.targetCoord[1]}&lng=${window.targetCoord[0]}`
				+ `&dest_lat=${poiCoord[1]}&dest_lng=${poiCoord[0]}`
				+ authParams();

			routeLayer.setSource(
				new VectorSource({
					format: new GeoJSON(),
					url: routeUrl
				})

			);
		}

	}
	else {
		showChart();
	}
});

$('#radius_meters').on('change', function() {
	reset();
});

$('#text_filter').on('change', function() {
	reset();
});

function htmltable(obj) {
	var html = '<table>';
	for (const [key, value] of Object.entries(obj)) {
		if (key === 'geometry') {
			continue;
		}

		var valueHtml = value;
		if (value != null && typeof value === "object") {
			valueHtml = htmltable(value);
		}
		html += (`<tr><td valign='top'>${key}:</td><td>${valueHtml}</td></tr>`);
	}
	html += '</table>'
	return html;
}

reset();

$(document).ready(function() {
	console.log("ready");
})


$.get(baseUrl() + "/qop/rest/api/read?table=qop.v_pvs_category_main" + authParams(),
	function(data) {
		let select = document.querySelector("#layersSelect")
		for (let elt of data.features) {
			let option = document.createElement("option");
			option.text = elt.properties.label;
			option.value = elt.properties.id;
			select.appendChild(option);
		}
		{
			let option = document.createElement("option");
			option.text = "<<not categorized>>";
			option.value = "without";
			option.selected = false;
			select.appendChild(option);
		}
		{
			let option = document.createElement("option");
			option.text = "STANDORT 1";
			option.value = "standort1";
			option.selected = true;
			select.appendChild(option);
		}
		{
			let option = document.createElement("option");
			option.text = "STANDORT WALKABILITY";
			option.value = "standort_walkability";
			option.selected = true;
			select.appendChild(option);
		}
		showCat();
	})

$("#layersSelect").on('change', function() {
	reset();
	//showCat();
});

$("#modesSelect").on('change', function() {
	reset();
	//showCat();
});

$("#timeFilterSelect").on('change', function() {
	reset();
	//showCat();
});

function baseUrl() {
	var develMode = window.location.host.includes("5173");
	if (develMode) {
		return 'http://localhost:4380';
	}
	else {
		return '';
	}
}

function selCat() {
	return document.querySelector("#layersSelect").value;
}

function analysisMode() {
	return selCat().startsWith('standort');
}

function authParams() {
	const urlParams = new URLSearchParams(window.location.search);
	
	return `&username=${urlParams.get('username')}&password=${urlParams.get('password')}`;
}

function getUrl() {
	const sel = selCat();
	
	// Get the select element
	var dnd = document.querySelector('#modesSelect');

	// Get all selected values
	var selectedModes = Array.from(dnd.options) // Convert options to an array
	    .filter(option => option.selected)      // Filter selected options
	    .map(option => option.value); 
		
	var textFilter = $('#text_filter').val();
	
	var timeFilter = $('#timeFilterSelect').val();
	
	const url = baseUrl() + '/qop/rest/api/traveltime_to_pois?'
		+ `provide_data_url=true&poi_table=qop.v_pvs_all&cat_id=${sel}`
		+ (analysisMode() ? `&analysis_id=${selCat()}` : '')
		+ '&modes='+ selectedModes.join()
		+ '&text_filter=' + textFilter
		+ '&time_filter=' + timeFilter
		+ '&routingResultsAsProperties=true'
		+ `&lat=${window.targetCoord[1]}&lng=${window.targetCoord[0]}`
		+ `&radius_meters=${$('#radius_meters').val()}`
		+ authParams();
	return url;
}

function showCat() {
	
	featureLayer.setSource(new VectorSource({
		format: new GeoJSON(),
		url: getUrl()
	}));

	showChart();
}

function clearChart() {
	root.container.children.clear();
	$('#chartdiv1').hide();
}

function showChart() {
	clearChart();
	if (!analysisMode())
	{
		return;
	}
	const ana2 = true;
	
	$('#chartdiv1').show();

	var chart = root.container.children.push(
		am5percent.PieChart.new(root, {
			layout: root.verticalLayout
		})
	);

	var conf = 	{
				name: "Series",
				valueField: "count",
				categoryField: 'label'
			};
			
	if (ana2) { conf.valueField= 'one'; }
	
	// Create series
	var series = chart.series.push(
		am5percent.PieSeries.new(root, conf)
	);

	series.slices.template.setAll({
		
		templateField: "columnSettings"
	});
	
	$.get(getUrl(),
		function(data) {
			$('#details').html( htmltable(data.extended.frequencies));
			
			for (const el of data.extended.frequencies) {
				
				el.one = 1;
				el.columnSettings = {
				    fill: am5.color(el.color),
					fillOpacity: ana2 ? el.rating : 0.7
				}
			}
			
			
			series.data.setAll(data.extended.frequencies);
			
		});

	series.labels.template.setAll({
		text: ana2 ? "{category}: {count}": "{category}: {value}",
		inside: true,
		radius: 5,
		
	});
	
}
