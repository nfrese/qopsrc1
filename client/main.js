import './style.css';
import {Map, View, Feature} from 'ol';
import TileLayer from 'ol/layer/Tile';
import GeoJSON from 'ol/format/GeoJSON';
import Point from 'ol/geom/Point';
import Style from 'ol/style/Style';
import Stroke from 'ol/style/Stroke';
import VectorLayer from 'ol/layer/Vector';
import VectorSource from 'ol/source/Vector';
import OSM from 'ol/source/OSM';
import Icon from 'ol/style/Icon';
import {fromLonLat} from 'ol/proj.js';
import {toLonLat} from 'ol/proj.js';
import $ from "jquery";
import * as am5 from "@amcharts/amcharts5";
import * as am5percent from "@amcharts/amcharts5/percent";

const content = document.getElementById('details');

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
		return lineStyle;
	}
}

window.targetCoord = [16.6000785, 48.4526522];

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
	showCat();
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

map.getViewport().addEventListener('contextmenu', function (evt) {
	evt.preventDefault();
	
	window.targetCoord = toLonLat(map.getEventCoordinate(evt));
	reset();
})

map.on('click', function (evt) {
	
	clearChart();
	
  const feature = map.forEachFeatureAtPixel(evt.pixel, function (feature) {
    return feature;
  });
  if (feature) {
    const poiCoordPrj = feature.getGeometry().getCoordinates();
	const poiCoord = toLonLat(poiCoordPrj);

    content.innerHTML = htmltable(feature.getProperties());
	
	const routeUrl = baseUrl() + '/qop/rest/api/route?'
	  + `lat=${window.targetCoord[1]}&lng=${window.targetCoord[0]}`
	  + `&dest_lat=${poiCoord[1]}&dest_lng=${poiCoord[0]}`
	  + `&username=api&password=zrS/NVPqlIUwSjcU`
	
	routeLayer.setSource(
		new VectorSource({
		    format: new GeoJSON(),
		    url:  routeUrl
		  })
		
	);
	
  }
  else {
	showChart();
  }
});

function htmltable (obj) {
	var html = '<table>';
	for (const [key, value] of Object.entries(obj)) {
		if (key === 'geometry')
		{
			continue;
		}
		
	  var valueHtml = value;
	  if (value != null && typeof value === "object")
	  {
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


$.get("http://localhost:4380/qop/rest/api/read?table=qop.pvs_category&username=api&password=zrS/NVPqlIUwSjcU",
	function(data) 
{ let select = document.querySelector("#layersSelect")  
	for (let elt of data.features){   
		let option = document.createElement("option");   
		option.text = elt.properties.label;   
		option.value = elt.properties.id;   
		select.appendChild(option); 
	}
	showCat();
})

$("#layersSelect").on('change', function() {
  console.log( this.value );
  showCat();
});

function baseUrl() {
	var develMode = window.location.host.includes("5173");
	if (develMode) {
		return 'http://localhost:4380';
	}
	else
	{
		return '';
	}
}

function getUrl() {
	const sel = document.querySelector("#layersSelect").value;

	const url = baseUrl() + '/qop/rest/api/traveltime_to_pois?'
		  + `provide_data_url=true&poi_table=qop.v_pvs_all&cat_id=${sel}`
		  + `&analysis_id=standort1`
	      + '&routingResultsAsProperties=true'
		  + `&lat=${window.targetCoord[1]}&lng=${window.targetCoord[0]}&radius_meters=5000`
	      + `&username=api&password=zrS/NVPqlIUwSjcU`
	return url;
}

function showCat() {

	featureLayer.setSource( new VectorSource({
		  format: new GeoJSON(),
		  url:  getUrl()
	}));
	
	showChart();
}

function clearChart() {
	root.container.children.clear();
	$('#chartdiv1').hide();
}

function showChart() {
	clearChart();
	$('#chartdiv1').show();
	
	
	var chart = root.container.children.push( 
	  am5percent.PieChart.new(root, {
	    layout: root.verticalLayout
	  }) 
	);

	// Create series
	var series = chart.series.push(
	  am5percent.PieSeries.new(root, {
	    name: "Series",
	    valueField: "count",
	    categoryField: "category"
	  })
	);
	
	$.get(getUrl(),
		function(data) {
			series.data.setAll(data.extended.frequencies);
		});
	
	

//	// Add legend
//	var legend = chart.children.push(am5.Legend.new(root, {
//		centerX: am5.percent(50),
//	  x: am5.percent(50),
//		layout: root.horizontalLayout
//	}));
//
//	legend.data.setAll(series.dataItems);

	series.labels.template.setAll({
	  text: "{category}: {value}",
	  textType: "circular",
	  inside: true,
	  radius: 10
	});
	
	
	
}
