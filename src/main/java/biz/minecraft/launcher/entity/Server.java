package biz.minecraft.launcher.entity;

import com.google.gson.annotations.SerializedName;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

public class Server {

    private String name;
    private String cover;
    private String version;
    @SerializedName("client_version") private String clientVersion;
    private String description;

    private String ip;
    private String port;
    private Boolean online;
    private String[] players;

    public Server() { }

    public String getName() { return name; }
    public String getVersion() { return version; }
    public String getClientVersion() { return clientVersion; }
    public String getDescription() { return description; }

    public String getIp() { return ip; }
    public String getPort() { return port; }
    public Boolean isOnline() { return online; }
    public String[] getPlayers() { return players; }

    public BufferedImage getCover() {

        URL url = this.getCoverURL();
        Optional<BufferedImage> cover = null;

        try {
            cover = Optional.of(ImageIO.read(url)); // 565x174
        } catch (IOException e) {
            e.printStackTrace();
        }

        return cover.orElse(this.getDefaultCover());

    }

    public URL getCoverURL() {

        URL url = null;

        try {
            url = new URL(this.cover);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;

    }

    public BufferedImage getDefaultCover() {

        BufferedImage defaultCover = null;

        try {
            defaultCover = ImageIO.read(getClass().getResource("/images/news-cover.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return defaultCover;
    }

}
