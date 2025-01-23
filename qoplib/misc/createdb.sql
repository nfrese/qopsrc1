DROP TABLE IF EXISTS qop.osm_pois_category;
CREATE TABLE qop.osm_pois_category (
	mainkey text NULL,
	mainval text NULL,
	cat_id text NULL,
	supply_type text NULL
);
CREATE UNIQUE INDEX osm_pois_categories_mainkey_idx ON qop.osm_pois_category USING btree (mainkey, mainval);
CREATE INDEX osm_pois_category_cat_id_idx ON qop.osm_pois_category USING btree (cat_id);


