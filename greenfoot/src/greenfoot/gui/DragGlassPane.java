package greenfoot.gui;

import greenfoot.GreenfootObject;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.net.URL;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.SwingUtilities;

/**
 * Component that can be used for dragging. It should be used as a glasspane on
 * a JFrame. A drag is started with the startDrag() method. The drag will end
 * when the mouse is released and the component on that location get the
 * MouseEvent (mouseReleased)
 * 
 * Some of this is taken from:
 * http://java.sun.com/docs/books/tutorial/uiswing/components/example-1dot4/GlassPaneDemo.java
 * 
 * @author Poul Henriksen <polle@mip.sdu.dk>
 * @version $Id: DragGlassPane.java 3124 2004-11-18 16:08:48Z polle $
 *  
 */
public class DragGlassPane extends JComponent
    implements MouseMotionListener, MouseListener
{

    private transient final static Logger logger = Logger.getLogger("greenfoot");

    /** Singleton */
    private static DragGlassPane instance;

    /** The image displayed when dragging where no DropTarget is below */
    private ImageIcon image;
    private Icon noParkingIcon;

    /** Should the dragGlassPane display the no drop image? */
    private boolean paintNoDropImage;

    /** Rotation of the image */
    private double rotation;

    /** The object that is dragged */
    private Object data;

    /** Rectangles used for graphics update */
    private Rectangle oldRect = new Rectangle();
    private Rectangle rect = new Rectangle();

    /**
     * Keeps track of the last drop target, in order to send messages to old
     * drop targets when drag moves away from the component
     */
    private DropTarget lastDropTarget;

    /**
     * Event listener that captures all events and redispatches them to the
     * glasspane if they didn't originate from the glasspane. This fixes weird
     * Mac-bug where the glasspane never got mouse events when drag initiated
     * with Shift-buttton
     */
    private AWTEventListener eventListener = new AWTEventListener() {
        public void eventDispatched(AWTEvent event)
        {
            if (event.getSource() != DragGlassPane.this) {
                //translate mouseevent coordinate
                if (event instanceof MouseEvent) {
                    MouseEvent mouseEvent = (MouseEvent) event;
                    DragGlassPane.this.translateAndDispatchEvent(mouseEvent, (Component) mouseEvent.getSource(),
                            DragGlassPane.this);
                }

            }
        }
    };

    public static DragGlassPane getInstance()
    {
        if (instance == null) {
            instance = new DragGlassPane();

        }
        return instance;
    }

    private DragGlassPane()
    {
        //HACK this is a mac hack that is necessay because I can't get the
        // glasspane to grab the focus.
        Toolkit.getDefaultToolkit().addAWTEventListener(eventListener,
                (AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK));

        this.addMouseMotionListener(this);
        this.addMouseListener(this);
        this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        URL noParkingIconFile = this.getClass().getClassLoader().getResource("noParking.png");
        if (noParkingIconFile != null) {
            noParkingIcon = new ImageIcon(noParkingIconFile);
        }
    }

    public void paintComponent(Graphics g)
    {
        if (image != null && paintNoDropImage) {
            Graphics2D g2 = (Graphics2D) g;

            int width = rect.width;
            int height = rect.height;

            double halfWidth = width / 2.;
            double halfHeight = height / 2.;

            double rotateX = halfWidth + rect.getX();
            double rotateY = halfHeight + rect.getY();
            g2.rotate(Math.toRadians(rotation), rotateX, rotateY);
            image.paintIcon(this, g2, rect.x, rect.y);

            g2.setColor(Color.RED);
            if (noParkingIcon != null) {
                int x = (int) (rect.getX() + halfWidth - noParkingIcon.getIconWidth() / 2);
                int y = (int) (rect.getY() + halfHeight - noParkingIcon.getIconHeight() / 2);
                noParkingIcon.paintIcon(this, g2, x, y);
            }
        }
        else {
            //we do nothing - a DropTarget should have handled this
        }
    }

    private Rectangle getClip()
    {
        int width = rect.width;
        int height = rect.height;
        int diag = (int) Math.ceil(Math.sqrt(width * width + height * height));
        int widthDiff = (diag - width) / 2;
        int heightDiff = (diag - height) / 2;
        Rectangle oldClip = new Rectangle(oldRect.x - widthDiff, oldRect.y - heightDiff, diag, diag);
        Rectangle newClip = new Rectangle(rect.x - widthDiff, rect.y - heightDiff, diag, diag);
        return oldClip.union(newClip);
    }

    /**
     * Initiates a drag
     * 
     * @param image
     *            The image that should be dragged
     * @param rotation
     *            Rotation of the image
     * @param object
     *            An optional object.
     */
    public void startDrag(GreenfootObject object)
    {
        if (object == null || object.getImage() == null) {
            return;
        }
        setDragImage(object.getImage(), object.getRotation());
        setDragObject(object);
        paintNoDropImage = false;
        setVisible(true);
        logger.info("DragGlassPane.startDrag begin: " + this);
    }

    /**
     * Call this method when the drag should be ended.
     *  
     */
    public void endDrag()
    {
        logger.info("DragGlassPane.endDrag: " + this);
        if (lastDropTarget != null) {
            lastDropTarget.dragEnded(data);
        }
        data = null;
        image = null;
        setVisible(false);
    }

    /**
     * Sets the image to be dragged around
     * 
     * @param image
     *            The image
     * @param rotation
     *            The rotation of the image
     */
    public void setDragImage(ImageIcon image, double rotation)
    {
        this.image = image;
        int width = image.getIconWidth();
        int height = image.getIconHeight();
        rect.width = width;
        rect.height = height;
        oldRect.width = width;
        oldRect.height = height;
        this.rotation = rotation;
    }

    public void setDragObject(Object object)
    {
        this.data = object;
    }

    public Object getDragObject()
    {
        return data;
    }

    private void move(MouseEvent e)
    {
        //logger.info("DragGlassPane.move" + e.paramString());
        oldRect.x = rect.x;
        oldRect.y = rect.y;
        storePosition(e);
        paintNoDropImage = true;
        Component destination = getComponentBeneath(e);
        DropTarget dropTarget = null;
        if (destination != null && destination instanceof DropTarget) {
            dropTarget = (DropTarget) destination;

            Point p = SwingUtilities.convertPoint(this, e.getPoint(), destination);
            if (dropTarget.drag(data, p)) {
                paintNoDropImage = false;
            }
        }

        if (lastDropTarget != null && dropTarget != lastDropTarget) {
            lastDropTarget.dragEnded(data);
        }
        lastDropTarget = dropTarget;

        if (isVisible()) {
            repaint(getClip());
        }
    }

    public void mouseMoved(MouseEvent e)
    {
        move(e);
    }

    public void mouseDragged(MouseEvent e)
    {
        move(e);
    }

    public void mouseClicked(MouseEvent e)
    {}

    public void mouseEntered(MouseEvent e)
    {}

    public void mouseExited(MouseEvent e)
    {}

    public void mousePressed(MouseEvent e)
    {}

    public void mouseReleased(MouseEvent e)
    {
        Component destination = getComponentBeneath(e);

        if (destination != null && destination instanceof DropTarget) {
            DropTarget dropTarget = (DropTarget) destination;
            Point destinationPoint = SwingUtilities.convertPoint(this, e.getPoint(), destination);
            Object tmpData = data;
            endDrag();
            dropTarget.drop(tmpData, destinationPoint);
        }

    }

    private Component getComponentBeneath(MouseEvent e)
    {
        JFrame frame = (JFrame) SwingUtilities.getRoot(this);
        if (frame == null) {
            return null;
        }
        Container contentPane = frame.getContentPane();

        Component glassPane = this;
        JMenuBar menuBar = frame.getJMenuBar();

        Point glassPanePoint = e.getPoint();
        Container container = contentPane;
        Point containerPoint = SwingUtilities.convertPoint(glassPane, glassPanePoint, contentPane);
        if (containerPoint.y < 0) { //we're not in the content pane
            if (containerPoint.y + menuBar.getHeight() >= 0) {
                //The mouse event is over the menu bar.
                //Could handle specially.
            }
            else {
                //The mouse event is over non-system window
                //decorations, such as the ones provided by
                //the Java look and feel.
                //Could handle specially.
            }
        }
        else {
            //The mouse event is probably over the content pane.
            //Find out exactly which component it's over.
            Component destination = SwingUtilities.getDeepestComponentAt(container, containerPoint.x, containerPoint.y);
            return destination;
        }
        return null;
    }

    /**
     * Translates the coordinates of the MouseEvent to destination component
     * coordinates. Then, it creates a new event and dispatches it.
     * 
     * @param e
     * @param source
     * @param destination
     */
    private void translateAndDispatchEvent(MouseEvent e, Component source, Component destination)
    {
        if ((destination != null)) {
            Point componentPoint = SwingUtilities.convertPoint(source, e.getPoint(), destination);
            destination.dispatchEvent(new MouseEvent(destination, e.getID(), e.getWhen(), e.getModifiers(),
                    componentPoint.x, componentPoint.y, e.getClickCount(), e.isPopupTrigger()));
        }
    }

    private void storePosition(MouseEvent e)
    {
        MouseEvent eThis = SwingUtilities.convertMouseEvent((Component) e.getSource(), e, this);
        rect.x = eThis.getX();
        rect.y = eThis.getY();
    }
}