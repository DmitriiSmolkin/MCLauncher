package biz.minecraft.launcher.entity.client.forge.version;

import biz.minecraft.launcher.entity.client.forge.ForgeLibrary;
import biz.minecraft.launcher.entity.client.forge.ForgeVersion;
import biz.minecraft.launcher.entity.client.minecraft.argument.Argument;
import biz.minecraft.launcher.entity.client.minecraft.argument.ArgumentType;

import java.util.List;
import java.util.Map;

/**
 * Java Object representation of the Forge version > 25.0.9 (Minecraft 1.13.2).
 *
 * Notes: The definition of the version format is outside this class.
 *        ForgeLibrary is an interface and needs custom type adapter for the specific version implementation.
 */
public class LatestForgeVersion implements ForgeVersion {

    private String inheritsFrom;
    private String mainClass;
    private List<ForgeLibrary> libraries;
    private Map<ArgumentType, List<Argument>> arguments;

    public LatestForgeVersion() {
        // An empty constructor is required for the Gson.
    }

    @Override
    public String getInheritsFrom() {
        return this.inheritsFrom;
    }

    @Override
    public String getMainClass() {
        return this.mainClass;
    }

    @Override
    public List<ForgeLibrary> getLibraries() {
        return this.libraries;
    }

    @Override
    public Map<ArgumentType, List<Argument>> getArguments() {
        return this.arguments;
    }

}
