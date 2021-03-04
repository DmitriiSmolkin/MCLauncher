package biz.minecraft.launcher.entity.client.forge.library;

import biz.minecraft.launcher.entity.client.forge.ForgeLibrary;
import biz.minecraft.launcher.entity.client.minecraft.LibraryDownloadInfo;

/**
 * Java Object representation of the Forge's version > 25.0.9 (Minecraft 1.13.2) library.
 * Note: The definition of the version format is outside this class.
 */
public class LatestForgeLibrary implements ForgeLibrary {

    private LibraryDownloadInfo downloads;
//    private ExtractRules extract;
    private String name;
//    private Map<OperatingSystem, String> natives;
//    private List<CompatibilityRule> rules;

    public LatestForgeLibrary() {
        // An empty constructor is required for the Gson.
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public LibraryDownloadInfo getDownloads() {
        return this.downloads;
    }

    @Override
    public void setDownloads(LibraryDownloadInfo downloads) {
        this.downloads = downloads;
    }

//    @Override
//    public ExtractRules getExtract() {
//        return this.extract;
//    }
//
//    @Override
//    public Map<OperatingSystem, String> getNatives() {
//        return this.natives;
//    }
//
//    @Override
//    public List<CompatibilityRule> getRules() {
//        return this.rules;
//    }

}
