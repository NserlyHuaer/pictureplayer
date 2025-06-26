package Tools;

import NComponent.AdvancedDownloadSpeed;
import NComponent.PaintPicturePanel;
import NComponent.PercentLabel;
import Runner.Main;
import Settings.Centre;
import Size.OperatingCoordinate;
import Tools.ImageManager.GetImageInformation;
import Tools.ImageManager.PictureInformationStorageManagement;
import com.jogamp.opengl.awt.GLJPanel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

@Getter
@Slf4j
public class PaintPictureManage {
    //默认缩放比例
    private final short Default;
    //设置放大时，引用
    public static final int Enlarge = 0;
    //设置缩小时，引用
    public static final int Reduce = 1;
    //最大缩放比例
    public final short MaxResizes = 1500;
    //最小缩放比例
    public final short MinResizes = 2;
    //当前最适合组件的比例
    public double FittestPercent;
    //最适合的调节比例
    private int AdjustPercent;
    //屏幕分辨率
    public static final Dimension ScreenSize = Toolkit.getDefaultToolkit().getScreenSize();
    //可用屏幕尺寸
    public static final Dimension FreeOfScreenSize;
    //图片路径
    @Getter
    String filePath;
    //判断是否启用硬件加速（针对本图片渲染）
    private boolean isEnableHardware;
    //上次图片路径
    String lastPath;
    //图片hashcode
    @Getter
    String picture_hashcode;
    //当前图片
    private BufferedImage image;
    //模糊后的bufferedImage
    private BufferedImage BlurBufferedImage;
    //移动图片时，鼠标最开始的坐标（对于桌面）
    Point mouseLocation;
    //打开上一个图片
    private static final int LastSign = 0x25;
    //打开下一个图片
    private static final int NextSign = 0x27;
    //创建鼠标坐标管理对象
    OperatingCoordinate op;
    //鼠标最小移动坐标位
    Point MinPoint;
    //鼠标最大移动坐标位
    Point MaxPoint;
    //图片显示器尺寸
    Dimension ShowingSize;
    //图片渲染器在屏幕上的坐标
    Point LocationOnScreen;
    // 定义边缘范围为5像素
    private MouseAdapter mouseAdapter;
    public boolean isOnlyInit = true;
    private PictureViewer pictureViewer;
    private File lastPicturePathParent;
    @Getter
    //当前图片路径下所有图片
    ArrayList<String> CurrentPathOfPicture;


    static {
        //获取可用屏幕分辨率
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();

        int width = gd.getDisplayMode().getWidth();
        int height = gd.getDisplayMode().getHeight();
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            height -= 45;
        }

        FreeOfScreenSize = new Dimension(width, height);
    }


    public PaintPictureManage(boolean EnableHardwareAcceleration, boolean EnableDoubleBuffered) {
        pictureViewer = new PictureViewer(EnableHardwareAcceleration, EnableDoubleBuffered);
        FittestPercent = Default = 100;
        pictureViewer.getSuperPictureShower().setPictureResizes(100);
        pictureViewer.getSuperPictureShower().setMinResizes(MinResizes);
        pictureViewer.getSuperPictureShower().setMaxResizes(MaxResizes);
        pictureViewer.getSuperPictureShower().setPaintedAction(() -> {
            int resizes = (int) pictureViewer.getSuperPictureShower().getFinishedPictureResizes();
            PaintPicturePanel.paintPicturePanel.PercentSlider.setValue(resizes);
            PercentLabel percentLabel = (PercentLabel) PaintPicturePanel.paintPicturePanel.percentLabel;
            percentLabel.set(resizes);
            //检查比例是否为最大值，如果为最大就把放大按钮禁用
            if (PaintPicturePanel.paintPicturePanel.enlargeButton != null) {
                PaintPicturePanel.paintPicturePanel.enlargeButton.setEnabled(!isTheBiggestRatio());
                if (!PaintPicturePanel.paintPicturePanel.enlargeButton.isEnabled())
                    pictureViewer.getHandleComponent().requestFocus();
            }
            //检查比例是否为最小值，如果为最小就把放大按钮禁用
            if (PaintPicturePanel.paintPicturePanel.reduceButton != null) {
                PaintPicturePanel.paintPicturePanel.reduceButton.setEnabled(!isTheSmallestRatio());
                if (!PaintPicturePanel.paintPicturePanel.reduceButton.isEnabled())
                    pictureViewer.getHandleComponent().requestFocus();
            }

        });
        new Thread(this::init_listener).start();
    }

    private double decide(double size) {
        double result = 0;
        if (size > MaxResizes) {
            result = MaxResizes;
        } else {
            result = size;
        }
        if (size < MinResizes) {
            result = MinResizes;
        }
        return result;
    }


    //恢复默认缩放比例
    public void restoreTheDefaultPercent() {
        pictureViewer.getSuperPictureShower().setPictureResizes(Default);
    }


    //是否当前图片显示比例是否是最大比例
    public boolean isTheBiggestRatio() {
        return getResize() >= MaxResizes;
    }

    //是否当前图片显示比例是否是最小比例
    public boolean isTheSmallestRatio() {
        return getResize() <= MinResizes;
    }

    //获取缩放比例
    public synchronized double getResize() {
        return pictureViewer.getSuperPictureShower().getWillBeFinishedPictureResizes();
    }

    //调节比例（如果返回值为true表示需要刷新，反之不需刷新）
    public double adjustPercent(int operate) {
        double resize = pictureViewer.getSuperPictureShower().getWillBeFinishedPictureResizes();
        if ((operate == Enlarge && resize == MaxResizes) || (operate == Reduce && resize == MinResizes)) {
            return 0;
        }
        double result = 0;
        switch (operate) {
            case Enlarge -> {
                if (AdjustPercent <= 0) {
                    if (resize < FittestPercent) result = decide(4 + resize);
                    else {
                        result = decide(11 + resize);
                    }
                } else if (resize < FittestPercent) {
                    result = decide(AdjustPercent + resize);
                } else if (resize > FittestPercent) {
                    result = decide(2 * AdjustPercent + resize);
                }
                resize = result;
                if (resize > MaxResizes) {
                    resize = MaxResizes;
                }
            }
            case Reduce -> {
                if (AdjustPercent <= 0) {
                    if (resize < FittestPercent) result = decide(-4 + resize);
                    else {
                        result = decide(-11 + resize);
                    }
                } else if (resize < FittestPercent) result = decide(-AdjustPercent + resize);
                else if (resize > FittestPercent) {
                    result = decide(-2 * AdjustPercent + resize);
                }
                resize = result;
                if (resize < MinResizes) {
                    resize = MaxResizes;
                }
            }
        }
        return resize - pictureViewer.getSuperPictureShower().getWillBeFinishedPictureResizes();
    }

    //刷新图片
    public void update() {
        pictureViewer.getSuperPictureShower().paintPicture();
    }

    //改变图片路径
    public void openPicture(String path) {
        openPicture(path, GetImageInformation.getHashcode(new File(path)));
    }

    public void openPicture(String path, String picture_hashcode) {
        BufferedImage image = PictureInformationStorageManagement.getImage(PaintPicturePanel.paintPicturePanel.pictureInformationStorageManagement.getCachedPicturePath(path, picture_hashcode));
        openPicture(image, path, picture_hashcode);
    }

    public void openPicture(final BufferedImage image, String path, String picture_hashcode) {
        //如果字符串前缀与后缀包含"，则去除其中的"
        if (path.startsWith("\"") && path.endsWith("\"")) {
            path = path.substring(1, path.length() - 1);
        }

        this.filePath = path;
        this.picture_hashcode = picture_hashcode;

        String finalPath = path;
        new Thread(() -> {
            setPictureInformationOnComponent(finalPath);
            //若这两个文件父目录不相同
            loadPictureInTheParent(finalPath);
        }).start();


        if (isEnableHardware) {
            BlurBufferedImage = GetImageInformation.CastToTYPE_INT_RGB(image);
            image.flush();
            this.image = BlurBufferedImage;
        }
        pictureViewer.getSuperPictureShower().setNewPicture(image);
        Dimension Component = getPictureViewer().getHandleComponent().getSize();
        Dimension getPictureSize = new Dimension(pictureViewer.getSuperPictureShower().getPictureWidth(), pictureViewer.getSuperPictureShower().getPictureHeight());
        AdjustPercent = (int) (((Math.abs(Component.getHeight() - getPictureSize.height) / 5.5 / getPictureSize.height) + (Math.abs(Component.getWidth() - getPictureSize.width) / 5.5 / getPictureSize.width)) / 2);
    }

    public void openPicture(final BufferedImage image, String path) {
        openPicture(image, path, GetImageInformation.getHashcode(new File(path)));
    }


    public void close() {
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream("data/PictureCacheManagement.obj"))) {
            objectOutputStream.writeObject(PaintPicturePanel.paintPicturePanel.pictureInformationStorageManagement);
            objectOutputStream.flush();
        } catch (IOException e) {
            log.error(Main.getExceptionMessage(e));
        }
        pictureViewer.getSuperPictureShower().close();
        System.gc();
    }

    //打开上一个/下一个图片
    public void openLONPicture(int sign) {
        for (String path : CurrentPathOfPicture) {
            File file = new File(path);
            if (!file.exists()) CurrentPathOfPicture.remove(path);
        }
        int CurrentIndex = CurrentPathOfPicture.indexOf(filePath);
        switch (sign) {
            case LastSign -> {
                if (CurrentIndex > 0) {
                    //向控制台输出打开文件路径
                    log.info("Opened:\"{}\"", CurrentPathOfPicture.get(CurrentIndex - 1));
                    openPicture(CurrentPathOfPicture.get(CurrentIndex - 1));
                    update();
                }
            }
            case NextSign -> {
                if (CurrentIndex + 1 < CurrentPathOfPicture.size()) {
                    //向控制台输出打开文件路径
                    log.info("Opened:\"{}\"", CurrentPathOfPicture.get(CurrentIndex + 1));
                    openPicture(CurrentPathOfPicture.get(CurrentIndex + 1));
                    update();
                }
            }
        }
    }

    //判断是否有下一个图片
    public boolean hasNext(int sign) {
        int CurrentIndex = CurrentPathOfPicture.indexOf(filePath);
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

    //设置X值大小
    public void setX(int x) {
        pictureViewer.getSuperPictureShower().setPictureShowCoordinate(x, pictureViewer.getSuperPictureShower().getWillBeFinishedPictureCoordinate().y);
    }

    //设置Y值大小
    public void setY(int y) {
        pictureViewer.getSuperPictureShower().setPictureShowCoordinate(pictureViewer.getSuperPictureShower().getWillBeFinishedPictureCoordinate().x, y);
    }

    //初始化监听器
    private void init_listener() {
        Robot robot = null;
        try {
            robot = new Robot();
        } catch (AWTException e) {
            log.warn("Couldn't get Mouse Information");
        }
        Robot finalRobot = robot;
        Image image = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(0, 0, new int[0], 0, 0));
        final boolean[] EnableCursorDisplay = new boolean[1];
        GLJPanel gljPanel = (GLJPanel) getPictureViewer().getPaintPanel();
        gljPanel.addMouseListener(new MouseAdapter() {
            //鼠标一按下就触发
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() != MouseEvent.BUTTON1) return;
                op = new OperatingCoordinate(e.getX(), e.getY());
                EnableCursorDisplay[0] = Centre.getBoolean("EnableCursorDisplay", Main.main.centre.CurrentData);
                if (EnableCursorDisplay[0]) return;
                getPictureViewer().getHandleComponent().setCursor(Toolkit.getDefaultToolkit().createCustomCursor(image, new Point(0, 0), null));
                mouseLocation = MouseInfo.getPointerInfo().getLocation();
                if (ShowingSize != null && LocationOnScreen != null && ShowingSize.equals(getPictureViewer().getHandleComponent().getSize()) && LocationOnScreen.equals(getLocationOnScreen()))
                    return;
                LocationOnScreen = getPictureViewer().getHandleComponent().getLocationOnScreen();
                ShowingSize = getPictureViewer().getHandleComponent().getSize();

                int minX = -LocationOnScreen.x, minY = -LocationOnScreen.y;
                if (minX < 0) minX = 0;
                if (minY < 0) minY = 0;
                MinPoint = new Point(minX, minY);

                int maxX = ShowingSize.width, maxY = ShowingSize.height;
                int x = ShowingSize.width + LocationOnScreen.x;
                int y = ShowingSize.height + LocationOnScreen.y;
                if (x > FreeOfScreenSize.width)
                    maxX = FreeOfScreenSize.width - LocationOnScreen.x;
                if (y > FreeOfScreenSize.height)
                    maxY = FreeOfScreenSize.height - LocationOnScreen.y;
                MaxPoint = new Point(maxX, maxY);
            }


            //鼠标放出触发
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() != MouseEvent.BUTTON1) return;
                if (!EnableCursorDisplay[0]) {
                    getPictureViewer().getHandleComponent().setCursor(Cursor.getDefaultCursor());
                    if (finalRobot != null) finalRobot.mouseMove(mouseLocation.x, mouseLocation.y);
                }
            }
        });
        mouseAdapter = new MouseAdapter() {
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
                        Point point = getLocationOnScreen();
                        if (finalRobot != null) {
                            finalRobot.mouseMove(x + point.x, y + point.y);
                        }
                        return;
                    }
                }
                int x1 = (int) ((1 + Centre.getDouble("MouseMoveOffsets", Main.main.centre.CurrentData) / 100.0) * (x - op.x()));
                int y1 = (int) ((1 + Centre.getDouble("MouseMoveOffsets", Main.main.centre.CurrentData) / 100.0) * (y - op.y()));
                if (x1 == 0 && y1 == 0) {
                    return;
                }
                //增加坐标值
                getPictureViewer().getSuperPictureShower().addPictureShowCoordinate(x1, y1);
                update();
                op = new OperatingCoordinate(x, y);
            }
        };

        gljPanel.addMouseMotionListener(mouseAdapter);
        gljPanel.addMouseWheelListener(new MouseAdapter() {
            //鼠标滚轮事件
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                double result = 0;
                //滚轮向后
                if (e.getWheelRotation() == 1) {
                    result = adjustPercent(Reduce) + adjustPercent(Reduce);

                }//滚轮向前
                else if (e.getWheelRotation() == -1) {
                    result = adjustPercent(Enlarge) + adjustPercent(Enlarge);
                }
                if (result != 0) {
                    getPictureViewer().getSuperPictureShower().addPictureResizesWithMouse(result, e.getX(), e.getY());
                    update();
                }
            }
        });
        gljPanel.addKeyListener(new KeyAdapter() {
            @Override
            public synchronized void keyReleased(KeyEvent e) {
                int KeyCode = e.getKeyCode();
                switch (KeyCode) {
                    case KeyEvent.VK_ESCAPE -> {
                        if (PaintPicturePanel.paintPicturePanel.fullScreenWindow.isShowing()) {
                            setFullScreen(false);
                            return;
                        }
                    }
                    case KeyEvent.VK_F11 -> {
                        setFullScreen(!PaintPicturePanel.paintPicturePanel.fullScreenWindow.isShowing());
                        return;
                    }
                }

                openLONPicture(KeyCode);
            }
        });
    }

    public void setFullScreen(boolean fullScreen) {
        if (PaintPicturePanel.paintPicturePanel.fullScreenWindow == null || Main.main == null)
            return;
        if (fullScreen == PaintPicturePanel.paintPicturePanel.fullScreenWindow.isShowing() && fullScreen != Main.main.isShowing()) {
            return;
        }
        if (fullScreen) {
            PaintPicturePanel.paintPicturePanel.fullScreenWindow.setImageCanvas(pictureViewer.getSuperPictureShower());
            Main.main.getGraphics().dispose();
        } else {
            PaintPicturePanel.paintPicturePanel.MainPanel.add(getPictureViewer().getHandleComponent(), BorderLayout.CENTER);
        }
        Main.main.setVisible(!fullScreen);
        PaintPicturePanel.paintPicturePanel.fullScreenWindow.setVisible(fullScreen);
        update();

        getPictureViewer().getHandleComponent().setCursor(Cursor.getDefaultCursor());
    }

    private void loadPictureInTheParent(String picturePath) {
        File pictureParent = new File(picturePath).getParentFile();
        if (!pictureParent.equals(lastPicturePathParent)) {
            //获取当前图片路径下所有图片
            CurrentPathOfPicture = GetImageInformation.getCurrentPathOfPicture(picturePath);
            lastPicturePathParent = pictureParent;
            Main.main.reviewPictureList(CurrentPathOfPicture);
        }
    }

    //显示图片大小、分辨率等信息
    private void setPictureInformationOnComponent(String path) {
        File PictureFile = new File(path);
        Dimension dimension = null;
        try {
            dimension = GetImageInformation.getImageSize(PictureFile);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        PaintPicturePanel.paintPicturePanel.PictureSize.setText(AdvancedDownloadSpeed.formatBytes(PictureFile.length()));
        if (dimension != null)
            PaintPicturePanel.paintPicturePanel.PictureResolution.setText((int) dimension.getWidth() + "x" + (int) dimension.getHeight());
    }

}
