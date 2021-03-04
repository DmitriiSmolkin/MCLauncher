package biz.minecraft.launcher.dao;

import biz.minecraft.launcher.entity.LauncherProfile;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.CharSequenceReader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.Optional;

/**
 * Object for accessing the launcher profile file.
 */
public class LauncherProfileDAO {

    private final static Logger LOGGER = LoggerFactory.getLogger(LauncherProfileDAO.class);

    private File file;
    private Gson gson;

    public LauncherProfileDAO() {
        file = new File(LauncherProfile.getWorkingDirectory(), ".launcher-profile");
        gson = new Gson();
    }

    /**
     * Get a launcher profile object from the existing json (at working directory).
     *
     * @return Optional: specified object when found, otherwise null (IOException).
     */
    public Optional<LauncherProfile> find() {

        LauncherProfile launcherProfile = null;

        try (Reader reader = new CharSequenceReader(
                new String(FileUtils.readFileToByteArray(file)))) {
            launcherProfile = gson.fromJson(reader, LauncherProfile.class);
        } catch (IOException e) {
            LOGGER.warn("Failed to find launcher profile.");
        }

        return Optional.ofNullable(launcherProfile);
    }

    /**
     * Create or overwrite launcher profile json from java object.
     *
     * @param launcherProfile Object to be represented by JSON.
     * @return Optional: specified object on successful save, null on error (IOException).
     */
    public Optional<LauncherProfile> save(LauncherProfile launcherProfile) {

        String json = gson.toJson(launcherProfile);

        try (FileWriter fw = new FileWriter(file, false)) {
            fw.write(json);
        } catch (IOException e) {
            LOGGER.warn("Failed to save launcher profile.", e);
            launcherProfile = null;
        }

        return Optional.ofNullable(launcherProfile);
    }

}
