/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cubizer;

import java.util.ArrayList;

/**
 *
 * @author Raymond
 */
public class Pose {
    private ArrayList<Model3d> models = new ArrayList<>();
    private ArrayList<Point3d> rotations = new ArrayList<>();

    public Pose() {
        
    }
    
    public int getPartCount()
    {
        return models.size();
    }
    
    public void addPosition(String modelAlias, Point3d rotation, ModelAliasConverter converter)
    {
        Model3d model = converter.getAlias(modelAlias);
        addPosition(model, rotation);
    }
    
    public void addPosition(Model3d model, Point3d rotation)
    {
        models.add(model);
        rotations.add(rotation);
    }
    
    public Model3d getModelAtIndex(int index)
    {
        return models.get(index);
    }
    public Point3d getRotationAtIndex(int index)
    {
        return rotations.get(index);
    }
    public String getPositionStringAtIndex(int index)
    {
        String modelName = getModelAtIndex(index).getName();
        Point3d modelRot = getRotationAtIndex(index);
        return modelName.split("_")[1] + ":" + modelRot.x + "," + modelRot.y + "," + modelRot.z;
    }

    @Override
    public String toString() {
        return this.hashCode() + " (" + getPartCount() + " parts)";
    }

    
}
