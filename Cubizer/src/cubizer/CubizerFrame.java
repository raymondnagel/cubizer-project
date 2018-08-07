/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cubizer;

import cubizer.Global.AXIS;
import cubizer.Global.MODE;
import cubizer.Global.SCOPE;
import cubizer.ShapeDialog.Result;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author rnagel
 */
public final class CubizerFrame extends javax.swing.JFrame {
    public static final File MODEL_DIR = new File("models");
    public static final File ANIM_DIR = new File("anims");
    public static final File SPRITE_DIR = new File("sprites");
    
    private final Orientation DEFAULT_ORIENTATION = new Orientation("default", 350, 10, 0);
    private final Orientation FRONT_ORIENTATION = new Orientation("front", 0, 0, 0);
    private final Orientation BACK_ORIENTATION = new Orientation("back", 0, 180, 0);
    private final Orientation LEFT_ORIENTATION = new Orientation("left", 0, 90, 0);
    private final Orientation RIGHT_ORIENTATION = new Orientation("right", 0, 270, 0);
    private final Orientation TOP_ORIENTATION = new Orientation("top", 270, 0, 0);
    private final Orientation BOTTOM_ORIENTATION = new Orientation("bottom", 90, 0, 0);
    
    private static final double AXIS_TRIGGER = 20;
    private Point mouseOrigin = null;
    private AXIS mouseAxis = null;
    private MODE mouseMode = MODE.ROTATE;    
    private SCOPE mouseScope = Global.SCOPE.SCENE;
    private boolean ctrlPressed = false;
    private boolean altPressed = false;
    private boolean shiftPressed = false;    
    
    private SwingWorker refreshThread = null;
    private SwingWorker blinkThread = null;    

    /**
     * Creates new form CubizerFrame
     */
    public CubizerFrame() {
        initComponents();  

        ((DefaultEditor)spnZShadowInc.getEditor()).getTextField().setFocusable(false);
        ((DefaultEditor)spnBaseShadowVal.getEditor()).getTextField().setFocusable(false);
        ((DefaultEditor)spnCaptureWidth.getEditor()).getTextField().setFocusable(false);
        ((DefaultEditor)spnCaptureHeight.getEditor()).getTextField().setFocusable(false);
        ((DefaultEditor)spnSpriteWidth.getEditor()).getTextField().setFocusable(false);
        ((DefaultEditor)spnSpriteHeight.getEditor()).getTextField().setFocusable(false);
        
        lstModels.setModel(new DefaultListModel());
        lstDirections.setModel(new DefaultListModel());
        lstPoses.setModel(new DefaultListModel());
        
        Global.MAIN_FRAME = this;
        Global.RENDERER_3D = renderer3D;
        Global.init();
        
        lstModels.setSelectedIndex(lstModels.getModel().getSize()-1);
        
        cboRestoreSceneOrientation.setModel(new DefaultComboBoxModel());
        btnDefaultColor.setBackground(Global.DEFAULT_FACE_COLOR);
        
        // Init Scale to 200%:
        sldScale.setValue(200);

        // Init Scene Rotations to default orientation:
        restoreSceneOrientation(DEFAULT_ORIENTATION);
        
        Global.requestRefresh3D();
        saveSceneOrientation("default");
        
        // Start the refresh thread, which refreshes if there is a refresh request scheduled.
        refreshThread = new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                do {
                    Thread.sleep(40);
                    Global.repaint3D();
                    
                } while (true == true);
            }            
        };
        refreshThread.execute();
        // Start the blink thread, which alternates the Global.BLINK boolean at intervals. 
        blinkThread = new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                do
                {
                    try {
                        Thread.sleep(100);
                        Global.selectionRotation += 36;
                        if (Global.selectionRotation == 360)
                            Global.selectionRotation = 0;
                        Global.BLINK = !Global.BLINK;
                        Global.requestRefresh3D();
                    } catch (Exception ex)                   
                    {
                        ex.printStackTrace();
                    }
                } while (true == true);
            }
        };        
        blinkThread.execute();
        
        //setExtendedState(Frame.MAXIMIZED_BOTH);
    }

    public void doSelectionCubeMove(int xMove, int yMove, int zMove)
    {
        Point3d oldLocation = Global.SelectionCube.getLocation();
        
        Global.SelectionCube.move(xMove*Global.CUBE_SIDE_SIZE, yMove*Global.CUBE_SIDE_SIZE, zMove*Global.CUBE_SIDE_SIZE);
        
        if (Global.SelectionCube.getLocation() == Global.ScenePivotCube.getLocation())
            Global.SelectionCube.setLocation(oldLocation);
        
        Global.requestRefresh3D();
    }
    
    public void doKeyModelMove(int xMove, int yMove, int zMove)
    {
        xMove = (int)(xMove * Global.CUBE_SIDE_SIZE);
        yMove = (int)(yMove * Global.CUBE_SIDE_SIZE);
        zMove = (int)(zMove * Global.CUBE_SIDE_SIZE);
        Point3d movePoint = new Point3d(xMove, yMove, zMove);
        Global.CurrentModel.move(movePoint);
        Global.SelectionCube.move(movePoint);
    }
    public void doKeySceneMove(int xMove, int yMove, int zMove)
    {                  
        xMove = (int)(xMove * Global.CUBE_SIDE_SIZE);
        yMove = (int)(yMove * Global.CUBE_SIDE_SIZE);
        zMove = (int)(zMove * Global.CUBE_SIDE_SIZE);
        Point3d movePoint = new Point3d(xMove, yMove, zMove);
        Global.moveAll(movePoint);
    }
    public void doKeyModelRotate(double xDeg, double yDeg, double zDeg)
    {
        Global.CurrentModel.rotate(xDeg, 0, 0);
        sldModelXAxis.setValue((int)Global.CurrentModel.getXRotation());
        Global.CurrentModel.rotate(0, yDeg, 0);
        sldModelYAxis.setValue((int)Global.CurrentModel.getYRotation());
        Global.CurrentModel.rotate(0, 0, zDeg);
        sldModelZAxis.setValue((int)Global.CurrentModel.getZRotation());
    }
    public void doKeySceneRotate(double xDeg, double yDeg, double zDeg)
    {                            
        Global.rotateXAxis(xDeg);
        sldSceneXAxis.setValue((int)Global.sceneXRotation);
        Global.rotateYAxis(yDeg);
        sldSceneYAxis.setValue((int)Global.sceneYRotation);
        Global.rotateZAxis(zDeg);
        sldSceneZAxis.setValue((int)Global.sceneZRotation);
    }
    
    
    public void doMouseRotation(double xMove, double yMove)
    {
        switch (mouseScope)
        {
            case SCENE:
                doMouseGlobalRotation(xMove, yMove);
                break;
            case MODEL:
                doMouseModelRotation(xMove, yMove);
                break;
        }
    }
    public void doMouseMove(int xMove, int yMove)
    {
        xMove = (int)(xMove * Global.CUBE_SIDE_SIZE);
        yMove = (int)(yMove * Global.CUBE_SIDE_SIZE);
        
        switch (mouseScope)
        {
            case SCENE:
                doMouseSceneMove(xMove, yMove);
                break;
            case MODEL:
                doMouseModelMove(xMove, yMove);
                break;
        }
    }    
    public void doMouseModelRotation(double xMove, double yMove)
    {
        if (Global.CurrentModel == null)
            return;
        
        double move = Math.abs(xMove) > Math.abs(yMove) ? xMove : yMove;
        switch (mouseAxis)
        {
            case X:
                Global.CurrentModel.rotate(-move, 0, 0);
                sldModelXAxis.setValue((int)Global.CurrentModel.getXRotation());
                break;
            case Y:
                Global.CurrentModel.rotate(0, move, 0);
                sldModelYAxis.setValue((int)Global.CurrentModel.getYRotation());
                break;
            case Z:
                Global.CurrentModel.rotate(0, 0, move);
                sldModelZAxis.setValue((int)Global.CurrentModel.getZRotation());
                break;
        }
    }
    public void doMouseModelMove(int xMove, int yMove)
    {
        // Return if there is no current model, or if it's a submodel.
        if (Global.CurrentModel == null)
            return;
        
        double move = Math.abs(xMove) > Math.abs(yMove) ? xMove : yMove;
        Point3d movePoint = null;
        switch (mouseAxis)
        {
            case X:
                movePoint = new Point3d(move, 0, 0);
                break;
            case Y:
                movePoint = new Point3d(0, move, 0);
                break;
            case Z:
                movePoint = new Point3d(0, 0, move);
                break;
        }
        Global.CurrentModel.move(movePoint);
        Global.SelectionCube.move(movePoint);
    }    
    public void doMouseGlobalRotation(double xMove, double yMove)
    {
        double move = Math.abs(xMove) > Math.abs(yMove) ? xMove : yMove;
        switch (mouseAxis)
        {
            case X:
                Global.rotateXAxis(-move);
                sldSceneXAxis.setValue((int)Global.sceneXRotation);
                break;
            case Y:
                Global.rotateYAxis(move);
                sldSceneYAxis.setValue((int)Global.sceneYRotation);
                break;
            case Z:
                Global.rotateZAxis(move);
                sldSceneZAxis.setValue((int)Global.sceneZRotation);
                break;
        }
    }
    public void doMouseSceneMove(int xMove, int yMove)
    {
        double move = Math.abs(xMove) > Math.abs(yMove) ? xMove : yMove;
        switch (mouseAxis)
        {
            case X:
                Global.moveAll(new Point3d(move, 0, 0));
                break;
            case Y:
                Global.moveAll(new Point3d(0, move, 0));
                break;
            case Z:
                Global.moveAll(new Point3d(0, 0, move));
                break;
        }
    }
 
    public void setDefaultColor(Color color)
    {
        btnDefaultColor.setBackground(color);
        lblColorDef.setBackground(color);
        Global.DEFAULT_FACE_COLOR = color;
    }

    public void saveSceneOrientation(String name)
    {
        Orientation orientation = new Orientation(name, Global.sceneXRotation, Global.sceneYRotation, Global.sceneZRotation);
        ((DefaultComboBoxModel)cboRestoreSceneOrientation.getModel()).addElement(orientation);
    }
    public void restoreSceneOrientation(Orientation orientation)
    {
        if (orientation != null)
        {
            sldSceneXAxis.setValue((int)orientation.x);
            sldSceneYAxis.setValue((int)orientation.y);
            sldSceneZAxis.setValue((int)orientation.z);
        }
    }
    
    public void setModelRotationsToSliders(Model3d model)
    {
        Global.CONTROL_LOCK = true;
        if (model != null)
        {
            sldModelXAxis.setEnabled(true);
            sldModelYAxis.setEnabled(true);
            sldModelZAxis.setEnabled(true);
            sldModelXAxis.setValue((int)model.getXRotation());
            lblModelXDeg.setText(sldModelXAxis.getValue() + "°");
            sldModelYAxis.setValue((int)model.getYRotation());
            lblModelYDeg.setText(sldModelYAxis.getValue() + "°");
            sldModelZAxis.setValue((int)model.getZRotation());
            lblModelZDeg.setText(sldModelZAxis.getValue() + "°");
        }
        else
        {
            sldModelXAxis.setEnabled(false);
            sldModelYAxis.setEnabled(false);
            sldModelZAxis.setEnabled(false);
            sldModelXAxis.setValue(0);
            lblModelXDeg.setText(sldModelXAxis.getValue() + "°");
            sldModelYAxis.setValue(0);
            lblModelYDeg.setText(sldModelYAxis.getValue() + "°");
            sldModelZAxis.setValue(0);
            lblModelZDeg.setText(sldModelZAxis.getValue() + "°");
        }
        Global.CONTROL_LOCK = false;
    }        
    public void changeModelPivotCube()
    {
        Cube cube = Global.getSelectedCube();
        if (cube != null)
        {
            Global.CurrentModel.setPivotCube(cube);
        }
        Global.requestRefresh3D();
    }    
    public JList getModelList()
    {
        return lstModels;
    }
    
    // Construct/insert shapes:
    public void constructCube(Point3d origin) // 0-dim
    {
        Cube cube = Global.getCubeAt(origin);
        boolean anotherModel = cube != null && cube.getModel() != null && cube.getModel() != Global.CurrentModel;
        if (cube == null || anotherModel)
        {
            cube = new Cube(true);
            cube.setLocation(origin);
            if (Global.CurrentModel == null)
            {
                JOptionPane.showMessageDialog(this, "No model existed.\nA new model called \"default\" has been created to hold the cube(s).", "Default model created", JOptionPane.INFORMATION_MESSAGE);
                Model3d defaultModel = new Model3d("default", cube);
                Global.addModel(defaultModel);
                Global.selectModel(defaultModel);
            }
            else
            {
                Global.CurrentModel.addCube(cube);
            }
        }                
        cube.setFaceColor(btnDefaultColor.getBackground());
        cube.setEdgeColor(Global.DEFAULT_EDGE_COLOR);
    }
    public void constructCube(Point3d origin, Color color, int alpha) // 0-dim
    {
        Cube cube = Global.getCubeAt(origin);
        if (cube == null)
        {
            cube = new Cube(true);
            cube.setLocation(origin);
            if (Global.CurrentModel == null)
            {
                JOptionPane.showMessageDialog(this, "No model existed.\nA new model called \"default\" has been created to hold the cube(s).", "Default model created", JOptionPane.INFORMATION_MESSAGE);
                Model3d defaultModel = new Model3d("default", cube);
                Global.addModel(defaultModel);
                Global.selectModel(defaultModel);
            }
            else
            {
                Global.CurrentModel.addCube(cube);
            }
        }                
        cube.setFaceColor(new Color(color.getRed(), color.getGreen(), color.getBlue()));
        cube.setAlpha(((double)alpha)/255.0);
        cube.setEdgeColor(Global.DEFAULT_EDGE_COLOR);
    }
    public void constructLine(Point3d origin, int length, Point3d increment) // 1-dim
    {
        origin = new Point3d(origin);
        for (int c = 0; c < length; c++)
        {
            constructCube(origin);
            origin.addScaledPoint(increment, Global.CUBE_SIDE_SIZE);
        }
    }
    public void constructRectangle(Point3d origin, int length1, Point3d increment1, int length2, Point3d increment2) // 2-dim
    {           
        for (int x = 0; x < length1; x++)
        {
            for (int y = 0; y < length2; y++)
            {
                Point3d insert = new Point3d(origin);
                Point3d incs = new Point3d((x*increment1.x)+(y*increment2.x), (x*increment1.y)+(y*increment2.y), (x*increment1.z)+(y*increment2.z));
                insert.addScaledPoint(incs, Global.CUBE_SIDE_SIZE);
                constructCube(insert);                
            }                  
        }
    }
    public void constructEllipse(Point3d origin, int length1, Point3d increment1, int length2, Point3d increment2) // 2-dim
    {              
        Ellipse2D ellipse = new Ellipse2D.Double(0, 0, length1, length2);
        
        for (int x = 0; x < length1; x++)
        {
            for (int y = 0; y < length2; y++)
            {
                if (ellipse.contains(x, y))
                {
                    Point3d insert = new Point3d(origin);
                    Point3d incs = new Point3d((x*increment1.x)+(y*increment2.x), (x*increment1.y)+(y*increment2.y), (x*increment1.z)+(y*increment2.z));
                    insert.addScaledPoint(incs, Global.CUBE_SIDE_SIZE);
                    constructCube(insert);                
                }
            }                  
        }
    }
    public void constructCuboid(Point3d origin, int length1, Point3d increment1, int length2, Point3d increment2, int length3, Point3d increment3) // 3-dim
    {    
        for (int x = 0; x < length1; x++)
        {
            for (int y = 0; y < length2; y++)
            {
                for (int z = 0; z < length3; z++)
                {                
                    Point3d insert = new Point3d(origin);
                    Point3d incs = new Point3d((x*increment1.x)+(y*increment2.x)+(z*increment3.x), (x*increment1.y)+(y*increment2.y)+(z*increment3.y), (x*increment1.z)+(y*increment2.z)+(z*increment3.z));
                    insert.addScaledPoint(incs, Global.CUBE_SIDE_SIZE);
                    constructCube(insert);                
                }
            }                  
        }
    }    
    public void constructSphere(Point3d origin, int radius) // 3-dim (identical)
    {    
        Point3d stableOrigin = new Point3d(origin);
        double scaledRadius = radius*Global.CUBE_SIDE_SIZE;
        Point3d center = new Point3d(stableOrigin.x+scaledRadius, stableOrigin.y+scaledRadius, stableOrigin.z+scaledRadius);
        for (int x = 0; x < radius*2; x++)
        {          
            for (int y = 0; y < radius*2; y++)
            {                
                for (int z = 0; z < radius*2; z++)
                {                                    
                    Point3d insert = new Point3d(stableOrigin);
                    Point3d incs = new Point3d(x, y, z);
                    insert.addScaledPoint(incs, Global.CUBE_SIDE_SIZE);
                    if (Global.distanceBetween(center, insert) <= scaledRadius)
                    {                        
                        constructCube(insert);
                    }                
                }
            }                  
        }
        Global.CurrentModel.setPivotCube(Global.getCubeAt(center));
    }    
    public void importImage(BufferedImage image, Point3d origin, int length1, Point3d increment1, int length2, Point3d increment2) // 2-dim
    {           
        for (int x = 0; x < length1; x++)
        {
            for (int y = 0; y < length2; y++)
            {
                Point3d insert = new Point3d(origin);
                Point3d incs = new Point3d((x*increment1.x)+(y*increment2.x), (x*increment1.y)+(y*increment2.y), (x*increment1.z)+(y*increment2.z));
                insert.addScaledPoint(incs, Global.CUBE_SIDE_SIZE);
                Color color = new Color(image.getRGB(x,y), true);
                if (color.getAlpha() > 0)
                {
                    constructCube(insert, color, color.getAlpha());
                }
            }                  
        }
    }
    
    public void copyModel()
    {
        if (Global.CurrentModel == null) return;
        
        ArrayList<Cube> copyCubes = new ArrayList<>();
        Cube copyPivot = null;
        
        CopyModelDialog dlg = new CopyModelDialog(this, true);
        dlg.setVisible(true);
        ShapeDialog.Result result = dlg.getDialogResult();
        switch(result)
        {
            case OK:
                for (int c = 0; c < Global.CurrentModel.getCubes().size(); c++)
                {
                    Cube copy = Global.CurrentModel.getCubes().get(c).copy();
                    if (copy.isModelPivot())
                    {
                        copyPivot = copy;
                    }
                    copyCubes.add(copy);
                }
                Global.ClipboardModel = new Model3d(dlg.getModelName(), copyPivot);                
                for (int c = 0; c < copyCubes.size(); c++)
                {
                    Cube cube = copyCubes.get(c);
                    Global.ClipboardModel.addCube(cube);
                    Point3d rel = cube.getLocationInModel();
                    rel.setPoint(dlg.isMirrorX() ? -rel.x : rel.x, 
                                 dlg.isMirrorY() ? -rel.y : rel.y,
                                 dlg.isMirrorZ() ? -rel.z : rel.z);
                    cube.setLocationInModel(rel);
                }                
                break;
            case CANCEL:
                break;
        }
    }
    public void pasteModel()
    {
        if (Global.ClipboardModel == null) return;
        Point3d pivotLoc = new Point3d(Global.SelectionCube.getLocation());
        Global.addModel(Global.ClipboardModel);
        for (int c = 0; c < Global.ClipboardModel.getCubes().size(); c++)
        {
            Global.addCube(Global.ClipboardModel.getCubes().get(c));
        }
        Global.ClipboardModel.movePivotTo(pivotLoc);
        Global.selectModel(Global.ClipboardModel);
    }
    public void pasteModelAsSubmodel()
    {        
        if (Global.ClipboardModel == null || Global.CurrentModel == null) return;
        Model3d parentModel = Global.CurrentModel;
        
        Point3d pivotLoc = new Point3d(Global.SelectionCube.getLocation());
        Global.addModel(Global.ClipboardModel);
        for (int c = 0; c < Global.ClipboardModel.getCubes().size(); c++)
        {
            Global.addCube(Global.ClipboardModel.getCubes().get(c));
        }
        Global.ClipboardModel.movePivotTo(pivotLoc);
        Global.ClipboardModel.setParentModel(parentModel);
    }
    public void colorizeModel(Color color)
    {
        if (Global.CurrentModel != null)
            Global.CurrentModel.colorize(color);
    }        
    public void changeModelSelection()
    {
        setModelRotationsToSliders(Global.CurrentModel);
        if (Global.CurrentModel != null)
        {
            lblCurrentModel.setText(Global.CurrentModel.getName());   
            lstModels.setSelectedValue(Global.CurrentModel, true);
            chkShowModel.setEnabled(true);
            chkShowModel.setSelected(Global.CurrentModel.isVisible());
        }
        else
        {
            lblCurrentModel.setText("<none>");
            lstModels.setSelectedIndex(-1);
            chkShowModel.setEnabled(false);
            chkShowModel.setSelected(false);            
        }
    }
    
    
    
    public void saveModelAndSubmodels(Model3d model, File file)
    {
        ArrayList<Model3d> submodels = Global.getSubmodelsForParent(model);          
        saveSingleModel(model, file, submodels);
        for (int m = 0; m < submodels.size(); m++)
        {            
            File childFile = new File(file.getParentFile(), submodels.get(m).getName() + ".czmod");
            saveModelAndSubmodels(submodels.get(m), childFile);            
        }
    }    
    public void saveSingleModel(Model3d model, File file, ArrayList<Model3d> submodels)
    {        
        try {
            if (!file.exists())
            {
                boolean created = file.createNewFile();
                if (!created)
                {
                    System.err.println("The model file could not be created.");
                    return;
                }
            }
            // Assign Cube uniqueIds:
            model.assignUniqueCubeIds();
            // Build the model string:
            StringBuilder modelString = new StringBuilder();
            // Add the model's rotation:
            Point3d rotation = model.getRotation();
            modelString.append(rotation.x).append(",").append(rotation.y).append(",").append(rotation.z).append("\n");
            // Add the model's parent:
            modelString.append("Parent:").append(model.getParentModel() == null ? "" : model.getParentModel().getName()).append("\n");
            // Add each submodel:
            for (int m = 0; m < submodels.size(); m++)
            {
                modelString.append("Child:").append(submodels.get(m).getName()).append("\n");
            }
            // Add a build string for each Cube in the model:               
            for (int c = 0; c < model.getCubes().size(); c++)
            {
                modelString.append(model.getCubes().get(c).createBuildString()).append("\n");
            }
            ReadWriteTextFile.setContents(file, modelString.toString().trim());
        } catch (IOException ex) {
            Logger.getLogger(CubizerFrame.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }
    
    public void loadModelAndSubmodels(File file)
    {
        ArrayList<String> submodelNames = loadSingleModel(file);
        for (int m = 0; m < submodelNames.size(); m++)
        {            
            File childFile = new File(file.getParentFile(), submodelNames.get(m) + ".czmod");
            loadModelAndSubmodels(childFile);            
        }
        String mainModelName = file.getName().substring(0, file.getName().lastIndexOf('.'));
        Model3d mainModel = Global.getModelByName(mainModelName);
        Global.selectModel(mainModel);
    }        
    public ArrayList<String> loadSingleModel(File file)
    {                 
        ArrayList<String> submodelNames = new ArrayList<>();
        // Get the model string:
        String[] modelStringLines = ReadWriteTextFile.getContents(file).split("\n");
        String[] point = modelStringLines[0].split(",");
        Point3d rotation = new Point3d(Double.parseDouble(point[0]), Double.parseDouble(point[1]), Double.parseDouble(point[2]));
        String parent = modelStringLines[1].split(":")[1].trim();
        Cube pivotCube = null;
        ArrayList<Cube> cubes = new ArrayList<>();
        // Check remaining lines, adding any submodels to a collection:
        for (int c = 2; c < modelStringLines.length; c++)
        {
            if (modelStringLines[c].trim().startsWith("Child"))
            {
                submodelNames.add(modelStringLines[c].trim().split(":")[1]);
            }
            else
            {
                Cube cube = Cube.fromBuildString(modelStringLines[c]);
                Global.addCube(cube);
                cubes.add(cube);
                if (cube.isModelPivot())
                    pivotCube = cube;
            }
        }        
        // Create the model:
        String name = file.getName().substring(0, file.getName().lastIndexOf('.')); 
        Model3d model = new Model3d(name, pivotCube);
        for (int c = 0; c < cubes.size(); c++)
        {
            model.addCube(cubes.get(c));
        }
        model.setRotation(rotation.x, rotation.y, rotation.z);
        if (parent != null && !parent.isEmpty())
        {
            model.setParentModel(Global.getModelByName(parent));
        }
        Global.addModel(model);
        return submodelNames;
    }
    
    public void loadAnimationFile(File file)
    {
        if (Global.CurrentModel == null) return;
        
        lstDirections.setModel(new DefaultListModel());
        lstPoses.setModel(new DefaultListModel());
        
        ArrayList<String> modelLabels = new ArrayList<>();
        String[] lines = ReadWriteTextFile.getContents(file).split("\n");
        String mode = "dir";
        for (int a = 0; a < lines.length; a++)
        {
            // Change mode:
            if (lines[a].trim().startsWith("*"))
            {
                if (lines[a].trim().toUpperCase().contains("DIRECTIONS"))
                    mode = "dir";
                if (lines[a].trim().toUpperCase().contains("POSES"))
                    mode = "pose";
            }
            else if (!lines[a].trim().startsWith("#"))
            {
                if (mode.equalsIgnoreCase("dir"))
                {
                    String[] tokens = lines[a].split(":");
                    String[] rotation = tokens[1].split(",");
                    double xRot = Double.parseDouble(rotation[0]);
                    double yRot = Double.parseDouble(rotation[1]);
                    double zRot = Double.parseDouble(rotation[2]);
                    Direction dir = new Direction(tokens[0], xRot, yRot, zRot);
                    ((DefaultListModel)lstDirections.getModel()).addElement(dir);
                }
                if (mode.equalsIgnoreCase("pose"))
                {
                    String[] tokens = lines[a].split(":");
                    if (!modelLabels.contains(tokens[0]))
                    {
                        modelLabels.add(tokens[0]);
                    }
                }
            }
        }
        AnimationSetupDialog animDlg = new AnimationSetupDialog(this, true, modelLabels);
        animDlg.setVisible(true);
        
        // Iterate through lines again, using the converter to create poses based on the aliased models:
        ModelAliasConverter converter = animDlg.getModelAliasConverter();  
        int poseIndex = -1;
        Pose currentPose = null;
        for (int a = 0; a < lines.length; a++)
        {
            // Change mode:
            if (lines[a].trim().startsWith("*"))
            {
                if (lines[a].trim().toUpperCase().contains("DIRECTIONS"))
                    mode = "dir";
                if (lines[a].trim().toUpperCase().contains("POSES"))
                    mode = "pose";
            }
            else if (lines[a].trim().startsWith("#"))
            {
                poseIndex++;
                currentPose = new Pose();
                ((DefaultListModel)lstPoses.getModel()).addElement(currentPose);
            }
            else 
            {                
                if (mode.equalsIgnoreCase("pose"))
                {
                    String[] tokens = lines[a].split(":");
                    String[] rotation = tokens[1].split(",");
                    double xRot = Double.parseDouble(rotation[0]);
                    double yRot = Double.parseDouble(rotation[1]);
                    double zRot = Double.parseDouble(rotation[2]);
                    currentPose.addPosition(tokens[0], new Point3d(xRot, yRot, zRot), converter);
                }
            }
        }        
    }
    public void saveAnimationFile(File file)
    {
        try {
            if (!file.exists())
            {
                boolean created = file.createNewFile();
                if (!created)
                {
                    System.err.println("The animation file could not be created.");
                    return;
                }
            }

            ArrayList<Direction> directions = getDirections();        
            ArrayList<Pose> poses = getPoses();

            StringBuilder fileText = new StringBuilder();
            fileText.append("*DIRECTIONS\n");
            for (int d = 0; d < directions.size(); d++)
            {
                fileText.append(directions.get(d).toString()).append("\n");
            }
            fileText.append("*POSES\n");
            for (int p = 0; p < poses.size(); p++)
            {
                fileText.append("#").append(p).append("\n");
                for (int m = 0; m < poses.get(p).getPartCount(); m++)
                {
                    fileText.append(poses.get(p).getPositionStringAtIndex(m)).append("\n");
                }
            }

            ReadWriteTextFile.setContents(file, fileText.toString());
        } catch (IOException ex) {
            Logger.getLogger(CubizerFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public ArrayList<Direction> getDirections()
    {
        DefaultListModel model = (DefaultListModel)lstDirections.getModel();
        ArrayList<Direction> directions = new ArrayList<>();
        for (int d = 0; d < model.getSize(); d++)
        {
            directions.add((Direction)model.getElementAt(d));
        }
        return directions;
    }
    public ArrayList<Pose> getPoses()
    {
        DefaultListModel model = (DefaultListModel)lstPoses.getModel();
        ArrayList<Pose> poses = new ArrayList<>();
        for (int p = 0; p < model.getSize(); p++)
        {
            poses.add((Pose)model.getElementAt(p));
        }
        return poses;
    }
    
    public void changeZShadowOption()
    {
        if (rdoZShadowNearest.isSelected())
            Global.Z_SHADOW_REF = Global.Z_SHADOW_MODE.NEAREST;
        else if (rdoZShadowScenePivot.isSelected())
            Global.Z_SHADOW_REF = Global.Z_SHADOW_MODE.SCENE_PIVOT;
        else if (rdoZShadowModelPivot.isSelected())
            Global.Z_SHADOW_REF = Global.Z_SHADOW_MODE.MODEL_PIVOT;       
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bgpZShadowRef = new javax.swing.ButtonGroup();
        renderer3D = new cubizer.Renderer3D();
        lblCurrentModel = new javax.swing.JLabel();
        tabControls = new javax.swing.JTabbedPane();
        pnlScene = new javax.swing.JPanel();
        pnlSceneRotation = new javax.swing.JPanel();
        sldSceneZAxis = new javax.swing.JSlider();
        sldSceneXAxis = new javax.swing.JSlider();
        lblSceneYDeg = new javax.swing.JLabel();
        lblSceneZAxis = new javax.swing.JLabel();
        lblSceneZDeg = new javax.swing.JLabel();
        lblSceneXDeg = new javax.swing.JLabel();
        sldSceneYAxis = new javax.swing.JSlider();
        lblSceneYAxis = new javax.swing.JLabel();
        lblSceneXAxis = new javax.swing.JLabel();
        sldScale = new javax.swing.JSlider();
        lblScale = new javax.swing.JLabel();
        pnlScenePresetViews = new javax.swing.JPanel();
        pnlScenePresetViewsGrid = new javax.swing.JPanel();
        btnSceneFrontView = new javax.swing.JButton();
        btnSceneLeftView = new javax.swing.JButton();
        btnSceneTopView = new javax.swing.JButton();
        btnSceneBackView = new javax.swing.JButton();
        btnSceneRightView = new javax.swing.JButton();
        btnSceneBottomView = new javax.swing.JButton();
        btnSaveSceneOrientation = new javax.swing.JButton();
        cboRestoreSceneOrientation = new javax.swing.JComboBox();
        pnlModel = new javax.swing.JPanel();
        pnlModels = new javax.swing.JPanel();
        scpModels = new javax.swing.JScrollPane();
        lstModels = new javax.swing.JList();
        pnlModelRotation = new javax.swing.JPanel();
        sldModelZAxis = new javax.swing.JSlider();
        sldModelXAxis = new javax.swing.JSlider();
        lblModelYDeg = new javax.swing.JLabel();
        lblModelZAxis = new javax.swing.JLabel();
        lblModelZDeg = new javax.swing.JLabel();
        lblModelXDeg = new javax.swing.JLabel();
        sldModelYAxis = new javax.swing.JSlider();
        lblModelYAxis = new javax.swing.JLabel();
        lblModelXAxis = new javax.swing.JLabel();
        chkShowModel = new javax.swing.JCheckBox();
        pnlColor = new javax.swing.JPanel();
        pnlDefaultColor = new javax.swing.JPanel();
        btnDefaultColor = new javax.swing.JButton();
        lblDefaultColor = new javax.swing.JLabel();
        pnlQuickColors = new javax.swing.JPanel();
        btnQuickColor1 = new javax.swing.JButton();
        btnQuickColor2 = new javax.swing.JButton();
        btnQuickColor3 = new javax.swing.JButton();
        btnQuickColor4 = new javax.swing.JButton();
        btnQuickColor5 = new javax.swing.JButton();
        btnQuickColor6 = new javax.swing.JButton();
        btnQuickColor7 = new javax.swing.JButton();
        btnQuickColor8 = new javax.swing.JButton();
        btnQuickColor9 = new javax.swing.JButton();
        btnQuickColor0 = new javax.swing.JButton();
        lblQuickColor1 = new javax.swing.JLabel();
        lblQuickColor2 = new javax.swing.JLabel();
        lblQuickColor3 = new javax.swing.JLabel();
        lblQuickColor4 = new javax.swing.JLabel();
        lblQuickColor5 = new javax.swing.JLabel();
        lblQuickColor6 = new javax.swing.JLabel();
        lblQuickColor7 = new javax.swing.JLabel();
        lblQuickColor8 = new javax.swing.JLabel();
        lblQuickColor9 = new javax.swing.JLabel();
        lblQuickColor0 = new javax.swing.JLabel();
        btnConvertColor = new javax.swing.JButton();
        pnlConstruction = new javax.swing.JPanel();
        pnlShapes1D = new javax.swing.JPanel();
        btnConstructLine = new javax.swing.JButton();
        pnlShapes2D = new javax.swing.JPanel();
        btnConstructRectangle = new javax.swing.JButton();
        btnConstructEllipse = new javax.swing.JButton();
        pnlShapes3D = new javax.swing.JPanel();
        btnConstructCuboid = new javax.swing.JButton();
        btnConstructSphere = new javax.swing.JButton();
        pnlVisualSettings = new javax.swing.JPanel();
        pnlModelCubesOptions = new javax.swing.JPanel();
        chkShowFaces = new javax.swing.JCheckBox();
        chkShowEdges = new javax.swing.JCheckBox();
        pnlPivotOptions = new javax.swing.JPanel();
        chkShowScenePivotCube = new javax.swing.JCheckBox();
        chkShowModelPivotIndicators = new javax.swing.JCheckBox();
        chkShowPivotMovement = new javax.swing.JCheckBox();
        chkShowPivotRotation = new javax.swing.JCheckBox();
        pnlSelectionOptions = new javax.swing.JPanel();
        chkShowSelectionCube = new javax.swing.JCheckBox();
        chkShowSelectionMovement = new javax.swing.JCheckBox();
        chkShowSelectionRotation = new javax.swing.JCheckBox();
        pnlMiscOptions = new javax.swing.JPanel();
        chkShowCaptureArea = new javax.swing.JCheckBox();
        chkUseHiResMode = new javax.swing.JCheckBox();
        btnHideIndicators = new javax.swing.JButton();
        btnBackgroundColor = new javax.swing.JButton();
        pnlZShadow = new javax.swing.JPanel();
        lblZShadowInc = new javax.swing.JLabel();
        spnZShadowInc = new javax.swing.JSpinner();
        btnSetZShadowInc = new javax.swing.JButton();
        btn0ZShadow = new javax.swing.JButton();
        btn10ZShadow = new javax.swing.JButton();
        btn20ZShadow = new javax.swing.JButton();
        btn30ZShadow = new javax.swing.JButton();
        btn40ZShadow = new javax.swing.JButton();
        rdoZShadowNearest = new javax.swing.JRadioButton();
        rdoZShadowScenePivot = new javax.swing.JRadioButton();
        rdoZShadowModelPivot = new javax.swing.JRadioButton();
        lblBaseShadowVal = new javax.swing.JLabel();
        spnBaseShadowVal = new javax.swing.JSpinner();
        btnSetBaseShadowVal = new javax.swing.JButton();
        pnlAnimation = new javax.swing.JPanel();
        spnCaptureWidth = new javax.swing.JSpinner();
        lblCaptureSize = new javax.swing.JLabel();
        lblSpriteSize = new javax.swing.JLabel();
        lblInterpolationNote = new javax.swing.JLabel();
        pnlPoses = new javax.swing.JPanel();
        btnCapturePose = new javax.swing.JButton();
        scpPoses = new javax.swing.JScrollPane();
        lstPoses = new javax.swing.JList();
        pnlDirections = new javax.swing.JPanel();
        btnCaptureDirection = new javax.swing.JButton();
        scpDirections = new javax.swing.JScrollPane();
        lstDirections = new javax.swing.JList();
        lblCaptureWidth = new javax.swing.JLabel();
        lblCaptureHeight = new javax.swing.JLabel();
        spnCaptureHeight = new javax.swing.JSpinner();
        lblSpriteWidth = new javax.swing.JLabel();
        spnSpriteWidth = new javax.swing.JSpinner();
        lblSpriteHeight = new javax.swing.JLabel();
        spnSpriteHeight = new javax.swing.JSpinner();
        pnlTesting = new javax.swing.JPanel();
        btn2DCapture = new javax.swing.JButton();
        pnlInfo = new javax.swing.JPanel();
        pnlQuickColorHints = new javax.swing.JPanel();
        lblColorDef = new javax.swing.JLabel();
        lblColor1 = new javax.swing.JLabel();
        lblColor2 = new javax.swing.JLabel();
        lblColor3 = new javax.swing.JLabel();
        lblColor4 = new javax.swing.JLabel();
        lblColor5 = new javax.swing.JLabel();
        lblColor6 = new javax.swing.JLabel();
        lblColor7 = new javax.swing.JLabel();
        lblColor8 = new javax.swing.JLabel();
        lblColor9 = new javax.swing.JLabel();
        lblColor0 = new javax.swing.JLabel();
        mnbMainMenu = new javax.swing.JMenuBar();
        mnuFile = new javax.swing.JMenu();
        mniSaveModel = new javax.swing.JMenuItem();
        mniSaveModelAndSubmodels = new javax.swing.JMenuItem();
        fileSep1 = new javax.swing.JPopupMenu.Separator();
        mniLoadModel = new javax.swing.JMenuItem();
        mniLoadModelAndSubmodels = new javax.swing.JMenuItem();
        fileSep2 = new javax.swing.JPopupMenu.Separator();
        mniImportImage2D = new javax.swing.JMenuItem();
        mnuModel = new javax.swing.JMenu();
        mniNewModel = new javax.swing.JMenuItem();
        mniNewSubModel = new javax.swing.JMenuItem();
        mniDeleteModel = new javax.swing.JMenuItem();
        sepModel1 = new javax.swing.JPopupMenu.Separator();
        mniChangeModelPivot = new javax.swing.JMenuItem();
        mniSmoothModel = new javax.swing.JMenuItem();
        mniHollowModel = new javax.swing.JMenuItem();
        mniPuffModel = new javax.swing.JMenuItem();
        sepModel2 = new javax.swing.JPopupMenu.Separator();
        mniCopyModel = new javax.swing.JMenuItem();
        mniCutModel = new javax.swing.JMenuItem();
        mniPasteModel = new javax.swing.JMenuItem();
        mniPasteAsSubmodel = new javax.swing.JMenuItem();
        mnuAnimation = new javax.swing.JMenu();
        mniSaveAnimationFile = new javax.swing.JMenuItem();
        mniLoadAnimationFile = new javax.swing.JMenuItem();
        animSep1 = new javax.swing.JPopupMenu.Separator();
        mniRunAnimation = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Cubizer");
        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                formKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                formKeyReleased(evt);
            }
        });

        renderer3D.setFocusable(false);
        renderer3D.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                renderer3DMouseWheelMoved(evt);
            }
        });
        renderer3D.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                renderer3DMouseReleased(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                renderer3DMousePressed(evt);
            }
        });
        renderer3D.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                renderer3DMouseDragged(evt);
            }
        });

        lblCurrentModel.setForeground(new java.awt.Color(255, 255, 255));
        lblCurrentModel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblCurrentModel.setText("<none>");
        lblCurrentModel.setFocusable(false);

        javax.swing.GroupLayout renderer3DLayout = new javax.swing.GroupLayout(renderer3D);
        renderer3D.setLayout(renderer3DLayout);
        renderer3DLayout.setHorizontalGroup(
            renderer3DLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblCurrentModel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        renderer3DLayout.setVerticalGroup(
            renderer3DLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(renderer3DLayout.createSequentialGroup()
                .addComponent(lblCurrentModel)
                .addContainerGap(283, Short.MAX_VALUE))
        );

        tabControls.setFocusable(false);

        pnlScene.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        pnlScene.setFocusable(false);

        pnlSceneRotation.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Scene Rotation", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));
        pnlSceneRotation.setFocusable(false);

        sldSceneZAxis.setMaximum(360);
        sldSceneZAxis.setValue(0);
        sldSceneZAxis.setFocusable(false);
        sldSceneZAxis.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sldSceneZAxisStateChanged(evt);
            }
        });

        sldSceneXAxis.setMaximum(360);
        sldSceneXAxis.setValue(0);
        sldSceneXAxis.setFocusable(false);
        sldSceneXAxis.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sldSceneXAxisStateChanged(evt);
            }
        });

        lblSceneYDeg.setText("0°");
        lblSceneYDeg.setFocusable(false);

        lblSceneZAxis.setForeground(new java.awt.Color(0, 0, 255));
        lblSceneZAxis.setText("Z-Axis");
        lblSceneZAxis.setFocusable(false);

        lblSceneZDeg.setText("0°");
        lblSceneZDeg.setFocusable(false);

        lblSceneXDeg.setText("0°");
        lblSceneXDeg.setFocusable(false);

        sldSceneYAxis.setMaximum(360);
        sldSceneYAxis.setValue(0);
        sldSceneYAxis.setFocusable(false);
        sldSceneYAxis.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sldSceneYAxisStateChanged(evt);
            }
        });

        lblSceneYAxis.setForeground(new java.awt.Color(0, 204, 0));
        lblSceneYAxis.setText("Y-Axis");
        lblSceneYAxis.setFocusable(false);

        lblSceneXAxis.setForeground(new java.awt.Color(255, 0, 0));
        lblSceneXAxis.setText("X-Axis");
        lblSceneXAxis.setFocusable(false);

        javax.swing.GroupLayout pnlSceneRotationLayout = new javax.swing.GroupLayout(pnlSceneRotation);
        pnlSceneRotation.setLayout(pnlSceneRotationLayout);
        pnlSceneRotationLayout.setHorizontalGroup(
            pnlSceneRotationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSceneRotationLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlSceneRotationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblSceneXAxis, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
                    .addComponent(lblSceneYAxis, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblSceneZAxis, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlSceneRotationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(sldSceneYAxis, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 414, Short.MAX_VALUE)
                    .addComponent(sldSceneXAxis, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(sldSceneZAxis, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlSceneRotationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(lblSceneYDeg, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 32, Short.MAX_VALUE)
                    .addComponent(lblSceneXDeg, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblSceneZDeg, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlSceneRotationLayout.setVerticalGroup(
            pnlSceneRotationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSceneRotationLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlSceneRotationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblSceneXAxis, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(sldSceneXAxis, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblSceneXDeg, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlSceneRotationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblSceneYAxis, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(sldSceneYAxis, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblSceneYDeg, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlSceneRotationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblSceneZAxis, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(sldSceneZAxis, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblSceneZDeg, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlSceneRotationLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {lblSceneXAxis, lblSceneYAxis, lblSceneZAxis, sldSceneXAxis, sldSceneYAxis, sldSceneZAxis});

        sldScale.setMajorTickSpacing(100);
        sldScale.setMaximum(1000);
        sldScale.setPaintLabels(true);
        sldScale.setPaintTicks(true);
        sldScale.setValue(100);
        sldScale.setFocusable(false);
        sldScale.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sldScaleStateChanged(evt);
            }
        });

        lblScale.setText("Scale:");
        lblScale.setFocusable(false);

        pnlScenePresetViews.setBorder(javax.swing.BorderFactory.createTitledBorder("Preset Views"));
        pnlScenePresetViews.setFocusable(false);

        pnlScenePresetViewsGrid.setFocusable(false);
        pnlScenePresetViewsGrid.setLayout(new java.awt.GridLayout(2, 3));

        btnSceneFrontView.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/front.png"))); // NOI18N
        btnSceneFrontView.setText("Front");
        btnSceneFrontView.setFocusable(false);
        btnSceneFrontView.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSceneFrontView.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        btnSceneFrontView.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSceneFrontViewActionPerformed(evt);
            }
        });
        pnlScenePresetViewsGrid.add(btnSceneFrontView);

        btnSceneLeftView.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/left.png"))); // NOI18N
        btnSceneLeftView.setText("Left");
        btnSceneLeftView.setFocusable(false);
        btnSceneLeftView.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSceneLeftView.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        btnSceneLeftView.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSceneLeftViewActionPerformed(evt);
            }
        });
        pnlScenePresetViewsGrid.add(btnSceneLeftView);

        btnSceneTopView.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/top.png"))); // NOI18N
        btnSceneTopView.setText("Top");
        btnSceneTopView.setFocusable(false);
        btnSceneTopView.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSceneTopView.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        btnSceneTopView.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSceneTopViewActionPerformed(evt);
            }
        });
        pnlScenePresetViewsGrid.add(btnSceneTopView);

        btnSceneBackView.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/back.png"))); // NOI18N
        btnSceneBackView.setText("Back");
        btnSceneBackView.setFocusable(false);
        btnSceneBackView.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSceneBackView.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        btnSceneBackView.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSceneBackViewActionPerformed(evt);
            }
        });
        pnlScenePresetViewsGrid.add(btnSceneBackView);

        btnSceneRightView.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/right.png"))); // NOI18N
        btnSceneRightView.setText("Right");
        btnSceneRightView.setFocusable(false);
        btnSceneRightView.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSceneRightView.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        btnSceneRightView.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSceneRightViewActionPerformed(evt);
            }
        });
        pnlScenePresetViewsGrid.add(btnSceneRightView);

        btnSceneBottomView.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/bottom.png"))); // NOI18N
        btnSceneBottomView.setText("Bottom");
        btnSceneBottomView.setFocusable(false);
        btnSceneBottomView.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSceneBottomView.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        btnSceneBottomView.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSceneBottomViewActionPerformed(evt);
            }
        });
        pnlScenePresetViewsGrid.add(btnSceneBottomView);

        javax.swing.GroupLayout pnlScenePresetViewsLayout = new javax.swing.GroupLayout(pnlScenePresetViews);
        pnlScenePresetViews.setLayout(pnlScenePresetViewsLayout);
        pnlScenePresetViewsLayout.setHorizontalGroup(
            pnlScenePresetViewsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlScenePresetViewsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlScenePresetViewsGrid, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        pnlScenePresetViewsLayout.setVerticalGroup(
            pnlScenePresetViewsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlScenePresetViewsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlScenePresetViewsGrid, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE)
                .addContainerGap())
        );

        btnSaveSceneOrientation.setText("Save Orientation");
        btnSaveSceneOrientation.setFocusable(false);
        btnSaveSceneOrientation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveSceneOrientationActionPerformed(evt);
            }
        });

        cboRestoreSceneOrientation.setEditable(true);
        cboRestoreSceneOrientation.setMaximumRowCount(12);
        cboRestoreSceneOrientation.setFocusable(false);
        cboRestoreSceneOrientation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboRestoreSceneOrientationActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlSceneLayout = new javax.swing.GroupLayout(pnlScene);
        pnlScene.setLayout(pnlSceneLayout);
        pnlSceneLayout.setHorizontalGroup(
            pnlSceneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSceneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlSceneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlSceneLayout.createSequentialGroup()
                        .addGroup(pnlSceneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(pnlSceneRotation, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(pnlSceneLayout.createSequentialGroup()
                                .addComponent(btnSaveSceneOrientation, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cboRestoreSceneOrientation, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(pnlScenePresetViews, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlSceneLayout.createSequentialGroup()
                        .addComponent(lblScale)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sldScale, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        pnlSceneLayout.setVerticalGroup(
            pnlSceneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSceneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlSceneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(pnlSceneLayout.createSequentialGroup()
                        .addGroup(pnlSceneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnSaveSceneOrientation)
                            .addComponent(cboRestoreSceneOrientation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlSceneRotation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(pnlScenePresetViews, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlSceneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(sldScale, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblScale, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tabControls.addTab("Scene", pnlScene);

        pnlModel.setFocusable(false);

        pnlModels.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Models", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));
        pnlModels.setFocusable(false);

        scpModels.setFocusable(false);

        lstModels.setFocusable(false);
        lstModels.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                lstModelsMousePressed(evt);
            }
        });
        lstModels.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                lstModelsValueChanged(evt);
            }
        });
        scpModels.setViewportView(lstModels);

        javax.swing.GroupLayout pnlModelsLayout = new javax.swing.GroupLayout(pnlModels);
        pnlModels.setLayout(pnlModelsLayout);
        pnlModelsLayout.setHorizontalGroup(
            pnlModelsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(scpModels, javax.swing.GroupLayout.DEFAULT_SIZE, 119, Short.MAX_VALUE)
        );
        pnlModelsLayout.setVerticalGroup(
            pnlModelsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(scpModels, javax.swing.GroupLayout.DEFAULT_SIZE, 264, Short.MAX_VALUE)
        );

        pnlModelRotation.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Model Rotation", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));
        pnlModelRotation.setFocusable(false);

        sldModelZAxis.setMaximum(360);
        sldModelZAxis.setValue(0);
        sldModelZAxis.setFocusable(false);
        sldModelZAxis.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sldModelZAxisStateChanged(evt);
            }
        });

        sldModelXAxis.setMaximum(360);
        sldModelXAxis.setValue(0);
        sldModelXAxis.setFocusable(false);
        sldModelXAxis.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sldModelXAxisStateChanged(evt);
            }
        });

        lblModelYDeg.setText("0°");
        lblModelYDeg.setFocusable(false);

        lblModelZAxis.setForeground(new java.awt.Color(0, 0, 255));
        lblModelZAxis.setText("Z-Axis");
        lblModelZAxis.setFocusable(false);

        lblModelZDeg.setText("0°");
        lblModelZDeg.setFocusable(false);

        lblModelXDeg.setText("0°");
        lblModelXDeg.setFocusable(false);

        sldModelYAxis.setMaximum(360);
        sldModelYAxis.setValue(0);
        sldModelYAxis.setFocusable(false);
        sldModelYAxis.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sldModelYAxisStateChanged(evt);
            }
        });

        lblModelYAxis.setForeground(new java.awt.Color(0, 204, 0));
        lblModelYAxis.setText("Y-Axis");
        lblModelYAxis.setFocusable(false);

        lblModelXAxis.setForeground(new java.awt.Color(255, 0, 0));
        lblModelXAxis.setText("X-Axis");
        lblModelXAxis.setFocusable(false);

        javax.swing.GroupLayout pnlModelRotationLayout = new javax.swing.GroupLayout(pnlModelRotation);
        pnlModelRotation.setLayout(pnlModelRotationLayout);
        pnlModelRotationLayout.setHorizontalGroup(
            pnlModelRotationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlModelRotationLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlModelRotationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblModelXAxis, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
                    .addComponent(lblModelYAxis, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblModelZAxis, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlModelRotationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(sldModelYAxis, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 520, Short.MAX_VALUE)
                    .addComponent(sldModelXAxis, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(sldModelZAxis, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlModelRotationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(lblModelYDeg, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 32, Short.MAX_VALUE)
                    .addComponent(lblModelXDeg, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblModelZDeg, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlModelRotationLayout.setVerticalGroup(
            pnlModelRotationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlModelRotationLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlModelRotationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblModelXAxis, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(sldModelXAxis, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblModelXDeg, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlModelRotationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblModelYAxis, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(sldModelYAxis, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblModelYDeg, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlModelRotationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblModelZAxis, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(sldModelZAxis, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblModelZDeg, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        chkShowModel.setText("Render selected Model");
        chkShowModel.setFocusable(false);
        chkShowModel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkShowModelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlModelLayout = new javax.swing.GroupLayout(pnlModel);
        pnlModel.setLayout(pnlModelLayout);
        pnlModelLayout.setHorizontalGroup(
            pnlModelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlModelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlModelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlModelRotation, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pnlModelLayout.createSequentialGroup()
                        .addComponent(chkShowModel)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pnlModels, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        pnlModelLayout.setVerticalGroup(
            pnlModelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlModelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlModelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(pnlModels, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pnlModelLayout.createSequentialGroup()
                        .addComponent(chkShowModel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(pnlModelRotation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        tabControls.addTab("Model", pnlModel);

        pnlColor.setFocusable(false);

        pnlDefaultColor.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Default Color", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));
        pnlDefaultColor.setFocusable(false);

        btnDefaultColor.setBackground(new java.awt.Color(153, 153, 153));
        btnDefaultColor.setFocusable(false);
        btnDefaultColor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDefaultColorActionPerformed(evt);
            }
        });

        lblDefaultColor.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblDefaultColor.setText("~");
        lblDefaultColor.setFocusable(false);

        javax.swing.GroupLayout pnlDefaultColorLayout = new javax.swing.GroupLayout(pnlDefaultColor);
        pnlDefaultColor.setLayout(pnlDefaultColorLayout);
        pnlDefaultColorLayout.setHorizontalGroup(
            pnlDefaultColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDefaultColorLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlDefaultColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnDefaultColor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblDefaultColor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlDefaultColorLayout.setVerticalGroup(
            pnlDefaultColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlDefaultColorLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblDefaultColor)
                .addGap(0, 0, 0)
                .addComponent(btnDefaultColor, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pnlQuickColors.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Quick Colors", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));
        pnlQuickColors.setFocusable(false);

        btnQuickColor1.setBackground(new java.awt.Color(51, 51, 51));
        btnQuickColor1.setFocusable(false);
        btnQuickColor1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQuickColor1ActionPerformed(evt);
            }
        });

        btnQuickColor2.setBackground(new java.awt.Color(255, 255, 255));
        btnQuickColor2.setFocusable(false);
        btnQuickColor2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQuickColor2ActionPerformed(evt);
            }
        });

        btnQuickColor3.setBackground(new java.awt.Color(255, 0, 0));
        btnQuickColor3.setFocusable(false);
        btnQuickColor3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQuickColor3ActionPerformed(evt);
            }
        });

        btnQuickColor4.setBackground(new java.awt.Color(0, 0, 255));
        btnQuickColor4.setFocusable(false);
        btnQuickColor4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQuickColor4ActionPerformed(evt);
            }
        });

        btnQuickColor5.setBackground(new java.awt.Color(255, 255, 0));
        btnQuickColor5.setFocusable(false);
        btnQuickColor5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQuickColor5ActionPerformed(evt);
            }
        });

        btnQuickColor6.setBackground(new java.awt.Color(255, 128, 0));
        btnQuickColor6.setFocusable(false);
        btnQuickColor6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQuickColor6ActionPerformed(evt);
            }
        });

        btnQuickColor7.setBackground(new java.awt.Color(255, 0, 255));
        btnQuickColor7.setFocusable(false);
        btnQuickColor7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQuickColor7ActionPerformed(evt);
            }
        });

        btnQuickColor8.setBackground(new java.awt.Color(0, 255, 0));
        btnQuickColor8.setFocusable(false);
        btnQuickColor8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQuickColor8ActionPerformed(evt);
            }
        });

        btnQuickColor9.setBackground(new java.awt.Color(140, 90, 30));
        btnQuickColor9.setFocusable(false);
        btnQuickColor9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQuickColor9ActionPerformed(evt);
            }
        });

        btnQuickColor0.setBackground(new java.awt.Color(255, 204, 153));
        btnQuickColor0.setFocusable(false);
        btnQuickColor0.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQuickColor0ActionPerformed(evt);
            }
        });

        lblQuickColor1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblQuickColor1.setText("1");
        lblQuickColor1.setFocusable(false);

        lblQuickColor2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblQuickColor2.setText("2");
        lblQuickColor2.setFocusable(false);

        lblQuickColor3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblQuickColor3.setText("3");
        lblQuickColor3.setFocusable(false);

        lblQuickColor4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblQuickColor4.setText("4");
        lblQuickColor4.setFocusable(false);

        lblQuickColor5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblQuickColor5.setText("5");
        lblQuickColor5.setFocusable(false);

        lblQuickColor6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblQuickColor6.setText("6");
        lblQuickColor6.setFocusable(false);

        lblQuickColor7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblQuickColor7.setText("7");
        lblQuickColor7.setFocusable(false);

        lblQuickColor8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblQuickColor8.setText("8");
        lblQuickColor8.setFocusable(false);

        lblQuickColor9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblQuickColor9.setText("9");
        lblQuickColor9.setFocusable(false);

        lblQuickColor0.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblQuickColor0.setText("0");
        lblQuickColor0.setFocusable(false);

        javax.swing.GroupLayout pnlQuickColorsLayout = new javax.swing.GroupLayout(pnlQuickColors);
        pnlQuickColors.setLayout(pnlQuickColorsLayout);
        pnlQuickColorsLayout.setHorizontalGroup(
            pnlQuickColorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlQuickColorsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlQuickColorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnQuickColor1, javax.swing.GroupLayout.DEFAULT_SIZE, 55, Short.MAX_VALUE)
                    .addComponent(lblQuickColor1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlQuickColorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblQuickColor2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnQuickColor2, javax.swing.GroupLayout.DEFAULT_SIZE, 56, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlQuickColorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblQuickColor3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnQuickColor3, javax.swing.GroupLayout.DEFAULT_SIZE, 61, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlQuickColorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnQuickColor4, javax.swing.GroupLayout.DEFAULT_SIZE, 55, Short.MAX_VALUE)
                    .addComponent(lblQuickColor4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlQuickColorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnQuickColor5, javax.swing.GroupLayout.DEFAULT_SIZE, 59, Short.MAX_VALUE)
                    .addComponent(lblQuickColor5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlQuickColorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnQuickColor6, javax.swing.GroupLayout.DEFAULT_SIZE, 60, Short.MAX_VALUE)
                    .addComponent(lblQuickColor6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlQuickColorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnQuickColor7, javax.swing.GroupLayout.DEFAULT_SIZE, 64, Short.MAX_VALUE)
                    .addComponent(lblQuickColor7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlQuickColorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnQuickColor8, javax.swing.GroupLayout.DEFAULT_SIZE, 56, Short.MAX_VALUE)
                    .addComponent(lblQuickColor8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlQuickColorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnQuickColor9, javax.swing.GroupLayout.DEFAULT_SIZE, 59, Short.MAX_VALUE)
                    .addComponent(lblQuickColor9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlQuickColorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnQuickColor0, javax.swing.GroupLayout.DEFAULT_SIZE, 57, Short.MAX_VALUE)
                    .addComponent(lblQuickColor0, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlQuickColorsLayout.setVerticalGroup(
            pnlQuickColorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlQuickColorsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlQuickColorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblQuickColor1)
                    .addComponent(lblQuickColor2)
                    .addComponent(lblQuickColor3)
                    .addComponent(lblQuickColor4)
                    .addComponent(lblQuickColor5)
                    .addComponent(lblQuickColor6)
                    .addComponent(lblQuickColor7)
                    .addComponent(lblQuickColor8)
                    .addComponent(lblQuickColor9)
                    .addComponent(lblQuickColor0))
                .addGap(0, 0, 0)
                .addGroup(pnlQuickColorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnQuickColor1, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnQuickColor2, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnQuickColor3, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnQuickColor4, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnQuickColor5, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnQuickColor6, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnQuickColor7, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnQuickColor8, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnQuickColor9, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnQuickColor0, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pnlQuickColorsLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {btnQuickColor0, btnQuickColor1, btnQuickColor2, btnQuickColor3, btnQuickColor4, btnQuickColor5, btnQuickColor6, btnQuickColor7, btnQuickColor8, btnQuickColor9});

        btnConvertColor.setText("Convert Color");
        btnConvertColor.setFocusable(false);
        btnConvertColor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConvertColorActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlColorLayout = new javax.swing.GroupLayout(pnlColor);
        pnlColor.setLayout(pnlColorLayout);
        pnlColorLayout.setHorizontalGroup(
            pnlColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlColorLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(btnConvertColor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlDefaultColor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlQuickColors, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        pnlColorLayout.setVerticalGroup(
            pnlColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlColorLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(pnlDefaultColor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlQuickColors, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnConvertColor, javax.swing.GroupLayout.DEFAULT_SIZE, 187, Short.MAX_VALUE)
                .addContainerGap())
        );

        tabControls.addTab("Color", pnlColor);

        pnlConstruction.setFocusable(false);

        pnlShapes1D.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "1D Shapes", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));
        pnlShapes1D.setFocusable(false);

        btnConstructLine.setText("Line");
        btnConstructLine.setFocusable(false);
        btnConstructLine.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConstructLineActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlShapes1DLayout = new javax.swing.GroupLayout(pnlShapes1D);
        pnlShapes1D.setLayout(pnlShapes1DLayout);
        pnlShapes1DLayout.setHorizontalGroup(
            pnlShapes1DLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlShapes1DLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnConstructLine, javax.swing.GroupLayout.DEFAULT_SIZE, 108, Short.MAX_VALUE)
                .addContainerGap())
        );
        pnlShapes1DLayout.setVerticalGroup(
            pnlShapes1DLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlShapes1DLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnConstructLine)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlShapes2D.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "2D Shapes", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));
        pnlShapes2D.setFocusable(false);

        btnConstructRectangle.setText("Rectangle");
        btnConstructRectangle.setFocusable(false);
        btnConstructRectangle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConstructRectangleActionPerformed(evt);
            }
        });

        btnConstructEllipse.setText("Ellipse");
        btnConstructEllipse.setFocusable(false);
        btnConstructEllipse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConstructEllipseActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlShapes2DLayout = new javax.swing.GroupLayout(pnlShapes2D);
        pnlShapes2D.setLayout(pnlShapes2DLayout);
        pnlShapes2DLayout.setHorizontalGroup(
            pnlShapes2DLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlShapes2DLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlShapes2DLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnConstructRectangle, javax.swing.GroupLayout.DEFAULT_SIZE, 108, Short.MAX_VALUE)
                    .addComponent(btnConstructEllipse, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlShapes2DLayout.setVerticalGroup(
            pnlShapes2DLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlShapes2DLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnConstructRectangle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnConstructEllipse)
                .addContainerGap(37, Short.MAX_VALUE))
        );

        pnlShapes3D.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "3D Shapes", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));
        pnlShapes3D.setFocusable(false);

        btnConstructCuboid.setText("Cuboid");
        btnConstructCuboid.setFocusable(false);
        btnConstructCuboid.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConstructCuboidActionPerformed(evt);
            }
        });

        btnConstructSphere.setText("Sphere");
        btnConstructSphere.setFocusable(false);
        btnConstructSphere.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConstructSphereActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlShapes3DLayout = new javax.swing.GroupLayout(pnlShapes3D);
        pnlShapes3D.setLayout(pnlShapes3DLayout);
        pnlShapes3DLayout.setHorizontalGroup(
            pnlShapes3DLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlShapes3DLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlShapes3DLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnConstructCuboid, javax.swing.GroupLayout.DEFAULT_SIZE, 108, Short.MAX_VALUE)
                    .addComponent(btnConstructSphere, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlShapes3DLayout.setVerticalGroup(
            pnlShapes3DLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlShapes3DLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnConstructCuboid)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnConstructSphere)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout pnlConstructionLayout = new javax.swing.GroupLayout(pnlConstruction);
        pnlConstruction.setLayout(pnlConstructionLayout);
        pnlConstructionLayout.setHorizontalGroup(
            pnlConstructionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlConstructionLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlShapes1D, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlShapes2D, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlShapes3D, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(351, Short.MAX_VALUE))
        );
        pnlConstructionLayout.setVerticalGroup(
            pnlConstructionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlConstructionLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlConstructionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(pnlShapes2D, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlShapes3D, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlShapes1D, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(175, Short.MAX_VALUE))
        );

        tabControls.addTab("Construction", pnlConstruction);

        pnlVisualSettings.setFocusable(false);

        pnlModelCubesOptions.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Model Cubes", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));
        pnlModelCubesOptions.setFocusable(false);

        chkShowFaces.setSelected(true);
        chkShowFaces.setText("Show Faces");
        chkShowFaces.setFocusable(false);
        chkShowFaces.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkShowFacesActionPerformed(evt);
            }
        });

        chkShowEdges.setSelected(true);
        chkShowEdges.setText("Show Edges");
        chkShowEdges.setFocusable(false);
        chkShowEdges.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkShowEdgesActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlModelCubesOptionsLayout = new javax.swing.GroupLayout(pnlModelCubesOptions);
        pnlModelCubesOptions.setLayout(pnlModelCubesOptionsLayout);
        pnlModelCubesOptionsLayout.setHorizontalGroup(
            pnlModelCubesOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlModelCubesOptionsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlModelCubesOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(chkShowEdges, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 215, Short.MAX_VALUE)
                    .addComponent(chkShowFaces, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(67, Short.MAX_VALUE))
        );
        pnlModelCubesOptionsLayout.setVerticalGroup(
            pnlModelCubesOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlModelCubesOptionsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(chkShowFaces)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkShowEdges)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlPivotOptions.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Pivot Cubes", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));
        pnlPivotOptions.setFocusable(false);

        chkShowScenePivotCube.setSelected(true);
        chkShowScenePivotCube.setText("Show Scene Pivot");
        chkShowScenePivotCube.setFocusable(false);
        chkShowScenePivotCube.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkShowScenePivotCubeActionPerformed(evt);
            }
        });

        chkShowModelPivotIndicators.setSelected(true);
        chkShowModelPivotIndicators.setText("Show Model Pivot Indicators");
        chkShowModelPivotIndicators.setFocusable(false);
        chkShowModelPivotIndicators.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkShowModelPivotIndicatorsActionPerformed(evt);
            }
        });

        chkShowPivotMovement.setText("Show Movement Axes");
        chkShowPivotMovement.setFocusable(false);
        chkShowPivotMovement.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkShowPivotMovementActionPerformed(evt);
            }
        });

        chkShowPivotRotation.setSelected(true);
        chkShowPivotRotation.setText("Show Rotation Axes");
        chkShowPivotRotation.setFocusable(false);
        chkShowPivotRotation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkShowPivotRotationActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlPivotOptionsLayout = new javax.swing.GroupLayout(pnlPivotOptions);
        pnlPivotOptions.setLayout(pnlPivotOptionsLayout);
        pnlPivotOptionsLayout.setHorizontalGroup(
            pnlPivotOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlPivotOptionsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlPivotOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chkShowScenePivotCube, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(chkShowModelPivotIndicators, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(chkShowPivotMovement, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(chkShowPivotRotation, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlPivotOptionsLayout.setVerticalGroup(
            pnlPivotOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlPivotOptionsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(chkShowScenePivotCube)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkShowModelPivotIndicators)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkShowPivotMovement)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkShowPivotRotation)
                .addContainerGap(19, Short.MAX_VALUE))
        );

        pnlSelectionOptions.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Selection Cube", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));
        pnlSelectionOptions.setFocusable(false);

        chkShowSelectionCube.setSelected(true);
        chkShowSelectionCube.setText("Show Selection Cube");
        chkShowSelectionCube.setFocusable(false);
        chkShowSelectionCube.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkShowSelectionCubeActionPerformed(evt);
            }
        });

        chkShowSelectionMovement.setSelected(true);
        chkShowSelectionMovement.setText("Show Movement Axes");
        chkShowSelectionMovement.setFocusable(false);
        chkShowSelectionMovement.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkShowSelectionMovementActionPerformed(evt);
            }
        });

        chkShowSelectionRotation.setText("Show Rotation Axes");
        chkShowSelectionRotation.setFocusable(false);
        chkShowSelectionRotation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkShowSelectionRotationActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlSelectionOptionsLayout = new javax.swing.GroupLayout(pnlSelectionOptions);
        pnlSelectionOptions.setLayout(pnlSelectionOptionsLayout);
        pnlSelectionOptionsLayout.setHorizontalGroup(
            pnlSelectionOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSelectionOptionsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlSelectionOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chkShowSelectionCube, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(chkShowSelectionRotation, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(chkShowSelectionMovement, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlSelectionOptionsLayout.setVerticalGroup(
            pnlSelectionOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSelectionOptionsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(chkShowSelectionCube)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkShowSelectionMovement)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chkShowSelectionRotation)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlMiscOptions.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Misc", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));
        pnlMiscOptions.setFocusable(false);

        chkShowCaptureArea.setSelected(true);
        chkShowCaptureArea.setText("Show 2D Capture Area");
        chkShowCaptureArea.setFocusable(false);
        chkShowCaptureArea.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkShowCaptureAreaActionPerformed(evt);
            }
        });

        chkUseHiResMode.setText("Use Hi-Res Mode");
        chkUseHiResMode.setFocusable(false);
        chkUseHiResMode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkUseHiResModeActionPerformed(evt);
            }
        });

        btnHideIndicators.setText("Hide All Indicators (Show Faces Only)");
        btnHideIndicators.setFocusable(false);
        btnHideIndicators.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHideIndicatorsActionPerformed(evt);
            }
        });

        btnBackgroundColor.setText("Background Color");
        btnBackgroundColor.setFocusable(false);
        btnBackgroundColor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBackgroundColorActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlMiscOptionsLayout = new javax.swing.GroupLayout(pnlMiscOptions);
        pnlMiscOptions.setLayout(pnlMiscOptionsLayout);
        pnlMiscOptionsLayout.setHorizontalGroup(
            pnlMiscOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMiscOptionsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlMiscOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(chkShowCaptureArea, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(chkUseHiResMode, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 21, Short.MAX_VALUE)
                .addGroup(pnlMiscOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnBackgroundColor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnHideIndicators, javax.swing.GroupLayout.PREFERRED_SIZE, 252, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        pnlMiscOptionsLayout.setVerticalGroup(
            pnlMiscOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMiscOptionsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlMiscOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlMiscOptionsLayout.createSequentialGroup()
                        .addComponent(btnHideIndicators)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnBackgroundColor))
                    .addGroup(pnlMiscOptionsLayout.createSequentialGroup()
                        .addComponent(chkShowCaptureArea)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(chkUseHiResMode)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlZShadow.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Z-Shadow", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));
        pnlZShadow.setFocusable(false);

        lblZShadowInc.setText("Z-Shadow Inc:");
        lblZShadowInc.setFocusable(false);

        spnZShadowInc.setModel(new javax.swing.SpinnerNumberModel(16.0d, 0.0d, 48.0d, 0.1d));
        spnZShadowInc.setFocusable(false);
        spnZShadowInc.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnZShadowIncStateChanged(evt);
            }
        });

        btnSetZShadowInc.setText("Set");
        btnSetZShadowInc.setFocusable(false);
        btnSetZShadowInc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSetZShadowIncActionPerformed(evt);
            }
        });

        btn0ZShadow.setText("0");
        btn0ZShadow.setFocusable(false);
        btn0ZShadow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn0ZShadowActionPerformed(evt);
            }
        });

        btn10ZShadow.setText("10");
        btn10ZShadow.setFocusable(false);
        btn10ZShadow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn10ZShadowActionPerformed(evt);
            }
        });

        btn20ZShadow.setText("20");
        btn20ZShadow.setFocusable(false);
        btn20ZShadow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn20ZShadowActionPerformed(evt);
            }
        });

        btn30ZShadow.setText("30");
        btn30ZShadow.setFocusable(false);
        btn30ZShadow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn30ZShadowActionPerformed(evt);
            }
        });

        btn40ZShadow.setText("40");
        btn40ZShadow.setFocusable(false);
        btn40ZShadow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn40ZShadowActionPerformed(evt);
            }
        });

        bgpZShadowRef.add(rdoZShadowNearest);
        rdoZShadowNearest.setSelected(true);
        rdoZShadowNearest.setText("Z-Shadow relative to nearest Cube");
        rdoZShadowNearest.setFocusable(false);
        rdoZShadowNearest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdoZShadowNearestActionPerformed(evt);
            }
        });

        bgpZShadowRef.add(rdoZShadowScenePivot);
        rdoZShadowScenePivot.setText("Z-Shadow relative to Scene Pivot");
        rdoZShadowScenePivot.setFocusable(false);
        rdoZShadowScenePivot.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdoZShadowScenePivotActionPerformed(evt);
            }
        });

        bgpZShadowRef.add(rdoZShadowModelPivot);
        rdoZShadowModelPivot.setText("Z-Shadow relative to Current Model Pivot");
        rdoZShadowModelPivot.setFocusable(false);
        rdoZShadowModelPivot.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdoZShadowModelPivotActionPerformed(evt);
            }
        });

        lblBaseShadowVal.setText("Base shadow value at reference:");
        lblBaseShadowVal.setFocusable(false);

        spnBaseShadowVal.setModel(new javax.swing.SpinnerNumberModel(128, 0, 255, 1));
        spnBaseShadowVal.setFocusable(false);

        btnSetBaseShadowVal.setText("Set");
        btnSetBaseShadowVal.setFocusable(false);
        btnSetBaseShadowVal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSetBaseShadowValActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlZShadowLayout = new javax.swing.GroupLayout(pnlZShadow);
        pnlZShadow.setLayout(pnlZShadowLayout);
        pnlZShadowLayout.setHorizontalGroup(
            pnlZShadowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlZShadowLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlZShadowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(rdoZShadowScenePivot, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(rdoZShadowModelPivot, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(rdoZShadowNearest, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pnlZShadowLayout.createSequentialGroup()
                        .addGroup(pnlZShadowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlZShadowLayout.createSequentialGroup()
                                .addComponent(lblZShadowInc, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spnZShadowInc, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnSetZShadowInc, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(pnlZShadowLayout.createSequentialGroup()
                                .addComponent(btn0ZShadow)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btn10ZShadow)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btn20ZShadow)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btn30ZShadow)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btn40ZShadow))
                            .addGroup(pnlZShadowLayout.createSequentialGroup()
                                .addComponent(lblBaseShadowVal)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spnBaseShadowVal, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnSetBaseShadowVal, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 4, Short.MAX_VALUE)))
                .addContainerGap())
        );

        pnlZShadowLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btnSetBaseShadowVal, btnSetZShadowInc});

        pnlZShadowLayout.setVerticalGroup(
            pnlZShadowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlZShadowLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlZShadowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblZShadowInc)
                    .addComponent(spnZShadowInc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSetZShadowInc))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlZShadowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btn0ZShadow)
                    .addComponent(btn10ZShadow)
                    .addComponent(btn20ZShadow)
                    .addComponent(btn30ZShadow)
                    .addComponent(btn40ZShadow))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(rdoZShadowNearest)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rdoZShadowScenePivot)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rdoZShadowModelPivot)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlZShadowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblBaseShadowVal)
                    .addComponent(spnBaseShadowVal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSetBaseShadowVal))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout pnlVisualSettingsLayout = new javax.swing.GroupLayout(pnlVisualSettings);
        pnlVisualSettings.setLayout(pnlVisualSettingsLayout);
        pnlVisualSettingsLayout.setHorizontalGroup(
            pnlVisualSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlVisualSettingsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlVisualSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlModelCubesOptions, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pnlZShadow, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlVisualSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlVisualSettingsLayout.createSequentialGroup()
                        .addComponent(pnlPivotOptions, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlSelectionOptions, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(pnlMiscOptions, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(27, Short.MAX_VALUE))
        );
        pnlVisualSettingsLayout.setVerticalGroup(
            pnlVisualSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlVisualSettingsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlVisualSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlVisualSettingsLayout.createSequentialGroup()
                        .addGroup(pnlVisualSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(pnlSelectionOptions, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(pnlPivotOptions, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlMiscOptions, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(pnlVisualSettingsLayout.createSequentialGroup()
                        .addComponent(pnlModelCubesOptions, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlZShadow, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        tabControls.addTab("Visual Settings", pnlVisualSettings);

        pnlAnimation.setFocusable(false);

        spnCaptureWidth.setModel(new javax.swing.SpinnerNumberModel(0, 0, 512, 1));
        spnCaptureWidth.setFocusable(false);
        spnCaptureWidth.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnCaptureWidthStateChanged(evt);
            }
        });

        lblCaptureSize.setText("2D Capture Size:");
        lblCaptureSize.setFocusable(false);

        lblSpriteSize.setText("2D Final Sprite Size:");
        lblSpriteSize.setFocusable(false);

        lblInterpolationNote.setFont(new java.awt.Font("Tahoma", 2, 11)); // NOI18N
        lblInterpolationNote.setText("(resized with bicubic interpolation)");
        lblInterpolationNote.setFocusable(false);

        pnlPoses.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Poses", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));
        pnlPoses.setFocusable(false);

        btnCapturePose.setText("Capture New");
        btnCapturePose.setFocusable(false);
        btnCapturePose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCapturePoseActionPerformed(evt);
            }
        });

        scpPoses.setFocusable(false);

        lstPoses.setFocusable(false);
        lstPoses.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                lstPosesValueChanged(evt);
            }
        });
        scpPoses.setViewportView(lstPoses);

        javax.swing.GroupLayout pnlPosesLayout = new javax.swing.GroupLayout(pnlPoses);
        pnlPoses.setLayout(pnlPosesLayout);
        pnlPosesLayout.setHorizontalGroup(
            pnlPosesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlPosesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlPosesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnCapturePose, javax.swing.GroupLayout.DEFAULT_SIZE, 218, Short.MAX_VALUE)
                    .addComponent(scpPoses, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlPosesLayout.setVerticalGroup(
            pnlPosesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlPosesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnCapturePose)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scpPoses, javax.swing.GroupLayout.DEFAULT_SIZE, 213, Short.MAX_VALUE)
                .addContainerGap())
        );

        pnlDirections.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Directions", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));
        pnlDirections.setFocusable(false);

        btnCaptureDirection.setText("Capture New");
        btnCaptureDirection.setFocusable(false);
        btnCaptureDirection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCaptureDirectionActionPerformed(evt);
            }
        });

        scpDirections.setFocusable(false);

        lstDirections.setFocusable(false);
        lstDirections.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                lstDirectionsValueChanged(evt);
            }
        });
        scpDirections.setViewportView(lstDirections);

        javax.swing.GroupLayout pnlDirectionsLayout = new javax.swing.GroupLayout(pnlDirections);
        pnlDirections.setLayout(pnlDirectionsLayout);
        pnlDirectionsLayout.setHorizontalGroup(
            pnlDirectionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDirectionsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlDirectionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnCaptureDirection, javax.swing.GroupLayout.DEFAULT_SIZE, 218, Short.MAX_VALUE)
                    .addComponent(scpDirections, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlDirectionsLayout.setVerticalGroup(
            pnlDirectionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDirectionsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnCaptureDirection)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scpDirections)
                .addContainerGap())
        );

        lblCaptureWidth.setText("W:");
        lblCaptureWidth.setFocusable(false);

        lblCaptureHeight.setText("H:");
        lblCaptureHeight.setFocusable(false);

        spnCaptureHeight.setModel(new javax.swing.SpinnerNumberModel(0, 0, 512, 1));
        spnCaptureHeight.setFocusable(false);
        spnCaptureHeight.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnCaptureHeightStateChanged(evt);
            }
        });

        lblSpriteWidth.setText("W:");
        lblSpriteWidth.setFocusable(false);

        spnSpriteWidth.setModel(new javax.swing.SpinnerNumberModel(0, 0, 512, 1));
        spnSpriteWidth.setFocusable(false);

        lblSpriteHeight.setText("H:");
        lblSpriteHeight.setFocusable(false);

        spnSpriteHeight.setModel(new javax.swing.SpinnerNumberModel(0, 0, 512, 1));
        spnSpriteHeight.setFocusable(false);

        javax.swing.GroupLayout pnlAnimationLayout = new javax.swing.GroupLayout(pnlAnimation);
        pnlAnimation.setLayout(pnlAnimationLayout);
        pnlAnimationLayout.setHorizontalGroup(
            pnlAnimationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAnimationLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlAnimationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlAnimationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(lblCaptureSize)
                        .addComponent(lblSpriteSize, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblInterpolationNote, javax.swing.GroupLayout.DEFAULT_SIZE, 172, Short.MAX_VALUE))
                    .addGroup(pnlAnimationLayout.createSequentialGroup()
                        .addComponent(lblCaptureWidth)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spnCaptureWidth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblCaptureHeight)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spnCaptureHeight, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlAnimationLayout.createSequentialGroup()
                        .addComponent(lblSpriteWidth)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spnSpriteWidth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblSpriteHeight)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(spnSpriteHeight, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addComponent(pnlDirections, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlPoses, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(87, 87, 87))
        );

        pnlAnimationLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {spnCaptureHeight, spnCaptureWidth});

        pnlAnimationLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {spnSpriteHeight, spnSpriteWidth});

        pnlAnimationLayout.setVerticalGroup(
            pnlAnimationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAnimationLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlAnimationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlPoses, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pnlAnimationLayout.createSequentialGroup()
                        .addComponent(lblCaptureSize)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlAnimationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(spnCaptureWidth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblCaptureWidth)
                            .addComponent(spnCaptureHeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblCaptureHeight))
                        .addGap(18, 18, 18)
                        .addComponent(lblSpriteSize)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblInterpolationNote)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlAnimationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(spnSpriteWidth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblSpriteWidth)
                            .addComponent(spnSpriteHeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblSpriteHeight))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(pnlDirections, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        tabControls.addTab("Animation", pnlAnimation);

        pnlTesting.setFocusable(false);

        btn2DCapture.setText("2D Capture");
        btn2DCapture.setFocusable(false);
        btn2DCapture.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn2DCaptureActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlTestingLayout = new javax.swing.GroupLayout(pnlTesting);
        pnlTesting.setLayout(pnlTestingLayout);
        pnlTestingLayout.setHorizontalGroup(
            pnlTestingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTestingLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btn2DCapture)
                .addContainerGap(696, Short.MAX_VALUE))
        );
        pnlTestingLayout.setVerticalGroup(
            pnlTestingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTestingLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btn2DCapture)
                .addContainerGap(275, Short.MAX_VALUE))
        );

        tabControls.addTab("Testing", pnlTesting);

        pnlInfo.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        pnlInfo.setFocusable(false);
        pnlInfo.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        pnlQuickColorHints.setFocusable(false);
        pnlQuickColorHints.setPreferredSize(new java.awt.Dimension(242, 20));
        java.awt.FlowLayout flowLayout1 = new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 2, 0);
        flowLayout1.setAlignOnBaseline(true);
        pnlQuickColorHints.setLayout(flowLayout1);

        lblColorDef.setBackground(new java.awt.Color(153, 153, 153));
        lblColorDef.setFont(new java.awt.Font("Courier New", 0, 12)); // NOI18N
        lblColorDef.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblColorDef.setText("~");
        lblColorDef.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        lblColorDef.setFocusable(false);
        lblColorDef.setOpaque(true);
        lblColorDef.setPreferredSize(new java.awt.Dimension(20, 20));
        pnlQuickColorHints.add(lblColorDef);

        lblColor1.setBackground(new java.awt.Color(51, 51, 51));
        lblColor1.setFont(new java.awt.Font("Courier New", 0, 12)); // NOI18N
        lblColor1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblColor1.setText("1");
        lblColor1.setFocusable(false);
        lblColor1.setOpaque(true);
        lblColor1.setPreferredSize(new java.awt.Dimension(20, 20));
        pnlQuickColorHints.add(lblColor1);

        lblColor2.setBackground(new java.awt.Color(255, 255, 255));
        lblColor2.setFont(new java.awt.Font("Courier New", 0, 12)); // NOI18N
        lblColor2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblColor2.setText("2");
        lblColor2.setFocusable(false);
        lblColor2.setOpaque(true);
        lblColor2.setPreferredSize(new java.awt.Dimension(20, 20));
        pnlQuickColorHints.add(lblColor2);

        lblColor3.setBackground(new java.awt.Color(255, 0, 0));
        lblColor3.setFont(new java.awt.Font("Courier New", 0, 12)); // NOI18N
        lblColor3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblColor3.setText("3");
        lblColor3.setFocusable(false);
        lblColor3.setOpaque(true);
        lblColor3.setPreferredSize(new java.awt.Dimension(20, 20));
        pnlQuickColorHints.add(lblColor3);

        lblColor4.setBackground(new java.awt.Color(0, 0, 255));
        lblColor4.setFont(new java.awt.Font("Courier New", 0, 12)); // NOI18N
        lblColor4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblColor4.setText("4");
        lblColor4.setFocusable(false);
        lblColor4.setOpaque(true);
        lblColor4.setPreferredSize(new java.awt.Dimension(20, 20));
        pnlQuickColorHints.add(lblColor4);

        lblColor5.setBackground(new java.awt.Color(255, 255, 0));
        lblColor5.setFont(new java.awt.Font("Courier New", 0, 12)); // NOI18N
        lblColor5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblColor5.setText("5");
        lblColor5.setFocusable(false);
        lblColor5.setOpaque(true);
        lblColor5.setPreferredSize(new java.awt.Dimension(20, 20));
        pnlQuickColorHints.add(lblColor5);

        lblColor6.setBackground(new java.awt.Color(255, 128, 0));
        lblColor6.setFont(new java.awt.Font("Courier New", 0, 12)); // NOI18N
        lblColor6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblColor6.setText("6");
        lblColor6.setFocusable(false);
        lblColor6.setOpaque(true);
        lblColor6.setPreferredSize(new java.awt.Dimension(20, 20));
        pnlQuickColorHints.add(lblColor6);

        lblColor7.setBackground(new java.awt.Color(255, 0, 255));
        lblColor7.setFont(new java.awt.Font("Courier New", 0, 12)); // NOI18N
        lblColor7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblColor7.setText("7");
        lblColor7.setFocusable(false);
        lblColor7.setOpaque(true);
        lblColor7.setPreferredSize(new java.awt.Dimension(20, 20));
        pnlQuickColorHints.add(lblColor7);

        lblColor8.setBackground(new java.awt.Color(0, 255, 0));
        lblColor8.setFont(new java.awt.Font("Courier New", 0, 12)); // NOI18N
        lblColor8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblColor8.setText("8");
        lblColor8.setFocusable(false);
        lblColor8.setOpaque(true);
        lblColor8.setPreferredSize(new java.awt.Dimension(20, 20));
        pnlQuickColorHints.add(lblColor8);

        lblColor9.setBackground(new java.awt.Color(140, 90, 30));
        lblColor9.setFont(new java.awt.Font("Courier New", 0, 12)); // NOI18N
        lblColor9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblColor9.setText("9");
        lblColor9.setFocusable(false);
        lblColor9.setOpaque(true);
        lblColor9.setPreferredSize(new java.awt.Dimension(20, 20));
        pnlQuickColorHints.add(lblColor9);

        lblColor0.setBackground(new java.awt.Color(255, 204, 153));
        lblColor0.setFont(new java.awt.Font("Courier New", 0, 12)); // NOI18N
        lblColor0.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblColor0.setText("0");
        lblColor0.setFocusable(false);
        lblColor0.setOpaque(true);
        lblColor0.setPreferredSize(new java.awt.Dimension(20, 20));
        pnlQuickColorHints.add(lblColor0);

        pnlInfo.add(pnlQuickColorHints);

        mnbMainMenu.setFocusable(false);

        mnuFile.setText("File");
        mnuFile.setFocusable(false);

        mniSaveModel.setText("Save 3D Model...");
        mniSaveModel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniSaveModelActionPerformed(evt);
            }
        });
        mnuFile.add(mniSaveModel);

        mniSaveModelAndSubmodels.setText("Save 3D Model (+submodels)...");
        mniSaveModelAndSubmodels.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniSaveModelAndSubmodelsActionPerformed(evt);
            }
        });
        mnuFile.add(mniSaveModelAndSubmodels);
        mnuFile.add(fileSep1);

        mniLoadModel.setText("Load 3D Model...");
        mniLoadModel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniLoadModelActionPerformed(evt);
            }
        });
        mnuFile.add(mniLoadModel);

        mniLoadModelAndSubmodels.setText("Load 3D Model (+submodels)...");
        mniLoadModelAndSubmodels.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniLoadModelAndSubmodelsActionPerformed(evt);
            }
        });
        mnuFile.add(mniLoadModelAndSubmodels);
        mnuFile.add(fileSep2);

        mniImportImage2D.setText("Import Image as 2D Model...");
        mniImportImage2D.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniImportImage2DActionPerformed(evt);
            }
        });
        mnuFile.add(mniImportImage2D);

        mnbMainMenu.add(mnuFile);

        mnuModel.setText("Model");
        mnuModel.setFocusable(false);

        mniNewModel.setText("New Model...");
        mniNewModel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniNewModelActionPerformed(evt);
            }
        });
        mnuModel.add(mniNewModel);

        mniNewSubModel.setText("New Sub-Model...");
        mniNewSubModel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniNewSubModelActionPerformed(evt);
            }
        });
        mnuModel.add(mniNewSubModel);

        mniDeleteModel.setText("Delete Model");
        mniDeleteModel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniDeleteModelActionPerformed(evt);
            }
        });
        mnuModel.add(mniDeleteModel);
        mnuModel.add(sepModel1);

        mniChangeModelPivot.setText("Change Model Pivot");
        mniChangeModelPivot.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniChangeModelPivotActionPerformed(evt);
            }
        });
        mnuModel.add(mniChangeModelPivot);

        mniSmoothModel.setText("Smooth Model");
        mniSmoothModel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniSmoothModelActionPerformed(evt);
            }
        });
        mnuModel.add(mniSmoothModel);

        mniHollowModel.setText("Hollow Model");
        mniHollowModel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniHollowModelActionPerformed(evt);
            }
        });
        mnuModel.add(mniHollowModel);

        mniPuffModel.setText("Puff Model...");
        mniPuffModel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniPuffModelActionPerformed(evt);
            }
        });
        mnuModel.add(mniPuffModel);
        mnuModel.add(sepModel2);

        mniCopyModel.setText("Copy Model...");
        mniCopyModel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniCopyModelActionPerformed(evt);
            }
        });
        mnuModel.add(mniCopyModel);

        mniCutModel.setText("Cut Model...");
        mniCutModel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniCutModelActionPerformed(evt);
            }
        });
        mnuModel.add(mniCutModel);

        mniPasteModel.setText("Paste Model");
        mniPasteModel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniPasteModelActionPerformed(evt);
            }
        });
        mnuModel.add(mniPasteModel);

        mniPasteAsSubmodel.setText("Paste As Submodel");
        mniPasteAsSubmodel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniPasteAsSubmodelActionPerformed(evt);
            }
        });
        mnuModel.add(mniPasteAsSubmodel);

        mnbMainMenu.add(mnuModel);

        mnuAnimation.setText("Animation");
        mnuAnimation.setFocusable(false);

        mniSaveAnimationFile.setText("Save Animation File...");
        mniSaveAnimationFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniSaveAnimationFileActionPerformed(evt);
            }
        });
        mnuAnimation.add(mniSaveAnimationFile);

        mniLoadAnimationFile.setText("Load Animation File...");
        mniLoadAnimationFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniLoadAnimationFileActionPerformed(evt);
            }
        });
        mnuAnimation.add(mniLoadAnimationFile);
        mnuAnimation.add(animSep1);

        mniRunAnimation.setText("Run Animation...");
        mniRunAnimation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniRunAnimationActionPerformed(evt);
            }
        });
        mnuAnimation.add(mniRunAnimation);

        mnbMainMenu.add(mnuAnimation);

        setJMenuBar(mnbMainMenu);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(renderer3D, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(pnlInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(tabControls)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(pnlInfo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(renderer3D, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tabControls, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // KEY EVENTS:
    private void formKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyPressed
        Cube selectedCube = Global.getSelectedCube();
        switch (evt.getKeyCode())
        {
            case KeyEvent.VK_ESCAPE:
                System.exit(0);
            case KeyEvent.VK_BACK_QUOTE:                
                if (selectedCube != null)
                    selectedCube.setFaceColor(btnDefaultColor.getBackground());
                break;
            case KeyEvent.VK_0:
                if (shiftPressed && ctrlPressed)
                    colorizeModel(btnQuickColor0.getBackground());
                else if (shiftPressed)
                    setDefaultColor(btnQuickColor0.getBackground());
                else if (selectedCube != null)
                    selectedCube.setFaceColor(btnQuickColor0.getBackground());
                break;
            case KeyEvent.VK_1:
                if (shiftPressed && ctrlPressed)
                    colorizeModel(btnQuickColor1.getBackground());
                else if (shiftPressed)
                    setDefaultColor(btnQuickColor1.getBackground());
                else if (selectedCube != null)
                    selectedCube.setFaceColor(btnQuickColor1.getBackground());
                break;
            case KeyEvent.VK_2:
                if (shiftPressed && ctrlPressed)
                    colorizeModel(btnQuickColor2.getBackground());
                else if (shiftPressed)
                    setDefaultColor(btnQuickColor2.getBackground());
                else if (selectedCube != null)
                    selectedCube.setFaceColor(btnQuickColor2.getBackground());
                break;
            case KeyEvent.VK_3:
                if (shiftPressed && ctrlPressed)
                    colorizeModel(btnQuickColor3.getBackground());
                else if (shiftPressed)
                    setDefaultColor(btnQuickColor3.getBackground());
                else if (selectedCube != null)
                    selectedCube.setFaceColor(btnQuickColor3.getBackground());
                break;
            case KeyEvent.VK_4:
                if (shiftPressed && ctrlPressed)
                    colorizeModel(btnQuickColor4.getBackground());
                else if (shiftPressed)
                    setDefaultColor(btnQuickColor4.getBackground());
                else if (selectedCube != null)
                    selectedCube.setFaceColor(btnQuickColor4.getBackground());
                break;
            case KeyEvent.VK_5:
                if (shiftPressed && ctrlPressed)
                    colorizeModel(btnQuickColor5.getBackground());
                else if (shiftPressed)
                    setDefaultColor(btnQuickColor5.getBackground());
                else if (selectedCube != null)
                    selectedCube.setFaceColor(btnQuickColor5.getBackground());
                break;
            case KeyEvent.VK_6:
                if (shiftPressed && ctrlPressed)
                    colorizeModel(btnQuickColor6.getBackground());
                else if (shiftPressed)
                    setDefaultColor(btnQuickColor6.getBackground());
                else if (selectedCube != null)
                    selectedCube.setFaceColor(btnQuickColor6.getBackground());
                break;
            case KeyEvent.VK_7:
                if (shiftPressed && ctrlPressed)
                    colorizeModel(btnQuickColor7.getBackground());
                else if (shiftPressed)
                    setDefaultColor(btnQuickColor7.getBackground());
                else if (selectedCube != null)
                    selectedCube.setFaceColor(btnQuickColor7.getBackground());
                break;
            case KeyEvent.VK_8:
                if (shiftPressed && ctrlPressed)
                    colorizeModel(btnQuickColor8.getBackground());
                else if (shiftPressed)
                    setDefaultColor(btnQuickColor8.getBackground());
                else if (selectedCube != null)
                    selectedCube.setFaceColor(btnQuickColor8.getBackground());
                break;
            case KeyEvent.VK_9:
                if (shiftPressed && ctrlPressed)
                    colorizeModel(btnQuickColor9.getBackground());
                else if (shiftPressed)
                    setDefaultColor(btnQuickColor9.getBackground());
                else if (selectedCube != null)
                    selectedCube.setFaceColor(btnQuickColor9.getBackground());
                break;                
            case KeyEvent.VK_UP:
                if (ctrlPressed && shiftPressed)
                    doKeySceneRotate(0,-1,0);
                else if (altPressed && shiftPressed)
                    doKeyModelRotate(0,-1,0);
                else if (ctrlPressed)
                    doKeySceneMove(0,-1,0);
                else if (altPressed)
                    doKeyModelMove(0,-1,0);
                else
                    doSelectionCubeMove(0, -1, 0);
                break;
            case KeyEvent.VK_DOWN:
                if (ctrlPressed && shiftPressed)
                    doKeySceneRotate(0,1,0);
                else if (altPressed && shiftPressed)
                    doKeyModelRotate(0,1,0);
                else if (ctrlPressed)
                    doKeySceneMove(0,1,0);
                else if (altPressed)
                    doKeyModelMove(0,1,0);
                else
                    doSelectionCubeMove(0, 1, 0);
                break;
            case KeyEvent.VK_LEFT:
                if (ctrlPressed && shiftPressed)
                    doKeySceneRotate(-1,0,0);
                else if (altPressed && shiftPressed)
                    doKeyModelRotate(-1,0,0);
                else if (ctrlPressed)
                    doKeySceneMove(-1,0,0);
                else if (altPressed)
                    doKeyModelMove(-1,0,0);
                else
                    doSelectionCubeMove(-1,0,0);
                break;
            case KeyEvent.VK_RIGHT:
                if (ctrlPressed && shiftPressed)
                    doKeySceneRotate(1,0,0);
                else if (altPressed && shiftPressed)
                    doKeyModelRotate(1,0,0);
                else if (ctrlPressed)
                    doKeySceneMove(1,0,0);
                else if (altPressed)
                    doKeyModelMove(1,0,0);
                else
                    doSelectionCubeMove(1,0,0);
                break;
            case KeyEvent.VK_SUBTRACT:
                if (ctrlPressed && shiftPressed)
                    doKeySceneRotate(0,0,1);
                else if (altPressed && shiftPressed)
                    doKeyModelRotate(0,0,1);
                else if (ctrlPressed)
                    doKeySceneMove(0,0,1);
                else if (altPressed)
                    doKeyModelMove(0,0,1);
                else
                    doSelectionCubeMove(0,0,1);
                break;
            case KeyEvent.VK_ADD:
                if (ctrlPressed && shiftPressed)
                    doKeySceneRotate(0,0,-1);
                else if (altPressed && shiftPressed)
                    doKeyModelRotate(0,0,-1);
                else if (ctrlPressed)
                    doKeySceneMove(0,0,-1);
                else if (altPressed)
                    doKeyModelMove(0,0,-1);
                else
                    doSelectionCubeMove(0,0,-1);
                break;
            case KeyEvent.VK_SPACE:                
                constructCube(Global.SelectionCube.getLocation());
                Global.requestRefresh3D();
                break;
            case KeyEvent.VK_DELETE:                
                Global.removeCube(selectedCube);
                Global.requestRefresh3D();
                break;
            case KeyEvent.VK_C:
                changeModelPivotCube();
                break;
            case KeyEvent.VK_SHIFT:
                shiftPressed = true;
                break;
            case KeyEvent.VK_CONTROL:
                ctrlPressed = true;                
                break;
            case KeyEvent.VK_ALT:
                altPressed = true;
                break;
        }
    }//GEN-LAST:event_formKeyPressed

    private void mniSaveModelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniSaveModelActionPerformed
        if (Global.CurrentModel == null) return;        
        JFileChooser saveChooser = new QuickFileChooser();
        saveChooser.setCurrentDirectory(MODEL_DIR);
        saveChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory() || file.getName().toUpperCase().endsWith(".CZMOD");
            }
            @Override
            public String getDescription() {
                return "Cubizer 3D models (*.czmod)";
            }
        });
        File saveFile = new File(saveChooser.getCurrentDirectory(), Global.CurrentModel.getName() + ".czmod");
        saveChooser.setSelectedFile(saveFile);
        saveChooser.showSaveDialog(this);
        saveFile = saveChooser.getSelectedFile();
        ArrayList<Model3d> submodels = Global.getSubmodelsForParent(Global.CurrentModel); 
        saveSingleModel(Global.CurrentModel, saveFile, submodels);
    }//GEN-LAST:event_mniSaveModelActionPerformed

    private void mniLoadModelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniLoadModelActionPerformed
        JFileChooser loadChooser = new QuickFileChooser();
        loadChooser.setCurrentDirectory(MODEL_DIR);
        loadChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory() || file.getName().toUpperCase().endsWith(".CZMOD");
            }
            @Override
            public String getDescription() {
                return "Cubizer 3D models (*.czmod)";
            }
        });        
        loadChooser.showOpenDialog(this);
        File loadFile = loadChooser.getSelectedFile();
        if (loadFile == null)
            return;
        
        if (!loadFile.exists())
        {            
            System.err.println("Couldn't open a non-existing model file.");
            return;
        }
        loadSingleModel(loadFile);
    }//GEN-LAST:event_mniLoadModelActionPerformed

    private void formKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyReleased
        switch (evt.getKeyCode())
        {
            case KeyEvent.VK_SHIFT:
                shiftPressed = false;
                break;
            case KeyEvent.VK_CONTROL:
                ctrlPressed = false;
                break;
            case KeyEvent.VK_ALT:
                altPressed = false;
        }
    }//GEN-LAST:event_formKeyReleased

    private void renderer3DMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_renderer3DMouseDragged
        if (mouseOrigin == null) return;
        
        int xMove = evt.getX() - mouseOrigin.x;
        int yMove = evt.getY() - mouseOrigin.y;

        double horzMove = Math.abs(xMove);
        double vertMove = Math.abs(yMove);

        if (mouseAxis != null) // Do the mode operation on the selected axis:
        {
            switch(mouseMode)
            {
                case ROTATE:
                doMouseRotation(xMove, yMove);
                break;
                case MOVE:
                doMouseMove(xMove, yMove);
                break;
            }
            // Keep this current, as we'll make relative adjustments.
            mouseOrigin = evt.getPoint();
        }
        else // Select the axis by direction, if we've moved enough to make a guess:
        {
            double totalMove = Math.sqrt((horzMove*horzMove)+(vertMove*vertMove));
            if (totalMove >= AXIS_TRIGGER)
            {
                switch(mouseMode)
                {
                    case ROTATE:
                    if (vertMove < .5 * horzMove)
                    mouseAxis = AXIS.Y;
                    else if (horzMove < .5 * vertMove)
                    mouseAxis = AXIS.X;
                    else
                    mouseAxis = AXIS.Z;
                    break;
                    case MOVE:
                    if (vertMove < .5 * horzMove)
                    mouseAxis = AXIS.X;
                    else if (horzMove < .5 * vertMove)
                    mouseAxis = AXIS.Y;
                    else
                    mouseAxis = AXIS.Z;
                    break;
                }

                // Keep this current, as we'll make relative adjustments.
                mouseOrigin = evt.getPoint();
            }
        }
    }//GEN-LAST:event_renderer3DMouseDragged

    private void renderer3DMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_renderer3DMousePressed
        mouseOrigin = evt.getPoint();

        if (ctrlPressed)
            mouseScope = SCOPE.SCENE;
        else if (altPressed)
            mouseScope = SCOPE.MODEL;
        else
        {
            // We don't yet have an operation that doesn't involve SCENE or MODEL scope.
            mouseOrigin = null;
            return;
        }

        if (evt.getButton() == MouseEvent.BUTTON1)
            mouseMode = MODE.MOVE;
        else
            mouseMode = MODE.ROTATE;
    }//GEN-LAST:event_renderer3DMousePressed

    private void renderer3DMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_renderer3DMouseReleased
        mouseOrigin = null;
        mouseAxis = null;
    }//GEN-LAST:event_renderer3DMouseReleased

    private void renderer3DMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_renderer3DMouseWheelMoved
        int change = evt.getWheelRotation() * 10;
        change += sldScale.getValue();
        if (change > sldScale.getMaximum()) change = sldScale.getMaximum();
        if (change < sldScale.getMinimum()) change = sldScale.getMinimum();
        sldScale.setValue(change);
    }//GEN-LAST:event_renderer3DMouseWheelMoved

    private void chkShowScenePivotCubeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkShowScenePivotCubeActionPerformed
        Global.SCENE_PIVOT_VISIBLE = chkShowScenePivotCube.isSelected();
        Global.requestRefresh3D();
    }//GEN-LAST:event_chkShowScenePivotCubeActionPerformed

    private void chkShowSelectionCubeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkShowSelectionCubeActionPerformed
        Global.SELECTION_CUBE_VISIBLE = chkShowSelectionCube.isSelected();
        Global.requestRefresh3D();
    }//GEN-LAST:event_chkShowSelectionCubeActionPerformed

    private void spnZShadowIncStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnZShadowIncStateChanged
        
    }//GEN-LAST:event_spnZShadowIncStateChanged

    private void chkShowEdgesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkShowEdgesActionPerformed
        Global.ALL_CUBE_EDGES_VISIBLE = chkShowEdges.isSelected();
        Global.requestRefresh3D();
    }//GEN-LAST:event_chkShowEdgesActionPerformed

    private void chkShowFacesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkShowFacesActionPerformed
        Global.ALL_CUBE_FACES_VISIBLE = chkShowFaces.isSelected();
        Global.requestRefresh3D();
    }//GEN-LAST:event_chkShowFacesActionPerformed

    private void sldModelYAxisStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sldModelYAxisStateChanged
        if (!Global.CONTROL_LOCK)
        {
            if (Global.CurrentModel != null)
            {
                Global.CurrentModel.setYRotation(sldModelYAxis.getValue());
            }
            lblModelYDeg.setText(sldModelYAxis.getValue() + "°");
            Global.requestRefresh3D();
        }
    }//GEN-LAST:event_sldModelYAxisStateChanged

    private void sldModelXAxisStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sldModelXAxisStateChanged
        if (!Global.CONTROL_LOCK)
        {
            if (Global.CurrentModel != null)
            {
                Global.CurrentModel.setXRotation(sldModelXAxis.getValue());
            }
            lblModelXDeg.setText(sldModelXAxis.getValue() + "°");
            Global.requestRefresh3D();
        }
    }//GEN-LAST:event_sldModelXAxisStateChanged

    private void sldModelZAxisStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sldModelZAxisStateChanged
        if (!Global.CONTROL_LOCK)
        {
            if (Global.CurrentModel != null)
            {
                Global.CurrentModel.setZRotation(sldModelZAxis.getValue());
            }
            lblModelZDeg.setText(sldModelZAxis.getValue() + "°");
            Global.requestRefresh3D();
        }
    }//GEN-LAST:event_sldModelZAxisStateChanged

    private void lstModelsValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstModelsValueChanged
        Model3d model = (Model3d)lstModels.getSelectedValue();
        if (model != null)
        {
            Global.selectModel(model);
        }
    }//GEN-LAST:event_lstModelsValueChanged

    private void btnDefaultColorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDefaultColorActionPerformed
        ColorDialog dlg = new ColorDialog(this, true);
        dlg.setChosenColor(btnDefaultColor.getBackground());
        dlg.setVisible(true);
        Color color = dlg.getChosenColor();
        if (color != null)
        {
            setDefaultColor(color);
        }
    }//GEN-LAST:event_btnDefaultColorActionPerformed

    private void cboRestoreSceneOrientationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboRestoreSceneOrientationActionPerformed
        Orientation orientation = (Orientation)cboRestoreSceneOrientation.getSelectedItem();
        restoreSceneOrientation(orientation);
    }//GEN-LAST:event_cboRestoreSceneOrientationActionPerformed

    private void btnSaveSceneOrientationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveSceneOrientationActionPerformed
        String name = JOptionPane.showInputDialog("Enter a name for the current orientation:");
        saveSceneOrientation(name);
    }//GEN-LAST:event_btnSaveSceneOrientationActionPerformed

    private void btnSceneBottomViewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSceneBottomViewActionPerformed
        restoreSceneOrientation(BOTTOM_ORIENTATION);
    }//GEN-LAST:event_btnSceneBottomViewActionPerformed

    private void btnSceneRightViewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSceneRightViewActionPerformed
        restoreSceneOrientation(RIGHT_ORIENTATION);
    }//GEN-LAST:event_btnSceneRightViewActionPerformed

    private void btnSceneBackViewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSceneBackViewActionPerformed
        restoreSceneOrientation(BACK_ORIENTATION);
    }//GEN-LAST:event_btnSceneBackViewActionPerformed

    private void btnSceneTopViewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSceneTopViewActionPerformed
        restoreSceneOrientation(TOP_ORIENTATION);
    }//GEN-LAST:event_btnSceneTopViewActionPerformed

    private void btnSceneLeftViewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSceneLeftViewActionPerformed
        restoreSceneOrientation(LEFT_ORIENTATION);
    }//GEN-LAST:event_btnSceneLeftViewActionPerformed

    private void btnSceneFrontViewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSceneFrontViewActionPerformed
        restoreSceneOrientation(FRONT_ORIENTATION);
    }//GEN-LAST:event_btnSceneFrontViewActionPerformed

    private void sldScaleStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sldScaleStateChanged
        Global.SCALE = ((double)sldScale.getValue())*.01;
        Global.requestRefresh3D();
    }//GEN-LAST:event_sldScaleStateChanged

    private void sldSceneYAxisStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sldSceneYAxisStateChanged
        if (mouseAxis == null)
        {
            Global.sceneYRotation = sldSceneYAxis.getValue();
        }
        lblSceneYDeg.setText(sldSceneYAxis.getValue() + "°");
        Global.requestRefresh3D();
    }//GEN-LAST:event_sldSceneYAxisStateChanged

    private void sldSceneXAxisStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sldSceneXAxisStateChanged
        if (mouseAxis == null)
        {
            Global.sceneXRotation = sldSceneXAxis.getValue();
        }
        lblSceneXDeg.setText(sldSceneXAxis.getValue() + "°");
        Global.requestRefresh3D();
    }//GEN-LAST:event_sldSceneXAxisStateChanged

    private void sldSceneZAxisStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sldSceneZAxisStateChanged
        if (mouseAxis == null)
        {
            Global.sceneZRotation = sldSceneZAxis.getValue();
        }
        lblSceneZDeg.setText(sldSceneZAxis.getValue() + "°");
        Global.requestRefresh3D();
    }//GEN-LAST:event_sldSceneZAxisStateChanged

    private void chkShowModelPivotIndicatorsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkShowModelPivotIndicatorsActionPerformed
        Global.MODEL_PIVOTS_VISIBLE = chkShowModelPivotIndicators.isSelected();
        Global.requestRefresh3D();
    }//GEN-LAST:event_chkShowModelPivotIndicatorsActionPerformed

    private void btnQuickColor1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuickColor1ActionPerformed
        ColorDialog dlg = new ColorDialog(this, true);
        dlg.setChosenColor(btnQuickColor1.getBackground());
        dlg.setVisible(true);
        Color color = dlg.getChosenColor();
        if (color != null)
        {
            btnQuickColor1.setBackground(color);
            lblColor1.setBackground(color);
        }
    }//GEN-LAST:event_btnQuickColor1ActionPerformed

    private void btnQuickColor2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuickColor2ActionPerformed
        ColorDialog dlg = new ColorDialog(this, true);
        dlg.setChosenColor(btnQuickColor2.getBackground());
        dlg.setVisible(true);
        Color color = dlg.getChosenColor();
        if (color != null)
        {
            btnQuickColor2.setBackground(color);
            lblColor2.setBackground(color);
        }
    }//GEN-LAST:event_btnQuickColor2ActionPerformed

    private void btnQuickColor3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuickColor3ActionPerformed
        ColorDialog dlg = new ColorDialog(this, true);
        dlg.setChosenColor(btnQuickColor3.getBackground());
        dlg.setVisible(true);
        Color color = dlg.getChosenColor();
        if (color != null)
        {
            btnQuickColor3.setBackground(color);
            lblColor3.setBackground(color);
        }
    }//GEN-LAST:event_btnQuickColor3ActionPerformed

    private void btnQuickColor4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuickColor4ActionPerformed
        ColorDialog dlg = new ColorDialog(this, true);
        dlg.setChosenColor(btnQuickColor4.getBackground());
        dlg.setVisible(true);
        Color color = dlg.getChosenColor();
        if (color != null)
        {
            btnQuickColor4.setBackground(color);
            lblColor4.setBackground(color);
        }
    }//GEN-LAST:event_btnQuickColor4ActionPerformed

    private void btnQuickColor5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuickColor5ActionPerformed
        ColorDialog dlg = new ColorDialog(this, true);
        dlg.setChosenColor(btnQuickColor5.getBackground());
        dlg.setVisible(true);
        Color color = dlg.getChosenColor();
        if (color != null)
        {
            btnQuickColor5.setBackground(color);
            lblColor5.setBackground(color);
        }
    }//GEN-LAST:event_btnQuickColor5ActionPerformed

    private void btnQuickColor6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuickColor6ActionPerformed
        ColorDialog dlg = new ColorDialog(this, true);
        dlg.setChosenColor(btnQuickColor6.getBackground());
        dlg.setVisible(true);
        Color color = dlg.getChosenColor();
        if (color != null)
        {
            btnQuickColor6.setBackground(color);
            lblColor6.setBackground(color);
        }
    }//GEN-LAST:event_btnQuickColor6ActionPerformed

    private void btnQuickColor7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuickColor7ActionPerformed
        ColorDialog dlg = new ColorDialog(this, true);
        dlg.setChosenColor(btnQuickColor7.getBackground());
        dlg.setVisible(true);
        Color color = dlg.getChosenColor();
        if (color != null)
        {
            btnQuickColor7.setBackground(color);
            lblColor7.setBackground(color);
        }
    }//GEN-LAST:event_btnQuickColor7ActionPerformed

    private void btnQuickColor8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuickColor8ActionPerformed
        ColorDialog dlg = new ColorDialog(this, true);
        dlg.setChosenColor(btnQuickColor8.getBackground());
        dlg.setVisible(true);
        Color color = dlg.getChosenColor();
        if (color != null)
        {
            btnQuickColor8.setBackground(color);
            lblColor9.setBackground(color);
        }
    }//GEN-LAST:event_btnQuickColor8ActionPerformed

    private void btnQuickColor9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuickColor9ActionPerformed
        ColorDialog dlg = new ColorDialog(this, true);
        dlg.setChosenColor(btnQuickColor9.getBackground());
        dlg.setVisible(true);
        Color color = dlg.getChosenColor();
        if (color != null)
        {
            btnQuickColor9.setBackground(color);
            lblColor9.setBackground(color);
        }
    }//GEN-LAST:event_btnQuickColor9ActionPerformed

    private void btnQuickColor0ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuickColor0ActionPerformed
        ColorDialog dlg = new ColorDialog(this, true);
        dlg.setChosenColor(btnQuickColor0.getBackground());
        dlg.setVisible(true);
        Color color = dlg.getChosenColor();
        if (color != null)
        {
            btnQuickColor0.setBackground(color);
            lblColor0.setBackground(color);
        }
    }//GEN-LAST:event_btnQuickColor0ActionPerformed

    private void btnBackgroundColorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackgroundColorActionPerformed
        ColorDialog dlg = new ColorDialog(this, true);
        dlg.setChosenColor(btnQuickColor0.getBackground());
        dlg.setVisible(true);
        Color color = dlg.getChosenColor();
        if (color != null)
        {
            renderer3D.setBackground(color);
            Global.requestRefresh3D();
        }
    }//GEN-LAST:event_btnBackgroundColorActionPerformed

    private void btnConstructLineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConstructLineActionPerformed
        ConstructLineDialog dlg = new ConstructLineDialog(this, true);
        dlg.setVisible(true);
        if (dlg.getDialogResult() == ShapeDialog.Result.OK)
        {
            constructLine(Global.SelectionCube.getLocation(), dlg.getLineLength(), dlg.getIncrement());
        }
    }//GEN-LAST:event_btnConstructLineActionPerformed

    private void btn0ZShadowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn0ZShadowActionPerformed
        spnZShadowInc.setValue(new Double(0));
        Global.Z_SHADOW_INCREMENT = (Double)spnZShadowInc.getValue();
        Global.requestRefresh3D();
    }//GEN-LAST:event_btn0ZShadowActionPerformed

    private void btnConstructRectangleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConstructRectangleActionPerformed
        ConstructRectangleDialog dlg = new ConstructRectangleDialog(this, true);
        dlg.setVisible(true);
        if (dlg.getDialogResult() == ShapeDialog.Result.OK)
        {
            constructRectangle(Global.SelectionCube.getLocation(), dlg.getLength1(), dlg.getIncrement1(), dlg.getLength2(), dlg.getIncrement2());
        }
    }//GEN-LAST:event_btnConstructRectangleActionPerformed

    private void btnConstructEllipseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConstructEllipseActionPerformed
        ConstructRectangleDialog dlg = new ConstructRectangleDialog(this, true);
        dlg.setTitle("Construct Ellipse");
        dlg.setVisible(true);
        if (dlg.getDialogResult() == ShapeDialog.Result.OK)
        {
            constructEllipse(Global.SelectionCube.getLocation(), dlg.getLength1(), dlg.getIncrement1(), dlg.getLength2(), dlg.getIncrement2());
        }
    }//GEN-LAST:event_btnConstructEllipseActionPerformed

    private void chkShowPivotMovementActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkShowPivotMovementActionPerformed
        Global.MODEL_PIVOTS_MOVEMENT = chkShowPivotMovement.isSelected();
        Global.SCENE_PIVOT_MOVEMENT = chkShowPivotMovement.isSelected();
        Global.requestRefresh3D();
    }//GEN-LAST:event_chkShowPivotMovementActionPerformed

    private void chkShowPivotRotationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkShowPivotRotationActionPerformed
        Global.MODEL_PIVOTS_ROTATION = chkShowPivotRotation.isSelected();
        Global.SCENE_PIVOT_ROTATION = chkShowPivotRotation.isSelected();
        Global.requestRefresh3D();
    }//GEN-LAST:event_chkShowPivotRotationActionPerformed

    private void chkShowSelectionMovementActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkShowSelectionMovementActionPerformed
        Global.SELECTION_CUBE_MOVEMENT = chkShowSelectionMovement.isSelected();
        Global.requestRefresh3D();
    }//GEN-LAST:event_chkShowSelectionMovementActionPerformed

    private void chkShowSelectionRotationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkShowSelectionRotationActionPerformed
        Global.SELECTION_CUBE_ROTATION = chkShowSelectionRotation.isSelected();
        Global.requestRefresh3D();
    }//GEN-LAST:event_chkShowSelectionRotationActionPerformed

    private void btnConstructCuboidActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConstructCuboidActionPerformed
        ConstructCuboidDialog dlg = new ConstructCuboidDialog(this, true);
        dlg.setVisible(true);
        if (dlg.getDialogResult() == ShapeDialog.Result.OK)
        {
            constructCuboid(Global.SelectionCube.getLocation(), dlg.getLength1(), dlg.getIncrement1(), dlg.getLength2(), dlg.getIncrement2(), dlg.getLength3(), dlg.getIncrement3());
        }
    }//GEN-LAST:event_btnConstructCuboidActionPerformed

    private void mniImportImage2DActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniImportImage2DActionPerformed
        JFileChooser loadChooser = new QuickFileChooser();
        loadChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory() || file.getName().toUpperCase().endsWith(".PNG");
            }
            @Override
            public String getDescription() {
                return "PNG images (*.png)";
            }
        });        
        loadChooser.showOpenDialog(this);
        File loadFile = loadChooser.getSelectedFile();
        if (loadFile == null)
            return;
        
        if (!loadFile.exists())
        {            
            System.err.println("Couldn't open a non-existing image file.");
            return;
        }
        try {
            BufferedImage image = ImageIO.read(loadFile);
            ImportImageDialog dlg = new ImportImageDialog(this, true, image);
            dlg.setVisible(true);
            if (dlg.getDialogResult() == ShapeDialog.Result.OK)
            {
                importImage(image, Global.SelectionCube.getLocation(), dlg.getLength1(), dlg.getIncrement1(), dlg.getLength2(), dlg.getIncrement2());
            }
            
        } catch (IOException ex) {
            Logger.getLogger(CubizerFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_mniImportImage2DActionPerformed

    private void mniNewModelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniNewModelActionPerformed
        String name = null;
        do 
        {
            name = JOptionPane.showInputDialog("Enter a name for the new model:");
            if (Global.getModelByName(name) != null)
            {
                JOptionPane.showMessageDialog(this, "The model name is already in use. Please choose a different name.", "Duplicate Name", JOptionPane.ERROR_MESSAGE);
            }
        } while (Global.getModelByName(name) != null);
        Cube pivot = Global.getSelectedCube();
        if (pivot == null)
        {
            pivot = new Cube(true);
            pivot.setLocation(Global.SelectionCube.getLocation());
        }
        Model3d model = new Model3d(name, pivot);
        Global.addModel(model);
        lstModels.setSelectedIndex(lstModels.getModel().getSize()-1);
        Global.requestRefresh3D();
    }//GEN-LAST:event_mniNewModelActionPerformed

    private void mniNewSubModelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniNewSubModelActionPerformed
        constructCube(Global.SelectionCube.getLocation());
        Cube joint = Global.getSelectedCube();
        String name = JOptionPane.showInputDialog("Enter a name for the new sub-model:");
        Model3d subModel = joint.createSubModel(name);
        Global.addModel(subModel);
        Global.requestRefresh3D();
    }//GEN-LAST:event_mniNewSubModelActionPerformed

    private void mniDeleteModelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniDeleteModelActionPerformed
        if (Global.CurrentModel != null)
        {
            Global.removeModel(Global.CurrentModel);
            if (lstModels.getModel().getSize() > 0)
            {
                lstModels.setSelectedIndex(0);
            }
            else
            {
                lstModels.setSelectedIndex(-1);
            }
        }
    }//GEN-LAST:event_mniDeleteModelActionPerformed

    private void mniChangeModelPivotActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniChangeModelPivotActionPerformed
        changeModelPivotCube();
    }//GEN-LAST:event_mniChangeModelPivotActionPerformed

    private void mniHollowModelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniHollowModelActionPerformed
        if (Global.CurrentModel != null)
        {
            Global.CurrentModel.hollow();
        }
        Global.requestRefresh3D();
        getModelList().repaint();
    }//GEN-LAST:event_mniHollowModelActionPerformed

    private void mniPuffModelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniPuffModelActionPerformed
        if (Global.CurrentModel == null)
            return;
            
        PuffModelDialog dlg = new PuffModelDialog(this, true);
        dlg.setVisible(true);
        if (dlg.getDialogResult() == PuffModelDialog.Result.OK)
        {
            switch(dlg.getPuffAxis())
            {
                case X:
                    Global.CurrentModel.puffX(dlg.isPositive(), dlg.isNegative(), dlg.getAmount());
                    break;
                case Y:
                    Global.CurrentModel.puffY(dlg.isPositive(), dlg.isNegative(), dlg.getAmount());
                    break;
                case Z:
                    Global.CurrentModel.puffZ(dlg.isPositive(), dlg.isNegative(), dlg.getAmount());
                    break;
            }
        }
        Global.requestRefresh3D();
        getModelList().repaint();
    }//GEN-LAST:event_mniPuffModelActionPerformed

    private void mniSaveModelAndSubmodelsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniSaveModelAndSubmodelsActionPerformed
        JFileChooser saveChooser = new QuickFileChooser();
        saveChooser.setCurrentDirectory(MODEL_DIR);
        saveChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory() || file.getName().toUpperCase().endsWith(".CZMOD");
            }
            @Override
            public String getDescription() {
                return "Cubizer 3D models (*.czmod)";
            }
        });
        File saveFile = new File(saveChooser.getCurrentDirectory(), Global.CurrentModel.getName() + ".czmod");
        saveChooser.setSelectedFile(saveFile);
        saveChooser.showSaveDialog(this);
        saveFile = saveChooser.getSelectedFile();
        saveModelAndSubmodels(Global.CurrentModel, saveFile);
    }//GEN-LAST:event_mniSaveModelAndSubmodelsActionPerformed

    private void mniLoadModelAndSubmodelsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniLoadModelAndSubmodelsActionPerformed
        JFileChooser loadChooser = new QuickFileChooser();
        loadChooser.setCurrentDirectory(MODEL_DIR);
        loadChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory() || file.getName().toUpperCase().endsWith(".CZMOD");
            }
            @Override
            public String getDescription() {
                return "Cubizer 3D models (*.czmod)";
            }
        });        
        loadChooser.showOpenDialog(this);
        File loadFile = loadChooser.getSelectedFile();
        if (loadFile == null)
            return;
        
        if (!loadFile.exists())
        {            
            System.err.println("Couldn't open a non-existing model file.");
            return;
        }
        loadModelAndSubmodels(loadFile);
    }//GEN-LAST:event_mniLoadModelAndSubmodelsActionPerformed

    private void btn2DCaptureActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn2DCaptureActionPerformed
        BufferedImage image = renderer3D.toSprite();
        try {
            ImageIO.write(image, "PNG", new File("test_sprite.png"));
        } catch (IOException ex) {
            Logger.getLogger(CubizerFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btn2DCaptureActionPerformed

    private void spnCaptureWidthStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnCaptureWidthStateChanged
        renderer3D.setCaptureSize((Integer)spnCaptureWidth.getValue(), (Integer)spnCaptureHeight.getValue());
        Global.requestRefresh3D();
    }//GEN-LAST:event_spnCaptureWidthStateChanged

    private void chkShowCaptureAreaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkShowCaptureAreaActionPerformed
        Global.CAPTURE_AREA_VISIBLE = chkShowCaptureArea.isSelected();
    }//GEN-LAST:event_chkShowCaptureAreaActionPerformed

    private void btnHideIndicatorsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHideIndicatorsActionPerformed
        chkShowCaptureArea.setSelected(false);
        chkShowEdges.setSelected(false);
        chkShowModelPivotIndicators.setSelected(false);
        chkShowPivotMovement.setSelected(false);
        chkShowPivotRotation.setSelected(false);
        chkShowScenePivotCube.setSelected(false);
        chkShowSelectionCube.setSelected(false);
        chkShowSelectionMovement.setSelected(false);
        chkShowSelectionRotation.setSelected(false);
        chkShowFaces.setSelected(true);      
        Global.ALL_CUBE_EDGES_VISIBLE = chkShowEdges.isSelected(); 
        Global.ALL_CUBE_FACES_VISIBLE = chkShowFaces.isSelected();
        Global.MODEL_PIVOTS_VISIBLE = chkShowModelPivotIndicators.isSelected();
        Global.MODEL_PIVOTS_MOVEMENT = chkShowPivotMovement.isSelected();
        Global.MODEL_PIVOTS_ROTATION = chkShowPivotRotation.isSelected();
        Global.SCENE_PIVOT_VISIBLE = chkShowScenePivotCube.isSelected();
        Global.SCENE_PIVOT_MOVEMENT = chkShowPivotMovement.isSelected();
        Global.SCENE_PIVOT_ROTATION = chkShowPivotRotation.isSelected();
        Global.SELECTION_CUBE_VISIBLE = chkShowSelectionCube.isSelected();
        Global.SELECTION_CUBE_MOVEMENT = chkShowSelectionMovement.isSelected();
        Global.SELECTION_CUBE_ROTATION = chkShowSelectionRotation.isSelected();
        Global.CAPTURE_AREA_VISIBLE = chkShowCaptureArea.isSelected();
        Global.requestRefresh3D();
    }//GEN-LAST:event_btnHideIndicatorsActionPerformed

    private void btnConstructSphereActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConstructSphereActionPerformed
        String sRadius = JOptionPane.showInputDialog(this, "Enter a radius for the Sphere:", 1);        
        try {
            int radius = Integer.parseInt(sRadius);
            constructSphere(Global.SelectionCube.getLocation(), radius);
        } catch (NumberFormatException ex)
        {
            JOptionPane.showMessageDialog(this, "Value must be an integer.", "Bad Number Format", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnConstructSphereActionPerformed

    private void lstModelsMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lstModelsMousePressed
        Model3d model = (Model3d)lstModels.getSelectedValue();
        if (model != null)
        {
            Global.selectModel(model);
        }
    }//GEN-LAST:event_lstModelsMousePressed

    private void chkShowModelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkShowModelActionPerformed
        if (Global.CurrentModel != null)
        {
            Global.CurrentModel.setVisible(chkShowModel.isSelected());
            Global.requestRefresh3D();
        }        
    }//GEN-LAST:event_chkShowModelActionPerformed

    private void mniCopyModelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniCopyModelActionPerformed
        copyModel();
    }//GEN-LAST:event_mniCopyModelActionPerformed

    private void mniPasteModelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniPasteModelActionPerformed
        pasteModel();
    }//GEN-LAST:event_mniPasteModelActionPerformed

    private void mniCutModelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniCutModelActionPerformed
        copyModel();
        Global.removeModel(Global.CurrentModel);
    }//GEN-LAST:event_mniCutModelActionPerformed

    private void chkUseHiResModeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkUseHiResModeActionPerformed
        Global.HI_RESOLUTION_MODE = chkUseHiResMode.isSelected();
    }//GEN-LAST:event_chkUseHiResModeActionPerformed

    private void btnSetZShadowIncActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSetZShadowIncActionPerformed
        Global.Z_SHADOW_INCREMENT = (Double)spnZShadowInc.getValue();
        Global.requestRefresh3D();
    }//GEN-LAST:event_btnSetZShadowIncActionPerformed

    private void mniPasteAsSubmodelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniPasteAsSubmodelActionPerformed
        pasteModelAsSubmodel();
    }//GEN-LAST:event_mniPasteAsSubmodelActionPerformed

    private void mniSmoothModelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniSmoothModelActionPerformed
        if (Global.CurrentModel != null)
            Global.CurrentModel.smooth();
        Global.requestRefresh3D();
    }//GEN-LAST:event_mniSmoothModelActionPerformed

    private void mniLoadAnimationFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniLoadAnimationFileActionPerformed
        JFileChooser loadChooser = new QuickFileChooser();
        loadChooser.setCurrentDirectory(ANIM_DIR);
        loadChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory() || file.getName().toUpperCase().endsWith(".CZANI");
            }
            @Override
            public String getDescription() {
                return "Cubizer animation sequences (*.czani)";
            }
        });        
        loadChooser.showOpenDialog(this);
        File loadFile = loadChooser.getSelectedFile();
        if (loadFile == null)
            return;
        
        if (!loadFile.exists())
        {            
            System.err.println("Couldn't open a non-existing animation sequence file.");
            return;
        }
        loadAnimationFile(loadFile);
    }//GEN-LAST:event_mniLoadAnimationFileActionPerformed

    private void btnCaptureDirectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCaptureDirectionActionPerformed
        if (Global.CurrentModel == null) return;
        
        Point3d dirRotation = Global.getSceneRotation();
        String dirName = JOptionPane.showInputDialog(this, "Enter a name for the current Scene direction:", "Direction", JOptionPane.QUESTION_MESSAGE);
        Direction direction = new Direction(dirName, dirRotation.x, dirRotation.y, dirRotation.z);
        ((DefaultListModel)lstDirections.getModel()).addElement(direction);
    }//GEN-LAST:event_btnCaptureDirectionActionPerformed

    private void btnCapturePoseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCapturePoseActionPerformed
        Pose pose = new Pose();
        for (int m = 0; m < Global.AllModels.size(); m++)
        {
            Model3d model = Global.AllModels.get(m);
            pose.addPosition(model, model.getRotation());
        }
        ((DefaultListModel)lstPoses.getModel()).addElement(pose);
    }//GEN-LAST:event_btnCapturePoseActionPerformed

    private void mniSaveAnimationFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniSaveAnimationFileActionPerformed
        if (Global.CurrentModel == null) return;        
        JFileChooser saveChooser = new QuickFileChooser();
        saveChooser.setCurrentDirectory(ANIM_DIR);
        saveChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory() || file.getName().toUpperCase().endsWith(".CZANI");
            }
            @Override
            public String getDescription() {
                return "Cubizer animation sequences (*.czani)";
            }
        });
        File saveFile = new File(saveChooser.getCurrentDirectory(), Global.CurrentModel.getName() + ".czani");
        saveChooser.setSelectedFile(saveFile);
        saveChooser.showSaveDialog(this);
        saveFile = saveChooser.getSelectedFile();        
        saveAnimationFile(saveFile);
    }//GEN-LAST:event_mniSaveAnimationFileActionPerformed

    private void lstDirectionsValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstDirectionsValueChanged
        Direction dir = (Direction)lstDirections.getSelectedValue();
        if (dir != null && Global.CurrentModel != null)
        {
            Global.setSceneRotation(dir.getXRot(), dir.getYRot(), dir.getZRot());
        }
        Global.requestRefresh3D();
    }//GEN-LAST:event_lstDirectionsValueChanged

    private void lstPosesValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstPosesValueChanged
        Pose pose = (Pose)lstPoses.getSelectedValue();
        if (pose != null && Global.CurrentModel != null)
        {
            for (int m = 0; m < pose.getPartCount(); m++)
            {
                Point3d rot = pose.getRotationAtIndex(m);
                pose.getModelAtIndex(m).setRotation(rot.x, rot.y, rot.z);
            }
        }
        Global.requestRefresh3D();
    }//GEN-LAST:event_lstPosesValueChanged

    private void mniRunAnimationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniRunAnimationActionPerformed
        
        int captureWidth = (Integer)spnCaptureWidth.getValue();
        int captureHeight = (Integer)spnCaptureHeight.getValue();
        int spriteWidth = (Integer)spnSpriteWidth.getValue();
        int spriteHeight = (Integer)spnSpriteHeight.getValue();
        
        if (captureWidth <= 0 || captureHeight <= 0 || spriteWidth <= 0 || spriteHeight <= 0)
        {
            JOptionPane.showMessageDialog(this, "All capture and sprite dimensions must be > 0 to create animation.", "Invalid Parameter(s)", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        Animation anim = new Animation("default", Global.CurrentModel, captureWidth, captureHeight, spriteWidth, spriteHeight);
        ArrayList<Direction> dirs = getDirections();
        ArrayList<Pose> poses = getPoses();
        for (int d = 0; d < dirs.size(); d++)
        {
            anim.addDirection(dirs.get(d));
        }
        for (int p = 0; p < poses.size(); p++)
        {
            anim.addPose(poses.get(p));
        }
        AnimationDialog animDlg = new AnimationDialog(this, true, anim);
        animDlg.setVisible(true);
    }//GEN-LAST:event_mniRunAnimationActionPerformed

    private void btnConvertColorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConvertColorActionPerformed
        ColorConversionDialog dlg = new ColorConversionDialog(this, true);
        dlg.setVisible(true);
        if (dlg.getDialogResult() == Result.OK)
        {
            Global.requestRefresh3D();
        }
    }//GEN-LAST:event_btnConvertColorActionPerformed

    private void spnCaptureHeightStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnCaptureHeightStateChanged
        renderer3D.setCaptureSize((Integer)spnCaptureWidth.getValue(), (Integer)spnCaptureHeight.getValue());
        Global.requestRefresh3D();
    }//GEN-LAST:event_spnCaptureHeightStateChanged

    private void btn10ZShadowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn10ZShadowActionPerformed
        spnZShadowInc.setValue(new Double(10));
        Global.Z_SHADOW_INCREMENT = (Double)spnZShadowInc.getValue();
        Global.requestRefresh3D();
    }//GEN-LAST:event_btn10ZShadowActionPerformed

    private void btn20ZShadowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn20ZShadowActionPerformed
        spnZShadowInc.setValue(new Double(20));
        Global.Z_SHADOW_INCREMENT = (Double)spnZShadowInc.getValue();
        Global.requestRefresh3D();
    }//GEN-LAST:event_btn20ZShadowActionPerformed

    private void btn30ZShadowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn30ZShadowActionPerformed
        spnZShadowInc.setValue(new Double(30));
        Global.Z_SHADOW_INCREMENT = (Double)spnZShadowInc.getValue();
        Global.requestRefresh3D();
    }//GEN-LAST:event_btn30ZShadowActionPerformed

    private void btn40ZShadowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn40ZShadowActionPerformed
        spnZShadowInc.setValue(new Double(40));
        Global.Z_SHADOW_INCREMENT = (Double)spnZShadowInc.getValue();
        Global.requestRefresh3D();
    }//GEN-LAST:event_btn40ZShadowActionPerformed

    private void rdoZShadowNearestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdoZShadowNearestActionPerformed
        changeZShadowOption();
    }//GEN-LAST:event_rdoZShadowNearestActionPerformed

    private void rdoZShadowScenePivotActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdoZShadowScenePivotActionPerformed
        changeZShadowOption();
    }//GEN-LAST:event_rdoZShadowScenePivotActionPerformed

    private void rdoZShadowModelPivotActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdoZShadowModelPivotActionPerformed
        changeZShadowOption();
    }//GEN-LAST:event_rdoZShadowModelPivotActionPerformed

    private void btnSetBaseShadowValActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSetBaseShadowValActionPerformed
        Global.BASE_Z_SHADOW_VAL = (Integer)spnBaseShadowVal.getValue();
        Global.requestRefresh3D();
    }//GEN-LAST:event_btnSetBaseShadowValActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(CubizerFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(CubizerFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(CubizerFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(CubizerFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        System.setProperty("com.sun.media.jai.disableMediaLib", "true");
        
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new CubizerFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPopupMenu.Separator animSep1;
    private javax.swing.ButtonGroup bgpZShadowRef;
    private javax.swing.JButton btn0ZShadow;
    private javax.swing.JButton btn10ZShadow;
    private javax.swing.JButton btn20ZShadow;
    private javax.swing.JButton btn2DCapture;
    private javax.swing.JButton btn30ZShadow;
    private javax.swing.JButton btn40ZShadow;
    private javax.swing.JButton btnBackgroundColor;
    private javax.swing.JButton btnCaptureDirection;
    private javax.swing.JButton btnCapturePose;
    private javax.swing.JButton btnConstructCuboid;
    private javax.swing.JButton btnConstructEllipse;
    private javax.swing.JButton btnConstructLine;
    private javax.swing.JButton btnConstructRectangle;
    private javax.swing.JButton btnConstructSphere;
    private javax.swing.JButton btnConvertColor;
    private javax.swing.JButton btnDefaultColor;
    private javax.swing.JButton btnHideIndicators;
    private javax.swing.JButton btnQuickColor0;
    private javax.swing.JButton btnQuickColor1;
    private javax.swing.JButton btnQuickColor2;
    private javax.swing.JButton btnQuickColor3;
    private javax.swing.JButton btnQuickColor4;
    private javax.swing.JButton btnQuickColor5;
    private javax.swing.JButton btnQuickColor6;
    private javax.swing.JButton btnQuickColor7;
    private javax.swing.JButton btnQuickColor8;
    private javax.swing.JButton btnQuickColor9;
    private javax.swing.JButton btnSaveSceneOrientation;
    private javax.swing.JButton btnSceneBackView;
    private javax.swing.JButton btnSceneBottomView;
    private javax.swing.JButton btnSceneFrontView;
    private javax.swing.JButton btnSceneLeftView;
    private javax.swing.JButton btnSceneRightView;
    private javax.swing.JButton btnSceneTopView;
    private javax.swing.JButton btnSetBaseShadowVal;
    private javax.swing.JButton btnSetZShadowInc;
    private javax.swing.JComboBox cboRestoreSceneOrientation;
    private javax.swing.JCheckBox chkShowCaptureArea;
    private javax.swing.JCheckBox chkShowEdges;
    private javax.swing.JCheckBox chkShowFaces;
    private javax.swing.JCheckBox chkShowModel;
    private javax.swing.JCheckBox chkShowModelPivotIndicators;
    private javax.swing.JCheckBox chkShowPivotMovement;
    private javax.swing.JCheckBox chkShowPivotRotation;
    private javax.swing.JCheckBox chkShowScenePivotCube;
    private javax.swing.JCheckBox chkShowSelectionCube;
    private javax.swing.JCheckBox chkShowSelectionMovement;
    private javax.swing.JCheckBox chkShowSelectionRotation;
    private javax.swing.JCheckBox chkUseHiResMode;
    private javax.swing.JPopupMenu.Separator fileSep1;
    private javax.swing.JPopupMenu.Separator fileSep2;
    private javax.swing.JLabel lblBaseShadowVal;
    private javax.swing.JLabel lblCaptureHeight;
    private javax.swing.JLabel lblCaptureSize;
    private javax.swing.JLabel lblCaptureWidth;
    private javax.swing.JLabel lblColor0;
    private javax.swing.JLabel lblColor1;
    private javax.swing.JLabel lblColor2;
    private javax.swing.JLabel lblColor3;
    private javax.swing.JLabel lblColor4;
    private javax.swing.JLabel lblColor5;
    private javax.swing.JLabel lblColor6;
    private javax.swing.JLabel lblColor7;
    private javax.swing.JLabel lblColor8;
    private javax.swing.JLabel lblColor9;
    private javax.swing.JLabel lblColorDef;
    private javax.swing.JLabel lblCurrentModel;
    private javax.swing.JLabel lblDefaultColor;
    private javax.swing.JLabel lblInterpolationNote;
    private javax.swing.JLabel lblModelXAxis;
    private javax.swing.JLabel lblModelXDeg;
    private javax.swing.JLabel lblModelYAxis;
    private javax.swing.JLabel lblModelYDeg;
    private javax.swing.JLabel lblModelZAxis;
    private javax.swing.JLabel lblModelZDeg;
    private javax.swing.JLabel lblQuickColor0;
    private javax.swing.JLabel lblQuickColor1;
    private javax.swing.JLabel lblQuickColor2;
    private javax.swing.JLabel lblQuickColor3;
    private javax.swing.JLabel lblQuickColor4;
    private javax.swing.JLabel lblQuickColor5;
    private javax.swing.JLabel lblQuickColor6;
    private javax.swing.JLabel lblQuickColor7;
    private javax.swing.JLabel lblQuickColor8;
    private javax.swing.JLabel lblQuickColor9;
    private javax.swing.JLabel lblScale;
    private javax.swing.JLabel lblSceneXAxis;
    private javax.swing.JLabel lblSceneXDeg;
    private javax.swing.JLabel lblSceneYAxis;
    private javax.swing.JLabel lblSceneYDeg;
    private javax.swing.JLabel lblSceneZAxis;
    private javax.swing.JLabel lblSceneZDeg;
    private javax.swing.JLabel lblSpriteHeight;
    private javax.swing.JLabel lblSpriteSize;
    private javax.swing.JLabel lblSpriteWidth;
    private javax.swing.JLabel lblZShadowInc;
    private javax.swing.JList lstDirections;
    private javax.swing.JList lstModels;
    private javax.swing.JList lstPoses;
    private javax.swing.JMenuBar mnbMainMenu;
    private javax.swing.JMenuItem mniChangeModelPivot;
    private javax.swing.JMenuItem mniCopyModel;
    private javax.swing.JMenuItem mniCutModel;
    private javax.swing.JMenuItem mniDeleteModel;
    private javax.swing.JMenuItem mniHollowModel;
    private javax.swing.JMenuItem mniImportImage2D;
    private javax.swing.JMenuItem mniLoadAnimationFile;
    private javax.swing.JMenuItem mniLoadModel;
    private javax.swing.JMenuItem mniLoadModelAndSubmodels;
    private javax.swing.JMenuItem mniNewModel;
    private javax.swing.JMenuItem mniNewSubModel;
    private javax.swing.JMenuItem mniPasteAsSubmodel;
    private javax.swing.JMenuItem mniPasteModel;
    private javax.swing.JMenuItem mniPuffModel;
    private javax.swing.JMenuItem mniRunAnimation;
    private javax.swing.JMenuItem mniSaveAnimationFile;
    private javax.swing.JMenuItem mniSaveModel;
    private javax.swing.JMenuItem mniSaveModelAndSubmodels;
    private javax.swing.JMenuItem mniSmoothModel;
    private javax.swing.JMenu mnuAnimation;
    private javax.swing.JMenu mnuFile;
    private javax.swing.JMenu mnuModel;
    private javax.swing.JPanel pnlAnimation;
    private javax.swing.JPanel pnlColor;
    private javax.swing.JPanel pnlConstruction;
    private javax.swing.JPanel pnlDefaultColor;
    private javax.swing.JPanel pnlDirections;
    private javax.swing.JPanel pnlInfo;
    private javax.swing.JPanel pnlMiscOptions;
    private javax.swing.JPanel pnlModel;
    private javax.swing.JPanel pnlModelCubesOptions;
    private javax.swing.JPanel pnlModelRotation;
    private javax.swing.JPanel pnlModels;
    private javax.swing.JPanel pnlPivotOptions;
    private javax.swing.JPanel pnlPoses;
    private javax.swing.JPanel pnlQuickColorHints;
    private javax.swing.JPanel pnlQuickColors;
    private javax.swing.JPanel pnlScene;
    private javax.swing.JPanel pnlScenePresetViews;
    private javax.swing.JPanel pnlScenePresetViewsGrid;
    private javax.swing.JPanel pnlSceneRotation;
    private javax.swing.JPanel pnlSelectionOptions;
    private javax.swing.JPanel pnlShapes1D;
    private javax.swing.JPanel pnlShapes2D;
    private javax.swing.JPanel pnlShapes3D;
    private javax.swing.JPanel pnlTesting;
    private javax.swing.JPanel pnlVisualSettings;
    private javax.swing.JPanel pnlZShadow;
    private javax.swing.JRadioButton rdoZShadowModelPivot;
    private javax.swing.JRadioButton rdoZShadowNearest;
    private javax.swing.JRadioButton rdoZShadowScenePivot;
    private cubizer.Renderer3D renderer3D;
    private javax.swing.JScrollPane scpDirections;
    private javax.swing.JScrollPane scpModels;
    private javax.swing.JScrollPane scpPoses;
    private javax.swing.JPopupMenu.Separator sepModel1;
    private javax.swing.JPopupMenu.Separator sepModel2;
    private javax.swing.JSlider sldModelXAxis;
    private javax.swing.JSlider sldModelYAxis;
    private javax.swing.JSlider sldModelZAxis;
    private javax.swing.JSlider sldScale;
    private javax.swing.JSlider sldSceneXAxis;
    private javax.swing.JSlider sldSceneYAxis;
    private javax.swing.JSlider sldSceneZAxis;
    private javax.swing.JSpinner spnBaseShadowVal;
    private javax.swing.JSpinner spnCaptureHeight;
    private javax.swing.JSpinner spnCaptureWidth;
    private javax.swing.JSpinner spnSpriteHeight;
    private javax.swing.JSpinner spnSpriteWidth;
    private javax.swing.JSpinner spnZShadowInc;
    private javax.swing.JTabbedPane tabControls;
    // End of variables declaration//GEN-END:variables
}
