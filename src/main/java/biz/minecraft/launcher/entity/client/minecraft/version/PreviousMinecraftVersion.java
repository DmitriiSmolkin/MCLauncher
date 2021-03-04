package biz.minecraft.launcher.entity.client.minecraft.version;

import biz.minecraft.launcher.entity.client.minecraft.DownloadInfo;
import biz.minecraft.launcher.entity.client.minecraft.DownloadType;
import biz.minecraft.launcher.entity.client.minecraft.Library;
import biz.minecraft.launcher.entity.client.minecraft.argument.ArgumentType;
import biz.minecraft.launcher.entity.client.minecraft.argument.Argument;

import biz.minecraft.launcher.entity.client.minecraft.MinecraftVersion;

import java.util.List;
import java.util.Map;

/**
 * Java Object representation of the Minecraft version >= pre-1.6 (old_alpha rd-132211).
 */
public class PreviousMinecraftVersion implements MinecraftVersion {

    private String assets;
    private Map<DownloadType, DownloadInfo> downloads;
    private String id;
    private List<Library> libraries;
    private Map<ArgumentType, List<Argument>> minecraftArguments;
    private String mainClass;

    public PreviousMinecraftVersion() {
        // An empty constructor is required for the Gson.
    }

    @Override
    public String getAssets() {
        return this.assets;
    }

    @Override
    public Map<DownloadType, DownloadInfo> getDownloads() {
        return this.downloads;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public List<Library> getLibraries() {
        return this.libraries;
    }

    @Override
    public String getMainClass() {
        return this.mainClass;
    }

    @Override
    public Map<ArgumentType, List<Argument>> getArguments() {
        return this.minecraftArguments;
    }

}
