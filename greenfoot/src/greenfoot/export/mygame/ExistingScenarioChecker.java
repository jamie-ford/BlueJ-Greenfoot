package greenfoot.export.mygame;

import java.io.IOException;
import java.net.UnknownHostException;

import javax.swing.SwingUtilities;

import bluej.utility.SwingWorker;
import greenfoot.export.WebPublisher;

/**
 * Class that can be used to check whether a scenario already exists on the
 * publish site. Asynchronously.
 * 
 * @author Poul Henriksen
 * 
 */
public abstract class ExistingScenarioChecker
{
    /** Worker to handle asynchronously checking of existing scenario */
    private SwingWorker worker;

    /** Are we in the process of checking? */
    private boolean checking = false;

    /** Indicates that the user wants to abort checking for this scenario. */
    private boolean abort = false;

    /** Indicates that the checking has finished. */
    private boolean finished = false;

    private String hostName;
    private String userName;
    private String scenarioName;

    class ScenarioWorker extends SwingWorker
    {
        @Override
        public Object construct()
        {
            return checkExistence(hostName, userName, scenarioName);
        }

        @Override
        public void finished()
        {
            workerFinished(getValue());
        }
    }

    /**
     * Will start a thread that checks whether a scenario with the given name
     * exists for the given user. When a result is ready the method
     * scenarioExistenceChecked will be called. If it is already checking for
     * the existence of another scenario name, that check will be aborted and
     * this check will start. If it is already checking for a scenario with that
     * name it will just return and continue that checking.
     * 
     * @param scenarioName
     * @param forceRecheck 
     */
    public synchronized void startScenarioExistenceCheck(final String hostName, final String userName,
            final String scenarioName, boolean forceRecheck)
    {
        boolean sameScenario = hostName.equals(this.hostName) && userName.equals(this.userName)
                && scenarioName.equals(this.scenarioName);
        if (sameScenario && !forceRecheck) {
            // Scenario already checked, but make sure finished is invoked to
            // update status (continue button)
            SwingUtilities.invokeLater(new Runnable() {
                public void run()
                {
                    worker.finished();
                }
            });
            return;
        }
        if (checking) {
            abort();
        }

        this.hostName = hostName;
        this.userName = userName;
        this.scenarioName = scenarioName;

        checking = true;
        abort = false;
        finished = false;
        worker = new ScenarioWorker();
        worker.start();
    }

    /**
     * Will abort the checking.
     * 
     * @return True if successful abort, false if we didn't manage to abort
     *         (because it already finished the check)
     * @throws IllegalStateException If the check has not started yet.
     */
    public synchronized boolean abort()
    {
        // pre: is checking
        // should sync with calling the hook methods, so that they do not get
        // called after this method has been called.
        if (finished) {
            return false;
        }

        if (!checking) {
            throw new IllegalStateException("Check not started yet. Nothing to abort.");
        }

        abort = true;
        worker.interrupt();
        return true;
    }

    /**
     * Blocks until the result is ready. TODO: This can block forever if the
     * server never responds?
     * 
     * @return An Exception if an error occurred, or null if the scenario does
     *         not exist, or a ScenarioInfo object if the scenario exists.
     */
    public Object getResult()
    {
        synchronized (this) {
            if (worker == null) {
                throw new IllegalStateException("Check not started yet. No result to get.");
            }
        }
        return worker.get();
    }

    /**
     * Method that will be called when the check has finished.
     * 
     * This will execute on the swing event thread.
     * 
     * @param info If the scenario exists info about it will be returned. If it
     *            does not exist it will return null.
     */
    public abstract void scenarioExistenceChecked(ScenarioInfo info);

    /**
     * Method that will be called if a check fails. This can be because if a
     * network error or other things. This will execute on the swing event
     * thread.
     * 
     * @param reason
     */
    public abstract void scenarioExistenceCheckFailed(Exception reason);

    /**
     * Checks the existence of the given scenario.
     */
    private Object checkExistence(final String hostName, final String userName, final String scenarioName)
    {
        WebPublisher client = new WebPublisher();
        ScenarioInfo info = new ScenarioInfo();
        Exception exception = null;
        try {
            if (client.checkExistingScenario(hostName, userName, scenarioName, info)) {
                return info;
            }
            else {
                return null;
            }

        }
        catch (UnknownHostException e) {
            exception = e;
        }
        catch (IOException e) {
            exception = e;
        }
        catch (InterruptedException e) {
            // We were probably interrupted for aborting
        }

        return exception;
    }

    /**
     * Called when the worker has finished and the result is ready to be
     * processed.
     * 
     * @param value
     */
    private synchronized void workerFinished(Object value)
    {
        finished = true;
        checking = false;

        if (!abort) {
            if (value instanceof Exception) {
                scenarioExistenceCheckFailed((Exception) value);
            }
            else {
                scenarioExistenceChecked((ScenarioInfo) value);
            }
        }
    }
}
