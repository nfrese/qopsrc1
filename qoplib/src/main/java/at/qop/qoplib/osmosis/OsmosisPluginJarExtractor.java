package at.qop.qoplib.osmosis;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.CodeSource;
import java.util.Enumeration;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class OsmosisPluginJarExtractor {

    public static File extractAllPluginJars() throws IOException {
        Path tempDir = Files.createTempDirectory("osmosis-plugins");
        tempDir.toFile().deleteOnExit();

        String jarPath = determineJarPath();
        if (jarPath != null && jarPath.endsWith(".jar")) {
            extractAllFromFatJar(jarPath, tempDir);
        } else {
            throw new IllegalStateException("This only works from fat JAR packaging.");
        }

        return tempDir.toFile();
    }


    private static String determineJarPath() {
        try {
            URL url = OsmosisPluginJarExtractor.class.getResource(OsmosisPluginJarExtractor.class.getSimpleName() + ".class");

            if (url != null && url.toString().startsWith("jar:file:")) {
                // jar:file:/path/to/app.jar!/BOOT-INF/classes!/your/package/OsmosisPluginJarExtractor.class
                String fullPath = url.toString();
                String jarPath = fullPath.substring("jar:file:".length(), fullPath.indexOf("!"));
                return URLDecoder.decode(jarPath, StandardCharsets.UTF_8.name());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void extractAllFromFatJar(String jarPath, Path targetDir) throws IOException {
        try (JarFile jarFile = new JarFile(jarPath)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();

                if (name.startsWith("BOOT-INF/lib/") && name.endsWith(".jar")) {
                    String fileName = Paths.get(name).getFileName().toString();
                    if (!fileName.contains("osmosis-core")) { // exclude if needed
                        try (InputStream is = jarFile.getInputStream(entry)) {
                            Path outPath = targetDir.resolve(fileName);
                            Files.copy(is, outPath, StandardCopyOption.REPLACE_EXISTING);
                            outPath.toFile().deleteOnExit();
                        }
                    }
                }
            }
        }
    }
}


