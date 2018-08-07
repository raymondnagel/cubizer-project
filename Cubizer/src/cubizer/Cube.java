/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cubizer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author rnagel
 */
public class Cube implements Comparable<Cube> {        
    final static float dash[] = {5.0f};
    final static BasicStroke dashedStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f, dash, 0.0f);
    
    
    private class Face implements Comparable<Face>{
        String name;
        Point3d p1r;
        Point3d p2r;
        Point3d p3r;
        Point3d p4r;
        
        public Face(String name, Point3d p1, Point3d p2, Point3d p3, Point3d p4) {
            this.name = name;            
            this.p1r = p1;
            this.p2r = p2;
            this.p3r = p3; 
            this.p4r = p4;
        }

        @Override
        public int compareTo(Face face) {
            return Double.compare(this.getFaceCenterZ(), face.getFaceCenterZ());
        }
        
        private double getFaceCenterZ()
        {
            return (p1r.z + p2r.z + p3r.z + p4r.z)/4.0;
        }

        @Override
        public String toString() {
            return name + ":" + getFaceCenterZ();
        }                
    }    
    private String uniqueId = null;
    private Color faceColor = Global.DEFAULT_FACE_COLOR;
    private Color edgeColor = Global.DEFAULT_EDGE_COLOR;
    private double alpha = 1.0;
    private Point3d location = new Point3d(0,0,0);
    private Model3d model = null;
    private boolean isModelPivot = false;
    private boolean isScenePivot = false;
    private boolean showEdges = true;
    private boolean showFaces = true;

    public Cube(boolean addToScene) {
        if (addToScene)
            Global.addCube(this);
    }
    
    public String createBuildString()
    {
        // location faceColor edgeColor alpha isModelPivot showEdges showFaces
        StringBuilder buildString = new StringBuilder();
        buildString.append(uniqueId).append("\t");
        buildString.append(location.x).append(",").append(location.y).append(",").append(location.z).append("\t");
        buildString.append(faceColor.getRed()).append(",").append(faceColor.getGreen()).append(",").append(faceColor.getBlue()).append("\t");
        buildString.append(edgeColor.getRed()).append(",").append(edgeColor.getGreen()).append(",").append(edgeColor.getBlue()).append("\t");
        buildString.append(alpha).append("\t");
        buildString.append(isModelPivot).append("\t");
        buildString.append(showEdges).append("\t");
        buildString.append(showFaces);
        return buildString.toString();
    }
    public static Cube fromBuildString(String buildString)
    {
        Cube newCube = new Cube(false);
        // uniqueId location faceColor edgeColor alpha isModelPivot showEdges showFaces
        String[] data = buildString.trim().split("\t");
        newCube.uniqueId = data[0].trim();
        String[] point = data[1].trim().split(",");
        newCube.location = new Point3d(Double.parseDouble(point[0].trim()), Double.parseDouble(point[1].trim()), Double.parseDouble(point[2].trim()));
        String[] rgba = data[2].trim().split(",");
        newCube.faceColor = new Color(Integer.parseInt(rgba[0].trim()), Integer.parseInt(rgba[1].trim()), Integer.parseInt(rgba[2].trim()));
        rgba = data[3].trim().split(",");
        newCube.edgeColor = new Color(Integer.parseInt(rgba[0].trim()), Integer.parseInt(rgba[1].trim()), Integer.parseInt(rgba[2].trim()));
        newCube.alpha = Double.parseDouble(data[4].trim());
        newCube.isModelPivot = Boolean.parseBoolean(data[5].trim());
        newCube.showEdges = Boolean.parseBoolean(data[6].trim());
        newCube.showFaces = Boolean.parseBoolean(data[7].trim());
        return newCube;
    }
    
    public Model3d getModel()
    {
        return this.model;
    }
    public void attachToModel(Model3d model)
    {
        this.model = model;
    }
    public void detach()
    {        
        this.model = null;
    }
    
    public Model3d createSubModel(String name)
    {
        Model3d parentModel = this.model;
        this.model.removeCube(this);
        Model3d subModel = new Model3d(name, this, parentModel);
        return subModel;
    }
    
    public void setScenePivot(boolean pivot)
    {
        this.isScenePivot = pivot;
    }
    public boolean isScenePivot()
    {
        return this.isScenePivot;
    }        
    
    public void setModelPivot(boolean pivot)
    {
        this.isModelPivot = pivot;
    }
    public boolean isModelPivot()
    {
        return this.isModelPivot;
    }
    
    public void setAlpha(double alpha)
    {
        this.alpha = alpha;        
    }
    public double getAlpha()
    {
        return alpha;   
    }
    public Color getAlphaColor()
    {
        return Global.makeAlphaColor(faceColor, alpha);
    }
    public void setFaceColor(Color color)
    {
        this.faceColor = color;
    }
    public Color getFaceColor()
    {
        return this.faceColor;
    }
    public void setEdgeColor(Color color)
    {
        this.edgeColor = color;
    }
    public Color getEdgeColor()
    {
        return this.edgeColor;
    }
    
    public void setShowingEdges(boolean showEdges)
    {
        this.showEdges = showEdges;
    }
    public boolean isShowingEdges()
    {
        return this.showEdges;
    }
    
    public void setShowingFaces(boolean showFaces)
    {
        this.showFaces = showFaces;
    }
    public boolean isShowingFaces()
    {
        return this.showFaces;
    }
    
    public void move(double x, double y, double z)
    {
        this.location.setPoint(location.x+x, location.y+y, location.z+z);
    }
    public void move(Point3d move)
    {
        this.location.setPoint(location.x+move.x, location.y+move.y, location.z+move.z);
    }
    public void setLocation(double x, double y, double z)
    {
        this.location.setPoint(x, y, z);
    }
    public void setLocation(Point3d location)
    {
        this.location.setPoint(location.x, location.y, location.z);
    }
    public Point3d getLocation()
    {
        return location;
    }
    public void setCubicLocation(double x, double y, double z)
    {
        this.location.setPoint(x * Global.CUBE_SIDE_SIZE,
                               y * Global.CUBE_SIDE_SIZE,
                               z * Global.CUBE_SIDE_SIZE);
    }
    public void setCubicLocation(Point3d location)
    {
        this.location.setPoint(location.x * Global.CUBE_SIDE_SIZE,
                               location.y * Global.CUBE_SIDE_SIZE,
                               location.z * Global.CUBE_SIDE_SIZE);
    }
    public Point3d getCubicLocation()
    {
        return new Point3d(location.x / Global.CUBE_SIDE_SIZE,
                           location.y / Global.CUBE_SIDE_SIZE,
                           location.z / Global.CUBE_SIDE_SIZE);
    }
    public Point3d getScaledLocation()
    {
        return new Point3d(location.x * Global.SCALE, location.y * Global.SCALE, location.z * Global.SCALE);        
    }
    public Point3d getScaledLocationSceneRotated()
    {
        return Global.rotateAround(getScaledLocation(), Global.ScenePivotCube.getScaledLocation(), Global.getSceneRotation());
    }
    public Point3d getLocationInModel()
    {
        if (model == null)
            return null;
        else
        {
            Point3d pivot = model.getPivotCube().getLocation();
            return new Point3d(location.x - pivot.x, location.y - pivot.y, location.z - pivot.z);
        }
    }
    public void setLocationInModel(Point3d relToPivot)
    {
        if (model != null)
        {
            Point3d pivot = model.getPivotCube().getLocation();
            setLocation(pivot.x + relToPivot.x, pivot.y + relToPivot.y, pivot.z + relToPivot.z);            
        }
    }
        
    public void smooth()
    {
        double levels = getNeighborCount()+1;
        if (levels < 6)
            setAlpha(levels*.167);
        else
            setAlpha(1.0);
    }
    public Cube getAdjacentCube(String faceName)
    {
        if (faceName.equalsIgnoreCase("front"))
            return Global.getCubeAt(new Point3d(location.x, location.y, location.z-Global.CUBE_SIDE_SIZE));
        if (faceName.equalsIgnoreCase("back"))
            return Global.getCubeAt(new Point3d(location.x, location.y, location.z+Global.CUBE_SIDE_SIZE));
        if (faceName.equalsIgnoreCase("left"))
            return Global.getCubeAt(new Point3d(location.x-Global.CUBE_SIDE_SIZE, location.y, location.z));
        if (faceName.equalsIgnoreCase("right"))
            return Global.getCubeAt(new Point3d(location.x+Global.CUBE_SIDE_SIZE, location.y, location.z));
        if (faceName.equalsIgnoreCase("top"))
            return Global.getCubeAt(new Point3d(location.x, location.y-Global.CUBE_SIDE_SIZE, location.z));
        if (faceName.equalsIgnoreCase("bottom"))
            return Global.getCubeAt(new Point3d(location.x, location.y+Global.CUBE_SIDE_SIZE, location.z));
        return null;
    }
    public int getNeighborCount()
    {
        int count = 0;
        if (Global.getCubeAt(new Point3d(location.x+Global.CUBE_SIDE_SIZE, location.y, location.z)) != null)
            count++;
        if (Global.getCubeAt(new Point3d(location.x-Global.CUBE_SIDE_SIZE, location.y, location.z)) != null)
            count++;
        if (Global.getCubeAt(new Point3d(location.x, location.y+Global.CUBE_SIDE_SIZE, location.z)) != null)
            count++;
        if (Global.getCubeAt(new Point3d(location.x, location.y-Global.CUBE_SIDE_SIZE, location.z)) != null)
            count++;
        if (Global.getCubeAt(new Point3d(location.x, location.y, location.z+Global.CUBE_SIDE_SIZE)) != null)
            count++;
        if (Global.getCubeAt(new Point3d(location.x, location.y, location.z-Global.CUBE_SIDE_SIZE)) != null)
            count++;
        return count;
    }
    public int getSelectiveNeighbors(boolean includeFront, boolean includeBack, boolean includeLeft, boolean includeRight, boolean includeTop, boolean includeBottom)
    {
        int count = 0;
        if (includeRight && Global.getCubeAt(new Point3d(location.x+Global.CUBE_SIDE_SIZE, location.y, location.z)) != null)
            count++;
        if (includeLeft && Global.getCubeAt(new Point3d(location.x-Global.CUBE_SIDE_SIZE, location.y, location.z)) != null)
            count++;
        if (includeBottom && Global.getCubeAt(new Point3d(location.x, location.y+Global.CUBE_SIDE_SIZE, location.z)) != null)
            count++;
        if (includeTop && Global.getCubeAt(new Point3d(location.x, location.y-Global.CUBE_SIDE_SIZE, location.z)) != null)
            count++;
        if (includeBack && Global.getCubeAt(new Point3d(location.x, location.y, location.z+Global.CUBE_SIDE_SIZE)) != null)
            count++;
        if (includeFront && Global.getCubeAt(new Point3d(location.x, location.y, location.z-Global.CUBE_SIDE_SIZE)) != null)
            count++;
        return count;
    }
    
    public Point3d getCombinedRotation()
    {        
        Point3d rot = new Point3d(Global.getSceneRotation());
        Model3d assocModel = getAssociatedModel();
        if (assocModel != null)
        {
            rot.addPoint(assocModel.getRotation());
        }
        return rot;
    }
    public double getFinalZ()
    {
        Point3d loc = new Point3d(location);
        
        Model3d assocModel = getAssociatedModel();
        if (assocModel != null)
        {            
            loc = Global.cumulativelyRotate(loc, assocModel);
        }        
        loc = Global.rotateAround(loc, Global.ScenePivotCube.getLocation(), Global.getSceneRotation());       
        return loc.z;
    }
    
    public Model3d getAssociatedModel()
    {        
        if (this == Global.SelectionCube)
            return Global.CurrentModel;
        else
            return model;
    }
    
    public double getCubeDistanceFromNearest()
    {
        double distanceFromNearest = getFinalZ() - Global.NEAREST_Z;
        distanceFromNearest /= Global.CUBE_SIDE_SIZE;
        return distanceFromNearest;
    }     
    public double getCubeDistanceFromScenePivot()
    {
        double distanceFromNearest = getFinalZ() - Global.ScenePivotCube.getFinalZ();
        distanceFromNearest /= Global.CUBE_SIDE_SIZE;
        return distanceFromNearest;
    }
    public double getCubeDistanceFromCurrentModelPivot()
    {
        double distanceFromNearest = getFinalZ() - Global.CurrentModel.getPivotCube().getFinalZ();
        distanceFromNearest /= Global.CUBE_SIDE_SIZE;
        return distanceFromNearest;
    }
    
    
    public Point2d get2dCenter()
    {        
        if (location != null)
        {                                                 
            // Scene Pivot:
            Point3d scenePivot = Global.ScenePivotCube.getScaledLocation();            
            // Cube Center:
            Point3d cubeCenter = getScaledLocation();           
            // Model Pivot:
            Model3d assocModel = getAssociatedModel();

            // If there is a model associated with this cube, rotate around the model's pivot:
            if (assocModel != null)
            {
                cubeCenter = Global.cumulativelyRotate(cubeCenter, assocModel);
            }                                    
            // Rotate around the scene pivot:
            cubeCenter = Global.rotateAround(cubeCenter, scenePivot, Global.getSceneRotation());
            return cubeCenter.toPoint2d();
        }
        else return null;
    }
    
    public void renderHiRes(Graphics g)
    {                        
        if (location != null && faceColor != null)
        {                                    
            double distanceFromNearest = getCubeDistanceFromNearest();
            double side = (Global.CUBE_SIDE_SIZE * Global.SCALE) - (distanceFromNearest * Global.Z_SCALE_INCREMENT);
            double halfSide = side * .5;
              
            // Scene Pivot:
            Point3d scenePivot = Global.ScenePivotCube.getScaledLocation();
            
            // Cube Center:
            Point3d cubeCenter = getScaledLocation();
            
            // Model Pivot:
            Model3d assocModel = getAssociatedModel();
            
            // If there is a model associated with this cube, rotate around the model's pivot:
            if (assocModel != null)
            {
                cubeCenter = Global.cumulativelyRotate(cubeCenter, assocModel);
            }                        
            
            // Rotate around the scene pivot:
            cubeCenter = Global.rotateAround(cubeCenter, scenePivot, Global.getSceneRotation());      

            // Represent the cube *VERY SIMPLY*
            g.setColor(faceColor);
            g.fillRect((int)(cubeCenter.x-halfSide), (int)(cubeCenter.y-halfSide), (int)side, (int)side);                        
            g.setColor(getZShadowColor());
            g.fillRect((int)(cubeCenter.x-halfSide), (int)(cubeCenter.y-halfSide), (int)side, (int)side);            
            
            
            // Other things we might draw:
            if (this.isScenePivot)
            {
                if (Global.SCENE_PIVOT_MOVEMENT)
                    drawMovementAxisGuide(halfSide, assocModel, g);
                if (Global.SCENE_PIVOT_ROTATION)
                    drawRotationAxisGuide(Global.selectionRotation, halfSide, assocModel, g);
            }
            else if (this.isModelPivot && Global.MODEL_PIVOTS_VISIBLE)
            {
                if (Global.MODEL_PIVOTS_MOVEMENT)
                    drawMovementAxisGuide(halfSide, assocModel, g);
                if (Global.MODEL_PIVOTS_ROTATION)
                    drawRotationAxisGuide(Global.selectionRotation, halfSide, assocModel, g);
            }
            else if (this == Global.SelectionCube)
            {                     
                if (Global.SELECTION_CUBE_MOVEMENT)
                    drawMovementAxisGuide(halfSide, assocModel, g);
                if (Global.SELECTION_CUBE_ROTATION)
                    drawRotationAxisGuide(Global.selectionRotation, halfSide, assocModel, g);
            }
        }
    }
    
    
    public void render3d(Graphics g)
    {                
        if (this == Global.SelectionCube && !Global.SELECTION_CUBE_VISIBLE) return;
        if (this == Global.ScenePivotCube && !Global.SCENE_PIVOT_VISIBLE) return;
        if (this.getModel() != null && !this.getModel().isVisible()) return;
        
        if (Global.HI_RESOLUTION_MODE)
        {
            renderHiRes(g);
            return;
        }
        
        if (location != null && faceColor != null)
        {                                    
            double distanceFromNearest = getCubeDistanceFromNearest();
            double side = (Global.CUBE_SIDE_SIZE * Global.SCALE) - (distanceFromNearest * Global.Z_SCALE_INCREMENT);
            double halfSide = side * .5;
              
            // Scene Pivot:
            Point3d scenePivot = Global.ScenePivotCube.getScaledLocation();
            
            // Cube Center:
            Point3d cubeCenter = getScaledLocation();
            
            // Model Pivot:
            Model3d assocModel = getAssociatedModel();


            // Get and apply z-location offset:
            //double zScaleDepthOffset = getZScaleDepthOffset(distanceFromNearest);
            
            // Set 4 points to make the "front" rectangle in 3D space:
            Point3d bL1 = new Point3d(cubeCenter.x-halfSide, cubeCenter.y-halfSide, cubeCenter.z+halfSide);
            Point3d bL2 = new Point3d(cubeCenter.x-halfSide, cubeCenter.y+halfSide, cubeCenter.z+halfSide);
            Point3d bR1 = new Point3d(cubeCenter.x+halfSide, cubeCenter.y-halfSide, cubeCenter.z+halfSide);
            Point3d bR2 = new Point3d(cubeCenter.x+halfSide, cubeCenter.y+halfSide, cubeCenter.z+halfSide);        
            // Set 4 points to make the "back" rectangle in 3D space:
            Point3d fL1 = new Point3d(cubeCenter.x-halfSide, cubeCenter.y-halfSide, cubeCenter.z-halfSide);
            Point3d fL2 = new Point3d(cubeCenter.x-halfSide, cubeCenter.y+halfSide, cubeCenter.z-halfSide);
            Point3d fR1 = new Point3d(cubeCenter.x+halfSide, cubeCenter.y-halfSide, cubeCenter.z-halfSide);
            Point3d fR2 = new Point3d(cubeCenter.x+halfSide, cubeCenter.y+halfSide, cubeCenter.z-halfSide);                              
            
            // Add z scale depth offset: this must be added before rotation;
            // and yet, it needs to be calculated based on distanceFromNearest,
            // which is calculated based on rotated values, which means it can only
            // be applied to rotated values. Which makes this a difficult problem :-/
//            cubeCenter.z -= zScaleDepthOffset;
//            bL1.z -= zScaleDepthOffset;
//            bL2.z -= zScaleDepthOffset;
//            bR1.z -= zScaleDepthOffset;
//            bR2.z -= zScaleDepthOffset;
//            fL1.z -= zScaleDepthOffset;
//            fL2.z -= zScaleDepthOffset;
//            fR1.z -= zScaleDepthOffset;
//            fR2.z -= zScaleDepthOffset;
            
            // If there is a model associated with this cube, rotate around the model's pivot:
            if (assocModel != null)
            {
                cubeCenter = Global.cumulativelyRotate(cubeCenter, assocModel);
                bL1 = Global.cumulativelyRotate(bL1, assocModel);
                bL2 = Global.cumulativelyRotate(bL2, assocModel);
                bR1 = Global.cumulativelyRotate(bR1, assocModel);
                bR2 = Global.cumulativelyRotate(bR2, assocModel);
                fL1 = Global.cumulativelyRotate(fL1, assocModel);
                fL2 = Global.cumulativelyRotate(fL2, assocModel);
                fR1 = Global.cumulativelyRotate(fR1, assocModel);
                fR2 = Global.cumulativelyRotate(fR2, assocModel);
            }                        
            
            // Rotate around the scene pivot:
            cubeCenter = Global.rotateAround(cubeCenter, scenePivot, Global.getSceneRotation());
            bL1 = Global.rotateAround(bL1, scenePivot, Global.getSceneRotation());
            bL2 = Global.rotateAround(bL2, scenePivot, Global.getSceneRotation());
            bR1 = Global.rotateAround(bR1, scenePivot, Global.getSceneRotation());
            bR2 = Global.rotateAround(bR2, scenePivot, Global.getSceneRotation());
            fL1 = Global.rotateAround(fL1, scenePivot, Global.getSceneRotation());
            fL2 = Global.rotateAround(fL2, scenePivot, Global.getSceneRotation());
            fR1 = Global.rotateAround(fR1, scenePivot, Global.getSceneRotation());
            fR2 = Global.rotateAround(fR2, scenePivot, Global.getSceneRotation());          

            
            // We have to sort the faces by Z-position so we know which order to paint them.
            ArrayList<Face> faces = new ArrayList<>();
            faces.add(new Face("front", fL1, fR1, fR2, fL2));
            faces.add(new Face("back", bL1, bR1, bR2, bL2));
            faces.add(new Face("left", bL1, fL1, fL2, bL2));
            faces.add(new Face("right", bR1, fR1, fR2, bR2));
            faces.add(new Face("top", bL1, bR1, fR1, fL1));
            faces.add(new Face("bottom", bL2, bR2, fR2, fL2));            
            Collections.sort(faces);
            
            // Do the faces in the reverse-sorted order, because they are sorted
            // in ascending order, which is front-to-back. We need back-to-front.
            for (int f = faces.size()-1; f >= 0; f--)
            {
                // Do face ONLY if its Z is in front of the cube center;
                // otherwise it's not currently visible.
                if (faces.get(f).getFaceCenterZ() < cubeCenter.z)
                    // Also, don't do the face if there is another cube in that direction:
                    if (getAdjacentCube(faces.get(f).name) == null || getAdjacentCube(faces.get(f).name).getModel() != assocModel)
                        doFace(faces.get(f), g);
            }
                                   
            // Other things we might draw:
            if (this.isScenePivot)
            {
                if (Global.SCENE_PIVOT_MOVEMENT)
                    drawMovementAxisGuide(halfSide, assocModel, g);
                if (Global.SCENE_PIVOT_ROTATION)
                    drawRotationAxisGuide(Global.selectionRotation, halfSide, assocModel, g);
            }
            else if (this.isModelPivot && Global.MODEL_PIVOTS_VISIBLE)
            {
                if (Global.MODEL_PIVOTS_MOVEMENT)
                    drawMovementAxisGuide(halfSide, assocModel, g);
                if (Global.MODEL_PIVOTS_ROTATION)
                    drawRotationAxisGuide(Global.selectionRotation, halfSide, assocModel, g);
            }
            else if (this == Global.SelectionCube)
            {                     
                if (Global.SELECTION_CUBE_MOVEMENT)
                    drawMovementAxisGuide(halfSide, assocModel, g);
                if (Global.SELECTION_CUBE_ROTATION)
                    drawRotationAxisGuide(Global.selectionRotation, halfSide, assocModel, g);
            }
            
//            DecimalFormat format = new DecimalFormat("0.0");
//            drawInfoTag("+                            " + cubeCenter.toString() + ": " + format.format(zScaleDepthOffset), cubeCenter, g);
        }
    }
    
    public void renderPivotOnly(Graphics g)
    {
        double distanceFromNearest = getCubeDistanceFromNearest();
        double side = (Global.CUBE_SIDE_SIZE * Global.SCALE) - (distanceFromNearest * Global.Z_SCALE_INCREMENT);
        double halfSide = side * .5;

        // Model Pivot:
        Model3d assocModel = getAssociatedModel();

        if (this.isScenePivot)
        {
            if (Global.SCENE_PIVOT_MOVEMENT)
                drawMovementAxisGuide(halfSide, assocModel, g);
            if (Global.SCENE_PIVOT_ROTATION)
                drawRotationAxisGuide(Global.selectionRotation, halfSide, assocModel, g);
        }
        else if (this.isModelPivot && Global.MODEL_PIVOTS_VISIBLE)
        {
            if (Global.MODEL_PIVOTS_MOVEMENT)
                drawMovementAxisGuide(halfSide, assocModel, g);
            if (Global.MODEL_PIVOTS_ROTATION)
                drawRotationAxisGuide(Global.selectionRotation, halfSide, assocModel, g);
        }        
    }
    
    private void drawInfoTag(String text, Point3d cubeCenter, Graphics g)
    {        
        Point2d cc = cubeCenter.toPoint2d();
        g.setColor(Color.WHITE);
        g.drawString(text, cc.X(), cc.Y());
    }
    
    private void drawDotAt(Point3d center, Graphics g)
    {        
        Point2d cc = center.toPoint2d();
        g.fillRect(cc.X()-1, cc.Y()-1, 3, 3);
    }
    
    private void drawRotationAxisGuide(double rotation, double halfSide, Model3d assocModel, Graphics g)
    {
        for (double offset = -5; offset <= 0; offset++)
        {
            int a = (int)(255+(offset*50));
            drawRotationAxisDots(rotation+(offset*12), halfSide, assocModel, g, a);
        }
    }
    
    private void drawRotationAxisDots(double rotation, double halfSide, Model3d assocModel, Graphics g, int a)
    {
        if (rotation >= 360) rotation -= 360;
        if (rotation < 0) rotation += 360;
        
        Point3d cubeCenter = getScaledLocation();
        
        Point3d x = new Point3d(cubeCenter.x, cubeCenter.y, cubeCenter.z+halfSide);
        Point3d y = new Point3d(cubeCenter.x+halfSide, cubeCenter.y, cubeCenter.z);
        Point3d z = new Point3d(cubeCenter.x, cubeCenter.y+halfSide, cubeCenter.z);  

        x = Global.rotateAround(x, cubeCenter, new Point3d(rotation, 0, 0));
        y = Global.rotateAround(y, cubeCenter, new Point3d(0, rotation, 0));
        z = Global.rotateAround(z, cubeCenter, new Point3d(0, 0, rotation));
        
        // If there is a model associated with this cube, rotate around the model's pivot:
        if (assocModel != null)
        {
            cubeCenter = Global.cumulativelyRotate(cubeCenter, assocModel);
            x = Global.cumulativelyRotate(x, assocModel);
            y = Global.cumulativelyRotate(y, assocModel);
            z = Global.cumulativelyRotate(z, assocModel);
        }
        // Rotate around the scene pivot:
        cubeCenter = Global.rotateAround(cubeCenter, Global.ScenePivotCube.getScaledLocation(), Global.getSceneRotation());
        x = Global.rotateAround(x, Global.ScenePivotCube.getScaledLocation(), Global.getSceneRotation());
        y = Global.rotateAround(y, Global.ScenePivotCube.getScaledLocation(), Global.getSceneRotation());
        z = Global.rotateAround(z, Global.ScenePivotCube.getScaledLocation(), Global.getSceneRotation());

        g.setColor(new Color(255, 0, 0, a));
        drawDotAt(x, g);
        g.setColor(new Color(0, 255, 0, a));
        drawDotAt(y, g);
        g.setColor(new Color(0, 0, 255, a));
        drawDotAt(z, g);
        g.setColor(Color.WHITE);
        drawDotAt(cubeCenter, g);
    }        
    
    private void drawMovementAxisGuide(double halfSide, Model3d assocModel, Graphics g)
    {
        Point3d cubeCenter = getScaledLocation();
        Point3d x1 = new Point3d(cubeCenter.x-halfSide, cubeCenter.y, cubeCenter.z);
        Point3d y1 = new Point3d(cubeCenter.x, cubeCenter.y-halfSide, cubeCenter.z);
        Point3d z1 = new Point3d(cubeCenter.x, cubeCenter.y, cubeCenter.z-halfSide);  
        Point3d x2 = new Point3d(cubeCenter.x+halfSide, cubeCenter.y, cubeCenter.z);
        Point3d y2 = new Point3d(cubeCenter.x, cubeCenter.y+halfSide, cubeCenter.z);
        Point3d z2 = new Point3d(cubeCenter.x, cubeCenter.y, cubeCenter.z+halfSide);  
                        
        // If there is a model associated with this cube, rotate around the model's pivot:
        if (assocModel != null)
        {
            cubeCenter = Global.cumulativelyRotate(cubeCenter, assocModel);
            x1 = Global.cumulativelyRotate(x1, assocModel);
            y1 = Global.cumulativelyRotate(y1, assocModel);
            z1 = Global.cumulativelyRotate(z1, assocModel);
            x2 = Global.cumulativelyRotate(x2, assocModel);
            y2 = Global.cumulativelyRotate(y2, assocModel);
            z2 = Global.cumulativelyRotate(z2, assocModel);
        }
        // Rotate around the scene pivot:
        cubeCenter = Global.rotateAround(cubeCenter, Global.ScenePivotCube.getScaledLocation(), Global.getSceneRotation());
        x1 = Global.rotateAround(x1, Global.ScenePivotCube.getScaledLocation(), Global.getSceneRotation());
        y1 = Global.rotateAround(y1, Global.ScenePivotCube.getScaledLocation(), Global.getSceneRotation());
        z1 = Global.rotateAround(z1, Global.ScenePivotCube.getScaledLocation(), Global.getSceneRotation());
        x2 = Global.rotateAround(x2, Global.ScenePivotCube.getScaledLocation(), Global.getSceneRotation());
        y2 = Global.rotateAround(y2, Global.ScenePivotCube.getScaledLocation(), Global.getSceneRotation());
        z2 = Global.rotateAround(z2, Global.ScenePivotCube.getScaledLocation(), Global.getSceneRotation());

        g.setColor(Color.RED);
        drawLine(x1.toPoint2d(), x2.toPoint2d(), g);
        g.setColor(Color.GREEN);
        drawLine(y1.toPoint2d(), y2.toPoint2d(), g);
        g.setColor(Color.BLUE);
        drawLine(z1.toPoint2d(), z2.toPoint2d(), g);

        g.setColor(Color.WHITE);            
        drawDotAt(cubeCenter, g);
    }
        
    private void drawLine(Point2d pt1, Point2d pt2, Graphics g)
    {
        g.drawLine(pt1.X(), pt1.Y(), pt2.X(), pt2.Y());
    }

    private void doFace(Face face, Graphics g)
    {
        if (showFaces && Global.ALL_CUBE_FACES_VISIBLE)
        {
            g.setColor(getAlphaColor());
            
            if (this == Global.SelectionCube && !Global.BLINK)
            {
                if (face.name.equalsIgnoreCase("front"))
                    g.setColor(Color.BLUE);
                if (face.name.equalsIgnoreCase("back"))
                    g.setColor(Color.BLUE);
                if (face.name.equalsIgnoreCase("left"))
                    g.setColor(Color.RED);
                if (face.name.equalsIgnoreCase("right"))
                    g.setColor(Color.RED);
                if (face.name.equalsIgnoreCase("top"))
                    g.setColor(Color.GREEN);
                if (face.name.equalsIgnoreCase("bottom"))
                    g.setColor(Color.GREEN);
            }
            
            makePolygon(face.p1r.toPoint2d(), face.p2r.toPoint2d(), face.p3r.toPoint2d(), face.p4r.toPoint2d(), true, g);
            
            if (this != Global.SelectionCube)
            {
                // Z-Shadow for faces:                            
                g.setColor(getZShadowColor());
                makePolygon(face.p1r.toPoint2d(), face.p2r.toPoint2d(), face.p3r.toPoint2d(), face.p4r.toPoint2d(), true, g);
            }
        }
        if (showEdges && Global.ALL_CUBE_EDGES_VISIBLE)
        {            
            g.setColor(edgeColor);
            
            if (model != null && model.isSelected())
                g.setColor(Color.WHITE);
            
            if (isModelPivot)
                g.setColor(Color.ORANGE);
            
            if (isScenePivot)
                g.setColor(Color.MAGENTA);
            
            if (this == Global.SelectionCube)
            {
                if (face.name.equalsIgnoreCase("front"))
                    g.setColor(Color.BLUE);
                if (face.name.equalsIgnoreCase("back"))
                    g.setColor(Color.BLUE);
                if (face.name.equalsIgnoreCase("left"))
                    g.setColor(Color.RED);
                if (face.name.equalsIgnoreCase("right"))
                    g.setColor(Color.RED);
                if (face.name.equalsIgnoreCase("top"))
                    g.setColor(Color.GREEN);
                if (face.name.equalsIgnoreCase("bottom"))
                    g.setColor(Color.GREEN);
            }
            
            makePolygon(face.p1r.toPoint2d(), face.p2r.toPoint2d(), face.p3r.toPoint2d(), face.p4r.toPoint2d(), false, g);
        }
    }
    
    private void makePolygon(Point2d p1, Point2d p2, Point2d p3, Point2d p4, boolean filled, Graphics g)
    {       
        Graphics2D g2 = (Graphics2D)g.create();
//        if (this == Global.SelectionCube)
//        {        
//            g2.setStroke(dashedStroke);
//        }
        
        int[] xPoints = new int[4];
        int[] yPoints = new int[4];
        xPoints[0] = p1.X();
        xPoints[1] = p2.X();
        xPoints[2] = p3.X();
        xPoints[3] = p4.X();
        yPoints[0] = p1.Y();
        yPoints[1] = p2.Y();
        yPoints[2] = p3.Y();
        yPoints[3] = p4.Y();                 

        if (filled)
            g2.fillPolygon(xPoints, yPoints, 4);
        else        
            g2.drawPolygon(xPoints, yPoints, 4);        
    }
    
    public Color getZShadowColor()
    {        
        int shadowOpacity = 0;
        double distanceFromNearest = 0;
        switch (Global.Z_SHADOW_REF)
        {
            case NEAREST:
                distanceFromNearest = getCubeDistanceFromNearest();
                shadowOpacity = Global.BASE_Z_SHADOW_VAL + (int)(distanceFromNearest * Global.Z_SHADOW_INCREMENT);                
                break;
                
            case SCENE_PIVOT:
                distanceFromNearest = getCubeDistanceFromScenePivot();
                shadowOpacity = Global.BASE_Z_SHADOW_VAL + (int)(distanceFromNearest * Global.Z_SHADOW_INCREMENT);                                
                break;
                
            case MODEL_PIVOT:
                distanceFromNearest = getCubeDistanceFromCurrentModelPivot();
                shadowOpacity = Global.BASE_Z_SHADOW_VAL + (int)(distanceFromNearest * Global.Z_SHADOW_INCREMENT); 
                break;
        }
        if (shadowOpacity > 255) shadowOpacity = 255;
        if (shadowOpacity < 0) shadowOpacity = 0;
        return new Color(0,0,0,shadowOpacity);
    }
    
    @Override
    public int compareTo(Cube cube) {
        if (cube == Global.SelectionCube)
            return 1;
        else if (this == Global.SelectionCube)
            return -1;
        else
            return Double.compare(this.getFinalZ(), cube.getFinalZ());
    }   
    
    @Override
    public String toString() {
        return this.location.toString();
    }    
    
    private static double getZScaleDepthOffset(double distanceFromNearest)
    {
        double offset = (distanceFromNearest * (Global.Z_SCALE_INCREMENT)) * .5;
        return (offset*offset);
    }
    
    public void assignUniqueId(int idNum)
    {
        this.uniqueId = this.model.getName() + "#" + idNum;
    }
    public String getUniqueId()
    {
        return this.uniqueId;
    }
    
    public Cube copy()
    {
        return fromBuildString(createBuildString());
    }
}
