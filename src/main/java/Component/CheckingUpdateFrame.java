package Component;

import Tools.Component.WindowLocation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CheckingUpdateFrame extends JFrame {
    private JLabel statusLabel;
    private JButton confirmButton;

    public CheckingUpdateFrame(Window window) {
        super("更新检测");
        setLocationRelativeTo(window);
        setLayout(new FlowLayout());
        statusLabel = new JLabel("正在检测更新...");
        add(statusLabel);
        confirmButton = new JButton("确定");
        confirmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
        setSize(300, 120);
        setLocation(WindowLocation.ComponentCenter(window, getWidth(), getHeight()));
        setVisible(true);
        revalidate();
        repaint();
    }

    //如果是最新版本
    public void upToDate() {
        java.awt.EventQueue.invokeLater(() -> {
            statusLabel.setText("您已经是最新版本。");
            add(confirmButton);
        });
    }

    //发生意外
    public void error() {
        java.awt.EventQueue.invokeLater(() -> {
            statusLabel.setText("更新出现意外");
            add(confirmButton);
        });
    }
}