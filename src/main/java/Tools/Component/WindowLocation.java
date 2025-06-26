package Tools.Component;

import NComponent.PaintPicturePanel;
import Tools.PaintPictureManage;

import java.awt.*;

public class WindowLocation {
    public static Point ComponentCenter(Window parent, int sonWidth, int sonHeight) {
        int windowWidth = PaintPictureManage.FreeOfScreenSize.width;
        int windowHeight = PaintPictureManage.FreeOfScreenSize.height;
        if (parent == null)
            return DesktopCenter(sonWidth, sonHeight);
        int parentWidth = parent.getSize().width;
        int parentHeight = parent.getSize().height;
        int resultX = (parentWidth - sonWidth) / 2 + parent.getLocation().x;
        int resultY = (parentHeight - sonHeight) / 2 + parent.getLocation().y;

        int cacheWidth = windowWidth - sonWidth;
        int cacheHeight = windowHeight - sonHeight;

        if (resultX > cacheWidth) resultX = cacheWidth;
        if (resultY > cacheHeight) resultY = cacheHeight;
        if (resultX < 0) resultX = 0;

        return new Point(resultX, resultY);
    }

    public static Dimension ComponentCenter(Component component) {
        int resultWidth = component.getSize().width / 2;
        int resultHeight = component.getSize().height / 2;
        return new Dimension(resultWidth, resultHeight);
    }

    public static Point DesktopCenter(int Width, int Height) {
        int resultX = (PaintPictureManage.FreeOfScreenSize.width - Width) / 2;
        int resultY = (PaintPictureManage.FreeOfScreenSize.height - Height) / 2;
        return new Point(resultX, resultY);
    }
}
