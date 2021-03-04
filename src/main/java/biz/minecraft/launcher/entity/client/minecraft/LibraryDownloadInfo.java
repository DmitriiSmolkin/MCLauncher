package biz.minecraft.launcher.entity.client.minecraft;

import java.util.Map;

public class LibraryDownloadInfo {

    private DownloadInfo artifact;
    private Map<String, DownloadInfo> classifiers;

    public LibraryDownloadInfo() {
        // An empty constructor is required for the Gson.
    }

    public DownloadInfo getDownloadInfo(String classifier) {
        if (classifier == null) {
            return this.artifact;
        }
        return this.classifiers.get(classifier);
    }

    public DownloadInfo getClassifier(String classifier) {
        return this.classifiers.get(classifier);
    }

    public DownloadInfo getArtifact() {
        return artifact;
    }

    public void setArtifact(DownloadInfo artifact) {
        this.artifact = artifact;
    }

    public Map<String, DownloadInfo> getClassifiers() {
        return classifiers;
    }

    public void setClassifiers(Map<String, DownloadInfo> classifiers) {
        this.classifiers = classifiers;
    }

}
