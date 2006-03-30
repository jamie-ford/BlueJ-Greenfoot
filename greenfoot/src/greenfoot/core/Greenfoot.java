package greenfoot.core;

import greenfoot.GreenfootImage;
import greenfoot.ActorVisitor;
import greenfoot.event.CompileListener;
import greenfoot.event.CompileListenerForwarder;
import greenfoot.event.ActorInstantiationListener;
import greenfoot.gui.GreenfootFrame;

import java.io.*;
import java.rmi.RemoteException;
import java.util.*;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import rmiextension.wrappers.RBlueJ;
import rmiextension.wrappers.RPackage;
import rmiextension.wrappers.RProject;
import rmiextension.wrappers.event.RInvocationListener;
import bluej.Config;
import bluej.debugmgr.CallHistory;
import bluej.extensions.CompilationNotStartedException;
import bluej.extensions.PackageNotFoundException;
import bluej.extensions.ProjectNotOpenException;
import bluej.utility.FileUtility;
import bluej.utility.Utility;

/**
 * The main class for greenfoot. This is a singelton.
 * 
 * @author Poul Henriksen <polle@mip.sdu.dk>
 * @version $Id$
 */
public class Greenfoot implements ClassImageManager
{
    /** Name of the greenfoot package file that holds information specific to a package/project */
    public static final String GREENFOOT_PKG_NAME = "project.greenfoot";
    private static Greenfoot instance;
    private transient final static Logger logger = Logger.getLogger("greenfoot");

    private RBlueJ rBlueJ;
    private GreenfootFrame frame;
    private GProject project;
    private GPackage pkg;
    
    /** Map of class names to images */
    private Map classImages = new HashMap();
    
    /** Package properties for opened packages */
    private Map packageProperties = new HashMap();
    
    private CompileListenerForwarder compileListenerForwarder;
    private List compileListeners = new ArrayList();

    private ActorInstantiationListener instantiationListener;
    private List invocationListeners = new ArrayList();
    
    private CallHistory callHistory = new CallHistory();

    private Greenfoot(RBlueJ rBlueJ, final RPackage pkg)
    {
        instance = this;
        this.rBlueJ = rBlueJ;
        try {
            this.pkg = new GPackage(pkg);
            this.project = this.pkg.getProject();
            // Config is initialized in GreenfootLauncher
        }
        catch (RemoteException e) {
            e.printStackTrace();
        }
        catch (ProjectNotOpenException e) {
            e.printStackTrace();
        }

        ActorVisitor.setClassImageManager(this);
        
        //Threading avoids deadlock when classbrowser tries to instantiate
        // objects to get images. this is necessy because greenfoot is started
        // from BlueJ-VM which waits for this call to return.
        final GProject finalProject = project;
        Thread t = new Thread() {
            public void run()
            {
                long t1 = System.currentTimeMillis();
                prepareGreenfootProject(finalProject);
                try {
                    frame = new GreenfootFrame(Greenfoot.this.rBlueJ, finalProject);
                }
                catch (ProjectNotOpenException e2) {
                    e2.printStackTrace();
                }
                catch (RemoteException e2) {
                    e2.printStackTrace();
                }
                logger.info("Frame created");
                
                //We must wait for the frame to finish preparing:
                frame.waitForProjectOpen();
                frame.setVisible(true);
//                frame.toFront();
                Utility.bringToFront();
                logger.info("Frame visible");
                try {
                    instantiationListener = new ActorInstantiationListener(WorldHandler.instance());
                    Greenfoot.this.rBlueJ.addInvocationListener(instantiationListener);
                    compileListenerForwarder = new CompileListenerForwarder(compileListeners);
                    Greenfoot.this.rBlueJ.addCompileListener(compileListenerForwarder, pkg.getProject().getName());
                }
                catch (RemoteException e) {
                    e.printStackTrace();
                }
                catch (ProjectNotOpenException e) {
                    e.printStackTrace();
                }
                long t2 = System.currentTimeMillis();
                logger.info("Creation of frame took: " + (t2 - t1));
            }
        };
        SwingUtilities.invokeLater(t);
    }

    /**
     * Initializes the singleton. This can only be done once - subsequent calls
     * will have no effect.
     */
    public static void initialize(RBlueJ rBlueJ, RPackage pkg)
    {
        if (instance == null) {
            instance = new Greenfoot(rBlueJ, pkg);
            System.setProperty("apple.laf.useScreenMenuBar", "true");
        }
    }

    /**
     * Gets the singleton.
     *  
     */
    public static Greenfoot getInstance()
    {
        return instance;
    }

    /**
     * Opens a project in the given directory
     * 
     * @param projectDir
     */
    public void openProject(String projectDir)
        throws RemoteException
    {
        rBlueJ.openProject(projectDir);
    }

    /**
     * Opens a file browser to find a greenfoot project
     *  
     */
    public void openProjectBrowser()
    {
        File dirName = FileUtility.getPackageName(frame);

        if (dirName != null) {
            try {
                openProject(dirName.getAbsolutePath());
            }
            catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Gets the package for this.
     */
    public GPackage getPackage()
    {
        return pkg;
    }
    
    public GProject getProject() {
        return project;
    }
    
    

    /**
     * Closes this greenfoot frame
     * 
     * TODO This sometimes leaves proceses hanging? Seems to be fixed with later
     * BlueJ versions
     */
    public void closeThisInstance()
    {
        try {
            logger.info("closeThisInstance(): " + project.getName());
            rBlueJ.removeCompileListener(compileListenerForwarder);
            rBlueJ.removeInvocationListener(instantiationListener);
            for (Iterator iter = invocationListeners.iterator(); iter.hasNext();) {
                RInvocationListener element = (RInvocationListener) iter.next();
                rBlueJ.removeInvocationListener(element);
            }
            if (rBlueJ.getOpenProjects().length <= 1) {
                //Close everything
                //TODO maybe open dummy project instead
               
               
                //And then exit greenfoot
                logger.info("exit greenfoot");
                rBlueJ.exit();
            }
            else {
                logger.info("closing project: " + project.getName());
                project.close();
            }
        }
        catch (ProjectNotOpenException e) {
            e.printStackTrace();
        }
        catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds a listener for compile events
     * 
     * @param listener
     */
    public void addCompileListener(CompileListener listener)
    {
        compileListeners.add(listener);
    }
    
    /**
     * removes a listener for compile events
     * 
     * @param listener
     */
    public void removeCompileListener(CompileListener listener)
    {
        compileListeners.remove(listener);
    }

    /**
     * Adds a listener for invocation events
     * 
     * @param listener
     */
    public void addInvocationListener(RInvocationListener listener)
        throws RemoteException
    {
        invocationListeners.add(listener);
        rBlueJ.addInvocationListener(listener);
    }

    /**
     * Compiles all files 
     */
    public void compileAll()
    {
        try {
            // we recompile all files in ccase something has been modified
            // externally and the isCompiled state is no longer up-to-date.
            pkg.compileAll(false);
        }
        catch (ProjectNotOpenException e) {
            e.printStackTrace();
        }
        catch (PackageNotFoundException e) {
            e.printStackTrace();
        }
        catch (RemoteException e) {
            e.printStackTrace();
        }
        catch (CompilationNotStartedException e) {
            e.printStackTrace();
        }
    }

   

    /**
     * Creates a new project
     */
    public void newProject()
    {

        String newname = FileUtility.getFileName(frame, Config.getString("pkgmgr.newPkg.title"), Config
                .getString("pkgmgr.newPkg.buttonLabel"), false, null, true);
        if (newname != null) {
            try {
                RProject newProject = rBlueJ.newProject(new File(newname));
                GProject gProject = new GProject(newProject);
                prepareGreenfootProject(gProject);
            }
            catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Get a reference to the CallHistory instance.
     */
    public CallHistory getCallHistory()
    {
        return callHistory;
    }
    
    /**
     * Get a reference to the invocation listener.
     */
    public ActorInstantiationListener getInvocationListener()
    {
        return instantiationListener;
    }
    
    /**
     * Get a reference to the greenfoot frame.
     */
    public GreenfootFrame getFrame()
    {
        return frame;
    }

    /**
     * Retrieve the properties for a package. Loads the properties if
     * necessary.
     */
    public Properties getPackageProperties(GPackage pkg)
        throws RemoteException, PackageNotFoundException
    {
        try {
            String pkgName = pkg.getName();
            
            Properties p = (Properties) packageProperties.get(pkgName);
            if (p == null) {
                p = new Properties();
                File propsFile = new File(pkg.getDir(), GREENFOOT_PKG_NAME);
                try {
                    p.load(new FileInputStream(propsFile));
                }
                catch (IOException ioe) {}
                
                packageProperties.put(pkgName, p);
            }
            return p;
        }
        catch (ProjectNotOpenException pnoe) {
            // Won't happen
            return null;
        }
    }
    
    // --------- ClassImageManager interface ---------
        
    public GreenfootImage getClassImage(String className)
    {
        GreenfootImage image = (GreenfootImage) classImages.get(className);
        if (image == null) {
            // get the package the class is in
            GPackage classPackage = null;
            int lastDot = className.lastIndexOf('.');
            if (lastDot == -1) {
                classPackage = getPackage();
            }
            else {
                String pkgName = className.substring(0, lastDot);
                className = className.substring(lastDot + 1);
                try {
                    classPackage = getProject().getPackage(pkgName);
                    if (classPackage == null) {
                        return null;
                    }
                }
                catch (ProjectNotOpenException pnoe) {
                    pnoe.printStackTrace();
                }
                catch (RemoteException re) {
                    re.printStackTrace();
                }
            }
                        
            // Get the class, find the image
            GClass gClass = classPackage.getClass(className);
            if (gClass != null) {
                String imageName = gClass.getClassProperty("image");
                if (imageName != null) {
                    image = new GreenfootImage("images/" + imageName);
                    classImages.put(className, image);
                }
            }
        }
        return image;
    }
    
    /**
     * Remove the cached version of an image for a particular class. This should be
     * called when the image for the class is set to something different.
     */
    public void removeCachedImage(String className)
    {
        classImages.remove(className);
    }        
    
    // ========= Private methods ==========
    
    /**
     * Makes a project a greenfoot project. That is, copy the system classes to
     * the users library.
     * 
     * @param project
     */
    private void prepareGreenfootProject(GProject project)
    {
        try {
            File projectDir = project.getDir();

            if (isStartupProject(projectDir)) {
                logger.info("GreenfootLauncher: This is startupProject... ");
                return;
            }

            File blueJLibDir = rBlueJ.getSystemLibDir();
            File src = new File(blueJLibDir, "skeletonProject");
            File dst = projectDir;

            validateClassFiles(src, dst);
            copyDir(src, dst);
        }
        catch (ProjectNotOpenException e) {
            e.printStackTrace();
        }
        catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    /**
     * Checks whether the odl and new source files for Actor and
     * World are the same. If they are not, the class files are
     * deleted.
     */
    private void validateClassFiles(File src, File dst)
    {
        File newActor = new File(src, "greenfoot/Actor.java");
        File oldActor = new File(dst, "greenfoot/Actor.java");
        File actorClassFile= new File(dst, "greenfoot/Actor.class");
        
        File newWorld = new File(src, "greenfoot/World.java");
        File oldWorld = new File(dst, "greenfoot/World.java");
        File gwClassFile= new File(dst, "greenfoot/World.class");
        
        if(! sameFileContents(newActor, oldActor) || ! sameFileContents(newWorld, oldWorld)) {
            try {
                GPackage defaultPkg = project.getDefaultPackage();
                if(defaultPkg != null) {
                    defaultPkg.deleteClassFiles();
                }
                GPackage greenfootPkg = project.getGreenfootPackage();
                if(greenfootPkg != null) {
                    greenfootPkg.deleteClassFiles();
                }                
            }
            catch (ProjectNotOpenException e) {
                e.printStackTrace();
            }
            catch (RemoteException e) {
                e.printStackTrace();
            }            
        } 
    }

    private boolean sameFileContents(File f1, File f2)
    {
        if (f1.canRead() && f2.canRead()) {
            if (f1.length() != f2.length()) {
                return false;
            }
            try {
                FileReader reader1 = new FileReader(f1);
                FileReader reader2 = new FileReader(f2);
                int read1;

                try {
                    while ((read1 = reader1.read()) != -1) {
                        int read2 = reader2.read();
                        if (read1 != read2) {
                            return false;
                        }
                    }
                    return true;
                }
                catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            catch (FileNotFoundException e) {}
        }
        return false;
    }

    /**
     * Checks if the project is the default startup project that is used when no
     * other project is open. It is necessary to have this dummy project,
     * becuase we must have a project in order to launch the DebugVM.
     * 
     */
    private boolean isStartupProject(File projectDir)
    {
        try {
            File blueJLibDir = rBlueJ.getSystemLibDir();
            File startupProject = new File(blueJLibDir, "startupProject");
            if (startupProject.equals(projectDir)) {
                return true;
            }
        }
        catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 
     * Copies the src-DIR recursively into dst.
     * 
     * TODO make files read-only?
     * 
     * @param src
     * @param dst
     */
    private void copyDir(File src, File dst)
    {
        if (!src.isDirectory()) {
            return;
        }
        if (!dst.exists()) {
            dst.mkdirs();
        }
        File[] files = src.listFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            File newDst = new File(dst, file.getName());
            if (file.isDirectory()) {
                copyDir(file, newDst);
            }
            else {
                copyFile(file, newDst);
            }
        }
    }

    /**
     * Copies the src to dst. Creating parent dirs for dst. If dst exist it
     * overrides it.
     * 
     * @param src
     *            The source. It must be a file
     * @param dst
     *            Must not exist as a DIR
     */
    private void copyFile(File src, File dst)
    {
        if (!src.isFile() || dst.isDirectory()) {
            return;
        }
        dst.getParentFile().mkdirs();
        if (dst.exists()) {
            dst.delete();
        }
        try {
            BufferedInputStream is = new BufferedInputStream(new FileInputStream(src));
            BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(dst));

            byte[] buffer = new byte[8192];
            int read = 0;
            while (read != -1) {
                os.write(buffer, 0, read);
                read = is.read(buffer);
            }
            os.flush();
            is.close();
            os.close();
        }
        catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {

        }
    }
}