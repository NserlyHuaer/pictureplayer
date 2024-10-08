package Dev;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import Tools.ImageManager.GetImageInformation;

public class AutoCompleteComboBox extends JComboBox<String> {
    private JTextField editor;
    private final HashSet<String> set = new HashSet<>();

    public AutoCompleteComboBox() {
        String[] array = ImageIO.getReaderFormatNames();
        set.addAll(List.of(array));
        setEditable(true);
        editor = (JTextField) getEditor().getEditorComponent();
        Document document = editor.getDocument();
        document.addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updatePopup();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updatePopup();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updatePopup();
            }

            private void updatePopup() {
                {
                    String filter = editor.getText();
                    if (!filter.isEmpty() && GetImageInformation.isRightPath(filter)) {
                        File[] files = new File(filter).listFiles();

                        List<String> filteredOptions = new ArrayList<>();

                        for (File i : files) {
                            String cache = i.getPath();
                            cache = cache.substring(cache.lastIndexOf("."));
                            if (set.contains(cache)) filteredOptions.add(cache);
                        }
                        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>((Vector) filteredOptions);
                        setModel(model);
                        setPopupVisible(!filteredOptions.isEmpty());
                    }
                }
            }
        });
    }

    public String getText() {
        return null;
    }

    public void setText(String a) {

    }
}
