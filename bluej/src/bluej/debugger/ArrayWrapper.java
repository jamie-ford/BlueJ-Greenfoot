package bluej.debugger;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import bluej.pkgmgr.PkgMgrFrame;
import bluej.prefmgr.PrefMgr;

/**
 * A wrapper around array objects.
 *
 * The array wrapper is represented by a few red ovals that are visible on the
 * object bench.
 *
 * @author  Andrew Patterson
 * @version $Id: ArrayWrapper.java 1700 2003-03-13 03:34:20Z ajp $
 */
public class ArrayWrapper extends ObjectWrapper
{
    public static int WORD_GAP = 8;
    public static int SHADOW_SIZE = 3;

    ArrayWrapper(PkgMgrFrame pmf, ObjectBench ob, DebuggerObject obj, String instanceName)
    {
        super(pmf, ob, obj, instanceName);
    }

    /**
     * Creates the popup menu structure by parsing the object's
     * class inheritance hierarchy.
     *
     * @param className   class name of the object for which the menu is to be built
     */
    protected void createMenu(String className)
    {
        menu = new JPopupMenu(instanceName);

        JMenuItem item;

        item = new JMenuItem("int length = " + obj.getInstanceFieldCount());
//        item.addActionListener(
//            new ActionListener() {
//                public void actionPerformed(ActionEvent e) { /*invokeMethod(e.getSource());*/ }
 //           });
        item.setFont(PrefMgr.getPopupMenuFont());
        menu.add(item);

        menu.addSeparator();

        // add inspect and remove options
        menu.add(item = new JMenuItem(inspect));
        item.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) { inspectObject(); }
            });
        item.setFont(PrefMgr.getStandoutMenuFont());
        item.setForeground(envOpColour);
  
        menu.add(item = new JMenuItem(remove));
        item.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) { removeObject(); }
            });
        item.setFont(PrefMgr.getStandoutMenuFont());
        item.setForeground(envOpColour);

        add(menu);
    }

    /**
     * draw a UML style object (array) instance
     */
    protected void drawUMLStyle(Graphics2D g)
    {
        g.setFont(PrefMgr.getStandardFont());
        FontMetrics fm = g.getFontMetrics();

        drawUMLObjectShape(g,GAP+10,10,WIDTH-10,HEIGHT-10,3,5);
        drawUMLObjectShape(g,GAP+5,5,WIDTH-10,HEIGHT-10,3,5);
        drawUMLObjectShape(g,GAP,0,WIDTH-10,HEIGHT-10,3,5);

        drawUMLObjectText(g,GAP,0,WIDTH-10,HEIGHT-10,3,
                            instanceName + ":", displayClassName);
        
    }
}
