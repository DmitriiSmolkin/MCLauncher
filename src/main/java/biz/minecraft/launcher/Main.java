package biz.minecraft.launcher;

import com.formdev.flatlaf.IntelliJTheme;

import javax.swing.*;
import java.io.IOException;

public class Main {

    public static final String version = "1.0 Pre-Release 1";

    public static void main(String[] args) throws IOException {

        // TODO: Download progress

        // TODO: Memory settings (Launcher profile)
        // TODO: Logout (Launcher profile)

        // ☑️ TODO: Launcher Updater

        // TODO: Refactoring
        IntelliJTheme.install(Main.class.getResourceAsStream("/material-oceanic-contrast.theme.json"));
        SwingUtilities.invokeLater(() -> new LauncherFrame(args));

    }

}
