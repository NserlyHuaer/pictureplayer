package Component;

import Listener.ClosingListener;

import javax.swing.*;

public class FileManagementFrame extends JDialog {
    private static final ClosingListener closingListener = new ClosingListener();

    public FileManagementFrame() {
        addKeyListener(closingListener);
    }

}
