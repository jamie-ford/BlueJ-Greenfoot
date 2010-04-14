/*
 This file is part of the Greenfoot program. 
 Copyright (C) 2005-2009  Poul Henriksen and Michael Kolling 
 
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
package rmiextension.wrappers;

import java.awt.EventQueue;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.Iterator;

import javax.swing.text.BadLocationException;

import bluej.editor.moe.MoeEditor;
import bluej.editor.moe.MoeSyntaxDocument;
import bluej.extensions.BClass;
import bluej.extensions.BConstructor;
import bluej.extensions.BField;
import bluej.extensions.BMethod;
import bluej.extensions.BPackage;
import bluej.extensions.ClassNotFoundException;
import bluej.extensions.CompilationNotStartedException;
import bluej.extensions.PackageNotFoundException;
import bluej.extensions.ProjectNotOpenException;
import bluej.extensions.editor.Editor;
import bluej.parser.nodes.ParsedNode;
import bluej.parser.nodes.NodeTree.NodeAndPosition;

/**
 * @author Poul Henriksen <polle@mip.sdu.dk>
 * @version $Id: RClassImpl.java 7338 2010-04-14 15:02:10Z nccb $
 */
public class RClassImpl extends java.rmi.server.UnicastRemoteObject
    implements RClass
{

    BClass bClass;
    
    private static ProjectNotOpenException pnoe;
    private static PackageNotFoundException pnfe;
    private boolean booleanResult;
    
    /**
     * Package-private constructor. Use WrapperPool to instantiate.
     */
    RClassImpl(BClass bClass)
        throws RemoteException
    {
        this.bClass = bClass;
        if (bClass == null) {
            throw new NullPointerException("Argument can't be null");
        }
    }

    public RClassImpl()
        throws RemoteException
    {

    }

    /**
     * @return
     * @throws ProjectNotOpenException
     * @throws PackageNotFoundException
     */
    public void compile(boolean waitCompileEnd)
        throws ProjectNotOpenException, PackageNotFoundException, CompilationNotStartedException
    {
        bClass.compile(waitCompileEnd);
    }

    public void edit()
        throws ProjectNotOpenException, PackageNotFoundException, RemoteException
    {
        synchronized (RClassImpl.class) {
            pnoe = null;
            pnfe = null;
            
            EventQueue.invokeLater(new Runnable() {
                public void run()
                {
                    try {
                        Editor editor = bClass.getEditor();
                        if (editor != null) {
                            editor.setVisible(true);
                        }
                    }
                    catch (ProjectNotOpenException e) {
                        pnoe = e;
                    }
                    catch (PackageNotFoundException e) {
                        pnfe = e;
                    }
                }
            });
            
            if (pnoe != null) throw pnoe;
            if (pnfe != null) throw pnfe;
        }
    }

    public void insertAppendMethod(String comment, String methodName, String methodBody) throws ProjectNotOpenException, PackageNotFoundException, RemoteException
    {
        Editor e = bClass.getEditor();
        bluej.editor.Editor bje = bluej.extensions.editor.EditorBridge.getEditor(e);
        MoeSyntaxDocument doc = (MoeSyntaxDocument)(((MoeEditor)bje).getSourceDocument());
        
        
        NodeAndPosition<ParsedNode> classNode = findClassNode(doc);
        NodeAndPosition<ParsedNode> existingMethodNode = findMethodNode(methodName, classNode);

        if (existingMethodNode != null) {
            //Append to existing method:
            appendTextToNode(e, existingMethodNode, methodBody);
        } else {
            //Make a new method:
            String fullMethod = comment + "    public void " + methodName + "()\n    {\n" + methodBody + "    }\n";
            appendTextToNode(e, classNode, fullMethod);
        }
        e.setVisible(true);
    }
    
    private NodeAndPosition<ParsedNode> findClassNode(MoeSyntaxDocument doc)
    {
        NodeAndPosition<ParsedNode> root = new NodeAndPosition<ParsedNode>(doc.getParser(), 0, doc.getParser().getSize());
        for (NodeAndPosition<ParsedNode> nap : iterable(root)) {
            if (nap.getNode().getNodeType() == ParsedNode.NODETYPE_TYPEDEF)
                return nap;
        }
        return null;
    }

    public void insertMethodCallInConstructor(String methodName)
            throws ProjectNotOpenException, PackageNotFoundException,
            RemoteException
    {
        Editor e = bClass.getEditor();
        bluej.editor.Editor bje = bluej.extensions.editor.EditorBridge.getEditor(e);
        MoeSyntaxDocument doc = (MoeSyntaxDocument)(((MoeEditor)bje).getSourceDocument());
        
        NodeAndPosition<ParsedNode> constructor = findMethodNode(bClass.getName(), findClassNode(doc));
        if (constructor != null && false == hasMethodCall(doc, methodName, constructor, true)) {
            //Add at the end of the constructor:
            appendTextToNode(e, constructor, "\n        " + methodName + "();\n    ");
        }
        
        e.setVisible(true);
    }
    
    // Appends text to a node that ends in a curly bracket:
    private void appendTextToNode(Editor e, NodeAndPosition<ParsedNode> node, String text)
    {
        e.setText(e.getTextLocationFromOffset(node.getEnd()-1), e.getTextLocationFromOffset(node.getEnd()-1), text);
    }
    
    // This really returns an iterator, but wrapping it into an iterable means that
    // we can use Java's nice for-each loops:
    private Iterable<NodeAndPosition<ParsedNode>> iterable(final NodeAndPosition<ParsedNode> parent)
    {
        return new Iterable<NodeAndPosition<ParsedNode>>()
        {
            public Iterator<NodeAndPosition<ParsedNode>> iterator()
            {
                return new Iterator<NodeAndPosition<ParsedNode>>()
                {
                    private NodeAndPosition<ParsedNode> nextChild = parent.getNode().findNodeAtOrAfter(parent.getPosition(), parent.getPosition());
                    public boolean hasNext()
                    {
                        return nextChild != null;
                    }
        
                    @Override
                    public NodeAndPosition<ParsedNode> next()
                    {
                        NodeAndPosition<ParsedNode> curChild = nextChild;
                        nextChild = parent.getNode().findNodeAtOrAfter(nextChild.getEnd(), parent.getPosition());
                        return curChild;
                    }
        
                    public void remove()
                    {
                    }
                };
            };
        };
    }
    
    private boolean hasMethodCall(MoeSyntaxDocument doc, String methodName, NodeAndPosition<ParsedNode> methodNode, boolean root)
    {
        for (NodeAndPosition<ParsedNode> nap : iterable(methodNode)) {
            // Method nodes have comments as children, and the body:
            if (nap.getNode().getNodeType() == ParsedNode.NODETYPE_NONE && root) {
                return hasMethodCall(doc, methodName, nap, false);
            }
            
            try {
                if (nap.getNode().getNodeType() == ParsedNode.NODETYPE_EXPRESSION && doc.getText(nap.getPosition(), nap.getSize()).startsWith(methodName)) {
                    return true;
                }
            }
            catch (BadLocationException e) {
            }            
        }
        
        return false;
    }
    
    private NodeAndPosition<ParsedNode> findMethodNode(String methodName, NodeAndPosition<ParsedNode> start)
    {
        for (NodeAndPosition<ParsedNode> nap : iterable(start)) {
            if (nap.getNode().getNodeType() == ParsedNode.NODETYPE_NONE) {
                NodeAndPosition<ParsedNode> r = findMethodNode(methodName, nap);
                if (r != null)
                    return r;
            }
            if (nap.getNode().getNodeType() == ParsedNode.NODETYPE_METHODDEF && nap.getNode().getName().equals(methodName)) {
                return nap;
            }
        }
        
        return null;
    }

    /**
     * @param signature
     * @return
     * @throws ProjectNotOpenException
     * @throws ClassNotFoundException
     */
    public RConstructor getConstructor(Class<?>[] signature)
        throws ProjectNotOpenException, ClassNotFoundException, RemoteException
    {

        BConstructor bConstructor = bClass.getConstructor(signature);

        RConstructor rConstructor = WrapperPool.instance().getWrapper(bConstructor);
        return rConstructor;
    }

    /**
     * @return
     * @throws ProjectNotOpenException
     * @throws ClassNotFoundException
     */
    public RConstructor[] getConstructors()
        throws ProjectNotOpenException, ClassNotFoundException, RemoteException
    {

        BConstructor[] bConstructors = bClass.getConstructors();
        int length = bConstructors.length;
        RConstructor[] rConstructors = new RConstructor[length];
        for (int i = 0; i < length; i++) {
            rConstructors[i] = WrapperPool.instance().getWrapper(bConstructors[i]);
        }

        return rConstructors;
    }

    /**
     * @param methodName
     * @param params
     * @return
     * @throws ProjectNotOpenException
     * @throws ClassNotFoundException
     */
    public BMethod getDeclaredMethod(String methodName, Class<?>[] params)
        throws ProjectNotOpenException, ClassNotFoundException
    {
        return null;
    }

    /**
     * @return
     * @throws ProjectNotOpenException
     * @throws ClassNotFoundException
     */
    public BMethod[] getDeclaredMethods()
        throws ProjectNotOpenException, ClassNotFoundException
    {
        return bClass.getDeclaredMethods();

    }

    /**
     * @param fieldName
     * @return
     * @throws ProjectNotOpenException
     * @throws ClassNotFoundException
     */
    public RField getField(String fieldName)
        throws ProjectNotOpenException, ClassNotFoundException, RemoteException
    {

        BField wrapped = bClass.getField(fieldName);
        RField wrapper = WrapperPool.instance().getWrapper(wrapped);
        return wrapper;

    }

    /**
     * @return
     * @throws ProjectNotOpenException
     * @throws ClassNotFoundException
     */
    public BField[] getFields()
        throws ProjectNotOpenException, ClassNotFoundException
    {
        return bClass.getFields();
    }

    /**
     * @return
     * @throws ProjectNotOpenException
     * @throws PackageNotFoundException
     */
    public RPackage getPackage()
        throws ProjectNotOpenException, PackageNotFoundException, RemoteException
    {

        BPackage wrapped = bClass.getPackage();
        RPackage wrapper = WrapperPool.instance().getWrapper(wrapped);
        return wrapper;
    }

    /**
     * Gets the superclass of this class if it is a part of the project.
     * 
     * @see #getSuperclassName()
     * @return
     * @throws ProjectNotOpenException
     * @throws PackageNotFoundException
     * @throws ClassNotFoundException
     */
    public RClass getSuperclass()
        throws ProjectNotOpenException, PackageNotFoundException, ClassNotFoundException, RemoteException
    {
        BClass wrapped = bClass.getSuperclass();
        RClass wrapper = WrapperPool.instance().getWrapper(wrapped);
        return wrapper;
    }

    /**
     * @return
     * @throws ProjectNotOpenException
     * @throws PackageNotFoundException
     */
    public boolean isCompiled()
        throws ProjectNotOpenException, PackageNotFoundException
    {
        synchronized (RClassImpl.class) {
            pnoe = null;
            pnfe = null;
            try {
                EventQueue.invokeAndWait(new Runnable() {
                    public void run()
                    {
                        try {
                            booleanResult = bClass.isCompiled();
                        } catch (ProjectNotOpenException e) {
                            pnoe = e;
                        } catch (PackageNotFoundException e) {
                            pnfe = e;
                        }
                    }                
                });
            }
            catch (InterruptedException ie) {
                ie.printStackTrace();
            }
            catch (InvocationTargetException ite) {
                ite.printStackTrace();
            }
            
            if (pnoe != null) throw pnoe;
            if (pnfe != null) throw pnfe;
            
            return booleanResult;
        }
    }

    public String getToString() throws ProjectNotOpenException, ClassNotFoundException
    {
        return bClass.getName();
    }

    public String getQualifiedName()
        throws RemoteException
    {
        return bClass.getName();
    }

    public File getJavaFile()
        throws ProjectNotOpenException, PackageNotFoundException, RemoteException
    {
        return bClass.getJavaFile();
    }

    public void remove() throws ProjectNotOpenException, PackageNotFoundException, ClassNotFoundException, RemoteException
    {
        bClass.remove();
    }


    public void setReadOnly(boolean b) throws RemoteException, ProjectNotOpenException, PackageNotFoundException 
    {
        if(bClass != null && bClass.getEditor() != null) {
            bClass.getEditor().setReadOnly(b);
        }
    }

}
