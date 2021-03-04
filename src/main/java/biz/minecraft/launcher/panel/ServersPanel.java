package biz.minecraft.launcher.panel;

import biz.minecraft.launcher.entity.Server;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ServersPanel extends JPanel {

    private JButton prev, next;
    private JLabel cover, description, name;
    private List<Server> serverList;
    private int current, total;
    private Server server;

    public ServersPanel(List<Server> serverList) {

        initComponents(serverList);

        total = serverList.size() - 1;
        current = 0;

        List<BufferedImage> covers = new ArrayList<>();

        Consumer<Server> consumer = s -> { covers.add(s.getCover()); };

        serverList.stream().forEach(consumer);

        server = serverList.get(current);

        cover.setIcon(new ImageIcon(server.getCover()));
        name.setText(server.getName().toUpperCase());
        description.setText(server.getDescription());

        prev.addActionListener(e -> {
            if (current > 0) {
                current--;
                server = serverList.get(current);
                cover.setIcon(new ImageIcon(covers.get(current)));
                name.setText(server.getName().toUpperCase());
                description.setText(server.getDescription());
            }
        });

        next.addActionListener(e -> {
            if (current < total) {
                current++;
                server = serverList.get(current);
                cover.setIcon(new ImageIcon(covers.get(current)));
                name.setText(server.getName().toUpperCase());
                description.setText(server.getDescription());
            }
        });

        setLayout(new MigLayout("fillx, aligny center", "[center growx][center][center growx]", ""));

        add(prev);
        add(cover);
        add(next, "wrap");

        add(description, "gaptop 10, gapbottom 3, span 3, wrap");
        add(name, "span 3");

    }

    public Server getSelectedServer() {
        return serverList.get(current);
    }

    private void initComponents(List<Server> serverList) {
        this.serverList = serverList;

        prev = new JButton("❮");
        next = new JButton("❯");
        cover = new JLabel();
        name = new JLabel();
        description = new JLabel();

        name.setForeground(Color.decode("#425b67"));
        name.setFont(new Font("Helvetica Neue", Font.BOLD, 38));

        prev.setBorder(new EmptyBorder(0, 0, 0, 0));
        prev.setPreferredSize(new Dimension(25, 60));
        next.setBorder(new EmptyBorder(0, 0, 0, 0));
        next.setPreferredSize(new Dimension(25, 60));
    }

}
