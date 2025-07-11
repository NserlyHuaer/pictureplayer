package top.nserly;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import lombok.extern.slf4j.Slf4j;
import top.nserly.PicturePlayer.Loading.Bundle;
import top.nserly.PicturePlayer.Loading.Init;
import top.nserly.PicturePlayer.NComponent.Compoent.PaintPicturePanel;
import top.nserly.PicturePlayer.NComponent.Compoent.ThumbnailPreviewOfImage;
import top.nserly.PicturePlayer.NComponent.Frame.ConfirmUpdateDialog;
import top.nserly.PicturePlayer.NComponent.Frame.OpenImageChooser;
import top.nserly.PicturePlayer.NComponent.Frame.ProxyServerChooser;
import top.nserly.PicturePlayer.NComponent.Listener.ChangeFocusListener;
import top.nserly.PicturePlayer.Settings.SettingsInfoHandle;
import top.nserly.PicturePlayer.Size.SizeOperate;
import top.nserly.PicturePlayer.Utils.ImageManager.CheckFileIsRightPictureType;
import top.nserly.PicturePlayer.Utils.ImageManager.Info.GetImageInformation;
import top.nserly.PicturePlayer.Utils.Window.WindowLocation;
import top.nserly.PicturePlayer.Version.DownloadChecker.CheckAndDownloadUpdate;
import top.nserly.PicturePlayer.Version.PicturePlayerVersion;
import top.nserly.SoftwareCollections_API.Handler.Exception.ExceptionHandler;
import top.nserly.SoftwareCollections_API.OSInformation.SystemMonitor;

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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
public class GUIStarter extends JFrame {
    //初始化
    public static final Init<String, String> init;
    public static GUIStarter main;
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
    public JTextField textField1;
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
    private JPanel A;
    private JPanel B;
    private JLabel Display_1st;
    private JLabel Display_2nd;
    private JLabel Display_3rd;
    private JButton FreeUpMemory;
    private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private static ScheduledFuture<?> future;

    private final ChangeFocusListener changeFocusListener;
    //是否启用代理服务器
    private static boolean EnableProxyServer;
    //最新版本下载地址（如果当前是最新版本，则返回null值）
    private static List<String> NewVersionDownloadingWebSide;
    //更新网站（必须指定VersionID.sum下载地址）
    public static String UPDATE_WEBSITE = "https://gitee.com/nserly-huaer/ImagePlayer/raw/master/artifacts/PicturePlayer_jar/VersionID.sum";
    String MouseMoveLabelPrefix;
    String ProxyServerPrefix;
    public SettingsInfoHandle centre;
    private boolean IsFreshen;
    private static final File REPEAT_PICTURE_PATH_LOGOTYPE = new File("???");
    private static ProxyServerChooser proxyServerChooser;

    private final TreeMap<String, ThumbnailPreviewOfImage> thumbnailPreviewOfImages = new TreeMap<>();
    private final MouseAdapter mouseAdapter = new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                tabbedPane1.setSelectedIndex(0);
            }
        }
    };
    public PaintPicturePanel paintPicture;
    private final Thread init_PaintPicture = new Thread(() -> {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (ClassNotFoundException | IllegalAccessException | UnsupportedLookAndFeelException |
                 InstantiationException e) {
            log.error(ExceptionHandler.getExceptionMessage(e));
        }
        paintPicture = new PaintPicturePanel();
    });

    public static void main(String[] args) throws IOException, InterruptedException {
        main = new GUIStarter("Picture Player(Version:" + PicturePlayerVersion.getVersion() + ")");
        new Thread(() -> {
            for (String arg : args) {
                if (GetImageInformation.isImageFile(new File(arg))) {
                    main.openPicture(arg);
                    return;
                }
            }
        }).start();
        extractedSystemInfoToLog();
    }

    //打开图片
    public void openPicture(String path) {
        textField1.setText(path);
        new Thread(() -> {
            if (path == null || path.endsWith("???")) return;
            try {
                init_PaintPicture.join();
            } catch (InterruptedException ignored) {

            }
            if (paintPicture.imageCanvas == null || paintPicture.imageCanvas.getPath() == null) {
                tabbedPane1.setComponentAt(1, paintPicture);
                new DropTarget(paintPicture, DnDConstants.ACTION_COPY_OR_MOVE, dropTargetAdapter, true);
            }
            try {
                SwingUtilities.invokeAndWait(() -> {
                    paintPicture.setSize(tabbedPane1.getSize());
                    paintPicture.changePicturePath(path);
                    tabbedPane1.setSelectedIndex(1);
                });
            } catch (InterruptedException | InvocationTargetException e) {
                log.error(ExceptionHandler.getExceptionMessage(e));
            }
        }).start();
    }        //静态代码块

    static {
        //初始化Init
        init = new Init<>();
        init.setUpdate(true);
        ExceptionHandler.setUncaughtExceptionHandler(log);
        log.info("The software starts running...");
        System.setProperty("sun.java2d.opengl", "true");
    }

    private static Method $$$cachedGetBundleMethod$$$ = null;

    public GUIStarter(String title) {
        super(title);
        $$$setupUI$$$();
        init_PaintPicture.setPriority(Thread.MAX_PRIORITY);
        init_PaintPicture.start();
        new Thread(() -> {
            ExceptionHandler.setUncaughtExceptionHandler(log);
            setDefaultLookAndFeelDecorated(false);
            setContentPane(this.panel1);
            log.info("Start GUI");
            setVisible(true);
            Dimension dimension = SizeOperate.FreeOfScreenSize;
            setSize((int) (dimension.getWidth() * 0.5), (int) (dimension.getHeight() * 0.6));
            setLocation(WindowLocation.componentCenter(null, getWidth(), getHeight()));
            setMinimumSize(new Dimension(680, 335));
            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

            try {
                init_PaintPicture.join();
            } catch (InterruptedException e) {
                log.error(ExceptionHandler.getExceptionMessage(e));
            }
            paintPicture.pictureInformationStorageManagement.optimize();
        }).start();
        changeFocusListener = new ChangeFocusListener(this);
        init.run();
        new Thread(() -> {
            ExceptionHandler.setUncaughtExceptionHandler(log);
            ProxyServerPrefix = ProxyServerLabel.getText();
            MouseMoveLabelPrefix = MouseMoveOffsetsLabel.getText();
            centre = new SettingsInfoHandle();
            init();
            if (!GetImageInformation.isHardwareAccelerated) {
                centre.CurrentData.replace("EnableHardwareAcceleration", "false");
                EnableHardwareAccelerationCheckBox.setSelected(false);
                EnableHardwareAccelerationCheckBox.setEnabled(false);
                centre.save();
            }
            about();
            if (SettingsInfoHandle.getBoolean("EnableProxyServer", main.centre.CurrentData)) {
                setProxyServerOfInit();
                log.info("Proxy Server is turned on, and all networking activities of the Program will be handled by the proxy server");
                log.info("proxy server ip(or domain):{}", UPDATE_WEBSITE);
            }
            proxyServerChooser = new ProxyServerChooser();
            proxyServerChooser.pack();
            proxyServerChooser.setTitle(Bundle.getMessage("InputProxyServer_Title"));

            CheckAndDownloadUpdate.secureConnection(EnableSecureConnectionCheckBox.isSelected());
            if (init.containsKey("AutoCheckUpdate") && init.getProperties().get("AutoCheckUpdate").equals("true")) {
                CheckAndDownloadUpdate downloadUpdate = new CheckAndDownloadUpdate(UPDATE_WEBSITE);
                new Thread(() -> {
                    ExceptionHandler.setUncaughtExceptionHandler(log);
                    NewVersionDownloadingWebSide = downloadUpdate.getUpdateWebSide();
                    if (NewVersionDownloadingWebSide != null && !NewVersionDownloadingWebSide.isEmpty()) {
                        UpdateForm(downloadUpdate);
                    }
                }).start();
            }
        }).start();
        PaintPicturePanel.isEnableHardwareAcceleration = Boolean.parseBoolean(init.getProperties().getProperty("EnableHardwareAcceleration")) && GetImageInformation.isHardwareAccelerated;
    }    private final DropTargetAdapter dropTargetAdapter = new DropTargetAdapter() {
        public void drop(DropTargetDropEvent dtde) {
            try {
                dtde.acceptDrop(DnDConstants.ACTION_COPY);
                Transferable transferable = dtde.getTransferable();
                if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    Object obj = transferable.getTransferData(DataFlavor.javaFileListFlavor);
                    List<File> files = null;
                    if (!(obj instanceof List<?>)) {
                        throw new ClassCastException("Can't convert object:" + obj + "to object List<File>");
                    }
                    files = (List<File>) obj;
                    File file = checkFileOpen(files);
                    if (file != null) {
                        openPicture(file.getPath());
                    }
                }
            } catch (IOException | UnsupportedFlavorException e) {
                log.error(ExceptionHandler.getExceptionMessage(e));
            }
        }
    };

    private File checkFileOpen(CheckFileIsRightPictureType checkFileIsRightPictureType, boolean isMakeSure) {
        checkFileIsRightPictureType.statistics();
        if (checkFileIsRightPictureType.getNotImageCount() != 0) {
            JOptionPane.showMessageDialog(GUIStarter.main, Bundle.getMessage("OpenPictureError_Content") + "\n\"" + checkFileIsRightPictureType.filePathToString("\n", checkFileIsRightPictureType.getNotImageList()) + "\"", Bundle.getMessage("OpenPictureError_Title"), JOptionPane.ERROR_MESSAGE);
        }
        if (checkFileIsRightPictureType.getImageCount() == 0) return null;
        File choose;
        if (checkFileIsRightPictureType.getImageCount() == 1) {
            choose = checkFileIsRightPictureType.getImageList().getFirst();
            String choose_hashcode = GetImageInformation.getHashcode(choose);
            if (GUIStarter.main.paintPicture != null && GUIStarter.main.paintPicture.imageCanvas.getPath() != null) {
                if (choose_hashcode == null && paintPicture.imageCanvas.getPicture_hashcode() == null) {
                    log.warn("Couldn't get current or opening picture hashcode,this will fake the judgment file path");
                    if (!new File(GUIStarter.main.paintPicture.imageCanvas.getPath()).equals(choose)) return null;
                } else if (Objects.equals(choose_hashcode, paintPicture.imageCanvas.getPicture_hashcode()))
                    return REPEAT_PICTURE_PATH_LOGOTYPE;
                if (isMakeSure && JOptionPane.showConfirmDialog(GUIStarter.main, Bundle.getMessage("OpenPictureExactly_Content") + "\n\"" + choose.getPath() + "\"", Bundle.getMessage("OpenPictureExactly_Title"), JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
                    return REPEAT_PICTURE_PATH_LOGOTYPE;
            }
        } else {
            choose = OpenImageChooser.openImageWithChoice(GUIStarter.main, checkFileIsRightPictureType.getImageList());
        }
        return choose;
    }

    private static void closeInformation() {
        if (PaintPicturePanel.paintPicture != null && PaintPicturePanel.paintPicture.sizeOperate != null)
            PaintPicturePanel.paintPicture.sizeOperate.close();
        if (main != null) main.dispose();
    }

    private static void extractedSystemInfoToLog() {
        //获取操作系统版本
        log.info("OS:{}", SystemMonitor.OS_NAME);
        //获取系统语言
        log.info("System Language:{}", System.getProperty("user.language"));
        //获取java版本
        log.info("Java Runtime:{} {} ({})", System.getProperty("java.vm.name"), System.getProperty("java.runtime.version"), System.getProperty("sun.boot.library.path"));
        //获取软件版本
        log.info("Software Version:{}({})", PicturePlayerVersion.getVersion(), PicturePlayerVersion.getVersionID());
        //程序是否启用硬件加速
        log.info("Enable Hardware Acceleration:{}", PaintPicturePanel.isEnableHardwareAcceleration);
        if (!GetImageInformation.isHardwareAccelerated) {
            log.warn("Hardware acceleration is not supported, and the image will be rendered using software!");
        }
    }

    //关闭
    public static void close() {
        if (GUIStarter.main.getTitle().contains("*")) {
            int choose = JOptionPane.showConfirmDialog(GUIStarter.main, Bundle.getMessage("WindowCloseWithSettingsNotSave_Content"), Bundle.getMessage("WindowCloseWithSettingsNotSave_Title"), JOptionPane.YES_NO_CANCEL_OPTION);
            if (choose == JOptionPane.YES_OPTION) {
                log.info("Saving Settings...");
                GUIStarter.main.centre.save();
                closeInformation();
                log.info("Program Termination!");
                System.exit(0);
            } else if (choose == JOptionPane.NO_OPTION) {
                closeInformation();
                log.info("Program Termination!");
                System.exit(0);
            } else {
                return;
            }
        }

        //加载配置文件
        init.loading();
        if (init.getProperties().get("EnableConfirmExit") != null && init.getProperties().get("EnableConfirmExit").toString().equalsIgnoreCase("false")) {
            closeInformation();
            log.info("Program Termination!");
            System.exit(0);
        }
        //设置消息对话框面板
        var jDialog = new JDialog(main, true);
        //设置面板标题
        jDialog.setTitle(Bundle.getMessage("DefaultWindowClose_Title"));
        //设置面板大小（获取父面板坐标）
        jDialog.setSize(260, 170);
        jDialog.setLocation(WindowLocation.componentCenter(main, jDialog.getWidth(), jDialog.getHeight()));
        //创建文字
        var jLabel1 = new JLabel(Bundle.getMessage("DefaultWindowClose_Content"));
        //设置文字字体、格式
        jLabel1.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        //设置显示大小、坐标
        jLabel1.setBounds(15, 3, 290, 50);
        //创建按钮
        var yes = new JButton(Bundle.getMessage("DefaultWindowClose_EXIT"));
        var no = new JButton(Bundle.getMessage("DefaultWindowClose_Cancel"));
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
        JCheckBox jCheckBox = new JCheckBox(Bundle.getMessage("DefaultWindowClose_Under"));
        jCheckBox.setBounds(60, 95, 200, 25);
        jCheckBox.addMouseListener(changeFocusListener);
        jDialog.add(jCheckBox);
        //如果确定退出软件，运行退出程序
        yes.addActionListener(e1 -> {
            //关闭面板
            main.dispose();
            closeInformation();
            if (jCheckBox.isSelected()) {
                init.changeValue("EnableConfirmExit", "false");
                init.update();
            }
            log.info("Program Termination!");
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
                    if (PaintPicturePanel.paintPicture != null && PaintPicturePanel.paintPicture.sizeOperate != null)
                        PaintPicturePanel.paintPicture.sizeOperate.close();
                    if (main != null) main.dispose();
                    if (jCheckBox.isSelected()) {
                        init.changeValue("EnableConfirmExit", "false");
                        init.update();
                    }
                    log.info("Program Termination!");
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

    //初始化所有组件设置
    private void init() {
        VersionView.setText(VersionView.getText() + PicturePlayerVersion.getShorterVersion());
        TurnButton.addMouseListener(changeFocusListener);
        TurnButton.addActionListener(e -> {
            String path = textField1.getText().trim();
            //如果字符串前缀与后缀包含"，则去除其中的"
            if (path.startsWith("\"") && path.endsWith("\"")) {
                path = path.substring(1, path.length() - 1);
            }
            File file = new File(path);
            if (GetImageInformation.isImageFile(file)) {
                openPicture(path);
                return;
            }

            JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
            fileChooser.setCurrentDirectory(file);
            String picturePath;
            while (true) {
                int returnValue = fileChooser.showOpenDialog(GUIStarter.main);
                File chooseFile = fileChooser.getSelectedFile();
                if (chooseFile == null) return;
                picturePath = chooseFile.getPath();
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    file = checkFileOpen(new File(picturePath));
                    if (file == null) continue;
                    String filePath = file.getAbsolutePath();
                    if (filePath.endsWith("???")) {
                        tabbedPane1.setSelectedIndex(1);
                        break;
                    }
                    openPicture(filePath);
                    paintPicture.sizeOperate.setPercent(paintPicture.sizeOperate.getPictureOptimalSize());
                }
                return;
            }
        });
        SecondPanel.addMouseListener(mouseAdapter);
        // 设置SecondPanel为可接受拖放
        new DropTarget(SecondPanel, DnDConstants.ACTION_COPY_OR_MOVE, dropTargetAdapter, true);
        settings();
        SaveButton.addActionListener(e -> {
            log.info("Saving Settings...");
            centre.save();
            settingRevised(false);
        });
        ResetButton.addActionListener(e -> {
            centre.setDefault();
            reFresh();
            settingRevised(true);
        });
        RefreshButton.addActionListener(e -> {
            centre.reFresh();
            reFresh();
            settingRevised(false);
        });
        ProxyServerButton.addActionListener(e -> {
            proxyServerChooser.setVisible(true);
        });
        new Thread(() -> {
            ExceptionHandler.setUncaughtExceptionHandler(log);
            JavaPath.setText(JavaPath.getText() + System.getProperty("sun.boot.library.path"));
            DefaultJVMMem.setText(DefaultJVMMem.getText() + SystemMonitor.convertSize(SystemMonitor.JVM_Initialize_Memory));
            JVMVersionLabel.setText(JVMVersionLabel.getText() + System.getProperty("java.runtime.version"));
            ProgramStartTime.setText(ProgramStartTime.getText() + SystemMonitor.PROGRAM_START_TIME);
            CurrentSoftwareVersionLabel.setText(CurrentSoftwareVersionLabel.getText() + PicturePlayerVersion.getVersion());
            CurrentSoftwareInteriorLabel.setText(CurrentSoftwareInteriorLabel.getText() + PicturePlayerVersion.getVersionID());
            OSLabel.setText(OSLabel.getText() + SystemMonitor.OS_NAME);
            CPUName.setText(CPUName.getText() + SystemMonitor.CPU_NAME);
            CurrentSoftwareLanguage.setText(CurrentSoftwareLanguage.getText() + System.getProperty("user.language"));
            if (EnableProxyServer)
                CheckVersionButton.setText(CheckVersionButton.getText() + Bundle.getMessage("IsEnableProxyServer"));
            final String JmemI = MemUsed.getText();
            final String TTI = TotalThread.getText();
            tabbedPane1.addChangeListener(e -> {
                request();
                if (tabbedPane1.getSelectedIndex() == 3) {
                    future = executor.scheduleAtFixedRate(() -> {
                        SystemMonitor.getInformation();
                        MemUsed.setText(JmemI + SystemMonitor.convertSize(SystemMonitor.JVM_Used_Memory) + "/" + SystemMonitor.convertSize(SystemMonitor.JVM_Maximum_Free_Memory) + "(" + SystemMonitor.JVM_Memory_Usage + "%" + ")");
                        TotalThread.setText(TTI + SystemMonitor.Program_Thread_Count);
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
                request();

            }

            @Override
            public void windowClosing(WindowEvent e) {
                close();
            }
        });
    }

    //代理服务器设置
    private static void setProxyServerOfInit() {
        String website = init.getProperties().getProperty("ProxyServer");
        if (!website.startsWith("http")) {
            website = "http://" + website;
        }
        if (!website.endsWith(".sum")) {
            website = website.trim();
            if (website.endsWith("/")) {
                website += "VersionID.sum";
            } else {
                website += "/VersionID.sum";
            }
        }
        UPDATE_WEBSITE = website;
        EnableProxyServer = true;
    }

    //更新界面
    public static void UpdateForm(CheckAndDownloadUpdate downloadUpdate) {
        ConfirmUpdateDialog confirmUpdateDialog = new ConfirmUpdateDialog(downloadUpdate);
        confirmUpdateDialog.setVisible(true);
    }

    //获取焦点
    private void request() {
        //当选项界面切换时
        if (tabbedPane1.getSelectedIndex() == 0) {
            //让路径输入框获取焦点
            textField1.requestFocusInWindow();
        } else if (tabbedPane1.getSelectedIndex() == 1) {
            //让图片渲染器获取焦点
            if (paintPicture != null && paintPicture.imageCanvas != null) {
                paintPicture.imageCanvas.requestFocusInWindow();
            }
        } else if (tabbedPane1.getSelectedIndex() == 2) {
            //让窗体获取焦点
            tabbedPane1.requestFocusInWindow();
        } else {
            //让窗体获取焦点
            requestFocusInWindow();
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
        panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.setRequestFocusEnabled(true);
        tabbedPane1 = new JTabbedPane();
        tabbedPane1.setRequestFocusEnabled(false);
        tabbedPane1.setToolTipText("");
        panel1.add(tabbedPane1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        FirstPanel = new JPanel();
        FirstPanel.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        FirstPanel.setName("");
        FirstPanel.setToolTipText("");
        tabbedPane1.addTab(this.$$$getMessageFromBundle$$$("messages", "FirstPanel"), FirstPanel);
        VersionView = new JLabel();
        VersionView.setRequestFocusEnabled(false);
        VersionView.setText("Version:");
        FirstPanel.add(VersionView, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_SOUTHEAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        textField1 = new JTextField();
        FirstPanel.add(textField1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        TurnButton = new JButton();
        TurnButton.setRequestFocusEnabled(false);
        this.$$$loadButtonText$$$(TurnButton, this.$$$getMessageFromBundle$$$("messages", "TurnButton"));
        FirstPanel.add(TurnButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        FirstPanel.add(scrollPane1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        FileChoosePane = new JPanel();
        FileChoosePane.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        scrollPane1.setViewportView(FileChoosePane);
        SecondPanel = new JPanel();
        SecondPanel.setLayout(new GridBagLayout());
        SecondPanel.setBackground(new Color(-1643536));
        SecondPanel.setEnabled(true);
        SecondPanel.setForeground(new Color(-1));
        tabbedPane1.addTab(this.$$$getMessageFromBundle$$$("messages", "SecondPanel"), SecondPanel);
        Display_1st = new JLabel();
        Font Display_1stFont = this.$$$getFont$$$(null, -1, 35, Display_1st.getFont());
        if (Display_1stFont != null) Display_1st.setFont(Display_1stFont);
        Display_1st.setHorizontalTextPosition(11);
        this.$$$loadLabelText$$$(Display_1st, this.$$$getMessageFromBundle$$$("messages", "Display_1st"));
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        SecondPanel.add(Display_1st, gbc);
        Display_2nd = new JLabel();
        Font Display_2ndFont = this.$$$getFont$$$(null, -1, 20, Display_2nd.getFont());
        if (Display_2ndFont != null) Display_2nd.setFont(Display_2ndFont);
        this.$$$loadLabelText$$$(Display_2nd, this.$$$getMessageFromBundle$$$("messages", "Display_2nd"));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        SecondPanel.add(Display_2nd, gbc);
        Display_3rd = new JLabel();
        Display_3rd.setBackground(new Color(-2104859));
        Font Display_3rdFont = this.$$$getFont$$$(null, -1, 15, Display_3rd.getFont());
        if (Display_3rdFont != null) Display_3rd.setFont(Display_3rdFont);
        Display_3rd.setHorizontalAlignment(0);
        Display_3rd.setHorizontalTextPosition(0);
        this.$$$loadLabelText$$$(Display_3rd, this.$$$getMessageFromBundle$$$("messages", "Display_3rd"));
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 5;
        SecondPanel.add(Display_3rd, gbc);
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
        ThirdPanel.setLayout(new GridLayoutManager(2, 3, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane1.addTab(this.$$$getMessageFromBundle$$$("messages", "ThirdPanel"), ThirdPanel);
        A = new JPanel();
        A.setLayout(new GridLayoutManager(2, 3, new Insets(0, 0, 0, 0), -1, -1));
        ThirdPanel.add(A, new GridConstraints(1, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final Spacer spacer6 = new Spacer();
        A.add(spacer6, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        ResetButton = new JButton();
        ResetButton.setRequestFocusEnabled(false);
        this.$$$loadButtonText$$$(ResetButton, this.$$$getMessageFromBundle$$$("messages", "ResetButton"));
        A.add(ResetButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_SOUTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        RefreshButton = new JButton();
        RefreshButton.setRequestFocusEnabled(false);
        this.$$$loadButtonText$$$(RefreshButton, this.$$$getMessageFromBundle$$$("messages", "RefreshButton"));
        A.add(RefreshButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_SOUTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        SaveButton = new JButton();
        SaveButton.setHorizontalAlignment(0);
        SaveButton.setRequestFocusEnabled(false);
        this.$$$loadButtonText$$$(SaveButton, this.$$$getMessageFromBundle$$$("messages", "SaveButton"));
        SaveButton.setVerticalTextPosition(0);
        A.add(SaveButton, new GridConstraints(1, 0, 1, 3, GridConstraints.ANCHOR_SOUTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        B = new JPanel();
        B.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        ThirdPanel.add(B, new GridConstraints(0, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, 1, 1, null, null, null, 0, false));
        final JScrollPane scrollPane2 = new JScrollPane();
        B.add(scrollPane2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(502, 372), null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(10, 2, new Insets(0, 0, 0, 0), -1, -1));
        scrollPane2.setViewportView(panel2);
        EnableConfirmExitCheckBox = new JCheckBox();
        EnableConfirmExitCheckBox.setRequestFocusEnabled(false);
        this.$$$loadButtonText$$$(EnableConfirmExitCheckBox, this.$$$getMessageFromBundle$$$("messages", "EnableConfirmExit"));
        panel2.add(EnableConfirmExitCheckBox, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(402, 25), null, 0, false));
        EnableHistoryLoaderCheckBox = new JCheckBox();
        EnableHistoryLoaderCheckBox.setRequestFocusEnabled(false);
        this.$$$loadButtonText$$$(EnableHistoryLoaderCheckBox, this.$$$getMessageFromBundle$$$("messages", "EnableHistoryLoader"));
        panel2.add(EnableHistoryLoaderCheckBox, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(402, 25), null, 0, false));
        EnableHardwareAccelerationCheckBox = new JCheckBox();
        EnableHardwareAccelerationCheckBox.setRequestFocusEnabled(false);
        this.$$$loadButtonText$$$(EnableHardwareAccelerationCheckBox, this.$$$getMessageFromBundle$$$("messages", "EnableHardwareAcceleration"));
        panel2.add(EnableHardwareAccelerationCheckBox, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(402, 25), null, 0, false));
        EnableCursorDisplayCheckBox = new JCheckBox();
        EnableCursorDisplayCheckBox.setRequestFocusEnabled(false);
        this.$$$loadButtonText$$$(EnableCursorDisplayCheckBox, this.$$$getMessageFromBundle$$$("messages", "EnableCursorDisplayCheckBox"));
        panel2.add(EnableCursorDisplayCheckBox, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(402, 25), null, 0, false));
        MouseMoveOffsetsLabel = new JLabel();
        MouseMoveOffsetsLabel.setRequestFocusEnabled(false);
        this.$$$loadLabelText$$$(MouseMoveOffsetsLabel, this.$$$getMessageFromBundle$$$("messages", "MouseMoveOffsetsLabel"));
        panel2.add(MouseMoveOffsetsLabel, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(402, 17), null, 0, false));
        MouseMoveOffsetsSlider = new JSlider();
        MouseMoveOffsetsSlider.setMaximum(150);
        MouseMoveOffsetsSlider.setMinimum(-65);
        MouseMoveOffsetsSlider.setRequestFocusEnabled(false);
        panel2.add(MouseMoveOffsetsSlider, new GridConstraints(5, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        EnableProxyServerCheckBox = new JCheckBox();
        EnableProxyServerCheckBox.setRequestFocusEnabled(false);
        this.$$$loadButtonText$$$(EnableProxyServerCheckBox, this.$$$getMessageFromBundle$$$("messages", "EnableProxyServerCheckBox"));
        panel2.add(EnableProxyServerCheckBox, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(402, 25), null, 0, false));
        ProxyServerLabel = new JLabel();
        ProxyServerLabel.setRequestFocusEnabled(false);
        this.$$$loadLabelText$$$(ProxyServerLabel, this.$$$getMessageFromBundle$$$("messages", "ProxyServerLabel"));
        panel2.add(ProxyServerLabel, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(402, 17), null, 0, false));
        EnableSecureConnectionCheckBox = new JCheckBox();
        EnableSecureConnectionCheckBox.setRequestFocusEnabled(false);
        this.$$$loadButtonText$$$(EnableSecureConnectionCheckBox, this.$$$getMessageFromBundle$$$("messages", "EnableSecureConnectionCheckBox"));
        panel2.add(EnableSecureConnectionCheckBox, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(402, 25), null, 0, false));
        AutoCheckUpdateCheckBox = new JCheckBox();
        AutoCheckUpdateCheckBox.setRequestFocusEnabled(false);
        this.$$$loadButtonText$$$(AutoCheckUpdateCheckBox, this.$$$getMessageFromBundle$$$("messages", "AutoCheckUpdateCheckBox"));
        panel2.add(AutoCheckUpdateCheckBox, new GridConstraints(9, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(402, 25), null, 0, false));
        ProxyServerButton = new JButton();
        ProxyServerButton.setRequestFocusEnabled(false);
        this.$$$loadButtonText$$$(ProxyServerButton, this.$$$getMessageFromBundle$$$("messages", "ProxyServerButton"));
        panel2.add(ProxyServerButton, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane3 = new JScrollPane();
        tabbedPane1.addTab(this.$$$getMessageFromBundle$$$("messages", "FourthPanel"), scrollPane3);
        FourthPanel = new JPanel();
        FourthPanel.setLayout(new GridLayoutManager(11, 7, new Insets(0, 0, 0, 0), -1, -1));
        scrollPane3.setViewportView(FourthPanel);
        JVMVersionLabel = new JLabel();
        Font JVMVersionLabelFont = this.$$$getFont$$$(null, -1, 16, JVMVersionLabel.getFont());
        if (JVMVersionLabelFont != null) JVMVersionLabel.setFont(JVMVersionLabelFont);
        this.$$$loadLabelText$$$(JVMVersionLabel, this.$$$getMessageFromBundle$$$("messages", "JVMVersionLabel"));
        FourthPanel.add(JVMVersionLabel, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer7 = new Spacer();
        FourthPanel.add(spacer7, new GridConstraints(10, 0, 1, 7, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        CurrentSoftwareVersionLabel = new JLabel();
        Font CurrentSoftwareVersionLabelFont = this.$$$getFont$$$(null, -1, 16, CurrentSoftwareVersionLabel.getFont());
        if (CurrentSoftwareVersionLabelFont != null)
            CurrentSoftwareVersionLabel.setFont(CurrentSoftwareVersionLabelFont);
        this.$$$loadLabelText$$$(CurrentSoftwareVersionLabel, this.$$$getMessageFromBundle$$$("messages", "CurrentSoftwareVersionLabel"));
        FourthPanel.add(CurrentSoftwareVersionLabel, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        CurrentSoftwareInteriorLabel = new JLabel();
        Font CurrentSoftwareInteriorLabelFont = this.$$$getFont$$$(null, -1, 16, CurrentSoftwareInteriorLabel.getFont());
        if (CurrentSoftwareInteriorLabelFont != null)
            CurrentSoftwareInteriorLabel.setFont(CurrentSoftwareInteriorLabelFont);
        this.$$$loadLabelText$$$(CurrentSoftwareInteriorLabel, this.$$$getMessageFromBundle$$$("messages", "CurrentSoftwareInteriorLabel"));
        FourthPanel.add(CurrentSoftwareInteriorLabel, new GridConstraints(7, 2, 1, 5, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        CheckVersionButton = new JButton();
        Font CheckVersionButtonFont = this.$$$getFont$$$(null, -1, 16, CheckVersionButton.getFont());
        if (CheckVersionButtonFont != null) CheckVersionButton.setFont(CheckVersionButtonFont);
        CheckVersionButton.setRequestFocusEnabled(false);
        CheckVersionButton.setRolloverEnabled(false);
        this.$$$loadButtonText$$$(CheckVersionButton, this.$$$getMessageFromBundle$$$("messages", "CheckVersionButton"));
        FourthPanel.add(CheckVersionButton, new GridConstraints(8, 0, 1, 7, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        OSLabel = new JLabel();
        Font OSLabelFont = this.$$$getFont$$$(null, -1, 16, OSLabel.getFont());
        if (OSLabelFont != null) OSLabel.setFont(OSLabelFont);
        this.$$$loadLabelText$$$(OSLabel, this.$$$getMessageFromBundle$$$("messages", "OSLabel"));
        FourthPanel.add(OSLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        ProgramStartTime = new JLabel();
        Font ProgramStartTimeFont = this.$$$getFont$$$(null, -1, 16, ProgramStartTime.getFont());
        if (ProgramStartTimeFont != null) ProgramStartTime.setFont(ProgramStartTimeFont);
        this.$$$loadLabelText$$$(ProgramStartTime, this.$$$getMessageFromBundle$$$("messages", "ProgramStartTime"));
        FourthPanel.add(ProgramStartTime, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        CurrentSoftwareLanguage = new JLabel();
        CurrentSoftwareLanguage.setEnabled(true);
        Font CurrentSoftwareLanguageFont = this.$$$getFont$$$(null, -1, 16, CurrentSoftwareLanguage.getFont());
        if (CurrentSoftwareLanguageFont != null) CurrentSoftwareLanguage.setFont(CurrentSoftwareLanguageFont);
        this.$$$loadLabelText$$$(CurrentSoftwareLanguage, this.$$$getMessageFromBundle$$$("messages", "CurrentSoftwareLanguage"));
        FourthPanel.add(CurrentSoftwareLanguage, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        CPUName = new JLabel();
        Font CPUNameFont = this.$$$getFont$$$(null, -1, 16, CPUName.getFont());
        if (CPUNameFont != null) CPUName.setFont(CPUNameFont);
        this.$$$loadLabelText$$$(CPUName, this.$$$getMessageFromBundle$$$("messages", "CPULabel"));
        FourthPanel.add(CPUName, new GridConstraints(1, 0, 1, 7, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        TotalThread = new JLabel();
        Font TotalThreadFont = this.$$$getFont$$$(null, -1, 16, TotalThread.getFont());
        if (TotalThreadFont != null) TotalThread.setFont(TotalThreadFont);
        this.$$$loadLabelText$$$(TotalThread, this.$$$getMessageFromBundle$$$("messages", "TotalThread"));
        FourthPanel.add(TotalThread, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        MemUsed = new JLabel();
        Font MemUsedFont = this.$$$getFont$$$(null, -1, 16, MemUsed.getFont());
        if (MemUsedFont != null) MemUsed.setFont(MemUsedFont);
        this.$$$loadLabelText$$$(MemUsed, this.$$$getMessageFromBundle$$$("messages", "MemUsed"));
        FourthPanel.add(MemUsed, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        DefaultJVMMem = new JLabel();
        Font DefaultJVMMemFont = this.$$$getFont$$$(null, -1, 16, DefaultJVMMem.getFont());
        if (DefaultJVMMemFont != null) DefaultJVMMem.setFont(DefaultJVMMemFont);
        this.$$$loadLabelText$$$(DefaultJVMMem, this.$$$getMessageFromBundle$$$("messages", "DefaultJVMMem"));
        FourthPanel.add(DefaultJVMMem, new GridConstraints(5, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer8 = new Spacer();
        FourthPanel.add(spacer8, new GridConstraints(0, 6, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer9 = new Spacer();
        FourthPanel.add(spacer9, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer10 = new Spacer();
        FourthPanel.add(spacer10, new GridConstraints(0, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer11 = new Spacer();
        FourthPanel.add(spacer11, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        JavaPath = new JLabel();
        Font JavaPathFont = this.$$$getFont$$$(null, -1, 16, JavaPath.getFont());
        if (JavaPathFont != null) JavaPath.setFont(JavaPathFont);
        this.$$$loadLabelText$$$(JavaPath, this.$$$getMessageFromBundle$$$("messages", "JavaPath"));
        FourthPanel.add(JavaPath, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        FreeUpMemory = new JButton();
        FreeUpMemory.setActionCommand(this.$$$getMessageFromBundle$$$("messages", "FreeUpMemory"));
        Font FreeUpMemoryFont = this.$$$getFont$$$(null, -1, 16, FreeUpMemory.getFont());
        if (FreeUpMemoryFont != null) FreeUpMemory.setFont(FreeUpMemoryFont);
        FreeUpMemory.setRequestFocusEnabled(false);
        FreeUpMemory.setRolloverEnabled(false);
        FreeUpMemory.setText("释放内存");
        FourthPanel.add(FreeUpMemory, new GridConstraints(9, 0, 1, 7, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        TopLabel = new JLabel();
        Font TopLabelFont = this.$$$getFont$$$(null, -1, 20, TopLabel.getFont());
        if (TopLabelFont != null) TopLabel.setFont(TopLabelFont);
        TopLabel.setHorizontalTextPosition(0);
        TopLabel.setText("Picture Player by nserly");
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
    private void $$$loadLabelText$$$(JLabel component, String text) {
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
            component.setDisplayedMnemonic(mnemonic);
            component.setDisplayedMnemonicIndex(mnemonicIndex);
        }
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
        return panel1;
    }


    //设置界面
    private void settings() {
        reFresh();
        EnableConfirmExitCheckBox.addActionListener(e -> {
            centre.CurrentData.replace("EnableConfirmExit", String.valueOf(EnableConfirmExitCheckBox.isSelected()));
            settingRevised(true);
        });
        EnableHistoryLoaderCheckBox.addActionListener(e -> {
            centre.CurrentData.replace("EnableHistoryLoader", String.valueOf(EnableHistoryLoaderCheckBox.isSelected()));
            settingRevised(true);
        });
        EnableCursorDisplayCheckBox.addActionListener(e -> {
            centre.CurrentData.replace("EnableCursorDisplay", String.valueOf(EnableCursorDisplayCheckBox.isSelected()));
            settingRevised(true);
        });
        MouseMoveOffsetsSlider.addChangeListener(e -> {
            centre.CurrentData.replace("MouseMoveOffsets", String.valueOf(MouseMoveOffsetsSlider.getValue()));
            StringBuilder stringBuffer1 = new StringBuilder(MouseMoveLabelPrefix);
            stringBuffer1.insert(MouseMoveLabelPrefix.indexOf(":") + 1, MouseMoveOffsetsSlider.getValue() + "% ");
            MouseMoveOffsetsLabel.setText(stringBuffer1.toString());
            settingRevised(true);
        });
        EnableProxyServerCheckBox.addActionListener(e -> {
            centre.CurrentData.replace("EnableProxyServer", String.valueOf(EnableProxyServerCheckBox.isSelected()));
            ProxyServerButton.setVisible(EnableProxyServerCheckBox.isSelected());
            ProxyServerLabel.setVisible(EnableProxyServerCheckBox.isSelected());
            settingRevised(true);
        });
        EnableHardwareAccelerationCheckBox.addActionListener(e -> {
            centre.CurrentData.replace("EnableHardwareAcceleration", String.valueOf(EnableHardwareAccelerationCheckBox.isSelected()));
            ProxyServerButton.setEnabled(EnableHardwareAccelerationCheckBox.isSelected());
            settingRevised(true);
        });
        EnableSecureConnectionCheckBox.addActionListener(e -> {
            if (!EnableSecureConnectionCheckBox.isSelected()) {
                EnableSecureConnectionCheckBox.setSelected(true);
                int choose = JOptionPane.showConfirmDialog(GUIStarter.main, Bundle.getMessage("ConfirmCloseSecureConnection_Content_1Line") + "\n" + Bundle.getMessage("ConfirmCloseSecureConnection_Content_2Line"), Bundle.getMessage("ConfirmCloseSecureConnection_Title"), JOptionPane.YES_NO_OPTION);
                if (choose != 0) {
                    return;
                }
                EnableSecureConnectionCheckBox.setSelected(false);
            }
            centre.CurrentData.replace("EnableSecureConnection", String.valueOf(EnableSecureConnectionCheckBox.isSelected()));
            settingRevised(true);
        });
        AutoCheckUpdateCheckBox.addActionListener(e -> {
            centre.CurrentData.replace("AutoCheckUpdate", String.valueOf(AutoCheckUpdateCheckBox.isSelected()));
            settingRevised(true);
        });
    }

    //关于界面设置
    private void about() {
        CheckAndDownloadUpdate downloadUpdate = new CheckAndDownloadUpdate(UPDATE_WEBSITE);
        CheckVersionButton.addActionListener(e -> {
            downloadUpdate.setWebSide(UPDATE_WEBSITE);
            try {
                if (!downloadUpdate.checkIfTheLatestVersion()) {
                    int choice = JOptionPane.showConfirmDialog(GUIStarter.main, Bundle.getMessage("NoAnyUpdate_Content_First") + "\n" + Bundle.getMessage("NoAnyUpdate_Content_Second"), Bundle.getMessage("NoAnyUpdate_Title"), JOptionPane.YES_NO_OPTION);
                    if (choice != JOptionPane.YES_OPTION) {
                        return;
                    }
                    downloadUpdate.ForceToGetUpdates();
                }
            } catch (IOException e1) {
                log.error(ExceptionHandler.getExceptionMessage(e1));
                JOptionPane.showMessageDialog(GUIStarter.main, "Error: " + e1 + "\n" + Bundle.getMessage("CantGetUpdate_Content"), Bundle.getMessage("CantGetUpdate_Title"), JOptionPane.ERROR_MESSAGE);
                return;
            }
            new Thread(() -> {
                ExceptionHandler.setUncaughtExceptionHandler(log);
                ConfirmUpdateDialog confirmUpdateDialog = new ConfirmUpdateDialog(downloadUpdate);
                confirmUpdateDialog.pack();
                confirmUpdateDialog.setVisible(true);
            }).start();
        });

        FreeUpMemory.addActionListener(e -> {
            System.gc();
            JOptionPane.showConfirmDialog(GUIStarter.main, Bundle.getMessage("FreeUpMemory_Content"), Bundle.getMessage("FreeUpMemory_Title"), JOptionPane.DEFAULT_OPTION);
        });

    }

    private void reFresh() {
        EnableHardwareAccelerationCheckBox.setSelected(SettingsInfoHandle.getBoolean("EnableHardwareAcceleration", centre.CurrentData));
        EnableConfirmExitCheckBox.setSelected(SettingsInfoHandle.getBoolean("EnableConfirmExit", centre.CurrentData));
        EnableHistoryLoaderCheckBox.setSelected(SettingsInfoHandle.getBoolean("EnableHistoryLoader", centre.CurrentData));
        EnableCursorDisplayCheckBox.setSelected(SettingsInfoHandle.getBoolean("EnableCursorDisplay", centre.CurrentData));
        MouseMoveOffsetsSlider.setValue((int) SettingsInfoHandle.getDouble("MouseMoveOffsets", centre.CurrentData));
        StringBuilder stringBuffer = new StringBuilder(MouseMoveLabelPrefix);
        stringBuffer.insert(MouseMoveLabelPrefix.indexOf(":") + 1, MouseMoveOffsetsSlider.getValue() + "% ");
        MouseMoveOffsetsLabel.setText(stringBuffer.toString());
        EnableProxyServerCheckBox.setSelected(SettingsInfoHandle.getBoolean("EnableProxyServer", centre.CurrentData));
        ProxyServerLabel.setText(ProxyServerPrefix + centre.CurrentData.get("ProxyServer"));
        EnableSecureConnectionCheckBox.setSelected(SettingsInfoHandle.getBoolean("EnableSecureConnection", centre.CurrentData));
        AutoCheckUpdateCheckBox.setSelected(SettingsInfoHandle.getBoolean("AutoCheckUpdate", centre.CurrentData));
        ProxyServerButton.setVisible(EnableProxyServerCheckBox.isSelected());
        ProxyServerLabel.setVisible(EnableProxyServerCheckBox.isSelected());
    }

    //设置修改
    private void settingRevised(boolean a) {
        if (a && !getTitle().contains("*")) {
            setTitle(getTitle() + "*");
        } else if ((!a) && getTitle().contains("*")) {
            setTitle(getTitle().substring(0, getTitle().lastIndexOf("*")));
        }
    }

    //代理服务器更改
    public void setProxyServerOfInit(String ProxyServerAddress) {
        if (ProxyServerAddress == null || ProxyServerAddress.trim().isEmpty()) return;
        ProxyServerAddress = ProxyServerAddress.trim();
        if (ProxyServerAddress.equals(centre.CurrentData.get("ProxyServer"))) return;
        if (!ProxyServerAddress.equals("proxy server address") && !ProxyServerAddress.isEmpty()) {
            init.changeValue("ProxyServer", ProxyServerAddress);
            setProxyServerOfInit();
            log.info("To enable a new proxy server:{}", ProxyServerAddress);
            EnableProxyServerCheckBox.setSelected(true);
            ProxyServerButton.setVisible(true);
            ProxyServerLabel.setVisible(true);
            centre.CurrentData.replace("ProxyServer", ProxyServerAddress);
            ProxyServerLabel.setText(Bundle.getMessage("ProxyServerLabel") + centre.CurrentData.get("ProxyServer"));
            JOptionPane.showConfirmDialog(GUIStarter.main, Bundle.getMessage("ProxyServerWasModified_Content"), Bundle.getMessage("ProxyServerWasModified_Title"), JOptionPane.YES_NO_OPTION);
            settingRevised(true);
        }
    }

    //检查文件打开
    private File checkFileOpen(File... files) {
        return checkFileOpen(new CheckFileIsRightPictureType(files), false);
    }

    public void reviewPictureList(ArrayList<String> picturePath) {
        ArrayList<String> cached = (ArrayList<String>) picturePath.clone();
        //移走所有已在列表中的图片路径
        cached.removeAll(thumbnailPreviewOfImages.keySet());

        ArrayList<String> removed = new ArrayList<>();

        //移走所有当前列表中的图片路径不在当前显示列表
        for (String currentBufferedPicturePath : thumbnailPreviewOfImages.keySet()) {
            if (!picturePath.contains(currentBufferedPicturePath)) {
                FileChoosePane.remove(thumbnailPreviewOfImages.get(currentBufferedPicturePath));
                removed.add(currentBufferedPicturePath);
            }
        }
        removed.forEach(thumbnailPreviewOfImages::remove);

        //创建当前显示列表没有的图片
        for (String path : cached) {
            ThumbnailPreviewOfImage thumbnailPreviewOfImage = null;
            try {
                thumbnailPreviewOfImage = new ThumbnailPreviewOfImage(path);
            } catch (IOException e) {
                log.error(ExceptionHandler.getExceptionMessage(e));
                picturePath.remove(path);
                continue;
            }
            FileChoosePane.add(thumbnailPreviewOfImage);
            thumbnailPreviewOfImages.put(path, thumbnailPreviewOfImage);
        }
        FileChoosePane.revalidate();
        FileChoosePane.repaint();
    }


    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    private File checkFileOpen(List<File> files) {
        return checkFileOpen(new CheckFileIsRightPictureType(files), true);
    }


}
