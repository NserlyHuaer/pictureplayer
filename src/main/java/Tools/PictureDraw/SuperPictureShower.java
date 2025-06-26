package Tools.PictureDraw;


import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public abstract class SuperPictureShower extends JComponent {
    //当前渲染的图片原始宽度
    @Getter
    protected int PictureWidth;
    //当前渲染的图片原始高度
    @Getter
    protected int PictureHeight;

    //图片显示大小小于渲染器所占窗体大小时，是否让图片居中显示
    @Setter
    @Getter
    protected boolean isCenter;

    //缓存图片
    protected BufferedImage bufferedImage;

    //父容器
    protected JPanel panel;

    @Setter
    @Getter
    private PaintedAction paintedAction;//绘制完成后执行（需要写实现时，需要自己实现）

    //调整图片缩放比(单位%)（以窗体中心为中心）
    abstract public void setPictureResizes(double resizes);

    //增大图片缩放比(单位%)（以窗体中心为中心）
    abstract public void addPictureResizes(double resizes);


    //调整图片左上角坐标
    abstract public void setPictureShowCoordinate(int x, int y);

    //增大图片左上角坐标
    abstract public void addPictureShowCoordinate(int x, int y);

    //设置图片旋转度数，若degrees=0则代表图片不需要旋转，若degrees=1则代表图片需要逆时针旋转90度，以此类推（degrees必须为非负整数，且大小必须小于4）
    //旋转后，每当渲染时，需要算法来调整坐标（无论旋转多少，程序传入的坐标统一为未旋转时的坐标）（旋转之后，自动匹配最合适的缩放比来适应窗体（显示在窗体中央））
    abstract public void setPictureRotationDegrees(byte degrees);

    //增大图片旋转度数
    abstract public void addPictureRotationDegrees(byte degrees);

    //获取当前图片旋转分量（已渲染的图片旋转分量）
    abstract public byte getFinishedPictureRotationDegrees();

    //获取当前图片旋转分量（未渲染的图片旋转分量）
    abstract public byte getWillBeFinishedPictureRotationDegrees();

    //重新绘制图片
    abstract public void paintPicture();

    //获取当前图片坐标（已渲染的图片坐标）
    abstract public Point getFinishedPictureCoordinate();

    //获取预设图片坐标（未渲染的图片坐标）
    abstract public Point getWillBeFinishedPictureCoordinate();

    //获取当前图片缩放比（已渲染的图片缩放比，单位%）
    abstract public double getFinishedPictureResizes();

    //获取当前图片缩放比（未渲染的图片缩放比，单位%）
    abstract public double getWillBeFinishedPictureResizes();

    //跟随鼠标放大或缩小图片（放大图片，但以鼠标为放大或缩小中心，单位%）
    abstract public void addPictureResizesWithMouse(double addResizes, int mouseX, int mouseY);

    /**
     * Sets a new picture to be displayed and configures whether the picture should be centered.
     * Updates the dimensions of the picture based on the provided BufferedImage.
     *
     * @param bufferedImage the BufferedImage representing the new picture to be set
     * @param isCenter      a boolean value indicating whether the picture should be centered when displayed
     */
    public void setNewPicture(BufferedImage bufferedImage, boolean isCenter) {
        this.bufferedImage = bufferedImage;
        this.isCenter = isCenter;
        this.PictureWidth = bufferedImage.getWidth();
        this.PictureHeight = bufferedImage.getHeight();
    }

    /**
     * Sets a new picture to be displayed without altering the centering configuration.
     * This method delegates to the overloaded {@link #setNewPicture(BufferedImage, boolean)} method,
     * passing {@code false} for the centering parameter.
     *
     * @param bufferedImage the BufferedImage representing the new picture to be set
     */
    public void setNewPicture(BufferedImage bufferedImage) {
        this.setNewPicture(bufferedImage, true);
    }

    //设置最大缩放比
    abstract public void setMaxResizes(double resizes);

    //设置最小缩放比
    abstract public void setMinResizes(double resizes);

    //关闭
    abstract public void close();

    //该类放在哪个panel里
    public SuperPictureShower(JPanel panel) {
        this.panel = panel;
    }

    //将图片设置为最合适的缩放比
    abstract public void setToOptimalResizes();

}