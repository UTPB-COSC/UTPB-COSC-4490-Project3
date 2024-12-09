package src;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;

public class Tank
{
    Game game;
    Toolkit tk;

    public static int yPos;
    public static int xPos;

    public double yVel = 0.0;
    public double yAcc = 0.1;
    public double xVel = 0.0;
    public double xAcc = 0.1;
    public double maxVel = 5.0;

    public double targetRotation = 0; // Target angle based on velocity
    public double currentRotation = 0; // Current rotation angle
    public double rotationSpeed = 0.1; // Per Frame rotation rate

    public boolean movingNorth = false;
    public boolean movingSouth = false;
    public boolean movingWest = false;
    public boolean movingEast = false;

    public static int width;
    public static int height;
    public int diagonal;

    private static BufferedImage tankBodyImage;
    private static BufferedImage tankTurretImage;
    private BufferedImage muzzleBlastImage = ImageIO.read(new File("BlastTest.png"));
    private BufferedImage[] muzzleBlast = new BufferedImage[7];
    
    private double mouseAngle;
    private int animframe = 0;
    private int animRate = 2;
    private int frameCount = 0;
    
    private Point2D[] corners;
    
    public static int shotsFired = 0; // Tracks shots in the current "magazine"
    private long lastFireTime = 0; // Tracks the last time fire() was called
    private final long fireCooldown = 1000; // Default cooldown in milliseconds
    private final long extraReloadTime = 4000; // Additional delay every fifth shot
    private final int magazineSize = 5; // Number of shots in a "magazine"
    private boolean isReloading = false;
    
    public static ArrayList<Projectile> projectiles = new ArrayList<>();
    
    private boolean isFiring = false;
    private boolean isDead = false;
    private BufferedImage youDiedImage = ImageIO.read(new File("YouDied.png"));
    
    public Tank(Game g, Toolkit tk) throws IOException {
        game = g;
        this.tk = tk;
    
        // Load the original tank body image
        BufferedImage originalTankBody = ImageIO.read(new File("TankBody1.2.png"));
        int originalWidth = originalTankBody.getWidth();
        int originalHeight = originalTankBody.getHeight();
    
        // Calculate diagonal for square side length
        diagonal = (int) Math.ceil(Math.sqrt(originalWidth * originalWidth + originalHeight * originalHeight));
    
        // Create square BufferedImage to contain the tank with rotation
        tankBodyImage = new BufferedImage(diagonal, diagonal, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = tankBodyImage.createGraphics();
    
        // Center the original image in the square image
        int xOffset = (diagonal - originalWidth) / 2;
        int yOffset = (diagonal - originalHeight) / 2;
        g2d.drawImage(originalTankBody, xOffset, yOffset, null);
        g2d.dispose();
    
        // Set width and height to the diagonal length to maintain a square for rotation
        width = diagonal;
        height = diagonal;
    
        tankTurretImage = ImageIO.read(new File("TankTurret1.3.png"));
    
        int fragHeight = muzzleBlastImage.getHeight() / 7;
        for (int i = 0 ; i < 7 ; i++) {
            Image temp = muzzleBlastImage.getSubimage(0, i * fragHeight, muzzleBlastImage.getWidth(), fragHeight).getScaledInstance(width, height, BufferedImage.SCALE_SMOOTH);
            muzzleBlast[i] = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics x = muzzleBlast[i].getGraphics();
            x.drawImage(temp, 0, 0, null);
            x.dispose();
        }
    
        g2d.setColor(Color.BLACK);
        g2d.drawString("Magazine", 10, 30); // Label
    
        int squareSize = 20; // Size of each square
        int spacing = 5;     // Space between squares
        int startX = 100;     // Starting x-coordinate
        int startY = 400;     // Starting y-coordinate
        
        for (int i = 0; i < magazineSize; i++) {
            if (i < magazineSize - shotsFired) {
                g2d.setColor(Color.GREEN); // Filled square
            } else {
                g2d.setColor(Color.RED); // Empty square
            }
            g2d.fillRect(startX + i * (squareSize + spacing), startY, squareSize, squareSize);
        }
            reset();
    }
    
    private double normalizeAngle(double angle) {
        while (angle > Math.PI) angle -= 2 * Math.PI;
        while (angle < -Math.PI) angle += 2 * Math.PI;
        return angle;
    }
    
    public void drawTank(Graphics g)
    {
        Graphics2D g2d = (Graphics2D) g;
        // Draw tank body
        // Calculate the angle in radians based on the velocity vector
        if (xVel != 0 || yVel != 0) {
            targetRotation = Math.atan2(yVel, xVel);
            }
    
        // Calculate the shortest rotation direction
        double angleDifference = normalizeAngle(targetRotation - currentRotation);
        // Normalize both angles to avoid unnecessary long rotation
        currentRotation = normalizeAngle(currentRotation);
        targetRotation = normalizeAngle(targetRotation);
                
        // Smoothly rotate towards the target rotation using the shortest path; + clockwise, - counterclockwise
        if (Math.abs(angleDifference) > rotationSpeed) {
            if (angleDifference > 0) {
                currentRotation += rotationSpeed;
            } else {
                currentRotation -= rotationSpeed;
            }
        } else {
            // If the angle difference is small enough, snap to the target rotation
            currentRotation = targetRotation;
        }
                
        // Apply rotation to image using AffineTransform
        AffineTransform at = new AffineTransform();
        at.rotate(currentRotation, width / 2, height / 2);
        AffineTransformOp ato = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);

        BufferedImage tankBody = ato.filter(tankBodyImage, null);
        g.drawImage(tankBody, xPos, yPos, null);
    
        // Draw tank turret
        // Calculate the angle between the tank's position and the mouse cursor
        mouseAngle = Math.atan2(game.mouseY - (yPos + height / 2), game.mouseX - (xPos + width / 2));
    
        // Apply rotation using AffineTransform
        AffineTransform atMouse = new AffineTransform();
        atMouse.rotate(mouseAngle, width / 2, height / 2);
        AffineTransformOp atoMouse = new AffineTransformOp(atMouse, AffineTransformOp.TYPE_BILINEAR);
    
        BufferedImage tankTurret = atoMouse.filter(tankTurretImage, null);
        g.drawImage(tankTurret, xPos, yPos, null);
    
        // Draw muzzle blast animation
        if (isFiring) {
            double centerX = xPos + width / 2.0;
            double centerY = yPos + height / 2.0;
        
            double blastX = centerX + Math.cos(mouseAngle) * (width / 2.0);
            double blastY = centerY + Math.sin(mouseAngle) * (width / 2.0);
        
            // Apply rotation using AffineTransform
            AffineTransform oldTransform = g2d.getTransform(); // Save the current transform
    
            AffineTransform transform = new AffineTransform();
            transform.translate(blastX - muzzleBlast[animframe].getWidth() / 2.0,
                blastY - muzzleBlast[animframe].getHeight() / 2.0);
            transform.rotate(mouseAngle, muzzleBlast[animframe].getWidth() / 2.0,
                muzzleBlast[animframe].getHeight() / 2.0);
    
            g2d.setTransform(transform);
            g2d.drawImage(muzzleBlast[animframe], 0, 0, null);
    
            g2d.setTransform(oldTransform); // Restore the original transform
        
            // Update animation frames
            frameCount++;
            if (frameCount % animRate == 0) {
                animframe++;
                if (animframe >= muzzleBlast.length) {
                    animframe = 0;
                    isFiring = false; // End the animation
                }
            }
        }
    
        // Draw hitbox
        if (game.debug)
        {
            // Get the rotated corners of the player image
            corners = RotationUtil.getRotatedCorners(xPos, yPos, tankBodyImage.getWidth(), tankBodyImage.getHeight(), 102, 61, currentRotation);
                
            g2d.setColor(Color.RED);
    
            // Draw lines between each consecutive corner to form the bounding box
            for (int i = 0; i < corners.length; i++) {
                Point2D start = corners[i];
                Point2D end = corners[(i + 1) % corners.length]; // Ensures the last corner connects to the first
                g2d.drawLine((int) start.getX(), (int) start.getY(), (int) end.getX(), (int) end.getY());
            }
        }

        // Display "YOU DIED" image if dead
        if (isDead && youDiedImage != null) {
            int screenWidth = tk.getScreenSize().width;
            int screenHeight = tk.getScreenSize().height;

            int imageWidth = youDiedImage.getWidth();
            int imageHeight = youDiedImage.getHeight();

            // Calculate the center position for the image
            int centerX = (screenWidth - imageWidth) / 2;
            int centerY = (screenHeight - imageHeight) / 2;

            g2d.drawImage(youDiedImage, centerX, centerY, null);
        }
    }
    
    public boolean collide(Bomber bomber)
    {
        corners = RotationUtil.getRotatedCorners(xPos, yPos, tankBodyImage.getWidth(),
            tankBodyImage.getWidth(), 102, 61, currentRotation);
    
        // Define the bomber’s core collision bounds
        int bomberCoreX = bomber.xPos + bomber.width / 2;
    
        // Check if any of the player’s rotated corners overlap with the bomber’s core
        for (Point2D corner : corners) {
            if (corner.getX() >= bomberCoreX && 
                corner.getX() <= bomber.xPos + bomber.width && 
                corner.getY() >= bomber.yPos && 
                corner.getY() <= bomber.yPos + bomber.width) {
                collide();
                return true;
            }
        }
        
        // Implement position adjustments relative to player corners inside bomber collision logic because its constantly called and works.
        int screenWidth = tk.getScreenSize().width;
        int screenHeight = tk.getScreenSize().height;
            
        // Variables to track required adjustments
        double xAdjustment = 0;
        double yAdjustment = 0;
            
        // Assuming getRotatedCorners() gives us an array of Points representing the player's four corners
        // Check each corner's position relative to screen boundaries
        for (Point2D corner : corners) {
            if (corner.getX() < 0) {
                xVel = 0;
                xAdjustment = Math.max(xAdjustment, -corner.getX()); // Move right
            } else if (corner.getX() > screenWidth) {
                xVel = 0;
                xAdjustment = Math.min(xAdjustment, screenWidth - corner.getX()); // Move left
            }
            
            if (corner.getY() < 0) {
                yVel = 0;
                yAdjustment = Math.max(yAdjustment, -corner.getY()); // Move down
            } else if (corner.getY() > screenHeight) {
                yVel = 0;
                yAdjustment = Math.min(yAdjustment, screenHeight - corner.getY()); // Move up
            }
        }
            
        // Apply adjustments to keep the player within bounds
        xPos += xAdjustment;
        yPos += yAdjustment;
    
        return false;
    }
    
    public boolean isHit(Projectile projectile) {
    
        // Check if the projectile's center is inside the polygon formed by the corners
        Polygon polygon = new Polygon();
        for (Point2D corner : corners) {
            polygon.addPoint((int) corner.getX(), (int) corner.getY());
        }
    
        double projX = projectile.getX();
        double projY = projectile.getY();
    
        return polygon.contains(projX, projY);
    }
    
    public boolean collideP(ArrayList<Projectile> enemyProjectiles) {
        for (int i = 0; i < enemyProjectiles.size(); i++) {
            Projectile p = enemyProjectiles.get(i);
        
            // Ignore projectiles spawned by the player itself
            if (p.getOwner() == this) continue;
        
            // Check if projectile hits the player
            if (isHit(p)) {
                enemyProjectiles.remove(i);
                i--; // Adjust index to account for removal
                collide();
                return true;
            }
        }
        return false;
    }
    
    private void collide() {
        isDead = true;
        new Thread(() ->
        {
            try
            {
                AudioInputStream ais = AudioSystem.getAudioInputStream(new File("explosionPlayer.wav").getAbsoluteFile());
                Clip clip = AudioSystem.getClip();
                clip.open(ais);
                FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                gain.setValue(20f * (float) Math.log10(game.volume));
                clip.start();
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }).start();
    }
    
    public void fire() throws IOException
    {
        if (shotsFired >= magazineSize || isReloading || isFiring) return;
    
        long currentTime = System.currentTimeMillis();
    
        // Check if enough time has passed since last shot
        if (currentTime - lastFireTime < fireCooldown) {
            return; // Cooldown in progress
        }
    
        // Spawn a new projectile
        double rotation = mouseAngle;
        int centerX = xPos + width / 2;
        int centerY = yPos + height / 2;
        projectiles.add(new Projectile((double)centerX, (double)centerY, rotation, 18.0, game, tk, this));
    
        // Trigger fire sequence.
        isFiring = true;
        animframe = 0;
        frameCount = 0;
    
        // Calculate delay based on shot count
        long delay = fireCooldown;
        if (shotsFired == magazineSize) {
            delay += extraReloadTime;
        }
    
        // Check if enough time has passed since last shot
        if (currentTime - lastFireTime < delay) {
            return; // Cooldown in progress
        }
    
        // Set timer and increment shot count
        lastFireTime = currentTime;
        shotsFired++;
    
        // Reset magazine if limit reached
        if (shotsFired > magazineSize) {
            shotsFired = 1; // Start a new magazine
        }
    }
    
    public void drawProjectiles(Graphics g) {
        for (Projectile p : projectiles) {
            p.draw(g);
        }
    }
    
    public void updateProjectiles() {
        // Update and remove off-screen projectiles
        projectiles.removeIf(p -> {
            p.update();
            return p.isOffScreen();
        });
    }
    
    public static int getX() {
        return xPos;
    }
        
    public static int getY() {
        return yPos;
    }
        
    // Movement flags per direction
    public void moveNorth() {
        movingNorth = true;
    }
        
    public void moveSouth() {
        movingSouth = true;
    }
        
    public void moveWest() {
        movingWest = true;
    }
        
    public void moveEast() {
        movingEast = true;
    }
        
    public void stopNorth() {
        movingNorth = false;
    }
        
    public void stopSouth() {
        movingSouth = false;
    }
        
    public void stopWest() {
        movingWest = false;
    }
        
    public void stopEast() {
        movingEast = false;
    }
    
    public static double getWidth() {
        return tankBodyImage.getWidth();
    }

    public static double getHeight() {
        return tankBodyImage.getHeight();
    }

    public void reset()
    {
        xPos = tk.getScreenSize().width / 2;
        yPos = tk.getScreenSize().height / 2;

        movingEast = false;
        movingWest = false;
        movingNorth = false;
        movingSouth = false;

        yVel = 0.0;
        xVel = 0.0;
        targetRotation = 0.0;
        currentRotation = 0.0;

        shotsFired = 0;
        isDead = false;
    }

    public void update()
    {
    // Vertical movement
    if (movingNorth) {
        yVel -= yAcc; // Accelerate upwards
        if (yVel < -maxVel) yVel = -maxVel; // Cap at max upwards velocity
    } else if (movingSouth) {
        yVel += yAcc; // Accelerate downwards
        if (yVel > maxVel) yVel = maxVel; // Cap at max downwards velocity
    } else {
        // Decelerate if no vertical movement
        if (yVel > 0) {
            yVel -= yAcc; // Decelerate downwards
            if (yVel < 0) yVel = 0; // Stop at 0
        } else if (yVel < 0) {
            yVel += yAcc; // Decelerate upwards
            if (yVel > 0) yVel = 0; // Stop at 0
        }
    }

    // Horizontal movement
    if (movingWest) {
        xVel -= xAcc; // Accelerate left
        if (xVel < -maxVel) xVel = -maxVel; // Cap at max leftwards velocity
    } else if (movingEast) {
        xVel += xAcc; // Accelerate right
        if (xVel > maxVel) xVel = maxVel; // Cap at max rightwards velocity
    } else {
        // Decelerate if no horizontal movement
        if (xVel > 0) {
            xVel -= xAcc; // Decelerate rightwards
            if (xVel < 0) xVel = 0; // Stop at 0
        } else if (xVel < 0) {
            xVel += xAcc; // Decelerate leftwards
            if (xVel > 0) xVel = 0; // Stop at 0
        }
    }

    // Update position based on velocities
    xPos += xVel;
    yPos += yVel;
    }
}
