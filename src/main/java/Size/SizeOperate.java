package Size;

import java.awt.*;
import java.io.IOException;

import Component.PaintPicture;

public class SizeOperate {
    //默认缩放比例
    private final short Default;
    //设置放大时，引用
    public static final int Enlarge = 0;
    //设置缩小时，引用
    public static final int Reduce = 1;
    //当前缩放比例
    private double percent = 0;
    //最大缩放比例
    public final short MaxPercent = 1500;
    //最小缩放比例
    public final short MinPercent = 2;
    //当前图片渲染器
    private PaintPicture.MyCanvas myCanvas;
    //当前组件信息
    private Dimension Component;
    //当前最适合组建的比例
    public double FittestPercent;
    //最适合的调节比例
    private int AdjustPercent;
    //默认调节比例
    public int DefaultAdjustPercent;
    //图片信息类
    private GetPictureSize getPictureSize;
    //屏幕分辨率
    public static final Dimension ScreenSize = Toolkit.getDefaultToolkit().getScreenSize();
    //可用屏幕尺寸
    public static final Dimension FreeOfScreenSize;

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

    //改变图片渲染器
    public void changeCanvas(PaintPicture.MyCanvas myCanvas) {
        this.myCanvas = myCanvas;
        if (myCanvas.getPath() != null) {
            try {
                getPictureSize = new GetPictureSize(myCanvas.getPath());
                if (Component != null) {
                    AdjustPercent = (int) (((Math.abs(Component.getHeight() - getPictureSize.height) / 5.5 / getPictureSize.height) + (Math.abs(Component.getWidth() - getPictureSize.width) / 5.5 / getPictureSize.width)) / 2);
                }
            } catch (IOException e) {
                System.out.println("Error:Could not read picture information\n" + e);
            }
        }
        FittestPercent = getPictureOptimalSize();
        setPercent(FittestPercent);
        update();
    }


    public SizeOperate(PaintPicture.MyCanvas myCanvas, Dimension Component) {
        this.Component = Component;
        this.myCanvas = myCanvas;
        FittestPercent = percent = Default = 100;

    }

    public SizeOperate(PaintPicture.MyCanvas myCanvas, short defaultPercent, Dimension Component) {
        this.myCanvas = myCanvas;
        FittestPercent = percent = Default = defaultPercent;
        this.Component = Component;
    }

    //获取窗体大小
    public Dimension getWindowSize() {
        return Component;
    }

    //获取窗体高度
    public int getWindowHeight() {
        return Component.height;
    }

    //获取窗体宽度
    public int getWindowWight() {
        return Component.height;
    }

    //传入窗体信息（此类无法直接获取窗体信息）
    public void incomeWindowDimension(Dimension window) {
        if (window != null) {
            this.Component = window;
            FittestPercent = getPictureOptimalSize();
        }
        if (window != null) {
            if (getPictureSize == null)
                try {
                    getPictureSize = new GetPictureSize(myCanvas.getPath());
                } catch (IOException e) {
                    return;
                }
            AdjustPercent = (int) (((Math.abs(window.getHeight() - getPictureSize.height) / 5.5 / getPictureSize.height) + (Math.abs(window.getWidth() - getPictureSize.width) / 5.5 / getPictureSize.width)) / 2);
        }
    }

    //获取默认缩放比例
    public short getDefaultPercent() {
        return Default;
    }


    private double decide(double size) {
        double result = 0;
        if (size > MaxPercent) {
            result = MaxPercent;
        } else {
            result = size;
        }
        if (size < MinPercent) {
            result = MinPercent;
        }
        return result;
    }

    //获取图片最佳比例
    public double getPictureOptimalSize() {
        if (Component == null || Component.width == 0 || Component.height == 0) {
            System.out.println("Error:Could not get window optimal size");
            return Default;
        }
        int PictureWidth = myCanvas.getImageWidth();
        int PictureHeight = myCanvas.getImageHeight();
        if (myCanvas.getDegrees() / 90 % 2 == 1) {
            int temp = PictureHeight;
            PictureHeight = PictureWidth;
            PictureWidth = temp;
        }
        if (PictureHeight == 0 && PictureWidth == 0) {
            return Default;
        }
        double WindowsWidth = Component.getWidth();
        double WindowsHeight = Component.getHeight();
        return Math.min(WindowsWidth * 100 / PictureWidth, WindowsHeight * 100 / PictureHeight);
    }

    //恢复默认缩放比例
    public void restoreTheDefaultPercent() {
        percent = Default;
    }


    //是否当前图片显示比例是否是最大比例
    public boolean isTheBiggestRatio() {
        return (short) getPercent() >= MaxPercent - 2;
    }

    //是否当前图片显示比例是否是最小比例
    public boolean isTheSmallestRatio() {
        return (short) getPercent() <= MinPercent + 2;
    }

    //获取缩放比例
    public synchronized double getPercent() {
        return percent;
    }

    //设置缩放比例
    public void setPercent(double percent) {
        if (percent > MaxPercent) {
            this.percent = MaxPercent;
        } else if (percent < MinPercent) {
            this.percent = MinPercent;
        } else this.percent = percent;
    }

    //调节比例（如果返回值为true表示需要刷新，反之不需刷新）
    public boolean adjustPercent(int operate) {
        if ((operate == Enlarge && percent == MaxPercent) || (operate == Reduce && percent == MinPercent)) {
            return false;
        }
        double result = 0;
        switch (operate) {
            case Enlarge -> {
                if (AdjustPercent <= 0) {
                    if (percent < FittestPercent) result = decide(4 + percent);
                    else {
                        result = decide(11 + percent);
                    }
                } else if (percent < FittestPercent) {
                    result = decide(AdjustPercent + percent);
                } else if (percent > FittestPercent) {
                    result = decide(2 * AdjustPercent + percent);
                }
                percent = result;
                if (percent > MaxPercent) {
                    percent = MaxPercent;
                }
            }
            case Reduce -> {
                if (AdjustPercent <= 0) {
                    if (percent < FittestPercent) result = decide(-4 + percent);
                    else {
                        result = decide(-11 + percent);
                    }
                } else if (percent < FittestPercent) result = decide(-AdjustPercent + percent);
                else if (percent > FittestPercent) {
                    result = decide(-2 * AdjustPercent + percent);
                }
                percent = result;
                if (percent < MinPercent) {
                    percent = MaxPercent;
                }
            }
        }
        return true;
    }

    //刷新图片
    public void update() {
        myCanvas.setWindowSize(Component);
        myCanvas.repaint();
    }

    //关闭管理
    public void close() {
        percent = 0;
        Component = null;
        myCanvas.close();
    }
}
