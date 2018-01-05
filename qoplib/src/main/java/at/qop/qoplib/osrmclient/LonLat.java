package at.qop.qoplib.osrmclient;

public class LonLat {
	
	public double lon;
	public double lat;
	
	public LonLat(double lon, double lat) {
		super();
		this.lon = lon;
		this.lat = lat;
	}
	
	@Override
	public String toString() {
		return lon + "," + lat;
	}
}
