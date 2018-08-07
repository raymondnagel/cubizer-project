/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cubizer;

/**
 *
 * @author rnagel
 */
public class Orientation {
    String name = null;
    public double x = 0;
    public double y = 0;
    public double z = 0;

    public Orientation(String name, double x, double y, double z) {
        this.name = name;
        setAxes(x, y, z);
    }  
    
    public final void setAxes(double x, double y, double z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public int X()
    {
        return (int)x;
    }
    public int Y()
    {
        return (int)y;
    }
    public int Z()
    {
        return (int)z;
    }
    
    @Override
    public String toString() {
        return name + ": [" + X() + "," + Y() + "," + Z() + "]";
    }
}
