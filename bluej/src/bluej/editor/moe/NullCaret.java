/*
 This file is part of the BlueJ program. 
 Copyright (C) 1999-2010  Michael Kolling and John Rosenberg 

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

import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ChangeListener;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;

/**
 * A dummy Caret, for use when doing operations which move the caret a lot (e.g. which do
 * many separate insertions/removals in the editor).
 * 
 * <p>The default caret implementation is relatively slow to move.
 * 
 * @author Davin McCall
 */
public class NullCaret implements Caret
{
    int markPos;
    int dotPos;
    List<ChangeListener> changeListeners = new ArrayList<ChangeListener>();
    
    public NullCaret(int mark, int dot)
    {
        markPos = mark;
        dotPos = dot;
    }
    
    @Override
    public void addChangeListener(ChangeListener l)
    {
        changeListeners.add(l);
    }

    @Override
    public void deinstall(JTextComponent c)
    {
    }

    @Override
    public int getBlinkRate()
    {
        return 0;
    }

    @Override
    public int getDot()
    {
        return dotPos;
    }

    @Override
    public Point getMagicCaretPosition()
    {
        return null;
    }

    @Override
    public int getMark()
    {
        return markPos;
    }

    @Override
    public void install(JTextComponent c)
    {
    }

    @Override
    public boolean isSelectionVisible()
    {
        return false;
    }

    @Override
    public boolean isVisible()
    {
        return false;
    }

    @Override
    public void moveDot(int dot)
    {
        dotPos = dot;
    }

    @Override
    public void paint(Graphics g)
    {
    }

    @Override
    public void removeChangeListener(ChangeListener l)
    {
        changeListeners.remove(l);
    }

    @Override
    public void setBlinkRate(int rate)
    {
    }

    @Override
    public void setDot(int dot)
    {
        markPos = dot;
        dotPos = dot;
    }

    @Override
    public void setMagicCaretPosition(Point p)
    {
    }

    @Override
    public void setSelectionVisible(boolean v)
    {
    }

    @Override
    public void setVisible(boolean v)
    {
    }

}
