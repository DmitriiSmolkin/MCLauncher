package biz.minecraft.launcher.task.queueable;

import biz.minecraft.launcher.entity.client.Download;
import biz.minecraft.launcher.entity.client.Downloader;
import biz.minecraft.launcher.task.util.QueueableProgressTask;
import biz.minecraft.launcher.task.util.TaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class DownloadFilesTask extends QueueableProgressTask {

    private final Logger LOGGER = LoggerFactory.getLogger(DownloadFilesTask.class);

    private final List<Download> files;

    public DownloadFilesTask(TaskManager taskManager, List<Download> files, JLabel progressLabel, JProgressBar progressBar) {
        super(taskManager, progressBar, progressLabel);
        this.files = files;
    }

    @Override
    protected String doInBackground() {

        int numberOfFiles = files.size();

        for (int i = 0; i < numberOfFiles; i++) {
            if (files.get(i) == null) continue;
            Download file = files.get(i);
            Downloader.downloadFromURL(file.getURL(), new File(file.getPath()));
            double factor = ((double)(i + 1) / numberOfFiles);
            publish(factor); // publish -> process
        }

        return "Загрузка завершена.";
    }

    @Override
    protected void process(List<Double> doubles) {
        int amount = progressBar.getMaximum() - progressBar.getMinimum();
        progressBar.setValue((int)(progressBar.getMinimum() + (amount * doubles.get(doubles.size() - 1))));
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

}
