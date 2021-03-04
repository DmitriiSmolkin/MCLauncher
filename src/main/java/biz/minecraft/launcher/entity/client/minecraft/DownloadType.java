package biz.minecraft.launcher.entity.client.minecraft;

import com.google.gson.annotations.SerializedName;

public enum DownloadType {

    @SerializedName("client") CLIENT,
    @SerializedName("client_mappings") CLIENT_MAPPINGS,
    @SerializedName("server") SERVER,
    @SerializedName("server_mappings") SERVER_MAPPINGS

}
