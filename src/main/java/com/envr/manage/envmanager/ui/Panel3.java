package com.envr.manage.envmanager.ui;

import com.envr.manage.envmanager.service.PanelStateObj;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

import static com.envr.manage.envmanager.utils.AppConstants.ENV_LOC;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;

public class Panel3 implements PanelSample {
    private static final String PREFIX_TEXT_FILE_LOC = "File Location: ";
    private final JBPanel envVariablePanel3;
    private final PanelStateObj panelStateObj;
    private final JBTextArea fileLocationLabel = new JBTextArea();
    private final JBTextArea systemEnvVariable = new JBTextArea();
    private final JBScrollPane envScroll;
    public Panel3(PanelStateObj inPanelStateObj) {
        panelStateObj = inPanelStateObj;
        envVariablePanel3 = new JBPanel();
        envVariablePanel3.setBounds(55, 15, 10, 10);
        envVariablePanel3.setLayout(null);

        Border blackLine = BorderFactory.createLineBorder(Color.black);
        envVariablePanel3.add(fileLocationLabel);
        fileLocationLabel.setBounds(10,10,700,50);
        fileLocationLabel.setBorder(blackLine);
        fileLocationLabel.setEditable(false);
        fileLocationLabel.setAutoscrolls(true);
        fileLocationLabel.setText(PREFIX_TEXT_FILE_LOC + PropertiesComponent.getInstance().getValue(ENV_LOC));

        final String[] systemEnvVars = {"System Vars: \n"};
        System.getenv().forEach((k,v)-> systemEnvVars[0] = systemEnvVars[0] +  k + "=" + v + ";\n");

        envScroll = new com.intellij.ui.components.JBScrollPane(systemEnvVariable,
                VERTICAL_SCROLLBAR_ALWAYS, HORIZONTAL_SCROLLBAR_ALWAYS);

        systemEnvVariable.setBorder(blackLine);
        systemEnvVariable.setEditable(false);
        systemEnvVariable.setAutoscrolls(true);
        systemEnvVariable.setText(systemEnvVars[0]);

        envScroll.setBounds(10,70,700,300);


        envVariablePanel3.add(envScroll);


    }

    @Override
    public JPanel getEnvPanel() {
        return envVariablePanel3;
    }

    @Override
    public boolean updateEnvPanel() {
        return false;
    }
}
