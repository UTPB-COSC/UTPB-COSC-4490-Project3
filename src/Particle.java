import java.awt.*;
import java.awt.image.BufferedImage;

public class Particle {
    private int x, y;              // Position of the particle
    private int dx, dy;            // Velocity of the particle
    private int lifetime;          // Lifetime of the particle
    private BufferedImage image;   // The fragment of the enemy boat

    public Particle(int x, int y, int dx, int dy, int lifetime, BufferedImage image) {
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
        this.lifetime = lifetime;
        this.image = image;
    }

    public void update() {
        // Update position
        x += dx;
        y += dy;

        // Decrease lifetime
        lifetime--;
    }

    public boolean isAlive() {
        return lifetime > 0;
    }

    public void draw(Graphics g) {
        if (isAlive()) {
            g.drawImage(image, x, y, null);
        }
    }
}
