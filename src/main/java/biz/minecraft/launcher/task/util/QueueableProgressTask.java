package biz.minecraft.launcher.task.util;

import javax.swing.*;

/**
 * Abstract SwingWorker Task class for any long process which needs
 * to be in sequence (queue) and to monitor status on a JProgressBar and JLabel.
 */
public abstract class QueueableProgressTask extends SwingWorker<String, Double> {

    protected TaskManager taskManager;
    protected JProgressBar progressBar;
    protected JLabel progressLabel;

    public QueueableProgressTask(TaskManager taskManager, JProgressBar progressBar, JLabel progressLabel) {
        this.taskManager = taskManager;
        this.progressBar = progressBar;
        this.progressLabel = progressLabel;
    }

    @Override
    protected abstract String doInBackground();

    @Override
    final protected void done()
    {
        result();
        taskManager.setExecuting(false);
        taskManager.executeNext();
    }

    protected abstract void result();

}
