package biz.minecraft.launcher.entity.client.minecraft.argument;

import biz.minecraft.launcher.OperatingSystem;
import biz.minecraft.launcher.entity.client.minecraft.compatibility.Action;
import biz.minecraft.launcher.entity.client.minecraft.compatibility.CompatibilityRule;
import biz.minecraft.launcher.entity.client.minecraft.compatibility.OSRestriction;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.*;

public class ArgumentsDeserializer implements JsonDeserializer<Map<ArgumentType, List<Argument>>> {

    @Override
    public Map<ArgumentType, List<Argument>> deserialize(JsonElement jElement, Type typeOfT, JsonDeserializationContext context) {

        Map<ArgumentType, List<Argument>> arguments = new HashMap<>();

        JsonObject jsonObject = jElement.getAsJsonObject();
        Set<Map.Entry<String, JsonElement>> argumentTypes = jsonObject.entrySet();

        for (Map.Entry<String, JsonElement> argumentType : argumentTypes) {

            List<Argument> argumentsList = new ArrayList<>();

            for (JsonElement argument : argumentType.getValue().getAsJsonArray()) {
                if (argument.isJsonObject()) {

                    JsonObject argumentObject = argument.getAsJsonObject();

                    argumentsList.add(deserializeArgument(argumentObject));

                } else {
                    argumentsList.add(new Argument(new String[]{argument.getAsString()}));
                }

            }

            arguments.put(ArgumentType.valueOf(argumentType.getKey().toUpperCase()), argumentsList);
        }

        return arguments;
    }

    /**
     * Deserializes argument object from snapshot 17w43a (release 1.13) and higher.
     *
     * Cases:
     *
     * - String i.e. "--username"
     * - Argument object where value's type is a String i.e. "value": "--demo"
     * - Argument object where value's type is an array of Strings i.e. "value": ["-Dos.name=Windows 10","-Dos.version=10.0"]
     *
     * @param argumentObject JsonObject representing Argument object.
     * @return
     */
    private Argument deserializeArgument(JsonObject argumentObject) {

        List<CompatibilityRule> rules = new ArrayList<>();

        if (argumentObject.has("rules")) {
            for (JsonElement rule : argumentObject.get("rules").getAsJsonArray()) {
                JsonObject compatibilityRuleObject = rule.getAsJsonObject();
                rules.add(deserializeCompatibilityRule(compatibilityRuleObject));
            }
        }

        List<String> values = new ArrayList<>(); // Originally called "value" but may contain arrays in some cases

        if (argumentObject.has("value")) {
            if (argumentObject.get("value").isJsonArray()) {
                for (JsonElement argumentValue : argumentObject.get("value").getAsJsonArray()) {
                    values.add(argumentValue.getAsString());
                }
            } else {
                values.add(argumentObject.get("value").getAsString());
            }
        }

        Argument argument = null;

        if (rules.isEmpty() && !values.isEmpty()) {
            argument = new Argument(values.toArray(new String[values.size()]));
        } else if (!rules.isEmpty()  && !values.isEmpty()) {
            argument = new Argument(rules, values.toArray(new String[values.size()]));
        }

        return argument;
    }

    /**
     * Get CompatibilityRule Object from JsonObject.
     *
     * @param compatibilityRuleObject JsonObject representing CompatibilityRule object.
     * @return
     */
    private CompatibilityRule deserializeCompatibilityRule(JsonObject compatibilityRuleObject) {

        CompatibilityRule compatibilityRule = new CompatibilityRule();

        if (compatibilityRuleObject.has("action")) {
            compatibilityRule.setAction(Action.valueOf(compatibilityRuleObject.get("action").getAsString().toUpperCase()));
        }

        if (compatibilityRuleObject.has("os")) {
            compatibilityRule.setOs(deserializeOSRestriction(compatibilityRuleObject.get("os").getAsJsonObject()));
        }

        if (compatibilityRuleObject.has("features")) {
            compatibilityRule.setFeatures(deserializeFeatures(compatibilityRuleObject.get("features").getAsJsonObject()));
        }

        return compatibilityRule;
    }

    /**
     * Get OSRestriction Object from JsonObject.
     *
     * @param osRestrictionObject JsonObject representing OSRestriction object.
     * @return
     */
    private OSRestriction deserializeOSRestriction(JsonObject osRestrictionObject) {

        OSRestriction osRestriction = new OSRestriction();

        if (osRestrictionObject.has("name")) {
            osRestriction.setName(OperatingSystem.valueOf(osRestrictionObject.get("name").getAsString().toUpperCase()));
        }

        if (osRestrictionObject.has("version")) {
            osRestriction.setVersion(osRestrictionObject.get("version").getAsString());
        }

        if (osRestrictionObject.has("arch")) {
            osRestriction.setArch(osRestrictionObject.get("arch").getAsString());
        }

        return osRestriction;
    }

    /**
     * Get features Map from JsonObject.
     *
     * @param featuresObject JsonObject representing features Map.
     * @return
     */
    private Map<String, String> deserializeFeatures(JsonObject featuresObject) {

        Map<String, String> features = new HashMap<>();

        Set<Map.Entry<String, JsonElement>> entries = featuresObject.entrySet();

        for (Map.Entry<String, JsonElement> entry : entries) {
            features.put(entry.getKey(), entry.getValue().getAsString());
        }

        return features;
    }

}
