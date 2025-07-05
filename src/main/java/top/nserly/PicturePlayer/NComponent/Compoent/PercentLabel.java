package top.nserly.PicturePlayer.NComponent.Compoent;

import top.nserly.GUIStarter;

import javax.swing.*;

public class PercentLabel extends JLabel {

    public void set(int percent) {
        String text = percent + "%";
        String lastText = getText();
        super.setText(text);
        if (text.length() != lastText.length() && GUIStarter.main.paintPicture != null && GUIStarter.main.paintPicture.AboveMainPanel != null) {
            GUIStarter.main.paintPicture.AboveMainPanel.revalidate();
        }

    }

    public PercentLabel() {
        super();
        super.setText(0 + "%");
        this.setVisible(true);
    }
}