/*
 This file is part of the BlueJ program. 
 Copyright (C) 1999-2009  Michael Kolling and John Rosenberg 

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
package bluej.editor.moe;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.Hashtable;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import bluej.BlueJTheme;
import bluej.Config;
import bluej.prefmgr.PrefMgr;
import bluej.prefmgr.PrefPanelListener;

/**
 * A PrefPanel subclass to allow the user to interactively edit
 * editor settings
 *
 * @author  Michael Kolling
 * @version $Id: EditorPrefPanel.java 7111 2010-02-10 03:12:54Z marionz $
 */
public class EditorPrefPanel extends JPanel implements PrefPanelListener
{
    private JTextField editorFontField;
    private JCheckBox hilightingBox;
    private JCheckBox autoIndentBox;
    private JCheckBox lineNumbersBox;
    private JCheckBox makeBackupBox;
    private JCheckBox matchBracketsBox;

    /**
     * Setup the UI for the dialog and event handlers for the buttons.
     */
    public EditorPrefPanel()
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BlueJTheme.generalBorder);

        //        add(Box.createVerticalGlue());

        JPanel editorPanel = new JPanel(new GridLayout(5,2,0,0));
        {
            String editorTitle = Config.getString("prefmgr.edit.editor.title");
            editorPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder(editorTitle),
                    BlueJTheme.generalBorder));
            editorPanel.setAlignmentX(LEFT_ALIGNMENT);

            
            JPanel fontPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            {
                fontPanel.add(new JLabel(Config.getString("prefmgr.edit.editorfontsize")+"  "));
                editorFontField = new JTextField(4);
                fontPanel.add(editorFontField);
            }
            editorPanel.add(fontPanel);
            autoIndentBox = new JCheckBox(Config.getString("prefmgr.edit.autoindent"));
            editorPanel.add(autoIndentBox);
            
            //colour scope highlighter slider
            editorPanel.add(new JLabel(Config.getString("prefmgr.edit.colortransparency")));
            lineNumbersBox = new JCheckBox(Config.getString("prefmgr.edit.displaylinenumbers"));
            editorPanel.add(lineNumbersBox);
            
            JPanel sliderPanel=new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            {               
                ScopeHighlightingPrefSlider colorSlider=new ScopeHighlightingPrefSlider();
                sliderPanel.add(colorSlider);
            }
            editorPanel.add(sliderPanel);           
            hilightingBox = new JCheckBox(Config.getString("prefmgr.edit.usesyntaxhilighting"));
            editorPanel.add(hilightingBox);
            
            editorPanel.add(new JLabel(" "));
            makeBackupBox = new JCheckBox(Config.getString("prefmgr.edit.makeBackup"));
            editorPanel.add(makeBackupBox);
            
            editorPanel.add(new JLabel(" "));
            matchBracketsBox= new JCheckBox(Config.getString("prefmgr.edit.matchBrackets"));
            editorPanel.add(matchBracketsBox);
        }
        add(editorPanel);

        add(Box.createVerticalStrut(BlueJTheme.generalSpacingWidth));

        //ScopeHighlightingSliderPanel scopePanel=new ScopeHighlightingSliderPanel();
        //add(scopePanel);
        add(Box.createVerticalGlue());
        add(Box.createVerticalGlue());
        add(Box.createVerticalGlue());
    }

    public void beginEditing()
    {
        editorFontField.setText(String.valueOf(PrefMgr.getEditorFontSize()));
        hilightingBox.setSelected(PrefMgr.getFlag(PrefMgr.HILIGHTING));
        autoIndentBox.setSelected(PrefMgr.getFlag(PrefMgr.AUTO_INDENT));
        lineNumbersBox.setSelected(PrefMgr.getFlag(PrefMgr.LINENUMBERS));
        makeBackupBox.setSelected(PrefMgr.getFlag(PrefMgr.MAKE_BACKUP));
        matchBracketsBox.setSelected(PrefMgr.getFlag(PrefMgr.MATCH_BRACKETS));
    }

    public void revertEditing()
    {
    }

    public void commitEditing()
    {
        int newFontSize = 0;

        try {
            newFontSize = Integer.parseInt(editorFontField.getText());
            PrefMgr.setEditorFontSize(newFontSize);
        }
        catch (NumberFormatException nfe) { }

        PrefMgr.setFlag(PrefMgr.HILIGHTING, hilightingBox.isSelected());
        PrefMgr.setFlag(PrefMgr.AUTO_INDENT, autoIndentBox.isSelected());
        PrefMgr.setFlag(PrefMgr.LINENUMBERS, lineNumbersBox.isSelected());
        PrefMgr.setFlag(PrefMgr.MAKE_BACKUP, makeBackupBox.isSelected());
        PrefMgr.setFlag(PrefMgr.MATCH_BRACKETS, matchBracketsBox.isSelected());
    }

    /**
     * scope highlighting colour slider
     */
  /*  class ScopeHighlightingSlider extends JSlider implements ChangeListener {

        public static final int MIN=0;
        public static final int MAX=255;
        public ScopeHighlightingSlider(){
            super(MIN, MAX);
            setValue(PrefMgr.getTransparency());
            Hashtable<Integer, JLabel>labelTable = new Hashtable<Integer, JLabel>();
            labelTable.put( new Integer(MIN), new JLabel("Transparent") );
            labelTable.put( new Integer(MAX), new JLabel("Highlighted") );
            setLabelTable( labelTable );
            setPaintLabels(true);
            addChangeListener(this);
        }

        public void stateChanged(ChangeEvent e) {
            JSlider slider = (JSlider) e.getSource();
            PrefMgr.setTransparency(slider.getValue());
        }
    }*/
}
