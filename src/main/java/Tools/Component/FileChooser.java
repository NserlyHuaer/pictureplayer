package Tools.Component;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.io.File;

public class FileChooser {
    private static final boolean isDesktopSupported;

    static {
        isDesktopSupported = Desktop.isDesktopSupported();
        if (!isDesktopSupported) {
            System.out.println("Windows fileChooser is not supported");
        }
    }

    public static File show() {
        if (isDesktopSupported) {
            return showWindowsFileChooser();
        } else {
            JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
            int returnValue = fileChooser.showOpenDialog(null);
            File chooseFile = fileChooser.getSelectedFile();
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                return chooseFile;
            }
        }
        return null;
    }


    private static File showWindowsFileChooser() {
        //获取windows平台的Desktop实例
        Desktop desktop = Desktop.getDesktop();
        File selectedFile = null;
        if (desktop.isSupported(Desktop.Action.OPEN)) {
            FileDialog fileDialog = new FileDialog((Frame) null, "Open File");
            fileDialog.setMode(FileDialog.LOAD);
            fileDialog.setVisible(true);

            String dir = fileDialog.getDirectory();
            String filename = fileDialog.getFile();
            if (dir != null && filename != null) {
                selectedFile = new File(dir, filename);
            }
        }
        return selectedFile;
    }
}