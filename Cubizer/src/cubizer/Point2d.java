/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cubizer;

/**
 *
 * @author rnagel
 */
public class Point2d {
    public double x = 0;
    public double y = 0;

    public Point2d(double x, double y) {
        setPoint(x, y);
    }  
    
    public final void setPoint(double x, double y)
    {
        this.x = x;
        this.y = y;
    }
    
    public int X()
    {
        return (int)x;
    }
    public int Y()
    {
        return (int)y;
    }

    @Override
    public String toString() {
        return "[" + X() + "," + Y() + "]";
    }
        
}
