package com.envr.manage.envmanager.ui;

import com.envr.manage.envmanager.models.EnvVars;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.envr.manage.envmanager.service.PanelStateObj;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;

import java.awt.*;

import static com.envr.manage.envmanager.utils.AppConstants.NEW_LINE;
import static javax.swing.BoxLayout.Y_AXIS;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;

public class Panel1 implements PanelSample {

    private final Logger log = Logger.getInstance(Panel1.class.getName());
    private final JBTextArea envEditArea;
    private final JBTextArea appLogArea;
    private final JBLabel lineNumberLabel = new JBLabel("Line Number:");
    private final JBLabel noteLabel = new JBLabel("<html><font color='red'>Save to not lose changes</font></html>");

    private final JButton saveEnv;
    private final JBScrollPane envEditAreaScroll;
    private final JBScrollPane appLogAreaScroll;

    private final JBPanel panelRow1 = new JBPanel();
    private final JBPanel panelRow2 = new JBPanel();
    private final PanelStateObj panelStateObj;
    private final JBPanel envVariablePanel1;

    Panel1(PanelStateObj panelStateObj) {

        this.panelStateObj = panelStateObj;
        envVariablePanel1 = new JBPanel();
        envEditArea = new JBTextArea("");
        appLogArea = new JBTextArea("");
        saveEnv = new JButton("Save");

        envEditAreaScroll = new JBScrollPane(envEditArea,
                VERTICAL_SCROLLBAR_ALWAYS, HORIZONTAL_SCROLLBAR_ALWAYS);
        appLogAreaScroll = new JBScrollPane(appLogArea,
                VERTICAL_SCROLLBAR_ALWAYS, HORIZONTAL_SCROLLBAR_ALWAYS);

        envEditArea.addCaretListener(listener -> {
            String tempStr = envEditArea.getText().substring(0, listener.getDot());
            lineNumberLabel.setText("At Line " + (tempStr.split(NEW_LINE).length - 1));
        });
        envEditArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                changedUpdate(e);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                changedUpdate(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                panelStateObj.setCurrentDataStr(envEditArea.getText());
                showPanelSaveLabel();
            }
        });

        appLogArea.setEditable(false);

        lineNumberLabel.setText("Line Number 0");

        saveEnv.addActionListener(e -> {
            if (panelStateObj.getCurrentEnv()!=null && !panelStateObj.getCurrentEnv().isBlank()) {
                log.debug("saving current env " + panelStateObj.getCurrentEnv());

                panelStateObj.setCurrentDataStr(envEditArea.getText());
                EnvVars tempEnvVars = new EnvVars(panelStateObj.getCurrentEnv());
                if (panelStateObj.validateEnvString(tempEnvVars, panelStateObj.getCurrentDataStr())) {
                    log.debug("saving the valid Environment " + panelStateObj.getCurrentEnv());
                    panelStateObj.getMyEnvironments().remove(tempEnvVars);
                    panelStateObj.getMyEnvironments().add(tempEnvVars);
                    try {
                        panelStateObj.saveEnvironments();
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                    noteLabel.setVisible(false);
                    JOptionPane.showMessageDialog(envVariablePanel1, "Saved the env " + panelStateObj.getCurrentEnv());
                } else {
                    appLogArea.setText(panelStateObj.getMainFrameDialogMessage());
                    JOptionPane.showMessageDialog(envVariablePanel1, panelStateObj.getMainFrameDialogMessage());
                }
            } else {
                JOptionPane.showMessageDialog(envVariablePanel1, "No Env Selected yet");
            }

        });

        envVariablePanel1.setLayout(new BoxLayout(envVariablePanel1, Y_AXIS));

        panelRow1.setLayout(new FlowLayout());
        panelRow1.add(lineNumberLabel);
        panelRow1.add(noteLabel);
        panelRow1.add(saveEnv);

        panelRow2.setLayout(new FlowLayout());
        panelRow2.add(envEditAreaScroll);
        panelRow2.add(appLogAreaScroll);

        envVariablePanel1.add(panelRow1);
        envVariablePanel1.add(panelRow2);

        envEditAreaScroll.setPreferredSize(new Dimension(300, 500));
        appLogAreaScroll.setPreferredSize(new Dimension(350, 430));
        lineNumberLabel.setPreferredSize(new Dimension(200, 50));
        noteLabel.setPreferredSize(new Dimension(300, 50));
        saveEnv.setPreferredSize(new Dimension(100, 50));
        envVariablePanel1.setPreferredSize(new Dimension(10, 10));

    }

    void showPanelSaveLabel() {
        noteLabel.setVisible(true);
    }

    @Override
    public JPanel getEnvPanel() {
        return envVariablePanel1;
    }

    @Override
    public boolean updateEnvPanel() {

        return true;
    }

    public void updateEnv() {
        envEditArea.setText(panelStateObj.getCurrentDataStr());
    }
}
