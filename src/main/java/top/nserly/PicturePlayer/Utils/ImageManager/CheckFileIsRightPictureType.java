package top.nserly.PicturePlayer.Utils.ImageManager;

import lombok.Getter;
import top.nserly.PicturePlayer.Utils.ImageManager.Info.GetImageInformation;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CheckFileIsRightPictureType {
    ArrayList<File> CheckFileList = new ArrayList<>();
    ArrayList<File> FinishedList = new ArrayList<>();
    ArrayList<File> UnfinishedList = new ArrayList<>();
    @Getter
    ArrayList<File> NotImageList = new ArrayList<>();
    @Getter
    ArrayList<File> ImageList = new ArrayList<>();

    public CheckFileIsRightPictureType(File... files) {
        add(files);
    }

    public CheckFileIsRightPictureType(List<File> files) {
        add(files);
    }

    public void add(File... files) {
        CheckFileList.addAll(Arrays.asList(files));
        UnfinishedList.addAll(Arrays.asList(files));
    }

    public void add(List<File> files) {
        CheckFileList.addAll(files);
        UnfinishedList.addAll(files);
    }

    public void statistics() {
        for (File file : UnfinishedList) {
            FinishedList.add(file);
            if (GetImageInformation.isImageFile(file)) {
                ImageList.add(file);
            } else {
                NotImageList.add(file);
            }
        }
        UnfinishedList.clear();
    }

    public int getImageCount() {
        return ImageList.size();
    }

    public int getNotImageCount() {
        return NotImageList.size();
    }

    public void clear() {
        NotImageList.clear();
        ImageList.clear();
        CheckFileList.clear();
        FinishedList.clear();
        UnfinishedList.clear();
    }

    public boolean isFinished() {
        return UnfinishedList.isEmpty();
    }

    public String FilePathToString(String separator, File... files) {
        StringBuffer sb = new StringBuffer();
        boolean isFist = true;
        for (File file : files) {
            if (!isFist) {
                sb.append(separator);
            }
            sb.append(file.getAbsolutePath());
            isFist = false;
        }
        return sb.toString();
    }

    public String FilePathToString(String separator, ArrayList<File> files) {
        StringBuffer sb = new StringBuffer();
        boolean isFist = true;
        for (File file : files) {
            if (!isFist) {
                sb.append(separator);
            }
            sb.append(file.getAbsolutePath());
            isFist = false;
        }
        return sb.toString();
    }
}
