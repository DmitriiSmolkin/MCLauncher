package biz.minecraft.launcher.entity.client.forge.library;

import biz.minecraft.launcher.entity.client.forge.ForgeArtifact;
import biz.minecraft.launcher.entity.client.forge.ForgeLibrary;
import biz.minecraft.launcher.entity.client.minecraft.DownloadInfo;
import biz.minecraft.launcher.entity.client.minecraft.LibraryDownloadInfo;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Java Object representation of the Forge's version < 25.0.9 (Minecraft 1.13.2) library.
 * Note: The definition of the version format is outside this class.
 */
public class PreviousForgeLibrary implements ForgeLibrary {

    private String name; // Package descriptor i.e. java3d:vecmath:1.5.2.
    private String url; // Libraries repository URL i.e. http://files.minecraftforge.net/maven/ Important: May be null.
    private List<String> checksums; // Array of possible hashes.
    private Boolean serverreq; // On null default – false.
    private Boolean clientreq; // On null default – true.

    public PreviousForgeLibrary() {
        // An empty constructor is required for the Gson.
    }

    public PreviousForgeLibrary(PreviousForgeLibrary previousForgeLibrary) {
        this.clientreq = previousForgeLibrary.getClientreq();
        this.serverreq = previousForgeLibrary.getServerreq();
    }

    public List<String> getChecksums() {
        return checksums;
    }

    public Boolean getClientreq() {
        return clientreq;
    }

    public Boolean getServerreq() {
        return serverreq;
    }

    public void setClientreq(Boolean clientreq) {
        this.clientreq = clientreq;
    }

    public void setServerreq(Boolean serverreq) {
        this.serverreq = serverreq;
    }

    public void setChecksums(List<String> checksums) {
        this.checksums = checksums;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Get full artifact URL from the name (classifier) and the repository URL.
     *
     * Example:
     * - this.name: org.ow2.asm:asm:7.2
     * - this.url: https://files.minecraftforge.net/
     *
     * Will return https://files.minecraftforge.net/maven/org/ow2/asm/asm/7.2/asm-7.2.jar
     *
     * Note: If the URL is empty will use default libraries repository URL (https://cloud.minecraft.biz/libraries/).
     *
     * @return URL.
     */
    private URL getUrl() {

        String stringURL = this.url;

        if (stringURL == null || stringURL.equals("")) {
            stringURL = "https://cloud.minecraft.biz/libraries/";
        }

        ForgeArtifact artifact = ForgeArtifact.from(this.name);
        stringURL += artifact.getPath();

        URL url = null;

        try {
            url = new URL(stringURL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    /**
     * Get the first checksum from the checksums array.
     *
     * Note: Forge's version < 25.0.9 (Minecraft 1.13.2) format had strange logic
     *       by which checksums had either one or a few hashes, and one of them
     *       expected to be correct.
     *
     * Appendix: If the first hash was not correct the launcher will be forced to reload the artifact.
     *
     * @return The first checksum in the checksums array if it's exists, otherwise null.
     */
    private String getSha1() {

        String sha1 = null;

        if (this.checksums != null && this.checksums.size() >= 1) {
            sha1 = this.checksums.get(0);
        }

        return sha1;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Converts this version format url, checksums array and name to LibraryDownloadInfo.
     *
     * @return LibraryDownloadInfo.
     */
    @Override
    public LibraryDownloadInfo getDownloads() {

        // Only for CLIENT

        if (this.clientreq == null || this.clientreq == true) {
            LibraryDownloadInfo libraryDownloadInfo = new LibraryDownloadInfo(); // artifact, classifiers

            DownloadInfo downloadInfo = new DownloadInfo(); // path, url, sha1, size

            downloadInfo.setUrl(this.getUrl());
            downloadInfo.setSha1(this.getSha1());
            downloadInfo.setPath(ForgeArtifact.from(this.name).getPath());

            libraryDownloadInfo.setArtifact(downloadInfo);

            return libraryDownloadInfo;
        }
        return null;
    }

    @Override
    public void setDownloads(LibraryDownloadInfo libraryDownloadInfo) {

        if (libraryDownloadInfo == null) {



        } else {

            DownloadInfo artifact = libraryDownloadInfo.getArtifact();

            List<String> checksums = new ArrayList<>();
            checksums.add(artifact.getSha1());
            this.checksums = checksums;
            this.name = this.getMavenNamingConventionStringFromPath(artifact.getPath());
            this.url = artifact.getURL().getProtocol() + "://" + artifact.getURL().getHost() + "/" + this.getFirstEndpoint(artifact.getURL()) + "/";
        }

    }

    private String getFirstEndpoint(URL url) {
        String path = url.getPath();
        String[] pathElements = path.split("/");
        return pathElements[1];
    }

    /**
     * From: org/lwjgl/lwjgl/lwjgl-platform/2.9.0/lwjgl-platform-2.9.0-natives-linux.jar
     * To: org.lwjgl.lwjgl:lwjgl-platform:2.9.0
     *
     * @param path
     * @return
     */
    private String getMavenNamingConventionStringFromPath(String path) {

        String[] pathElements = path.split("/");
        // Cut artifact
        String[] mavenSample = this.getSliceOfArray(pathElements, 0, pathElements.length - 1);
        // Cut name and version
        String[] nameAndVersionElements = this.getSliceOfArray(mavenSample, mavenSample.length - 2, mavenSample.length);
        // Cut package
        String[] groupElements = this.getSliceOfArray(mavenSample, 0, mavenSample.length - 2);

        String group = String.join(".", groupElements);
        String nameAndVersion = String.join(":", nameAndVersionElements);

        return group + ":" + nameAndVersion;

    }

    private String[] getSliceOfArray(String[] arr, int start, int end) {
        // Get the slice of the Array
        String[] slice = new String[end - start];

        // Copy elements of arr to slice
        for (int i = 0; i < slice.length; i++) {
            slice[i] = arr[start + i];
        }

        // return the slice
        return slice;
    }

//    @Override
//    public ExtractRules getExtract() {
//        // Never seen it in this format version during research.
//        return null;
//    }
//
//    @Override
//    public Map<OperatingSystem, String> getNatives() {
//        // Never seen it in this format version during research.
//        return null;
//    }

    /**
     * If the current library is only server required return DISALLOW action rule.
     *
     * @return List<CompatibilityRule> rules with Disallow action on library is only server required, otherwise null.
     */
//    @Override
//    public List<CompatibilityRule> getRules() {
//
//        List<CompatibilityRule> compatibilityRules = new ArrayList<>();
//
//        if (this.clientreq == null || this.clientreq == true) {
//            CompatibilityRule compatibilityRule = new CompatibilityRule();
//            compatibilityRule.setAction(Action.DISALLOW);
//            compatibilityRules.add(compatibilityRule);
//            return compatibilityRules;
//        }
//
//        return null;
//    }

}
