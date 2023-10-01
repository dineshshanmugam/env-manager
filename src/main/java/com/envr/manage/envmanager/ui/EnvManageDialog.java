package com.envr.manage.envmanager.ui;

import com.envr.manage.envmanager.exception.AppException;
import com.envr.manage.envmanager.service.FileStorage;
import com.envr.manage.envmanager.service.EnvironmentState;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTabbedPane;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

import static com.envr.manage.envmanager.utils.AppConstants.ENV_LOCATION_PROPERTY;
import static com.envr.manage.envmanager.utils.AppConstants.ENV_MANAGER_DEFAULT_FILE_NAME;
import static javax.swing.SwingConstants.TOP;

public class EnvManageDialog extends DialogWrapper {
    private final Logger log = Logger.getInstance(EnvManageDialog.class.getName());
    private final EnvironmentState panelStateObj;
    private final JComboBox<DefaultComboBoxModel<String>> environmentList;
    private final JButton newEnv;
    private final JButton deleteEnv;
    private final JBTabbedPane tabbedPane;
    private final MainPanel environmentEditorPanel;
    private final UtilityPanel utility;
    private final InfoPanel infoPanel;
    private final JBPanel environmentSelectorPanel;
    private final JBPanel mainFrame;
    private String envFileLocation;
    private FileStorage envVariableStorage;
    private final DefaultComboBoxModel<String> envItems;
    public EnvManageDialog() {
        super(true);

        mainFrame = new JBPanel<>();
        environmentSelectorPanel = new JBPanel<>();
        envFileLocation = getEnvFileLocation();

        char[] pin = getPin("Enter PIN");

        envVariableStorage = new FileStorage(envFileLocation, pin);
        panelStateObj = new EnvironmentState(envVariableStorage);

        tabbedPane = new com.intellij.ui.components.JBTabbedPane();

        environmentEditorPanel = new MainPanel(panelStateObj);
        tabbedPane.add("Manage", environmentEditorPanel.getEnvPanel());
        utility = new UtilityPanel(panelStateObj);
        tabbedPane.add("Utility", utility.getEnvPanel());
        infoPanel = new InfoPanel();
        tabbedPane.add("Info", infoPanel.getEnvPanel());
        envItems = new DefaultComboBoxModel(this.panelStateObj.myEnvironmentNames.toArray());
        environmentList = new com.intellij.openapi.ui.ComboBox(envItems);
        environmentList.setEditable(false);
        environmentList.setSelectedIndex(-1);

        environmentList.addActionListener(e -> {
            panelStateObj.setCurrentEnv((String) environmentList.getSelectedItem());
            environmentEditorPanel.updateEnv();
            utility.updateEnv();
        });

        newEnv = new JButton("Clone");
        deleteEnv = new JButton("Delete");

        addNewEnvironmentAction();
        addDeleteEnvironmentAction();

        environmentSelectorPanel.setLayout(new BorderLayout());
        environmentSelectorPanel.add(environmentList, BorderLayout.CENTER);
        environmentSelectorPanel.add(newEnv, BorderLayout.LINE_START);
        environmentSelectorPanel.add(deleteEnv, BorderLayout.LINE_END);
        environmentSelectorPanel.setMaximumSize(new Dimension(800, 50));

        mainFrame.setLayout(new BoxLayout(mainFrame, BoxLayout.Y_AXIS));
        mainFrame.add(environmentSelectorPanel);
        mainFrame.add(tabbedPane);
        mainFrame.setSize(800, 700);

        tabbedPane.setTabPlacement(TOP);
        tabbedPane.addChangeListener(changeEvent -> {
            JTabbedPane sourceTabbedPane = (JTabbedPane) changeEvent.getSource();
            int index = sourceTabbedPane.getSelectedIndex();
            if (index == 1) {
                utility.updateEnv();
            } else if (index == 0) {
                environmentEditorPanel.updateEnv();
            }
        });
        init();
    }

    private void addNewEnvironmentAction(){
        newEnv.addActionListener(action -> {
            String newEnvName = JOptionPane.showInputDialog("Enter a new name");
            if (panelStateObj.myEnvironmentNames.contains(newEnvName) || newEnvName == null || newEnvName.isEmpty() || newEnvName.isBlank()) {
                JOptionPane.showMessageDialog(tabbedPane, "Environment with this name exists already or Invalid Name");
            } else {
                panelStateObj.createEnvironment(newEnvName);
                envItems.addElement(newEnvName);
                envItems.setSelectedItem(newEnvName);
                environmentEditorPanel.updateEnv();
                utility.updateEnv();
                try {
                    panelStateObj.backupEnvironments();
                    panelStateObj.saveEnvironments();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }

            }
        });
    }

    public void addDeleteEnvironmentAction(){
        deleteEnv.addActionListener(action -> {
            if (panelStateObj.getCurrentEnv() != null || !panelStateObj.getCurrentEnv().isBlank()) {
                try {
                    if (panelStateObj.deleteEnvironment(panelStateObj.getCurrentEnv())) {
                        envItems.removeElement(panelStateObj.getCurrentEnv());
                        environmentList.setSelectedIndex(-1);
                    } else {
                        JOptionPane.showMessageDialog(tabbedPane, "Could Not Delete this (At least one env needed)");
                    }
                } catch (IOException e) {
                    throw new AppException(e);
                }
            }
        });
    }
    private String getDefaultStorageLocation(){
        return PathManager.getConfigPath() + File.separator + "options" + File.separator + ENV_MANAGER_DEFAULT_FILE_NAME;
    }

    private String getEnvFileLocation(){
        if (PropertiesComponent.getInstance().getValue(ENV_LOCATION_PROPERTY) == null) {
            PropertiesComponent.getInstance().setValue(ENV_LOCATION_PROPERTY, getDefaultStorageLocation());
            return getDefaultStorageLocation();
        } else {
          return PropertiesComponent.getInstance().getValue(ENV_LOCATION_PROPERTY);
        }
    }
    public char[] getPin(String message) {
        String pinString = Messages.showPasswordDialog(null, message, "Enter PIN (Default 0000)", null);
        if (pinString == null || pinString.isBlank() || pinString.isEmpty()) {
            return ("0000".toCharArray());
        }
        return pinString.toCharArray();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        mainFrame.setPreferredSize(new Dimension(800, 800));
        return mainFrame;
    }

    @Override
    protected ValidationInfo doValidate() {
        return null; // Validation passed
    }

    @Override
    protected Action[] createActions() {
        return new Action[]{};
    }

}
