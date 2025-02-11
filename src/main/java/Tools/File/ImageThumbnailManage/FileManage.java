package Tools.File.ImageThumbnailManage;

import Loading.Init;
import Runner.Main;
import Tools.ImageManager.GetImageInformation;
import Tools.ImageManager.GetPictureThumbnail;
import Tools.String.RandomString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.TreeMap;

public class FileManage implements Serializable {
    @Serial
    private static final long serialVersionUID = 22562L;
    //<原本图片地址（String），{0.缩略图的地址（String），1.原图的hashcode值（String），2.缩略图的hashcode值（String）}>
    private final TreeMap<String, ArrayList> fileStoreInfo = new TreeMap<>();
    private static GetPictureThumbnail getPictureThumbnail = new GetPictureThumbnail(10, 10);
    private static final Logger logger = LoggerFactory.getLogger(FileManage.class);

    public FileManage() {

    }

    //获取图片对应存放的缩略图地址
    public String getFileStoreInfo(File file) {
        if (fileStoreInfo.containsKey(file.getPath()) && new File(String.valueOf(fileStoreInfo.get(file.getPath()).getFirst())).exists() && GetImageInformation.getHashcode(file).equals(fileStoreInfo.get(file.getPath()).get(1)) && GetImageInformation.getHashcode(new File(String.valueOf(fileStoreInfo.get(file.getPath()).getFirst()))).equals(fileStoreInfo.get(file.getPath()).get(2))) {
            return fileStoreInfo.get(file.getPath()).getFirst().toString();
        }
        fileStoreInfo.remove(file.getPath());
        ArrayList arrayList = new ArrayList();
        String ThumbnailPath = null;
        File file1 = null;
        try {
            BufferedImage image = getPictureThumbnail.getImage(ImageIO.read(file));
            ThumbnailPath = "cache/thum/" + RandomString.getRandomString(15);
            arrayList.add(ThumbnailPath);
            file1 = new File(ThumbnailPath);
            file1.createNewFile();
            ImageIO.write(image, "png", file1);
        } catch (IOException e) {
            logger.error(e.getMessage());
            return null;
        }
        arrayList.add(GetImageInformation.getHashcode(file));
        arrayList.add(GetImageInformation.getHashcode(file1));
        fileStoreInfo.put(file.getPath(), arrayList);
        return ThumbnailPath;
    }

    public String getFileStoreInfo(File file, int maxHeight) {
        if (maxHeight == 0) return getFileStoreInfo(file);
        Image image = null;
        try {
            image = ImageIO.read(file);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        int height = image.getHeight(null), width = image.getWidth(null);
        int finalWidth = maxHeight * width / height;
        getPictureThumbnail = new GetPictureThumbnail(finalWidth, height);
        String result = getFileStoreInfo(file);
        getPictureThumbnail = new GetPictureThumbnail(10, 10);
        return result;
    }

    //清理特定的图片的缩略图（不删除原图片）
    public void clearFileStoreInfo(File file) {
        if (fileStoreInfo.containsKey(file.getPath()) && new File(String.valueOf(fileStoreInfo.get(file.getPath()).getFirst())).exists() && GetImageInformation.getHashcode(file).equals(fileStoreInfo.get(file.getPath()).get(1)) && GetImageInformation.getHashcode(new File(String.valueOf(fileStoreInfo.get(file.getPath()).getFirst()))).equals(fileStoreInfo.get(file.getPath()).get(2))) {
            File file1 = new File(fileStoreInfo.get(file.getPath()).getFirst().toString());
            file1.delete();
            fileStoreInfo.remove(file.getPath());
        }
    }

    //清空所有缩略图
    public void clearAllFileStoreInfo() {
        fileStoreInfo.clear();
        Init.clearDirectory(new File("./cache/"));
    }
}
