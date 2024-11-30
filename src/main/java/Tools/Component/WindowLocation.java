package Tools.Component;

import Size.SizeOperate;

import javax.swing.*;
import java.awt.*;

public class WindowLocation {
    public static Point ParentCenter(Window parent, int sonWidth, int sonHeight) {
        int parentWidth = parent.getSize().width;
        int parentHeight = parent.getSize().height;
        int resultX = (parentWidth - sonWidth) / 2 + parent.getLocation().x;
        int resultY = (parentHeight - sonHeight) / 2 + parent.getLocation().y;
        int windowWidth = SizeOperate.screenSize.width;
        int windowHeight = SizeOperate.screenSize.height;
        if (sonWidth + resultX > windowWidth) {
            resultX = (windowWidth - sonWidth) / 2;
        }
        if (sonHeight + resultY > windowHeight) {
            resultY = (windowHeight - sonHeight) / 2;
        }
        return new Point(resultX, resultY);
    }
}
