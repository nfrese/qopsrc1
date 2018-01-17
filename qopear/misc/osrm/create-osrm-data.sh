#!/bin/bash -x

PBF_NAME=vienna_austria.osm.pbf
OSRM_NAME=vienna_austria.osrm
DATA_DIR=/home/opt2/osrm-data

wget https://s3.amazonaws.com/metro-extracts.mapzen.com/vienna_austria.osm.pbf

cd $DATA_DIR/

mkdir car
cd car
cp $DATA_DIR/$PBF_NAME .
osrm-extract -p /usr/local/share/osrm/profiles/car.lua $PBF_NAME
osrm-contract $OSRM_NAME

cd $DATA_DIR/

mkdir bicycle
cd bicycle
cp $DATA_DIR/$PBF_NAME .
osrm-extract -p /usr/local/share/osrm/profiles/bicycle.lua $PBF_NAME
osrm-contract $OSRM_NAME

cd $DATA_DIR/

mkdir foot
cd foot
cp $DATA_DIR/$PBF_NAME .
osrm-extract -p /usr/local/share/osrm/profiles/foot.lua $PBF_NAME
osrm-contract $OSRM_NAME
