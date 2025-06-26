package NComponent;

import Listener.ChangeFocusListener;
import Loading.Bundle;
import Runner.Main;
import Tools.ImageManager.PictureInformationStorageManagement;
import Tools.PaintPictureManage;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Method;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class PaintPicturePanel extends JPanel {
    public JPanel MainPanel;
    //缩放比例标签
    public JLabel percentLabel;
    //上部组件
    public JPanel AboveMainPanel;
    //逆时针旋转按钮
    public JButton counterclockwise;
    //还原图片缩放按钮
    public JButton Reset;
    //全屏按钮
    public JButton FullScreen;
    //顺时针旋转按钮
    public JButton clockwise;
    //下部总组件
    public JPanel BelowMainPanel;
    //下部左组件
    public JPanel SouthLeftPanel;
    //下部右组件
    public JPanel SouthRightPanel;
    //图片分辨率
    public JLabel PictureResolution;
    //图片大小
    public JLabel PictureSize;
    //图片缩小按钮
    public JButton reduceButton;
    //图片缩放调节滑动条
    public JSlider PercentSlider;
    //图片放大按钮
    public JButton enlargeButton;
    //图片打开面板
    public static PaintPicturePanel paintPicturePanel;
    //图片全屏窗体
    public FullScreenWindow fullScreenWindow;

    //图片渲染管理
    public PaintPictureManage paintPictureManage;


    //是否启用硬件加速
    public static boolean isEnableHardwareAcceleration;

    private static boolean isMousePressed; // 标记鼠标是否按下
    public PictureInformationStorageManagement pictureInformationStorageManagement;

    private Thread init;

    public boolean isOnlyInit = true;

    //构造方法（无参函数）（用于初始化）
    public PaintPicturePanel() {
        paintPicturePanel = this;
        $$$setupUI$$$();
        try (ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream("data/PictureCacheManagement.obj"))) {
            pictureInformationStorageManagement = (PictureInformationStorageManagement) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            pictureInformationStorageManagement = new PictureInformationStorageManagement();
        }
        AtomicReference<ChangeFocusListener> changeFocusListener = new AtomicReference<>();
        init = new Thread(() -> {
            Main.setUncaughtExceptionHandler(log);
            setLayout(new BorderLayout());
            fullScreenWindow = new FullScreenWindow();
            paintPictureManage = new PaintPictureManage(isEnableHardwareAcceleration, true);
            changeFocusListener.set(new ChangeFocusListener(PaintPicturePanel.paintPicturePanel.paintPictureManage.getPictureViewer().getHandleComponent()));
            MainPanel.add(paintPictureManage.getPictureViewer().getPaintPanel(), BorderLayout.CENTER);
        });
        init.setPriority(Thread.MAX_PRIORITY);
        init.start();
        new Thread(() -> {
            Font font = new Font("", Font.PLAIN, 15);
            try {
                init.join();
            } catch (InterruptedException ignored) {

            }
            reduceButton.setText(Bundle.getMessage("Display_reduce"));
            enlargeButton.setText(Bundle.getMessage("Display_enlarge"));
            PictureResolution.setFont(font);
            PictureSize.setFont(font);
            PercentLabel percentLabel = (PercentLabel) this.percentLabel;
            //设置文本中显示的图片缩放比例
            percentLabel.set((int) paintPictureManage.getResize());
            //设置图片缩放滑动条
            PercentSlider.setMinimum(paintPictureManage.MinResizes);
            PercentSlider.setMaximum(paintPictureManage.MaxResizes);
            PercentSlider.setValue((int) paintPictureManage.getResize());
            //设置图片顺时针按钮可见
            clockwise.setVisible(true);
            //设置图片逆时针按钮可见
            clockwise.setVisible(true);
            //设置重置按钮可见
            Reset.setVisible(true);
            FullScreen.setVisible(true);
            clockwise.setText(Bundle.getMessage("Display_RotateClockwiseButton"));
            Reset.setText(Bundle.getMessage("Display_reset"));
            counterclockwise.setText(Bundle.getMessage("Display_RotateCounterclockwise"));
            FullScreen.setText(Bundle.getMessage("Display_FullScreenButton"));
            init_Listener(changeFocusListener.get());
            this.setLayout(new BorderLayout());
            this.add(MainPanel, BorderLayout.CENTER);
            this.revalidate();
        }).start();
    }

    //构造方法（函数）（用于直接显示图片）
    public PaintPicturePanel(String path) {
        this();
        try {
            init.join();
        } catch (InterruptedException e) {
            Main.getExceptionMessage(e);
        }
        paintPictureManage.openPicture(path);
    }

    private void init_Listener(ChangeFocusListener changeFocusListener) {
        PercentSlider.addMouseListener(changeFocusListener);
        FullScreen.addMouseListener(changeFocusListener);
        FullScreen.addActionListener(e -> {
            paintPictureManage.setFullScreen(true);
        });
        Reset.addMouseListener(changeFocusListener);
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
                if (paintPictureManage.getPictureViewer().getSuperPictureShower().getWillBeFinishedPictureRotationDegrees() != 0 && lastRotationDegrees.getAndSet(paintPictureManage.getPictureViewer().getSuperPictureShower().getWillBeFinishedPictureRotationDegrees()) != paintPictureManage.getPictureViewer().getSuperPictureShower().getWillBeFinishedPictureRotationDegrees()) {
                    //将大小调到合适的比例
                    paintPictureManage.getPictureViewer().getSuperPictureShower().setToOptimalResizes();
                    paintPictureManage.update();
                    times.set(1);
                    break aaa;
                }
                if (times.get() == 1) {
                    //恢复默认大小（100%）
                    paintPictureManage.restoreTheDefaultPercent();
                    paintPictureManage.update();
                } else if (times.get() == 2 && paintPictureManage.getPictureViewer().getSuperPictureShower().getWillBeFinishedPictureRotationDegrees() != 0) {
                    paintPictureManage.getPictureViewer().getSuperPictureShower().setPictureRotationDegrees((byte) 0);
                    paintPictureManage.getPictureViewer().getSuperPictureShower().setToOptimalResizes();
                    paintPictureManage.update();
                } else if (times.get() == 3) {
                    paintPictureManage.restoreTheDefaultPercent();
                    paintPictureManage.update();
                }
                times.getAndIncrement();
            } else {
                //将大小调到合适的比例
                paintPictureManage.getPictureViewer().getSuperPictureShower().setToOptimalResizes();
                paintPictureManage.update();
                times.set(1);
            }
            ClickedTime.set(System.currentTimeMillis());
        });
        //创建图片顺时针按钮监听器
        clockwise.addActionListener(e -> {
            paintPictureManage.getPictureViewer().getSuperPictureShower().addPictureRotationDegrees((byte) -1);
            paintPictureManage.update();
        });
        clockwise.addMouseListener(changeFocusListener);
        //创建图片顺时针按钮监听器
        counterclockwise.addActionListener(e -> {
            paintPictureManage.getPictureViewer().getSuperPictureShower().addPictureRotationDegrees((byte) 1);
            paintPictureManage.update();
        });
        counterclockwise.addMouseListener(changeFocusListener);
        reduceButton.addMouseListener(new MouseAdapter() {
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
                        if (!reduceButton.isEnabled()) return;
                        double result = paintPictureManage.adjustPercent(PaintPictureManage.Reduce);
                        if (result != 0) {
                            paintPictureManage.getPictureViewer().getSuperPictureShower().addPictureResizes(result);
                            paintPictureManage.update();
                        }

                        //抛出异常
                        try {
                            //线程休眠
                            Thread.sleep(16);
                        } catch (InterruptedException ex) {
                            break;
                        }
                    } while (!isReleased);
                    PaintPicturePanel.paintPicturePanel.paintPictureManage.getPictureViewer().getHandleComponent().requestFocus();
                }).start();

            }

            //鼠标放出触发
            @Override
            public void mouseReleased(MouseEvent e) {
                isReleased = true;
            }
        });


        PercentSlider.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                isMousePressed = true;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isMousePressed = false;
            }
        });

        PercentSlider.addChangeListener(e -> {
            if (isMousePressed) {
                PercentLabel percentLabel = (PercentLabel) this.percentLabel;
                percentLabel.set(PercentSlider.getValue());
                paintPictureManage.getPictureViewer().getSuperPictureShower().setPictureResizes(PercentSlider.getValue());
                paintPictureManage.update();
            }
        });
        enlargeButton.addMouseListener(new MouseAdapter() {
            boolean isDown = false;

            @Override
            public void mousePressed(MouseEvent e) {//点击、长按触发
                isDown = false;
                new Thread(() -> {
                    do {
                        if (!enlargeButton.isEnabled()) return;
                        double result = paintPictureManage.adjustPercent(PaintPictureManage.Enlarge);
                        if (result != 0) {
                            paintPictureManage.getPictureViewer().getSuperPictureShower().addPictureResizes(result);
                            paintPictureManage.update();
                        }
                        PaintPicturePanel.paintPicturePanel.paintPictureManage.getPictureViewer().getHandleComponent().requestFocus();
                        PaintPicturePanel.paintPicturePanel.paintPictureManage.update();
                        try {
                            Thread.sleep(16);
                        } catch (InterruptedException ex) {
                            break;
                        }
                    } while (!isDown);
                }).start();
            }

            @Override
            public void mouseReleased(MouseEvent e) {//鼠标放出触发
                isDown = true;
            }
        });
    }


    private void createUIComponents() {
        // TODO: place custom component creation code here
        percentLabel = new PercentLabel();
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
        MainPanel = new JPanel();
        MainPanel.setLayout(new BorderLayout(0, 0));
        AboveMainPanel = new JPanel();
        AboveMainPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        MainPanel.add(AboveMainPanel, BorderLayout.NORTH);
        counterclockwise = new JButton();
        this.$$$loadButtonText$$$(counterclockwise, this.$$$getMessageFromBundle$$$("messages", "Display_RotateCounterclockwise"));
        AboveMainPanel.add(counterclockwise);
        Reset = new JButton();
        this.$$$loadButtonText$$$(Reset, this.$$$getMessageFromBundle$$$("messages", "Display_reset"));
        AboveMainPanel.add(Reset);
        FullScreen = new JButton();
        this.$$$loadButtonText$$$(FullScreen, this.$$$getMessageFromBundle$$$("messages", "Display_FullScreenButton"));
        AboveMainPanel.add(FullScreen);
        clockwise = new JButton();
        this.$$$loadButtonText$$$(clockwise, this.$$$getMessageFromBundle$$$("messages", "Display_RotateClockwiseButton"));
        AboveMainPanel.add(clockwise);
        percentLabel.setText("");
        AboveMainPanel.add(percentLabel);
        BelowMainPanel = new JPanel();
        BelowMainPanel.setLayout(new BorderLayout(1, 2));
        MainPanel.add(BelowMainPanel, BorderLayout.SOUTH);
        SouthLeftPanel = new JPanel();
        SouthLeftPanel.setLayout(new FlowLayout(FlowLayout.LEADING, 5, 5));
        BelowMainPanel.add(SouthLeftPanel, BorderLayout.WEST);
        PictureResolution = new JLabel();
        PictureResolution.setText("");
        SouthLeftPanel.add(PictureResolution);
        PictureSize = new JLabel();
        PictureSize.setText("");
        SouthLeftPanel.add(PictureSize);
        SouthRightPanel = new JPanel();
        SouthRightPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        BelowMainPanel.add(SouthRightPanel, BorderLayout.EAST);
        reduceButton = new JButton();
        this.$$$loadButtonText$$$(reduceButton, this.$$$getMessageFromBundle$$$("messages", "Display_reduce"));
        SouthRightPanel.add(reduceButton);
        PercentSlider = new JSlider();
        SouthRightPanel.add(PercentSlider);
        enlargeButton = new JButton();
        this.$$$loadButtonText$$$(enlargeButton, this.$$$getMessageFromBundle$$$("messages", "Display_enlarge"));
        SouthRightPanel.add(enlargeButton);
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
        return MainPanel;
    }

}
