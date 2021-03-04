package biz.minecraft.launcher.task.queueable;

import biz.minecraft.launcher.service.GameService;
import biz.minecraft.launcher.task.util.QueueableProgressTask;
import biz.minecraft.launcher.task.util.TaskManager;

import javax.swing.*;
import java.util.concurrent.ExecutionException;

public class GameRunnerTask extends QueueableProgressTask {

    private GameService gameService;

    public GameRunnerTask(GameService gameService, TaskManager taskManager, JProgressBar progressBar, JLabel progressLabel) {
        super(taskManager, progressBar, progressLabel);
        this.gameService = gameService;
    }

    @Override
    protected String doInBackground() {
        gameService.runGame();
        return "Папка mods очищена.";
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
