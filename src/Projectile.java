package src;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

public class Projectile {

    Game game;
    Toolkit tk;

    private double xPos, yPos;
    private double speed;
    private double rotation;
    private static BufferedImage image;
    private final int height = 8;
    private final int width = 24;
    private Point2D[] corners;
    private Object owner;

    public Projectile(double x, double y, double rotation, double speed, Game g, Toolkit tk, Object owner) throws IOException {
        this.xPos = x;
        this.yPos = y;
        this.rotation = rotation;
        this.speed = speed;
        this.owner = owner;
        this.game = g;
        this.tk = tk;

        BufferedImage originalImage = ImageIO.read(new File("125mmProjectile.png"));
        
        this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = this.image.createGraphics();
        g2d.drawImage(originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, null);
        g2d.dispose();
        updateHitbox();
    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        AffineTransform old = g2d.getTransform();
        g2d.rotate(rotation, xPos + width / 2.0, yPos + height / 2.0);
        g2d.drawImage(image, (int)xPos, (int)yPos, null);
        g2d.setTransform(old);
        update();

        // Draw hitbox
        if (game.debug)
        {
            g2d.setColor(Color.RED);
            corners = RotationUtil.getRotatedCorners((int)xPos, (int)yPos, width, height, width, height, rotation);

            // Draw lines between each consecutive corner to form the bounding box
            for (int i = 0; i < corners.length; i++) {
                Point2D start = corners[i];
                Point2D end = corners[(i + 1) % corners.length]; // Ensures the last corner connects to the first
                g2d.drawLine((int) start.getX(), (int) start.getY(), (int) end.getX(), (int) end.getY());
            }
        }
    }

    public int getX() {
        return (int)xPos;
    }

    public int getY() {
        return (int)yPos;
    }

    public Object getOwner() {
        return owner;
    }

    public void update() {
        xPos += speed * Math.cos(rotation);
        yPos += speed * Math.sin(rotation);
        updateHitbox();
    }

    public boolean isOffScreen() {
        return xPos < 0 || yPos < 0 || xPos > tk.getScreenSize().width || yPos > tk.getScreenSize().height;
    }

    private void updateHitbox() {
        corners = RotationUtil.getRotatedCorners((int)xPos, (int)yPos, width - 20, height, width, height, rotation);
    }
}
