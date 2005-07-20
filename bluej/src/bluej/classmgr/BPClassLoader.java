package bluej.classmgr;

import java.io.File;
import java.io.UnsupportedEncodingException;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;


/**
 * A Bluej Project ClassLoader that can be used to load or obtain information about classes loadable in a bluej project.
 * Different projects have different class loaders since they shoule each have a well defined and unique namespace.
 * Every time a project is compiled, even when the compilation is started from the GUI, a new ProjectLoader is created and
 * if the Extension currently have a copy of the old one it should discard it.
 * Note: There is a name clash with ProjectClassLoader that should be deleted at the end of refactornig,
 * unfortunately ProjectClassLoader has different semantic and it would be unvise to break the current behaviour before
 * having a correct working version. This is the reason for this class being named BPClassLoader.
 * it will be renamed when the new classloading is refactored and tested.
 *
 * @version    $Id: BPClassLoader.java 3473 2005-07-20 18:00:29Z damiano $
 */

/*
 * Author: Damiano Bolla
 */
public final class BPClassLoader extends URLClassLoader {
    /**
     * Constructructor.
     * @param parent the parent loader that is searched first to resolve classes.
     * @param urls the list of jars and directory that are searched next.
     */
    public BPClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    /**
     * Create a string with this class path as a separated list of strings.
     * Note that a classpath to be used to start another local JVM cannot refer to a URL but to a local file.
     * It is therefore advisable to move as much as possible from a Classpath oriented vew to a ClassLoader.
     *
     * @return  The classpath as string.
     */
    public String getClassPathAsString() {
        URL[] urls = super.getURLs();

        if ((urls == null) || (urls.length < 1)) {
            return "";
        }

        boolean addSeparator = false; // Do not add a separator at the beginning
        StringBuffer buf = new StringBuffer();

        for (int index = 0; index < urls.length; index++) {
            if (addSeparator) {
                buf.append(File.pathSeparatorChar);
            }

            URL url = urls[index];

            // A class path is always without the qualifier file in front of it.
            // However some characters (such as space) are encoded.
            try {
                buf.append(URLDecoder.decode(url.getPath(), "UTF-8"));
            } catch (UnsupportedEncodingException uee) {
            }

            // From now on, you have to add a separator.
            addSeparator = true;
        }

        return buf.toString();
    }

    public String toString() {
        return "BPClassLoader path=" + getClassPathAsString();
    }
}
