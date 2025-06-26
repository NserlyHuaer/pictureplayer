package Tools;

import Tools.PictureDraw.DefaultPictureShower;
import Tools.PictureDraw.OpenGLPictureShower;
import Tools.PictureDraw.SuperPictureShower;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLJPanel;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;

public class PictureViewer {

    @Getter
    private boolean EnableHardwareAcceleration;
    @Getter
    private boolean EnableDoubleBuffered;
    @Getter
    private JPanel paintPanel;
    @Getter
    private SuperPictureShower superPictureShower;

    @Getter
    private JComponent handleComponent;


    /**
     * 使用指定的渲染选项构造新的 PictureViewer。
     *
     * @param EnableHardwareAcceleration（如果为 true），则启用硬件加速以提高性能;
     *                                       如果为 false，则禁用硬件加速
     * @param EnableDoubleBuffered           如果为 true，则启用双缓冲以减少闪烁并提高渲染质量;
     *                                       如果为 false，则禁用双缓冲
     */
    public PictureViewer(boolean EnableHardwareAcceleration, boolean EnableDoubleBuffered) {
        this.EnableHardwareAcceleration = EnableHardwareAcceleration;
        this.EnableDoubleBuffered = EnableDoubleBuffered;
        if (EnableHardwareAcceleration) {
            GLProfile.initSingleton();  // 初始化OpenGL
            GLProfile profile = GLProfile.get(GLProfile.GL2);
            GLCapabilities caps = new GLCapabilities(profile);
            // 创建Swing兼容的OpenGL面板
            GLJPanel gljPanel = new GLJPanel(caps);
            superPictureShower = new OpenGLPictureShower(gljPanel);
            gljPanel.addGLEventListener((OpenGLPictureShower) superPictureShower); // 自定义渲染器
            paintPanel = gljPanel;
            handleComponent = paintPanel;
        } else {
            paintPanel = new JPanel();
            superPictureShower = new DefaultPictureShower(paintPanel);
            paintPanel.setLayout(new BorderLayout());
            paintPanel.add(superPictureShower, BorderLayout.CENTER);
            handleComponent = superPictureShower;
        }
        paintPanel.setDoubleBuffered(EnableDoubleBuffered);
    }
}
