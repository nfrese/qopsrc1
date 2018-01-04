package at.qop.qoplib.osrmclient;

public class LatLon {
	
	public double lat;
	public double lon;
	
	public LatLon(double lat, double lon) {
		super();
		this.lat = lat;
		this.lon = lon;
	}
	
	@Override
	public String toString() {
		return lat + "," + lon;
	}
}
