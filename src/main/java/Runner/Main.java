package Runner;

import Listener.ChangeFocusListener;
import Loading.Init;
import Tools.Component.WindowLocation;
import Tools.File.ImageThumbnailManage.Center;
import Tools.ImageManager.CheckFileIsRightPictureType;
import Tools.ImageManager.GetImageInformation;
import Version.DownloadUpdate;
import Tools.OSInformation.SystemMonitor;
import Version.Version;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import Settings.Centre;
import Component.*;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
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
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Main extends JFrame {
    //初始化
    public static final Init<String, String> init;
    public Centre centre;
    public static Main main;
    private JPanel panel1;
    private JTabbedPane tabbedPane1;
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
    private JPanel SecondPanel;
    private JLabel TopLabel;
    private JLabel VersionView;
    private JLabel CurrentSoftwareInteriorLabel;
    private JLabel OSLabel;
    private JLabel CurrentSoftwareLanguage;
    private JLabel MemUsed;
    private JPanel FileChoosePane;
    private JPanel FirstPanel;
    private JPanel ThirdPanel;
    private JPanel FourthPanel;
    private JLabel TotalThread;
    private JLabel DefaultJVMMem;
    private JLabel ProgramStartTime;
    private JLabel CPUName;
    private JLabel JavaPath;
    private JCheckBox EnableHardwareAccelerationCheckBox;
    private JCheckBox EnableTurnAboveOrBelowCheckBox;
    private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private static ScheduledFuture<?> future;
    private final ChangeFocusListener changeFocusListener;
    //图片缩略图
    public Center center;
    //是否启用代理服务器
    private static boolean EnableProxyServer;
    //最新版本下载地址（如果当前是最新版本，则返回null值）
    private static List<String> NewVersionDownloadingWebSide;
    //更新维护线程
    public static Thread DaemonUpdate;
    //更新网站（必须指定VersionID.sum下载地址）
    public static String UPDATE_WEBSITE = "https://gitee.com/nserly-huaer/ImagePlayer/raw/master/artifacts/PicturePlayer_jar/VersionID.sum";
    final String MouseMoveLabelPrefix;
    final String ProxyServerPrefix;
    public PaintPicture paintPicture;
    private boolean IsFreshen;
    private static final File REPEAT_PICTURE_PATH_LOGOTYPE = new File("???");
    private final MouseAdapter mouseAdapter = new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                tabbedPane1.setSelectedIndex(0);
            }
        }
    };
    private final DropTargetAdapter dropTargetAdapter = new DropTargetAdapter() {
        public void drop(DropTargetDropEvent dtde) {
            try {
                dtde.acceptDrop(DnDConstants.ACTION_COPY);
                Transferable transferable = dtde.getTransferable();
                if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    List<File> files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                    File file = checkFileOpen(files, true);
                    if (file != null) openPicture(file.getPath());
                }
            } catch (IOException | UnsupportedFlavorException e) {
                System.out.println("Error:" + e);
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
            EnableProxyServer = true;
        }
        if (!GetImageInformation.isHardwareAccelerated) {
            System.out.println("Waring:Hardware acceleration is not supported, and the image will be rendered using software!");
        }
    }

    public static void main(String[] args) {
        main = new Main("Picture Player(Version:" + Version.getVersion() + ")");
        if (args.length > 0 && GetImageInformation.isImageFile(new File(args[0]))) {
            main.openPicture(args[0]);
        }
        //获取操作系统版本
        System.out.println("OS:" + SystemMonitor.OS_NAME);
        //获取系统语言
        System.out.println("System Language:" + System.getProperty("user.language"));
        //获取java版本
        System.out.println("Java Runtime:" + System.getProperty("java.vm.name") + " " + System.getProperty("java.runtime.version") + " (" + System.getProperty("sun.boot.library.path") + ")");
        //获取软件版本
        System.out.println("Software Version:" + Version.getVersion());
        //程序是否启用硬件加速
        System.out.println("Enable Hardware Acceleration:" + PaintPicture.isEnableHardwareAcceleration);
        if (init.containsKey("AutoCheckUpdate") && init.getProperties().get("AutoCheckUpdate").equals("true")) {
            DownloadUpdate downloadUpdate = new DownloadUpdate(UPDATE_WEBSITE);
            new Thread(() -> {
                NewVersionDownloadingWebSide = downloadUpdate.getUpdateWebSide();
                if (NewVersionDownloadingWebSide != null && !NewVersionDownloadingWebSide.isEmpty()) {
                    UpdateForm(downloadUpdate);
                }
            }).start();
        }
    }

    public Main(String title) {
        super(title);
        $$$setupUI$$$();
        new Thread(() -> {
            setContentPane(this.panel1);
            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
            setVisible(true);
            setSize(800, 580);
        }).start();
        changeFocusListener = new ChangeFocusListener(this);
        new Thread(() -> {
            setLocation(WindowLocation.ComponentCenter(null, 800, 580));
            centre = new Centre();
            center = new Center();
            Init();
            PaintPicture.isEnableHardwareAcceleration = EnableHardwareAccelerationCheckBox.isSelected() && GetImageInformation.isHardwareAccelerated;
            if (!GetImageInformation.isHardwareAccelerated) {
                EnableHardwareAccelerationCheckBox.setSelected(false);
                EnableHardwareAccelerationCheckBox.setEnabled(false);
                centre.save();
            }
            About();
        }).start();
        ProxyServerPrefix = ProxyServerLabel.getText();
        MouseMoveLabelPrefix = MouseMoveOffsetsLabel.getText();
    }

    //初始化所有组件设置
    private void Init() {
        VersionView.setText(VersionView.getText() + Version.getVersion());
        TurnButton.addMouseListener(changeFocusListener);
        TurnButton.addActionListener(e -> {

        });
        SecondPanel.addMouseListener(mouseAdapter);
        // 设置SecondPanel为可接受拖放
        new DropTarget(SecondPanel, DnDConstants.ACTION_COPY_OR_MOVE, dropTargetAdapter, true);
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
            if (!centre.CurrentData.get("ProxyServer").trim().isEmpty())
                newProxyServer = JOptionPane.showInputDialog(Main.main, "Please type here for Proxy Server", centre.CurrentData.get("ProxyServer"));
            else
                newProxyServer = JOptionPane.showInputDialog(Main.main, "Please type here for Proxy Server", "代理服务器地址");
            if (newProxyServer == null) return;
            newProxyServer = newProxyServer.replace(" ", "");
            if (newProxyServer.equals(centre.CurrentData.get("ProxyServer"))) return;
            if (!newProxyServer.equals("代理服务器地址") && !newProxyServer.isEmpty()) {
                centre.CurrentData.replace("ProxyServer", newProxyServer);
                ProxyServerLabel.setText("代理服务器: " + centre.CurrentData.get("ProxyServer"));
                JOptionPane.showConfirmDialog(Main.main, "代理服务器设置成功，重启生效~", "Proxy Server have Done", JOptionPane.YES_NO_OPTION);
                SettingRevised(true);
            }
        });
        new Thread(() -> {
            JavaPath.setText(JavaPath.getText() + System.getProperty("sun.boot.library.path"));
            DefaultJVMMem.setText(DefaultJVMMem.getText() + SystemMonitor.convertSize(SystemMonitor.JVM_Initialize_Memory));
            JVMVersionLabel.setText(JVMVersionLabel.getText() + System.getProperty("java.runtime.version"));
            ProgramStartTime.setText(ProgramStartTime.getText() + SystemMonitor.PROGRAM_START_TIME);
            CurrentSoftwareVersionLabel.setText(CurrentSoftwareVersionLabel.getText() + Version.getVersion());
            CurrentSoftwareInteriorLabel.setText(CurrentSoftwareInteriorLabel.getText() + Version.getVersionID());
            OSLabel.setText(OSLabel.getText() + SystemMonitor.OS_NAME);
            CPUName.setText(CPUName.getText() + SystemMonitor.CPU_NAME);
            CurrentSoftwareLanguage.setText(CurrentSoftwareLanguage.getText() + System.getProperty("user.language"));
            if (EnableProxyServer) CheckVersionButton.setText(CheckVersionButton.getText() + "（已启用代理服务器）");
            final String JmemI = MemUsed.getText();
            final String TTI = TotalThread.getText();
            tabbedPane1.addChangeListener(e -> {
                request();
                if (tabbedPane1.getSelectedIndex() == 3) {
                    future = executor.scheduleAtFixedRate(new Runnable() {
                        @Override
                        public void run() {
                            SystemMonitor.GetInformation();
                            MemUsed.setText(JmemI + SystemMonitor.convertSize(SystemMonitor.JVM_Used_Memory) + "/" + SystemMonitor.convertSize(SystemMonitor.JVM_Maximum_Free_Memory) + "(" + SystemMonitor.JVM_Memory_Usage + "%" + ")");
                            TotalThread.setText(TTI + SystemMonitor.Program_Thread_Count);
                        }
                    }, 0, 2, TimeUnit.SECONDS);
                } else {
                    if (future != null) future.cancel(false);
                }
                if (tabbedPane1.getSelectedIndex() == 2 && !IsFreshen) {
                    IsFreshen = true;
                    reFresh();
                }
            });
        }).start();
        //设置窗体在显示时自动获取焦点
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                //当前窗体成为活动窗体时，获取焦点
                request();
            }

            @Override
            public void windowClosing(WindowEvent e) {
                close();
            }
        });
    }

    //获取焦点
    private void request() {
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
        } else {
            //让窗体获取焦点
            requestFocus();
        }
    }

    //打开图片
    public void openPicture(String path) {
        if (path == null) return;
        if (paintPicture == null) {
            paintPicture = new PaintPicture(path);
        } else if (path.endsWith("???")) {
            return;
        } else {
            paintPicture.changePicturePath(path);
        }
        new DropTarget(paintPicture, DnDConstants.ACTION_COPY_OR_MOVE, dropTargetAdapter, true);
        tabbedPane1.setComponentAt(1, paintPicture);
        tabbedPane1.setSelectedIndex(1);
    }


    //设置界面
    private void Settings() {
        reFresh();
        EnableTurnAboveOrBelowCheckBox.addActionListener(e -> {
            centre.CurrentData.replace("EnableTurnAboveOrBelow", String.valueOf(EnableTurnAboveOrBelowCheckBox.isSelected()));
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
            StringBuilder stringBuffer1 = new StringBuilder(MouseMoveLabelPrefix);
            stringBuffer1.insert(MouseMoveLabelPrefix.indexOf(":"), MouseMoveOffsetsSlider.getValue() + "% ");
            MouseMoveOffsetsLabel.setText(stringBuffer1.toString());
            SettingRevised(true);
        });
        EnableProxyServerCheckBox.addActionListener(e -> {
            centre.CurrentData.replace("EnableProxyServer", String.valueOf(EnableProxyServerCheckBox.isSelected()));
            ProxyServerButton.setEnabled(EnableProxyServerCheckBox.isSelected());
            SettingRevised(true);
        });
        EnableHardwareAccelerationCheckBox.addActionListener(e -> {
            centre.CurrentData.replace("EnableHardwareAcceleration", String.valueOf(EnableHardwareAccelerationCheckBox.isSelected()));
            ProxyServerButton.setEnabled(EnableHardwareAccelerationCheckBox.isSelected());
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
        EnableTurnAboveOrBelowCheckBox.setSelected(Centre.getBoolean("EnableTurnAboveOrBelow", centre.CurrentData));
        EnableHardwareAccelerationCheckBox.setSelected(Centre.getBoolean("EnableHardwareAcceleration", centre.CurrentData));
        EnableConfirmExitCheckBox.setSelected(Centre.getBoolean("EnableConfirmExit", centre.CurrentData));
        EnableHistoryLoaderCheckBox.setSelected(Centre.getBoolean("EnableHistoryLoader", centre.CurrentData));
        EnableCursorDisplayCheckBox.setSelected(Centre.getBoolean("EnableCursorDisplay", centre.CurrentData));
        MouseMoveOffsetsSlider.setValue((int) Centre.getDouble("MouseMoveOffsets", centre.CurrentData));
        StringBuilder stringBuffer = new StringBuilder(MouseMoveLabelPrefix);
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
                    JOptionPane.showConfirmDialog(Main.main, "已是最新版本！", "You are up to date", JOptionPane.YES_NO_OPTION);
                    return;
                }
            } catch (IOException e1) {
                System.out.println("Error:" + e1);
                JOptionPane.showMessageDialog(Main.main, "Error: " + e1 + "\n无法获取更新，请稍后重试~", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            new Thread(() -> {
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
        panel1.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.setRequestFocusEnabled(true);
        TopLabel = new JLabel();
        Font TopLabelFont = this.$$$getFont$$$(null, -1, 20, TopLabel.getFont());
        if (TopLabelFont != null) TopLabel.setFont(TopLabelFont);
        TopLabel.setText("Picture Player");
        panel1.add(TopLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tabbedPane1 = new JTabbedPane();
        tabbedPane1.setRequestFocusEnabled(false);
        panel1.add(tabbedPane1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        FirstPanel = new JPanel();
        FirstPanel.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        FirstPanel.setName("");
        FirstPanel.setToolTipText("");
        tabbedPane1.addTab("打开", FirstPanel);
        VersionView = new JLabel();
        VersionView.setRequestFocusEnabled(false);
        VersionView.setText("Version:");
        FirstPanel.add(VersionView, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_SOUTHEAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        textField1 = new JTextField();
        FirstPanel.add(textField1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        TurnButton = new JButton();
        TurnButton.setRequestFocusEnabled(false);
        TurnButton.setText("打开");
        FirstPanel.add(TurnButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        FirstPanel.add(FileChoosePane, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        SecondPanel = new JPanel();
        SecondPanel.setLayout(new GridBagLayout());
        SecondPanel.setBackground(new Color(-1643536));
        SecondPanel.setEnabled(true);
        SecondPanel.setForeground(new Color(-1));
        tabbedPane1.addTab("显示", SecondPanel);
        final JLabel label1 = new JLabel();
        Font label1Font = this.$$$getFont$$$(null, -1, 35, label1.getFont());
        if (label1Font != null) label1.setFont(label1Font);
        label1.setHorizontalTextPosition(11);
        label1.setText("开始使用照片查看器");
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        SecondPanel.add(label1, gbc);
        final JLabel label2 = new JLabel();
        Font label2Font = this.$$$getFont$$$(null, -1, 20, label2.getFont());
        if (label2Font != null) label2.setFont(label2Font);
        label2.setText("选择图片后，你将能够在此处查看照片");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        SecondPanel.add(label2, gbc);
        final JLabel label3 = new JLabel();
        label3.setBackground(new Color(-2104859));
        Font label3Font = this.$$$getFont$$$(null, -1, 15, label3.getFont());
        if (label3Font != null) label3.setFont(label3Font);
        label3.setHorizontalAlignment(0);
        label3.setHorizontalTextPosition(0);
        label3.setText("点击此处导入图片或将图片拖拽到此处");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 5;
        SecondPanel.add(label3, gbc);
        final JPanel spacer1 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 9;
        gbc.fill = GridBagConstraints.VERTICAL;
        SecondPanel.add(spacer1, gbc);
        final JPanel spacer2 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 8;
        gbc.fill = GridBagConstraints.VERTICAL;
        SecondPanel.add(spacer2, gbc);
        final JPanel spacer3 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 7;
        gbc.fill = GridBagConstraints.VERTICAL;
        SecondPanel.add(spacer3, gbc);
        final JPanel spacer4 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 6;
        gbc.fill = GridBagConstraints.VERTICAL;
        SecondPanel.add(spacer4, gbc);
        final JPanel spacer5 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.VERTICAL;
        SecondPanel.add(spacer5, gbc);
        ThirdPanel = new JPanel();
        ThirdPanel.setLayout(new GridLayoutManager(3, 3, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane1.addTab("设置", ThirdPanel);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(2, 3, new Insets(0, 0, 0, 0), -1, -1));
        ThirdPanel.add(panel2, new GridConstraints(2, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final Spacer spacer6 = new Spacer();
        panel2.add(spacer6, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
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
        final Spacer spacer7 = new Spacer();
        ThirdPanel.add(spacer7, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        ThirdPanel.add(panel3, new GridConstraints(0, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel3.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(502, 372), null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new FormLayout("fill:114px:noGrow,left:4dlu:noGrow,fill:d:grow,left:4dlu:noGrow,fill:max(d;4px):noGrow", "center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow"));
        scrollPane1.setViewportView(panel4);
        EnableConfirmExitCheckBox = new JCheckBox();
        EnableConfirmExitCheckBox.setRequestFocusEnabled(false);
        EnableConfirmExitCheckBox.setText("启用退出提示");
        CellConstraints cc = new CellConstraints();
        panel4.add(EnableConfirmExitCheckBox, cc.xyw(1, 1, 3));
        EnableHardwareAccelerationCheckBox = new JCheckBox();
        EnableHardwareAccelerationCheckBox.setRequestFocusEnabled(false);
        EnableHardwareAccelerationCheckBox.setText("启用硬件加速");
        panel4.add(EnableHardwareAccelerationCheckBox, cc.xy(1, 3));
        EnableHistoryLoaderCheckBox = new JCheckBox();
        EnableHistoryLoaderCheckBox.setRequestFocusEnabled(false);
        EnableHistoryLoaderCheckBox.setText("启用历史路径加载");
        panel4.add(EnableHistoryLoaderCheckBox, cc.xyw(1, 5, 3));
        EnableCursorDisplayCheckBox = new JCheckBox();
        EnableCursorDisplayCheckBox.setRequestFocusEnabled(false);
        EnableCursorDisplayCheckBox.setText("启用鼠标光标显示");
        panel4.add(EnableCursorDisplayCheckBox, cc.xyw(1, 9, 3));
        MouseMoveOffsetsLabel = new JLabel();
        MouseMoveOffsetsLabel.setRequestFocusEnabled(false);
        MouseMoveOffsetsLabel.setText("鼠标移动补偿:");
        panel4.add(MouseMoveOffsetsLabel, cc.xyw(1, 11, 5));
        MouseMoveOffsetsSlider = new JSlider();
        MouseMoveOffsetsSlider.setMaximum(150);
        MouseMoveOffsetsSlider.setMinimum(-65);
        MouseMoveOffsetsSlider.setRequestFocusEnabled(false);
        panel4.add(MouseMoveOffsetsSlider, cc.xyw(1, 13, 5, CellConstraints.FILL, CellConstraints.DEFAULT));
        EnableProxyServerCheckBox = new JCheckBox();
        EnableProxyServerCheckBox.setRequestFocusEnabled(false);
        EnableProxyServerCheckBox.setText("启用代理服务器");
        panel4.add(EnableProxyServerCheckBox, cc.xy(1, 15));
        ProxyServerLabel = new JLabel();
        ProxyServerLabel.setRequestFocusEnabled(false);
        ProxyServerLabel.setText("代理服务器：");
        panel4.add(ProxyServerLabel, cc.xyw(1, 17, 3));
        ProxyServerButton = new JButton();
        ProxyServerButton.setRequestFocusEnabled(false);
        ProxyServerButton.setText("设置代理服务器");
        panel4.add(ProxyServerButton, cc.xy(5, 17));
        EnableSecureConnectionCheckBox = new JCheckBox();
        EnableSecureConnectionCheckBox.setRequestFocusEnabled(false);
        EnableSecureConnectionCheckBox.setText("启用安全连接模式");
        panel4.add(EnableSecureConnectionCheckBox, cc.xyw(1, 19, 3));
        AutoCheckUpdateCheckBox = new JCheckBox();
        AutoCheckUpdateCheckBox.setRequestFocusEnabled(false);
        AutoCheckUpdateCheckBox.setText("启用自动检查更新");
        panel4.add(AutoCheckUpdateCheckBox, cc.xyw(1, 21, 3));
        EnableTurnAboveOrBelowCheckBox = new JCheckBox();
        EnableTurnAboveOrBelowCheckBox.setRequestFocusEnabled(false);
        EnableTurnAboveOrBelowCheckBox.setText("启用图片上下打开");
        panel4.add(EnableTurnAboveOrBelowCheckBox, cc.xyw(1, 7, 3));
        FourthPanel = new JPanel();
        FourthPanel.setLayout(new GridLayoutManager(10, 7, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane1.addTab("关于", FourthPanel);
        JVMVersionLabel = new JLabel();
        Font JVMVersionLabelFont = this.$$$getFont$$$(null, -1, 16, JVMVersionLabel.getFont());
        if (JVMVersionLabelFont != null) JVMVersionLabel.setFont(JVMVersionLabelFont);
        JVMVersionLabel.setText("JVM版本：");
        FourthPanel.add(JVMVersionLabel, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer8 = new Spacer();
        FourthPanel.add(spacer8, new GridConstraints(9, 0, 1, 7, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        CurrentSoftwareVersionLabel = new JLabel();
        Font CurrentSoftwareVersionLabelFont = this.$$$getFont$$$(null, -1, 16, CurrentSoftwareVersionLabel.getFont());
        if (CurrentSoftwareVersionLabelFont != null)
            CurrentSoftwareVersionLabel.setFont(CurrentSoftwareVersionLabelFont);
        CurrentSoftwareVersionLabel.setText("软件版本：");
        FourthPanel.add(CurrentSoftwareVersionLabel, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        CurrentSoftwareInteriorLabel = new JLabel();
        Font CurrentSoftwareInteriorLabelFont = this.$$$getFont$$$(null, -1, 16, CurrentSoftwareInteriorLabel.getFont());
        if (CurrentSoftwareInteriorLabelFont != null)
            CurrentSoftwareInteriorLabel.setFont(CurrentSoftwareInteriorLabelFont);
        CurrentSoftwareInteriorLabel.setText("软件内部版本：");
        FourthPanel.add(CurrentSoftwareInteriorLabel, new GridConstraints(7, 2, 1, 5, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        CheckVersionButton = new JButton();
        Font CheckVersionButtonFont = this.$$$getFont$$$(null, -1, 16, CheckVersionButton.getFont());
        if (CheckVersionButtonFont != null) CheckVersionButton.setFont(CheckVersionButtonFont);
        CheckVersionButton.setRequestFocusEnabled(false);
        CheckVersionButton.setText("检查更新");
        FourthPanel.add(CheckVersionButton, new GridConstraints(8, 0, 1, 7, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        OSLabel = new JLabel();
        Font OSLabelFont = this.$$$getFont$$$(null, -1, 16, OSLabel.getFont());
        if (OSLabelFont != null) OSLabel.setFont(OSLabelFont);
        OSLabel.setText("操作系统：");
        FourthPanel.add(OSLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        ProgramStartTime = new JLabel();
        Font ProgramStartTimeFont = this.$$$getFont$$$(null, -1, 16, ProgramStartTime.getFont());
        if (ProgramStartTimeFont != null) ProgramStartTime.setFont(ProgramStartTimeFont);
        ProgramStartTime.setText("软件启动时间：");
        FourthPanel.add(ProgramStartTime, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        CurrentSoftwareLanguage = new JLabel();
        CurrentSoftwareLanguage.setEnabled(true);
        Font CurrentSoftwareLanguageFont = this.$$$getFont$$$(null, -1, 16, CurrentSoftwareLanguage.getFont());
        if (CurrentSoftwareLanguageFont != null) CurrentSoftwareLanguage.setFont(CurrentSoftwareLanguageFont);
        CurrentSoftwareLanguage.setText("系统语言：");
        FourthPanel.add(CurrentSoftwareLanguage, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        CPUName = new JLabel();
        Font CPUNameFont = this.$$$getFont$$$(null, -1, 16, CPUName.getFont());
        if (CPUNameFont != null) CPUName.setFont(CPUNameFont);
        CPUName.setText("CPU：");
        FourthPanel.add(CPUName, new GridConstraints(1, 0, 1, 7, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        TotalThread = new JLabel();
        Font TotalThreadFont = this.$$$getFont$$$(null, -1, 16, TotalThread.getFont());
        if (TotalThreadFont != null) TotalThread.setFont(TotalThreadFont);
        TotalThread.setText("总线程数：");
        FourthPanel.add(TotalThread, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        MemUsed = new JLabel();
        Font MemUsedFont = this.$$$getFont$$$(null, -1, 16, MemUsed.getFont());
        if (MemUsedFont != null) MemUsed.setFont(MemUsedFont);
        MemUsed.setText("JVM内存：");
        FourthPanel.add(MemUsed, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        DefaultJVMMem = new JLabel();
        Font DefaultJVMMemFont = this.$$$getFont$$$(null, -1, 16, DefaultJVMMem.getFont());
        if (DefaultJVMMemFont != null) DefaultJVMMem.setFont(DefaultJVMMemFont);
        DefaultJVMMem.setText("JVM初始默认内存：");
        FourthPanel.add(DefaultJVMMem, new GridConstraints(5, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer9 = new Spacer();
        FourthPanel.add(spacer9, new GridConstraints(0, 6, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer10 = new Spacer();
        FourthPanel.add(spacer10, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer11 = new Spacer();
        FourthPanel.add(spacer11, new GridConstraints(0, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer12 = new Spacer();
        FourthPanel.add(spacer12, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        JavaPath = new JLabel();
        Font JavaPathFont = this.$$$getFont$$$(null, -1, 16, JavaPath.getFont());
        if (JavaPathFont != null) JavaPath.setFont(JavaPathFont);
        JavaPath.setText("Java路径：");
        FourthPanel.add(JavaPath, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
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

    private static void CloseInformation() {
        if (PaintPicture.paintPicture != null && PaintPicture.paintPicture.sizeOperate != null)
            PaintPicture.paintPicture.sizeOperate.close();
        if (main != null) main.dispose();
        if (Main.main != null) Main.main.center.save();
    }

    //关闭
    public static void close() {
        if (Main.main.getTitle().contains("*")) {
            int choose = JOptionPane.showConfirmDialog(Main.main, "是否保存设置？", "关闭提示", JOptionPane.YES_NO_CANCEL_OPTION);
            if (choose == JOptionPane.YES_OPTION) {
                Main.main.centre.save();
                if (init.getProperties().get("EnableConfirmExit") != null && init.getProperties().get("EnableConfirmExit").toString().equalsIgnoreCase("false")) {
                    CloseInformation();
                    System.exit(0);
                }
            } else if (choose == JOptionPane.NO_OPTION) {
                CloseInformation();
                System.exit(0);
            } else if (choose == JOptionPane.CANCEL_OPTION || choose == JOptionPane.CLOSED_OPTION) {
                return;
            }
        }

        //加载配置文件
        init.Loading();
        if (init.getProperties().get("EnableConfirmExit") != null && init.getProperties().get("EnableConfirmExit").toString().equalsIgnoreCase("false")) {
            CloseInformation();
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
        jLabel1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
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
            CloseInformation();
            if (jCheckBox.isSelected()) {
                init.ChangeValue("EnableConfirmExit", "false");
                init.Update();
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
                JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
                String lastChooseDir = "";
                if (paintPicture != null && paintPicture.myCanvas != null) {
                    lastChooseDir = new File(paintPicture.myCanvas.getPath()).getParent();
                }
                fileChooser.setCurrentDirectory(new File(lastChooseDir));
                String picturePath;
                while (true) {
                    int returnValue = fileChooser.showOpenDialog(Main.main);
                    File chooseFile = fileChooser.getSelectedFile();
                    if (chooseFile == null) return;
                    picturePath = chooseFile.getPath();
                    if (returnValue == JFileChooser.APPROVE_OPTION) {
                        File file = checkFileOpen(false, new File(picturePath));
                        if (file == null) continue;
                        String filePath = file.getAbsolutePath();
                        if (filePath.endsWith("???")) {
                            tabbedPane1.setSelectedIndex(1);
                            break;
                        }
                        openPicture(filePath);
                    }
                    return;
                }
            }
        });
        FileChoosePane.add(openButton);
    }

    //检查文件打开
    private File checkFileOpen(boolean isMakeSure, File... files) {
        return checkFileOpen(new CheckFileIsRightPictureType(files), isMakeSure);
    }

    private File checkFileOpen(List<File> files, boolean isMakeSure) {
        return checkFileOpen(new CheckFileIsRightPictureType(files), isMakeSure);
    }

    private File checkFileOpen(CheckFileIsRightPictureType checkFileIsRightPictureType, boolean isMakeSure) {
        checkFileIsRightPictureType.statistics();
        if (checkFileIsRightPictureType.getNotImageCount() != 0) {
            JOptionPane.showMessageDialog(Main.main, "尚未支持打开此文件:\n\"" + checkFileIsRightPictureType.FilePathToString("\n", checkFileIsRightPictureType.getNotImageList()) + "\"", "Error", JOptionPane.ERROR_MESSAGE);
        }
        if (checkFileIsRightPictureType.getImageCount() == 0) return null;
        File choose;
        if (checkFileIsRightPictureType.getImageCount() == 1) {
            choose = checkFileIsRightPictureType.getImageList().getFirst();
            String choose_hashcode = GetImageInformation.getHashcode(choose);
            if (Main.main.paintPicture != null) {
                if (choose_hashcode == null && paintPicture.myCanvas.getPicture_hashcode() == null) {
                    System.out.println("Waring:Couldn't get current or opening picture hashcode,this will fake the judgment file path");
                    if (!new File(Main.main.paintPicture.myCanvas.getPath()).equals(choose)) return null;
                } else if (Objects.equals(choose_hashcode, paintPicture.myCanvas.getPicture_hashcode()))
                    return REPEAT_PICTURE_PATH_LOGOTYPE;
                if (isMakeSure && JOptionPane.showConfirmDialog(Main.main, "是否打开文件:\n\"" + choose.getPath() + "\"", "打开", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
                    return REPEAT_PICTURE_PATH_LOGOTYPE;
            }
        } else {
            choose = OpenImageChooser.openImageWithChoice(Main.main, checkFileIsRightPictureType.getImageList());
        }
        return choose;
    }


}
