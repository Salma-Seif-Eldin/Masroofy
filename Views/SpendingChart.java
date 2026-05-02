package Views;

import java.awt.*;
import java.util.Map;
import javax.swing.*;

public class SpendingChart extends JPanel {
    private Map<String, Double> data;

    public void setData(Map<String, Double> data) {
        this.data = data;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (data == null || data.isEmpty()) {
            g.drawString("No spending data to display", 50, 50);
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int barWidth = (width / data.size()) - 20;
        double maxVal = data.values().stream().max(Double::compare).orElse(1.0);

        int x = 10;
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            // Calculate bar height relative to the panel size
            double value = entry.getValue();
            int barHeight = (int) ((value / maxVal) * (height - 50));

            // Draw Bar
            g2.setColor(new Color(70, 130, 180)); // Steel Blue
            g2.fillRect(x, height - barHeight - 30, barWidth, barHeight);

            // Draw Label and Value
            g2.setColor(Color.BLACK);
            g2.drawString(entry.getKey(), x, height - 15);
            g2.drawString(String.format("%.0f", value), x, height - barHeight - 35);

            x += barWidth + 20;
        }
    }
}
