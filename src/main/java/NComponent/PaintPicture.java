package NComponent;//导入包

import Listener.ChangeFocusListener;
import Loading.Bundle;
import Runner.Main;
import Settings.Centre;
import Size.OperatingCoordinate;
import Size.SizeOperate;
import Tools.Component.ComponentInJPanel;
import Tools.EqualsProportion;
import Tools.ImageManager.GetImageInformation;
import Tools.ImageManager.ImageRotationHelper;
import Tools.ImageManager.MultiThreadBlur;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.awt.image.VolatileImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

//创建类
public class PaintPicture extends JPanel {
    //图片打开面板
    public static PaintPicture paintPicture;
    //图片全屏窗体
    FullScreenWindow fullScreenWindow;
    //上部组件
    public JPanel On;
    //下部总组件
    public JPanel Under;
    //下部左组件
    public JPanel UnderLeft;
    //下部右组件
    public JPanel UnderRight;
    //图片分辨率
    public JLabel PictureResolution;
    //逆时针旋转按钮
    public JButton counterclockwise;
    //还原图片缩放按钮
    public JButton Reset;
    //全屏按钮
    public JButton FullScreen;
    //顺时针旋转按钮
    public JButton clockwise;
    //图片大小
    public JLabel PictureSize;
    //打开上一个图片按钮
    private JButton Last;
    //打开下一个图片按钮
    private JButton Next;
    //图片缩小按钮
    public JButton smallest;
    //图片缩放调节滑动条
    public JSlider PercentSlider;
    //图片放大按钮
    public JButton biggest;
    //打开上一个图片
    private static final int LastSign = 0x25;
    //打开下一个图片
    private static final int NextSign = 0x27;
    //创建鼠标坐标管理对象
    OperatingCoordinate op = null;
    //鼠标最小移动坐标位
    Point MinPoint;
    //鼠标最大移动坐标位
    Point MaxPoint;
    //图片显示器尺寸
    Dimension ShowingSize;
    //图片渲染器在屏幕上的坐标
    Point LocationOnScreen;
    //缩放比例标签
    PercentLabel percentLabel;
    //图片比例管理
    public SizeOperate sizeOperate;
    //图片渲染管理
    public ImageCanvas imageCanvas;
    //移动图片时，鼠标最开始的坐标（对于桌面）
    Point mouseLocation;
    //当前图片路径下所有图片
    ArrayList<String> CurrentPathOfPicture = null;
    //是否启用硬件加速
    public static boolean isEnableHardwareAcceleration;
    // 定义边缘范围为5像素
    private static final int EDGE_THRESHOLD = 5;

    private MouseEvent lastMouseEvent;
    private MouseAdapter mouseAdapter;
    private File lastPicturePathParent;
    private static final Logger logger = LoggerFactory.getLogger(PaintPicture.class);

    //构造方法（函数）
    public PaintPicture(String path) {
        setLayout(new BorderLayout());
        paintPicture = this;
        percentLabel = new PercentLabel();
        fullScreenWindow = new FullScreenWindow();
        //向控制台输出打开文件路径
        logger.info("Opened:\"{}\"", path);
        //创建画布
        imageCanvas = new NComponent.PaintPicture.ImageCanvas(path);
        imageCanvas.setBorder(BorderFactory.createEtchedBorder());
        //添加画布至组件中
        add(imageCanvas, BorderLayout.CENTER);
        sizeOperate = new SizeOperate(imageCanvas, imageCanvas.getSize());
        new Thread(() -> {
            Last = new JButton("<");
            Next = new JButton(">");
            smallest = new JButton(Bundle.getMessage("Display_reduce"));
            PercentSlider = new JSlider(sizeOperate.MinPercent, sizeOperate.MaxPercent);
            biggest = new JButton(Bundle.getMessage("Display_enlarge"));
            PictureResolution = new JLabel();
            PictureSize = new JLabel();
            Font font = new Font("", Font.PLAIN, 15);
            PictureResolution.setFont(font);
            PictureSize.setFont(font);
            //显示图片信息
            setPictureInformationOnComponent(path);
            smallest.addMouseListener(new MouseAdapter() {
                //判断是否鼠标释放
                boolean isReleased = false;

                //点击、长按触发
                @Override
                public void mousePressed(MouseEvent e) {
                    //设置鼠标没有释放
                    isReleased = false;
                    //创建线程
                    new Thread(() -> {
                        //循环
                        do {
                            if (!smallest.isEnabled()) return;
                            if (sizeOperate.adjustPercent(SizeOperate.Reduce)) {
                                sizeOperate.update(false);
                            }

                            //抛出异常
                            try {
                                //线程休眠
                                Thread.sleep(16);
                            } catch (InterruptedException ex) {
                                break;
                            }
                        } while (!isReleased);
                        imageCanvas.requestFocus();
                    }).start();

                }

                //鼠标放出触发
                @Override
                public void mouseReleased(MouseEvent e) {
                    isReleased = true;
                }
            });
            //添加面板大小改变监听器
            addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    if (fullScreenWindow.isShowing()) return;
                    sizeOperate.incomeWindowDimension(imageCanvas.getSize());
                    sizeOperate.update(false);
                }
            });

            PercentSlider.addChangeListener(e -> {
                percentLabel.set(PercentSlider.getValue());
                sizeOperate.setPercent(PercentSlider.getValue());
                sizeOperate.update(false);
            });
            biggest.addMouseListener(new MouseAdapter() {
                boolean isDown = false;

                @Override
                public void mousePressed(MouseEvent e) {//点击、长按触发
                    isDown = false;
                    new Thread(() -> {
                        do {
                            if (!biggest.isEnabled()) return;
                            if (sizeOperate.adjustPercent(SizeOperate.Enlarge)) {
                                sizeOperate.update(false);
                            }
                            try {
                                Thread.sleep(16);
                            } catch (InterruptedException ex) {
                                break;
                            }
                        } while (!isDown);
                        imageCanvas.requestFocus();
                    }).start();
                }

                @Override
                public void mouseReleased(MouseEvent e) {//鼠标放出触发
                    isDown = true;
                }
            });
            ChangeFocusListener changeFocusListener = new ChangeFocusListener(imageCanvas);
            PercentSlider.addMouseListener(changeFocusListener);
            Last.addMouseListener(changeFocusListener);
            Next.addMouseListener(changeFocusListener);
            Last.addActionListener(e -> {
                imageCanvas.openLONPicture(LastSign);
                if (lastMouseEvent != null) {
                    mouseAdapter.mouseMoved(lastMouseEvent);
                }
            });
            Next.addActionListener(e -> {
                imageCanvas.openLONPicture(NextSign);
                if (lastMouseEvent != null) {
                    mouseAdapter.mouseMoved(lastMouseEvent);
                }
            });
            //初始化面板
            Under = new JPanel();
            UnderLeft = new JPanel();
            UnderRight = new JPanel();
            On = getjPanel(changeFocusListener);
            UnderLeft.setLayout(new FlowLayout(FlowLayout.LEADING));
            UnderRight.setLayout(new FlowLayout(FlowLayout.RIGHT));
            Under.setLayout(new GridLayout(1, 2));

            //添加左组件
            UnderLeft.add(PictureResolution);
            UnderLeft.add(PictureSize);

            //添加右组件
            UnderRight.add(smallest);
            UnderRight.add(PercentSlider);
            UnderRight.add(biggest);
            //加入下方总组件
            Under.add(UnderLeft);
            Under.add(UnderRight);
            //设置菜单为面板下方并添加至组件中
            add(Under, BorderLayout.SOUTH);
            //设置为面板上方并添加至组件中
            add(On, BorderLayout.NORTH);
            //设置文本中显示的图片缩放比例
            percentLabel.set((int) sizeOperate.getPercent());
            //设置图片缩放滑动条
            PercentSlider.setValue((int) sizeOperate.getPercent());
            paintPicture.validate();
            loadPictureInTheParent(path);
        }).start();
    }

    private void loadPictureInTheParent(String picturePath) {
        File pictureParent = new File(picturePath).getParentFile();
        if (!pictureParent.equals(lastPicturePathParent)) {
            //获取当前图片路径下所有图片
            CurrentPathOfPicture = GetImageInformation.getCurrentPathOfPicture(picturePath);
            lastPicturePathParent = pictureParent;
        }
    }

    private JPanel getjPanel(ChangeFocusListener changeFocusListener) {
        var On = new JPanel();
        clockwise = new JButton(Bundle.getMessage("Display_RotateClockwiseButton"));
        //在容器On中添加还原图片按钮
        Reset = new JButton(Bundle.getMessage("Display_reset"));
        //顺时针
        counterclockwise = new JButton(Bundle.getMessage("Display_RotateCounterclockwise"));
        //全屏
        FullScreen = new JButton(Bundle.getMessage("Display_FullScreenButton"));
        //设置图片顺时针按钮可见
        clockwise.setVisible(true);
        //创建图片顺时针按钮监听器
        clockwise.addActionListener(e -> {
            imageCanvas.turnLeft();
        });
        clockwise.addMouseListener(changeFocusListener);
        //设置图片逆时针按钮可见
        clockwise.setVisible(true);
        //创建图片顺时针按钮监听器
        counterclockwise.addActionListener(e -> {
            imageCanvas.turnRight();
        });
        counterclockwise.addMouseListener(changeFocusListener);
        //设置重置按钮可见
        Reset.setVisible(true);
        //点击时间
        AtomicLong ClickedTime = new AtomicLong();
        //规定时间内点击次数
        AtomicInteger times = new AtomicInteger();
        //上次点击reset按钮时的角度
        AtomicReference<Byte> lastRotationDegrees = new AtomicReference<>((byte) 0);
        //创建还原按钮监听器
        Reset.addActionListener(e -> {
            aaa:
            if (System.currentTimeMillis() - ClickedTime.get() < 1500) {
                //若上次点击reset按钮时的角度与当前的旋转角度不否
                if (imageCanvas.RotationDegrees != 0 && lastRotationDegrees.getAndSet(imageCanvas.RotationDegrees) != imageCanvas.RotationDegrees) {
                    //将大小调到合适的比例
                    sizeOperate.setPercent(sizeOperate.getPictureOptimalSize());
                    sizeOperate.update(false);
                    times.set(1);
                    break aaa;
                }
                if (times.get() == 1) {
                    //恢复默认大小（100%）
                    sizeOperate.restoreTheDefaultPercent();
                    sizeOperate.update(false);
                } else if (times.get() == 2 && imageCanvas.RotationDegrees != 0) {
                    //旋转回来，并将大小调到合适的比例
                    imageCanvas.reSetDegrees();
                    sizeOperate.setPercent(sizeOperate.getPictureOptimalSize());
                    sizeOperate.update(false);
                } else if (times.get() == 3) {
                    sizeOperate.restoreTheDefaultPercent();
                    sizeOperate.update(false);
                }
                times.getAndIncrement();
            } else {
                //将大小调到合适的比例
                sizeOperate.setPercent(sizeOperate.getPictureOptimalSize());
                sizeOperate.update(false);
                times.set(1);
            }
            ClickedTime.set(System.currentTimeMillis());
        });
        Reset.addMouseListener(changeFocusListener);
        FullScreen.setVisible(true);
        FullScreen.addMouseListener(changeFocusListener);
        FullScreen.addActionListener(e -> {
            if (imageCanvas == null) return;
            fullScreenWindow.setImageCanvas(imageCanvas);
            Main.main.setVisible(false);
            fullScreenWindow.setVisible(true);
            Main.main.getGraphics().dispose();
            sizeOperate.update(false);
        });
        //将图片逆时针按钮添加到组件中
        On.add(counterclockwise);
        //将重置按钮添加到组件中
        On.add(Reset);
        //将全屏按钮添加到组件中
        On.add(FullScreen);
        //将图片顺时针按钮添加到组件中
        On.add(clockwise);
        //将比例显示添加到组件中
        On.add(percentLabel);
        return On;
    }

    //改变图片路径
    public void changePicturePath(String path) {
        logger.info("Opened:\"{}\"", path);
        imageCanvas.changePicturePath(path);
    }

    //显示图片大小、分辨率等信息
    private void setPictureInformationOnComponent(String path) {
        File PictureFile = new File(path);
        Dimension dimension = null;
        try {
            dimension = GetImageInformation.getImageSize(PictureFile);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        PictureSize.setText(AdvancedDownloadSpeed.formatBytes(PictureFile.length()));
        if (dimension != null)
            PictureResolution.setText((int) dimension.getWidth() + "x" + (int) dimension.getHeight());
    }

    public class ImageCanvas extends JComponent {
        //图片路径
        String path;
        //上次图片路径
        String lastPath;
        //图片hashcode
        String picture_hashcode;
        //当前图片X坐标
        private double X;
        //当前图片Y坐标
        private double Y;
        //点击时，鼠标X坐标
        private double mouseX;
        //点击时，鼠标Y坐标
        private double mouseY;
        //上次图片缩放比
        private double LastPercent;
        //上次图片宽度
        private double lastWidth;
        //上次图片高度
        private double lastHeight;
        //当前图片
        private Image image;

        //模糊后的bufferedImage
        private BufferedImage BlurBufferedImage;
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
        //是否为移动
        private boolean isMove;

        //是否要模糊显示
        private boolean isNeedBlurToView;

        //创建时间计时器，（图片模糊）
        private Timer timer;

        //构造方法初始化
        public ImageCanvas(String path) {
            //加载图片
            loadImage(path);
            setDoubleBuffered(true);
            new Thread(() -> {
                //初始化监听器
                init_listener();
                //添加图片hashcode
                this.picture_hashcode = GetImageInformation.getHashcode(new File(path));
                AtomicReference<Double> LastPercent = new AtomicReference<>((double) -9999999);
                AtomicReference<String> LastPicture_hashcode = new AtomicReference<>("");
                MultiThreadBlur multiThreadBlur;
                boolean isEnableHardware = image instanceof VolatileImage;
                if (isEnableHardware) {
                    multiThreadBlur = new MultiThreadBlur(BlurBufferedImage);
                    timer = new Timer(400, e -> {
                        ((Timer) e.getSource()).stop(); // 停止计时器
                        new Thread(() -> {
                            if (image != null) {
                                if (!LastPicture_hashcode.get().equals(picture_hashcode)) {
                                    multiThreadBlur.flushSrc();
                                    multiThreadBlur.changeImage(BlurBufferedImage);
                                } else if (LastPercent.get() == sizeOperate.getPercent()) {
                                    isNeedBlurToView = true;
                                    repaint();
                                    return;
                                }
                                BlurBufferedImage.flush();
                                int KernelSize = multiThreadBlur.calculateKernelSize(sizeOperate.getPercent());
                                BlurBufferedImage = multiThreadBlur.applyOptimizedBlur(KernelSize);
                                if (KernelSize == 1) {
                                    BlurBufferedImage = multiThreadBlur.getSrc();
                                }
                                isNeedBlurToView = true;
                                LastPercent.set(sizeOperate.getPercent());
                                LastPicture_hashcode.set(picture_hashcode);
                                repaint();
                            }
                        }).start();
                    });
                }
            }).start();
        }

        public ImageCanvas(String path, String picture_hashcode) {
            //加载图片
            loadImage(path);
            //初始化监听器
            init_listener();
            //添加图片hashcode
            this.picture_hashcode = picture_hashcode;
            setDoubleBuffered(true);

        }

        //获取图片路径
        public String getPath() {
            return path;
        }

        //获取图片hashcode值
        public String getPicture_hashcode() {
            return picture_hashcode;
        }

        //图片左转
        public void turnLeft() {
            addDegrees(1);
        }


        //图片右转
        public void turnRight() {
            addDegrees(-1);
        }

        //重置旋转度数
        public void reSetDegrees() {
            RotationDegrees = 0;
            if (lastRotationDegrees != RotationDegrees) sizeOperate.changeCanvas(this);
        }


        //改变度数
        private void addDegrees(int addDegrees) {
            RotationDegrees += addDegrees;
            RotationDegrees = (byte) (RotationDegrees % 4);
            if (RotationDegrees < 0) RotationDegrees = (byte) (4 + RotationDegrees);
            sizeOperate.update(false);
        }

        //设置度数
        private void setDegrees(int Degrees) {
            RotationDegrees = (byte) (Degrees % 4);
            if (RotationDegrees < 0) RotationDegrees = (byte) (4 + RotationDegrees);
            sizeOperate.changeCanvas(this);
        }

        //获取度数
        public int getDegrees() {
            return Math.abs(RotationDegrees) * 90;
        }

        //改变图片路径
        public void changePicturePath(String path) {
            //如果字符串前缀与后缀包含"，则去除其中的"
            if (path.startsWith("\"") && path.endsWith("\"")) {
                path = path.substring(1, path.length() - 1);
            }
            setPictureInformationOnComponent(path);
            //若这两个文件父目录不相同
            loadPictureInTheParent(path);
            RotationDegrees = lastRotationDegrees = 0;
            this.path = path;
            picture_hashcode = GetImageInformation.getHashcode(new File(path));
            LastPercent = lastWidth = lastHeight = X = Y = mouseX = mouseY = 0;
            NewWindow = LastWindow = null;
            if (image != null) image.flush();
            if (BlurBufferedImage != null) BlurBufferedImage.flush();
            try {
                BlurBufferedImage = ImageIO.read(new File(path));
                image = GetImageInformation.CastToTYPE_INT_RGB(BlurBufferedImage);
                if (isEnableHardwareAcceleration) {
                    BlurBufferedImage = (BufferedImage) image;
                    image = GetImageInformation.convert(BlurBufferedImage);
                } else {
                    BlurBufferedImage.flush();
                    BlurBufferedImage = null;
                }

            } catch (IOException e) {
                logger.error(e.getMessage());
            }
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
            if (BlurBufferedImage != null) {
                BlurBufferedImage.flush();
                BlurBufferedImage = null;
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
            return image.getHeight(null);
        }

        //获取图片宽度
        public int getImageWidth() {
            if (image == null) return 0;
            return image.getWidth(null);
        }

        //打开上一个/下一个图片
        public void openLONPicture(int sign) {
            int CurrentIndex = CurrentPathOfPicture.indexOf(imageCanvas.path);
            switch (sign) {
                case LastSign -> {
                    if (CurrentIndex > 0) {
                        //向控制台输出打开文件路径
                        logger.info("Opened:\"{}\"", CurrentPathOfPicture.get(CurrentIndex - 1));
                        imageCanvas.changePicturePath(CurrentPathOfPicture.get(CurrentIndex - 1));
                        sizeOperate.update(false);
                    }
                }
                case NextSign -> {
                    if (CurrentIndex + 1 < CurrentPathOfPicture.size()) {
                        //向控制台输出打开文件路径
                        logger.info("Opened:\"{}\"", CurrentPathOfPicture.get(CurrentIndex + 1));
                        imageCanvas.changePicturePath(CurrentPathOfPicture.get(CurrentIndex + 1));
                        sizeOperate.update(false);
                    }
                }
            }
        }

        //判断是否有下一个图片
        public boolean hasNext(int sign) {
            int CurrentIndex = CurrentPathOfPicture.indexOf(imageCanvas.path);
            switch (sign) {
                case LastSign -> {
                    if (CurrentIndex > 0) {
                        return true;
                    }
                }
                case NextSign -> {
                    if (CurrentIndex + 1 < CurrentPathOfPicture.size()) {
                        return true;
                    }
                }
            }
            return false;
        }


        @Override
        public synchronized void paint(Graphics g) {
            if (NewWindow == null || NewWindow.width == 0 || NewWindow.height == 0 || (LastPercent == sizeOperate.getPercent() && RotationDegrees == LastPercent && !isMove && lastPath.equals(path))) {
                return;
            }
            if (timer != null) timer.stop();
            this.g = g;
            double FinalX = X, FinalY = Y;
            var graphics2D = (Graphics2D) g;
            graphics2D.rotate(Math.toRadians(RotationDegrees * 90));
            // 1. 启用图形抗锯齿（对线条、形状有效）
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // 2. 启用图像插值（对缩放后的图像有效）
            graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            //消除字体锯齿
//            graphics2D.setRenderingHint(
//                    RenderingHints.KEY_TEXT_ANTIALIASING,
//                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON
//            );

            if (isNeedBlurToView && BlurBufferedImage != null) {
                // 3. 可选：更高质量但更慢的插值
                graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                graphics2D.drawImage(BlurBufferedImage, (int) X, (int) Y, (int) lastWidth, (int) lastHeight, null);
                isNeedBlurToView = false;
                return;
            }
            //如果转动角度为0或180度，请无使用此值，此值为旋转其他度数的设计
            double tempHeight = 0, tempWidth = 0;
            tempWidth = lastHeight;
            tempHeight = lastWidth;
            //如果旋转角度不为0度，则转换x,y值（因为坐标轴）
            if (RotationDegrees == 1) {
                double temp = mouseX;
                mouseX = mouseY;
                mouseY = -temp;
            } else if (RotationDegrees == 2) {
                mouseX = -mouseX;
                mouseY = -mouseY;
            } else if (RotationDegrees == 3) {
                double temp = mouseX;
                mouseX = -mouseY;
                mouseY = temp;
            }

            double width;
            double WindowHeight = 0, WindowWidth = 0, LastWindowHeight = 0, LastWindowWidth = 0;
            //尝试获取之前和现在的组件大小信息
            WindowWidth = NewWindow.getWidth();
            WindowHeight = NewWindow.getHeight();
            if (LastWindow == null || LastWindow.width == 0 || LastWindow.height == 0) {
                LastWindow = NewWindow;
            }
            LastWindowWidth = LastWindow.getWidth();
            LastWindowHeight = LastWindow.getHeight();
            //判断是否为移动（若移动，则执行本代码）;窗体、图片缩放比例相比于之前是否存在改变（如果没有，则执行本代码）
            if (isMove && RotationDegrees == lastRotationDegrees && LastPercent == sizeOperate.getPercent() && LastWindow != null && LastWindow.equals(NewWindow)) {
                X += mouseX;
                Y += mouseY;
                if (RotationDegrees == 0) {
                    if (X > WindowWidth) X = WindowWidth;
                    if (Y > WindowHeight) Y = WindowHeight;
                    if (X + lastWidth < 0) X = -lastWidth;
                    if (Y + lastHeight < 0) Y = -lastHeight;
                } else if (RotationDegrees == 1) {
                    if (X > WindowHeight) X = WindowHeight;
                    if (Y > 0) Y = 0;
                    if (X + tempHeight < 0) X = -tempHeight;
                    if (Y + tempWidth < -WindowWidth) Y = (-WindowWidth - tempWidth);
                } else if (RotationDegrees == 2) {
                    if (X + lastWidth < -WindowWidth) X = (-WindowWidth - lastWidth);
                    if (Y > 0) Y = 0;
                    if (X > 0) X = 0;
                    if (Y + lastHeight < -WindowHeight) Y = (-WindowHeight - lastHeight);
                } else if (RotationDegrees == 3) {
                    if (X > 0) X = 0;
                    if (X + tempHeight < -WindowHeight) X = (-WindowHeight - tempHeight);
                    if (Y > WindowWidth) Y = WindowWidth;
                    if (Y + tempWidth < 0) Y = -tempWidth;
                }
                graphics2D.drawImage(image, (int) X, (int) Y, (int) lastWidth, (int) lastHeight, null);
                mouseX = mouseY = 0;
                lastRotationDegrees = RotationDegrees;
                isMove = false;
                lastPath = path;
                if (isEnableHardwareAcceleration && timer != null) {
                    timer.start();
                }
                return;
            }
            //判断图片缩放比例是否与上次相同
            if (RotationDegrees != lastRotationDegrees) {
                sizeOperate.setPercent(sizeOperate.getPictureOptimalSize());
                Point point = ImageRotationHelper.getRotatedCoord((int) FinalX, (int) FinalY, 360 - 90 * RotationDegrees, (int) lastWidth, (int) lastHeight);
                FinalX = point.getX();
                FinalY = point.getY();
            }

            if (RotationDegrees == lastRotationDegrees && mouseX == 0 && mouseY == 0) {
                if (RotationDegrees == 0) {
                    mouseX = (WindowWidth / 2);
                    mouseY = (WindowHeight / 2);
                } else if (RotationDegrees == 1) {
                    mouseX = (WindowHeight / 2);
                    mouseY = -(WindowWidth / 2);
                } else if (RotationDegrees == 2) {
                    mouseX = -(WindowWidth / 2);
                    mouseY = -(WindowHeight / 2);
                } else if (RotationDegrees == 3) {
                    mouseX = -(WindowHeight / 2);
                    mouseY = (WindowWidth / 2);
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
            if (PictureChangeRatio == 0) return;
            width = (getImageWidth() * sizeOperate.getPercent() / 100 * (1 / PictureChangeRatio));
            double height = EqualsProportion.Start(0, width, getImageHeight(), getImageWidth());
            sizeOperate.setPercent(width * 100.0 / getImageWidth());

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
                    if (FinalX + width < WindowWidth) FinalX = (WindowWidth - width);
                } else FinalX = ((WindowWidth - width) / 2);

                if (WindowHeight <= height) {
                    if (FinalY > 0) FinalY = 0;
                    if (FinalY + height < WindowHeight) FinalY = (WindowHeight - height);
                } else FinalY = ((WindowHeight - height) / 2);
            } else if (RotationDegrees == 1) {
                if (WindowHeight <= width) {
                    if (FinalX > 0) FinalX = 0;
                    if (FinalX + width < WindowHeight) FinalX = (WindowHeight - width);
                } else FinalX = ((WindowHeight - width) / 2);

                if (WindowWidth <= height) {
                    if (FinalY > -WindowWidth) FinalY = -WindowWidth;
                    if (FinalY < -height) FinalY = -height;
                } else FinalY = ((-height - WindowWidth) / 2);
            } else if (RotationDegrees == 2) {
                if (WindowWidth <= width) {
                    if (FinalX > -WindowWidth) FinalX = -WindowWidth;
                    if (FinalX < -width) FinalX = -width;
                } else FinalX = ((-width - WindowWidth) / 2);

                if (WindowHeight <= height) {
                    if (FinalY > -WindowHeight) FinalY = -WindowHeight;
                    if (FinalY < -height) FinalY = -height;
                } else FinalY = ((-height - WindowHeight) / 2);
            } else if (RotationDegrees == 3) {
                if (WindowHeight <= width) {
                    if (FinalX > -WindowHeight) FinalX = -WindowHeight;
                    if (FinalX < -width) FinalX = -width;
                } else FinalX = ((-width - WindowHeight) / 2);

                if (WindowWidth <= height) {
                    if (FinalY > 0) FinalY = 0;
                    if (FinalY + height < WindowWidth) FinalY = (WindowWidth - height);
                } else FinalY = ((WindowWidth - height) / 2);

            }

            //显示图像
            graphics2D.drawImage(image, (int) FinalX, (int) FinalY, (int) width, (int) height, null);
            //检查比例是否为最大值，如果为最大就把放大按钮禁用
            if (paintPicture.biggest != null) {
                paintPicture.biggest.setEnabled(!paintPicture.sizeOperate.isTheBiggestRatio());
                if (!paintPicture.biggest.isEnabled()) imageCanvas.requestFocus();
            }
            //检查比例是否为最小值，如果为最小就把放大按钮禁用
            if (paintPicture.smallest != null) {
                paintPicture.smallest.setEnabled(!paintPicture.sizeOperate.isTheSmallestRatio());
                if (!paintPicture.smallest.isEnabled()) imageCanvas.requestFocus();
            }

            //设置文本中显示的图片缩放比例
            percentLabel.set((int) sizeOperate.getPercent());
            //设置图片缩放滑动条
            PercentSlider.setValue((int) sizeOperate.getPercent());
            this.X = FinalX;
            this.Y = FinalY;
            this.LastWindow = this.NewWindow;
            this.LastPercent = sizeOperate.getPercent();
            this.lastWidth = width;
            this.lastHeight = height;
            this.lastRotationDegrees = RotationDegrees;
            mouseX = mouseY = 0;
            lastPath = path;
            if (isEnableHardwareAcceleration && timer != null) {
                timer.start();
            }
        }

        //设置是否图片移动（不应该改变图片大小）
        public void setIsMove(boolean isMove) {
            this.isMove = isMove;
        }

        //获取是否图片将要移动
        public boolean getIsMove() {
            return isMove;
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

        //加载图片
        private void loadImage(String PicturePath) {
            //如果字符串前缀与后缀包含"，则去除其中的"
            if (PicturePath.startsWith("\"") && PicturePath.endsWith("\"")) {
                PicturePath = PicturePath.substring(1, PicturePath.length() - 1);
            }
            if (image != null) image.flush();
            if (BlurBufferedImage != null) BlurBufferedImage.flush();
            this.path = PicturePath;
            try {
                BlurBufferedImage = ImageIO.read(new File(path));
                image = GetImageInformation.CastToTYPE_INT_RGB(BlurBufferedImage);
            } catch (IOException e) {
                logger.error("Error loading image \"{}\"", path);
                return;
            }
            if (isEnableHardwareAcceleration) {
                BlurBufferedImage = (BufferedImage) image;
                image = GetImageInformation.convert(BlurBufferedImage);
            } else {
                BlurBufferedImage.flush();
                BlurBufferedImage = null;
            }
        }

        //初始化监听器
        private void init_listener() {
            Robot robot = null;
            try {
                robot = new Robot();
            } catch (AWTException e) {
                logger.warn("Couldn't get Mouse Information");
            }
            Robot finalRobot = robot;
            Image image = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(0, 0, new int[0], 0, 0));
            final boolean[] EnableCursorDisplay = new boolean[1];
            addMouseListener(new MouseAdapter() {
                //鼠标一按下就触发
                @Override
                public void mousePressed(MouseEvent e) {
                    if (e.getButton() != MouseEvent.BUTTON1) return;
                    op = new OperatingCoordinate(e.getX(), e.getY());
                    EnableCursorDisplay[0] = Centre.getBoolean("EnableCursorDisplay", Main.main.centre.CurrentData);
                    if (EnableCursorDisplay[0]) return;
                    setCursor(Toolkit.getDefaultToolkit().createCustomCursor(image, new Point(0, 0), null));
                    mouseLocation = MouseInfo.getPointerInfo().getLocation();
                    if (ShowingSize != null && LocationOnScreen != null && ShowingSize.equals(sizeOperate.getWindowSize()) && LocationOnScreen.equals(imageCanvas.getLocationOnScreen()))
                        return;
                    LocationOnScreen = imageCanvas.getLocationOnScreen();
                    ShowingSize = sizeOperate.getWindowSize();

                    int minX = -LocationOnScreen.x, minY = -LocationOnScreen.y;
                    if (minX < 0) minX = 0;
                    if (minY < 0) minY = 0;
                    MinPoint = new Point(minX, minY);

                    int maxX = ShowingSize.width, maxY = ShowingSize.height;
                    int x = ShowingSize.width + LocationOnScreen.x;
                    int y = ShowingSize.height + LocationOnScreen.y;
                    if (x > SizeOperate.FreeOfScreenSize.width)
                        maxX = SizeOperate.FreeOfScreenSize.width - LocationOnScreen.x;
                    if (y > SizeOperate.FreeOfScreenSize.height)
                        maxY = SizeOperate.FreeOfScreenSize.height - LocationOnScreen.y;
                    MaxPoint = new Point(maxX, maxY);
                }


                //鼠标放出触发
                public void mouseReleased(MouseEvent e) {
                    if (e.getButton() != MouseEvent.BUTTON1) return;
                    if (!EnableCursorDisplay[0]) {
                        setCursor(Cursor.getDefaultCursor());
                        if (finalRobot != null) finalRobot.mouseMove(mouseLocation.x, mouseLocation.y);
                    }
                }
            });
            paintPicture.mouseAdapter = new MouseAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (!SwingUtilities.isLeftMouseButton(e)) return;
                    int x = e.getX(), y = e.getY();
                    if (!EnableCursorDisplay[0]) {
                        boolean NeedToMove = false;
                        if (x <= MinPoint.x) {
                            x = MaxPoint.x - 2;
                            NeedToMove = true;
                        } else if (x >= MaxPoint.x - 1) {
                            x = MinPoint.x + 1;
                            NeedToMove = true;
                        }
                        if (y <= MinPoint.y) {
                            y = MaxPoint.y - 2;
                            NeedToMove = true;
                        } else if (y >= MaxPoint.y - 1) {
                            y = MinPoint.y + 1;
                            NeedToMove = true;
                        }

                        if (NeedToMove) {
                            op = new OperatingCoordinate(x, y);
                            Point point = imageCanvas.getLocationOnScreen();
                            if (finalRobot != null) {
                                finalRobot.mouseMove(x + point.x, y + point.y);
                            }
                            return;
                        }
                    }
                    //增加坐标值
                    imageCanvas.setMouseCoordinate((int) ((1 + Centre.getDouble("MouseMoveOffsets", Main.main.centre.CurrentData) / 100.0) * (x - op.x())), (int) ((1 + Centre.getDouble("MouseMoveOffsets", Main.main.centre.CurrentData) / 100.0) * (y - op.y())));
                    sizeOperate.update(true);
                    op = new OperatingCoordinate(x, y);
                }

                @Override
                public void mouseMoved(MouseEvent e) {
                    if (sizeOperate == null || !Centre.getBoolean("EnableTurnAboveOrBelow", Main.main.centre.CurrentData))
                        return;
                    int x = e.getX();
                    int width = paintPicture.getWidth();
                    if (x <= EDGE_THRESHOLD || x >= width - EDGE_THRESHOLD) {
                        if ((!ComponentInJPanel.isComponentInJPanel(paintPicture, Last)) && hasNext(LastSign)) {
                            Last.setPreferredSize(new Dimension(width / 35, 0));
                            paintPicture.add(Last, BorderLayout.WEST);
                            paintPicture.lastMouseEvent = e;
                            if (!hasNext(NextSign)) paintPicture.remove(Next);
                            paintPicture.revalidate();
                        }
                        if ((!ComponentInJPanel.isComponentInJPanel(paintPicture, Next)) && hasNext(NextSign)) {
                            Next.setPreferredSize(new Dimension(width / 35, 0));
                            paintPicture.add(Next, BorderLayout.EAST);
                            paintPicture.lastMouseEvent = e;
                            if (!hasNext(LastSign)) paintPicture.remove(Last);
                            paintPicture.revalidate();
                        }
                        return;
                    }
                    paintPicture.remove(Last);
                    paintPicture.remove(Next);
                    paintPicture.revalidate();
                }
            };

            addMouseMotionListener(paintPicture.mouseAdapter);
            addMouseWheelListener(new MouseAdapter() {
                //鼠标滚轮事件
                @Override
                public void mouseWheelMoved(MouseWheelEvent e) {
                    //滚轮向后
                    if (e.getWheelRotation() == 1) {
                        if (sizeOperate.adjustPercent(SizeOperate.Reduce) || sizeOperate.adjustPercent(SizeOperate.Reduce)) {
                            imageCanvas.setMouseCoordinate(e.getX(), e.getY());
                            sizeOperate.update(false);
                        }
                    }//滚轮向前
                    else if (e.getWheelRotation() == -1) {
                        if (sizeOperate.adjustPercent(SizeOperate.Enlarge) || sizeOperate.adjustPercent(SizeOperate.Enlarge)) {
                            imageCanvas.setMouseCoordinate(e.getX(), e.getY());
                            sizeOperate.update(false);
                        }
                    }
                }
            });
            addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ESCAPE && fullScreenWindow.isShowing()) {
                        PaintPicture.paintPicture.add(imageCanvas, BorderLayout.CENTER);
                        fullScreenWindow.setVisible(false);
                        Main.main.setVisible(true);
                        sizeOperate.incomeWindowDimension(imageCanvas.getSize());
                        sizeOperate.update(false);
                        return;
                    }
                    imageCanvas.openLONPicture(e.getKeyCode());
                }
            });
        }
    }
}