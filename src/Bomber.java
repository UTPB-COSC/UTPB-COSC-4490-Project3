package src;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Bomber
{
    Game game;

    private Toolkit tk;

    public int yPos;
    public int xPos;

    public int hitboxX;
    public int hitboxWidth;

    public double defaultVel = 4.0;
    public double xVel = 3.0;

    public int width;
    public int height;

    private boolean scoreable = true;
    public boolean spawnable = true;

    private static BufferedImage[] explosionImages = new BufferedImage[4];

    private long lastExplosionFrameTime = 0;
    private int currentExplosionIndex;

    private int dtp = 400;

    public Bomber(Game g, Toolkit tk, int y, int w, int h)
    {
        game = g;
        this.tk = tk;

        width = w;
        height = h;

        xPos = tk.getScreenSize().width;
        yPos = (int) (Math.random() * (tk.getScreenSize().height - h));
    }

    public void drawBomber(Graphics g)
    {
        Graphics2D g2d = (Graphics2D) g;

        hitboxX = xPos + width / 2;
        hitboxWidth = width / 2;

        if(startBombing()) {
            if (explosionImages[0] == null){
                try {
                    for (int i = 0; i < 4; i++) {
                        explosionImages[i] = ImageIO.read(new File("Explosion" + (i + 1) + ".png"));
                    }
                } catch (IOException e) {
                e.printStackTrace();
                }
            }

             // Animation parameters
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastExplosionFrameTime >= 300) { // 0.3 seconds per frame
                lastExplosionFrameTime = currentTime;
                currentExplosionIndex = (int) (Math.random() * explosionImages.length);
            }

            // Draw explosions
            for (int i = 0; i < 3; i++) { // 3 fixed positions
                // Calculate explosion position
                int explosionX = hitboxX;
                int explosionY = yPos - 26 + (i * height / 3);

                // Scale the image
                AffineTransform at = new AffineTransform();
                at.translate(explosionX, explosionY);
                at.scale(2.25, 2.25);

                // Draw the explosion image
                g2d.drawImage(explosionImages[currentExplosionIndex], at, null);
            }
        }

        g2d.drawImage(game.bomberImage, xPos, yPos, null);
        
        if (game.debug)
        {
            g2d.setColor(Color.RED);
            g2d.drawRect(hitboxX, yPos, hitboxWidth, height);
        }
    }

    private boolean startBombing() {
        // Calculate the centers of the bomber and the player
        double bomberCenterX = xPos + width / 2.0;
        double bomberCenterY = yPos + height / 2.0;
        
        double playerCenterX = Tank.getX() + Tank.getWidth() / 2.0;
        double playerCenterY = Tank.getY() + Tank.getHeight() / 2.0;
        
        // Compute the Euclidean distance
        double distance = Math.sqrt(Math.pow(bomberCenterX - playerCenterX, 2) + Math.pow(bomberCenterY - playerCenterY, 2));
        // Check if the player is within the detection range
        return distance <= dtp;
    }

    public boolean update()
    {
        xVel = defaultVel + game.difficulty;
        xPos -= xVel;

        startBombing();

        if (scoreable && xPos < tk.getScreenSize().width / 2)
        {
            scoreable = false;
            return true;
        }
        return false;
    }
}
