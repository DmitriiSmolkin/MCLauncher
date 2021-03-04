package biz.minecraft.launcher.entity.client.minecraft;

import biz.minecraft.launcher.OperatingSystem;
import biz.minecraft.launcher.entity.client.Download;
import biz.minecraft.launcher.entity.client.Downloader;
import biz.minecraft.launcher.entity.client.minecraft.argument.Argument;
import biz.minecraft.launcher.entity.client.minecraft.argument.ArgumentType;
import biz.minecraft.launcher.entity.client.minecraft.argument.ArgumentsDeserializer;
import biz.minecraft.launcher.entity.client.minecraft.argument.ArgumentsFromStringDeserializer;
import biz.minecraft.launcher.entity.client.minecraft.compatibility.Action;
import biz.minecraft.launcher.entity.client.minecraft.compatibility.CompatibilityRule;
import biz.minecraft.launcher.entity.client.minecraft.version.LatestMinecraftVersion;
import biz.minecraft.launcher.entity.client.minecraft.version.PreviousMinecraftVersion;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * High-level universal Minecraft Version Convention.
 */
public interface MinecraftVersion {

    OperatingSystem os = OperatingSystem.getCurrentPlatform();

    String getAssets();

    Map<DownloadType, DownloadInfo> getDownloads();

    String getId(); // Game version, i.e. "1.16.3"

    List<Library> getLibraries();

    String getMainClass();

    Map<ArgumentType, List<Argument>> getArguments();

    static MinecraftVersion from(URL url) {
        MinecraftVersion minecraftVersion = null;

        try (InputStream is = url.openStream()) {

            String json = IOUtils.toString(is, StandardCharsets.UTF_8);

            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            Set<Map.Entry<String, JsonElement>> entries = jsonObject.entrySet();

            for (Map.Entry<String, JsonElement> entry : entries) {
                if (entry.getKey().equals("arguments")) {
                    // Minecraft version >= 1.13 (17w43a)
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    Type argumentsType = new TypeToken<Map<ArgumentType, List<Argument>>>(){}.getType();
                    gsonBuilder.registerTypeAdapter(argumentsType, new ArgumentsDeserializer());
                    Gson gs = gsonBuilder.create();

                    minecraftVersion = gs.fromJson(json, LatestMinecraftVersion.class);
                } else if (entry.getKey().equals("minecraftArguments")) {
                    // Minecraft version <= 1.12.2
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    Type argumentsType = new TypeToken<Map<ArgumentType, List<Argument>>>(){}.getType();
                    gsonBuilder.registerTypeAdapter(argumentsType, new ArgumentsFromStringDeserializer());
                    Gson gs = gsonBuilder.create();

                    minecraftVersion = gs.fromJson(json, PreviousMinecraftVersion.class);
                }
            }

            if (minecraftVersion == null) {
                throw new RuntimeException("Invalid Minecraft Version format.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return minecraftVersion;
    }

    /**
     * Get required (allowed) for the current OS libraries.
     *
     * @return List of libraries needed for the current OS.
     */
    default List<Library> getSystemRequiredLibraries() {

        List<Library> libraries = this.getLibraries();
        List<Library> requiredLibraries = new ArrayList<>();

        for (Library library : libraries) {
            DownloadInfo artifact = library.getDownloads().getArtifact();
            List<CompatibilityRule> rules = library.getRules();

            if (artifact == null) continue;

            String path = null;

            if (rules == null) {
                requiredLibraries.add(library);
            } else {
                Action actionForCurrentOS = null;
                for (CompatibilityRule rule : rules) {
                    if (rule.getAppliedAction() != null) {
                        actionForCurrentOS = rule.getAction();
                    }
                }
                if (actionForCurrentOS == Action.ALLOW) {
                    requiredLibraries.add(library);
                }
            }
        }

        return requiredLibraries;
    }

    /**
     * Get allowed for the current OS libraries classpath.
     *
     * @param clientDirectory Absolute path to client's directory, i.e. '/../../Minecraft.biz/actual/'.
     * @return List of the absolute paths to the required for the current OS libraries.
     */
    default List<String> getClasspath(File clientDirectory) {

        List<String> classpath = new LinkedList<>();
        List<Library> libraries = getSystemRequiredLibraries();

        for (Library library : libraries) {
            DownloadInfo artifact = library.getDownloads().getArtifact();
            List<CompatibilityRule> rules = library.getRules();

            if (artifactRequiredForOS(artifact, clientDirectory, rules)) {
                File file = new File(clientDirectory, "libraries/" + artifact.getPath());
                classpath.add(file.getAbsolutePath());
            }
        }

        return classpath;
    }

    default boolean artifactRequiredForOS(DownloadInfo artifact, File clientDirectory, List<CompatibilityRule> rules) {

        if (artifact == null) return false;

        String path = null;

        if (rules == null) {
            File file = new File(clientDirectory, "libraries/" + artifact.getPath());
            return true;
        } else {
            Action actionForCurrentOS = null;
            for (CompatibilityRule rule : rules) {
                if (rule.getAppliedAction() != null) {
                    actionForCurrentOS = rule.getAction();
                }
            }
            if (actionForCurrentOS == Action.ALLOW) {
                File file = new File(clientDirectory, "libraries/" + artifact.getPath());
                return true;
            }
        }

        return false;
    }

    /**
     * Get the client artifact that need to be updated.
     *
     * @param clientDirectory Absolute path to client's directory, i.e. '/../../Minecraft.biz/actual/'.
     * @return Client artifact (download) that need to be updated, if the client is up to date will return 'null'.
     */
    default Download getClient(File clientDirectory) {
        Map<DownloadType, DownloadInfo> downloads = this.getDownloads();
        if (downloads.containsKey(DownloadType.CLIENT)) {
            DownloadInfo clientArtifact = downloads.get(DownloadType.CLIENT);
            File file = new File(clientDirectory, "versions/" + getId() + "/minecraft.jar");
            clientArtifact.setPath(file.getAbsolutePath());
            if ((file.exists() && !file.isDirectory() && !Downloader.checksumValid(file, clientArtifact.getSha1())) || (!file.exists())) {
                return clientArtifact;
            }
        }
        return null;
    }

    /**
     * Get a list of libraries artifacts that need to be updated.
     *
     * @param clientDirectory Absolute path to client's directory, i.e. '/../../Minecraft.biz/actual/'.
     * @return List of library artifacts (downloads) that need to be updated.
     */
    default List<Download> getLibraries(File clientDirectory) {

        List<Download> downloads = new ArrayList<>();
        List<Library> libraries = this.getLibraries();

        for (Library library : libraries) {
            DownloadInfo artifact = library.getDownloads().getArtifact();
            List<CompatibilityRule> rules = library.getRules();

            if (artifactRequiredForUpdate(artifact, clientDirectory, rules)) {
                String path = new File(clientDirectory, "libraries/" + artifact.getPath()).getAbsolutePath();
                artifact.setPath(path);
                downloads.add(artifact);
            }
        }

        return downloads;
    }

    /**
     * Get a list of native libraries artifacts that need to be updated.
     *
     * @param clientDirectory Absolute path to client's directory, i.e. '/../../Minecraft.biz/actual/'.
     * @return List of native library artifacts (downloads) that need to be updated.
     */
    default List<Download> getNatives(File clientDirectory) {

        List<Download> artifacts = new ArrayList<>();

        for (Library library : getLibraries()) {

            Map<OperatingSystem, String> natives = library.getNatives();
            Map<String, DownloadInfo> classifiers = library.getDownloads().getClassifiers();

            if (natives != null && natives.get(this.os) != null) {

                DownloadInfo nativeLibrary = classifiers.get(natives.get(this.os));
                File file = new File(clientDirectory, "libraries/" + nativeLibrary.getPath());
                nativeLibrary.setPath(file.getAbsolutePath());

                if ((file.exists() && !file.isDirectory() && !Downloader.checksumValid(file, nativeLibrary.getSha1())) || (!file.exists())) {
                    artifacts.add(nativeLibrary);
                }
            }
        }

        return artifacts;
    }

    /**
     * Get list of libraries that contain native libraries required by the operating system.
     *
     * @return List of libraries with required natives.
     */
    default List<Library> getLibrariesWithNatives(File clientDirectory) {

        List<Library> libraries = new ArrayList<>();

        for (Library library : getLibraries()) {
            Map<OperatingSystem, String> natives = library.getNatives();
            Map<String, DownloadInfo> classifiers = library.getDownloads().getClassifiers();
            if (natives != null && natives.get(this.os) != null) {
                DownloadInfo nativeLibrary = classifiers.get(natives.get(os));
                libraries.add(library);
            }
        }

        return libraries;
    }

    /**
     * Is artifact need to be updated.
     *
     * @param artifact
     * @param clientDirectory
     * @param rules
     * @return
     */
    default boolean artifactRequiredForUpdate(DownloadInfo artifact, File clientDirectory, List<CompatibilityRule> rules) {

        if (artifact == null) return false;

        String path = null;

        if (rules == null) {
            File file = new File(clientDirectory, "libraries/" + artifact.getPath());
            if ((file.exists() && !file.isDirectory() && !Downloader.checksumValid(file, artifact.getSha1())) || (!file.exists())) {
                return true;
            }
        } else {
            Action actionForCurrentOS = null;
            for (CompatibilityRule rule : rules) {
                if (rule.getAppliedAction() != null) {
                    actionForCurrentOS = rule.getAction();
                }
            }
            if (actionForCurrentOS == Action.ALLOW) {
                File file = new File(clientDirectory, "libraries/" + artifact.getPath());
                if ((file.exists() && !file.isDirectory() && !Downloader.checksumValid(file, artifact.getSha1())) || (!file.exists())) {
                    return true;
                }
            }
        }

        return false;
    }

    default Download getAssets(File clientDirectory) {

        URL assetsURL = Downloader.url("https://cloud.minecraft.biz/assets/" + getAssets() + ".zip");
        File assetsFile = new File(clientDirectory, "assets.zip");
        File assetsDirectory = new File(clientDirectory, "assets");

        if (!assetsDirectory.exists()) {
            DownloadInfo assets = new DownloadInfo();
            assets.setPath(assetsFile.getAbsolutePath());
            assets.setUrl(assetsURL);
            return assets;
        }
        return null;
    }

    default List<Download> getExtraDownloads(File clientDirectory, URL clientURL) {

        List<Download> extraDownloads = new ArrayList<>();

        try (InputStream is = clientURL.openStream()) {
            String json = IOUtils.toString(is, StandardCharsets.UTF_8);
            Gson gson = new Gson();
            Download[] downloads = gson.fromJson(json, DownloadInfo[].class);
            for (Download download : downloads) {
                File file = new File(clientDirectory, download.getPath());

                if (file.exists() && !file.isDirectory() && download.getSha1() == null) {

                } else if ((file.exists() && !file.isDirectory() && !Downloader.checksumValid(file, download.getSha1())) || (!file.exists())) {
                    extraDownloads.add(download);
                }

            }

            return Arrays.asList(downloads);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    // get client jar -> Download client -> DownloadWorker(...)
    // get libraries to update -> DownloadWorker(List<Download> downloads)
    // get natives to update   -> DownloadWorker(List<Download> downloads) // DownloadFilesTask()
    // get system required libraries with required natives -> UnpackNativesTask(List<Library> librariesContainingRequiredNatives)

    default void updateClient(File localClientDirectory, String clientVersion) {
        Map<DownloadType, DownloadInfo> downloads = this.getDownloads();
        if (downloads.containsKey(DownloadType.CLIENT)) {
            DownloadInfo clientDownload = downloads.get(DownloadType.CLIENT);
            File file = new File(localClientDirectory, "versions/" + clientVersion + "/minecraft.jar");
            if ((file.exists() && !file.isDirectory() && !Downloader.checksumValid(file, clientDownload.getSha1())) || (!file.exists())) {
                Downloader.downloadFromURL(clientDownload.getURL(), file);
            }
        }
    }

    /**
     *
     * @return Classpath
     */
    default List<String> updateLibraries(File localClientDirectory, String clientVersion) {
        List<String> classpath = new LinkedList<>();
        List<Library> libraries = this.getLibraries();

        for (Library library : libraries) {
            DownloadInfo artifact = library.getDownloads().getArtifact();
            List<CompatibilityRule> rules = library.getRules();

            String path = this.updateArtifact(localClientDirectory, artifact, rules);

            if (path != null && !path.equals("")) classpath.add(path);

            Map<OperatingSystem, String> natives = library.getNatives();
            Map<String, DownloadInfo> classifiers = library.getDownloads().getClassifiers();
            ExtractRules extractRules = library.getExtract();

            this.updateNatives(localClientDirectory, clientVersion, natives, classifiers, extractRules);
        }

        return classpath;
    }

    /**
     *
     * @param localClientDirectory
     * @param artifact
     * @param rules
     * @return Absolute path for classpath or null
     */
    default String updateArtifact(File localClientDirectory, DownloadInfo artifact, List<CompatibilityRule> rules) {

        if (artifact == null) return null;

        String path = null;

        if (rules == null) {
            File file = new File(localClientDirectory, "libraries/" + artifact.getPath());
            path = file.getAbsolutePath();
            if ((file.exists() && !file.isDirectory() && !Downloader.checksumValid(file, artifact.getSha1())) || (!file.exists())) {
                Downloader.downloadFromURL(artifact.getURL(), file);
            }
        } else {
            Action actionForCurrentOS = null;
            for (CompatibilityRule rule : rules) {
                if (rule.getAppliedAction() != null) {
                    actionForCurrentOS = rule.getAction();
                }
            }
            if (actionForCurrentOS == Action.ALLOW) {
                File file = new File(localClientDirectory, "libraries/" + artifact.getPath());
                path = file.getAbsolutePath();
                if ((file.exists() && !file.isDirectory() && !Downloader.checksumValid(file, artifact.getSha1())) || (!file.exists())) {
                    Downloader.downloadFromURL(artifact.getURL(), file);
                }
            }
        }
        return path;
    }

    /**
     *
     * @param localClientDirectory
     * @param clientVersion
     * @param natives
     * @param classifiers
     * @param extractRules
     */
    default void updateNatives(File localClientDirectory, String clientVersion, Map<OperatingSystem, String> natives, Map<String, DownloadInfo> classifiers, ExtractRules extractRules) {

        if (natives != null && natives.get(this.os) != null) {

            DownloadInfo nativeLibrary = classifiers.get(natives.get(this.os));
            File file = new File(localClientDirectory, "libraries/" + nativeLibrary.getPath());

            if ((file.exists() && !file.isDirectory() && !Downloader.checksumValid(file, nativeLibrary.getSha1())) || (!file.exists())) {
                Downloader.downloadFromURL(nativeLibrary.getURL(), file);
                Downloader.unpackNative(nativeLibrary, extractRules, localClientDirectory, clientVersion);
            }

            // TODO: Also check for update exactly native library (.dll etc.)
            // Possible solution: Basically official launcher unpack them every time on game starts
        }
    }

}
