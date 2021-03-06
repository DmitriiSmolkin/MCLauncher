package biz.minecraft.launcher.entity.client.forge;

import com.google.gson.*;

import java.io.File;
import java.lang.reflect.Type;

public class ForgeArtifact {

    // Descriptor parts: group:name:version[:classifier][@extension]

    private String domain;
    private String name;
    private String version;
    private String classifier = null;
    private String ext = "jar";

    // Caches so we don't rebuild every time we're asked.

    private String path;
    private String file;
    private String descriptor;

    public static ForgeArtifact from(String descriptor)
    {
        ForgeArtifact ret = new ForgeArtifact();
        ret.descriptor = descriptor;

        String[] pts = descriptor.split(":");
        ret.domain = pts[0];
        ret.name = pts[1];

        int last = pts.length - 1;
        int idx = pts[last].indexOf('@');
        if (idx != -1) {
            ret.ext = pts[last].substring(idx + 1);
            pts[last] = pts[last].substring(0, idx);
        }

        ret.version = pts[2];
        if (pts.length > 3)
            ret.classifier = pts[3];

        ret.file = ret.name + '-' + ret.version;
        if (ret.classifier != null) ret.file += '-' + ret.classifier;
        ret.file += '.' + ret.ext;

        ret.path = ret.domain.replace('.', '/') + '/' + ret.name + '/' + ret.version + '/' + ret.file;

        return ret;
    }

    public File getLocalPath(File base) {
        return new File(base, path.replace('/', File.separatorChar));
    }

    public String getDescriptor(){ return descriptor; }
    public String getPath()      { return path;       }
    public void setPathParent(String parent) { this.path = parent + this.path;}
    public String getDomain()    { return domain;     }
    public String getName()      { return name;       }
    public String getVersion()   { return version;    }
    public String getClassifier(){ return classifier; }
    public String getExt()       { return ext;        }
    public String getFilename()  { return file;       }
    @Override
    public String toString() {
        return getDescriptor();
    }

    public static class Adapter implements JsonDeserializer<ForgeArtifact>, JsonSerializer<ForgeArtifact> {
        @Override
        public JsonElement serialize(ForgeArtifact src, Type typeOfSrc, JsonSerializationContext context) {
            return src == null ? JsonNull.INSTANCE : new JsonPrimitive(src.getDescriptor());
        }

        @Override
        public ForgeArtifact deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return json.isJsonPrimitive() ? ForgeArtifact.from(json.getAsJsonPrimitive().getAsString()) : null;
        }
    }
}
