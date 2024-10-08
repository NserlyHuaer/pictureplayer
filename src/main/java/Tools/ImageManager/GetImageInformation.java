package Tools.ImageManager;

import Size.GetPictureSize;
import Size.GetSystemSize;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class GetImageInformation {
    //判断文件路径是否正确、是否为文件（非文件夹）
    public static boolean isRightPath(String path) {
        if (path.startsWith("\"") && path.startsWith("\"")) {
            path = path.substring(1, path.length() - 1);
        }
        return new File(path).isFile() && new File(path).exists();
    }
    //算法实现：获取文件是否为受Java支持的图片格式
    public static boolean isImageFile(File file) {
        if (file == null) return false;
        try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(file)) {
            Iterator<ImageReader> imageReaderIterator = ImageIO.getImageReaders(imageInputStream);
            return imageReaderIterator.hasNext();
        } catch (IOException e) {
            return false;
        }
    }
    //算法实现：获取最佳大小、坐标
    public static Rectangle getBestSize(String path) {
        //如果字符串前缀与后缀包含"，则去除其中的"
        if (path.startsWith("\"") && path.startsWith("\"")) {
            path = path.substring(1, path.length() - 1);
        }
        //初始化宽度、高度
        int Width, Height, X, Y = 0;
        //创建GetPictureSize对象
        GetPictureSize getPictureSize = null;
        //抛出异常
        try {
            getPictureSize = new GetPictureSize(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //初始化变量
        int SystemWidth = GetSystemSize.width;
        int SystemHeight = GetSystemSize.height;
        int PictureWidth = getPictureSize.width;
        int PictureHeight = getPictureSize.height;
        //算法实现
        Width = PictureWidth > SystemWidth * 0.8 ? (int) (SystemWidth * 0.8) : PictureWidth;
        Height = PictureHeight > SystemHeight * 0.8 ? (int) (SystemHeight * 0.8) : PictureHeight;
        Height += 70;
        Width += 20;
        if (Height < SystemWidth * 0.3) Height = (int) (SystemWidth * 0.3);
        if (Width < SystemHeight * 0.5) Width = (int) (SystemHeight * 0.5);
        X = Math.abs((int) ((SystemWidth * 0.9 - PictureWidth) / 2));
        Y = Math.abs((int) ((SystemHeight * 0.9 - PictureHeight) / 2));
        //返回结果
        return new Rectangle(X, Y, Width, Height);
    }
    //获取当前图片路径下所有图片
    public static ArrayList<String> getCurrentPathOfPicture(String path) {
        File[] files = new File(new File(path).getParent()).listFiles();
        ArrayList<String> arrayList = new ArrayList<>();
        if (files != null) for (File file : files) {
            if (file.isFile() && GetImageInformation.isImageFile(file)) arrayList.add(file.getPath());
        }
        return arrayList;
    }
}
