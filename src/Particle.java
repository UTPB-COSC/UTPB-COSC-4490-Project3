package src;

import java.awt.*;
import java.util.Random;

public class Particle {
    private double x, y; // Position
    private double velocityX, velocityY; // Velocity
    private int size; // Size of the particle
    private int lifespan; // Lifespan of the particle in frames
    private Color color; // Color of the particle
    private Random random = new Random();

    public Particle(double x, double y) {
        this.x = x;
        this.y = y;
        this.velocityX = (random.nextDouble() - 0.5) * 4; // Random velocity in X
        this.velocityY = (random.nextDouble() - 0.5) * 4; // Random velocity in Y
        this.size = 2 + random.nextInt(4); // Random size between 2 and 5
        this.lifespan = 30 + random.nextInt(30); // Random lifespan between 30 and 60 frames
        this.color = new Color(random.nextFloat(), random.nextFloat(), random.nextFloat(), 1.0f); // Random color
    }

    public void update() {
        x += velocityX;
        y += velocityY;
        lifespan--; // Decrease lifespan
    }

    public void draw(Graphics2D g2d) {
        g2d.setColor(color);
        g2d.fillOval((int) x, (int) y, size, size);
    }

    public boolean isAlive() {
        return lifespan > 0;
    }
}