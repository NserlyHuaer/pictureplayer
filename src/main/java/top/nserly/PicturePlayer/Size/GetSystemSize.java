package top.nserly.PicturePlayer.Size;

import java.awt.*;

public class GetSystemSize {
    public static final int width;
    public static final int height;

    static {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension dimension = toolkit.getScreenSize();
        width = dimension.width;
        height = dimension.height;
    }

    private GetSystemSize() {
    }

}
