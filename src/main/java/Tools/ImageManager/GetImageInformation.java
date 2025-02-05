package Tools.ImageManager;

import Size.GetPictureSize;
import Size.GetSystemSize;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;

public class GetImageInformation {
    public static final boolean isHardwareAccelerated;

    static {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        GraphicsConfiguration gc = gd.getDefaultConfiguration();
        isHardwareAccelerated = gc.getBufferCapabilities().isPageFlipping();
    }

    //判断文件路径是否正确、是否为文件（非文件夹）
    public static boolean isRightPath(String path) {
        if (path.startsWith("\"") && path.startsWith("\"")) {
            path = path.substring(1, path.length() - 1);
        }
        return new File(path).isFile() && new File(path).exists();
    }

    //算法实现：获取文件是否为受Java支持的图片格式
    public static boolean isImageFile(File file) {
        if (file == null || !file.exists() || file.isDirectory()) {
            return false;
        }
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            byte[] buffer = new byte[8];
            fileInputStream.read(buffer);
            String hexString = bytesToHex(buffer);
            // 常见图片格式的文件头十六进制标识
            if (hexString.startsWith("FFD8FF")) {
                // JPEG格式
                return true;
            } else if (hexString.startsWith("89504E47")) {
                // PNG格式
                return true;
            } else if (hexString.startsWith("47494638")) {
                // GIF格式
                return true;
            } else if (hexString.startsWith("49492A00")) {
                // TIFF格式
                return true;
            } else if (hexString.startsWith("424D")) {
                // BMP格式
                return true;
            }
            return false;
        } catch (IOException e) {
            System.out.println("Error: " + e);
            return false;
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    //算法实现：将Image转换成VolatileImage
    public static VolatileImage convert(BufferedImage source) {
        // 获取当前图形环境配置
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        GraphicsConfiguration gc = gd.getDefaultConfiguration();

        // 创建兼容的 VolatileImage（尺寸与 BufferedImage 一致）
        VolatileImage volatileImage = gc.createCompatibleVolatileImage(
                source.getWidth(),
                source.getHeight(),
                VolatileImage.TRANSLUCENT // 根据需求选择透明度模式
        );

        // 验证 VolatileImage 有效性
        if (volatileImage.validate(gc) == VolatileImage.IMAGE_INCOMPATIBLE) {
            volatileImage = gc.createCompatibleVolatileImage(
                    source.getWidth(),
                    source.getHeight(),
                    VolatileImage.TRANSLUCENT
            );
        }

        // 绘制 BufferedImage 到 VolatileImage
        Graphics2D g = volatileImage.createGraphics();
        g.drawImage(source, 0, 0, null);
        g.dispose();

        return volatileImage;
    }

    //算法实现：获取图片大小
    public static Dimension getImageSize(File file) throws IOException {
        if (file == null) return null;
        Image image = ImageIO.read(file);
        if (image == null) return null;
        return new Dimension(image.getWidth(null), image.getHeight(null));
    }

    //算法实现：获取图片hashcode值
    public static String getHashcode(File file) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                md.update(buffer, 0, bytesRead);
            }
            fis.close();
            byte[] hashBytes = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            System.out.println("Error:" + e.getMessage());
        }
        return null;
    }

    //算法实现：获取图片格式
    public static String getPictureType(File file) throws IOException {
        try {
            ImageInputStream iis = ImageIO.createImageInputStream(file);
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (readers.hasNext()) {
                ImageReader reader = readers.next();
                reader.setInput(iis);
                return reader.getFormatName();
            }
        } catch (IOException e) {
            System.out.println("Error:" + e);
        }
        return null;
    }

    // 获取位深度的方法
    public static int getBitDepth(String imagePath) throws IOException {
        File file = new File(imagePath);
        BufferedImage image = ImageIO.read(file);
        return image.getColorModel().getPixelSize();
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
            if (GetImageInformation.isImageFile(file)) arrayList.add(file.getPath());
        }
        return arrayList;
    }
}
