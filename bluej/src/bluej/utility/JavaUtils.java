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
package bluej.utility;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bluej.debugger.gentype.GenTypeClass;
import bluej.debugger.gentype.GenTypeDeclTpar;
import bluej.debugger.gentype.GenTypeParameter;
import bluej.debugger.gentype.GenTypeSolid;
import bluej.debugger.gentype.JavaType;
import bluej.debugger.gentype.Reflective;

/**
 * Utilities for dealing with reflection, which must behave differently for
 * Java 1.4 / 1.5. Use the factory method "getJavaUtils" to retrieve an object
 * to use. 
 *   
 * @author Davin McCall
 */
public abstract class JavaUtils
{
    private static JavaUtils jutils;
    
    /**
     * Factory method. Returns a JavaUtils object.
     * @return an object supporting the appropriate feature set
     */
    public static JavaUtils getJavaUtils()
    {
        if( jutils != null ) {
            return jutils;
        }
        
        jutils = new JavaUtils15();
        return jutils;
    }
    
    /**
     * Get a "signature" description of a method.
     * Looks like:  void method(int, int, int)
     *   (ie. excludes parameter names)
     * @param method The method to get the signature for
     * @return the signature string
     */
    public static String getSignature(Method method)
    {
        String name = getFQTypeName(method.getReturnType()).replace('$', '.') + " " + method.getName();
        Class<?>[] params = method.getParameterTypes();
        return makeSignature(name, params);
    }

    /**
     * Get a fully-qualified type name. For array types return the base type
     * name plus the appropriate number of "[]" qualifiers.
     */
    static public String getFQTypeName(Class<?> type)
    {
        Class<?> primtype = type;
        int dimensions = 0;
        while (primtype.isArray()) {
            dimensions++;
            primtype = primtype.getComponentType();
        }
        StringBuffer sb = new StringBuffer();
        sb.append(primtype.getName());
        for (int i = 0; i < dimensions; i++)
            sb.append("[]");
        return sb.toString();
    }

    /**
     * Build the signature string. Format: name(type,type,type)
     */
    private static String makeSignature(String name, Class<?>[] params)
    {
        StringBuffer sb = new StringBuffer();
        sb.append(name);
        sb.append("(");
        for (int j = 0; j < params.length; j++) {
            String typeName = getFQTypeName(params[j]).replace('$', '.');
            sb.append(typeName);
            if (j < (params.length - 1))
                sb.append(", ");
        }
        sb.append(")");
        return sb.toString();
    }

    /**
     * Get a "signature" description of a constructor.
     * Looks like:  ClassName(int, int, int)
     *   (ie. excludes parameter names)
     * @param cons the Constructor to get the signature for
     * @return the signature string
     */
    public static String getSignature(Constructor<?> cons)
    {
        String name = JavaNames.getBase(cons.getName());
        Class<?>[] params = cons.getParameterTypes();
        return makeSignature(name, params);
    }
 
    /**
     * Get a "short description" of a method. This is like the signature,
     * but substitutes the parameter names for their types.
     * 
     * @param method   The method to get the description of
     * @param paramnames  The parameter names of the method
     * @return The description.
     */
    abstract public String getShortDesc(Method method, String [] paramnames);

    /**
     * Get a "short description" of a method, and map class type parameters to
     * the given types. A short description is like the signature, but
     * substitutes the parameter names for their types. Generic method type
     * parameters are left unmapped.
     * 
     * @param method   The method to get the description of
     * @param paramnames The parameter names of the method
     * @param tparams  The map (String -> GenType) for class type parameters
     * @return The description.
     */
    abstract public String getShortDesc(Method method, String [] paramnames,
            Map<String,GenTypeParameter> tparams);

    /**
     * Get a long String describing the method. A long description is
     * similar to the short description, but it has type names and parameters
     * included.
     */
    abstract public String getLongDesc(Method method, String [] paramnames);
    
    /**
     * Get a long String describing the method, with class type parameters
     * mapped to their instantiation types. A long description is similar to a
     * short description, but it has type names of parameters included.
     * 
     * @param method   The method to get the description of
     * @param paramnames  The parameters names of the method
     * @param tparams  The map (String -> GenType) for class type parameters
     * @return The long description string.
     */
    abstract public String getLongDesc(Method method, String [] paramnames,
            Map<String,GenTypeParameter> tparams);
    
    /**
     * Get a "short description" of a constructor. This is like the signature,
     * but substitutes the parameter names for their types.
     * 
     * @param constructor   The constructor to get the description of
     * @return The description.
     */
    abstract public String getShortDesc(Constructor<?> constructor, String [] paramnames);
    
    /**
     * Get a long String describing the constructor. A long description is
     * similar to the short description, but it has type names and parameters
     * included.
     */
    abstract public String getLongDesc(Constructor<?> constructor, String [] paramnames);
    
    abstract public boolean isVarArgs(Constructor<?> cons);
    
    abstract public boolean isVarArgs(Method method);    
   
    abstract public boolean isSynthetic(Method method);
    
    abstract public boolean isEnum(Class<?> cl);
    
    /**
     * Get the return type of a method.
     */
    abstract public JavaType getReturnType(Method method);
    
    abstract public JavaType getRawReturnType(Method method);

    /**
     * Get the declared type of a field.
     */
    abstract public JavaType getFieldType(Field field);
    
    abstract public JavaType getRawFieldType(Field field);
    
    /**
     * Get a list of the type parameters for a generic method.
     * (return an empty list if the method is not generic).
     * 
     * @param method   The method fro which to find the type parameters
     * @return  A list of GenTypeDeclTpar
     */
    abstract public List<GenTypeDeclTpar> getTypeParams(Method method);
    
    /**
     * Get a list of the type parameters for a generic constructor.
     * (return an empty list if the method is not generic).
     * 
     * @param method   The method fro which to find the type parameters
     * @return  A list of GenTypeDeclTpar
     */
    abstract public List<GenTypeDeclTpar> getTypeParams(Constructor<?> cons);
    
    /**
     * Get a list of the type parameters for a class. Return an empty list if
     * the class is not generic.
     * 
     * @param cl the class
     * @return A List of GenTypeDeclTpar
     */
    abstract public List<GenTypeDeclTpar> getTypeParams(Class<?> cl);
    
    /**
     * Get the declared supertype of a class.
     */
    abstract public GenTypeClass getSuperclass(Class<?> cl);
    
    /**
     * Get a list of the interfaces directly implemented by the given class.
     * @param cl  The class for which to find the interfaces
     * @return    An array of interfaces
     */
    abstract public GenTypeClass [] getInterfaces(Class<?> cl);
    
    /**
     * Gets an array of nicely formatted strings with the types of the parameters.
     * Include the ellipsis (...) for a varargs method.
     * 
     * @param method The method to get the parameters for.
     */
    abstract public String[] getParameterTypes(Method method);
    
    /**
     * Get an array containing the argument types of the method.
     * 
     * In the case of a varargs method, the last argument will be an array
     * type.
     * 
     * @param method  the method whose argument types to get
     * @param raw     whether to return the raw versions of argument types
     * @return  the argument types
     */
    abstract public JavaType[] getParamGenTypes(Method method, boolean raw);
    
    /**
     * Gets an array of nicely formatted strings with the types of the parameters.
     * Include the ellipsis (...) for a varargs constructor.
     * 
     * @param constructor The constructor to get the parameters for.
     */
    abstract public String[] getParameterTypes(Constructor<?> constructor);
    
    /**
     * Get an array containing the argument types of the method.
     * 
     * In the case of a varargs method, the last argument will be an array
     * type.
     * 
     * @param method  the method whose argument types to get
     * @return  the argument types
     */
    abstract public JavaType[] getParamGenTypes(Constructor<?> constructor);

    /**
     * Build a JavaType structure from a "Class" object.
     */
    abstract public JavaType genTypeFromClass(Class<?> t);
    
    /**
     * Open a web browser to show the given URL. On Java 6+ we can use
     * the desktop integration functionality of the JDK to do this. On
     * prior versions we fall back to older methods.
     * 
     * @return true if successful
     */
    public boolean openWebBrowser(URL url)
    {
        // For now, do this via reflection so that BlueJ can be built
        // on Java 5 and earlier.
        
        try {
            Class<?> cl = Class.forName("java.awt.Desktop");
            Method m = cl.getMethod("isDesktopSupported", new Class[0]);
            Boolean result = (Boolean) m.invoke(null, (Object[]) null);
            if (result.booleanValue()) {
                // The Desktop abstraction is supported
                m = cl.getMethod("getDesktop", new Class[0]);
                Object desktop = m.invoke(null, (Object[]) null);
                
                // Invoke the browse method
                m = cl.getMethod("browse", new Class[] {URI.class});
                m.invoke(desktop, new Object[] {url.toURI()});
                return true;
            }
        }
        catch (ClassNotFoundException cnfe) {}
        catch (NoSuchMethodException nsme) {}
        catch (IllegalAccessException iae) {}
        catch (InvocationTargetException ite) {}
        catch (URISyntaxException use) {}
        return false;
    }
    
    /**
     * Change a list of type parameters (with bounds) into a map, which maps
     * the name of the parameter to its bounding type.
     * 
     * @param tparams   A list of GenTypeDeclTpar
     * @return          A map (String -> GenTypeSolid)
     */
    public static Map<String,GenTypeSolid> TParamsToMap(List<GenTypeDeclTpar> tparams)
    {
        Map<String,GenTypeSolid> rmap = new HashMap<String,GenTypeSolid>();
        for( Iterator<GenTypeDeclTpar> i = tparams.iterator(); i.hasNext(); ) {
            GenTypeDeclTpar n = i.next();
            rmap.put(n.getTparName(), n.getBound().mapTparsToTypes(rmap));
        }
        return rmap;
    }
    
    /**
     * Check whether a member of some container type can be accessed from another type
     * according to its modifiers.
     * 
     * @param container  The type containing the member to which access is being checked
     * @param accessor   The type trying to access the member
     * @param modifiers  The modifiers of the member
     * @param isStatic   True if the access is a in static context; false if not
     * @return  true if the access is allowed, false otherwise
     */
    public static boolean checkMemberAccess(Reflective container, Reflective accessor, int modifiers, boolean isStatic)
    {
        // Access from a static context can only access static members
        if (isStatic && !Modifier.isStatic(modifiers))
            return false;
        
        if (Modifier.isPublic(modifiers)) {
            return true;
        }
        
        String accessorName = accessor.getName();
        if (! Modifier.isPrivate(modifiers)) {
            String cpackage = JavaNames.getPrefix(container.getName());
            if (accessorName.startsWith(cpackage)
                    && accessorName.indexOf('.', cpackage.length() + 1) == -1) {
                // Classes are in the same package, and the member is not private: access allowed
                return true;
            }
        }
        
        // access class == container class, then access is always allowed
        if (accessorName.equals(container.getName())) {
            return true;
        }
        
        int dollarIndex = accessorName.lastIndexOf('$');
        if (dollarIndex != -1) {
            // Inner classes can access outer class members with outer class privileges
            Reflective outer = container.getRelativeClass(accessor.getName().substring(0, dollarIndex));
            isStatic |= accessor.isStatic();
            if (checkMemberAccess(container, outer, modifiers, isStatic)) {
                return true;
            }
        }
        
        List<Reflective> supers = accessor.getSuperTypesR();
        Set<String> done = new HashSet<String>();
        String cpackage = JavaNames.getPrefix(container.getName());
        while (! supers.isEmpty()) {
            Reflective r = supers.remove(0);
            if (done.add(r.getName())) {
                if (r.getName().equals(container.getName())) {
                    if (Modifier.isProtected(modifiers)) {
                        return true;
                    }
                    if (! Modifier.isPrivate(modifiers)) {
                        if (accessor.getName().startsWith(cpackage)
                                && accessor.getName().indexOf('.', cpackage.length() + 1) == -1) {
                            // Classes are in the same package, and the member is not private: access allowed
                            return true;
                        }
                    }
                }
            }
        }
        
        return false;
    }

    /**
     * Make a descriptive signature. This includes the method/constructor name (which may
     * be preceded by type parameters), and parameter types or names or types and names.
     * (The type is always substituted if the name is missing). 
     * 
     * @param name       The method/constructor name (including preceding
     *                          type parameters if any)
     * @param paramTypes   The parameter types
     * @param paramNames   The parameter names (may be null)
     * @param includeTypeNames   True if the parameter type should always be included
     * @param isVarArgs      True if the method is varargs (requires ellipsis insertion)
     */
    protected static String makeDescription(String name, String[] paramTypes, String[] paramNames, boolean includeTypeNames, boolean isVarArgs)
    {
        StringBuffer sb = new StringBuffer();
        sb.append(name);
        sb.append("(");
        for (int j = 0; j < paramTypes.length; j++) {
            boolean typePrinted = false;
            if (isVarArgs && j == paramTypes.length - 1) {
                if (includeTypeNames || paramNames == null || paramNames[j] == null) {
                    sb.append(paramTypes[j].substring(0, paramTypes[j].length() - 2));
                    sb.append(" ");
                }
                sb.append("...");
                typePrinted = true;
            }
            else if (includeTypeNames || paramNames == null || paramNames[j] == null) {                              
                sb.append(paramTypes[j]);
                typePrinted = true;
            }
            
            if (paramNames != null && paramNames[j] != null) {
                if (typePrinted)
                    sb.append(" ");
                sb.append(paramNames[j]);
            }
            if (j < (paramTypes.length - 1))
                sb.append(", ");
        }
        sb.append(")");
        return sb.toString();
    }
    
    /**
     * Convert a javadoc comment to a string with just the comment body, i.e. strip the
     * leading asterisks.
     */
    public static String javadocToString(String javadoc)
    {
        String eol = System.getProperty("line.separator");
        
        if (javadoc == null || javadoc.length() < 5) {
            return null;
        }
        
        StringBuffer outbuf = new StringBuffer();
        
        String str = javadoc.substring(3, javadoc.length() - 2);
        int nl = str.indexOf('\n');
        int cr = str.indexOf('\r');
        int pos = 0;
        while (nl != -1 || cr != -1) {
            int lineEnd = Math.min(nl, cr);
            lineEnd = (nl == -1) ? cr : lineEnd;
            lineEnd = (cr == -1) ? nl : lineEnd;
            
            String line = str.substring(pos, lineEnd);
            line = stripLeadingStars(line);
            
            outbuf.append(line);
            outbuf.append(eol);
            
            pos = lineEnd + 1;
            if (pos == nl) {
                pos++;
            }

            nl = str.indexOf('\n', pos);
            cr = str.indexOf('\r', pos);
        }
        
        String line = stripLeadingStars(str.substring(pos)).trim();
        if (line.length() > 0) {
            outbuf.append(line);
        }
        
        return outbuf.toString();
    }
    
    /**
     * Convert javadoc comment body (as extracted by javadocToString for instance)
     * to HTML suitable for display by HTMLEditorKit.
     */
    public static String javadocToHtml(String javadocString)
    {
        // find the first block tag
        int i;
        for (i = 0; i < javadocString.length(); i++) {
            // Here we are the start of the line
            while (i < javadocString.length() && Character.isWhitespace(javadocString.charAt(i))) {
                i++;
            }
            if (i >= javadocString.length() || javadocString.charAt(i) == '@') {
                break;
            }
            while (i < javadocString.length()
                    && javadocString.charAt(i) != '\n'
                    && javadocString.charAt(i) != '\r') {
                i++;
            }
        }
        
        // Process the block tags
        if (i < javadocString.length()) {
            String header = javadocString.substring(0, i);
            String blocksText = javadocString.substring(i);
            String[] lines = Utility.splitLines(blocksText);
            
            List<String> blocks = getBlockTags(lines);
            
            StringBuilder rest = new StringBuilder();
            StringBuilder params = new StringBuilder();
            params.append("<h3>Parameters</h3>").append("<table border=0>");
            
            for (String block : blocks) {
                if (block.startsWith("param ")) {
                    int p = "param".length();
                    // Find the parameter name
                    while (Character.isWhitespace(block.charAt(p))) {
                        p++;
                    }
                    int k = p;
                    while (k < block.length() && !Character.isWhitespace(block.charAt(k))) {
                        k++;
                    }
                    String paramName = block.substring(p, k);
                    String paramDesc = block.substring(k);
                    
                    params.append("<tr><td valign=\"top\">&nbsp;&nbsp;&nbsp;");
                    params.append(makeGreen(paramName));
                    params.append("</td><td>");
                    params.append(makeGreen(" - " + paramDesc));
                    params.append("</td></tr>");
                } else {
                    rest.append(convertBlockTag(block)).append("<br>");
                }
            }           
            
            params.append("</table><p>");
            
            String result = makeGreen(header) + params.toString() + rest.toString();
            return result;
        }
        
        return makeGreen(javadocString);
    }
    
    private static String makeGreen(String text)
    {
        return "<font color='green'>" + text + "</font>";
    }

    private static List<String> getBlockTags(String[] lines)
    {
        LinkedList<String> blocks = new LinkedList<String>();
        String cur = "";
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("@")) {
                if (false == cur.isEmpty()) {
                    blocks.addLast(cur);
                }
                cur = line.substring(1);
            } else {
                //If it doesn't start with an at, it's part of the previous tag
                cur += line + " ";
            }
        }
        blocks.addLast(cur);
        return blocks;
    }
    
    private static String convertBlockTag(String block)
    {        
        int k = 0;
        while (k < block.length() && !Character.isWhitespace(block.charAt(k)))
            k++;
        
        String r = "<b>" + block.substring(0, k) + "</b> - " + makeGreen(block.substring(k));
        
        return r;
    }
    
    /**
     * Strip leading asterisk characters (and any preceding whitespace) from a single
     * line of text.
     */
    private static String stripLeadingStars(String s)
    {
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '*') {
                do {
                    i++;
                } while (i < s.length() && s.charAt(i) == '*');
                s = s.substring(i);
                break;
            }
            if (! Character.isWhitespace(s.charAt(i))) {
                break;
            }
        }
        return s;
    }
}
