package biz.minecraft.launcher.panel;

import biz.minecraft.launcher.dao.LauncherProfileDAO;
import biz.minecraft.launcher.entity.LauncherProfile;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

public class SettingsPanel extends JPanel {

    private final static Logger LOGGER = LoggerFactory.getLogger(SettingsPanel.class);
    private final LauncherProfileDAO repository = new LauncherProfileDAO();
    private LauncherProfile profile = repository.find().orElse(new LauncherProfile());

    JCheckBox jre, client;

    public SettingsPanel() {

        JLabel minHeapSizeLabel = new JLabel("Минимальный размер выделяемой памяти: ");
        JLabel maxHeapSizeLabel = new JLabel("Максимальный размер выделяемой памяти: ");

        JTextField minHeapSizeField = new JTextField(8);
        JTextField maxHeapSizeField = new JTextField(8);

        minHeapSizeField.setText(Integer.toString(profile.getMinHeapSize()));
        maxHeapSizeField.setText(Integer.toString(profile.getMaxHeapSize()));

        setLayout(new MigLayout("insets 10"));

        setBackground(UIManager.getColor( "List.background" ));

        add(minHeapSizeLabel, "growx, split 2");
        add(minHeapSizeField, "span 2, wrap");

        add(maxHeapSizeLabel, "growx, split 2");
        add(maxHeapSizeField, "span 2, wrap");

    }

}
