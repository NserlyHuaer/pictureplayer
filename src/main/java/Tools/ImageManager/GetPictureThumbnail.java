package Tools.ImageManager;

import java.awt.*;
import java.awt.image.BufferedImage;

public class GetPictureThumbnail {
    private final int width;
    private final int height;

    public GetPictureThumbnail(int ThumbnailWidth, int ThumbnailHeight) {
        if (ThumbnailWidth <= 0 || ThumbnailHeight <= 0) {
            throw new IllegalArgumentException();
        }
        this.width = ThumbnailWidth;
        this.height = ThumbnailHeight;
    }

    public BufferedImage getImage(BufferedImage src) {
        Image img = src.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage res = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = res.createGraphics();
        g2d.drawImage(img, 0, 0, null);
        g2d.dispose();
        return res;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
