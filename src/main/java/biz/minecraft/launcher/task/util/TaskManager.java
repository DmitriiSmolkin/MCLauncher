package biz.minecraft.launcher.task.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.LinkedList;
import java.util.Queue;

public class TaskManager {

    private final Logger LOGGER = LoggerFactory.getLogger(TaskManager.class);

    volatile Queue<QueueableProgressTask> tasks;
    volatile boolean executing;

    public TaskManager()
    {
        tasks = new LinkedList<>();
    }

    public synchronized void queueExecution(QueueableProgressTask worker)
    {
        tasks.add(worker);
        if (!executing) executeNext();
    }

    public synchronized void executeNext()
    {
        SwingWorker worker = tasks.poll();

        if (worker != null) {
            setExecuting(true);
            LOGGER.debug("Starting the next task... (" + tasks.size() + " more queued)");
            worker.execute();
        }
    }

    public void setExecuting(boolean executing)
    {
        this.executing = executing;
    }

}
