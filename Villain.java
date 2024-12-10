import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import java.awt.Color;

public class Villain {
    private int x, y; // Villain position on the screen
    private double fireCooldown = 2.0; // Villain fires a spike every 2 seconds
    private double timeSinceLastFire = 0;
    private Image villainImage;



    public Villain(int x, int y) {
        this.x = x;
        this.y = y;
            try {
        villainImage = ImageIO.read(new File("dragon.png")); // Update path to your image
    } catch (IOException e) {
        e.printStackTrace();
    }
    }

    public void update(double deltaTime, Ball ball, List<ProjectileSpike> projectiles) {
        timeSinceLastFire += deltaTime;
        if (timeSinceLastFire >= fireCooldown) {
            // Fire a projectile towards the ball
            fireAtBall(ball, projectiles);
            timeSinceLastFire = 0;
        }
    }

    private void fireAtBall(Ball ball, List<ProjectileSpike> projectiles) {
        // Calculate direction from villain to ball
        double dx = ball.getX() - x;
        double dy = ball.getY() - y;

        // A basic approach: 
        // Let's give the projectile an initial velocity that aims at the ball.
        // You can refine this logic (e.g., predicting ball movement).
        double speed = 500; 
        double dist = Math.sqrt(dx*dx + dy*dy);
        double vx = (dx / dist) * speed;
        double vy = (dy / dist) * speed - 100; // Slight upward angle, adjust as needed

        projectiles.add(new ProjectileSpike(x, y, vx, vy));
    }

    public void draw(Graphics g) {
        if (villainImage != null) {
            // Draw the image if it's loaded successfully
            g.drawImage(villainImage, x - 20, y - 40, 80, 80, null);
        } else {
            // If the image isn't loaded, draw a magenta rectangle as a fallback
            g.setColor(Color.MAGENTA);
            g.fillRect(x - 20, y - 40, 40, 40); 
        }// Draw the villain as a simple magenta square for now
    }
}
