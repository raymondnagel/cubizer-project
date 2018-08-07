/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cubizer;


import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.PixelGrabber;
import java.awt.image.RGBImageFilter;
import java.awt.image.renderable.ParameterBlock;
import javax.media.jai.JAI;
import javax.swing.ImageIcon;

/**
 *
 * @author rnagel
 */
public class ImageHelper {
    public static enum             Interpolation    {NONE, BILINEAR, BICUBIC};    
    
    public static BufferedImage tint(Color tintColor, BufferedImage bi)
    {
        return tint((double)tintColor.getRed()/255f,(double)tintColor.getGreen()/255f,(double)tintColor.getBlue()/255f, bi);
    }
    
    public static BufferedImage tint(double redPct, double greenPct, double bluePct, BufferedImage bi)
    {
        BufferedImage nbi = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < bi.getWidth(); x++)
            for (int y = 0; y < bi.getHeight(); y++)
            {
                int rgb = bi.getRGB(x, y);
                int r = (int)(redPct * ((rgb >> 16) & 0xff));
                int g = (int)(greenPct * ((rgb >> 8) & 0xff));
                int b = (int)(bluePct * (rgb & 0xff));     
                int k = ((256*256)*r) + (256*g) + b;
                nbi.setRGB(x, y, k);
                nbi.getTransparency();
            }
        return nbi;
    }
    
    public static BufferedImage invertColors(BufferedImage bi)
    {
        BufferedImage nbi = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < bi.getWidth(); x++)
            for (int y = 0; y < bi.getHeight(); y++)
            {
                int rgb = bi.getRGB(x, y);
                int r = 255 - ((rgb >> 16) & 0xff);
                int g = 255 - ((rgb >> 8) & 0xff);
                int b = 255 - (rgb & 0xff);  
                int k = ((256*256)*r) + (256*g) + b;
                nbi.setRGB(x, y, k);
            }
        return nbi;
    }
    
    public static BufferedImage makeTranslucent(BufferedImage bi, float pct)
    {
        BufferedImage aimg = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TRANSLUCENT);  
        // Get the images graphics  
        Graphics2D g = aimg.createGraphics();
        // Set the Graphics composite to Alpha  
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, pct));  
        // Draw the LOADED img into the prepared reciver image  
        g.drawImage(bi, null, 0, 0);  
        // let go of all system resources in this Graphics  
        g.dispose();  
        return aimg;
    }
    
    public static BufferedImage makeTranslucentScale(double redPct, double greenPct, double bluePct, BufferedImage bi)
    {
        BufferedImage nbi = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < bi.getWidth(); x++)
            for (int y = 0; y < bi.getHeight(); y++)
            {
                int rgb = bi.getRGB(x, y);
                int r = (int)(redPct * ((rgb >> 16) & 0xff));
                int g = (int)(greenPct * ((rgb >> 8) & 0xff));
                int b = (int)(bluePct * (rgb & 0xff));     
                int k = ((256*256*256)*r) + ((256*256)*g) + (b*256);// + (255-((r+g+b)/3));
                nbi.setRGB(x, y, k);
            }
        return nbi;
    }
    
    // This method returns a buffered image with the contents of an image
    public static BufferedImage toBufferedImage(Image image) {
        if (image instanceof BufferedImage) {
            return (BufferedImage)image;
        }
    
        // This code ensures that all the pixels in the image are loaded
        image = new ImageIcon(image).getImage();
    
        // Determine if the image has transparent pixels; for this method's
        // implementation, see e661 Determining If an Image Has Transparent Pixels
        boolean hasAlpha = hasAlpha(image);
    
        // Create a buffered image with a format that's compatible with the screen
        BufferedImage bimage = null;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        try {
            // Determine the type of transparency of the new buffered image
            int transparency = Transparency.OPAQUE;
            if (hasAlpha) {
                transparency = Transparency.BITMASK;
            }
    
            // Create the buffered image
            GraphicsDevice gs = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gs.getDefaultConfiguration();
            bimage = gc.createCompatibleImage(
                image.getWidth(null), image.getHeight(null), transparency);
        } catch (HeadlessException e) {
            // The system does not have a screen
        }
    
        if (bimage == null) {
            // Create a buffered image using the default color model
            int type = BufferedImage.TYPE_INT_RGB;
            if (hasAlpha) {
                type = BufferedImage.TYPE_INT_ARGB;
            }
            bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
        }
    
        // Copy image to buffered image
        Graphics g = bimage.createGraphics();
    
        // Paint the image onto the buffered image
        g.drawImage(image, 0, 0, null);
        g.dispose();
    
        return bimage;
    }
    
    // This method returns true if the specified image has transparent pixels
    public static boolean hasAlpha(Image image) {
        // If buffered image, the color model is readily available
        if (image instanceof BufferedImage) {
            BufferedImage bimage = (BufferedImage)image;
            return bimage.getColorModel().hasAlpha();
        }
    
        // Use a pixel grabber to retrieve the image's color model;
        // grabbing a single pixel is usually sufficient
         PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
        try {
            pg.grabPixels();
        } catch (InterruptedException e) {
        }
    
        // Get the image's color model
        ColorModel cm = pg.getColorModel();
        return cm.hasAlpha();
    }
    
    public static Image makeColorTransparent(Image im, final Color color) 
    {
        ImageFilter filter = new RGBImageFilter() 
        {
            // the color we are looking for... Alpha bits are set to opaque
            public int markerRGB = color.getRGB() | 0xFF000000;
            public final int filterRGB(int x, int y, int rgb) 
            {
                if (( rgb | 0xFF000000 ) == markerRGB) 
                {
                    // Mark the alpha bits as zero - transparent
                    return 0x00FFFFFF & rgb;
                }
                else 
                {
                    // nothing to do
                    return rgb;
                }
            }
        }; 
        ImageProducer ip = new FilteredImageSource(im.getSource(), filter);
        return Toolkit.getDefaultToolkit().createImage(ip);
    }
    
    public static BufferedImage scale(BufferedImage bsrc, int width, int height)
    {        
        BufferedImage bdest = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bdest.createGraphics();
        
        AffineTransform at = AffineTransform.getScaleInstance((double)width/bsrc.getWidth(), (double)height/bsrc.getHeight());
        g.drawRenderedImage(bsrc,at);
        g.dispose();
        return bdest;
    }
    
    public static BufferedImage compareImagesEachRGB(BufferedImage img1, BufferedImage img2)
    {
        BufferedImage comparison = new BufferedImage(img1.getWidth(), img1.getHeight(), img1.getType());
        for (int h = 0; h < img1.getWidth(); h++)
            for (int v = 0; v < img1.getHeight(); v++)
            {
                Color c1 = new Color(img1.getRGB(h, v));
                Color c2 = new Color(img2.getRGB(h, v));
                int r = (int)((c2.getRed()-c1.getRed())/2) + 128;
                int g = (int)((c2.getGreen()-c1.getGreen())/2) + 128;
                int b = (int)((c2.getBlue()-c1.getBlue())/2) + 128;
                Color c3 = new Color(r,g,b);
                comparison.setRGB(h, v, c3.getRGB());
            }
        return comparison;
    }
    
    public static BufferedImage compareImagesAllRGB(BufferedImage img1, BufferedImage img2)
    {
        BufferedImage comparison = new BufferedImage(img1.getWidth(), img1.getHeight(), img1.getType());
        for (int h = 0; h < img1.getWidth(); h++)
            for (int v = 0; v < img1.getHeight(); v++)
            {
                int rgb = img1.getRGB(h, v) - img2.getRGB(h, v);
                comparison.setRGB(h, v, rgb);
            }
        return comparison;
    }
    
    public static BufferedImage compareImagesBWColorDif(BufferedImage img1, BufferedImage img2)
    {
        BufferedImage comparison = new BufferedImage(img1.getWidth(), img1.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int h = 0; h < img1.getWidth(); h++)
            for (int v = 0; v < img1.getHeight(); v++)
            {
                Color c1 = new Color(img1.getRGB(h, v));
                Color c2 = new Color(img2.getRGB(h, v));
                int white1 = c1.getRed();
                int white2 = c2.getRed();
                int r = 0, g = 0, b = 0;
                int whiteDif = white2 - white1;
                if (whiteDif >= 0)
                {
                    r = whiteDif;//*whiteDif;
                }
                else
                {
                    g = -whiteDif;//*-whiteDif;
                }
                if (r > 255) r = 255;
                if (g > 255) g = 255;
                Color c3 = new Color(r,g,b);
                comparison.setRGB(h, v, c3.getRGB());
            }
        return comparison;
    }
    
    public static boolean compareImagesIndentical(BufferedImage img1, BufferedImage img2)
    {
        for (int h = 0; h < img1.getWidth(); h++)
            for (int v = 0; v < img1.getHeight(); v++)
            {
                if (img1.getRGB(h, v) != img2.getRGB(h, v)) return false;
            }
        return true;
    }    
    
    public static BufferedImage scaleImage(BufferedImage bi, int newWidth, int newHeight, Interpolation interpolation)
    {           
        BufferedImage scaledImage = null;    
        ParameterBlock pb = new ParameterBlock();
        float hScale = (float)newWidth / bi.getWidth();
        float vScale = (float)newHeight / bi.getHeight();
        switch (interpolation)
        {
            case NONE:
                scaledImage = new BufferedImage(newWidth, newHeight, bi.getType());
                Graphics2D g = (Graphics2D)scaledImage.getGraphics();
                g.drawImage(bi, 0, 0, newWidth, newHeight, null);                
                break;
            case BILINEAR:                
                pb.addSource(bi);
                pb.add(hScale).add(vScale).add(0.0F).add(0.0F);
                pb.add(javax.media.jai.Interpolation.getInstance(javax.media.jai.Interpolation.INTERP_BILINEAR));
                scaledImage = JAI.create("scale", pb).getAsBufferedImage();
                break;
            case BICUBIC:                
                pb.addSource(bi);
                pb.add(hScale).add(vScale).add(0.0F).add(0.0F);
                pb.add(javax.media.jai.Interpolation.getInstance(javax.media.jai.Interpolation.INTERP_BICUBIC));
                scaledImage = JAI.create("scale", pb).getAsBufferedImage();
                break;
        }
        return scaledImage;
    }
    public static BufferedImage scaleImage(BufferedImage bi, float scaleFactor, Interpolation interpolation)
    {           
        BufferedImage scaledImage = null;    
        ParameterBlock pb = new ParameterBlock();
        switch (interpolation)
        {
            case NONE:
                int width = (int)(scaleFactor*bi.getWidth());
                int height = (int)(scaleFactor*bi.getHeight());
                scaledImage = new BufferedImage(width, height, bi.getType());
                Graphics2D g = (Graphics2D)scaledImage.getGraphics();
                g.drawImage(bi, 0, 0, width, height, null);                
                break;
            case BILINEAR:                
                pb.addSource(bi);
                pb.add(scaleFactor).add(scaleFactor).add(0.0F).add(0.0F);
                pb.add(javax.media.jai.Interpolation.getInstance(javax.media.jai.Interpolation.INTERP_BILINEAR));
                scaledImage = JAI.create("scale", pb).getAsBufferedImage();
                break;
            case BICUBIC:                
                pb.addSource(bi);
                pb.add(scaleFactor).add(scaleFactor).add(0.0F).add(0.0F);
                pb.add(javax.media.jai.Interpolation.getInstance(javax.media.jai.Interpolation.INTERP_BICUBIC));
                scaledImage = JAI.create("scale", pb).getAsBufferedImage();
                break;
        }
        return scaledImage;
    }
    public static BufferedImage scaleImageByWidth(BufferedImage bi, int newWidth, Interpolation interpolation)
    {
        return scaleImage(bi, (float)newWidth/(float)bi.getWidth(), interpolation);        
    }
    public static BufferedImage scaleImageByHeight(BufferedImage bi, int newHeight, Interpolation interpolation)
    {
        return scaleImage(bi, (float)newHeight/(float)bi.getHeight(), interpolation);        
    }
    public static BufferedImage scaleImageToBest(BufferedImage bi, int maxWidth, int maxHeight, Interpolation interpolation)
    {
        double horzFactor = (double)maxWidth/(double)bi.getWidth();
        double vertFactor = (double)maxHeight/(double)bi.getHeight();
        if (vertFactor<horzFactor)
        {
            return scaleImageByHeight(bi, maxHeight, interpolation);
        }
        else
        {
            return scaleImageByWidth(bi, maxWidth, interpolation);
        }
    }
    
    public static BufferedImage swapColors(BufferedImage img, Color color1, Color color2)
    {
        BufferedImage newImg = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
        for (int h = 0; h < img.getWidth(); h++)
            for (int v = 0; v < img.getHeight(); v++)
            {
                if (img.getRGB(h, v)==color1.getRGB())
                {
                    newImg.setRGB(h, v, color2.getRGB());
                }
                else if (img.getRGB(h, v)==color2.getRGB())
                {
                    newImg.setRGB(h, v, color1.getRGB());
                }                    
                else
                {
                    newImg.setRGB(h, v, img.getRGB(h, v));
                }
            }
        return newImg;
    }
    
}

