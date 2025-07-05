package top.nserly.PicturePlayer.Utils.ImageManager;

import java.awt.*;

public class ImageRotationHelper {
    //获取未旋转状态下的坐标
    public static Point getOriginalCord(int rotatedX, int rotatedY, int angle, int width, int height) {
        int originalX = rotatedX;
        int originalY = rotatedY;
        switch (angle / 90 % 4) {
            case 1 -> {
                originalX = rotatedY;
                originalY = height - rotatedX;
            }
            case 2 -> {
                originalX = width - rotatedX;
                originalY = height - rotatedY;
            }
            case 3 -> {
                originalX = height - rotatedY;
                originalY = rotatedX;
            }
        }
        return new Point(originalX, originalY);
    }

    //获取旋转状态下的坐标
    public static Point getRotatedCord(int originalX, int originalY, int angle, int width, int height) {
        int newX = originalX;
        int newY = originalY;
        switch (angle / 90 % 4) {
            case 1 -> {
                newX = height - originalY;
                newY = originalX;
            }
            case 2 -> {
                newX = height - originalY;
                newY = height - originalY;
            }
            case 3 -> {
                newX = originalY;
                newY = height - originalX;
            }
        }
        return new Point(newX, newY);
    }

}
