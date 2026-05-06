package Views;

import java.awt.*;
import java.util.Map;
import javax.swing.*;

/**
 * A custom Swing panel that renders a pie chart of spending by category.
 * <p>
 * Displays each category as a colored slice of a pie chart along with a
 * color-coded legend. If no data is available, a placeholder message is shown.
 * </p>
 *
 * @author Masroofy Team
 * @version 1.0
 */
public class SpendingChart extends JPanel {
    private Map<String, Double> data;

    /**
     * Constructs a new SpendingChart panel.
     */
    public SpendingChart() {
    }

    /**
     * Sets the spending data to be visualized and triggers a repaint.
     *
     * @param data a map of category names to their total spending amounts;
     *             may be {@code null} or empty to show a placeholder
     */
    public void setData(Map<String, Double> data) {
        this.data = data;
        this.repaint();
    }

    /**
     * Paints the pie chart component.
     * <p>
     * If data is null or empty, a gray circle with a "No spending data yet" message
     * is drawn. Otherwise, each category is rendered as a pie slice with a legend
     * entry.
     * </p>
     *
     * @param g the {@link Graphics} context used for painting
     */
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

        if (data == null || data.isEmpty()) {
            g2.setColor(Color.LIGHT_GRAY);
            g2.fillOval(x, y, size, size);
            String text = "No spending data yet";
            FontMetrics fm = g2.getFontMetrics();
            int textX = (width - fm.stringWidth(text)) / 2;
            int textY = (height / 2) + (fm.getAscent() / 2) - (fm.getDescent() / 2);
            g2.setColor(Color.BLACK);
            g2.drawString(text, textX, textY);
            return;
        }

        double total = data.values().stream().mapToDouble(Double::doubleValue).sum();
        int startAngle = 0;
        int colorIndex = 0;
        Color[] colors = { Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE, Color.MAGENTA, Color.CYAN };

        for (Map.Entry<String, Double> entry : data.entrySet()) {
            int arcAngle = (int) Math.round((entry.getValue() / total) * 360);
            g2.setColor(colors[colorIndex % colors.length]);
            g2.fillArc(x, y, size, size, startAngle, arcAngle);

            g2.fillRect(10, 10 + (colorIndex * 20), 10, 10);
            g2.setColor(Color.BLACK);
            g2.drawString(entry.getKey(), 25, 20 + (colorIndex * 20));

            startAngle += arcAngle;
            colorIndex++;
        }
    }
}