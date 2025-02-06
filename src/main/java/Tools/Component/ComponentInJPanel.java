package Tools.Component;

import javax.swing.*;
import java.awt.*;

public class ComponentInJPanel {
    private ComponentInJPanel() {

    }

    public static boolean isComponentInJPanel(JPanel panel, Component comp) {
        if (panel == null || comp == null) return false;
        Component[] components = panel.getComponents();
        for (Component component : components) {
            if (comp == component) return true;
        }
        return false;
    }
}
