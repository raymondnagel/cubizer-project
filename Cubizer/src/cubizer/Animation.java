/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cubizer;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Raymond
 */
public class Animation {
    private String name = null;
    private Model3d baseModel = null;
    private int captureWidth = 0;
    private int captureHeight = 0;
    private int spriteWidth = 0;
    private int spriteHeight = 0;
    
    private ArrayList<Direction> directions = new ArrayList<>();
    private ArrayList<Pose> poses = new ArrayList<>(); 

    public Animation(String name, Model3d baseModel, int captureWidth, int captureHeight, int spriteWidth, int spriteHeight) {
        this.name = name; 
        this.baseModel = baseModel;
        this.captureWidth = captureWidth;
        this.captureHeight = captureHeight;
        this.spriteWidth = spriteWidth;
        this.spriteHeight = spriteHeight;
    }
    
    public String getName()
    {
        return this.name;
    }
    public int getPoseCount()
    {
        return poses.size();
    }
    
    public ArrayList<Direction> getDirections()
    {
        return this.directions;
    }
    public ArrayList<Pose> getPoses()
    {
        return this.poses;
    }
    
    public void addDirection(Direction dir)
    {
        this.directions.add(dir);
    }
    public void addPose(Pose pose)
    {
        this.poses.add(pose);
    }
    
    public BufferedImage createFrame(String directionName, int poseIndex)
    {
        Direction direction = getDirectionByName(directionName);
        Global.setSceneRotation(direction.getXRot(), direction.getYRot(), direction.getZRot());
        int partCount = poses.get(poseIndex).getPartCount();
        for (int p = 0; p < partCount; p++)
        {
            Point3d rotation = poses.get(poseIndex).getRotationAtIndex(p);
            poses.get(poseIndex).getModelAtIndex(p).setRotation(rotation.x, rotation.y, rotation.z);
        }
        Global.setCaptureSize(captureWidth, captureHeight);
        Global.RENDERER_3D.refresh();
        BufferedImage captureImage = Global.getCapture();
        captureImage = ImageHelper.scaleImage(captureImage, spriteWidth, spriteHeight, ImageHelper.Interpolation.BICUBIC);
        return captureImage;
    }
    
    private Direction getDirectionByName(String directionName)
    {
        for (int d = 0; d < directions.size(); d++)
        {
            if (directions.get(d).getName().equalsIgnoreCase(directionName))
                return directions.get(d);
        }
        System.err.println("Invalid direction name: " + directionName);
        return null;
    }
    
    
}
