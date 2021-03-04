package biz.minecraft.launcher.task.queueable;

import biz.minecraft.launcher.OperatingSystem;
import biz.minecraft.launcher.entity.LauncherProfile;
import biz.minecraft.launcher.entity.client.Downloader;
import biz.minecraft.launcher.task.util.QueueableProgressTask;
import biz.minecraft.launcher.task.util.TaskManager;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;

public class UpdateJreTask extends QueueableProgressTask {

    private final Logger LOGGER = LoggerFactory.getLogger(UpdateJreTask.class);

    public UpdateJreTask(TaskManager taskManager, JProgressBar progressBar, JLabel progressLabel) {
        super(taskManager, progressBar, progressLabel);
    }

    @Override
    protected String doInBackground()
    {
        updateJre();
        // publish -> process
        return null;
    }

    // TODO test process

    @Override
    protected void result()
    {
        //String message = name + " has finished!";
        progressLabel.setText("Done");
        LOGGER.debug("Jre Update has finished!");
    }

    private void updateJre() {
        String arch = "x86";
        if (System.getProperty("os.arch").contains("64")) arch = "x64";
        File jreFolder = new File(LauncherProfile.getWorkingDirectory(), "runtime/jre");
        if (!jreFolder.exists()) {
            OperatingSystem os = OperatingSystem.getCurrentPlatform();
            URL url = Downloader.url("https://cloud.minecraft.biz/jre/" + os.getName() + "/" + arch + "/bundle.zip");
            File archive = new File(LauncherProfile.getWorkingDirectory(), "runtime/bundle.zip");

            Downloader.downloadFromURL(url, archive); // monitor process somehow

            File destination = new File(LauncherProfile.getWorkingDirectory(), "runtime/");
            Downloader.unpackZipArchive(archive, destination);

            if (OperatingSystem.getCurrentPlatform() == OperatingSystem.OSX) {
                File java = new File(jreFolder, "Contents/Home/bin/java");
                try {
                    Files.setPosixFilePermissions(Paths.get(java.getAbsolutePath()), PosixFilePermissions.fromString("rwxr-xr-x"));
                } catch (IOException e) {
                    LOGGER.warn("Failed to set custom java executable rule.", e);
                }
            }

            boolean success = FileUtils.deleteQuietly(archive);

            if (success) {
                LOGGER.debug("'" + archive.getName() + "' deleted successfully.");
            } else {
                LOGGER.debug("Filed to delete '" + archive.getName() + "'.");
            }
        }
    }

}
