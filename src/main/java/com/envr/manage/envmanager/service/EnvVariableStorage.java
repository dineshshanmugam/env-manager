package com.envr.manage.envmanager.service;

import com.envr.manage.envmanager.models.EnvVars;

import java.io.IOException;
import java.util.HashSet;

/**
 * Save and retrieve env variables
 */
public interface EnvVariableStorage {
    HashSet<EnvVars> getEnvironments() throws IOException, ClassNotFoundException;
    void saveEnvironments(HashSet<EnvVars> myEnvironments)  throws IOException;

    void backupEnvironments() throws IOException;
}
