package src;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

public class EnemyTank {
    
    Game game;
    Toolkit tk;

    private int screenWidth;
    private int screenHeight;
    
    public double yVel = 0.0;
    public double yAcc = 0.1;
    public double xVel = 0.0;
    public double xAcc = 0.1;
    public double maxVel = 5.0;

    private long lastFireTime = 0; // Tracks the last time the tank fired
    private long fireInterval = 2500; // Interval between shots in milliseconds (2.5 seconds)

    private int rightSideStart, rightSideEnd;
    private int maxY;

    private Point2D[] corners;

    private int xPos, yPos;
    private int width, height;
    private double targetX, targetY;
    private final double targetThreshold = 40.0;
    private double currentRotation, targetRotation;
    private double rotationSpeed = 0.1;

    private boolean isFiring;
    private BufferedImage muzzleBlastImage = ImageIO.read(new File("BlastTest.png"));
    private BufferedImage[] muzzleBlast = new BufferedImage[7];
    private int animframe = 0;
    private int animRate = 2;
    private int frameCount = 0;

    private BufferedImage tankBody;
    private BufferedImage tankTurret;
    public static ArrayList<Projectile> projectiles = new ArrayList<>();

    public EnemyTank(Game g, Toolkit tk) throws IOException {
        screenWidth = tk.getScreenSize().width;
        screenHeight = tk.getScreenSize().height;

        this.game = g;
        this.tankBody = ImageIO.read(new File("EnemyTankBody1.2.png"));
        try {
            this.tankTurret = ImageIO.read(new File("EnemyTankTurret1.3.png"));
            if (this.tankTurret == null) {
                System.out.println("Image loaded but is null!");
            }
        } catch (IOException e) {
            System.out.println("Error loading turret image: " + e.getMessage());
        }
        this.width = tankBody.getWidth();
        this.height = tankBody.getHeight();

        // Set initial random target
        generateTargetPosition(screenWidth, screenHeight);

        // Calculate random x position within the right 1/5th of the screen
        rightSideStart = (int)(screenWidth * 0.8); // Start of the rightmost 1/5th
        rightSideEnd = screenWidth - width; // Ensure tank doesn't spawn out of bounds
        this.xPos = rightSideStart + new Random().nextInt(rightSideEnd - rightSideStart);
        // Calculate random y position within the screen height
        maxY = screenHeight - height; // Ensure tank doesn't spawn out of bounds
        this.yPos = new Random().nextInt(maxY);

        int fragHeight = muzzleBlastImage.getHeight() / 7;
        for (int i = 0 ; i < 7 ; i++) {
            Image temp = muzzleBlastImage.getSubimage(0, i * fragHeight, muzzleBlastImage.getWidth(), fragHeight).getScaledInstance(width, height, BufferedImage.SCALE_SMOOTH);
            muzzleBlast[i] = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics x = muzzleBlast[i].getGraphics();
            x.drawImage(temp, 0, 0, null);
            x.dispose();
        }
    }

    private void generateTargetPosition(int screenWidth, int screenHeight) {
        // Randomly generate a target position within screen bounds
        Random rand = new Random();
        targetX = rand.nextInt(screenWidth);
        targetY = rand.nextInt(screenHeight);
    }

    private double normalizeAngle(double angle) {
        while (angle > Math.PI) angle -= 2 * Math.PI;
        while (angle < -Math.PI) angle += 2 * Math.PI;
        return angle;
    }

    public void draw(Graphics g) {
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

        AffineTransform at = new AffineTransform();
        at.rotate(currentRotation, xPos + width / 2.0, yPos + height / 2.0);
        at.translate(xPos, yPos);
        g2d.drawImage(tankBody, at, null);

        // Draw tank turret
        // Calculate the turret's rotation point (center of the tank)
        double turretCenterX = xPos + width / 2.0;
        double turretCenterY = yPos + height / 2.0;
        
        // Calculate the angle between the tank's position and the player's position
        Tank player = game.getPlayer();
        double dx = player.getX() - turretCenterX;
        double dy = player.getY() - turretCenterY;
        double angle = Math.atan2(dy, dx);

        // Apply rotation using AffineTransform
        AffineTransform atTurret = new AffineTransform();
        atTurret.translate(xPos + width / 2.0 - tankTurret.getWidth() / 2.0, 
            yPos + height / 2.0 - tankTurret.getHeight() / 2.0);
        atTurret.rotate(angle, tankTurret.getWidth() / 2.0, tankTurret.getHeight() / 2.0);
        g2d.drawImage(tankTurret, atTurret, null);

        // Draw muzzle blast animation
        if (isFiring) {
            double centerX = xPos + width / 2.0;
            double centerY = yPos + height / 2.0;
    
            double blastX = centerX + Math.cos(angle) * (width / 2.0);
            double blastY = centerY + Math.sin(angle) * (width / 2.0);
    
            // Apply rotation using AffineTransform
            AffineTransform oldTransform = g2d.getTransform(); // Save the current transform

            AffineTransform transform = new AffineTransform();
            transform.translate(blastX - muzzleBlast[animframe].getWidth() / 2.0,
                blastY - muzzleBlast[animframe].getHeight() / 2.0);
            transform.rotate(angle, muzzleBlast[animframe].getWidth() / 2.0, 
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
            corners = RotationUtil.getRotatedCorners(xPos, yPos, width, height, 102, 61, currentRotation);
            
            g2d.setColor(Color.RED);

            // Draw lines between each consecutive corner to form the bounding box
            for (int i = 0; i < corners.length; i++) {
                Point2D start = corners[i];
                Point2D end = corners[(i + 1) % corners.length]; // Ensures the last corner connects to the first
                g2d.drawLine((int) start.getX(), (int) start.getY(), (int) end.getX(), (int) end.getY());
            }
        }
    }

    public void drawProjectiles(Graphics g) {
        // Draw projectiles
        for (Projectile p : projectiles) {
            p.draw(g);
        }
    }

    public boolean isHit(Projectile projectile) {
        corners = RotationUtil.getRotatedCorners(xPos, yPos, width, height, 102, 61, currentRotation);

        // Check if the projectile's center is inside the polygon formed by the corners
        Polygon polygon = new Polygon();
        for (Point2D corner : corners) {
            polygon.addPoint((int) corner.getX(), (int) corner.getY());
        }

        double projX = projectile.getX();
        double projY = projectile.getY();

        return polygon.contains(projX, projY);
    }

    public boolean collideP(ArrayList<Projectile> playerProjectiles) {
        for (int i = 0; i < playerProjectiles.size(); i++) {
            Projectile p = playerProjectiles.get(i);
    
            // Ignore projectiles spawned by the player itself
            if (p.getOwner() == this) continue;
    
            // Check if projectile hits the player
            if (isHit(p)) {
                playerProjectiles.remove(i);
                i--; // Adjust index to account for removal
                collide();
                game.score += 1;
                return true;
            }
        }
        return false;
    }

    private void collide(){
        new Thread(() ->
        {
            try
            {
                AudioInputStream ais = AudioSystem.getAudioInputStream(new File("explosionEnemy.wav").getAbsoluteFile());
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
        reset();
    }

    public void updateProjectiles() {
        // Update and remove off-screen projectiles
        projectiles.removeIf(p -> {
            p.update();
            return p.isOffScreen();
        });
    }

    public void fireAtPlayer() throws IOException {
        // Get player location
        Tank player = game.getPlayer();
        int playerX = player.getX();
        int playerY = player.getY();
    
        // Determine angle from enemy to player
        double dx = playerX - (xPos + width / 2.0);
        double dy = playerY - (yPos + height / 2.0);
        double angle = Math.atan2(dy, dx);

        isFiring = true;
    
        // Fire directly at player, adjusted projectile speed with difficulty
        projectiles.add(new Projectile(xPos + width / 2.0, yPos + height / 2.0, angle, (game.difficulty * 1.25 + 1) * 10, game, tk, this));
    }

    public void reset()
    {
        xPos = rightSideStart + new Random().nextInt(rightSideEnd - rightSideStart);
        yPos = new Random().nextInt(maxY);
        
        yVel = 0.0;
        xVel = 0.0;
        currentRotation = 0.0;
    }

    public void update() throws IOException {
        // Calculate the distance to the target
        double distanceToTarget = Math.sqrt(Math.pow(targetX - xPos, 2) + Math.pow(targetY - yPos, 2));
    
        // If within the threshold, generate a new target
        if (distanceToTarget < targetThreshold) {
            generateTargetPosition(screenWidth, screenHeight);
            return;
        }

        // Calculate the angle toward the target
        double angleToTarget = Math.atan2(targetY - yPos, targetX - xPos);

        // Accelerate toward the target
        xVel += xAcc * Math.cos(angleToTarget);
        yVel += yAcc * Math.sin(angleToTarget);

        // Cap the velocity
        double totalVel = Math.sqrt(xVel * xVel + yVel * yVel);
        if (totalVel > maxVel) {
            xVel = (xVel / totalVel) * maxVel;
            yVel = (yVel / totalVel) * maxVel;
        }

        // Update position
        xPos += xVel;
        yPos += yVel;

        // Fire logic
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFireTime >= fireInterval) {
            fireAtPlayer();
            lastFireTime = currentTime;
        }

        if (collideP(Tank.projectiles)){
            collide();
        }
    }
}
