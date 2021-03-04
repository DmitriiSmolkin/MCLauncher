package biz.minecraft.launcher.entity.client.forge;

import biz.minecraft.launcher.OperatingSystem;
import biz.minecraft.launcher.entity.client.Download;
import biz.minecraft.launcher.entity.client.Downloader;
import biz.minecraft.launcher.entity.client.forge.library.LatestForgeLibrariesDeserializer;
import biz.minecraft.launcher.entity.client.forge.library.PreviousForgeLibrariesDeserializer;
import biz.minecraft.launcher.entity.client.forge.version.LatestForgeVersion;
import biz.minecraft.launcher.entity.client.forge.version.PreviousForgeVersion;
import biz.minecraft.launcher.entity.client.minecraft.DownloadInfo;
import biz.minecraft.launcher.entity.client.minecraft.ExtractRules;
import biz.minecraft.launcher.entity.client.minecraft.Library;
import biz.minecraft.launcher.entity.client.minecraft.argument.Argument;
import biz.minecraft.launcher.entity.client.minecraft.argument.ArgumentType;
import biz.minecraft.launcher.entity.client.minecraft.argument.ArgumentsDeserializer;
import biz.minecraft.launcher.entity.client.minecraft.argument.ArgumentsFromStringDeserializer;
import biz.minecraft.launcher.entity.client.minecraft.compatibility.Action;
import biz.minecraft.launcher.entity.client.minecraft.compatibility.CompatibilityRule;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * High-level universal Forge Version Convention.
 */
public interface ForgeVersion {

    String getInheritsFrom();

    String getMainClass();

    List<ForgeLibrary> getLibraries();

    Map<ArgumentType, List<Argument>> getArguments();

    static ForgeVersion from(URL url) {

        ForgeVersion forgeVersion = null;

        try (InputStream is = url.openStream()) {

            String json = IOUtils.toString(is, StandardCharsets.UTF_8);

            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            Set<Map.Entry<String, JsonElement>> entries = jsonObject.entrySet();

            for (Map.Entry<String, JsonElement> entry : entries) {
                if (entry.getKey().equals("arguments")) {
                    // Forge version >= 25.0.9 (Minecraft 1.13.2)
                    GsonBuilder gsonBuilder = new GsonBuilder();

                    Type librariesType = new TypeToken<List<ForgeLibrary>>(){}.getType();
                    Type argumentsType = new TypeToken<Map<ArgumentType, List<Argument>>>(){}.getType();

                    gsonBuilder.registerTypeAdapter(librariesType, new LatestForgeLibrariesDeserializer());
                    gsonBuilder.registerTypeAdapter(argumentsType, new ArgumentsDeserializer());

                    Gson gs = gsonBuilder.create();

                    forgeVersion = gs.fromJson(json, LatestForgeVersion.class);
                } else if (entry.getKey().equals("minecraftArguments")) {
                    // Forge version < 25.0.9 (Minecraft 1.13.2)
                    GsonBuilder gsonBuilder = new GsonBuilder();

                    Type librariesType = new TypeToken<List<ForgeLibrary>>(){}.getType();
                    Type argumentsType = new TypeToken<Map<ArgumentType, List<Argument>>>(){}.getType();

                    gsonBuilder.registerTypeAdapter(librariesType, new PreviousForgeLibrariesDeserializer());
                    gsonBuilder.registerTypeAdapter(argumentsType, new ArgumentsFromStringDeserializer());

                    Gson gs = gsonBuilder.create();

                    forgeVersion = gs.fromJson(json, PreviousForgeVersion.class);
                }
            }

            if (forgeVersion == null) {
                throw new RuntimeException("Invalid Forge Version format.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return forgeVersion;
    }

    /**
     * Get allowed for the current OS libraries classpath.
     *
     * @param clientDirectory Absolute path to client's directory, i.e. '/../../Minecraft.biz/actual/'.
     * @return List of the absolute paths to the required for the current OS libraries.
     */
    default List<String> getClasspath(File clientDirectory) {

        List<String> classpath = new ArrayList<>();

        for (ForgeLibrary library : getLibraries()) {
            DownloadInfo artifact = library.getDownloads().getArtifact();

            // Note: Some Forge Libraries does not have checksums, that's why every single game launch they will be updated

            if (artifact == null) continue;

            File file = new File(clientDirectory, "libraries/" + artifact.getPath());
            String path = file.getAbsolutePath();

            if (path != null && !path.equals("")) classpath.add(path);
        }

        return classpath;
    }
    // get system required libraries

    default List<Download> getLibraries(File clientDirectory) {
        List<Download> artifacts = new ArrayList<>();

        for (ForgeLibrary library : getLibraries()) {
            DownloadInfo artifact = library.getDownloads().getArtifact();

            if (artifact == null) continue;

            File file = new File(clientDirectory, "libraries/" + artifact.getPath());
            String path = file.getAbsolutePath();
            artifact.setPath(path);

            if ((file.exists() && !file.isDirectory() && !Downloader.checksumValid(file, artifact.getSha1())) || (!file.exists())) {
                artifacts.add(artifact);
            }
        }

        return artifacts;
    }

    default List<String> updateLibraries(File localClientDirectory) {
        List<String> classpath = new LinkedList<>();
        List<ForgeLibrary> libraries = this.getLibraries();

        for (ForgeLibrary library : libraries) {
            DownloadInfo artifact = library.getDownloads().getArtifact();

            String path = this.updateArtifact(localClientDirectory, artifact);

            if (path != null && !path.equals("")) classpath.add(path);
        }

        return classpath;
    }

    default String updateArtifact(File localClientDirectory, DownloadInfo artifact) {

        // Note: Some Forge Libraries does not have checksums, that's why every single game launch they will be updated

        if (artifact == null) return null;

        File file = new File(localClientDirectory, "libraries/" + artifact.getPath());
        String path = file.getAbsolutePath();

        if ((file.exists() && !file.isDirectory() && !Downloader.checksumValid(file, artifact.getSha1())) || (!file.exists())) {
            Downloader.downloadFromURL(artifact.getURL(), file);
        }

        return path;
    }

}
