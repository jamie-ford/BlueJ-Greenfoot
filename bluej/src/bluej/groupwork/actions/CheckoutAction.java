/*
 This file is part of the BlueJ program. 
 Copyright (C) 1999-2009,2014,2015,2016  Michael Kolling and John Rosenberg 
 
 This program is free software; you can redistribute it and/or 
 modify it under the terms of the GNU General Public License 
 as published by the Free Software Foundation; either version 2 
 of the License, or (at your option) any later version. 
 
 This program is distributed in the hope that it will be useful, 
 but WITHOUT ANY WARRANTY; without even the implied warranty of 
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 GNU General Public License for more details. 
 
 You should have received a copy of the GNU General Public License 
 along with this program; if not, write to the Free Software 
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA. 
 
 This file is subject to the Classpath exception as provided in the  
 LICENSE.txt file that accompanied this code.
 */
package bluej.groupwork.actions;

import java.io.File;

import javafx.application.Platform;

import threadchecker.OnThread;
import threadchecker.Tag;
import bluej.Config;
import bluej.groupwork.Repository;
import bluej.groupwork.TeamSettingsController;
import bluej.groupwork.TeamUtils;
import bluej.groupwork.TeamworkCommand;
import bluej.groupwork.TeamworkCommandResult;
import bluej.groupwork.ui.ModuleSelectDialog;
import bluej.groupwork.ui.TeamSettingsDialog;
import bluej.pkgmgr.Import;
import bluej.pkgmgr.Package;
import bluej.pkgmgr.PkgMgrFrame;
import bluej.pkgmgr.Project;
import bluej.utility.Debug;
import bluej.utility.DialogManager;
import bluej.utility.FileUtility;
import bluej.utility.SwingWorker;

/**
 * An action to perform a checkout of a module in CVS. The module is checked
 * out into some directory (chosen by the user) and then opened as a BlueJ
 * project.
 *
 * @author Kasper
 */
public class CheckoutAction extends TeamAction
{
    public CheckoutAction(PkgMgrFrame pmf)
    {
        super(pmf, "team.checkout");
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(PkgMgrFrame oldFrame)
    {
        //TeamSettingsController tsc = new TeamSettingsController(projectDir);
        // Create a TeamSettingsController for the current project directory,
        // switch it to the new one once it's been created
        TeamSettingsController tsc = new TeamSettingsController(new File(".").getAbsoluteFile());
        TeamSettingsDialog tsd = tsc.getTeamSettingsDialog();
        tsd.setLocationRelativeTo(oldFrame);

        if (tsd.doTeamSettings() == TeamSettingsDialog.OK) {
            File projectDir;
            if (!tsc.getRepository(true).isDVCS()) {
                //if not DVCS, we need to select module.
                ModuleSelectDialog moduleDialog = new ModuleSelectDialog(oldFrame::getFXWindow, tsc.getRepository(true));
                moduleDialog.setLocationRelativeTo(oldFrame);
                moduleDialog.setVisible(true);

                String moduleName = moduleDialog.getModuleName();
                if (moduleName != null) {
                    File parentDir = FileUtility.getDirName(oldFrame, Config.getString("team.checkout.filechooser.title"),
                            Config.getString("team.checkout.filechooser.button"), true, true);
                    
                    if (Package.isPackage(parentDir)) {
                        Debug.message("Attempted to checkout a project into an existing project: " + parentDir);
                        DialogManager.showError(null, "team-cannot-import-into-existing-project");
                        return;
                    }
                    projectDir = new File(parentDir, moduleName);
                } else {
                    //null module.
                    return;
                }
            } else {
                //it is a DVCS project.
                //there is no module selection in a DVCS project. Therefore,
                //the selected file is the project dir.
                projectDir = FileUtility.getDirName(oldFrame, Config.getString("team.checkout.DVCS.filechooser.title"),
                        Config.getString("team.checkout.filechooser.button"), true, true);
                if (projectDir != null) {
                    if (Package.isPackage(projectDir)) {
                        Debug.message("Attempted to checkout a project into an existing project: " + projectDir);
                        DialogManager.showError(null, "team-cannot-import-into-existing-project");
                        return;
                    }
                } else {
                    //no project dir. nothing to do.
                    return;
                }
            }

            if (projectDir.exists()) {
                DialogManager.showError(null, "directory-exists-non-empty");
                return;
            }

            PkgMgrFrame newFrame;
            if (oldFrame.isEmptyFrame()) {
                newFrame = oldFrame;
            } else {
                newFrame = PkgMgrFrame.createFrame();
                newFrame.setVisible(true);
                newFrame.setEnabled(false);
            }

            new CheckoutWorker(newFrame, tsc.getRepository(true), projectDir, tsc).start();
        }
    }

    /**
     * A worker to perform the checkout operation.
     *
     * @author Davin McCall
     */
    private class CheckoutWorker extends SwingWorker
    {
        private Repository repository;
        private PkgMgrFrame newFrame;
        private File projDir;
        private TeamSettingsController tsc;

        private TeamworkCommandResult response;
        private boolean failed = true;

        public CheckoutWorker(PkgMgrFrame newFrame, Repository repository, File projDir, TeamSettingsController tsc)
        {
            this.newFrame = newFrame;
            this.repository = repository;
            this.projDir = projDir;
            this.tsc = tsc;
        }

        /*
         * Get the files from the repository.
         * @see bluej.utility.SwingWorker#construct()
         */
        public Object construct()
        {
            newFrame.setStatus(Config.getString("team.checkingout"));
            newFrame.startProgress();
            TeamworkCommand checkoutCmd = repository.checkout(projDir);
            response = checkoutCmd.getResult();

            failed = response.isError();

            newFrame.stopProgress();
            if (! failed) {
                newFrame.setStatus(Config.getString("team.checkedout"));
            }

            newFrame.stopProgress();

            return response;
        }

        /*
         * Now open the directory as a BlueJ project.
         * @see bluej.utility.SwingWorker#finished()
         */
        public void finished()
        {
            if (! failed) {
                if (! Project.isProject(projDir.toString())) {
                    // Try and convert it to a project
                    if (! Import.convertNonBlueJ(newFrame::getFXWindow, projDir)) {
                        cleanup();
                        return;
                    }
                }

                Project project = Project.openProject(projDir.toString(), newFrame);

                project.setTeamSettingsController(tsc);
                Package initialPackage = project.getPackage(project.getInitialPackageName());
                newFrame.openPackage(initialPackage);
                newFrame.setEnabled(true);
            }
            else {
                Platform.runLater(() -> TeamUtils.handleServerResponseFX(response, newFrame.getFXWindow()));
                cleanup();
            }
        }

        /**
         * Clean up after failed checkout.
         */
        @OnThread(Tag.Swing)
        public void cleanup()
        {
            deleteDirectory(projDir);
            projDir.delete();
            newFrame.doClose(true, false);
            // The frame might not have closed if it was the
            // last frame. In that case we want to enable it.
            newFrame.setEnabled(true);
        }
        
        /**
         * deletes all files and subdirectories inside a directory
         * @param dir 
         */
        private void deleteDirectory(File dir)
        {
            if (dir != null && dir.listFiles() != null) {
                for (File f : dir.listFiles()) {
                    if (f.isDirectory()) {
                        deleteDirectory(f);
                    }
                    f.delete();
                }
            }
        }
    }
}
