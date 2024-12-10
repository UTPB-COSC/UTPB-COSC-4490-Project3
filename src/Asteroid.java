package src;

import java.awt.*;
import java.io.File;
import java.util.Random;

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
    }

    public void drawAsteroid(Graphics2D g2d)
    {
/*    	
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(game.pipeImage, xPos, yPos, null);
        g2d.drawImage(game.flippedPipe, xPos, yPos - gap - height, null);
*/        
        
        
        g2d.setColor(Color.GRAY);
        g2d.drawOval((int) x - size / 2, (int) y - size / 2, size, size);
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
    public Rectangle getBounds() {
        return new Rectangle((int) x - size / 2, (int) y - size / 2, size, size);
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
