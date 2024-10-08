package Runner;

import java.awt.*;

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
    private Main.MyCanvas myCanvas;
    //当前组件信息
    private Dimension Component;
    //当前最适合组建的比例
    public double FittestPercent;

    //改变图片渲染器
    public void changeCanvas(Main.MyCanvas myCanvas) {
        this.myCanvas = myCanvas;
        FittestPercent = getPictureOptimalSize();
        setPercent(FittestPercent);
        update();
    }


    public SizeOperate(Main.MyCanvas myCanvas, Dimension Component) {
        this.Component = Component;
        this.myCanvas = myCanvas;
        FittestPercent = percent = Default = 100;

    }

    public SizeOperate(Main.MyCanvas myCanvas, short defaultPercent, Dimension Component) {
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
    }

    //获取默认缩放比例
    public short getDefaultPercent() {
        return Default;
    }


    private void decide(double size) {
        if (size > MaxPercent) {
            percent = MaxPercent;
        } else {
            percent = size;
        }
        if (size < MinPercent) {
            percent = MinPercent;
        }
        update();
    }

    //获取图片最佳比例
    public double getPictureOptimalSize() {
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
            this.percent = MaxPercent;
        } else this.percent = percent;
    }

    //调节比例
    public void adjustPercent(int operate) {
        switch (operate) {
            case Enlarge -> {
                if (percent >= MaxPercent) return;
                if (percent < FittestPercent) decide(4 + percent);
                else {
                    decide(11 + percent);
                }
            }
            case Reduce -> {
                if (percent <= MinPercent) return;
                if (percent < FittestPercent) decide(-4 + percent);
                else {
                    decide(-11 + percent);
                }
            }
        }
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
