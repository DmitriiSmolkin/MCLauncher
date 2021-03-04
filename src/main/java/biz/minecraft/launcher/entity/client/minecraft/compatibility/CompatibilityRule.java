package biz.minecraft.launcher.entity.client.minecraft.compatibility;

import java.util.Map;

/**
 * The rules key is used to determine which platforms to download the file to.
 * When the action is allow, the file will be downloaded to the platform stated in os.
 * When the action is disallow, the file will not be downloaded to the platform stated in os.
 * If there is no os key, the rule is default for non-specified platforms.
 */
public class CompatibilityRule
{
    private Action action;
    private OSRestriction os;
    private Map<String, String> features;

    public CompatibilityRule() {
        this.action = Action.ALLOW;
    }

    public CompatibilityRule(final CompatibilityRule compatibilityRule) {
        this.action = Action.ALLOW;
        this.action = compatibilityRule.action;
        if (compatibilityRule.os != null) {
            this.os = new OSRestriction(compatibilityRule.os);
        }
        if (compatibilityRule.features != null) {
            this.features = compatibilityRule.features;
        }
    }


    public Action getAppliedAction() {
        if (this.os != null && !this.os.isCurrentOperatingSystem()) {
            return null;
        }
        return this.action;
    }

    public Action getAppliedAction(final FeatureMatcher featureMatcher) {
        if (this.os != null && !this.os.isCurrentOperatingSystem()) {
            return null;
        }
        if (this.features != null) {
            if (featureMatcher == null) {
                return null;
            }
            for (final Map.Entry<String, String> feature : this.features.entrySet()) {
                if (!featureMatcher.hasFeature(feature.getKey(), feature.getValue())) {
                    return null;
                }
            }
        }
        return this.action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public void setOs(OSRestriction os) {
        this.os = os;
    }

    public void setFeatures(Map<String, String> features) {
        this.features = features;
    }

    public Action getAction() {
        return this.action;
    }

    public OSRestriction getOs() {
        return this.os;
    }

    public Map<String, String> getFeatures() {
        return this.features;
    }

}