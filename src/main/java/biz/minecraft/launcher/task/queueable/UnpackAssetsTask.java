package biz.minecraft.launcher.task.queueable;

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
import java.util.concurrent.ExecutionException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class UnpackAssetsTask extends QueueableProgressTask {

    private final static Logger LOGGER = LoggerFactory.getLogger(UnpackAssetsTask.class);

    private final File assets;
    private final File clientDirectory;

    /**
     * Constructor.
     *
     * @param progressLabel JLabel for JProgressBar.
     * @param progressBar JProgressBar.
     */
    public UnpackAssetsTask(TaskManager taskManager, File assets, File clientDirectory, JLabel progressLabel, JProgressBar progressBar) {
        super(taskManager, progressBar, progressLabel);
        this.assets = assets;
        this.clientDirectory = clientDirectory;
    }

    @Override
    protected String doInBackground() {

        if (assets.exists() && assets.isFile()) {
            for (int i = 0; i < 1; i++) {

                unpackZipArchive(assets, new File(clientDirectory, "assets"));

                double factor = ((double)(i+1) / 1);
                System.out.println("Intermediate results ready");
                publish(factor); // publish the progress
            }
        }

        return "Ассеты распакованы!";
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
     * Unpack zip file to destination folder.
     *
     * @param archive Zip archive file.
     * @param destination Destination folder.
     */
    public static void unpackZipArchive(File archive, File destination) {

        try (ZipFile zf = new ZipFile(archive)) {

            Enumeration<? extends ZipEntry> entries = zf.entries();

            LOGGER.debug("Unpacking: '" + archive.getName() + "' to '" + destination.toString() + "'");

            while (entries.hasMoreElements()) {

                ZipEntry entry = entries.nextElement();
                File targetFile = new File(destination, entry.getName());

                if (targetFile.getParentFile() != null) targetFile.getParentFile().mkdirs();

                if (entry.isDirectory()) continue;

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
            LOGGER.warn("Failed to unpack Zip archive: '" + archive.getName() + "' to '" + destination.toString() + "'", e);
            return;
        }

        LOGGER.debug("Successfully unpacked: '" + archive.getName() + "' to '" + destination.toString() + "'");
    }
}
