package biz.minecraft.launcher.service;

import biz.minecraft.launcher.OperatingSystem;
import biz.minecraft.launcher.entity.client.minecraft.argument.Argument;
import biz.minecraft.launcher.entity.client.minecraft.argument.ArgumentType;
import biz.minecraft.launcher.entity.client.minecraft.compatibility.Action;
import biz.minecraft.launcher.entity.client.minecraft.compatibility.CompatibilityRule;
import biz.minecraft.launcher.task.queueable.*;
import biz.minecraft.launcher.entity.LauncherProfile;
import biz.minecraft.launcher.entity.Server;
import biz.minecraft.launcher.entity.client.Download;
import biz.minecraft.launcher.entity.client.Downloader;
import biz.minecraft.launcher.entity.client.forge.ForgeVersion;
import biz.minecraft.launcher.entity.client.minecraft.Library;
import biz.minecraft.launcher.entity.client.minecraft.MinecraftVersion;
import biz.minecraft.launcher.task.util.TaskManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GameService {

    private final static Logger LOGGER = LoggerFactory.getLogger(GameService.class);

    private Server server;

    private URL clientURL;
    private File clientDirectory;

    private MinecraftVersion minecraftVersion;
    private ForgeVersion forgeVersion;

    /**
     * Empty constructor.
     */
    public GameService() { }

    public void setServer(Server server) {
        this.server = server;
        this.clientURL = Downloader.url("https://cloud.minecraft.biz/clients/" + server.getName().toLowerCase());
        this.clientDirectory = new File(LauncherProfile.getWorkingDirectory(), server.getName().toLowerCase());
        this.minecraftVersion = MinecraftVersion.from(Downloader.url(clientURL + "/minecraft.json"));
        this.forgeVersion = ForgeVersion.from(Downloader.url(clientURL + "/forge.json"));
    }

    public void updateJre(TaskManager taskManager, JProgressBar progressBar, JLabel progressLabel) {
        taskManager.queueExecution(new UpdateJreTask(taskManager, progressBar, progressLabel));
    }

    public void updateClient(TaskManager taskManager, JProgressBar progressBar, JLabel label) {

        List<Download> downloads = new ArrayList<>();
        List<Library>  natives   = minecraftVersion.getLibrariesWithNatives(clientDirectory);

        downloads.add(minecraftVersion.getClient(clientDirectory));       // Minecraft.jar
        downloads.addAll(minecraftVersion.getLibraries(clientDirectory)); // Libraries (artifacts) to update
        downloads.addAll(minecraftVersion.getNatives(clientDirectory));   // Native libraries (artifacts)
        downloads.addAll(forgeVersion.getLibraries(clientDirectory));     // Libraries (artifacts) to update
        downloads.add(minecraftVersion.getAssets(clientDirectory));       //
        downloads.addAll(minecraftVersion.getExtraDownloads(clientDirectory, Downloader.url(clientURL + "/extra.json")));

        taskManager.queueExecution(new DownloadFilesTask(taskManager, downloads, label, progressBar));
        taskManager.queueExecution(new UnpackNativesTask(taskManager, natives, clientDirectory, minecraftVersion.getId(), label, progressBar));
        if (minecraftVersion.getAssets(clientDirectory) != null) {
            File assets = new File(minecraftVersion.getAssets(clientDirectory).getPath());
            if (assets.exists() && assets.isFile()) {
                taskManager.queueExecution(new UnpackAssetsTask(taskManager, assets, clientDirectory, label, progressBar));
            }
        }


//        DownloadFilesTask downloadFilesTask = ;
//        downloadFilesTask.addPropertyChangeListener(evt -> {
//            if ("state".equals(evt.getPropertyName())
//                    && (SwingWorker.StateValue.DONE.equals(evt.getNewValue()))) {
//                unpackNatives(natives, label, progressBar);
//                unpackAssets(label, progressBar);
//            }
//        });
//        downloadFilesTask.execute(); // It schedules SwingWorker for the execution on a worker thread and returns immediately.
    }

    /**
     * unpack + delete archive
     */
//    public void unpackNatives(List<Library> librariesWithNatives, JLabel label, JProgressBar progressBar) {
//        UnpackNativesTask unpackNativesTask = new UnpackNativesTask(librariesWithNatives, clientDirectory, minecraftVersion.getId(), label, progressBar);
//        unpackNativesTask.execute();
//    }
//
//    public void unpackAssets(JLabel label, JProgressBar progressBar) {
//        UnpackZipArchiveTask unpackZipArchiveTask = new UnpackZipArchiveTask(new File(minecraftVersion.getAssets(clientDirectory).getPath()), clientDirectory, label, progressBar);
//        unpackZipArchiveTask.execute();
//    }

    public void runGame() {

        MinecraftVersion minecraftVersion = MinecraftVersion.from(Downloader.url(clientURL + "/minecraft.json"));
        ForgeVersion     forgeVersion     = ForgeVersion.from(Downloader.url(clientURL + "/forge.json"));

        Map<String, String> replacements = new HashMap<>();

        replacements.put("auth_player_name", "White");
        replacements.put("version_name", "1.16.3-forge-34.1.1");
        replacements.put("game_directory", clientDirectory.getAbsolutePath());
        replacements.put("assets_root", clientDirectory.getAbsolutePath() + "/assets");
        replacements.put("assets_index_name", "1.16");
        replacements.put("auth_uuid", "a8ee6fc8736c4fe6a089cd7171533a4d");
        replacements.put("auth_access_token", "8ed56ba7c6ed4190915827bd8753e3f4");
        replacements.put("user_type", "mojang");
        replacements.put("version_type", "release");

        List<String> arguments = this.filterArguments(
                minecraftVersion.getClasspath(clientDirectory),
                forgeVersion.getClasspath(clientDirectory),
                minecraftVersion.getArguments(),
                forgeVersion.getArguments(),
                replacements
        );

//        filterMods(getMods((Download[])minecraftVersion.getExtraDownloads(clientDirectory, Downloader.url(clientURL + "/extra.json")).toArray()));

        runGame(arguments); // JAVA <JVM> <CP> <MAIN> <APP>
    }



    private List<Download> getMods(Download[] downloads) {

        List<Download> mods = new ArrayList<>();

        for (Download download : downloads) {
            if (download.getPath().startsWith("mods/")) mods.add(download);
        }

        return mods;
    }

    public void runGame(TaskManager taskManager, JProgressBar progressBar, JLabel progressLabel) {

        taskManager.queueExecution(new GameRunnerTask(this, taskManager, progressBar, progressLabel));

    }

    /**
     * wrapper
     *
     * @param taskManager
     * @param progressBar
     * @param progressLabel
     */
    public void filterMods(TaskManager taskManager, JProgressBar progressBar, JLabel progressLabel) {

        taskManager.queueExecution(new ModFilterTask(this, taskManager, progressBar, progressLabel));

    }

    public void filterMods() {

        List<Download> mods = getMods((Download[])minecraftVersion.getExtraDownloads(clientDirectory, Downloader.url(clientURL + "/extra.json")).toArray());

        IOFileFilter forbiddenModsFileFilter = new IOFileFilter() {
            @Override
            public boolean accept(File file) {
                if (!mods.contains(file)) {
                    return true;
                }
                return false;
            }

            @Override
            public boolean accept(File dir, String name) {
                File file = new File(dir, name);

                if (!mods.contains(file)) {
                    return true;
                }
                return false;
            }
        };

        // TODO: iterate directories as well
        Iterator<File> iterator = FileUtils.iterateFiles(new File(clientDirectory, "mods"), forbiddenModsFileFilter, null);

        while (iterator.hasNext()) {
            try {
                File forbiddenFile = iterator.next();
                FileUtils.forceDelete(forbiddenFile);
                LOGGER.debug("Deleted " + forbiddenFile);
            } catch (IOException e) {
                LOGGER.warn("Failed deleting forbidden mod.", e);
            }
        }
    }

    private void runGame(List<String> arguments) {

        ProcessBuilder pb = new ProcessBuilder(arguments);

        pb.directory(clientDirectory);
        pb.inheritIO();

        LOGGER.debug("Running Minecraft with the parameters:");
        for (String parameter : arguments) {
            LOGGER.debug(parameter);
        }

        Process process = null;

        try {
            process = pb.start();
            LOGGER.debug("Minecraft has been started successfully!");
        } catch (IOException e) {
            LOGGER.warn("Failed to start Minecraft process.", e);
        }
    }

    private List<String> filterArguments(
            List<String> minecraftClasspath,
            List<String> forgeClasspath,
            Map<ArgumentType, List<Argument>> minecraftArguments,
            Map<ArgumentType, List<Argument>> forgeArguments,
            Map<String, String> replacements) {

        List<String> arguments = new LinkedList<>();
        String pathSeparator = System.getProperties().getProperty("path.separator");

        List<String> classpath = new LinkedList<>();

        classpath.addAll(minecraftClasspath);
        classpath.addAll(forgeClasspath);

        String     libraries = classpath.stream().collect(Collectors.joining(pathSeparator));
        File         natives = new File(clientDirectory, "versions/" + minecraftVersion.getId() + "/natives/");

        List<String> jvmArguments = new LinkedList<>();
        List<String> gameArguments = new LinkedList<>();

        replacements.put("classpath", libraries + pathSeparator + new File(clientDirectory, "versions/" + minecraftVersion.getId() + "/minecraft.jar").getAbsolutePath());
        replacements.put("natives_directory", natives.getAbsolutePath());

        List<Argument> minecraftJvmArguments = minecraftArguments.get(ArgumentType.JVM);
        List<Argument> forgeJvmArguments = forgeArguments.get(ArgumentType.JVM);

        if (minecraftJvmArguments != null) jvmArguments.addAll(this.getArgumentsAsList(minecraftJvmArguments));
        if (forgeJvmArguments != null) jvmArguments.addAll(this.getArgumentsAsList(forgeJvmArguments));

        jvmArguments = this.replaceValues(jvmArguments, replacements);

        List<Argument> minecraftGameArguments = minecraftArguments.get(ArgumentType.GAME);
        List<Argument> forgeGameArguments = forgeArguments.get(ArgumentType.GAME);

        if (minecraftGameArguments != null) gameArguments.addAll(this.getArgumentsAsList(minecraftGameArguments));
        if (forgeGameArguments != null) gameArguments.addAll(this.getArgumentsAsList(forgeGameArguments));

        gameArguments = this.replaceValues(gameArguments, replacements);

        // JAVA
        //arguments.add(LauncherProfile.getJreDir());
        arguments.add(OperatingSystem.getCurrentPlatform().getJavaDir());
        // JVM OPTIONS
        arguments.addAll(jvmArguments);
        // MAIN CLASS
        //arguments.add(this.minecraftVersion.getMainClass());
        arguments.add(this.forgeVersion.getMainClass());
        // GAME
        arguments.addAll(gameArguments);

        return arguments;
    }

    /**
     * Get applied for current OS arguments from List<Argument> as a List<String>
     *
     * @param arguments
     * @return
     */
    private List<String> getArgumentsAsList(List<Argument> arguments) {

        List<String> argumentsList = new LinkedList<>();

        if (arguments == null) {
            return null;
        }

        for (Argument argument : arguments) {

            List<CompatibilityRule> rules = argument.getRules();

            if (rules == null) {
                for (String value : argument.getValues()) {
                    argumentsList.add(value);
                }
            } else {
                biz.minecraft.launcher.entity.client.minecraft.compatibility.Action actionForCurrentOS = null;
                for (CompatibilityRule rule : rules) {
                    if (rule.getAppliedAction() != null) {
                        actionForCurrentOS = rule.getAction();
                    }
                    if (rule.getFeatures() != null) {
                        actionForCurrentOS = biz.minecraft.launcher.entity.client.minecraft.compatibility.Action.DISALLOW; // Not supporting features currently
                    }
                }
                if (actionForCurrentOS == Action.ALLOW) {
                    for (String value : argument.getValues()) {
                        argumentsList.add(value);
                    }
                }
            }
        }

        return argumentsList;
    }

    // TODO USE SET INSTEAD OF LIST 🤔
    private List<String> replaceValues(List<String> arguments, Map<String, String> replacements) {

        Pattern pattern = Pattern.compile("^.*\\$\\{(.*)\\}.*$");

        for (int i = 0; i < arguments.size(); i++) {

            String argument = arguments.get(i);
            Matcher matcher = pattern.matcher(argument);

            if (matcher.find()) {
                if (replacements.containsKey(matcher.group(1))) {
                    argument = argument.replaceAll("\\$\\{.*?\\}", replacements.get(matcher.group(1)));
                }
            }

            arguments.set(i, argument);

        }

        return arguments;
    }

}
