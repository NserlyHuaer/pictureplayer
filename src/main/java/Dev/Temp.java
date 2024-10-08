package Dev;

import Tools.ImageManager.GetImageInformation;
import Tools.Simplified;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Temp {
    //文件最后读取时间
    private Date fileLastReaderDate;
    //文件路径
    private String FilePath;
    //比例列表
    private List list;

    public Temp(String FilePath) {
        fileLastReaderDate = new Date();
        this.FilePath = FilePath;
        list = new ArrayList();
    }

    public void ComputingRatio() {
        if (!GetImageInformation.isImageFile(new File(FilePath))) return;
        Image image = new ImageIcon(FilePath).getImage();
        long[] longs = Simplified.Computing(image.getWidth(null), image.getHeight(null));
        list.add(longs[1]);
        list.add(longs[2]);
    }

}
