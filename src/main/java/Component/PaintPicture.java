package Component;//导入包

import Listener.ChangeFocusListener;
import Runner.Main;
import Settings.Centre;
import Size.OperatingCoordinate;
import Size.SizeOperate;
import Tools.EqualsProportion;
import Tools.ImageManager.GetImageInformation;
import Tools.ImageManager.ImageRotationHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.MemoryImageSource;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

//创建类
public class PaintPicture extends JPanel {
    //图片打开面板
    public static PaintPicture paintPicture;
    //上部
    public JPanel On;
    //下部
    public JPanel Under;
    //图片放大按钮
    public JButton biggest;
    //图片缩小按钮
    public JButton smallest;
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
    public MyCanvas myCanvas;
    //判断按钮是否被按下
    private static boolean IsDragging;
    //移动图片时，鼠标最开始的坐标（对于桌面）
    Point mouseLocation;


    //构造方法（函数）
    public PaintPicture(String path) {
        paintPicture = this;
        //获取当前图片路径下所有图片
        ArrayList<String> CurrentPathOfPicture = GetImageInformation.getCurrentPathOfPicture(path);
        percentLabel = new PercentLabel();
        //向控制台输出打开文件路径
        System.out.println("Opened:\t\"" + path + "\"");
        //创建画布
        myCanvas = new MyCanvas(path);
        sizeOperate = new SizeOperate(myCanvas, myCanvas.getSize());
        //设置文本中显示的图片缩放比例
        percentLabel.set((int) sizeOperate.getPercent());
        //添加面板大小改变监听器
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
//                super.componentResized(e);
                sizeOperate.incomeWindowDimension(myCanvas.getSize());
                sizeOperate.update();
            }
        });
        setLayout(new BorderLayout(1, 1));
        Robot robot = null;
        try {
            robot = new Robot();
        } catch (AWTException e) {
            System.out.println("Couldn't get Mouse Information");
        }
        biggest = new JButton("enlarge");
        Robot finalRobot = robot;
        Image image = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(0, 0, new int[0], 0, 0));
        final boolean[] EnableCursorDisplay = new boolean[1];
        myCanvas.addMouseListener(new MouseAdapter() {
            //鼠标一按下就触发
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() != MouseEvent.BUTTON1) return;
                op = new OperatingCoordinate(e.getX(), e.getY());
                EnableCursorDisplay[0] = Centre.getBoolean("EnableCursorDisplay", Main.main.centre.CurrentData);
                if (EnableCursorDisplay[0]) return;
                setCursor(Toolkit.getDefaultToolkit().createCustomCursor(image, new Point(0, 0), null));
                mouseLocation = MouseInfo.getPointerInfo().getLocation();
                if (ShowingSize != null && LocationOnScreen != null && ShowingSize.equals(sizeOperate.getWindowSize()) && LocationOnScreen.equals(myCanvas.getLocationOnScreen()))
                    return;
                LocationOnScreen = myCanvas.getLocationOnScreen();
                ShowingSize = sizeOperate.getWindowSize();

                int minX = -LocationOnScreen.x, minY = -LocationOnScreen.y;
                if (minX < 0) minX = 0;
                if (minY < 0) minY = 0;
                MinPoint = new Point(minX, minY);

                int maxX = ShowingSize.width, maxY = ShowingSize.height;
                int x = ShowingSize.width + LocationOnScreen.x;
                int y = ShowingSize.height + LocationOnScreen.y;
                if (x > SizeOperate.FreeOfScreenSize.width)
                    maxX = SizeOperate.FreeOfScreenSize.width + LocationOnScreen.x;
                if (y > SizeOperate.FreeOfScreenSize.height)
                    maxY = SizeOperate.FreeOfScreenSize.height + LocationOnScreen.y;
                MaxPoint = new Point(maxX, maxY);
                setLayout(new BorderLayout());
            }


            //鼠标放出触发
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() != MouseEvent.BUTTON1) return;
                if (!EnableCursorDisplay[0]) {
                    setCursor(Cursor.getDefaultCursor());
                    if (finalRobot != null)
                        finalRobot.mouseMove(mouseLocation.x, mouseLocation.y);
                }
            }
        });
        myCanvas.addMouseMotionListener(
                new MouseAdapter() {
                    @Override
                    public void mouseDragged(MouseEvent e) {
                        if (!SwingUtilities.isLeftMouseButton(e))
                            return;
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
                                Point point = myCanvas.getLocationOnScreen();
                                if (finalRobot != null) {
                                    finalRobot.mouseMove(x + point.x, y + point.y);
                                }
                                return;
                            }
                        }
                        //增加坐标值
                        myCanvas.setMouseCoordinate((int) ((1 + Centre.getDouble("MouseMoveOffsets", Main.main.centre.CurrentData) / 100.0) * (x - op.x())), (int) ((1 + Centre.getDouble("MouseMoveOffsets", Main.main.centre.CurrentData) / 100.0) * (y - op.y())));
                        sizeOperate.update();
                        op = new OperatingCoordinate(x, y);
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
                    if (sizeOperate.adjustPercent(SizeOperate.Reduce) || sizeOperate.adjustPercent(SizeOperate.Reduce)) {
                        sizeOperate.update();
                    }
                }//滚轮向前
                else if (e.getWheelRotation() == -1) {
                    if (sizeOperate.adjustPercent(SizeOperate.Enlarge) || sizeOperate.adjustPercent(SizeOperate.Enlarge)) {
                        sizeOperate.update();
                    }
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
                        if (!biggest.isEnabled()) return;
                        if (sizeOperate.adjustPercent(SizeOperate.Enlarge)) {
                            sizeOperate.update();
                        }
                        try {
                            Thread.sleep(16);
                        } catch (InterruptedException ex) {
                            break;
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
                        if (!smallest.isEnabled()) return;
                        if (sizeOperate.adjustPercent(SizeOperate.Reduce)) {
                            sizeOperate.update();
                        }

                        //抛出异常
                        try {
                            //线程休眠
                            Thread.sleep(16);
                        } catch (InterruptedException ex) {
                            break;
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
        Under = new JPanel();
        On = getjPanel(changeFocusListener);
        Under.setLayout(new GridLayout(1, 2));
        //添加组件
        Under.add(smallest);
        Under.add(biggest);
        //设置菜单为面板下方并添加至组件中
        add(Under, BorderLayout.SOUTH);
        //设置为面板上方并添加至组件中
        add(On, BorderLayout.NORTH);
        //添加画布至组件中
        add(myCanvas, BorderLayout.CENTER);
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
                }
            }
        });
        myCanvas.requestFocus();
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
        });
        Reset.addMouseListener(changeFocusListener);
        JButton FlipHorizontally = new JButton("FlipHorizontally");
        FlipHorizontally.addMouseListener(changeFocusListener);
        //将图片左转按钮添加到组件中
        On.add(TurnLeft);
        //将重置按钮添加到组件中
        On.add(Reset);
        //将图片右转按钮添加到组件中
        On.add(TurnRight);
        //将比例显示添加到组件中
        On.add(percentLabel);
        return On;
    }

    //改变图片路径
    public void changePicturePath(String path) {
        myCanvas = new MyCanvas(path);
        sizeOperate = new SizeOperate(myCanvas, myCanvas.getSize());
        add(myCanvas, BorderLayout.CENTER);
    }

    public class MyCanvas extends JComponent {
        //图片路径
        String path;
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
            sizeOperate.update();
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
            if (NewWindow == null || NewWindow.width == 0 || NewWindow.height == 0) {
                return;
            }
            this.g = g;
            var graphics2D = (Graphics2D) g;
            graphics2D.rotate(Math.toRadians(RotationDegrees * 90));
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
            if (NewWindow != null) {
                WindowWidth = NewWindow.getWidth();
                WindowHeight = NewWindow.getHeight();
                if (LastWindow == null || LastWindow.width == 0 || LastWindow.height == 0) {
                    LastWindow = NewWindow;
                }
                LastWindowWidth = LastWindow.getWidth();
                LastWindowHeight = LastWindow.getHeight();
            }
            //判断窗体、图片缩放比例相比于之前是否存在改变（如果没有，则执行本代码）
            if (RotationDegrees == lastRotationDegrees && LastPercent == sizeOperate.getPercent() && LastWindow != null && LastWindow.equals(NewWindow)) {
                X += mouseX;
                Y += mouseY;
                if (RotationDegrees == 0) {
                    if (X > WindowWidth) X = WindowWidth;
                    if (Y > WindowHeight) Y = WindowHeight;
                    if (X + lastWidth < 0) X = -lastWidth;
                    if (Y + lastWidth < 0) Y = -lastWidth;
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
                return;
            }
            //判断图片缩放比例是否与上次相同
            if (RotationDegrees != lastRotationDegrees) {
                sizeOperate.setPercent(sizeOperate.getPictureOptimalSize());
                Point point = ImageRotationHelper.getRotatedCoord((int) X, (int) Y, 360 - 90 * RotationDegrees, (int) lastWidth, (int) lastHeight);
                X = point.getX();
                Y = point.getY();
            }

//            if (NewWindow != null && NewWindow.getHeight() * NewWindow.getWidth() > lastHeight * lastWidth) {
//                sizeOperate.setPercent(sizeOperate.getPercent() + 1);//面板·放大图片比例会变小，没写好该校正代码，暂以+1来校对，以后优化！
//            }

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
            double FinalX = X, FinalY = Y;

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
            if (paintPicture.biggest != null)
                paintPicture.biggest.setEnabled(!paintPicture.sizeOperate.isTheBiggestRatio());
            //检查比例是否为最小值，如果为最小就把放大按钮禁用
            if (paintPicture.smallest != null)
                paintPicture.smallest.setEnabled(!paintPicture.sizeOperate.isTheSmallestRatio());
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
            System.out.println(1);
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