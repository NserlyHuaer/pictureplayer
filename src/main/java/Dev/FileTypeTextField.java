package Dev;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class FileTypeTextField extends JTextField {
    private JPopupMenu popupMenu;

    //设置显示个数
    public void setVisibleRowCount(int count) {
        suggestionList.setVisibleRowCount(count);
    }

    //设置下拉菜单选项
    public void setListData(String[] data) {
        listModel.clear();
        for (String item : data) {
            listModel.addElement(item);
        }
    }


    private JList<String> suggestionList;
    private DefaultListModel<String> listModel;

    public FileTypeTextField() {
        listModel = new DefaultListModel<>();
        suggestionList = new JList<>(listModel);
        suggestionList.setVisibleRowCount(5);
        suggestionList.setBorder(BorderFactory.createEmptyBorder(0, 4, 4, 4));
        suggestionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        suggestionList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = suggestionList.getSelectedValue();
                if (selected != null) {
                    setSelected(selected);
                }
            }

//        //添加键盘监听器
//            addKeyListener(new KeyAdapter() {
//                @Override
//                public void keyPressed(KeyEvent e) {
//                    if (e.getKeyCode() == KeyEvent.VK_UP) {
//                        suggestionList.setSelectedIndex(suggestionList.getSelectedIndex() - 1);
//                    } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
//                        suggestionList.setSelectedIndex(suggestionList.getSelectedIndex() + 1);
//                    } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
//                        if (suggestionList.isSelectionEmpty()) {
//                            return;
//                        }
//                        setText(suggestionList.getSelectedValue());
//                        popupMenu.setVisible(false);
//                    }
//                }
//            });


        });


        //初始化下拉菜单和列表
        popupMenu = new JPopupMenu();

        listModel = new DefaultListModel<>();
        //设置列表高度
        suggestionList.setFixedCellHeight(13);
        suggestionList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                requestFocusInWindow();
                int index = suggestionList.locationToIndex(e.getPoint());
                if (index != -1) {
                    setText(suggestionList.getModel().getElementAt(index));
                }
                popupMenu.setVisible(false);
            }
        });
        popupMenu.add(new JScrollPane(suggestionList));
        //添加文本字段的鼠标监听器
//        addMouseListener(new MouseAdapter() {
//            @Override
//            public void mouseClicked(MouseEvent e) {
//                if (!popupMenu.isVisible()) {
//                    popupMenu.show(FileTypeTextField.this, 0, getHeight());
//                }
//            }
//        });

        getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateSuggestions();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateSuggestions();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateSuggestions();
            }

            private void updateSuggestions() {
                popupMenu.removeAll();
                String text = getText();
                listModel.removeAllElements();
                File f = new File(text);
                File parent = null;
                File[] sonDirectoryFile = null;
                File[] sonFile = null;
                if (f.isDirectory()) {
                    sonDirectoryFile = f.listFiles();
                } else if (f.getParent() != null) {
                    parent = new File(f.getParent());
                    sonDirectoryFile = parent.listFiles();
                } else return;
                String abs = f.getAbsolutePath();
                if (sonDirectoryFile == null) return;
                for (File file : sonDirectoryFile) {
                    if (file.getPath().toLowerCase().startsWith(f.getPath().toLowerCase())) {
//                        listModel.addElement(file.getPath());
                        JMenuItem jMenuItem = null;
                        if (file.isDirectory())
                            jMenuItem = new JMenuItem(file.getPath() + "/");
                        else
                            jMenuItem = new JMenuItem(file.getPath());
                        JMenuItem finalJMenuItem = jMenuItem;
                        jMenuItem.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                setText(finalJMenuItem.getText());
                            }
                        });
                        popupMenu.add(jMenuItem);
                    }
                }
                suggestionList.setVisible(true);

//                popupMenu.setVisible(true);
                showing();
                revalidate();

                repaint();
            }
        });
//        addKeyListener(new KeyAdapter() {
//            @Override
//            public void keyPressed(KeyEvent e) {
//                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
//                    popupMenu.requestFocusInWindow();
//                    suggestionList.setSelectedIndex(0);
//
//                } else if (e.getKeyCode() == KeyEvent.VK_UP) {
//                    if (suggestionList.isFocusOwner()) {
//                        int index = suggestionList.getSelectedIndex();
//                        if (index > 0) {
//                            suggestionList.setSelectedIndex(index - 1);
//                        }
//                    }
//                }
//            }
//        });

    }

    private void setSelected(String selected) {
        setText(selected);
        suggestionList.setVisible(false);
        requestFocusInWindow();
    }

    public Dimension getPreferredSise() {
        Dimension size = super.getPreferredSize();
        if (suggestionList.isVisible()) {
            size.height += suggestionList.getPreferredSize().height;

        }
        return size;
    }

    public void doLayout() {
        super.doLayout();
        if (suggestionList.isVisible()) {
            suggestionList.setBounds(0, getHeight() - suggestionList.getPreferredSize().height, getWidth(), suggestionList.getPreferredSize().height);
        }
    }

    public void showing() {
        if (popupMenu.getComponentCount() == 0) return;
        //计算弹出菜单的位置
        Dimension JTextFiledSize = getSize();
        Point JTextFiledLocation = getLocationOnScreen();
        Rectangle screenBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        int popupX = (int) JTextFiledLocation.getX();
        int popupY = (int) (JTextFiledLocation.getY() + JTextFiledSize.getHeight());
        //检查是否超出屏幕边界
        if (popupY + popupMenu.getHeight() > screenBounds.getMaxY()) {
            popupY = (int) JTextFiledLocation.getY() - popupMenu.getHeight();
        }
        popupMenu.show(this, popupX - JTextFiledLocation.x, popupY - JTextFiledLocation.y);
    }
}
