package biz.minecraft.launcher.panel;

import biz.minecraft.launcher.service.GameService;
import biz.minecraft.launcher.task.util.TaskManager;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public class StarterPanel extends JPanel {

    JLabel progressLabel;
    JProgressBar progressBar;
    JButton startButton;

    GameService gameService = new GameService();
    TaskManager taskManager = new TaskManager();

    public StarterPanel(ServersPanel serversPanel) {

        initComponents();

        startButton.addActionListener(e -> {
            progressLabel.setText("Обновление клиента, пожалуйста подождите...");
            gameService.setServer(serversPanel.getSelectedServer());
            gameService.updateJre(taskManager, progressBar, progressLabel);
            gameService.updateClient(taskManager, progressBar, progressLabel);
            gameService.filterMods(taskManager, progressBar, progressLabel);
            gameService.runGame(taskManager, progressBar, progressLabel);
        });

        setBackground(UIManager.getColor( "List.background" ));
        setLayout(new MigLayout("", "[grow][]", ""));

        add(progressLabel, "growx");
        add(startButton, "span 1 2, wrap");
        add(progressBar, "growx");

    }

    public void initComponents() {
        progressLabel = new JLabel("");
        progressBar = new JProgressBar();
        startButton = new JButton("ИГРАТЬ");
        startButton.setPreferredSize(new Dimension(150, 50));
    }

}
