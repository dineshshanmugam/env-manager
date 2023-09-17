package com.envr.manage.envmanager.ui;

import com.envr.manage.envmanager.service.FileStorage;
import com.envr.manage.envmanager.service.PanelStateObj;
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

import static com.envr.manage.envmanager.utils.AppConstants.ENV_LOC;
import static com.envr.manage.envmanager.utils.AppConstants.ENV_MANAGER_DEFAULT_FILE_NAME;
import static javax.swing.SwingConstants.TOP;

public class EnvManageDialog extends DialogWrapper {
    private final Logger log = Logger.getInstance(EnvManageDialog.class.getName());
    private final PanelStateObj panelStateObj;
    private final JComboBox<DefaultComboBoxModel<String>> environmentsList;
    private final JButton newEnv;
    private final JButton deleteEnv;
    private final JBTabbedPane tabbedPane;
    private final Panel1 panel1;
    private final Panel2 panel2;
    private final Panel3 panel3;
    private final JBPanel panel0;
    private final JBPanel mainFrame;
    private String envFileLocation;
    private FileStorage envVariableStorage;

    public EnvManageDialog() {
        super(true);

        mainFrame = new JBPanel();
        panel0 = new JBPanel();

        final String manageEnv = PathManager.getConfigPath() + File.separator + "options" + File.separator + ENV_MANAGER_DEFAULT_FILE_NAME;
        envFileLocation = PropertiesComponent.getInstance().getValue(ENV_LOC);

        if (envFileLocation == null) {
            PropertiesComponent.getInstance().setValue(ENV_LOC, manageEnv);
            envFileLocation = manageEnv;
        } else {
            log.debug(envFileLocation);
        }

        char[] pin = getPin("Enter PIN");

        envVariableStorage = new FileStorage(envFileLocation, pin);
        panelStateObj = new PanelStateObj(envVariableStorage);

        tabbedPane = new com.intellij.ui.components.JBTabbedPane();

        panel1 = new Panel1(panelStateObj);
        tabbedPane.add("Manage", panel1.getEnvPanel());
        panel2 = new Panel2(panelStateObj);
        tabbedPane.add("Utility", panel2.getEnvPanel());
        panel3 = new Panel3(panelStateObj);
        tabbedPane.add("Info", panel3.getEnvPanel());

        final DefaultComboBoxModel<String> envItems = new DefaultComboBoxModel(this.panelStateObj.myEnvironmentNames.toArray());
        environmentsList = new com.intellij.openapi.ui.ComboBox(envItems);
        environmentsList.setEditable(false);
        environmentsList.setSelectedIndex(-1);

        environmentsList.addActionListener(e -> {
            panelStateObj.setCurrentEnv((String) environmentsList.getSelectedItem());
            panel1.updateEnv();
            panel2.updateEnv();
        });

        newEnv = new JButton("Clone");
        deleteEnv = new JButton("Delete");

        newEnv.addActionListener(action -> {
            String newEnvName = JOptionPane.showInputDialog("Enter a new name");
            if (panelStateObj.myEnvironmentNames.contains(newEnvName) || newEnvName == null || newEnvName.isEmpty() || newEnvName.isBlank()) {
                JOptionPane.showMessageDialog(tabbedPane, "Environment with this name exists already or Invalid Name");
            } else {
                panelStateObj.createEnvironment(newEnvName);
                envItems.addElement(newEnvName);
                envItems.setSelectedItem(newEnvName);
                panel1.updateEnv();
                panel2.updateEnv();
                try {
                    panelStateObj.backupEnvironments();
                    panelStateObj.saveEnvironments();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }

            }
        });

        deleteEnv.addActionListener(action -> {
            if (panelStateObj.getCurrentEnv() != null || !panelStateObj.getCurrentEnv().isBlank()) {
                try {
                    if (panelStateObj.deleteEnvironment(panelStateObj.getCurrentEnv())) {
                        envItems.removeElement(panelStateObj.getCurrentEnv());
                        environmentsList.setSelectedIndex(-1);
                    } else {
                        JOptionPane.showMessageDialog(tabbedPane, "Could Not Delete this (At least one env needed)");
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        panel0.setLayout(new BorderLayout());
        panel0.add(environmentsList, BorderLayout.CENTER);
        panel0.add(newEnv, BorderLayout.LINE_START);
        panel0.add(deleteEnv, BorderLayout.LINE_END);
        panel0.setMaximumSize(new Dimension(800, 50));

        mainFrame.setLayout(new BoxLayout(mainFrame, BoxLayout.Y_AXIS));
        mainFrame.add(panel0);
        mainFrame.add(tabbedPane);
        mainFrame.setSize(800, 700);

        tabbedPane.setTabPlacement(TOP);
        tabbedPane.addChangeListener(changeEvent -> {
            JTabbedPane sourceTabbedPane = (JTabbedPane) changeEvent.getSource();
            int index = sourceTabbedPane.getSelectedIndex();
            if (index == 1) {
                panel2.updateEnv();
            } else if (index == 0) {
                panel1.updateEnv();
            }
        });
        init();
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
