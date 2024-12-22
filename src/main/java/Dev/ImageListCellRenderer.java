package Dev;

import Tools.File.ImageThumbnailManage.Center;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;

class ImageListCellRenderer implements ListCellRenderer<String> {
    private final List<File> files;
    private final int maxHeight;

    public ImageListCellRenderer(List<File> files, int maxHeight) {
        this.files = files;
        this.maxHeight = maxHeight;
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {
        String imagePath = Center.fileManage.getFileStoreInfo(new File(value), maxHeight);
        ImageIcon icon = new ImageIcon(imagePath);
        JLabel label = new JLabel(icon);
        label.setText(value);
        if (isSelected) {
            label.setBackground(list.getSelectionBackground());
            label.setForeground(list.getSelectionForeground());
        } else {
            label.setBackground(list.getBackground());
            label.setForeground(list.getForeground());
        }
        return label;
    }
}