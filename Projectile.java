import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
public class Projectile {
    private double x, y; // Precise position for smooth movement
    private final int width, height;
    private final double speed = 5; // Projectile speed
    private double directionX, directionY; // Unit direction vector
    private BufferedImage image;
    private boolean active = true; // Whether the projectile is active
    private GameClient client; // Add GameClient reference for networking
    private boolean isPlayerProjectile; // Flag to differentiate player and enemy projectiles

    public Projectile(int startX, int startY, double directionX, double directionY, String imagePath, GameClient client, boolean isPlayerProjectile) {
        this.x = startX;
        this.y = startY;
        this.width = 30; // Size of the projectile
        this.height = 30;
        this.client = client; // Initialize the GameClient
        this.isPlayerProjectile = isPlayerProjectile; // Set the flag

        // Normalize the direction vector
        double magnitude = Math.sqrt(directionX * directionX + directionY * directionY);
        if (magnitude != 0) {
            this.directionX = directionX / magnitude;
            this.directionY = directionY / magnitude;
        } else {
            this.directionX = 0;
            this.directionY = 0;
        }

        // Load the projectile image
        try {
            image = ImageIO.read(new File(imagePath));
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading projectile image: " + imagePath);
        }
    }

    public void update() {
        x += directionX * speed;
        y += directionY * speed;

        sendProjectilePosition();

        // Deactivate projectile if it moves off-screen
        if (x < 0 || x > 1000 || y < 0 || y > 800) {
            active = false;
        }
    }

    private void sendProjectilePosition() {
        if (client == null) {
            System.err.println("UDP Client is not initialized. Cannot send projectile position.");
            return;
        }
    
        try {
            // Prepare the position message with the projectile type (player or enemy)
            String position = (isPlayerProjectile ? "playerProjectile" : "enemyProjectile") + "," + x + "," + y; // Format: "type,x,y"
            System.out.println((isPlayerProjectile ? "Sent Player " : "Sent Enemy ") + "Projectile Position: " + position); // Log the position being sent
            client.send(position); // Send the projectile position to the server
        } catch (Exception e) {
            System.err.println("Failed to send projectile position.");
            e.printStackTrace();
        }
    }
    

    public void draw(Graphics g) {
        if (active && image != null) {
            g.drawImage(image, (int) x, (int) y, width, height, null);
        }
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, width, height);
    }
}
