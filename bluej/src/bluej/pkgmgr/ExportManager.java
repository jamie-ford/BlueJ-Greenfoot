package bluej.pkgmgr;

import java.util.*;
import java.util.jar.*;
import java.util.zip.*;
import java.io.*;

import bluej.Config;
import bluej.utility.Debug;
import bluej.utility.DialogManager;
import bluej.utility.FileUtility;
import bluej.utility.BlueJFileReader;

/**
 * Component to manage exporting projects to standard Java format.
 * The format can be either a directory tree or a jar file.
 *
 * @author  Michael Kolling
 * @version $Id: ExportManager.java 636 2000-07-07 05:03:00Z mik $
 */
final class ExportManager
{
    private PkgMgrFrame frame;

    public ExportManager(PkgMgrFrame frame)
    {
        this.frame = frame;
    }

    /**
     * Envoke the "export" user function. This starts by displaying the
     * export dialog, then it calls the appropriate export implementation
     * function (jar or directory).
     */
    public void export()
    {
        ExportDialog dialog = new ExportDialog(frame);
        boolean okay = dialog.display();

        if(!okay)
            return;

        String newName;
        if(dialog.saveAsJar())
            newName = FileUtility.getFileName(frame,
                                        "Specify name for jar file",
                                        "Create",
                                        false);
        else
            newName = FileUtility.getFileName(frame,
                                        "Specify name for export directory",
                                        "Create",
                                        false);
        if(newName == null)
            return;

        String sourceDir = frame.getProject().getProjectDir().getPath();

        if(dialog.saveAsJar())
            exportJar(sourceDir, newName, dialog.getMainClass(),
                      dialog.includeSource());
        else
            exportDir(sourceDir, newName, dialog.getMainClass(),
                      false, dialog.includeSource(), "error-exporting");
    }

    public void saveAs(String sourceDir, String destDir)
    {
        exportDir(sourceDir, destDir, null, true, true, "cannot-copy-package");
    }

    /**
     * Export this project to a jar file.
     */
    private void exportJar(String sourceDir, String fileName,
                           String mainClass, boolean includeSource)
    {
        if(!fileName.endsWith(".jar"))
           fileName = fileName + ".jar";

        File jarFile = new File(fileName);
        if(jarFile.exists()) {
            DialogManager.showError(frame, "directory-exists");
            return;
        }

        OutputStream oStream = null;
        JarOutputStream jStream = null;

        try {
            // create manifest
            Manifest manifest = new Manifest();
            Attributes attr = manifest.getMainAttributes();
            attr.put(Attributes.Name.MANIFEST_VERSION, "1.0");
            attr.put(Attributes.Name.MAIN_CLASS, mainClass);

            // create jar file
            oStream = new FileOutputStream(jarFile);
            if(mainClass != null && mainClass.length() > 0)
                jStream = new JarOutputStream(oStream, manifest);
            else
                jStream = new JarOutputStream(oStream);

            // write jar entries from source directory
            File srcFile = new File(sourceDir);
            String[] dir = srcFile.list();
            for(int i=0; i<dir.length; i++) {
                String srcName = sourceDir + File.separator + dir[i];
                File file = new File(srcName);
                if(file.isDirectory()) {
                    ; // ignore for now
                }
                else {
                    if(!FileUtility.skipFile(dir[i], true, !includeSource)) {
                        writeJarEntry(file, jStream, dir[i]);
                    }
                }
            }
            frame.setStatus(Config.getString("pkgmgr.exported.jar"));
        }
        catch(IOException exc) {
            DialogManager.showError(frame, "error-writing-jar");
            Debug.reportError("problen writing jar file: " + exc);
        } finally {
            try {
                if(jStream != null)
                    jStream.close();
            } catch (IOException e) {}
        }
    }

    /**
     * Write a jar file entry to the jar output stream.
     */
    private void writeJarEntry(File file, JarOutputStream jStream,
                                  String entryName)
        throws IOException
    {
        InputStream in = null;
        try {
            in = new FileInputStream(file);
            jStream.putNextEntry(new ZipEntry(entryName));
            FileUtility.copyStream(in, jStream);
        }
        //catch(IOException exc) {
        finally {
            if(in != null)
                in.close();
        }
    }

    /**
     * Export this project to a directory.
     */
    private void exportDir(String sourceDir, String destDir, String mainClass,
                           boolean includeBlueJ, boolean includeSource,
                           String errorMessage)
    {
        int result = FileUtility.copyDirectory(sourceDir, destDir,
                                               !includeBlueJ, !includeSource);
        switch(result) {
        case FileUtility.NO_ERROR:
            break;
        case FileUtility.DEST_EXISTS:
            DialogManager.showError(frame, "directory-exists");
            return;
        case FileUtility.SRC_NOT_DIRECTORY:
        case FileUtility.COPY_ERROR:
            DialogManager.showError(frame, errorMessage);
            return;
        }
        writeReadMe(destDir, mainClass);
        frame.setStatus(Config.getString("pkgmgr.exported"));
    }

    /**
     * If a main class was specified, add information to the readme
     * file about how to start the application.
     */
    private void writeReadMe(String dir, String mainClass)
    {
        if(mainClass == null || mainClass.length() == 0)
            return;

        try {
            // copy README to tmp file
            String readMePath = dir + File.separator + Package.readmeName;
            File readMe = new File(readMePath);
            File tmp = File.createTempFile("bluej", "txt");
            FileUtility.copyFile(readMe, tmp);

            // write template to README
            Hashtable translations = new Hashtable();
            translations.put("MAINCLASS", mainClass);

            String templateName = 
                Config.getLibFilename("template.readme.export");
            BlueJFileReader.translateFile(templateName, readMePath,
                                          translations);
            // append original README
            InputStream in = new BufferedInputStream(new FileInputStream(tmp));
            OutputStream out = new BufferedOutputStream(
                new FileOutputStream(readMePath, true));
            FileUtility.copyStream(in, out);
            in.close();
            out.close();
        } catch(IOException e) {
            DialogManager.showError(frame, "error-writing-readme");
            Debug.reportError("README file could not be updated. " + e);
        }
    }

}
