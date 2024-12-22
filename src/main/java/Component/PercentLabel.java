package Component;

import Runner.Main;

import java.awt.*;

public class PercentLabel extends Label {

    public void set(int percent) {
        String text = percent + "%";
        String lastText = getText();
        super.setText(text);
        if (text.length() != lastText.length() && Main.main.paintPicture != null) {
            Main.main.paintPicture.On.revalidate();
        }

    }

    public PercentLabel() {
        super();
        super.setText(0 + "%");
        this.setVisible(true);
    }
}
