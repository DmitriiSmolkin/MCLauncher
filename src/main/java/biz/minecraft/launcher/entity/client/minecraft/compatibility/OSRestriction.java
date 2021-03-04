package biz.minecraft.launcher.entity.client.minecraft.compatibility;

import biz.minecraft.launcher.OperatingSystem;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OSRestriction
{
    private OperatingSystem name;
    private String version;
    private String arch;

    public OSRestriction() {
    }

    public OperatingSystem getName() {
        return this.name;
    }

    public String getVersion() {
        return this.version;
    }

    public String getArch() {
        return this.arch;
    }

    public OSRestriction(final OSRestriction osRestriction) {
        this.name = osRestriction.name;
        this.version = osRestriction.version;
        this.arch = osRestriction.arch;
    }

    public void setName(OperatingSystem name) {
        this.name = name;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setArch(String arch) {
        this.arch = arch;
    }

    public boolean isCurrentOperatingSystem() {
        if (this.name != null && this.name != OperatingSystem.getCurrentPlatform()) {
            return false;
        }
        if (this.version != null) {
            try {
                final Pattern pattern = Pattern.compile(this.version);
                final Matcher matcher = pattern.matcher(System.getProperty("os.version"));
                if (!matcher.matches()) {
                    return false;
                }
            }
            catch (Throwable t) {}
        }
        if (this.arch != null) {
            try {
                final Pattern pattern = Pattern.compile(this.arch);
                final Matcher matcher = pattern.matcher(System.getProperty("os.arch"));
                if (!matcher.matches()) {
                    return false;
                }
            }
            catch (Throwable t2) {}
        }
        return true;
    }

    @Override
    public String toString() {
        return "OSRestriction{name=" + this.name + ", version='" + this.version + '\'' + ", arch='" + this.arch + '\'' + '}';
    }
}
