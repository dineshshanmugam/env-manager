package com.envr.manage.envmanager.models;

import java.io.Serializable;
import java.util.*;

public class EnvironmentVariables implements Serializable {
    private static final long serialVersionUID = 0L;
    public EnvironmentVariables(String envName) {
        this.envName = envName;
        this.envTags = new ArrayList<>();
        this.environment = new HashMap<>();
    }

    public String getEnvName() {
        return envName;
    }

    public List<String> getEnvTags() {
        return envTags;
    }

    public void setEnvTags(String envTags) {
        this.envTags.add(envTags);
    }

    public Map<String,String> getEnvironment() {
        return this.environment;
    }

    public void setEnvironment(String key, String value) {
        this.environment.put(key, value);
    }

    private String envName;

    private List<String> envTags;

    private Map<String, String> environment;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EnvironmentVariables)) return false;

        EnvironmentVariables that = (EnvironmentVariables) o;

        return Objects.equals(envName, that.envName);
    }

    @Override
    public int hashCode() {
        return envName != null ? envName.hashCode() : 0;
    }
}
