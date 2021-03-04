package biz.minecraft.launcher.service;

import biz.minecraft.launcher.Main;
import biz.minecraft.launcher.OperatingSystem;
import biz.minecraft.launcher.entity.LauncherProfile;
import biz.minecraft.launcher.entity.LauncherVersion;
import biz.minecraft.launcher.entity.NewsItem;
import biz.minecraft.launcher.entity.Server;
import biz.minecraft.launcher.entity.client.Downloader;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class LauncherService {

    private final static Logger LOGGER = LoggerFactory.getLogger(LauncherService.class);

    private Gson gson;

    public LauncherService() {
        gson = new GsonBuilder().setDateFormat("dd-MM-yyyy HH:mm").create();
    }

    /**
     * Get a list of news item objects from the API.
     *
     * @return List of news item objects or empty list on error (Malformed URL or IOException).
     */
    public List<NewsItem> getNewsList() {

        List<NewsItem> newsList = new LinkedList<>();

        try (InputStreamReader reader = new InputStreamReader(new URL("https://cloud.minecraft.biz/news.json").openStream())) {
            newsList = Arrays.asList(gson.fromJson(reader, NewsItem[].class));
        } catch (MalformedURLException e) {
            LOGGER.warn("Malformed news URL.", e);
        } catch (IOException e) {
            LOGGER.warn("Failed to get news from the API.", e);
        }

        return newsList;
    }

    /**
     * Get a list of server objects from the API.
     *
     * @return List of server objects or empty list on error (Malformed URL or IOException).
     */
    public List<Server> getServerList() {

        List<Server> serverList = new LinkedList<>();

        try (InputStreamReader reader = new InputStreamReader(new URL("https://cloud.minecraft.biz/servers.json").openStream())) {
            serverList = Arrays.asList(gson.fromJson(reader, Server[].class));
        } catch (MalformedURLException e) {
            LOGGER.warn("Malformed server list URL.", e);
        } catch (IOException e) {
            LOGGER.warn("Failed to get server list from the API.", e);
        }

        return serverList;
    }



    /**
     * Launcher-Updater Thread.
     *
     * If the Launcher is outdated (compare current version with https://cloud.minecraft.biz/launcher/version.json)
     *
     * - Download new version into temp directory
     * - Start new version passing path of old version as a command-line argument
     * - Die
     *
     * If the Launcher is not outdated and one command-line argument handled
     *
     * - Check if process has been started with one command-line argument (old versions path expected as a 0-index array element)
     * - Delete old version
     * - Copy the new version to replace the old one
     * - Delete temp directory
     * - Start new version from old place
     * - Die
     *
     * @param args Application arguments as List.
     */
    public void updateLauncher(List<String> args) {

        LauncherVersion launcherVersion = LauncherVersion.from("https://cloud.minecraft.biz/launcher/version.json");
        String currentVersion = Main.version;
        String latestVersion = launcherVersion.getVersion();
        File tempLauncherFile = new File(LauncherProfile.getWorkingDirectory(), "Launcher.exe");

        if (!currentVersion.equals(latestVersion)) {

            // Launcher is outdated

            LOGGER.debug("Launcher version is outdated. Preparing for update.");

            // Downloading new version to the temp folder

            while (true) {
                try {
                    FileUtils.copyURLToFile(launcherVersion.getUrl(), tempLauncherFile);
                    LOGGER.debug("Downloaded '" + launcherVersion.getUrl() + " -> " + tempLauncherFile);
                    break;
                } catch (IOException e) {
                    LOGGER.warn("Download failed " + launcherVersion.getUrl() + " -> " + tempLauncherFile, e);
                    int userChoice = JOptionPane.showConfirmDialog(null, "Ошибка загрузки новой версии лаунчера, повторить?", "Ошибка", JOptionPane.YES_NO_OPTION);

                    if (userChoice == 0) {
                        continue;
                    } else {
                        System.exit(0);
                    }
                }
            }

            // Starting new version passing old version's path as a command-line argument

            String oldLauncherPath = null;

            try {
                oldLauncherPath = Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            } catch (URISyntaxException e) {
                LOGGER.warn("Launcher path URL is not formatted to be converted to a URI.", e);
                JOptionPane.showConfirmDialog(null, "Ошибка определения пути лаунчера, пожалуйста обратитесь к администратору.", "Ошибка", JOptionPane.OK_CANCEL_OPTION);
                System.exit(0);
            }

            ArrayList<String> params = new ArrayList<>();

            params.add(OperatingSystem.getCurrentPlatform().getJavaDir()); // Java path (string)
            params.add("-jar");
            params.add(tempLauncherFile.toString()); // Start new launcher from the temp folder
            params.add(oldLauncherPath); // Pass old launcher's path as a command-line argument

            ProcessBuilder pb = new ProcessBuilder(params);

            Process process = null;

            try {
                process = pb.start();
            } catch (IOException e) {
                LOGGER.warn("Error starting new launcher from the temp folder.", e);
                JOptionPane.showConfirmDialog(null, "Ошибка запуска новой версии лаунчера из временной папки, пожалуйста обратитесь к администратору.", "Ошибка", JOptionPane.OK_CANCEL_OPTION);
                System.exit(0);
            }

            // Die

            LOGGER.debug("New version successfully started from temp folder. Dying.");

            System.exit(0);

        } else if (args.size() == 1) {

            // Launcher is not outdated and one command-line argument handled

            File oldLauncherPath = null;

            try {
                oldLauncherPath = new File(args.get(0));
            } catch (Exception e) {
                LOGGER.warn("Failed to parse old version launcher path from command-line argument.", e);
                JOptionPane.showConfirmDialog(null, "Ошибка запуска новой версии лаунчера из временной папки, пожалуйста обратитесь к администратору.", "Ошибка", JOptionPane.OK_CANCEL_OPTION);
                System.exit(0);
            }

            // Delete old version

            try {
                FileUtils.forceDelete(oldLauncherPath);
                LOGGER.debug("Deleted " + oldLauncherPath);
            } catch (IOException e) {
                LOGGER.warn("Failed to delete " + oldLauncherPath, e);
                JOptionPane.showConfirmDialog(null, "Не удалось удалить старую вресию лаунчера, пожалуйста обратитесь к администратору.", "Ошибка", JOptionPane.OK_CANCEL_OPTION);
                System.exit(0);
            }

            // Copy the new version to replace the old one

            try {
                FileUtils.copyFile(tempLauncherFile, oldLauncherPath);
                LOGGER.debug("Copied " + tempLauncherFile + " -> " + oldLauncherPath);
            } catch (IOException e) {
                LOGGER.warn("Failed to copy " + tempLauncherFile + " -> " + oldLauncherPath, e);
                JOptionPane.showConfirmDialog(null, "Не удалось скопировать новую версию лаунчера на старое место, пожалуйста обратитесь к администратору.", "Ошибка", JOptionPane.OK_CANCEL_OPTION);
                System.exit(0);
            }

            // Delete temp directory

            try {
                FileUtils.forceDelete(tempLauncherFile);
                LOGGER.debug("Deleted " + tempLauncherFile);
            } catch (IOException e) {
                LOGGER.warn("Failed to delete new launcher version from temp folder: " + tempLauncherFile, e);
            }

            // Start new version from old place

            ArrayList<String> params = new ArrayList<>();

            params.add(OperatingSystem.getCurrentPlatform().getJavaDir()); // Java path (string)
            params.add("-jar");
            params.add(oldLauncherPath.getPath()); // Start new launcher from old place

            ProcessBuilder pb = new ProcessBuilder(params);

            Process process = null;

            try {
                process = pb.start();
            } catch (IOException e) {
                LOGGER.warn("Error starting new launcher from old place.", e);
                JOptionPane.showConfirmDialog(null, "Ошибка запуска новой версии лаунчера на старом месте, пожалуйста обратитесь к администратору.", "Ошибка", JOptionPane.OK_CANCEL_OPTION);
                System.exit(0);
            }

            // Die

            LOGGER.debug("New version successfully started from old version's folder. Dying.");

            System.exit(0);

        }

    }

}
