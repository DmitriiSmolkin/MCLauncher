package biz.minecraft.launcher.entity;

import biz.minecraft.launcher.OperatingSystem;
import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.util.Vector;

import static biz.minecraft.launcher.OperatingSystem.getCurrentPlatform;

public class LauncherProfile {

    private String username;

    // TODO: assetsUpToDate boolean

    // API Generates remember token when passed valid credentials and "remember me" option is activated
    // API Substitutes remember token to null on log out or on valid credentials login
    // API Only can compare not null remember tokens
    // API Gives back username, uuid and access token on valid credentials or valid pair of username and remember token
    @SerializedName(value = "remember-token")
    private String rememberToken;

    @SerializedName(value = "min-heap-size")
    private int minHeapSize; // Default: -Xms4096m

    @SerializedName(value = "max-heap-size")
    private int maxHeapSize; // Default: -Xmx8192m

    @SerializedName(value = "jre-bundle-installed")
    private boolean jreBundleInstalled;

    public LauncherProfile() {
        username = "";
        rememberToken = "";
        minHeapSize = 4096;
        maxHeapSize = 8192;
        jreBundleInstalled = false;
    }

    public static File getWorkingDirectory() {

        final String userHome = System.getProperty("user.home", ".");
        File workingDirectory = null;

        switch (OperatingSystem.getCurrentPlatform()) {
            case LINUX: {
                workingDirectory = new File(userHome, "Minecraft.biz/");
                break;
            }
            case WINDOWS: {
                final String applicationData = System.getenv("APPDATA");
                final String folder = (applicationData != null) ? applicationData : userHome;
                workingDirectory = new File(folder, "Minecraft.biz/");
                break;
            }
            case OSX: {
                workingDirectory = new File(userHome, "Library/Application Support/Minecraft.biz");
                break;
            }
        }

        return workingDirectory;
    }

    /**
     * Get Launcher's JRE path to java compiler on OSX or Windows if the file exists.
     *
     * @return True if file exists, otherwise null.
     */
    public static String getJreDir() {
        String separator = System.getProperty("file.separator");
        String WinPath = getWorkingDirectory() + separator + "runtime" + separator + "jre"+ separator + "bin" + separator;
        String OSXpath = getWorkingDirectory() + separator + "runtime" + separator + "jre" + separator + "Contents" + separator + "Home" + separator + "bin" + separator;
        if (getCurrentPlatform() == OperatingSystem.WINDOWS && new File(WinPath + "javaw.exe").isFile()) {
            return WinPath + "javaw.exe";
        } else if (getCurrentPlatform() == OperatingSystem.OSX && new File(OSXpath + "java").isFile()) {
            return OSXpath + "java";
        }
        return null;
    }

    public String getUsername() {
        return username;
    }

    public String getRememberToken() {
        return rememberToken;
    }

    public int getMinHeapSize() {
        return minHeapSize;
    }

    public int getMaxHeapSize() {
        return maxHeapSize;
    }

    public boolean isJreBundleInstalled() {
        return jreBundleInstalled;
    }

    /**
     * Custom node for Contacts JTree
     *
     * @param <E>
     */
    private class ContactsNodeVector<E> extends Vector<E> {

        String name;

        ContactsNodeVector(String name) {
            this.name = name;
        }

        public ContactsNodeVector(String name, E elements[]) {
            this.name = name;
            for (int i = 0, n = elements.length; i < n; i++) {
                add(elements[i]);
            }
        }

        public String toString() {
            return name;
        }
    }

    //TODO: update players data
}
