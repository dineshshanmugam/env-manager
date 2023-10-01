package com.envr.manage.envmanager.service;

import com.envr.manage.envmanager.exception.AppException;
import com.envr.manage.envmanager.models.EnvironmentVariables;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.Messages;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import static com.envr.manage.envmanager.utils.AppConstants.NEW_LINE;

/**
 * state of current variables per env
 */
public class EnvironmentState {
    private static final String LOG_PREFIX = "Line ";
    private static final String ENVIRONMENT = "ENVIRONMENT";
    private final Logger log = Logger.getInstance(EnvironmentState.class.getName());
    private String mainFrameDialogMessage = "";
    private HashSet<EnvironmentVariables> loadedEnvironments = new HashSet<>();
    public final List<String> myEnvironmentNames = new ArrayList<>();

    private String currentDataStr = "";
    private String currentEnv = "";
    private EnvironmentVariables currentSelectedEnv;
    private final EnvironmentVariableStorage envVariableStorage;

    public EnvironmentState(FileStorage envVariableStorage) {
        this.envVariableStorage = envVariableStorage;
        try {
            getEnvironments();
        }catch(Exception ex) {
            log.error(ex.getMessage(),ex);
            throw new AppException("Could not get environment");
        }
        setCurrentDataStr("");
        setCurrentEnvData();
    }

    public void createEnvironment(String envName) {
        this.myEnvironmentNames.add(envName);
        var newEnv = new EnvironmentVariables(envName);
        newEnv.getEnvironment().putAll(currentSelectedEnv.getEnvironment());
        this.loadedEnvironments.add(newEnv);

    }

    public boolean deleteEnvironment(String envName) throws IOException {
        if (this.loadedEnvironments.size() == 1) {
            return false;
        }
        var envToDelete = new EnvironmentVariables(envName);
        this.myEnvironmentNames.remove(envName);
        this.loadedEnvironments.remove(envToDelete);
        saveEnvironments();
        return true;
    }

    public Set<EnvironmentVariables> getLoadedEnvironments() {
        return loadedEnvironments;
    }

    public String getCurrentDataStr() {
        return currentDataStr;
    }

    public void setCurrentDataStr(String currentDataStr) {
        this.currentDataStr = currentDataStr;
    }

    public String getCurrentEnv() {
        return currentEnv;
    }

    public void setCurrentEnv(String currentEnv) {
        this.currentEnv = currentEnv;
        setCurrentEnvData();
    }

    public String getMainFrameDialogMessage() {
        return mainFrameDialogMessage;
    }

    public boolean validateEnvString(EnvironmentVariables tempEnvVars, String envString) {
        Map<String, Integer> keyCounter = new LinkedHashMap<>();
        mainFrameDialogMessage = "Error:" + NEW_LINE;
        boolean noError = true;
        String[] data = envString.split(NEW_LINE);
        for (int j = 0; j < data.length; j++) {
            boolean tempNoError = true;
            int equalIndex = data[j].indexOf('=');
            if (equalIndex == -1) {
                mainFrameDialogMessage = mainFrameDialogMessage.concat(LOG_PREFIX + j + " missing equal to sign " + NEW_LINE);
                tempNoError = false;
            }
            if (!data[j].endsWith(";")) {
                mainFrameDialogMessage = mainFrameDialogMessage.concat(LOG_PREFIX + j + " missing ; at end " + NEW_LINE);
                tempNoError = false;
            }
            if (tempNoError) {
                int strLength = data[j].length();
                String key = data[j].substring(0, equalIndex);
                String value = data[j].substring(equalIndex + 1, strLength - 1);
                tempEnvVars.setEnvironment(key, value);
                if (keyCounter.containsKey(key)) {
                    noError = false;
                    mainFrameDialogMessage = mainFrameDialogMessage.concat(LOG_PREFIX + j + " Duplicate data at Line " + keyCounter.get(key) + NEW_LINE);
                } else {
                    keyCounter.put(key, j);
                }
            } else {
                noError = false;
            }
        }
        if (noError) {
            mainFrameDialogMessage = "";
        }
        return noError;
    }

    public void saveEnvironments() throws IOException {
        envVariableStorage.saveEnvironments(loadedEnvironments);
    }

    public void backupEnvironments() throws IOException {
        envVariableStorage.backupEnvironments();
    }

    public void getEnvironments() throws IOException {
        try {
            loadedEnvironments = envVariableStorage.getEnvironments();
            if (loadedEnvironments == null) {
                int userChoice = Messages.showOkCancelDialog(
                        "Invalid PIN Provided, use Cancel to try again (OR) do you want to create a new file with the pin provided ? (First time user)",
                        "Invalid Pin",
                        "Cancel",
                        "New File",
                        Messages.getQuestionIcon()
                );

                if (userChoice == Messages.CANCEL) {
                    createNewEnv();

                } else if (userChoice == Messages.OK) {
                    log.debug("User clicked Retry.");
                }
            }
            updateEnvironmentList();
        } catch ( FileNotFoundException ex) {
            log.error("No existing environments available - creating New");
            log.error("Error when loading file");
            createNewEnv();
            saveEnvironments();
        }
        catch (Exception ex) {
            log.error(ex.getMessage(),ex);
            throw new AppException(ex.getMessage());
        }

    }

    private void updateEnvironmentList(){
        loadedEnvironments.forEach(element -> myEnvironmentNames.add(element.getEnvName()));
    }
    private void createNewEnv() {
        this.loadedEnvironments = new HashSet<>();
        EnvironmentVariables dummy1 = new EnvironmentVariables("DEV");
        dummy1.setEnvironment(ENVIRONMENT, "DEV");
        EnvironmentVariables dummy2 = new EnvironmentVariables("PROD");
        dummy2.setEnvironment(ENVIRONMENT, "PROD");
        this.loadedEnvironments.add(dummy1);
        this.loadedEnvironments.add(dummy2);
    }

    private void setCurrentEnvData() {
        log.debug("Setting the current Env Data");
        currentDataStr = "";
        if(this.loadedEnvironments ==null){
            throw new AppException("Retry with valid PIN");
        }
        loadedEnvironments.forEach(env -> {

            if (env.getEnvName().equalsIgnoreCase(currentEnv)) {
                currentSelectedEnv = env;
                env.getEnvironment().forEach((k, v) -> {
                    if (currentDataStr.isEmpty()) {
                        currentDataStr = k + "=" + v + ";" + NEW_LINE;
                    } else {
                        currentDataStr = currentDataStr + k + "=" + v + ";" + NEW_LINE;
                    }
                });
            }
        });

    }




}