package bluej.debugger.jdi;

import com.sun.jdi.*;
import com.sun.jdi.event.*;

/**
 * Event handler class to handle events coming from the remote VM.
 *
 * @author  Michael Kolling
 * @version $Id: VMEventHandler.java 2378 2003-11-24 02:25:58Z ajp $
 */
class VMEventHandler implements Runnable
{
	final static String DONT_RESUME = "dontResume";
	 
    private Thread thread;
    private VMReference vm;
    private EventQueue queue;
    
    volatile boolean exiting = false;

    VMEventHandler(VMReference vm, VirtualMachine vmm)
    {
        this.vm = vm;
        queue = vmm.eventQueue();
        thread = new Thread(this, "vm-event-handler");
        thread.start();  // will execute our own run method
    }

    public void run()
    {
        while (!exiting) {
            try {
            	// wait for the next event
                EventSet eventSet = queue.remove();

				// From the JDK documentation
				// The events that are grouped in an EventSet are restricted in the following ways:
				//   * Always singleton sets:
				//		 o VMStartEvent
				//		 o VMDisconnectEvent 
				//   * Only with other VMDeathEvents:
				//		 o VMDeathEvent 
				//   * Only with other ThreadStartEvents for the same thread:
				//		 o ThreadStartEvent 
				//   * Only with other ThreadDeathEvents for the same thread:
				//		 o ThreadDeathEvent 
				//   * Only with other ClassPrepareEvents for the same class:
				//		 o ClassPrepareEvent 
				//   * Only with other ClassUnloadEvents for the same class:
				//		 o ClassUnloadEvent 
				//   * Only with other AccessWatchpointEvents for the same field access:
				//		 o AccessWatchpointEvent 
				//   * Only with other ModificationWatchpointEvents for the same field modification:
				//		 o ModificationWatchpointEvent 
				//   * Only with other ExceptionEvents for the same exception occurrance:
				//		 o ExceptionEvent 
				//   * Only with other MethodExitEvents for the same method exit:
				//		 o MethodExitEvent 
				//   * Only with other members of this group, at the same location and in the same thread:
				//		 o BreakpointEvent
				//		 o StepEvent
				//		 o MethodEntryEvent 

                boolean addToSuspendCount = false;

				// iterate through all events in the set
                EventIterator it = eventSet.eventIterator();
                
                while (it.hasNext()) {
                	Event ev = it.nextEvent();
                	
					// do some processing with this event
					// this calls back into VMReference
                    handleEvent(ev);

					// for breakpoint and step events, we may want
					// to leave the relevant thread suspended. If the dontResume
					// property for the event is set, then lets do this.
					if(ev.request() != null) {
						if(ev.request().getProperty(DONT_RESUME) != null) {
							addToSuspendCount = true;
						}
					}

					if (addToSuspendCount) {
						// we ensure that the thread will stay suspended
						// by incrementing its suspend count (to counter
						// the resume() that will be executed in eventSet.resume())
						// we may have a case where multiple Break, Step events
						// occur at the same line (in the same EventSet).
						// use of the addToSuspendCount variable ensures that
						// we will still only add 1 to the suspend count					
						if(ev instanceof LocatableEvent) {
							LocatableEvent le = (LocatableEvent) ev;
							le.thread().suspend();
						}
					}
                }

				// resume the VM
            	eventSet.resume();
            }
            catch (InterruptedException exc) { }
            catch (VMDisconnectedException discExc) { exiting = true; }
        }
    }

    private void handleEvent(Event event)
    {
    	if (event instanceof VMStartEvent) {
    		vm.vmStartEvent((VMStartEvent) event);
    	} else if (event instanceof VMDeathEvent) {
			vm.vmExitEvent();
		} else if (event instanceof VMDisconnectEvent) {
			vm.vmDisconnectEvent();
    	} else if (event instanceof ExceptionEvent) {
			vm.exceptionEvent((ExceptionEvent)event);
        } else if (event instanceof BreakpointEvent) {
			vm.breakpointEvent((LocatableEvent)event, true);
        } else if (event instanceof StepEvent) {
			vm.breakpointEvent((LocatableEvent)event, false);
        } else if (event instanceof ThreadStartEvent) {
			vm.threadStartEvent((ThreadStartEvent)event);
        } else if (event instanceof ThreadDeathEvent) {
			vm.threadDeathEvent((ThreadDeathEvent)event);
        } else if (event instanceof ClassPrepareEvent) {
            classPrepareEvent(event);
        } else {
			//Debug.message("[VM event] unhandled: " + event);
        }
    }

    private boolean classPrepareEvent(Event event)
    {
		ClassPrepareEvent cle = (ClassPrepareEvent)event;
		ReferenceType refType = cle.referenceType();
	
		if(refType.name().equals(VMReference.SERVER_CLASSNAME)) {
		    vm.serverClassPrepared();
		}
		return true;
    }

}
