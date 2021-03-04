package biz.minecraft.launcher.task.queueable;

import biz.minecraft.launcher.OperatingSystem;
import biz.minecraft.launcher.entity.client.minecraft.DownloadInfo;
import biz.minecraft.launcher.entity.client.minecraft.ExtractRules;
import biz.minecraft.launcher.entity.client.minecraft.Library;
import biz.minecraft.launcher.task.util.QueueableProgressTask;
import biz.minecraft.launcher.task.util.TaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class UnpackNativesTask extends QueueableProgressTask {

    private final static Logger LOGGER = LoggerFactory.getLogger(UnpackNativesTask.class);

    private final List<Library> libraries;
    private final File clientDirectory;
    private final String clientVersion;

    /**
     * Constructor.
     *
     * @param libraries List of libraries that contain native libraries required by the operating system.
     * @param progressLabel JLabel for JProgressBar.
     * @param progressBar JProgressBar.
     */
    public UnpackNativesTask(TaskManager taskManager, List<Library> libraries, File clientDirectory, String clientVersion, JLabel progressLabel, JProgressBar progressBar) {
        super(taskManager, progressBar, progressLabel);
        this.libraries = libraries;
        this.clientDirectory = clientDirectory;
        this.clientVersion = clientVersion;
    }

    @Override
    protected String doInBackground() {

        int maxNumber = libraries.size();

        for (int i = 0; i < maxNumber; i++) {
            Library library = libraries.get(i);
            Map<OperatingSystem, String> natives = library.getNatives();
            Map<String, DownloadInfo> classifiers = library.getDownloads().getClassifiers();
            ExtractRules extractRules = library.getExtract();
            OperatingSystem os = OperatingSystem.getCurrentPlatform();
            if (natives != null && natives.get(os) != null) {
                DownloadInfo nativeLibrary = classifiers.get(natives.get(os));
                unpackNative(nativeLibrary, extractRules, clientDirectory, clientVersion);
            }

            double factor = ((double)(i+1) / maxNumber);
            publish(factor); // publish -> process
        }
        return "Нативные библиотеки распакованы!";
    }

    @Override
    protected void process( List<Double> aDoubles ) {
        int amount = progressBar.getMaximum() - progressBar.getMinimum();
        progressBar.setValue( ( int ) (progressBar.getMinimum() + ( amount * aDoubles.get( aDoubles.size() - 1 ))) );
    }

    @Override
    protected void result() {
        try {
            progressLabel.setText(get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    /**
     * Unpack native archive to destination folder.
     *
     * @param library Native library's path, sha1, size, url.
     * @param extractRules List of excludes when unpacking.
     * @param clientDirectory Target folder for unpacking.
     */
    public static void unpackNative(DownloadInfo library, ExtractRules extractRules, File clientDirectory, String clientVersion) {

        File artifact = new File(library.getPath());
        File destination = new File(clientDirectory, "versions/" + clientVersion + "/natives/");

        try (ZipFile zf = new ZipFile(artifact)) {

            Enumeration<? extends ZipEntry> entries = zf.entries();

            LOGGER.debug("Unpacking '" + library.getPath() + "' to '" + destination.toString() + "'");

            while (entries.hasMoreElements()) {

                ZipEntry entry = entries.nextElement();

                if (extractRules != null && !extractRules.shouldExtract(entry.getName())) {
                    continue;
                }

                File targetFile = new File(destination, entry.getName());

                if (targetFile.getParentFile() != null) {
                    targetFile.getParentFile().mkdirs();
                }

                if (entry.isDirectory()) {
                    continue;
                }

                try (BufferedInputStream bis = new BufferedInputStream(zf.getInputStream(entry));
                     FileOutputStream fos = new FileOutputStream(targetFile);
                     BufferedOutputStream bos = new BufferedOutputStream(fos)) {

                    byte[] buffer = new byte[2048];
                    int length;

                    while ((length = bis.read(buffer, 0, buffer.length)) != -1) {
                        bos.write(buffer, 0, length);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to unpack native archive: '" + artifact.getName() + "' to '" + destination.toString() + "'", e);
            return;
        }

        LOGGER.debug("Successfully unpacked: '" + artifact.getName() + "' to '" + destination.toString() + "'");
    }
}
