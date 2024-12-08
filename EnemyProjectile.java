import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class EnemyProjectile {
    private int x, y, width, height;
    private int speed = 4; // Speed of the projectile
    private double directionX, directionY; // Direction of movement
    private BufferedImage image;
    private boolean active = true;
    private GameClient client;

    public EnemyProjectile(int startX, int startY, int targetX, int targetY, GameClient client) {
        this.x = startX;
        this.y = startY;
        this.width = 30;
        this.height = 30;
        this.client = client;

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

        // Send position to the server
        sendEnemyProjectilePosition();

        // Mark projectile as inactive if it goes off-screen
        if (x < 0 || x > 1000 || y < 0 || y > 800) {
            active = false;
        }
    }

    private void sendEnemyProjectilePosition() {
        if (client == null) {
            System.err.println("UDP Client is not initialized. Cannot send enemy projectile position.");
            return;
        }

        try {
            String position = x + "," + y; // Format: "x,y"
            System.out.println("Sent " + " Enemyprojectile position: " + position); // Log the type of projectile
            client.send(position); // Send position to the server
        } catch (Exception e) {
            System.err.println("Failed to send enemy projectile position.");
            e.printStackTrace();
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
