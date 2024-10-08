package Component;


import Runner.Main;

import java.awt.*;

public class PercentLabel extends Label {
    public void set(int percent) {
        super.setText(percent + "%");
        if (Main.main != null)
            Main.main.setTitle(Main.titleStyle + "     Size:" + percent + "%");
    }

    public PercentLabel() {
        super();
        super.setText(0 + "%");
        this.setVisible(true);
    }
}
