package bluej.parser;

import java.io.Reader;

import javax.swing.event.DocumentEvent;
import javax.swing.text.Document;

import org.syntax.jedit.tokenmarker.Token;

import bluej.parser.NodeTree.NodeAndPosition;

/**
 * A parsed compilation unit node.
 * 
 * @author davmac
 */
public class ParsedCUNode extends ParsedNode
{
	//private JavaTokenMarker marker = new JavaTokenMarker();
	
	
	public Token getMarkTokensFor(int pos, int length, int nodePos, Document document)
	{
//		TokenMarker tm = ((SyntaxDocument) document).getTokenMarker();
//		Segment line = new Segment();
//		
//		try {
//			document.getText(pos, length, line);
//		}
//		catch (BadLocationException ble) {
//			return null;
//		}
//		
//		int lineIndex = document.getDefaultRootElement().getElementIndex(pos);
//		return tm.markTokens(line, lineIndex);
		
		Token tok = new Token(length, Token.NULL);
		tok.next = new Token(0, Token.END);
		return tok;
	}

	public void textInserted(int nodePos, DocumentEvent event)
	{
	    NodeAndPosition child = getNodeTree().findNode(event.getOffset(), nodePos);
	    if (child != null) {
	        ParsedNode cnode = child.getNode();
	        NodeTree cnodeTree = cnode.getContainingNodeTree();
	        // grow the child node
	        cnodeTree.setNodeSize(cnodeTree.getNodeSize() + event.getLength());
	        // inform the child node of the change
	        child.getNode().textInserted(child.getPosition(), event);
	    }
	    else {
	        // We must handle the insertion ourself
	        // TODO
            // for now just do a full reparse
	        doReparse(event.getDocument());
	    }
	}
    
    public void textRemoved(int nodePos, DocumentEvent event)
    {
        int endPos = event.getOffset() - event.getLength();
        NodeAndPosition child = getNodeTree().findNodeAtOrBefore(endPos, nodePos);
	    
        while (child != null) {
            int childEndPos = child.getPosition() + child.getSize();
            if (childEndPos <= event.getOffset()) {
                break;
            }
            
            // Possible cases: beginning of child is removed, end of child is removed,
            // middle of child is removed, all of child is removed.
            if (child.getPosition() >= event.getOffset() && childEndPos <= endPos) {
                // Child node to be removed completely
                // TODO
            }
        }
        
        if (child != null) {
            child.getNode().textRemoved(nodePos + child.getPosition(), event);
            // TODO shrink the child node?
            // TODO check if an entire child/children were removed.
        }
        else {
            // We must handle the insertion ourself
            // TODO
            // for now just do a full reparse
            doReparse(event.getDocument());
        }
	}
	
	private void doReparse(Document document)
	{
	    getNodeTree().clear();
	    Reader r = new DocumentReader(document);
	    EditorParser parser = new EditorParser(r);
	    parser.parseCU(this);
	}
}
