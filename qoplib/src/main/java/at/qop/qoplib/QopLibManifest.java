package at.qop.qoplib;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.Manifest;

public class QopLibManifest {
	
	public final Manifest manifest;
	
	public QopLibManifest() {
		super();
		this.manifest = read();
	}
	
	private Manifest read() {
		try {
			URLClassLoader cl =  (URLClassLoader) getClass().getClassLoader();
			URL url = cl.findResource("META-INF/MANIFEST.MF");
			Manifest manifest1 = new Manifest(url.openStream());
			return manifest1;
		} catch (Exception e) {
			System.err.println(e);
			return new Manifest();
		}
	}
	
	public String getSCMRevision() {
		return manifest.getMainAttributes().getValue("SCM-Revision");
	}
	
	public String getBuildTime() {
		return manifest.getMainAttributes().getValue("Build-Time");
	}
	
	public String getShortInfo() {
		return getSCMRevision() + "@" + getBuildTime();
	}
	
}
