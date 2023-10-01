package com.envr.manage.envmanager;

import com.envr.manage.envmanager.ui.EnvManageDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.NotNull;

public class ManageEnvironmentsAction extends AnAction implements DumbAware {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        EnvManageDialog dialog = new EnvManageDialog();
        dialog.show();
    }
    @Override
    public void update(AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(true);
    }
}
