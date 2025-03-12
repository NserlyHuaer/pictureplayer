package NComponent;

import Loading.Bundle;
import Runner.Main;
import Tools.Component.WindowLocation;
import Tools.ProxyServer.Handle;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Method;
import java.util.*;

public class ProxyServerChooser extends JDialog {
    public Handle handle;
    private AddProxyServer addProxyServer;
    private JPanel contentPane;
    private JButton ChooseThisProxyServerButton;
    private JButton AddProxyServerButton;
    private JButton EditProxyServerButton;
    private JButton DeleteProxyServerButton;
    private JButton CancelProxyServerButton;
    private JButton RefreshProxyServerButton;
    private JTable ProxyServerTable;
    // 表格的数据模型
    DefaultTableModel tableModel = new DefaultTableModel();
    //表格的列
    public static final String[] columnNames = {Bundle.getMessage("ProxyServer_TableFirst"), Bundle.getMessage("ProxyServer_TableSecond")};

    private HashMap<String, JPanel> ProxyServer = new HashMap<>();

    public ProxyServerChooser() {
        $$$setupUI$$$();
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(ChooseThisProxyServerButton);
        handle = new Handle("data\\ProxyServerMenu.pxs");
        addProxyServer = new AddProxyServer(this);
        refresh();

        ChooseThisProxyServerButton.addActionListener(e -> {
            choice();
        });

        AddProxyServerButton.addActionListener(e -> {
            add();
        });

        EditProxyServerButton.addActionListener(e -> {
            //获取被选中的行号
            int row = ProxyServerTable.getSelectedRow();
            edit(row);
        });

        DeleteProxyServerButton.addActionListener(e -> {
            //获取被选中的行号
            int[] rows = ProxyServerTable.getSelectedRows();
            if (rows.length > 0) {
                for (int i = 0; i < rows.length; i++) {
                    delete(rows[i]);
                }

            }
        });

        CancelProxyServerButton.addActionListener(e -> {
            cancel();
        });

        RefreshProxyServerButton.addActionListener(e -> {
            refresh();
        });

        // 点击 X 时调用 onCancel()
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                cancel();
            }
        });

        // 遇到 ESCAPE 时调用 onCancel()
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void choice() {
        //获取被选中的行号
        int row = ProxyServerTable.getSelectedRow();
        if (row != -1) {
            String str = (String) tableModel.getValueAt(row, 0);
            if (str != null && !str.trim().isEmpty()) {
                Main.main.setProxyServerOfInit(handle.getProxyServerAddress(str));
                dispose();
            }
        }

    }


    private void cancel() {
        dispose();
    }

    public void refresh() {
        handle.refresh();
        tableModel.setRowCount(0);
        handle.getAllProxyServerNames().forEach(this::addElement);
    }

    private void addElement(String ProxyServerName) {
        tableModel.addRow(new String[]{ProxyServerName, handle.getProxyServerAddress(ProxyServerName)});
    }

    private void addElement(String ProxyServerName, String ProxyServerAddress) {
        tableModel.addRow(new String[]{ProxyServerName, ProxyServerAddress});
    }

    private void add() {
        addProxyServer.pack();
        addProxyServer.setVisible(true, -1);
    }

    private void edit(int index) {
        if (index == -1) return;
        String ProxyServerName = (String) tableModel.getValueAt(index, 0);
        addProxyServer.ProxyServerNameTextField.setText(ProxyServerName);
        addProxyServer.ProxyServerAddressTextField.setText(handle.getProxyServerAddress(ProxyServerName));
        addProxyServer.pack();
        addProxyServer.setVisible(true, index);
    }

    /**
     * 添加代理服务器列表
     *
     * @param ProxyServerName    服务器名称
     * @param ProxyServerAddress 服务器地址
     * @param index              表格中的行号
     */
    public void addNewProxyServer(String ProxyServerName, String ProxyServerAddress, int index) {
        if (ProxyServerAddress.isBlank()) return;
        boolean isExist = handle.containsProxyServerName(ProxyServerName);
        ProxyServerName = ProxyServerName.isBlank() ? ProxyServerAddress : ProxyServerName;
        handle.add(ProxyServerName, ProxyServerAddress);
        if (index < 0 && !isExist)
            addElement(ProxyServerName, ProxyServerAddress);
        else {
            tableModel.setValueAt(ProxyServerName, index, 0);
            tableModel.setValueAt(ProxyServerAddress, index, 1);
        }

        handle.save();
        addProxyServer.clear();
    }

    private void delete(int index) {
        handle.delete((String) tableModel.getValueAt(index, 0));
        tableModel.removeRow(index);
        handle.save();
    }

    public void setVisible(boolean visible) {
        refresh();
        setLocation(WindowLocation.ComponentCenter(Main.main, getWidth(), getHeight()));
        super.setVisible(visible);
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));
        contentPane.setToolTipText("");
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(2, 4, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        ChooseThisProxyServerButton = new JButton();
        ChooseThisProxyServerButton.setHorizontalTextPosition(0);
        ChooseThisProxyServerButton.setRequestFocusEnabled(false);
        this.$$$loadButtonText$$$(ChooseThisProxyServerButton, this.$$$getMessageFromBundle$$$("messages", "ChooseThisProxyServerButton"));
        panel2.add(ChooseThisProxyServerButton, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        AddProxyServerButton = new JButton();
        AddProxyServerButton.setHorizontalTextPosition(0);
        AddProxyServerButton.setRequestFocusEnabled(false);
        this.$$$loadButtonText$$$(AddProxyServerButton, this.$$$getMessageFromBundle$$$("messages", "AddProxyServerButton"));
        panel2.add(AddProxyServerButton, new GridConstraints(0, 2, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        EditProxyServerButton = new JButton();
        EditProxyServerButton.setRequestFocusEnabled(false);
        this.$$$loadButtonText$$$(EditProxyServerButton, this.$$$getMessageFromBundle$$$("messages", "EditProxyServerButton"));
        panel2.add(EditProxyServerButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        DeleteProxyServerButton = new JButton();
        DeleteProxyServerButton.setRequestFocusEnabled(false);
        this.$$$loadButtonText$$$(DeleteProxyServerButton, this.$$$getMessageFromBundle$$$("messages", "DeleteProxyServerButton"));
        panel2.add(DeleteProxyServerButton, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        CancelProxyServerButton = new JButton();
        CancelProxyServerButton.setRequestFocusEnabled(false);
        this.$$$loadButtonText$$$(CancelProxyServerButton, this.$$$getMessageFromBundle$$$("messages", "CancelProxyServerButton"));
        panel2.add(CancelProxyServerButton, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        RefreshProxyServerButton = new JButton();
        RefreshProxyServerButton.setRequestFocusEnabled(false);
        this.$$$loadButtonText$$$(RefreshProxyServerButton, this.$$$getMessageFromBundle$$$("messages", "RefreshProxyServerButton"));
        panel2.add(RefreshProxyServerButton, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        scrollPane1.setToolTipText("");
        panel3.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        scrollPane1.setViewportView(ProxyServerTable);
    }

    private static Method $$$cachedGetBundleMethod$$$ = null;

    private String $$$getMessageFromBundle$$$(String path, String key) {
        ResourceBundle bundle;
        try {
            Class<?> thisClass = this.getClass();
            if ($$$cachedGetBundleMethod$$$ == null) {
                Class<?> dynamicBundleClass = thisClass.getClassLoader().loadClass("com.intellij.DynamicBundle");
                $$$cachedGetBundleMethod$$$ = dynamicBundleClass.getMethod("getBundle", String.class, Class.class);
            }
            bundle = (ResourceBundle) $$$cachedGetBundleMethod$$$.invoke(null, path, thisClass);
        } catch (Exception e) {
            bundle = ResourceBundle.getBundle(path);
        }
        return bundle.getString(key);
    }

    /**
     * @noinspection ALL
     */
    private void $$$loadButtonText$$$(AbstractButton component, String text) {
        StringBuffer result = new StringBuffer();
        boolean haveMnemonic = false;
        char mnemonic = '\0';
        int mnemonicIndex = -1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '&') {
                i++;
                if (i == text.length()) break;
                if (!haveMnemonic && text.charAt(i) != '&') {
                    haveMnemonic = true;
                    mnemonic = text.charAt(i);
                    mnemonicIndex = result.length();
                }
            }
            result.append(text.charAt(i));
        }
        component.setText(result.toString());
        if (haveMnemonic) {
            component.setMnemonic(mnemonic);
            component.setDisplayedMnemonicIndex(mnemonicIndex);
        }
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

    private void createUIComponents() {
        tableModel = new DefaultTableModel(new String[0][], columnNames);
        ProxyServerTable = new JTable(tableModel);
        RowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        ProxyServerTable.setRowSorter(sorter);
    }
}
