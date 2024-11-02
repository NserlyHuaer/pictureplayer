package Runner;//导入包

import Component.PercentLabel;
import Listener.ChangeFocusListener;
import Loading.Init;
import Size.OperatingCoordinate;
import Tools.EqualsProportion;
import Tools.ImageManager.GetImageInformation;
import Tools.ImageManager.ImageRotationHelper;
import Version.Download.DownloadUpdate;
import Version.Version;
import Component.FileManagementFrame;
import Component.AdvancedDownloadSpeedDisplay;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

//创建类
public class Main extends JFrame {
    //文件打开界面
    private static JDialog jFrame;
    //图片打开窗体
    public static Main main;
    //标题风格
    public static final String titleStyle;
    //初始化
    private static final Init init;
    //路径输入框
    private static JTextField jTextField;
    //图片放大按钮
    public JButton biggest;
    //图片缩小按钮
    public JButton smallest;
    //缩放比例标签
    PercentLabel percentLabel;
    //创建鼠标坐标管理对象
    OperatingCoordinate op = null;
    //图片比例管理
    public SizeOperate sizeOperate;
    //图片渲染管理
    private MyCanvas myCanvas;
    //全屏模式按钮
    private JButton FullScreen;
    //上次是否处于全屏
    private static boolean isLastInFullScreen;
    //设备是否支持全屏模式
    private boolean SupportFullScreen = true;
    //鼠标移动补偿
    private static double MouseMoveOffsets = 0.0;
    //判断按钮是否被按下
    private static boolean IsDragging;
    //最新版本下载地址（如果当前是最新版本，则返回null值）
    private static List<String> NewVersionDownloadingWebSide;
    //更新界面窗口
    public static JFrame UpdatingForm;
    //更新维护线程
    public static Thread DaemonUpdate;
    //更新网站（必须指定VersionID.sum下载地址）
    public static String UPDATE_WEBSITE = "https://gitee.com/nserly-huaer/ImagePlayer/raw/master/artifacts/PicturePlayer_jar/VersionID.sum";

    //静态代码块
    static {
        //设置标题风格
        titleStyle = "Pictures player";
        //初始化Init
        init = new Init();
        init.Run();
        init.SetUpdate(true);
        if (init.containsKey("MouseMoveOffsets")) {
            String temp = (String) init.getProperties().get("MouseMoveOffsets");
            if (temp.matches("-?\\d+(\\.\\d+)?")) {
                MouseMoveOffsets = Double.valueOf(temp).doubleValue();
            } else {
                init.Writer("MouseMoveOffsets", String.valueOf(MouseMoveOffsets));
            }
        } else {
            init.Writer("MouseMoveOffsets", String.valueOf(MouseMoveOffsets));
        }
        if (init.containsKey("EnableProxyServer") && init.containsKey("ProxyServer") && init.getProperties().get("EnableProxyServer").equals("true") && !init.getProperties().get("ProxyServer").toString().isBlank()) {
            String website = init.getProperties().getProperty("ProxyServer");
            if (website.endsWith(".sum")) {
                UPDATE_WEBSITE = website;
            } else {
                UPDATE_WEBSITE = website.trim() + ".sum";
            }
        }
    }

    //构造方法（函数）
    public Main(String path) {
        //获取当前图片路径下所有图片
        ArrayList<String> CurrentPathOfPicture = GetImageInformation.getCurrentPathOfPicture(path);
        percentLabel = new PercentLabel();
        init.Run();
        //向控制台输出打开文件路径
        System.out.println("Opened:\t\"" + path + "\"");
        //设置窗体默认关闭规则
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        //创建画布
        myCanvas = new MyCanvas(path);
        sizeOperate = new SizeOperate(myCanvas, myCanvas.getSize());
        //设置窗体大小、坐标
        setBounds(GetImageInformation.getBestSize(path));
        //设置文本中显示的图片缩放比例
        percentLabel.set((int) sizeOperate.getPercent());
        //添加窗体大小改变监听器
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                sizeOperate.incomeWindowDimension(myCanvas.getSize());
                sizeOperate.update();
            }
        });

        //创建容器
        Container container = getContentPane();
        container.setLayout(new BorderLayout());
        biggest = new JButton("enlarge");
        myCanvas.addMouseMotionListener(new MouseAdapter() {
            //当鼠标按下并移动鼠标时触发
            @Override
            public void mouseDragged(MouseEvent e) {
                if (op != null && op.GetX() != 0 && op.GetY() != 0) {
//                    增加坐标值
//                    myCanvas.AddCoordinate(e.getX() - op.GetX(), e.getY() - op.GetY());
                    myCanvas.setMouseCoordinate((int) ((1 + MouseMoveOffsets) * (e.getX() - op.GetX())), (int) ((1 + MouseMoveOffsets) * (e.getY() - op.GetY())));
                    sizeOperate.update();
                }
                op = new OperatingCoordinate(e.getX(), e.getY());

            }
        });
        myCanvas.addMouseListener(new MouseAdapter() {
            //鼠标放出触发
            public void mouseReleased(MouseEvent e) {
                op = new OperatingCoordinate(0, 0);
            }
        });

        myCanvas.addMouseWheelListener(new MouseAdapter() {
            //鼠标滚轮事件
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (IsDragging) return;
                myCanvas.setMouseCoordinate(e.getX(), e.getY());
                //滚轮向后
                if (e.getWheelRotation() == 1) {
                    sizeOperate.adjustPercent(SizeOperate.Reduce);
                    sizeOperate.adjustPercent(SizeOperate.Reduce);
                }//滚轮向前
                else if (e.getWheelRotation() == -1) {
                    sizeOperate.adjustPercent(SizeOperate.Enlarge);
                    sizeOperate.adjustPercent(SizeOperate.Enlarge);
                }
            }
        });

        biggest.addMouseListener(new MouseAdapter() {
            boolean isDown = false;

            @Override
            public void mousePressed(MouseEvent e) {//点击、长按触发
                isDown = false;
                //将鼠标被按下属性设为true
                IsDragging = true;
                new Thread(() -> {
                    do {
                        sizeOperate.adjustPercent(SizeOperate.Enlarge);
                        sizeOperate.update();
                        try {
                            Thread.sleep(16);
                        } catch (InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }
                    } while (!isDown);
                    myCanvas.requestFocus();
                }).start();

            }


            @Override
            public void mouseReleased(MouseEvent e) {//鼠标放出触发
                isDown = true;
                //将鼠标被按下属性设为false
                IsDragging = false;
            }
        });
        smallest = new JButton("reduce");
        smallest.addMouseListener(new MouseAdapter() {
            //判断是否鼠标释放
            boolean isReleased = false;

            //点击、长按触发
            @Override
            public void mousePressed(MouseEvent e) {
                //设置鼠标没有释放
                isReleased = false;
                //将鼠标被按下属性设为true
                IsDragging = true;
                //创建线程
                new Thread(() -> {
                    //循环
                    do {
                        sizeOperate.adjustPercent(SizeOperate.Reduce);
                        sizeOperate.update();
                        //抛出异常
                        try {
                            //线程休眠
                            Thread.sleep(16);
                        } catch (InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }
                    } while (!isReleased);
                    myCanvas.requestFocus();
                }).start();

            }

            //鼠标放出触发
            @Override
            public void mouseReleased(MouseEvent e) {
                isReleased = true;
                //将鼠标被按下属性设为false
                IsDragging = false;
            }
        });
        ChangeFocusListener changeFocusListener = new ChangeFocusListener(myCanvas);
        //初始化面板
        var Enum = new JPanel();
        var On = getjPanel(changeFocusListener);
        Enum.setLayout(new GridLayout(1, 2));
        //添加组件
        Enum.add(smallest);
        Enum.add(biggest);
        //设置菜单为窗体下方并添加至组件中
        container.add(Enum, BorderLayout.SOUTH);
        //设置为窗体上方并添加至组件中
        container.add(On, BorderLayout.NORTH);
        //添加画布至组件中
        container.add(myCanvas, BorderLayout.CENTER);
        myCanvas.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int CurrentIndex = CurrentPathOfPicture.indexOf(myCanvas.path);
                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    if (CurrentIndex > 0) {
                        //向控制台输出打开文件路径
                        System.out.println("Opened:\t\"" + CurrentPathOfPicture.get(CurrentIndex - 1) + "\"");
                        myCanvas.changeOriginalPicturePath(CurrentPathOfPicture.get(CurrentIndex - 1));
                        sizeOperate.update();
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    if (CurrentIndex + 1 < CurrentPathOfPicture.size()) {
                        //向控制台输出打开文件路径
                        System.out.println("Opened:\t\"" + CurrentPathOfPicture.get(CurrentIndex + 1) + "\"");
                        myCanvas.changeOriginalPicturePath(CurrentPathOfPicture.get(CurrentIndex + 1));
                        sizeOperate.update();
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    if (isLastInFullScreen) {
                        isLastInFullScreen = false;
                        //退出全屏模式
                        GraphicsDevice graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
                        graphicsDevice.setFullScreenWindow(null);
                        FullScreen.setText("Full Screen");
                        On.remove(percentLabel);
                        isLastInFullScreen = false;
                        return;
                    }
                    //设置窗体可见
                    jFrame.setVisible(true);
                    //关闭图片显示窗口
                    Main.main.dispose();
                    //获取图片渲染器存储当前图片路径
                    if (myCanvas.getPath() != null) {
                        jTextField.setText(myCanvas.getPath());
                    }
                    //关闭画布
                    sizeOperate.close();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_F11) {
                    manageFullScreen(On);
                }

            }
        });
        //创建窗体监听器
        this.addWindowListener(new WindowAdapter() {
            //监听窗体关闭
            @Override
            public void windowClosing(WindowEvent e) {
                //设置窗体可见
                jFrame.setVisible(true);
                //获取图片渲染器存储当前图片路径
                if (myCanvas.getPath() != null) {
                    jTextField.setText(myCanvas.getPath());
                }
                //关闭画布
                sizeOperate.close();
            }
        });
        //设置窗体在显示时自动获取焦点
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                //当前窗体成为活动窗体时，让myCanvas获取焦点
                myCanvas.requestFocus();
            }
        });
        //设置窗体可见
        setVisible(true);
        //如果上次处于全屏模式，则自动启用全屏
        if (isLastInFullScreen) {
            //设置窗口为全屏模式
            GraphicsDevice graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            graphicsDevice.setFullScreenWindow(this);
            FullScreen.setText("Exit Full Screen");
            On.add(percentLabel);
            sizeOperate.incomeWindowDimension(myCanvas.getSize());
            sizeOperate.setPercent(sizeOperate.getPictureOptimalSize());
            sizeOperate.update();
        }
    }

    private JPanel getjPanel(ChangeFocusListener changeFocusListener) {
        var On = new JPanel();
        JButton TurnLeft = new JButton("Left");
        //设置图片左转按钮可见
        TurnLeft.setVisible(true);
        //创建图片左转按钮监听器
        TurnLeft.addActionListener(e -> {
            myCanvas.turnLeft();
        });
        TurnLeft.addMouseListener(changeFocusListener);
        JButton TurnRight = new JButton("Right");
        //设置图片右转按钮可见
        TurnLeft.setVisible(true);
        //创建图片右转按钮监听器
        TurnRight.addActionListener(e -> {
            myCanvas.turnRight();
        });
        TurnRight.addMouseListener(changeFocusListener);
        //在容器On中添加还原图片按钮
        JButton Reset = new JButton("reset");
        //设置重置按钮可见
        Reset.setVisible(true);
        //点击时间
        AtomicLong ClickedTime = new AtomicLong();
        //规定时间内点击次数
        AtomicInteger times = new AtomicInteger();
        //创建还原按钮监听器
        Reset.addActionListener(e -> {
            myCanvas.setDefaultCoordinate();
            if (System.currentTimeMillis() - ClickedTime.get() < 2000) {
                if (times.get() == 1) {
                    sizeOperate.restoreTheDefaultPercent();
                    sizeOperate.update();
                } else if (times.get() == 2) {
                    myCanvas.reSetDegrees();
                    sizeOperate.update();
                }
                times.getAndIncrement();
            } else {
                sizeOperate.setPercent(sizeOperate.getPictureOptimalSize());
                sizeOperate.update();
                times.set(1);
            }
            ClickedTime.set(System.currentTimeMillis());
            sizeOperate.update();
        });
        Reset.addMouseListener(changeFocusListener);
        JButton FlipHorizontally = new JButton("FlipHorizontally");
        FlipHorizontally.addMouseListener(changeFocusListener);
        //在容器On中添加全屏按钮
        FullScreen = new JButton("Full Screen");
        //创建全屏按钮监听器
        FullScreen.addActionListener(e -> {
            manageFullScreen(On);
        });
        FullScreen.addMouseListener(changeFocusListener);
        //如果设备支持全屏，设置全屏按钮可见
        if (SupportFullScreen)
            FullScreen.setVisible(true);

        //将图片左转按钮添加到组件中
        On.add(TurnLeft);
        //将重置按钮添加到组件中
        On.add(Reset);
        //将图片右转按钮添加到组件中
        On.add(TurnRight);
        //将全屏模式按钮添加到组件中
        On.add(FullScreen);
        return On;
    }

    //管理全屏
    private void manageFullScreen(JPanel On) {
        GraphicsDevice graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        if (FullScreen.getText().equals("Full Screen")) {
            //检查设备是否支持全屏模式
            if (graphicsDevice.isFullScreenSupported()) {
                //设置窗口为全屏模式
                graphicsDevice.setFullScreenWindow(this);
                FullScreen.setText("Exit Full Screen");
                On.add(percentLabel);
                isLastInFullScreen = true;
            } else {
                JOptionPane.showMessageDialog(jFrame, "The device doesn't support full screen", "Error", JOptionPane.ERROR_MESSAGE);
                On.remove(FullScreen);
                SupportFullScreen = isLastInFullScreen = false;
            }
        } else {
            graphicsDevice.setFullScreenWindow(null);
            FullScreen.setText("Full Screen");
            On.remove(percentLabel);
            isLastInFullScreen = false;
        }
    }


    //软件主方法（函数）
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
                    UpdateForm();
                }
            }).start();
        }
        System.out.println("Loading...");
        long begin = System.currentTimeMillis();
        //初始化窗体
        jFrame = new FileManagementFrame();
        //禁止用户调整窗体尺寸
        jFrame.setResizable(false);
        //设置标题
        jFrame.setTitle("Open picture");
        jFrame.setBounds(500, 500, 400, 200);
        jFrame.setLayout(null);
        var jLabel = new JLabel("Path:");
        jLabel.setBounds(20, 20, 50, 35);
        jLabel.setFont(new Font("", 0, 15));
//        var jTextField = new FileTypeTextField();
        jTextField = new JTextField();
        jTextField.setBounds(75, 20, 230, 35);
        jFrame.add(jLabel);
        jFrame.add(jTextField);

        ChangeFocusListener changeFocusListener = new ChangeFocusListener(jFrame);
        //设置按钮
        var OK = new JButton("OK");
        OK.setBounds(150, 100, 100, 30);
        var FileChoose = new JButton("```");
        FileChoose.setBounds(315, 20, 40, 35);

        //添加键盘监听器
        jTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (GetImageInformation.isRightPath(jTextField.getText())) {
                        jFrame.setVisible(false);
                        main = new Main(jTextField.getText());
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    close();
                }
            }
        });
        OK.addMouseListener(changeFocusListener);
        //为按钮添加动作监听
        OK.addActionListener(e -> {
            //判断是否为正确的文件地址
            if (GetImageInformation.isRightPath(jTextField.getText())) {
                jFrame.setVisible(false);
                main = new Main(jTextField.getText());
                return;
            }
            JOptionPane.showMessageDialog(jFrame, "Couldn't open or recognize the file:\n\"" + jTextField.getText() + "\"", "Invalid image path", JOptionPane.ERROR_MESSAGE);
        });
        FileChoose.addMouseListener(changeFocusListener);
        FileChoose.addActionListener(e -> {
            JFileChooser jFileChooser = new JFileChooser();
            File cachePath = null;
            //如果文本框中含有字符，则自动打开该目录（非文件）
            if ((!jTextField.getText().isBlank()) && new File(jTextField.getText()).exists()) {
                cachePath = new File(jTextField.getText());
                //判断文件（夹）是否为文件
                if (cachePath.isFile()) {
                    //若是文件，则获取文件所在的文件夹的绝对路径
                    cachePath = new File(cachePath.getParent());
                }
                //设置文件选择器的当前目录为上次打开的默认目录
                jFileChooser.setCurrentDirectory(cachePath);
            }
            File file = null;
            //是否为受Java支持的文件格式
            for (int times = 0; !GetImageInformation.isImageFile(file); times++) {
                if (times > 0) {
                    JOptionPane.showMessageDialog(jFrame, "Couldn't open or recognize the file:\n\"" + file.getPath() + "\"", "Invalid image path", JOptionPane.ERROR_MESSAGE);
                }
                //显示选择文件框
                int userSelection = jFileChooser.showOpenDialog(jFrame);
                //获取用户打开的文件
                file = jFileChooser.getSelectedFile();
                //判断文件对象是否为空值
                if (userSelection != JFileChooser.APPROVE_OPTION) {
                    return;
                }
            }
            //获取String类型文件路径
            String path = file.getPath();
            //将文件路径文本框设置为path
            jTextField.setText(path);
            //隐藏文件打开管理窗体
            jFrame.setVisible(false);
            //创建图片查看窗体
            main = new Main(jTextField.getText());
        });
        //添加按钮至窗体中
        jFrame.add(OK);
        jFrame.add(FileChoose);
        //显示窗体
        jFrame.setVisible(true);
        //添加窗体监听器
        jFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                close();
            }
        });
        //设置窗体默认关闭规则
        jFrame.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        System.out.println("Done!(Spent " + (System.currentTimeMillis() - begin) + "ms)");
        //设置窗体在显示时自动获取焦点
        jFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                //当前窗体成为活动窗体时，让myCanvas获取焦点
                jFrame.requestFocus();
            }
        });
    }

    //改变图片路径
    public void changePicturePath(String path) {
        myCanvas = new MyCanvas(path);
        sizeOperate = new SizeOperate(myCanvas, myCanvas.getSize());
        add(myCanvas, BorderLayout.CENTER);
    }

    //关闭
    public static void close() {
        //加载配置文件
        init.Loading();
        if (init.getProperties().get("EnableConfirmExit") != null && init.getProperties().get("EnableConfirmExit").toString().toLowerCase().equals("false")) {
            if (Main.main != null && Main.main.sizeOperate != null) Main.main.sizeOperate.close();
            if (jFrame != null) jFrame.dispose();
            System.exit(0);
        }
        //设置消息对话框窗体
        var jDialog = new JDialog(jFrame, true);
        //设置窗体标题
        jDialog.setTitle("Confirm Exit");
        //设置窗体大小、坐标（获取父窗体坐标）
        jDialog.setBounds(jFrame.getX(), jFrame.getY(), 260, 170);
        //创建文字
        var jLabel1 = new JLabel("Are you sure you want to exit?");
        //设置文字字体、格式
        jLabel1.setFont(new Font("隶书", 0, 15));
        //设置显示大小、坐标
        jLabel1.setBounds(15, 3, 200, 50);
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
            //关闭窗体
            jFrame.dispose();
            //判断main是否为null值（以防止出现空指针异常）
            if (Main.main != null && Main.main.sizeOperate != null) Main.main.sizeOperate.close();
            if (jFrame != null) jFrame.dispose();
            if (jCheckBox.isSelected()) {
                init.ChangeValue("EnableConfirmExit", "false");
                init.Update();
            }
            System.exit(0);
        });
        yes.addMouseListener(changeFocusListener);
        //点击取消以询问隐藏窗体（不会退出程序）
        no.addActionListener(e1 -> {
            jDialog.setVisible(false);
        });
        no.addMouseListener(changeFocusListener);
        //窗体键盘监听器
        jDialog.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    jDialog.setVisible(false);
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    //关闭窗体
                    jFrame.dispose();
                    //判断main是否为null值（以防止出现空指针异常）
                    if (Main.main != null && Main.main.sizeOperate != null) Main.main.sizeOperate.close();
                    if (jFrame != null) jFrame.dispose();
                    if (jCheckBox.isSelected()) {
                        init.ChangeValue("EnableConfirmExit", "false");
                        init.Update();
                    }
                    System.exit(0);
                }
            }
        });
        //设置窗体在显示时自动获取焦点
        jDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                //当前窗体成为活动窗体时，让myCanvas获取焦点
                jDialog.requestFocus();
            }
        });
        //显示窗体
        jDialog.setVisible(true);
    }

    public static void UpdateForm() {
        AdvancedDownloadSpeedDisplay advancedDownloadSpeedDisplay = new AdvancedDownloadSpeedDisplay();
        advancedDownloadSpeedDisplay.createAndShowGUI(NewVersionDownloadingWebSide);

    }

    public class MyCanvas extends JComponent {
        //图片路径
        String path;
        //当前图片X坐标
        private int X;
        //当前图片Y坐标
        private int Y;
        //点击时，鼠标X坐标
        private int mouseX;
        //点击时，鼠标Y坐标
        private int mouseY;
        //上次图片缩放比
        private double LastPercent;
        //上次图片宽度
        private int lastWidth;
        //上次图片高度
        private int lastHeight;
        //当前图片
        private Image image;
        //渲染器
        private Graphics g;
        //当前组件信息
        private Dimension NewWindow;
        //上次组件信息
        private Dimension LastWindow;
        //旋转度数（逆时针）
        private byte RotationDegrees;
        //上次旋转度数
        private byte lastRotationDegrees;


        //构造方法初始化
        public MyCanvas(String path) {
            //如果字符串前缀与后缀包含"，则去除其中的"
            if (path.startsWith("\"") && path.endsWith("\"")) {
                path = path.substring(1, path.length() - 1);
            }
            this.path = path;
            image = new ImageIcon(path).getImage();
        }

        //获取图片路径
        public String getPath() {
            return path;
        }

        //图片左转
        public void turnLeft() {
            changeDegrees(1);
        }


        //图片右转
        public void turnRight() {
            changeDegrees(-1);
        }

        //重置旋转度数
        public void reSetDegrees() {
            RotationDegrees = 0;
            if (lastRotationDegrees != RotationDegrees)
                sizeOperate.changeCanvas(this);
        }


        //改变度数
        private void changeDegrees(int addDegrees) {
            RotationDegrees += addDegrees;
            RotationDegrees = (byte) (RotationDegrees % 4);
            if (RotationDegrees < 0) RotationDegrees = (byte) (4 + RotationDegrees);
            sizeOperate.update();
        }

        //设置度数
        private void setDegrees(int Degrees) {
            RotationDegrees = (byte) (Degrees % 4);
            sizeOperate.changeCanvas(this);
        }

        //获取度数
        public int getDegrees() {
            return Math.abs(RotationDegrees) * 90;
        }

        //改变图片路径
        public void changeOriginalPicturePath(String path) {
            //如果字符串前缀与后缀包含"，则去除其中的"
            if (path.startsWith("\"") && path.endsWith("\"")) {
                path = path.substring(1, path.length() - 1);
            }
            RotationDegrees = lastRotationDegrees = 0;
            this.path = path;
            LastPercent = lastWidth = lastHeight = X = Y = mouseX = mouseY = 0;
            NewWindow = LastWindow = null;
            image = new ImageIcon(path).getImage();
            sizeOperate.changeCanvas(this);
        }

        public void close() {
            if (g != null) {
                g.dispose();
                g = null;
            }
            if (image != null) {
                image.flush();
                image = null;
            }
            path = null;
            LastPercent = lastWidth = lastHeight = X = Y = mouseX = mouseY = 0;

            NewWindow = LastWindow = null;
            this.removeAll();
        }

        //设置已知组件长度
        public void setWindowSize(Dimension window) {
            this.NewWindow = window;
        }

        //获取图片高度
        public int getImageHeight() {
            if (image == null) return 0;
//            else if (RotationDegrees % 2 == 0) return image.getHeight(null);
//            else return image.getWidth(null);
            return image.getHeight(null);
        }

        //获取图片宽度
        public int getImageWidth() {
            if (image == null) return 0;
//            else if (RotationDegrees % 2 == 0) return image.getWidth(null);
//            else return image.getHeight(null);
            return image.getWidth(null);
        }


        @Override
        public synchronized void paint(Graphics g) {
            this.g = g;
            var graphics2D = (Graphics2D) g;
            graphics2D.rotate(Math.toRadians(RotationDegrees * 90));
            int tempHeight = 0, tempWidth = 0;
            //算法实现图片居中

            if (Math.abs(RotationDegrees) == 1) {
                int temp = mouseX;
                mouseX = mouseY;
                mouseY = -temp;
                tempWidth = lastHeight;
                tempHeight = lastWidth;
            } else if (Math.abs(RotationDegrees) == 2) {
                mouseX = -mouseX;
                mouseY = -mouseY;
            } else if (Math.abs(RotationDegrees) == 3) {
                int temp = mouseX;
                mouseX = -mouseY;
                mouseY = temp;
                tempWidth = lastHeight;
                tempHeight = lastWidth;
            }

            int width;
            double WindowHeight = 0, WindowWidth = 0, LastWindowHeight = 0, LastWindowWidth = 0;
            if (NewWindow != null) {
                WindowWidth = NewWindow.getWidth();
                WindowHeight = NewWindow.getHeight();
                if (LastWindow == null) {
                    LastWindow = NewWindow;
                }
                LastWindowWidth = LastWindow.getWidth();
                LastWindowHeight = LastWindow.getHeight();
            }
            if (RotationDegrees == lastRotationDegrees && LastPercent == sizeOperate.getPercent() && LastWindow != null && LastWindow.equals(NewWindow)) {
                X += mouseX;
                Y += mouseY;
                if (RotationDegrees == 0) {
                    if (X > WindowWidth) X = (int) WindowWidth;
                    if (Y > WindowHeight) Y = (int) WindowHeight;
                    if (X + lastWidth < 0) X = -lastWidth;
                    if (Y + lastWidth < 0) Y = -lastWidth;
                } else if (RotationDegrees == 1) {
                    if (X > WindowHeight) X = (int) WindowHeight;
                    if (Y > 0) Y = 0;
                    if (X + tempHeight < 0) X = -tempHeight;
                    if (Y + tempWidth < -WindowWidth) Y = (int) (-WindowWidth - tempWidth);
                } else if (RotationDegrees == 2) {
                    if (X + lastWidth < -WindowWidth) X = (int) (-WindowWidth - lastWidth);
                    if (Y > 0) Y = 0;
                    if (X > 0) X = 0;
                    if (Y + lastHeight < -WindowHeight) Y = (int) (-WindowHeight - lastHeight);
                } else if (RotationDegrees == 3) {
                    if (X > 0) X = 0;
                    if (X + tempHeight < -WindowHeight) X = (int) (-WindowHeight - tempHeight);
                    if (Y > WindowWidth) Y = (int) WindowWidth;
                    if (Y + tempWidth < 0) Y = -tempWidth;
                }
                graphics2D.drawImage(image, X, Y, lastWidth, lastHeight, null);
                mouseX = mouseY = 0;
                lastRotationDegrees = RotationDegrees;
                return;
            }
            if (RotationDegrees != lastRotationDegrees) {
                sizeOperate.setPercent(sizeOperate.getPictureOptimalSize());
                Point point = ImageRotationHelper.getRotatedCoord(X, Y, 360 - 90 * RotationDegrees, lastWidth, lastHeight);
                X = (int) point.getX();
                Y = (int) point.getY();
            }

            if (NewWindow != null && NewWindow.getHeight() * NewWindow.getWidth() > lastHeight * lastWidth) {
                sizeOperate.setPercent(sizeOperate.getPercent() + 1);//窗口放大图片比例会变小，没写好该校正代码，暂以+1来校对，以后优化！
            }
            if ((RotationDegrees - lastRotationDegrees == 0) && mouseX == 0 && mouseY == 0) {
                if (RotationDegrees == 0) {
                    mouseX = (int) (WindowWidth / 2);
                    mouseY = (int) (WindowHeight / 2);
                } else if (RotationDegrees == 1) {
                    mouseX = (int) (WindowHeight / 2);
                    mouseY = (int) -(WindowWidth / 2);
                } else if (RotationDegrees == 2) {
                    mouseX = (int) -(WindowWidth / 2);
                    mouseY = (int) -(WindowHeight / 2);
                } else if (RotationDegrees == 3) {
                    mouseX = (int) -(WindowHeight / 2);
                    mouseY = (int) (WindowWidth / 2);
                }
            }
            double WidthRatio = 0;
            double HeightRatio = 0;
            double PictureChangeRatio = 1;
            if (WindowWidth != 0 && WindowHeight != 0) {
                WidthRatio = LastWindowWidth / WindowWidth;
                HeightRatio = LastWindowHeight / WindowHeight;
                if (WidthRatio != 1 && HeightRatio != 1) PictureChangeRatio = (HeightRatio + WidthRatio) / 2;
            }

            PictureChangeRatio = 1 / PictureChangeRatio;

            width = (int) (getImageWidth() * sizeOperate.getPercent() / 100 * PictureChangeRatio);
            int height = (int) EqualsProportion.Start(0, width, getImageHeight(), getImageWidth());
            sizeOperate.setPercent(width * 100.0 / getImageWidth());


            int FinalX = X, FinalY = Y;


            if (RotationDegrees % 2 == 0 && NewWindow != null && NewWindow.equals(LastWindow) && lastWidth != 0 && lastHeight != 0) {
                FinalX = width * (FinalX - mouseX) / lastWidth + mouseX;
                FinalY = height * (FinalY - mouseY) / lastHeight + mouseY;
            } else if (RotationDegrees % 2 == 1 && NewWindow != null && NewWindow.equals(LastWindow) && lastWidth != 0 && lastHeight != 0) {
                FinalX = height * (FinalX - mouseX) / lastHeight + mouseX;
                FinalY = width * (FinalY - mouseY) / lastWidth + mouseY;
            }

            if (RotationDegrees == 0) {
                if (WindowWidth <= width) {
                    if (FinalX > 0) FinalX = 0;
                    if (FinalX + width < WindowWidth) FinalX = (int) (WindowWidth - width);
                } else FinalX = (int) ((WindowWidth - width) / 2);

                if (WindowHeight <= height) {
                    if (FinalY > 0) FinalY = 0;
                    if (FinalY + height < WindowHeight) FinalY = (int) (WindowHeight - height);
                } else FinalY = (int) ((WindowHeight - height) / 2);
            } else if (RotationDegrees == 1) {
                if (WindowHeight <= width) {
                    if (FinalX > 0) FinalX = 0;
                    if (FinalX + width < WindowHeight) FinalX = (int) (WindowHeight - width);
                } else FinalX = (int) ((WindowHeight - width) / 2);

                if (WindowWidth <= height) {
                    if (FinalY > -WindowWidth) FinalY = (int) -WindowWidth;
                    if (FinalY < -height) FinalY = -height;
                } else FinalY = (int) ((-height - WindowWidth) / 2);
            } else if (RotationDegrees == 2) {
                if (WindowWidth <= width) {
                    if (FinalX > -WindowWidth) FinalX = (int) -WindowWidth;
                    if (FinalX < -width) FinalX = -width;
                } else FinalX = (int) ((-width - WindowWidth) / 2);

                if (WindowHeight <= height) {
                    if (FinalY > -WindowHeight) FinalY = (int) -WindowHeight;
                    if (FinalY < -height) FinalY = -height;
                } else FinalY = (int) ((-height - WindowHeight) / 2);
            } else if (RotationDegrees == 3) {
                if (WindowHeight <= width) {
                    if (FinalX > -WindowHeight) FinalX = (int) -WindowHeight;
                    if (FinalX < -width) FinalX = -width;
                } else FinalX = (int) ((-width - WindowHeight) / 2);

                if (WindowWidth <= height) {
                    if (FinalY > 0) FinalY = 0;
                    if (FinalY + height < WindowWidth) FinalY = (int) (WindowWidth - height);
                } else FinalY = (int) ((WindowWidth - height) / 2);

            }


            //显示图像
            graphics2D.drawImage(image, FinalX, FinalY, width, height, null);
            //检查比例是否为最大值，如果为最大就把放大按钮禁用
            Main.main.biggest.setEnabled(!Main.main.sizeOperate.isTheBiggestRatio());
            //检查比例是否为最小值，如果为最小就把放大按钮禁用
            Main.main.smallest.setEnabled(!Main.main.sizeOperate.isTheSmallestRatio());
            //设置文本中显示的图片缩放比例
            percentLabel.set((int) sizeOperate.getPercent());
            this.X = FinalX;
            this.Y = FinalY;
            this.LastWindow = this.NewWindow;
            this.LastPercent = sizeOperate.getPercent();
            this.lastWidth = width;
            this.lastHeight = height;
            this.lastRotationDegrees = RotationDegrees;
            mouseX = mouseY = 0;
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

        }

        public void repaint() {
            super.repaint();
        }

        //添加坐标值
        public void addCoordinate(int x, int y) {
            this.X += x;
            this.Y += y;
        }

        //恢复默认坐标
        public void setDefaultCoordinate() {
            this.X = this.Y = 0;
        }

        //设置X值大小
        public void setX(int x) {
            this.X = x;
        }

        //设置鼠标坐标值
        public void setMouseCoordinate(int mouseX, int mouseY) {
            this.mouseX = mouseX;
            this.mouseY = mouseY;
        }

        //设置Y值大小
        public void setY(int y) {
            this.Y = y;
        }
    }

}