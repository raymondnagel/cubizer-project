/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cubizer;

import static cubizer.Global.AllCubes;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author rnagel
 */
public final class Renderer3D extends javax.swing.JPanel {  
    private BufferedImage renderBuffer = null;
    private boolean isPainting = false;
    private boolean isActive = false;
    private int captureWidth = 0;
    private int captureHeight = 0;
    
    /**
     * Creates new form Renderer3D
     */
    public Renderer3D() {
        initComponents();             
    }   
    
    public void setCaptureSize(int width, int height)
    {
        this.captureWidth = width;
        this.captureHeight = height;
    }
    public Rectangle getCaptureArea()
    {
        if (Global.CurrentModel != null)
        {
            Cube modelPivot = Global.CurrentModel.getPivotCube();
            Point2d center2d = modelPivot.get2dCenter();
            int cX = center2d.X()-(captureWidth/2);
            int cY = center2d.Y()-(captureHeight/2);
            return new Rectangle(cX, cY, captureWidth, captureHeight);
        }
        else
            return null;
    }
    
    public void activate()
    {
        this.isActive = true;
        redefBuffer();
    }
    
    private void redefBuffer()
    {
        renderBuffer = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
    }
    
    public void refresh()
    {
        if (isPainting) return; // To avoid a ConcurrentModificationException against AllCubes
        if (Global.IS_REFRESHING) return; // Return if the last refresh hasn't completed yet.
        
        Global.IS_REFRESHING = true;
        Collections.sort(AllCubes);
        
        // Now that cubes are sorted by Z value, get the nearest (least) Z.
        Global.NEAREST_Z = AllCubes.get(0).getFinalZ();
        // If the nearest one is the SelectionCube, get the next-nearest
        // if another exists. We don't want to base it on SelectionCube
        // because it's not reall part of the model.
        if (Global.SelectionCube == AllCubes.get(0) && AllCubes.size() > 1)
        {
            Global.NEAREST_Z = AllCubes.get(1).getFinalZ();
        }        
        render(renderBuffer.getGraphics());
        Global.IS_REFRESHING = false;
    }        
    
    public BufferedImage toSprite()
    {
        Rectangle captureArea = getCaptureArea();
        if (captureArea != null)
        {
            BufferedImage image = new BufferedImage(captureArea.width, captureArea.height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = image.createGraphics();   
            g.translate(-captureArea.x, -captureArea.y);
            
            for (int c = AllCubes.size()-1; c >= 0; c--)
            {            
                AllCubes.get(c).render3d(g);            
            }
            return image;
        }
        else
            return null;
    }
    
    private void render(Graphics g)
    {     
        g.setColor(this.getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        
        // "Refreshing" involves sorting AllCubes and happens on a different thread;
        // so if IS_REFRESHING is true, a ConcurrentModificationException may be thrown.
        if (isActive)
        {
            // "Painting" iterates over AllCubes, so we need an isPainting flag
            // to prevent sorting on a different thread to avoid ConcurrentModificationException.
            isPainting = true;
            
            // Translate to center:
            g.translate(getWidth()/2, getHeight()/2);
            // Render all Cubes:
            for (int c = AllCubes.size()-1; c >= 0; c--)
            {            
                AllCubes.get(c).render3d(g);            
            }
            // Paint the capture area:
            Rectangle captureArea = getCaptureArea();
            if (captureArea != null)
            {
                g.setColor(Color.WHITE);
                g.drawRect(captureArea.x, captureArea.y, captureArea.width, captureArea.height);
            }
            // Translate back:            
            g.translate(-(getWidth()/2), -(getHeight()/2));
            
            // Add some info:
            int y = getHeight()-48;
            int x = 10;
            StringBuilder info = new StringBuilder();
            g.setColor(Color.WHITE);

            info.append("Selection:").append(Global.SelectionCube.getCubicLocation()).append("  ");
            g.drawString(info.toString(), x, y);

            if (Global.CurrentModel != null)
            {
                y+=12;
                info = new StringBuilder(); 
                info.append("Model Pivot:").append(Global.CurrentModel.getPivotCube().getCubicLocation()).append("   ");
                info.append("Rotation:").append(Global.CurrentModel.getRotation()).append("  ");
                g.drawString(info.toString(), x, y);
            }

            y+=12;
            info = new StringBuilder();
            info.append("Scene Pivot:").append(Global.ScenePivotCube.getCubicLocation()).append("   ");
            info.append("Rotation:").append(Global.getSceneRotation()).append("  ");
            g.drawString(info.toString(), x, y);
                    
            isPainting = false;
        }
    }
    
    @Override
    public void paint(Graphics g) {
        super.paint(g);  
        if (isActive)
        {
            if (Global.IS_REFRESH_SCHEDULED)
            {
                Global.IS_REFRESH_SCHEDULED = false;
                refresh();
            }
            g.drawImage(renderBuffer, 0, 0, this);
        }
    }    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setBackground(new java.awt.Color(102, 102, 102));
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 200, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 200, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
        redefBuffer();
    }//GEN-LAST:event_formComponentResized


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
