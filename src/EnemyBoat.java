import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;

public class EnemyBoat {
    private int x, y;
    private int dx, dy;
    private BufferedImage enemyBoatImage;
    private List<Projectile> projectiles = new ArrayList<>();

    private final int WIDTH = 60;  // Adjust size as needed
    private final int HEIGHT = 40; // Adjust size as needed
    private final int patrolX1, patrolY1, patrolX2, patrolY2; // Patrol boundaries
    private final int speed = 1;  // Speed of the enemy boat
    private final int detectionRange = 600; // Shooting range
 
    
    
    public EnemyBoat(int startX, int startY, int patrolX1, int patrolY1, int patrolX2, int patrolY2) {
        this.x = startX;
        this.y = startY;
        this.patrolX1 = patrolX1;
        this.patrolY1 = patrolY1;
        this.patrolX2 = patrolX2;
        this.patrolY2 = patrolY2;
        this.dx = speed;
        this.dy = 0;
        loadEnemyBoatImage();
        this.lastShotTime = System.currentTimeMillis(); // Initialize the shot timer

    }

    private void loadEnemyBoatImage() {
        try {
            enemyBoatImage = ImageIO.read(new File("src/assets/enemyboat1.png"));
        } catch (IOException e) {
            System.out.println("Error loading enemy boat image.");
            e.printStackTrace();
        }
    }

    public void updatePosition() {
        x += dx;
        y += dy;
    
        // Horizontal patrol only
        if (x <= patrolX1 || x >= patrolX2 - WIDTH) {
            dx = -dx;
        }
    
        // Vertical patrol (if applicable)
        if (y <= patrolY1 || y >= patrolY2 - HEIGHT) {
            dy = -dy;
        }
    }
    
  
    
   



    public void draw(Graphics g) {
        if (enemyBoatImage != null) {
            g.drawImage(enemyBoatImage, x, y, WIDTH, HEIGHT, null);
        }
        
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, WIDTH, HEIGHT);
    }

    public boolean isPlayerInRange(Boat playerBoat) {
        int playerX = playerBoat.getX();
        int playerY = playerBoat.getY();
        int distance = (int) Math.sqrt(Math.pow(playerX - x, 2) + Math.pow(playerY - y, 2));
    
        System.out.println("Distance to player: " + distance); 
        System.out.println("Player in range: " + (distance <= detectionRange));
        return distance <= detectionRange;
    }
    
    

    public List<Projectile> getProjectiles() {
        return projectiles;
    }
}
