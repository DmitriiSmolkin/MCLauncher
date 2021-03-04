package biz.minecraft.launcher.entity.client;

import java.net.URL;

public interface Download {

    String getPath();

    URL getURL();

    String getSha1();

    int getSize();

}
