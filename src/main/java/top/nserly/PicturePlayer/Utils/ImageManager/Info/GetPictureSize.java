package top.nserly.PicturePlayer.Utils.ImageManager.Info;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class GetPictureSize {
    public int width;
    public int height;

    public GetPictureSize(String path) throws IOException {
        BufferedImage image = ImageIO.read(new File(path));
        this.width = image.getWidth();
        this.height = image.getHeight();
    }

    public GetPictureSize(File path) throws IOException {
        BufferedImage image = ImageIO.read(path);
        this.width = image.getWidth();
        this.height = image.getHeight();
    }
}
