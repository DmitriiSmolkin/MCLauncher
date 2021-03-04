package biz.minecraft.launcher.entity.client.forge;

import biz.minecraft.launcher.entity.client.Download;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;

public class ForgeDownloadInfo implements Download {

    private final static Logger LOGGER = LoggerFactory.getLogger(ForgeDownloadInfo.class);

    private String url;
    private String path;
    private String sha1;
    private int size;

    // TODO: Using GSON make url -> URL, and on MalformedURLException or empty constructor parameter do following get URL methods.

    /**
     * Generate URL from path.
     *
     * Path: "net/minecraftforge/forge/1.16.2-33.0.60"
     * Result: "https://cloud.minecraft.biz/libraries/net/minecraftforge/forge/1.16.2-33.0.60/forge-1.16.2-33.0.60.jar"
     *
     * @return URL by path using project's repository or null.
     */
    private URL getURLByPath() {

        url = "https://cloud.minecraft.biz/libraries/" + this.path;

        URL result = null;

        try {
            result = new URL(url);
        } catch (MalformedURLException e) {
            LOGGER.warn("Malformed Forge's library generated from path URL: '" + url + "'", e);
        }

        return result;
    }

    /**
     * Get download URL.
     *
     * In some cases forge may leave download's url blanked,
     * the method will try to generate URL based on path and project's repository URL.
     *
     * @return URL or null, when failed to generate.
     */
    @Override
    public URL getURL() {

        URL url = null;

        try {
            url = new URL(this.url);
        } catch (MalformedURLException e) {
            LOGGER.warn("Malformed Forge's library URL: '" + this.url + "' Trying to generate from path...");
        }

        if (this.url == null || this.url.equals("")) {
            url = getURLByPath();
        }

        return url;
    }

    @Override
    public String getSha1() {
        return this.sha1;
    }

    @Override
    public String getPath() {
        return this.path;
    }

    @Override
    public int getSize() {
        return this.size;
    }

}
