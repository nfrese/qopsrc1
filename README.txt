migrating  to new db:

create role qopuser
create extension postgis_raster;

psql -v ON_ERROR_STOP=1 --host=localhost --port=5432 --username=postgres -d qop-dev < /xxx/qop_dump_all/dump.sql