package bluej.pkgmgr.graphPainter;

import java.awt.*;
import java.awt.Graphics2D;

import bluej.Config;
import bluej.pkgmgr.target.*;
import bluej.pkgmgr.target.PackageTarget;
import bluej.prefmgr.PrefMgr;
import bluej.utility.Utility;

/**
 * Paints a packageTarget
 * 
 * @author fisker
 * @version $Id: PackageTargetPainter.java 4257 2006-05-14 16:38:01Z davmac $
 */
public class PackageTargetPainter
{
    private static final int TAB_HEIGHT = 12;
    private static final int HANDLE_SIZE = 20;

    private static final Color defaultbg = Config.getItemColour("colour.package.bg.default");
    private static final Color bordercolour = Config.getItemColour("colour.target.border");

    private static final int TEXT_HEIGHT = GraphPainterStdImpl.TEXT_HEIGHT;
    private static final int TEXT_BORDER = GraphPainterStdImpl.TEXT_BORDER;
    private static final Color[] shadowColours = GraphPainterStdImpl.shadowColours;
    private static final AlphaComposite alphaComposite = GraphPainterStdImpl.alphaComposite;
    private static Composite oldComposite;
    private int tabWidth;

    /**
     *  
     */
    public PackageTargetPainter()
    {
    }

    public void paint(Graphics2D g, Target target, boolean hasFocus)
    {
        PackageTarget packageTarget = (PackageTarget) target;
        g.translate(packageTarget.getX(), packageTarget.getY());
        int width = packageTarget.getWidth();
        int height = packageTarget.getHeight();
        drawUMLStyle(g, packageTarget, hasFocus, width, height);
        g.translate(-packageTarget.getX(), -packageTarget.getY());
    }

    public void paintGhost(Graphics2D g, Target target, boolean hasFocus)
    {
        PackageTarget packageTarget = (PackageTarget) target;
        oldComposite = g.getComposite();
        g.translate(packageTarget.getGhostX(), packageTarget.getGhostY());
        int width = packageTarget.getGhostWidth();
        int height = packageTarget.getGhostHeight();

        g.setComposite(alphaComposite);
        drawUMLStyle(g, packageTarget, hasFocus, width, height);
        g.setComposite(oldComposite);
        g.translate(-packageTarget.getGhostX(), -packageTarget.getGhostY());
    }

    /**
     * Draw the package icon.
     */
    private void drawUMLStyle(Graphics2D g, PackageTarget packageTarget, boolean hasFocus, int width, int height)
    {
        tabWidth = packageTarget.getWidth() / 3;

        g.setColor(defaultbg);
        g.fillRect(0, 0, tabWidth, TAB_HEIGHT);
        g.fillRect(0, TAB_HEIGHT, width, height - TAB_HEIGHT);

        drawShadow(g, packageTarget, width, height);

        g.setColor(bordercolour);
        g.setFont(getFont(packageTarget));
        Utility.drawCentredText(g, packageTarget.getDisplayName(), TEXT_BORDER, TEXT_BORDER + TAB_HEIGHT, 
                width - 2 * TEXT_BORDER, TEXT_HEIGHT);
        drawUMLBorders(g, packageTarget, hasFocus, width, height);
    }

    /**
     * Draw the borders of the package icon.
     */
    private void drawUMLBorders(Graphics2D g, PackageTarget packageTarget, boolean hasFocus, int width, int height)
    {
        int thickness = 1;  // default
        boolean isSelected = packageTarget.isSelected() && hasFocus;
        if(isSelected)
            thickness = 2;
        
        Utility.drawThickRect(g, 0, 0, tabWidth, TAB_HEIGHT, thickness);
        Utility.drawThickRect(g, 0, TAB_HEIGHT, width, height - TAB_HEIGHT, thickness);

        if (!isSelected)
            return;

        // Draw lines showing resize tag
        g.drawLine(width - HANDLE_SIZE - 2, height, width, height - HANDLE_SIZE - 2);
        g.drawLine(width - HANDLE_SIZE + 2, height, width, height - HANDLE_SIZE + 2);
    }

    /**
     * Draw the shadow.
     */
    private void drawShadow(Graphics2D g, PackageTarget packageTarget, int width, int height)
    {
        g.setColor(shadowColours[3]);
        g.drawLine(3, height + 1, width, height + 1);                   //bottom

        g.setColor(shadowColours[2]);
        g.drawLine(4, height + 2, width, height + 2);                   //bottom
        g.drawLine(width + 1, height + 2, width + 1, 3 + TAB_HEIGHT);   //right
        g.drawLine(tabWidth + 1, 3, tabWidth + 1, TAB_HEIGHT);          //tab

        g.setColor(shadowColours[1]);
        g.drawLine(5, height + 3, width + 1, height + 3);               // bottom
        g.drawLine(width + 2, height + 3, width + 2, 4 + TAB_HEIGHT);   // right
        g.drawLine(tabWidth + 2, 4, tabWidth + 2, TAB_HEIGHT);//tab

        g.setColor(shadowColours[0]);
        g.drawLine(6, height + 4, width + 2, height + 4);               // bottom
        g.drawLine(width + 3, height + 3, width + 3, 5 + TAB_HEIGHT);   // right
        g.drawLine(tabWidth + 3, 5, tabWidth + 3, TAB_HEIGHT);          // tab
    }

    private Font getFont(PackageTarget packageTarget)
    {
        return PrefMgr.getStandardFont();
    }

}