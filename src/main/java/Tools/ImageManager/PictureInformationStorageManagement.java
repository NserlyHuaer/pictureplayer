package Tools.ImageManager;

import Tools.String.RandomString;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.TreeMap;

public class PictureInformationStorageManagement implements Serializable {
    //Map(Key:图片路径 ; Value：List([0]:图片hashcode值 ; [1]:缓存的图片路径 ;[2]:缓存的图片hashcode值))
    private TreeMap<String, ArrayList<Object>> treeMap;

    private static final String saveDir = "cache/PictureCache/";
    private static final String saveType = "png";
    private static final String FileSuffix = ".png";

    public TreeMap<String, ArrayList<Object>> getTreeMap() {
        return (TreeMap<String, ArrayList<Object>>) treeMap.clone();
    }

    public PictureInformationStorageManagement() {
        treeMap = new TreeMap<>();
    }

    public PictureInformationStorageManagement(TreeMap<String, ArrayList<Object>> treeMap) {
        this.treeMap = treeMap;
    }

    public String getCachedPicturePath(String OriginalPicturePath) {
        return getCachedPicturePath(OriginalPicturePath, GetImageInformation.getHashcode(new File(OriginalPicturePath)));
    }

    public String getCachedPicturePath(String OriginalPicturePath, String OriginalPictureHashCode) {
        if (treeMap == null) treeMap = new TreeMap<>();
        File OriginalPicture = new File(OriginalPicturePath);
        if (!OriginalPicture.exists()) return OriginalPicturePath;
        if (treeMap.containsKey(OriginalPicturePath)) {
            ArrayList<Object> pictureInformation = treeMap.get(OriginalPicturePath);
            File cachedPicture = new File(pictureInformation.get(1).toString());
            if (pictureInformation.getFirst().equals(OriginalPictureHashCode) && cachedPicture.exists() && pictureInformation.get(2).equals(GetImageInformation.getHashcode(cachedPicture))) {
                return cachedPicture.getPath();
            }
            treeMap.remove(OriginalPicturePath);
        }
        ArrayList<Object> pictureInformation = new ArrayList<>();
        pictureInformation.add(OriginalPictureHashCode);
        File savePath = new File(saveDir + RandomString.getRandomString(10) + FileSuffix);
        pictureInformation.add(savePath.getPath());
        writeImage(getImage(OriginalPicturePath), savePath.getPath(), saveType);
        pictureInformation.add(GetImageInformation.getHashcode(savePath));
        treeMap.put(OriginalPicturePath,pictureInformation);
        return savePath.getPath();
    }

    public static BufferedImage getImage(String path) {
        BufferedImage bufferedImage;
        try {
            bufferedImage = ImageIO.read(new File(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return bufferedImage;
    }

    public static boolean writeImage(BufferedImage image, String path, String type) {
        File file = new File(path);
        try {
            ImageIO.write(image, type, file);
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}
