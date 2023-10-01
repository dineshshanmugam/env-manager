package com.envr.manage.envmanager.service;

import com.envr.manage.envmanager.models.EnvironmentVariables;

import java.io.IOException;
import java.util.HashSet;

/**
 * Save and retrieve env variables
 */
public interface EnvironmentVariableStorage {
    HashSet<EnvironmentVariables> getEnvironments() throws IOException, ClassNotFoundException;
    void saveEnvironments(HashSet<EnvironmentVariables> myEnvironments)  throws IOException;

    void backupEnvironments() throws IOException;
}
