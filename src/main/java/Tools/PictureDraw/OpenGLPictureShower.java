
package Tools.PictureDraw;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class OpenGLPictureShower extends SuperPictureShower implements GLEventListener {
    private GL2 gl;
    private Texture texture;
    private double resizes = 100.0; // 当前缩放比例（百分比）
    private double lastResizes = 100.0;
    private byte rotationDegrees = 0; // 当前旋转角度（0, 1, 2, 3 分别对应 0°, 90°, 180°, 270°）
    private byte lastRotationDegrees = 0;
    private Point pictureCoordinate = new Point(0, 0); // 图片左上角坐标
    private Point lastPictureCoordinate = new Point(0, 0);
    private boolean isCenter = false; // 是否居中显示
    private double maxResizes = 500.0; // 最大缩放比例
    private double minResizes = 10.0; // 最小缩放比例

    public OpenGLPictureShower(JPanel panel) {
        super(panel);
    }

    @Override
    public void setToOptimalResizes() {
        if (bufferedImage == null || panel == null) return;

        Dimension panelSize = panel.getSize();
        if (panelSize.width <= 0 || panelSize.height <= 0) return;

        // 计算图片在四个旋转方向的宽高
        int imgWidth = bufferedImage.getWidth();
        int imgHeight = bufferedImage.getHeight();

        double widthRatio, heightRatio;
        if (rotationDegrees % 2 == 0) { // 0° 或 180°
            widthRatio = (double) panelSize.width / imgWidth;
            heightRatio = (double) panelSize.height / imgHeight;
        } else { // 90° 或 270°
            widthRatio = (double) panelSize.width / imgHeight;
            heightRatio = (double) panelSize.height / imgWidth;
        }

        // 选择较小的比率，以确保图像完全适应面板
        double ratio = Math.min(widthRatio, heightRatio) * 0.9; // 0.9是留出一些边距

        // 设置缩放比例（百分比）
        double newResizes = ratio * 100.0;

        // 确保在最大和最小缩放比例之间
        newResizes = Math.min(Math.max(newResizes, minResizes), maxResizes);

        // 应用新的缩放比例
        setPictureResizes(newResizes);

        // 如果需要居中显示，设置居中坐标
        if (isCenter) {
            setCenter();
        }
    }

    @Override
    public synchronized void addPictureShowCoordinate(int dx, int dy) {
        int newX = pictureCoordinate.x + dx;
        int newY = pictureCoordinate.y + dy;

        // 根据旋转角度调整边界检查
        Dimension panelSize = panel.getSize();
        double scaledWidth = bufferedImage.getWidth() * (resizes / 100.0);
        double scaledHeight = bufferedImage.getHeight() * (resizes / 100.0);

        // 根据旋转角度计算实际宽高
        double actualWidth, actualHeight;
        if (rotationDegrees % 2 == 0) {
            actualWidth = scaledWidth;
            actualHeight = scaledHeight;
        } else {
            actualWidth = scaledHeight;
            actualHeight = scaledWidth;
        }

        // 确保图片不会完全移出可视区域
        // 对于旋转的情况，边界检查需要考虑旋转后的尺寸
        switch (rotationDegrees) {
            case 0: // 不旋转
                if (newX > panelSize.width) newX = panelSize.width;
                if (newY > panelSize.height) newY = panelSize.height;
                if (newX + actualWidth < 0) newX = (int)(-actualWidth + 1);
                if (newY + actualHeight < 0) newY = (int)(-actualHeight + 1);
                break;
            case 1: // 90°
                if (newX > panelSize.height) newX = panelSize.height;
                if (newY < -panelSize.width) newY = -panelSize.width;
                if (newX + actualWidth < 0) newX = (int)(-actualWidth + 1);
                if (newY + actualHeight > 0) newY = -1;
                break;
            case 2: // 180°
                if (newX < -panelSize.width) newX = -panelSize.width;
                if (newY < -panelSize.height) newY = -panelSize.height;
                if (newX + actualWidth > 0) newX = -1;
                if (newY + actualHeight > 0) newY = -1;
                break;
            case 3: // 270°
                if (newX < -panelSize.height) newX = -panelSize.height;
                if (newY > panelSize.width) newY = panelSize.width;
                if (newX + actualWidth > 0) newX = -1;
                if (newY + actualHeight < 0) newY = (int)(-actualHeight + 1);
                break;
        }

        pictureCoordinate.setLocation(newX, newY);
        paintPicture();
    }

    public void setCenter() {
        if (bufferedImage == null || panel == null) return;

        Dimension panelSize = panel.getSize();
        if (panelSize.width <= 0 || panelSize.height <= 0) return;

        // 计算缩放后的图片尺寸
        double scaledWidth = bufferedImage.getWidth() * (resizes / 100.0);
        double scaledHeight = bufferedImage.getHeight() * (resizes / 100.0);

        // 根据旋转角度计算实际的宽度和高度
        double actualWidth, actualHeight;
        if (rotationDegrees % 2 == 0) { // 0° 或 180°
            actualWidth = scaledWidth;
            actualHeight = scaledHeight;
        } else { // 90° 或 270°
            actualWidth = scaledHeight;
            actualHeight = scaledWidth;
        }

        // 计算居中的坐标
        int x, y;
        switch (rotationDegrees) {
            case 0: // 不旋转
                x = (int) ((panelSize.width - actualWidth) / 2);
                y = (int) ((panelSize.height - actualHeight) / 2);
                break;
            case 1: // 90°
                x = (int) ((panelSize.height - actualWidth) / 2);
                y = (int) (-(panelSize.width + actualHeight) / 2);
                break;
            case 2: // 180°
                x = (int) (-(panelSize.width + actualWidth) / 2);
                y = (int) (-(panelSize.height + actualHeight) / 2);
                break;
            case 3: // 270°
                x = (int) (-(panelSize.height + actualWidth) / 2);
                y = (int) ((panelSize.width - actualHeight) / 2);
                break;
            default:
                x = 0;
                y = 0;
        }

        pictureCoordinate.setLocation(x, y);
        paintPicture();
    }

    @Override
    public synchronized void addPictureResizesWithMouse(double addResizes, int mouseX, int mouseY) {
        if (bufferedImage == null) return;

        // 计算当前缩放比例
        double oldResizes = resizes;
        double newResizes = oldResizes + addResizes;

        // 确保缩放比例在允许范围内
        newResizes = Math.max(minResizes, Math.min(newResizes, maxResizes));
        if (newResizes == oldResizes) return; // 如果没有变化，直接返回

        double ratio = newResizes / oldResizes; // 缩放比率

        // 将鼠标坐标转换为相对于图片坐标系的坐标
        int relativeX = mouseX - pictureCoordinate.x;
        int relativeY = mouseY - pictureCoordinate.y;

        // 计算新的图片坐标，保持鼠标指向的点不变
        int newX = (int) (mouseX - relativeX * ratio);
        int newY = (int) (mouseY - relativeY * ratio);

        // 更新缩放比例和坐标
        resizes = newResizes;
        pictureCoordinate.setLocation(newX, newY);

        paintPicture();
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        gl = drawable.getGL().getGL2();
        gl.glEnable(GL.GL_TEXTURE_2D); // 启用纹理
        gl.glEnable(GL2.GL_BLEND);
        gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
        gl.glClearColor(0.95f, 0.95f, 0.95f, 1.0f); // 设置背景色
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glOrtho(0, width, height, 0, -1, 1);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        setToOptimalResizes();
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

        Dimension parent = panel.getSize();
        if (bufferedImage == null) return;

        if (texture == null) {
            texture = AWTTextureIO.newTexture(GLProfile.getDefault(), bufferedImage, true);
            if (texture != null) {
                texture.setTexParameteri(gl, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
                texture.setTexParameteri(gl, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
            }
        }

        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();

        // 计算缩放后的尺寸
        double scaledWidth = width * (resizes / 100.0);
        double scaledHeight = height * (resizes / 100.0);

        double cacheX = pictureCoordinate.x;
        double cacheY = pictureCoordinate.y;

        // 绑定纹理并绘制
        texture.enable(gl);
        texture.bind(gl);

        gl.glPushMatrix();

        // 调整绘制位置和旋转
        switch (rotationDegrees) {
            case 0: // 0°
                gl.glTranslatef((float) cacheX, (float) cacheY, 0);
                break;
            case 1: // 90°
                gl.glTranslatef((float) cacheX, (float) cacheY, 0);
                gl.glRotatef(90, 0, 0, 1);
                break;
            case 2: // 180°
                gl.glTranslatef((float) cacheX, (float) cacheY, 0);
                gl.glRotatef(180, 0, 0, 1);
                break;
            case 3: // 270°
                gl.glTranslatef((float) cacheX, (float) cacheY, 0);
                gl.glRotatef(270, 0, 0, 1);
                break;
        }

        gl.glBegin(GL2.GL_QUADS);
        gl.glTexCoord2f(0, 0);
        gl.glVertex2d(0, 0);
        gl.glTexCoord2f(1, 0);
        gl.glVertex2d(scaledWidth, 0);
        gl.glTexCoord2f(1, 1);
        gl.glVertex2d(scaledWidth, scaledHeight);
        gl.glTexCoord2f(0, 1);
        gl.glVertex2d(0, scaledHeight);
        gl.glEnd();

        gl.glPopMatrix();

        texture.disable(gl);
        new Thread(() -> {
            if (super.getPaintedAction() != null) {
                super.getPaintedAction().handle();
            }
        }).start();
        lastPictureCoordinate = new Point(pictureCoordinate);
        lastRotationDegrees = rotationDegrees;
        lastResizes = resizes;
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        if (texture != null) {
            GL2 gl = drawable.getGL().getGL2();
            texture.destroy(gl);
            texture = null;
        }
    }

    @Override
    public void setNewPicture(BufferedImage bufferedImage, boolean isCenter) {
        // 清理旧纹理
        texture = null;

        super.setNewPicture(bufferedImage, isCenter);
        this.isCenter = isCenter;

        // 重置状态
        resizes = 100.0;
        lastResizes = 100.0;
        rotationDegrees = 0;
        lastRotationDegrees = 0;
        pictureCoordinate.setLocation(0, 0);
        lastPictureCoordinate.setLocation(0, 0);

        if (isCenter) {
            setCenter();
        }
        if (panel != null && panel.isVisible()) {
            setToOptimalResizes();
        }
    }

    @Override
    public synchronized void setPictureResizes(double resizes) {
        // 确保缩放比例在允许的范围内
        double newResizes = Math.max(minResizes, Math.min(resizes, maxResizes));
        if (this.resizes == newResizes) return;

        // 计算缩放比率
        double ratio = newResizes / this.resizes;

        // 根据缩放比率调整图片位置，以使图片中心保持不变
        Dimension panelSize = panel.getSize();
        double centerX = panelSize.width / 2.0;
        double centerY = panelSize.height / 2.0;

        double scaledWidth = bufferedImage.getWidth() * (this.resizes / 100.0);
        double scaledHeight = bufferedImage.getHeight() * (this.resizes / 100.0);

        // 根据旋转角度计算实际宽度和高度
        double actualWidth, actualHeight;
        if (rotationDegrees % 2 == 0) { // 0° 或 180°
            actualWidth = scaledWidth;
            actualHeight = scaledHeight;
        } else { // 90° 或 270°
            actualWidth = scaledHeight;
            actualHeight = scaledWidth;
        }

        // 当前图片中心点
        double imgCenterX, imgCenterY;
        switch (rotationDegrees) {
            case 0:
                imgCenterX = pictureCoordinate.x + actualWidth / 2;
                imgCenterY = pictureCoordinate.y + actualHeight / 2;
                break;
            case 1:
                imgCenterX = pictureCoordinate.x + actualWidth / 2;
                imgCenterY = pictureCoordinate.y - actualHeight / 2;
                break;
            case 2:
                imgCenterX = pictureCoordinate.x - actualWidth / 2;
                imgCenterY = pictureCoordinate.y - actualHeight / 2;
                break;
            case 3:
                imgCenterX = pictureCoordinate.x - actualWidth / 2;
                imgCenterY = pictureCoordinate.y + actualHeight / 2;
                break;
            default:
                imgCenterX = pictureCoordinate.x + actualWidth / 2;
                imgCenterY = pictureCoordinate.y + actualHeight / 2;
                break;
        }

        // 保持图片中心不变，计算新的坐标
        double newScaledWidth = bufferedImage.getWidth() * (newResizes / 100.0);
        double newScaledHeight = bufferedImage.getHeight() * (newResizes / 100.0);

        double newActualWidth, newActualHeight;
        if (rotationDegrees % 2 == 0) {
            newActualWidth = newScaledWidth;
            newActualHeight = newScaledHeight;
        } else {
            newActualWidth = newScaledHeight;
            newActualHeight = newScaledWidth;
        }

        int newX, newY;
        switch (rotationDegrees) {
            case 0:
                newX = (int) (imgCenterX - newActualWidth / 2);
                newY = (int) (imgCenterY - newActualHeight / 2);
                break;
            case 1:
                newX = (int) (imgCenterX - newActualWidth / 2);
                newY = (int) (imgCenterY + newActualHeight / 2);
                break;
            case 2:
                newX = (int) (imgCenterX + newActualWidth / 2);
                newY = (int) (imgCenterY + newActualHeight / 2);
                break;
            case 3:
                newX = (int) (imgCenterX + newActualWidth / 2);
                newY = (int) (imgCenterY - newActualHeight / 2);
                break;
            default:
                newX = (int) (imgCenterX - newActualWidth / 2);
                newY = (int) (imgCenterY - newActualHeight / 2);
                break;
        }

        // 更新缩放比例和坐标
        this.resizes = newResizes;
        pictureCoordinate.setLocation(newX, newY);

        paintPicture();
    }

    @Override
    public synchronized void addPictureResizes(double resizes) {
        setPictureResizes(this.resizes + resizes);
    }

    @Override
    public synchronized void setPictureShowCoordinate(int x, int y) {
        pictureCoordinate.setLocation(x, y);
        paintPicture();
    }

    @Override
    public synchronized void setPictureRotationDegrees(byte degrees) {
        byte cache = 0;
        if (degrees < 0) {
            cache = (byte) (4 + degrees % 4);
        } else {
            cache = (byte) (degrees % 4);
        }
        this.rotationDegrees = cache;

        // 旋转后重新调整图片位置
        if (isCenter) {
            setCenter();
        }

        paintPicture();
    }

    @Override
    public synchronized void addPictureRotationDegrees(byte degrees) {
        setPictureRotationDegrees((byte) (rotationDegrees + degrees));
    }

    @Override
    public byte getFinishedPictureRotationDegrees() {
        return lastRotationDegrees;
    }

    @Override
    public byte getWillBeFinishedPictureRotationDegrees() {
        return rotationDegrees;
    }

    @Override
    public void paintPicture() {
        // 获取GLJPanel并重绘
        if (panel instanceof GLJPanel) {
            GLJPanel gljPanel = (GLJPanel) panel;
            gljPanel.display();
        }
    }

    @Override
    public Point getFinishedPictureCoordinate() {
        return new Point(lastPictureCoordinate);
    }

    @Override
    public Point getWillBeFinishedPictureCoordinate() {
        return new Point(pictureCoordinate);
    }

    @Override
    public double getFinishedPictureResizes() {
        return lastResizes;
    }

    @Override
    public double getWillBeFinishedPictureResizes() {
        return resizes;
    }

    @Override
    public void setMaxResizes(double resizes) {
        this.maxResizes = resizes;
        // 确保当前缩放不超过新的最大值
        if (this.resizes > maxResizes) {
            setPictureResizes(maxResizes);
        }
    }

    @Override
    public void setMinResizes(double resizes) {
        this.minResizes = resizes;
        // 确保当前缩放不低于新的最小值
        if (this.resizes < minResizes) {
            setPictureResizes(minResizes);
        }
    }

    @Override
    public void close() {
        if (texture != null && gl != null) {
            texture.destroy(gl);
            texture = null;
        }
    }
}
