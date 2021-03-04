package biz.minecraft.launcher.entity.client.minecraft.argument;

import biz.minecraft.launcher.entity.client.minecraft.compatibility.CompatibilityRule;

import java.util.List;

public class Argument {

    private List<CompatibilityRule> rules;
    private String[] values;

    public Argument() {

    }

    public String[] getValues() {
        return values;
    }

    public List<CompatibilityRule> getRules() {
        return rules;
    }

    public Argument(String[] values) {
        this.values = values;
    }

    public Argument(List<CompatibilityRule> rules, String[] values) {
        this.rules  = rules;
        this.values = values;
    }

}
