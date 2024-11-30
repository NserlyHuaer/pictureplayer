package Component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CustomInputDialog {
    public static String showCustomInputDialog(JFrame owner, String title, String message) {
        final JDialog dialog = new JDialog(owner, title, true);

        JPanel panel = new JPanel(new BorderLayout());

        JLabel label = new JLabel(message);
        panel.add(label, BorderLayout.NORTH);

        final JTextField textField = new JTextField();
        panel.add(textField, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });
        buttonPanel.add(okButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textField.setText("");
                dialog.dispose();
            }
        });
        buttonPanel.add(cancelButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);

        if (textField.getText().isEmpty()) {
            return null;
        } else {
            return textField.getText();
        }
    }

}
