package biz.minecraft.launcher.entity.client.forge.library;

import biz.minecraft.launcher.entity.client.forge.ForgeLibrary;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Forge version JSON < 25.0.9 (Minecraft 1.13.2).
 *
 * Key: "libraries"
 * Value parser to Java List of Objects ForgeLibrary.
 */
public class PreviousForgeLibrariesDeserializer implements JsonDeserializer<List<ForgeLibrary>> {

//    "name": "com.typesafe.akka:akka-actor_2.11:2.3.3",
//    "url": "https://cloud.minecraft.biz/libraries/",
//    "checksums": [
//      "ed62e9fc709ca0f2ff1a3220daa8b70a2870078e",
//      "25a86ccfdb6f6dfe08971f4825d0a01be83a6f2e"
//    ],
//    "serverreq": true,
//    "clientreq": true

// Important: May only contain the "name" key.

    @Override
    public List<ForgeLibrary> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {

        List<ForgeLibrary> libraries = new ArrayList<>();
        JsonArray jsonArray = jsonElement.getAsJsonArray();

        for (JsonElement libraryElement : jsonArray) {

            JsonObject libraryObject = libraryElement.getAsJsonObject();

            PreviousForgeLibrary previousForgeLibrary = this.getPreviousForgeLibrary(libraryObject);
            ForgeLibrary library = new PreviousForgeLibrary(previousForgeLibrary);

            library.setName(previousForgeLibrary.getName());
            library.setDownloads(previousForgeLibrary.getDownloads());

            libraries.add(library);
        }

        return libraries;
    }

    /**
     * Get Java Object representation of the Forge's version JSON < 25.0.9 (Minecraft 1.13.2) library.
     *
     * @param libraryObject
     * @return PreviousForgeLibrary.
     */
    private PreviousForgeLibrary getPreviousForgeLibrary(JsonObject libraryObject) {

        PreviousForgeLibrary library = new PreviousForgeLibrary();

        if (libraryObject.has("name")) {
            library.setName(libraryObject.get("name").getAsString());
        }

        if (libraryObject.has("url")) {
            library.setUrl(libraryObject.get("url").getAsString());
        }

        if (libraryObject.has("checksums")) {
            List<String> checksums = new ArrayList<>();
            for (JsonElement checksumElement : libraryObject.get("checksums").getAsJsonArray()) {
                checksums.add(checksumElement.getAsString());
            }
            library.setChecksums(checksums);
        }

        if (libraryObject.has("serverreq")) {
            library.setServerreq(libraryObject.get("serverreq").getAsBoolean());
        }

        if (libraryObject.has("clientreq")) {
            library.setClientreq(libraryObject.get("clientreq").getAsBoolean());
        }

        return library;
    }


}
