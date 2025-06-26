package Tools.PictureDraw;

import java.awt.*;

public class PictureShowerHelp {
    // 计算等比例缩放后的高度
    public static double calculateProportionalHeight(double width, int originalHeight, int originalWidth) {
        return (width * originalHeight) / originalWidth;
    }

    // 应用旋转坐标变换
    public static Point applyRotation(double mouseX, double mouseY, byte rotationDegrees) {
        double rotatedX = mouseX;
        double rotatedY = mouseY;

        switch (rotationDegrees) {
            case 1:
                rotatedX = mouseY;
                rotatedY = -mouseX;
                break;
            case 2:
                rotatedX = -mouseX;
                rotatedY = -mouseY;
                break;
            case 3:
                rotatedX = -mouseY;
                rotatedY = mouseX;
                break;
        }
        return new Point((int) rotatedX, (int) rotatedY);
    }

    // 计算窗口尺寸变化比率
    public static double calculateWindowChangeRatio(Dimension lastWindow, Dimension newWindow) {
        if (newWindow.width == 0 || newWindow.height == 0) return 1;
        double widthRatio = lastWindow.getWidth() / newWindow.getWidth();
        double heightRatio = lastWindow.getHeight() / newWindow.getHeight();
        return (widthRatio + heightRatio) / 2;
    }
}
