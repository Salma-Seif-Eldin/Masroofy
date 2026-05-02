package Views;

import java.awt.*;
import java.util.Map;
import javax.swing.*;

public class SpendingChart extends JPanel {
    private Map<String, Double> data;

    public void setData(Map<String, Double> data) {
        this.data = data;
        this.repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int size = Math.min(width, height) - 40;
        int x = (width - size) / 2;
        int y = (height - size) / 2;

        // Default: Draw a gray circle if data is null or empty (Zero state)
        if (data == null || data.isEmpty()) {
            g2.setColor(Color.LIGHT_GRAY);
            g2.fillOval(x, y, size, size);
            // 2. Get the measurements of the text to center it
        String text = "No spending data yet";
        FontMetrics fm = g2.getFontMetrics();
        
        // Calculate the horizontal start point
        int textX = (width - fm.stringWidth(text)) / 2;
        
        // Calculate the vertical start point (baseline)
        int textY = (height / 2) + (fm.getAscent() / 2) - (fm.getDescent() / 2);

        g2.setColor(Color.BLACK);
        g2.drawString(text, textX, textY);
        return;
        }

        double total = data.values().stream().mapToDouble(Double::doubleValue).sum();
        int startAngle = 0;
        int colorIndex = 0;
        Color[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE, Color.MAGENTA, Color.CYAN};

        for (Map.Entry<String, Double> entry : data.entrySet()) {
            int arcAngle = (int) Math.round((entry.getValue() / total) * 360);
            g2.setColor(colors[colorIndex % colors.length]);
            g2.fillArc(x, y, size, size, startAngle, arcAngle);
            
            // Draw Legend
            g2.fillRect(10, 10 + (colorIndex * 20), 10, 10);
            g2.setColor(Color.BLACK);
            g2.drawString(entry.getKey(), 25, 20 + (colorIndex * 20));

            startAngle += arcAngle;
            colorIndex++;
        }
    }
}
