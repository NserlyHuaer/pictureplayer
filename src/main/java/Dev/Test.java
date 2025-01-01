package Dev;

import Tools.OSInformation.SystemMonitor;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Test {
    public static void main(String[] args) {
        SystemMonitor systemMonitor = new SystemMonitor();
        systemMonitor.GetInformation();
    }
}
