package biz.minecraft.launcher.entity.client;

import biz.minecraft.launcher.Main;
import biz.minecraft.launcher.entity.LauncherProfile;
import biz.minecraft.launcher.entity.Server;
import biz.minecraft.launcher.entity.client.forge.ForgeVersion;
import biz.minecraft.launcher.entity.client.minecraft.*;
import biz.minecraft.launcher.entity.client.minecraft.argument.Argument;
import biz.minecraft.launcher.entity.client.minecraft.argument.ArgumentType;
import biz.minecraft.launcher.entity.client.minecraft.compatibility.Action;
import biz.minecraft.launcher.entity.client.minecraft.compatibility.CompatibilityRule;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * COMPLETE CLIENT OBJECT BOTH MINECRAFT + FORGE BASED ON JSONS
 */
public class ClientProfile extends Downloader {
//
//    private File localClientDirectory;
//
//    private String name;
//    private URL remoteClientRepository;
//
//    private MinecraftVersion minecraftVersion;
//    private ForgeVersion forgeVersion;
//    private List<DownloadInfo> extras;
//
//    private String clientVersion;
//    private Server server;
//    private String clientName;
//    private String remoteClientDirectory;
//
//    /**
//     * Constructor based on a server name gets the required client information.
//     *
//     * @param server Server representation object.
//     */
//    public ClientProfile(Server server) {
//
//        this.server     = server;
//        this.clientName = this.server.getName().toLowerCase();
//        this.clientVersion = this.server.getClientVersion();
//
//        this.remoteClientDirectory = "https://cloud.minecraft.biz/clients/" + this.clientName + "/";
//        this.remoteClientRepository = this.url(this.remoteClientDirectory);
//        this.localClientDirectory  = new File(LauncherProfile.getWorkingDirectory(), this.clientName);
//
//        // GET MINECRAFT, FORGE VERSIONS & EXTRA FILES FROM API
//
//        this.minecraftVersion = MinecraftVersion.from(url(this.remoteClientDirectory + "minecraft.json"));
//        this.forgeVersion     = ForgeVersion.from(url(this.remoteClientDirectory + "forge.json"));
//
//        List<String> minecraftClasspath = minecraftVersion.updateLibraries(this.localClientDirectory, this.clientVersion);
//        List<String> forgeClasspath = forgeVersion.updateLibraries(this.localClientDirectory);
//
//        minecraftVersion.updateClient(this.localClientDirectory, this.clientVersion);
//
//        this.updateAssets(this.localClientDirectory, this.minecraftVersion.getAssets());
//
//        Download[] extraDownloads = this.updateExtraDownloads(this.remoteClientRepository, this.localClientDirectory);
//
//        this.updateJre();
//
//        // ARGUMENTS
//
//        Map<String, String> replacements = new HashMap<>();
//
//        replacements.put("auth_player_name", "White");
//        replacements.put("version_name", "1.16.3");
//        replacements.put("game_directory", this.localClientDirectory.getAbsolutePath());
//        replacements.put("assets_root", this.localClientDirectory.getAbsolutePath() + "/assets");
//        replacements.put("assets_index_name", "1.16");
//        replacements.put("auth_uuid", "a8ee6fc8736c4fe6a089cd7171533a4d");
//        replacements.put("auth_access_token", "8ed56ba7c6ed4190915827bd8753e3f4");
//        replacements.put("user_type", "mojang");
//        replacements.put("version_type", "Forge");
//
//        List<String> arguments = this.filterArguments(
//                minecraftClasspath,
//                forgeClasspath,
//                minecraftVersion.getArguments(),
//                forgeVersion.getArguments(),
//                replacements
//        );
//
//        this.filterMods(this.getMods(extraDownloads));
//
//        this.runGame(arguments); // JAVA <JVM> <CP> <MAIN> <APP>
//
//    }
//
//    private List<Download> getMods(Download[] downloads) {
//
//        List<Download> mods = new ArrayList<>();
//
//        for (Download download : downloads) {
//            if (download.getPath().startsWith("mods/")) mods.add(download);
//        }
//
//        return mods;
//    }
//
//    private void filterMods(List<Download> mods) {
//        IOFileFilter forbiddenModsFileFilter = new IOFileFilter() {
//            @Override
//            public boolean accept(File file) {
//                if (!mods.contains(file)) {
//                    return true;
//                }
//                return false;
//            }
//
//            @Override
//            public boolean accept(File dir, String name) {
//                File file = new File(dir, name);
//
//                if (!mods.contains(file)) {
//                    return true;
//                }
//                return false;
//            }
//        };
//
//        // TODO: iterate directories as well
//        Iterator<File> iterator = FileUtils.iterateFiles(new File(this.localClientDirectory, "mods"), forbiddenModsFileFilter, null);
//
//        while (iterator.hasNext()) {
//            try {
//                File forbiddenFile = iterator.next();
//                FileUtils.forceDelete(forbiddenFile);
//                LOGGER.debug("Deleted " + forbiddenFile);
//            } catch (IOException e) {
//                LOGGER.warn("Failed deleting forbidden mod.", e);
//            }
//        }
//    }
//
//    private void runGame(List<String> arguments) {
//
//        ProcessBuilder pb = new ProcessBuilder(arguments);
//
//        pb.directory(this.localClientDirectory);
//        pb.inheritIO();
//
//        LOGGER.debug("Running Minecraft with the parameters:");
//        for (String parameter : arguments) {
//            LOGGER.debug(parameter);
//        }
//
//        Process process = null;
//
//        try {
//            process = pb.start();
//            LOGGER.debug("Minecraft has been started successfully!");
//        } catch (IOException e) {
//            LOGGER.warn("Failed to start Minecraft process.", e);
//        }
//    }
//
//    private List<String> filterArguments(List<String> minecraftClasspath, List<String> forgeClasspath, Map<ArgumentType, List<Argument>> minecraftArguments, Map<ArgumentType, List<Argument>> forgeArguments, Map<String, String> replacements) {
//
//        List<String> arguments = new LinkedList<>();
//        String pathSeparator = System.getProperties().getProperty("path.separator");
//
//        List<String> classpath = new LinkedList<>();
//
//        classpath.addAll(minecraftClasspath);
//        classpath.addAll(forgeClasspath);
//
//        String     libraries = classpath.stream().collect(Collectors.joining(pathSeparator));
//        File         natives = new File(this.localClientDirectory, "versions/" + this.clientVersion + "/natives/");
//
//        List<String> jvmArguments = new LinkedList<>();
//        List<String> gameArguments = new LinkedList<>();
//
//        replacements.put("classpath", libraries + pathSeparator + new File(this.localClientDirectory, "versions/" + this.clientVersion + "/minecraft.jar").getAbsolutePath());
//        replacements.put("natives_directory", natives.getAbsolutePath());
//
//        List<Argument> minecraftJvmArguments = minecraftArguments.get(ArgumentType.JVM);
//        List<Argument> forgeJvmArguments = forgeArguments.get(ArgumentType.JVM);
//
//        if (minecraftJvmArguments != null) jvmArguments.addAll(this.getArgumentsAsList(minecraftJvmArguments));
//        if (forgeJvmArguments != null) jvmArguments.addAll(this.getArgumentsAsList(forgeJvmArguments));
//
//        jvmArguments = this.replaceValues(jvmArguments, replacements);
//
//        List<Argument> minecraftGameArguments = minecraftArguments.get(ArgumentType.GAME);
//        List<Argument> forgeGameArguments = forgeArguments.get(ArgumentType.GAME);
//
//        if (minecraftGameArguments != null) gameArguments.addAll(this.getArgumentsAsList(minecraftGameArguments));
//        if (forgeGameArguments != null) gameArguments.addAll(this.getArgumentsAsList(forgeGameArguments));
//
//        gameArguments = this.replaceValues(gameArguments, replacements);
//
//        // JAVA
//        arguments.add(LauncherProfile.getJreDir());
//        // JVM OPTIONS
//        arguments.addAll(jvmArguments);
//        // MAIN CLASS
//        arguments.add(this.minecraftVersion.getMainClass());
//        // GAME
//        arguments.addAll(gameArguments);
//
//        return arguments;
//    }
//
//    /**
//     * Get applied for current OS arguments from List<Argument> as a List<String>
//     *
//     * @param arguments
//     * @return
//     */
//    private List<String> getArgumentsAsList(List<Argument> arguments) {
//
//        List<String> argumentsList = new LinkedList<>();
//
//        if (arguments == null) {
//            return null;
//        }
//
//        for (Argument argument : arguments) {
//
//            List<CompatibilityRule> rules = argument.getRules();
//
//            if (rules == null) {
//                for (String value : argument.getValues()) {
//                    argumentsList.add(value);
//                }
//            } else {
//                Action actionForCurrentOS = null;
//                for (CompatibilityRule rule : rules) {
//                    if (rule.getAppliedAction() != null) {
//                        actionForCurrentOS = rule.getAction();
//                    }
//                    if (rule.getFeatures() != null) {
//                        actionForCurrentOS = Action.DISALLOW; // Not supporting features currently
//                    }
//                }
//                if (actionForCurrentOS == Action.ALLOW) {
//                    for (String value : argument.getValues()) {
//                        argumentsList.add(value);
//                    }
//                }
//            }
//        }
//
//        return argumentsList;
//    }
//
//    // TODO USE SET INSTEAD OF LIST 🤔
//    private List<String> replaceValues(List<String> arguments, Map<String, String> replacements) {
//
//        Pattern pattern = Pattern.compile("^.*\\$\\{(.*)\\}.*$");
//
//        for (int i = 0; i < arguments.size(); i++) {
//
//            String argument = arguments.get(i);
//            Matcher matcher = pattern.matcher(argument);
//
//            if (matcher.find()) {
//                if (replacements.containsKey(matcher.group(1))) {
//                    argument = argument.replaceAll("\\$\\{.*?\\}", replacements.get(matcher.group(1)));
//                }
//            }
//
//            arguments.set(i, argument);
//
//        }
//
//        return arguments;
//    }



}
