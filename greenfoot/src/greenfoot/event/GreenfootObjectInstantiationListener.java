package greenfoot.event;

import greenfoot.GreenfootObject;
import greenfoot.WorldHandler;
import greenfoot.gui.DragGlassPane;

import java.rmi.RemoteException;

import rmiextension.ObjectTracker;
import rmiextension.wrappers.RObject;
import rmiextension.wrappers.event.RInvocationEvent;
import rmiextension.wrappers.event.RInvocationListenerImpl;

/**
 * Listens for new instances of GrenfootObjects
 * 
 * @author Poul Henriksen <polle@mip.sdu.dk>
 * @version $Id: GreenfootObjectInstantiationListener.java,v 1.6 2004/11/18
 *          09:43:52 polle Exp $
 */
public class GreenfootObjectInstantiationListener extends RInvocationListenerImpl
{
    private WorldHandler worldHandler;
    
    public GreenfootObjectInstantiationListener(WorldHandler worldHandler)
        throws RemoteException
    {
        super();
        this.worldHandler = worldHandler;
    }

    public void invocationFinished(RInvocationEvent event)
        throws RemoteException
    {
        Object result = event.getResult();

        if (result == null || !(result instanceof RObject)) {
            return;
        }

        RObject remoteObj = (RObject) result;

        if (event.getMethodName() != null) {
            //this is not a call from a constructor
            return;
        }

        Object realObject = ObjectTracker.instance().getRealObject(remoteObj);
        if (realObject instanceof GreenfootObject) {
            GreenfootObject go = (GreenfootObject) realObject;
            int xoffset = -go.getImage().getIconWidth() / 2;
            int yoffset = -go.getImage().getIconHeight() / 2;
            DragGlassPane.getInstance().startDrag(go, xoffset, yoffset, worldHandler);
        }
    }

}