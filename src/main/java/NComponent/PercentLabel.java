package NComponent;

import Runner.Main;

import javax.swing.*;

public class PercentLabel extends JLabel {

    public void set(int percent) {
        String text = percent + "%";
        String lastText = getText();
        super.setText(text);
        if (text.length() != lastText.length() && Main.main.paintPicture != null && Main.main.paintPicture.AboveMainPanel != null) {
            Main.main.paintPicture.AboveMainPanel.revalidate();
        }

    }

    public PercentLabel() {
        super();
        super.setText(0 + "%");
        this.setVisible(true);
    }
}