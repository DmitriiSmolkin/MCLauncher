package biz.minecraft.launcher.entity.client.forge.library;

import biz.minecraft.launcher.entity.client.forge.ForgeLibrary;
import biz.minecraft.launcher.entity.client.minecraft.DownloadInfo;
import biz.minecraft.launcher.entity.client.minecraft.LibraryDownloadInfo;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Forge version JSON >= 25.0.9 (Minecraft 1.13.2).
 *
 * Key: "libraries"
 * Value parser to Java List of Objects ForgeLibrary.
 */
public class LatestForgeLibrariesDeserializer implements JsonDeserializer<List<ForgeLibrary>> {

    @Override
    public List<ForgeLibrary> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {

        List<ForgeLibrary> libraries = new ArrayList<>();
        JsonArray jsonArray = jsonElement.getAsJsonArray();

        for (JsonElement libraryElement : jsonArray) {

            JsonObject libraryObject = libraryElement.getAsJsonObject();

            LibraryDownloadInfo libraryDownloadInfo = this.getLibraryDownloadInfo(libraryObject.getAsJsonObject("downloads"));

            ForgeLibrary library = new LatestForgeLibrary();

            library.setName(libraryObject.get("name").getAsString());
            library.setDownloads(libraryDownloadInfo);

            libraries.add(library);
        }

        return libraries;
    }

    private LibraryDownloadInfo getLibraryDownloadInfo(JsonObject libraryDownloadsObject) {

        LibraryDownloadInfo libraryDownloadInfo = new LibraryDownloadInfo();

        libraryDownloadInfo.setArtifact(this.getDownloadInfo(libraryDownloadsObject.getAsJsonObject("artifact")));

        return libraryDownloadInfo;
    }

    private DownloadInfo getDownloadInfo(JsonObject downloadsArtifactObject) {

        DownloadInfo downloadInfo = new DownloadInfo();

        String path = downloadsArtifactObject.get("path").getAsString();
        String url = downloadsArtifactObject.get("url").getAsString();

        downloadInfo.setPath(path);
        downloadInfo.setUrl(this.getUrlFromString(url, path));
        downloadInfo.setSha1(downloadsArtifactObject.get("sha1").getAsString());
        downloadInfo.setSize(downloadsArtifactObject.get("size").getAsInt());

        return downloadInfo;
    }

    private URL getUrlFromString(String stringURL, String path) {

        if (stringURL.equals("") || stringURL == null) {
            stringURL = "https://cloud.minecraft.biz/libraries/" + path;
        }

        URL url = null;

        try {
            url = new URL(stringURL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

}
