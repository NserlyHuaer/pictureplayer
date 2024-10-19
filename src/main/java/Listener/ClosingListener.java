package Listener;

import Runner.Main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class ClosingListener implements KeyListener {
    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            Main.close();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
