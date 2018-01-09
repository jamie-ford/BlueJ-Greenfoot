/*
 This file is part of the Greenfoot program. 
 Copyright (C) 2005-2009,2010,2011,2013,2014,2015  Poul Henriksen and Michael Kolling 
 
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
package greenfoot.actions;

import bluej.extensions.SourceType;
import greenfoot.core.GClass;
import greenfoot.core.GPackage;
import greenfoot.gui.NewClassDialog;
import greenfoot.gui.classbrowser.ClassBrowser;
import greenfoot.gui.classbrowser.ClassView;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import bluej.Config;


/**
 * Action that creates a new class as a subclass of an existing class
 * 
 * @author Poul Henriksen <polle@mip.sdu.dk>
 */
public class NewSubclassAction extends AbstractAction
{
    protected ClassView superclass;
    protected ClassBrowser classBrowser;

    /**
     * Creates a new subclass of the class represented by the view
     * 
     * @param view
     *            The class that is to be the superclass
     * @param classBrowser
     *            The tree that is to contain the class
     * @param interactionListener
     *            The listener to be notified of interactions (instance creation, method calls) which
     *            occur on the new class.
     */
    public NewSubclassAction(ClassView view, ClassBrowser classBrowser)
    {
        this();
        this.superclass = view;
        this.classBrowser = classBrowser;
    }
    
    protected NewSubclassAction()
    {
        super(Config.getString("new.sub.class"));
    }
    
    @Override
    public void actionPerformed(ActionEvent e)
    {
        GClass superG = superclass.getGClass();
        
        boolean imageClass = superG.isActorClass() || superG.isActorSubclass();
        imageClass |= superG.isWorldClass() || superG.isWorldSubclass();
            
        if (imageClass) {
            createImageClass(Config.getString("imagelib.newClass"), null, null);
        }
        else {
            createNonActorClass();
        }
    }
    
    public void createImageClass(String title, String defaultName, List<String> description)
    {
        // The whole class is due to be deleted.
    }

    public void createNonActorClass()
    {
        JFrame f = (JFrame) SwingUtilities.getWindowAncestor(classBrowser);
        GPackage pkg = classBrowser.getProject().getDefaultPackage();
        NewClassDialog dialog = new NewClassDialog(f, pkg);
        dialog.setVisible(true);
        if (!dialog.okPressed()) {
            return;
        }

        createClassSilently(dialog.getClassName(), dialog.getSelectedLanguage());
    }

    public ClassView createClassSilently(String className, SourceType selectedLanguage) {
        ClassView classView = null;
        GClass gClass = superclass.createSubclass(className, selectedLanguage);
        if (gClass != null) {
            classView = new ClassView(classBrowser, gClass);
            classBrowser.addClass(classView);
        }
        return classView;
    }

}