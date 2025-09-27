package at.qop.qoplib.osmosis;
import java.io.*;
import java.net.*;
import java.nio.file.*;

public class RedirectAwareDownloader {

    private static final int MAX_REDIRECTS = 10;

    public static void download(String fileURL, String savePath) throws IOException {
        URL url = new URL(fileURL);
        int redirectCount = 0;

        while (true) {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setInstanceFollowRedirects(false); // We handle it manually
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();

            if (isRedirect(responseCode)) {
                if (redirectCount++ >= MAX_REDIRECTS) {
                    throw new IOException("Too many redirects");
                }

                String location = conn.getHeaderField("Location");
                if (location == null) {
                    throw new IOException("Redirect without Location header");
                }

                url = new URL(url, location); // Handle relative URLs
                continue;
            }

            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (InputStream in = conn.getInputStream()) {
                    Files.copy(in, Paths.get(savePath), StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("Downloaded to: " + savePath);
                    break;
                }
            } else {
                throw new IOException("Failed with HTTP code: " + responseCode);
            }
        }
    }

    private static boolean isRedirect(int code) {
        return code == HttpURLConnection.HTTP_MOVED_TEMP || // 302
               code == HttpURLConnection.HTTP_MOVED_PERM || // 301
               code == HttpURLConnection.HTTP_SEE_OTHER ||  // 303
               code == 307 || // Temporary Redirect
               code == 308;   // Permanent Redirect
    }

    public static void main(String[] args) {
        String url = "https://example.com/download";
        String savePath = "file.zip";

        try {
            download(url, savePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
