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

const content = document.getElementById('details');

var lineStyle = new Style({
  stroke: new Stroke({ color: '#ffcc33', width: 3 })
});
var styleMarker = new Style({
  image: new Icon({
    scale: .7, anchor: [0.5, 1],
    src: '//raw.githubusercontent.com/jonataswalker/map-utils/master/images/marker.png'
  })
});

var styleMarkerTarget = new Style({
  image: new Icon({
    scale: .4, anchor: [0.5, 1],
    src: '//raw.githubusercontent.com/jonataswalker/map-utils/master/images/marker.png'
  })
});

const targetCoord = [16.6000785, 48.4526522];
var targetCoordPrj = fromLonLat(targetCoord);

var marker1 = new Point(targetCoordPrj);
var featureMarker1 = new Feature(marker1);

//var line = new ol.geom.LineString([coord1, coord2]);
//var lineFeature = new ol.Feature(line);

var vector = new VectorLayer({
  source: new VectorSource({
  	features: [featureMarker1]
  }),
  style: [lineStyle, styleMarker]
});

const url = 'http://localhost:4380/qop/rest/api/traveltime_to_pois?'
	  + 'provide_data_url=true&poi_table=qop.v_pvs_all&cat_id=infra'
      + '&routingResultsAsProperties=true'
	  + `&lat=${targetCoord[1]}&lng=${targetCoord[0]}&radius_meters=5000`
      + `&username=api&password=zrS/NVPqlIUwSjcU`

const featureLayer = new VectorLayer({
  title: 'added Layer',
  source: new VectorSource({
    format: new GeoJSON(),
    url:  url
  }),
  style: [lineStyle, styleMarkerTarget]
})

const map = new Map({
  target: 'mapId',
  layers: [
    new TileLayer({
      source: new OSM()
    }), vector, featureLayer
  ],
  view: new View({
    center: targetCoordPrj,
    zoom: 15
  })
});

console.log(map.getView().getProjection());
console.log(url);

//map.setSize(500,500);

map.on('click', function (evt) {
  const feature = map.forEachFeatureAtPixel(evt.pixel, function (feature) {
    return feature;
  });
  if (feature) {
    const coordinates = feature.getGeometry().getCoordinates();

    content.innerHTML = htmltable(feature.getProperties());
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
	  html += (`<tr><td valign='top'>${key}:</td><td> ${valueHtml}</td></tr>`);
	}
	html += '</table>'
	return html;
}
