package src;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

public class Asteroid
{
    Game game;

    private Toolkit tk;

    public int yPos;
    public int xPos;

    public double defaultVel = 3.0;
    public double xVel = 3.0;

    public int width;
    public int height;
    public int gap;

    private boolean scoreable = true;
    public boolean spawnable = true;
    private BufferedImage image;
    
    
    private double x, y;             // Position
    private double velocityX, velocityY; // Velocity components
    private int size;                // Size of the asteroid
    private boolean destroyed;       // Whether the asteroid is destroyed


    //public Pipe(Game g, Toolkit tk, int y, int w, int h)
    public Asteroid(Game g, Toolkit tk, int x, int y, int size)
    {
    	
        game = g;
        this.tk = tk;
/*
        xPos = tk.getScreenSize().width;

        width = w;
        height = h;
        if (g.randomGaps) {
            int range = tk.getScreenSize().height / 3;
            gap = tk.getScreenSize().height / 6 + (int) (Math.random() * range);
        } else {
            gap = tk.getScreenSize().height / 3;
        }

        yPos = y + gap / 2;
*/        
        
        
        this.x = x;
        this.y = y;
        this.size = size;
        this.destroyed = false;

        // Randomly set velocity (speed and direction)
        Random rand = new Random();
        double speed = 1 + rand.nextDouble() * 2; // Speed between 1 and 3
        double angle = rand.nextDouble() * 2 * Math.PI; // Random angle between 0 and 2*PI
        this.velocityX = speed * Math.cos(angle);
        this.velocityY = speed * Math.sin(angle);

        loadImage();
    }

    public void drawAsteroid(Graphics2D g2d) {
        if (image != null) {
/*
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(game.pipeImage, xPos, yPos, null);
        g2d.drawImage(game.flippedPipe, xPos, yPos - gap - height, null);
*/
            //g2d.translate(x, y);
            int imageWidth = image.getWidth();
            int imageHeight = image.getHeight();
            g2d.drawImage(image, (int) x, (int) y, null);

            //g2d.setColor(Color.GRAY);
            // g2d.fillOval((int) x - size / 2, (int) y - size / 2, size, size);

            if (game.debug) {
                g2d.setColor(Color.RED);
                g2d.drawRect((int) x, (int) y, getWidth(), getHeight());
            }
        }
    }

    // Load the PNG image
    private void loadImage() {
        try {
            image = ImageIO.read(new File("junk.png"));
        } catch (IOException e) {
            System.err.println("Error loading enemy ship image: " + e.getMessage());
            image = null;
        }
    }

    //public boolean update()
    public void update()
    {
/*    	
        xVel = defaultVel + game.difficulty;
        xPos -= xVel;

        if (scoreable && xPos < tk.getScreenSize().width / 2)
        {
            scoreable = false;
            return true;
        }
        return false;
*/        
        
        
        x += velocityX;
        y += velocityY;

        // Screen wrapping
        if (x < 0) x = tk.getScreenSize().width;
        if (x > tk.getScreenSize().width) x = 0;
        if (y < 0) y = tk.getScreenSize().height;
        if (y > tk.getScreenSize().height) y = 0;
    }
    
    public void boom()
    {
        //yVel -= 5.0;

        new Thread(() ->
        {
            try
            {
                AudioInputStream ais = AudioSystem.getAudioInputStream(new File("explosion.wav").getAbsoluteFile());
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
    
    
 // Get the bounding box for collision detection
   // public Rectangle getBounds() {
     //   return new Rectangle((int) x - size / 2, (int) y - size / 2, size, size);
    //}
// Get the bounds for collision detection

    public int getWidth() {
        return image != null ? image.getWidth() : 0;
    }

    public int getHeight() {
        return image != null ? image.getHeight() : 0;
    }

 public Rectangle getBounds() {
     //int width = image != null ? image.getWidth() : 0;
     //int height = image != null ? image.getHeight() : 0;
     return new Rectangle((int) x, (int) y, getWidth(), getHeight());
 }
    // Set the asteroid as destroyed
    public void setDestroyed(boolean destroyed) {
        this.destroyed = destroyed;
    }

    // Check if the asteroid is destroyed
    public boolean isDestroyed() {
        return destroyed;
    }
    
 // Get the current X position
    public double getX() {
        return x;
    }

    // Get the current Y position
    public double getY() {
        return y;
    }
}
