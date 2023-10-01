package com.envr.manage.envmanager.ui;

import com.envr.manage.envmanager.models.EnvironmentVariables;
import com.envr.manage.envmanager.service.EnvironmentState;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;

import static com.envr.manage.envmanager.utils.AppConstants.NEW_LINE;
import static com.envr.manage.envmanager.utils.Utils.*;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;

public class UtilityPanel implements PanelSample {
    private final Logger log = Logger.getInstance(UtilityPanel.class.getName());
    private final JBTextArea envEditArea;
    private final JBTextArea appLogArea;
    private final JLabel lineNumberLabel = new JLabel("Line Number:");

    private final JButton getJson;
    private final JButton getString;

    private final JButton jsonToEnv;

    private final JBScrollPane envEditAreaScroll;
    private final JBScrollPane appLogAreaScroll;

    private final EnvironmentState panelStateObj;
    private final JBPanel envVariablePanel2;
    UtilityPanel(EnvironmentState panelStateObj){
        this.panelStateObj = panelStateObj;
        envVariablePanel2 = new JBPanel<>();
        envEditArea = new JBTextArea("");
        appLogArea = new JBTextArea("");
        getJson = new JButton("ToJson");
        getString = new JButton("JoinLine");
        jsonToEnv = new JButton("JsonToStr");

        envEditAreaScroll = new com.intellij.ui.components.JBScrollPane(envEditArea,
                VERTICAL_SCROLLBAR_ALWAYS, HORIZONTAL_SCROLLBAR_ALWAYS);
        appLogAreaScroll = new com.intellij.ui.components.JBScrollPane(appLogArea,
                VERTICAL_SCROLLBAR_ALWAYS, HORIZONTAL_SCROLLBAR_ALWAYS);

        envVariablePanel2.setLayout(null);

        envEditArea.addCaretListener(listener -> {
            String tempStr = envEditArea.getText().substring(0, listener.getDot());
            lineNumberLabel.setText("At Line " + (tempStr.split(NEW_LINE).length - 1));
        });

        envEditArea.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void removeUpdate(DocumentEvent e) {
                changedUpdate(e);
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                changedUpdate(e);
            }

            @Override
            public void changedUpdate(DocumentEvent arg0) {
                panelStateObj.setCurrentDataStr(envEditArea.getText());
            }
        });

        appLogArea.setEditable(false);

        envVariablePanel2.add(getJson);
        envVariablePanel2.add(getString);
        envVariablePanel2.add(jsonToEnv);

        lineNumberLabel.setText("Line Number 0");
        envVariablePanel2.add(lineNumberLabel);

        getJson.addActionListener(e -> {
            log.debug("saving current env " + panelStateObj.getCurrentEnv());
            EnvironmentVariables tempEnvVars = new EnvironmentVariables(panelStateObj.getCurrentEnv());
            if (panelStateObj.validateEnvString(tempEnvVars,envEditArea.getText())) {
                appLogArea.setText(toJson(tempEnvVars.getEnvironment()));
            } else {
                appLogArea.setText(panelStateObj.getMainFrameDialogMessage());
                JOptionPane.showMessageDialog(envVariablePanel2, panelStateObj.getMainFrameDialogMessage());
            }


        });

        getString.addActionListener(e->
           appLogArea.setText(toSingleLine(envEditArea.getText())));

        jsonToEnv.addActionListener(e->
           appLogArea.setText(jsonStrToEnvStr(envEditArea.getText())));

        envVariablePanel2.add(envEditAreaScroll);

        envVariablePanel2.add(appLogAreaScroll);


        envEditAreaScroll.setBounds(10, 60, 300, 500);
        appLogAreaScroll.setBounds(325, 130, 350, 430);

        lineNumberLabel.setBounds(100, 10, 300, 50);
        getJson.setBounds(325, 60, 100, 40);
        getString.setBounds(425,60,100,40);
        jsonToEnv.setBounds(525,60,100,40);

        envVariablePanel2.setBounds(55, 15, 10, 10);

    }


    @Override
    public JPanel getEnvPanel(){
        return envVariablePanel2;
    }

    @Override
    public boolean updateEnvPanel() {
        return false;
    }


    public void updateEnv() {
        envEditArea.setText(panelStateObj.getCurrentDataStr());
    }
}
