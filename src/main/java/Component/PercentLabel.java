package Component;

import java.awt.*;

public class PercentLabel extends Label {

    public void set(int percent) {
        super.setText(percent + "%");
    }

    public PercentLabel() {
        super();
        super.setText(0 + "%");
        this.setVisible(true);
    }
}
