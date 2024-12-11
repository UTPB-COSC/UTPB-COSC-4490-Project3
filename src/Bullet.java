package src;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class Bullet {
	
	private double x, y, angle;
    private final int SPEED = 3;

    public Bullet(double x, double y, double angle) {
        this.x = x;
        this.y = y;
        this.angle = angle;
    }

    public void update() {
        x += SPEED * Math.cos(Math.toRadians(angle));
        y += SPEED * Math.sin(Math.toRadians(angle));
    }

    public void draw(Graphics2D g2d, boolean debug) {
        g2d.setColor(Color.YELLOW);
        g2d.fillOval((int) x - 2, (int) y - 2, 4, 4);

        if (debug) {
            g2d.setColor(Color.RED);
            g2d.drawRect((int) x - 2, (int) y - 2, 4, 4);
        }
    }

    public boolean isOnScreen(int width, int height) {
        return x >= 0 && x <= width && y >= 0 && y <= height;
    }

    public Rectangle getBounds() {
        return new Rectangle((int) x - 2, (int) y - 2, 4, 4);
    }

}
