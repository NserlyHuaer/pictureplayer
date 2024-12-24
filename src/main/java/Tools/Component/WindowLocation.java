package Tools.Component;

import Size.SizeOperate;

import java.awt.*;

public class WindowLocation {
    public static Point ComponentCenter(Window parent, int sonWidth, int sonHeight) {
        int parentWidth = parent.getSize().width;
        int parentHeight = parent.getSize().height;
        int resultX = (parentWidth - sonWidth) / 2 + parent.getLocation().x;
        int resultY = (parentHeight - sonHeight) / 2 + parent.getLocation().y;
        int windowWidth = SizeOperate.FreeOfScreenSize.width;
        int windowHeight = SizeOperate.FreeOfScreenSize.height;

        int cacheWidth = windowWidth - sonWidth;
        int cacheHeight = windowHeight - sonHeight;

        if (resultX > cacheWidth) resultX = cacheWidth;
        if (resultY > cacheHeight) resultY = cacheHeight;
        if (resultX < 0) resultX = 0;

        return new Point(resultX, resultY);
    }

    public static Point ComponentCenter(Component component) {
        int resultWidth = component.getSize().width / 2;
        int resultHeight = component.getSize().height / 2;
        return new Point(resultWidth, resultHeight);
    }
}
