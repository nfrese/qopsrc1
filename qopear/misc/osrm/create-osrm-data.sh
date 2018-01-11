#!/bin/bash -x

PBF_NAME=vienna_austria.osm.pbf
OSRM_NAME=vienna_austria.osrm

wget https://s3.amazonaws.com/metro-extracts.mapzen.com/vienna_austria.osm.pbf

cd ~/osrm-data

mkdir car
cd car
cp ~/osrm-data/$PBF_NAME .
osrm-extract -p /usr/local/share/osrm/profiles/car.lua $PBF_NAME
osrm-contract $OSRM_NAME

cd ~/osrm-data

mkdir bicycle
cd bicycle
cp ~/osrm-data/$PBF_NAME .
osrm-extract -p /usr/local/share/osrm/profiles/bicycle.lua $PBF_NAME
osrm-contract $OSRM_NAME

cd ~/osrm-data

mkdir foot
cd foot
cp ~/osrm-data/$PBF_NAME .
osrm-extract -p /usr/local/share/osrm/profiles/foot.lua $PBF_NAME
osrm-contract $OSRM_NAME


