package javaswingdev.gauge;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import javax.swing.JComponent;
import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.TimingTarget;
import org.jdesktop.animation.timing.interpolation.PropertySetter;

public class GaugeChart extends JComponent {

    public float getTrackStart() {
        return trackStart;
    }

    public void setTrackStart(float trackStart) {
        this.trackStart = trackStart;
        createRender();
        repaint();
    }

    public float getTrackStop() {
        return trackStop;
    }

    public void setTrackStop(float trackStop) {
        this.trackStop = trackStop;
        createRender();
        repaint();
    }

    public Color getColorTrackStart() {
        return colorTrackStart;
    }

    public void setColorTrackStart(Color colorTrackStart) {
        this.colorTrackStart = colorTrackStart;
        createRender();
        repaint();
    }

    public Color getColorTrackStop() {
        return colorTrackStop;
    }

    public void setColorTrackStop(Color colorTrackStop) {
        this.colorTrackStop = colorTrackStop;
        createRender();
        repaint();
    }

    public DecimalFormat getFormat() {
        return format;
    }

    public void setFormat(DecimalFormat format) {
        this.format = format;
        repaint();
    }

    public Font getFontValue() {
        return fontValue;
    }

    public void setFontValue(Font fontValue) {
        this.fontValue = fontValue;
        repaint();
    }

    public float getMinValue() {
        return minValue;
    }

    public void setMinValue(float minValue) {
        if (minValue < -1 || (minValue >= maxValue && maxValue != -1)) {
            minValue = -1;
        }
        this.minValue = minValue;
        createRender();
        setValue(value);
    }

    public float getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(float maxValue) {
        if (maxValue < -1 || (maxValue <= minValue && minValue != -1)) {
            maxValue = -1;
        } else if (maxValue > 100) {
            maxValue = 100;
        }
        this.maxValue = maxValue;
        createRender();
        setValue(value);
    }

    public float getThresholdIndicator() {
        return thresholdIndicator;
    }

    public void setThresholdIndicator(float thresholdIndicator) {
        if (thresholdIndicator < -1) {
            thresholdIndicator = -1;
        } else if (thresholdIndicator > 100) {
            thresholdIndicator = 100;
        }
        this.thresholdIndicator = thresholdIndicator;
        createRender();
        repaint();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        createRender();
        repaint();
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        if (value < 0) {
            value = 0;
        } else if (value > 100) {
            value = 100;
        }
        if (minValue != -1) {
            if (value < minValue) {
                value = minValue;
            }
        }
        if (maxValue != -1) {
            if (value > maxValue) {
                value = maxValue;
            }
        }
        this.value = value;
        repaint();
    }

    public void setTrackSelection(float start, float stop) {
        this.trackStart = start;
        this.trackStop = stop;
        createRender();
        repaint();
    }

    private String title = "";
    private float value;
    private float minValue = -1;
    private float maxValue = -1;
    private float thresholdIndicator = -1;
    private float trackStart = -1;
    private float trackStop = -1;
    private Color colorTrackStart = new Color(152, 74, 226);
    private Color colorTrackStop = new Color(52, 95, 253);
    private DecimalFormat format = new DecimalFormat("#,#00.00");
    private Font fontValue = new Font("DS-Digital", Font.BOLD, 20);
    private final Animator animator;
    private TimingTarget target;
    private BufferedImage imageRender;

    public GaugeChart() {
        setFont(new java.awt.Font("sansserif", 0, 15));
        setSize(new Dimension(300, 300));
        setPreferredSize(new Dimension(300, 300));
        animator = new Animator(1000);
        animator.setAcceleration(.5f);
        animator.setDeceleration(.5f);
        animator.setResolution(0);
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (imageRender == null) {
            createRender();
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.drawImage(imageRender, 0, 0, null);
        drawValue(g2);
        drawPointer(g2);
        drawCircle(g2);
        g2.dispose();
        super.paintComponent(g);
    }

    private void createRender() {
        imageRender = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = imageRender.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        drawBackground(g2);
        drawFrame(g2);
        drawTrackSelection(g2);
        drawMeasured(g2);
        drawTitle(g2);
        drawMeasuredMinAndMax(g2);
        drawThresholdIndicator(g2);
        g2.dispose();
    }

    private Shape getLine(float x, float y, float size, float stroke) {
        Area area = new Area(new Ellipse2D.Float(x, y, size, size));
        area.subtract(new Area(new Ellipse2D.Float(x + stroke, y + stroke, size - stroke * 2, size - stroke * 2)));
        return area;
    }

    private void drawTitle(Graphics2D g2) {
        if (title != null && !title.equals("")) {
            float width = getWidth();
            float height = getHeight();
            float size = Math.min(width, height) / 2;
            float centerX = width / 2;
            float centerY = height / 2;
            Font font = getFont().deriveFont(Font.BOLD, (getFont().getSize() - 3f) * size / 100f);
            g2.setFont(font);
            float fontWidth = g2.getFontMetrics().stringWidth(title) / 2;
            g2.drawString(title, centerX - fontWidth, centerY - size * 0.25f);
        }
    }

    private void drawValue(Graphics2D g2) {
        float width = getWidth();
        float height = getHeight();
        float size = Math.min(width, height) / 2;
        float centerX = width / 2;
        float centerY = height / 2;
        Font font = fontValue.deriveFont((fontValue.getSize()) * size / 100f);
        g2.setFont(font);
        String v = format.format(value);
        float fontWidth = g2.getFontMetrics().stringWidth(v) / 2;
        float boxWidth = size * 0.4f;
        Rectangle2D r2 = g2.getFontMetrics().getStringBounds(v, g2);
        g2.setColor(new Color(243, 243, 243));
        float yy = (centerY + size * 0.35f) - (float) r2.getHeight();
        float space = size * 0.05f;
        g2.fill(new RoundRectangle2D.Double((centerX - boxWidth / 2f) - space, yy, boxWidth + space * 2, r2.getHeight(), 10, 10));
        g2.setColor(new Color(45, 45, 45));
        g2.drawString(v, centerX - fontWidth, yy + g2.getFontMetrics().getAscent());
    }

    private void drawBackground(Graphics2D g2) {
        float width = getWidth();
        float height = getHeight();
        float size = Math.min(width, height);
        float x = (width - size) / 2;
        float y = (height - size) / 2;
        g2.setPaint(new GradientPaint(0, y, new Color(30, 30, 30), 0, y + size * 1.3f, new Color(219, 219, 219)));
        Area area = new Area(new Ellipse2D.Float(x, y, size, size));
        g2.fill(area);
        Path2D p = new Path2D.Float(new QuadCurve2D.Float(x, height / 2, width / 2, y + size * 0.3f, x + size, height / 2));
        p.lineTo(width, height);
        p.lineTo(0, height);
        area.subtract(new Area(p));
        Composite oldCom = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
        g2.setPaint(new GradientPaint(0, y, new Color(255, 255, 255, 20), 0, y + size * 0.5f, new Color(255, 255, 255)));
        g2.fill(area);
        g2.setComposite(oldCom);
    }

    private void drawFrame(Graphics2D g2) {
        float width = getWidth();
        float height = getHeight();
        float size = Math.min(width, height);
        float x = (width - size) / 2;
        float y = (height - size) / 2;
        g2.setPaint(new GradientPaint(0, y, new Color(245, 245, 245), 0, y + size * 0.5f, new Color(221, 221, 221)));
        Area area = new Area(new Ellipse2D.Float(x, y, size, size));
        float size1 = size * 0.83f;
        float x1 = (width - size1) / 2;
        float y1 = (height - size1) / 2;
        area.subtract(new Area(new Ellipse2D.Float(x1, y1, size1, size1)));
        g2.fill(area);
        g2.setColor(new Color(201, 201, 201));
        g2.fill(getLine(x, y, size, size * 0.003f));
        g2.setColor(new Color(255, 255, 255));
        g2.fill(getLine(x1, y1, size1, size * 0.004f));

    }

    private void drawCircle(Graphics2D g2) {
        float width = getWidth();
        float height = getHeight();
        float size = Math.min(width, height);
        drawCircle(g2, width / 2, height / 2, size * 0.09f);
        drawCirclePoint(g2, 120);
        drawCirclePoint(g2, 60);
    }

    private void drawCirclePoint(Graphics2D g2, float angle) {
        float width = getWidth();
        float height = getHeight();
        float size = Math.min(width, height);
        float centerX = width / 2;
        float centerY = height / 2;
        float locationX = (float) (Math.cos(Math.toRadians(angle)) * size * 0.35f);
        float locationY = (float) Math.sin(Math.toRadians(angle)) * (size * 0.35f);
        drawCircle(g2, centerX + locationX, centerY + locationY, size * 0.04f);
    }

    private void drawCircle(Graphics2D g2, float x, float y, float size) {
        float s = size / 2f;
        x -= s;
        y -= s;
        g2.setPaint(new GradientPaint(0, y, new Color(235, 235, 235), 0, y + size * 0.5f, new Color(55, 55, 55)));
        g2.fill(new Ellipse2D.Float(x, y, size, size));
        float size1 = size * 0.7f;
        float x1 = x + (size - size1) / 2;
        float y1 = y + (size - size1) / 2;
        Area area = new Area(new Ellipse2D.Float(x1, y1, size1, size1));
        g2.setPaint(new GradientPaint(0, y, new Color(248, 248, 248), 0, y + size * 0.8f, new Color(167, 167, 167)));
        g2.fill(area);
    }

    private void drawMeasured(Graphics2D g2) {
        float total = 286f / 100f;
        float angle = 127;
        float width = getWidth();
        float height = getHeight();
        float size = Math.min(width, height);
        float centerX = width / 2;
        float centerY = height / 2;
        for (int i = 0; i <= 100; i++) {
            drawMeasuredLine(g2, angle, centerX, centerY, size / 2, i);
            angle += total;
        }

    }

    private void drawMeasuredLine(Graphics2D g2, float angle, float centerX, float centerY, float size, int values) {
        float cosX = (float) Math.cos(Math.toRadians(angle));
        float sinY = (float) Math.sin(Math.toRadians(angle));
        float add;
        if (values % 10 == 0) {
            g2.setColor(new Color(240, 240, 240));
            add = 0.68f;
            drawValues(g2, angle, centerX, centerY, size, values + "");
        } else if (values % 5 == 0) {
            g2.setColor(new Color(230, 230, 230));
            add = 0.71f;
        } else {
            g2.setColor(new Color(189, 189, 189, 200));
            add = 0.72f;
        }
        g2.draw(new Line2D.Float(centerX + cosX * size * 0.75f, centerY + sinY * size * 0.75f, centerX + cosX * size * add, centerY + sinY * size * add));

    }

    private void drawValues(Graphics2D g2, float angle, float centerX, float centerY, float size, String values) {
        AffineTransform tran = g2.getTransform();
        Font font = getFont().deriveFont((getFont().getSize() - 3) * size / 100);
        g2.setFont(font);
        float valuesWidth = g2.getFontMetrics().stringWidth(values) / 2;
        g2.rotate(Math.toRadians(angle - 270), centerX, centerY);
        g2.drawString(values, centerX - valuesWidth, centerY - size * 0.5f);
        g2.setTransform(tran);
    }

    private void drawPointer(Graphics2D g2) {
        AffineTransform tran = g2.getTransform();
        float width = getWidth();
        float height = getHeight();
        float size = Math.min(width, height) / 2;
        float centerX = width / 2;
        float centerY = height / 2;
        float angle = valueToAngle(value);
        Path2D p = new Path2D.Float();
        p.moveTo(centerX - size * 0.09f, centerY);
        p.curveTo(centerX - size * 0.09f, centerY, centerX - size * 0.03f, centerY - size * 0.05f, centerX - 0.7, centerY - size * 0.73f);
        p.lineTo(centerX + 0.7, centerY - size * 0.73f);
        p.curveTo(centerX + 0.7, centerY - size * 0.73f, centerX + size * 0.03f, centerY - size * 0.05f, centerX + size * 0.09f, centerY);
        Composite oldCom = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.9f));
        g2.setPaint(new GradientPaint(centerX - size * 0.09f, centerY, new Color(181, 5, 5), centerX, centerY - size * 0.73f, new Color(255, 128, 128)));
        g2.rotate(Math.toRadians(angle - 270), centerX, centerY);
        g2.fill(p);
        g2.setTransform(tran);
        g2.setComposite(oldCom);
    }

    private void drawMeasuredMinAndMax(Graphics2D g2) {
        if (minValue != -1) {
            drawMeasuredPointAt(g2, valueToAngle(minValue), new Color(52, 36, 251));
        }
        if (maxValue != -1) {
            drawMeasuredPointAt(g2, valueToAngle(maxValue), new Color(252, 74, 74));
        }
    }

    private void drawMeasuredPointAt(Graphics2D g2, float angle, Color color) {
        AffineTransform tran = g2.getTransform();
        float width = getWidth();
        float height = getHeight();
        float size = Math.min(width, height) / 2;
        float centerX = width / 2;
        float centerY = height / 2;
        float w = size * 0.03f;
        Path2D p = new Path2D.Float();
        p.moveTo(centerX, centerY - size * 0.73f);
        p.lineTo(centerX - w, centerY - size * 0.78f);
        p.lineTo(centerX + w, centerY - size * 0.78f);
        g2.setColor(color);
        g2.rotate(Math.toRadians(angle - 270), centerX, centerY);
        g2.fill(p);
        g2.setTransform(tran);
    }

    private float valueToAngle(float value) {
        float total = 286 / 100f;
        float angle = 127 + value * total;
        return angle;
    }

    private void drawThresholdIndicator(Graphics2D g2) {
        if (thresholdIndicator != -1) {
            float angle = valueToAngle(thresholdIndicator);
            AffineTransform tran = g2.getTransform();
            float width = getWidth();
            float height = getHeight();
            float size = Math.min(width, height) / 2;
            float centerX = width / 2;
            float centerY = height / 2;
            float w = size * 0.035f;
            Path2D p = new Path2D.Float();
            p.moveTo(centerX, centerY - size * 0.73f);
            p.lineTo(centerX - w, centerY - size * 0.64f);
            p.lineTo(centerX + w, centerY - size * 0.64f);
            p.lineTo(centerX, centerY - size * 0.73f);
            g2.setColor(new Color(253, 55, 55));
            g2.rotate(Math.toRadians(angle - 270), centerX, centerY);
            g2.fill(p);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(size * 0.005f));
            g2.draw(p);
            g2.setTransform(tran);
        }
    }

    private void drawTrackSelection(Graphics2D g2) {
        if (trackStart >= 0 && trackStop > 0) {
            float angleStart = valueToAngle(trackStart);
            float angleStop = valueToAngle(trackStop);
            float width = getWidth();
            float height = getHeight();
            float size = Math.min(width, height);
            float centerX = width / 2;
            float centerY = height / 2;
            float s = size * 0.77f;
            float x = (width - s) / 2;
            float y = (height - s) / 2;
            float size1 = size * 0.7f;
            float x1 = (width - size1) / 2;
            float y1 = (height - size1) / 2;
            Area area = new Area(new Ellipse2D.Float(x, y, s, s));
            area.subtract(new Area(new Ellipse2D.Float(x1, y1, size1, size1)));
            area.intersect(new Area(new Arc2D.Float(x, y, s, s, -angleStart, -(angleStop - angleStart), Arc2D.PIE)));
            float cx = (float) Math.cos(Math.toRadians(angleStart)) * s / 2f;
            float cy = (float) Math.sin(Math.toRadians(angleStart)) * s / 2f;
            float cx1 = (float) Math.cos(Math.toRadians(angleStop)) * s / 2f;
            float cy1 = (float) Math.sin(Math.toRadians(angleStop)) * s / 2f;
            g2.setPaint(new GradientPaint(centerX + cx, centerY + cy, colorTrackStart, centerX + cx1, centerY + cy1, colorTrackStop));
            g2.fill(area);
        }
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        createRender();
    }

    public void setValueAnimate(float value) {
        if (animator.isRunning()) {
            animator.stop();
        }
        animator.removeTarget(target);
        target = new PropertySetter(this, "value", this.value, Math.max(minValue, Math.min(value, maxValue)));
        animator.addTarget(target);
        animator.start();
    }
}
