package biz.minecraft.launcher.entity.client;

import biz.minecraft.launcher.Main;
import biz.minecraft.launcher.OperatingSystem;
import biz.minecraft.launcher.entity.LauncherProfile;
import biz.minecraft.launcher.entity.client.minecraft.DownloadInfo;
import biz.minecraft.launcher.entity.client.minecraft.ExtractRules;
import biz.minecraft.launcher.entity.client.minecraft.MinecraftVersion;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Downloader {

    public final static Logger LOGGER = LoggerFactory.getLogger(Downloader.class);

    /**
     * Get checksum (hash) of the given File and Hash Algorithm (SHA-1/MD5/etc.)
     *
     * @param file File.
     * @param digest Instance of Message Digest i.e. MessageDigest.getInstance("SHA-1").
     * @return Hash string or null.
     * @throws IOException
     */
    protected static String getFileChecksum(File file, MessageDigest digest) {

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] byteArray = new byte[1024];
            int bytesCount = 0;

            while ((bytesCount = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            };
        } catch (FileNotFoundException e) {
            LOGGER.warn("Failed to found the file for calculating a hash: '" + file.toString() + "'", e);
            return null;
        } catch (IOException e) {
            LOGGER.warn("Error on calculating hash of: '" + file.toString() + "'", e);
            return null;
        }

        byte[] bytes = digest.digest(); // bytes in decimal format

        StringBuilder sb = new StringBuilder();

        for(int i=0; i< bytes.length ;i++) {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1)); // convert to hexadecimal format
        }

        return sb.toString();
    }

    /**
     * Unpack native archive to destination folder.
     *
     * @param library Native library's path, sha1, size, url.
     * @param extractRules List of excludes when unpacking.
     * @param localClientDirectory Target folder for unpacking.
     */
    public static void unpackNative(DownloadInfo library, ExtractRules extractRules, File localClientDirectory, String clientVersion) {

        File file = new File(localClientDirectory, "libraries/" + library.getPath());
        File destination = new File(localClientDirectory, "versions/" + clientVersion + "/natives/");

        try (ZipFile zf = new ZipFile(file)) {

            Enumeration<? extends ZipEntry> entries = zf.entries();

            LOGGER.debug("Unpacking '" + library.getPath() + "' to '" + destination.toString() + "'");

            while (entries.hasMoreElements()) {

                ZipEntry entry = entries.nextElement();

                if (extractRules != null && !extractRules.shouldExtract(entry.getName())) {
                    continue;
                }

                File targetFile = new File(destination, entry.getName());

                if (targetFile.getParentFile() != null) {
                    targetFile.getParentFile().mkdirs();
                }

                if (entry.isDirectory()) {
                    continue;
                }

                try (BufferedInputStream bis = new BufferedInputStream(zf.getInputStream(entry));
                     FileOutputStream fos = new FileOutputStream(targetFile);
                     BufferedOutputStream bos = new BufferedOutputStream(fos)) {

                    byte[] buffer = new byte[2048];
                    int length;

                    while ((length = bis.read(buffer, 0, buffer.length)) != -1) {
                        bos.write(buffer, 0, length);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to unpack native archive: '" + file.getName() + "' to '" + destination.toString() + "'");
            return;
        }

        LOGGER.debug("Successfully unpacked: '" + file.getName() + "' to '" + destination.toString() + "'");
    }


    /**
     * Check whether the file is matches the given hash.
     *
     * @param file File.
     * @param hash Hash to compare to.
     * @param digest Hash Algorithm. Instance of Message Digest i.e. MessageDigest.getInstance("SHA-1").
     * @return True if the file matches hash, otherwise false.
     */
    protected boolean fileMatchesHash(File file, String hash, MessageDigest digest) {

        String originalHash = getFileChecksum(file, digest);

        if (originalHash != null && !originalHash.equals("")) {
            if (getFileChecksum(file, digest).equals(hash)) return true;
        }

        return false;
    }

    // JRE / MOD FILTER / MULTI SERVER RUNNING / REFACTORING

    protected void updateJre() {
        String arch = "x86";
        if (System.getProperty("os.arch").contains("64")) arch = "x64";
        File jreFolder = new File(LauncherProfile.getWorkingDirectory(), "runtime/jre");
        if (!jreFolder.exists()) {
            OperatingSystem os = OperatingSystem.getCurrentPlatform();
            URL url = this.url("https://cloud.minecraft.biz/jre/" + os.getName() + "/" + arch + "/bundle.zip");
            File archive = new File(LauncherProfile.getWorkingDirectory(), "runtime/bundle.zip");
            this.downloadFromURL(url, archive);
            File destination = new File(LauncherProfile.getWorkingDirectory(), "runtime/");
            this.unpackZipArchive(archive, destination);

            if (OperatingSystem.getCurrentPlatform() == OperatingSystem.OSX) {
                File java = new File(jreFolder, "Contents/Home/bin/java");
                try {
                    Files.setPosixFilePermissions(Paths.get(java.getAbsolutePath()), PosixFilePermissions.fromString("rwxr-xr-x"));
                } catch (IOException e) {
                    LOGGER.warn("Failed to set custom java executable rule.", e);
                }
            }

            boolean success = FileUtils.deleteQuietly(archive);

            if (success) {
                LOGGER.debug("'" + archive.getName() + "' deleted successfully.");
            } else {
                LOGGER.debug("Filed to delete '" + archive.getName() + "'.");
            }

        }

    }

    protected Download[] updateExtraDownloads(URL remoteClientRepository, File localClientDirectory) {

        try (InputStream is = url(remoteClientRepository, "extra.json").openStream()) {

            String json = IOUtils.toString(is, StandardCharsets.UTF_8);

            Gson gson = new Gson();

            Download[] downloads = gson.fromJson(json, DownloadInfo[].class);

            for (Download download : downloads) {

                File file = new File(localClientDirectory, download.getPath());

                if (file.exists() && !file.isDirectory() && download.getSha1() == null) {

                } else if ((file.exists() && !file.isDirectory() && !Downloader.checksumValid(file, download.getSha1())) || (!file.exists())) {
                    this.downloadFromURL(download.getURL(), file);
                }

            }

            return downloads;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    protected void updateAssets(File localClientDirectory, String assetsVersion) {

        URL assetsURL = url("https://cloud.minecraft.biz/assets/" + assetsVersion + ".zip");
        File assetsFile = new File(localClientDirectory, "assets.zip");
        File assetsDirectory = new File(localClientDirectory, "assets");

        if (!assetsDirectory.exists()) {
            this.downloadFromURL(assetsURL, assetsFile);
            this.unpackZipArchive(assetsFile, assetsDirectory);
            boolean success = FileUtils.deleteQuietly(assetsFile);

            if (success) {
                LOGGER.debug("'" + assetsFile.getName() + "' deleted successfully.");
            } else {
                LOGGER.debug("Filed to delete '" + assetsFile.getName() + "'.");
            }
        }

    }

    /**
     * Unpack zip file to destination folder.
     *
     * @param archive Zip archive file.
     * @param destination Destination folder.
     */
    public static void unpackZipArchive(File archive, File destination) {

        try (ZipFile zf = new ZipFile(archive)) {

            Enumeration<? extends ZipEntry> entries = zf.entries();

            LOGGER.debug("Unpacking: '" + archive.getName() + "' to '" + destination.toString() + "'");

            while (entries.hasMoreElements()) {

                ZipEntry entry = entries.nextElement();
                File targetFile = new File(destination, entry.getName());

                if (targetFile.getParentFile() != null) targetFile.getParentFile().mkdirs();

                if (entry.isDirectory()) continue;

                try (BufferedInputStream  bis = new BufferedInputStream(zf.getInputStream(entry));
                     FileOutputStream     fos = new FileOutputStream(targetFile);
                     BufferedOutputStream bos = new BufferedOutputStream(fos)) {

                    byte[] buffer = new byte[2048];
                    int length;

                    while ((length = bis.read(buffer, 0, buffer.length)) != -1) {
                        bos.write(buffer, 0, length);
                    }
                }
            }

        } catch (Exception e) {
            LOGGER.warn("Failed to unpack Zip archive: '" + archive.getName() + "' to '" + destination.toString() + "'");
            return;
        }

        LOGGER.debug("Successfully unpacked: '" + archive.getName() + "' to '" + destination.toString() + "'");
    }

    /**
     * Download file from URL to destination file.
     *
     * @param fileURL File to download URL.
     * @param destination Destination File.
     */
    public static void downloadFromURL(URL fileURL, File destination) {

        LOGGER.debug("Downloading '" + fileURL + "' to '" + destination + "'");

        try {
            FileUtils.copyURLToFile(fileURL, destination);
        } catch (IOException e) {
            LOGGER.warn("Error downloading: '" + fileURL + "' to '" + destination + "'", e);
        }
    }



    public static boolean checksumValid(File file, String hash) {

        String currentHash = null;

        try {
            currentHash = getFileChecksum(file, MessageDigest.getInstance("SHA-1"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        if (currentHash.equals(hash)) {
            return true;
        }

        return false;
    }

    /**
     * Get URL from String.
     *
     * @param string String URL.
     * @return URL Object.
     */
    public static URL url(String string) {
        URL url = null;

        try {
            url = new URL(string);
        } catch (MalformedURLException e) {
            LOGGER.warn("Error converting URL from String – Malformed URL: '" + string + "'", e);
        }

        return url;
    }

    public static URL url(URL url, String child) {

        try {
            url = new URL(url, child);
        } catch (MalformedURLException e) {
            LOGGER.warn("Error converting URL from String – Malformed URL: '" + url + "/" + child + "'", e);
        }

        return url;
    }

}
