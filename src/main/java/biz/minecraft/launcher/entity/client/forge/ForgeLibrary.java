package biz.minecraft.launcher.entity.client.forge;

import biz.minecraft.launcher.entity.client.minecraft.LibraryDownloadInfo;

/**
 * Different Forge versions may have different library formats,
 * this interface is designed to unify them.
 */
public interface ForgeLibrary {

    String getName();

    void setName(String name);

    LibraryDownloadInfo getDownloads();

    void setDownloads(LibraryDownloadInfo libraryDownloadInfo);

    // ExtractRules getExtract(); // Never seen it in this format version during research.

    // Map<OperatingSystem, String> getNatives(); // Never seen it in this format version during research.

    // List<CompatibilityRule> getRules(); // Never seen it in this format version during research.

}
