package biz.minecraft.launcher.entity.client.minecraft;

import biz.minecraft.launcher.entity.client.Download;

import java.net.URL;

public class DownloadInfo implements Download {

    private URL url;
    private String path;
    private String sha1;
    private int size;

    @Override
    public URL getURL() {
        return this.url;
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

    public void setUrl(URL url) {
        this.url = url;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setSha1(String sha1) {
        this.sha1 = sha1;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
