package biz.minecraft.launcher.entity.client.minecraft;

import biz.minecraft.launcher.OperatingSystem;
import biz.minecraft.launcher.entity.client.minecraft.compatibility.CompatibilityRule;

import java.util.List;
import java.util.Map;

public class Library {

    private LibraryDownloadInfo downloads;
    private ExtractRules extract;
    private String name;
    private Map<OperatingSystem, String> natives;
    private List<CompatibilityRule> rules;

    public Library() {
        // An empty constructor is required for the Gson.
    }

    public LibraryDownloadInfo getDownloads() {
        return downloads;
    }

    public ExtractRules getExtract() {
        return extract;
    }

    public String getName() {
        return name;
    }

    public Map<OperatingSystem, String> getNatives() {
        return natives;
    }

    public List<CompatibilityRule> getRules() {
        return rules;
    }

}
