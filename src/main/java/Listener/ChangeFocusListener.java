package Listener;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ChangeFocusListener extends MouseAdapter {
    private final Container container;

    public ChangeFocusListener(Container container) {
        this.container = container;
    }

    public void mouseReleased(MouseEvent e){
        container.requestFocus();
    }
}
