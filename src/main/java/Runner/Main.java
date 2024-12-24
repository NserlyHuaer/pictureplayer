package Runner;

import Listener.ChangeFocusListener;
import Loading.Init;
import Tools.Component.WindowLocation;
import Tools.File.ImageThumbnailManage.Center;
import Tools.ImageManager.CheckFileIsRightPictureType;
import Tools.ImageManager.GetImageInformation;
import Tools.OSInformation.MemoryUtil;
import Version.Download.DownloadUpdate;
import Version.Version;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import Settings.Centre;
import Component.*;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Main extends JFrame {
    //初始化
    public static final Init init;
    public Centre centre;
    public static Main main;
    private JPanel panel1;
    private JTabbedPane tabbedPane1;
    private JCheckBox DoNotThingOnCloseCheckBox;
    private JCheckBox EnableConfirmExitCheckBox;
    private JCheckBox EnableCursorDisplayCheckBox;
    private JCheckBox EnableHistoryLoaderCheckBox;
    private JLabel MouseMoveOffsetsLabel;
    private JSlider MouseMoveOffsetsSlider;
    private JCheckBox EnableProxyServerCheckBox;
    private JLabel ProxyServerLabel;
    private JButton ProxyServerButton;
    private JCheckBox EnableSecureConnectionCheckBox;
    private JCheckBox AutoCheckUpdateCheckBox;
    private JButton SaveButton;
    private JButton ResetButton;
    private JButton RefreshButton;
    private JLabel JVMVersionLabel;
    private JLabel CurrentSoftwareVersionLabel;
    private JButton CheckVersionButton;
    private JTextField textField1;
    private JButton TurnButton;
    private JPanel FirstPanel;
    private JPanel SecondPanel;
    private JPanel ThirdPanel;
    private JPanel FourthPanel;
    private JLabel TopLabel;
    private JLabel VersionView;
    private JLabel CurrentSoftwareInteriorLabel;
    private JLabel OSLabel;
    private JLabel CurrentSoftwareLanguage;
    private JLabel MemUsed;
    private JPanel FileChoosePane;
    private JScrollPane jscrollPane;
    private static ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private static ScheduledFuture<?> future;
    private final ChangeFocusListener changeFocusListener;
    //图片缩略图
    public Center center;
    //判断按钮是否被按下
    private static boolean IsDragging;
    //最新版本下载地址（如果当前是最新版本，则返回null值）
    private static List<String> NewVersionDownloadingWebSide;
    //更新维护线程
    public static Thread DaemonUpdate;
    //更新网站（必须指定VersionID.sum下载地址）
    public static String UPDATE_WEBSITE = "https://gitee.com/nserly-huaer/ImagePlayer/raw/master/artifacts/PicturePlayer_jar/VersionID.sum";
    final String MouseMoveLabelPrefix;
    final String ProxyServerPrefix;
    public PaintPicture paintPicture;
    private MouseAdapter mouseAdapter = new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                tabbedPane1.setSelectedIndex(0);
            }
        }
    };

    //静态代码块
    static {
        //初始化Init
        init = new Init<String, String>();
        init.SetUpdate(true);
        init.Run();
        if (init.containsKey("EnableProxyServer") && init.containsKey("ProxyServer") && init.getProperties().get("EnableProxyServer").equals("true") && !init.getProperties().get("ProxyServer").toString().isBlank()) {
            String website = init.getProperties().getProperty("ProxyServer");
            if (website.endsWith(".sum")) {
                UPDATE_WEBSITE = website;
            } else {
                UPDATE_WEBSITE = website.trim();
                if (UPDATE_WEBSITE.endsWith("/")) {
                    UPDATE_WEBSITE += "VersionID.sum";
                } else {
                    UPDATE_WEBSITE += "/VersionID.sum";
                }
            }
        }
    }

    public static void main(String[] args) {
        //获取操作系统版本
        System.out.println("OS:" + System.getProperty("os.name") + " " + System.getProperty("os.arch") + " " + System.getProperty("os.version"));
        //获取系统语言
        System.out.println("System Language:" + System.getProperty("user.language"));
        //获取java版本
        System.out.println("Java Runtime:" + System.getProperty("java.vm.name") + " " + System.getProperty("java.runtime.version") + " (" + System.getProperty("sun.boot.library.path") + ")");
        //获取软件版本
        System.out.println("Software Version:" + Version.getVersion());
        if (init.containsKey("AutoCheckUpdate") && init.getProperties().get("AutoCheckUpdate").equals("true")) {
            DownloadUpdate downloadUpdate = new DownloadUpdate(UPDATE_WEBSITE);
            new Thread(() -> {
                NewVersionDownloadingWebSide = downloadUpdate.getUpdateWebSide();
                if (NewVersionDownloadingWebSide != null && !NewVersionDownloadingWebSide.isEmpty()) {
                    UpdateForm(downloadUpdate);
                }
            }).start();
        }
        main = new Main("Picture Player");
    }

    public Main(String title) {
        super(title);
        center = new Center();
        changeFocusListener = new ChangeFocusListener(this);
        centre = new Centre();
        $$$setupUI$$$();
        setContentPane(this.panel1);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
//        pack();
        setBounds(500, 350, 800, 580);
        ProxyServerPrefix = ProxyServerLabel.getText();
        MouseMoveLabelPrefix = MouseMoveOffsetsLabel.getText();
        Init();
        About();
        setVisible(true);
    }

    //初始化所有组件设置
    private void Init() {
        SecondPanel.addMouseListener(mouseAdapter);
        TurnButton.addActionListener(e -> {

        });
        // 设置JPanel为可接受拖放
        new DropTarget(SecondPanel, DnDConstants.ACTION_COPY_OR_MOVE, new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    Transferable transferable = dtde.getTransferable();
                    if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        List<File> files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                        CheckFileIsRightPictureType checkFileIsRightPictureType = new CheckFileIsRightPictureType();
                        checkFileIsRightPictureType.add(files);
                        checkFileIsRightPictureType.statistics();

                        if (checkFileIsRightPictureType.getNotImageCount() != 0) {
                            JOptionPane.showMessageDialog(Main.main, "尚未支持打开此文件:\n\"" + checkFileIsRightPictureType.FilePathToString("\n", checkFileIsRightPictureType.getNotImageList()) + "\"", "Error", JOptionPane.ERROR_MESSAGE);
                            if (checkFileIsRightPictureType.getImageCount() == 0) return;
                        }
                        File choose;
                        if (checkFileIsRightPictureType.getImageCount() == 1) {
                            choose = checkFileIsRightPictureType.getImageList().getFirst();
                            if (Main.main.paintPicture != null && !new File(Main.main.paintPicture.myCanvas.getPath()).equals(choose) && JOptionPane.showConfirmDialog(Main.main, "是否打开文件:\n\"" + choose.getPath() + "\"", "打开", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                                return;
                            }
                        } else {
                            choose = OpenImageChooser.openImageWithChoice(Main.main, checkFileIsRightPictureType.getImageList());
                            if (choose == null) return;
                        }
                        if (paintPicture != null && paintPicture.myCanvas != null && choose.equals(new File(paintPicture.myCanvas.getPath())))
                            return;
                        openPicture(String.valueOf(choose));
                    }
                } catch (IOException | UnsupportedFlavorException e) {
                    System.out.println("Error:" + e);
                }
            }
        }, true);

        VersionView.setText(VersionView.getText() + Version.getVersion());
        Settings();
        SaveButton.addActionListener(e -> {
            System.out.println("Saving Settings...");
            centre.save();
            SettingRevised(false);
        });
        ResetButton.addActionListener(e -> {
            centre.setDefault();
            reFresh();
            SettingRevised(true);
        });
        RefreshButton.addActionListener(e -> {
            centre.reFresh();
            reFresh();
            SettingRevised(false);
        });
        ProxyServerButton.addActionListener(e -> {
            String newProxyServer = null;
            if (!centre.CurrentData.get("ProxyServer").toString().trim().isEmpty())
                newProxyServer = JOptionPane.showInputDialog("Please type here for Proxy Server", centre.CurrentData.get("ProxyServer"));
            else
                newProxyServer = JOptionPane.showInputDialog("Please type here for Proxy Server", "代理服务器地址");
            if (newProxyServer != null && !newProxyServer.equals("代理服务器地址") && !newProxyServer.isEmpty()) {
                centre.CurrentData.replace("ProxyServer", newProxyServer);
                ProxyServerLabel.setText("代理服务器: " + centre.CurrentData.get("ProxyServer"));
                SettingRevised(true);
            }
        });
        TurnButton.addMouseListener(changeFocusListener);
        JVMVersionLabel.setText(JVMVersionLabel.getText() + System.getProperty("java.runtime.version"));
        CurrentSoftwareVersionLabel.setText(CurrentSoftwareVersionLabel.getText() + Version.getVersion());
        CurrentSoftwareInteriorLabel.setText(CurrentSoftwareInteriorLabel.getText() + Version.getVersionID());
        CheckVersionButton.addMouseListener(changeFocusListener);
        OSLabel.setText(OSLabel.getText() + System.getProperty("os.name") + " " + System.getProperty("os.arch") + " " + System.getProperty("os.version"));
        CurrentSoftwareLanguage.setText(CurrentSoftwareLanguage.getText() + System.getProperty("user.language"));
        final String memI = MemUsed.getText();
        tabbedPane1.addChangeListener(e -> {
            //当选项界面切换时
            if (tabbedPane1.getSelectedIndex() == 0) {
                //让路径输入框获取焦点
                textField1.requestFocus();
            } else if (tabbedPane1.getSelectedIndex() == 1) {
                //让图片渲染器获取焦点
                if (paintPicture != null) {
                    paintPicture.myCanvas.requestFocus();
                }
            } else if (tabbedPane1.getSelectedIndex() == 2) {
                //让窗体获取焦点
                tabbedPane1.requestFocus();
                reFresh();
            } else {
                //让窗体获取焦点
                requestFocus();
                future = executor.scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        Map<String, Object> map = MemoryUtil.getMemoryInfo();
                        MemUsed.setText(memI + map.get("heapMemoryUsed") + "/" + map.get("heapMemoryMax") + "(" + map.get("heapUsage") + ")");
                    }
                }, 0, 2, TimeUnit.SECONDS);
            }
            if (tabbedPane1.getSelectedIndex() != 3) {
                if (future != null)
                    future.cancel(false);
            }
        });
        //设置窗体在显示时自动获取焦点
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                //当前窗体成为活动窗体时
                if (tabbedPane1.getSelectedIndex() == 0) {
                    //让路径输入框获取焦点
                    textField1.requestFocus();
                } else {
                    //让窗体获取焦点
                    requestFocus();
                }
            }

            @Override
            public void windowClosing(WindowEvent e) {
                close();
            }
        });
    }

    //打开图片
    private void openPicture(String path) {
        SecondPanel.setLayout(new BorderLayout());
        SecondPanel.removeAll();
        SecondPanel.removeMouseListener(mouseAdapter);
        paintPicture = new PaintPicture(path);
        SecondPanel.add(paintPicture);
        SecondPanel.revalidate();
        tabbedPane1.setSelectedIndex(1);
        paintPicture.myCanvas.requestFocus();
        paintPicture.sizeOperate.incomeWindowDimension(SecondPanel.getSize());
        paintPicture.sizeOperate.setPercent(paintPicture.sizeOperate.getPictureOptimalSize());
        paintPicture.sizeOperate.update();
    }


    //设置界面
    private void Settings() {
        reFresh();
        DoNotThingOnCloseCheckBox.addActionListener(e -> {
            centre.CurrentData.replace("DoNotThingOnClose", String.valueOf(DoNotThingOnCloseCheckBox.isSelected()));
            SettingRevised(true);
        });
        EnableConfirmExitCheckBox.addActionListener(e -> {
            centre.CurrentData.replace("EnableConfirmExit", String.valueOf(EnableConfirmExitCheckBox.isSelected()));
            SettingRevised(true);
        });
        EnableHistoryLoaderCheckBox.addActionListener(e -> {
            centre.CurrentData.replace("EnableHistoryLoader", String.valueOf(EnableHistoryLoaderCheckBox.isSelected()));
            SettingRevised(true);
        });
        EnableCursorDisplayCheckBox.addActionListener(e -> {
            centre.CurrentData.replace("EnableCursorDisplay", String.valueOf(EnableCursorDisplayCheckBox.isSelected()));
            SettingRevised(true);
        });
        MouseMoveOffsetsSlider.addChangeListener(e -> {
            centre.CurrentData.replace("MouseMoveOffsets", String.valueOf(MouseMoveOffsetsSlider.getValue()));
            StringBuffer stringBuffer1 = new StringBuffer(MouseMoveLabelPrefix);
            stringBuffer1.insert(MouseMoveLabelPrefix.indexOf(":"), MouseMoveOffsetsSlider.getValue() + "% ");
            MouseMoveOffsetsLabel.setText(stringBuffer1.toString());
            SettingRevised(true);
        });
        EnableProxyServerCheckBox.addActionListener(e -> {
            centre.CurrentData.replace("EnableProxyServer", String.valueOf(EnableProxyServerCheckBox.isSelected()));
            ProxyServerButton.setEnabled(EnableProxyServerCheckBox.isSelected());
            SettingRevised(true);
        });
        EnableSecureConnectionCheckBox.addActionListener(e -> {
            if (!EnableSecureConnectionCheckBox.isSelected()) {
                EnableSecureConnectionCheckBox.setSelected(true);
                int choose = JOptionPane.showConfirmDialog(Main.main, "Are you sure it's closed?\nIt may make the computer more vulnerable", "Turn off Secure Connection", JOptionPane.YES_NO_OPTION);
                if (choose == 1) {
                    return;
                }
                EnableSecureConnectionCheckBox.setSelected(false);
            }
            centre.CurrentData.replace("EnableSecureConnection", String.valueOf(EnableSecureConnectionCheckBox.isSelected()));
            SettingRevised(true);
        });
        AutoCheckUpdateCheckBox.addActionListener(e -> {
            centre.CurrentData.replace("AutoCheckUpdate", String.valueOf(AutoCheckUpdateCheckBox.isSelected()));
            SettingRevised(true);
        });
    }

    private void reFresh() {
        DoNotThingOnCloseCheckBox.setSelected(Centre.getBoolean("DoNotThingOnClose", centre.CurrentData));
        EnableConfirmExitCheckBox.setSelected(Centre.getBoolean("EnableConfirmExit", centre.CurrentData));
        EnableHistoryLoaderCheckBox.setSelected(Centre.getBoolean("EnableHistoryLoader", centre.CurrentData));
        EnableCursorDisplayCheckBox.setSelected(Centre.getBoolean("EnableCursorDisplay", centre.CurrentData));
        MouseMoveOffsetsSlider.setValue((int) Centre.getDouble("MouseMoveOffsets", centre.CurrentData));
        StringBuffer stringBuffer = new StringBuffer(MouseMoveLabelPrefix);
        stringBuffer.insert(MouseMoveLabelPrefix.indexOf(":"), MouseMoveOffsetsSlider.getValue() + "% ");
        MouseMoveOffsetsLabel.setText(stringBuffer.toString());
        EnableProxyServerCheckBox.setSelected(Centre.getBoolean("EnableProxyServer", centre.CurrentData));
        ProxyServerLabel.setText(ProxyServerPrefix + centre.CurrentData.get("ProxyServer"));
        EnableSecureConnectionCheckBox.setSelected(Centre.getBoolean("EnableSecureConnection", centre.CurrentData));
        AutoCheckUpdateCheckBox.setSelected(Centre.getBoolean("AutoCheckUpdate", centre.CurrentData));
        ProxyServerButton.setEnabled(EnableProxyServerCheckBox.isSelected());
    }

    //关于界面设置
    private void About() {
        DownloadUpdate downloadUpdate = new DownloadUpdate(UPDATE_WEBSITE);
        CheckVersionButton.addActionListener(e -> {
            try {
                if (!downloadUpdate.checkIfTheLatestVersion()) {
                    JOptionPane.showConfirmDialog(Main.main, "已是最新版本！", "You are up to date", JOptionPane.OK_OPTION);
                    return;
                }
            } catch (IOException e1) {
                System.out.println("Error:" + e1);
                JOptionPane.showConfirmDialog(Main.main, "无法获取更新，请稍后重试~", "Error", JOptionPane.OK_OPTION);
                return;
            }
            new Thread(() -> {
//                NewVersionDownloadingWebSide = downloadUpdate.downloadFileWebSide;
//                if (NewVersionDownloadingWebSide != null && !NewVersionDownloadingWebSide.isEmpty()) {
//                    UpdateForm();
//                }
                ConfirmUpdateDialog confirmUpdateDialog = new ConfirmUpdateDialog(downloadUpdate);
                confirmUpdateDialog.pack();
                confirmUpdateDialog.setVisible(true);
            }).start();
        });


    }

    //设置修改
    private void SettingRevised(boolean a) {
        if (a && !getTitle().contains("*")) {
            setTitle(getTitle() + "*");
        } else if ((!a) && getTitle().contains("*")) {
            setTitle(getTitle().substring(0, getTitle().lastIndexOf("*")));
        }
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
        panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.setRequestFocusEnabled(true);
        jscrollPane = new JScrollPane();
        panel1.add(jscrollPane, new GridConstraints(1, 0, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        tabbedPane1 = new JTabbedPane();
        tabbedPane1.setRequestFocusEnabled(false);
        jscrollPane.setViewportView(tabbedPane1);
        FirstPanel = new JPanel();
        FirstPanel.setLayout(new GridLayoutManager(3, 2, new Insets(0, 0, 0, 0), -1, -1));
        FirstPanel.setName("");
        FirstPanel.setToolTipText("");
        tabbedPane1.addTab("打开", FirstPanel);
        VersionView = new JLabel();
        VersionView.setRequestFocusEnabled(false);
        VersionView.setText("Version:");
        FirstPanel.add(VersionView, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_SOUTHEAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        textField1 = new JTextField();
        FirstPanel.add(textField1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        TurnButton = new JButton();
        TurnButton.setText("跳转");
        FirstPanel.add(TurnButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        FirstPanel.add(FileChoosePane, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        SecondPanel = new JPanel();
        SecondPanel.setLayout(new GridLayoutManager(8, 4, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane1.addTab("显示", SecondPanel);
        final JLabel label1 = new JLabel();
        Font label1Font = this.$$$getFont$$$(null, -1, 35, label1.getFont());
        if (label1Font != null) label1.setFont(label1Font);
        label1.setHorizontalTextPosition(11);
        label1.setText("开始使用照片查看器");
        SecondPanel.add(label1, new GridConstraints(2, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        SecondPanel.add(spacer1, new GridConstraints(0, 0, 3, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        SecondPanel.add(spacer2, new GridConstraints(0, 3, 3, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        Font label2Font = this.$$$getFont$$$(null, -1, 20, label2.getFont());
        if (label2Font != null) label2.setFont(label2Font);
        label2.setText("选择图片后，你将能够在此处查看照片");
        SecondPanel.add(label2, new GridConstraints(3, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        SecondPanel.add(spacer3, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final Spacer spacer4 = new Spacer();
        SecondPanel.add(spacer4, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final Spacer spacer5 = new Spacer();
        SecondPanel.add(spacer5, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final Spacer spacer6 = new Spacer();
        SecondPanel.add(spacer6, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final Spacer spacer7 = new Spacer();
        SecondPanel.add(spacer7, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        Font label3Font = this.$$$getFont$$$(null, -1, 15, label3.getFont());
        if (label3Font != null) label3.setFont(label3Font);
        label3.setText("点击此处导入图片");
        SecondPanel.add(label3, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        ThirdPanel = new JPanel();
        ThirdPanel.setLayout(new GridLayoutManager(11, 3, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane1.addTab("设置", ThirdPanel);
        DoNotThingOnCloseCheckBox = new JCheckBox();
        DoNotThingOnCloseCheckBox.setRequestFocusEnabled(false);
        DoNotThingOnCloseCheckBox.setText("退出时，隐藏至系统托盘");
        ThirdPanel.add(DoNotThingOnCloseCheckBox, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        EnableConfirmExitCheckBox = new JCheckBox();
        EnableConfirmExitCheckBox.setRequestFocusEnabled(false);
        EnableConfirmExitCheckBox.setText("启用退出提示");
        ThirdPanel.add(EnableConfirmExitCheckBox, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        EnableCursorDisplayCheckBox = new JCheckBox();
        EnableCursorDisplayCheckBox.setRequestFocusEnabled(false);
        EnableCursorDisplayCheckBox.setText("启用鼠标光标显示");
        ThirdPanel.add(EnableCursorDisplayCheckBox, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        EnableHistoryLoaderCheckBox = new JCheckBox();
        EnableHistoryLoaderCheckBox.setRequestFocusEnabled(false);
        EnableHistoryLoaderCheckBox.setText("启用历史路径加载");
        ThirdPanel.add(EnableHistoryLoaderCheckBox, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        MouseMoveOffsetsLabel = new JLabel();
        MouseMoveOffsetsLabel.setRequestFocusEnabled(false);
        MouseMoveOffsetsLabel.setText("鼠标移动补偿:");
        ThirdPanel.add(MouseMoveOffsetsLabel, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        EnableProxyServerCheckBox = new JCheckBox();
        EnableProxyServerCheckBox.setRequestFocusEnabled(false);
        EnableProxyServerCheckBox.setText("启用代理服务器");
        ThirdPanel.add(EnableProxyServerCheckBox, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        ProxyServerLabel = new JLabel();
        ProxyServerLabel.setRequestFocusEnabled(false);
        ProxyServerLabel.setText("代理服务器：");
        ThirdPanel.add(ProxyServerLabel, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        ProxyServerButton = new JButton();
        ProxyServerButton.setRequestFocusEnabled(false);
        ProxyServerButton.setText("设置代理服务器");
        ThirdPanel.add(ProxyServerButton, new GridConstraints(7, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        EnableSecureConnectionCheckBox = new JCheckBox();
        EnableSecureConnectionCheckBox.setRequestFocusEnabled(false);
        EnableSecureConnectionCheckBox.setText("启用安全连接模式");
        ThirdPanel.add(EnableSecureConnectionCheckBox, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(2, 3, new Insets(0, 0, 0, 0), -1, -1));
        ThirdPanel.add(panel2, new GridConstraints(10, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final Spacer spacer8 = new Spacer();
        panel2.add(spacer8, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        ResetButton = new JButton();
        ResetButton.setRequestFocusEnabled(false);
        ResetButton.setText("重置所有设置");
        panel2.add(ResetButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_SOUTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        RefreshButton = new JButton();
        RefreshButton.setRequestFocusEnabled(false);
        RefreshButton.setText("恢复");
        panel2.add(RefreshButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_SOUTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        SaveButton = new JButton();
        SaveButton.setHorizontalAlignment(0);
        SaveButton.setRequestFocusEnabled(false);
        SaveButton.setText("保存");
        SaveButton.setVerticalTextPosition(0);
        panel2.add(SaveButton, new GridConstraints(1, 0, 1, 3, GridConstraints.ANCHOR_SOUTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer9 = new Spacer();
        ThirdPanel.add(spacer9, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        MouseMoveOffsetsSlider = new JSlider();
        MouseMoveOffsetsSlider.setMaximum(150);
        MouseMoveOffsetsSlider.setMinimum(-65);
        MouseMoveOffsetsSlider.setRequestFocusEnabled(false);
        ThirdPanel.add(MouseMoveOffsetsSlider, new GridConstraints(5, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        AutoCheckUpdateCheckBox = new JCheckBox();
        AutoCheckUpdateCheckBox.setRequestFocusEnabled(false);
        AutoCheckUpdateCheckBox.setText("启用自动检查更新");
        ThirdPanel.add(AutoCheckUpdateCheckBox, new GridConstraints(9, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        FourthPanel = new JPanel();
        FourthPanel.setLayout(new GridLayoutManager(7, 4, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane1.addTab("关于", FourthPanel);
        JVMVersionLabel = new JLabel();
        Font JVMVersionLabelFont = this.$$$getFont$$$(null, -1, 16, JVMVersionLabel.getFont());
        if (JVMVersionLabelFont != null) JVMVersionLabel.setFont(JVMVersionLabelFont);
        JVMVersionLabel.setText("JVM Version:");
        FourthPanel.add(JVMVersionLabel, new GridConstraints(1, 0, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer10 = new Spacer();
        FourthPanel.add(spacer10, new GridConstraints(6, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        CurrentSoftwareVersionLabel = new JLabel();
        Font CurrentSoftwareVersionLabelFont = this.$$$getFont$$$(null, -1, 16, CurrentSoftwareVersionLabel.getFont());
        if (CurrentSoftwareVersionLabelFont != null)
            CurrentSoftwareVersionLabel.setFont(CurrentSoftwareVersionLabelFont);
        CurrentSoftwareVersionLabel.setText("当前软件版本：");
        FourthPanel.add(CurrentSoftwareVersionLabel, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        CurrentSoftwareInteriorLabel = new JLabel();
        Font CurrentSoftwareInteriorLabelFont = this.$$$getFont$$$(null, -1, 16, CurrentSoftwareInteriorLabel.getFont());
        if (CurrentSoftwareInteriorLabelFont != null)
            CurrentSoftwareInteriorLabel.setFont(CurrentSoftwareInteriorLabelFont);
        CurrentSoftwareInteriorLabel.setText("内部版本：");
        FourthPanel.add(CurrentSoftwareInteriorLabel, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        CheckVersionButton = new JButton();
        Font CheckVersionButtonFont = this.$$$getFont$$$(null, -1, 16, CheckVersionButton.getFont());
        if (CheckVersionButtonFont != null) CheckVersionButton.setFont(CheckVersionButtonFont);
        CheckVersionButton.setText("检查更新");
        FourthPanel.add(CheckVersionButton, new GridConstraints(5, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        OSLabel = new JLabel();
        Font OSLabelFont = this.$$$getFont$$$(null, -1, 16, OSLabel.getFont());
        if (OSLabelFont != null) OSLabel.setFont(OSLabelFont);
        OSLabel.setText("操作系统：");
        FourthPanel.add(OSLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        CurrentSoftwareLanguage = new JLabel();
        CurrentSoftwareLanguage.setEnabled(true);
        Font CurrentSoftwareLanguageFont = this.$$$getFont$$$(null, -1, 16, CurrentSoftwareLanguage.getFont());
        if (CurrentSoftwareLanguageFont != null) CurrentSoftwareLanguage.setFont(CurrentSoftwareLanguageFont);
        CurrentSoftwareLanguage.setText("当前软件语言：");
        FourthPanel.add(CurrentSoftwareLanguage, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        MemUsed = new JLabel();
        Font MemUsedFont = this.$$$getFont$$$(null, -1, 16, MemUsed.getFont());
        if (MemUsedFont != null) MemUsed.setFont(MemUsedFont);
        MemUsed.setText("JVM内存：");
        FourthPanel.add(MemUsed, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        TopLabel = new JLabel();
        Font TopLabelFont = this.$$$getFont$$$(null, -1, 20, TopLabel.getFont());
        if (TopLabelFont != null) TopLabel.setFont(TopLabelFont);
        TopLabel.setText("Picture Player");
        panel1.add(TopLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel1;
    }

    //关闭
    public static void close() {
        //加载配置文件
        Main.main.init.Loading();
        if (init.getProperties().get("EnableConfirmExit") != null && init.getProperties().get("EnableConfirmExit").toString().toLowerCase().equals("false")) {
            if (PaintPicture.paintPicture != null && PaintPicture.paintPicture.sizeOperate != null)
                PaintPicture.paintPicture.sizeOperate.close();
            if (main != null) main.dispose();
            System.exit(0);
        }
        //设置消息对话框面板
        var jDialog = new JDialog(main, true);
        //设置面板标题
        jDialog.setTitle("Confirm Exit");
        //设置面板大小（获取父面板坐标）
        jDialog.setSize(260, 170);
        jDialog.setLocation(WindowLocation.ComponentCenter(main, jDialog.getWidth(), jDialog.getHeight()));
        //创建文字
        var jLabel1 = new JLabel("Are you sure you want to exit?");
        //设置文字字体、格式
        jLabel1.setFont(new Font("微软雅黑", 0, 15));
        //设置显示大小、坐标
        jLabel1.setBounds(15, 3, 290, 50);
        //创建按钮
        var yes = new JButton("exit");
        var no = new JButton("cancel");
        yes.setBounds(20, 50, 100, 35);
        yes.setForeground(Color.RED);
        no.setBounds(130, 50, 100, 35);
        jDialog.setResizable(false);
        //设置布局
        jDialog.setLayout(null);
        //将文字、按钮放入组件中
        jDialog.add(jLabel1);
        jDialog.add(yes);
        jDialog.add(no);
        ChangeFocusListener changeFocusListener = new ChangeFocusListener(jDialog);
        JCheckBox jCheckBox = new JCheckBox("Don't ask again");
        jCheckBox.setBounds(60, 95, 200, 25);
        jCheckBox.addMouseListener(changeFocusListener);
        jDialog.add(jCheckBox);
        //如果确定退出软件，运行退出程序
        yes.addActionListener(e1 -> {
            //关闭面板
            main.dispose();
            //判断paintPicture是否为null值（以防止出现空指针异常）
            if (PaintPicture.paintPicture != null && PaintPicture.paintPicture.sizeOperate != null)
                PaintPicture.paintPicture.sizeOperate.close();
            if (main != null) main.dispose();
            if (jCheckBox.isSelected()) {
                init.ChangeValue("EnableConfirmExit", "false");
                init.Update();
            }
            if (Main.main != null) {
                Main.main.center.save();
            }
            System.exit(0);
        });
        yes.addMouseListener(changeFocusListener);
        //点击取消以询问隐藏面板（不会退出程序）
        no.addActionListener(e1 -> {
            jDialog.setVisible(false);
        });
        no.addMouseListener(changeFocusListener);
        //面板键盘监听器
        jDialog.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    jDialog.setVisible(false);
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    //关闭面板
                    main.dispose();
                    //判断paintPicture是否为null值（以防止出现空指针异常）
                    if (PaintPicture.paintPicture != null && PaintPicture.paintPicture.sizeOperate != null)
                        PaintPicture.paintPicture.sizeOperate.close();
                    if (main != null) main.dispose();
                    if (jCheckBox.isSelected()) {
                        init.ChangeValue("EnableConfirmExit", "false");
                        init.Update();
                    }
                    if (Main.main != null) {
                        Main.main.center.save();
                    }
                    System.exit(0);
                }
            }
        });
        //设置面板在显示时自动获取焦点
        jDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                //当前面板成为活动面板时，让myCanvas获取焦点
                jDialog.requestFocus();
            }
        });
        //显示面板
        jDialog.setVisible(true);
    }

    //更新界面
    public static void UpdateForm(DownloadUpdate downloadUpdate) {
        ConfirmUpdateDialog confirmUpdateDialog = new ConfirmUpdateDialog(downloadUpdate);
        confirmUpdateDialog.setVisible(true);
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        FileChoosePane = new JPanel();
        JButton openButton = new JButton("打开文件");
        openButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                String lastChooseDir = "";
                if (paintPicture != null && paintPicture.myCanvas != null) {
                    lastChooseDir = new File(paintPicture.myCanvas.getPath()).getParent();
                }
                fileChooser.setCurrentDirectory(new File(lastChooseDir));
                while (true) {
                    int returnValue = fileChooser.showOpenDialog(Main.main);
                    if (returnValue == JFileChooser.APPROVE_OPTION) {
                        String picturePath = fileChooser.getSelectedFile().getAbsolutePath();
                        if (!GetImageInformation.isImageFile(new File(picturePath))) {
                            JOptionPane.showMessageDialog(Main.main, "尚未支持打开此文件:\n\"" + picturePath + "\"", "Error", JOptionPane.ERROR_MESSAGE);
                            continue;
                        } else {
                            if (Main.main.paintPicture != null && new File(Main.main.paintPicture.myCanvas.getPath()).equals(fileChooser.getSelectedFile())) {
                                tabbedPane1.setSelectedIndex(1);
                                return;
                            }
                            break;
                        }
                    }
                    return;
                }
                openPicture(fileChooser.getSelectedFile().getPath());
            }
        });
        FileChoosePane.add(openButton);
    }
}
