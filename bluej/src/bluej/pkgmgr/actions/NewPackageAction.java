package bluej.pkgmgr.actions;

import bluej.pkgmgr.PkgMgrFrame;

/**
 * "New Package" command. Allows the user to create a new sub-package with a
 * specified name.
 * 
 * @author Davin McCall
 * @version $Id: NewPackageAction.java 2873 2004-08-16 05:50:32Z davmac $
 */
final public class NewPackageAction extends PkgMgrAction {
    
    static private NewPackageAction instance = null;
    
    /**
     * Factory method. This is the way to retrieve an instance of the class,
     * as the constructor is private.
     * @return an instance of the class.
     */
    static public NewPackageAction getInstance()
    {
        if(instance == null)
            instance = new NewPackageAction();
        return instance;
    }
    
    private NewPackageAction()
    {
        super("menu.edit.newPackage");
    }
    
    public void actionPerformed(PkgMgrFrame pmf)
    {
        pmf.menuCall();
        pmf.doCreateNewPackage();
    }
}
