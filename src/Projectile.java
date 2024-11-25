import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class Projectile {
    private int x, y, width, height;
    private int speed = 5; // Speed of the projectile
    private BufferedImage image;
    private boolean active = true; // Track if the projectile is still active

    public Projectile(int x, int y) {
        this.x = x;
        this.y = y;
        this.width = 30; // Adjust to desired projectile size
        this.height = 30;

        try {
            image = ImageIO.read(new File("src/assets/fireball.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void update() {
        x += speed; // Move the projectile to the right
        if (x > 1000) { // Remove the projectile if it goes off-screen
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
