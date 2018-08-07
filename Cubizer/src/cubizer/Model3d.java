/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cubizer;

import static cubizer.Global.requestRefresh3D;
import java.awt.Color;
import java.util.ArrayList;

/**
 *
 * @author rnagel
 */
public class Model3d {
    private double xRotation = 0;
    private double yRotation = 0;
    private double zRotation = 0;
    private boolean visible = true;
    private String name = null;
    private boolean selected = false;
    private ArrayList<Cube> cubes = new ArrayList<>();
    private Model3d parentModel = null;
    private Cube pivotCube = null;

    public Model3d(String name, Cube pivotCube) {
        setName(name);
        setPivotCube(pivotCube);
    }
    
    public Model3d(String name, Cube pivotCube, Model3d parent) {
        setName(name);
        setParentModel(parent);
        setPivotCube(pivotCube);
    }
    
    public void setParentModel(Model3d parent)
    {
        this.parentModel = parent;
    }
    public Model3d getParentModel()
    {
        return this.parentModel;
    }
    
    public void setSelected(boolean selected)
    {
        this.selected = selected;        
    }
    public boolean isSelected()
    {
        return this.selected;
    }
    
    public boolean isVisible()
    {
        return this.visible;
    }
    public void setVisible(boolean visible)
    {
        this.visible = visible;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    public String getName()
    {
        return this.name;
    }
    
    public ArrayList<Cube> getCubes()
    {
        return this.cubes;
    }
    
    
    public void addCube(Cube cube)
    {
        cube.attachToModel(this);
        if (!cubes.contains(cube))
        {
            cubes.add(cube);
        }
    }
    public void removeCube(Cube cube)
    {
        cube.detach();
        cubes.remove(cube);
    }
    
    public void setPivotCube(Cube pivotCube)
    {
        if (this.pivotCube != null)
        {
            this.pivotCube.setModelPivot(false);
        }        
        if (!cubes.contains(pivotCube))
        {
            addCube(pivotCube);
        }
        this.pivotCube = pivotCube;
        this.pivotCube.setModelPivot(true);
    }
    public Cube getPivotCube()
    {
        return this.pivotCube;
    }
    
    public Point3d getLocation()
    {
        return getPivotCube().getLocation();
    }
    public Point3d getScaledLocation()
    {
        return getPivotCube().getScaledLocation();
    }
    
    public void move(Point3d move)
    {
        for (int c = cubes.size()-1; c >= 0; c--)
        {            
            cubes.get(c).move(move);
        }
        ArrayList<Model3d> submodels = Global.getSubmodelsForParent(this);
        for (int m = 0; m < submodels.size(); m++)
        {
            submodels.get(m).move(move);
        }
        requestRefresh3D();
    }
    public void movePivotTo(Point3d newPivotLocation)
    {
        // This moves the whole model by specifying a new location for the existing Pivot.
        Point3d movePt = new Point3d(newPivotLocation.x-pivotCube.getLocation().x,
                                     newPivotLocation.y-pivotCube.getLocation().y,
                                     newPivotLocation.z-pivotCube.getLocation().z);
        move(movePt);
    }
    
    
    public void setRotation(double xDeg, double yDeg, double zDeg)
    {
        this.xRotation = xDeg;
        this.yRotation = yDeg;
        this.zRotation = zDeg;
    }    
    public Point3d getRotation()
    {
        return new Point3d(xRotation, yRotation, zRotation);
    }
    public Point3d getOppositeRotation()
    {
        return new Point3d(-xRotation, -yRotation, -zRotation);
    }
    public void rotate(double xDeg, double yDeg, double zDeg)
    {
        // X
        xRotation += xDeg;
        while (xRotation >= 360)
            xRotation -= 360;
        while (xRotation < 0)
            xRotation += 360;
        
        // Y
        yRotation += yDeg;
        while (yRotation >= 360)
            yRotation -= 360;
        while (yRotation < 0)
            yRotation += 360;
        
        // Z
        zRotation += zDeg;
        while (zRotation >= 360)
            zRotation -= 360;
        while (zRotation < 0)
            zRotation += 360;
    }
    
    public void setXRotation(double xRotation)
    {
        this.xRotation = xRotation;
    }
    public void setYRotation(double yRotation)
    {
        this.yRotation = yRotation;
    }
    public void setZRotation(double zRotation)
    {
        this.zRotation = zRotation;
    }
    public double getXRotation()
    {
        return xRotation;
    }
    public double getYRotation()
    {
        return yRotation;
    }
    public double getZRotation()
    {
        return zRotation;
    }
    

    public void colorize(Color color)
    {
        for (int c = 0; c < cubes.size(); c++)
        {
            cubes.get(c).setFaceColor(color);
        }
    }
    
    public void hollow()
    {
        for (int c = cubes.size()-1; c >= 0; c--)
        {
            if (cubes.get(c).getNeighborCount() == 6 && !cubes.get(c).isModelPivot())
            {
                Global.removeCube(cubes.get(c));
            }
        }
    }
    
    public void smooth()
    {
        for (int c = 0; c < cubes.size(); c++)
            cubes.get(c).smooth();
    }
    
    public void puffX(boolean positive, boolean negative, int times)
    {
        for (int t = 0; t < times; t++)
        {
            for (int c = cubes.size()-1; c >= 0; c--)
            {
                int neighbors = cubes.get(c).getSelectiveNeighbors(true, true, false, false, true, true);

                if (neighbors == 4)
                {
                    if (positive)
                    {
                        Point3d puffPoint = new Point3d(cubes.get(c).getLocation());  
                        puffPoint.x += Global.CUBE_SIDE_SIZE;
                        if (Global.getCubeAt(puffPoint) == null)
                        {
                            Cube newCube = new Cube(true);
                            newCube.setLocation(puffPoint);
                            newCube.setFaceColor(cubes.get(c).getFaceColor());
                            newCube.setEdgeColor(Global.DEFAULT_EDGE_COLOR);
                            this.addCube(newCube);
                        }
                    }
                    if (negative)
                    {
                        Point3d puffPoint = new Point3d(cubes.get(c).getLocation());  
                        puffPoint.x -= Global.CUBE_SIDE_SIZE;
                        if (Global.getCubeAt(puffPoint) == null)
                        {
                            Cube newCube = new Cube(true);
                            newCube.setLocation(puffPoint);
                            newCube.setFaceColor(cubes.get(c).getFaceColor());
                            newCube.setEdgeColor(Global.DEFAULT_EDGE_COLOR);
                            this.addCube(newCube);
                        }
                    }
                }
            }
        }
    }
    public void puffY(boolean positive, boolean negative, int times)
    {
        for (int t = 0; t < times; t++)
        {
            for (int c = cubes.size()-1; c >= 0; c--)
            {
                int neighbors = cubes.get(c).getSelectiveNeighbors(true, true, true, true, false, false);

                if (neighbors == 4)
                {
                    if (positive)
                    {
                        Point3d puffPoint = new Point3d(cubes.get(c).getLocation());  
                        puffPoint.y += Global.CUBE_SIDE_SIZE;
                        if (Global.getCubeAt(puffPoint) == null)
                        {
                            Cube newCube = new Cube(true);
                            newCube.setLocation(puffPoint);
                            newCube.setFaceColor(cubes.get(c).getFaceColor());
                            newCube.setEdgeColor(Global.DEFAULT_EDGE_COLOR);
                            this.addCube(newCube);
                        }
                    }
                    if (negative)
                    {
                        Point3d puffPoint = new Point3d(cubes.get(c).getLocation());  
                        puffPoint.y -= Global.CUBE_SIDE_SIZE;
                        if (Global.getCubeAt(puffPoint) == null)
                        {
                            Cube newCube = new Cube(true);
                            newCube.setLocation(puffPoint);
                            newCube.setFaceColor(cubes.get(c).getFaceColor());
                            newCube.setEdgeColor(Global.DEFAULT_EDGE_COLOR);
                            this.addCube(newCube);
                        }
                    }
                }
            }
        }
    }
    public void puffZ(boolean positive, boolean negative, int times)
    {
        for (int t = 0; t < times; t++)
        {
            for (int c = cubes.size()-1; c >= 0; c--)
            {
                int neighbors = cubes.get(c).getSelectiveNeighbors(false, false, true, true, true, true);

                if (neighbors == 4)
                {
                    if (positive)
                    {
                        Point3d puffPoint = new Point3d(cubes.get(c).getLocation());  
                        puffPoint.z += Global.CUBE_SIDE_SIZE;
                        if (Global.getCubeAt(puffPoint) == null)
                        {
                            Cube newCube = new Cube(true);
                            newCube.setLocation(puffPoint);
                            newCube.setFaceColor(cubes.get(c).getFaceColor());
                            newCube.setEdgeColor(Global.DEFAULT_EDGE_COLOR);
                            this.addCube(newCube);
                        }
                    }
                    if (negative)
                    {
                        Point3d puffPoint = new Point3d(cubes.get(c).getLocation());  
                        puffPoint.z -= Global.CUBE_SIDE_SIZE;
                        if (Global.getCubeAt(puffPoint) == null)
                        {
                            Cube newCube = new Cube(true);
                            newCube.setLocation(puffPoint);
                            newCube.setFaceColor(cubes.get(c).getFaceColor());
                            newCube.setEdgeColor(Global.DEFAULT_EDGE_COLOR);
                            this.addCube(newCube);
                        }
                    }
                }
            }
        }
    }
    
    public void assignUniqueCubeIds()
    {
        for (int i = 0; i < cubes.size(); i++)
        {
            cubes.get(i).assignUniqueId(i);
        }
    }
    
    @Override
    public String toString() {
        return name + " (" + cubes.size() + " cubes)";
    }
    
    
}
