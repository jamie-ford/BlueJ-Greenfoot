package greenfoot;

import java.io.IOException;
import java.util.Random;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import greenfoot.core.KeyboardManager;
import greenfoot.core.Simulation;
import greenfoot.core.WorldHandler;
import greenfoot.sound.SoundPlayer;


/**
 * This utility class provides methods to control the simulation
 * and interact with the system.
 * 
 * <h3>Key names</h3>
 * 
 * <p>Part of the functionality provided by this class is the ability to
 * retrieve keyboard input. The methods getKey() and isKeyDown() are used
 * for this and they return/understand the following key names:
 * 
 * <ul>
 * <li>"a", "b", .., "z" (alphabetical keys), "0".."9" (digits), most
 *     punctuation marks. getKey() also returns uppercase characters when
 *     appropriate.
 * <li>"up", "down", "left", "right" (the cursor keys)
 * <li>"enter", "space", "tab", "escape"
 * <li>"F1", "F2", .., "F12" (the function keys)
 * </ul>
 * 
 * @author Davin McCall
 * @version 1.2.0
 * @cvs-version $Id: Greenfoot.java 5264 2007-10-03 15:45:29Z polle $
 */
public class Greenfoot
{
    
    private static Random randomGenerator = new Random();
    
    // Whether we have handled a LineUnavailableException
    private static boolean lineUnavailableHandled = false;

    private static boolean illegalArgumentHandled;

    private static boolean securityHandled;
    
    /**
     * Get the most recently pressed key, since the last time this method was
     * called. If no key was pressed since this method was last called, it
     * will return null. If more than one key was pressed, this returns only
     * the most recently pressed key.
     * 
     * @return  The name of the most recently pressed key
     */
    public static String getKey()
    {
        return WorldHandler.getInstance().getKeyboardManager().getKey();
    }
    
    /**
     * Check whether a given key is currently pressed down.
     * 
     * @param keyName  The name of the key to check
     * @return         True if the key is down
     */
    public static boolean isKeyDown(String keyName)
    {
        return WorldHandler.getInstance().getKeyboardManager().isKeyDown(keyName);
    }
    
    /**
     * This method will delay the current execution by a number of time steps. 
     * The size of one time step is defined by the Greenfoot environment (the speed slider).
     * 
     * @see #setSimulationSpeed(int)
     */
    public static void delay(int time)
    {
        for(int i=0; i < time; i++) {
            WorldHandler.getInstance().repaint();
            Simulation.getInstance().sleep();
        }
    }
    
    /**
     * Set the speed of the simulation execution.
     *  
     * @param speed  The new speed. the value must be in the range (1..100)
     */
    public static void setSimulationSpeed(int speed)
    {
        Simulation.getInstance().setSpeed(speed);
    }
    
    /**
     * Pause the simulation.
     */
    public static void stopSimulation()
    {
        Simulation.getInstance().setPaused(true);
    }
    
    /**
     * Run (or resume) the simulation.
     */
    public static void startSimulation()
    {
        Simulation.getInstance().setPaused(false);
    }
    
    /**
     * Return a random number between 0 (inclusive) and limit (exclusive).
     */
    public static int getRandomNumber(int limit) {
        return randomGenerator.nextInt(limit);
    }

    /**
     * Play sound from a file. The following formats are supported: AIFF, AU and
     * WAV.
     * <p>
     * 
     * The file name may be an absolute path, a base name for a file located in
     * the project directory or in the sounds directory of the project
     * directory.
     * 
     * @param filename Typically the name of a file in the sounds directory in
     *            the project directory.
     * @throws IllegalArgumentException If the sound can not be loaded.
     */
    public static void playSound(final String soundFile)
    {
        try {
            SoundPlayer.getInstance().play(soundFile);
        }
        catch (IOException e) {
            throw new IllegalArgumentException("Could not open sound file: " + soundFile, e);
        }
        catch (UnsupportedAudioFileException e) {
            throw new IllegalArgumentException("Format of sound file not supported: " + soundFile, e);
        }
        catch (SecurityException e) {
            // We only want to print this error message once.
            if(! securityHandled) {
                System.err.println("Could not play sound file due to security restrictions: " + soundFile); 
                System.err.println("If you have a sound card installed, check your system settings.");
                e.printStackTrace();
                securityHandled = true;
            }
        }
        catch (IllegalArgumentException e) {
            // We only want to print this error message once.
            if(! illegalArgumentHandled) {
                System.err.println("Could not play sound file: " + soundFile); 
                System.err.println("If you have a sound card installed, check your system settings.");
                e.printStackTrace();
                illegalArgumentHandled = true;
            }
        }
        catch (LineUnavailableException e) {
            // We only want to print this error message once.
            if(! lineUnavailableHandled) {
                System.err.println("Can not get access to the sound card. "
                    + "If you have a sound card installed, check your system settings, "
                    + "and close down any other programs that might be using the sound card.");
                e.printStackTrace();
                lineUnavailableHandled = true;
            }
        }
    }
}
