package bluej.debugmgr.texteval;

import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.JEditorPane;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;
import javax.swing.text.Document;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Element;

import bluej.editor.moe.BlueJSyntaxView;

/**
 * A modified editor pane for the text evaluation area.
 * The standard JEditorPane is adjusted to take the tag line to the left into
 * account in size computations.
 * 
 * @author Michael Kolling
 * @version $Id: TextEvalPane.java 2798 2004-07-14 16:22:29Z mik $
 */
public class TextEvalPane extends JEditorPane {

    public Dimension getPreferredSize() 
    {
        Dimension d = super.getPreferredSize();
        d.width += BlueJSyntaxView.TAG_WIDTH + 8;  // bit of empty space looks nice
        return d;
    }
    
    /**
     * Make sure, when we are scrolling to follow the caret,
     * that we can see the tag area as well.
     */
    public void scrollRectToVisible(Rectangle rect)
    {
        rect.x -= BlueJSyntaxView.TAG_WIDTH + 4;
        rect.width += BlueJSyntaxView.TAG_WIDTH + 4;
        super.scrollRectToVisible(rect);
    }
    
    /**
     * Paste the contents of the clipboard.
     */
    public void paste()
    {
        if(!isLegalCaretPos())
            setCaretPosition(getDocument().getLength());
        super.paste();
    }

    /**
     * Check whether the given text positiob is allowed as a caret position in
     * the text eval area. This will only allow positions in the last line.
     * 
     * @param pos  The position to be checked
     * @return  True is placing the caret to this position is okay.
     */
    public boolean isLegalCaretPos()
    {
        AbstractDocument doc = (AbstractDocument) getDocument();
        int pos = getCaretPosition();
        Element line = doc.getParagraphElement(doc.getLength());
        int lineStart = line.getStartOffset() + 1;  // ignore space at front
        return pos >= lineStart;
    }
    
}
