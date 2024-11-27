import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.awt.Rectangle;

public class EnemyBoat {
    private int x, y;
    private int dx, dy;
    private BufferedImage enemyBoatImage;

    // Dimensions of the enemy boat, slightly larger than player boat
    private final int WIDTH = 60;  // Adjust size as needed
    private final int HEIGHT = 40; // Adjust size as needed

    private final int patrolX1, patrolY1, patrolX2, patrolY2; // Patrol boundaries
    private final int speed = 2;  // Speed of the enemy boat
    private boolean destroyed = false; // Flag to track if the boat is destroyed
    private boolean destroying = false; // Flag to indicate the boat is in the process of being destroyed
    private long destructionStartTime = 0; // Time when the destruction started
    private final int DESTRUCTION_DURATION = 500; // Duration of particle effect in milliseconds


    public EnemyBoat(int startX, int startY, int patrolX1, int patrolY1, int patrolX2, int patrolY2) {
        this.x = startX;
        this.y = startY;
        this.patrolX1 = patrolX1;
        this.patrolY1 = patrolY1;
        this.patrolX2 = patrolX2;
        this.patrolY2 = patrolY2;
        this.dx = speed;
        this.dy = speed;
        loadEnemyBoatImage();
    }
    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics g = resizedImage.getGraphics();
        g.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        g.dispose();
        return resizedImage;
    }
    
    private void loadEnemyBoatImage() {
        try {
            BufferedImage originalImage = ImageIO.read(new File("src/assets/enemyboat1.png"));
            enemyBoatImage = resizeImage(originalImage, WIDTH, HEIGHT); // Resize to match display size
        } catch (IOException e) {
            System.out.println("Error loading enemy boat image.");
            e.printStackTrace();
        }
    }
    

    public void updatePosition() {
        if (destroying) {
            long elapsedTime = System.currentTimeMillis() - destructionStartTime;
            if (elapsedTime >= DESTRUCTION_DURATION) {
                destroying = false;
                destroyed = true; // Mark the boat as fully destroyed
            }
        } else if (!destroyed) {
            // Normal movement logic
            x += dx;
            y += dy;
    
            // Reverse direction when reaching patrol area boundaries
            if (x <= patrolX1 || x >= patrolX2 - WIDTH) {
                dx = -dx;
            }
            if (y <= patrolY1 || y >= patrolY2 - HEIGHT) {
                dy = -dy;
            }
        }
    }
    
    public void draw(Graphics g) {
        if (destroying) {
            // Draw particle effect
            BufferedImage enemyFragment = enemyBoatImage.getSubimage(0, 0, WIDTH / 2, HEIGHT / 2);
            g.drawImage(enemyFragment, x, y, WIDTH / 2, HEIGHT / 2, null);
            // Additional particle drawing logic can be added here
        } else if (!destroyed && enemyBoatImage != null) {
            // Draw the enemy boat if not destroyed
            g.drawImage(enemyBoatImage, x, y, null);
        }
    }
    
    

    public Rectangle getBounds() {
        if (destroyed) {
            return new Rectangle(0, 0, 0, 0); // No collision if destroyed
        }
        return new Rectangle(x, y, WIDTH, HEIGHT);
    }

    public void destroy() {
        if (!destroying && !destroyed) {
            destroying = true;
            destructionStartTime = System.currentTimeMillis();
        }
    }
    

    public boolean isDestroyed() {
        return destroyed;
    }
    // Getters
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
    public BufferedImage  getImage() {
        return enemyBoatImage;
    }
    public int getWidth() {
        return WIDTH; // Return the resized width
    }

    public int getHeight() {
        return HEIGHT; // Return the resized height
    }

    // Optional: Check if player boat is in range
    public boolean isPlayerInRange(Boat playerBoat) {
        int detectionRange = 150; // Set range as needed
        return getBounds().intersects(playerBoat.getBounds());
    }
}
