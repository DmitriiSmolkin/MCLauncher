package biz.minecraft.launcher;

import biz.minecraft.launcher.dao.LauncherProfileDAO;
import biz.minecraft.launcher.entity.LauncherProfile;
import biz.minecraft.launcher.entity.NewsItem;
import biz.minecraft.launcher.entity.Server;
import biz.minecraft.launcher.panel.*;
import biz.minecraft.launcher.service.LauncherService;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class LauncherFrame extends JFrame {

    public LauncherFrame(String[] args) {

        super("Minecraft.biz Launcher");

        LauncherService service = new LauncherService();

        // service.updateLauncher(Arrays.asList(args)); TODO: ACTIVATE ON PROD

        LauncherProfileDAO launcherProfileDAO = new LauncherProfileDAO();

        LauncherProfile launcherProfile = launcherProfileDAO.find().orElse(new LauncherProfile());

        List<NewsItem> news = service.getNewsList();
        List<Server> servers = service.getServerList();

        Container contentPane = getContentPane();
        contentPane.setLayout(new MigLayout());

        NewsPanel newsPanel = new NewsPanel(news);
        SettingsPanel settingsPanel = new SettingsPanel();

        JTabbedPane tabbedPane = new JTabbedPane();

        ServersPanel serverPanel = new ServersPanel(servers);

        tabbedPane.add("Новости", newsPanel);
        tabbedPane.add("Наши сервера", serverPanel);
        tabbedPane.add("Настройки", settingsPanel);

        add(tabbedPane, "wrap");
        add(new StarterPanel(serverPanel), "gaptop 5, growx");

        pack();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);

    }

    // Не нагружаем EDT поток вынося тяжелые операции в отдельный
//    public void actionPerformed(ActionEvent e) {
//        new Thread(new Runnable() {
//            public void run() {
//                final String text = readHugeFile();
//                SwingUtilities.invokeLater(new Runnable() {
//                    public void run() {
//                        textArea.setText(text);
//                    }
//                });
//            }
//        }).start();
//    }

}
