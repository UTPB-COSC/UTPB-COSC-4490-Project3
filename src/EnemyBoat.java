import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.awt.Rectangle;
import java.util.List;

public class EnemyBoat {
    private int x, y;
    private int dx, dy;
    private BufferedImage enemyBoatImage;

    // Dimensions of the enemy boat
    private final int WIDTH = 60;
    private final int HEIGHT = 40;

    private int patrolX1, patrolY1, patrolX2, patrolY2; // Patrol area coordinates
    private final int speed = 2;

    private boolean destroyed = false;
    private boolean destroying = false;
    private long destructionStartTime = 0;
    private final int DESTRUCTION_DURATION = 500;

    private long lastShotTime = 0; // Tracks time of last shot
    private final int SHOOT_DELAY = 1000; // Delay between shots in milliseconds

    private int detectionRange = 300; // Example value, adjust as needed

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
            enemyBoatImage = resizeImage(originalImage, WIDTH, HEIGHT);
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
                destroyed = true;
            }
        } else if (!destroyed) {
            x += dx;
            y += dy;

            if (x <= patrolX1 || x >= patrolX2 - WIDTH) {
                dx = -dx;
            }
            if (y <= patrolY1 || y >= patrolY2 - HEIGHT) {
                dy = -dy;
            }
        }
    }

    public void checkAndShoot(Boat playerBoat, List<Projectile> projectiles) {
        if (!destroyed && !destroying && isPlayerInRange(playerBoat)) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastShotTime >= SHOOT_DELAY) {
                shootAtPlayer(playerBoat, projectiles);
                lastShotTime = currentTime;
            }
        }
    }

    private void shootAtPlayer(Boat playerBoat, List<Projectile> projectiles) {
        int playerX = playerBoat.getX();
        int playerY = playerBoat.getY();

        // Calculate direction vector
        int deltaX = playerX - x;
        int deltaY = playerY - y;
        double magnitude = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        // Normalize direction
        double directionX = deltaX / magnitude;
        double directionY = deltaY / magnitude;

        // Create projectile and add to the list
        int projectileX = x + WIDTH / 2;
        int projectileY = y + HEIGHT / 2;
        projectiles.add(new Projectile(projectileX, projectileY, directionX, directionY, "src/assets/enemyProjectile.png"));
    }

    public void draw(Graphics g) {
        if (destroying) {
            BufferedImage enemyFragment = enemyBoatImage.getSubimage(0, 0, WIDTH / 2, HEIGHT / 2);
            g.drawImage(enemyFragment, x, y, WIDTH / 2, HEIGHT / 2, null);
        } else if (!destroyed && enemyBoatImage != null) {
            g.drawImage(enemyBoatImage, x, y, null);
        }
    }

    public Rectangle getBounds() {
        if (destroyed) {
            return new Rectangle(0, 0, 0, 0);
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

    public BufferedImage getImage() {
        return enemyBoatImage;
    }

    public int getWidth() {
        return WIDTH; // Return the resized width
    }

    public int getHeight() {
        return HEIGHT; // Return the resized height
    }

    public boolean isPlayerInRange(Boat playerBoat) {
        int playerX = playerBoat.getX();
        int playerY = playerBoat.getY();

        // Calculate distance to player
        int distanceX = playerX - x;
        int distanceY = playerY - y;
        double distance = Math.sqrt(distanceX * distanceX + distanceY * distanceY);

        return distance <= detectionRange;
    }

    // Setter method to update patrol area
    public void setPatrolArea(int patrolX1, int patrolY1, int patrolX2, int patrolY2) {
        this.patrolX1 = patrolX1;
        this.patrolY1 = patrolY1;
        this.patrolX2 = patrolX2;
        this.patrolY2 = patrolY2;
    }
}
