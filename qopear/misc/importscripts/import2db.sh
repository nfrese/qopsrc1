#!/bin/bash

USER_NAME="qopuser"
DB="qop"
STORECMD="psql -h 10.0.0.17 -U $USER_NAME -d $DB"
TOSQLCMD="shp2pgsql -d -w -I -s 4326 -W \"LATIN1\""

# generate with generateimplist.sh!

...