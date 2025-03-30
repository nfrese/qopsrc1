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

const url = 'http://localhost:4380/qop/rest/api/traveltime_to_pois?provide_data_url=true&poi_table=qop.v_pvs_all&cat_id=infra'
      + `&lat=${targetCoord[1]}&lng=${targetCoord[0]}&radius_meters=5000`
      + `&username=api&password=zrS/NVPqlIUwSjcU`

const featureLayer = new VectorLayer({
  title: 'added Layer',
  source: new VectorSource({
    format: new GeoJSON(),
    url:  url
    // // new GeoJSON().readFeatures(url , {
    // //   dataProjection: 'EPSG:4326',
    // //   featureProjection: 'EPSG:3857'
    // // })
    // new GeoJSON({
    //   // projection : 'EPSG:4326',
    //   url:  url   ,
    //   dataProjection: 'EPSG:4326',
    //   featureProjection: 'EPSG:3857'
    // })
  }),
  style: [lineStyle, styleMarkerTarget]
})

const map = new Map({
  // dataProjection: 'EPSG:4326',
  // projection: 'EPSG:4326',
  target: 'map',
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
    content.innerHTML =
      '<p>Category:</p><code>' + feature.get('category') + '</code><br>' +
      '<p>Title:</p><code>' + feature.get('title') + '</code><br>' +
      '<p>Description:</p><code>' + feature.get('description') + '</code>'
    //overlay.setPosition(coordinates);
  }
});

//setTimeout(() => { map.updateSize(); });

