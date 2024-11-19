import java.awt.*;
public class Projectile {
    private int x, y;
    private int velocityX, velocityY; // Cannon-like slow movement
    private Rectangle bounds;

    public Projectile(int startX, int startY, int velocityX, int velocityY) {
        this.x = startX;
        this.y = startY;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.bounds = new Rectangle(x, y, 10, 10); // Adjust size as needed
    }

    public void move() {
        this.x += velocityX;
        this.y += velocityY;
        this.bounds.setLocation(x, y);
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public boolean isOutOfBounds(int width, int height) {
        return x < 0 || x > width || y < 0 || y > height;
    }
}
