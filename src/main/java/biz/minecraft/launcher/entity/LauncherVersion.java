package biz.minecraft.launcher.entity;

import com.google.gson.Gson;

import java.io.InputStreamReader;
import java.net.URL;

/**
 * Java Object representation of the latest launcher version JSON object from web.
 *
 * Endpoint: 'https://cloud.minecraft.biz/launcher/version.json'
 * Note: The current launcher's version is located in the Main class.
 */
public class LauncherVersion {

    private String version;
    private URL url;
    private String path;

    public LauncherVersion() {
        // An empty constructor is required for the Gson.
    }

    /**
     * Get LauncherVersion Java object from JSON.
     *
     * @param urlString String URL to JSON object.
     * @return LauncherVersion object, fields will be empty on parsing error.
     */
    public static LauncherVersion from(String urlString) {

        Gson gson = new Gson();
        LauncherVersion launcherVersion = new LauncherVersion();

        try (InputStreamReader reader = new InputStreamReader(new URL(urlString).openStream())) {
            launcherVersion = gson.fromJson(reader, LauncherVersion.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return launcherVersion;
    }

    public String getVersion() { return version; }

    public URL getUrl() { return url; }

    public String getPath() { return path; }

}
