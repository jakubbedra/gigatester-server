package com.konfyrm.gigatester.features.domain;

/**
 * Every feature flag the application knows about. Flags are declared here, in code, together with their default value.
 * Their current values live only in memory (see FeatureFlagService), so a server restart puts every flag back to its default.
 */
public enum FeatureFlag {

    TODO_LIST("To-do board", "The personal to-do board: sidebar button, page and API.", false);

    private final String label;
    private final String description;
    private final boolean defaultValue;

    FeatureFlag(String label, String description, boolean defaultValue) {
        this.label = label;
        this.description = description;
        this.defaultValue = defaultValue;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public boolean getDefaultValue() {
        return defaultValue;
    }
}
