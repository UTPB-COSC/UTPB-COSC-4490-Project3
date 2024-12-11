package src;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.awt.Rectangle;

public class EnemyShip {
    private double x, y; // Position of the enemy ship
    private double speed; // Speed of the enemy ship
    private BufferedImage image; // Image for the enemy ship
    private boolean active; // Whether the enemy ship is currently visible
    //private boolean destroyed;       // Whether the enemy is destroyed

    private static final Random random = new Random();

    public EnemyShip() {
        this.speed = 2 + random.nextDouble() * 3; // Random speed between 2 and 5
        loadEnemyImage(); // Load the image
        resetPosition(); // Start off-screen
        //this.destroyed = false;
    }

    // Load the PNG image
    private void loadEnemyImage() {
        try {
            image = ImageIO.read(new File("alien.png"));
        } catch (IOException e) {
            System.err.println("Error loading enemy ship image: " + e.getMessage());
            image = null;
        }
    }

    // Reset the enemy ship's position
    public void resetPosition() {
        this.x = -getWidth(); // Start off-screen to the left
        this.y = random.nextInt(600 - getHeight()); // Random Y position within screen height
        this.active = false; // Initially inactive
    }

    // Activate the enemy ship to start flying
    public void activate() {
        this.active = true;
    }

    public void update() {
        if (active) {
            x += speed; // Move to the right
            if (x > 800) { // If the ship moves off-screen, deactivate
                resetPosition();
                active = false;
            }
        }
    }

    public void draw(Graphics2D g2d) {
        if (active && image != null) {
            g2d.drawImage(image, (int) x, (int) y, null);
        }
    }

    public boolean isActive() {
        return active;
    }

    public int getWidth() {
        return image != null ? image.getWidth() : 0;
    }

    public int getHeight() {
        return image != null ? image.getHeight() : 0;
    }

    // Get the bounds for collision detection
    public Rectangle getEnemyBounds() {
        int width = image != null ? image.getWidth() : 0;
        int height = image != null ? image.getHeight() : 0;
        return new Rectangle((int) x - width / 2, (int) y - height / 2, width, height);
    }

    // Set the asteroid as destroyed
    //public void setDestroyed(boolean destroyed) {
        //this.destroyed = destroyed;
    //}

    // Check if the asteroid is destroyed
    //public boolean isDestroyed() {
        //return destroyed;
    //}

    // Get the current X position
    public double getX() {
        return x;
    }

    // Get the current Y position
    public double getY() {
        return y;
    }

    //public synchronized void delEnemy() {
        //active = false;
    //}


}

