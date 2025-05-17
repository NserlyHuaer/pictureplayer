package Tools.ImageManager;

import Loading.Init;
import Tools.String.RandomString;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;
import java.util.TreeMap;

public class PictureInformationStorageManagement implements Serializable {
    //Map(Key:图片路径 ; Value：List([0]:图片hashcode值 ; [1]:缓存的图片路径 ;[2]:缓存的图片hashcode值))
    private TreeMap<String, ArrayList<Object>> treeMap;

    //图片缓存存储位置
    private static final String saveDir = "cache/PictureCache/";
    //保存图片缓存类型
    private static final String saveType = "png";
    //保存图片缓存后缀
    private static final String FileSuffix = ".png";

    //主体
    public TreeMap<String, ArrayList<Object>> getTreeMap() {
        return (TreeMap<String, ArrayList<Object>>) treeMap.clone();
    }

    //初始化
    public PictureInformationStorageManagement() {
        treeMap = new TreeMap<>();
    }

    //初始化
    public PictureInformationStorageManagement(TreeMap<String, ArrayList<Object>> treeMap) {
        this.treeMap = (TreeMap<String, ArrayList<Object>>) treeMap.clone();
    }

    //获取特定图片缓存路径
    public String getCachedPicturePath(String OriginalPicturePath) {
        return getCachedPicturePath(OriginalPicturePath, GetImageInformation.getHashcode(new File(OriginalPicturePath)));
    }

    //获取特定图片缓存路径
    public String getCachedPicturePath(String OriginalPicturePath, String OriginalPictureHashCode) {
        if (treeMap == null) treeMap = new TreeMap<>();
        File OriginalPicture = new File(OriginalPicturePath);
        if (!OriginalPicture.exists()) return OriginalPicturePath;
        if (GetImageInformation.isOriginalJavaSupportedPictureType(OriginalPicturePath))
            return OriginalPicturePath;
        if (treeMap.containsKey(OriginalPicturePath)) {
            ArrayList<Object> pictureInformation = treeMap.get(OriginalPicturePath);
            File cachedPicture = new File(pictureInformation.get(1).toString());
            if (pictureInformation.getFirst().equals(OriginalPictureHashCode) && cachedPicture.exists() && hashcodeEquals(cachedPicture.getPath(), (String) pictureInformation.get(2))) {
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
        treeMap.put(OriginalPicturePath, pictureInformation);
        return savePath.getPath();
    }

    //移除并删除特定图片缓存
    public void removePictureCache(String OriginalPicturePath) {
        if (treeMap == null) return;
        String cachedPicture = (String) treeMap.get(OriginalPicturePath).get(1);
        File cachedPictureFile = new File(cachedPicture);
        if (cachedPictureFile.exists()) {
            cachedPictureFile.delete();
        }
        treeMap.remove(OriginalPicturePath);
    }

    //清除所有的图片缓存
    public void clear() {
        treeMap.clear();
        Init.clearDirectory(new File(saveDir));
    }

    //清除原图片不存在的图片缓存（返回清除的缓存图片数量）（建议在新进程中调用）
    public int optimize() {
        if (treeMap == null) return 0;
        if (treeMap.isEmpty()) {
            Init.clearDirectory(new File(saveDir));
            return 0;
        }

        int number = 0;
        ArrayList<Object> cache;
        File currentProcessingFile;
        File currentProcessingCachedFile;
        for (String OriginalPicturePath : treeMap.keySet()) {
            cache = treeMap.get(OriginalPicturePath);
            currentProcessingFile = new File(OriginalPicturePath);
            currentProcessingCachedFile = new File((String) cache.get(1));
            if (!currentProcessingFile.exists() || !currentProcessingCachedFile.exists() || GetImageInformation.isOriginalJavaSupportedPictureType(OriginalPicturePath) || !hashcodeEquals(OriginalPicturePath, (String) cache.getFirst()) || !hashcodeEquals(currentProcessingCachedFile.getPath(), (String) cache.get(2))) {
                treeMap.remove(OriginalPicturePath);
            }
        }

        ArrayList<String> CachedPicturePaths = new ArrayList<>();
        treeMap.values().forEach(e -> {
            CachedPicturePaths.add((String) e.get(1));
        });

        File[] AllCachedPicture = new File(saveDir).listFiles();
        if (AllCachedPicture != null) {
            for (File it : AllCachedPicture) {
                if (it.isDirectory()) Init.clearDirectory(it);
                if (!CachedPicturePaths.contains(it.getPath())) it.delete();
            }
        }

        return number;
    }

    //判断文件hashcode值是否与提供的hashcode值相等
    public static boolean hashcodeEquals(String filePath, String hashcode) {
        File file = new File(filePath);
        if (!file.exists()) return false;
        return Objects.equals(GetImageInformation.getHashcode(file), hashcode);
    }

    //获取图片Image对象
    public static BufferedImage getImage(String path) {
        BufferedImage bufferedImage;
        try {
            bufferedImage = ImageIO.read(new File(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return bufferedImage;
    }

    //写入图片
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
