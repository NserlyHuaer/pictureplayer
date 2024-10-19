package Component;

import Listener.ClosingListener;
import Runner.Main;

import javax.swing.*;

public class FileManagementFrame extends JDialog {
    private static final ClosingListener closingListener = new ClosingListener();

    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b) {
            addKeyListener(closingListener);
        }
    }

    public void dispose() {
        super.dispose();
        removeKeyListener(closingListener);
    }

}
