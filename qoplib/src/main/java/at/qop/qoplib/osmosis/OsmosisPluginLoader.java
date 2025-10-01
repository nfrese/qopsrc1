package at.qop.qoplib.osmosis;

import java.io.File;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class OsmosisPluginLoader implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        //File pluginDir = OsmosisPluginJarExtractor.extractAllPluginJars();
        //System.out.println("Plugins extracted to: " + pluginDir.getAbsolutePath());

        // Pass plugin directory to Osmosis or JPF, e.g. via system property
        //System.setProperty("plugin.dir", pluginDir.getAbsolutePath());

        // You can start Osmosis or register plugins here if you want
    }
}
