import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class EnemyProjectile {
    private int x, y, width, height;
    private int speed = 4; // Speed of the projectile
    private double directionX, directionY; // Direction of movement
    private BufferedImage image;
    private boolean active = true;

    public EnemyProjectile(int startX, int startY, int targetX, int targetY) {
        this.x = startX;
        this.y = startY;
        this.width = 30;
        this.height = 30;

        try {
            image = ImageIO.read(new File("src/assets/fireball.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Calculate the direction towards the target
        double deltaX = targetX - x;
        double deltaY = targetY - y;
        double magnitude = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        directionX = deltaX / magnitude * speed;
        directionY = deltaY / magnitude * speed;
    }

    public void update() {
        x += directionX;
        y += directionY;

        // Mark projectile as inactive if it goes off-screen
        if (x < 0 || x > 1000 || y < 0 || y > 800) {
            active = false;
        }
    }

    public void draw(Graphics g) {
        if (active) {
            g.drawImage(image, x, y, width, height, null);
        }
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
}
