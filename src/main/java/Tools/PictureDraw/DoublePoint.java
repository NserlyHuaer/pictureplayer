package Tools.PictureDraw;

import java.awt.*;
import java.awt.geom.Point2D;

public class DoublePoint {
    public double x;
    public double y;
    public DoublePoint(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void setLocation(double x, double y){
        this.x = x;
        this.y = y;
    }
}
