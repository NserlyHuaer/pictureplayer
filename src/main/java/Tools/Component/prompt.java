package Tools.Component;

import javax.swing.*;
import java.awt.*;

/**
 * 若显示时间为-1代表永久显示
 */
public class prompt extends JFrame {
    private JLabel content_label;
    private long VisibleTime = 1500;
    private JFrame parent_frame;

    public static void main(String[] args) {
        prompt prompt = new prompt("tye", null);
        prompt.setVisible(true);
        prompt.setBounds(100, 100, 200, 200);
    }

    public prompt(String content, JFrame parent_frame) {
        this.parent_frame = parent_frame;
        content_label = new JLabel(content);
        setUndecorated(true);
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
    }

    public prompt(JFrame parent_frame) {
        this.parent_frame = parent_frame;
        content_label = new JLabel();
        setUndecorated(true);
    }

    public void setVisibleTime(long ms) {
        VisibleTime = ms;
    }

    public long getVisibleTime() {
        return VisibleTime;
    }

    public void setParent_frame(JFrame parent_frame) {
        this.parent_frame = parent_frame;
    }

    public String get() {
        return content_label.getText();
    }

    public void set(String content) {
        content_label.setText(content);
    }

    public void setLabelFont(Font font) {
        content_label.setFont(font);
    }

    public void setLabelForeground(Color c) {
        content_label.setForeground(c);
    }

    public void setLabelBackground(Color c) {
        content_label.setBackground(c);
    }
}
