package bluej.pkgmgr;

import java.io.*;
import java.util.*;

import bluej.utility.JavaNames;

/**
 * Utility functions to help in the process of importing directory
 * structures into BlueJ.
 *
 * @author  Michael Cahill
 * @author  Michael Kolling
 * @author  Axel Schmolitzky
 * @author  Andrew Patterson
 * @version $Id: Import.java 1700 2003-03-13 03:34:20Z ajp $
 */
class Import
{
    /**
     * Find all directories under a certain directory which
     * we deem 'interesting'.
     * An interesting directory is one which either contains
     * a java source file or contains a directory which in
     * turn contains a java source file.
     *
     * @param   dir     the directory to look in
     * @returns         a list of File's representing the
     *                  interesting directories
     */
    public static List findInterestingDirectories(File dir)
    {
        List interesting = new LinkedList();

        File[] files = dir.listFiles();

        if (files == null)
            return interesting;

        boolean imInteresting = false;

        for (int i=0; i<files.length; i++) {
            if (files[i].isDirectory()) {
                // if any of our sub directories are interesting
                // then we are interesting
                // we ensure that the subdirectory would have
                // a valid java package name before considering
                // anything in it
                if(JavaNames.isIdentifier(files[i].getName())) {
                    List subInteresting = findInterestingDirectories(files[i]);

                    if (subInteresting.size() > 0) {
                        interesting.addAll(subInteresting);
                        imInteresting = true;
                    }
                }
            }
            else {
                if (files[i].getName().endsWith(".java"))
                    imInteresting = true;
            }
        }

        // if we have found anything of interest (either a java
        // file or a subdirectory with java files) then we consider
        // ourselves interesting and add ourselves to the list
        if (imInteresting)
            interesting.add(dir);

        return interesting;
    }

    /**
     * Find all Java files contained in a list of
     * directory paths.
     */
    public static List findJavaFiles(List dirs)
    {
        List interesting = new LinkedList();

        Iterator it = dirs.iterator();

        while(it.hasNext()) {
            File dir = (File) it.next();

            File[] files = dir.listFiles();

            if (files == null)
                continue;

            for (int i=0; i<files.length; i++) {
                if (files[i].isFile() && files[i].getName().endsWith(".java")) {
                    interesting.add(files[i]);
                }
            }
        }

        return interesting;
    }

    /**
     * Convert an existing directory structure to one
     * that BlueJ can open as a project.
     */
    public static void convertDirectory(List dirs)
    {
        // create a bluej.pkg file in every directory that
        // we have determined to be interesting

        Iterator i = dirs.iterator();

        while(i.hasNext()) {
            File f = (File) i.next();

            File bluejFile = new File(f, Package.pkgfileName);

            if (bluejFile.exists())
                continue;

            try {
                bluejFile.createNewFile();
            }
            catch(IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
}
