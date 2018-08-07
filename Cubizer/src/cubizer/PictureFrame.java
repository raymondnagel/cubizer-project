/**
 * PictureFrame.java
 * Created on January 9, 2007, 1:25 PM
 * @author rnagel
 */

package cubizer;

import java.awt.*;
import java.awt.image.*;
import java.io.Serializable;
import java.net.URL;
import javax.imageio.*;
import javax.swing.*;

/**
 * Defines a bean which loads and displays an image.
 * Has the ability to specify a layout for the image.
 * 
 * @author   rnagel
 * @version  1.0
 * @see      #ImageLayoutModes
 * @since    JDK1.6.1
 */
public class PictureFrame extends JPanel implements Serializable
{


    /**
     * Contains modes for laying out an <code>Image</code> in a <code>PictureFrame</code>. <UL>
     * <LI>NORMAL - displays the <code>Image</code> in the top-left corner.</LI>
     * <LI>CENTER - centers the <code>Image</code>.</LI>
     * <LI>STRETCH - resizes the <code>Image</code> to fill the whole <code>PictureFrame</code>.</LI>
     * <LI>AUTO_RESIZE - resizes the <code>PictureFrame</code> to fit the <code>Image</code>.</LI></UL>
     */
    public static enum ImageLayoutModes {NORMAL, CENTER, STRETCH, AUTO_RESIZE}
             
    private static int              ourCount = 0;
    private URL                     myImageURL;
    private BufferedImage           myImage;
    private boolean                 myImageDisplayed = true;
    private ImageLayoutModes        myLayoutMode = ImageLayoutModes.NORMAL;
    private String                  myName = null;
    
    
    //____________________PROPERTY METHODS____________________//
    
    
    @Override
    public String getName() {
        return myName;
    }

    @Override
    public void setName(String name) {
        this.myName = name;
    }
    
    
    
    
   /**
    * Sets the <code>Image</code> contained by this <code>PictureFrame</code>.
    * 
    * @param     newImage    the <code>Image</code> to set.
    * @see       #getImage()
    */
    public void setImage(BufferedImage newImage)
    {
        myImage = newImage;
        myImageURL = null;
    }
    
   /**
    * Gets the <code>Image</code> contained by this <code>PictureFrame</code>.
    * 
    * @return    the <code>PictureFrame</code>'s <code>Image</code>
    * @see       #setImage(BufferedImage)
    */
    public BufferedImage getImage()
    {
        return myImage;
    }

   /**
    * Loads an <code>Image</code> from the specified <code>URL</code>.
    * 
    * @param     newImageURL    the <code>URL</code> of an <code>Image</code>
    *            file
    * @see       #getImageURL()
    */
    public void setImageURL(URL newImageURL)
    {
        URL oldValue = null;
        try {oldValue = new URL(myImageURL.toString());}
        catch(Exception e){oldValue = null;}
        finally
        {
            try
            {
                BufferedImage NewImage = ImageIO.read(newImageURL);
                setImage(NewImage);
            } 
            catch (Exception e) 
            {
                e.toString();
            }   
            myImageURL = newImageURL;
        }
    }
    
   /**
    * Gets the <code>URL</code> to the currently contained <code>Image</code>,
    * if it was loaded from a <code>URL</code>.
    * 
    * @return    the <code>URL</code> of the <code>PictureFrame</code>'s
    *            current <code>Image</code>, or <code>null</code> if the
    *            <code>Image</code> was not loaded from a <code>URL</code>
    * @see       #setImageURL(URL)
    */
    public URL getImageURL()
    {
        return myImageURL;
    }

   /**
    * Determines whether the <code>Image</code> contained should be
    * displayed. If the <code>Image</code> is not displayed, the entire
    * <code>PictureFrame</code> will be invisible.
    * 
    * @param     displayed    <code>true</code> if the <code>Image</code>
    *            should be displayed, or <code>false</code> if it should
    *            not be displayed.
    */
    public void setImageDisplayed(boolean displayed)
    {
        myImageDisplayed = displayed;
    }
    
   /**
    * Determine whether the <code>Image</code> contained should be
    * displayed. If the <code>Image</code> is not displayed, the entire
    * <code>PictureFrame</code> will be invisible.
    * 
    * @param     displayed    <code>true</code> if the <code>Image</code>
    *            should be displayed, or <code>false</code> if it should
    *            not be displayed.
    */
    public boolean isImageDisplayed()
    {
        return myImageDisplayed;
    }

   /**
    * Sets the layout mode for the <code>PictureFrame</code>'s <code>Image</code>.
    * 
    * @param     newLayoutMode    a value from <code>ImageLayoutModes</code>
    * @see       #getLayoutMode()
    * @see       #ImageLayoutModes
    */
    public void setLayoutMode(ImageLayoutModes newLayoutMode)
    {
        myLayoutMode = newLayoutMode;
    }
    
   /**
    * Gets the layout mode for the <code>PictureFrame</code>'s <code>Image</code>.
    * 
    * @return    a value from <code>ImageLayoutModes</code>
    * @see       #setLayoutMode(ImageLayoutModes)
    * @see       #ImageLayoutModes
    */
    public ImageLayoutModes getLayoutMode()
    {
        return myLayoutMode;
    }

    //____________________CONSTRUCTOR METHODS____________________//
    
    public PictureFrame()
    {
        super();
    }
    
   /**
    * Creates a new <code>PictureFrame</code> object and attempts to
    * load an <code>Image</code> from the specified <code>URL</code>.
    * 
    * @param     ImageURL    the <code>URL</code> of an <code>Image</code>
    *            file
    */
    public PictureFrame(URL ImageURL)
    {
        setName("PictureFrame" + ++ourCount);
        setImageURL(ImageURL);
        setSize(myImage.getWidth(), myImage.getHeight());
    }
    
    public PictureFrame(BufferedImage image)
    {
        setName("PictureFrame" + ++ourCount);
        setImage(image);
        setSize(myImage.getWidth(), myImage.getHeight());
    }
    
    
    //____________________PUBLIC INTERFACE METHODS____________________//
    
   // Paints the image...
    @Override
    public void paint(Graphics g)
    {
        super.paint(g);
        if (myImageDisplayed && myImage != null)
            switch (myLayoutMode)
            {
                case AUTO_RESIZE:
                    if (getWidth() != myImage.getWidth() || getHeight() != myImage.getHeight())
                    {
                        setSize(myImage.getWidth(), myImage.getHeight());
                        setPreferredSize(new Dimension(myImage.getWidth(), myImage.getHeight()));
                    }
                    //No break: let it just drop down and draw normally...
                case NORMAL: 
                    g.drawImage(myImage,0,0,null);
                    break;
                case CENTER: 
                    int sx1, sy1, sx2, sy2, dx1, dy1, dx2, dy2;
                    sx1 = (myImage.getWidth()/2) - (getWidth()/2);
                    sy1 = (myImage.getHeight()/2) - (getHeight()/2);
                    sx2 = sx1 + getWidth();
                    sy2 = sy1 + getHeight();
                    dx1 = 0; dy1 = 0; dx2 = getWidth(); dy2 = getHeight();
                    g.drawImage(myImage,dx1,dy1,dx2,dy2,sx1,sy1,sx2,sy2,null);
                    break;
                case STRETCH:
                    g.drawImage(myImage,0,0,getWidth(),getHeight(),0,0,myImage.getWidth(),myImage.getHeight(),null);
                    break;
            }
    }
    
}
