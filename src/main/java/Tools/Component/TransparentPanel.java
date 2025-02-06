package Tools.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class TransparentPanel extends JPanel {
    private float alpha = 0.7f; // 透明度,范围从0.0(完全透明)到1.0(完全不透明)

    public TransparentPanel(float alpha) {
        setOpaque(false); // 设置面板为非不透明
        this.alpha = alpha;
    }

    public TransparentPanel() {
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        // 设置复合模式,使用alpha值
        AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
        g2.setComposite(ac);

        // 绘制圆角矩形作为背景
        RoundRectangle2D.Float rRect = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15);
        g2.fill(rRect);

        // 绘制子组件
        super.paintComponent(g);
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
        repaint();
    }
}