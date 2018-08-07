/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cubizer;

/**
 *
 * @author rnagel
 */
public class Point3d {
    public double x = 0;
    public double y = 0;
    public double z = 0;

    public Point3d(double x, double y, double z) {
        setPoint(x, y, z);
    }  

    public Point3d(Point3d point) {
        copyFromPoint(point);
    }
    
    
    
    public final void copyFromPoint(Point3d point)
    {
        this.x = point.x;
        this.y = point.y;
        this.z = point.z;
    }
    public final void setPoint(double x, double y, double z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public void addPoint(Point3d point)
    {
        this.x+=point.x;
        this.y+=point.y;
        this.z+=point.z;
    }
    public void subtractPoint(Point3d point)
    {
        this.x-=point.x;
        this.y-=point.y;
        this.z-=point.z;
    }
    public void addScaledPoint(Point3d point, double scale)
    {
        this.x+=(point.x*scale);
        this.y+=(point.y*scale);
        this.z+=(point.z*scale);
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
    
    public Point2d toPoint2d()
    {
        return new Point2d(x, y);
    }

    public boolean equals(Point3d point3d) {
        return this.x == point3d.x &&
               this.y == point3d.y &&
               this.z == point3d.z;
    }
    
    @Override
    public String toString() {
        return "[" + X() + "," + Y() + "," + Z() + "]";
    }
}
