package Component;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;

public class OpenImageChooser {
    public static File openImageWithChoice(Component component, List<File> options) {
        // 创建一个JList模型并添加打开选项
        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (File option : options) {
            listModel.addElement(option.getPath());
        }

        // 创建JList并设置模型
        JList<String> list = new JList<>(listModel);

        // 使用JOptionPane显示包含JList的对话框，不显示图标
        int choice = JOptionPane.showOptionDialog(component, new Object[]{new JScrollPane(list)}, "Open Image",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
        if (choice == JOptionPane.OK_OPTION) {
            String selectedValue = list.getSelectedValue();
            if (selectedValue != null) {
                return new File(selectedValue);
            }
        }
        return null;
    }
}
