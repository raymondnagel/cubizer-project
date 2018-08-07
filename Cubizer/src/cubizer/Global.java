/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cubizer;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.DefaultListModel;


/**
 *
 * @author rnagel
 */
public class Global {
            
    public static enum AXIS {X, Y, Z};
    public static enum MODE {MOVE, ROTATE};
    public static enum SCOPE {MODEL, SCENE};  
    public static enum Z_SHADOW_MODE {NEAREST, SCENE_PIVOT, MODEL_PIVOT};
    
    // Pointers to Important Interface Components:    
    public static CubizerFrame MAIN_FRAME = null;
    public static Renderer3D RENDERER_3D = null;
    
    // Used by Interface:
    public static boolean IS_REFRESHING = false;
    public static boolean IS_REFRESH_SCHEDULED = false;
    public static boolean CONTROL_LOCK = false;
    public static boolean BLINK = true;        
    
    // Settings:
    public static double CUBE_SIDE_SIZE = 10.0;
    public static double SCALE = 1.0;
    public static int BASE_Z_SHADOW_VAL = 128;
    public static double Z_SHADOW_INCREMENT = 12.0;
    public static double Z_SCALE_INCREMENT = 0;
    public static double NEAREST_Z = 0;    
    public static Color DEFAULT_FACE_COLOR = new Color(221,221,221);
    public static Color DEFAULT_EDGE_COLOR = new Color(0,0,0);
    
    // Options:    
    public static Z_SHADOW_MODE Z_SHADOW_REF = Z_SHADOW_MODE.NEAREST;
    public static boolean HI_RESOLUTION_MODE = false;
    
    public static boolean ALL_CUBE_FACES_VISIBLE = true;
    public static boolean ALL_CUBE_EDGES_VISIBLE = true;
    
    public static boolean SELECTION_CUBE_VISIBLE = true;
    public static boolean SELECTION_CUBE_MOVEMENT = true;
    public static boolean SELECTION_CUBE_ROTATION = false;
    
    public static boolean SCENE_PIVOT_VISIBLE = true;
    public static boolean SCENE_PIVOT_MOVEMENT = false;
    public static boolean SCENE_PIVOT_ROTATION = true;
    
    public static boolean MODEL_PIVOTS_VISIBLE = true;
    public static boolean MODEL_PIVOTS_MOVEMENT = false;
    public static boolean MODEL_PIVOTS_ROTATION = true;
    
    public static boolean CAPTURE_AREA_VISIBLE = true;
        
    // In degrees:
    public static double sceneXRotation = 0;
    public static double sceneYRotation = 0;
    public static double sceneZRotation = 0;
    
    public static double selectionRotation = 0;    
    
    // Cubes & Models:
    public static Cube ScenePivotCube = null;
    public static Cube SelectionCube = null;
    public static ArrayList<Cube> AllCubes = new ArrayList<>();
    public static Model3d CurrentModel = null;
    public static Model3d ClipboardModel = null;
    public static ArrayList<Model3d> AllModels = new ArrayList<>();
    
    
    public static void init()
    {        
        // Special Cube used as a 3D placeholder for the scene's center.
        ScenePivotCube = new Cube(true);
        ScenePivotCube.setLocation(0,0,0);
        ScenePivotCube.setFaceColor(Color.WHITE);
        ScenePivotCube.setScenePivot(true);
        
        // Special Cube used for navigating and selecting.
        SelectionCube = new Cube(true);
        SelectionCube.setLocation(0,0,0);
        SelectionCube.setAlpha(0.0);
        SelectionCube.setShowingFaces(true);
        SelectionCube.setShowingEdges(true);
        
        RENDERER_3D.activate();
    }
    
    
    public static void requestRefresh3D()
    {
        IS_REFRESH_SCHEDULED = true;
    }
    
    public static void repaint3D()
    {
        RENDERER_3D.repaint();
    }
    
    public static BufferedImage getCapture()
    {
        return RENDERER_3D.toSprite();
    }
    public static void setCaptureSize(int width, int height)
    {
        RENDERER_3D.setCaptureSize(width, height);
    }
    
    public static void clearCurrentModel()
    {
        CurrentModel = null;
        MAIN_FRAME.changeModelSelection();
    }
    public static void addModel(Model3d model)
    {
        AllModels.add(model);  
        ((DefaultListModel)(MAIN_FRAME.getModelList().getModel())).addElement(model);
        selectModel(model);
    }
    public static void removeModel(Model3d model)
    {
        // If the model we're removing is the CurrentModel, clear CurrentModel:
        clearCurrentModel();
                
        // Remove this model from the Models collection:
        AllModels.remove(model);        
        
        // Remove the model's pivot specially, because removeCube doesn't work on it:
        AllCubes.remove(model.getPivotCube());
        model.removeCube(model.getPivotCube());
        
        // Remove all other cubes:
        for (int c = model.getCubes().size()-1; c >= 0; c--)
        {
            removeCube(model.getCubes().get(c));
        }
        
        ((DefaultListModel)(MAIN_FRAME.getModelList().getModel())).removeElement(model);
        MAIN_FRAME.getModelList().repaint();
    }    
    public static void selectModel(Model3d model)
    {
        if (CurrentModel != null)
        {
            CurrentModel.setSelected(false);
        }        
        model.setSelected(true);
        CurrentModel = model;        
        SelectionCube.setLocation(CurrentModel.getPivotCube().getLocation());
        MAIN_FRAME.changeModelSelection();
        requestRefresh3D();
    }
    public static Model3d getModelByName(String modelName)
    {
        for (int m = 0; m < AllModels.size(); m++)
        {
            if (AllModels.get(m).getName() != null && AllModels.get(m).getName().equalsIgnoreCase(modelName))
                return AllModels.get(m);
        }
        return null;
    }
    public static ArrayList<Model3d> getSubmodelsForParent(Model3d parentModel)
    {
        ArrayList<Model3d> submodels = new ArrayList<>();
        for (int m = 0; m < Global.AllModels.size(); m++)
        {
            if (parentModel == Global.AllModels.get(m).getParentModel())
            {
                submodels.add(Global.AllModels.get(m));
            }
        }      
        return submodels;
    }
    
    public static void setPose(Model3d model, String rotationStr)
    {        
        String[] rotations = rotationStr.split(",");
        double xRot = Double.parseDouble(rotations[0]);
        double yRot = Double.parseDouble(rotations[1]);
        double zRot = Double.parseDouble(rotations[2]);
        setPose(model, xRot, yRot, zRot);
    }
    public static void setPose(Model3d model, double xRot, double yRot, double zRot)
    {
        model.setRotation(xRot, yRot, zRot);
    }
    
    public static void addCube(Cube cube)
    {
        AllCubes.add(cube);
        MAIN_FRAME.getModelList().repaint();
    }
    public static void removeCube(Cube cube)
    {
        // Cannot delete a null cube or a model pivot:
        if (cube == null || cube.isModelPivot()) return;
        
        if (cube.getModel() != null)
            cube.getModel().removeCube(cube);
        AllCubes.remove(cube);
        MAIN_FRAME.getModelList().repaint();
    }

    public static void moveAll(Point3d move)
    {
        for (int c = AllCubes.size()-1; c >= 0; c--)
        {            
            AllCubes.get(c).move(move);
        }
        requestRefresh3D();
    }
    
    public static Cube getCubeAt(Point3d point)
    {
        for (int c = 0; c < AllCubes.size(); c++)
        {
            if (AllCubes.get(c) != SelectionCube && AllCubes.get(c).getLocation().equals(point))
                return AllCubes.get(c);
        }
        return null;
    }    
    public static Cube getSelectedCube()
    {
        return getCubeAt(SelectionCube.getLocation());
    }    
    public static Cube getCubeByUniqueId(String uniqueId)
    {
        // The uniqueId field can only be trusted to be non-null and unique
        // during a saving or loading operation, immediately after 
        // assignUniqueCubeIds() is called.
        for (int c = 0; c < AllCubes.size(); c++)
        {
            if (AllCubes.get(c).getUniqueId() != null && AllCubes.get(c).getUniqueId().equals(uniqueId))
                return AllCubes.get(c);
        }
        return null;
    }
    
    public static void setSceneRotation(double xRotation, double yRotation, double zRotation)
    {
        sceneXRotation = xRotation;
        sceneYRotation = yRotation;
        sceneZRotation = zRotation;
    }
    public static Point3d getSceneRotation()
    {
        return new Point3d(sceneXRotation, sceneYRotation, sceneZRotation);        
    }
    public static Point3d getOppositeSceneRotation()
    {
        return new Point3d(sceneXRotation, sceneYRotation, sceneZRotation);        
    }
    public static void rotateScene(Point3d rotation)
    {
        rotateXAxis(rotation.x);
        rotateYAxis(rotation.y);
        rotateZAxis(rotation.z);
    }
    public static void rotateXAxis(double degrees)
    {
        sceneXRotation += degrees;
        while (sceneXRotation >= 360)
            sceneXRotation -= 360;
        while (sceneXRotation < 0)
            sceneXRotation += 360;
    }
    public static void rotateYAxis(double degrees)
    {
        sceneYRotation += degrees;
        while (sceneYRotation >= 360)
            sceneYRotation -= 360;
        while (sceneYRotation < 0)
            sceneYRotation += 360;
    }
    public static void rotateZAxis(double degrees)
    {
        sceneZRotation += degrees;
        while (sceneZRotation >= 360)
            sceneZRotation -= 360;
        while (sceneZRotation < 0)
            sceneZRotation += 360;
    }
    
    public static Point3d cumulativelyRotate(Point3d point, Model3d model)
    {
        while (model != null)
        {
            Point3d modelPivot = model.getScaledLocation();
            Point3d modelRotation = model.getRotation();   
            point = rotateAround(point, modelPivot, modelRotation);
            model = model.getParentModel();
        }
        return point;
    }
    
    public static Point3d rotateAround(Point3d point, Point3d center, Point3d amount)
    {
        Point3d dest = new Point3d(point.x, point.y, point.z);
        
        // X-Axis rotation:
        Point2d zyX = rotatePoint(center.z, center.y, amount.x, new Point2d(dest.z, dest.y));
        dest.z = zyX.x;
        dest.y = zyX.y;
        
        // Y-Axis rotation:
        Point2d xzY = rotatePoint(center.x, center.z, amount.y, new Point2d(dest.x, dest.z));
        dest.x = xzY.x;
        dest.z = xzY.y;
        
        // Z-Axis rotation:
        Point2d xyZ = rotatePoint(center.x, center.y, amount.z, new Point2d(dest.x, dest.y));
        dest.x = xyZ.x;
        dest.y = xyZ.y;
        
        return dest;
    }
    
    public static Point2d rotatePoint(double cX, double cY, double angle, Point2d p)
    {
      angle = Math.toRadians(angle);
      double s = Math.sin(angle);
      double c = Math.cos(angle);

      // translate point back to origin:
      p.setPoint(p.x-cX, p.y-cY);

      // rotate point
      double xnew = p.x * c - p.y * s;
      double ynew = p.x * s + p.y * c;

      // translate point back:
      p.setPoint(xnew+cX, ynew+cY);
      return p;
    }

    public static Point2d translate3dTo2d(Point3d pt3, Point3d ctr)
    {
        Point2d pt2 = new Point2d(pt3.x, pt3.y);  

        // Rotate according to the current axis rotation values; keep X and Y.
        Point3d yR = rotateAround(pt3, ctr, new Point3d(sceneXRotation,sceneYRotation,sceneZRotation));                
        pt2.x = yR.x;
        pt2.y = yR.y;
        
        return pt2;
    }
    
    public static Color makeAlphaColor(Color c, double alpha)
    {
        int a = (int)(255.0 * alpha);
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), a);
    }
    
    public static double distanceBetween(Point3d pt1, Point3d pt2)
    {
        double x = Math.abs(pt1.x - pt2.x);
        double y = Math.abs(pt1.y - pt2.y);
        double z = Math.abs(pt1.z - pt2.z);
        double total = (x*x)+(y*y)+(z*z);
        return Math.sqrt(total);
    }
}
