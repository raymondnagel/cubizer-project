/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cubizer;

/**
 *
 * @author Raymond
 */
public class Direction {

    private String name = null;
    private double xRot = 0;
    private double yRot = 0;
    private double zRot = 0;
    
    Direction (String name, double xRot, double yRot, double zRot)
    {
        this.name = name;
        this.xRot = xRot;
        this.yRot = yRot;
        this.zRot = zRot;
    }
    
    public String getName()
    {
        return this.name;
    }
    public double getXRot()
    {
        return this.xRot;        
    }
    public double getYRot()
    {
        return this.yRot;        
    }
    public double getZRot()
    {
        return this.zRot;        
    }
    public Point3d getRotation()
    {
        return new Point3d(xRot, yRot, zRot);
    }

    @Override
    public String toString() {
        return this.name + ":" + this.xRot + "," + this.yRot + "," + this.zRot;
    }
    
    
}
